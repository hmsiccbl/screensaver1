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

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.springframework.test.AbstractTransactionalSpringContextTests;

import com.google.common.collect.Sets;

import edu.harvard.med.iccbl.screensaver.io.screens.ScreenPrivacyExpirationUpdater;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.SchemaUtil;
import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.Publication;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenDataSharingLevel;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.StatusValue;
import edu.harvard.med.screensaver.model.screens.StudyType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUserClassification;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.util.Pair;

public class ScreenDataSharingLevelUpdaterTest extends AbstractTransactionalSpringContextTests
{
  private static Logger log = Logger.getLogger(ScreenDataSharingLevelUpdaterTest.class);
  protected GenericEntityDAO genericEntityDao;
  protected SchemaUtil schemaUtil;
  protected  ScreenDataSharingLevelUpdater screenDataSharingLevelUpdater;


  private static int screen_number_counter = 1;
  AdministratorUser admin = null;
  AdministratorUser otherDSLAdmin = null;
  LabHead labHead = null;
  ScreeningRoomUser leadScreener = null;
  ScreeningRoomUser collaborator = null;
  
  protected void onSetUpInTransaction() throws Exception 
  {
    log.info("onSetupInTransaction called");
    String server = "ss.harvard.com"; // note mailinator reduced size of supported addresses
    admin = new AdministratorUser("admin", "testaccount", "admin@" + server, "", "", "", "dev", "");
    admin.addScreensaverUserRole(ScreensaverUserRole.SCREENS_ADMIN);
    admin.addScreensaverUserRole(ScreensaverUserRole.SCREEN_DATA_SHARING_LEVELS_ADMIN);
    genericEntityDao.persistEntity(admin);

    otherDSLAdmin = new AdministratorUser("dslAdmin", "testaccount", "dslAdmin@" + server, "", "", "", "dsl1", "");
    otherDSLAdmin.addScreensaverUserRole(ScreensaverUserRole.SCREENS_ADMIN);
    otherDSLAdmin.addScreensaverUserRole(ScreensaverUserRole.SCREEN_DATA_SHARING_LEVELS_ADMIN);
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
    Screen screen = new Screen(leadScreener, labHead, screen_number_counter++, type, StudyType.IN_VITRO, title);
    screen.addCollaborator(collaborator);
    screen.createStatusItem(new LocalDate(), StatusValue.ACCEPTED);
    genericEntityDao.persistEntity(screen);
    return screen;
  }

  @Override
  protected String[] getConfigLocations()
  {
    return new String[] { "spring-context-test.xml" };
  }

  public ScreenDataSharingLevelUpdaterTest() 
  {
    setPopulateProtectedVariables(true);
  }
  
  @Override
  protected void onSetUpBeforeTransaction() throws Exception
  {
    schemaUtil.truncateTablesOrCreateSchema();
  }
  
  public void testAdjustDataPrivacyExpirationByActivities()
  {
    // as of [#2175] expiration services to become notification services only
    // the DPED will be set separately from the ScreenResultImport (the old way).
    // It will now be set by batch process, SCREEN_ACTIVITY_DATA_PRIVACY_EXPIRATION_AGE_DAYS from the last ScreeningActivity date for a screen:
    // Meaning:
    // DPED will be set in one step (this test)
    // DPED will be used in separate step to find and expire screens (next test)
    
    // 1. create some screens
    Screen screen1NoActivities = createScreen("SDSL TEST1");
    genericEntityDao.persistEntity(screen1NoActivities);
    
    Screen screen2HasActivity = createScreen("SDSL TEST2");
    genericEntityDao.persistEntity(screen2HasActivity);

    Screen screen3RnaiHasActivity = createScreen("SDSL TEST RNAI", ScreenType.RNAI);
    genericEntityDao.persistEntity(screen3RnaiHasActivity);

    Screen screen4HasNoLibraryScreeningActivity = createScreen("screen4HasNoLibraryScreeningActivity");
    genericEntityDao.persistEntity(screen4HasNoLibraryScreeningActivity);

    // 2. add some activities
    LocalDate newActivityDate = new LocalDate();
    screen2HasActivity.createLibraryScreening(admin, leadScreener, newActivityDate);
    screen2HasActivity.createLibraryScreening(admin, leadScreener, newActivityDate);
    screen2HasActivity.createLibraryScreening(admin, leadScreener, newActivityDate);
    screen2HasActivity.createLibraryScreening(admin, leadScreener, newActivityDate);
    // create a screeining for user provided plates
    LibraryScreening ls = screen2HasActivity.createLibraryScreening(admin, leadScreener, newActivityDate.plusDays(100));
    ls.setForExternalLibraryPlates(true);
    // create a non-library-screening activity too
    screen2HasActivity.createCherryPickRequest(admin, leadScreener, newActivityDate.plusDays(100));
    
    screen4HasNoLibraryScreeningActivity.createCherryPickRequest(admin, leadScreener, newActivityDate.plusDays(100));
    
    screen3RnaiHasActivity.createLibraryScreening(admin, leadScreener, newActivityDate);

    // 3. add some results
    screen2HasActivity.createScreenResult();
    screen4HasNoLibraryScreeningActivity.createScreenResult();
    
    setComplete();
    endTransaction();

    // 3. find the ones with "old" activities (activity age > SCREEN_ACTIVITY_DATA_PRIVACY_EXPIRATION_AGE_DAYS
    int ageToExpireFromActivityDateInDays =ScreenPrivacyExpirationUpdater.SCREEN_ACTIVITY_DATA_PRIVACY_EXPIRATION_AGE_DAYS;
    startNewTransaction();
    
    ScreenDataSharingLevelUpdater.DataPrivacyAdjustment adjustment = screenDataSharingLevelUpdater
        .adjustDataPrivacyExpirationByActivities(ageToExpireFromActivityDateInDays, admin);

    setComplete();
    endTransaction();
    
    startNewTransaction();
    
    screen1NoActivities = genericEntityDao.reloadEntity(screen1NoActivities);
    screen2HasActivity = genericEntityDao.reloadEntity(screen2HasActivity);
    screen3RnaiHasActivity = genericEntityDao.reloadEntity(screen3RnaiHasActivity);
    screen4HasNoLibraryScreeningActivity = genericEntityDao.reloadEntity(screen4HasNoLibraryScreeningActivity);

    boolean containsFirst=false, containsSecond = false, containsThird = false, containsFourth = false;
    for(Pair<Screen,AdministrativeActivity> result:adjustment.screensAdjusted)
    {
      log.info("entry: " + result.getSecond());
      if(result.getFirst().equals(screen1NoActivities)) containsFirst = true;
      if(result.getFirst().equals(screen2HasActivity)) containsSecond = true;
      if(result.getFirst().equals(screen3RnaiHasActivity)) containsThird = true;
      if(result.getFirst().equals(screen4HasNoLibraryScreeningActivity)) containsFourth = true;
    }
    
    // TODO: update test for this
    assertTrue("there should be no screens that where adjustment was not allowed", 
               adjustment.screenPrivacyAdjustmentNotAllowed.isEmpty());
    
    assertFalse("screen1NoActivities should not have been adjusted", containsFirst);
    assertTrue("screen2HasActivity should have been adjusted", containsSecond);
    assertFalse("screen3RnaiHasActivity should not have been adjusted", containsThird);
    assertFalse("screen4HasNoLibraryScreeningActivity should not have been adjusted", containsFourth);
    assertEquals("new screen2HasActivity dataPrivacyExpirationDate() wrong: ",
                 newActivityDate.plusDays(ageToExpireFromActivityDateInDays),
                 screen2HasActivity.getDataPrivacyExpirationDate());
    
  }
  
  public void testGetDataSharingLevelAdmins()
  {
    setComplete();
    endTransaction();
    startNewTransaction();
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
  
  public void testFindNewPublishedPrivate()
  {
    Screen screen1NotPublished = createScreen("SDSL TEST1 Not Published");
    genericEntityDao.persistEntity(screen1NotPublished);

    Screen screen2Published = createScreen("SDSL TEST2 Published");
    screen2Published.createScreenResult();
    Publication publication = screen2Published.createPublication();
    publication.setAuthors("Test Authors");
    publication.setJournal("Test Journal");
    publication.setTitle("Test Publication Title");
    genericEntityDao.persistEntity(screen2Published);

    Screen screen3Mutual= createScreen("SDSL screen3Mutual");
    screen3Mutual.createScreenResult();
    screen3Mutual.setDataSharingLevel(ScreenDataSharingLevel.MUTUAL_SCREENS);
    publication = screen3Mutual.createPublication();
    publication.setAuthors("Test Authors x");
    publication.setJournal("Test Journal x");
    publication.setTitle("Test Publication Title x");
    genericEntityDao.persistEntity(screen3Mutual);

    Screen screen4Private= createScreen("SDSL private");
    screen4Private.createScreenResult();
    screen4Private.setDataSharingLevel(ScreenDataSharingLevel.PRIVATE);
    publication = screen4Private.createPublication();
    publication.setAuthors("Test Authors x");
    publication.setJournal("Test Journal x");
    publication.setTitle("Test Publication Title x");
    genericEntityDao.persistEntity(screen4Private);
    
    Screen screenPrivatePublishedTransferred = createScreen("SDSL screenPrivatePublishedTransferred");
    screenPrivatePublishedTransferred.createScreenResult();
    screenPrivatePublishedTransferred.setDataSharingLevel(ScreenDataSharingLevel.PRIVATE);
    publication = screenPrivatePublishedTransferred.createPublication();
    publication.setAuthors("Test Authors x");
    publication.setJournal("Test Journal x");
    publication.setTitle("Test Publication Title x");
    screenPrivatePublishedTransferred.createStatusItem(new LocalDate(), StatusValue.TRANSFERRED_TO_BROAD_INSTITUTE);
    genericEntityDao.persistEntity(screenPrivatePublishedTransferred);

    Screen screen4Public= createScreen("SDSL screen4Public");
    screen4Public.createScreenResult();
    screen4Public.setDataSharingLevel(ScreenDataSharingLevel.SHARED);
    publication = screen4Public.createPublication();
    publication.setAuthors("Test Authors xx");
    publication.setJournal("Test Journal xx");
    publication.setTitle("Test Publication Title xx");
    genericEntityDao.persistEntity(screen4Public);

    setComplete();
    endTransaction();

    startNewTransaction();
    screen2Published = genericEntityDao.reloadEntity(screen2Published);
    screen3Mutual = genericEntityDao.reloadEntity(screen3Mutual);
    screen4Private = genericEntityDao.reloadEntity(screen4Private);
    
    Set<Screen> publishedScreens = Sets.newHashSet(screenDataSharingLevelUpdater.findNewPublishedPrivate());
    assertNotNull(publishedScreens);
    assertEquals(Sets.newHashSet(screen2Published, screen3Mutual, screen4Private), publishedScreens);
  }

  
  /**
   * If this test is run by itself, it leaves the database in a state that is convenient for testing from the command line.
   */
  public void testThatSetsUpForCommandLineTest()
  {
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
    screen4Published.createPublication();
    
    genericEntityDao.persistEntity(screen1ToExpireTodayAfterAdjust);
    genericEntityDao.persistEntity(screen2ToNotifyTodayAfterAdjust);
    genericEntityDao.persistEntity(screen3AjustementOverridden);
    genericEntityDao.persistEntity(screen3aAjustementOverridden);
    genericEntityDao.persistEntity(screen4Published);
    
    setComplete();
    endTransaction();

  }
    
  Screen screen1NotExpired = null;
  Screen screen2Expired = null;
  Screen screen3Expired = null;
  Screen screen4Default =    null;
  Screen screen5RnaiExpired = null;
  Screen screen6ExpiredNoResults = null;
  Screen screen7ExpiredDropped = null;
  Screen screen8ExpiredTransferred = null;
  
  public void testFindExpired()
  {
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
    screen7ExpiredDropped.createStatusItem(new LocalDate(), StatusValue.DROPPED_TECHNICAL);
    genericEntityDao.persistEntity(screen7ExpiredDropped);
    
    screen8ExpiredTransferred = createScreen("test expired transferred");
    screen8ExpiredTransferred.setDataPrivacyExpirationDate(date.minusYears(1));
    screen8ExpiredTransferred.createScreenResult();
    screen8ExpiredTransferred.createStatusItem(new LocalDate(), StatusValue.TRANSFERRED_TO_BROAD_INSTITUTE);
    genericEntityDao.persistEntity(screen8ExpiredTransferred);
    
    setComplete();
    endTransaction();

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
    
    assertTrue("screen2Expired should be expired: ", screens.contains(screen2Expired));
    assertTrue("screen3Expired should be expired: ", screens.contains(screen3Expired));
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
  public void testExpire()
  {
    LocalDate date = new LocalDate(new Date());

    testFindExpired();
    setComplete();
    endTransaction();   
    startNewTransaction();
    
    screen1NotExpired = genericEntityDao.reloadEntity(screen1NotExpired);
    screen2Expired = genericEntityDao.reloadEntity(screen2Expired);
    screen3Expired = genericEntityDao.reloadEntity(screen3Expired);
    screen4Default = genericEntityDao.reloadEntity(screen4Default);
    screen5RnaiExpired = genericEntityDao.reloadEntity(screen5RnaiExpired);
    screen6ExpiredNoResults = genericEntityDao.reloadEntity(screen6ExpiredNoResults);
    screen7ExpiredDropped = genericEntityDao.reloadEntity(screen7ExpiredDropped);
    screen8ExpiredTransferred = genericEntityDao.reloadEntity(screen8ExpiredTransferred);
    
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
  }
  
  /**
   * Test the updates of single screens to various levels
   * TODO: should check the audit log too
   */
  public void testScreenDataSharingLevelUpdater()
  {
    Screen screen = createScreen("SDSL TEST");
    setComplete();
    endTransaction();
    
    startNewTransaction();
    screen = genericEntityDao.reloadEntity(screen);
    assertEquals(ScreenDataSharingLevel.PRIVATE, screen.getDataSharingLevel());
    screenDataSharingLevelUpdater.updateScreen(screen, ScreenDataSharingLevel.MUTUAL_SCREENS, admin);
    setComplete();
    endTransaction();

    startNewTransaction();
    screen = genericEntityDao.reloadEntity(screen);
    assertEquals(ScreenDataSharingLevel.MUTUAL_SCREENS, screen.getDataSharingLevel());
    screenDataSharingLevelUpdater.updateScreen(screen, ScreenDataSharingLevel.MUTUAL_POSITIVES, admin);
    setComplete();
    endTransaction();
    
    startNewTransaction();
    screen = genericEntityDao.reloadEntity(screen);
    assertEquals(ScreenDataSharingLevel.MUTUAL_POSITIVES, screen.getDataSharingLevel());
    screenDataSharingLevelUpdater.updateScreen(screen, ScreenDataSharingLevel.SHARED, admin);
    setComplete();
    endTransaction();

    startNewTransaction();
    screen = genericEntityDao.reloadEntity(screen);
    assertEquals(ScreenDataSharingLevel.SHARED, screen.getDataSharingLevel());
    screenDataSharingLevelUpdater.updateScreen(screen, ScreenDataSharingLevel.PRIVATE, admin);
    setComplete();
    endTransaction();

    startNewTransaction();
    screen = genericEntityDao.reloadEntity(screen);
    assertEquals(ScreenDataSharingLevel.PRIVATE, screen.getDataSharingLevel());
  }
}
