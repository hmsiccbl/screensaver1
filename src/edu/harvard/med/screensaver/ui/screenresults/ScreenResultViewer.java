// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import javax.faces.component.UISelectMany;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.db.ScreenResultsDAO;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultExporter;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellType;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.UniqueDataHeaderNames;
import edu.harvard.med.screensaver.ui.control.LibrariesController;
import edu.harvard.med.screensaver.ui.control.ScreensController;
import edu.harvard.med.screensaver.ui.table.SortChangedEvent;
import edu.harvard.med.screensaver.ui.table.TableColumn;
import edu.harvard.med.screensaver.ui.table.TableSortManager;
import edu.harvard.med.screensaver.ui.util.UISelectManyBean;
import edu.harvard.med.screensaver.ui.util.UISelectOneBean;
import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;


/**
 * JSF backing bean for Screen Result Viewer web page (screenresultviewer.jsp).
 * <p>
 * The <code>screenResult</code> property should be set to the
 * {@link ScreenResult} that is to be viewed.<br>

 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class ScreenResultViewer extends AbstractBackingBean implements Observer
{

  // static data members
  
  private static Logger log = Logger.getLogger(ScreenResultViewer.class);
  
  private static final DataModel EMPTY_DATAHEADERS_MODEL = new ListDataModel(new ArrayList<DataHeaderRow>());
  private static final ScreenResultDataModel EMPTY_RAW_DATA_MODEL = new EmptyScreenResultDataModel();
  // TODO: consider replacing DataHeaderRowDefinition with TableColumn<ResultValueType>
  private static final DataHeaderRowDefinition[] DATA_HEADER_ATTRIBUTES = new DataHeaderRowDefinition[] {
    new DataHeaderRowDefinition("description", "Description", "A description of the data header"),
    new DataHeaderRowDefinition("replicateOrdinal", "Replicate Number", "To which replicate this data header refers"),
    new DataHeaderRowDefinition("assayReadoutType", "Assay Readout Type", "The type of readout used to calculate these values"),
    new DataHeaderRowDefinition("timePoint", "Time Point", "The time point the readout was taken"),
    new DataHeaderRowDefinition("derived", "Derived", "True when this column is derived from other data headers"),
    new DataHeaderRowDefinition("howDerived", "How Derived", "How this column was derived from other data headers"),
    new DataHeaderRowDefinition("typesDerivedFrom", "Types Derived From", "The data headers from which this column was derived") 
    { 
      @Override
      public String formatValue(ResultValueType rvt)
      {
        StringBuilder typesDerivedFromText = new StringBuilder();
        for (ResultValueType derivedFromRvt : rvt.getTypesDerivedFrom()) {
          if (typesDerivedFromText.length() > 0) {
            typesDerivedFromText.append(", ");
          }
          typesDerivedFromText.append(derivedFromRvt.getUniqueName());
        }
        return typesDerivedFromText.toString();
      }
    },
    new DataHeaderRowDefinition("positiveIndicator", "Positive Indicator", "True if this data header is used to indicate \"positives\""),
    new DataHeaderRowDefinition("positiveIndicatorType", "Positive Indicator Type", "'Numerical', 'Boolean', or 'Partition'"),
    new DataHeaderRowDefinition("positiveIndicatorDirection", "Indicator Direction", "For numerical indicators, whether high or low values are \"positives\""),
    new DataHeaderRowDefinition("positiveIndicatorCutoff", "Indicator Cutoff", "The numerical score demarking \"positives\" from \"non-positives\""),
    new DataHeaderRowDefinition("followUpData", "Follow Up Data", "Primary or follow up screen data"),
    new DataHeaderRowDefinition("assayPhenotype", "Assay Phenotype", "The phenotype being monitored"),
    new DataHeaderRowDefinition("comments", "Comments", "Data header comments"),
    new DataHeaderRowDefinition("positivesCount", "Positives", "The number of \"positives\", if this is a Positive Indicator"),
    new DataHeaderRowDefinition("positivesPercentage", "Positive %", "The % of experimental wells in the results that have been deemed \"positive\"")
    {
      @Override
      public String formatValue(ResultValueType rvt)
      {
        if (rvt.getPositivesPercentage() == null) {
          return "";
        }
        return NumberFormat.getPercentInstance().format(rvt.getPositivesPercentage());
      }
    }
  };

  public static final Integer DATA_TABLE_FILTER_SHOW_ALL = -2;
  public static final Integer DATA_TABLE_FILTER_SHOW_POSITIVES = -1;

  public static final int DATA_TABLE_FIXED_COLUMNS = 3;

  
  // instance data members

  private ScreensController _screensController;
  private LibrariesController _librariesController;
  private ScreenResultsDAO _screenResultsDao;
  private LibrariesDAO _librariesDao;
  private ScreenResultExporter _screenResultExporter;
  private ScreenResult _screenResult;
  private Map<String,Boolean> _isPanelCollapsedMap;
  
  // data members for data headers table
  private UniqueDataHeaderNames _uniqueDataHeaderNames;
  private UISelectMany _dataHeadersSelectManyUIInput;
  private UISelectManyBean<ResultValueType> _selectedDataHeaders;
  private DataModel _dataHeadersColumnModel;
  private DataModel _dataHeadersModel;
  private TableSortManager<Map<String,Object>> _sortManager;

  // data members for raw data table 
  /**
   * Data model for the raw data, <i>containing only the set of rows being displayed in the current view</i>.
   */
  private ScreenResultDataModel _rawDataModel;
  /**
   * For internal tracking of first data row displayed in data table (the data
   * table's model rowIndex is always 0).
   */
  private UIData _dataTable;
  private UIInput _rowNumberUIInput;
  private UIInput _dataTableRowsPerPageUIInput;
  private UISelectOneBean<Integer> _dataFilter;
  private DataTableRowsPerPageUISelectOneBean _dataTableRowsPerPage;
  private UISelectOneBean<ResultValueType> _showPositivesOnlyForDataHeader;
  private int _screenResultSize;


  
  // public methods
  
  public ScreenResultViewer()
  {
    _isPanelCollapsedMap = new HashMap<String,Boolean>();
    _isPanelCollapsedMap.put("screenSummary", false);
    _isPanelCollapsedMap.put("screenResultSummary", false);
    _isPanelCollapsedMap.put("dataHeadersTable", true);
    _isPanelCollapsedMap.put("dataTable", true);
    _isPanelCollapsedMap.put("heatMaps", true);
  }
  
  public void setLibrariesDao(LibrariesDAO librariesDao)
  {
    _librariesDao = librariesDao;
  }

  public void setScreenResultsDao(ScreenResultsDAO screenResultsDao)
  {
    _screenResultsDao = screenResultsDao;
  }

  public void setScreensController(ScreensController screensController) 
  {
    _screensController = screensController;
  }

  public void setLibrariesController(LibrariesController librariesController) 
  {
    _librariesController = librariesController;
  }

  public void setScreenResult(ScreenResult screenResult)
  {
    resetView();
    _screenResult = screenResult;
  }

  public ScreenResult getScreenResult()
  {
    return _screenResult;
  }

  public void setScreenResultSize(int screenResultSize)
  {
    _screenResultSize = screenResultSize;
  }
  
  public void setScreenResultExporter(ScreenResultExporter screenResultExporter)
  {
    _screenResultExporter = screenResultExporter;
  }

  public Map getIsPanelCollapsedMap()
  {
    return _isPanelCollapsedMap;
  }
  
  public UISelectMany getDataHeadersSelectManyUIInput()
  {
    return _dataHeadersSelectManyUIInput;
  }

  public void setDataHeadersSelectManyUIInput(UISelectMany dataHeadersSelectMany)
  {
    _dataHeadersSelectManyUIInput = dataHeadersSelectMany;
  }

  public UIInput getRowNumberUIInput()
  {
    return _rowNumberUIInput;
  }

  public void setRowNumberUIInput(UIInput rowNumberInput)
  {
    _rowNumberUIInput = rowNumberInput;
  }

  public UIInput getDataTableRowsPerPageUIInput()
  {
    return _dataTableRowsPerPageUIInput;
  }

  public void setDataTableRowsPerPageUIInput(UIInput dataTableRowsPerPageUIInput)
  {
    _dataTableRowsPerPageUIInput = dataTableRowsPerPageUIInput;
  }

  /** Get 1-based row number of the first row displayed in the data table. */
  public int getRowNumber()
  {
    return getDataTable().getFirst() + 1;
  }

  /** Set 1-based row number of the first row displayed in the data table. */
  public void setRowNumber(int rowNumber)
  {
    getDataTable().setFirst(rowNumber - 1);
  }

  public UIData getDataTable()
  {
    return _dataTable;
  }

  public void setDataTable(UIData dataUIComponent)
  {
    _dataTable = dataUIComponent;
  }
  
  public DataModel getDataHeaders()
  {
    lazyBuildDataHeadersModel();
    if (_dataHeadersModel == null) {
      return EMPTY_DATAHEADERS_MODEL;
    }
    return _dataHeadersModel;
  }

  public ScreenResultDataModel getDataTableModel()
  {
    if (_rawDataModel == null) {
      updateDataTableContent();
    }
    return _rawDataModel;
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

  public String getRowRangeText()
  {
    return getRowNumber() + 
           ".." + 
           Math.min(getRowNumber() + getDataTableRowsPerPage().getSelection() - 1, 
                    getRawDataSize()) + 
           " of " + 
           getRawDataSize();
  }
  
  public UISelectManyBean<ResultValueType> getSelectedDataHeaders()
  {
    if (_selectedDataHeaders == null) {
      _selectedDataHeaders = new UISelectManyBean<ResultValueType>(getResultValueTypes())
      {
        @Override
        protected String getLabel(ResultValueType rvt)
        {
          return getUniqueDataHeaderNames().get(rvt);
        }
      };
      // select all data headers, initially
      _selectedDataHeaders.setSelections(getResultValueTypes());
      
      _selectedDataHeaders.addObserver(this);
    }
    return _selectedDataHeaders;
  }

  public DataModel getDataHeadersColumnModel()
  {
    if (_dataHeadersColumnModel == null) {
      updateDataHeadersColumnModel();
    }
    return _dataHeadersColumnModel;
  }
  
  public UniqueDataHeaderNames getUniqueDataHeaderNames()
  {
    if (_uniqueDataHeaderNames == null) {
      _uniqueDataHeaderNames = new UniqueDataHeaderNames(getScreenResult());
    }
    return _uniqueDataHeaderNames;
  }
  
  /**
   * @motivation for "Columns" JSF data table component
   */
  public Object getDataHeadersCellValue()
  {
    DataModel dataModel = getDataHeaders();
    DataModel columnModel = getDataHeadersColumnModel();
    if (columnModel.isRowAvailable()) {
      String columnName = (String) columnModel.getRowData();  // getRowData() is really getColumnData()
      DataHeaderRow row = (DataHeaderRow) dataModel.getRowData();
      return row.getDataHeaderSinglePropertyValues().get(columnName);
    }
    return null;
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

  public UISelectOneBean<ResultValueType> getShowPositivesOnlyForDataHeader()
  {
    if (_showPositivesOnlyForDataHeader == null) {
      updateDataHeaderSelectionsForShowPositives();
    }
    return _showPositivesOnlyForDataHeader;
  }
  
  // UI update pseudo-event handlers

  private void updateDataHeadersColumnModel()
  {
    log.debug("updating data headers");
    _dataHeadersColumnModel = new ListDataModel(getSelectedDataHeaderNames());
  }

  private void updateDataTableContent()
  {
    log.debug("updating data table content");
    if (_screenResult == null) {
      _rawDataModel = EMPTY_RAW_DATA_MODEL;
    } 
    else if (getDataFilter().getSelection() == DATA_TABLE_FILTER_SHOW_ALL) {
      _rawDataModel = new FullScreenResultDataModel(_screenResult,
                                                    getSelectedDataHeaders().getSelections(),
                                                    getSortManager().getSortColumnIndex(),
                                                    getSortManager().getSortDirection(),
                                                    _screenResultsDao,
                                                    getDataTableRowsPerPage().getSelection(),
                                                    _screenResultSize);
    }
    else if (getDataFilter().getSelection() == DATA_TABLE_FILTER_SHOW_POSITIVES) {
      _rawDataModel = new PositivesOnlyScreenResultDataModel(_screenResult,
                                                             getSelectedDataHeaders().getSelections(),
                                                             getSortManager().getSortColumnIndex(),
                                                             getSortManager().getSortDirection(),
                                                             _screenResultsDao,
                                                             getShowPositivesOnlyForDataHeader().getSelection());
    }
    else if (getDataFilter().getSelection() >= 0) { // plate number
      _rawDataModel = new SinglePlateScreenResultDataModel(_screenResult,
                                                           getSelectedDataHeaders().getSelections(),
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

  private void updateSortManagerWithSelectedDataHeaders()
  {
    log.debug("updating sort manager with selected data headers");
    List<TableColumn<Map<String,Object>>> columns = getDataTableFixedColumns();
    columns.addAll(getColumnsForSelectedDataHeaders());
    getSortManager().setColumns(columns);
  }

  private void updateDataHeaderSelectionsForShowPositives()
  {
    log.debug("updating data header selections for show positives");
    List<ResultValueType> resultValueTypes = new ArrayList<ResultValueType>();
    resultValueTypes.addAll(getResultValueTypes());
    for (Iterator iter = resultValueTypes.iterator(); iter.hasNext();) {
      ResultValueType rvt = (ResultValueType) iter.next();
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


  // JSF application methods
  
  public String gotoPage(int pageIndex)
  {
    try {
      int newRowIndex = (pageIndex * getDataTable().getRows());
      if (newRowIndex >= 0 && newRowIndex < getRawDataSize()) {
        getDataTable().setFirst(newRowIndex);
      }
      return REDISPLAY_PAGE_ACTION_RESULT;
    } 
    catch (Exception e) {
      return ERROR_ACTION_RESULT;
    }
  }
  
  public String firstPage()
  {
    return gotoPage(0);
  }

  public String lastPage()
  {
    int rowsPerPage = getDataTable().getRows();
    if (rowsPerPage > 0) {
      int newPage = Math.max(0, getRawDataSize() / rowsPerPage);
      // handle case where total rows is evenly divided by rowsPerPage, in which
      // case "last page" would have 0 rows
      if (newPage * rowsPerPage == getRawDataSize()) {
        --newPage;
      }
      return gotoPage(newPage);
    }
    else {
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
  }
  
  public String nextPage()
  {
    return gotoPage(getPageIndex() + 1); 
  }
  
  public String prevPage()
  {
    return gotoPage(getPageIndex() - 1); 
  }
  
  public String download()
  {
    return _screensController.downloadScreenResult(getScreenResult());
  }
  
  public String delete()
  {
    return _screensController.deleteScreenResult(getScreenResult());
  }
  
  public String cellAction()
  { 
    return (String) getSortManager().getCurrentColumn().cellAction(getDataTableModel().getRowData());
  }
  
  public String saveScreenResult()
  {
    // note: saving the parent screen will save its screenResult
    return _screensController.saveScreen(_screenResult.getScreen(), null);
  }
  
  public String showAllDataHeaders()
  {
    getSelectedDataHeaders().setSelections(getResultValueTypes());
    _dataHeadersSelectManyUIInput.setValue(getSelectedDataHeaders().getValue());
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  
  // JSF event listener methods
  
  @SuppressWarnings("unchecked")
  public void dataHeadersSelectionListener(ValueChangeEvent event)
  {
    log.debug("data header selections changed to " + event.getNewValue());
    getSelectedDataHeaders().setValue((List<String>) event.getNewValue());
    
    // enforce minimum of 1 selected data header (data table query will break otherwise)
    if (getSelectedDataHeaders().getSelections().size() == 0) {
      getSelectedDataHeaders().setSelections(new ArrayList<ResultValueType>(getResultValueTypes()).subList(0,1));
      // this call shouldn't be necessary, as I would've expected UIInput component to query its model in render phase, but...
      _dataHeadersSelectManyUIInput.setValue(getSelectedDataHeaders().getValue());
    }

    // skip "update model" JSF phase, to avoid overwriting model values set above
    getFacesContext().renderResponse();
  }
  
  public void rowNumberListener(ValueChangeEvent event)
  {
    log.debug("row number changed to " + event.getNewValue());
    gotoDataTableRowIndex(Integer.parseInt(event.getNewValue().toString()) - 1);

    // skip "update model" JSF phase, to avoid overwriting model values set above
    getFacesContext().renderResponse();
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

  private List<String> getSelectedDataHeaderNames()
  {
    return getUniqueDataHeaderNames().get(getSelectedDataHeaders().getSelections());
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
        return _librariesController.viewWell(well, null);
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
    List<ResultValueType> selectedDataHeaders = getSelectedDataHeaders().getSelections();
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

  /**
   * Generates a standard name for dynamically-added table columns.
   */
  private String makeResultValueTypeColumnName(String name)
  {
    return name.replaceAll("[ ()]", "") + "Column";
  }
  
  private void resetView()
  {
    _dataHeadersColumnModel = null;
    _dataHeadersModel = null;
    _dataFilter = null;
    _dataTableRowsPerPage = null;
    _rawDataModel = null;
    _selectedDataHeaders = null;
    _uniqueDataHeaderNames = null;
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
    _rowNumberUIInput.setValue(rowIndex + 1);
    _dataTable.setFirst(rowIndex);
  }

  private int getPageIndex()
  {
    return getDataTable().getFirst() / getDataTableRowsPerPage().getSelection();
  }

  private void lazyBuildDataHeadersModel()
  {
    if (_dataHeadersModel == null) {
      List<DataHeaderRow> tableData = new ArrayList<DataHeaderRow>();
      for (DataHeaderRowDefinition dataHeaderAttribute : DATA_HEADER_ATTRIBUTES) {
        try {
          tableData.add(new DataHeaderRow(getResultValueTypes(),
                                          getUniqueDataHeaderNames(),
                                          dataHeaderAttribute));
        }
        catch (Exception e) {
          e.printStackTrace();
          log.error("could not obtain value for property ResultValueType." + 
                    dataHeaderAttribute.getPropertyName());
        }
      }
      _dataHeadersModel = new ListDataModel(tableData);
    }
  }


  // inner classes
  
  public static class DataHeaderRowDefinition
  {
    private String _propertyName;
    private String _displayName;
    private String _description;

    public DataHeaderRowDefinition(String propertyName, 
                                   String displayName,
                                   String description)
    {
      _propertyName = propertyName;
      // TODO: HACK: nbsp replacement; right place to do this?
      _displayName = displayName.replaceAll(" ", "&nbsp;");
      _description = description;
    }

    public String getDisplayName()
    {
      return _displayName;
    }

    public String getPropertyName()
    {
      return _propertyName;
    }
    
    public String getDescription()
    {
      return _description;
    }
    
    /**
     * Override to format value in a custom way.
     */
    public String formatValue(ResultValueType rvt)
    {
      try {
        return BeanUtils.getProperty(rvt, _propertyName);
      }
      catch (Exception e) {
        log.error(e.getMessage());
        return "<error>";
      }
    }
  }
  
  /**
   * DataHeaderRow bean, used to provide ScreenResult data headers to JSF components
   * @see ScreenResultViewer#getDataHeadersCellValue()
   * 
   * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
   * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
   */
  public static class DataHeaderRow
  {
    private DataHeaderRowDefinition _dataHeaderRowDefinition;
    /**
     * Array containing the value of the same property for each ResultValueType
     */
    private Map<String,Object> _rvtPropertyValues;    

    /**
     * Constructs a DataHeaderRow object.
     */
    public DataHeaderRow(Collection<ResultValueType> rvts, 
                         UniqueDataHeaderNames uniqueNames, 
                         DataHeaderRowDefinition dataHeaderRowDefinition)
    {
      _dataHeaderRowDefinition = dataHeaderRowDefinition;
      _rvtPropertyValues = new HashMap<String,Object>();
      for (ResultValueType rvt : rvts) {
        _rvtPropertyValues.put(uniqueNames.get(rvt), 
                               dataHeaderRowDefinition.formatValue(rvt));
      }
    }

    public String getRowLabel()
    {
      return _dataHeaderRowDefinition.getDisplayName();
    }
    
    public String getRowTitle()
    {
      return _dataHeaderRowDefinition.getDescription();
    }
    
    public Map getDataHeaderSinglePropertyValues()
    {
      return _rvtPropertyValues;
    }
  }

  public void update(Observable o, Object obj)
  {
    if (o == _selectedDataHeaders) {
      _sortManager.deleteObserver(this); // avoid recursive notifications
      try {
      if (log.isDebugEnabled()) {
        log.debug("data header selection changed: " + getSelectedDataHeaderNames());
      }
      updateDataHeadersColumnModel();
      updateSortManagerWithSelectedDataHeaders();
      updateDataTableContent();
      } 
      finally {
        _sortManager.addObserver(this);
      }
    }
    else if (obj instanceof SortChangedEvent) {
      SortChangedEvent event = (SortChangedEvent) obj;
      if (log.isDebugEnabled()) {
        log.debug(event.toString());
      }
      // TODO: full rebuild is only strictly needed by FullScreenResultDataModel, other ScreenResultDataModel classes could have a callback method called to avoid database calls (they would do their own in-memory sorting)
      updateDataTableContent();
    }
  }
}
