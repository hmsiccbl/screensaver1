// $HeadURL:
// svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/test/edu/harvard/med/screensaver/TestHibernate.java
// $
// $Id: ComplexDAOTest.java 2359 2008-05-09 21:16:57Z ant4 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
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
import edu.harvard.med.screensaver.model.screenresults.AssayWellType;
import edu.harvard.med.screensaver.model.screenresults.DataType;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
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
          ResultValueType[] rvt = new ResultValueType[replicates];
          for (int i = 0; i < replicates; i++) {
            rvt[i] = screenResult.createResultValueType("rvt" + i).forReplicate(i + 1).forPhenotype("human");
            rvt[i].setAssayReadoutType(i % 2 == 0 ? AssayReadoutType.PHOTOMETRY: AssayReadoutType.FLUORESCENCE_INTENSITY);
            if (i % 2 == 0) {
              rvt[i].makeBooleanPositiveIndicator();
            }
            else {
              rvt[i].makePartitionPositiveIndicator();
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
            AssayWell assayWell = screenResult.createAssayWell(wells[iWell], AssayWellType.EXPERIMENTAL);
            for (int iResultValue = 0; iResultValue < rvt.length; ++iResultValue) {
              rvt[iResultValue].createResultValue(assayWell,
                                                  "value " + iWell + "," + iResultValue,
                                                  iWell % 2 == 1);
            }
          }
          genericEntityDao.saveOrUpdateEntity(library);

          // test the calculation of replicateCount from child ResultValueTypes,
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
          SortedSet<ResultValueType> resultValueTypes = screenResult.getResultValueTypes();
          assertEquals(2, replicates);
          for (ResultValueType rvt : resultValueTypes) {
            assertEquals(
              screenResult,
              rvt.getScreenResult());
            assertEquals(
              iResultValue % 2 == 0 ? AssayReadoutType.PHOTOMETRY : AssayReadoutType.FLUORESCENCE_INTENSITY,
              rvt.getAssayReadoutType());
            assertEquals(
              iResultValue % 2 == 0 ? DataType.POSITIVE_INDICATOR_BOOLEAN: DataType.POSITIVE_INDICATOR_PARTITION,
              rvt.getDataType());
            assertEquals(
              "human",
              rvt.getAssayPhenotype());

            Map<WellKey,ResultValue> resultValues = rvt.getWellKeyToResultValueMap();
            for (WellKey wellKey : resultValues.keySet()) {
              assertTrue(wellKeys.contains(wellKey));
              // note that our naming scheme is testing the ordering of the
              // ResultValueType and ResultValue entities (within their parent
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
    final SortedSet<ResultValueType> derivedRvtSet1 = new TreeSet<ResultValueType>();
    final SortedSet<ResultValueType> derivedRvtSet2 = new TreeSet<ResultValueType>();
    genericEntityDao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          ScreenResult screenResult = ScreenResultParserTest.makeScreenResult();

          for (int i = 0; i < replicates; i++) {
            ResultValueType rvt = screenResult.createResultValueType("rvt" + i).forReplicate(1).forPhenotype("human");
            derivedRvtSet1.add(rvt);
            if (i % 2 == 0) {
              derivedRvtSet2.add(rvt);
            }
          }
          ResultValueType derivedRvt1 = screenResult.createResultValueType("derivedRvt1").forReplicate(1).forPhenotype("human");
          for (ResultValueType resultValueType : derivedRvtSet1) {
            derivedRvt1.addTypeDerivedFrom(resultValueType);
          }

          ResultValueType derivedRvt2 = screenResult.createResultValueType("derivedRvt2").forReplicate(1).forPhenotype("human");
          for (ResultValueType resultValueType : derivedRvtSet2) {
            derivedRvt2.addTypeDerivedFrom(resultValueType);
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
          SortedSet<ResultValueType> resultValueTypes =
            new TreeSet<ResultValueType>(screenResult.getResultValueTypes());

          ResultValueType derivedRvt = resultValueTypes.last();
          Set<ResultValueType> derivedFromSet = derivedRvt.getTypesDerivedFrom();
          assertEquals(derivedRvtSet2, derivedFromSet);

          resultValueTypes.remove(derivedRvt);
          derivedRvt = resultValueTypes.last();
          derivedFromSet = derivedRvt.getTypesDerivedFrom();
          assertEquals(derivedRvtSet1, derivedFromSet);
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
   * ScreenResult's ResultValueType.
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
        ResultValueType rvt1 = screenResult.createResultValueType("RVT1");
        rvt1.makePartitionPositiveIndicator();
        ResultValueType rvt2 = screenResult.createResultValueType("RVT2");
        rvt2.makeBooleanPositiveIndicator();
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
          AssayWellType assayWellType = i % 2 == 0 ? AssayWellType.EXPERIMENTAL : AssayWellType.ASSAY_POSITIVE_CONTROL;
          AssayWell assayWell = screenResult.createAssayWell(well, assayWellType);
          boolean exclude = i % 8 == 0;
          String rvt1Value = values[i % 4];
          rvt1.createResultValue(assayWell, rvt1Value, exclude);
          rvt2.createResultValue(assayWell, "false", false);
          if (assayWellType.equals(AssayWellType.EXPERIMENTAL)) {
            expectedExperimentalWellCount[0]++;
            if (!exclude && rvt1Value != null) {
              log.debug("result value " + rvt1Value + " is deemed a positive by this test");
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
        assertEquals("positives", expectedPositives[0], screen.getScreenResult().getResultValueTypesList().get(0).getPositivesCount().intValue());
        assertEquals("0 positives (but not null)", 0, screen.getScreenResult().getResultValueTypesList().get(1).getPositivesCount().intValue());
      }
    });
  }


  public void testFindResultValuesByPlate()
  {
    final Screen screen = MakeDummyEntities.makeDummyScreen(1);
    ScreenResult screenResult = screen.createScreenResult();
    ResultValueType rvt1 = screenResult.createResultValueType("Raw Value").makeNumeric(3);
    ResultValueType rvt2 = screenResult.createResultValueType("Derived Value").makeNumeric(3);
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
        AssayWell assayWell = screenResult.createAssayWell(well, AssayWellType.EXPERIMENTAL);
        rvt1.createResultValue(assayWell, (double) iWell);
        rvt2.createResultValue(assayWell, iWell + 10.0);
      }
    }
    genericEntityDao.saveOrUpdateEntity(library);
    genericEntityDao.saveOrUpdateEntity(screen.getLeadScreener());
    genericEntityDao.saveOrUpdateEntity(screen.getLabHead());
    genericEntityDao.saveOrUpdateEntity(screen);

    // test findResultValuesByPlate(Integer, RVT)
    Map<WellKey,ResultValue> resultValues1 = screenResultsDao.findResultValuesByPlate(2, rvt1);
    assertEquals("result values size", 10, resultValues1.size());
    for (int iWell = 0; iWell < 10; ++iWell) {
      ResultValue rv = resultValues1.get(new WellKey(2, 0, iWell));
      assertEquals("rv.value", new Double(iWell), rv.getNumericValue());
    }
  }
}
