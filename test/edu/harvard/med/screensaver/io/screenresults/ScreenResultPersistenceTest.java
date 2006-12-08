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
import java.util.Collections;

import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.SchemaUtil;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.Well;
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
  
  protected DAO dao;
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
    dao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        File workbookFile = new File(TEST_INPUT_FILE_DIR, "NewFormatTest.xls");
        Screen screen = ScreenResultParser.makeDummyScreen(115);
        Library library = new Library(
          "library 1",
          "lib1",
          ScreenType.SMALL_MOLECULE,
          LibraryType.COMMERCIAL,
          1,
          1);
        new Well(library, 1, "A01");
        new Well(library, 1, "A02");
        new Well(library, 1, "A03");
        new Well(library, 2, "A01");
        new Well(library, 2, "A02");
        new Well(library, 2, "A03");
        dao.persistEntity(library);
        screenResultParser.parse(screen, 
                                 workbookFile);
        assertEquals(Collections.EMPTY_LIST, screenResultParser.getErrors());
        dao.persistEntity(screen);
      }
    });
    
    dao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        Screen screen = dao.findEntityByProperty(Screen.class, "hbnScreenNumber", 115);
        ScreenResult screenResult = screen.getScreenResult();
        assertNotNull(screenResult);
        ResultValueType rvt0 = screenResult.getResultValueTypesList().get(0);
        ResultValue rv = rvt0.getResultValues().get(new WellKey(1,0,0));
        assertEquals("1071894.0", rv.getValue());
        // this tests how Hibernate will make use of WellKey, initializing with a concatenated key string
        rv = rvt0.getResultValues().get(new WellKey("00001:A01"));
        assertEquals("1071894.0", rv.getValue());
      }
    });
    
  }
  
  
}
