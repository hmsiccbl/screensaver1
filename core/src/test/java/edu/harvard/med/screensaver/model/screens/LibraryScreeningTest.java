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
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.service.libraries.LibraryScreeningDerivedPropertiesUpdater;
import edu.harvard.med.screensaver.test.MakeDummyEntities;

public class LibraryScreeningTest extends AbstractEntityInstanceTest<LibraryScreening>
{
  @Autowired
  protected LibraryScreeningDerivedPropertiesUpdater libraryScreeningDerivedPropertiesUpdater;

  public static TestSuite suite()
  {
    return buildTestSuite(LibraryScreeningTest.class, LibraryScreening.class);
  }

  public LibraryScreeningTest()
  {
    super(LibraryScreening.class);
  }
  
  public void testScreenedExperimentalWellCount()
  {
    schemaUtil.truncateTables();
    
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Library library1 = MakeDummyEntities.makeDummyLibrary(1, ScreenType.SMALL_MOLECULE, 2);
        Library library2 = MakeDummyEntities.makeDummyLibrary(2, ScreenType.SMALL_MOLECULE, 2);
        Copy lib1Copy = library1.createCopy((AdministratorUser) library1.getCreatedBy(), CopyUsageType.LIBRARY_SCREENING_PLATES, "A");
        Copy lib2Copy = library2.createCopy((AdministratorUser) library2.getCreatedBy(), CopyUsageType.LIBRARY_SCREENING_PLATES, "A");
        Plate plate1000 = lib1Copy.findPlate(1000).withWellVolume(new Volume(0));
        Plate plate1001 = lib1Copy.findPlate(1001).withWellVolume(new Volume(0));
        genericEntityDao.persistEntity(library1);
        genericEntityDao.persistEntity(library2);
        genericEntityDao.flush(); // necessary, since LibraryScreeningEntityUpdater queries for library 
        
        Screen screen = MakeDummyEntities.makeDummyScreen(1, ScreenType.SMALL_MOLECULE);
        AdministratorUser admin = new AdministratorUser("Admin", "User");
        ScreeningRoomUser user = new ScreeningRoomUser("Screener", "User");
        LibraryScreening libraryScreening = screen.createLibraryScreening(admin, user, new LocalDate());
        genericEntityDao.persistEntity(screen);
        
        libraryScreening.setNumberOfReplicates(1);
        libraryScreening.addAssayPlatesScreened(plate1000);
        libraryScreening.addAssayPlatesScreened(plate1001);
        genericEntityDao.flush();
        libraryScreeningDerivedPropertiesUpdater.updateScreeningStatistics(libraryScreening);
        
    
        //libraryScreenings = genericEntityDao.findAllEntitiesOfType(LibraryScreening.class).get(0);
        assertEquals(384 * 2, libraryScreening.getScreenedExperimentalWellCount());
        assertEquals(2, libraryScreening.getLibraryPlatesScreenedCount());
        assertEquals(1, libraryScreening.getLibrariesScreenedCount());

        Plate plate2000 = lib2Copy.findPlate(2000).withWellVolume(new Volume(0));
        Plate plate2001 = lib2Copy.findPlate(2001).withWellVolume(new Volume(0));
        libraryScreening.addAssayPlatesScreened(plate2000);
        libraryScreening.addAssayPlatesScreened(plate2001);
        libraryScreeningDerivedPropertiesUpdater.updateScreeningStatistics(libraryScreening);
        assertEquals(384 * 4, libraryScreening.getScreenedExperimentalWellCount());
        assertEquals(4, libraryScreening.getLibraryPlatesScreenedCount());
        assertEquals(2, libraryScreening.getLibrariesScreenedCount());

        libraryScreening.removeAssayPlatesScreened(plate2000);
        libraryScreening.removeAssayPlatesScreened(plate2001);
        libraryScreeningDerivedPropertiesUpdater.updateScreeningStatistics(libraryScreening);
        assertEquals(384 * 2, libraryScreening.getScreenedExperimentalWellCount());
        assertEquals(2, libraryScreening.getLibraryPlatesScreenedCount());
        assertEquals(1, libraryScreening.getLibrariesScreenedCount());

        libraryScreening.addAssayPlatesScreened(plate2001);
        libraryScreeningDerivedPropertiesUpdater.updateScreeningStatistics(libraryScreening);
        assertEquals(384 * 3, libraryScreening.getScreenedExperimentalWellCount());
        assertEquals(3, libraryScreening.getLibraryPlatesScreenedCount());
        assertEquals(2, libraryScreening.getLibrariesScreenedCount());

        
        // multiple replicates should only be counted once for experimental well count
        libraryScreening = screen.createLibraryScreening(admin, user, new LocalDate());
        libraryScreening.setNumberOfReplicates(2);
        libraryScreening.addAssayPlatesScreened(plate2000);
        genericEntityDao.saveOrUpdateEntity(libraryScreening);
        libraryScreeningDerivedPropertiesUpdater.updateScreeningStatistics(libraryScreening);
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

