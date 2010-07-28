// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.faces.component.UIData;
import javax.faces.model.DataModel;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.joda.time.LocalDate;
import org.springframework.util.comparator.NullSafeComparator;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.db.Query;
import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.db.accesspolicy.DefaultEntityViewPolicy;
import edu.harvard.med.screensaver.db.datafetcher.Tuple;
import edu.harvard.med.screensaver.db.datafetcher.TupleDataFetcher;
import edu.harvard.med.screensaver.io.DataExporter;
import edu.harvard.med.screensaver.io.libraries.smallmolecule.LibraryContentsVersionReference;
import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.MolecularFormula;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.DataColumn;
import edu.harvard.med.screensaver.model.screenresults.PartitionedValue;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.Study;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.ui.libraries.WellViewer;
import edu.harvard.med.screensaver.ui.table.Criterion;
import edu.harvard.med.screensaver.ui.table.Criterion.Operator;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;
import edu.harvard.med.screensaver.ui.table.column.TableColumnManager;
import edu.harvard.med.screensaver.ui.table.column.entity.HasFetchPaths;
import edu.harvard.med.screensaver.ui.table.model.DataTableModel;

/**
 * High-level test covering DataTable, TableColumnManager, SearchResults, and
 * WellSearchResults.
 */
public class WellSearchResultsTest extends AbstractSpringPersistenceTest
{
  // static members

  private static Logger log = Logger.getLogger(WellSearchResultsTest.class);

  // instance data members

  protected LibrariesDAO librariesDao;

  private WellSearchResults _wellSearchResults;
  private WellViewer _wellViewer;

  private static boolean oneTimeDataSetup = false;
  private static Library _bigSmallMoleculeLibrary;
  private static Library _smallSmallMoleculeLibrary;
  private static SortedSet<WellKey> _smallWellKeys;
  private static SortedSet<WellKey> _bigWellKeys;
  private static SortedSet<WellKey> _allWellKeys;
  private static ScreenResult _screenResult;
  private static Screen _study;


  // public constructors and methods

  @Override
  protected void onSetUp() throws Exception
  {
    if (!oneTimeDataSetup) {
      oneTimeDataSetup = true;
      super.onSetUp();

      _bigSmallMoleculeLibrary = MakeDummyEntities.makeDummyLibrary(1,
                                                                    ScreenType.SMALL_MOLECULE,
                                                                    3 /* must be large enough to trigger use of VirtualPagingDataModel */);
      addUnreleasedContentsVersion(_bigSmallMoleculeLibrary);
      genericEntityDao.persistEntity(_bigSmallMoleculeLibrary);
      _bigWellKeys = new TreeSet<WellKey>();
      for (Well well : _bigSmallMoleculeLibrary.getWells()) {
        _bigWellKeys.add(well.getWellKey());
      }
      
      _smallSmallMoleculeLibrary = MakeDummyEntities.makeDummyLibrary(2,
                                                                      ScreenType.SMALL_MOLECULE,
                                                                      1);
      addUnreleasedContentsVersion(_smallSmallMoleculeLibrary);
      genericEntityDao.persistEntity(_smallSmallMoleculeLibrary);
      _smallWellKeys = new TreeSet<WellKey>();
      for (Well well : _smallSmallMoleculeLibrary.getWells()) {
        _smallWellKeys.add(well.getWellKey());
      }

      _allWellKeys = new TreeSet<WellKey>();
      _allWellKeys.addAll(_bigWellKeys);
      _allWellKeys.addAll(_smallWellKeys);

      _screenResult = setupScreenResult();
      _study = setupStudy();

      genericEntityDao.doInTransaction(new DAOTransaction() {
        public void runTransaction() {
          Library rnaiLibrary = MakeDummyEntities.makeDummyLibrary(3, ScreenType.RNAI, 1);
          genericEntityDao.persistEntity(rnaiLibrary);
          Screen rnaiScreen = MakeDummyEntities.makeDummyScreen(3, ScreenType.RNAI);
          MakeDummyEntities.makeDummyScreenResult(rnaiScreen, rnaiLibrary);
          genericEntityDao.saveOrUpdateEntity(rnaiScreen.getLeadScreener());
          genericEntityDao.saveOrUpdateEntity(rnaiScreen.getLabHead());
          genericEntityDao.persistEntity(rnaiScreen);

          for (Well well : rnaiLibrary.getWells()) {
            _allWellKeys.add(well.getWellKey());
          }
        }
      });
    }

    _wellViewer = new WellViewer(null, null, genericEntityDao, librariesDao, new DefaultEntityViewPolicy(), null, null, null, null, null);
    _wellSearchResults = new WellSearchResults(genericEntityDao,
                                               librariesDao,
                                               new DefaultEntityViewPolicy(),
                                               null,
                                               _wellViewer,
                                               null,
                                               new LibraryContentsVersionReference(),
                                               Collections.<DataExporter<Tuple<String>>>emptyList());
    _wellSearchResults.setDataTableUIComponent(new UIData());
  }

  /** for testing that multiple contents versions are handled in a disjoint manner */
  private void addUnreleasedContentsVersion(Library library)
  {
    library.createContentsVersion(new AdministrativeActivity((AdministratorUser) library.getLatestReleasedContentsVersion().getLoadingActivity().getPerformedBy(), 
                                                             new LocalDate(), 
                                                             AdministrativeActivityType.LIBRARY_CONTENTS_LOADING));
    for (Well well : library.getWells()) {
      SmallMoleculeReagent latestReleasedReagent = well.getLatestReleasedReagent();
      well.createSmallMoleculeReagent(latestReleasedReagent.getVendorId(),
                                      latestReleasedReagent.getMolfile() +
                                        "(2)",
                                      latestReleasedReagent.getSmiles() +
                                        "(2)",
                                      latestReleasedReagent.getInchi() +
                                        "(2)",
                                      latestReleasedReagent.getMolecularMass()
                                                           .add(new BigDecimal(1)),
                                      latestReleasedReagent.getMolecularWeight()
                                                           .add(new BigDecimal(1)),
                                      new MolecularFormula(latestReleasedReagent.getMolecularFormula() +
                                                           "(2)"));
    }
  }

  private void setOrderBy(WellSearchResults wsr)
  {
    TableColumnManager<Tuple<String>> columnManager = wsr.getColumnManager();
    columnManager.addCompoundSortColumns(columnManager.getColumn("Plate"),
                                         columnManager.getColumn("Well"));
    columnManager.setSortColumnName("Plate");
    wsr.resort(); // necessary, since primary sort column may not have actually changed, although the compound sort columns have
  }
  
  public void testSearchAllWellsIsInitiallyEmpty()
  {
    _wellSearchResults.searchAll();
    assertEquals("search result is initially empty", 0, _wellSearchResults.getRowCount());
    ((Criterion<String>) _wellSearchResults.getColumnManager().getColumn("Well").getCriterion()).setOperatorAndValue(Operator.TEXT_STARTS_WITH, "B");
    _wellSearchResults.searchCommandListener(null);
    assertTrue("search result is non-empty after explicit search command listener is invoked", 0 != _wellSearchResults.getRowCount());
  }

  /**
   * Tests WellSearchResult of all wells from all libraries.
   */
  public void testAllEntitiesOfType()
  {
    WellSearchResults wsr = _wellSearchResults;
    wsr.searchAll();
    wsr.searchCommandListener(null); // invoke search now (necessary when using searchAll(), above)
    setOrderBy(wsr);
    wsr.clearFilter();
    DataTableModel model = wsr.getDataTableModel();
    verifySearchResult(wsr, model, _allWellKeys);
  }

  public void testEntitySet()
  {
    WellSearchResults wsr = _wellSearchResults;
    wsr.searchWells(_bigWellKeys);
    setOrderBy(wsr);
    DataTableModel model = wsr.getDataTableModel();
    verifySearchResult(wsr, model, _bigWellKeys);
  }

  public void testParentedEntity()
  {
    WellSearchResults wsr = _wellSearchResults;
    // reload library to ensure LCV.equals() works in Well.getReagent() 
    Library library =
      genericEntityDao.reloadEntity(_bigSmallMoleculeLibrary, true, Library.wells.to(Well.latestReleasedReagent).getPath());
    wsr.searchWellsForLibrary(library);
    setOrderBy(wsr);
    DataTableModel model = wsr.getDataTableModel();
    verifySearchResult(wsr, model, _bigWellKeys);
  }

  /**
   * Tests that WellSearchResults handles columns for DataColumns and AnnotationTypes. Also tests in such a way that
   * these columns are added after the initial, basic search results is created and tested; this ensures that column
   * addition logic is working properly.
   */
  public void testScreenResultDataColumnAndStudyAnnotationColumns()
  {
    WellSearchResults wsr = _wellSearchResults;

    Set<TableColumn<?,?>> columns = Sets.newHashSet();
    wsr.searchAll(); // initialize columns
    for (DataColumn dataColumn : _screenResult.getDataColumns()) {
      columns.add(_wellSearchResults.getColumnManager().getColumn(WellSearchResults.makeColumnName(dataColumn, dataColumn.getScreenResult().getScreen().getScreenNumber())));
    }
    for (AnnotationType annotType : _study.getAnnotationTypes()) {
      columns.add(_wellSearchResults.getColumnManager().getColumn(WellSearchResults.makeColumnName(annotType, annotType.getStudy().getScreenNumber())));
    }
    assertTrue("table columns exist for all screen result data columns and study annotation types", Iterables.all(columns, Predicates.notNull()));
    assertEquals("table columns exist for all screen result data columns and study annotation types", _screenResult.getDataColumns().size() +
      _study.getAnnotationTypes().size(), columns.size());

    wsr.searchWellsForScreenResult(_screenResult);
    verifyScreenResultData(wsr,
                           _bigWellKeys,
                           columns);

    SortedSet<WellKey> expectedWellKeys = _bigWellKeys.headSet(new WellKey("01001:A01"));
    wsr.searchWells(expectedWellKeys);
    verifyScreenResultData(wsr,
                           expectedWellKeys,
                           columns);
  }

  private void verifyScreenResultData(WellSearchResults wsr,
                                      SortedSet<WellKey> expectedKeys,
                                      Set<TableColumn<?,?>> dataColumns)
  {
    setOrderBy(wsr);
    DataTableModel model = wsr.getDataTableModel();
    verifySearchResult(wsr, model, expectedKeys);

    // now add the data columns and test them
    for (TableColumn<?,?> column : dataColumns) {
      wsr.getColumnManager().getColumn(column.getName()).setVisible(true);
    }
    verifySearchResult(wsr, model, expectedKeys);

    // test the data columns
    int j = 0;
    for (WellKey expectedWellKey : expectedKeys) {
      model.setRowIndex(j++);
      assertEquals("row data " + j,
                     expectedWellKey.toString(),
                     ((Tuple<String>) model.getRowData()).getKey());
      Set<TableColumn<?,?>> columnsTested = Sets.newHashSet();
      for (TableColumn<Tuple<String>,?> column : wsr.getColumnManager().getVisibleColumns()) {
        if (dataColumns.contains(column)) {
          Tuple<String> tuple = (Tuple<String>) model.getRowData();
          Object actualCellValue = column.getCellValue(tuple);
          assertEquals("row " + j + ":" + column.getName(),
                       tuple.getProperty(TupleDataFetcher.makePropertyKey(((HasFetchPaths) column).getPropertyPath())),
                       actualCellValue);
          columnsTested.add(column);
        }
      }
      assertEquals("all extra columns tested for row " + j, dataColumns, columnsTested);
    }

    doTestSortForAllColumnsAndDirections(wsr, expectedKeys.size());
  }

  public void testFilterScreenResultResultValues()
  {
    WellSearchResults wsr = _wellSearchResults;
    wsr.searchWellsForScreenResult(_screenResult);
    setOrderBy(wsr);

    // initialize search result filter to select only the first well
    SortedSet<WellKey> expectedKeys = ImmutableSortedSet.of(new WellKey("01000:A01"), new WellKey("01000:A05")); 

    TableColumn<Tuple<String>,String> filterColumn1 =
      (TableColumn<Tuple<String>,String>) wsr.getColumnManager().getColumn("text repl1 [1]");
    filterColumn1.clearCriteria().addCriterion(new Criterion<String>(Operator.LESS_THAN, "text00008"));
    TableColumn<Tuple<String>,PartitionedValue> filterColumn2 =
      (TableColumn<Tuple<String>,PartitionedValue>) wsr.getColumnManager().getColumn("positive [1]");
    filterColumn2.clearCriteria().addCriterion(new Criterion<PartitionedValue>(Operator.EQUAL, PartitionedValue.STRONG));
    // TODO: also test a numeric DataColumn column

    DataTableModel model = wsr.getDataTableModel();
    verifySearchResult(wsr, model, expectedKeys);
  }

  // TODO: must test via a manual UI test, since entity viewer updates only occur from UI data table scrolling events, not model.setRowIndex()
//  public void testEntityModeScrolling()
//  {
//    wsr.searchAll();
//    setOrderBy();
//    wsr.getRowsPerPageSelector().setSelection(wsr.getRowsPerPageSelector().getDefaultSelection());
//    assertFalse("summary view mode", wsr.isEntityView());
//    wsr.getRowsPerPageSelector().setSelection(1);
//    assertTrue("entity view mode", wsr.isEntityView());
//
//    DataTableModel model = wsr.getDataTableModel();
//    assertTrue("VirtualPagingDataModel used",
//               wsr.getBaseDataTableModel() == DataTableModelType.VIRTUAL_PAGING);
//    List<WellKey> expectedWellKeys = new ArrayList<WellKey>(_allWellKeys).subList(0, 3);
//    model.setRowIndex(0);
//    assertEquals("entity 0", expectedWellKeys.get(0), _wellViewer.getWell().getWellKey());
//    model.setRowIndex(1);
//    assertEquals("entity 1", expectedWellKeys.get(1), _wellViewer.getWell().getWellKey());
//    model.setRowIndex(2);
//    assertEquals("entity 2", expectedWellKeys.get(2), _wellViewer.getWell().getWellKey());
//
//    wsr.getRowsPerPageSelector().setSelection(wsr.getRowsPerPageSelector().getDefaultSelection());
//    assertEquals("returning to summary mode, with last-viewed entity shown as first row",
//                 2,
//                 wsr.getDataTableUIComponent().getFirst());
//    assertEquals("last-viewed entity shown as first row",
//                 expectedWellKeys.get(2),
//                 ((Well) wsr.getDataTableModel().getRowData()).getWellKey());
//  }

  public void testFilterOnColumns()
  {
    WellSearchResults wsr = _wellSearchResults;
    wsr.searchAll();
    wsr.searchCommandListener(null); // invoke search now (necessary when using searchAll(), above

    Map<String,Object> columnNameToExpectedValue = new HashMap<String,Object>();
    
    // test 2 filter columns
    columnNameToExpectedValue.put("Plate", Integer.valueOf(1000));
    columnNameToExpectedValue.put("Well", "H01");
    doTestFilterOnColumns(wsr, columnNameToExpectedValue, 1);

    // test list of text
    columnNameToExpectedValue.clear();
    columnNameToExpectedValue.put("Compound Names", "compound" + 2);
    wsr.getColumnManager().getColumn("Compound Names").setVisible(true);
    doTestFilterOnColumns(wsr, columnNameToExpectedValue, 2);

    // test list of integers
    columnNameToExpectedValue.clear();
    columnNameToExpectedValue.put("PubChem CIDs", 10002);
    wsr.getColumnManager().getColumn("PubChem CIDs").setVisible(true);
    doTestFilterOnColumns(wsr, columnNameToExpectedValue, 2);
    
    // tests the well-to-reagent relationship
    columnNameToExpectedValue.clear();
    columnNameToExpectedValue.put("Reagent ID", "sm179");
    doTestFilterOnColumns(wsr, columnNameToExpectedValue, 2);

    // tests the well-to-reagent-to-annotationValue relationship
    columnNameToExpectedValue.clear();
    columnNameToExpectedValue.put("text annot [100000]", "bbb");
    wsr.getColumnManager().getColumn("text annot [100000]").setVisible(true);
    doTestFilterOnColumns(wsr, columnNameToExpectedValue, 1);
    
    // tests the well-to-reagent-to-annotationValue relationship, 
    // looking for an old reagent not mapped by the latest LCV 
    columnNameToExpectedValue.clear();
    columnNameToExpectedValue.put("text annot [100000]", "bbb (non-released)");
    wsr.getColumnManager().getColumn("text annot [100000]").setVisible(true);
    doTestFilterOnColumns(wsr, columnNameToExpectedValue, 0);    
    // this is the value that would be expected if [#2251] data table filtering not restricting properly on study annotation columns
    // were not implemented
    //doTestFilterOnColumns(wsr, columnNameToExpectedValue, 384*3);    
  }
  
  public void testSortColumns(WellSearchResults wsr)
  {
    wsr.searchWellsForLibrary(_bigSmallMoleculeLibrary);
    doTestSortForAllColumnsAndDirections(wsr, _bigSmallMoleculeLibrary.getWells().size());

    wsr.searchWellsForLibrary(_smallSmallMoleculeLibrary);
    doTestSortForAllColumnsAndDirections(wsr, _smallSmallMoleculeLibrary.getWells().size());

    wsr.searchWellsForLibraryContentsVersion(_bigSmallMoleculeLibrary.getLatestContentsVersion());
    doTestSortForAllColumnsAndDirections(wsr, _bigSmallMoleculeLibrary.getWells().size());

    wsr.searchWellsForLibraryContentsVersion(_smallSmallMoleculeLibrary.getLatestContentsVersion());
    doTestSortForAllColumnsAndDirections(wsr, _smallSmallMoleculeLibrary.getWells().size());
  }

  /**
   * For the set of columnName/value pairs, filter the search result on those
   * columns (simultaneously), using 'equals' operator, and verify that all
   * filtered rows having matching values in the filtered columns.
   * 
   * @param <R>
   * @param searchResults
   * @param columnNameToExpectedValue
   */
  private <R> void doTestFilterOnColumns(SearchResults<R,?,?> searchResults,
                                         Map<String,Object> columnNameToExpectedValue,
                                         int expectedRowCount)
  {
    DataTableModel<R> model = searchResults.getDataTableModel();
    TableColumnManager<R> columnManager = searchResults.getColumnManager();
    searchResults.clearFilter();
    for (String columnName : columnNameToExpectedValue.keySet()) {
      TableColumn<R,?> column = columnManager.getColumn(columnName);
      assertNotNull("column " + columnName + " exists", column);
      Object expectedValue = columnNameToExpectedValue.get(column.getName());
      Criterion<Object> criterion = new Criterion<Object>(Operator.EQUAL, expectedValue);
      column.clearCriteria().addCriterion((Criterion) criterion);
    }    
    assertEquals("filtered row count", expectedRowCount, model.getRowCount());
    for (String columnName : columnNameToExpectedValue.keySet()) {
      Object expectedValue = columnNameToExpectedValue.get(columnName);
      int columnIndex = columnManager.getVisibleColumns().indexOf(columnManager.getColumn(columnName));
      assert columnIndex >= 0;
      columnManager.getVisibleColumnModel().setRowIndex(columnIndex);
      for (int i = 0; i < model.getRowCount(); ++i) {
        model.setRowIndex(i);
        if (columnManager.getColumn(columnIndex).isMultiValued()) {
          assertTrue("filtered on column " + columnName + "=" +  expectedValue + ", row " + i, 
                     ((Set<?>) searchResults.getCellValue()).contains(expectedValue)); 
        }
        else {
          assertEquals("filtered on column " + columnName + "=" +  expectedValue + ", row " + i, 
                       expectedValue, 
                       searchResults.getCellValue());
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private <R> void doTestSortForAllColumnsAndDirections(SearchResults<R,?,?> searchResults, int expectedSize)
  {
    for (SortDirection sortDirection : SortDirection.values()) {
      for (TableColumn<R,?> sortColumn : searchResults.getColumnManager().getVisibleColumns()) {
        doTestSort(searchResults.getDataTableModel(),
                   sortColumn,
                   sortDirection,
                   expectedSize);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private <R> void doTestSort(DataTableModel<R> dataModel,
                              TableColumn<R,?> sortColumn,
                              SortDirection sortDirection,
                              int expectedSize)
  {
    log.info("testing sort on " + sortColumn + " in " + sortDirection);
    dataModel.sort(Arrays.asList(sortColumn), sortDirection);
    assertEquals("row count after sort", expectedSize, dataModel.getRowCount()); // ensure nothing funky occurring on query when sorting on entity types that are related to table's root entity (i.e. cross-product query results)
    List<Comparable> actualSortedValues = new ArrayList<Comparable>();
    List<Comparable> expectedSortedValues = new ArrayList<Comparable>();
    for (int i = 0; i < expectedSize; i++) {
      dataModel.setRowIndex(i);
      R rowData = (R) dataModel.getRowData();
      assertNotNull("row data not null for row " + i + ", column " + sortColumn,
                    rowData);
      actualSortedValues.add((Comparable) sortColumn.getCellValue(rowData));
    }
    expectedSortedValues.addAll(actualSortedValues);
    Collections.sort(expectedSortedValues, NullSafeComparator.NULLS_HIGH);
    if (sortDirection == SortDirection.DESCENDING) {
      Collections.reverse(expectedSortedValues);
    }
    assertEquals("sorted values on " + sortColumn.getName() + ", " + sortDirection,
                 expectedSortedValues,
                 actualSortedValues);
  }

  private void doTestColumnsCreated(String testDescription,
                                    WellSearchResults wellSearchResults,
                                    List<String> columnNames,
                                    boolean expectedToExist)
  {
    for (String columnName : columnNames) {
      TableColumn<Tuple<String>,?> column = wellSearchResults.getColumnManager().getColumn(columnName);
      if (expectedToExist) {
        assertNotNull(testDescription + ": column " + columnName + " exists", column);
      }
      else {
        assertNull(testDescription + ": column " + columnName + " does not exist", column);
      }
    }
  }

  private ScreenResult setupScreenResult()
  {
    genericEntityDao.runQuery(new Query() {
      public List execute(Session session) {
        Library library = genericEntityDao.reloadEntity(_bigSmallMoleculeLibrary,
                                                        true);
        Screen screen = MakeDummyEntities.makeDummyScreen(1, ScreenType.SMALL_MOLECULE);
        MakeDummyEntities.makeDummyScreenResult(screen, library);
        genericEntityDao.persistEntity(screen.getLeadScreener());
        genericEntityDao.persistEntity(screen.getLabHead());
        genericEntityDao.persistEntity(screen);
        return Arrays.asList(screen.getScreenResult());
      } });
    // reload from database to ensure objects have entity-ID-based hashCodes, not Object hashCodes
    return (ScreenResult)
    genericEntityDao.runQuery(new Query() {
      public List execute(Session session) {
        Screen screen = genericEntityDao.findEntityByProperty(Screen.class, "screenNumber", 1, true);
        genericEntityDao.needReadOnly(screen.getScreenResult(), "dataColumns.resultValues");
        return Arrays.asList(screen.getScreenResult());
      }
    }).get(0);
  }

  private Screen setupStudy()
  {
    genericEntityDao.runQuery(new Query() {
      public List execute(Session session) {
        Library library = genericEntityDao.reloadEntity(_bigSmallMoleculeLibrary,
                                                        true,
                                                        Library.wells.getPath());
        Study study = MakeDummyEntities.makeDummyStudy(library);

        
        genericEntityDao.persistEntity(study.getLeadScreener());
        genericEntityDao.persistEntity(study.getLabHead());
        genericEntityDao.persistEntity(study);
        return Arrays.asList(study);
      } });
    // reload from database to ensure objects have entity-ID-based hashCodes, not Object hashCodes
    return (Screen)
    genericEntityDao.runQuery(new Query() {
      public List execute(Session session) {
        Screen screen = genericEntityDao.findEntityByProperty(Screen.class, "screenNumber", Study.MIN_STUDY_NUMBER, true);
        genericEntityDao.needReadOnly(screen, "reagents");
        genericEntityDao.needReadOnly(screen, "annotationTypes.annotationValues");
        return Arrays.asList(screen);
      }
    }).get(0);
  }

  private void verifySearchResult(WellSearchResults wsr,
                                  DataModel model,
                                  SortedSet<WellKey> expectedWellKeys)
  {
    wsr.getColumnManager().getColumn("Compound SMILES").setVisible(true);
    wsr.getColumnManager().getColumn("Gene Name").setVisible(true);
    assertEquals("row count", expectedWellKeys.size(), model.getRowCount());
    int j = 0;
    for (WellKey expectedWellKey : expectedWellKeys) {
      model.setRowIndex(j++);
      Tuple<String> rowData = (Tuple<String>) model.getRowData();
      assertEquals("row data " + j, expectedWellKey.toString(), rowData.getKey());
      List<TableColumn<Tuple<String>,?>> columnsTested = Lists.newArrayList();
      for (TableColumn<Tuple<String>,?> column : wsr.getColumnManager().getVisibleColumns()) {
        Object cellValue = column.getCellValue(rowData);
        if (column.getName().equals("Library")) {
          assertEquals("row " + j + ":Library",
                         librariesDao.findLibraryWithPlate(expectedWellKey.getPlateNumber()).getLibraryName(),
                         (String) cellValue);
          columnsTested.add(column);
        }
        else if (column.getName().equals("Plate")) {
          assertEquals("row " + j + ":Plate",
                         (Integer) expectedWellKey.getPlateNumber(),
                         (Integer) cellValue);
          columnsTested.add(column);
        }
        else if (column.getName().equals("Well")) {
          assertEquals("row " + j + ":Well",
                         expectedWellKey.getWellName(),
                         (String) cellValue);
          columnsTested.add(column);
        }
        else if (column.getName().equals("Compound SMILES")) {
          if (rowData.getProperty("screenType") == ScreenType.SMALL_MOLECULE) {
            String smiles = (String) cellValue;
            assertEquals("row " + j + ":Compounds", "smiles" + expectedWellKey, smiles);
          }
          columnsTested.add(column);
        }
        else if (column.getName().equals("Gene Name")) {
          if (rowData.getProperty("screenType") == ScreenType.RNAI) {
            assertEquals("row " + j + ":SilencingReagent",
                           "geneName" + expectedWellKey,
                           (String) cellValue);
          }
          columnsTested.add(column);
        }
      }
      //log.debug("test columns: " + columnsTested);
      assertEquals("tested all columns for row " + j, 5, columnsTested.size());
    }
  }
}
