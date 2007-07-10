// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.ui.util.UISelectOneBean;

import org.apache.log4j.Logger;

public abstract class TableSortManager implements Observer
{
  // static members

  private static Logger log = Logger.getLogger(TableSortManager.class);


  // instance data members

  private List<String> _columnNames;
  private DataModel _columnModel;
  private UISelectOneBean<SortDirection> _sortDirection;
  private UISelectOneBean<String> _sortColumn;
  
  
  // public constructors and methods
  
  public TableSortManager(List<String> columnNames)
  {
    _columnNames = columnNames;
    _columnModel = new ListDataModel(_columnNames);
  }

  /**
   * Get the current sort column name.
   * 
   * @motivation allow sort column to be set from a drop-down list UI component
   *             (in addition to clicking on table column headers)
   * @return the current sort column name
   */
  public String getSortColumnName()
  {
    return getSortColumnSelector().getSelection();
  }

  public int getSortColumnIndex()
  {
    return getColumnNames().indexOf(getSortColumnName());
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
   * Get the name of the column currently being rendered by JSF.
   * @return
   */
  public String getCurrentColumnName()
  {
    return getColumnNames().get(_columnModel.getRowIndex());
  }

  /**
   * Set the current sort column name.
   * 
   * @motivation allow sort column to be set from a drop-down list UI component
   *             (in addition to clicking on table column headers)
   * @param currentSortColumnName the new current sort column name
   */
  public void setSortColumnName(String currentSortColumnName)
  {
    if (!getSortColumnName().equals(currentSortColumnName)) {
      getSortColumnSelector().setSelection(currentSortColumnName);
    }
  }

  /**
   * Called by dataTable JSF component.
   * @param sortAscending true if new sort direction is ascending; false if descending
   */
  public void setSortAscending(boolean sortAscending)
  {
    if (sortAscending) {
      setSortDirection(SortDirection.ASCENDING);
    }
    else {
      setSortDirection(SortDirection.DESCENDING);
    }
  }
  
  /**
   * Called by dataTable JSF component.
   * @return true if current sort direction is ascending; false if descending
   */
  public boolean isSortAscending()
  {
    return getSortDirection().equals(SortDirection.ASCENDING);
  }
    
  /**
   * Get the current sort direction.
   * 
   * @motivation allow sort direction to be set from a drop-down list UI
   *             component (in addition to clicking on table column headers)
   * @return the current sort column name
   */
  public SortDirection getSortDirection()
  {
    return getSortDirectionSelector().getSelection();
  }

  /**
   * Set the current sort direction.
   * 
   * @motivation allow sort direction to be set from a drop-down list UI
   *             component (in addition to clicking on table column headers)
   * @param currentSortDirection the new current sort direction
   */
  public void setSortDirection(SortDirection currentSortDirection)
  {
    if (!getSortDirection().equals(currentSortDirection)) {
      getSortDirectionSelector().setSelection(currentSortDirection);
    }
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
    if (!columnNames.contains(getSortColumnName())) {
      getSortColumnSelector().setSelectionIndex(0);
      getSortDirectionSelector().setSelection(SortDirection.ASCENDING);
      sortChanged(getSortColumnName(), getSortDirection());
    }
  }

  public List<String> getColumnNames()
  {
    return _columnNames;
  }
  
  /**
   * Get a list of SelectItem objects for the set of columns that can be sorted
   * on.
   * 
   * @return list of SelectItem objects for the set of columns that can be
   *         sorted on
   */
  public UISelectOneBean<String> getSortColumnSelector()
  {
    if (_sortColumn == null) {
      _sortColumn = new UISelectOneBean<String>(getColumnNames());
      _sortColumn.addObserver(this);
    }
    return _sortColumn;
  }

  /**
   * Get a list of SelectItem objects for the set of sort directions (ascending,
   * descending).
   * 
   * @return list of SelectItem objects for the set of sort directions
   *         (ascending, descending)
   */
  public UISelectOneBean<SortDirection> getSortDirectionSelector()
  {
    if (_sortDirection == null) {
      _sortDirection = new SortDirectionSelector();
      _sortDirection.addObserver(this);
    }
    return _sortDirection;
  }

  
  // Observer methods

  public void update(Observable o, Object arg)
  {
    sortChanged(getSortColumnName(), getSortDirection());
  }

  
  // abstract methods

  abstract protected void sortChanged(String newSortColumnName, SortDirection newSortDirection);

  
  // private methods
  
}
