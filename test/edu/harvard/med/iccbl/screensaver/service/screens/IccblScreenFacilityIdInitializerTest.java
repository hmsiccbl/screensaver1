// $HeadURL: $
// $Id: $
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.service.screens;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.db.ScreenDAO;
import edu.harvard.med.screensaver.model.screens.ProjectPhase;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.StudyType;
import edu.harvard.med.screensaver.model.users.LabHead;

public class IccblScreenFacilityIdInitializerTest extends AbstractSpringPersistenceTest
{
  private IccblScreenFacilityIdInitializer screenFacilityIdInitializer;
  protected ScreenDAO screenDao;

  @Override
  protected void onSetUp() throws Exception
  {
    super.onSetUp();
    screenFacilityIdInitializer = new IccblScreenFacilityIdInitializer(genericEntityDao, screenDao);
    LabHead user = new LabHead("Test", "User", null);
    Screen primaryScreen = new Screen(null, "1", user, user, ScreenType.RNAI, StudyType.IN_VITRO, ProjectPhase.PRIMARY_SCREEN, "test");
    primaryScreen.setProjectId("PID1");
    genericEntityDao.persistEntity(primaryScreen);

    Screen study = new Screen(null, "100000", user, user, ScreenType.RNAI, StudyType.IN_VITRO, ProjectPhase.PRIMARY_SCREEN, "test");
    study.setProjectPhase(ProjectPhase.ANNOTATION);
    genericEntityDao.saveOrUpdateEntity(study);
  }

  public void testPrimaryScreen()
  {
    Screen primaryScreen = new Screen(null);
    primaryScreen.setProjectPhase(ProjectPhase.PRIMARY_SCREEN);
    assertTrue(screenFacilityIdInitializer.initializeFacilityId(primaryScreen));
    assertEquals("2", primaryScreen.getFacilityId());
  }

  public void testPrimaryScreenWithAssignedFacilityId()
  {
    Screen primaryScreen2 = new Screen(null);
    primaryScreen2.setProjectPhase(ProjectPhase.PRIMARY_SCREEN);
    primaryScreen2.setFacilityId("X");
    assertFalse(screenFacilityIdInitializer.initializeFacilityId(primaryScreen2));
    assertEquals("X", primaryScreen2.getFacilityId());
  }

  public void testRelatedScreen()
  {
    LabHead user = new LabHead("Test", "User", null);
    Screen followUpScreen = new Screen(null, null, user, user, ScreenType.RNAI, StudyType.IN_VITRO, ProjectPhase.PRIMARY_SCREEN, "test");
    followUpScreen.setProjectId("PID1");
    followUpScreen.setProjectPhase(ProjectPhase.FOLLOW_UP_SCREEN);
    assertTrue(screenFacilityIdInitializer.initializeFacilityId(followUpScreen));
    assertEquals("1-1", followUpScreen.getFacilityId());

    genericEntityDao.persistEntity(followUpScreen);

    followUpScreen = new Screen(null);
    followUpScreen.setProjectId("PID1");
    followUpScreen.setProjectPhase(ProjectPhase.FOLLOW_UP_SCREEN);
    assertTrue(screenFacilityIdInitializer.initializeFacilityId(followUpScreen));
    assertEquals("1-2", followUpScreen.getFacilityId());
  }

  public void testInadmissableAndSuspectPrimaryScreenFacilityIds()
  {
    LabHead user = new LabHead("Test", "User2", null);
    Screen invalidIdPrimaryScreen = new Screen(null, "XX3", user, user, ScreenType.RNAI, StudyType.IN_VITRO, ProjectPhase.PRIMARY_SCREEN, "test2");
    genericEntityDao.persistEntity(invalidIdPrimaryScreen);
    user = new LabHead("Test", "User3", null);
    Screen suspectIdPrimaryScreen = new Screen(null, "2X", user, user, ScreenType.RNAI, StudyType.IN_VITRO, ProjectPhase.PRIMARY_SCREEN, "test2");
    genericEntityDao.persistEntity(suspectIdPrimaryScreen);

    Screen nextPrimaryScreen = new Screen(null);
    nextPrimaryScreen.setProjectPhase(ProjectPhase.PRIMARY_SCREEN);
    assertTrue(screenFacilityIdInitializer.initializeFacilityId(nextPrimaryScreen));
    assertEquals("3", nextPrimaryScreen.getFacilityId());
  }
}
