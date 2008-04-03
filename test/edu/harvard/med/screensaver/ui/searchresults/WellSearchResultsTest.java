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
import edu.harvard.med.screensaver.db.datafetcher.Getter;
import edu.harvard.med.screensaver.io.DataExporter;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.libraries.Library;
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
import edu.harvard.med.screensaver.ui.table.Criterion.Operator;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;
import edu.harvard.med.screensaver.ui.table.column.TableColumnManager;
import edu.harvard.med.screensaver.ui.table.model.DataTableModel;
import edu.harvard.med.screensaver.ui.table.model.InMemoryEntityDataModel;
import edu.harvard.med.screensaver.ui.table.model.VirtualPagingDataModel;
import edu.harvard.med.screensaver.ui.table.model.VirtualPagingEntityDataModel;

import org.apache.log4j.Logger;
import org.hibernate.Session;

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
      genericEntityDao.persistEntity(_bigSmallMoleculeLibrary);
      _smallSmallMoleculeLibrary = MakeDummyEntities.makeDummyLibrary(2,
                                                                      ScreenType.SMALL_MOLECULE,
                                                                      1);
      genericEntityDao.persistEntity(_smallSmallMoleculeLibrary);
      _smallWellKeys = new TreeSet<WellKey>();
      for (Well well : _smallSmallMoleculeLibrary.getWells()) {
        _smallWellKeys.add(well.getWellKey());
      }
      _bigWellKeys = new TreeSet<WellKey>();
      for (Well well : _bigSmallMoleculeLibrary.getWells()) {
        _bigWellKeys.add(well.getWellKey());
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

    _wellViewer = new WellViewer(genericEntityDao, null, null, null);
    _wellSearchResults = new WellSearchResults(genericEntityDao,
                                               null,
                                               _wellViewer,
                                               null,
                                               null,
                                               Collections.<DataExporter<Well,String>> emptyList());
    _wellSearchResults.setDataTableUIComponent(new UIData());
  }

  private void setOrderBy()
  {
    TableColumnManager<Well> columnManager = _wellSearchResults.getColumnManager();
    columnManager.addCompoundSortColumns(columnManager.getColumn("Plate"),
                                         columnManager.getColumn("Well"));
    columnManager.setSortColumnName("Plate");
  }

  /**
   * Tests WellSearchResult of all wells from all libraries.
   */
  // note: in "all entities" mode, InMemoryDataModel will never be used
  public void testAllEntitiesOfType()
  {
    _wellSearchResults.searchAllWells();
    setOrderBy();
    _wellSearchResults.clearFilter();
    DataTableModel model = _wellSearchResults.getDataTableModel();
    assertTrue("VirtualPagingDataModel used",
               _wellSearchResults.getBaseDataTableModel() instanceof VirtualPagingDataModel);
    doTestSearchResult(model, _allWellKeys);
  }

  public void testEntitySetInMemory()
  {
    _wellSearchResults.searchWells(_smallWellKeys);
    setOrderBy();
    DataTableModel model = _wellSearchResults.getDataTableModel();
    assertTrue("InMemoryDataModel used",
               _wellSearchResults.getBaseDataTableModel() instanceof InMemoryEntityDataModel);
    doTestSearchResult(model, _smallWellKeys);
  }

  public void testEntitySetVirtualPaging()
  {
    _wellSearchResults.searchWells(_bigWellKeys);
    setOrderBy();
    DataTableModel model = _wellSearchResults.getDataTableModel();
    assertTrue("VirtualPagingDataModel used",
               _wellSearchResults.getBaseDataTableModel() instanceof VirtualPagingDataModel);
    doTestSearchResult(model, _bigWellKeys);
  }

  // TODO: this is failing only because we commented out the logic in WellSearchResults.doBuildDataModel that decides which DataTableModel type to use, based upon search result size
  public void testParentedEntityInMemory()
  {
    _wellSearchResults.searchWellsForLibrary(_smallSmallMoleculeLibrary);
    setOrderBy();
    DataTableModel model = _wellSearchResults.getDataTableModel();
    assertTrue("InMemoryDataModel used",
               _wellSearchResults.getBaseDataTableModel() instanceof InMemoryEntityDataModel);
    doTestSearchResult(model, _smallWellKeys);
  }

  public void testParentedEntitySetVirtualPaging()
  {
    _wellSearchResults.searchWellsForLibrary(_bigSmallMoleculeLibrary);
    setOrderBy();
    DataTableModel model = _wellSearchResults.getDataTableModel();
    assertTrue("VirtualPagingDataModel used",
               _wellSearchResults.getBaseDataTableModel() instanceof VirtualPagingDataModel);
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
                                 ResultValue rv = rvt1.getResultValues().get(well.getWellKey());
                                 return rv == null ? null : new BigDecimal(rv.getNumericValue()).setScale(3, RoundingMode.HALF_UP); } } );
    columnsAndValueGetters.put(_wellSearchResults.getColumnManager().getColumn("text repl1 [1]"),
                               new Getter<Well,Object>() { public Object get(Well well) {
                                 ResultValue rv = rvt2.getResultValues().get(well.getWellKey());
                                 return rv == null ? null : rv.getValue(); } } );
    columnsAndValueGetters.put(_wellSearchResults.getColumnManager().getColumn("comments [1]"),
                               new Getter<Well,Object>() { public Object get(Well well) {
                                 ResultValue rv = rvt3.getResultValues().get(well.getWellKey());
                                 return rv == null ? null : rv.getValue(); } } );
    columnsAndValueGetters.put(_wellSearchResults.getColumnManager().getColumn("text annot [100000]"),
                               new Getter<Well,Object>() { public Object get(Well well) {
                                 AnnotationValue av = annotType2.getAnnotationValues().get(well.getReagent());
                                 return av == null ? null : av.getValue(); } } );
    columnsAndValueGetters.put(_wellSearchResults.getColumnManager().getColumn("numeric annot [100000]"),
                               new Getter<Well,Object>() { public Object get(Well well) {
                                 AnnotationValue av = annotType1.getAnnotationValues().get(well.getReagent());
                                 return av == null ? av : new BigDecimal(av.getNumericValue()).setScale(3, RoundingMode.HALF_UP); }; });

    // test with InMemoryDataModel
    doTestScreenResult(_bigWellKeys,
                       VirtualPagingEntityDataModel.class,
                       columnsAndValueGetters);

    SortedSet<WellKey> expectedWellKeys = _bigWellKeys.headSet(new WellKey("01001:A01"));
    _wellSearchResults.searchWells(expectedWellKeys);
    columnsAndValueGetters.clear();
    columnsAndValueGetters.put(_wellSearchResults.getColumnManager().getColumn("numeric repl1 [1]"),
                               new Getter<Well,Object>() { public Object get(Well well) {
                                 ResultValue rv = rvt1.getResultValues().get(well.getWellKey());
                                 return rv == null ? null : new BigDecimal(rv.getNumericValue()).setScale(3, RoundingMode.HALF_UP); } } );
    columnsAndValueGetters.put(_wellSearchResults.getColumnManager().getColumn("text repl1 [1]"),
                               new Getter<Well,Object>() { public Object get(Well well) {
                                 ResultValue rv = rvt2.getResultValues().get(well.getWellKey());
                                 return rv == null ? null : rv.getValue(); } } );
    columnsAndValueGetters.put(_wellSearchResults.getColumnManager().getColumn("comments [1]"),
                               new Getter<Well,Object>() { public Object get(Well well) {
                                 ResultValue rv = rvt3.getResultValues().get(well.getWellKey());
                                 return rv == null ? null : rv.getValue(); } } );
    columnsAndValueGetters.put(_wellSearchResults.getColumnManager().getColumn("text annot [100000]"),
                               new Getter<Well,Object>() { public Object get(Well well) {
                                 AnnotationValue av = annotType2.getAnnotationValues().get(well.getReagent());
                                 return av == null ? null : av.getValue(); } } );
    columnsAndValueGetters.put(_wellSearchResults.getColumnManager().getColumn("numeric annot [100000]"),
                               new Getter<Well,Object>() { public Object get(Well well) {
                                 AnnotationValue av = annotType1.getAnnotationValues().get(well.getReagent());
                                 return av == null ? av : new BigDecimal(av.getNumericValue()).setScale(3, RoundingMode.HALF_UP); }; });
    doTestScreenResult(expectedWellKeys,
                       InMemoryEntityDataModel.class,
                       columnsAndValueGetters);
  }

  public void doTestScreenResult(SortedSet<WellKey> expectedKeys,
                                 Class<? extends DataTableModel> expectedDataTableModelClass,
                                 Map<TableColumn<?,?>,Getter<Well,?>> addColumns)
  {
    setOrderBy();
    DataTableModel model = _wellSearchResults.getDataTableModel();
    assertEquals("DataTableModel in use", expectedDataTableModelClass,
                 _wellSearchResults.getBaseDataTableModel().getClass());
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
    assertTrue("VirtualPagingDataModel used", _wellSearchResults.getBaseDataTableModel() instanceof VirtualPagingDataModel);
    doTestSearchResult(model, _expectedKeys);
  }

  public void testEntityModeScrolling()
  {
    {
      _wellSearchResults.searchAllWells();
      setOrderBy();
      _wellSearchResults.getRowsPerPageSelector().setSelection(_wellSearchResults.getRowsPerPageSelector().getDefaultSelection());
      assertFalse("summary view mode", _wellSearchResults.isEntityView());
      _wellSearchResults.getRowsPerPageSelector().setSelection(1);
      assertTrue("entity view mode", _wellSearchResults.isEntityView());

      DataTableModel model = _wellSearchResults.getDataTableModel();
      assertTrue("VirtualPagingDataModel used",
                 _wellSearchResults.getBaseDataTableModel() instanceof VirtualPagingDataModel);
      List<WellKey> expectedWellKeys = new ArrayList<WellKey>(_allWellKeys).subList(0, 3);
      model.setRowIndex(0);
      assertEquals("entity 0", expectedWellKeys.get(0), _wellViewer.getWell().getWellKey());
      model.setRowIndex(1);
      assertEquals("entity 1", expectedWellKeys.get(1), _wellViewer.getWell().getWellKey());
      model.setRowIndex(2);
      assertEquals("entity 2", expectedWellKeys.get(2), _wellViewer.getWell().getWellKey());

      _wellSearchResults.getRowsPerPageSelector().setSelection(_wellSearchResults.getRowsPerPageSelector().getDefaultSelection());
      assertEquals("returning to summary mode, with last-viewed entity shown as first row",
                   2,
                   _wellSearchResults.getDataTableUIComponent().getFirst());
      assertEquals("last-viewed entity shown as first row",
                   expectedWellKeys.get(2),
                   ((Well) _wellSearchResults.getDataTableModel().getRowData()).getWellKey());
    }

    // TODO: should test all of above again, but using InMemoryDataModel (since
    // both of these data models use different code to implement DataModel
    // listener notifications)

  }

  public void testValidColumnSelections()
  {
    Library rnaiLibrary = genericEntityDao.findEntityByProperty(Library.class, "libraryName", "library 3", true, "wells");
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
    doTestColumnsCreated("compound screen result", _wellSearchResults, smallMolColumnNames, true);
    doTestColumnsCreated("compound screen result", _wellSearchResults, rnaiColumnNames, false);
    _wellSearchResults.searchWellsForScreenResult(rnaiScreenResult);
    doTestColumnsCreated("rnai screen result", _wellSearchResults, smallMolColumnNames, false);
    doTestColumnsCreated("rnai screen result", _wellSearchResults, rnaiColumnNames, true);

    _wellSearchResults.searchWellsForLibrary(smallMolLibrary);
    doTestColumnsCreated("compound library", _wellSearchResults, smallMolColumnNames, true);
    doTestColumnsCreated("compound library", _wellSearchResults, rnaiColumnNames, false);
    _wellSearchResults.searchWellsForLibrary(rnaiLibrary);
    doTestColumnsCreated("compound library", _wellSearchResults, smallMolColumnNames, false);
    doTestColumnsCreated("compound library", _wellSearchResults, rnaiColumnNames, true);

    Set<WellKey> rnaiWellKeys = new HashSet<WellKey>();
    for (Well well : rnaiLibrary.getWells()) {
      rnaiWellKeys.add(well.getWellKey());
      if (rnaiWellKeys.size() == 10) {
        break;
      }
    }
    Set<WellKey> compoundWellKeys = new HashSet<WellKey>();
    for (Well well : smallMolLibrary.getWells()) {
      compoundWellKeys.add(well.getWellKey());
      if (compoundWellKeys.size() == 10) {
        break;
      }
    }
    Set<WellKey> bothWellKeys = new HashSet<WellKey>();
    bothWellKeys.addAll(rnaiWellKeys);
    bothWellKeys.addAll(compoundWellKeys);
    _wellSearchResults.searchWells(rnaiWellKeys);
    doTestColumnsCreated("rnai wells", _wellSearchResults, smallMolColumnNames, false);
    doTestColumnsCreated("rnai wells", _wellSearchResults, rnaiColumnNames, true);
    _wellSearchResults.searchWells(compoundWellKeys);
    doTestColumnsCreated("compound wells", _wellSearchResults, smallMolColumnNames, true);
    doTestColumnsCreated("compound wells", _wellSearchResults, rnaiColumnNames, false);
    _wellSearchResults.searchWells(bothWellKeys);
    doTestColumnsCreated("both wells", _wellSearchResults, smallMolColumnNames, true);
    doTestColumnsCreated("both wells", _wellSearchResults, rnaiColumnNames, true);
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

  // private methods

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
    _wellSearchResults.getColumnManager().getColumn("Compounds SMILES").setVisible(true);
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
        else if (column.getName().equals("Compounds SMILES")) {
          if (rowData.getLibrary().getScreenType() == ScreenType.SMALL_MOLECULE) {
            List<String> items = (List<String>) cellValue;
            if (items.size() > 0) {
              assertEquals("row " + j + ":Compounds",
                           "smiles" + expectedWellKey,
                           items.get(0));
            }
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
