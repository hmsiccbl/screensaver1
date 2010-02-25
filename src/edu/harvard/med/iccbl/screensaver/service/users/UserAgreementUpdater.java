// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
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

import edu.harvard.med.iccbl.screensaver.policy.DataSharingLevelMapper;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.AttachedFileType;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ChecklistItem;
import edu.harvard.med.screensaver.model.users.ChecklistItemEvent;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.service.OperationRestrictedException;
import edu.harvard.med.screensaver.util.Pair;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class UserAgreementUpdater
{
  static final String USER_AGREEMENT_ATTACHED_FILE_TYPE = "2009 ICCB-L/NSRB Small Molecule User Agreement";
  static final String USER_AGREEMENT_CHECKLIST_ITEM_NAME = "Current Small Molecule User Agreement active";
  private static Logger log = Logger.getLogger(UserAgreementUpdater.class);
  
  private GenericEntityDAO _dao;
  
  protected UserAgreementUpdater() {}
  
  public UserAgreementUpdater(GenericEntityDAO dao)
  {
    _dao = dao;
  }
  
  @Transactional
  public List<Pair<ScreeningRoomUser,List<AdministrativeActivity>>> findAndUpdateUsersWithExpiredSMUserAgreements(LocalDate date,
                                                                                                          AdministratorUser recordedBy)
  {
    List<Pair<ScreeningRoomUser,List<AdministrativeActivity>>> updates = Lists.newLinkedList();
    for(ScreeningRoomUser user:findUsersWithOldSMAgreements(date))
    {
      updates.add(new Pair<ScreeningRoomUser,List<AdministrativeActivity>>(user, expireUser(user, recordedBy)));
    }
    return updates;
  }
  
  /**
   * This method locates the _not yet expired_ users who have a SMUA with an activation on or before the date given.
   * @param date
   * @return
   */
  @Transactional 
  public Set<ScreeningRoomUser> findUsersWithOldSMAgreements(LocalDate date)
  {
    ChecklistItem userAgreementChecklistItem = _dao.findEntityByProperty(ChecklistItem.class, "itemName", USER_AGREEMENT_CHECKLIST_ITEM_NAME);
    if (userAgreementChecklistItem == null) {
      throw new BusinessRuleViolationException("checklist item '" + USER_AGREEMENT_CHECKLIST_ITEM_NAME + "' does not exist");
    }
    String hql = "select sru from ScreeningRoomUser as sru inner join sru.checklistItemEvents cie where " +
                  " cie.expiration != ? " +  // do this to limit the set, but not as the final check
                  " and cie.checklistItem = ? ";
    Set<ScreeningRoomUser> users = Sets.newHashSet(_dao.findEntitiesByHql(ScreeningRoomUser.class, hql, /*TODO: see if we eliminate this param*/ Boolean.TRUE, userAgreementChecklistItem));

    Set<ScreeningRoomUser> expiredSet = Sets.newHashSet();
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
        expiredSet.add(user);
      }
    }
    return expiredSet;
  }
  
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
      events.last().createChecklistItemExpirationEvent(new LocalDate(), recordedBy);
      activitiesPerformed.add(user.createUpdateActivity(recordedBy, 
                                                        "expired '" + USER_AGREEMENT_CHECKLIST_ITEM_NAME + "' checklist item"));
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
                                                        "removed " + ScreensaverUserRole.SCREENSAVER_USER + " role after expiring the '" + USER_AGREEMENT_CHECKLIST_ITEM_NAME + "' checklist item"));
    }
  }

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
