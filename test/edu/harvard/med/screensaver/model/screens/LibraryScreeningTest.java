// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import java.beans.IntrospectionException;

import junit.framework.TestSuite;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

import org.joda.time.LocalDate;

public class LibraryScreeningTest extends AbstractEntityInstanceTest<LibraryScreening>
{
  public static TestSuite suite()
  {
    return buildTestSuite(LibraryScreeningTest.class, LibraryScreening.class);
  }

  public LibraryScreeningTest() throws IntrospectionException
  {
    super(LibraryScreening.class);
  }
  
  public void testScreenedExperimentalWellCount()
  {
    schemaUtil.truncateTablesOrCreateSchema();
    
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Library library1 = MakeDummyEntities.makeDummyLibrary(1, ScreenType.SMALL_MOLECULE, 2);
        Library library2 = MakeDummyEntities.makeDummyLibrary(2, ScreenType.SMALL_MOLECULE, 2);
        genericEntityDao.saveOrUpdateEntity(library1);
        genericEntityDao.saveOrUpdateEntity(library2);
        genericEntityDao.flush();

        Screen screen = MakeDummyEntities.makeDummyScreen(1, ScreenType.SMALL_MOLECULE);
        genericEntityDao.saveOrUpdateEntity(screen);

        AdministratorUser admin = new AdministratorUser("Admin", "User", "", "", "", "", "", "");
        ScreeningRoomUser user = new ScreeningRoomUser("Screener", "User");
        LibraryScreening libraryScreening = screen.createLibraryScreening(admin, user, new LocalDate());
        libraryScreening.createPlatesUsed(1000, 1001, new Copy(library1, CopyUsageType.FOR_LIBRARY_SCREENING, "A"));
        genericEntityDao.saveOrUpdateEntity(libraryScreening);
        libraryScreening.update();
        
        Copy lib2Copy = new Copy(library2, CopyUsageType.FOR_LIBRARY_SCREENING, "A");
    
        //libraryScreening = genericEntityDao.findAllEntitiesOfType(LibraryScreening.class).get(0);
        assertEquals(384 * 2, libraryScreening.getScreenedExperimentalWellCount());

        PlatesUsed platesUsed = libraryScreening.createPlatesUsed(2000, 2001, lib2Copy);
        libraryScreening.update();
        assertEquals(384 * 4, libraryScreening.getScreenedExperimentalWellCount());

        libraryScreening.deletePlatesUsed(platesUsed);
        libraryScreening.update();
        assertEquals(384 * 2, libraryScreening.getScreenedExperimentalWellCount());

        libraryScreening.createPlatesUsed(2001, 2001, lib2Copy);
        libraryScreening.update();
        assertEquals(384 * 3, libraryScreening.getScreenedExperimentalWellCount());

        libraryScreening = screen.createLibraryScreening(admin, user, new LocalDate());
        libraryScreening.createPlatesUsed(2000, 2000, lib2Copy);
        genericEntityDao.saveOrUpdateEntity(libraryScreening);
        libraryScreening.update();
        assertEquals(384 * 1, libraryScreening.getScreenedExperimentalWellCount());
      }
    });
  }
}

