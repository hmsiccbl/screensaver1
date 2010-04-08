// $HeadURL:
// svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml
// $
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.faces.component.UIData;
import javax.faces.model.DataModel;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.db.Query;
import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.db.accesspolicy.DefaultEntityViewPolicy;
import edu.harvard.med.screensaver.db.datafetcher.DataFetcher;
import edu.harvard.med.screensaver.db.datafetcher.EntityDataFetcher;
import edu.harvard.med.screensaver.db.datafetcher.Getter;
import edu.harvard.med.screensaver.io.DataExporter;
import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.MolecularFormula;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.AnnotationValue;
import edu.harvard.med.screensaver.model.screenresults.PartitionedValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.DataColumn;
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
import edu.harvard.med.screensaver.ui.table.model.DataTableModel;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.joda.time.LocalDate;
import org.springframework.util.comparator.NullSafeComparator;

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

  private WellSearchResults _inMemoryWellSearchResults;
  private WellSearchResults _virtualPagingWellSearchResults;
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

    _wellViewer = new WellViewer(null, null, genericEntityDao, librariesDao, new DefaultEntityViewPolicy(), null, null, null, null);
    _inMemoryWellSearchResults = new WellSearchResults(genericEntityDao,
                                                       new DefaultEntityViewPolicy(),
                                                       null,
                                                       _wellViewer,
                                                       null,
                                                       null,
                                                       Collections.<DataExporter<?>> emptyList()) {
    };
    _inMemoryWellSearchResults.setDataTableUIComponent(new UIData());

    _virtualPagingWellSearchResults = new WellSearchResults(genericEntityDao,
                                                            new DefaultEntityViewPolicy(),
                                                            null,
                                                            _wellViewer,
                                                            null,
                                                            null,
                                                            Collections.<DataExporter<?>> emptyList()) {
      @Override
      protected DataTableModel<Well> buildDataTableModel(DataFetcher<Well,String,PropertyPath<Well>> dataFetcher,
                                                         List<? extends TableColumn<Well,?>> columns) {
        if (dataFetcher instanceof EntityDataFetcher) {
          return new InMemoryEntitySearchResultsDataModel((EntityDataFetcher<Well,String>) dataFetcher);
        }
        return super.buildDataTableModel(dataFetcher, columns);
      }
    };
    _virtualPagingWellSearchResults.setDataTableUIComponent(new UIData());
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
    TableColumnManager<Well> columnManager = wsr.getColumnManager();
    columnManager.addCompoundSortColumns(columnManager.getColumn("Plate"),
                                         columnManager.getColumn("Well"));
    columnManager.setSortColumnName("Plate");
    wsr.resort(); // necessary, since primary sort column may not have actually changed, although the compound sort columns have
  }
  
  public void testSearchAllWellsIsInitiallyEmpty()
  {
    doTestSearchAllWellsIsInitiallyEmpty(_inMemoryWellSearchResults);
    doTestSearchAllWellsIsInitiallyEmpty(_virtualPagingWellSearchResults);
  }

  public void doTestSearchAllWellsIsInitiallyEmpty(WellSearchResults wsr)
  {
    wsr.searchAll();
    assertEquals("search result is initially empty", 0, wsr.getRowCount());
    ((Criterion<String>) wsr.getColumnManager().getColumn("Well").getCriterion()).setOperatorAndValue(Operator.TEXT_STARTS_WITH, "B");
    wsr.searchCommandListener(null);
    assertTrue("search result is non-empty after explicit search command listener is invoked", 0 != wsr.getRowCount());
  }

  /**
   * Tests WellSearchResult of all wells from all libraries.
   */
  public void testAllEntitiesOfType()
  {
    doTestAllEntitiesOfType(_inMemoryWellSearchResults);
    doTestAllEntitiesOfType(_virtualPagingWellSearchResults);
  }

  private void doTestAllEntitiesOfType(WellSearchResults wsr)
  {
    wsr.searchAll();
    wsr.searchCommandListener(null); // invoke search now (necessary when using searchAll(), above
    setOrderBy(wsr);
    wsr.clearFilter();
    DataTableModel model = wsr.getDataTableModel();
    verifySearchResult(wsr, model, _allWellKeys);
  }

  public void testEntitySet()
  {
    doTestAllEntitiesOfType(_inMemoryWellSearchResults);
    doTestAllEntitiesOfType(_virtualPagingWellSearchResults);
  }
  
  public void doTestEntitySet(WellSearchResults wsr)
  {
    wsr.searchWells(_bigWellKeys);
    setOrderBy(wsr);
    DataTableModel model = wsr.getDataTableModel();
    verifySearchResult(wsr, model, _bigWellKeys);
  }

  public void testParentedEntity()
  {
    doTestParentedEntity(_inMemoryWellSearchResults);
    doTestParentedEntity(_virtualPagingWellSearchResults);
  }

  public void doTestParentedEntity(WellSearchResults wsr)
  {
    // reload library to ensure LCV.equals() works in Well.getReagent() 
    Library smallSmallMoleculeLibrary = 
      genericEntityDao.reloadEntity(_bigSmallMoleculeLibrary, true, Library.wells.to(Well.latestReleasedReagent).getPath());
    wsr.searchWellsForLibrary(smallSmallMoleculeLibrary);
    setOrderBy(wsr);
    DataTableModel model = wsr.getDataTableModel();
    verifySearchResult(wsr, model, _bigWellKeys);
  }

    /**
   * Tests that WellSearchResults handles columns for DataColumns and
   * AnnotationTypes. And so, transitively, tests the
   * SearchResults/EntityDataFetcher "feature" of specifying columns for a
   * <i>subset</i> of items contained in a child collection. Also tests in such
   * a way that these columns are added after the initial, basic search results
   * is created and tested; this ensures that column addition logic is working
   * properly. All in all, this test is essentially the Big Kahuna integration
   * test for DataTable/SearchResults, DataTableModel, DataFetcher class
   * hierarchies.
   */
  public void testScreenResultWithResultValuesAndAnnotations()
  {
    doTestScreenResultWithResultValuesAndAnnotations(_inMemoryWellSearchResults);
    doTestScreenResultWithResultValuesAndAnnotations(_virtualPagingWellSearchResults);
  }
  
  public void doTestScreenResultWithResultValuesAndAnnotations(WellSearchResults wsr)
  {
    final DataColumn col1 = _screenResult.getDataColumnsList().get(0);
    final DataColumn col2 = _screenResult.getDataColumnsList().get(2);
    final DataColumn col3 = _screenResult.getDataColumnsList().get(7); // comment, has sparse values
    Iterator<AnnotationType> annotTypeIter = _study.getAnnotationTypes().iterator();
    final AnnotationType annotType1 = annotTypeIter.next();
    final AnnotationType annotType2 = annotTypeIter.next();

    wsr.searchWellsForScreenResult(_screenResult);
    Map<TableColumn<?,?>,Getter<Well,?>> columnsAndValueGetters = new HashMap<TableColumn<?,?>,Getter<Well,?>>();
    columnsAndValueGetters.put(wsr.getColumnManager().getColumn("numeric repl1 [1]"),
                               new Getter<Well,Object>() { public Object get(Well well) {
                                 ResultValue rv = col1.getWellKeyToResultValueMap().get(well.getWellKey());
                                 return rv == null ? null : new BigDecimal(rv.getNumericValue()).setScale(3, RoundingMode.HALF_UP); } } );
    columnsAndValueGetters.put(wsr.getColumnManager().getColumn("text repl1 [1]"),
                               new Getter<Well,Object>() { public Object get(Well well) {
                                 ResultValue rv = col2.getWellKeyToResultValueMap().get(well.getWellKey());
                                 return rv == null ? null : rv.getValue(); } } );
    columnsAndValueGetters.put(wsr.getColumnManager().getColumn("comments [1]"),
                               new Getter<Well,Object>() { public Object get(Well well) {
                                 ResultValue rv = col3.getWellKeyToResultValueMap().get(well.getWellKey());
                                 return rv == null ? null : rv.getValue(); } } );
    columnsAndValueGetters.put(wsr.getColumnManager().getColumn("text annot [100000]"),
                               new Getter<Well,Object>() { public Object get(Well well) {
                                 AnnotationValue av = annotType2.getAnnotationValues().get(well.<Reagent>getLatestReleasedReagent());
                                 return av == null ? null : av.getValue(); } } );
    columnsAndValueGetters.put(wsr.getColumnManager().getColumn("numeric annot [100000]"),
                               new Getter<Well,Object>() { public Object get(Well well) {
                                 AnnotationValue av = annotType1.getAnnotationValues().get(well.<Reagent>getLatestReleasedReagent());
                                 return av == null ? av : new BigDecimal(av.getNumericValue()).setScale(3, RoundingMode.HALF_UP); }; });

    veriyfScreenResultData(wsr, 
                           _bigWellKeys,
                           columnsAndValueGetters);
    
    SortedSet<WellKey> expectedWellKeys = _bigWellKeys.headSet(new WellKey("01001:A01"));
    wsr.searchWells(expectedWellKeys);
    columnsAndValueGetters.clear();
    columnsAndValueGetters.put(wsr.getColumnManager().getColumn("numeric repl1 [1]"),
                               new Getter<Well,Object>() { public Object get(Well well) {
                                 ResultValue rv = col1.getWellKeyToResultValueMap().get(well.getWellKey());
                                 return rv == null ? null : new BigDecimal(rv.getNumericValue()).setScale(3, RoundingMode.HALF_UP); } } );
    columnsAndValueGetters.put(wsr.getColumnManager().getColumn("text repl1 [1]"),
                               new Getter<Well,Object>() { public Object get(Well well) {
                                 ResultValue rv = col2.getWellKeyToResultValueMap().get(well.getWellKey());
                                 return rv == null ? null : rv.getValue(); } } );
    columnsAndValueGetters.put(wsr.getColumnManager().getColumn("comments [1]"),
                               new Getter<Well,Object>() { public Object get(Well well) {
                                 ResultValue rv = col3.getWellKeyToResultValueMap().get(well.getWellKey());
                                 return rv == null ? null : rv.getValue(); } } );
    columnsAndValueGetters.put(wsr.getColumnManager().getColumn("text annot [100000]"),
                               new Getter<Well,Object>() { public Object get(Well well) {
                                 AnnotationValue av = annotType2.getAnnotationValues().get(well.<Reagent>getLatestReleasedReagent());
                                 return av == null ? null : av.getValue(); } } );
    columnsAndValueGetters.put(wsr.getColumnManager().getColumn("numeric annot [100000]"),
                               new Getter<Well,Object>() { public Object get(Well well) {
                                 AnnotationValue av = annotType1.getAnnotationValues().get(well.<Reagent>getLatestReleasedReagent());
                                 return av == null ? av : new BigDecimal(av.getNumericValue()).setScale(3, RoundingMode.HALF_UP); }; });
    veriyfScreenResultData(wsr,
                           expectedWellKeys,
                           columnsAndValueGetters);
  }

  private void veriyfScreenResultData(WellSearchResults wsr,
                                      SortedSet<WellKey> expectedKeys,
                                      Map<TableColumn<?,?>,Getter<Well,?>> addColumns)
  {
    setOrderBy(wsr);
    DataTableModel model = wsr.getDataTableModel();
    verifySearchResult(wsr, model, expectedKeys);

    // now add the extra columns and test them
    for (TableColumn<?,?> column : addColumns.keySet()) {
      column.setVisible(true);
    }
    verifySearchResult(wsr, model, expectedKeys);
    // test the extra columns
    int j = 0;
    for (WellKey expectedWellKey : expectedKeys) {
      model.setRowIndex(j++);
      assertEquals("row data " + j,
                   expectedWellKey,
                   ((Well) model.getRowData()).getWellKey());
      int columnsTested = 0;
      for (TableColumn<Well,?> column : wsr.getColumnManager().getVisibleColumns()) {
        Well well = (Well) model.getRowData();
        Object cellValue = column.getCellValue(well);
        if (addColumns.containsKey(column)) {
          Getter<Well,?> expectedValueGetter = addColumns.get(column);
          if (cellValue instanceof Double) {
            cellValue = new BigDecimal((Double) cellValue);
          }
          if (cellValue instanceof BigDecimal) {
            cellValue = ((BigDecimal) cellValue).setScale(3, RoundingMode.HALF_UP);
          }
          assertEquals("row " + j + ":" + column.getName(),
                       expectedValueGetter.get(well),
                       cellValue);
          ++columnsTested;
        }
      }
      assertEquals("tested all additional columns for row " + j, addColumns.size(), columnsTested);
    }

    // TODO: test sorting on result values
  }

  public void testFilterScreenResultResultValues()
  {
    doTestFilterScreenResultResultValues(_inMemoryWellSearchResults);
    doTestFilterScreenResultResultValues(_virtualPagingWellSearchResults);
  }
  
  public void doTestFilterScreenResultResultValues(WellSearchResults wsr)
  {
    wsr.searchWellsForScreenResult(_screenResult);
    setOrderBy(wsr);

    // initialize search result filter to select only the first well
    Well well1 = genericEntityDao.findEntityById(Well.class, "01000:A01");
    Well well2 = genericEntityDao.findEntityById(Well.class, "01000:A05");
    SortedSet<WellKey> _expectedKeys = new TreeSet<WellKey>();
    _expectedKeys.add(well1.getWellKey());
    _expectedKeys.add(well2.getWellKey());

    TableColumn<Well,String> filterColumn1 =
      (TableColumn<Well,String>) wsr.getColumnManager().getColumn("text repl1 [1]");
    filterColumn1.clearCriteria().addCriterion(new Criterion<String>(Operator.LESS_THAN, "text00008"));
    TableColumn<Well,String> filterColumn2 =
      (TableColumn<Well,String>) wsr.getColumnManager().getColumn("positive [1]");
    filterColumn2.clearCriteria().addCriterion(new Criterion<String>(Operator.EQUAL, PartitionedValue.STRONG.getValue()));
    // TODO: also test a numeric DataColumn column

    DataTableModel model = wsr.getDataTableModel();
    verifySearchResult(wsr, model, _expectedKeys);
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

  public void testValidColumnSelections()
  {
    doTestValidColumnSelections(_inMemoryWellSearchResults);
    doTestValidColumnSelections(_virtualPagingWellSearchResults);
  }

  public void doTestValidColumnSelections(WellSearchResults wsr)
  {
    Library rnaiLibrary = genericEntityDao.findEntityByProperty(Library.class, "libraryName", "library 3", true, Library.wells.getPath(), Library.contentsVersions.getPath());
    ScreenResult rnaiScreenResult = genericEntityDao.findEntityByProperty(Screen.class, "screenNumber", 3, true, "screenResult.dataColumns").getScreenResult();
    ScreenResult smallMolScreenResult = _screenResult;
    Library smallMolLibrary = _bigSmallMoleculeLibrary;

    List<String> smallMolColumnNames = new ArrayList<String>();
    for (DataColumn col : smallMolScreenResult.getDataColumns()) {
      smallMolColumnNames.add(WellSearchResults.makeColumnName(col, smallMolScreenResult.getScreen().getScreenNumber()));
    }
    List<String> rnaiColumnNames = new ArrayList<String>();
    for (DataColumn col : rnaiScreenResult.getDataColumns()) {
      rnaiColumnNames.add(WellSearchResults.makeColumnName(col, rnaiScreenResult.getScreen().getScreenNumber()));
    }
    for (AnnotationType at : _study.getAnnotationTypes()) {
      smallMolColumnNames.add(WellSearchResults.makeColumnName(at, _study.getStudyNumber()));
    }

    wsr.searchWellsForScreenResult(smallMolScreenResult);
    setOrderBy(wsr);
    doTestColumnsCreated("small molecule screen result", wsr, smallMolColumnNames, true);
    doTestColumnsCreated("small molecule screen result", wsr, rnaiColumnNames, false);
    wsr.searchWellsForScreenResult(rnaiScreenResult);
    doTestColumnsCreated("rnai screen result", wsr, smallMolColumnNames, false);
    doTestColumnsCreated("rnai screen result", wsr, rnaiColumnNames, true);

    wsr.searchWellsForLibrary(smallMolLibrary);
    doTestColumnsCreated("small molecule library", wsr, smallMolColumnNames, true);
    doTestColumnsCreated("small molecule library", wsr, rnaiColumnNames, false);
    wsr.searchWellsForLibrary(rnaiLibrary);
    doTestColumnsCreated("rnai library", wsr, smallMolColumnNames, false);
    doTestColumnsCreated("rnai library", wsr, rnaiColumnNames, true);

    Set<WellKey> rnaiWellKeys = new HashSet<WellKey>();
    for (Well well : rnaiLibrary.getWells()) {
      rnaiWellKeys.add(well.getWellKey());
      if (rnaiWellKeys.size() == 10) {
        break;
      }
    }
    Set<WellKey> smallMoleculeWellKeys = new HashSet<WellKey>();
    for (Well well : smallMolLibrary.getWells()) {
      smallMoleculeWellKeys.add(well.getWellKey());
      if (smallMoleculeWellKeys.size() == 10) {
        break;
      }
    }
    Set<WellKey> bothWellKeys = new HashSet<WellKey>();
    bothWellKeys.addAll(rnaiWellKeys);
    bothWellKeys.addAll(smallMoleculeWellKeys);
    wsr.searchWells(rnaiWellKeys);
    doTestColumnsCreated("rnai wells", wsr, smallMolColumnNames, false);
    doTestColumnsCreated("rnai wells", wsr, rnaiColumnNames, true);
    wsr.searchWells(smallMoleculeWellKeys);
    doTestColumnsCreated("small molecule wells", wsr, smallMolColumnNames, true);
    doTestColumnsCreated("small molecule wells", wsr, rnaiColumnNames, false);
    wsr.searchWells(bothWellKeys);
    doTestColumnsCreated("both wells", wsr, smallMolColumnNames, true);
    doTestColumnsCreated("both wells", wsr, rnaiColumnNames, true);
  }

  public void testFilterOnColumns()
  {
    doTestFilterOnColumns(_inMemoryWellSearchResults);
    doTestFilterOnColumns(_virtualPagingWellSearchResults);
  }
  
  public void doTestFilterOnColumns(WellSearchResults wsr)
  {
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
    columnNameToExpectedValue.put("Vendor ID", "179");
    doTestFilterOnColumns(wsr, columnNameToExpectedValue, 3);

    // tests the well-to-reagent-to-annotationValue relationship
    columnNameToExpectedValue.clear();
    columnNameToExpectedValue.put("text annot [100000]", "bbb");
    wsr.getColumnManager().getColumn("text annot [100000]").setVisible(true);
    doTestFilterOnColumns(wsr, columnNameToExpectedValue, 1);
    
  }
  
  public void testSortColumns(WellSearchResults wsr)
  {
    doTestSortColumns(_inMemoryWellSearchResults);
    doTestSortColumns(_virtualPagingWellSearchResults);
  }
  
  public void doTestSortColumns(WellSearchResults wsr)
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
      assertNotNull("column " + columnName + " exists", columnName);
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
    Collections.sort(expectedSortedValues, NullSafeComparator.NULLS_LOW);
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
      TableColumn<Well,?> column = wellSearchResults.getColumnManager().getColumn(columnName);
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
                                                        true,
                                                        "wells.screenResults");
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
        genericEntityDao.needReadOnly(screen.getScreenResult(), "wells");
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
                                                        "wells");
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
      Well well = (Well) model.getRowData();
      assertEquals("row data " + j, expectedWellKey, well.getWellKey());
      List<TableColumn<Well,?>> columnsTested = new ArrayList<TableColumn<Well,?>>();
      for (TableColumn<Well,?> column : wsr.getColumnManager().getVisibleColumns()) {
        Well rowData = (Well) model.getRowData();
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
          if (rowData.getLibrary().getScreenType() == ScreenType.SMALL_MOLECULE) {
            String smiles = (String) cellValue;
            assertEquals("row " + j + ":Compounds", "smiles" + expectedWellKey, smiles);
          }
          columnsTested.add(column);
        }
        else if (column.getName().equals("Gene Name")) {
          if (rowData.getLibrary().getScreenType() == ScreenType.RNAI) {
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
