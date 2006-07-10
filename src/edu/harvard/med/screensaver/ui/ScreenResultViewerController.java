// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIColumn;
import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.component.UIInput;
import javax.faces.component.UISelectBoolean;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.servlet.http.HttpSession;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.ui.util.JSFUtils;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;

/**
 * JSF backing bean for Screen Result Viewer web page (screenresultviewer.jsp).
 * <p>
 * The <code>screenResult</code> property should be set to the
 * {@link ScreenResult} that is to be viewed.<br>
 * The <code>itemsPerPage</code> property controls how many rows to show per
 * "page"; defaults to {@link #DEFAULT_ITEMS_PER_PAGE}.<br>

 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
// TODO: JSF issue: is it "wrong" to be modifying view structure from within
// this class? By updating the view dynamically here, are we violating MVC
// conventions? Already, this class is concerned with both Controller and Model
// parts, and with the dynamic view modification (variable set of table
// columns), we now are dealing with the all parts of MVC.

// TODO: this class needs to be broken up! it's too big! (maybe)
public class ScreenResultViewerController
{

  // static data members
  
  private static Logger log = Logger.getLogger(ScreenResultViewerController.class);
  
  private static final int DEFAULT_ITEMS_PER_PAGE = 10;

  private static final int RAWDATA_TABLE_FIXED_COLUMN_COUNT = 2;

  private static final int METADATA_TABLE_FIXED_COLUMN_COUNT = 1;
  

  // instance data members

  private DAO _dao;
  private ScreenResult _screenResult;
  private int itemsPerPage = DEFAULT_ITEMS_PER_PAGE;
  private int _firstRow;
  private boolean _showMetadataTable = true;
  private boolean _showRawDataTable = true;
  private UIData _dataTable;
  private UIData _metadataTable;
  private UIInput _firstDisplayedRowNumberInput;
  private UIInput _plateNumberInput;
  private String _plateNumber;
  private String[] _selectedDataHeaderNames;
  private String _rowRangeText;

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

  private List<DataRow> _rawTableData;

  private HashSet _selectedDataHeaderNamesSet;

  private Map<ResultValueType,String> _uniqueDataHeaderNamesMap;

  
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

  public ScreenResult getScreenResult()
  {
    return _screenResult;
  }
  
  public List<ResultValueType> getDataHeaders()
  {
    return new ArrayList<ResultValueType>(_screenResult.getResultValueTypes());
  }

  public void setScreenResult(ScreenResult screenResult)
  {
    if (_screenResult != screenResult) {
      flagNeedViewLayoutUpdate();
    }
    _screenResult = screenResult;
    updateViewLayout();
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

  public UIData getDataTable()
  {
    return _dataTable;
  }

  public void setDataTable(UIData dataUIComponent)
  {
    _dataTable = dataUIComponent;
    updateViewLayout();
  }
  
  public UIData getMetadataTable()
  {
    return _metadataTable;
  }

  public void setMetadataTable(UIData dataUIComponent)
  {
    _metadataTable = dataUIComponent;
    updateViewLayout();
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

//  public UIInput getShowMetadataTableInput()
//  {
//    return _showMetadataTableInput;
//  }
//
//
//  public void setShowMetadataTableInput(UIInput showMetadataTableInput)
//  {
//    _showMetadataTableInput = showMetadataTableInput;
//  }
//
//
//  public UIInput getShowRawDataTableInput()
//  {
//    return _showRawDataTableInput;
//  }
//
//
//  public void setShowRawDataTableInput(UIInput showRawDataTableInput)
//  {
//    _showRawDataTableInput = showRawDataTableInput;
//  }


  public List<MetadataRow> getMetadata()
  {
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
                                      getUniqueDataHeaderNamesMap(),
                                      property));
      }
      catch (Exception e) {
        e.printStackTrace();
        log.error("could not obtain value for property ResultValueType." + property);
      }
    }
    return tableData;
  }

  /**
   * @return a List of {@link DataRow} objects
   */
  public List<DataRow> getRawData()
  {
    lazyBuildRawData();
    return _rawTableData;
  }
  
  /**
   * @motivation for firstDisplayedRow validator maximum
   */
  public int getRawDataSize()
  {
    return getRawData().size();
  }

  public int getItemsPerPage()
  {
    return itemsPerPage;
  }

  public void setItemsPerPage(int itemsPerPage)
  {
    this.itemsPerPage = itemsPerPage;
  }
  
  public String getRowRangeText()
  {
    return "of " + _dataTable.getRowCount();
  }
  
  public int getFirstDisplayedRowNumber()
  {
    return _firstRow + 1;
  }
  
  public void setFirstDisplayedRowNumber(int firstDisplayedRowNumber)
  {
    log.debug("setFirstDisplayedRowNumber(" + firstDisplayedRowNumber + ")");
    _firstRow = firstDisplayedRowNumber - 1;
    _dataTable.setFirst(_firstRow);
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
  
  // TODO: rename to getPlateSelectItems
  public List<SelectItem> getPlateSelectItems()
  {
    return JSFUtils.createUISelectItems(_screenResult.getPlateNumbers());
  }
  
  public List<SelectItem> getDataHeaderSelectItems()
  {
    List<SelectItem> result = new ArrayList<SelectItem>();
    for (ResultValueType rvt : _screenResult.getResultValueTypes()) {
      result.add(new SelectItem(getUniqueDataHeaderNamesMap().get(rvt)));
    }
    return result;
  }
  
  public String[] getSelectedDataHeaderNames()
  {
    if (_selectedDataHeaderNames == null) {
      // select all items, if selection array not yet defined
      _selectedDataHeaderNames = new String[_screenResult.getResultValueTypes()
                                                         .size()];
      _selectedDataHeaderNames = getUniqueDataHeaderNamesMap().values()
                                                              .toArray(new String[getUniqueDataHeaderNamesMap().size()]);
    }
    return _selectedDataHeaderNames;
  }


  public void setSelectedDataHeaderNames(String[] selectedDataHeaderNames)
  {
    _selectedDataHeaderNames = selectedDataHeaderNames;
  }


  public String getSessionInfo()
  {
    HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
    return "ID: " + session.getId() + "\n" +
    "last accessed time: " + session.getLastAccessedTime();
  }
  
  public Map<ResultValueType,String> getUniqueDataHeaderNamesMap()
  {
    lazyBuildUniqueDataHeaderNamesMap();
    return _uniqueDataHeaderNamesMap;
  }

//  public Converter getRowToPlateConverter()
//  {
//    if (_rowToPlateConverter == null) {
//      _rowToPlateConverter = new Converter() {
//        public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) throws ConverterException
//        {
//          for (SelectItem item : getPlates()) {
//            if (item.getValue().toString().equals(arg2)) {
//              return item;
//            }
//          }
//          throw new ConverterException("no item matches string representation '" + arg2 + "'");
//        }
//        
//        public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2) throws ConverterException 
//        {
//          for (SelectItem item : getPlates()) {
//            if (item.getValue().equals(arg2)) {
//              return item.getLabel();
//            }
//          }
//          throw new ConverterException("no item matches object representation '" + arg2 + "'");
//        };
//      };
//    }
//    return _rowToPlateConverter;
//  }
  

  // JSF application methods
  
  public String gotoPage(int pageIndex)
  {
    try {
      UIData dataTable = getDataTable();
      int firstRow = ( pageIndex * dataTable.getRows() ) + 1;
      if (firstRow > 0 &&
        firstRow <= _screenResult.getResultValueTypes().first().getResultValues().size()) {
        setFirstDisplayedRowNumber(firstRow);
        _plateNumberInput.setValue(getRawData().get(firstRow - 1).getWell().getPlateNumber().toString());
      }
      return null;
    } 
    catch (Exception e) {
      return "error";
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
    FacesContext.getCurrentInstance().addMessage("dataForm",
                                                 new FacesMessage("Download feature not yet implemented!"));
    // TODO: implement
    return null;
  }
  
  public String update()
  {
    log.debug("update action received");
    return null; // redisplay
  }
  
  public String done()
  {
    log.debug("done action received");
    //    resetViewLayout(); // don't want to do this cuz tables won't be rebuilt if screenResult is the same on next use
    return "done";
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
    Object value = getRawData().get(Integer.parseInt(event.getNewValue().toString())).getWell().getPlateNumber().toString();
    _plateNumberInput.setValue(value);
  }
  
  public void plateNumberListener(ValueChangeEvent event)
  {
    log.debug("new plate number: '" + event.getNewValue() + "'");
    int firstRowForPlateNumber = findFirstRowForPlateNumber(Integer.parseInt(event.getNewValue().toString()));
    _firstDisplayedRowNumberInput.setValue(firstRowForPlateNumber);
  }
  
  @SuppressWarnings("unchecked")
  public void selectedDataHeadersListener(ValueChangeEvent event)
  {
    log.debug("data headers selection changed: '" + event.getNewValue() + "'");
    _selectedDataHeaderNamesSet = new HashSet(Arrays.asList((String[]) event.getNewValue()));
    resetTables();
    flagNeedViewLayoutUpdate();
    updateViewLayout();
  }
  
  
  // private methods
  
  /**
   * Generates a standard name for dynamically-added table columns.
   */
  private String makeResultValueTypeColumnName(String name)
  {
    return name.replaceAll("[ ()]", "") + "Column";
  }
  
  
  private void resetViewLayout()
  {
    _showMetadataTable = true;
    _showRawDataTable = true;
    _plateNumber2FirstRow = null;
    _rawTableData = null;
    _firstRow = 0;
    _plateNumber = null;
    _uniqueDataHeaderNamesMap = null;
//    _rowToPlateConverter = null;

    resetTables();
  }


  private void resetTables()
  {
    if (getDataTable() != null && getDataTable().getChildren().size() > RAWDATA_TABLE_FIXED_COLUMN_COUNT) {
      resetTable(getDataTable(), RAWDATA_TABLE_FIXED_COLUMN_COUNT);
    }
    if (getMetadataTable() != null && getMetadataTable().getChildren().size() > METADATA_TABLE_FIXED_COLUMN_COUNT) {
      resetTable(getMetadataTable(), METADATA_TABLE_FIXED_COLUMN_COUNT);
    }
  }

  /**
   * Removes any dynamically-added columns, allowing the table's dynamic columns
   * to be added anew for a new ScreenResult.
   */
  @SuppressWarnings("unchecked")
  private void resetTable(UIData table, int fixedColumnCount)
  {
    
    log.debug("dynamically removing old columns from table '" + table + "'");
    Collection dynamicColumns = table.getChildren().subList(fixedColumnCount, table.getChildCount());
    List columns = table.getChildren();
    // set parent of columns to remove to null, just to be safe (JSF spec says
    // you must do this)
    for (Iterator iter = dynamicColumns.iterator(); iter.hasNext();) {
      UIColumn column = (UIColumn) iter.next();
      column.setParent(null);
      iter.remove();
      assert !columns.contains(column) : "column was not removed";  
    }
    
    // reset the 1st row to be displayed
    table.setFirst(0);
    // reset the "current" row (in terms of selection/editing)
    // "-1" also causes the state of child components to be reset by UIData (according to JSF spec)
    table.setRowIndex(-1);
  }

  /**
   * Dynamically add table columns for Data Headers (aka ResultValueTypes) to
   * both raw data and metadata tables, iff they have not already been added
   */
  private void updateViewLayout()
  {
    if (_screenResult == null || _dataTable == null || _metadataTable == null) {
      // both tables' UI components and ScreenResult must be known before we can
      // do our thing
      return;
    }
    if (!_needViewLayoutUpdate) {
      return;
    }

    assert _metadataTable.getChildCount() > 0 : "cannot add dynamic columns before fixed columns have been created for metadata table";
    assert _dataTable.getChildCount() > 0 : "cannot add dynamic columns before fixed columns have been created for raw data table";
    resetViewLayout();
    log.debug("dynamically adding columns to tables");
    String iteratedRowBeanVariableName = "row";
    _dataTable.setVar(iteratedRowBeanVariableName);
    _metadataTable.setVar(iteratedRowBeanVariableName);
    for (ResultValueType rvt : _screenResult.getResultValueTypes()) {
      String uniqueName = getUniqueDataHeaderNamesMap().get(rvt);
      if (_selectedDataHeaderNamesSet == null ||
        _selectedDataHeaderNamesSet.contains(uniqueName)) {
        JSFUtils.addTableColumn(FacesContext.getCurrentInstance(),
                                _metadataTable,
                                makeResultValueTypeColumnName(uniqueName),
                                uniqueName,
                                MetadataRow.getBindingExpression(iteratedRowBeanVariableName, rvt));
        JSFUtils.addTableColumn(FacesContext.getCurrentInstance(),
                                _dataTable,
                                makeResultValueTypeColumnName(uniqueName),
                                uniqueName,
                                DataRow.getBindingExpression(iteratedRowBeanVariableName, rvt));
      }
    }
    resetNeedViewLayoutUpdate();
  }
  
  private void flagNeedViewLayoutUpdate()
  {
    _needViewLayoutUpdate = true;
  }
  
  private void resetNeedViewLayoutUpdate()
  {
    _needViewLayoutUpdate = false;
  }

  private int getPageIndex()
  {
    return _dataTable.getFirst() / _dataTable.getRows();
  }

  private void lazyBuildUniqueDataHeaderNamesMap()
  {
    if (_uniqueDataHeaderNamesMap == null) {
      _uniqueDataHeaderNamesMap = new HashMap<ResultValueType,String>();
      List<String> names = new ArrayList<String>();
      for (ResultValueType rvt : _screenResult.getResultValueTypes()) {
        names.add(rvt.getName());
      }
      List<String> names2 = new ArrayList<String>();
      for (ResultValueType rvt : _screenResult.getResultValueTypes()) {
        String name = rvt.getName();
        if (Collections.frequency(names, name) > 1) {
          names2.add(name);
          name += " (" + Collections.frequency(names2, name) + ")";
        }
        _uniqueDataHeaderNamesMap.put(rvt, name);
      }
    }
  }

  private void lazyBuildPlateNumber2FirstRow()
  {
    if (_plateNumber2FirstRow == null) {
      _plateNumber2FirstRow = new HashMap<Integer,Integer>();
      List<DataRow> rawData = getRawData();
      for (int i = 0; i < rawData.size(); ++i) {
        Integer plateNumber = rawData.get(i).getWell().getPlateNumber();
        if (!_plateNumber2FirstRow.containsKey(plateNumber)) {
          _plateNumber2FirstRow.put(plateNumber, i);
        }
      }
    }
  }

  private void lazyBuildRawData()
  {
    if (_rawTableData == null) {
      // to build our table data structure, we will iterate the ResultValueTypes'
      // ResultValues in parallel (kind of messy!)
      Map<ResultValueType,Iterator> rvtIterators = new HashMap<ResultValueType,Iterator>();
      for (ResultValueType rvt : _screenResult.getResultValueTypes()) {
        rvtIterators.put(rvt,
                         rvt.getResultValues().iterator());
      }
      _rawTableData = new ArrayList<DataRow>();
      for (ResultValue majorResultValue : _screenResult.getResultValueTypes().first().getResultValues()) {
        DataRow dataRow = new DataRow(_screenResult.getResultValueTypes(), majorResultValue.getWell());
        for (Map.Entry<ResultValueType,Iterator> entry : rvtIterators.entrySet()) {
          Iterator rvtIterator = entry.getValue();
          dataRow.addResultValue(entry.getKey(), 
                                 (ResultValue) rvtIterator.next());
        }
        _rawTableData.add(dataRow);
      }
    }
  }
  
  private int findFirstRowForPlateNumber(int selectedPlateNumber)
  {
    lazyBuildPlateNumber2FirstRow();
    
    if (!_plateNumber2FirstRow.containsKey(selectedPlateNumber)) {
      log.error("invalid plate number: " + selectedPlateNumber);
      return getDataTable().getFirst();
    }
    return _plateNumber2FirstRow.get(selectedPlateNumber) + 1;
  }

  
  // inner classes
  
  /**
   * MetadataRow bean, used to provide ScreenResult metadata to JSF components via EL
   * @author ant
   */
  public static class MetadataRow
  {
    private String _rowLabel;
    /**
     * Array containing the value of the same property for each ResultValueType
     */
    private String[] _rvtPropertyValues;    

    /**
     * Constructs a MetadataRow object.
     * @param rvts The {@link ResultValueType}s that contain the data for this row
     * @param uniqueNames 
     * @param property a bean property of the {@link ResultValueType}, which defines the type of metadata to be displayed by this row
     * @throws Exception if the specified property cannot be determined for a ResultValueType
     */
    public MetadataRow(Collection<ResultValueType> rvts, Map<ResultValueType,String> uniqueNames, String propertyName) throws Exception
    {
      _rowLabel = propertyName; // TODO: need better display name
      _rvtPropertyValues = new String[rvts.size()];
      int i = 0;
      for (ResultValueType rvt : rvts) {
        _rvtPropertyValues[i] = BeanUtils.getProperty(rvt, propertyName);
        // HACK: construct a string representing the typesDerivedFrom property
        // TODO: how can we do this properly?  ResultValueType.typesDerivedFromTextList property?
        if (propertyName.equals("name")) {
          _rvtPropertyValues[i] = uniqueNames.get(i);
        }
        else if (propertyName.equals("typesDerivedFrom")) {
          StringBuilder typesDerivedFromText = new StringBuilder();
          for (ResultValueType derivedFromRvt : rvt.getTypesDerivedFrom()) {
            if (typesDerivedFromText.length() > 0) {
              typesDerivedFromText.append(", ");
            }
            typesDerivedFromText.append(uniqueNames.get(derivedFromRvt));
          }
          _rvtPropertyValues[i] = typesDerivedFromText.toString();
        }
        ++i;
      }
    }

    /**
     * Returns a JSF EL expression that can be used to specify the bean property to
     * bind to a UIData cell. Since the DataRow is in fact the bean providing the data
     * to a UIData table, it is the best place for storing the knowledge of how
     * to access its data via a JSF EL expression.
     * 
     * @param dataTable
     * @param rvt
     * @return a JSF EL expression
     */
    public static String getBindingExpression(String rowBeanVariableName, ResultValueType rvt)
    {
      return "#{" + rowBeanVariableName + ".dataHeaderSinglePropertyValues[" + rvt.getOrdinal() + "]}";
    }
    
    public String getRowLabel()
    {
      return _rowLabel;
    }
    
    public String[] getDataHeaderSinglePropertyValues()
    {
      return _rvtPropertyValues;
    }
  }

  
  /**
   * DataRow bean, used to provide ScreenResult data to JSF components via EL
   * @author ant
   */
  public static class DataRow
  {
    private ScreenResult _screenResult;
    private Well _well;
    private ResultValue[] _resultValues;
    
    public DataRow(SortedSet<ResultValueType> rvts, Well well)
    {
      _resultValues = new ResultValue[rvts.size()];
      _well = well;
    }

    /**
     * Returns a JSF EL expression that can be used to specify the bean property
     * to bind to a UIData cell. The expression converts a null value to the
     * empty string. Since the DataRow is in fact the bean providing the data to
     * a UIData table, it is the best place for storing the knowledge of how to
     * access its data via a JSF EL expression.
     * 
     * @param dataTable
     * @param rvt
     * @return a JSF EL expression
     */
    public static String getBindingExpression(String rowBeanVariableName, ResultValueType rvt)
    {
      String valueExpr = rowBeanVariableName + ".resultValues[" + rvt.getOrdinal() + "].value";
      return "#{" + valueExpr + "}";
    }

    public Well getWell()
    {
      return _well;
    }

    public ResultValue[] getResultValues()
    {
      return _resultValues;
    }

    public void addResultValue(ResultValueType rvt, ResultValue rv)
    {
      _resultValues[rvt.getOrdinal()] = rv;
    }
  }
  
  public static class RowValidator implements Validator
  {
    public void validate(FacesContext ctx, UIComponent toValidate, Object value) throws ValidatorException
    {
        try {
          int firstDisplayedRowNumber = Integer.parseInt(value.toString());
          if (firstDisplayedRowNumber < 0 || firstDisplayedRowNumber >= 100) {
            throw new ValidatorException(new FacesMessage("row number out of range"));
          }
        } 
        catch (NumberFormatException e) {
          throw new ValidatorException(new FacesMessage("invalid row number"), e);
        }
    }
  }

}
