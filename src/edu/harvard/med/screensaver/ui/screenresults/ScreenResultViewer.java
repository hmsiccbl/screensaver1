// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/ui/screenresults/ScreenResultViewer.java $
// $Id: ScreenResultViewer.java 706 2006-10-31 17:33:20Z ant4 $
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
import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultExporter;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.UniqueDataHeaderNames;
import edu.harvard.med.screensaver.ui.control.LibrariesController;
import edu.harvard.med.screensaver.ui.control.ScreensController;
import edu.harvard.med.screensaver.ui.table.TableSortManager;
import edu.harvard.med.screensaver.ui.util.UISelectManyBean;
import edu.harvard.med.screensaver.ui.util.UISelectOneBean;

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
public class ScreenResultViewer extends AbstractBackingBean
{

  // static data members
  
  private static Logger log = Logger.getLogger(ScreenResultViewer.class);
  
  private static final DataModel EMPTY_DATAHEADERS_MODEL = new ListDataModel(new ArrayList<DataHeaderRow>());
  private static final ScreenResultDataModel EMPTY_RAW_DATA_MODEL = new EmptyScreenResultDataModel();
  public static final List<String> DATA_TABLE_FIXED_COLUMN_HEADERS = Arrays.asList("Plate", "Well", "Type");
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
    new DataHeaderRowDefinition("activityIndicator", "Positive Indicator", "True if this data header is used to indicate hits"),
    new DataHeaderRowDefinition("activityIndicatorType", "Positive Indicator Type", "'Numerical', 'Boolean', or 'Partition'"),
    new DataHeaderRowDefinition("indicatorDirection", "Indicator Direction", "For numerical indicators, whether high or low values are hits"),
    new DataHeaderRowDefinition("indicatorCutoff", "Indicator Cutoff", "The numerical score demarking hits from non-hits"),
    new DataHeaderRowDefinition("followUpData", "Follow Up Data", "Primary or follow up screen data"),
    new DataHeaderRowDefinition("assayPhenotype", "Assay Phenotype", "The phenotype being monitored"),
    new DataHeaderRowDefinition("comments", "Comments", "Data header comments"),
    new DataHeaderRowDefinition("hits", "Hits", "The number of hits, if this is a Positive Indicator"),
    new DataHeaderRowDefinition("hitRatio", "Hit %", "The percent of experimental wells in the results that were hits")
    {
      @Override
      public String formatValue(ResultValueType rvt)
      {
        if (rvt.getHitRatio() == null) {
          return "";
        }
        return NumberFormat.getPercentInstance().format(rvt.getHitRatio());
      }
    }
  };

  static final int DATA_TABLE_FIXED_COLUMNS = 3;

  public static final Integer DATA_TABLE_FILTER_SHOW_ALL = -2;
  public static final Integer DATA_TABLE_FILTER_SHOW_HITS = -1;

  
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
  private UISelectMany _dataHeadersSelectMany;
  private UISelectManyBean<ResultValueType> _selectedDataHeaders;
  private DataModel _dataHeadersColumnModel;
  private DataModel _dataHeadersModel;
  private TableSortManager _sortManager;

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
  private UIInput _rowNumberInput;
  private UIInput _dataTableRowsPerPageUIInput;
  private UISelectOneBean<Integer> _dataFilter;
  private DataTableRowsPerPageUISelectOneBean _dataTableRowsPerPage;
  private UISelectOneBean<ResultValueType> _showHitsOnlyForDataHeader;
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
  
  public UISelectMany getDataHeadersSelectMany()
  {
    return _dataHeadersSelectMany;
  }

  public void setDataHeadersSelectMany(UISelectMany dataHeadersSelectMany)
  {
    _dataHeadersSelectMany = dataHeadersSelectMany;
  }

  public UIInput getRowNumberInput()
  {
    return _rowNumberInput;
  }

  public void setRowNumberInput(UIInput rowNumberInput)
  {
    _rowNumberInput = rowNumberInput;
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

  /**
   * @return a List of {@link RawDataRow} objects
   */
  public ScreenResultDataModel getDataTableModel()
  {
    if (_rawDataModel == null) {
      updateDataTableContent();
    }
    return _rawDataModel;
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
    int columnIndex = getSortManager().getColumnModel().getRowIndex();
    // "plate", "well", and "type" columns are non-numeric
    if (columnIndex < DATA_TABLE_FIXED_COLUMNS) {
      return false;
    }
    // columns based upon ResultValueTypes can be queried directly for numericalness
    columnIndex -= DATA_TABLE_FIXED_COLUMNS;
    return getSelectedDataHeaders().getSelections().get(columnIndex).isNumeric();
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
      _selectedDataHeaders = new UISelectManyBean<ResultValueType>(getScreenResult().getResultValueTypes())
      {
        @Override
        protected String getLabel(ResultValueType rvt)
        {
          return getUniqueDataHeaderNames().get(rvt);
        }
      };
      selectAllDataHeaders();
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

  private List<String> getSelectedDataHeaderNames()
  {
    return getUniqueDataHeaderNames().get(getSelectedDataHeaders().getSelections());
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
   * @return
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
  
  public TableSortManager getSortManager()
  {
    if (_sortManager == null) {
      List<String> columnHeaders = new ArrayList<String>(DATA_TABLE_FIXED_COLUMN_HEADERS);
      columnHeaders.addAll(getUniqueDataHeaderNames().asList());
      _sortManager = new TableSortManager(columnHeaders) {
        @Override
        protected void sortChanged(String sortColumnName, SortDirection sortDirection)
        {
          // TODO: full rebuild is only strictly needed by FullScreenResultDataModel, other ScreenResultDataModel classes could have a sortChanged() method called to avoid database calls (they would do their own in-memory sorting)
          updateDataTableContent();
        }
      };
    }
    return _sortManager;
  }

  public UISelectOneBean<ResultValueType> getShowHitsOnlyForDataHeader()
  {
    if (_showHitsOnlyForDataHeader == null) {
      updateDataHeaderSelectionsForShowHits();
    }
    return _showHitsOnlyForDataHeader;
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
                                                    getSortManager(),
                                                    getSelectedDataHeaders().getSelections(),
                                                    _screenResultsDao,
                                                    getDataTableRowsPerPage().getSelection(),
                                                    _screenResultSize);
    }
    else if (getDataFilter().getSelection() == DATA_TABLE_FILTER_SHOW_HITS) {
      _rawDataModel = new HitsOnlyScreenResultDataModel(_screenResult,
                                                        getSortManager(),
                                                        getSelectedDataHeaders().getSelections(),
                                                        _screenResultsDao,
                                                        getShowHitsOnlyForDataHeader().getSelection());
    }
    else if (getDataFilter().getSelection() >= 0) { // plate number
      _rawDataModel = new SinglePlateScreenResultDataModel(_screenResult,
                                                           getSortManager(),
                                                           getSelectedDataHeaders().getSelections(),
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
    List<String> columnHeaders = new ArrayList<String>(DATA_TABLE_FIXED_COLUMN_HEADERS);
    columnHeaders.addAll(getSelectedDataHeaderNames());
    getSortManager().setColumnNames(columnHeaders);
  }

  private void updateDataHeaderSelectionsForShowHits()
  {
    log.debug("updating data header selections for show hits");
    List<ResultValueType> resultValueTypes = new ArrayList<ResultValueType>();
    resultValueTypes.addAll(getScreenResult().getResultValueTypes());
    for (Iterator iter = resultValueTypes.iterator(); iter.hasNext();) {
      ResultValueType rvt = (ResultValueType) iter.next();
      if (!rvt.isActivityIndicator()) {
        iter.remove();
      }
    }
    _showHitsOnlyForDataHeader = new UISelectOneBean<ResultValueType>(resultValueTypes) {
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
      return;
    }

    log.debug("updating data table filter selections");

    SortedSet<Integer> filters = new TreeSet<Integer>(_screenResult.getPlateNumbers());
    filters.add(DATA_TABLE_FILTER_SHOW_ALL);
    if (getShowHitsOnlyForDataHeader().getSize() > 0) {
      filters.add(DATA_TABLE_FILTER_SHOW_HITS);
    }
    _dataFilter = 
      new UISelectOneBean<Integer>(filters, DATA_TABLE_FILTER_SHOW_ALL) {
      @Override
      protected String getLabel(Integer val)
      {
        if (val == DATA_TABLE_FILTER_SHOW_ALL) {
          return "All";
        }
        if (val == DATA_TABLE_FILTER_SHOW_HITS) {
          if (getShowHitsOnlyForDataHeader().getSize() == 1) {
            return "Hits (" + getShowHitsOnlyForDataHeader().getSelection().getUniqueName() + ")";
          }
          return "Hits";
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
    else if (getDataFilter().getSelection() == DATA_TABLE_FILTER_SHOW_HITS) {
      _dataTableRowsPerPage = new DataTableRowsPerPageUISelectOneBean(Arrays.asList(10, 20, 50, 100, -1));
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
  
  public String viewWell()
  { 
    assert DATA_TABLE_FIXED_COLUMN_HEADERS.get(0).equals("Plate");
    assert DATA_TABLE_FIXED_COLUMN_HEADERS.get(1).equals("Well");
    
    Integer plateNumber = Integer.valueOf((String) ((Map) getDataTableModel().getRowData()).get(DATA_TABLE_FIXED_COLUMN_HEADERS.get(0)));
    String wellName = (String) ((Map) getDataTableModel().getRowData()).get(DATA_TABLE_FIXED_COLUMN_HEADERS.get(1));
    Well well = _librariesDao.findWell(new WellKey(plateNumber, wellName));
    return _librariesController.viewWell(well, null);
  }
  
  public String saveScreenResult()
  {
    // note: saving the parent screen will save its screenResult
    return _screensController.saveScreen(_screenResult.getScreen(), null);
  }
  
  public String showAllDataHeaders()
  {
    selectAllDataHeaders();
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
      getSelectedDataHeaders().setSelections(getScreenResult().getResultValueTypesList().subList(0,1));
      // this call shouldn't be necessary, as I would've expected UIInput component to query its model in render phase, but...
      _dataHeadersSelectMany.setValue(getSelectedDataHeaders().getValue());
    }
    updateDataHeadersColumnModel();
    updateSortManagerWithSelectedDataHeaders();

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
   * @return
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
   * @return
   */
  public void showHitsForDataHeaderListener(ValueChangeEvent event)
  {
    log.debug("showHitsForDataHeader changed to " + event.getNewValue());
    getShowHitsOnlyForDataHeader().setValue((String) event.getNewValue());
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
    _showHitsOnlyForDataHeader = null;
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
    _rowNumberInput.setValue(rowIndex + 1);
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
          tableData.add(new DataHeaderRow(getScreenResult().getResultValueTypes(),
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

  @SuppressWarnings("unchecked")
  private void selectAllDataHeaders()
  {
    getSelectedDataHeaders().setSelections(getScreenResult().getResultValueTypes());
    _dataHeadersSelectMany.setValue(getSelectedDataHeaders().getValue());
    updateDataHeadersColumnModel();
    updateSortManagerWithSelectedDataHeaders();
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
     * @param rvt
     * @return
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
   * @author ant
   */
  public static class DataHeaderRow
  {
    private DataHeaderRowDefinition _dataHeaderRowDefinition;
    /**
     * Array containing the value of the same property for each ResultValueType
     */
    private Map<String,String> _rvtPropertyValues;    

    /**
     * Constructs a DataHeaderRow object.
     * 
     * @param rvts The {@link ResultValueType}s that contain the data for this
     *          row
     * @param uniqueNames
     * @param property a bean property of the {@link ResultValueType}, which
     *          defines the type of data header to be displayed by this row
     * @throws Exception if the specified property cannot be determined for a
     *           ResultValueType
     */
    public DataHeaderRow(Collection<ResultValueType> rvts, 
                         UniqueDataHeaderNames uniqueNames, 
                         DataHeaderRowDefinition dataHeaderRowDefinition)

      throws Exception
    {
      _dataHeaderRowDefinition = dataHeaderRowDefinition;
      _rvtPropertyValues = new HashMap<String,String>();
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
}
