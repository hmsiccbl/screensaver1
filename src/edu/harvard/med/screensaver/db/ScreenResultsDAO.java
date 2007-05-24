//$HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
//$Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $

//Copyright 2006 by the President and Fellows of Harvard College.

//Screensaver is an open-source project developed by the ICCB-L and NSRB labs
//at Harvard Medical School. This software is distributed under the terms of
//the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.ui.searchresults.SortDirection;
import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

public class ScreenResultsDAO extends AbstractDAO
{
  // static members

  private static Logger log = Logger.getLogger(ScreenResultsDAO.class);

  public static int SORT_BY_PLATE_WELL = -3;
  public static int SORT_BY_WELL_PLATE = -2;
  public static int SORT_BY_ASSAY_WELL_TYPE = -1;

  // instance data members

  // public constructors and methods

  /**
   * @motivation for CGLIB dynamic proxy creation
   */
  public ScreenResultsDAO()
  {
  }
  
  /*
  select index(rv), rv.value, rv.assayWellType, rv.exclude from ResultValueType rvt join rvt.resultValues rv where rvt.id=? and substring(index(rv),1,5) = ?
   */
  public Map<WellKey,ResultValue> findResultValuesByPlate(Integer plateNumber, ResultValueType rvt)
  {
    String hql = "select index(rv), elements(rv) " +
    "from ResultValueType rvt join rvt.resultValues rv " +
    "where rvt.id=? and substring(index(rv),1," + Well.PLATE_NUMBER_LEN + ") = ?";
    String paddedPlateNumber = String.format("%0" + Well.PLATE_NUMBER_LEN + "d", plateNumber);
    List hqlResult = getHibernateTemplate().find(hql.toString(), new Object[] { rvt.getEntityId(), paddedPlateNumber });
    Map<WellKey,ResultValue> result = new HashMap<WellKey,ResultValue>(hqlResult.size());
    for (Iterator iter = hqlResult.iterator(); iter.hasNext();) {
      Object[] row = (Object[]) iter.next();
      result.put((WellKey) row[0],
                 (ResultValue) row[1]);
    }
    return result;
  }


  /*
  For example, sorting on 2nd RVT:
  select index(rv2), elements(rv1.value), elements(rv2.value), elements(rv3.value)
  from ResultValueType rvt1 join rvt1.resultValues rv1,
       ResultValueType rvt2 join rvt2.resultValues rv2,
       ResultValueType rvt3 join rvt3.resultValues rv3,
  where
        rvt1.id=? and
        rvt2.id=? and
        rvt3.id=? and
        index(rv1)=index(rv2) and
        index(rv3)=index(rv2)
  order by rv2.value
   */
  //TODO: due to denormalized design, this method requires all 384 ResultValues to exist for each ResultValueType
  @SuppressWarnings("unchecked")
  public Map<WellKey,List<ResultValue>> findSortedResultValueTableByRange(final List<ResultValueType> selectedRvts,
                                                                          final int sortBy,
                                                                          final SortDirection sortDirection,
                                                                          final int fromIndex,
                                                                          final int rowsToFetch,
                                                                          final ResultValueType hitsOnlyRvt)
                                                                          {
    Map<WellKey,List<ResultValue>> mapResult = (Map<WellKey,List<ResultValue>>)
    getHibernateTemplate().execute(new HibernateCallback() 
    {
      public Object doInHibernate(Session session) throws HibernateException, SQLException
      {
        Query query = buildQueryForSortedResultValueTypeTableByRange(session,
                                                                     selectedRvts,
                                                                     sortBy,
                                                                     sortDirection,
                                                                     hitsOnlyRvt);
        Map<WellKey,List<ResultValue>> mapResult = new LinkedHashMap<WellKey,List<ResultValue>>();
        ScrollableResults scrollableResults = query.scroll();
        if (scrollableResults.setRowNumber(fromIndex) && rowsToFetch > 0) {
          int rowCount = 0;
          do {
            Object[] valuesArray = scrollableResults.get();
            List<ResultValue> values = new ArrayList<ResultValue>(selectedRvts.size());
            mapResult.put(new WellKey(valuesArray[0].toString()), values);
          } while (scrollableResults.next() && ++rowCount < rowsToFetch);

          // now add the ResultValues 
          Map<WellKey,List<ResultValue>> secondaryMapResult = 
            findRelatedResultValues(session,
                                    mapResult.keySet(),
                                    selectedRvts);
          for (Map.Entry<WellKey,List<ResultValue>> entry : mapResult.entrySet()) {
            entry.getValue().addAll(secondaryMapResult.get(entry.getKey()));
          }


        }
        return mapResult;
      }

    });
    return mapResult;
                                                                          }



  public void deleteScreenResult(ScreenResult screenResult)
  {
    // disassociate ScreenResult from Screen
    screenResult.getScreen().setScreenResult(null);

    getHibernateTemplate().delete(screenResult);
    log.debug("deleted " + screenResult);
  }




  // private methods

  private Query buildQueryForSortedResultValueTypeTableByRange(Session session,
                                                               List<ResultValueType> selectedRvts,
                                                               int sortBy,
                                                               SortDirection sortDirection,
                                                               ResultValueType hitsOnlyRvt)
  {
    assert selectedRvts.size() > 0;
    assert sortBy < selectedRvts.size();
    assert hitsOnlyRvt == null || selectedRvts.contains(hitsOnlyRvt);

    StringBuilder hql = new StringBuilder();
    List<String> selectFields = new ArrayList<String>();
    List<String> fromClauses = new ArrayList<String>();
    List<String> whereClauses = new ArrayList<String>();
    List<String> orderByClauses = new ArrayList<String>();
    List<Integer> args = new ArrayList<Integer>();

    // TODO: can simplify this code now that we no longer require iteration to generate our HQL string
    String sortByRvAlias = "rv";
    selectFields.add("index(" + sortByRvAlias + ")");
    fromClauses.add("ResultValueType rvt join rvt.resultValues rv");
    whereClauses.add("rvt.id=?");
    args.add(selectedRvts.get(Math.max(0, sortBy)).getEntityId());

    if (hitsOnlyRvt != null) {
      // TODO: this makes the query quite slow, as it has to join all ResultValues for 2 ResultValueTypes
      fromClauses.add("ResultValueType hitsOnlyRvt join hitsOnlyRvt.resultValues hitsOnlyRv");
      whereClauses.add("hitsOnlyRv.hit = true");
      whereClauses.add("index(hitsOnlyRv) = index(rv)");
      whereClauses.add("hitsOnlyRvt.id=?");
      args.add(hitsOnlyRvt.getEntityId());
    }

    String sortDirStr = sortDirection.equals(SortDirection.ASCENDING)? " asc" : " desc";
    if (sortBy >= 0) {
      if (selectedRvts.get(sortBy).isNumeric()) {
        orderByClauses.add(sortByRvAlias + ".numericValue" + sortDirStr);
      }
      else {
        orderByClauses.add(sortByRvAlias + ".value" + sortDirStr);
      }
    }
    else if (sortBy == SORT_BY_PLATE_WELL) {
      orderByClauses.add("index(" + sortByRvAlias + ")" + sortDirStr);
    }
    else if (sortBy == SORT_BY_WELL_PLATE) {
      orderByClauses.add("substring(index(" + sortByRvAlias + "),7,3)" + sortDirStr);
      orderByClauses.add("substring(index(" + sortByRvAlias + "),1,5)");
    }
    else if (sortBy == SORT_BY_ASSAY_WELL_TYPE) {
      orderByClauses.add(sortByRvAlias + ".assayWellType" + sortDirStr);
    }

    hql.append("select ").append(StringUtils.makeListString(selectFields, ", "));
    hql.append(" from ").append(StringUtils.makeListString(fromClauses, ", "));
    hql.append(" where ").append(StringUtils.makeListString(whereClauses, " and "));
    hql.append(" order by ").append(StringUtils.makeListString(orderByClauses, ", "));

    if (log.isDebugEnabled()) {
      log.debug("buildQueryForSortedResultValueTypeTableByRange() executing HQL: " + hql.toString());
    }

    Query query = session.createQuery(hql.toString());
    for (int i = 0; i < args.size(); ++i) {
      query.setInteger(i, args.get(i));
    }
    return query;
  }

  private Map<WellKey,List<ResultValue>> findRelatedResultValues(Session session,
                                                                 Set<WellKey> wellKeys, 
                                                                 List<ResultValueType> selectedRvts)
                                                                 {
    String wellKeysList = StringUtils.makeListString(StringUtils.wrapStrings(wellKeys, "'", "'"), ",");
    Map<WellKey,List<ResultValue>> result = new HashMap<WellKey,List<ResultValue>>();
    for (int i = 0; i < selectedRvts.size(); i++) {
      ResultValueType rvt = selectedRvts.get(i);

      StringBuilder hql = new StringBuilder();
      // TODO: see if we can produce an equivalent HQL query that does not need to use the result_value_type table at all, as result_value_type_result_values.result_value_type_id can be used directly (at least, if we were doing this directly with sql)
      hql.append("select indices(rv), elements(rv) from ResultValueType rvt join rvt.resultValues rv where rvt.id = " + 
                 rvt.getEntityId() + " and index(rv) in (" + wellKeysList + ")");
      Query query = session.createQuery(hql.toString());
      for (Iterator iter = query.list().iterator(); iter.hasNext();) {
        Object[] row = (Object[]) iter.next();
        WellKey wellKey = new WellKey(row[0].toString());
        List<ResultValue> resultValues = result.get(wellKey);
        if (resultValues == null) {
          resultValues = new ArrayList<ResultValue>();
          result.put(wellKey, resultValues);
        }
        resultValues.add((ResultValue) row[1]);
      }
    }
    return result;
                                                                 }

}

