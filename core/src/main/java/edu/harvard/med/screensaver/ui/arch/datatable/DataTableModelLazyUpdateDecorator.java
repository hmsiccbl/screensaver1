// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.arch.datatable;

import java.util.Iterator;
import java.util.List;

import javax.faces.model.DataModelListener;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TableColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.model.DataTableModel;

/**
 * Delays the actual invocation of {@link #fetch}, {@link #sort} and
 * {@link #filter} until an accessor (getter) method is called that returns
 * data. This allows the above methods to be called multiple times without
 * repeatedly incurring the expense of their respective operations (assuming no
 * accessor method calls are interleaved). Also ensures that dependency and
 * ordering of these calls is correct and optimal.
 *
 * @motivation DataTable's Observables can notify DataTable of changes multiple
 *             times, without causing fetch, sort, and filter to be invoked
 *             unnecessarily
 * @author drew
 */
public class DataTableModelLazyUpdateDecorator<R> extends DataTableModel<R>
{

  private static final Logger log = Logger.getLogger(DataTableModelLazyUpdateDecorator.class);

  private DataTableModel<R> _base;
  private boolean _fetchNeeded;
  private boolean _filterNeeded;
  private boolean _sortNeeded;
  private List<? extends TableColumn<R,?>> _fetchColumns;
  private List<? extends TableColumn<R,?>> _filterColumns;
  private List<? extends TableColumn<R,?>> _sortColumns;
  private SortDirection _sortDirection;

  public DataTableModelLazyUpdateDecorator(DataTableModel<R> base)
  {
    _base = base;
  }

  @Override
  public void fetch(List<? extends TableColumn<R,?>> fetchColumns)
  {
    _fetchColumns = fetchColumns;
    _fetchNeeded = true;
    log.debug("lazy refetch() called (will execute later)");
  }

  @Override
  public void sort(List<? extends TableColumn<R,?>> sortColumns,
                   SortDirection sortDirection)
  {
    _sortColumns = sortColumns;
    _sortDirection = sortDirection;
    _sortNeeded = true;
    log.debug("lazy sort() called (will execute later)");
  }

  @Override
  public void filter(List<? extends TableColumn<R,?>> columns)
  {
    _filterColumns = columns;
    _filterNeeded = true;
    log.debug("lazy filter() called (will execute later)");
  }

  @Override
  public int getRowCount()
  {
    lazyUpdate();
    return _base.getRowCount();
  }

  @Override
  public Object getRowData()
  {
    lazyUpdate();
    return _base.getRowData();
  }

  @Override
  public int getRowIndex()
  {
    lazyUpdate();
    return _base.getRowIndex();
  }

  @Override
  public Object getWrappedData()
  {
    lazyUpdate();
    return _base.getWrappedData();
  }

  @Override
  public boolean isRowAvailable()
  {
    lazyUpdate();
    return _base.isRowAvailable();
  }

  @Override
  public void setRowIndex(int rowIndex)
  {
    _base.setRowIndex(rowIndex);
  }

  @Override
  public void setWrappedData(Object data)
  {
    _base.setWrappedData(data);
  }

  @Override
  public void addDataModelListener(DataModelListener listener)
  {
    _base.addDataModelListener(listener);
  }

  @Override
  public DataModelListener[] getDataModelListeners()
  {
    return _base.getDataModelListeners();
  }

  @Override
  public void removeDataModelListener(DataModelListener listener)
  {
    _base.removeDataModelListener(listener);
  }

  @Override
  public DataTableModelType getModelType()
  {
    return _base.getModelType();
  }

  // private methods

  private void lazyUpdate()
  {
    // For InMemoryDataModel, if a fetch is needed, we must be sure to
    // also refilter. If a refilter is needed, we must be sure to also resort,
    // since InMemoryDataModel performs the refiltering on unsorted data
    // HACK: Arguably, InMemoryDataModel should handle this dependency, but it's 
    // actually more efficient to do so here, since we avoid the possibility 
    // of executing redundant filter and sort operations
    if (_base.getModelType() == DataTableModelType.IN_MEMORY) {
      if (_fetchNeeded) {
        if (_filterColumns != null) {
          _filterNeeded = true;
        }
      }
      if (_filterNeeded) {
        if (_sortColumns != null) {
          _sortNeeded = true;
        }
      }
    }

    if (_fetchNeeded) {
      log.debug("executing lazy fetch() now");
      _base.fetch(_fetchColumns);
      _fetchNeeded = false;
    }
    if (_filterNeeded) {
      _filterNeeded = false;
      log.debug("executing lazy filter() now");
      _base.filter(_filterColumns);
    }
    if (_sortNeeded) {
      _sortNeeded = false;
      log.debug("executing lazy sort() now");
      _base.sort(_sortColumns, _sortDirection);
    }
  }

  @Override
  public Iterator<R> iterator()
  {
    return _base.iterator();
  }
}
