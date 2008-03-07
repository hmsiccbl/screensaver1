// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/test/edu/harvard/med/screensaver/io/screenresults/ScreenResultParserTest.java $
// $Id: ScreenResultParserTest.java 693 2006-10-26 18:42:59Z ant4 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.screenresults;

import java.io.File;

import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.db.SchemaUtil;
import edu.harvard.med.screensaver.io.workbook2.WorkbookParseError;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;

/**
 */
public class ScreenResultPersistenceTest extends AbstractSpringTest
{

  public static final File TEST_INPUT_FILE_DIR = new File("test/edu/harvard/med/screensaver/io/screenresults");

  protected GenericEntityDAO genericEntityDao;
  protected LibrariesDAO librariesDao;
  protected SchemaUtil schemaUtil;
  protected ScreenResultParser screenResultParser;

  protected void onSetUp() throws Exception
  {
    super.onSetUp();
    schemaUtil.truncateTablesOrCreateSchema();
  }

  /**
   */
  public void testParseSaveLoadScreenResult() throws Exception
  {
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        File workbookFile = new File(TEST_INPUT_FILE_DIR, ScreenResultParserTest.SCREEN_RESULT_115_TEST_WORKBOOK_FILE);
        Screen screen = MakeDummyEntities.makeDummyScreen(115);
        Library library = new Library(
          "library 1",
          "lib1",
          ScreenType.SMALL_MOLECULE,
          LibraryType.COMMERCIAL,
          1,
          3);
        librariesDao.loadOrCreateWellsForLibrary(library);
        genericEntityDao.saveOrUpdateEntity(library);
        genericEntityDao.flush();

        screenResultParser.parse(screen,
                                 workbookFile);
        for (WorkbookParseError error: screenResultParser.getErrors()) {
          System.out.println("error: " + error);
        }
        assertFalse(screenResultParser.getHasErrors());
        genericEntityDao.saveOrUpdateEntity(screen.getLeadScreener());
        genericEntityDao.saveOrUpdateEntity(screen.getLabHead());
        genericEntityDao.saveOrUpdateEntity(screen);
      }
    });

    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction() {
        Screen screen = genericEntityDao.findEntityByProperty(Screen.class, "screenNumber", 115);
        ScreenResult screenResult = screen.getScreenResult();
        assertNotNull(screenResult);
        ResultValueType rvt0 = screenResult.getResultValueTypesList().get(0);
        rvt0.getResultValues().keySet().iterator();
        ResultValue rv = rvt0.getResultValues().get(new WellKey(1,"A01"));
        assertEquals("1071894", rv.getValue());
        // this tests how Hibernate will make use of WellKey, initializing with a concatenated key string
        rv = rvt0.getResultValues().get(new WellKey("00001:A01"));
        assertEquals("1071894", rv.getValue());
      }
    });
  }
}
