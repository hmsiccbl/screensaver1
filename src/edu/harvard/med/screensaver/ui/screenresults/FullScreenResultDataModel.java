// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.harvard.med.screensaver.db.ScreenResultsDAO;
import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;

import org.apache.log4j.Logger;

public class FullScreenResultDataModel extends ScreenResultDataModel
{
  // static members

  private static Logger log = Logger.getLogger(FullScreenResultDataModel.class);

  // instance data members

  private int _preCalculatedSize;
  private int _rowsToFetch;
  private int _firstRowIndexToFetch = -1;
  private Map<Integer,Map<String,Object>> _fetchedRows;
  private Map<Integer,List<Boolean>> _excludedResultValuesMap;


  // public constructors and methods

  public FullScreenResultDataModel(List<ResultValueType> resultValueTypes,
                                   int sortColumnIndex,
                                   SortDirection sortDirection,
                                   ScreenResultsDAO dao,
                                   int rowsToFetch,
                                   int preCalculatedSize)
  {
    super(resultValueTypes, sortColumnIndex, sortDirection, dao);
    _rowsToFetch = rowsToFetch;
    _preCalculatedSize = preCalculatedSize;
    _fetchedRows = new HashMap<Integer,Map<String,Object>>();
    _excludedResultValuesMap = new HashMap<Integer,List<Boolean>>();
  }

  @Override
  public int getRowCount()
  {
    return _preCalculatedSize;
  }

  @Override
  public Map<String,Object> getRowData()
  {
    if (!_fetchedRows.containsKey(_rowIndex)) {
      log.debug("row not yet fetched: " + _rowIndex);
      _firstRowIndexToFetch = _rowIndex;
      build();
    }
    return _fetchedRows.get(_rowIndex);
  }

  @Override
  public List<Map<String,Object>> getWrappedData()
  {
    throw new UnsupportedOperationException();
  }

  public void setRowsToFetch(int rowsToFetch)
  {
    log.debug("set rowsToFetch=" + rowsToFetch);
    _rowsToFetch = rowsToFetch;
  }

  protected Map<WellKey,List<ResultValue>> fetchData(List<ResultValueType> selectedResultValueTypes,
                                                     int sortBy,
                                                     SortDirection sortDirection)
  {
    Map<WellKey,List<ResultValue>> rvData =
      _screenResultsDao.findSortedResultValueTableByRange(_resultValueTypes,
                                                          sortBy,
                                                          sortDirection,
                                                          _firstRowIndexToFetch,
                                                          _rowsToFetch,
                                                          null,
                                                          null);
    log.debug("  fetched rows " + _firstRowIndexToFetch +
              " to " + ((_firstRowIndexToFetch + rvData.size()) - 1));
    log.debug("total fetched row count = " + rvData.size());
    return rvData;
  }

  @Override
  protected void addRowValues(int rowIndex, Map<String,Object> rowValues)
  {
    _fetchedRows.put(_firstRowIndexToFetch + rowIndex, rowValues);
  }

  @Override
  protected void addRowResultValueExcludes(int rowIndex, List<Boolean> rowExcludes)
  {
    _excludedResultValuesMap.put(_firstRowIndexToFetch + rowIndex, rowExcludes);
  }

  @Override
  public boolean isResultValueCellExcluded(int colIndex)
  {
    if (colIndex < ScreenResultDataModel.DATA_TABLE_FIXED_COLUMNS) {
      return false;
    }
    return _excludedResultValuesMap.get(getRowIndex()).get(colIndex - ScreenResultDataModel.DATA_TABLE_FIXED_COLUMNS);
  }
}

