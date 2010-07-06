// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table.model;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.db.datafetcher.DataFetcher;
import edu.harvard.med.screensaver.ui.table.DataTableModelType;
import edu.harvard.med.screensaver.ui.util.ValueReference;

/**
 * JSF DataModel class that supports virtual paging (i.e., on-demand fetching of
 * row data).
 * <p>
 * Note that DataModel's wrappedData property is not supported, as virtual
 * paging implies that the underlying data cannot (always) be made fully
 * available.
 * <p>
 * @param <K> the type of the key used to identify a row of data
 * @param <R> the data type containing the data displayed across each row.
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public abstract class VirtualPagingDataModel<K,R> extends DataTableModel<R>
{
  private static Logger log = Logger.getLogger(VirtualPagingDataModel.class);

  private static final int MAX_CACHE_SIZE_ROWS = 1 << 10;

  protected DataFetcher<R,K,?> _dataFetcher;
  protected ValueReference<Integer> _rowsToFetch;
  protected int _rowIndex = -1;
  protected List<K> _sortedKeys;
  protected SortDirection _sortDirection;
  protected LinkedHashMap<K,R> _fetchedRows = new LinkedHashMap<K,R>(MAX_CACHE_SIZE_ROWS, 0.75F, true /* ordered by access */) {
    private static final long serialVersionUID = 1L;
    protected boolean removeEldestEntry(Map.Entry<K,R> eldest) { 
      return _fetchedRows.size() >= MAX_CACHE_SIZE_ROWS;
    }
  };
  
  
  protected VirtualPagingDataModel() {}

  public VirtualPagingDataModel(DataFetcher<R,K,?> dataFetcher,
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
  public R getRowData()
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
        Set<K> keysToFetch = getUnfetchedKeysBatch();
        if (log.isDebugEnabled()) {
          log.debug("need to fetch " + keysToFetch.size() + " rows");
        }
        Map<K,R> data = _dataFetcher.fetchData(keysToFetch);
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
    return keys;
  }

  private void cacheFetchedData(Map<K,R> fetchedData)
  {
    assert fetchedData.size() <= MAX_CACHE_SIZE_ROWS : "number of new data rows are larger than cache size";
    _fetchedRows.putAll(fetchedData);
    if (log.isDebugEnabled()) {
      log.debug("cached " + fetchedData.size() + " rows; new cache size = " + _fetchedRows.size());
    }
    assert _fetchedRows.size() <= MAX_CACHE_SIZE_ROWS;
  }

  private static final Logger iterLog = Logger.getLogger(VirtualPagingDataModel.VirtualPagingIterator.class);

  private final class VirtualPagingIterator implements Iterator<R>
  {
    private static final int FETCH_SIZE = MAX_CACHE_SIZE_ROWS;

    private BlockingQueue<R> _queue;
    private Thread _fetcherThread;
    private int _remaining;

    private class DataFetcherTask implements Runnable
    {
      @Override
      public void run()
      {
        Iterator<List<K>> keyBatchIterator = Iterators.partition(_sortedKeys.iterator(), FETCH_SIZE);
        while (keyBatchIterator.hasNext()) {
          List<K> keyBatch = keyBatchIterator.next();
          iterLog.debug("fetching next batch");
          Map<K,R> data = _dataFetcher.fetchData(Sets.newHashSet(keyBatch));
          for (K k : keyBatch) {
            try {
              _queue.put(data.get(k));
            }
            catch (InterruptedException e) {
              iterLog.error("fetcher thread interrupted: " + e);
              return;
            }
          }
          iterLog.debug("done fetching next batch");
        }
        iterLog.debug("data fetcher task is done");
      }
    }

    protected VirtualPagingIterator()
    {
      _queue = new LinkedBlockingQueue<R>(FETCH_SIZE);
      _remaining = _sortedKeys.size();
      _fetcherThread = new Thread(new DataFetcherTask());
      _fetcherThread.start();
    }

    @Override
    public boolean hasNext()
    {
      return _remaining > 0;
    }

    @Override
    public R next()
    {
      try {
        if (!hasNext()) {
          return null; // note: this avoids the possibility of this method never returning, due to an exhausted queue
        }
        R element = _queue.take();
        --_remaining;
        return element;
      }
      catch (InterruptedException e) {
        log.error("iterator.next() interrupted: " + e);
        return null;
      }
    }

    @Override
    public void remove()
    {
      throw new UnsupportedOperationException();
    }
  }

  @Override
  public Iterator<R> iterator()
  {
    return new VirtualPagingIterator();
  }

}
