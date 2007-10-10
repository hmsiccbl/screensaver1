// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table;

import java.util.ArrayList;
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
  private static final Comparator<Object> PHYSICAL_ORDER_COMPARATOR = new Comparator<Object>() {
    public int compare(Object o1, Object o2){ return 0; }
  };


  // static members

  private static Logger log = Logger.getLogger(TableSortManager.class);


  // instance data members

  private VisibleTableColumnModel<E> _columnModel;
  private UISelectOneBean<SortDirection> _sortDirectionSelector;
  private UISelectOneBean<TableColumn<E>> _sortColumnSelector;
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
  @SuppressWarnings("unchecked")
  public Comparator<E> getSortColumnComparator()
  {
    if (getSortColumn() == null) {
      return (Comparator<E>) PHYSICAL_ORDER_COMPARATOR;
    }
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
    if (comparators.size() > 0) {
      _comparators.put(compoundSortColumns.get(0), comparators);
    }
  }

  public void addCompoundSortColumns(Integer[] compoundSortIndexes)
  {
    List<TableColumn<E>> compoundSortColumns = new ArrayList<TableColumn<E>>();
    for (Integer colIndex : compoundSortIndexes) {
      compoundSortColumns.add(getColumns().get(colIndex));
    }
    addCompoundSortColumns(compoundSortColumns);
  }

  public void addAllCompoundSorts(List<List<TableColumn<E>>> allCompoundSorts)
  {
    for (List<TableColumn<E>> compoundSort : allCompoundSorts) {
      addCompoundSortColumns(compoundSort);
    }
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
  public void setSortColumn(TableColumn<E> newSortColumn)
  {
    if (newSortColumn != null) {
      if (!newSortColumn.equals(getSortColumn())) {
        getSortColumnSelector().setSelection(newSortColumn);
      }
    }
  }

  /**
   * @motivation for use by dataTable JSF component.
   * @param sortColumnName the name of the new sort column
   */
  public void setSortColumnName(String sortColumnName)
  {
    if (sortColumnName != null) {
      setSortColumn(getColumnModel().getColumn(sortColumnName));
    }
    else {
      setSortColumn(null);
    }
  }

  /**
   * @motivation for use by dataTable JSF component.
   * @return true the name of the current sort column
   */
  public String getSortColumnName()
  {
    if (getSortColumn() == null) {
      return null;
    }
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

  public void setColumns(List<TableColumn<E>> columns)
  {
    _columnModel = new VisibleTableColumnModel<E>(columns) {
      /**
       * Intercept call to VisibleTableColumnModel.updateVisibleColumns(), so
       * that we can also update our _sortColumnSelector UISelectOneBean.
       */
      @Override
      public void updateVisibleColumns()
      {
        super.updateVisibleColumns();
        if (_sortColumnSelector != null) {
          TableColumn<E> sortColumn = _sortColumnSelector.getSelection();
          _sortColumnSelector = null; // force recreate, based upon new set of visible columns
          UISelectOneBean<TableColumn<E>> sortColumnSelector = getSortColumnSelector(); // recreate now
          // re-select previous selection, if it still exists
          if (sortColumn.isVisible()) {
            sortColumnSelector.setSelection(sortColumn);
          }
        }
      }
    };
    // ensure sort column exists in the new set of columns
    if (columns.size() > 0 && !columns.contains(getSortColumn())) {
      getSortColumnSelector().setSelectionIndex(0);
      getSortDirectionSelector().setSelection(SortDirection.ASCENDING);
    }
  }

  @SuppressWarnings("unchecked")
  public List<TableColumn<E>> getColumns()
  {
    return (List<TableColumn<E>>) getColumnModel().getWrappedData();
  }

  public UISelectOneBean<TableColumn<E>> getSortColumnSelector()
  {
    if (_sortColumnSelector == null) {
      _sortColumnSelector = new UISelectOneBean<TableColumn<E>>(getColumns()) {
        @Override
        protected String getLabel(TableColumn<E> t) { return t.getName(); }
      };
      _sortColumnSelector.addObserver(this);
    }
    return _sortColumnSelector;
  }

  public UISelectOneBean<SortDirection> getSortDirectionSelector()
  {
    if (_sortDirectionSelector == null) {
      _sortDirectionSelector = new SortDirectionSelector();
      _sortDirectionSelector.addObserver(this);
    }
    return _sortDirectionSelector;
  }


  // Observer methods

  public void update(Observable o, Object arg)
  {
    SortChangedEvent<E> sortChangedEvent = null;
    if (o == _sortColumnSelector) {
      sortChangedEvent = new SortChangedEvent<E>(getSortColumn());
      setChanged();
    }
    else if (o == _sortDirectionSelector) {
      sortChangedEvent = new SortChangedEvent<E>(getSortDirection());
      setChanged();
    }
    if (sortChangedEvent != null) {
      notifyObservers(sortChangedEvent);
    }
  }


  // private methods

}
