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
import java.util.List;

import javax.faces.model.ListDataModel;

import edu.harvard.med.screensaver.ui.util.HtmlUtils;

import org.apache.log4j.Logger;

/**
 * A combination of a list of {@link NameValueTable NameValueTables}. The data from each
 * NameValueTable are presented in order, with an empty row between each.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
abstract public class ComboNameValueTable extends NameValueTable
{

  // static members

  private static Logger log = Logger.getLogger(ComboNameValueTable.class);


  // private instance fields

  private NameValueTable [] _childTables;
  private int [] _rowIndexToChildTableIndex;
  private int [] _rowIndexToChildTableRowIndex;


  // instance methods

  /**
   * Initialize the ComboNameValueTable with the child tables. This method must be called
   * before any attempt to use this table! You may want to call it in the constructor or any
   * subclass of this class, as in {@link WellNameValueTable}.
   * @param childTables the child tables
   */
  @SuppressWarnings("unchecked")
  protected void initializeComboNameValueTable(NameValueTable [] childTables)
  {
    _childTables = childTables;

    int numRows = countRows(childTables);
    _rowIndexToChildTableIndex = new int[numRows];
    _rowIndexToChildTableRowIndex = new int[numRows];

    // TODO: see if i can do without the setDataModel crap. or maybe I can do it more simply
    // with an Arrays.fill() or something like that. why is this needed? i am managing all the
    // callbacks from here...

    List<Object> dataModelContents = new ArrayList<Object>();
    int parentRowIndex = 0;
    for (int childTableIndex = 0; childTableIndex < _childTables.length; childTableIndex ++) {
      NameValueTable childTable = _childTables[childTableIndex];
      for (int childTableRowIndex = 0; childTableRowIndex < childTable.getNumRows(); childTableRowIndex ++) {
        _rowIndexToChildTableIndex[parentRowIndex] = childTableIndex;
        _rowIndexToChildTableRowIndex[parentRowIndex] = childTableRowIndex;
        parentRowIndex ++;

        dataModelContents.add(childTable.getValue(childTableRowIndex));
      }

      // blank row between child tables
      if (childTableIndex + 1 < _childTables.length) {
        _rowIndexToChildTableIndex[parentRowIndex] = -1;
        _rowIndexToChildTableRowIndex[parentRowIndex] = -1;
        parentRowIndex ++;

        dataModelContents.add("spacer");
      }
    }

    setDataModel(new ListDataModel(dataModelContents));
  }

  @Override
  public int getNumRows()
  {
    return _rowIndexToChildTableIndex.length;
  }

  @Override
  public String getDescription(int rowIndex)
  {
    int childTableIndex = _rowIndexToChildTableIndex[rowIndex];
    if (childTableIndex == -1) {
      return HtmlUtils.NON_BREAKING_SPACE;
    }
    int childTableRowIndex = _rowIndexToChildTableRowIndex[rowIndex];
    return _childTables[childTableIndex].getDescription(childTableRowIndex);
  }

  @Override
  public String getName(int rowIndex)
  {
    int childTableIndex = _rowIndexToChildTableIndex[rowIndex];
    if (childTableIndex == -1) {
      return HtmlUtils.NON_BREAKING_SPACE;
    }
    int childTableRowIndex = _rowIndexToChildTableRowIndex[rowIndex];
    return _childTables[childTableIndex].getName(childTableRowIndex);
  }

  @Override
  public ValueType getValueType(int rowIndex)
  {
    int childTableIndex = _rowIndexToChildTableIndex[rowIndex];
    if (childTableIndex == -1) {
      return ValueType.UNESCAPED_TEXT;
    }
    int childTableRowIndex = _rowIndexToChildTableRowIndex[rowIndex];
    return _childTables[childTableIndex].getValueType(childTableRowIndex);
  }

  @Override
  public Object getValue(int rowIndex)
  {
    int childTableIndex = _rowIndexToChildTableIndex[rowIndex];
    if (childTableIndex == -1) {
      return HtmlUtils.NON_BREAKING_SPACE;
    }
    int childTableRowIndex = _rowIndexToChildTableRowIndex[rowIndex];
    return _childTables[childTableIndex].getValue(childTableRowIndex);
  }

  @Override
  public String getAction(int rowIndex, String value)
  {
    int childTableIndex = _rowIndexToChildTableIndex[rowIndex];
    if (childTableIndex == -1) {
      return null;
    }
    int childTableRowIndex = _rowIndexToChildTableRowIndex[rowIndex];
    return _childTables[childTableIndex].getAction(childTableRowIndex, value);
  }

  @Override
  public String getLink(int rowIndex, String value)
  {
    int childTableIndex = _rowIndexToChildTableIndex[rowIndex];
    if (childTableIndex == -1) {
      return null;
    }
    int childTableRowIndex = _rowIndexToChildTableRowIndex[rowIndex];
    return _childTables[childTableIndex].getLink(childTableRowIndex, value);
  }


  // private instance methods

  private int countRows(NameValueTable [] childTables)
  {
    int numRows = 0;
    for (int i = 0; i < childTables.length; i++) {
      numRows += childTables[i].getNumRows() + 1;
    }
    return Math.max(-- numRows, 0);
  }
}
