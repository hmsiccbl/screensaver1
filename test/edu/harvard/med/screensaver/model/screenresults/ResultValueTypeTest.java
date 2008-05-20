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

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultParser;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultParserTest;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;

import org.apache.log4j.Logger;

public class ResultValueTypeTest extends AbstractEntityInstanceTest<ResultValueType>
{
  // static members

  private static Logger log = Logger.getLogger(ResultValueTypeTest.class);

  public static final String SCREEN_RESULT_FILE = "ScreenResultTest115-result-values-size.xls";


  // instance data members

  protected ScreenResultParser screenResultParser;
  protected LibrariesDAO librariesDao;


  // public constructors and methods

  public ResultValueTypeTest() throws IntrospectionException
  {
    super(ResultValueType.class);
  }

  @Override
  protected <NE extends AbstractEntity> NE newInstanceViaParent(Class<NE> entityClass,
                                                                AbstractEntity parentBean,
                                                                boolean persistEntities)
  {
    if (entityClass.equals(ResultValueType.class)) {
      ScreenResult screenResult = (ScreenResult) parentBean;
      ResultValueType rvt = screenResult.createResultValueType(getTestValueForType(String.class).toString());
      if (persistEntities) {
        genericEntityDao.persistEntity(rvt);
      }
      return (NE) rvt;
    }
    return super.newInstanceViaParent(entityClass, parentBean, persistEntities);
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
        screenResultParser.parse(screen, workbookFile);
        log.debug(screenResultParser.getErrors());
        assertFalse("screen result had no errors", screenResultParser.getHasErrors());
        persistEntityNetwork(screen);
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
}

