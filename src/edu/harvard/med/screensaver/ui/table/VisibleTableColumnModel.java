// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.DataModel;

import org.apache.log4j.Logger;

/**
 * @motivation Manager table column visibility. 
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class VisibleTableColumnModel<E> extends DataModel
{
  // static members

  private static Logger log = Logger.getLogger(VisibleTableColumnModel.class);


  // instance data members
  
  private List<TableColumn<E>> _columns;
  private List<String> _visibleColumnNames;
  private List<TableColumn<E>> _visibleColumns;
  private List<Integer> _baseIndexMap;
  private int _visibleColumnIndex = -1;

  
  // public constructors and methods
  
  public VisibleTableColumnModel(List<TableColumn<E>> columns)
  {
    setWrappedData(columns);
  }

  @Override
  public int getRowCount()
  {
    return _visibleColumns.size();
  }

  @Override
  public TableColumn<E> getRowData()
  {
    if (isRowAvailable()) { 
      return _visibleColumns.get(_visibleColumnIndex);
    }
    return null;
  }

  @Override
  /**
   * @return -1 if no column is visible
   */
  public int getRowIndex()
  {
    return _visibleColumnIndex;
  }

  @Override
  public void setRowIndex(int visibleColumnIndex)
  {
    try {
      _visibleColumnIndex = visibleColumnIndex;
    }
    catch (Exception e) {
      throw new IllegalArgumentException("illegal (visible) column index: " + e.getMessage());
    }
  }

  @Override
  public Object getWrappedData()
  {
    return _visibleColumns;
  }

  @Override
  public boolean isRowAvailable()
  {
    return isColumnAvailable(_visibleColumnIndex);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void setWrappedData(Object columns)
  {
    _columns = (List<TableColumn<E>>) columns;
    updateVisibleColumns();
  }

  public void updateVisibleColumns()
  {
    TableColumn<E> currentColumn = getRowData();
    _visibleColumns = new ArrayList<TableColumn<E>>();
    _baseIndexMap = new ArrayList<Integer>();
    _visibleColumnNames = null;
    int i = 0;
    for (TableColumn<E> column : _columns) {
      if (column.isVisible()) {
        _visibleColumns.add(column);
        _baseIndexMap.add(i);
      }
      ++i;
    }
    
    // preserve current column, if it's still visible
    if (_visibleColumns.size() == 0) {
      _visibleColumnIndex = -1;
    }
    else {
      _visibleColumnIndex = 0;
      for (int j = 0; j < _visibleColumns.size(); j++) {
        TableColumn<E> visibleColumn = _visibleColumns.get(j);
        if (visibleColumn.equals(currentColumn)) {
          _visibleColumnIndex = j;
        }
      }
    }
  }
  
  public List<String> getColumnNames()
  {
    if (_visibleColumnNames == null) {
      _visibleColumnNames = new ArrayList<String>(_columns.size());
      for (TableColumn<E> column : _columns) {
        if (column.isVisible()) {
          _visibleColumnNames.add(column.getName());
        }
      }
    }
    return _visibleColumnNames;
  }

  public TableColumn<E> getColumn(String sortColumnName)
  {
    int i = getColumnNames().indexOf(sortColumnName);
    if (isColumnAvailable(i)) {
      return _visibleColumns.get(i);
    }
    return null; 
  }
  
  // private methods

  private boolean isColumnAvailable(int visibleColumnIndex)
  {
    return visibleColumnIndex >= 0 && visibleColumnIndex < _visibleColumns.size();
  }
}

