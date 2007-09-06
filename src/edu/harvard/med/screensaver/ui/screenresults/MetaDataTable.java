// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.faces.component.UISelectMany;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.ui.util.UISelectManyBean;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;

/**
 * Backing bean for a "meta data" table, which displays information for a set of
 * data types, one data type per column, one row per data type attribute.
 * Manages a "select many" component that allows the user to select the subset
 * of data types that are to be viewed.
 *
 * @motivation Encapsulate common functionality for ScreenResultViewer's Data
 *             Headers table and Annotation table.
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public abstract class MetaDataTable<T extends MetaDataType> extends AbstractBackingBean implements Observer
{
  // static members

  private static Logger log = Logger.getLogger(MetaDataTable.class);

  private static final DataModel EMPTY_METADATA_MODEL = new ListDataModel(new ArrayList<MetaDataTableRow<?>>());


  // instance data members

  private List<T> _metaDataTypes;
  private UISelectMany _selectManyUIInput;
  private UISelectManyBean<T> _selections;
  private DataModel _columnModel;
  private DataModel _dataModel;
  /**
   * @motivation This class can't extend Observable due to Java's
   *             single-inheritance limitation.
   */
  private Observer _observer;


  // abstract methods

  abstract protected List<MetaDataTableRowDefinition<T>> getMetaDataTableRowDefinitions();


  // public constructors and methods

  /**
   * Set the meta data types (table columns), along with an observer that will
   * be notified when the set of selected (visible) data types changes. Must be
   * called before other public methods are called.
   *
   * @param observer the observer to be notified when the set of visible meta
   *          data types changes. Can be null. The observer's update() method
   *          will be passed a UISelectManyBean object in the Observable
   *          parameter, and a List of MetaDataTypes in the Object parameter.
   */
  public void initialize(List<T> metaDataTypes, Observer observer)
  {
    reset();
    _metaDataTypes = metaDataTypes;
    _observer = observer;
  }

  public DataModel getDataModel()
  {
    lazyBuildDataModel();
    if (_dataModel == null) {
      return EMPTY_METADATA_MODEL;
    }
    return _dataModel;
  }

  public UISelectMany getSelectManyUIInput()
  {
    return _selectManyUIInput;
  }

  public void setSelectManyUIInput(UISelectMany selectManyUIInput)
  {
    _selectManyUIInput = selectManyUIInput;
  }

  public DataModel getColumnModel()
  {
    if (_columnModel == null) {
      updateColumnModel();
    }
    return _columnModel;
  }

  public UISelectManyBean<T> getSelections()
  {
    if (_selections == null) {
      _selections = new UISelectManyBean<T>(getDataTypes())
      {
        protected String getLabel(T dataType)
        {
          return dataType.getName();
        }
      };
      // select all meta data types, initially
      _selections.setSelections(getDataTypes());

      _selections.addObserver(this);
    }
    return _selections;
  }

  /**
   * @motivation for "Columns" JSF data table component
   */
  @SuppressWarnings("unchecked")
  public Object getCellValue()
  {
    DataModel dataModel = getDataModel();
    DataModel columnModel = getColumnModel();
    if (columnModel.isRowAvailable()) {
      String columnName = (String) columnModel.getRowData();  // getRowData() is really getColumnData()
      MetaDataTableRow<T> row = (MetaDataTableRow<T>) dataModel.getRowData();
      return row.getSinglePropertyValues().get(columnName);
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public void update(Observable o, Object obj)
  {
    if (log.isDebugEnabled()) {
      log.debug("meta data selections changed: " + getSelectedMetaDataTypeNames());
    }
    updateColumnModel();
    if (_observer != null) {
      // note: we must notify the observer only *after* we've updated our column model
      _observer.update(o, obj);
    }
  }


  // JSF event listeners

  @SuppressWarnings("unchecked")
  public void selectionListener(ValueChangeEvent event)
  {
    log.debug("meta data type selections changed to " + event.getNewValue());
    getSelections().setValue((List<String>) event.getNewValue());

    // enforce minimum of 1 selected meta data type (data table query will break otherwise)
    if (getSelections().getSelections().size() == 0) {
      getSelections().setSelections(getDataTypes().subList(0,1));
      // this call shouldn't be necessary, as I would've expected UIInput component to query its model in render phase, but...
      _selectManyUIInput.setValue(getSelections().getValue());
    }

    // skip "update model" JSF phase, to avoid overwriting model values set above
    getFacesContext().renderResponse();
  }


  // JSF application methods

  @UIControllerMethod
  public String selectAll()
  {
    getSelections().setSelections(getDataTypes());
    _selectManyUIInput.setValue(getSelections().getValue());
    return REDISPLAY_PAGE_ACTION_RESULT;
  }


  // private methods

  private void reset()
  {
    _metaDataTypes = null;
    _selections = null;
    _columnModel = null;
    _dataModel = null;
  }

  private List<T> getDataTypes()
  {
    if (_metaDataTypes == null) {
      throw new IllegalStateException("not initialized (hint: call initialize() first)");
    }
    return _metaDataTypes;
  }

  private void lazyBuildDataModel()
  {
    if (_dataModel == null) {
      List<MetaDataTableRow<T>> tableData = new ArrayList<MetaDataTableRow<T>>();
      for (MetaDataTableRowDefinition<T> rowDef : getMetaDataTableRowDefinitions()) {
        try {
          tableData.add(new MetaDataTableRow<T>(getDataTypes(), rowDef));
        }
        catch (Exception e) {
          log.error("could not obtain value for property " + rowDef.getPropertyName());
        }
      }
      _dataModel = new ListDataModel(tableData);
    }
  }

  private List<String> getSelectedMetaDataTypeNames()
  {
    List<String> namesOfSelected = new ArrayList<String>();
    for (T metaDataType : getSelections().getSelections()) {
      namesOfSelected.add(metaDataType.getName());
    }
    return namesOfSelected;
  }

  private void updateColumnModel()
  {
    log.debug("updating meta data types");
    _columnModel = new ListDataModel(getSelectedMetaDataTypeNames());
  }


  // inner classes

  public static class MetaDataTableRowDefinition<T extends MetaDataType>
  {
    private String _propertyName;
    private String _displayName;
    private String _description;

    public MetaDataTableRowDefinition(String propertyName,
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
    public String formatValue(T metaDataType)
    {
      try {
        return BeanUtils.getProperty(metaDataType, _propertyName);
      }
      catch (Exception e) {
        log.error(e.getMessage());
        return "<error>";
      }
    }
  }

  public static class MetaDataTableRow<T extends MetaDataType>
  {
    private MetaDataTableRowDefinition<T> _rowDefinition;
    /**
     * Array containing the value of the same property for each MetaDataType
     */
    private Map<String,Object> _propertyValues;

    public MetaDataTableRow(Collection<T> metaDataTypes,
                            MetaDataTableRowDefinition<T> rowDefinition)
    {
      _rowDefinition = rowDefinition;
      _propertyValues = new HashMap<String,Object>();
      for (T metaDataType : metaDataTypes) {
        _propertyValues.put(metaDataType.getName(),
                               rowDefinition.formatValue(metaDataType));
      }
    }

    public String getRowLabel()
    {
      return _rowDefinition.getDisplayName();
    }

    public String getRowDescription()
    {
      return _rowDefinition.getDescription();
    }

    public Map<String,Object> getSinglePropertyValues()
    {
      return _propertyValues;
    }
  }
}

