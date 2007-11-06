// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults;

import java.util.TreeSet;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.db.ScreenResultsDAO;
import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.db.ScreenResultSortQuery.SortByWellProperty;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.ui.searchresults.ResultValueTypeColumn;
import edu.harvard.med.screensaver.ui.searchresults.WellColumn;
import edu.harvard.med.screensaver.ui.table.TableColumn;
import edu.harvard.med.screensaver.ui.table.VirtualPagingDataModel;

import org.apache.log4j.Logger;
import org.springframework.orm.hibernate3.HibernateTemplate;

public class ScreenResultDataModelTest extends AbstractSpringPersistenceTest
{
  // static members

  private static Logger log = Logger.getLogger(ScreenResultDataModelTest.class);

  protected HibernateTemplate hibernateTemplate;
  protected ScreenResultsDAO screenResultsDao;

  private Screen _rnaiScreen;
  private Screen _smallMoleculeScreen;
  private Library _rnaiLibrary;
  private WellColumn _plateNumberColumn;
  private WellColumn _wellNameColumn;
  private WellColumn _assayWellTypeColumn;
  private ScreenResult _screenResult;
  private int _plates = 3;
  private int _rowsToFetch = 10;

  @Override
  protected void onSetUp() throws Exception
  {
    super.onSetUp();

//    _smallMoleculeScreen = MakeDummyEntities.makeDummyScreen(1, ScreenType.SMALL_MOLECULE);
//    Library smallMoleculeLibrary = MakeDummyEntities.makeDummyLibrary(_smallMoleculeScreen.getScreenNumber(), _smallMoleculeScreen.getScreenType(), 3);
//    MakeDummyEntities.makeDummyScreenResult(_smallMoleculeScreen, smallMoleculeLibrary);
//    genericEntityDao.saveOrUpdateEntity(smallMoleculeLibrary);
//    genericEntityDao.saveOrUpdateEntity(_smallMoleculeScreen);

    _rnaiScreen = MakeDummyEntities.makeDummyScreen(2, ScreenType.RNAI);
    _rnaiLibrary = MakeDummyEntities.makeDummyLibrary(_rnaiScreen.getScreenNumber(), _rnaiScreen.getScreenType(), _plates);
    MakeDummyEntities.makeDummyScreenResult(_rnaiScreen, _rnaiLibrary);
    genericEntityDao.saveOrUpdateEntity(_rnaiLibrary);
    genericEntityDao.saveOrUpdateEntity(_rnaiScreen);

    _screenResult = _rnaiScreen.getScreenResult();

    _plateNumberColumn = new WellColumn(SortByWellProperty.PLATE_NUMBER,
                                        "plateNumber",
                                        "Plate Number",
                                        true) {
      @Override public Object getCellValue(Well entity) { return entity.getPlateNumber(); }
    };
    _wellNameColumn = new WellColumn(SortByWellProperty.WELL_NAME,
                                     "wellName",
                                     "Well Name",
                                     false) {
      @Override public Object getCellValue(Well entity) { return entity.getWellName(); }
    };
    _assayWellTypeColumn = new WellColumn(SortByWellProperty.ASSAY_WELL_TYPE,
                                          "assayWellType",
                                          "Assay Well Type",
                                          true) {
      @Override public Object getCellValue(Well entity) { return entity.getResultValues().get(_screenResult.getResultValueTypes().first()); }
    };
  }

  public void testFullScreenResultDataModel()
  {
    FullScreenResultDataModel dataModel =
      new FullScreenResultDataModel(_screenResult,
                                    _screenResult.getResultValueTypesList(),
                                    _screenResult.getWellCount(),
                                    _rowsToFetch,
                                    _wellNameColumn,
                                    SortDirection.ASCENDING,
                                    genericEntityDao);
    assertEquals("result size", 384 * _plates, dataModel.getRowCount());
    doTestSorts(_screenResult,
                _plateNumberColumn,
                _wellNameColumn,
                _assayWellTypeColumn,
                dataModel);
  }

  public void testPositivesScreenResultDataModel()
  {
    ResultValueType positivesRvt = _screenResult.getResultValueTypes().last();
    assertTrue("properly created 'positives' RVT with positives", positivesRvt.getPositivesCount() > 0);
    PositivesOnlyScreenResultDataModel dataModel =
      new PositivesOnlyScreenResultDataModel(_screenResult,
                                             _screenResult.getResultValueTypesList(),
                                             _rowsToFetch,
                                             _wellNameColumn,
                                             SortDirection.ASCENDING,
                                             genericEntityDao,
                                             positivesRvt);

    assertEquals("result size", (384 * 3 / 4) * _plates, dataModel.getRowCount());

    for (int i = 0; i < dataModel.getRowCount(); i++) {
      dataModel.setRowIndex(i);
      Well rowData = dataModel.getRowData();
      assertTrue("well is a positive", rowData.getResultValues().get(positivesRvt).isPositive());
    }
    doTestSorts(_screenResult,
                _plateNumberColumn,
                _wellNameColumn,
                _assayWellTypeColumn,
                dataModel);
  }

  public void testEmptyPositivesScreenResultDataModel()
  {
    PositivesOnlyScreenResultDataModel dataModel =
      new PositivesOnlyScreenResultDataModel(_screenResult,
                                             _screenResult.getResultValueTypesList(),
                                             _rowsToFetch,
                                             _wellNameColumn,
                                             SortDirection.ASCENDING,
                                             genericEntityDao,
                                             _screenResult.getResultValueTypes().first());
    assertEquals("empty positives filter", 0, dataModel.getRowCount());
  }

  public void testSinglePlateScreenResultDataModel()
  {
    Integer expectedPlateNumber = _screenResult.getPlateNumbers().last();
    SinglePlateScreenResultDataModel dataModel =
      new SinglePlateScreenResultDataModel(_screenResult,
                                           _screenResult.getResultValueTypesList(),
                                           384,
                                           _rowsToFetch,
                                           _wellNameColumn,
                                           SortDirection.ASCENDING,
                                           genericEntityDao,
                                           expectedPlateNumber);
    assertEquals("single plate result size", 384, dataModel.getRowCount());
    for (int i = 0; i < dataModel.getRowCount(); i++) {
      dataModel.setRowIndex(i);
      Well rowData = dataModel.getRowData();
      assertEquals("well is for filter plate", expectedPlateNumber, rowData.getPlateNumber());
    }
    doTestSorts(_screenResult,
                _plateNumberColumn,
                _wellNameColumn,
                _assayWellTypeColumn,
                dataModel);
  }

  public void testEmptyScreenResultDataModel()
  {
    EmptyScreenResultDataModel dataModel = new EmptyScreenResultDataModel();
    assertEquals("0 result size", 0, dataModel.getRowCount());
  }

  public void testMinimalDataLoaded()
  {
    // create another screen result using the same wells as screen 2, to associate more result values with those wells
    Screen rnaiScreen2 = MakeDummyEntities.makeDummyScreen(3, ScreenType.RNAI);
    ScreenResult screenResult2 = MakeDummyEntities.makeDummyScreenResult(rnaiScreen2, _rnaiLibrary);
    genericEntityDao.saveOrUpdateEntity(rnaiScreen2);
    Well well = genericEntityDao.findEntityById(Well.class, screenResult2.getWells().first().getWellId(), true, "resultValues");
    assertEquals("well has result values for two screen results",
                 well.getResultValues().size(),
                 screenResult2.getResultValueTypes().size() * 2);

    WellColumn sortColumn =
      new WellColumn(SortByWellProperty.WELL_NAME,
                     "wellName",
                     "Well Name",
                     false) {
      @Override public Object getCellValue(Well entity) { return entity; }
    };
    ScreenResult screenResult = _rnaiScreen.getScreenResult();
    FullScreenResultDataModel dataModel =
      new FullScreenResultDataModel(screenResult,
                                    screenResult.getResultValueTypesList(),
                                    screenResult.getWellCount(),
                                    _rowsToFetch,
                                    sortColumn,
                                    SortDirection.ASCENDING,
                                    genericEntityDao);
    dataModel.setRowIndex(0);
    Well rowData = dataModel.getRowData();
    assertEquals("well has only result values for this screen result",
                 screenResult.getResultValueTypes(),
                 new TreeSet<ResultValueType>(rowData.getResultValues().keySet()));
  }

  private void doTestSorts(final ScreenResult screenResult,
                           WellColumn plateNumberColumn,
                           WellColumn wellNameColumn,
                           WellColumn assayWellTypeColumn,
                           ScreenResultDataModel dataModel)
  {
    for (SortDirection sortDir : SortDirection.values()) {
      doTestSort(dataModel, plateNumberColumn, sortDir);
      doTestSort(dataModel, wellNameColumn, sortDir);
      doTestSort(dataModel, assayWellTypeColumn, sortDir);
      for (ResultValueType rvt : screenResult.getResultValueTypesList()) {
        ResultValueTypeColumn rvtColumn = new ResultValueTypeColumn(rvt);
        doTestSort(dataModel, rvtColumn, sortDir);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void doTestSort(VirtualPagingDataModel<String,Well> dataModel, TableColumn sortColumn, SortDirection sortDirection)
  {
    dataModel.sort(sortColumn, sortDirection);
    Well lastRowData = null;
    for (int i = 0; i < _rowsToFetch; i++) {
      dataModel.setRowIndex(i);
      Well rowData = dataModel.getRowData();
      if (lastRowData != null) {
        assertTrue("sorted values on column " + sortColumn + ", " + sortDirection,
                   sortColumn.getComparator(sortDirection).compare(lastRowData, rowData) <= 0);
        lastRowData = rowData;
      }
    }
  }

}
