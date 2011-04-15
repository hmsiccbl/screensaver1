// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.service.users;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;

import edu.harvard.med.iccbl.screensaver.policy.DataSharingLevelMapper;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.AttachedFileType;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ChecklistItem;
import edu.harvard.med.screensaver.model.users.ChecklistItemEvent;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.service.OperationRestrictedException;
import edu.harvard.med.screensaver.util.Pair;

public class UserAgreementUpdater
{
  static final String USER_AGREEMENT_ATTACHED_FILE_TYPE = "2010 ICCB-L/NSRB Small Molecule User Agreement";
  static final String USER_AGREEMENT_CHECKLIST_ITEM_NAME = "Current Small Molecule User Agreement active";
  private static Logger log = Logger.getLogger(UserAgreementUpdater.class);
  
  private GenericEntityDAO _dao;
  
  protected UserAgreementUpdater() {}
  
  public UserAgreementUpdater(GenericEntityDAO dao)
  {
    _dao = dao;
  }
  
  /**
   * This method locates the<br>
   * _not yet expired_ users who have a SMUA with an activation on or before the date given.
   * @param showNotifiedItems if set, show items that have already been notified, as indicated by the
   * sru.lastNotifiedSMUAChecklistItemEvent being set.
   */
  @Transactional 
  public List<Pair<ScreeningRoomUser, ChecklistItemEvent>> findUsersWithOldSMAgreements(LocalDate date, boolean showNotifiedItems)
  {
    ChecklistItem userAgreementChecklistItem = _dao.findEntityByProperty(ChecklistItem.class, "itemName", USER_AGREEMENT_CHECKLIST_ITEM_NAME);
    if (userAgreementChecklistItem == null) {
      throw new BusinessRuleViolationException("checklist item '" + USER_AGREEMENT_CHECKLIST_ITEM_NAME + "' does not exist");
    }
    String hql = "select distinct(sru) from ScreeningRoomUser as sru inner join sru.checklistItemEvents cie where " +
                  " cie.expiration != ? " +  // do this to limit the set, but not as the final check
                  " and cie.checklistItem = ?  " +
                  //                  "and ( sru.lastNotifiedSMUAChecklistItemEvent is null or sru.lastNotifiedSMUAChecklistItemEvent <> cie ) " +
                  "order by sru.lastName, sru.firstName";
    List<ScreeningRoomUser> users = _dao.findEntitiesByHql(ScreeningRoomUser.class, 
                                                                          hql, /*TODO: see if we eliminate this param*/ Boolean.TRUE, 
                                                                          userAgreementChecklistItem);

    List<Pair<ScreeningRoomUser,ChecklistItemEvent>> expiredSet = Lists.newLinkedList();
    for(ScreeningRoomUser user:users)
    {
      log.debug("test: " + user);
      ChecklistItemEvent checklistItemEvent = user.getChecklistItemEvents(userAgreementChecklistItem).last();
      if( checklistItemEvent.isExpiration() )
      {
        if(log.isDebugEnabled())
          log.debug("user is already expired: " + user+ ", date: " + checklistItemEvent.getDatePerformed());
        // TODO: evaluate logic: 
        // we will not check that the _role_ has been removed here, since we are only trying to find the _not yet expired_ users
      }
      else if(checklistItemEvent.getDatePerformed().compareTo(date) > 0 )
      {
        if(log.isDebugEnabled())
          log.debug("remove: " + user + ", expire date too new: " + checklistItemEvent.getDatePerformed());
      }else{
        if(!showNotifiedItems 
          && checklistItemEvent.equals(user.getLastNotifiedSMUAChecklistItemEvent()))
        {
          log.info("Already notified the user for: " + checklistItemEvent);
        }
        else
        {
          expiredSet.add(Pair.newPair(user,checklistItemEvent));
        }
      }
    }
    return expiredSet;
  }
  
  public void setLastNotifiedSMUAChecklistItemEvent(ScreeningRoomUser sru, ChecklistItemEvent cie)
  {
    sru.setLastNotifiedSMUAChecklistItemEvent(cie);
    _dao.saveOrUpdateEntity(sru);
  }  
  /**
   * <ul>Expire a user, by 
   * <li>creating an &quot;expiration&quot; ChecklistItemEvent 
   * {@link ChecklistItemEvent#createChecklistItemExpirationEvent(LocalDate, AdministratorUser)}
   * for the last ChecklistItemEvent for that user (provided it is not already an expiration).
   * <li>removing the role returned as the {@link UserAgreementUpdater#findPrimaryDataSharingLevelRole(ScreeningRoomUser)}
   * <li>removing the {@link ScreensaverUserRole#SCREENSAVER_USER} role if the user is not also a {@link ScreensaverUserRole#RNAI_SCREENS} user.
   * </ul>
   * @return a List of the activities performed.
   */
  @Transactional
  public List<AdministrativeActivity> expireUser(ScreeningRoomUser user, AdministratorUser recordedBy)
  {
    ChecklistItem userAgreementChecklistItem = _dao.findEntityByProperty(ChecklistItem.class, "itemName", USER_AGREEMENT_CHECKLIST_ITEM_NAME);
    if (userAgreementChecklistItem == null) {
      throw new BusinessRuleViolationException("checklist item '" + USER_AGREEMENT_CHECKLIST_ITEM_NAME + "' does not exist");
    }

    List<AdministrativeActivity> activitiesPerformed = Lists.newLinkedList();
    user = _dao.reloadEntity(user);
    recordedBy = _dao.reloadEntity(recordedBy);

    verifyOperationPermitted(user, recordedBy);

    SortedSet<ChecklistItemEvent> events = user.getChecklistItemEvents(userAgreementChecklistItem);
    if(events.isEmpty())
    {
      // TODO: remove the data sharing role anyway - if they have no checklist items... however, this could be due to a manual admin override
      log.debug("User has no checklist item events: " + user + ", so no action will be taken");
      return activitiesPerformed;
    }
    if(events.last().isExpiration())
    {
      log.debug("User's last checklistItemEvent was already expired");
      return activitiesPerformed;
    }else{
      ChecklistItemEvent cie = events.last();
      cie.createChecklistItemExpirationEvent(new LocalDate(), recordedBy);
      activitiesPerformed.add(user.createUpdateActivity(recordedBy, 
                                                        "expired '" + USER_AGREEMENT_CHECKLIST_ITEM_NAME + "' " +
                                                        		", checklist item: " + cie.getChecklistItemEventId() + 
                                                        		", datePerformed: " + cie.getDatePerformed()));
      removeRole(findPrimaryDataSharingLevelRole(user), user, recordedBy, activitiesPerformed);
      if (!!!user.getScreensaverUserRoles().contains(ScreensaverUserRole.RNAI_SCREENS)) {
        removeRole(ScreensaverUserRole.SCREENSAVER_USER, user, recordedBy, activitiesPerformed);
      }
    }
    return activitiesPerformed;
  }

  private ScreensaverUserRole findPrimaryDataSharingLevelRole(ScreeningRoomUser user)
  {
    assert Sets.intersection(user.getPrimaryScreensaverUserRoles(), DataSharingLevelMapper.UserSmDslRoles).size() == 1;
    return Sets.intersection(user.getPrimaryScreensaverUserRoles(), DataSharingLevelMapper.UserSmDslRoles).iterator().next();
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
   * Create a Small Molecule User Data Sharing Checklist Item for the User.<br>
   * Details:
   * <ul>
   * <li>ChecklistItemEvent is active now
   * <li>Remove current Data Sharing role(s) and add the passed in dataSharingRole to the user. 
   * {@link UserAgreementUpdater#getCurrentDataSharingLevelRoleName(ScreeningRoomUser)}
   * <li>Create the attached file containing the SMUA, referenced to the User.
   * </ul>
   * @param user
   * @param dataSharingLevelRole
   * @param userAgreementFileName
   * @param userAgreementFileContents
   * @param recordedBy
   * @return
   * @throws IOException
   */
  @Transactional
  public ScreeningRoomUser updateUser(ScreeningRoomUser user, 
                                      ScreensaverUserRole dataSharingLevelRole, 
                                      String userAgreementFileName, 
                                      InputStream userAgreementFileContents, 
                                      AdministratorUser recordedBy) throws IOException
  {
    user = _dao.reloadEntity(user);
    recordedBy = _dao.reloadEntity(recordedBy);

    verifyOperationPermitted(user, recordedBy);

    if (!!!DataSharingLevelMapper.UserSmDslRoles.contains(dataSharingLevelRole)) {
      throw new BusinessRuleViolationException("data sharing level be must one of " + Joiner.on(", ").join(DataSharingLevelMapper.UserSmDslRoles));
    }
    
    ChecklistItem userAgreementChecklistItem = _dao.findEntityByProperty(ChecklistItem.class, "itemName", USER_AGREEMENT_CHECKLIST_ITEM_NAME);
    if (userAgreementChecklistItem == null) {
      throw new BusinessRuleViolationException("checklist item '" + USER_AGREEMENT_CHECKLIST_ITEM_NAME + "' does not exist");
    }
    SortedSet<ChecklistItemEvent> extantUserAgreementChecklistItemEvents = user.getChecklistItemEvents(userAgreementChecklistItem);
    if (!!!extantUserAgreementChecklistItemEvents.isEmpty() && !!!extantUserAgreementChecklistItemEvents.last().isExpiration()) {
      throw new BusinessRuleViolationException("cannot update the user agreement of a user that already has an active user agreement");
    }

    AttachedFileType userAgreementAttachedFileType = _dao.findEntityByProperty(AttachedFileType.class, "value", USER_AGREEMENT_ATTACHED_FILE_TYPE);
    if (userAgreementAttachedFileType == null) {
      throw new BusinessRuleViolationException("attached file type '" + USER_AGREEMENT_ATTACHED_FILE_TYPE + "' does not exist");
    }
    
    user.createChecklistItemActivationEvent(userAgreementChecklistItem, new LocalDate(), recordedBy);
    String oldDataSharingLevelRoleName = getCurrentDataSharingLevelRoleName(user);
    ScreensaverUserRole oldDataSharingLevelRole = getCurrentDataSharingLevelRole(user);

    user.createUpdateActivity(recordedBy, 
                              "activated '" + USER_AGREEMENT_CHECKLIST_ITEM_NAME + "' checklist item");

    user.removeScreensaverUserRole(oldDataSharingLevelRole);
    user.addScreensaverUserRole(dataSharingLevelRole);
    if (oldDataSharingLevelRole != dataSharingLevelRole) {
      user.createUpdateActivity(recordedBy, 
                                "updated data sharing level from '" + oldDataSharingLevelRoleName + "' to '" + dataSharingLevelRole.getDisplayableRoleName() + "'");
    }
      
    if (user.addScreensaverUserRole(ScreensaverUserRole.SCREENSAVER_USER)) {
      user.createUpdateActivity(recordedBy, "added '" + ScreensaverUserRole.SCREENSAVER_USER.getDisplayableRoleName() + "' role");
    }

    user.createAttachedFile(userAgreementFileName,
                            userAgreementAttachedFileType,
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
    Set<ScreensaverUser> admins = Sets.newHashSet(_dao.findEntitiesByHql(ScreensaverUser.class, hql, ScreensaverUserRole.USERS_ADMIN.getRoleName()));
    admins.addAll(Sets.newHashSet(_dao.findEntitiesByHql(ScreensaverUser.class, hql, ScreensaverUserRole.USER_ROLES_ADMIN.getRoleName())));
    admins.addAll(Sets.newHashSet(_dao.findEntitiesByHql(ScreensaverUser.class, hql, ScreensaverUserRole.LAB_HEADS_ADMIN.getRoleName())));
    return admins;
  }
  
  public static ScreensaverUserRole getCurrentDataSharingLevelRole(ScreeningRoomUser user)
  {
    TreeSet<ScreensaverUserRole> userSmDslRoles = Sets.newTreeSet(Sets.intersection(user.getScreensaverUserRoles(), DataSharingLevelMapper.UserSmDslRoles));
    ScreensaverUserRole oldDataSharingLevelRole = userSmDslRoles.isEmpty() ? null : userSmDslRoles.last();
    return oldDataSharingLevelRole;
  }

  public static String getCurrentDataSharingLevelRoleName(ScreeningRoomUser user)
  {
    ScreensaverUserRole currentDataSharingLevelRole = edu.harvard.med.iccbl.screensaver.service.users.UserAgreementUpdater.getCurrentDataSharingLevelRole((ScreeningRoomUser) user);
    return currentDataSharingLevelRole == null ? "<none>" : currentDataSharingLevelRole.getDisplayableRoleName();
  }
}
