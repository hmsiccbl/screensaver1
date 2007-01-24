// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
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
 * A name-value table! TODO: add some documentation around here somewhere.
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
   * Workaround for JSF suckiness.
   */
  private final Map<String,String> _linkMapper = new HashMap<String,String>() {
    private static final long serialVersionUID = 1L;
    public String get(Object key)
    {
      String linkValue = (String) key;
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

  public Object getCellValue()
  {
    if (isNameColumn()) {
      return getName(getRowIndex());
    }
    return getValue(getRowIndex());
  }

  public boolean getIsTextValue()
  {
    return ! isNameColumn() && getValueType(getRowIndex()).equals(ValueType.TEXT);
  }
  
  public boolean getIsNameOrUnescapedTextValue()
  {
    return isNameColumn() || getValueType(getRowIndex()).equals(ValueType.UNESCAPED_TEXT);
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
   * {@link #getIsCommandLink()} is true.
   * 
   * @return the navigation rule to go along with the action for clicking on the current cell 
   */
  @SuppressWarnings("unchecked")
  public Object action()
  {
    return getAction(getRowIndex(), (String) getRequestParameter("actionValue"));
  }


  // abstract public and private methods
  
  abstract public int getNumRows();
  
  abstract public String getName(int index);
  
  abstract protected ValueType getValueType(int index);
  
  abstract protected Object getValue(int index);
  
  abstract protected String getAction(int index, String value);
  
  abstract protected String getLink(int index, String value);

  
  // protected instance methods
  
  protected int getRowIndex()
  {
    return getDataModel().getRowIndex();
  }
  
  protected boolean isNameColumn()
  {
    return getColumnModel().getRowData().equals(NAME);
  }
}
