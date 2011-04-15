// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.libraries;

import org.springframework.beans.factory.annotation.Autowired;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryContentsVersion;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.service.OperationRestrictedException;

public class LibraryContentsVersionManagerTest extends AbstractSpringPersistenceTest
{
  @Autowired
  protected LibraryContentsVersionManager libraryContentsVersionManager;
  @Autowired
  protected LibrariesDAO librariesDao;
  @Autowired
  protected LibraryCreator libraryCreator;
  
  public void testReleaseLibraryContentsVersion()
  {
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Library library = dataFactory.newInstance(Library.class);
        library.setScreenType(ScreenType.RNAI);
        libraryCreator.createWells(library);
        library.createContentsVersion(dataFactory.newInstance(AdministratorUser.class));
        for (Well well : library.getWells()) {
          int x = well.getWellKey().hashCode();
          well.createSilencingReagent(new ReagentVendorIdentifier("vendor", "rvi" + x),
                                      SilencingReagentType.SIRNA,
                                      "ACTG");
        }
      }
    });
    
    Library library = genericEntityDao.findAllEntitiesOfType(Library.class, true, Library.contentsVersions).get(0);
    LibraryContentsVersion lcv = library.getLatestContentsVersion();
    assertFalse(lcv.isReleased());
    
    // test operation restriction
    AdministratorUser admin = dataFactory.newInstance(AdministratorUser.class);
    genericEntityDao.saveOrUpdateEntity(admin);
    try {
      lcv = libraryContentsVersionManager.releaseLibraryContentsVersion(lcv, admin);
      fail("expected OperationRestrictedException");
    }
    catch (OperationRestrictedException e) {}
    assertFalse(lcv.isReleased());
    
    admin.addScreensaverUserRole(ScreensaverUserRole.LIBRARIES_ADMIN);
    genericEntityDao.saveOrUpdateEntity(admin);
    lcv = libraryContentsVersionManager.releaseLibraryContentsVersion(lcv, admin);
    library = lcv.getLibrary();
    
    // test in-memory representation updated
    assertTrue(lcv.isReleased());
    assertNotNull(library.getLatestReleasedContentsVersion());
    assertEquals(lcv.getLibraryContentsVersionId(), library.getLatestReleasedContentsVersion().getLibraryContentsVersionId());

    // test database representation updated
    genericEntityDao.doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        Library library = genericEntityDao.findAllEntitiesOfType(Library.class, true).get(0);
        LibraryContentsVersion lcv = library.getContentsVersions().first();
        assertTrue(lcv.isReleased());

        // test the well.latestReleasedReagent is updated
        assertEquals(lcv, library.getLatestReleasedContentsVersion());
        for (Well well : library.getWells()) {
          assertEquals(lcv, well.getLatestReleasedReagent().getLibraryContentsVersion());
        }
      }
    });
  }
}
