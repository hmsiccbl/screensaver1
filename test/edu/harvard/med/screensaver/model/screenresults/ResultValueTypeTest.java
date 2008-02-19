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
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultParser;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultParserTest;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellType;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.engine.EntityKey;

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
        persistEntity(screen, new HashSet<AbstractEntity>());
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
        assertEquals(screen.getScreenResult().getResultValueTypesList().get(0).getResultValues().get(new WellKey(1, "A01")).getValue(),
                     "1071894");
      }
    });
  }
  
  /**
   * Test that our Hibernate mapping is set properly to extra-lazy load
   * RVT->ResultValues. This is an ancient test that was implemented during the
   * learning of Hibernate (and before we "trusted" particular mappings were
   * doing what we expected), but it we might as well keep it around.
   */
  public void testResultValueTypeResultValuesAreExtraLazy()
  {
    schemaUtil.truncateTablesOrCreateSchema();
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Screen screen = MakeDummyEntities.makeDummyScreen(107);
        ScreenResult screenResult = screen.createScreenResult(new Date());
        ResultValueType rvt = screenResult.createResultValueType("Luminescence");
        Library library = new Library(
          "library 1",
          "lib1",
          ScreenType.SMALL_MOLECULE,
          LibraryType.COMMERCIAL,
          1,
          1);
        for (int i = 1; i < 10; ++i) {
          Well well = library.createWell(new WellKey(i, "A01"), WellType.EMPTY);
          genericEntityDao.saveOrUpdateEntity(well);
          rvt.createResultValue(well, Integer.toString(i));
        }
        genericEntityDao.saveOrUpdateEntity(screen);
      }
    });

    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Session session = hibernateTemplate.getSessionFactory().getCurrentSession();
        assertEquals("session initially empty", 0, session.getStatistics().getEntityCount());
        Screen screen = genericEntityDao.findEntityByProperty(Screen.class, "screenNumber", 107);
        List<ResultValueType> rvts = screen.getScreenResult().getResultValueTypesList();
        ResultValueType rvt = rvts.get(0);

        for (Object key : session.getStatistics().getEntityKeys()) {
          EntityKey entityKey = (EntityKey) key;
          log.debug(entityKey);
          assertFalse("no resultValue entities in session",
                      entityKey.getEntityName().endsWith("ResultValue"));
        }

        Set<Well> wells = rvt.getScreenResult().getWells();
        Map<WellKey,ResultValue> resultValues = rvt.getResultValues();
        log.debug("resultValue count=" + resultValues.size());
        Iterator<Well> iter = wells.iterator();
        int n = session.getStatistics().getEntityKeys().size();
        log.debug("entity key count (pre-iteration)=" + n);
        while (iter.hasNext()) {
          WellKey wellKey = (WellKey) iter.next().getWellKey();
          ResultValue rv = resultValues.get(wellKey);
          rv.getValue();
          log.debug("entity key count=" + session.getStatistics().getEntityKeys().size());
          assertEquals("Hibernate session cache did not grow in size",
                       n,
                       session.getStatistics().getEntityKeys().size());
        }
      }
    });
  }
  
}

