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

import edu.harvard.med.screensaver.db.ScreenResultsDAO;
import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.ui.table.VirtualPagingDataModel;

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
abstract public class ScreenResultDataModel extends VirtualPagingDataModel<WellKey,ResultValue>
{
  // static members

  private static Logger log = Logger.getLogger(ScreenResultDataModel.class);

  public static final int DATA_TABLE_FIXED_COLUMNS = 3;


  // instance data members

  protected ScreenResultsDAO _screenResultsDao;
  protected List<ResultValueType> _resultValueTypes;
  private Map<Integer,List<Boolean>> _excludedResultValuesMap = new HashMap<Integer,List<Boolean>>();


  // public constructors and methods

  public ScreenResultDataModel(List<ResultValueType> resultValueTypes,
                               int rowsToFetch,
                               int sortColumnIndex,
                               SortDirection sortDirection,
                               ScreenResultsDAO dao)
  {
    super(rowsToFetch,
          resultValueTypes == null || resultValueTypes.size() == 0 ? 0 : resultValueTypes.get(0).getResultValues().size(),
          sortColumnIndex - DATA_TABLE_FIXED_COLUMNS,
          sortDirection);
    _resultValueTypes = resultValueTypes;
    _screenResultsDao = dao;
  }

  public boolean isResultValueCellExcluded(int colIndex)
  {
    if (colIndex < DATA_TABLE_FIXED_COLUMNS) {
      // fixed columns do not contain result values
      return false;
    }
    return _excludedResultValuesMap.get(getRowIndex()).get(colIndex - DATA_TABLE_FIXED_COLUMNS);
  }


  // protected methods

  /**
   * @sideeffect adds element to {@link #_excludedResultValues}
   */
  @Override
  protected Map<String,Object> makeRow(int rowIndex, WellKey wellKey, List<ResultValue> rowData)
  {
    HashMap<String,Object> row = new HashMap<String,Object>();
    // TODO: eliminate hardcoded column name strings
    row.put("Plate", wellKey.getPlateNumber());
    row.put("Well", wellKey.getWellName());
    row.put("Type", rowData.get(0).getAssayWellType());
    List<Boolean> rowExcludes = new ArrayList<Boolean>();
    Iterator<ResultValueType> rvtIter = _resultValueTypes.iterator();
    for (ResultValue rv : rowData) {
      ResultValueType rvt = rvtIter.next();
      rowExcludes.add(rv.isExclude());
      Object typedValue = ResultValue.getTypedValue(rv, rvt);
      row.put(rvt.getUniqueName(),
                     typedValue == null ? null : typedValue.toString());
    }
    _excludedResultValuesMap.put(rowIndex, rowExcludes);
    return row;
  }

}
