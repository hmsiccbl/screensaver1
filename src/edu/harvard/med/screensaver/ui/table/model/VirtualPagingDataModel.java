// $HeadURL:
// svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml
// $
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.db.datafetcher.DataFetcher;
import edu.harvard.med.screensaver.ui.table.DataTableModelType;
import edu.harvard.med.screensaver.ui.util.ValueReference;

import org.apache.log4j.Logger;

/**
 * JSF DataModel class that supports virtual paging (i.e., on-demand fetching of
 * row data).
 * <p>
 * Note that DataModel's wrappedData property is not supported, as virtual
 * paging implies that the underlying data cannot (always) be made fully
 * available.
 * <p>
 * @param <K> the type of the key used to identify a row of data
 * @param <E> the data type containing the data displayed across each row.
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public abstract class VirtualPagingDataModel<K,E> extends DataTableModel<E>
{

  // static members

  private static final int DEFAULT_ROWS_TO_FETCH = 1;


  private static Logger log = Logger.getLogger(VirtualPagingDataModel.class);


  // instance data

  protected DataFetcher<E,K,?> _dataFetcher;
  protected ValueReference<Integer> _rowsToFetch;
  protected int _rowIndex = -1;
  protected List<K> _sortedKeys;
  protected SortDirection _sortDirection;
  protected Map<K,E> _fetchedRows = new HashMap<K,E>();

  // constructors

  protected VirtualPagingDataModel()
  {
  }

  public VirtualPagingDataModel(DataFetcher<E,K,?> dataFetcher,
                                ValueReference<Integer> rowsToFetch)
  {
    _dataFetcher = dataFetcher;
    _rowsToFetch = rowsToFetch;
  }

  // public methods

  @Override
  public DataTableModelType getModelType()
  {
    return DataTableModelType.VIRTUAL_PAGING;
  }

  /**
   * Subclass should call this method to determine how many rows of data need to
   * be fetched in order to populate the visible rows of the data table.
   */
  public int getRowsToFetch()
  {
    return _rowsToFetch.value();
  }

  @Override
  public int getRowIndex()
  {
    return _rowIndex;
  }

  @Override
  public int getRowCount()
  {
    return getFilteredAndSortedKeys().size();
  }

  public int getFilteredRowCount()
  {
    // TODO: implement
    return getRowCount();
  }

  @Override
  public boolean isRowAvailable()
  {
    return _rowIndex < getRowCount() && _rowIndex >= 0;
  }

  @Override
  public void setRowIndex(int rowIndex)
  {
    _rowIndex = rowIndex;
  }

  @Override
  public E getRowData()
  {
    if (!isRowAvailable()) {
      return null;
    }
    doFetchIfNecessary();
    return _fetchedRows.get(getFilteredAndSortedKeys().get(getSortIndex(_rowIndex)));
  }

  @Override
  final public Object getWrappedData()
  {
    throw new UnsupportedOperationException("virtual paging data model cannot provide an object representing the full underlying dataset");
  }

  @Override
  final public void setWrappedData(Object data)
  {
    throw new UnsupportedOperationException("virtual paging data model cannot be provided an object representing the full underlying dataset");
  }


  // private methods

  private int getSortIndex(int rowIndex)
  {
    if (_sortDirection == SortDirection.ASCENDING) {
      return rowIndex;
    }
    return (getRowCount() - rowIndex) - 1;
  }

  private List<K> getFilteredAndSortedKeys()
  {
    if (_sortedKeys == null) {
      _sortedKeys = _dataFetcher.findAllKeys();
    }
    return _sortedKeys;
  }

  private void doFetchIfNecessary()
  {
    if (isRowAvailable()) {
      if (!_fetchedRows.containsKey(getFilteredAndSortedKeys().get(getSortIndex(_rowIndex)))) {
        Map<K,E> data = _dataFetcher.fetchData(getUnfetchedKeysBatch());
        cacheFetchedData(data);
      }
    }
  }

  private Set<K> getUnfetchedKeysBatch()
  {
    Set<K> keys = new HashSet<K>();
    int from = _rowIndex;
    int to = Math.min(_rowIndex + getRowsToFetch(), getRowCount());
    for (int i = from; i < to; ++i) {
      K key = getFilteredAndSortedKeys().get(getSortIndex(i));
      if (!_fetchedRows.containsKey(key)) {
        keys.add(key);
      }
    }
    if (log.isDebugEnabled()) {
      log.debug("need to fetch keys " + keys + " for rows " + from + ".." + to);
    }
    return keys;
  }

  private void cacheFetchedData(Map<K,E> fetchedData)
  {
    _fetchedRows.putAll(fetchedData);
    if (log.isDebugEnabled()) {
      log.debug("fetched " + fetchedData.size() + " rows: " + _rowIndex +
                " to " + ((_rowIndex + fetchedData.size()) - 1));
    }
  }
}
