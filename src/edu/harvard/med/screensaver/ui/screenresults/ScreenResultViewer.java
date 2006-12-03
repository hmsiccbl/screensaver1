// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/ui/screenresults/ScreenResultViewer.java $
// $Id: ScreenResultViewer.java 706 2006-10-31 17:33:20Z ant4 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIData;
import javax.faces.component.UIInput;
import javax.faces.component.UISelectBoolean;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultExporter;
import edu.harvard.med.screensaver.io.workbook.Workbook;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.AssayWellType;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.UniqueDataHeaderNames;
import edu.harvard.med.screensaver.ui.control.LibrariesController;
import edu.harvard.med.screensaver.ui.control.ScreenResultsController;
import edu.harvard.med.screensaver.ui.control.ScreensController;
import edu.harvard.med.screensaver.ui.searchresults.ScreenSearchResults;
import edu.harvard.med.screensaver.ui.searchresults.SortDirection;
import edu.harvard.med.screensaver.ui.util.JSFUtils;
import edu.harvard.med.screensaver.ui.util.TableSortManager;
import edu.harvard.med.screensaver.ui.util.UISelectManyBean;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;


/**
 * JSF backing bean for Screen Result Viewer web page (screenresultviewer.jsp).
 * <p>
 * The <code>screenResult</code> property should be set to the
 * {@link ScreenResult} that is to be viewed.<br>

 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
// TODO: this class needs to be broken up! it's too big! (maybe)
public class ScreenResultViewer extends AbstractBackingBean
{

  // static data members
  
  private static Logger log = Logger.getLogger(ScreenResultViewer.class);
  
  private static final DataModel EMPTY_METADATA_MODEL = new ListDataModel(new ArrayList<MetadataRow>());
  private static final DataModel EMPTY_RAW_DATA_MODEL = new ListDataModel(new ArrayList<Map<String,String>>());
  private static final List<String> DATA_TABLE_FIXED_COLUMN_HEADERS = Arrays.asList("Plate", "Well", "Type", "Excluded");

  
  // instance data members

  private ScreenResultsController _screenResultsController;
  private ScreensController _screensController;
  private LibrariesController _librariesController;
  private DAO _dao;
  private Screen _screen;
  private ScreenSearchResults _screenSearchResults;
  private ScreenResultExporter _screenResultExporter;
  /**
   * For internal tracking of first data row displayed in data table.
   */
  private int _firstResultValueIndex;
  private UISelectManyBean<ResultValueType> _selectedResultValueTypes;
  private String _rowRangeText;
  private UIInput _rowNumberInput;
  private UIData _dataTable;
  private UniqueDataHeaderNames _uniqueDataHeaderNames;
  /**
   * Data model for the raw data, <i>containing only the set of rows being displayed in the current view</i>.
   */
  private DataModel _rawDataModel;
  private DataModel _rawDataColumnModel;
  private DataModel _dataHeadersColumnModel;
  private DataModel _metadataModel;
  private Map<String,Boolean> _collapsablePanelsState;
  private TableSortManager _sortManager;


  // public methods
  
  public ScreenResultViewer()
  {
    _collapsablePanelsState = new HashMap<String,Boolean>();
    _collapsablePanelsState.put("summary", false);
    _collapsablePanelsState.put("dataHeadersTable", true);
    _collapsablePanelsState.put("dataTable", true);
    _collapsablePanelsState.put("heatMaps", true);
  }

  public void setDao(DAO dao)
  {
    _dao = dao;
  }

  public void setScreenResultsController(ScreenResultsController screenResultsController)
  {
    _screenResultsController = screenResultsController;
  }

  public void setScreensController(ScreensController screensController) {
    _screensController = screensController;
  }
  
  public void setLibrariesController(LibrariesController librariesController) 
  {
    _librariesController = librariesController;
  }

  public void setScreenSearchResults(ScreenSearchResults screenSearchResults) {
    _screenSearchResults = screenSearchResults;
  }

  public void setScreen(Screen screen) 
  {
    _screen = screen;
    resetView();
  }
  
  public Screen getScreen()
  {
    return _screen;
  }
  

  public ScreenResult getScreenResult()
  {
    // TODO: HACK: data-access-permissions aware 
    if (_screen.getScreenResult() != null) {
      return _dao.findEntityById(ScreenResult.class, _screen.getScreenResult().getEntityId());
    }
    return null;
  }

  public void setScreenResultExporter(ScreenResultExporter screenResultExporter)
  {
    _screenResultExporter = screenResultExporter;
  }

  public Map getCollapsablePanelsState()
  {
    return _collapsablePanelsState;
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
  
  public DataModel getMetadata()
  {
    lazyBuildMetadataModel();
    if (_metadataModel == null) {
      return EMPTY_METADATA_MODEL;
    }
    return _metadataModel;
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
  
  /**
   * @motivation for rowNumber validator maximum
   */
  public int getRawDataSize()
  {
    try {
      return getScreenResult().getResultValueTypes().first().getResultValues().size();
    }
    catch (Exception e) {
      return 0;
    }
  }

  public String getRowRangeText()
  {
    return getRowNumber() + ".." + (getRowNumber() + _dataTable.getRows() - 1) + " of " + getRawDataSize();
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
  public Object getMetadataCellValue()
  {
    DataModel dataModel = getMetadata();
    DataModel columnModel = getDataHeadersColumnModel();
    if (columnModel.isRowAvailable()) {
      String columnName = (String) columnModel.getRowData();  // getRowData() is really getColumnData()
      MetadataRow row = (MetadataRow) dataModel.getRowData();
      return row.getDataHeaderSinglePropertyValues().get(columnName);
    }
    return null;
  }


  // JSF application methods
  
  public TableSortManager getSortManager()
  {
    if (_sortManager == null) {
      List<String> columnHeaders = new ArrayList<String>(DATA_TABLE_FIXED_COLUMN_HEADERS);
      columnHeaders.addAll(getUniqueDataHeaderNames().asList());
      _sortManager = new TableSortManager(columnHeaders) {
        @Override
        protected void doSort(String sortColumnName, SortDirection sortDirection)
        {
          // we cannot efficiently determine the new row index, so we set back to 0 on a sort
          _firstResultValueIndex = 0;
          rebuildDataTable();
        }
      };
    }
    return _sortManager;
  }

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
      return gotoPage(Math.max(0, getRawDataSize()) / getDataTable().getRows());
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
  
  public String viewScreen()
  {
    return _screensController.viewScreen(_screen, _screenSearchResults);
  }
  
  public String download()
  {
    File exportedWorkbookFile = null;
    FileOutputStream out = null;
    try {
      HSSFWorkbook workbook = _screenResultExporter.build(getScreenResult());
      exportedWorkbookFile = File.createTempFile("screenResult" + _screen.getScreenNumber() + ".",
                                                 ".xls");
      out = new FileOutputStream(exportedWorkbookFile);
      workbook.write(out);
      out.close();
      JSFUtils.handleUserFileDownloadRequest(getFacesContext(),
                                             exportedWorkbookFile,
                                             Workbook.MIME_TYPE);
    }
    catch (IOException e)
    {
      showMessage("systemError");
      log.error(e.getMessage());
    }
    finally {
      IOUtils.closeQuietly(out);
      if (exportedWorkbookFile != null && exportedWorkbookFile.exists()) {
        exportedWorkbookFile.delete();
      }
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public String delete()
  {
    _dao.deleteScreenResult(getScreenResult());
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public String showWell()
  { 
    Integer wellId = (Integer) getFacesContext().getExternalContext().getRequestParameterMap().get("wellIdParam");
    Well well = _dao.findEntityById(Well.class, wellId);
    return _librariesController.viewWell(well, null);
  }
  
  public String update()
  {
    log.debug("update action received");
    rebuildDataTable();
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
    _firstResultValueIndex = Integer.parseInt(event.getNewValue().toString()) - 1;
    // ensure value is within valid range, and in particular that we never show
    // less than the table's configured row count (unless it's more than the
    // total number of rows)
    _firstResultValueIndex = Math.max(0,
                                      Math.min(_firstResultValueIndex,
                                               getRawDataSize() - _dataTable.getRows()));
    _rowNumberInput.setValue(_firstResultValueIndex + 1);
    rebuildDataTable();
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
    _metadataModel = null;
    _rawDataModel = null;
    _firstResultValueIndex = 0;
    _selectedResultValueTypes = null;
    _uniqueDataHeaderNames = null;
    _sortManager = null;

    // clear the bound UI components, so that they get recreated next time this view is used
    _dataTable = null;
    _rowNumberInput = null;

    // _rowToPlateConverter = null;    
  }

  private void rebuildDataTable()
  {
    // clear state of our data table, forcing lazy initialization when needed
    _dataHeadersColumnModel = null;
    _rawDataModel = null;

    // enforce minimum of 1 selected data header (data table query will break otherwise)
    if (getSelectedDataHeaderNames().size() == 0) {
      _selectedResultValueTypes.setSelections(Arrays.asList(getScreenResult().getResultValueTypes().first()));
    }

    updateSortManagerWithSelectedDataHeaders();
  }

  private void updateSortManagerWithSelectedDataHeaders()
  {
    List<String> columnHeaders = new ArrayList<String>(DATA_TABLE_FIXED_COLUMN_HEADERS);
    columnHeaders.addAll(getSelectedDataHeaderNames());
    getSortManager().setColumnNames(columnHeaders);
  }

  private int getPageIndex()
  {
    return _firstResultValueIndex / getDataTable().getRows();
  }

  private void lazyBuildMetadataModel()
  {
    if (_metadataModel == null) {
      String[] properties = null;
      try {
        properties = new String[] {
          "name",
          "description",
          "ordinal",
          "replicateOrdinal",
          "assayReadoutType",
          "timePoint",
          "derived",
          "howDerived",
          "typesDerivedFrom",
          "activityIndicator",
          "activityIndicatorType",
          "indicatorDirection",
          "indicatorCutoff",
          "followUpData",
          "assayPhenotype",
          "cherryPick",
          "comments"
        };
      } catch (Exception e) {
        e.printStackTrace();
        log.error("property not valid for ResultValueType");
      }
      
      List<MetadataRow> tableData = new ArrayList<MetadataRow>();
      for (String property : properties) {
        try {
          tableData.add(new MetadataRow(getScreenResult().getResultValueTypes(),
                                        getUniqueDataHeaderNames(),
                                        property));
        }
        catch (Exception e) {
          e.printStackTrace();
          log.error("could not obtain value for property ResultValueType." + property);
        }
      }
      _metadataModel = new ListDataModel(tableData);
    }
  }

  @SuppressWarnings("unchecked")
  private void lazyBuildRawData()
  {
    if (getScreenResult() != null && _rawDataModel == null) {
      int sortByArg;
      switch (getSortManager().getCurrentSortColumnIndex())
      {
      case 0: sortByArg = DAO.SORT_BY_PLATE_WELL; break;
      case 1: sortByArg = DAO.SORT_BY_WELL_PLATE; break;
      case 2: sortByArg = DAO.SORT_BY_ASSAY_WELL_TYPE; break;
      case 3: sortByArg = DAO.SORT_BY_PLATE_WELL; break; // error!
      default:
          sortByArg = getSortManager().getCurrentSortColumnIndex() - 4;
      }
      Map<WellKey,List<ResultValue>> rvData = 
        _dao.findSortedResultValueTableByRange(_selectedResultValueTypes.getSelections(),
                                               sortByArg,
                                               getSortManager().getCurrentSortDirection(),
                                               _firstResultValueIndex,
                                               getDataTable().getRows());
      
      List<Map<String,String>> tableData = new ArrayList<Map<String,String>>();
      for (Map.Entry<WellKey,List<ResultValue>> entry : rvData.entrySet()) {
        WellKey wellKey = entry.getKey();
        tableData.add(buildRow(wellKey,
                               entry.getValue().get(0).getAssayWellType(),
                               entry.getValue()));
      }
      _rawDataModel = new ListDataModel(tableData);
    }
  }
  
  
  private Map<String,String> buildRow(WellKey wellKey,
                                      AssayWellType assayWellType,
                                      List<ResultValue> resultValues)
  {
    List<String> columnNames = getSortManager().getColumnNames();
    StringBuilder excludes = new StringBuilder();
    for (int i = 0; i < resultValues.size(); ++i) {
      ResultValue rv = resultValues.get(i);
      if (rv != null && rv.isExclude()) {
        if (excludes.length() > 0) {
          excludes.append(", ");
        }
        excludes.append(columnNames.get(i));
      }
    }
    int i = 0;
    HashMap<String,String> cellValues = new HashMap<String,String>();
    cellValues.put(columnNames.get(i++), Integer.toString(wellKey.getPlateNumber()));
    cellValues.put(columnNames.get(i++), wellKey.getWellName());
    cellValues.put(columnNames.get(i++), assayWellType.toString());
    cellValues.put(columnNames.get(i++), excludes.toString());
    for (ResultValue rv : resultValues) {
      cellValues.put(columnNames.get(i++), rv.getValue());
    }
    return cellValues;
  }
    
  @SuppressWarnings("unchecked")
  private void selectAllResultValueTypes()
  {
    getSelectedResultValueTypes().setSelections(getScreenResult().getResultValueTypes());
    rebuildDataTable();
  }


  // inner classes
  
  /**
   * MetadataRow bean, used to provide ScreenResult metadata to JSF components
   * @see ScreenResultViewer#getMetadataCellValue()
   * @author ant
   */
  public static class MetadataRow
  {
    private String _rowLabel;
    /**
     * Array containing the value of the same property for each ResultValueType
     */
    private Map<String,String> _rvtPropertyValues;    

    /**
     * Constructs a MetadataRow object.
     * @param rvts The {@link ResultValueType}s that contain the data for this row
     * @param uniqueNames 
     * @param property a bean property of the {@link ResultValueType}, which defines the type of metadata to be displayed by this row
     * @throws Exception if the specified property cannot be determined for a ResultValueType
     */
    public MetadataRow(Collection<ResultValueType> rvts, UniqueDataHeaderNames uniqueNames, String propertyName) throws Exception
    {
      _rowLabel = edu.harvard.med.screensaver.util.BeanUtils.formatPropertyName(propertyName);
      _rvtPropertyValues = new HashMap<String,String>();
      for (ResultValueType rvt : rvts) {
        String value = BeanUtils.getProperty(rvt, propertyName);
        // HACK: handle special cases, where we must format the property value of the ResultValueType
        // TODO: how can we do this properly/generally?
        if (propertyName.equals("name")) {
          value = uniqueNames.get(rvt);
        }
        else if (propertyName.equals("typesDerivedFrom")) {
          StringBuilder typesDerivedFromText = new StringBuilder();
          for (ResultValueType derivedFromRvt : rvt.getTypesDerivedFrom()) {
            if (typesDerivedFromText.length() > 0) {
              typesDerivedFromText.append(", ");
            }
            typesDerivedFromText.append(uniqueNames.get(derivedFromRvt));
          }
          value = typesDerivedFromText.toString();
        }
        _rvtPropertyValues.put(uniqueNames.get(rvt), value);
      }
    }

    public String getRowLabel()
    {
      return _rowLabel;
    }
    
    public Map getDataHeaderSinglePropertyValues()
    {
      return _rvtPropertyValues;
    }
  }

}
