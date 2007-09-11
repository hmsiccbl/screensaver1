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

import org.apache.log4j.Logger;

/**
 * Abstract data model class for JSF data table that displays the ResultValues
 * for an arbitrary set of ResultValueTypes from a single ScreenResult. The
 * ResultValueTypes define the table columns. This abstract class provides the
 * following common functionality to its subclasses:
 * <ul>
 * <li>Adds a set of fixed "key" columns before the ResultValueType columns: Plate, Well, Well Type</li>
 * <li>Can report whether a given ResultValue (cell value) is "excluded"</li>
 * </ul>
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
abstract public class ScreenResultDataModel extends DataModel
{
  // static members

  private static Logger log = Logger.getLogger(ScreenResultDataModel.class);

  public static final int DATA_TABLE_FIXED_COLUMNS = 3;


  // instance data members

  protected List<ResultValueType> _resultValueTypes;
  protected int _sortColumnIndex;
  protected SortDirection _sortDirection;
  protected ScreenResultsDAO _screenResultsDao;
  protected int _rowIndex;
  protected int _rowsToFetch;

  private List<List<Boolean>> _excludedResultValues;
  private List<Map<String,Object>> _wrappedData;


  // abstract methods


  abstract protected Map<WellKey,List<ResultValue>> fetchData(List<ResultValueType> selectedResultValueTypes,
                                                              int sortBy,
                                                              SortDirection sortDirection);


  //abstract public void sort(String sortColumnName, SortDirection sortDirection);


  // public constructors and methods

  public ScreenResultDataModel(List<ResultValueType> resultValueTypes,
                               int sortColumnIndex,
                               SortDirection sortDirection,
                               ScreenResultsDAO dao)
  {
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

  final public void setRowsToFetch(int rowsToFetch)
  {
    log.debug("set rowsToFetch=" + rowsToFetch);
    _rowsToFetch = rowsToFetch;
  }

  /**
   * Subclassses that implement virtual paging (i.e., on-demand fetching of
   * viewable data) can call this method to determine how many rows of data need
   * to be fetched in order to populate the visible rows of the data table.
   *
   * @param rowsToFetch
   */
  public int getRowsToFetch()
  {
    return _rowsToFetch;
  }

  public boolean isResultValueCellExcluded(int colIndex)
  {
    if (colIndex < DATA_TABLE_FIXED_COLUMNS) {
      return false;
    }
    return _excludedResultValues.get(getRowIndex()).get(colIndex - DATA_TABLE_FIXED_COLUMNS);
  }


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
      sortByArg = _sortColumnIndex - DATA_TABLE_FIXED_COLUMNS;
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
      cellValues.put(rvt.getUniqueName(),
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
}
