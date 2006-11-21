
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import javax.faces.component.UIData;
import javax.faces.component.UIInput;
import javax.faces.component.UISelectBoolean;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultExporter;
import edu.harvard.med.screensaver.io.workbook.Workbook;
import edu.harvard.med.screensaver.model.libraries.Well;
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
import edu.harvard.med.screensaver.ui.util.JSFUtils;

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
  
  private static final int DEFAULT_ITEMS_PER_PAGE = 10;

  private static final int RAWDATA_TABLE_FIXED_COLUMN_COUNT = 2;

  private static final int METADATA_TABLE_FIXED_COLUMN_COUNT = 1;
  
  private static final DataModel EMPTY_METADATA_MODEL = new ListDataModel(new ArrayList<MetadataRow>());
  private static final DataModel EMPTY_RAW_DATA_MODEL = new ListDataModel(new ArrayList<RawDataRow>());

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
  private int _firstRow;
  /**
   * For rowNumber UIInput component. 1-based value.
   */
  private int _rowNumber;
  private Integer _plateNumber;
  private String[] _selectedDataHeaderNames;
  private String _rowRangeText;
  private UIInput _rowNumberInput;
  private UIInput _plateNumberInput;
  private UIData _dataTable;

  /**
   * Flag indicating whether the tables on this page need to have their columns
   * dynamically updated to reflect a new ScreenResult.
   * 
   * @motivation the table structure in the JSF view is reused and must be
   *             updated when a new ScreenResult is to be viewed
   */
  private boolean _needViewLayoutUpdate;

  /**
   * Remembers the first row where a given plate number appears.
   * 
   * @motivation optimization
   */
  private Map<Integer,Integer> _plateNumber2FirstRow;
  private UniqueDataHeaderNames _uniqueDataHeaderNames;
  private DataModel _rawDataModel;
  private DataModel _rawDataColumnModel;
  private DataModel _dataHeaderColumnModel;
  private DataModel _metadataModel;
  private Map<String,Boolean> _collapsablePanelsState;


  // public methods
  
  public ScreenResultViewer()
  {
    _collapsablePanelsState = new HashMap<String,Boolean>();
    _collapsablePanelsState.put("summary", false);
    _collapsablePanelsState.put("dataHeadersTable", true);
    _collapsablePanelsState.put("dataTable", true);
    _collapsablePanelsState.put("heatMaps", true);
  }

  
  // bean property methods

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
    return _rowNumber;
  }

  public void setRowNumber(int rowNumber)
  {
    _rowNumber = rowNumber;
  }

  public UIInput getPlateNumberInput()
  {
    return _plateNumberInput;
  }

  public void setPlateNumberInput(UIInput plateNumberInput)
  {
    _plateNumberInput = plateNumberInput;
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
    return getRawData().getRowCount();
  }

  public String getRowRangeText()
  {
    return "of " + getRawDataSize();
  }
  
  public Integer getPlateNumber()
  {
    return _plateNumber;
  }

  public void setPlateNumber(Integer plateNumber)
  {
    log.debug("setPlateNumber(" + plateNumber + ")");
    _plateNumber = plateNumber;
  }

  public List<SelectItem> getPlateSelectItems()
  {
    return JSFUtils.createUISelectItems(getScreenResult().getPlateNumbers());
  }
  
  public List<SelectItem> getDataHeaderSelectItems()
  {
    return JSFUtils.createUISelectItems(getUniqueDataHeaderNames().asList());
  }
  
  public String[] getSelectedDataHeaderNames()
  {
    if (_selectedDataHeaderNames == null) {
      selectAllDataHeaders();
    }
    return _selectedDataHeaderNames;
  }

  public void setSelectedDataHeaderNames(String[] selectedDataHeaderNames)
  {
    _selectedDataHeaderNames = selectedDataHeaderNames;
  }
  
  public DataModel getDataHeaderColumnModel()
  {
    if (_dataHeaderColumnModel == null) {
      _dataHeaderColumnModel = new ListDataModel(Arrays.asList(getSelectedDataHeaderNames()));
    }
    return _dataHeaderColumnModel;
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
    DataModel columnModel = getDataHeaderColumnModel();
    if (columnModel.isRowAvailable()) {
      String columnName = (String) columnModel.getRowData();  // getRowData() is really getColumnData()
      MetadataRow row = (MetadataRow) dataModel.getRowData();
      return row.getDataHeaderSinglePropertyValues().get(columnName);
    }
    return null;
  }

  /**
   * @motivation for "Columns" JSF data table component
   * @return
   */
  public Object getRawDataCellValue()
  {
    DataModel dataModel = getRawData();
    DataModel columnModel = getDataHeaderColumnModel();
    if (columnModel.isRowAvailable()) {
      String columnName = (String) columnModel.getRowData(); // getRowData() is really getColumnData()
      assert columnName != null : "columnName is null";
      RawDataRow row = (RawDataRow) dataModel.getRowData();
      assert row != null : "row is null";
      return row.getResultValues().get(columnName).getValue();
    }
    return null;
  }


  // JSF application methods
  
  @SuppressWarnings("unchecked")
  public String gotoPage(int pageIndex)
  {
    try {
      // firstRow is a 1-based index
      _firstRow = (pageIndex * getDataTable().getRows());
      if (_firstRow > 0 &&
        _firstRow <= getScreenResult().getResultValueTypes().first().getResultValues().size()) {
        // scroll the data table to the new row
        getDataTable().setFirst(_firstRow);

        // update the plate selection list to the current plate
        if (getRawData() != EMPTY_RAW_DATA_MODEL) {
         _plateNumber = ((List<RawDataRow>) getRawData().getWrappedData()).get(_firstRow).getPlateNumber();
         _rowNumber = _firstRow + 1;
        }
      }
      return REDISPLAY_PAGE_ACTION_RESULT;
    } 
    catch (Exception e) {
      return ERROR_ACTION_RESULT;
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
  
//  public String nextPlate()
//  {
//    
//    return gotoPlate(_plateIndex + 1); 
//  }
//  
//  public String prevPlate()
//  {
//    return gotoPlate(_plateIndex - 1); 
//  }
  
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
    return REDISPLAY_PAGE_ACTION_RESULT; // redisplay
  }
  
  public String showAllDataHeaders()
  {
    selectAllDataHeaders();
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
    _firstRow = Integer.parseInt(event.getNewValue().toString()) - 1;
    // ensure value is within valid range, and in particular that we never show
    // less than the table's configured row count (unless it's more than the
    // total number of rows)
    _firstRow = Math.max(0,
                         Math.min(_firstRow,
                                  getRawDataSize() - _dataTable.getRows()));
    // scroll the data table to the new row
    getDataTable().setFirst(_firstRow);
    // update the plate selection list to the current plate
    @SuppressWarnings("unchecked")
    RawDataRow dataRow = ((List<RawDataRow>) getRawData().getWrappedData()).get(_firstRow);
    Integer plateNumber = dataRow.getPlateNumber();
    _plateNumberInput.setValue(plateNumber);
    _rowNumberInput.setValue(_firstRow + 1);
  }
  
  public void plateNumberListener(ValueChangeEvent event)
  {
    log.debug("new plate number: '" + event.getNewValue() + "'");
    _firstRow = findFirstRowNumberForPlateNumber(Integer.parseInt(event.getNewValue().toString()));
    // scroll the data table to the new row
    getDataTable().setFirst(_firstRow);
    // update the row field
    _rowNumberInput.setValue(_firstRow + 1);
  }
  
  public void selectedDataHeadersListener(ValueChangeEvent event)
  {
    log.debug("data headers selection changed: '" + event.getNewValue() + "'");
    _dataHeaderColumnModel = null; // cause to be reinitialized for new set of selected data headers
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
    _plateNumber2FirstRow = null;
    _dataHeaderColumnModel = null;
    _metadataModel = null;
    _rawDataModel = null;
    _firstRow = 0;
    _rowNumber = 1;
    _plateNumber = null;
    _selectedDataHeaderNames = null;
    _uniqueDataHeaderNames = null;

    // clear the bound UI components, so that they get recreated next time this view is used
    _dataTable = null;
    _rowNumberInput = null;
    _plateNumberInput = null;

    // _rowToPlateConverter = null;    
  }

  private int getPageIndex()
  {
    return /*getDataTable().getFirst()*/_firstRow / getDataTable().getRows();
  }

  private int findFirstRowNumberForPlateNumber(int selectedPlateNumber)
  {
    lazyBuildPlateNumber2FirstRow();
    
    if (!_plateNumber2FirstRow.containsKey(selectedPlateNumber)) {
      log.error("invalid plate number: " + selectedPlateNumber);
      return getDataTable().getFirst();
    }
    return _plateNumber2FirstRow.get(selectedPlateNumber);
  }

  private void lazyBuildPlateNumber2FirstRow()
  {
    if (_plateNumber2FirstRow == null) {
      _plateNumber2FirstRow = new HashMap<Integer,Integer>();
      DataModel rawData = getRawData();
      for (int i = 0; i < rawData.getRowCount(); ++i) {
        rawData.setRowIndex(i);
        Integer plateNumber = ((RawDataRow) rawData.getRowData()).getPlateNumber();
        if (!_plateNumber2FirstRow.containsKey(plateNumber)) {
          _plateNumber2FirstRow.put(plateNumber, i);
        }
      }
    }
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

  private void lazyBuildRawData()
  {
    if (_rawDataModel == null && getScreenResult() != null) {
      // to build our table data structure, we will iterate the
      // ResultValueTypes'
      // ResultValues in parallel (kind of messy!)
      Map<ResultValueType,Iterator> rvtIterators = new HashMap<ResultValueType,Iterator>();
      for (ResultValueType rvt : getScreenResult().getResultValueTypes()) {
        rvtIterators.put(rvt, rvt.getResultValues().iterator());
      }
      List<RawDataRow> tableData = new ArrayList<RawDataRow>();
      for (ResultValue majorResultValue : getScreenResult().getResultValueTypes()
                                                       .first()
                                                       .getResultValues()) {
        RawDataRow dataRow = new RawDataRow(getScreenResult().getResultValueTypes(),
                                            getUniqueDataHeaderNames(),
                                            majorResultValue.getWell().getPlateNumber(),
                                            majorResultValue.getWell().getWellName(),
                                            majorResultValue.getAssayWellType());
        for (Map.Entry<ResultValueType,Iterator> entry : rvtIterators.entrySet()) {
          Iterator rvtIterator = entry.getValue();
          dataRow.addResultValue(entry.getKey(),
                                 (ResultValue) rvtIterator.next());
        }
        tableData.add(dataRow);
      }
      _rawDataModel = new ListDataModel(tableData);
    }
  }
  
  private void selectAllDataHeaders()
  {
    _selectedDataHeaderNames = getUniqueDataHeaderNames().asArray();
    _dataHeaderColumnModel = null; // cause to be reinitialized for new set of selected data headers
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

  
  /**
   * RawDataRow bean, used to provide ScreenResult data to JSF components
   * @see ScreenResultViewer#getRawDataCellValue()
   * @author ant
   */
  public static class RawDataRow
  {
    private Integer _plateNumber;
    private String _wellName;
    private AssayWellType _assayWellType;
    private Map<String,ResultValue> _resultValues;
    private UniqueDataHeaderNames _uniqueNames;
    private StringBuilder _excludes = new StringBuilder();
    
    public RawDataRow(SortedSet<ResultValueType> rvts,
                      UniqueDataHeaderNames uniqueNames,
                      Integer plateNumber,
                      String wellName,
                      AssayWellType assayWellType)
    {
      _resultValues = new HashMap<String,ResultValue>();
      _uniqueNames = uniqueNames;
      _plateNumber = plateNumber;
      _wellName = wellName;
      _assayWellType = assayWellType;
    }
    
    public Integer getPlateNumber()
    {
      return _plateNumber;
    }

    public String getWellName()
    {
      return _wellName;
    }
    
    public String getAssayWellType()
    {
      return _assayWellType.toString();
    }
    
    public String getExcludes()
    {
      return _excludes.toString();
    }

    public Map<String,ResultValue> getResultValues()
    {
      return _resultValues;
    }

    public void addResultValue(ResultValueType rvt, ResultValue rv)
    {
      _resultValues.put(_uniqueNames.get(rvt), rv);
      if (rv.isExclude()) {
        if (_excludes.length() > 0) {
          _excludes.append(", ");
        }
        _excludes.append(_uniqueNames.get(rvt));
      }
    }
  }

}
