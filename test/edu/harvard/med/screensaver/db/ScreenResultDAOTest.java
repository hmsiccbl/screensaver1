// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.springframework.test.AbstractTransactionalSpringContextTests;

import com.google.common.collect.Sets;

import edu.harvard.med.screensaver.io.screenresults.ScreenResultParserTest;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.LibraryWellType;
import edu.harvard.med.screensaver.model.libraries.PlateSize;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.AssayWell;
import edu.harvard.med.screensaver.model.screenresults.DataColumn;
import edu.harvard.med.screensaver.model.screenresults.PartitionedValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenDataSharingLevel;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.ui.libraries.WellSearchResults;

/**
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class ScreenResultDAOTest extends AbstractTransactionalSpringContextTests
{

  private static final Logger log = Logger.getLogger(ScreenResultDAOTest.class);

  protected GenericEntityDAO genericEntityDao;
  protected ScreenResultsDAO screenResultsDao;
  protected LibrariesDAO librariesDao;
  protected SchemaUtil schemaUtil;

  @Override
  protected String[] getConfigLocations()
  {
    return new String[] { "spring-context-test.xml" };
  }

  public ScreenResultDAOTest() 
  {
    setPopulateProtectedVariables(true);
  }
  
  @Override
  protected void onSetUpBeforeTransaction() throws Exception
  {
    schemaUtil.truncateTablesOrCreateSchema();
  }
  
  
  public void testFindMutualPositiveColumns()
  {
    //    To test, create 4 screens:
    //      1. "my" screen
    //      2. "others" screen w/no overlapping wells, but with some positives anyway
    //      3. "others" screen w/overlapping wells, but no overlapping positives
    //      4. "others" screen w/overlapping wells, with some overlapping positives
    // the test should assert that the query only returns screen 4 
    // create My screen
    Library library = MakeDummyEntities.makeDummyLibrary(1,ScreenType.SMALL_MOLECULE,1);
    Iterator<Well> wellsIter = library.getWells()
                                      .iterator();
    Well overLapWell1 = wellsIter.next();
    Well overLapWell2 = wellsIter.next();
    Well overLapWell3 = wellsIter.next();
    Well nonOverlapWell1 = wellsIter.next();
    Well nonOverlapWell2 = wellsIter.next();

    Screen myScreen = MakeDummyEntities.makeDummyScreen(0,
                                                        ScreenType.SMALL_MOLECULE);
    myScreen.setDataSharingLevel(ScreenDataSharingLevel.MUTUAL_POSITIVES);

    ScreenResult screenResult = myScreen.createScreenResult();
    DataColumn col = screenResult.createDataColumn("col1")
                                 .forReplicate(1);
    col.makeBooleanPositiveIndicator();
    AssayWell assayWell = screenResult.createAssayWell(overLapWell1);
    ResultValue resultValue = col.createBooleanPositiveResultValue(assayWell, true, false);
    assert resultValue.isPositive();
    assayWell = screenResult.createAssayWell(overLapWell2);
    resultValue = col.createBooleanPositiveResultValue(assayWell, false, false);
    assert !resultValue.isPositive();
    assayWell = screenResult.createAssayWell(overLapWell3);
    resultValue = col.createBooleanPositiveResultValue(assayWell, true, false);
    assert resultValue.isPositive();

    Screen screenWithNoOverlaps = MakeDummyEntities.makeDummyScreen(1,
                                                                    ScreenType.SMALL_MOLECULE);
    screenWithNoOverlaps.setDataSharingLevel(ScreenDataSharingLevel.MUTUAL_POSITIVES);

    screenResult = screenWithNoOverlaps.createScreenResult();
    col = screenResult.createDataColumn("col2")
                      .forReplicate(1);
    col.makeBooleanPositiveIndicator();
    assayWell = screenResult.createAssayWell(nonOverlapWell1);
    resultValue = col.createBooleanPositiveResultValue(assayWell, true, false);
    assert resultValue.isPositive();
    assayWell = screenResult.createAssayWell(nonOverlapWell2);
    resultValue = col.createBooleanPositiveResultValue(assayWell, false, false);
    assert !resultValue.isPositive();

    Screen screenWithOverlapNegative = MakeDummyEntities.makeDummyScreen(2,
                                                                         ScreenType.SMALL_MOLECULE);
    screenWithOverlapNegative.setDataSharingLevel(ScreenDataSharingLevel.MUTUAL_POSITIVES);

    screenResult = screenWithOverlapNegative.createScreenResult();
    col = screenResult.createDataColumn("col1")
                      .forReplicate(1);
    col.makeBooleanPositiveIndicator();
    assayWell = screenResult.createAssayWell(overLapWell1);
    resultValue = col.createBooleanPositiveResultValue(assayWell, false, false);
    assert !resultValue.isPositive();
    assayWell = screenResult.createAssayWell(overLapWell2);
    resultValue = col.createBooleanPositiveResultValue(assayWell, false, false);
    assert !resultValue.isPositive();
    assayWell = screenResult.createAssayWell(nonOverlapWell2);
    resultValue = col.createBooleanPositiveResultValue(assayWell, true, false);
    assert resultValue.isPositive();

    Screen screenWithOverlapNegative1 = MakeDummyEntities.makeDummyScreen(21,
                                                                          ScreenType.SMALL_MOLECULE);
    screenWithOverlapNegative1.setDataSharingLevel(ScreenDataSharingLevel.MUTUAL_POSITIVES);

    screenResult = screenWithOverlapNegative1.createScreenResult();
    // make positive that doesn't overlap
    col = screenResult.createDataColumn("col1")
                      .forReplicate(1);
    DataColumn positiveNonOverlapColumn = col;
    col.makeBooleanPositiveIndicator();
    assayWell = screenResult.createAssayWell(overLapWell2);
    resultValue = col.createBooleanPositiveResultValue(assayWell, true, false);
    assert resultValue.isPositive();
    assayWell = screenResult.createAssayWell(overLapWell3);
    resultValue = col.createBooleanPositiveResultValue(assayWell, false, false);
    assert !resultValue.isPositive();
    assayWell = screenResult.createAssayWell(nonOverlapWell2);
    resultValue = col.createBooleanPositiveResultValue(assayWell, true, false);
    assert resultValue.isPositive();

    Screen screenWithOverlapPositive = MakeDummyEntities.makeDummyScreen(3,
                                                                         ScreenType.SMALL_MOLECULE);
    screenWithOverlapPositive.setDataSharingLevel(ScreenDataSharingLevel.MUTUAL_POSITIVES);

    screenResult = screenWithOverlapPositive.createScreenResult();
    // make a mutual positive column
    DataColumn mutualColumn = screenResult.createDataColumn("col1").forReplicate(1);
    col = mutualColumn;
    col.makeBooleanPositiveIndicator();
    assayWell = screenResult.createAssayWell(overLapWell1);
    resultValue = col.createBooleanPositiveResultValue(assayWell, true, false); // this is the
                                                            // mutual positive
    assert resultValue.isPositive();
    assayWell = screenResult.createAssayWell(overLapWell2);
    resultValue = col.createBooleanPositiveResultValue(assayWell, true, false);
    assert resultValue.isPositive();
    assayWell = screenResult.createAssayWell(overLapWell3);
    resultValue = col.createBooleanPositiveResultValue(assayWell, false, false);
    assert !resultValue.isPositive();
    assayWell = screenResult.createAssayWell(nonOverlapWell2);
    resultValue = col.createBooleanPositiveResultValue(assayWell, true, false);
    assert resultValue.isPositive();


    genericEntityDao.persistEntity(library);
    genericEntityDao.persistEntity(myScreen);
    genericEntityDao.persistEntity(screenWithNoOverlaps);
    genericEntityDao.persistEntity(screenWithOverlapNegative);
    genericEntityDao.persistEntity(screenWithOverlapNegative1);
    genericEntityDao.persistEntity(screenWithOverlapPositive);
    genericEntityDao.flush();

    setComplete();
    endTransaction();

    startNewTransaction();

    List<DataColumn> columns = screenResultsDao.findMutualPositiveColumns(myScreen.getScreenResult());
    for (DataColumn dc : columns) {
      log.info("return dataColumn: " +
               WellSearchResults.makeColumnName(dc, dc.getScreenResult()
                                                      .getScreen()
                                                      .getFacilityId()));
    }
    assertEquals("should only find one mutual column", 1, columns.size());
    assertTrue("should contain the mutual column: " + mutualColumn,
               columns.contains(mutualColumn));
    assertFalse("should not contain the positiveNonOverlapColumn column: " +
                  positiveNonOverlapColumn,
                columns.contains(positiveNonOverlapColumn));
          
    
  }

  public void testDerivedScreenResults()
  {
    int replicates = 3;
    SortedSet<DataColumn> derivedColSet1 = new TreeSet<DataColumn>();
    SortedSet<DataColumn> derivedColSet2 = new TreeSet<DataColumn>();

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

    setComplete();
    endTransaction();
    startNewTransaction();
          
    List<ScreenResult> screenResults = genericEntityDao.findAllEntitiesOfType(ScreenResult.class);
    screenResult = screenResults.get(0);
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

  // TODO: this unit test has been superceded by ScreenResultLoadedAndDeleterTest; should verify that all assertions tested here are duplicated there, and then remove this test
  public void testDeleteScreenResult()
  {
    Library library = MakeDummyEntities.makeDummyLibrary(1, ScreenType.SMALL_MOLECULE, 1);
    genericEntityDao.saveOrUpdateEntity(library);
    Screen screen1 = MakeDummyEntities.makeDummyScreen(1);
    MakeDummyEntities.makeDummyScreenResult(screen1, library);
    screen1.setLibraryPlatesDataLoadedCount(1);
    screen1.setMaxDataLoadedReplicateCount(2);
    screen1.setMinDataLoadedReplicateCount(2);
    genericEntityDao.saveOrUpdateEntity(screen1);
    setComplete();
    endTransaction();

    startNewTransaction();
    screen1 = genericEntityDao.findEntityByProperty(Screen.class, Screen.facilityId.getPropertyName(), "1");
    screen1.invalidate();
    screen1.update();
    setComplete();
    endTransaction();

    startNewTransaction();
    screen1 = genericEntityDao.findEntityByProperty(Screen.class, Screen.facilityId.getPropertyName(), "1");
    assertNotNull(screen1.getScreenResult());
    endTransaction();

    screenResultsDao.deleteScreenResult(screen1.getScreenResult());

    startNewTransaction();
    screen1 = genericEntityDao.findEntityByProperty(Screen.class, Screen.facilityId.getPropertyName(), "1", true);
    assertNull(screen1.getScreenResult());
    assertEquals(0, screen1.getLibraryPlatesDataLoadedCount());
    assertNull(screen1.getMaxDataLoadedReplicateCount());
    assertNull(screen1.getMinDataLoadedReplicateCount());
  }

  /**
   * A ScreenResult's plateNumbers, wells, experimentWellCount, and positives
   * properties should be updated when a ResultValue is added to a
   * ScreenResult's DataColumn.
   */
  public void testScreenResultDerivedPersistentValues()
  {
    SortedSet<AssayWell> expectedAssayWells = Sets.newTreeSet();
    int expectedExperimentalWellCount = 0;
    int expectedPositives = 0;

    Screen screen = MakeDummyEntities.makeDummyScreen(1);
    ScreenResult screenResult = screen.createScreenResult();
    DataColumn col1 = screenResult.createDataColumn("DataColumn1");
    col1.makePartitionPositiveIndicator();
    DataColumn col2 = screenResult.createDataColumn("DataColumn2");
    col2.makeBooleanPositiveIndicator();
    Library library = new Library((AdministratorUser) screen.getCreatedBy(),
                                  "library 1",
                                  "lib1",
                                  ScreenType.SMALL_MOLECULE,
                                  LibraryType.COMMERCIAL,
                                  1,
                                  1,
                                  PlateSize.WELLS_384);
    for (int i = 1; i <= 10; ++i) {
      int plateNumber = i;
      Well well = library.createWell(new WellKey(plateNumber, "A01"), LibraryWellType.EXPERIMENTAL);
      AssayWell assayWell = screenResult.createAssayWell(well);
      expectedAssayWells.add(assayWell);
      boolean exclude = i % 8 == 0;
      PartitionedValue col1Value = PartitionedValue.values()[i % 4];
      col1.createPartitionedPositiveResultValue(assayWell, col1Value, exclude);
      col2.createBooleanPositiveResultValue(assayWell, false, false);
      if (well.getLibraryWellType() == LibraryWellType.EXPERIMENTAL) {
        expectedExperimentalWellCount++;
        if (!exclude && col1Value != PartitionedValue.NOT_POSITIVE) {
          log.debug("result value " + col1Value + " is deemed a positive by this test");
          ++expectedPositives;
        }
      }
    }
    genericEntityDao.saveOrUpdateEntity(library);
    genericEntityDao.saveOrUpdateEntity(screen.getLeadScreener());
    genericEntityDao.saveOrUpdateEntity(screen.getLabHead());
    genericEntityDao.saveOrUpdateEntity(screen);

    setComplete();
    endTransaction();
    startNewTransaction();

    screen = genericEntityDao.findEntityByProperty(Screen.class, Screen.facilityId.getPropertyName(), "1");
    assertEquals("wells", expectedAssayWells, screen.getScreenResult().getAssayWells());
    assertEquals("experimental well count", expectedExperimentalWellCount, screen.getScreenResult().getExperimentalWellCount().intValue());
    assertEquals("positives", expectedPositives, screen.getScreenResult().getDataColumnsList().get(0).getPositivesCount().intValue());
    assertEquals("0 positives (but not null)", 0, screen.getScreenResult().getDataColumnsList().get(1).getPositivesCount().intValue());
  }


  public void testFindResultValuesByPlate()
  {
    final Screen screen = MakeDummyEntities.makeDummyScreen(1);
    ScreenResult screenResult = screen.createScreenResult();
    DataColumn col1 = screenResult.createDataColumn("Raw Value").makeNumeric(3);
    DataColumn col2 = screenResult.createDataColumn("Derived Value").makeNumeric(3);
    Library library = new Library((AdministratorUser) screen.getCreatedBy(),
                                  "library 1",
                                  "lib1",
                                  ScreenType.SMALL_MOLECULE,
                                  LibraryType.COMMERCIAL,
                                  1,
                                  1,
                                  PlateSize.WELLS_384);
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
    setComplete();
    endTransaction();

    startNewTransaction();
    // test findResultValuesByPlate(Integer, DataColumn)
    Map<WellKey,ResultValue> resultValues1 = screenResultsDao.findResultValuesByPlate(2, col1);
    assertEquals("result values size", 10, resultValues1.size());
    for (int iWell = 0; iWell < 10; ++iWell) {
      ResultValue rv = resultValues1.get(new WellKey(2, 0, iWell));
      assertEquals("rv.value", new Double(iWell), rv.getNumericValue());
    }
  }


}
