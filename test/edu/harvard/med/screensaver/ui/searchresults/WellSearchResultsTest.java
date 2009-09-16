// $HeadURL:
// svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml
// $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
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
import edu.harvard.med.screensaver.db.accesspolicy.UnrestrictedDataAccessPolicy;
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
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.AnnotationValue;
import edu.harvard.med.screensaver.model.screenresults.PartitionedValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.Study;
import edu.harvard.med.screensaver.ui.libraries.WellViewer;
import edu.harvard.med.screensaver.ui.table.Criterion;
import edu.harvard.med.screensaver.ui.table.DataTableModelType;
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

    _wellViewer = new WellViewer(null, genericEntityDao, librariesDao, new UnrestrictedDataAccessPolicy(), null, null, null, null, null);
    _wellSearchResults = new WellSearchResults(genericEntityDao,
                                               new UnrestrictedDataAccessPolicy(),
                                                null,
                                               _wellViewer,
                                               null,
                                               Collections.<DataExporter<?>> emptyList());
    _wellSearchResults.setDataTableUIComponent(new UIData());
  }

  /** for testing that multiple contents versions are handled in a disjoint manner */
  private void addUnreleasedContentsVersion(Library library)
  {
    library.createContentsVersion(new AdministrativeActivity(library.getLatestReleasedContentsVersion().getLoadingActivity().getPerformedBy(), 
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

  private void setOrderBy()
  {
    TableColumnManager<Well> columnManager = _wellSearchResults.getColumnManager();
    columnManager.addCompoundSortColumns(columnManager.getColumn("Plate"),
                                         columnManager.getColumn("Well"));
    columnManager.setSortColumnName("Plate");
    _wellSearchResults.resort(); // necessary, since primary sort column may not have actually changed, although the compound sort columns have
  }
  
  public void testSearchAllWellsIsInitiallyEmpty()
  {
    _wellSearchResults.searchAllWells();
    assertEquals("search result is initially empty", 0, _wellSearchResults.getRowCount());
    ((Criterion<String>) _wellSearchResults.getColumnManager().getColumn("Well").getCriterion()).setOperatorAndValue(Operator.TEXT_STARTS_WITH, "B");
    _wellSearchResults.searchCommandListener(null);
    assertTrue("search result is non-empty after explicit search command listener is invoked", 0 != _wellSearchResults.getRowCount());
  }

  /**
   * Tests WellSearchResult of all wells from all libraries.
   */
  // note: in "all entities" mode, InMemoryDataModel will never be used
  public void testAllEntitiesOfType()
  {
    _wellSearchResults.searchAllWells();
    _wellSearchResults.searchCommandListener(null); // invoke search now (necessary when using searchAllWells(), above
    setOrderBy();
    _wellSearchResults.clearFilter();
    DataTableModel model = _wellSearchResults.getDataTableModel();
    assertTrue("VirtualPagingDataModel used",
               model.getModelType() == DataTableModelType.VIRTUAL_PAGING);
    doTestSearchResult(model, _allWellKeys);
  }

  public void testEntitySetInMemory()
  {
    _wellSearchResults.searchWells(_smallWellKeys);
    setOrderBy();
    DataTableModel model = _wellSearchResults.getDataTableModel();
    assertTrue("InMemoryDataModel used",
               model.getModelType() == DataTableModelType.IN_MEMORY);
    doTestSearchResult(model, _smallWellKeys);
  }

  public void testEntitySetVirtualPaging()
  {
    _wellSearchResults.searchWells(_bigWellKeys);
    setOrderBy();
    DataTableModel model = _wellSearchResults.getDataTableModel();
    assertTrue("VirtualPagingDataModel used",
               model.getModelType() == DataTableModelType.VIRTUAL_PAGING);
    doTestSearchResult(model, _bigWellKeys);
  }

  public void testParentedEntityInMemory()
  {
    // reload library to ensure LCV.equals() works in Well.getReagent() 
    Library smallSmallMoleculeLibrary = 
      genericEntityDao.reloadEntity(_smallSmallMoleculeLibrary, true, Library.wells.to(Well.latestReleasedReagent).getPath());
    _wellSearchResults.searchWellsForLibrary(smallSmallMoleculeLibrary);
    setOrderBy();
    DataTableModel model = _wellSearchResults.getDataTableModel();
    assertTrue("InMemoryDataModel used",
               model.getModelType() == DataTableModelType.IN_MEMORY);
    doTestSearchResult(model, _smallWellKeys);
  }

  public void testParentedEntityVirtualPaging()
  {
    // reload library to ensure LCV.equals() works in Well.getReagent() 
    Library bigSmallMoleculeLibrary = 
      genericEntityDao.reloadEntity(_bigSmallMoleculeLibrary, true, Library.wells.to(Well.latestReleasedReagent).getPath());
    _wellSearchResults.searchWellsForLibrary(bigSmallMoleculeLibrary);
    setOrderBy();
    DataTableModel model = _wellSearchResults.getDataTableModel();
    assertTrue("VirtualPagingDataModel used",
               model.getModelType() == DataTableModelType.VIRTUAL_PAGING);
    doTestSearchResult(model, _bigWellKeys);
  }

  /**
   * Tests that WellSearchResults handles columns for ResultValueTypes and
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
    final ResultValueType rvt1 = _screenResult.getResultValueTypesList().get(0);
    final ResultValueType rvt2 = _screenResult.getResultValueTypesList().get(2);
    final ResultValueType rvt3 = _screenResult.getResultValueTypesList().get(7); // comment, has sparse values
    Iterator<AnnotationType> annotTypeIter = _study.getAnnotationTypes().iterator();
    final AnnotationType annotType1 = annotTypeIter.next();
    final AnnotationType annotType2 = annotTypeIter.next();

    // test with VirtualPagingDataModel
    _wellSearchResults.searchWellsForScreenResult(_screenResult);
    Map<TableColumn<?,?>,Getter<Well,?>> columnsAndValueGetters = new HashMap<TableColumn<?,?>,Getter<Well,?>>();
    columnsAndValueGetters.put(_wellSearchResults.getColumnManager().getColumn("numeric repl1 [1]"),
                               new Getter<Well,Object>() { public Object get(Well well) {
                                 ResultValue rv = rvt1.getWellKeyToResultValueMap().get(well.getWellKey());
                                 return rv == null ? null : new BigDecimal(rv.getNumericValue()).setScale(3, RoundingMode.HALF_UP); } } );
    columnsAndValueGetters.put(_wellSearchResults.getColumnManager().getColumn("text repl1 [1]"),
                               new Getter<Well,Object>() { public Object get(Well well) {
                                 ResultValue rv = rvt2.getWellKeyToResultValueMap().get(well.getWellKey());
                                 return rv == null ? null : rv.getValue(); } } );
    columnsAndValueGetters.put(_wellSearchResults.getColumnManager().getColumn("comments [1]"),
                               new Getter<Well,Object>() { public Object get(Well well) {
                                 ResultValue rv = rvt3.getWellKeyToResultValueMap().get(well.getWellKey());
                                 return rv == null ? null : rv.getValue(); } } );
    columnsAndValueGetters.put(_wellSearchResults.getColumnManager().getColumn("text annot [100000]"),
                               new Getter<Well,Object>() { public Object get(Well well) {
                                 AnnotationValue av = annotType2.getAnnotationValues().get(well.<Reagent>getLatestReleasedReagent());
                                 return av == null ? null : av.getValue(); } } );
    columnsAndValueGetters.put(_wellSearchResults.getColumnManager().getColumn("numeric annot [100000]"),
                               new Getter<Well,Object>() { public Object get(Well well) {
                                 AnnotationValue av = annotType1.getAnnotationValues().get(well.<Reagent>getLatestReleasedReagent());
                                 return av == null ? av : new BigDecimal(av.getNumericValue()).setScale(3, RoundingMode.HALF_UP); }; });

    // test with InMemoryDataModel
    doTestScreenResult(_bigWellKeys,
                       DataTableModelType.VIRTUAL_PAGING,
                       columnsAndValueGetters);

    SortedSet<WellKey> expectedWellKeys = _bigWellKeys.headSet(new WellKey("01001:A01"));
    _wellSearchResults.searchWells(expectedWellKeys);
    columnsAndValueGetters.clear();
    columnsAndValueGetters.put(_wellSearchResults.getColumnManager().getColumn("numeric repl1 [1]"),
                               new Getter<Well,Object>() { public Object get(Well well) {
                                 ResultValue rv = rvt1.getWellKeyToResultValueMap().get(well.getWellKey());
                                 return rv == null ? null : new BigDecimal(rv.getNumericValue()).setScale(3, RoundingMode.HALF_UP); } } );
    columnsAndValueGetters.put(_wellSearchResults.getColumnManager().getColumn("text repl1 [1]"),
                               new Getter<Well,Object>() { public Object get(Well well) {
                                 ResultValue rv = rvt2.getWellKeyToResultValueMap().get(well.getWellKey());
                                 return rv == null ? null : rv.getValue(); } } );
    columnsAndValueGetters.put(_wellSearchResults.getColumnManager().getColumn("comments [1]"),
                               new Getter<Well,Object>() { public Object get(Well well) {
                                 ResultValue rv = rvt3.getWellKeyToResultValueMap().get(well.getWellKey());
                                 return rv == null ? null : rv.getValue(); } } );
    columnsAndValueGetters.put(_wellSearchResults.getColumnManager().getColumn("text annot [100000]"),
                               new Getter<Well,Object>() { public Object get(Well well) {
                                 AnnotationValue av = annotType2.getAnnotationValues().get(well.<Reagent>getLatestReleasedReagent());
                                 return av == null ? null : av.getValue(); } } );
    columnsAndValueGetters.put(_wellSearchResults.getColumnManager().getColumn("numeric annot [100000]"),
                               new Getter<Well,Object>() { public Object get(Well well) {
                                 AnnotationValue av = annotType1.getAnnotationValues().get(well.<Reagent>getLatestReleasedReagent());
                                 return av == null ? av : new BigDecimal(av.getNumericValue()).setScale(3, RoundingMode.HALF_UP); }; });
    doTestScreenResult(expectedWellKeys,
                       DataTableModelType.IN_MEMORY,
                       columnsAndValueGetters);
  }

  public void doTestScreenResult(SortedSet<WellKey> expectedKeys,
                                 DataTableModelType expectedDataTableModelType,
                                 Map<TableColumn<?,?>,Getter<Well,?>> addColumns)
  {
    setOrderBy();
    DataTableModel model = _wellSearchResults.getDataTableModel();
    assertEquals("DataTableModel in use", expectedDataTableModelType,
                 model.getModelType());
    doTestSearchResult(model, expectedKeys);

    // now add the extra columns and test them
    for (TableColumn<?,?> column : addColumns.keySet()) {
      column.setVisible(true);
    }
    doTestSearchResult(model, expectedKeys);
    // test the extra columns
    int j = 0;
    for (WellKey expectedWellKey : expectedKeys) {
      model.setRowIndex(j++);
      assertEquals("row data " + j,
                   expectedWellKey,
                   ((Well) model.getRowData()).getWellKey());
      int columnsTested = 0;
      for (TableColumn<Well,?> column : _wellSearchResults.getColumnManager().getVisibleColumns()) {
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
    _wellSearchResults.searchWellsForScreenResult(_screenResult);
    setOrderBy();

    // initialize search result filter to select only the first well
    Well well1 = genericEntityDao.findEntityById(Well.class, "01000:A01");
    Well well2 = genericEntityDao.findEntityById(Well.class, "01000:A05");
    SortedSet<WellKey> _expectedKeys = new TreeSet<WellKey>();
    _expectedKeys.add(well1.getWellKey());
    _expectedKeys.add(well2.getWellKey());

    TableColumn<Well,String> filterColumn1 =
      (TableColumn<Well,String>) _wellSearchResults.getColumnManager().getColumn("text repl1 [1]");
    filterColumn1.clearCriteria().addCriterion(new Criterion<String>(Operator.LESS_THAN, "text00008"));
    TableColumn<Well,String> filterColumn2 =
      (TableColumn<Well,String>) _wellSearchResults.getColumnManager().getColumn("positive [1]");
    filterColumn2.clearCriteria().addCriterion(new Criterion<String>(Operator.EQUAL, PartitionedValue.STRONG.getValue()));
    // TODO: also test a numeric ResultValueType column

    DataTableModel model = _wellSearchResults.getDataTableModel();
    assertTrue("VirtualPagingDataModel used", model.getModelType() == DataTableModelType.VIRTUAL_PAGING);
    doTestSearchResult(model, _expectedKeys);
  }

  // TODO: must test via a JSFUnit test, since entity viewer updates only occur from UI data table scrolling events, not model.setRowIndex()
//  public void testEntityModeScrolling()
//  {
//    _wellSearchResults.searchAllWells();
//    setOrderBy();
//    _wellSearchResults.getRowsPerPageSelector().setSelection(_wellSearchResults.getRowsPerPageSelector().getDefaultSelection());
//    assertFalse("summary view mode", _wellSearchResults.isEntityView());
//    _wellSearchResults.getRowsPerPageSelector().setSelection(1);
//    assertTrue("entity view mode", _wellSearchResults.isEntityView());
//
//    DataTableModel model = _wellSearchResults.getDataTableModel();
//    assertTrue("VirtualPagingDataModel used",
//               _wellSearchResults.getBaseDataTableModel() == DataTableModelType.VIRTUAL_PAGING);
//    List<WellKey> expectedWellKeys = new ArrayList<WellKey>(_allWellKeys).subList(0, 3);
//    model.setRowIndex(0);
//    assertEquals("entity 0", expectedWellKeys.get(0), _wellViewer.getWell().getWellKey());
//    model.setRowIndex(1);
//    assertEquals("entity 1", expectedWellKeys.get(1), _wellViewer.getWell().getWellKey());
//    model.setRowIndex(2);
//    assertEquals("entity 2", expectedWellKeys.get(2), _wellViewer.getWell().getWellKey());
//
//    _wellSearchResults.getRowsPerPageSelector().setSelection(_wellSearchResults.getRowsPerPageSelector().getDefaultSelection());
//    assertEquals("returning to summary mode, with last-viewed entity shown as first row",
//                 2,
//                 _wellSearchResults.getDataTableUIComponent().getFirst());
//    assertEquals("last-viewed entity shown as first row",
//                 expectedWellKeys.get(2),
//                 ((Well) _wellSearchResults.getDataTableModel().getRowData()).getWellKey());
//  }

  public void testValidColumnSelections()
  {
    Library rnaiLibrary = genericEntityDao.findEntityByProperty(Library.class, "libraryName", "library 3", true, Library.wells.getPath(), Library.contentsVersions.getPath());
    ScreenResult rnaiScreenResult = genericEntityDao.findEntityByProperty(Screen.class, "screenNumber", 3, true, "screenResult.resultValueTypes").getScreenResult();
    ScreenResult smallMolScreenResult = _screenResult;
    Library smallMolLibrary = _bigSmallMoleculeLibrary;

    List<String> smallMolColumnNames = new ArrayList<String>();
    for (ResultValueType rvt : smallMolScreenResult.getResultValueTypes()) {
      smallMolColumnNames.add(WellSearchResults.makeColumnName(rvt, smallMolScreenResult.getScreen().getScreenNumber()));
    }
    List<String> rnaiColumnNames = new ArrayList<String>();
    for (ResultValueType rvt : rnaiScreenResult.getResultValueTypes()) {
      rnaiColumnNames.add(WellSearchResults.makeColumnName(rvt, rnaiScreenResult.getScreen().getScreenNumber()));
    }
    for (AnnotationType at : _study.getAnnotationTypes()) {
      smallMolColumnNames.add(WellSearchResults.makeColumnName(at, _study.getStudyNumber()));
    }

    _wellSearchResults.searchWellsForScreenResult(smallMolScreenResult);
    setOrderBy();
    doTestColumnsCreated("small molecule screen result", _wellSearchResults, smallMolColumnNames, true);
    doTestColumnsCreated("small molecule screen result", _wellSearchResults, rnaiColumnNames, false);
    _wellSearchResults.searchWellsForScreenResult(rnaiScreenResult);
    doTestColumnsCreated("rnai screen result", _wellSearchResults, smallMolColumnNames, false);
    doTestColumnsCreated("rnai screen result", _wellSearchResults, rnaiColumnNames, true);

    _wellSearchResults.searchWellsForLibrary(smallMolLibrary);
    doTestColumnsCreated("small molecule library", _wellSearchResults, smallMolColumnNames, true);
    doTestColumnsCreated("small molecule library", _wellSearchResults, rnaiColumnNames, false);
    _wellSearchResults.searchWellsForLibrary(rnaiLibrary);
    doTestColumnsCreated("rnai library", _wellSearchResults, smallMolColumnNames, false);
    doTestColumnsCreated("rnai library", _wellSearchResults, rnaiColumnNames, true);

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
    _wellSearchResults.searchWells(rnaiWellKeys);
    doTestColumnsCreated("rnai wells", _wellSearchResults, smallMolColumnNames, false);
    doTestColumnsCreated("rnai wells", _wellSearchResults, rnaiColumnNames, true);
    _wellSearchResults.searchWells(smallMoleculeWellKeys);
    doTestColumnsCreated("small molecule wells", _wellSearchResults, smallMolColumnNames, true);
    doTestColumnsCreated("small molecule wells", _wellSearchResults, rnaiColumnNames, false);
    _wellSearchResults.searchWells(bothWellKeys);
    doTestColumnsCreated("both wells", _wellSearchResults, smallMolColumnNames, true);
    doTestColumnsCreated("both wells", _wellSearchResults, rnaiColumnNames, true);
  }

  public void testFilterOnColumns()
  {
    _wellSearchResults.searchAllWells();
    _wellSearchResults.searchCommandListener(null); // invoke search now (necessary when using searchAllWells(), above

    Map<String,Object> columnNameToExpectedValue = new HashMap<String,Object>();
    columnNameToExpectedValue.put("Plate", Integer.valueOf(1000));
    columnNameToExpectedValue.put("Well", "H01");
    doTestFilterOnColumns(_wellSearchResults, columnNameToExpectedValue, 1);

    // tests the well-to-reagent relationship
    columnNameToExpectedValue.clear();
    columnNameToExpectedValue.put("Vendor ID", "179");
    doTestFilterOnColumns(_wellSearchResults, columnNameToExpectedValue, 3);

    // tests the well-to-reagent-to-annotationValue relationship
    columnNameToExpectedValue.clear();
    columnNameToExpectedValue.put("text annot [100000]", "bbb");
    _wellSearchResults.getColumnManager().getColumn("text annot [100000]").setVisible(true);
    doTestFilterOnColumns(_wellSearchResults, columnNameToExpectedValue, 1);
  }
  
  public void testSortColumns()
  {
    _wellSearchResults.searchWellsForLibrary(_bigSmallMoleculeLibrary);
    assertTrue("using VirtualPagingDataModel", _wellSearchResults.getDataTableModel().getModelType() == DataTableModelType.VIRTUAL_PAGING);
    doTestSortForAllColumnsAndDirections(_wellSearchResults, _bigSmallMoleculeLibrary.getWells().size());

    _wellSearchResults.searchWellsForLibrary(_smallSmallMoleculeLibrary);
    assertTrue("using InMemoryModel", _wellSearchResults.getDataTableModel().getModelType() == DataTableModelType.IN_MEMORY);
    doTestSortForAllColumnsAndDirections(_wellSearchResults, _smallSmallMoleculeLibrary.getWells().size());

    _wellSearchResults.searchWellsForLibraryContentsVersion(_bigSmallMoleculeLibrary.getLatestContentsVersion());
    assertTrue("using VirtualPagingDataModel", _wellSearchResults.getDataTableModel().getModelType() == DataTableModelType.VIRTUAL_PAGING);
    doTestSortForAllColumnsAndDirections(_wellSearchResults, _bigSmallMoleculeLibrary.getWells().size());

    _wellSearchResults.searchWellsForLibraryContentsVersion(_smallSmallMoleculeLibrary.getLatestContentsVersion());
    assertTrue("using InMemoryModel", _wellSearchResults.getDataTableModel().getModelType() == DataTableModelType.IN_MEMORY);
    doTestSortForAllColumnsAndDirections(_wellSearchResults, _smallSmallMoleculeLibrary.getWells().size());
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
    assertTrue("using VirtualPagingDataModel", model.getModelType() == DataTableModelType.VIRTUAL_PAGING);
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
        assertEquals("filtered on column " + columnName + "=" +  expectedValue + ", row " + i, 
                     expectedValue, 
                     searchResults.getCellValue());
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
        genericEntityDao.needReadOnly(screen.getScreenResult(), "resultValueTypes.resultValues");
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

  private void doTestSearchResult(DataModel model,
                                  SortedSet<WellKey> expectedWellKeys)
  {
    _wellSearchResults.getColumnManager().getColumn("Compound SMILES").setVisible(true);
    _wellSearchResults.getColumnManager().getColumn("Gene Name").setVisible(true);
    assertEquals("row count", expectedWellKeys.size(), model.getRowCount());
    int j = 0;
    for (WellKey expectedWellKey : expectedWellKeys) {
      model.setRowIndex(j++);
      Well well = (Well) model.getRowData();
      assertEquals("row data " + j, expectedWellKey, well.getWellKey());
      List<TableColumn<Well,?>> columnsTested = new ArrayList<TableColumn<Well,?>>();
      for (TableColumn<Well,?> column : _wellSearchResults.getColumnManager().getVisibleColumns()) {
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
