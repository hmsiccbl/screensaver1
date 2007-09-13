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
import java.util.List;
import java.util.Map;

import javax.faces.model.DataModel;

import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.ui.screenresults.FullScreenResultDataModel;

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
 * @param <V> the data type of each cell. Use <code>Object</code> (or some common
 *          base type) if cell data types are heterogenous.
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public abstract class VirtualPagingDataModel<K,V> extends DataModel
{

  // static members

  private static Logger log = Logger.getLogger(FullScreenResultDataModel.class);


  // instance data

  private int _rowsToFetch;
  private int _totalRowCount;
  protected int _sortColumnIndex;
  protected SortDirection _sortDirection;
  protected int _rowIndex;
  private Map<Integer,Map<String,Object>> _fetchedRows = new HashMap<Integer,Map<String,Object>>();


  // constructors

  protected VirtualPagingDataModel()
  {
  }

  protected VirtualPagingDataModel(int rowsToFetch,
                                   int totalRowCount,
                                   int sortColumnIndex,
                                   SortDirection sortDirection)
  {
    _rowsToFetch = rowsToFetch;
    _totalRowCount = totalRowCount;
    _sortColumnIndex = sortColumnIndex;
    _sortDirection = sortDirection;
  }

  // abstract methods

  abstract protected Map<K,List<V>> fetchData(int firstRowIndex, int rowsToFetch);

  abstract protected Map<String,Object> makeRow(int rowIndex, K rowKey, List<V> rowData);

  //abstract public void sort(String sortColumnName, SortDirection sortDirection);


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
  public Map<String,Object> getRowData()
  {
    if (!_fetchedRows.containsKey(_rowIndex)) {
      if (log.isDebugEnabled()) {
        log.debug("row not yet fetched: " + _rowIndex);
      }
      Map<K,List<V>> data = fetchData(_rowIndex, _rowsToFetch);
      int i = _rowIndex;
      for (Map.Entry<K,List<V>> entry : data.entrySet()) {
        _fetchedRows.put(i, makeRow(i, entry.getKey(), entry.getValue()));
        ++i;
      }
      if (log.isDebugEnabled()) {
        log.debug("  fetched " + data.size() + " rows " + _rowIndex +
                  " to " + ((_rowIndex + data.size()) - 1));
      }
    }
    return _fetchedRows.get(_rowIndex);
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
}
