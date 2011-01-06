// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.util.Set;

import com.google.common.collect.Sets;
import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.screens.ProjectPhase;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.StudyType;
import edu.harvard.med.screensaver.ui.screens.ScreenSearchResults;

public class ScreenSearchResultsTest extends AbstractSpringPersistenceTest
{
  private static Logger log = Logger.getLogger(ScreenSearchResultsTest.class);

  protected ScreenSearchResults screensBrowser;

  public void testSearchMyScreens()
  {}
  
  public void testSearchScreensForProject()
  {
    Screen screen1a = MakeDummyEntities.makeDummyScreen("1", ScreenType.RNAI, StudyType.IN_SILICO);
    Screen screen1b = MakeDummyEntities.makeDummyScreen("1f", ScreenType.RNAI, StudyType.IN_SILICO);
    Screen screen1c = MakeDummyEntities.makeDummyScreen("1c", ScreenType.RNAI, StudyType.IN_SILICO);
    screen1a.setProjectId("P1");
    screen1b.setProjectId("P1");
    screen1b.setProjectPhase(ProjectPhase.FOLLOW_UP_SCREEN);
    screen1c.setProjectId("P1");
    screen1c.setProjectPhase(ProjectPhase.COUNTER_SCREEN);
    Screen screen2 = MakeDummyEntities.makeDummyScreen("2", ScreenType.RNAI, StudyType.IN_SILICO);
    screen2.setProjectId(null);
    Screen screen3 = MakeDummyEntities.makeDummyScreen("3", ScreenType.RNAI, StudyType.IN_SILICO);
    screen3.setProjectId("P3");
    genericEntityDao.persistEntity(screen1a);
    genericEntityDao.persistEntity(screen1b);
    genericEntityDao.persistEntity(screen1c);
    genericEntityDao.persistEntity(screen2);
    genericEntityDao.persistEntity(screen3);

    screensBrowser.searchScreensForProject("P1");
    screensBrowser.getRowCount();
    Set<Screen> expectedScreens = Sets.newHashSet(genericEntityDao.findEntityById(Screen.class, screen1a.getEntityId()),
                                                  genericEntityDao.findEntityById(Screen.class, screen1b.getEntityId()),
                                                  genericEntityDao.findEntityById(Screen.class, screen1c.getEntityId()));
    Set<Screen> actualScreens = Sets.newHashSet(screensBrowser.getDataTableModel().iterator());
    assertEquals(expectedScreens, actualScreens);
  }
}
