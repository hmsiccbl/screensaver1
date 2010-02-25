// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.service.users;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Set;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.SchemaUtil;
import edu.harvard.med.screensaver.model.AttachedFileType;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ChecklistItem;
import edu.harvard.med.screensaver.model.users.ChecklistItemGroup;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.model.users.UserAttachedFileType;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.springframework.test.AbstractTransactionalSpringContextTests;

import com.google.common.collect.Sets;

public class UserAgreementUpdaterTest extends AbstractTransactionalSpringContextTests
{
  private static Logger log = Logger.getLogger(UserAgreementUpdaterTest.class);
  
  protected GenericEntityDAO genericEntityDao;
  protected SchemaUtil schemaUtil;
  protected UserAgreementUpdater userAgreementUpdater;


  @Override
  protected String[] getConfigLocations()
  {
    return new String[] { "spring-context-test.xml" };
  }

  public UserAgreementUpdaterTest() 
  {
    setPopulateProtectedVariables(true);
  }
  
  @Override
  protected void onSetUpBeforeTransaction() throws Exception
  {
    schemaUtil.truncateTablesOrCreateSchema();
  }
  
  public void testFindUsersWithOldSMUAgreementsAndExpire() throws IOException
  {
    // create the entities
    AdministratorUser admin = new AdministratorUser("Test", "Admin", "", "", "", "", "", "");
    admin.addScreensaverUserRole(ScreensaverUserRole.USERS_ADMIN);
    admin.addScreensaverUserRole(ScreensaverUserRole.USER_ROLES_ADMIN);
    genericEntityDao.persistEntity(admin);

    // create the UA CLI
    ChecklistItem checklistItem = new ChecklistItem(UserAgreementUpdater.USER_AGREEMENT_CHECKLIST_ITEM_NAME, true, ChecklistItemGroup.FORMS, 0);
    genericEntityDao.persistEntity(checklistItem);

    // create the attachment
    AttachedFileType attachedFileType = new UserAttachedFileType(UserAgreementUpdater.USER_AGREEMENT_ATTACHED_FILE_TYPE);
    genericEntityDao.persistEntity(attachedFileType);

    // create one entity that will be expired
    ScreeningRoomUser user = new ScreeningRoomUser("Test", "User");
    genericEntityDao.persistEntity(user);
    

    // create another entity that will not be expired
    ScreeningRoomUser user2 = new ScreeningRoomUser("Test", "User2");
    genericEntityDao.persistEntity(user2);
    setComplete();
    endTransaction();

    // check that not yet "old" - there are no events yet!
    Set<ScreeningRoomUser> expiredSet = userAgreementUpdater.findUsersWithOldSMAgreements(new LocalDate());
    assertTrue(expiredSet.isEmpty());

    // add a checklist item and event to the user
    startNewTransaction();
    user = genericEntityDao.reloadEntity(user);
    user2 = genericEntityDao.reloadEntity(user2);
    admin = genericEntityDao.reloadEntity(admin);
    InputStream inputStream = new ByteArrayInputStream("contents".getBytes());
    userAgreementUpdater.updateUser(user, ScreensaverUserRole.SM_DSL_LEVEL1_MUTUAL_SCREENS, "user_agreement.pdf", inputStream, admin);

    // set the other user to the future (have to do this manually)
    LocalDate date2 = new LocalDate();
    date2 = date2.plusYears(1);
    user2.createChecklistItemActivationEvent(checklistItem, date2, admin);

    setComplete();
    endTransaction();

    // first find the user with the old event first
    LocalDate findDate = new LocalDate();
    expiredSet = userAgreementUpdater.findUsersWithOldSMAgreements(findDate);
    
    assertFalse(expiredSet.isEmpty());
    assertTrue(expiredSet.contains(user));
    assertFalse(expiredSet.contains(user2));
    assertEquals(1,expiredSet.size());
    
    // now move the date out 
    findDate = findDate.plusYears(2);
    expiredSet = userAgreementUpdater.findUsersWithOldSMAgreements(findDate);
    
    assertFalse(expiredSet.isEmpty());
    assertTrue(expiredSet.contains(user));
    assertTrue(expiredSet.contains(user2));
    assertEquals(2,expiredSet.size());
    
    //TODO: this next section is testing the expiration.  Might want to move to another test...
    // Expire the first user
    userAgreementUpdater.expireUser(user, admin);
    //date2 = date2.plusYears(2);
    // query again, to see if the expired user still shows up
    expiredSet = userAgreementUpdater.findUsersWithOldSMAgreements(findDate);
    
    assertFalse(expiredSet.isEmpty());
    assertFalse(expiredSet.contains(user));
    assertTrue(expiredSet.contains(user2));
    assertEquals(1,expiredSet.size());
    
  }
  
  public void testUserAgreementUpdater() throws IOException, SQLException
  {
    ChecklistItem checklistItem = new ChecklistItem(UserAgreementUpdater.USER_AGREEMENT_CHECKLIST_ITEM_NAME, true, ChecklistItemGroup.FORMS, 0);
    AttachedFileType attachedFileType = new UserAttachedFileType(UserAgreementUpdater.USER_AGREEMENT_ATTACHED_FILE_TYPE);
    ScreeningRoomUser user = new ScreeningRoomUser("Test", "User");
    AdministratorUser admin = new AdministratorUser("Test", "Admin", "", "", "", "", "", "");
    admin.addScreensaverUserRole(ScreensaverUserRole.USERS_ADMIN);
    admin.addScreensaverUserRole(ScreensaverUserRole.USER_ROLES_ADMIN);
    genericEntityDao.persistEntity(checklistItem);
    genericEntityDao.persistEntity(attachedFileType);
    genericEntityDao.persistEntity(user);
    genericEntityDao.persistEntity(admin);
    setComplete();
    endTransaction();
    
    startNewTransaction();
    user = genericEntityDao.reloadEntity(user);
    admin = genericEntityDao.reloadEntity(admin);
    InputStream inputStream = new ByteArrayInputStream("contents".getBytes());
    userAgreementUpdater.updateUser(user, ScreensaverUserRole.SM_DSL_LEVEL1_MUTUAL_SCREENS, "user_agreement.pdf", inputStream, admin);
    setComplete();
    endTransaction();
    
    startNewTransaction();
    user = genericEntityDao.reloadEntity(user);
    checklistItem = genericEntityDao.reloadEntity(checklistItem);
    assertEquals(Sets.newHashSet(ScreensaverUserRole.SM_DSL_LEVEL1_MUTUAL_SCREENS,
                                 ScreensaverUserRole.SM_DSL_LEVEL2_MUTUAL_POSITIVES,
                                 ScreensaverUserRole.SM_DSL_LEVEL3_SHARED_SCREENS,
                                 ScreensaverUserRole.SCREENSAVER_USER), 
                 user.getScreensaverUserRoles());
    assertEquals(checklistItem, user.getChecklistItemEvents().first().getChecklistItem());
    assertEquals("user_agreement.pdf", user.getAttachedFiles().iterator().next().getFilename());
    assertEquals("contents", new String(IOUtils.toByteArray(user.getAttachedFiles().iterator().next().getFileContents().getBinaryStream())));
    setComplete();
    endTransaction();

    startNewTransaction();
    user = genericEntityDao.reloadEntity(user);
    admin = genericEntityDao.reloadEntity(admin);
    inputStream = new ByteArrayInputStream("contents".getBytes());
    try {
      userAgreementUpdater.updateUser(user, ScreensaverUserRole.SM_DSL_LEVEL3_SHARED_SCREENS, "user_agreement.pdf", inputStream, admin);
      fail("expected exception");
    }
    catch (BusinessRuleViolationException e) { 
      assertTrue(e.getMessage().contains("already has an active user agreement")); 
    }
    endTransaction();
    
    startNewTransaction();
    user = genericEntityDao.reloadEntity(user);
    admin = genericEntityDao.reloadEntity(admin);
    checklistItem = genericEntityDao.reloadEntity(checklistItem);
    user.getChecklistItemEvents(checklistItem).last().createChecklistItemExpirationEvent(new LocalDate(), admin);
    setComplete();
    endTransaction();

    startNewTransaction();
    user = genericEntityDao.reloadEntity(user);
    admin = genericEntityDao.reloadEntity(admin);
    inputStream = new ByteArrayInputStream("contents".getBytes());
    userAgreementUpdater.updateUser(user, ScreensaverUserRole.SM_DSL_LEVEL3_SHARED_SCREENS, "user_agreement.pdf", inputStream, admin);
    setComplete();
    endTransaction();
    
    startNewTransaction();
    user = genericEntityDao.reloadEntity(user);
    checklistItem = genericEntityDao.reloadEntity(checklistItem);
    assertEquals(Sets.newHashSet(ScreensaverUserRole.SM_DSL_LEVEL3_SHARED_SCREENS,
                                 ScreensaverUserRole.SCREENSAVER_USER), 
                 user.getScreensaverUserRoles());
    assertEquals(checklistItem, user.getChecklistItemEvents().first().getChecklistItem());
    assertEquals("user_agreement.pdf", user.getAttachedFiles().iterator().next().getFilename());
    assertEquals("contents", new String(IOUtils.toByteArray(user.getAttachedFiles().iterator().next().getFileContents().getBinaryStream())));

  }

}
