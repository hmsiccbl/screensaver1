// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.namevaluetable;

import java.util.HashMap;
import java.util.Map;

import javax.faces.model.DataModel;

import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.libraries.AnnotationNameValueTable;
import edu.harvard.med.screensaver.ui.util.HtmlUtils;

import org.apache.log4j.Logger;


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


  // private instance data

  private DataModel _dataModel;

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
  }


  // public getters and setters - used by nameValueTable.jspf

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
  
  /**
   * Return true if this NameValueTable has grouping, or summary info for sets of rows (see
   * the AnnotationNameValue table).
   * @return true if there is group info available
   * @see #getGroupingValueTable()
   */
  public boolean getHasGroupingInfo()
  {
    return false;
  }
  
  /**
   * If there is grouping information for this table and set of rows, return the DataModel for 
   * this grouping information.  This DataModel is then used to create a subtable of summary
   * information.
   * @return the DataModel for the grouping/summary information
   * @see AnnotationNameValueTable
   */
  public DataModel getGroupingValueTable()
  {
    return null;
  }
  
  public String getGroupingFooter()
  {
    return null;
  }
  
  public Object getGroupId()
  {
    return null;
  }

  public String getNameDescription()
  {
    return getDescription(getRowIndex());
  }

  public Object getLabel()
  {
    return HtmlUtils.toNonBreakingSpaces(getName(getRowIndex()));
  }

  public Object getValue()
  {
    return getValue(getRowIndex());
  }

  public boolean getIsTextValue()
  {
    return getValueType(getRowIndex()).equals(ValueType.TEXT);
  }

  public boolean getIsUnescapedTextValue()
  {
    return getValueType(getRowIndex()).equals(ValueType.UNESCAPED_TEXT);
  }

  public boolean getIsCommandValue()
  {
    return getValueType(getRowIndex()).equals(ValueType.COMMAND);
  }

  public boolean getIsLinkValue()
  {
    return getValueType(getRowIndex()).equals(ValueType.LINK);
  }

  public boolean getIsImageValue()
  {
    return getValueType(getRowIndex()).equals(ValueType.IMAGE);
  }

  public boolean getIsTextListValue()
  {
    return getValueType(getRowIndex()).equals(ValueType.TEXT_LIST);
  }

  public boolean getIsLinkListValue()
  {
    return getValueType(getRowIndex()).equals(ValueType.LINK_LIST);
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

  protected int getRowIndex()
  {
    return getDataModel().getRowIndex();
  }
}
