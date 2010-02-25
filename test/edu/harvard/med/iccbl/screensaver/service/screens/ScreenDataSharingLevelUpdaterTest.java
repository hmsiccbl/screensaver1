// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.service.screens;

import java.util.Date;
import java.util.List;
import java.util.Set;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.SchemaUtil;
import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenDataSharingLevel;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.StudyType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUserClassification;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.util.Pair;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.springframework.test.AbstractTransactionalSpringContextTests;

import com.google.common.collect.Sets;

public class ScreenDataSharingLevelUpdaterTest extends AbstractTransactionalSpringContextTests
{
  private static Logger log = Logger.getLogger(ScreenDataSharingLevelUpdaterTest.class);
  protected GenericEntityDAO genericEntityDao;
  protected SchemaUtil schemaUtil;
  protected  ScreenDataSharingLevelUpdater screenDataSharingLevelUpdater;


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
  
  public void testFindExpired()
  {
    LocalDate date = new LocalDate(new Date());
    
    LabHead labHead = new LabHead("Lab", "Head", null);
    ScreeningRoomUser leadScreener = new ScreeningRoomUser("Lab", "Member1", ScreeningRoomUserClassification.UNASSIGNED);
    leadScreener.setLab(labHead.getLab());
    
    Screen screen1NotExpired = new Screen(leadScreener, labHead, 1, ScreenType.SMALL_MOLECULE, StudyType.IN_VITRO, "SDSL TEST1");
    screen1NotExpired.setDataPrivacyExpirationDate(date.plusYears(2));
    genericEntityDao.persistEntity(screen1NotExpired);

    Screen screen2Expired = new Screen(leadScreener, labHead, 2, ScreenType.SMALL_MOLECULE, StudyType.IN_VITRO, "SDSL TEST2");
    screen2Expired.setDataPrivacyExpirationDate(date);
    genericEntityDao.persistEntity(screen2Expired);

    Screen screen3Expired = new Screen(leadScreener, labHead, 3, ScreenType.SMALL_MOLECULE, StudyType.IN_VITRO, "SDSL TEST3");
    screen3Expired.setDataPrivacyExpirationDate(date.minusYears(1));
    genericEntityDao.persistEntity(screen3Expired);

    Screen screen4Default = new Screen(leadScreener, labHead, 4, ScreenType.SMALL_MOLECULE, StudyType.IN_VITRO, "SDSL TEST4");
    genericEntityDao.persistEntity(screen4Default);
    setComplete();
    endTransaction();

    startNewTransaction();
    
    List<Screen> allScreens = genericEntityDao.findAllEntitiesOfType(Screen.class);
    for(Screen screen:allScreens)
    {
      log.info("allScreens: " + screen.getTitle() + " , expires: " + screen.getDataPrivacyExpirationDate() );
    }
    
    
    Set<Screen> screens = screenDataSharingLevelUpdater.findNewExpired(date);
    for(Screen screen:screens)
    {
      log.info("expiredScreens: " + screen.getTitle() + " , expires: " + screen.getDataPrivacyExpirationDate() );
    }
    
    screen1NotExpired = genericEntityDao.reloadEntity(screen1NotExpired);
    screen2Expired = genericEntityDao.reloadEntity(screen2Expired);
    screen3Expired = genericEntityDao.reloadEntity(screen3Expired);
    screen4Default = genericEntityDao.reloadEntity(screen4Default);

    assertTrue("screen2Expired should be expired: ", screens.contains(screen2Expired));
    assertTrue("screen3Expired should be expired: ", screens.contains(screen3Expired));
    assertFalse("screen4Default should not be expired: ", screens.contains(screen4Default));
    assertFalse("screen1NotExpired should be expired: ", screens.contains(screen1NotExpired));
    
    // just a little test here to see if we get a null or an empty list, find none
    screens = screenDataSharingLevelUpdater.findNewExpired(date.minusYears(20));
    log.info("empty result: " + screens);
    assertNotNull(screens);
    assertTrue(screens.isEmpty());
  }
  
  /**
   * Test the bulk expire method
   */
  public void testExpire()
  {
    LocalDate date = new LocalDate();
    
    LabHead labHead = new LabHead("Lab", "Head", null);
    ScreeningRoomUser leadScreener = new ScreeningRoomUser("Lab", "Member1");
    leadScreener.setLab(labHead.getLab());
    AdministratorUser admin = new AdministratorUser("Test", "Admin", "", "", "", "", "", "");
    admin.addScreensaverUserRole(ScreensaverUserRole.SCREENS_ADMIN);
    
    Screen screen1NotExpired = new Screen(leadScreener, labHead, 1, ScreenType.SMALL_MOLECULE, StudyType.IN_VITRO, "SDSL TEST1");
    screen1NotExpired.setDataPrivacyExpirationDate(date.plusYears(2));
    genericEntityDao.persistEntity(screen1NotExpired);

    Screen screen2Expired = new Screen(leadScreener, labHead, 2, ScreenType.SMALL_MOLECULE, StudyType.IN_VITRO, "SDSL TEST2");
    screen2Expired.setDataPrivacyExpirationDate(date);
    genericEntityDao.persistEntity(screen2Expired);

    Screen screen3Expired = new Screen(leadScreener, labHead, 3, ScreenType.SMALL_MOLECULE, StudyType.IN_VITRO, "SDSL TEST3");
    screen3Expired.setDataPrivacyExpirationDate(date.minusYears(1));
    genericEntityDao.persistEntity(screen3Expired);

    Screen screen4Default = new Screen(leadScreener, labHead, 4, ScreenType.SMALL_MOLECULE, StudyType.IN_VITRO, "SDSL TEST4");
    genericEntityDao.persistEntity(screen4Default);
    setComplete();
    endTransaction();

    startNewTransaction();
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

    assertTrue("screen2Expired should be expired: ", screens.contains(screen2Expired));
    assertTrue("screen2Expired should be public: ", screen2Expired.getDataSharingLevel() == ScreenDataSharingLevel.MUTUAL_SCREENS );

    assertTrue("screen3Expired should be expired: ", screens.contains(screen3Expired));
    assertTrue("screen3Expired should be public: ", screen3Expired.getDataSharingLevel() == ScreenDataSharingLevel.MUTUAL_SCREENS );
    
    assertFalse("screen4Default should not be expired: ", screens.contains(screen4Default));
    assertTrue("screen4Default should not be public: ", screen4Default.getDataSharingLevel() != ScreenDataSharingLevel.MUTUAL_SCREENS );

    assertFalse("screen1NotExpired should be expired: ", screens.contains(screen1NotExpired));
    assertTrue("screen1NotExpired should not be public: ", screen1NotExpired.getDataSharingLevel() != ScreenDataSharingLevel.MUTUAL_SCREENS );
    
    
  }
  
  /**
   * Test the updates of single screens to various levels
   * TODO: should check the audit log too
   */
  public void testScreenDataSharingLevelUpdater()
  {
    LabHead labHead = new LabHead("Lab", "Head", null);
    ScreeningRoomUser leadScreener = new ScreeningRoomUser("Lab", "Member1", ScreeningRoomUserClassification.UNASSIGNED);
    leadScreener.setLab(labHead.getLab());
    AdministratorUser admin = new AdministratorUser("Test", "Admin", "", "", "", "", "", "");
    admin.addScreensaverUserRole(ScreensaverUserRole.SCREENS_ADMIN);
    Screen screen = new Screen(leadScreener, labHead, 1, ScreenType.SMALL_MOLECULE, StudyType.IN_VITRO, "SDSL TEST");

    genericEntityDao.persistEntity(labHead);
    genericEntityDao.persistEntity(leadScreener);
    genericEntityDao.persistEntity(admin);
    genericEntityDao.persistEntity(screen);
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
