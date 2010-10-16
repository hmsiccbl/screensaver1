// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.util.Iterator;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;


/**
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class ScreenDAOTest extends AbstractSpringPersistenceTest
{

  private static final Logger log = Logger.getLogger(ScreenDAOTest.class);


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
}