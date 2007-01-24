// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
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

import org.apache.log4j.Logger;

/**
 * A combintation of a list of {@link NameValueTable NameValueTables}. The data from each
 * NameValueTable are presented in order, with an empty row between each.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class ComboNameValueTable extends NameValueTable
{
  // static members

  private static Logger log = Logger.getLogger(ComboNameValueTable.class);

  
  // private instance fields
  
  private NameValueTable [] _comboTables;
  private int [] _comboTableIndexes;
  
  
  // instance methods
  
  @SuppressWarnings("unchecked")
  protected void initializeComboNameValueTable(NameValueTable [] comboTables)
  {
    List<Object> dataModelContents = new ArrayList<Object>();
    _comboTables = comboTables;
    _comboTableIndexes = new int[comboTables.length + 1];
    int currentIndex = 0;
    int i = 0;
    while (i < comboTables.length) {
      _comboTableIndexes[i] = currentIndex;
      currentIndex += comboTables[i].getNumRows() + 1;
      dataModelContents.addAll((List<Object>) comboTables[i].getDataModel().getWrappedData());
      i ++;
    }
    _comboTableIndexes[i] = -- currentIndex; // the -- prevents a blank line at the end of the table
    setDataModel(new ListDataModel(dataModelContents));
  }
  
  protected String getAction(int index, String value)
  {
    int comboTableIndex = getComboTableIndexFromRowIndex(index);
    if (comboTableIndex == -1) {
      return null;
    }
    int adjustedRowIndex = index - _comboTableIndexes[comboTableIndex];
    return _comboTables[comboTableIndex].getAction(adjustedRowIndex, value);
  }

  protected String getLink(int index, String value)
  {
    int comboTableIndex = getComboTableIndexFromRowIndex(index);
    if (comboTableIndex == -1) {
      return null;
    }
    int adjustedRowIndex = index - _comboTableIndexes[comboTableIndex];
    return _comboTables[comboTableIndex].getLink(adjustedRowIndex, value);
  }

  public String getName(int index)
  {
    int comboTableIndex = getComboTableIndexFromRowIndex(index);
    if (comboTableIndex == -1) {
      return null;
    }
    int adjustedRowIndex = index - _comboTableIndexes[comboTableIndex];
    return _comboTables[comboTableIndex].getName(adjustedRowIndex);
  }

  public int getNumRows()
  {
    return _comboTableIndexes[_comboTableIndexes.length - 1];
  }

  protected Object getValue(int index)
  {
    int comboTableIndex = getComboTableIndexFromRowIndex(index);
    if (comboTableIndex == -1) {
      return "&nbsp;";
    }
    int adjustedRowIndex = index - _comboTableIndexes[comboTableIndex];
    return _comboTables[comboTableIndex].getValue(adjustedRowIndex);
  }

  protected ValueType getValueType(int index)
  {
    int comboTableIndex = getComboTableIndexFromRowIndex(index);
    if (comboTableIndex == -1) {
      return ValueType.UNESCAPED_TEXT;
    }
    int adjustedRowIndex = index - _comboTableIndexes[comboTableIndex];
    return _comboTables[comboTableIndex].getValueType(adjustedRowIndex);
  }
  
  /**
   * Return the index of the combo table that this row index indexes into. Return -1 if this row
   * index hits a blank row between two combo tables.
   * @param rowIndex
   * @return
   */
  private int getComboTableIndexFromRowIndex(int rowIndex)
  {
    for (int tableIndex = 0; tableIndex < _comboTableIndexes.length - 1; tableIndex ++) {
      int startRowIndexForComboTable = _comboTableIndexes[tableIndex];
      int endRowIndexForComboTable = _comboTableIndexes[tableIndex + 1] - 1;
      if (rowIndex >= startRowIndexForComboTable) {
        if (rowIndex < endRowIndexForComboTable) {
          return tableIndex;
        }
        if (rowIndex == endRowIndexForComboTable) {
          return -1;
        }
      }
    }
    throw new IndexOutOfBoundsException(Integer.toString(rowIndex));
  }
}
