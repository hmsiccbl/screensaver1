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

import javax.faces.component.UIData;
import javax.faces.component.UIInput;
import javax.faces.component.UISelectBoolean;
import javax.faces.component.UISelectMany;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.db.ScreenResultsDAO;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultExporter;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.AssayWellType;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.UniqueDataHeaderNames;
import edu.harvard.med.screensaver.ui.control.LibrariesController;
import edu.harvard.med.screensaver.ui.control.ScreensController;
import edu.harvard.med.screensaver.ui.searchresults.SortDirection;
import edu.harvard.med.screensaver.ui.util.TableSortManager;
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
  private static final DataModel EMPTY_RAW_DATA_MODEL = new ListDataModel(new ArrayList<Map<String,String>>());
  private static final List<String> DATA_TABLE_FIXED_COLUMN_HEADERS = Arrays.asList("Plate", "Well", "Type");
  private static final DataHeaderRowDefinition[] DATA_HEADER_ATTRIBUTES = new DataHeaderRowDefinition[] {
    new DataHeaderRowDefinition("description", "Description", "A description of the data header"),
    new DataHeaderRowDefinition("replicateOrdinal", "Replicate Number", "Which replicate this data header refers to"),
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
    new DataHeaderRowDefinition("assayPhenotype", "Assay Phenotype", "The phenotype being tested for"),
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

  private static final int DATA_TABLE_FIXED_COLUMNS = 3;

  
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
  private UISelectManyBean<ResultValueType> _selectedResultValueTypes;
  private DataModel _dataHeadersColumnModel;
  private DataModel _dataHeadersModel;
  private TableSortManager _sortManager;

  // data members for raw data table 
  /**
   * Data model for the raw data, <i>containing only the set of rows being displayed in the current view</i>.
   */
  private DataModel _rawDataModel;
  private List<List<Boolean>> _excludedResultValues;
  /**
   * For internal tracking of first data row displayed in data table (the data
   * table's model rowIndex is always 0).
   */
  private int _firstResultValueIndex;
  private UIInput _rowNumberInput;
  private UIData _dataTable;
  private UISelectOneBean<ResultValueType> _hitsForDataHeader;
  private boolean _showHitsOnly;
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

  public int getRowNumber()
  {
    return _firstResultValueIndex + 1;
  }

  public void setRowNumber(int rowNumber)
  {
    _firstResultValueIndex = rowNumber - 1;
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
  public DataModel getRawData()
  {
    lazyBuildRawData();
    if (_rawDataModel == null) {
      return EMPTY_RAW_DATA_MODEL;
    }
    return _rawDataModel;
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
    return getSelectedResultValueTypes().getSelections().get(columnIndex).isNumeric();
  }
  
  public boolean isResultValueCellExcluded()
  {
    int columnIndex = getSortManager().getColumnModel().getRowIndex();
    if (columnIndex < DATA_TABLE_FIXED_COLUMNS) {
      return false;
    }
    int rowIndex = _rawDataModel.getRowIndex();
    return _excludedResultValues.get(rowIndex).get(columnIndex - DATA_TABLE_FIXED_COLUMNS);
  }
                                       
  
  /**
   * @motivation for rowNumber validator maximum
   */
  public int getRawDataSize()
  {
    try {
      if (isShowHitsOnly()) {
        return getHitsForDataHeader().getSelection().getHits();
      } 
      else {
        return _screenResultSize;
      }
    }
    catch (Exception e) {
      return 0;
    }
  }

  public String getRowRangeText()
  {
    return getRowNumber() + 
           ".." + 
           Math.min(getRowNumber() + _dataTable.getRows() - 1, 
                    getRawDataSize()) + 
           " of " + 
           getRawDataSize();
  }
  
  public UISelectManyBean<ResultValueType> getSelectedResultValueTypes()
  {
    if (_selectedResultValueTypes == null) {
      _selectedResultValueTypes = new UISelectManyBean<ResultValueType>(getScreenResult().getResultValueTypes())
      {
        @Override
        protected String getLabel(ResultValueType rvt)
        {
          return getUniqueDataHeaderNames().get(rvt);
        }
      };
      selectAllResultValueTypes();
    }
    return _selectedResultValueTypes;
  }

  public DataModel getDataHeadersColumnModel()
  {
    if (_dataHeadersColumnModel == null) {
      _dataHeadersColumnModel = new ListDataModel(getSelectedDataHeaderNames());
    }
    return _dataHeadersColumnModel;
  }

  private List<String> getSelectedDataHeaderNames()
  {
    return getUniqueDataHeaderNames().get(getSelectedResultValueTypes().getSelections());
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
          // we cannot efficiently determine the new row index, so we set back to 0 on a sort
          _firstResultValueIndex = 0;
          rebuildDataTable();
        }
      };
    }
    return _sortManager;
  }

  public boolean isShowHitsOnly()
  {
    if (getHitsForDataHeader().getSelectItems().size() == 0) {
      _showHitsOnly = false;
    }
    return _showHitsOnly;
  }

  public void setShowHitsOnly(boolean showHitsOnly)
  {
    if (getHitsForDataHeader().getSelectItems().size() == 0) {
      // can't show hits only, if no assay indicator data headers are visible
      _showHitsOnly = false;
    }
    else {
      _showHitsOnly = showHitsOnly;
    }
  }

  public UISelectOneBean<ResultValueType> getHitsForDataHeader()
  {
    if (_hitsForDataHeader == null) {
      updateHitsForDataHeaderSelections();
    }
    return _hitsForDataHeader;
  }


  // JSF application methods
  
  public String gotoPage(int pageIndex)
  {
    try {
      int tmpFirstResultValueIndex = (pageIndex * getDataTable().getRows());
      if (tmpFirstResultValueIndex >= 0 &&
        tmpFirstResultValueIndex < getScreenResult().getResultValueTypes().first().getResultValues().size()) {
        // update the plate selection list to the current plate
        if (getRawData() != EMPTY_RAW_DATA_MODEL) {
          _firstResultValueIndex = tmpFirstResultValueIndex;
        }
      }
      rebuildDataTable();
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
  
  public String nextPlate()
  {
    int rowsPerPage = getDataTable().getRows();
    assert (Well.PLATE_ROWS * Well.PLATE_COLUMNS) % rowsPerPage == 0 : "expected rows per page to divide evenly into plate well size";
    return gotoPage(getPageIndex() + (Well.PLATE_ROWS * Well.PLATE_COLUMNS) / rowsPerPage); 
  }
  
  public String prevPlate()
  {
    int rowsPerPage = getDataTable().getRows();
    assert (Well.PLATE_ROWS * Well.PLATE_COLUMNS) % rowsPerPage == 0 : "expected rows per page to divide evenly into plate well size";
    return gotoPage(getPageIndex() - (Well.PLATE_ROWS * Well.PLATE_COLUMNS) / rowsPerPage); 
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
    
    Integer plateNumber = Integer.valueOf((String) ((Map) _rawDataModel.getRowData()).get(DATA_TABLE_FIXED_COLUMN_HEADERS.get(0)));
    String wellName = (String) ((Map) _rawDataModel.getRowData()).get(DATA_TABLE_FIXED_COLUMN_HEADERS.get(1));
    Well well = _librariesDao.findWell(new WellKey(plateNumber, wellName));
    return _librariesController.viewWell(well, null);
  }
  
  public String saveScreenResult()
  {
    // note: saving the parent screen will save its screenResult
    return _screensController.saveScreen(_screenResult.getScreen(), null);
  }
  
  public String updateDataHeaders()
  {
    // clear state of our data headers model, forcing lazy initialization when needed
    _dataHeadersColumnModel = null;
    updateHitsForDataHeaderSelections();
    updateDataTableRows();
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public String updateDataTableRows()
  {
    setFirstResultValueIndex(0);
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  public String showAllDataHeaders()
  {
    selectAllResultValueTypes();
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  
  // JSF event listener methods
  
  public void showTableOptionListener(ValueChangeEvent event)
  {
    log.debug("refreshing page in response to value change event from " + event.getComponent().getId());
    ((UISelectBoolean) event.getComponent()).setValue(event.getNewValue());
  }

  public void rowNumberListener(ValueChangeEvent event)
  {
    log.debug("rowNumberListener called: " + event.getNewValue());
    setFirstResultValueIndex(Integer.parseInt(event.getNewValue().toString()) - 1);
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
    _rawDataModel = null;
    _excludedResultValues = null;
    _firstResultValueIndex = 0;
    _selectedResultValueTypes = null;
    _uniqueDataHeaderNames = null;
    _sortManager = null;
    _showHitsOnly = false;
    _hitsForDataHeader = null;
  }

  private void rebuildDataTable()
  {
    // clear state of our data table, forcing lazy initialization when needed
    _rawDataModel = null;
  }

  private void updateSortManagerWithSelectedDataHeaders()
  {
    List<String> columnHeaders = new ArrayList<String>(DATA_TABLE_FIXED_COLUMN_HEADERS);
    columnHeaders.addAll(getSelectedDataHeaderNames());
    getSortManager().setColumnNames(columnHeaders);
  }

  private void updateHitsForDataHeaderSelections()
  {
    List<ResultValueType> resultValueTypes = new ArrayList<ResultValueType>();
    resultValueTypes.addAll(getScreenResult().getResultValueTypes());
    for (Iterator iter = resultValueTypes.iterator(); iter.hasNext();) {
      ResultValueType rvt = (ResultValueType) iter.next();
      if (!rvt.isActivityIndicator() || 
        !getSelectedResultValueTypes().getSelections().contains(rvt)) {
        iter.remove();
      }
    }
    _hitsForDataHeader = new UISelectOneBean<ResultValueType>(resultValueTypes) {
      @Override
      protected String getLabel(ResultValueType t)
      {
        return t.getName();
      }
    };
  }

  private void setFirstResultValueIndex(int firstResultValueIndex)
  {
    _firstResultValueIndex = firstResultValueIndex;
    // ensure value is within valid range, and in particular that we never show
    // less than the table's configured row count (unless it's more than the
    // total number of rows)
    _firstResultValueIndex = Math.max(0,
                                      Math.min(_firstResultValueIndex,
                                               getRawDataSize() - _dataTable.getRows()));
    _rowNumberInput.setValue(_firstResultValueIndex + 1);
    rebuildDataTable();
  }

  private int getPageIndex()
  {
    return _firstResultValueIndex / getDataTable().getRows();
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
  public void resultValueTypesChangeListener(ValueChangeEvent event)
  {
    _selectedResultValueTypes.setValue((List<String>) event.getNewValue());
    // enforce minimum of 1 selected data header (data table query will break otherwise)
    if (getSelectedDataHeaderNames().size() == 0) {
      _selectedResultValueTypes.setSelections(Arrays.asList(getScreenResult().getResultValueTypes().first()));
    }

    // next line sets the local value of the dataHeaders JSF component, and
    // prevents its old value from being used during the Update Model JSF phase,
    // when updating our UISelectManyBean
    _dataHeadersSelectMany.setValue(_selectedResultValueTypes.getValue());
    
    updateSortManagerWithSelectedDataHeaders();
  }
  
  @SuppressWarnings("unchecked")
  private void lazyBuildRawData()
  {
    if (getScreenResult() != null && _rawDataModel == null) {
      int sortByArg;
      switch (getSortManager().getCurrentSortColumnIndex())
      {
      case 0: sortByArg = ScreenResultsDAO.SORT_BY_PLATE_WELL; break;
      case 1: sortByArg = ScreenResultsDAO.SORT_BY_WELL_PLATE; break;
      case 2: sortByArg = ScreenResultsDAO.SORT_BY_ASSAY_WELL_TYPE; break;
      default:
          sortByArg = getSortManager().getCurrentSortColumnIndex() - DATA_TABLE_FIXED_COLUMNS;
      }
      Map<WellKey,List<ResultValue>> rvData = 
        _screenResultsDao.findSortedResultValueTableByRange(_selectedResultValueTypes.getSelections(),
                                                            sortByArg,
                                                            getSortManager().getCurrentSortDirection(),
                                                            _firstResultValueIndex,
                                                            getDataTable().getRows(),
                                                            isShowHitsOnly() ? getHitsForDataHeader().getSelection() : null);
      
      List<Map<String,String>> tableData = new ArrayList<Map<String,String>>();
      _excludedResultValues = new ArrayList<List<Boolean>>();
      for (Map.Entry<WellKey,List<ResultValue>> entry : rvData.entrySet()) {
        WellKey wellKey = entry.getKey();
        tableData.add(buildRow(wellKey,
                               entry.getValue().get(0).getAssayWellType(),
                               entry.getValue(),
                               _selectedResultValueTypes.getSelections()));
      }
      _rawDataModel = new ListDataModel(tableData);
    }
  }
  
  /**  
   * @sideeffect adds element to {@link #_excludedResultValues}
   */
  private Map<String,String> buildRow(WellKey wellKey,
                                      AssayWellType assayWellType,
                                      List<ResultValue> resultValues, 
                                      List<ResultValueType> resultValueTypes)
  {
    List<String> columnNames = getSortManager().getColumnNames();
    int i = 0;
    HashMap<String,String> cellValues = new HashMap<String,String>();
    cellValues.put(columnNames.get(i++), Integer.toString(wellKey.getPlateNumber()));
    cellValues.put(columnNames.get(i++), wellKey.getWellName());
    cellValues.put(columnNames.get(i++), assayWellType.toString());
    List<Boolean> excludedResultValuesRow = new ArrayList<Boolean>();
    Iterator<ResultValueType> rvtIter = resultValueTypes.iterator();
    for (ResultValue rv : resultValues) {
      ResultValueType rvt = rvtIter.next();
      excludedResultValuesRow.add(rv.isExclude());
      Object typedValue = ResultValue.getTypedValue(rv, rvt);
      cellValues.put(columnNames.get(i++),
                     typedValue == null ? null : typedValue.toString());
    }
    _excludedResultValues.add(excludedResultValuesRow);
    return cellValues;
  }
    
  @SuppressWarnings("unchecked")
  private void selectAllResultValueTypes()
  {
    getSelectedResultValueTypes().setSelections(getScreenResult().getResultValueTypes());
    updateDataHeaders();
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
