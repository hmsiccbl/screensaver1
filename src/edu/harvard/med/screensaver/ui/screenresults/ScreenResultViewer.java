//$HeadURL$
//$Id$

//Copyright 2006 by the President and Fellows of Harvard College.

//Screensaver is an open-source project developed by the ICCB-L and NSRB labs
//at Harvard Medical School. This software is distributed under the terms of
//the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.faces.component.UIData;
import javax.faces.component.UIInput;
import javax.faces.event.ValueChangeEvent;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.db.ScreenResultsDAO;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultExporter;
import edu.harvard.med.screensaver.io.workbook.Workbook;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellType;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.ui.libraries.WellViewer;
import edu.harvard.med.screensaver.ui.screens.ScreenViewer;
import edu.harvard.med.screensaver.ui.searchresults.ScreenSearchResults;
import edu.harvard.med.screensaver.ui.table.DataTableRowsPerPageUISelectOneBean;
import edu.harvard.med.screensaver.ui.table.SortChangedEvent;
import edu.harvard.med.screensaver.ui.table.TableColumn;
import edu.harvard.med.screensaver.ui.table.TableSortManager;
import edu.harvard.med.screensaver.ui.util.JSFUtils;
import edu.harvard.med.screensaver.ui.util.UISelectManyBean;
import edu.harvard.med.screensaver.ui.util.UISelectOneBean;
import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataAccessException;


/**
 * JSF backing bean for Screen Result Viewer web page (screenresultviewer.jsp).
 * <p>
 * The <code>screenResult</code> property should be set to the
 * {@link ScreenResult} that is to be viewed.<br>

 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
@SuppressWarnings("unchecked")
public class ScreenResultViewer extends AbstractBackingBean implements Observer
{

  // static data members

  private static Logger log = Logger.getLogger(ScreenResultViewer.class);

  private static final ScreenResultDataModel EMPTY_RAW_DATA_MODEL = new EmptyScreenResultDataModel();

  public static final Integer DATA_TABLE_FILTER_SHOW_ALL = -2;
  public static final Integer DATA_TABLE_FILTER_SHOW_POSITIVES = -1;
  public static final int DATA_TABLE_FIXED_COLUMNS = 3;


  // instance data members

  private GenericEntityDAO _dao;
  private ScreenResultsDAO _screenResultsDao;
  private LibrariesDAO _librariesDao;
  private ScreenSearchResults _screensBrowser;
  private ScreenViewer _screenViewer;
  private ScreenResultExporter _screenResultExporter;
  private WellViewer _wellViewer;

  private ScreenResult _screenResult;
  private Map<String,Boolean> _isPanelCollapsedMap;

  private ResultValueTypeTable _rvtTable;

  // data members for raw data table
  private ScreenResultDataModel _rawDataModel;
  private UIData _dataTable;
  private TableSortManager<Map<String,Object>> _sortManager;
  private UIInput _dataTableRowsPerPageUIInput;
  private UISelectOneBean<Integer> _dataFilter;
  private DataTableRowsPerPageUISelectOneBean _dataTableRowsPerPage;
  private UISelectOneBean<ResultValueType> _showPositivesOnlyForDataHeader;
  private int _screenResultSize;



  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected ScreenResultViewer()
  {
  }

  public ScreenResultViewer(GenericEntityDAO dao,
                            ScreenResultsDAO screenResultsDao,
                            LibrariesDAO librariesDao,
                            ScreenSearchResults screensBrowser,
                            ScreenViewer screenViewer,
                            ScreenResultExporter screenResultExporter,
                            WellViewer wellViewer,
                            ResultValueTypeTable rvtTable)
  {
    _dao = dao;
    _screenResultsDao = screenResultsDao;
    _librariesDao = librariesDao;
    _screensBrowser = screensBrowser;
    _screenViewer = screenViewer;
    _screenResultExporter = screenResultExporter;
    _wellViewer = wellViewer;
    _rvtTable = rvtTable;

    _isPanelCollapsedMap = new HashMap<String,Boolean>();
    _isPanelCollapsedMap.put("screenSummary", false);
    _isPanelCollapsedMap.put("screenResultSummary", false);
    _isPanelCollapsedMap.put("dataHeadersTable", true);
    _isPanelCollapsedMap.put("dataTable", true);
    _isPanelCollapsedMap.put("heatMaps", true);
  }


  // public methods

  public void setScreenResult(ScreenResult screenResult)
  {
    resetView();
    _screenResult = screenResult;
    getDataHeadersTable().initialize(getResultValueTypes(), this);
  }

  public ScreenResult getScreenResult()
  {
    return _screenResult;
  }

  public void setScreenResultSize(int screenResultSize)
  {
    _screenResultSize = screenResultSize;
  }

  public Map<?,?> getIsPanelCollapsedMap()
  {
    return _isPanelCollapsedMap;
  }

  public UISelectManyBean<ResultValueType> getDataHeaderSelections()
  {
    return getDataHeadersTable().getSelections();
  }

  public ResultValueTypeTable getDataHeadersTable()
  {
    return _rvtTable;
  }

  @SuppressWarnings("unchecked")
  public void update(Observable o, Object obj)
  {
    if (o == getDataHeadersTable().getSelections()) {
      // visible columns changed
      _sortManager.deleteObserver(this); // avoid recursive notifications
        if (log.isDebugEnabled()) {
          log.debug("data header selection changed");
        }
        _sortManager = null; // force recreate
        updateDataTableContent();
    }
    else if (obj instanceof SortChangedEvent) {
      // sort column changed
      SortChangedEvent<Map<String,Object>> event = (SortChangedEvent<Map<String,Object>>) obj;
      if (log.isDebugEnabled()) {
        log.debug(event.toString());
      }
      // TODO: full rebuild is only strictly needed by FullScreenResultDataModel, other ScreenResultDataModel classes could have a callback method called to avoid database calls (they would do their own in-memory sorting)
      updateDataTableContent();
    }
  }


  // JSF application methods

  @UIControllerMethod
  public String download()
  {
    try {
      _dao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          ScreenResult screenResult = _dao.reloadEntity(_screenResult,
                                                        true,
          "hbnResultValueTypes");
          // note: we eager fetch the resultValues for each ResultValueType
          // individually, since fetching all with a single needReadOnly() call
          // would generate an SQL result cross-product for all RVTs+RVs that
          // would include a considerable amount of redundant data
          // for the (denormalized) RVT fields
          for (ResultValueType rvt : screenResult.getResultValueTypes()) {
            // note: requesting the iterator generates an SQL statement that
            // only includes the result_value_type_result_values table, whereas
            // the needReadOnly() call's SQL statement joins to the
            // result_value_type table as well, which is slower
            rvt.getResultValues().keySet().iterator();
            //_dao.needReadOnly(rvt, "resultValues");
          }
          File exportedWorkbookFile = null;
          FileOutputStream out = null;
          try {
            if (screenResult != null) {
              HSSFWorkbook workbook = _screenResultExporter.build(screenResult);
              exportedWorkbookFile = File.createTempFile("screenResult" + screenResult.getScreen().getScreenNumber() + ".",
              ".xls");
              out = new FileOutputStream(exportedWorkbookFile);
              workbook.write(out);
              out.close();
              JSFUtils.handleUserFileDownloadRequest(getFacesContext(),
                                                     exportedWorkbookFile,
                                                     Workbook.MIME_TYPE);
            }
          }
          catch (IOException e)
          {
            reportApplicationError(e);
          }
          finally {
            IOUtils.closeQuietly(out);
            if (exportedWorkbookFile != null && exportedWorkbookFile.exists()) {
              exportedWorkbookFile.delete();
            }
          }
        }
      });
    }
    catch (DataAccessException e) {
      showMessage("databaseOperationFailed", e.getMessage());
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String delete()
  {
    if (_screenResult != null) {
      try {
        _screenResultsDao.deleteScreenResult(_screenResult);
        _screensBrowser.invalidateSearchResult();
        return _screenViewer.viewScreen(_screenResult.getScreen());
      }
      catch (ConcurrencyFailureException e) {
        showMessage("concurrentModificationConflict");
      }
      catch (DataAccessException e) {
        showMessage("databaseOperationFailed", e.getMessage());
      }
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String saveScreenResult()
  {
    // note: saving the parent screen will save its screenResult; assumes
    // ScreenViewer and ScreenResultViewer are in sync (showing data for same
    // screen)
    return _screenViewer.saveScreen();
  }


  // data table methods

  public UIData getDataTable()
  {
    return _dataTable;
  }

  public void setDataTable(UIData dataUIComponent)
  {
    _dataTable = dataUIComponent;
  }

  public ScreenResultDataModel getDataTableModel()
  {
    if (_rawDataModel == null) {
      updateDataTableContent();
    }
    return _rawDataModel;
  }

  public TableSortManager<Map<String,Object>> getSortManager()
  {
    if (_sortManager == null) {
      List<TableColumn<Map<String,Object>>> columns = getDataTableFixedColumns();
      columns.addAll(getColumnsForSelectedDataHeaders());
      _sortManager = new TableSortManager<Map<String,Object>>(columns);
      _sortManager.addObserver(this);
    }
    return _sortManager;
  }

  public UIInput getDataTableRowsPerPageUIInput()
  {
    return _dataTableRowsPerPageUIInput;
  }

  public void setDataTableRowsPerPageUIInput(UIInput dataTableRowsPerPageUIInput)
  {
    _dataTableRowsPerPageUIInput = dataTableRowsPerPageUIInput;
  }

  public String cellAction()
  {
    return (String) getSortManager().getCurrentColumn().cellAction(getDataTableModel().getRowData());
  }

  public boolean isResultValueExcluded()
  {
    return getDataTableModel().isResultValueCellExcluded(getSortManager().getCurrentColumnIndex());
  }

  public UISelectOneBean<Integer> getDataFilter()
  {
    if (_dataFilter == null) {
      updateDataFilterSelections();
    }
    return _dataFilter;
  }

  public UISelectOneBean<Integer> getDataTableRowsPerPage()
  {
    if (_dataTableRowsPerPage == null) {
      updateDataTableRowsPerPageSelections();
    }
    return _dataTableRowsPerPage;
  }

  public boolean isNumericColumn()
  {
    return getSortManager().getCurrentColumn().isNumeric();
  }

  public int getRawDataSize()
  {
    return getDataTableModel().getRowCount();
  }

  public UISelectOneBean<ResultValueType> getShowPositivesOnlyForDataHeader()
  {
    if (_showPositivesOnlyForDataHeader == null) {
      updateDataHeaderSelectionsForShowPositives();
    }
    return _showPositivesOnlyForDataHeader;
  }

  private void updateDataTableContent()
  {
    log.debug("updating data table content");
    if (_screenResult == null) {
      _rawDataModel = EMPTY_RAW_DATA_MODEL;
    }
    else if (getDataFilter().getSelection() == DATA_TABLE_FILTER_SHOW_ALL) {
      _rawDataModel = new FullScreenResultDataModel(_screenResult,
                                                    getDataHeaderSelections().getSelections(),
                                                    getSortManager().getSortColumnIndex(),
                                                    getSortManager().getSortDirection(),
                                                    _screenResultsDao,
                                                    getDataTableRowsPerPage().getSelection(),
                                                    _screenResultSize);
    }
    else if (getDataFilter().getSelection() == DATA_TABLE_FILTER_SHOW_POSITIVES) {
      _rawDataModel = new PositivesOnlyScreenResultDataModel(_screenResult,
                                                             getDataHeaderSelections().getSelections(),
                                                             getSortManager().getSortColumnIndex(),
                                                             getSortManager().getSortDirection(),
                                                             _screenResultsDao,
                                                             getShowPositivesOnlyForDataHeader().getSelection());
    }
    else if (getDataFilter().getSelection() >= 0) { // plate number
      _rawDataModel = new SinglePlateScreenResultDataModel(_screenResult,
                                                           getDataHeaderSelections().getSelections(),
                                                           getSortManager().getSortColumnIndex(),
                                                           getSortManager().getSortDirection(),
                                                           _screenResultsDao,
                                                           getDataFilter().getSelection());
    }
    else {
      log.warn("unknown data filter value");
      _rawDataModel = EMPTY_RAW_DATA_MODEL;
    }
    gotoDataTableRowIndex(0);
  }

  private void updateDataHeaderSelectionsForShowPositives()
  {
    log.debug("updating data header selections for show positives");
    List<ResultValueType> resultValueTypes = new ArrayList<ResultValueType>();
    resultValueTypes.addAll(getResultValueTypes());
    for (Iterator<ResultValueType> iter = resultValueTypes.iterator(); iter.hasNext();) {
      ResultValueType rvt = iter.next();
      if (!rvt.isPositiveIndicator()) {
        iter.remove();
      }
    }
    _showPositivesOnlyForDataHeader = new UISelectOneBean<ResultValueType>(resultValueTypes) {
      @Override
      protected String getLabel(ResultValueType t)
      {
        return t.getName();
      }
    };
  }

  private void updateDataFilterSelections()
  {
    if (_screenResult == null) {
      _dataFilter = new UISelectOneBean<Integer>();
      return;
    }

    log.debug("updating data table filter selections");

    SortedSet<Integer> filters = new TreeSet<Integer>(_screenResult.getPlateNumbers());
    filters.add(DATA_TABLE_FILTER_SHOW_ALL);
    if (getShowPositivesOnlyForDataHeader().getSize() > 0) {
      filters.add(DATA_TABLE_FILTER_SHOW_POSITIVES);
    }
    _dataFilter =
      new UISelectOneBean<Integer>(filters, DATA_TABLE_FILTER_SHOW_ALL) {
      @Override
      protected String getLabel(Integer val)
      {
        if (val == DATA_TABLE_FILTER_SHOW_ALL) {
          return "All";
        }
        if (val == DATA_TABLE_FILTER_SHOW_POSITIVES) {
          return "Positives";
        }
        return super.getLabel(val);
      }
    };
  }

  private void updateDataTableRowsPerPageSelections()
  {
    log.debug("updating data table rows per page selections");
    if (getDataFilter().getSelection() == DATA_TABLE_FILTER_SHOW_ALL) {
      // note: don't allow "show all rows" when not filtering result values (too many!)
      _dataTableRowsPerPage = new DataTableRowsPerPageUISelectOneBean(Arrays.asList(16, 24, 48, 96, 384));
    }
    else if (getDataFilter().getSelection() == DATA_TABLE_FILTER_SHOW_POSITIVES) {
      _dataTableRowsPerPage = new DataTableRowsPerPageUISelectOneBean(Arrays.asList(10, 20, 50, 100, DataTableRowsPerPageUISelectOneBean.SHOW_ALL_VALUE));
      _dataTableRowsPerPage.setAllRowsValue(getRawDataSize());
    }
    else { // single plate
      _dataTableRowsPerPage = new DataTableRowsPerPageUISelectOneBean(Arrays.asList(16, 24, 48, 96, 384));
    }
    // prevent dataTableRowsPerPageListener from being invoked; we only want
    // listener invoked when the user explicitly changes the selection in this
    // UIInput component, not in response to a programmatic update
    _dataTableRowsPerPageUIInput.setValue(_dataTableRowsPerPage.getValue());
  }


  // JSF event listener methods

  public void rowNumberListener(ValueChangeEvent event)
  {
    if (event.getNewValue() != null && event.getNewValue().toString().trim().length() > 0) {
      log.debug("row number changed to " + event.getNewValue());
      gotoDataTableRowIndex(Integer.parseInt(event.getNewValue().toString()) - 1);

      // skip "update model" JSF phase, to avoid overwriting model values set above
      getFacesContext().renderResponse();
    }
  }

  /**
   * Called when the set of rows to be displayed in the table needs to be changed (row filtering).
   */
  public void dataTableFilterListener(ValueChangeEvent event)
  {
    log.debug("dataTableFilter changed to " + event.getNewValue());
    getDataFilter().setValue((String) event.getNewValue());
    // ordering of next 2 lines is significant
    updateDataTableRowsPerPageSelections();
    updateDataTableContent();
    // setAllRowsValue() was already called in
    // updateDataTableRowsPerPageSelections(), above, but its value was
    // determined from the previous data table model, due to ordering or above
    // two lines
    _dataTableRowsPerPage.setAllRowsValue(getRawDataSize());

    // skip "update model" JSF phase, to avoid overwriting model values set above
    getFacesContext().renderResponse();
  }

  /**
   * Called when the set of rows to be displayed in the table needs to be changed (row filtering).
   */
  public void showPositivesForDataHeaderListener(ValueChangeEvent event)
  {
    log.debug("showPositivesForDataHeader changed to " + event.getNewValue());
    getShowPositivesOnlyForDataHeader().setValue((String) event.getNewValue());
    updateDataTableContent();

    // skip "update model" JSF phase, to avoid overwriting model values set above
    getFacesContext().renderResponse();
  }

  public void dataTableRowsPerPageListener(ValueChangeEvent event)
  {
    log.debug("dataTableRowsPerPage changed to " + event.getNewValue());
    getDataTableRowsPerPage().setValue((String) event.getNewValue());
    getDataTableModel().setRowsToFetch(getDataTableRowsPerPage().getSelection());

    // skip "update model" JSF phase, to avoid overwriting model values set above
    getFacesContext().renderResponse();
  }



  // protected methods

  protected ScreensaverUserRole getEditableAdminRole()
  {
    return ScreensaverUserRole.SCREEN_RESULTS_ADMIN;
  }


  // private methods

  /**
   * Get ResultValueTypes set, safely, handling case that no screen result
   * exists.
   */
  private List<ResultValueType> getResultValueTypes()
  {
    List<ResultValueType> rvts = new ArrayList<ResultValueType>();
    if (getScreenResult() != null) {
      rvts.addAll(getScreenResult().getResultValueTypes());
    }
    return rvts;
  }

  private List<TableColumn<Map<String,Object>>> getDataTableFixedColumns()
  {
    List<TableColumn<Map<String,Object>>> fixedColumns = new ArrayList<TableColumn<Map<String,Object>>>(3);
    fixedColumns.add(new TableColumn<Map<String,Object>>("Plate", "The plate number", true) {
      @Override
      public Object getCellValue(Map<String,Object> row) { return row.get(getName()); }
    });
    fixedColumns.add(new TableColumn<Map<String,Object>>("Well", "The well name") {
      @Override
      public Object getCellValue(Map<String,Object> row) { return row.get(getName()); }

      @Override
      public boolean isCommandLink() { return true; }

      @Override
      public Object cellAction(Map<String,Object> row)
      {
        Integer plateNumber = (Integer) getSortManager().getColumn(0).getCellValue(row);
        String wellName = (String) getSortManager().getColumn(1).getCellValue(row);
        Well well = _librariesDao.findWell(new WellKey(plateNumber, wellName));
        return _wellViewer.viewWell(well);
      }
    });
    fixedColumns.add(new TableColumn<Map<String,Object>>("Type",
      StringUtils.makeListString(StringUtils.wrapStrings(Arrays.asList(WellType.values()), "'", "'"), ", ").toLowerCase()) {
      @Override
      public Object getCellValue(Map<String,Object> row) { return row.get(getName()); }
    });
    return fixedColumns;
  }

  private List<TableColumn<Map<String,Object>>> getColumnsForSelectedDataHeaders()
  {
    ArrayList<TableColumn<Map<String,Object>>> result = new ArrayList<TableColumn<Map<String,Object>>>();
    List<ResultValueType> selectedDataHeaders = getDataHeaderSelections().getSelections();
    for (ResultValueType rvt : selectedDataHeaders) {
      result.add(new TableColumn<Map<String,Object>>(rvt.getUniqueName(), rvt.getDescription(), rvt.isNumeric()) {
        @Override
        public Object getCellValue(Map<String,Object> row)
        {
          return row.get(getName());
        }
      });
    }
    return result;
  }

  private void resetView()
  {
    _dataFilter = null;
    _dataTableRowsPerPage = null;
    _rawDataModel = null;
    _sortManager = null;
    _showPositivesOnlyForDataHeader = null;
    _screenResultSize = 0;
  }

  private void gotoDataTableRowIndex(int rowIndex)
  {
    log.debug("goto data table row index " + rowIndex);
    // ensure value is within valid range, and in particular that we never show
    // less than the table's configured row count (unless it's more than the
    // total number of rows)
    rowIndex = Math.max(0,
                        Math.min(rowIndex,
                                 getRawDataSize() - _dataTable.getRows()));
    _dataTable.setFirst(rowIndex);
  }

}
