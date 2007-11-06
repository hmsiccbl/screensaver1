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
import java.util.TreeMap;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.Query;
import edu.harvard.med.screensaver.db.ScreenResultDataQuery;
import edu.harvard.med.screensaver.db.ScreenResultSortQuery;
import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screenresults.AbstractEntityIdComparator;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.ui.searchresults.ResultValueTypeColumn;
import edu.harvard.med.screensaver.ui.searchresults.WellColumn;
import edu.harvard.med.screensaver.ui.table.TableColumn;
import edu.harvard.med.screensaver.ui.table.VirtualPagingDataModel;

import org.apache.log4j.Logger;
import org.hibernate.Session;

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

  protected ScreenResult _screenResult;
  protected List<ResultValueType> _resultValueTypes;


  // abstract methods

  abstract ScreenResultSortQuery getScreenResultSortQuery();


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


  // protected methods

  @Override
  final protected List<String> fetchAscendingSortOrder(TableColumn column)
  {

    ScreenResultSortQuery query = getScreenResultSortQuery();
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
  final protected Map<String,Well> fetchData(final Set<String> keys)
  {
    final Map<String,Well> result = new HashMap<String,Well>(keys.size());
    if (keys.size() > 0) {
      final Map<Well,Map<ResultValueType,ResultValue>> well2RestrictedResultValues = new HashMap<Well,Map<ResultValueType,ResultValue>>();
      _dao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction() {
          List<Well> wells = fetchWells(keys);
          for (Well well : wells) {
            Map<ResultValueType,ResultValue> rvt2rv =
              fetchResultValuesForWellAndResultValueTypes(well, _resultValueTypes);
            well2RestrictedResultValues.put(well, rvt2rv);
            result.put(well.getEntityId(), well);
          }
        }
      });

      for (Well well : result.values()) {
        well.setResultValuesSubset(well2RestrictedResultValues.get(well));
      }
    }
    return result;
  }

  private List<Well> fetchWells(final Set<String> wellIds)
  {
    List<Well> wells =
      _dao.runQuery(new Query() {
        public org.hibernate.Query getQuery(Session session)
        {
          String hql = "select w from Well w where w.id in (:wellIds)";
          org.hibernate.Query query = session.createQuery(hql);
          query.setReadOnly(true);
          query.setParameterList("wellIds", wellIds);
          return query;
        }
      });
    return wells;
  }

  private Map<ResultValueType,ResultValue> fetchResultValuesForWellAndResultValueTypes(Well well,
                                                                                       List<ResultValueType> rvts)
  {
    ScreenResultDataQuery query = new ScreenResultDataQuery();
    query.setWell(well);
    // note: we create a TreeMap with AEID so that map keys can be found even when detached RVTs are used in map.get() call (RVT equality is instance equality)
    Map<ResultValueType,ResultValue> rvt2rv =
      new TreeMap<ResultValueType,ResultValue>(new AbstractEntityIdComparator<ResultValueType,Integer>());
    for (ResultValueType rvt : rvts) {
      query.setResultValueType(rvt);
      rvt2rv.put(rvt, ((List<ResultValue>) _dao.<ResultValue>runQuery(query)).get(0));
    }
    return rvt2rv;
  }

}
