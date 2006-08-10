// $HeadURL$
// $Id$
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

import javax.faces.application.FacesMessage;
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
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.ui.AbstractController;
import edu.harvard.med.screensaver.ui.UniqueDataHeaderNames;
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
public class ScreenResultViewerController extends AbstractController
{

  // static data members
  
  private static Logger log = Logger.getLogger(ScreenResultViewerController.class);
  
  private static final int DEFAULT_ITEMS_PER_PAGE = 10;

  private static final int RAWDATA_TABLE_FIXED_COLUMN_COUNT = 2;

  private static final int METADATA_TABLE_FIXED_COLUMN_COUNT = 1;
  

  // instance data members

  private DAO _dao;
  private ScreenResult _screenResult;
  private ScreenResultExporter _screenResultExporter;
  private int _firstRow;
  private boolean _showMetadataTable = true;
  private boolean _showRawDataTable = true;
  private String _plateNumber;
  private String[] _selectedDataHeaderNames;
  private String _rowRangeText;
  private UIInput _firstDisplayedRowNumberInput;
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

  private HeatMapViewerController _heatMapViewer;

  
//  private Converter _rowToPlateConverter;
  

  // public methods
  
  public ScreenResultViewerController()
  {
  }
  

  // bean property methods

  public DAO getDAO()
  {
    return _dao;
  }

  public void setDAO(DAO dao)
  {
    _dao = dao;
  }

  public HeatMapViewerController getHeatMapViewer()
  {
    return _heatMapViewer;
  }

  public void setHeatMapViewer(HeatMapViewerController heatMapViewer)
  {
    _heatMapViewer = heatMapViewer;
  }

  public ScreenResult getScreenResult()
  {
    return _screenResult;
  }
  
  public void setScreenResult(ScreenResult screenResult)
  {
    if (_screenResult != screenResult) {
      resetView();
    }
    _screenResult = screenResult;
  }
  
  public ScreenResultExporter getScreenResultExporter()
  {
    return _screenResultExporter;
  }


  public void setScreenResultExporter(ScreenResultExporter screenResultExporter)
  {
    _screenResultExporter = screenResultExporter;
  }


  public boolean isShowMetadataTable()
  {
    return _showMetadataTable;
  }

  public void setShowMetadataTable(boolean showMetadataTable)
  {
    _showMetadataTable = showMetadataTable;
  }

  public boolean isShowRawDataTable()
  {
    return _showRawDataTable;
  }

  public void setShowRawDataTable(boolean showRawDataTable)
  {
    _showRawDataTable = showRawDataTable;
  }

  public UIInput getFirstDisplayedRowNumberInput()
  {
    return _firstDisplayedRowNumberInput;
  }

  public void setFirstDisplayedRowNumberInput(UIInput firstDisplayedRowNumberInput)
  {
    _firstDisplayedRowNumberInput = firstDisplayedRowNumberInput;
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
    return _metadataModel;
  }

  /**
   * @return a List of {@link RawDataRow} objects
   */
  public DataModel getRawData()
  {
    lazyBuildRawData();
    return _rawDataModel;
  }
  
  /**
   * @motivation for firstDisplayedRow validator maximum
   */
  public int getRawDataSize()
  {
    return getRawData().getRowCount();
  }

  public String getRowRangeText()
  {
    return "of " + getRawDataSize();
  }
  
  public int getFirstDisplayedRowNumber()
  {
    return _firstRow + 1;
  }
  
  /**
   * 
   * @param firstDisplayedRowNumber 1-based index
   */
  public void setFirstDisplayedRowNumber(int firstDisplayedRowNumber)
  {
    _firstRow = firstDisplayedRowNumber - 1;
  }

  public String getPlateNumber()
  {
    return _plateNumber;
  }

  public void setPlateNumber(String plateNumber)
  {
    log.debug("setPlateNumber(" + plateNumber + ")");
    _plateNumber = plateNumber;
  }

  public List<SelectItem> getPlateSelectItems()
  {
    return JSFUtils.createUISelectItems(_screenResult.generatePlateNumbers());
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
      _uniqueDataHeaderNames = new UniqueDataHeaderNames(_screenResult);
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
      int firstRow = (pageIndex * getDataTable().getRows()) + 1;
      if (firstRow > 0 &&
        firstRow <= _screenResult.getResultValueTypes().first().getResultValues().size()) {
        // update the row field
        setFirstDisplayedRowNumber(firstRow);
        // scroll the data table to the new row
        getDataTable().setFirst(getFirstDisplayedRowNumber() - 1);
        // update the plate selection list to the current plate
        _plateNumberInput.setValue(((List<RawDataRow>) getRawData().getWrappedData()).get(getFirstDisplayedRowNumber() - 1).getWell().getPlateNumber().toString());
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
  
  public String download()
  {
    File exportedWorkbookFile = null;
    FileOutputStream out = null;
    try {
      HSSFWorkbook workbook = _screenResultExporter.build(_screenResult);
      exportedWorkbookFile = File.createTempFile("workbook-export.",
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
    getFacesContext().addMessage("dataForm",
                                 new FacesMessage("Delete feature not yet implemented!"));
    // TODO: implement
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public String viewHeatMaps()
  {
    _heatMapViewer.setScreenResult(_screenResult);
    return "viewHeatMaps";
  }
  
  public String showWell()
  { 
    Object wellId = (Object) getFacesContext().getExternalContext().getRequestParameterMap().get("wellIdParam");
    log.debug("action event on well " + wellId);
    return "showWell";
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
  
  public String done()
  {
    log.debug("done action received");
    return DONE_ACTION_RESULT;
  }
  
  
  // JSF event listener methods
  
  public void showTableOptionListener(ValueChangeEvent event)
  {
    log.debug("refreshing page in response to value change event from " + event.getComponent().getId());
    ((UISelectBoolean) event.getComponent()).setValue(event.getNewValue());
  }

  public void firstDisplayedRowNumberListener(ValueChangeEvent event)
  {
    log.debug("firstDisplayedRowNumberListener called: " + event.getNewValue());
    int newFirstDisplayRowIndex = Integer.parseInt(event.getNewValue().toString()) - 1;
    // ensure value is within valid range, and in particular that we never show
    // less than the table's configured row count (unless it's more than the
    // total number of rows)
    newFirstDisplayRowIndex = Math.max(0,
                                        Math.min(newFirstDisplayRowIndex,
                                                 getRawDataSize() - _dataTable.getRows()));
    // scroll the data table to the new row
    getDataTable().setFirst(newFirstDisplayRowIndex);
    // update the plate selection list to the current plate
    @SuppressWarnings("unchecked")
    RawDataRow dataRow = ((List<RawDataRow>) getRawData().getWrappedData()).get(newFirstDisplayRowIndex);
    Integer plateNumber = dataRow.getWell().getPlateNumber();
    _plateNumberInput.setValue(plateNumber.toString());
  }
  
  public void plateNumberListener(ValueChangeEvent event)
  {
    log.debug("new plate number: '" + event.getNewValue() + "'");
    int newFirstRowNumberForPlateNumber = findFirstRowNumberForPlateNumber(Integer.parseInt(event.getNewValue().toString()));
    // scroll the data table to the new row
    getDataTable().setFirst(newFirstRowNumberForPlateNumber - 1);
    // update the row field
    _firstDisplayedRowNumberInput.setValue(newFirstRowNumberForPlateNumber);
  }
  
  public void selectedDataHeadersListener(ValueChangeEvent event)
  {
    log.debug("data headers selection changed: '" + event.getNewValue() + "'");
    _dataHeaderColumnModel = null; // cause to be reinitialized for new set of selected data headers
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
    _showMetadataTable = true;
    _showRawDataTable = true;
    _plateNumber2FirstRow = null;
    _dataHeaderColumnModel = null;
    _metadataModel = null;
    _rawDataModel = null;
    setFirstDisplayedRowNumber(1);
    _plateNumber = null;
    _selectedDataHeaderNames = null;
    _uniqueDataHeaderNames = null;

    // clear the bound UI components, so that they get recreated next time this view is used
    _dataTable = null;
    _firstDisplayedRowNumberInput = null;
    _plateNumberInput = null;

    // _rowToPlateConverter = null;    
  }

  private int getPageIndex()
  {
    return getDataTable().getFirst() / getDataTable().getRows();
  }

  private int findFirstRowNumberForPlateNumber(int selectedPlateNumber)
  {
    lazyBuildPlateNumber2FirstRow();
    
    if (!_plateNumber2FirstRow.containsKey(selectedPlateNumber)) {
      log.error("invalid plate number: " + selectedPlateNumber);
      return getDataTable().getFirst();
    }
    return _plateNumber2FirstRow.get(selectedPlateNumber) + 1;
  }

  private void lazyBuildPlateNumber2FirstRow()
  {
    if (_plateNumber2FirstRow == null) {
      _plateNumber2FirstRow = new HashMap<Integer,Integer>();
      DataModel rawData = getRawData();
      for (int i = 0; i < rawData.getRowCount(); ++i) {
        rawData.setRowIndex(i);
        Integer plateNumber = ((RawDataRow) rawData.getRowData()).getWell().getPlateNumber();
        if (!_plateNumber2FirstRow.containsKey(plateNumber)) {
          _plateNumber2FirstRow.put(plateNumber, i);
        }
      }
    }
  }

  private void lazyBuildMetadataModel()
  {
    if (_metadataModel == null) {
      assert _screenResult != null : "screenResult property not initialized";
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
          tableData.add(new MetadataRow(_screenResult.getResultValueTypes(),
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
    if (_rawDataModel == null) {
      // to build our table data structure, we will iterate the
      // ResultValueTypes'
      // ResultValues in parallel (kind of messy!)
      Map<ResultValueType,Iterator> rvtIterators = new HashMap<ResultValueType,Iterator>();
      for (ResultValueType rvt : _screenResult.getResultValueTypes()) {
        rvtIterators.put(rvt, rvt.getResultValues()
                                 .iterator());
      }
      List<RawDataRow> tableData = new ArrayList<RawDataRow>();
      for (ResultValue majorResultValue : _screenResult.getResultValueTypes()
                                                       .first()
                                                       .getResultValues()) {
        RawDataRow dataRow = new RawDataRow(_screenResult.getResultValueTypes(),
                                            getUniqueDataHeaderNames(),
                                            majorResultValue.getWell());
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
   * @see ScreenResultViewerController#getMetadataCellValue()
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
   * @see ScreenResultViewerController#getRawDataCellValue()
   * @author ant
   */
  public static class RawDataRow
  {
    private ScreenResult _screenResult;
    private Well _well;
    private Map<String,ResultValue> _resultValues;
    private UniqueDataHeaderNames _uniqueNames;
    
    public RawDataRow(SortedSet<ResultValueType> rvts, UniqueDataHeaderNames uniqueNames, Well well)
    {
      _resultValues = new HashMap<String,ResultValue>();
      _uniqueNames = uniqueNames;
      _well = well;
    }

    public Well getWell()
    {
      return _well;
    }

    public Map<String,ResultValue> getResultValues()
    {
      return _resultValues;
    }

    public void addResultValue(ResultValueType rvt, ResultValue rv)
    {
      _resultValues.put(_uniqueNames.get(rvt), rv);
    }
  }

}
