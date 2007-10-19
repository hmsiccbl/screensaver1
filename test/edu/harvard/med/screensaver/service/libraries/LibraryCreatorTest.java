//$HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
//$Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $

//Copyright 2006 by the President and Fellows of Harvard College.

//Screensaver is an open-source project developed by the ICCB-L and NSRB labs
//at Harvard Medical School. This software is distributed under the terms of
//the GNU General Public License.

package edu.harvard.med.screensaver.service.libraries;

import java.io.FileNotFoundException;
import java.io.InputStream;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellType;
import edu.harvard.med.screensaver.model.screens.ScreenType;

import org.apache.log4j.Logger;

public class LibraryCreatorTest extends AbstractSpringPersistenceTest
{
  private static final String RNAI_LIBRARY_CONTENTS_TEST_FILE = "rnaiLibraryContentsFile.xls";
  private static Logger log = Logger.getLogger(LibraryCreatorTest.class);

  protected LibraryCreator libraryCreator;
  protected LibrariesDAO librariesDao;

  public void testCreateLibrary() throws FileNotFoundException
  {
    Library library = new Library("library", "lib", ScreenType.RNAI, LibraryType.COMMERCIAL, 50439, 50439);
    library.setVendor("Dharmacon");
    library.setDescription("test library");

    InputStream contentsIn = LibraryCreatorTest.class.getResourceAsStream(RNAI_LIBRARY_CONTENTS_TEST_FILE);
    final Library library2 = libraryCreator.createLibrary(library, contentsIn);
    assertSame(library, library2);
    assertNotNull("library was assigned ID", library2.getLibraryId());

    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        Library library3 = genericEntityDao.findEntityById(Library.class, library2.getEntityId(), true,
        "wells.silencingReagents");
        assertNotNull("library was persisted", library3);
        Well firstEmptyWell = librariesDao.findWell(new WellKey(50439, "A1"));
        Well lastEmptyWell = librariesDao.findWell(new WellKey(50439, "P24"));
        Well firstExpWell = librariesDao.findWell(new WellKey(50439, "A5"));
        Well lastExpWell = librariesDao.findWell(new WellKey(50439, "J17"));
        assertNotNull("library wells created (check first empty well)", firstEmptyWell);
        assertNotNull("library wells created (check last empty well)", lastEmptyWell);
        assertEquals("first empty well", WellType.EMPTY, firstEmptyWell.getWellType());
        assertEquals("last empty well", WellType.EMPTY, lastEmptyWell.getWellType());
        assertNotNull("library wells created (check first exp well)", firstExpWell);
        assertNotNull("library wells created (check last exp well)", lastExpWell);
        // we're not yet setting the well type to anything other than empty
//      assertEquals("first exp well", WellType.EXPERIMENTAL, firstExpWell.getWellType());
//      assertEquals("last exp well", WellType.EXPERIMENTAL, lastExpWell.getWellType());
        assertNotNull("library contents imported", firstExpWell.getSilencingReagents().size() > 0);
      }
    });

    try {
      libraryCreator.createLibrary(library, contentsIn);
      fail("expected failure on redundant library create");
    }
    catch (IllegalArgumentException e) {}
  }

  /**
   * Test the service.libraries.LibraryCreator.createLibrary does not suffer
   * from a race condition on library plate range allocation
   */
  public void testConcurrentLibraryCreate()
  {
    // TODO: implement
    fail("not yet implemented");
  }
}
