// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.util.HtmlUtils;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;

/**
 * Backing bean for a "meta data" table, which displays information for a set of
 * data types (e.g. {@link ResultValueType} or {@link AnnotationType}), one data
 * type per column, one row per data type attribute.
 *
 * @motivation Encapsulate common functionality for ScreenResultViewer's Data
 *             Headers table and Annotation table.
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public abstract class MetaDataTable<T extends MetaDataType> extends AbstractBackingBean
{
  // static members

  private static Logger log = Logger.getLogger(MetaDataTable.class);

  private static final DataModel EMPTY_METADATA_MODEL = new ListDataModel(new ArrayList<MetaDataTableRow<?>>());



  // instance data members

  private List<T> _metaDataTypes = Collections.emptyList();
  private DataModel _columnModel;
  private DataModel _dataModel;


  // abstract methods

  abstract protected List<MetaDataTableRowDefinition<T>> getMetaDataTableRowDefinitions();


  // public constructors and methods

  /**
   * Set the meta data types (table columns), along with an observer that will
   * be notified when the set of selected (visible) data types changes. Must be
   * called before other public methods are called.
   */
  public void initialize(List<T> metaDataTypes)
  {
    reset();
    _metaDataTypes = metaDataTypes;
  }

  public DataModel getDataModel()
  {
    lazyBuildDataModel();
    if (_dataModel == null) {
      return EMPTY_METADATA_MODEL;
    }
    return _dataModel;
  }

  public DataModel getColumnModel()
  {
    if (_columnModel == null) {
      _columnModel = new ListDataModel(getMetaDataTypeNames());
    }
    return _columnModel;
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


  // private methods

  private void reset()
  {
    _metaDataTypes = Collections.emptyList();
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


  private List<String> getMetaDataTypeNames()
  {
    List<String> names= new ArrayList<String>();
    for (T metaDataType : _metaDataTypes) {
      names.add(metaDataType.getName());
    }
    return names;
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
      _displayName = HtmlUtils.toNonBreakingSpaces(displayName);
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

