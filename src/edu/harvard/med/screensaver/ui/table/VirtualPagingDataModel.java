// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.harvard.med.screensaver.db.SortDirection;

import org.apache.log4j.Logger;

/**
 * Abstract, generic JSF DataModel class that supports virtual paging (i.e.,
 * on-demand fetching of row data). Requires that the total row count can be
 * determined by calling code prior to instantiation.
 * <p>
 * Note that DataModel's wrappedData property is not supported, as virtual
 * paging implies that the underlying data cannot (always) be made fully
 * available.
 * <p>
 * DataModel classes that <i>may</i> require virtual paging in the future are
 * encouraged to extend this class from their outset, as this will simplify its
 * migration to a paging scheme in the future, as needed. To fetch all data at
 * once, simply implement {@link #fetchData(int, int)} to fetch all data rows
 * whenever it is called.
 *
 * @param <K> the type of the key used to identify a row of data
 * @param <V> the data type containing the data displayed across each row.
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public abstract class VirtualPagingDataModel<K,V> extends SortableDataModel<V>
{

  // static members

  private static Logger log = Logger.getLogger(VirtualPagingDataModel.class);


  // instance data

  private int _rowsToFetch;
  private int _totalRowCount;
  protected int _rowIndex;
  private TableColumn<V,?> _sortColumn;
  private SortDirection _sortDirection;
  private List<K> _sortedKeys;
  private Map<K,V> _fetchedRows = new HashMap<K,V>();


  // constructors

  protected VirtualPagingDataModel()
  {
  }

  protected VirtualPagingDataModel(int rowsToFetch,
                                   int totalRowCount,
                                   TableColumn<V,?> sortColumn,
                                   SortDirection sortDirection)
  {
    if (rowsToFetch < 0) {
      throw new IllegalArgumentException("rowsToFetch >= 0");
    }
    if (totalRowCount < 0) {
      throw new IllegalArgumentException("totalRowCount >= 0");
    }
    _rowsToFetch = rowsToFetch;
    _totalRowCount = totalRowCount;
    _sortColumn = sortColumn;
    _sortDirection = sortDirection;
  }

  // abstract methods

  abstract protected List<K> fetchAscendingSortOrder(TableColumn<V,?> column);

  abstract protected Map<K,V> fetchData(Set<K> keys);


  // public methods

  public void setRowsToFetch(int rowsToFetch)
  {
    _rowsToFetch = rowsToFetch;
  }

  /**
   * Subclass should call this method to determine how many rows of data need to
   * be fetched in order to populate the visible rows of the data table.
   */
  public int getRowsToFetch()
  {
    return _rowsToFetch;
  }

  @Override
  public int getRowIndex()
  {
    return _rowIndex;
  }

  @Override
  public int getRowCount()
  {
    return _totalRowCount;
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
  final public void sort(TableColumn<V,?> column, SortDirection direction)
  {
    _sortColumn = column;
    _sortDirection = direction;
    _sortedKeys = null;  // force re-query
  }

  @Override
  public V getRowData()
  {
    if (!isRowAvailable()) {
      return null;
    }
    doFetchIfNecessary();
    return _fetchedRows.get(getSortedKeys().get(getSortIndex(_rowIndex)));
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
    return (_totalRowCount - rowIndex) - 1;
  }

  private List<K> getSortedKeys()
  {
    if (_sortedKeys == null) {
      _sortedKeys = fetchAscendingSortOrder(_sortColumn);
    }
    return _sortedKeys;
  }

  private void doFetchIfNecessary()
  {
    if (!_fetchedRows.containsKey(getSortedKeys().get(getSortIndex(_rowIndex)))) {
      Map<K,V> data = fetchData(getUnfetchedKeysBatch());
      cacheFetchedData(data);
    }
  }

  private Set<K> getUnfetchedKeysBatch()
  {
    Set<K> keys = new HashSet<K>();
    int from = _rowIndex;
    int to = Math.min(_rowIndex + _rowsToFetch, _totalRowCount);
    for (int i = from; i < to; ++i) {
      K key = getSortedKeys().get(getSortIndex(i));
      if (!_fetchedRows.containsKey(key)) {
        keys.add(key);
      }
    }
    if (log.isDebugEnabled()) {
      log.debug("need to fetch keys " + keys + " for rows " + from + ".." + to);
    }
    return keys;
  }

  private void cacheFetchedData(Map<K,V> fetchedData)
  {
    _fetchedRows.putAll(fetchedData);
    if (log.isDebugEnabled()) {
      log.debug("fetched " + fetchedData.size() + " rows: " + _rowIndex +
                " to " + ((_rowIndex + fetchedData.size()) - 1));
    }
  }
}
