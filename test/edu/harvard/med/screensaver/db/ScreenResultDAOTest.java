// $HeadURL:
// svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/test/edu/harvard/med/screensaver/TestHibernate.java
// $
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultParserTest;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.LibraryWellType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.AssayWell;
import edu.harvard.med.screensaver.model.screenresults.AssayWellControlType;
import edu.harvard.med.screensaver.model.screenresults.DataType;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.DataColumn;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.AssayReadoutType;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;

import org.apache.log4j.Logger;


/**
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class ScreenResultDAOTest extends AbstractSpringPersistenceTest
{

  private static final Logger log = Logger.getLogger(ScreenResultDAOTest.class);


  /**
   * Bean property, for database access via Spring and Hibernate.
   */
  protected ScreenResultsDAO screenResultsDao;


  public void testScreenResults()
  {
    final int replicates = 2;

    genericEntityDao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          ScreenResult screenResult = ScreenResultParserTest.makeScreenResult();
          DataColumn[] col = new DataColumn[replicates];
          for (int i = 0; i < replicates; i++) {
            col[i] = screenResult.createDataColumn("col" + i).forReplicate(i + 1).forPhenotype("human");
            col[i].setAssayReadoutType(i % 2 == 0 ? AssayReadoutType.PHOTOMETRY: AssayReadoutType.FLUORESCENCE_INTENSITY);
            if (i % 2 == 0) {
              col[i].makeBooleanPositiveIndicator();
            }
            else {
              col[i].makePartitionPositiveIndicator();
            }
          }

          Library library = new Library(
            "library with results",
            "lwr",
            ScreenType.SMALL_MOLECULE,
            LibraryType.COMMERCIAL,
            1,
            1);
          Well[] wells = new Well[3];
          for (int iWell = 0; iWell < wells.length; ++iWell) {
            WellKey wellKey = new WellKey(( iWell / 2 ) + 1,
                                          (iWell / library.getPlateSize().getRows()),
                                          (iWell % library.getPlateSize().getColumns()));
            wells[iWell] = library.createWell(wellKey, LibraryWellType.EXPERIMENTAL);
            AssayWell assayWell = screenResult.createAssayWell(wells[iWell]);
            for (int iResultValue = 0; iResultValue < col.length; ++iResultValue) {
              col[iResultValue].createResultValue(assayWell,
                                                  "value " + iWell + "," + iResultValue,
                                                  iWell % 2 == 1);
            }
          }
          genericEntityDao.saveOrUpdateEntity(library);

          // test the calculation of replicateCount from child DataColumns,
          // before setReplicate() is called by anyone
          assertEquals(replicates, screenResult.getReplicateCount().intValue());

          SortedSet<Integer> expectedPlateNumbers = new TreeSet<Integer>();
          expectedPlateNumbers.add(1);
          expectedPlateNumbers.add(2);
          assertEquals(expectedPlateNumbers, screenResult.getPlateNumbers());

          genericEntityDao.saveOrUpdateEntity(screenResult.getScreen().getLeadScreener());
          genericEntityDao.saveOrUpdateEntity(screenResult.getScreen().getLabHead());
          genericEntityDao.saveOrUpdateEntity(screenResult.getScreen());
        }

      });

    genericEntityDao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          Library library = genericEntityDao.findEntityByProperty(
            Library.class,
            "libraryName",
            "library with results");
          Set<Well> wells = library.getWells();
          Set<WellKey> wellKeys = new HashSet<WellKey>();
          for (Well well : wells) {
            wellKeys.add(well.getWellKey());
          }
          ScreenResult screenResult =
            genericEntityDao.findAllEntitiesOfType(ScreenResult.class).get(0);
          assertEquals(replicates,screenResult.getReplicateCount().intValue());
          int iResultValue = 0;
          SortedSet<DataColumn> dataColumns = screenResult.getDataColumns();
          assertEquals(2, replicates);
          for (DataColumn col : dataColumns) {
            assertEquals(
              screenResult,
              col.getScreenResult());
            assertEquals(
              iResultValue % 2 == 0 ? AssayReadoutType.PHOTOMETRY : AssayReadoutType.FLUORESCENCE_INTENSITY,
              col.getAssayReadoutType());
            assertEquals(
              iResultValue % 2 == 0 ? DataType.POSITIVE_INDICATOR_BOOLEAN: DataType.POSITIVE_INDICATOR_PARTITION,
              col.getDataType());
            assertEquals(
              "human",
              col.getAssayPhenotype());

            Map<WellKey,ResultValue> resultValues = col.getWellKeyToResultValueMap();
            for (WellKey wellKey : resultValues.keySet()) {
              assertTrue(wellKeys.contains(wellKey));
              // note that our naming scheme is testing the ordering of the
              // DataColumn and ResultValue entities (within their parent
              // sets)
              ResultValue rv = resultValues.get(wellKey);
              assertEquals("value " + wellKey.getColumn() + "," + iResultValue, rv.getValue());
              assertEquals(wellKey.getColumn() % 2 == 1, rv.isExclude());
            }
            iResultValue++;
          }
        }
      });
  }

  public void testDerivedScreenResults()
  {
    final int replicates = 3;
    final SortedSet<DataColumn> derivedColSet1 = new TreeSet<DataColumn>();
    final SortedSet<DataColumn> derivedColSet2 = new TreeSet<DataColumn>();
    genericEntityDao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          ScreenResult screenResult = ScreenResultParserTest.makeScreenResult();

          for (int i = 0; i < replicates; i++) {
            DataColumn col = screenResult.createDataColumn("col" + i).forReplicate(1).forPhenotype("human");
            derivedColSet1.add(col);
            if (i % 2 == 0) {
              derivedColSet2.add(col);
            }
          }
          DataColumn derivedCol1 = screenResult.createDataColumn("derivedCol1").forReplicate(1).forPhenotype("human");
          for (DataColumn dataColumn : derivedColSet1) {
            derivedCol1.addTypeDerivedFrom(dataColumn);
          }

          DataColumn derivedCol2 = screenResult.createDataColumn("derivedCol2").forReplicate(1).forPhenotype("human");
          for (DataColumn dataColumn : derivedColSet2) {
            derivedCol2.addTypeDerivedFrom(dataColumn);
          }

          genericEntityDao.saveOrUpdateEntity(screenResult.getScreen().getLeadScreener());
          genericEntityDao.saveOrUpdateEntity(screenResult.getScreen().getLabHead());
          genericEntityDao.saveOrUpdateEntity(screenResult.getScreen());
      }
    });

    genericEntityDao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          List<ScreenResult> screenResults = genericEntityDao.findAllEntitiesOfType(ScreenResult.class);
          ScreenResult screenResult = screenResults.get(0);
          SortedSet<DataColumn> dataColumns =
            new TreeSet<DataColumn>(screenResult.getDataColumns());

          DataColumn derivedCol = dataColumns.last();
          Set<DataColumn> derivedFromSet = derivedCol.getTypesDerivedFrom();
          assertEquals(derivedColSet2, derivedFromSet);

          dataColumns.remove(derivedCol);
          derivedCol = dataColumns.last();
          derivedFromSet = derivedCol.getTypesDerivedFrom();
          assertEquals(derivedColSet1, derivedFromSet);
        }
      });
  }

  public void testDeleteScreenResult()
  {
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Library library = MakeDummyEntities.makeDummyLibrary(1, ScreenType.SMALL_MOLECULE, 1);
        genericEntityDao.saveOrUpdateEntity(library);
        Screen screen1 = MakeDummyEntities.makeDummyScreen(1);
        MakeDummyEntities.makeDummyScreenResult(screen1, library);
        genericEntityDao.saveOrUpdateEntity(screen1);
      }
    });

    Screen screen1 = genericEntityDao.findEntityByProperty(Screen.class, "screenNumber", 1);
    assertNotNull("screen1 has screen result initially", screen1.getScreenResult());
    final Integer screenResultId = screen1.getScreenResult().getEntityId();
    screenResultsDao.deleteScreenResult(screen1.getScreenResult());
    assertNull("in-memory screen has no screen result after delete from screen, ", screen1.getScreenResult());

    screen1 = genericEntityDao.findEntityByProperty(Screen.class, "screenNumber", 1);
    assertNull("screen1 has no screen result after delete and commit", screen1.getScreenResult());
    ScreenResult screenResult1 = genericEntityDao.findEntityById(ScreenResult.class, screenResultId);
    assertNull("screenResult1 was deleted from database", screenResult1);
  }

  /**
   * A ScreenResult's plateNumbers, wells, experimentWellCount, and positives
   * properties should be updated when a ResultValue is added to a
   * ScreenResult's DataColumn.
   */
  public void testScreenResultDerivedPersistentValues()
  {
    final SortedSet<Integer> expectedPlateNumbers = new TreeSet<Integer>();
    final SortedSet<Well> expectedWells = new TreeSet<Well>();
    final int[] expectedExperimentalWellCount = new int[1];
    final int[] expectedPositives = new int[1];
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Screen screen = MakeDummyEntities.makeDummyScreen(1);
        ScreenResult screenResult = screen.createScreenResult();
        DataColumn col1 = screenResult.createDataColumn("DataColumn1");
        col1.makePartitionPositiveIndicator();
        DataColumn col2 = screenResult.createDataColumn("DataColumn2");
        col2.makeBooleanPositiveIndicator();
        Library library = new Library(
          "library 1",
          "lib1",
          ScreenType.SMALL_MOLECULE,
          LibraryType.COMMERCIAL,
          1,
          1);
        String values[] = { null, "1", "2", "3" };
        for (int i = 1; i <= 10; ++i) {
          int plateNumber = i;
          expectedPlateNumbers.add(i);
          Well well = library.createWell(new WellKey(plateNumber, "A01"), LibraryWellType.EXPERIMENTAL);
          expectedWells.add(well);
          AssayWellControlType assayWellControlType = i % 2 == 0 ? null : AssayWellControlType.ASSAY_POSITIVE_CONTROL;
          AssayWell assayWell = screenResult.createAssayWell(well);
          boolean exclude = i % 8 == 0;
          String col1Value = values[i % 4];
          col1.createResultValue(assayWell, col1Value, exclude);
          col2.createResultValue(assayWell, "false", false);
          if (well.getLibraryWellType() == LibraryWellType.EXPERIMENTAL) {
            expectedExperimentalWellCount[0]++;
            if (!exclude && col1Value != null) {
              log.debug("result value " + col1Value + " is deemed a positive by this test");
              ++expectedPositives[0];
            }
          }
        }
        genericEntityDao.saveOrUpdateEntity(library);
        genericEntityDao.saveOrUpdateEntity(screen.getLeadScreener());
        genericEntityDao.saveOrUpdateEntity(screen.getLabHead());
        genericEntityDao.saveOrUpdateEntity(screen);
      }
    });
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Screen screen = genericEntityDao.findEntityByProperty(Screen.class, "screenNumber", 1);
        assertEquals("plate numbers", expectedPlateNumbers, screen.getScreenResult().getPlateNumbers());
        assertEquals("wells", expectedWells, screen.getScreenResult().getWells());
        assertEquals("experimental well count", expectedExperimentalWellCount[0], screen.getScreenResult().getExperimentalWellCount().intValue());
        assertEquals("positives", expectedPositives[0], screen.getScreenResult().getDataColumnsList().get(0).getPositivesCount().intValue());
        assertEquals("0 positives (but not null)", 0, screen.getScreenResult().getDataColumnsList().get(1).getPositivesCount().intValue());
      }
    });
  }


  public void testFindResultValuesByPlate()
  {
    final Screen screen = MakeDummyEntities.makeDummyScreen(1);
    ScreenResult screenResult = screen.createScreenResult();
    DataColumn col1 = screenResult.createDataColumn("Raw Value").makeNumeric(3);
    DataColumn col2 = screenResult.createDataColumn("Derived Value").makeNumeric(3);
    Library library = new Library(
      "library 1",
      "lib1",
      ScreenType.SMALL_MOLECULE,
      LibraryType.COMMERCIAL,
      1,
      1);
    for (int iPlate = 1; iPlate <= 3; ++iPlate) {
      int plateNumber = iPlate;
      for (int iWell = 0; iWell < 10; ++iWell) {
        Well well = library.createWell(new WellKey(plateNumber, "A" + (iWell + 1)), LibraryWellType.EXPERIMENTAL);
        AssayWell assayWell = screenResult.createAssayWell(well);
        col1.createResultValue(assayWell, (double) iWell);
        col2.createResultValue(assayWell, iWell + 10.0);
      }
    }
    genericEntityDao.saveOrUpdateEntity(library);
    genericEntityDao.saveOrUpdateEntity(screen.getLeadScreener());
    genericEntityDao.saveOrUpdateEntity(screen.getLabHead());
    genericEntityDao.saveOrUpdateEntity(screen);

    // test findResultValuesByPlate(Integer, DataColumn)
    Map<WellKey,ResultValue> resultValues1 = screenResultsDao.findResultValuesByPlate(2, col1);
    assertEquals("result values size", 10, resultValues1.size());
    for (int iWell = 0; iWell < 10; ++iWell) {
      ResultValue rv = resultValues1.get(new WellKey(2, 0, iWell));
      assertEquals("rv.value", new Double(iWell), rv.getNumericValue());
    }
  }
}
