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
import java.util.Set;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.ScreenResultSortQuery;
import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.ui.searchresults.ResultValueTypeColumn;
import edu.harvard.med.screensaver.ui.searchresults.WellColumn;
import edu.harvard.med.screensaver.ui.table.TableColumn;
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
// note: more type-safe to use WellKey instead of String for VirtualPagingDataModel K param, but doubles memory usage
abstract public class ScreenResultDataModel extends VirtualPagingDataModel<String,Well>
{
  // static members

  private static Logger log = Logger.getLogger(ScreenResultDataModel.class);

  public static final int DATA_TABLE_FIXED_COLUMNS = 3;


  // instance data members

  protected GenericEntityDAO _dao;

  private ScreenResult _screenResult;
  protected List<ResultValueType> _resultValueTypes;
  private Map<Integer,List<Boolean>> _excludedResultValuesMap = new HashMap<Integer,List<Boolean>>();



  // public constructors and methods

  public ScreenResultDataModel(ScreenResult screenResult,
                               List<ResultValueType> resultValueTypes,
                               int totalRowCount,
                               int rowsToFetch,
                               TableColumn<Well> sortColumn,
                               SortDirection sortDirection,
                               GenericEntityDAO dao)
  {
    super(rowsToFetch,
          totalRowCount,
          sortColumn,
          sortDirection);
    _dao = dao;
    _screenResult = screenResult;
    _resultValueTypes = resultValueTypes;
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

  protected ScreenResultSortQuery getScreenResultDataQuery()
  {
    return null;
  }

  @Override
  protected List<String> fetchAscendingSortOrder(TableColumn column)
  {
    ScreenResultSortQuery query = new ScreenResultSortQuery(_screenResult);
    if (column instanceof WellColumn) {
      query.setSortByWellProperty(((WellColumn) column).getWellProperty());
    }
    else if (column instanceof ResultValueTypeColumn) {
      query.setSortByResultValueType(((ResultValueTypeColumn) column).getResultValueType());
    }
    else {
      throw new IllegalArgumentException("invalid TableColumn type: "  + column.getClass());
    }
    return _dao.<String>runQuery(query);
  }

  @Override
  protected Map<String,Well> fetchData(final Set<String> keys)
  {
    final Map<String,Well> result = new HashMap<String,Well>(keys.size());
    _dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction() {
        for (String wellId : keys) {
          // TODO: make a single db call to fetch a *set* of wells
          Well well = _dao.findEntityById(Well.class,
                                          wellId,
                                          true,
          "resultValues.resultValueType");
          if (log.isDebugEnabled()) {
            log.debug("fetched " + well);
          }
          result.put(wellId, well);
        }
      }
    });
    return result;
  }
}
