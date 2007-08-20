// $HeadURL$
// $Id$
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

import org.apache.log4j.Logger;

abstract public class ScreenResultDataModel extends DataModel
{
  // static members

  private static Logger log = Logger.getLogger(ScreenResultDataModel.class);

  // instance data members

  protected ScreenResult _screenResult;
  protected List<ResultValueType> _resultValueTypes;
  protected int _sortColumnIndex;
  protected SortDirection _sortDirection;
  protected ScreenResultsDAO _screenResultsDao;
  protected int _rowIndex;
  
  private List<List<Boolean>> _excludedResultValues;
  private List<Map<String,Object>> _wrappedData;


  // public constructors and methods

  public ScreenResultDataModel(ScreenResult screenResult,
                               List<ResultValueType> resultValueTypes,
                               int sortColumnIndex,
                               SortDirection sortDirection,
                               ScreenResultsDAO dao)
  {
    _screenResult = screenResult;
    _sortColumnIndex = sortColumnIndex;
    _sortDirection = sortDirection;
    _resultValueTypes = resultValueTypes;
    _screenResultsDao = dao;
  }
  
  @Override
  public Map<String,Object> getRowData()
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

  public List<Map<String,Object>> getWrappedData()
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
    switch (_sortColumnIndex)
    {
    case 0: sortByArg = ScreenResultsDAO.SORT_BY_PLATE_WELL; break;
    case 1: sortByArg = ScreenResultsDAO.SORT_BY_WELL_PLATE; break;
    case 2: sortByArg = ScreenResultsDAO.SORT_BY_ASSAY_WELL_TYPE; break;
    default:
      sortByArg = _sortColumnIndex - ScreenResultViewer.DATA_TABLE_FIXED_COLUMNS;
    }
    _wrappedData = new ArrayList<Map<String,Object>>();
    _excludedResultValues = new ArrayList<List<Boolean>>();
    int rowIndex = 0;
    for (Map.Entry<WellKey,List<ResultValue>> entry : fetchData(_resultValueTypes,
                                                                sortByArg,
                                                                _sortDirection).entrySet()) {
      WellKey wellKey = entry.getKey();
      addRow(rowIndex++, 
             wellKey,
             entry.getValue().get(0).getAssayWellType(),
             entry.getValue(),
             _resultValueTypes);
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
    int i = 0;
    HashMap<String,Object> cellValues = new HashMap<String,Object>();
    // TODO: eliminate hardcoded column name strings
    cellValues.put("Plate", wellKey.getPlateNumber());
    cellValues.put("Well", wellKey.getWellName());
    cellValues.put("Type", assayWellType);
    List<Boolean> excludedResultValuesRow = new ArrayList<Boolean>();
    Iterator<ResultValueType> rvtIter = resultValueTypes.iterator();
    for (ResultValue rv : resultValues) {
      ResultValueType rvt = rvtIter.next();
      excludedResultValuesRow.add(rv.isExclude());
      Object typedValue = ResultValue.getTypedValue(rv, rvt);
      cellValues.put(resultValueTypes.get(i++).getUniqueName(),
                     typedValue == null ? null : typedValue.toString());
    }
    addRowValues(rowIndex, cellValues);
    addRowResultValueExcludes(rowIndex, excludedResultValuesRow);
  }

  protected void addRowValues(int rowIndex, Map<String,Object> rowValues)
  {
    _wrappedData.add(rowIndex, rowValues);
  }

  protected void addRowResultValueExcludes(int rowIndex, List<Boolean> rowExcludes)
  {
    _excludedResultValues.add(rowIndex, rowExcludes);
  }
  
  public boolean isResultValueCellExcluded(int colIndex)
  {
    if (colIndex < ScreenResultViewer.DATA_TABLE_FIXED_COLUMNS) {
      return false;
    }
    return _excludedResultValues.get(getRowIndex()).get(colIndex - ScreenResultViewer.DATA_TABLE_FIXED_COLUMNS);
  }
}
