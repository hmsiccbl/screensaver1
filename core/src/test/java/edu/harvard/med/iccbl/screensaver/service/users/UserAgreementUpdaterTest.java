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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import edu.harvard.med.screensaver.model.AttachedFileType;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ChecklistItem;
import edu.harvard.med.screensaver.model.users.ChecklistItemEvent;
import edu.harvard.med.screensaver.model.users.ChecklistItemGroup;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.model.users.UserAttachedFileType;
import edu.harvard.med.screensaver.test.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.util.Pair;

@Transactional
public class UserAgreementUpdaterTest extends AbstractSpringPersistenceTest
{
  private static Logger log = Logger.getLogger(UserAgreementUpdaterTest.class);
  
  @Autowired
  protected UserAgreementUpdater userAgreementUpdater;

  AdministratorUser admin = null;
  AdministratorUser otherUserAgreementAdmin = null;
  AdministratorUser nonAdminUser = null;
  
  private void initializeData()
  {
    admin = new AdministratorUser("admin", "testaccount");
    admin.addScreensaverUserRole(ScreensaverUserRole.USER_ROLES_ADMIN);
    admin.addScreensaverUserRole(ScreensaverUserRole.LAB_HEADS_ADMIN);
    admin.addScreensaverUserRole(ScreensaverUserRole.USER_AGREEMENT_EXPIRATION_NOTIFY);
    genericEntityDao.persistEntity(admin);

    otherUserAgreementAdmin = new AdministratorUser("userAgreementAdmin", "testaccount");
    otherUserAgreementAdmin.addScreensaverUserRole(ScreensaverUserRole.USER_AGREEMENT_EXPIRATION_NOTIFY);
    genericEntityDao.persistEntity(otherUserAgreementAdmin);

    nonAdminUser = new AdministratorUser("nonAdminUser", "testaccount");
    nonAdminUser.addScreensaverUserRole(ScreensaverUserRole.USER_AGREEMENT_EXPIRATION_NOTIFY);
    genericEntityDao.persistEntity(nonAdminUser);
  }
  
  /**
   * Same as testFindUsersWithOldSMUAgreementsAndExpire except don't expire them, useful for testing subsequently from 
   * the command line.
   * @throws IOException
   */
  public void testFindUsersWithOldSMUAgreements() throws IOException
  {
    initializeData();

    // create the UA CLI
    ChecklistItem checklistItem = new ChecklistItem(UserAgreementUpdater.USER_AGREEMENT_CHECKLIST_ITEM_NAME.get(SMALL_MOLECULE),
                                                    true, ChecklistItemGroup.FORMS, 0);
    genericEntityDao.persistEntity(checklistItem);

    // create the attachment
    AttachedFileType attachedFileType = new UserAttachedFileType(UserAgreementUpdater.USER_AGREEMENT_ATTACHED_FILE_TYPE.get(SMALL_MOLECULE));
    genericEntityDao.persistEntity(attachedFileType);

    // create one entity that will be dated now (default)
    ScreeningRoomUser user1 = new ScreeningRoomUser("expired", "User");
    user1.setLoginId("user1");
    user1.setEmail("expireduser@screensaver.med.harvard.edu");
    user1.addScreensaverUserRole(ScreensaverUserRole.SM_DSL_LEVEL1_MUTUAL_SCREENS);
    genericEntityDao.persistEntity(user1);
    
    // create another entity that will be dated two years ago
    ScreeningRoomUser userExpired2YearsAgo = new ScreeningRoomUser("2yearsago", "User");
    userExpired2YearsAgo.setEmail("userExpiredNow@screensaver.med.harvard.edu");
    userExpired2YearsAgo.setLoginId("2yrsold");
    userExpired2YearsAgo.addScreensaverUserRole(ScreensaverUserRole.SM_DSL_LEVEL1_MUTUAL_SCREENS);
    genericEntityDao.persistEntity(userExpired2YearsAgo);
    

    // create another entity that will not be expired
    ScreeningRoomUser user2 = new ScreeningRoomUser("not_expired", "User2");
    user2.setEmail("notexpired@screensaver.med.harvard.edu");
    user2.setLoginId("user2");
    user2.addScreensaverUserRole(ScreensaverUserRole.SM_DSL_LEVEL1_MUTUAL_SCREENS);
    genericEntityDao.persistEntity(user2);
    
    // create an expired user, but remove her roles, she should not then be notified
    ScreeningRoomUser user3 = new ScreeningRoomUser("expired, no roles", "User3");
    user3.setEmail("notexpired@screensaver.med.harvard.edu");
    user3.setLoginId("user3");
    //user2.addScreensaverUserRole(ScreensaverUserRole.SM_DSL_LEVEL1_MUTUAL_SCREENS);
    genericEntityDao.persistEntity(user3);
    

    flushAndClear();

    // check that not yet "old" - there are no events yet!
    List<Pair<ScreeningRoomUser,ChecklistItemEvent>> expiredSet = userAgreementUpdater.findUsersWithOldUserAgreements(new LocalDate(), false, SMALL_MOLECULE);
    assertTrue(expiredSet.isEmpty());

    flushAndClear();

    // add a checklist item and event to the user
    user1 = genericEntityDao.reloadEntity(user1);
    userExpired2YearsAgo = genericEntityDao.reloadEntity(userExpired2YearsAgo);
    user2 = genericEntityDao.reloadEntity(user2);
    user3 = genericEntityDao.reloadEntity(user3);
    admin = genericEntityDao.reloadEntity(admin);
    
    InputStream inputStream = new ByteArrayInputStream("contents".getBytes());
    userAgreementUpdater.updateUser(user1, 
                                    ScreensaverUserRole.SM_DSL_LEVEL1_MUTUAL_SCREENS, 
                                    SMALL_MOLECULE,
                                    "user_agreement.pdf", 
                                    inputStream, admin);

    // set the other user to the PAST (have to do this manually)
    // Note: this doesn't actually create the attachment
    LocalDate date1 = new LocalDate();
    date1 = date1.minusYears(2).plusDays(14);
    userExpired2YearsAgo.createChecklistItemActivationEvent(checklistItem, date1, admin);
    
    
    // set the other user to the future (have to do this manually)
    LocalDate date2 = new LocalDate();
    date2 = date2.plusYears(1);
    ChecklistItemEvent cieToBeNotified = user2.createChecklistItemActivationEvent(checklistItem, date2, admin);
    ChecklistItemEvent cieToBeDeactivated = user3.createChecklistItemActivationEvent(checklistItem, date1, admin);
    cieToBeDeactivated.createChecklistItemExpirationEvent(new LocalDate(), admin);

    flushAndClear();

    user1 = genericEntityDao.reloadEntity(user1);
    user2 = genericEntityDao.reloadEntity(user2);
    user3 = genericEntityDao.reloadEntity(user3);
    userExpired2YearsAgo = genericEntityDao.reloadEntity(userExpired2YearsAgo);
    
    // first find the user with the old event first
    LocalDate findDate = new LocalDate();
    expiredSet = userAgreementUpdater.findUsersWithOldUserAgreements(findDate, false, SMALL_MOLECULE);
    
    assertFalse("expiredSet is empty", expiredSet.isEmpty());
    assertTrue(contains(user1, expiredSet));
    assertTrue(contains(userExpired2YearsAgo, expiredSet));
    assertFalse(contains(user2, expiredSet));
    assertFalse(contains(user3, expiredSet));
    assertEquals(2,expiredSet.size());
    
    // now move the date out 
    findDate = findDate.plusYears(2);
    expiredSet = userAgreementUpdater.findUsersWithOldUserAgreements(findDate, false, SMALL_MOLECULE);
    
    assertFalse(expiredSet.isEmpty());
    assertTrue(contains(user1, expiredSet));
    assertTrue(contains(userExpired2YearsAgo, expiredSet));
    assertTrue(contains(user2, expiredSet));
    assertFalse(contains(user3, expiredSet));
    assertEquals(3,expiredSet.size());
    
    // set the notification flag on the SRU
    user2.setLastNotifiedSMUAChecklistItemEvent(cieToBeNotified);
    genericEntityDao.saveOrUpdateEntity(user2);
    
    flushAndClear();

    // Now find again, not that only user 1 is found
    
    expiredSet = userAgreementUpdater.findUsersWithOldUserAgreements(findDate, false, SMALL_MOLECULE);
    
    assertFalse(expiredSet.isEmpty());
    assertTrue(contains(user1, expiredSet));
    assertFalse(contains(user2, expiredSet));
  }
  
  private boolean contains(ScreeningRoomUser user, List<Pair<ScreeningRoomUser, ChecklistItemEvent>> list)
  {
    for(Pair<ScreeningRoomUser, ChecklistItemEvent> pair: list) if(user.equals(pair.getFirst())) return true;
    return false;
  }
  
	public void testFindUsersWithOldSMUAgreementsAndExpire() throws IOException {
		initializeData();

		// create the UA CLI
		ChecklistItem checklistItem = new ChecklistItem(
				UserAgreementUpdater.USER_AGREEMENT_CHECKLIST_ITEM_NAME.get(SMALL_MOLECULE), true, ChecklistItemGroup.FORMS, 0);
		genericEntityDao.persistEntity(checklistItem);
		ChecklistItem checklistItemRNAi = new ChecklistItem(
				UserAgreementUpdater.USER_AGREEMENT_CHECKLIST_ITEM_NAME.get(RNAI), true, ChecklistItemGroup.FORMS, 1);
		genericEntityDao.persistEntity(checklistItemRNAi);

		// create the attachment
		AttachedFileType attachedFileType = new UserAttachedFileType(
				UserAgreementUpdater.USER_AGREEMENT_ATTACHED_FILE_TYPE.get(SMALL_MOLECULE));
		genericEntityDao.persistEntity(attachedFileType);		
		AttachedFileType attachedFileTypeRNAI = new UserAttachedFileType(
				UserAgreementUpdater.USER_AGREEMENT_ATTACHED_FILE_TYPE.get(RNAI));
		genericEntityDao.persistEntity(attachedFileTypeRNAI);

		// create one entity that will be expired
		ScreeningRoomUser user = new ScreeningRoomUser("Test", "User");
		user.setEmail("testuser1@screensaver.med.harvard.edu");
		genericEntityDao.persistEntity(user);
		
		// create another user that will be expired, but will still have a screensaverUserLogin role, because we are going to add RNAi levels to them
		ScreeningRoomUser user1a = new ScreeningRoomUser("Test", "User1a");
		user1a.setEmail("testuser1@screensaver.med.harvard.edu");
		genericEntityDao.persistEntity(user1a);

		// create another entity that will not be expired
		ScreeningRoomUser user2 = new ScreeningRoomUser("Test", "User2");
		user2.setEmail("testuser2@screensaver.med.harvard.edu");
		genericEntityDao.persistEntity(user2);

		flushAndClear();

		// check that not yet "old" - there are no events yet!
		List<Pair<ScreeningRoomUser, ChecklistItemEvent>> expiredSet = userAgreementUpdater.findUsersWithOldUserAgreements(
				new LocalDate(), true, SMALL_MOLECULE);
		assertTrue(expiredSet.isEmpty());

		flushAndClear();

		// add a checklist item and event to the user
		user = genericEntityDao.reloadEntity(user);
		user1a = genericEntityDao.reloadEntity(user1a);
		user2 = genericEntityDao.reloadEntity(user2);
		admin = genericEntityDao.reloadEntity(admin);
		InputStream inputStream = new ByteArrayInputStream("contents".getBytes());
		userAgreementUpdater.updateUser(user, ScreensaverUserRole.SM_DSL_LEVEL1_MUTUAL_SCREENS, SMALL_MOLECULE,
				"user_agreement.pdf", inputStream, admin);
		userAgreementUpdater.updateUser(user1a, ScreensaverUserRole.SM_DSL_LEVEL1_MUTUAL_SCREENS, SMALL_MOLECULE,
				"user_agreement.pdf", inputStream, admin);
		userAgreementUpdater.updateUser(user1a, ScreensaverUserRole.RNAI_DSL_LEVEL1_MUTUAL_SCREENS, RNAI,
				"user_agreement.pdf", inputStream, admin);  // add the RNAI role to this user, to test whether "small molecule" expiring a user who has RNAi roles, will leave the ScreensaverUserLogin role alone

		// set the other user to the future (have to do this manually)
		LocalDate date2 = new LocalDate();
		date2 = date2.plusYears(1);
		user2.createChecklistItemActivationEvent(checklistItem, date2, admin);

		flushAndClear();

		// first find the user with the old event first
		LocalDate findDate = new LocalDate();
		expiredSet = userAgreementUpdater.findUsersWithOldUserAgreements(findDate, true, SMALL_MOLECULE);

		assertFalse(expiredSet.isEmpty());
		assertTrue(contains(user, expiredSet));
		assertTrue(contains(user1a, expiredSet));
		assertFalse(contains(user2, expiredSet));
		assertEquals(2, expiredSet.size());

		// now move the date out
		findDate = findDate.plusYears(2);
		expiredSet = userAgreementUpdater.findUsersWithOldUserAgreements(findDate, true, SMALL_MOLECULE);

		assertFalse(expiredSet.isEmpty());
		assertTrue(contains(user, expiredSet));
		assertTrue(contains(user1a, expiredSet));
		assertTrue(contains(user2, expiredSet));
		assertEquals(3, expiredSet.size());

		// TODO: this next section is testing the expiration.

		// Expire the first user
		userAgreementUpdater.expireUser(user, admin, SMALL_MOLECULE);
		userAgreementUpdater.expireUser(user1a, admin, SMALL_MOLECULE);
		// query again, to see if the expired user still shows up
		expiredSet = userAgreementUpdater.findUsersWithOldUserAgreements(findDate, true, SMALL_MOLECULE);

		assertFalse(expiredSet.isEmpty());
		assertFalse(contains(user, expiredSet));
		assertFalse(contains(user1a, expiredSet));
		assertTrue(contains(user2, expiredSet));
		assertEquals(1, expiredSet.size());

		// find out if user1a still has the login role
		flushAndClear();
		user = genericEntityDao.reloadEntity(user);
		user1a = genericEntityDao.reloadEntity(user1a);
		assertTrue(user1a.getScreensaverUserRoles().contains(ScreensaverUserRole.SCREENSAVER_USER));
		assertFalse(user.getScreensaverUserRoles().contains(ScreensaverUserRole.SCREENSAVER_USER));
	}
  
  public void testUserAgreementUpdater() throws IOException, SQLException
  {
    initializeData();

    ChecklistItem checklistItem = new ChecklistItem(UserAgreementUpdater.USER_AGREEMENT_CHECKLIST_ITEM_NAME.get(SMALL_MOLECULE), true, ChecklistItemGroup.FORMS, 0);
    AttachedFileType attachedFileType = new UserAttachedFileType(UserAgreementUpdater.USER_AGREEMENT_ATTACHED_FILE_TYPE.get(SMALL_MOLECULE));
    ScreeningRoomUser user = new ScreeningRoomUser("Test", "User");
    genericEntityDao.persistEntity(checklistItem);
    genericEntityDao.persistEntity(attachedFileType);
    genericEntityDao.persistEntity(user);
    genericEntityDao.persistEntity(admin);
    
    flushAndClear();

    user = genericEntityDao.reloadEntity(user);
    admin = genericEntityDao.reloadEntity(admin);
    InputStream inputStream = new ByteArrayInputStream("contents".getBytes());
    userAgreementUpdater.updateUser(user, ScreensaverUserRole.SM_DSL_LEVEL1_MUTUAL_SCREENS, SMALL_MOLECULE, "user_agreement.pdf", inputStream, admin);

    flushAndClear();
    
    user = genericEntityDao.reloadEntity(user);
    checklistItem = genericEntityDao.reloadEntity(checklistItem);
    assertEquals(Sets.newHashSet(ScreensaverUserRole.SM_DSL_LEVEL1_MUTUAL_SCREENS,
                                 ScreensaverUserRole.SM_DSL_LEVEL2_MUTUAL_POSITIVES,
                                 ScreensaverUserRole.SM_DSL_LEVEL3_SHARED_SCREENS,
                                 ScreensaverUserRole.SCREENSAVER_USER), 
                 user.getScreensaverUserRoles());
    assertEquals(checklistItem, user.getChecklistItemEvents().first().getChecklistItem());
    assertEquals("user_agreement.pdf", user.getAttachedFiles().iterator().next().getFilename());
    assertEquals("contents", IOUtils.toString(user.getAttachedFiles().iterator().next().getFileContents()));

    flushAndClear();

    user = genericEntityDao.reloadEntity(user);
    admin = genericEntityDao.reloadEntity(admin);
    inputStream = new ByteArrayInputStream("contents".getBytes());
    try {
      userAgreementUpdater.updateUser(user, ScreensaverUserRole.SM_DSL_LEVEL3_SHARED_SCREENS, SMALL_MOLECULE, "user_agreement.pdf", inputStream, admin);
      fail("expected exception");
    }
    catch (BusinessRuleViolationException e) { 
      assertTrue(e.getMessage().contains("already has an active user agreement")); 
    }
    genericEntityDao.clear();
    
    user = genericEntityDao.reloadEntity(user);
    admin = genericEntityDao.reloadEntity(admin);
    checklistItem = genericEntityDao.reloadEntity(checklistItem);
    user.getChecklistItemEvents(checklistItem).last().createChecklistItemExpirationEvent(new LocalDate(), admin);

    flushAndClear();

    user = genericEntityDao.reloadEntity(user);
    admin = genericEntityDao.reloadEntity(admin);
    inputStream = new ByteArrayInputStream("contents".getBytes());
    userAgreementUpdater.updateUser(user, ScreensaverUserRole.SM_DSL_LEVEL3_SHARED_SCREENS, SMALL_MOLECULE, "user_agreement.pdf", inputStream, admin);
    
    flushAndClear();

    user = genericEntityDao.reloadEntity(user);
    checklistItem = genericEntityDao.reloadEntity(checklistItem);
    assertEquals(Sets.newHashSet(ScreensaverUserRole.SM_DSL_LEVEL3_SHARED_SCREENS,
                                 ScreensaverUserRole.SCREENSAVER_USER), 
                 user.getScreensaverUserRoles());
    assertEquals(checklistItem, user.getChecklistItemEvents().first().getChecklistItem());
    assertEquals("user_agreement.pdf", user.getAttachedFiles().iterator().next().getFilename());
    assertEquals("contents", IOUtils.toString(user.getAttachedFiles().iterator().next().getFileContents()));
  }
  
  public void testFindUserAgreementAdmins()
  {
    initializeData();

    flushAndClear();

    admin = genericEntityDao.reloadEntity(admin);
    otherUserAgreementAdmin = genericEntityDao.reloadEntity(otherUserAgreementAdmin);
    log.info("admin: " + admin);
    
    Set<ScreensaverUser> admins = userAgreementUpdater.findUserAgreementAdmins();
    log.info("admins: " + admins);
    assertNotNull(admins);
    assertTrue("should be two admins" + admins, admins.size()==3);
    assertTrue("admins doesn't contain the admin", admins.contains(admin));
    assertTrue("admins should contain the ", admins.contains(otherUserAgreementAdmin) );
    assertTrue("admins should contain the ", admins.contains(nonAdminUser) );
  }

	public void testFindUsersWithOldRNAIUAgreementsAndExpire() throws IOException {
			initializeData();
	
			// create the UA CLI
			ChecklistItem checklistItem = new ChecklistItem(
					UserAgreementUpdater.USER_AGREEMENT_CHECKLIST_ITEM_NAME.get(SMALL_MOLECULE), true, ChecklistItemGroup.FORMS, 0);
			genericEntityDao.persistEntity(checklistItem);
			ChecklistItem checklistItemRNAi = new ChecklistItem(
					UserAgreementUpdater.USER_AGREEMENT_CHECKLIST_ITEM_NAME.get(RNAI), true, ChecklistItemGroup.FORMS, 1);
			genericEntityDao.persistEntity(checklistItemRNAi);
	
			// create the attachment
			AttachedFileType attachedFileType = new UserAttachedFileType(
					UserAgreementUpdater.USER_AGREEMENT_ATTACHED_FILE_TYPE.get(SMALL_MOLECULE));
			genericEntityDao.persistEntity(attachedFileType);		
			AttachedFileType attachedFileTypeRNAI = new UserAttachedFileType(
					UserAgreementUpdater.USER_AGREEMENT_ATTACHED_FILE_TYPE.get(RNAI));
			genericEntityDao.persistEntity(attachedFileTypeRNAI);
	
			// create one entity that will be expired
			ScreeningRoomUser user = new ScreeningRoomUser("Test", "User");
			user.setEmail("testuser1@screensaver.med.harvard.edu");
			genericEntityDao.persistEntity(user);
			
			// create another user that will be expired, but will still have a screensaverUserLogin role, because we are going to add RNAi levels to them
			ScreeningRoomUser user1a = new ScreeningRoomUser("Test", "User1a");
			user1a.setEmail("testuser1@screensaver.med.harvard.edu");
			genericEntityDao.persistEntity(user1a);
	
			// create another entity that will not be expired
			ScreeningRoomUser user2 = new ScreeningRoomUser("Test", "User2");
			user2.setEmail("testuser2@screensaver.med.harvard.edu");
			genericEntityDao.persistEntity(user2);
	
			flushAndClear();
	
			// check that not yet "old" - there are no events yet!
			List<Pair<ScreeningRoomUser, ChecklistItemEvent>> expiredSet = userAgreementUpdater.findUsersWithOldUserAgreements(
					new LocalDate(), true, RNAI);
			assertTrue(expiredSet.isEmpty());
	
			flushAndClear();
	
			// add a checklist item and event to the user
			user = genericEntityDao.reloadEntity(user);
			user1a = genericEntityDao.reloadEntity(user1a);
			user2 = genericEntityDao.reloadEntity(user2);
			admin = genericEntityDao.reloadEntity(admin);
			InputStream inputStream = new ByteArrayInputStream("contents".getBytes());
			userAgreementUpdater.updateUser(user, ScreensaverUserRole.RNAI_DSL_LEVEL1_MUTUAL_SCREENS, RNAI,
					"user_agreement.pdf", inputStream, admin);
			userAgreementUpdater.updateUser(user1a, ScreensaverUserRole.RNAI_DSL_LEVEL1_MUTUAL_SCREENS, RNAI,
					"user_agreement.pdf", inputStream, admin);
			userAgreementUpdater.updateUser(user1a, ScreensaverUserRole.SM_DSL_LEVEL1_MUTUAL_SCREENS, SMALL_MOLECULE,
					"user_agreement.pdf", inputStream, admin);  // add the RNAI role to this user, to test whether "RNAI" expiring a user who has SM roles, will leave the ScreensaverUserLogin role alone
	
			// set the other user to the future (have to do this manually)
			LocalDate date2 = new LocalDate();
			date2 = date2.plusYears(1);
			user2.createChecklistItemActivationEvent(checklistItemRNAi, date2, admin);
	
			flushAndClear();
	
			// first find the user with the old event first
			LocalDate findDate = new LocalDate();
			expiredSet = userAgreementUpdater.findUsersWithOldUserAgreements(findDate, true, RNAI);
	
			assertFalse(expiredSet.isEmpty());
			assertTrue(contains(user, expiredSet));
			assertTrue(contains(user1a, expiredSet));
			assertFalse(contains(user2, expiredSet));
			assertEquals(2, expiredSet.size());
	
			// now move the date out
			findDate = findDate.plusYears(2);
			expiredSet = userAgreementUpdater.findUsersWithOldUserAgreements(findDate, true, RNAI);
	
			assertFalse(expiredSet.isEmpty());
			assertTrue(contains(user, expiredSet));
			assertTrue(contains(user1a, expiredSet));
			assertTrue(contains(user2, expiredSet));
			assertEquals(3, expiredSet.size());
	
			// TODO: this next section is testing the expiration.
	
			// Expire the first user
			userAgreementUpdater.expireUser(user, admin, RNAI);
			userAgreementUpdater.expireUser(user1a, admin, RNAI);
			// query again, to see if the expired user still shows up
			expiredSet = userAgreementUpdater.findUsersWithOldUserAgreements(findDate, true, RNAI);
	
			assertFalse(expiredSet.isEmpty());
			assertFalse(contains(user, expiredSet));
			assertFalse(contains(user1a, expiredSet));
			assertTrue(contains(user2, expiredSet));
			assertEquals(1, expiredSet.size());
	
			// find out if user1a still has the login role
			flushAndClear();
			user = genericEntityDao.reloadEntity(user);
			user1a = genericEntityDao.reloadEntity(user1a);
			assertTrue(user1a.getScreensaverUserRoles().contains(ScreensaverUserRole.SCREENSAVER_USER));
			assertFalse(user.getScreensaverUserRoles().contains(ScreensaverUserRole.SCREENSAVER_USER));
		}

	
}
