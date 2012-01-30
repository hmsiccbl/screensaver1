// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.service.users;

import static edu.harvard.med.screensaver.model.screens.ScreenType.RNAI;
import static edu.harvard.med.screensaver.model.screens.ScreenType.SMALL_MOLECULE;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;

import edu.harvard.med.iccbl.screensaver.policy.DataSharingLevelMapper;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivity;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ChecklistItem;
import edu.harvard.med.screensaver.model.users.ChecklistItemEvent;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.model.users.UserAttachedFileType;
import edu.harvard.med.screensaver.service.OperationRestrictedException;
import edu.harvard.med.screensaver.util.DevelopmentException;
import edu.harvard.med.screensaver.util.NullSafeUtils;
import edu.harvard.med.screensaver.util.Pair;

public class UserAgreementUpdater
{
  static final Map<ScreenType,String> USER_AGREEMENT_ATTACHED_FILE_TYPE = ImmutableMap.of(SMALL_MOLECULE, "2010 ICCB-L/NSRB Small Molecule User Agreement",
                                                                                          RNAI, "ICCB-L/NSRB RNAi User Agreement");
  static final Map<ScreenType,String> USER_AGREEMENT_CHECKLIST_ITEM_NAME = ImmutableMap.of(SMALL_MOLECULE, "Current Small Molecule User Agreement active",
                                                                                           RNAI, "Current RNAi User Agreement active");

  private static Logger log = Logger.getLogger(UserAgreementUpdater.class);
  
  private GenericEntityDAO _dao;
  
  protected UserAgreementUpdater() {}
  
  public UserAgreementUpdater(GenericEntityDAO dao)
  {
    _dao = dao;
  }

  /**
   * This method locates the<br>
   * _not yet expired_ users who have a UA with an activation on or before the date given.
   * 
   * @param showNotifiedItems if set, show items that have already been notified, as indicated by the
   *          sru.lastNotifiedUAChecklistItemEvent being set.
   * @param screenType TODO
   */
  @Transactional 
  public List<Pair<ScreeningRoomUser,ChecklistItemEvent>> findUsersWithOldUserAgreements(LocalDate date,
                                                                                         boolean showNotifiedItems,
                                                                                         ScreenType screenType)
  {
    String checklistItemName = USER_AGREEMENT_CHECKLIST_ITEM_NAME.get(screenType);
    ChecklistItem userAgreementChecklistItem = _dao.findEntityByProperty(ChecklistItem.class, "itemName", checklistItemName);
    if (userAgreementChecklistItem == null) {
      throw new BusinessRuleViolationException("checklist item '" + checklistItemName + "' does not exist");
    }
    String hql = "select distinct(sru) from ScreeningRoomUser as sru inner join sru.checklistItemEvents cie where " +
                  " cie.expiration != ? " +  // do this to limit the set, but not as the final check
                  " and cie.checklistItem = ?  " +
                  "order by sru.lastName, sru.firstName";
    List<ScreeningRoomUser> users = _dao.findEntitiesByHql(ScreeningRoomUser.class, 
                                                           hql,
                                                           /* TODO: see if we eliminate this param */Boolean.TRUE,
                                                           userAgreementChecklistItem);

    List<Pair<ScreeningRoomUser,ChecklistItemEvent>> expiredSet = Lists.newLinkedList();
    for(ScreeningRoomUser user:users)
    {
      log.debug("test: " + user.getFullNameFirstLast());
      ChecklistItemEvent checklistItemEvent = user.getChecklistItemEvents(userAgreementChecklistItem).last();
      if (checklistItemEvent.isExpiration()) {
        if(log.isDebugEnabled())
          log.debug("user is already expired: " + user+ ", date: " + checklistItemEvent.getDatePerformed());
        // TODO: evaluate logic: 
        // we will not check that the _role_ has been removed here, since we are only trying to find the _not yet expired_ users
      }
      else if (checklistItemEvent.getDatePerformed().compareTo(date) > 0) {
        if(log.isDebugEnabled())
          log.debug("remove: " + user + ", expire date too new: " + checklistItemEvent.getDatePerformed());
      }
      else {
        if (!showNotifiedItems && checklistItemEvent.equals(getLastNotifiedUserAgreementChecklistItemEvent(user, screenType))) {
          log.info("Already notified the user for: " + checklistItemEvent);
        }
        else {
          expiredSet.add(Pair.newPair(user,checklistItemEvent));
        }
      }
    }
    return expiredSet;
  }
  
  private ChecklistItemEvent getLastNotifiedUserAgreementChecklistItemEvent(ScreeningRoomUser user, ScreenType screenType)
  {
    switch (screenType) {
      case SMALL_MOLECULE:
        return user.getLastNotifiedSMUAChecklistItemEvent();
      case RNAI:
        return user.getLastNotifiedRNAiUAChecklistItemEvent();
      default:
        throw new DevelopmentException("not implemented for " + screenType + " screen type");
    }
  }

  public void setLastNotifiedUserAgreementChecklistItemEvent(ScreeningRoomUser sru,
                                                             ChecklistItemEvent cie,
                                                             ScreenType screenType)
  {
    switch (screenType) {
      case SMALL_MOLECULE:
        sru.setLastNotifiedSMUAChecklistItemEvent(cie);
        break;
      case RNAI:
        sru.setLastNotifiedRNAiUAChecklistItemEvent(cie);
        break;
      default:
        throw new DevelopmentException("not implemented for RNAi screen type");
    }
    _dao.saveOrUpdateEntity(sru);
  }  
  
  /**
   * <ul>Expire a user, by 
   * <li>creating an &quot;expiration&quot; ChecklistItemEvent 
   * {@link ChecklistItemEvent#createChecklistItemExpirationEvent(LocalDate, AdministratorUser)}
   * for the last ChecklistItemEvent for that user (provided it is not already an expiration).
   * <li>removing the role returned as the {@link DataSharingLevelMapper#getPrimaryDataSharingLevelRoleForUser(ScreenType, ScreensaverUser)}
   * <li>removing the {@link ScreensaverUserRole#SCREENSAVER_USER} role if the user has no other data sharing level roles in place (i.e. for another {@link ScreenType } ).
   * </ul>
   * @return a List of the activities performed.
   */
  @Transactional
  public List<AdministrativeActivity> expireUser(ScreeningRoomUser user, AdministratorUser recordedBy, ScreenType screenType)
  {
    String checklistItemName = USER_AGREEMENT_CHECKLIST_ITEM_NAME.get(screenType);
    ChecklistItem userAgreementChecklistItem = _dao.findEntityByProperty(ChecklistItem.class, "itemName", checklistItemName);
    if (userAgreementChecklistItem == null) {
      throw new BusinessRuleViolationException("checklist item '" + checklistItemName + "' does not exist");
    }

    List<AdministrativeActivity> activitiesPerformed = Lists.newLinkedList();
    user = _dao.reloadEntity(user);
    recordedBy = _dao.reloadEntity(recordedBy);

    verifyOperationPermitted(user, recordedBy);

    SortedSet<ChecklistItemEvent> events = user.getChecklistItemEvents(userAgreementChecklistItem);
    if (events.isEmpty()) {
      // TODO: remove the data sharing role anyway - if they have no checklist items... however, this could be due to a manual admin override
      log.debug("User has no checklist item events: " + user + ", so no action will be taken");
      return activitiesPerformed;
    }
    if (events.last().isExpiration()) {
      log.debug("User's last checklistItemEvent was already expired");
      return activitiesPerformed;
    }
    else {
      ChecklistItemEvent cie = events.last();
      cie.createChecklistItemExpirationEvent(new LocalDate(), recordedBy);
      activitiesPerformed.add(user.createUpdateActivity(recordedBy, 
                                                        "expired '" + USER_AGREEMENT_CHECKLIST_ITEM_NAME + "' " +
                                                          ", checklist item: " + cie.getChecklistItemEventId() +
                                                          ", datePerformed: " + cie.getDatePerformed()));
      removeRole(DataSharingLevelMapper.getPrimaryDataSharingLevelRoleForUser(screenType, user), user, recordedBy, activitiesPerformed);
      if (screenType == ScreenType.SMALL_MOLECULE && !!!user.getScreensaverUserRoles().contains(ScreensaverUserRole.RNAI_DSL_LEVEL3_SHARED_SCREENS)) {
        removeRole(ScreensaverUserRole.SCREENSAVER_USER, user, recordedBy, activitiesPerformed);
      }
      else if (screenType == ScreenType.RNAI && !!!user.getScreensaverUserRoles().contains(ScreensaverUserRole.SM_DSL_LEVEL3_SHARED_SCREENS)) {
        removeRole(ScreensaverUserRole.SCREENSAVER_USER, user, recordedBy, activitiesPerformed);
      }
    }
    return activitiesPerformed;
  }

  private void removeRole(ScreensaverUserRole role,
                          ScreeningRoomUser user,
                          AdministratorUser recordedBy,
                          List<AdministrativeActivity> activitiesPerformed)
  {
    if (user.removeScreensaverUserRole(role)) {
      activitiesPerformed.add(user.createUpdateActivity(recordedBy, 
                                                        "removed \"" + role.getDisplayableRoleName() + "\" role after expiring the '" + USER_AGREEMENT_CHECKLIST_ITEM_NAME + "' checklist item"));
    }
  }

  /**
   * Create a User Data Sharing Checklist Item for the User.<br>
   * Details:
   * <ul>
   * <li>ChecklistItemEvent is active now
   * <li>Remove current Data Sharing role(s) and add the passed in dataSharingRole to the user.
   * <li>Create the attached file containing the UA, referenced to the User.
   * </ul>
   * 
   * @param user
   * @param dataSharingLevelRole
   * @param userAgreementFileName
   * @param userAgreementFileContents
   * @param recordedBy
   * @throws IOException
   */
  @Transactional
  public ScreeningRoomUser updateUser(ScreeningRoomUser user, 
                                      ScreensaverUserRole dataSharingLevelRole,
                                      ScreenType screenType,
                                      String userAgreementFileName,
                                      InputStream userAgreementFileContents, 
                                      AdministratorUser recordedBy) throws IOException
  {
    user = _dao.reloadEntity(user);
    recordedBy = _dao.reloadEntity(recordedBy);

    verifyOperationPermitted(user, recordedBy);

    if (!!!DataSharingLevelMapper.UserDslRoles.get(screenType).contains(dataSharingLevelRole)) {
      throw new BusinessRuleViolationException("data sharing level be must one of " +
        Joiner.on(", ").join(DataSharingLevelMapper.UserDslRoles.get(screenType)));
    }
    
    String checklistItemName = USER_AGREEMENT_CHECKLIST_ITEM_NAME.get(screenType);
    ChecklistItem userAgreementChecklistItem = _dao.findEntityByProperty(ChecklistItem.class, "itemName", checklistItemName);
    if (userAgreementChecklistItem == null) {
      throw new BusinessRuleViolationException("checklist item '" + checklistItemName + "' does not exist");
    }
    SortedSet<ChecklistItemEvent> extantUserAgreementChecklistItemEvents = user.getChecklistItemEvents(userAgreementChecklistItem);
    if (!!!extantUserAgreementChecklistItemEvents.isEmpty() && !!!extantUserAgreementChecklistItemEvents.last().isExpiration()) {
      throw new BusinessRuleViolationException("cannot update the user agreement of a user that already has an active user agreement");
    }

    String userAgreementAttachedFileTypeName = USER_AGREEMENT_ATTACHED_FILE_TYPE.get(screenType);
    UserAttachedFileType userAgreementAttachedFileType = _dao.findEntityByProperty(UserAttachedFileType.class, "value", userAgreementAttachedFileTypeName);
    if (userAgreementAttachedFileType == null) {
      throw new BusinessRuleViolationException("attached file type '" + userAgreementAttachedFileTypeName + "' does not exist");
    }
    
    user.createChecklistItemActivationEvent(userAgreementChecklistItem, new LocalDate(), recordedBy);
    ScreensaverUserRole oldDataSharingLevelRole = DataSharingLevelMapper.getPrimaryDataSharingLevelRoleForUser(screenType, user);

    user.createUpdateActivity(recordedBy, "activated '" + checklistItemName + "' checklist item");

    user.removeScreensaverUserRole(oldDataSharingLevelRole);
    user.addScreensaverUserRole(dataSharingLevelRole);
    if (oldDataSharingLevelRole != dataSharingLevelRole) {
      user.createUpdateActivity(recordedBy, 
                                "updated data sharing level from '" +
                                  (oldDataSharingLevelRole == null ? "<none>"
                                    : oldDataSharingLevelRole.getDisplayableRoleName()) + "' to '" +
                                  dataSharingLevelRole.getDisplayableRoleName() + "'");
    }
      
    if (user.addScreensaverUserRole(ScreensaverUserRole.SCREENSAVER_USER)) {
      user.createUpdateActivity(recordedBy, "added '" + ScreensaverUserRole.SCREENSAVER_USER.getDisplayableRoleName() + "' role");
    }

    user.createAttachedFile(userAgreementFileName,
                            userAgreementAttachedFileType,
                            null,
                            userAgreementFileContents);
    return user;
  }

  private void verifyOperationPermitted(ScreeningRoomUser user,
                                        AdministratorUser recordedBy)
  throws OperationRestrictedException
  {
    if (!!!recordedBy.getScreensaverUserRoles().contains(ScreensaverUserRole.USERS_ADMIN)) {
      throw new OperationRestrictedException("to update a user's user agreement, administrator must have " + ScreensaverUserRole.USERS_ADMIN.getDisplayableRoleName() + " role");
    }
    if (!!!recordedBy.getScreensaverUserRoles().contains(ScreensaverUserRole.USER_ROLES_ADMIN)) {
      throw new OperationRestrictedException("to update a user's user agreement, administrator must have " + ScreensaverUserRole.USER_ROLES_ADMIN.getDisplayableRoleName() + " role");
    }
    if (user.isHeadOfLab()) {
      if (!!!recordedBy.getScreensaverUserRoles().contains(ScreensaverUserRole.LAB_HEADS_ADMIN)) {
        throw new OperationRestrictedException("to update a lab head's user agreement, administrator must have " + ScreensaverUserRole.LAB_HEADS_ADMIN.getDisplayableRoleName() + " role");
      }
    }
  }
  
  /**
   * This is used to get the set of admins that will be notified of actions taken.
   * //TODO: [#2175] Screen Data Sharing And User Agreement Expiration Services 
   */
  public Set<ScreensaverUser> findUserAgreementAdmins()
  {
    String hql = "from ScreensaverUser where ? in elements (screensaverUserRoles)" ;
    Set<ScreensaverUser> admins = Sets.newHashSet(_dao.findEntitiesByHql(ScreensaverUser.class, hql, ScreensaverUserRole.USER_AGREEMENT_EXPIRATION_NOTIFY.getRoleName()));
    return admins;
  }
}
