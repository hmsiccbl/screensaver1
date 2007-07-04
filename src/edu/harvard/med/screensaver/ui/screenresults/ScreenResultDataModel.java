// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.model.DataModel;

import edu.harvard.med.screensaver.db.ScreenResultsDAO;
import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.AssayWellType;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.ui.table.TableSortManager;

import org.apache.log4j.Logger;

abstract public class ScreenResultDataModel extends DataModel
{
  // static members

  private static Logger log = Logger.getLogger(ScreenResultDataModel.class);

  // instance data members

  protected ScreenResult _screenResult;
  protected TableSortManager _sortManager;
  protected List<ResultValueType> _selectedResultValueTypes;
  protected ScreenResultsDAO _screenResultsDao;
  protected int _rowIndex;
  
  private List<List<Boolean>> _excludedResultValues;
  private List<Map<String,String>> _wrappedData;


  // public constructors and methods

  public ScreenResultDataModel(ScreenResult screenResult,
                               TableSortManager sortManager,
                               List<ResultValueType> selectedResultValueTypes,
                               ScreenResultsDAO dao)
  {
    _screenResult = screenResult;
    _sortManager = sortManager;
    _selectedResultValueTypes = selectedResultValueTypes;
    _screenResultsDao = dao;
  }
  
  @Override
  public Map<String,String> getRowData()
  {
    return getWrappedData().get(_rowIndex);
  }

  @Override
  public int getRowIndex()
  {
    return _rowIndex;
  }

  @Override
  public int getRowCount()
  {
    return getWrappedData().size();
  }

  public List<Map<String,String>> getWrappedData()
  {
    if (_wrappedData == null) {
      build();
    }
    return _wrappedData;
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
  final public void setWrappedData(Object data)
  {
    throw new UnsupportedOperationException();
  }
  
  public boolean isResultValueCellExcluded()
  {
    int columnIndex = _sortManager.getCurrentColumnIndex();
    if (columnIndex < ScreenResultViewer.DATA_TABLE_FIXED_COLUMNS) {
      return false;
    }
    int dataHeaderIndex = columnIndex - ScreenResultViewer.DATA_TABLE_FIXED_COLUMNS;
    return isResultValueCellExcluded(_rowIndex, dataHeaderIndex);
  }
  
  abstract protected Map<WellKey,List<ResultValue>> fetchData(List<ResultValueType> selectedResultValueTypes,
                                                              int sortBy,
                                                              SortDirection sortDirection);

  public void setRowsToFetch(int rowsToFetch)
  {
  }

  //abstract public void sort(String sortColumnName, SortDirection sortDirection);

  
  // private methods

  @SuppressWarnings("unchecked")
  final protected void build()
  {
    log.debug("building ScreenResultDataModel");
    int sortByArg;
    switch (_sortManager.getCurrentSortColumnIndex())
    {
    case 0: sortByArg = ScreenResultsDAO.SORT_BY_PLATE_WELL; break;
    case 1: sortByArg = ScreenResultsDAO.SORT_BY_WELL_PLATE; break;
    case 2: sortByArg = ScreenResultsDAO.SORT_BY_ASSAY_WELL_TYPE; break;
    default:
      sortByArg = _sortManager.getCurrentSortColumnIndex() - ScreenResultViewer.DATA_TABLE_FIXED_COLUMNS;
    }
    _wrappedData = new ArrayList<Map<String,String>>();
    _excludedResultValues = new ArrayList<List<Boolean>>();
    int rowIndex = 0;
    for (Map.Entry<WellKey,List<ResultValue>> entry : fetchData(_selectedResultValueTypes,
                                                                sortByArg,
                                                                _sortManager.getCurrentSortDirection()).entrySet()) {
      WellKey wellKey = entry.getKey();
      addRow(rowIndex++, 
             wellKey,
             entry.getValue().get(0).getAssayWellType(),
             entry.getValue(),
             _selectedResultValueTypes);
    }
  }
  
  /**  
   * @sideeffect adds element to {@link #_excludedResultValues}
   */
  private void addRow(int rowIndex,
                      WellKey wellKey,
                      AssayWellType assayWellType,
                      List<ResultValue> resultValues, 
                      List<ResultValueType> resultValueTypes)
  {
    List<String> columnNames = _sortManager.getColumnNames();
    int i = 0;
    HashMap<String,String> cellValues = new HashMap<String,String>();
    cellValues.put(columnNames.get(i++), Integer.toString(wellKey.getPlateNumber()));
    cellValues.put(columnNames.get(i++), wellKey.getWellName());
    cellValues.put(columnNames.get(i++), assayWellType.toString());
    List<Boolean> excludedResultValuesRow = new ArrayList<Boolean>();
    Iterator<ResultValueType> rvtIter = resultValueTypes.iterator();
    for (ResultValue rv : resultValues) {
      ResultValueType rvt = rvtIter.next();
      excludedResultValuesRow.add(rv.isExclude());
      Object typedValue = ResultValue.getTypedValue(rv, rvt);
      cellValues.put(columnNames.get(i++),
                     typedValue == null ? null : typedValue.toString());
    }
    addRowValues(rowIndex, cellValues);
    addRowResultValueExcludes(rowIndex, excludedResultValuesRow);
  }

  protected void addRowValues(int rowIndex, Map<String,String> rowValues)
  {
    _wrappedData.add(rowIndex, rowValues);
  }

  protected void addRowResultValueExcludes(int rowIndex, List<Boolean> rowExcludes)
  {
    _excludedResultValues.add(rowIndex, rowExcludes);
  }
  
  protected boolean isResultValueCellExcluded(int rowIndex, int dataHeaderIndex)
  {
    return _excludedResultValues.get(rowIndex).get(dataHeaderIndex);
  }
}
