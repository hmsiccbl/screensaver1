// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.service.screens;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import edu.harvard.med.screensaver.model.activities.AdministrativeActivity;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.ProjectPhase;
import edu.harvard.med.screensaver.model.screens.Publication;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenDataSharingLevel;
import edu.harvard.med.screensaver.model.screens.ScreenStatus;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.StudyType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUserClassification;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.test.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.util.Pair;

@Transactional
public class ScreenDataSharingLevelUpdaterTest extends AbstractSpringPersistenceTest
{
  private static Logger log = Logger.getLogger(ScreenDataSharingLevelUpdaterTest.class);

  private static int screen_number_counter = 1;

  @Autowired
  protected  ScreenDataSharingLevelUpdater screenDataSharingLevelUpdater;

  AdministratorUser admin = null;
  AdministratorUser otherDSLAdmin = null;
  LabHead labHead = null;
  ScreeningRoomUser leadScreener = null;
  ScreeningRoomUser collaborator = null;
  
  @Transactional
  public void initializeData() throws Exception
  {
    log.info("setupInTransaction called");
    String server = "ss.harvard.com"; // note mailinator reduced size of supported addresses
    admin = new AdministratorUser("admin", "testaccount");
    admin.addScreensaverUserRole(ScreensaverUserRole.SCREENS_ADMIN);
    admin.addScreensaverUserRole(ScreensaverUserRole.SCREEN_DATA_SHARING_LEVELS_ADMIN);
    admin.addScreensaverUserRole(ScreensaverUserRole.SCREEN_DSL_EXPIRATION_NOTIFY);
    
    genericEntityDao.persistEntity(admin);

    otherDSLAdmin = new AdministratorUser("dslAdmin", "testaccount");
    otherDSLAdmin.addScreensaverUserRole(ScreensaverUserRole.SCREENS_ADMIN);
    otherDSLAdmin.addScreensaverUserRole(ScreensaverUserRole.SCREEN_DATA_SHARING_LEVELS_ADMIN);
    otherDSLAdmin.addScreensaverUserRole(ScreensaverUserRole.SCREEN_DSL_EXPIRATION_NOTIFY);
    genericEntityDao.persistEntity(otherDSLAdmin);
 
    labHead = new LabHead("test", "PrincipalInvestigator", null);
    labHead.setEmail("pi@" + server);
    labHead.setECommonsId("pi1");
    genericEntityDao.persistEntity(labHead);
    
    leadScreener = new ScreeningRoomUser("test", "LeadScreener", ScreeningRoomUserClassification.POSTDOC);
    leadScreener.setLab(labHead.getLab());
    leadScreener.setECommonsId("scrnr1");
    leadScreener.setEmail("ldscrnr@" + server);
    genericEntityDao.persistEntity(leadScreener);
    
    collaborator = new ScreeningRoomUser("test", "Collaborator", ScreeningRoomUserClassification.POSTDOC);
    collaborator.setLab(labHead.getLab());
    collaborator.setECommonsId("collab1");
    collaborator.setEmail("collab1@" + server);
    genericEntityDao.persistEntity(collaborator);
  }
  
  private Screen createScreen(String title)
  {
    return createScreen(title, ScreenType.SMALL_MOLECULE);
  }  
  
  
  private Screen createScreen(String title, ScreenType type)
  {
    log.info("leads" + leadScreener);
    Screen screen = new Screen(null, Integer.toString(screen_number_counter++), leadScreener, labHead, type, StudyType.IN_VITRO, ProjectPhase.PRIMARY_SCREEN, title);
    screen.addCollaborator(collaborator);
    screen.createStatusItem(new LocalDate(), ScreenStatus.ACCEPTED);
    genericEntityDao.persistEntity(screen);
    return screen;
  }

  public void testAdjustDataPrivacyExpirationByActivities() throws Exception
  {
    initializeData();

    // as of [#2175] expiration services to become notification services only
    // the DPED will be set separately from the ScreenResultImport (the old way).
    // It will now be set by batch process, SCREEN_ACTIVITY_DATA_PRIVACY_EXPIRATION_AGE_DAYS from the last ScreeningActivity date for a screen:
    // Meaning:
    // DPED will be set in one step (this test)
    // DPED will be used in separate step to find and expire screens (next test)
    
    // 1. create some screens
    Screen screen1NoActivities = createScreen("SDSL TEST1");
    Screen screen2HasActivity = createScreen("SDSL TEST2");
    Screen screen5TransferredHasActivity = createScreen("SDSL TEST5");
    screen5TransferredHasActivity.createStatusItem(new LocalDate(), ScreenStatus.TRANSFERRED_TO_BROAD_INSTITUTE);
    Screen screen3RnaiHasActivity = createScreen("SDSL TEST RNAI", ScreenType.RNAI);
    Screen screen4HasNoLibraryScreeningActivity = createScreen("screen4HasNoLibraryScreeningActivity");
    Screen screen6MinMaxSet = createScreen("screen6MinMaxSet");

    // 2. add some activities
    LocalDate newActivityDate = new LocalDate();
    screen2HasActivity.createLibraryScreening(admin, leadScreener, newActivityDate);
    screen2HasActivity.createLibraryScreening(admin, leadScreener, newActivityDate);
    screen2HasActivity.createLibraryScreening(admin, leadScreener, newActivityDate);
    screen2HasActivity.createLibraryScreening(admin, leadScreener, newActivityDate);
    // create a screening for user provided plates
    LibraryScreening ls = screen2HasActivity.createLibraryScreening(admin, leadScreener, newActivityDate.plusDays(100));
    ls.setForExternalLibraryPlates(true);
    // create a non-library-screening activity too
    screen2HasActivity.createCherryPickRequest(admin, leadScreener, newActivityDate.plusDays(100));
    
    screen4HasNoLibraryScreeningActivity.createCherryPickRequest(admin, leadScreener, newActivityDate.plusDays(100));
    
    screen3RnaiHasActivity.createLibraryScreening(admin, leadScreener, newActivityDate);

    screen5TransferredHasActivity.createLibraryScreening(admin, leadScreener, newActivityDate);
    screen5TransferredHasActivity.createLibraryScreening(admin, leadScreener, newActivityDate);
    screen5TransferredHasActivity.createLibraryScreening(admin, leadScreener, newActivityDate);
    screen5TransferredHasActivity.createLibraryScreening(admin, leadScreener, newActivityDate);
    // create a screening for user provided plates
    ls = screen5TransferredHasActivity.createLibraryScreening(admin, leadScreener, newActivityDate.plusDays(100));
    ls.setForExternalLibraryPlates(true);
    // 3. add some results
    screen2HasActivity.createScreenResult();
    screen5TransferredHasActivity.createScreenResult();
    screen4HasNoLibraryScreeningActivity.createScreenResult();
    
    LocalDate maxAllowedDataPrivacyExpirationDate = newActivityDate.minusDays(10);
    LocalDate minAllowedDataPrivacyExpirationDate = newActivityDate.minusDays(20);
    screen6MinMaxSet.setMinAllowedDataPrivacyExpirationDate(minAllowedDataPrivacyExpirationDate);
    screen6MinMaxSet.setMaxAllowedDataPrivacyExpirationDate(maxAllowedDataPrivacyExpirationDate);
    screen6MinMaxSet.createLibraryScreening(admin, leadScreener, newActivityDate);
    screen6MinMaxSet.createScreenResult();
    
    genericEntityDao.persistEntity(screen1NoActivities);
    genericEntityDao.persistEntity(screen2HasActivity);
    genericEntityDao.persistEntity(screen3RnaiHasActivity);
    genericEntityDao.persistEntity(screen4HasNoLibraryScreeningActivity);
    genericEntityDao.persistEntity(screen5TransferredHasActivity);
    genericEntityDao.persistEntity(screen6MinMaxSet);
    flushAndClear();

    // 4. find the ones with "old" activities (activity age > SCREEN_ACTIVITY_DATA_PRIVACY_EXPIRATION_AGE_DAYS)
    int ageToExpireFromActivityDateInDays = 760;

    ScreenDataSharingLevelUpdater.DataPrivacyAdjustment adjustment = screenDataSharingLevelUpdater
        .adjustDataPrivacyExpirationByActivities(ageToExpireFromActivityDateInDays, admin);

    flushAndClear();
    
    screen1NoActivities = genericEntityDao.reloadEntity(screen1NoActivities);
    screen2HasActivity = genericEntityDao.reloadEntity(screen2HasActivity);
    screen3RnaiHasActivity = genericEntityDao.reloadEntity(screen3RnaiHasActivity);
    screen4HasNoLibraryScreeningActivity = genericEntityDao.reloadEntity(screen4HasNoLibraryScreeningActivity);
    screen5TransferredHasActivity = genericEntityDao.reloadEntity(screen5TransferredHasActivity);
    screen6MinMaxSet = genericEntityDao.reloadEntity(screen6MinMaxSet);

    boolean containsFirst = false, containsSecond = false, containsThird = false, containsFourth = false, containsFifth = false, containsSixth = false;
    for(Pair<Screen,AdministrativeActivity> result:adjustment.screensAdjusted)
    {
      log.info("entry: " + result.getSecond());
      if(result.getFirst().equals(screen1NoActivities)) containsFirst = true;
      if(result.getFirst().equals(screen2HasActivity)) containsSecond = true;
      if(result.getFirst().equals(screen3RnaiHasActivity)) containsThird = true;
      if(result.getFirst().equals(screen4HasNoLibraryScreeningActivity)) containsFourth = true;
      if (result.getFirst().equals(screen5TransferredHasActivity)) containsFifth = true;
      if (result.getFirst().equals(screen6MinMaxSet)) containsSixth = true;
    }
    
    // TODO: update test for this
    assertTrue("no screens should not be allowed", 
               adjustment.screenPrivacyAdjustmentNotAllowed.isEmpty());
    assertFalse("the sixth screen should adjusted to allowed", 
               adjustment.screensAdjustedToAllowed.isEmpty());
    
    assertFalse("screen1NoActivities should not have been adjusted", containsFirst);
    assertTrue("screen2HasActivity should have been adjusted", containsSecond);
    assertFalse("screen3RnaiHasActivity should not have been adjusted", containsThird);
    assertFalse("screen4HasNoLibraryScreeningActivity should not have been adjusted", containsFourth);
    assertFalse("screen5TransferredHasActivity should not have been adjusted", containsFifth);
    assertFalse("screen6MinMaxSet should not have been adjusted", containsSixth);
    assertEquals("new screen2HasActivity dataPrivacyExpirationDate() wrong: ",
                 newActivityDate.plusDays(ageToExpireFromActivityDateInDays),
                 screen2HasActivity.getDataPrivacyExpirationDate());
    
  }

  public void testGetDataSharingLevelAdmins() throws Exception
  {
    initializeData();

    flushAndClear();

    admin = genericEntityDao.reloadEntity(admin);
    otherDSLAdmin = genericEntityDao.reloadEntity(otherDSLAdmin);
    log.info("admin: " + admin);
    
    Set<ScreensaverUser> admins = screenDataSharingLevelUpdater.findDataSharingLevelAdminUsers();
    log.info("admins: " + admins);
    assertNotNull(admins);
    assertTrue("should be two admins" + admins, admins.size()==2);
    assertTrue("admins doesn't contain the admin", admins.contains(admin));
    assertTrue("admins should contain the ", admins.contains(otherDSLAdmin) );
  }

  public void testFindNewPublishedPrivate() throws Exception
  {
    initializeData();

    Screen screen1NotPublished = createScreen("SDSL TEST1 Not Published");
    genericEntityDao.persistEntity(screen1NotPublished);

    Screen screen2Published = createScreen("SDSL TEST2 Published");
    screen2Published.createScreenResult();
    Publication publication = new Publication();
    publication.setAuthors("Test Authors");
    publication.setJournal("Test Journal");
    publication.setTitle("Test Publication Title");
    screen2Published.addPublication(publication);
    genericEntityDao.persistEntity(screen2Published);

    Screen screen3Mutual= createScreen("SDSL screen3Mutual");
    screen3Mutual.createScreenResult();
    screen3Mutual.setDataSharingLevel(ScreenDataSharingLevel.MUTUAL_SCREENS);
    publication = new Publication();
    publication.setAuthors("Test Authors x");
    publication.setJournal("Test Journal x");
    publication.setTitle("Test Publication Title x");
    screen3Mutual.addPublication(publication);
    genericEntityDao.persistEntity(screen3Mutual);

    Screen screen4Private= createScreen("SDSL private");
    screen4Private.createScreenResult();
    screen4Private.setDataSharingLevel(ScreenDataSharingLevel.PRIVATE);
    publication = new Publication();
    publication.setAuthors("Test Authors x");
    publication.setJournal("Test Journal x");
    publication.setTitle("Test Publication Title x");
    screen4Private.addPublication(publication);
    genericEntityDao.persistEntity(screen4Private);
    
    Screen screenPrivatePublishedTransferred = createScreen("SDSL screenPrivatePublishedTransferred");
    screenPrivatePublishedTransferred.createScreenResult();
    screenPrivatePublishedTransferred.setDataSharingLevel(ScreenDataSharingLevel.PRIVATE);
    publication = new Publication();
    publication.setAuthors("Test Authors x");
    publication.setJournal("Test Journal x");
    publication.setTitle("Test Publication Title x");
    screenPrivatePublishedTransferred.createStatusItem(new LocalDate(), ScreenStatus.TRANSFERRED_TO_BROAD_INSTITUTE);
    screenPrivatePublishedTransferred.addPublication(publication);
    genericEntityDao.persistEntity(screenPrivatePublishedTransferred);

    Screen screen4Public= createScreen("SDSL screen4Public");
    screen4Public.createScreenResult();
    screen4Public.setDataSharingLevel(ScreenDataSharingLevel.SHARED);
    publication = new Publication();
    publication.setAuthors("Test Authors xx");
    publication.setJournal("Test Journal xx");
    publication.setTitle("Test Publication Title xx");
    screen4Public.addPublication(publication);
    genericEntityDao.persistEntity(screen4Public);

    flushAndClear();

    screen2Published = genericEntityDao.reloadEntity(screen2Published);
    screen3Mutual = genericEntityDao.reloadEntity(screen3Mutual);
    screen4Private = genericEntityDao.reloadEntity(screen4Private);
    
    Set<Screen> publishedScreens = Sets.newHashSet(screenDataSharingLevelUpdater.findNewPublishedPrivate());
    assertNotNull(publishedScreens);
    assertEquals(Sets.newHashSet(screen2Published, screen3Mutual, screen4Private), publishedScreens);
  }

  /**
   * If this test is run by itself, it leaves the database in a state that is convenient for testing from the command
   * line.
   */
  @Rollback(value = false)
  public void testThatSetsUpForCommandLineTest() throws Exception
  {
    initializeData();

    int daysToNofify = 60; 
    int daysToExpire = 790; // 2 years, 2months  (technically, a month is variable, using 30 days for all calculations)
    LocalDate today = new LocalDate();
    LocalDate activityDateToExpireToday = today.minusDays(daysToExpire);
    LocalDate activityDateToNotifyToday = today.minusDays(daysToExpire).plusDays(daysToNofify);
    
    Screen screen1ToExpireTodayAfterAdjust = createScreen("is testing for expiration (760 days after lab activity) today");
    screen1ToExpireTodayAfterAdjust.createScreenResult();
    screen1ToExpireTodayAfterAdjust.createLibraryScreening(admin, leadScreener, activityDateToExpireToday);
    
    Screen screen2ToNotifyTodayAfterAdjust = createScreen("is testing for notification (60 days before expiration) today");
    screen2ToNotifyTodayAfterAdjust.createScreenResult();
    screen2ToNotifyTodayAfterAdjust.createLibraryScreening(admin, leadScreener, activityDateToNotifyToday);
    
    Screen screen3AjustementOverridden = createScreen("is testing for max/min DPED override - Adjustment to the allowed value");
    screen3AjustementOverridden.createScreenResult();
    screen3AjustementOverridden.createLibraryScreening(admin, leadScreener, activityDateToExpireToday);
    screen3AjustementOverridden.setDataPrivacyExpirationDate(today.plusDays(2));
    screen3AjustementOverridden.setMinAllowedDataPrivacyExpirationDate(today.plusDays(1));
    
    Screen screen3aAjustementOverridden = createScreen("is testing for max/min DPED override - adjustment not allowed - overridden");
    screen3aAjustementOverridden.createScreenResult();
    screen3aAjustementOverridden.createLibraryScreening(admin, leadScreener, activityDateToExpireToday);
    screen3aAjustementOverridden.setMinAllowedDataPrivacyExpirationDate(today.plusDays(1));
    screen3aAjustementOverridden.setDataPrivacyExpirationDate(today.plusDays(1));
    
    Screen screen4Published = createScreen("is testing a published Screen");
    screen4Published.createScreenResult();
    screen4Published.createLibraryScreening(admin, leadScreener, activityDateToNotifyToday);
    screen4Published.addPublication(new Publication());
    
    genericEntityDao.persistEntity(screen1ToExpireTodayAfterAdjust);
    genericEntityDao.persistEntity(screen2ToNotifyTodayAfterAdjust);
    genericEntityDao.persistEntity(screen3AjustementOverridden);
    genericEntityDao.persistEntity(screen3aAjustementOverridden);
    genericEntityDao.persistEntity(screen4Published);
    genericEntityDao.flush();
  }
    
  Screen screen1NotExpired = null;
  Screen screen2Expired = null;
  Screen screen3Expired = null;
  Screen screen4Default =    null;
  Screen screen5RnaiExpired = null;
  Screen screen6ExpiredNoResults = null;
  Screen screen7ExpiredDropped = null;
  Screen screen8ExpiredTransferred = null;
  Screen screen9ExpiredMaxDatePassed = null;
  
  public void testFindExpired() throws Exception
  {
    initializeData();

    LocalDate date = new LocalDate(new Date());

    screen1NotExpired = createScreen("SDSL TEST1");
    screen1NotExpired.setDataPrivacyExpirationDate(date.plusYears(2));
    genericEntityDao.persistEntity(screen1NotExpired);

    screen2Expired = createScreen("SDSL TEST2");
    screen2Expired.createScreenResult();
    screen2Expired.setDataPrivacyExpirationDate(date);
    genericEntityDao.persistEntity(screen2Expired);

    screen3Expired = createScreen("SDSL TEST3");
    screen3Expired.createScreenResult();
    screen3Expired.setDataPrivacyExpirationDate(date.minusYears(1));
    genericEntityDao.persistEntity(screen3Expired);
    
    screen9ExpiredMaxDatePassed = createScreen("screen9ExpiredMaxDatePassed");
    screen9ExpiredMaxDatePassed.createScreenResult();
    screen9ExpiredMaxDatePassed.setMinAllowedDataPrivacyExpirationDate(date.minusYears(2));
    screen9ExpiredMaxDatePassed.setMaxAllowedDataPrivacyExpirationDate(date.minusYears(2));
    screen9ExpiredMaxDatePassed.setDataPrivacyExpirationDate(date.minusYears(2));
    genericEntityDao.persistEntity(screen9ExpiredMaxDatePassed);
    
    screen4Default = createScreen("SDSL TEST4");
    genericEntityDao.persistEntity(screen4Default);

    screen5RnaiExpired = createScreen("Test RNAI expired",ScreenType.RNAI);
    screen5RnaiExpired.setDataPrivacyExpirationDate(date.minusYears(1));
    screen5RnaiExpired.createScreenResult();
    genericEntityDao.persistEntity(screen5RnaiExpired);

    screen6ExpiredNoResults = createScreen("test expired no results",ScreenType.RNAI);
    screen6ExpiredNoResults.setDataPrivacyExpirationDate(date.minusYears(1));
    genericEntityDao.persistEntity(screen6ExpiredNoResults);

    screen7ExpiredDropped = createScreen("test expired dropped technical");
    screen7ExpiredDropped.setDataPrivacyExpirationDate(date.minusYears(1));
    screen7ExpiredDropped.createScreenResult();
    screen7ExpiredDropped.createStatusItem(new LocalDate(), ScreenStatus.DROPPED_TECHNICAL);
    genericEntityDao.persistEntity(screen7ExpiredDropped);
    
    screen8ExpiredTransferred = createScreen("test expired transferred");
    screen8ExpiredTransferred.setDataPrivacyExpirationDate(date.minusYears(1));
    screen8ExpiredTransferred.createScreenResult();
    screen8ExpiredTransferred.createStatusItem(new LocalDate(), ScreenStatus.TRANSFERRED_TO_BROAD_INSTITUTE);
    genericEntityDao.persistEntity(screen8ExpiredTransferred);

    flushAndClear();

//    startNewTransaction();
    
    List<Screen> allScreens = genericEntityDao.findAllEntitiesOfType(Screen.class);
    for(Screen screen:allScreens)
    {
      log.info("allScreens: " + screen.getTitle() + " , expires: " + screen.getDataPrivacyExpirationDate() );
    }
    
    List<Screen> screens = screenDataSharingLevelUpdater.findNewExpiredNotNotified(date);
    for(Screen screen:screens)
    {
      log.info("expiredScreens: " + screen.getTitle() + " , expires: " + screen.getDataPrivacyExpirationDate() );
    }
    assertFalse("no expired screens", screens.isEmpty());
    
    screen1NotExpired = genericEntityDao.reloadEntity(screen1NotExpired);
    screen2Expired = genericEntityDao.reloadEntity(screen2Expired);
    screen3Expired = genericEntityDao.reloadEntity(screen3Expired);
    screen4Default = genericEntityDao.reloadEntity(screen4Default);
    screen5RnaiExpired = genericEntityDao.reloadEntity(screen5RnaiExpired);
    screen6ExpiredNoResults = genericEntityDao.reloadEntity(screen6ExpiredNoResults);
    screen7ExpiredDropped = genericEntityDao.reloadEntity(screen7ExpiredDropped);
    screen8ExpiredTransferred = genericEntityDao.reloadEntity(screen8ExpiredTransferred);
    screen9ExpiredMaxDatePassed = genericEntityDao.reloadEntity(screen9ExpiredMaxDatePassed);
    
    assertTrue("screen2Expired should be expired: ", screens.contains(screen2Expired));
    assertTrue("screen3Expired should be expired: ", screens.contains(screen3Expired));
    assertTrue("screen9ExpiredMaxDatePassed should be expired: ", screens.contains(screen9ExpiredMaxDatePassed));

    assertFalse("screen4Default should not be expired: ", screens.contains(screen4Default));
    assertFalse("screen1NotExpired should not be expired: ", screens.contains(screen1NotExpired));
    assertFalse("screen5RnaiExpired should not be expired: ", screens.contains(screen5RnaiExpired));
    assertFalse("screen6ExpiredNoResults should not be expired: ", screens.contains(screen6ExpiredNoResults));
    assertFalse("screen7ExpiredDropped should not be expired: ", screens.contains(screen7ExpiredDropped));
    assertFalse("screen8ExpiredTransferred should not be expired: ", screens.contains(screen8ExpiredTransferred));
    
    
    // just a little test here to see if we get a null or an empty list, find none
    screens = screenDataSharingLevelUpdater.findNewExpiredNotNotified(date.minusYears(20));
    log.info("empty result: " + screens);
    assertNotNull(screens);
    assertTrue(screens.isEmpty());
  }
  
  /**
   * Test the bulk expire method
   */
  public void testExpire() throws Exception
  {
    initializeData();

    LocalDate date = new LocalDate(new Date());

    testFindExpired();
    
    flushAndClear();

    screen1NotExpired = genericEntityDao.reloadEntity(screen1NotExpired);
    screen2Expired = genericEntityDao.reloadEntity(screen2Expired);
    screen3Expired = genericEntityDao.reloadEntity(screen3Expired);
    screen4Default = genericEntityDao.reloadEntity(screen4Default);
    screen5RnaiExpired = genericEntityDao.reloadEntity(screen5RnaiExpired);
    screen6ExpiredNoResults = genericEntityDao.reloadEntity(screen6ExpiredNoResults);
    screen7ExpiredDropped = genericEntityDao.reloadEntity(screen7ExpiredDropped);
    screen8ExpiredTransferred = genericEntityDao.reloadEntity(screen8ExpiredTransferred);
    screen9ExpiredMaxDatePassed = genericEntityDao.reloadEntity(screen9ExpiredMaxDatePassed);
    
    List<Pair<Screen,AdministrativeActivity>> results = screenDataSharingLevelUpdater.expireScreenDataSharingLevels(date, admin);
    //TODO: Do some checking of the activities as well.
    Set<Screen> screens = Sets.newLinkedHashSet();
    for(Pair<Screen,AdministrativeActivity> result:results)
    {
      screens.add(result.getFirst());
    }
    log.info("screens: " + screens);
    screen1NotExpired = genericEntityDao.reloadEntity(screen1NotExpired);
    screen2Expired = genericEntityDao.reloadEntity(screen2Expired);
    screen3Expired = genericEntityDao.reloadEntity(screen3Expired);
    screen4Default = genericEntityDao.reloadEntity(screen4Default);
    screen5RnaiExpired = genericEntityDao.reloadEntity(screen5RnaiExpired);

    assertTrue("screen2Expired should be expired: ", screens.contains(screen2Expired));
    assertTrue("screen2Expired should be public: ", screen2Expired.getDataSharingLevel() == ScreenDataSharingLevel.MUTUAL_SCREENS );

    assertTrue("screen3Expired should be expired: ", screens.contains(screen3Expired));
    assertTrue("screen3Expired should be public: ", screen3Expired.getDataSharingLevel() == ScreenDataSharingLevel.MUTUAL_SCREENS );
    
    assertFalse("screen4Default should not be expired: ", screens.contains(screen4Default));
    assertTrue("screen4Default should not be public: ", screen4Default.getDataSharingLevel() != ScreenDataSharingLevel.MUTUAL_SCREENS );

    assertFalse("screen1NotExpired should be expired: ", screens.contains(screen1NotExpired));
    assertTrue("screen1NotExpired should not be public: ", screen1NotExpired.getDataSharingLevel() != ScreenDataSharingLevel.MUTUAL_SCREENS );
    
    assertFalse("screen5RnaiExpired should not be expired: ", screens.contains(screen5RnaiExpired));
    assertTrue("screen5RnaiExpired should not be public: ", screen5RnaiExpired.getDataSharingLevel() != ScreenDataSharingLevel.MUTUAL_SCREENS );
    
    assertTrue("screen9ExpiredMaxDatePassed should be expired: ", screens.contains(screen9ExpiredMaxDatePassed));
    assertTrue("screen9ExpiredMaxDatePassed should be public: ", screen9ExpiredMaxDatePassed.getDataSharingLevel() == ScreenDataSharingLevel.MUTUAL_SCREENS );
  }

  /**
   * Test the updates of single screens to various levels
   * TODO: should check the audit log too
   * 
   * @throws Exception
   */
  public void testScreenDataSharingLevelUpdater() throws Exception
  {
    initializeData();
    Screen screen = createScreen("SDSL TEST");
    
    flushAndClear();

    screen = genericEntityDao.reloadEntity(screen);
    assertEquals(ScreenDataSharingLevel.PRIVATE, screen.getDataSharingLevel());
    screenDataSharingLevelUpdater.updateScreen(screen, ScreenDataSharingLevel.MUTUAL_SCREENS, admin);

    flushAndClear();

    screen = genericEntityDao.reloadEntity(screen);
    assertEquals(ScreenDataSharingLevel.MUTUAL_SCREENS, screen.getDataSharingLevel());
    screenDataSharingLevelUpdater.updateScreen(screen, ScreenDataSharingLevel.MUTUAL_POSITIVES, admin);

    flushAndClear();

    screen = genericEntityDao.reloadEntity(screen);
    assertEquals(ScreenDataSharingLevel.MUTUAL_POSITIVES, screen.getDataSharingLevel());
    screenDataSharingLevelUpdater.updateScreen(screen, ScreenDataSharingLevel.SHARED, admin);

    flushAndClear();

    screen = genericEntityDao.reloadEntity(screen);
    assertEquals(ScreenDataSharingLevel.SHARED, screen.getDataSharingLevel());
    screenDataSharingLevelUpdater.updateScreen(screen, ScreenDataSharingLevel.PRIVATE, admin);

    flushAndClear();

    screen = genericEntityDao.reloadEntity(screen);
    assertEquals(ScreenDataSharingLevel.PRIVATE, screen.getDataSharingLevel());
  }
}
