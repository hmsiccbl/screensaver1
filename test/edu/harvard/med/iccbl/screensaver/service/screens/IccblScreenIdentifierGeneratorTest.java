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

public class IccblScreenIdentifierGeneratorTest extends AbstractSpringPersistenceTest
{
  private IccblScreenIdentifierGenerator screenIdentifierGenerator;
  protected ScreenDAO screenDao;

  @Override
  protected void onSetUp() throws Exception
  {
    super.onSetUp();
    screenIdentifierGenerator = new IccblScreenIdentifierGenerator(genericEntityDao, screenDao);
    LabHead user = new LabHead("Test", "User", null);
    Screen primaryScreen = new Screen(null, "1", user, user, ScreenType.RNAI, StudyType.IN_VITRO, ProjectPhase.PRIMARY_SCREEN, "test");
    primaryScreen.setProjectId("PID1");
    genericEntityDao.persistEntity(primaryScreen);
  }

  public void testPrimaryScreen()
  {
    Screen primaryScreen = new Screen(null);
    primaryScreen.setProjectPhase(ProjectPhase.PRIMARY_SCREEN);
    assertTrue(screenIdentifierGenerator.updateIdentifier(primaryScreen));
    assertEquals("2", primaryScreen.getFacilityId());
  }

  public void testPrimaryScreenWithAssignedFacilityId()
  {
    Screen primaryScreen2 = new Screen(null);
    primaryScreen2.setProjectPhase(ProjectPhase.PRIMARY_SCREEN);
    primaryScreen2.setFacilityId("X");
    assertFalse(screenIdentifierGenerator.updateIdentifier(primaryScreen2));
    assertEquals("X", primaryScreen2.getFacilityId());
  }

  public void testFollowUpScreen()
  {
    Screen followUpScreen = new Screen(null);
    followUpScreen.setProjectId("PID1");
    followUpScreen.setProjectPhase(ProjectPhase.FOLLOW_UP_SCREEN);
    assertTrue(screenIdentifierGenerator.updateIdentifier(followUpScreen));
    assertEquals("1F", followUpScreen.getFacilityId());
  }

  public void testCounterScreen()
  {
    Screen counterScreen = new Screen(null);
    counterScreen.setProjectId("PID1");
    counterScreen.setProjectPhase(ProjectPhase.COUNTER_SCREEN);
    assertTrue(screenIdentifierGenerator.updateIdentifier(counterScreen));
    assertEquals("1C", counterScreen.getFacilityId());
  }

  public void testNonPrimaryScreenMissingPrimaryScreen()
  {
    Screen followUpScreen2 = new Screen(null);
    followUpScreen2.setProjectId("PID2");
    followUpScreen2.setProjectPhase(ProjectPhase.FOLLOW_UP_SCREEN);
    assertFalse(screenIdentifierGenerator.updateIdentifier(followUpScreen2));
    assertNull(followUpScreen2.getFacilityId());
  }
}
