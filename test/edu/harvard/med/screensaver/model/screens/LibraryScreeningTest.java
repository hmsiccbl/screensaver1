// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
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
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.libraries.PlateType;
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
        Copy lib1Copy = library1.createCopy(CopyUsageType.FOR_LIBRARY_SCREENING, "A");
        Copy lib2Copy = library2.createCopy(CopyUsageType.FOR_LIBRARY_SCREENING, "A");
        Plate plate1000 = lib1Copy.createPlate(1000, "", PlateType.ABGENE, new Volume(0));
        Plate plate1001 = lib1Copy.createPlate(1001, "", PlateType.ABGENE, new Volume(0));
        genericEntityDao.saveOrUpdateEntity(library1);
        genericEntityDao.saveOrUpdateEntity(library2);
        genericEntityDao.flush();
        
        Screen screen = MakeDummyEntities.makeDummyScreen(1, ScreenType.SMALL_MOLECULE);
        AdministratorUser admin = new AdministratorUser("Admin", "User", "", "", "", "", "", "");
        ScreeningRoomUser user = new ScreeningRoomUser("Screener", "User");
        LibraryScreening libraryScreening = screen.createLibraryScreening(admin, user, new LocalDate());
        genericEntityDao.saveOrUpdateEntity(screen);
        
        libraryScreening.setNumberOfReplicates(1);
        libraryScreening.addAssayPlatesScreened(plate1000);
        libraryScreening.addAssayPlatesScreened(plate1001);
        libraryScreening.update();
        
    
        //libraryScreenings = genericEntityDao.findAllEntitiesOfType(LibraryScreening.class).get(0);
        assertEquals(384 * 2, libraryScreening.getScreenedExperimentalWellCount());
        assertEquals(2, libraryScreening.getLibraryPlatesScreenedCount());
        assertEquals(1, libraryScreening.getLibrariesScreenedCount());

        Plate plate2000 = lib2Copy.createPlate(2000, "", PlateType.ABGENE, new Volume(0));
        Plate plate2001 = lib2Copy.createPlate(2001, "", PlateType.ABGENE, new Volume(0));
        libraryScreening.addAssayPlatesScreened(plate2000);
        libraryScreening.addAssayPlatesScreened(plate2001);
        libraryScreening.update();
        assertEquals(384 * 4, libraryScreening.getScreenedExperimentalWellCount());
        assertEquals(4, libraryScreening.getLibraryPlatesScreenedCount());
        assertEquals(2, libraryScreening.getLibrariesScreenedCount());

        libraryScreening.removeAssayPlatesScreened(plate2000);
        libraryScreening.removeAssayPlatesScreened(plate2001);
        libraryScreening.update();
        assertEquals(384 * 2, libraryScreening.getScreenedExperimentalWellCount());
        assertEquals(2, libraryScreening.getLibraryPlatesScreenedCount());
        assertEquals(1, libraryScreening.getLibrariesScreenedCount());

        libraryScreening.addAssayPlatesScreened(plate2001);
        libraryScreening.update();
        assertEquals(384 * 3, libraryScreening.getScreenedExperimentalWellCount());
        assertEquals(3, libraryScreening.getLibraryPlatesScreenedCount());
        assertEquals(2, libraryScreening.getLibrariesScreenedCount());

        
        // multiple replicates should only be counted once for experimental well count
        libraryScreening = screen.createLibraryScreening(admin, user, new LocalDate());
        libraryScreening.setNumberOfReplicates(2);
        libraryScreening.addAssayPlatesScreened(plate2000);
        genericEntityDao.saveOrUpdateEntity(libraryScreening);
        libraryScreening.update();
        assertEquals(384 * 1, libraryScreening.getScreenedExperimentalWellCount());
        assertEquals(1, libraryScreening.getLibraryPlatesScreenedCount());
        assertEquals(1, libraryScreening.getLibrariesScreenedCount());

        try {
          libraryScreening.addAssayPlatesScreened(plate2000);
          fail("expected DuplicateEntityException");
        }
        catch (Exception e) {}
}
    });
  }
}

