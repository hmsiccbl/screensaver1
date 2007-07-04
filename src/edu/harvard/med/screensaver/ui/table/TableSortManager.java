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
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.ui.util.JSFUtils;

import org.apache.log4j.Logger;

public abstract class TableSortManager
{
  // static members

  private static Logger log = Logger.getLogger(TableSortManager.class);


  // instance data members

  private List<String> _columnNames;
  private DataModel _columnModel;
  private String _currentSortColumnName;
  private SortDirection  _currentSortDirection = SortDirection.ASCENDING;
  
  
  // public constructors and methods
  
  public TableSortManager(List<String> columnNames)
  {
    _columnNames = columnNames;
    _columnModel = new ListDataModel(_columnNames);
    _currentSortColumnName = _columnNames.get(0);
  }

  /**
   * Get the current sort column name.
   * 
   * @motivation allow sort column to be set from a drop-down list UI component
   *             (in addition to clicking on table column headers)
   * @return the current sort column name
   */
  public String getCurrentSortColumnName()
  {
    return _currentSortColumnName;
  }

  public int getCurrentSortColumnIndex()
  {
    return getColumnNames().indexOf(getCurrentSortColumnName());
  }
  
  /**
   * Get the index of the column currently being rendered by JSF.
   * @return
   */
  public int getCurrentColumnIndex()
  {
    return _columnModel.getRowIndex();
  }

  /**
   * Set the current sort column name.
   * 
   * @motivation allow sort column to be set from a drop-down list UI component
   *             (in addition to clicking on table column headers)
   * @param currentSortColumnName the new current sort column name
   */
  public void setCurrentSortColumnName(String currentSortColumnName)
  {
    if (!_currentSortColumnName.equals(currentSortColumnName)) {
      _currentSortColumnName = currentSortColumnName;
      sortChanged(_currentSortColumnName, _currentSortDirection);
    }
  }

  /**
   * Called by dataTable JSF component.
   * @param sortAscending true if new sort direction is ascending; false if descending
   */
  public void setSortAscending(boolean sortAscending)
  {
    if (sortAscending) {
      setCurrentSortDirection(SortDirection.ASCENDING);
    }
    else {
      setCurrentSortDirection(SortDirection.DESCENDING);
    }
  }
  
  /**
   * Called by dataTable JSF component.
   * @return true if current sort direction is ascending; false if descending
   */
  public boolean isSortAscending()
  {
    return getCurrentSortDirection().equals(SortDirection.ASCENDING);
  }
    
  /**
   * Get the current sort direction.
   * 
   * @motivation allow sort direction to be set from a drop-down list UI
   *             component (in addition to clicking on table column headers)
   * @return the current sort column name
   */
  public SortDirection getCurrentSortDirection()
  {
    return _currentSortDirection;
  }

  /**
   * Set the current sort direction.
   * 
   * @motivation allow sort direction to be set from a drop-down list UI
   *             component (in addition to clicking on table column headers)
   * @param currentSortDirection the new current sort direction
   */
  public void setCurrentSortDirection(SortDirection currentSortDirection)
  {
    if (!_currentSortDirection.equals(currentSortDirection)) {
      _currentSortDirection = currentSortDirection;
      sortChanged(_currentSortColumnName, _currentSortDirection);
    }
  }

  /**
   * Get a list of SelectItem objects for the set of columns that can be sorted
   * on.
   * 
   * @return list of SelectItem objects for the set of columns that can be
   *         sorted on
   */
  public List<SelectItem> getSortColumnSelections()
  {
    List<String> selections = new ArrayList<String>();
    for (String columnName : _columnNames) {
      selections.add(columnName);
    }
    return JSFUtils.createUISelectItems(selections);
  }

  /**
   * Get a list of SelectItem objects for the set of sort directions (ascending,
   * descending).
   * 
   * @return list of SelectItem objects for the set of sort directions
   *         (ascending, descending)
   */
  public List<SelectItem> getSortDirectionSelections()
  {
    List<SelectItem> selections = new ArrayList<SelectItem>();
    for (SortDirection sortOrder : SortDirection.values()) {
      selections.add(new SelectItem(sortOrder,
                                    sortOrder.toString()));
    }
    return selections;
  }

  /**
   * Get the data header column model.
   * @return the data header column model
   */
  public DataModel getColumnModel()
  {
    return _columnModel;
  }

  /**
   * Set the data header column model.
   * @param dataHeaderColumnModel the data header column model
   */
  public void setColumnNames(List<String> columnNames)
  {
    _columnNames = columnNames;
    _columnModel = new ListDataModel(columnNames);
    if (!columnNames.contains(_currentSortColumnName)) {
      _currentSortColumnName = columnNames.get(0);
      _currentSortDirection = SortDirection.ASCENDING;
      sortChanged(_currentSortColumnName, _currentSortDirection);
    }
  }

  public List<String> getColumnNames()
  {
    return _columnNames;
  }

  
  // public action command methods & action listeners


  // abstract methods

  abstract protected void sortChanged(String newSortColumnName, SortDirection newSortDirection);

  
  // private methods
  
}

