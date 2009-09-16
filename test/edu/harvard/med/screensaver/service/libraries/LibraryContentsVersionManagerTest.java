// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.libraries;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.TestDataFactory;
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
  protected LibraryContentsVersionManager libraryContentsVersionManager;
  protected LibrariesDAO librariesDao;
  
  public void testReleaseLibraryContentsVersion()
  {
    final TestDataFactory dataFactory = new TestDataFactory();
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Library library = dataFactory.newInstance(Library.class);
        library.setScreenType(ScreenType.RNAI);
        librariesDao.loadOrCreateWellsForLibrary(library);
        dataFactory.newInstance(LibraryContentsVersion.class, library);
        for (Well well : library.getWells()) {
          int x = well.getWellKey().hashCode();
          well.createSilencingReagent(new ReagentVendorIdentifier("vendor", "rvi" + x),
                                      SilencingReagentType.SIRNA,
                                      "ACTG");
        }
      }
    });
    
    Library library = genericEntityDao.findAllEntitiesOfType(Library.class, true, Library.contentsVersions.getPath()).get(0);
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
    library = genericEntityDao.findAllEntitiesOfType(Library.class, true, 
                                                     Library.contentsVersions.getPath(),
                                                     Library.wells.to(Well.latestReleasedReagent).getPath(),
                                                     Library.wells.to(Well.reagents).getPath()).get(0);
    lcv = library.getContentsVersions().first();
    assertTrue(lcv.isReleased());
    
    // test the well.latestReleasedReagent is updated
    assertEquals(lcv, library.getLatestReleasedContentsVersion());
    for (Well well : library.getWells()) {
      assertEquals(lcv, well.getLatestReleasedReagent().getLibraryContentsVersion());
    }
  }
}
