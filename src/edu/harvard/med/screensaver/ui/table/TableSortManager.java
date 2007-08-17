// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.ui.util.UISelectOneBean;

import org.apache.log4j.Logger;

public class TableSortManager<E> extends Observable implements Observer 
{
  // static members

  private static Logger log = Logger.getLogger(TableSortManager.class);


  // instance data members

  private VisibleTableColumnModel<E> _columnModel;
  private UISelectOneBean<SortDirection> _sortDirection;
  private UISelectOneBean<TableColumn<E>> _sortColumn;
  private Map<TableColumn<E>,Map<SortDirection,Comparator<E>>> _comparators = new HashMap<TableColumn<E>,Map<SortDirection,Comparator<E>>>();
  
  
  // public constructors and methods
  
  public TableSortManager(List<TableColumn<E>> columns)
  {
    setColumns(columns);
  }

  /**
   * Get the current sort column.
   * 
   * @motivation allow sort column to be set from a drop-down list UI component
   *             (in addition to clicking on table column headers)
   * @return the current sort column
   */
  public TableColumn<E> getSortColumn()
  {
    return getSortColumnSelector().getSelection();
  }
  
  /**
   * Get a comparator that can be used to sort rows. If there exists a compound
   * sorting order for the primary sort column, the comparator will take this
   * into account.
   * 
   * @return the sort columns
   */
  public Comparator<E> getSortColumnComparator()
  {
    Map<SortDirection,Comparator<E>> comparator = _comparators.get(getSortColumn());
    if (comparator != null) {
      // return the compound column sort comparator
      return comparator.get(getSortDirection());
    }
    // return the single-column sort comparator
    return getSortColumn().getComparator(getSortDirection());
  }
  
  public void addCompoundSortColumns(List<TableColumn<E>> compoundSortColumns)
  {
    Map<SortDirection,Comparator<E>> comparators = new HashMap<SortDirection,Comparator<E>>(2);
    comparators.put(SortDirection.ASCENDING, new CompoundColumnComparator<E>(compoundSortColumns, SortDirection.ASCENDING));
    comparators.put(SortDirection.DESCENDING, new CompoundColumnComparator<E>(compoundSortColumns, SortDirection.DESCENDING));
    _comparators.put(compoundSortColumns.get(0), comparators);
  }

  public int getSortColumnIndex()
  {
    return getSortColumnSelector().getSelectionIndex();
  }
  
  /**
   * Get the index of the column currently being rendered by JSF.
   */
  public int getCurrentColumnIndex()
  {
    return getColumnModel().getRowIndex();
  }

  /**
   * Get the column currently being rendered by JSF.
   */
  @SuppressWarnings("unchecked")
  public TableColumn<E> getCurrentColumn()
  {
    return (TableColumn<E>) getColumnModel().getRowData();
  }
  
  public TableColumn<E> getColumn(int i)
  {
    return getColumns().get(i);
  }

  /**
   * Set the current sort column.
   * 
   * @motivation allow sort column to be set from a drop-down list UI component
   *             (in addition to clicking on table column headers)
   * @param currentSortColumn the new current sort column 
   */
  public void setSortColumn(TableColumn<E> currentSortColumn)
  {
    if (!getSortColumn().equals(currentSortColumn)) {
      getSortColumnSelector().setSelection(currentSortColumn);
    }
  }
  
  /**
   * @motivation for use by dataTable JSF component.
   * @param sortColumnName the name of the new sort column
   */
  public void setSortColumnName(String sortColumnName)
  {
    setSortColumn(getColumnModel().getColumn(sortColumnName));
  }
  
  /**
   * @motivation for use by dataTable JSF component.
   * @return true the name of the current sort column
   */
  public String getSortColumnName()
  {
    return getSortColumn().getName();
  }

  /**
   * @motivation for use by dataTable JSF component.
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
   * @motivation for use by dataTable JSF component.
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
  public VisibleTableColumnModel<E> getColumnModel()
  {
    return _columnModel;
  }

  /**
   * Set the columns.
   * 
   * @param columns the new list columns
   */
  public void setColumns(List<TableColumn<E>> columns)
  {
    _columnModel = new VisibleTableColumnModel<E>(columns) {
      /**
       * Intercept call to VisibleTableColumnModel.updateVisibleColumns(), so
       * that we can update our _sortColumn UISelectOneBean.
       */
      @Override
      public void updateVisibleColumns()
      {
        super.updateVisibleColumns();
        if (_sortColumn != null) {
          TableColumn<E> sortColumn = _sortColumn.getSelection();
          _sortColumn = null; // force recreate, based upon new set of visible columns
          UISelectOneBean<TableColumn<E>> sortColumnSelector = getSortColumnSelector(); // recreate now
          // re-select previous selection, if it still exists
          if (sortColumn.isVisible()) {
            sortColumnSelector.setSelection(sortColumn);
          }
        }
      }
    };
    // ensure sort column exists in the new set of columns
    if (!columns.contains(getSortColumn())) {
      getSortColumnSelector().setSelectionIndex(0);
      getSortDirectionSelector().setSelection(SortDirection.ASCENDING);
    }
  }
  
  @SuppressWarnings("unchecked")
  public List<TableColumn<E>> getColumns()
  {
    return (List<TableColumn<E>>) getColumnModel().getWrappedData();
  }

  /**
   * Get a list of SelectItem objects for the set of columns that can be sorted
   * on.
   * 
   * @return list of SelectItem objects for the set of columns that can be
   *         sorted on
   */
  public UISelectOneBean<TableColumn<E>> getSortColumnSelector()
  {
    if (_sortColumn == null) {
      _sortColumn = new UISelectOneBean<TableColumn<E>>(getColumns()) {
        @Override
        protected String getLabel(TableColumn<E> t) { return t.getName(); }
      };
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
    SortChangedEvent<E> sortChangedEvent = null;
    if (o == _sortColumn) {
      sortChangedEvent = new SortChangedEvent<E>(getSortColumn()); 
      setChanged();
    }
    else if (o == _sortDirection) {
      sortChangedEvent = new SortChangedEvent<E>(getSortDirection()); 
      setChanged();
    }
    if (sortChangedEvent != null) {
      notifyObservers(sortChangedEvent);
    }
  }


  // private methods
  
}
