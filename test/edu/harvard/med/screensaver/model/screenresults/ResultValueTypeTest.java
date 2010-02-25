// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screenresults;

import java.beans.IntrospectionException;
import java.io.File;
import java.io.FileNotFoundException;

import junit.framework.TestSuite;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultParser;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultParserTest;
import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;

public class ResultValueTypeTest extends AbstractEntityInstanceTest<ResultValueType>
{
  public static TestSuite suite()
  {
    return buildTestSuite(ResultValueTypeTest.class, ResultValueType.class);
  }

  public static final String SCREEN_RESULT_FILE = "ScreenResultTest115-result-values-size.xls";


  protected ScreenResultParser screenResultParser;
  protected LibrariesDAO librariesDao;

  public ResultValueTypeTest() throws IntrospectionException
  {
    super(ResultValueType.class);
  }

  /**
   * Dual-purpose test:
   * 1. Regression test for (apparent) Hibernate bug, which determines size of
   * extra-lazy value-typed collections by counting non-null values of the first
   * declared property, which may be nullable. If nullable, the reported
   * collection size will be less than the real collection size!  RT #82230.
   * 2. Test that the resultValues collection, mapped by WellKey, is working properly
   */
  public void testResultValuesSize()
  {
    schemaUtil.truncateTablesOrCreateSchema();
    Library library = new Library("name", "short", ScreenType.SMALL_MOLECULE, LibraryType.DOS, 1, 3);
    librariesDao.loadOrCreateWellsForLibrary(library);
    genericEntityDao.saveOrUpdateEntity(library);

    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        File workbookFile = new File(ScreenResultParserTest.TEST_INPUT_FILE_DIR, SCREEN_RESULT_FILE);
        final Screen screen = MakeDummyEntities.makeDummyScreen(115);
        genericEntityDao.persistEntity(screen);
        try {
          screenResultParser.parse(screen, workbookFile);
        }
        catch (FileNotFoundException e) {
          fail(e.getMessage());
        }
        assertFalse("screen result had no errors", screenResultParser.getHasErrors());
      }
    });

    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Screen screen = genericEntityDao.findEntityByProperty(Screen.class, "screenNumber", 115);
        assertEquals("result values count",
                     28,
                     screen.getScreenResult().getResultValueTypesList().get(0).getResultValues().size());
        assertEquals(screen.getScreenResult().getResultValueTypesList().get(0).getWellKeyToResultValueMap().get(new WellKey(1, "A01")).getValue(),
                     "1071894");
      }
    });
  }

  public void testDeleteResultValueTest()
  {
    schemaUtil.truncateTablesOrCreateSchema();

    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Screen screen = MakeDummyEntities.makeDummyScreen(1);
        Library library = MakeDummyEntities.makeDummyLibrary(1, screen.getScreenType(), 1);
        MakeDummyEntities.makeDummyScreenResult(screen, library);
        genericEntityDao.persistEntity(library);
        genericEntityDao.persistEntity(screen);
      }
    });

    ScreenResult screenResult =
      genericEntityDao.findEntityByProperty(Screen.class,
                                            "screenNumber",
                                            1,
                                            true,
                                            "screenResult.resultValueTypes.derivedTypes",
                                            "screenResult.resultValueTypes.typesDerivedFrom")
                                            .getScreenResult();
    assertEquals("pre-delete rvt count", 8, screenResult.getResultValueTypes().size());

    try {
      screenResult.deleteResultValueType(screenResult.getResultValueTypesList().get(1));
      fail("expected DataModelViolationException when deleting RVT that is derived from");
    }
    catch (DataModelViolationException e) {}

    screenResult.deleteResultValueType(screenResult.getResultValueTypesList().get(6));
    screenResult.deleteResultValueType(screenResult.getResultValueTypesList().get(5));
    screenResult.deleteResultValueType(screenResult.getResultValueTypesList().get(3));
    screenResult.deleteResultValueType(screenResult.getResultValueTypesList().get(1));
    assertEquals("post-delete rvt count", 4, screenResult.getResultValueTypes().size());

    genericEntityDao.saveOrUpdateEntity(screenResult);

    screenResult =
      genericEntityDao.findEntityByProperty(Screen.class,
                                            "screenNumber",
                                            1,
                                            true,
                                            "screenResult.resultValueTypes.derivedTypes",
                                            "screenResult.resultValueTypes.typesDerivedFrom")
                                            .getScreenResult();
    assertEquals("post-delete, post-persist rvt count", 4, screenResult.getResultValueTypes().size());

  }
}

