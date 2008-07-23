// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.screendb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.Query;
import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.AffiliationCategory;
import edu.harvard.med.screensaver.model.users.ChecklistItem;
import edu.harvard.med.screensaver.model.users.ChecklistItemEvent;
import edu.harvard.med.screensaver.model.users.ChecklistItemGroup;
import edu.harvard.med.screensaver.model.users.LabAffiliation;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUserClassification;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

/**
 * Assumes first/last doesn't change, that it is safe to use a business key across the two
 * databases.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class UserSynchronizer
{
  // static members

  private static Logger log = Logger.getLogger(UserSynchronizer.class);
  private static Map<String,ChecklistItem> CHECKLIST_ITEM_TYPE_MAP = new HashMap<String,ChecklistItem>();
  static {
    CHECKLIST_ITEM_TYPE_MAP.put("Added to ICCB screening users list",
                                new ChecklistItem("Added to ICCB-L/NSRB email list", true, ChecklistItemGroup.MAILING_LISTS_AND_WIKIS, 1));
    CHECKLIST_ITEM_TYPE_MAP.put("ID submitted for access to screening room",
                                new ChecklistItem("ID submitted for access to screening room", true, ChecklistItemGroup.MAILING_LISTS_AND_WIKIS, 6));
    CHECKLIST_ITEM_TYPE_MAP.put("Data sharing agreement signed",
                                new ChecklistItem("Data sharing agreement signed", false, ChecklistItemGroup.FORMS, 1));
    CHECKLIST_ITEM_TYPE_MAP.put("ICCB server account set up",
                                new ChecklistItem("Historical - ICCB-L/NSRB server account set up (general account)", true, ChecklistItemGroup.LEGACY, 1));
    CHECKLIST_ITEM_TYPE_MAP.put("ICCB server account requested",
                                new ChecklistItem("Historical - ICCB server account requested", true, ChecklistItemGroup.LEGACY, 2));
  }
  private static final ChecklistItem[] NON_SCREENDB_CHECKLIST_ITEM_TYPE_NAMES = {
    new ChecklistItem("Added to RNAi email list", true, ChecklistItemGroup.MAILING_LISTS_AND_WIKIS, 2),
    new ChecklistItem("Added to Imaging email list", true, ChecklistItemGroup.MAILING_LISTS_AND_WIKIS, 3),
    new ChecklistItem("RNAi wiki access set up", true, ChecklistItemGroup.MAILING_LISTS_AND_WIKIS, 4),
    new ChecklistItem("QPCR wiki access set up", true, ChecklistItemGroup.MAILING_LISTS_AND_WIKIS, 5),
    new ChecklistItem("Non-HMS Biosafety Training Form on File", false, ChecklistItemGroup.FORMS, 2),
    new ChecklistItem("Screener given a copy of 'What Every New Screener Needs to Know'", false, ChecklistItemGroup.FORMS, 3),
    new ChecklistItem("Invited user eCommons account requested", false, ChecklistItemGroup.NON_HARVARD_SCREENERS, 1),
    new ChecklistItem("Temporary ID requested", false, ChecklistItemGroup.NON_HARVARD_SCREENERS, 2),
    new ChecklistItem("ID submitted for C-607 access", false, ChecklistItemGroup.IMAGING, 1),
    new ChecklistItem("Image Xpress Micro Training", false, ChecklistItemGroup.IMAGING, 2),
    new ChecklistItem("CellWoRx training", false, ChecklistItemGroup.IMAGING, 3),
    new ChecklistItem("Opera Training", false, ChecklistItemGroup.IMAGING, 4),
    new ChecklistItem("Image Analysis training", false, ChecklistItemGroup.IMAGING, 5),
    new ChecklistItem("ICCB-L/NSRB image file server access set up", true, ChecklistItemGroup.IMAGING, 6),
    
  };
  
  private static final Map<String,String> ADMIN_INITIALS_TO_ECOMMONS_ID = new HashMap<String,String>();
  private static final String DEFAULT_CHECKLIST_ITEM_ENTERED_BY_ADMIN_INITIALS = "KLS";
  static {
    ADMIN_INITIALS_TO_ECOMMONS_ID.put("CS", "ces6");
    ADMIN_INITIALS_TO_ECOMMONS_ID.put("CES", "ces6");
    ADMIN_INITIALS_TO_ECOMMONS_ID.put("KLS", "kls4");
    ADMIN_INITIALS_TO_ECOMMONS_ID.put("KLR", "kls4");
    ADMIN_INITIALS_TO_ECOMMONS_ID.put("SC", "slc9");
    ADMIN_INITIALS_TO_ECOMMONS_ID.put("SLC", "slc9");
    ADMIN_INITIALS_TO_ECOMMONS_ID.put("SMJ", "smj9");
    ADMIN_INITIALS_TO_ECOMMONS_ID.put("SJ", "smj9");
    ADMIN_INITIALS_TO_ECOMMONS_ID.put("SPR", "sr50");
    ADMIN_INITIALS_TO_ECOMMONS_ID.put("SR", "sr50");
    ADMIN_INITIALS_TO_ECOMMONS_ID.put("DJW", "djw11");
    
//    ADMIN_INITIALS_TO_ECOMMONS_ID.put("DG", ""); // Dara Greenhouse (164)
//    ADMIN_INITIALS_TO_ECOMMONS_ID.put("DH", ""); // Dara Greenhouse? (47)
//    ADMIN_INITIALS_TO_ECOMMONS_ID.put("JVF", ""); // (162)
//    ADMIN_INITIALS_TO_ECOMMONS_ID.put("JF", ""); // (162)
//    ADMIN_INITIALS_TO_ECOMMONS_ID.put("CR", ""); // (16)
//    ADMIN_INITIALS_TO_ECOMMONS_ID.put("NT", ""); // (9)
//    ADMIN_INITIALS_TO_ECOMMONS_ID.put("DAH", ""); // (5)
//    ADMIN_INITIALS_TO_ECOMMONS_ID.put("RK", ""); // (3)
  }


  // instance data members

  private Connection _connection;
  private GenericEntityDAO _dao;

  private Map<Integer,Integer> _screenDBUserIdToLabHeadIdMap = new HashMap<Integer,Integer>();
  private Map<Integer,ScreeningRoomUser> _screenDBUserIdToScreeningRoomUserMap =
    new HashMap<Integer,ScreeningRoomUser>();
  ScreeningRoomUserClassification.UserType _userClassificationUserType =
    new ScreeningRoomUserClassification.UserType();
  private LabAffiliationCategoryMapper _labAffiliationCategoryMapper =
    new LabAffiliationCategoryMapper();
  private ScreenDBSynchronizationException _synchronizationException = null;


  // public constructors and methods

  public UserSynchronizer(Connection connection, GenericEntityDAO dao)
  {
    _connection = connection;
    _dao = dao;
  }

  public void synchronizeUsers() throws ScreenDBSynchronizationException
  {
    _dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        try {
          recreateChecklistItems();
          synchronizeUsersProper();
          connectUsersToLabHeads();
          persistScreeningRoomUsers();
        }
        catch (SQLException e) {
          _synchronizationException = new ScreenDBSynchronizationException(
            "SQL exception synchronizing users: " + e.getMessage(),
            e);
        }
        catch (ScreenDBSynchronizationException e) {
          _synchronizationException = e;
        }
      }

    });
    if (_synchronizationException != null) {
      throw _synchronizationException;
    }
  }

  public ScreeningRoomUser getScreeningRoomUserForScreenDBUserId(Integer userId)
  {
    return _screenDBUserIdToScreeningRoomUserMap.get(userId);
  }


  // private methods

  private void recreateChecklistItems()
  {
    _dao.runQuery(new Query() { 
      public java.util.List execute(org.hibernate.Session session) { 
        int n = session.createQuery("delete from ChecklistItemEvent").executeUpdate();
        log.info("deleted " + n + " ChecklistItemEvent(s)");
        n = session.createQuery("delete from ChecklistItem").executeUpdate();
        log.info("deleted " + n + " ChecklistItem(s)");
        return null;
      }
    });
    _dao.flush(); // force db deletes before inserts
    for (ChecklistItem checklistItem : CHECKLIST_ITEM_TYPE_MAP.values()) {
      _dao.persistEntity(checklistItem);
      log.info("created new checklist item " + checklistItem + ": " + checklistItem.getItemName()); 
    }
    for (ChecklistItem checklistItem : NON_SCREENDB_CHECKLIST_ITEM_TYPE_NAMES) {
      _dao.persistEntity(checklistItem);
      log.info("created new checklist item " + checklistItem + ": " + checklistItem.getItemName()); 
    }
  }

  private void synchronizeUsersProper() throws SQLException, ScreenDBSynchronizationException
  {
    Statement statement = _connection.createStatement();
    ResultSet resultSet = statement.executeQuery("SELECT * FROM users");
    while (resultSet.next()) {
      ScreeningRoomUser user = createOrUpdateUser(resultSet);
      Integer id = resultSet.getInt("id");
      log.info("synchronizing ScreenDB user " + id);
      _screenDBUserIdToLabHeadIdMap.put(id, resultSet.getInt("lab_name"));
      _screenDBUserIdToScreeningRoomUserMap.put(id, user);
      synchronizeChecklistItems(id, user);
    }
  }
 
  private ScreeningRoomUser createOrUpdateUser(ResultSet resultSet) throws SQLException, ScreenDBSynchronizationException
  {
    DateTime dateCreated = ResultSetUtil.getDateTime(resultSet, "date_created");
    String firstName = resultSet.getString("first");
    String lastName = resultSet.getString("last");
    String email = getEmail(resultSet);
    String phone = resultSet.getString("phone");
    String mailingAddress = resultSet.getString("lab_location");
    String comments = resultSet.getString("comments");
    String ecommonsId = getEcommonsId(resultSet);
    if ("".equals(ecommonsId)) {
      ecommonsId = null;
    }
    String harvardId = resultSet.getString("harvard_id");
    LocalDate harvardIdExpirationDate = ResultSetUtil.getDate(resultSet, "harvard_id_exp_date");
    String affiliationName = resultSet.getString("lab_affiliation");
    ScreeningRoomUserClassification classification = getClassification(resultSet);
    boolean isNonScreeningUser = resultSet.getBoolean("non_user");
    boolean isRnaiUser = resultSet.getBoolean("rani_user" /*[sic]*/);
    String comsCrhbaPermitNumber = resultSet.getString("permit_no");
    String comsCrhbaPermitPrincipalInvestigator = resultSet.getString("permit_pi");
    
    ScreeningRoomUser user = getExistingUser(firstName, lastName);
    if (user == null) {
      if (classification == ScreeningRoomUserClassification.PRINCIPAL_INVESTIGATOR) {
        LabAffiliation labAffiliation = getLabAffiliation(affiliationName);
        user = new LabHead(firstName, lastName, email, phone, mailingAddress, comments, ecommonsId, harvardId, labAffiliation);
      } 
      else {
        user = new ScreeningRoomUser(firstName, lastName, email, phone,
                                     mailingAddress, comments, ecommonsId, harvardId, classification);
        // note: in Screensaver data model, only the lab head (whose entity also
        // represents the "lab" as a whole), should have the lab affiliation
        if (affiliationName != null) {
          comments = comments + "\nScreenDB import: user has alternate affiliation from lab affiliation: " + affiliationName;
          log.info("added alternate affiliation '" + affiliationName + "' for non-lab head user " + user.getFullNameLastFirst());  
        }
      }
      user.setDateCreated(dateCreated);
    }
    else {
      user.setDateCreated(dateCreated);
      user.setEmail(email);
      user.setPhone(phone);
      user.setMailingAddress(mailingAddress);
      user.setComments(comments);
      user.setECommonsId(ecommonsId);
      user.setHarvardId(harvardId);
      user.setUserClassification(classification);
    }

    if (!isNonScreeningUser) {
      if (isRnaiUser) {
        user.addScreensaverUserRole(ScreensaverUserRole.RNAI_SCREENER);
      }
      else {
        user.addScreensaverUserRole(ScreensaverUserRole.SMALL_MOLECULE_SCREENER);
      }
    }

    user.setComsCrhbaPermitNumber(comsCrhbaPermitNumber);
    user.setComsCrhbaPermitPrincipalInvestigator(comsCrhbaPermitPrincipalInvestigator);
    user.setHarvardIdExpirationDate(harvardIdExpirationDate);

    return user;
  }

  private LabAffiliation getLabAffiliation(String affiliationName)
  throws ScreenDBSynchronizationException
  {
    if (affiliationName == null || affiliationName.equals("")) {
      return null;
    }
    LabAffiliation labAffiliation = _dao.findEntityById(LabAffiliation.class, affiliationName);
    if (labAffiliation == null) {
      AffiliationCategory affiliationCategory =
        _labAffiliationCategoryMapper.getAffiliationCategoryForLabAffiliation(affiliationName);
      if (affiliationCategory == null) {
        log.error(
          "no affiliation category mapping for affiliation name: " + affiliationName + "\n" +
          "To fix this, you probably want to add an entry into " +
          "resources/edu/harvard/med/screensaver/db/screendb/lab_affiliation_categories.txt");
        return null;
      }
      labAffiliation = new LabAffiliation(
        affiliationName,
        affiliationCategory);
      _dao.saveOrUpdateEntity(labAffiliation);
    }
    return labAffiliation;
  }

  private ScreeningRoomUser getExistingUser(String firstName, String lastName) {
    ScreeningRoomUser user;
    Map<String,Object> nameMap = new HashMap<String,Object>();
    nameMap.put("firstName", firstName);
    nameMap.put("lastName", lastName);
    user = _dao.findEntityByProperties(ScreeningRoomUser.class, nameMap);
    return user;
  }

  private String getEcommonsId(ResultSet resultSet) throws SQLException {
    String eCommonsId = resultSet.getString("ecommons_id");
    if (eCommonsId != null) {
      eCommonsId = eCommonsId.toLowerCase();
    }
    return eCommonsId;
  }

  /**
   * Get the email for the user. Return a fake email with high likelihood of uniqueness, and
   * clearly recognizable as a fake email, if ScreenDB is missing the email.
   */
  private String getEmail(ResultSet resultSet) throws SQLException {
    String email = resultSet.getString("email");
    if (email == null || email.contains("unknown") || email.contains("notknown")) {
      email =
        resultSet.getString("first") + "." +
        resultSet.getString("last") + "@has.missing.email";
    }
    return email;
  }

  private ScreeningRoomUserClassification getClassification(ResultSet resultSet)
  throws SQLException
  {
    String classificationString = resultSet.getString("classification");
    if (classificationString != null && classificationString.equals("PI")) {
      classificationString = "Principal Investigator";
    }
    ScreeningRoomUserClassification classification =
      _userClassificationUserType.getTermForValue(classificationString);
    if (classification == null) {
      classification = ScreeningRoomUserClassification.UNASSIGNED;
    }
    return classification;
  }

  private void synchronizeChecklistItems(Integer screendbUserId, ScreeningRoomUser user) throws SQLException, ScreenDBSynchronizationException
  {
    PreparedStatement statement = _connection.prepareStatement(
      "SELECT * FROM checklist_item AS ci, checklist_item_type AS cit\n" +
      "WHERE ci.user_id = ? AND ci.item_type_id = cit.id");
    statement.setInt(1, screendbUserId);
    ResultSet resultSet = statement.executeQuery();
    while (resultSet.next()) {
      LocalDate activationDate = ResultSetUtil.getDate(resultSet, "activate_date");
      String activationInitials = resultSet.getString("activate_initials");
      LocalDate deactivationDate = ResultSetUtil.getDate(resultSet, "deactivate_date");
      String deactivationInitials = resultSet.getString("deactivate_initials");

      if (activationInitials == null && deactivationInitials == null && activationDate == null && deactivationDate == null) {
        continue;
      }

      StringBuilder comments = new StringBuilder();
      if (activationDate == null) {
        activationDate = new LocalDate(0);
        comments.append("ScreenDB import: activation date missing, setting to ").append(activationDate).append("\n");
      }
      if (deactivationInitials != null && deactivationDate == null) {
        deactivationDate = new LocalDate();
        comments.append("ScreenDB import: deactivation date missing, setting to ").append(deactivationDate).append("\n");
      }
      if (deactivationDate != null && deactivationDate.compareTo(activationDate) < 0) {
        comments.append("ScreenDB import: deactivation date ").append(deactivationDate).append(" is before activation date ").append(activationDate).append("; swapping dates & initials");
        LocalDate oldDeactivationDate = deactivationDate;
        String oldDeactivationInitials = deactivationInitials;
        deactivationDate = activationDate;
        deactivationInitials = activationInitials;
        activationDate = oldDeactivationDate;
        activationInitials = oldDeactivationInitials;
      }

      String screendbChecklistItemName = resultSet.getString("name");
      ChecklistItem checklistItemType =
        getChecklistItemTypeForScreenDBChecklistItemName(screendbChecklistItemName);
      AdministratorUser enteredByAdmin = findAdminUserByInitials(activationInitials);
      if (enteredByAdmin == null) {
        enteredByAdmin = findAdminUserByInitials(DEFAULT_CHECKLIST_ITEM_ENTERED_BY_ADMIN_INITIALS);
        if (activationInitials == null) {
          comments.append("ScreenDB import: activation initials missing; setting to ").append(enteredByAdmin.getFullNameFirstLast()).append("\n");
        }
        else {
          comments.append("ScreenDB import: activation initials '").append(activationInitials).append("' unknown; setting to ").append(enteredByAdmin.getFullNameFirstLast()).append("\n");
        }
      }

      ChecklistItemEvent checklistItemActivation = 
        user.createChecklistItemActivationEvent(checklistItemType, 
                                                activationDate,
                                                new AdministrativeActivity(enteredByAdmin, 
                                                                           activationDate, // best guess 
                                                                           AdministrativeActivityType.CHECKLIST_ITEM_EVENT));
      if (comments.length() > 0) {
        checklistItemActivation.getEntryActivity().setComments(comments.toString());
        log.info(comments);
      }

      if (deactivationDate != null) {
        enteredByAdmin = findAdminUserByInitials(deactivationInitials);
        comments = new StringBuilder();
        if (enteredByAdmin == null) {
          enteredByAdmin = findAdminUserByInitials(DEFAULT_CHECKLIST_ITEM_ENTERED_BY_ADMIN_INITIALS);
          if (activationInitials == null) {
            comments.append("ScreenDB import: deactivation initials missing; setting to ").append(enteredByAdmin.getFullNameFirstLast()).append("\n");
          }
          else {
            comments.append("ScreenDB import: deactivation initials '").append(deactivationInitials).append("' unknown; setting to ").append(enteredByAdmin.getFullNameFirstLast()).append("\n");
          }
        }

        ChecklistItemEvent checklistItemDeactivation = 
          checklistItemActivation.createChecklistItemExpirationEvent(deactivationDate,
                                                                     new AdministrativeActivity(enteredByAdmin, 
                                                                                                deactivationDate, // best guess 
                                                                                                AdministrativeActivityType.CHECKLIST_ITEM_EVENT));
        if (comments.length() > 0) {
          checklistItemDeactivation.getEntryActivity().setComments(comments.toString());
          log.info(comments);
        }
      }
    }
  }

  private AdministratorUser findAdminUserByInitials(String initials)
  {
    if (initials == null) { 
      return null;
    }
    String eCommonsId = ADMIN_INITIALS_TO_ECOMMONS_ID.get(initials.toUpperCase());
    if (eCommonsId == null) {
      return null;
    }
    return 
    _dao.findEntityByProperty(AdministratorUser.class,
                              "ECommonsId",
                              eCommonsId);
  }

  private ChecklistItem getChecklistItemTypeForScreenDBChecklistItemName(String screendbChecklistItemName) throws ScreenDBSynchronizationException {
    ChecklistItem checklistItem = CHECKLIST_ITEM_TYPE_MAP.get(screendbChecklistItemName);
    if (checklistItem == null) {
      throw new ScreenDBSynchronizationException("couldn't find Screensaver ChecklistItem corresponding to ScreenDB checklist item \"" +
                                                 screendbChecklistItemName + "\"");
    }
    return checklistItem;
  }

  private void connectUsersToLabHeads()
  {
    for (Integer memberId : _screenDBUserIdToLabHeadIdMap.keySet()) {
      ScreeningRoomUser member = _screenDBUserIdToScreeningRoomUserMap.get(memberId);
      log.info("setting lab for ScreenDB user " + memberId);
      Integer headId = _screenDBUserIdToLabHeadIdMap.get(memberId);
      if (member.getUserClassification() == ScreeningRoomUserClassification.PRINCIPAL_INVESTIGATOR) {
        if (headId != null && headId != 0) {
          throw new ScreenDBSynchronizationException("lab head " + memberId + " is not allowed to have a lab head (" + headId + ")");
        }
        continue;
      }
      if (headId == null || headId == 0) {
        member.setLab(null);
      }
      else {
        ScreeningRoomUser head = _screenDBUserIdToScreeningRoomUserMap.get(headId);
        if (!(member instanceof LabHead)) {
          if (head != member) {
            if (!(head instanceof LabHead)) {
              throw new ScreenDBSynchronizationException("lab member " + memberId + " has lab head " + headId + " that is not a PI");
            }
            member.setLab(head.getLab());
          }
        }
      }
    }
  }

  private void persistScreeningRoomUsers()
  {
    for (ScreeningRoomUser user : _screenDBUserIdToScreeningRoomUserMap.values()) {
      _dao.saveOrUpdateEntity(user);
    }
  }
}

