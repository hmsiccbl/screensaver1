//$HeadURL:
//svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/test/edu/harvard/med/screensaver/TestHibernate.java
//$
//$Id: ComplexDAOTest.java 793 2006-11-22 15:54:30Z ant4 $

//Copyright 2006 by the President and Fellows of Harvard College.

//Screensaver is an open-source project developed by the ICCB-L and NSRB labs
//at Harvard Medical School. This software is distributed under the terms of
//the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.ui.searchresults.SortDirection;


/**
 * Tests the efficiency of accessing ResultValues via Hibernate scrollable
 * queries as used in ScreenResultsDAO.findSortedResultValueTableByRange(), comparing to
 * conventional model-based access via ResultValuType.getResultValues().
 * <i>These tests always pass, as they have not asserts. They are intended only
 * to produce timing output, for review (and interpretation) by a developer. For
 * this reason, these tests should NOT be run from our PackageTestSuite test
 * aggregator.</i>
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class ResultValueScrollableAccessEfficiencyTest extends AbstractSpringTest
{

  private static final Logger log = Logger.getLogger(ResultValueScrollableAccessEfficiencyTest.class);

  // public static methods

  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(ResultValueScrollableAccessEfficiencyTest.class);
  }


  // protected instance fields

  /**
   * Bean property, for database access via Spring and Hibernate.
   */
  protected GenericEntityDAO genericEntityDao;
  protected ScreenResultsDAO screenResultsDao;

  /**
   * For schema-related test setup tasks.
   */
  protected SchemaUtil schemaUtil;

  private final int plates = 1000;
  private ResultValueType rvt;
  private final int expectedFullResultSize = plates * Well.MAX_WELL_COLUMN;

  private long _start;


  // AbstractDependencyInjectionSpringContextTests methods

  @Override
  protected void onSetUp() throws Exception
  {
    super.onSetUp();
    schemaUtil.truncateTablesOrCreateSchema();
    genericEntityDao.doInTransaction(new DAOTransaction() 
    {
      public void runTransaction()
      {
        final Screen screen = MakeDummyEntities.makeDummyScreen(1); 
        ScreenResult screenResult = new ScreenResult(screen, new Date());
        ResultValueType rvt = new ResultValueType(screenResult, "rvt");
        Library library = new Library(
          "library 1",
          "lib1",
          ScreenType.SMALL_MOLECULE,
          LibraryType.COMMERCIAL,
          1,
          1);
        for (int iPlate = 1; iPlate <= plates; ++iPlate) {
          int plateNumber = iPlate;
          for (int iWell = 1; iWell <= Well.MAX_WELL_COLUMN; ++iWell) {
            Well well = new Well(library, plateNumber, "A" + iWell);
            String value = Integer.toString(plateNumber * Well.MAX_WELL_COLUMN + iWell);
            rvt.addResultValue(well, value);
          }
        }
        genericEntityDao.persistEntity(screen);
      }
    });

    genericEntityDao.doInTransaction(new DAOTransaction() 
    {
      public void runTransaction()
      {
        // preload parent entities to minimize one-time caching impact
        Screen preloadedScreen = genericEntityDao.findEntityByProperty(Screen.class, "hbnScreenNumber", 1);
        ScreenResult preloadedScreenResult = preloadedScreen.getScreenResult();
        rvt = preloadedScreenResult.getResultValueTypesList().get(0);
      }
    });

    _start = System.currentTimeMillis();

  }

  @Override
  protected void onTearDown() throws Exception
  {
    long elapsed = System.currentTimeMillis() - _start;
    log.debug("data access elapsed time: " + elapsed);
  }


  // JUnit test methods 

  public void testOnlyFirstResultValue()
  {

    Map<WellKey,List<ResultValue>> result = 
      screenResultsDao.findSortedResultValueTableByRange(Arrays.asList(rvt),
                                                         0, 
                                                         SortDirection.ASCENDING,
                                                         0, 
                                                         1,
                                                         null,
                                                         null);
    assertEquals(1, result.size());
  }

  public void testOnlyLastResultValue()
  {
    Map<WellKey,List<ResultValue>> result = 
      screenResultsDao.findSortedResultValueTableByRange(Arrays.asList(rvt), 
                                                         0, 
                                                         SortDirection.ASCENDING,
                                                         expectedFullResultSize - 1,
                                                         1,
                                                         null,
                                                         null);
    assertEquals(1, result.size());
  }

  public void testFullResultValueSet()
  {
    Map<WellKey,List<ResultValue>> result = 
      screenResultsDao.findSortedResultValueTableByRange(Arrays.asList(rvt), 
                                                         0, 
                                                         SortDirection.ASCENDING,
                                                         0,
                                                         expectedFullResultSize,
                                                         null,
                                                         null);
    assertEquals(expectedFullResultSize, result.size());
  }

  public void testFullResultValueSetViaEntityAccessorWithoutTableInitialization()
  {
    genericEntityDao.doInTransaction(new DAOTransaction() 
    {
      public void runTransaction()
      {
        genericEntityDao.persistEntity(rvt); // reattach to Hibernate sesssion
        Map<WellKey,ResultValue> resultValues = rvt.getResultValues();
        for (Map.Entry<WellKey,ResultValue> entry: resultValues.entrySet()) {
          entry.getValue().getValue();
        }
        assertEquals(expectedFullResultSize, resultValues.size());
      }
    });
  }

  public void testFullResultValueSetViaEntityAccessorWithTableInitialization()
  {
    genericEntityDao.doInTransaction(new DAOTransaction() 
    {
      public void runTransaction()
      {
        genericEntityDao.persistEntity(rvt); // reattach to Hibernate sesssion
        Map<WellKey,ResultValue> resultValues = rvt.getResultValues();
        Map<WellKey,List<ResultValue>> table = new HashMap<WellKey,List<ResultValue>>();
        List<ResultValue> values = new ArrayList<ResultValue>(1);
        for (Map.Entry<WellKey,ResultValue> entry: resultValues.entrySet()) {
          values.add(entry.getValue());
          table.put(entry.getKey(), values);
        }
        assertEquals(expectedFullResultSize, resultValues.size());
      }
    });
  }

}

