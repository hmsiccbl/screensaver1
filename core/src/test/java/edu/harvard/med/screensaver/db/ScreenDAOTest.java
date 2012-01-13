// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screens.ProjectPhase;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.StudyType;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.test.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.test.MakeDummyEntities;


/**
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class ScreenDAOTest extends AbstractSpringPersistenceTest
{
  private static final Logger log = Logger.getLogger(ScreenDAOTest.class);

  @Autowired
  protected ScreenDAO screenDao;

  public void testDelete()
  {
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Screen screen = MakeDummyEntities.makeDummyScreen(1);
        genericEntityDao.persistEntity(screen);
      }
    });

    genericEntityDao.doInTransaction(new DAOTransaction()
    {

      public void runTransaction()
      {
        screenDao.deleteStudy(genericEntityDao.findEntityByProperty(Screen.class, Screen.facilityId.getPropertyName(), "1"));
      }
    });

    Screen screen1 = genericEntityDao.findEntityByProperty(Screen.class, Screen.facilityId.getPropertyName(), "1");
    assertNull("screen not deleted", screen1);
  }

  /**
   * Delete a study that has annotations and annotation values associated.
   */
  public void testDeleteStudy()
  {
    genericEntityDao.doInTransaction(new DAOTransaction()
      {
      public void runTransaction()
      {
        Library library = MakeDummyEntities.makeDummyLibrary(1, ScreenType.SMALL_MOLECULE, 1);
        Iterator<Well> wellsIter = library.getWells().iterator();
        Well well1 = wellsIter.next();
        genericEntityDao.saveOrUpdateEntity(library);

        Screen screen = MakeDummyEntities.makeDummyScreen(1);
        genericEntityDao.persistEntity(screen);
        // Create the dummy annotation
        AnnotationType aType = new AnnotationType((Screen) screen,
                                                  "dummy annotation",
                                                  "dummy annotation desc",
                                                  0,
                                                  Boolean.TRUE);
        aType.createAnnotationValue(well1.getLatestReleasedReagent(), "1.0");
        genericEntityDao.saveOrUpdateEntity(aType);

      }
    });

    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        screenDao.deleteStudy(genericEntityDao.findEntityByProperty(Screen.class, Screen.facilityId.getPropertyName(), "1"));
      }
    });

    Screen screen1 = genericEntityDao.findEntityByProperty(Screen.class, Screen.facilityId.getPropertyName(), "1");
    assertNull("screen not deleted", screen1);
  }

  public void testFindRelatedScreens()
  {
    LabHead user = new LabHead("Test", "User", null);
    Screen screen1 = new Screen(null, "1", user, user, ScreenType.RNAI, StudyType.IN_VITRO, ProjectPhase.PRIMARY_SCREEN, "test");
    screen1.setProjectId("PID1");
    genericEntityDao.saveOrUpdateEntity(screen1);
    Screen screen2 = new Screen(null, "1-1", user, user, ScreenType.RNAI, StudyType.IN_VITRO, ProjectPhase.PRIMARY_SCREEN, "test");
    screen2.setProjectId("PID1");
    genericEntityDao.saveOrUpdateEntity(screen2);
    Screen screen3 = new Screen(null, "1-2", user, user, ScreenType.RNAI, StudyType.IN_VITRO, ProjectPhase.PRIMARY_SCREEN, "test");
    screen3.setProjectId("PID1");
    genericEntityDao.saveOrUpdateEntity(screen3);

    List<Screen> relatedScreens = screenDao.findRelatedScreens(screen1);
    assertEquals(ImmutableList.of("1", "1-1", "1-2"),
                 ImmutableList.copyOf(Iterables.transform(relatedScreens, Screen.ToFacilityId)));

  }

  public void testIsScreenFacilityIdUnique()
  {
    LabHead user = new LabHead("Test", "User", null);
    Screen screen1 = new Screen(null, "1", user, user, ScreenType.RNAI, StudyType.IN_VITRO, ProjectPhase.PRIMARY_SCREEN, "test");
    assertTrue(screenDao.isScreenFacilityIdUnique(screen1));
    genericEntityDao.saveOrUpdateEntity(screen1);
    Screen screen2 = new Screen(null, "2", user, user, ScreenType.RNAI, StudyType.IN_VITRO, ProjectPhase.PRIMARY_SCREEN, "test");
    genericEntityDao.saveOrUpdateEntity(screen2);
    assertTrue(screenDao.isScreenFacilityIdUnique(screen2));
    screen2.setFacilityId("1");
    assertFalse(screenDao.isScreenFacilityIdUnique(screen2));
    Screen screen3 = new Screen(null, "1", user, user, ScreenType.RNAI, StudyType.IN_VITRO, ProjectPhase.PRIMARY_SCREEN, "test");
    assertFalse(screenDao.isScreenFacilityIdUnique(screen3));
    screen3.setFacilityId("3");
    assertTrue(screenDao.isScreenFacilityIdUnique(screen3));
    genericEntityDao.saveOrUpdateEntity(screen3);
    assertTrue(screenDao.isScreenFacilityIdUnique(screen3));
  }
}