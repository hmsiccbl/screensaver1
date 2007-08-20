// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.namevaluetable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIData;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.ui.AbstractBackingBean;


/**
 * A name-value table! A two-column table whose left column is a label, and whose right column
 * can contain various value types, such as text, links, commands, lists of links, etc., as
 * described by the {@link ValueType ValueType enum}.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
abstract public class NameValueTable extends AbstractBackingBean
{
  
  // public static final data
  
  private static final Logger log = Logger.getLogger(NameValueTable.class);
  private static final String NAME = "name";
  private static final String VALUE = "value";


  // private instance data
  
  private UIData _dataTable;
  private DataModel _dataModel;
  private DataModel _columnModel;

  /**
   * Workaround for JSF suckiness. Used by {@link #getLink()}.
   */
  private final Map<String,String> _linkMapper = new HashMap<String,String>() {
    private static final long serialVersionUID = 1L;
    public String get(Object key)
    {
      String linkValue;
      if (key instanceof String) {
        linkValue = (String) key;        
      }
      else if (key instanceof Integer) {
        linkValue = ((Integer) key).toString();
      }
      else {
        throw new RuntimeException("expected String or Integer value for key");
      }
      return getLink(getRowIndex(), linkValue);
    }
  };

  
  // public constructor
  
  /**
   * Construct a new <code>NameValueTable</code> object.
   */
  public NameValueTable()
  {
    List<String> columns = new ArrayList<String>(2);
    columns.add(NAME);
    columns.add(VALUE);
    _columnModel = new ListDataModel(columns);
  }

  
  // public getters and setters - used by nameValueTable.jspf
  
  /**
   * Get the data table.
   * @return the data table
   */
  public UIData getDataTable()
  {
    return _dataTable;
  }

  /**
   * Set the data table.
   * @param dataTable the new data table
   */
  public void setDataTable(UIData dataTable)
  {
    _dataTable = dataTable;
  }
  
  /**
   * Get the data model.
   * @return the data model
   */
  public DataModel getDataModel()
  {
    return _dataModel;
  }

  /**
   * Set the data model.
   * @param dataModel the new data model
   */
  public void setDataModel(DataModel dataModel)
  {
    _dataModel = dataModel;
  }  

  public DataModel getColumnModel()
  {
    return _columnModel;
  }
  
  public void setColumnModel(DataModel columnModel)
  {
    _columnModel = columnModel;
  }

  public String getColumnStyle()
  {
    return isNameColumn() ? "keyColumn" : "textColumn";
  }

  public String getNameDescription()
  {
    return getDescription(getRowIndex());
  }
  
  public Object getCellValue()
  {
    if (isNameColumn()) {
      return getName(getRowIndex());
    }
    return getValue(getRowIndex());
  }

  public boolean getIsNameColumn()
  {
    return isNameColumn();
  }

  public boolean getIsTextValue()
  {
    return ! isNameColumn() && getValueType(getRowIndex()).equals(ValueType.TEXT);
  }
  
  public boolean getIsUnescapedTextValue()
  {
    return ! isNameColumn() && getValueType(getRowIndex()).equals(ValueType.UNESCAPED_TEXT);
  }

  public boolean getIsCommandValue()
  {
    return ! isNameColumn() && getValueType(getRowIndex()).equals(ValueType.COMMAND);
  }

  public boolean getIsLinkValue()
  {
    return ! isNameColumn() && getValueType(getRowIndex()).equals(ValueType.LINK);
  }
  
  public boolean getIsImageValue()
  {
    return ! isNameColumn() && getValueType(getRowIndex()).equals(ValueType.IMAGE);
  }

  public boolean getIsTextListValue()
  {
    return ! isNameColumn() && getValueType(getRowIndex()).equals(ValueType.TEXT_LIST);
  }

  public boolean getIsLinkListValue()
  {
    return ! isNameColumn() && getValueType(getRowIndex()).equals(ValueType.LINK_LIST);
  }

  public Map<String,String> getLink()
  {
    return _linkMapper;
  }


  // public action command methods & action listeners

  /**
   * Perform the action for clicking on the current cell. Return the navigation rule to go
   * along with the action for clicking on the current cell. This method is only called when
   * {@link #getIsLinkValue()} is true.
   * 
   * @return the navigation rule to go along with the action for clicking on the current cell 
   */
  @SuppressWarnings("unchecked")
  public Object action()
  {
    return getAction(getRowIndex(), (String) getRequestParameter("actionValue"));
  }


  // abstract public methods
  
  /**
   * Get the number of rows in the table.
   * @return the number of rows in the table
   */
  abstract public int getNumRows();
  
  /**
   * Get the description for the row with the given index. Used for mouse-over help-text.
   * @param index the index of the table row
   * @return the description for the row
   */
  abstract public String getDescription(int index);
  
  /**
   * Get the name (left-column value) for the row with the given index.
   * @param index the index of the table row
   * @return the name for the row
   */
  abstract public String getName(int index);
  
  /**
   * Get the value type (the type of the right-column) for the row with the given index.
   * @param index the index of the table row
   * @return the value type for the row
   */
  abstract public ValueType getValueType(int index);
  
  /**
   * Get the display value for the row with the given index. Normally a String value, this
   * should return a list of strings when {@link #getValueType(int)} returns {@link
   * ValueType#TEXT_LIST} or {@link ValueType#LINK_LIST}.
   * @param index the index of the table row
   * @return the display value for the row
   */
  abstract public Object getValue(int index);
  
  /**
   * Perform the action, and return the action code, associated with the given row index and
   * value. The value parameter is supplied to differentiate between the different elements of
   * the currently non-existent COMMAND_LIST {@link ValueType}.
   * @param index the index of the table row
   * @param value the value of the list element in the table row
   * @return the action for the given row and value
   */
  abstract public String getAction(int index, String value);
  
  /**
   * Return the URL link associated with the given row index and value. The value parameter is
   * supplied to differentiate between the different elements of {@link ValueType#LINK_LIST}.
   * @param index the index of the table row
   * @param value the value of the list element in the table row
   * @return the action for the given row and value
   */
  abstract public String getLink(int index, String value);

  
  // private instance methods
  
  private int getRowIndex()
  {
    return getDataModel().getRowIndex();
  }
  
  private boolean isNameColumn()
  {
    return getColumnModel().getRowData().equals(NAME);
  }
}
