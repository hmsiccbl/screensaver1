//$HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
//$Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $

//Copyright 2006 by the President and Fellows of Harvard College.

//Screensaver is an open-source project developed by the ICCB-L and NSRB labs
//at Harvard Medical School. This software is distributed under the terms of
//the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.AssayWellType;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.util.CollectionUtils;
import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

public class ScreenResultsDAOImpl extends AbstractDAO implements ScreenResultsDAO
{
  // static members

  private static Logger log = Logger.getLogger(ScreenResultsDAOImpl.class);

  public static int SORT_BY_PLATE_WELL = -3;
  public static int SORT_BY_WELL_PLATE = -2;
  public static int SORT_BY_ASSAY_WELL_TYPE = -1;

  // instance data members

  // public constructors and methods

  /**
   * @motivation for CGLIB dynamic proxy creation
   */
  public ScreenResultsDAOImpl()
  {
  }
  
  public Map<WellKey,ResultValue> findResultValuesByPlate(Integer plateNumber, ResultValueType rvt)
  {
    Map<WellKey,List<ResultValue>> result1 = findResultValuesByPlate(plateNumber, Arrays.asList(rvt));
    Map<WellKey,ResultValue> result = new HashMap<WellKey,ResultValue>(result1.size());
    for (Map.Entry<WellKey,List<ResultValue>> entry : result1.entrySet()) {
      result.put(entry.getKey(), entry.getValue().get(0));
    }
    return result;
  }
  
  /*
  select index(rv), rv.value, rv.assayWellType, rv.exclude from ResultValueType rvt join rvt.resultValues rv where rvt.id in (...) and substring(index(rv),1,5) = ?
   */
  public Map<WellKey,List<ResultValue>> findResultValuesByPlate(Integer plateNumber, List<ResultValueType> rvts)
  {
    List<Integer> rvtIds = new ArrayList<Integer>(rvts.size());
    Map<Integer,Integer> rvtOrder = new HashMap<Integer,Integer>(rvts.size());
    int i = 0;
    for (ResultValueType rvt : rvts) {
      rvtIds.add(rvt.getEntityId());
      rvtOrder.put(rvt.getEntityId(), i++);
    }

    String hql = "select index(rv), elements(rv), rvt.id " +
    "from ResultValueType rvt join rvt.resultValues rv " +
    "where rvt.id in (" + StringUtils.makeListString(rvtIds, ",") + 
    ") and substring(index(rv),1," + Well.PLATE_NUMBER_LEN + ") = ? " +
    "order by rvt.id";
    String paddedPlateNumber = String.format("%0" + Well.PLATE_NUMBER_LEN + "d", plateNumber);
    List hqlResult = getHibernateTemplate().find(hql.toString(), new Object[] { paddedPlateNumber });
    Map<WellKey,List<ResultValue>> result = new HashMap<WellKey,List<ResultValue>>(hqlResult.size());
    for (Iterator iter = hqlResult.iterator(); iter.hasNext();) {
      Object[] row = (Object[]) iter.next();
      WellKey wellKey = (WellKey) row[0];
      List<ResultValue> rvsForRvt = result.get(wellKey);
      if (rvsForRvt == null) {
        rvsForRvt = new ArrayList<ResultValue>(rvts.size());
        CollectionUtils.fill(rvsForRvt, null, rvts.size());
        result.put(wellKey, rvsForRvt);
      }
      rvsForRvt.set(rvtOrder.get((Integer) row[2]), (ResultValue) row[1]);
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
  public Map<WellKey,List<ResultValue> > findSortedResultValueTableByRange(final List<ResultValueType> selectedRvts,
                                                                          final int sortBy,
                                                                          final SortDirection sortDirection,
                                                                          final int fromIndex,
                                                                          final Integer rowsToFetch,
                                                                          final ResultValueType positivesOnlyRvt,
                                                                          final Integer plateNumber)
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
                                                                     positivesOnlyRvt,
                                                                     plateNumber);
        Map<WellKey,List<ResultValue>> mapResult = new LinkedHashMap<WellKey,List<ResultValue>>();
        ScrollableResults scrollableResults = query.scroll();
        if (scrollableResults.setRowNumber(fromIndex) && (rowsToFetch == null || rowsToFetch > 0)) {
          int rowCount = 0;
          do {
            Object[] valuesArray = scrollableResults.get();
            List<ResultValue> values = new ArrayList<ResultValue>(selectedRvts.size());
            mapResult.put(new WellKey(valuesArray[0].toString()), values);
          } while (scrollableResults.next() && (rowsToFetch == null || ++rowCount < rowsToFetch));
        
          // now add the ResultValues 
          Map<WellKey,List<ResultValue>> secondaryMapResult = 
            findRelatedResultValuesInParts(session,
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
                                                               ResultValueType positivesOnlyRvt,
                                                               Integer plateNumber)
  {
    assert selectedRvts.size() > 0;
    assert sortBy < selectedRvts.size();
    assert positivesOnlyRvt == null || selectedRvts.contains(positivesOnlyRvt);

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

    if (positivesOnlyRvt != null) {
      // TODO: this makes the query quite slow, as it has to join all ResultValues for 2 ResultValueTypes
      fromClauses.add("ResultValueType positivesOnlyRvt join positivesOnlyRvt.resultValues positivesOnlyRv");
      whereClauses.add("positivesOnlyRv.positive = true");
      whereClauses.add("index(positivesOnlyRv) = index(rv)");
      whereClauses.add("positivesOnlyRvt.id=?");
      args.add(positivesOnlyRvt.getEntityId());
    }
    
    if (plateNumber != null) {
      whereClauses.add("cast(substring(index(rv),1,5),int)=?");
      args.add(plateNumber);
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
  
  /**
   * @motivation optimization: querying for too many wellKeys in the same query
   *             is slower than breaking up the queries into subsets and
   *             recombining the results in memory.
   */
  private Map<WellKey,List<ResultValue>> findRelatedResultValuesInParts(Session session,
                                                                        Set<WellKey> wellKeys, 
                                                                        List<ResultValueType> selectedRvts) {
    Map<WellKey,List<ResultValue>> result = new HashMap<WellKey,List<ResultValue>>();
    ArrayList<WellKey> wellKeysList = new ArrayList<WellKey>(wellKeys);
    // the size of each subset probably shouldn't be less than 384, since
    // performance is okay at this value, and querying for <=384 wellKeys is a
    // very common (384 well plate size)
    int subsetSize = 384;
    int i = 0;
    while (i * subsetSize < wellKeys.size()) {
      List<WellKey> wellKeysSubset = 
        wellKeysList.subList(i * subsetSize,
                             Math.min((i + 1) * subsetSize, wellKeysList.size()));
      result.putAll(findRelatedResultValues(session, wellKeysSubset, selectedRvts));
      ++i;
    }
    assert result.size() == wellKeys.size();
    return result;
  }

  private Map<WellKey,List<ResultValue>> findRelatedResultValues(Session session,
                                                                 Collection<WellKey> wellKeys, 
                                                                 List<ResultValueType> selectedRvts) 
  {
    Map<Number,Integer> rvtId2Pos = new HashMap<Number,Integer>(selectedRvts.size());
    String wellKeysList = StringUtils.makeListString(StringUtils.wrapStrings(wellKeys, "'", "'"), ",");
    List<Number> rvtIds = new ArrayList<Number>();
    for (int i = 0; i < selectedRvts.size(); i++) {
      ResultValueType rvt = selectedRvts.get(i);
      rvtIds.add(rvt.getEntityId());
      rvtId2Pos.put(rvt.getEntityId(), i);
    }
    String rvtIdsList = StringUtils.makeListString(StringUtils.wrapStrings(rvtIds, "'", "'"), ",");
        
    StringBuilder sql = new StringBuilder();
    sql.append("select rv.result_value_type_id, rv.key, rv.assay_well_type, rv.value, rv.numeric_value, rv.numeric_decimal_precision, rv.exclude, rv.positive ").
    append("from result_value_type_result_values rv ").
    append("where (rv.result_value_type_id in (").append(rvtIdsList).
    append(")) and (rv.key in (").append(wellKeysList).append("))");
    
    Map<WellKey,List<ResultValue>> result = new HashMap<WellKey,List<ResultValue>>();
    Query query = session.createSQLQuery(sql.toString());
    for (Iterator iter = query.list().iterator(); iter.hasNext();) {
      int field = 0;
      Object[] row = (Object[]) iter.next();
      Number rvtId = (Number) row[field++];
      WellKey wellKey = new WellKey(row[field++].toString());
      List<ResultValue> resultValues = result.get(wellKey);
      if (resultValues == null) {
        resultValues = new ArrayList<ResultValue>(selectedRvts.size());
        CollectionUtils.fill(resultValues, null, selectedRvts.size());
        result.put(wellKey, resultValues);
      }
      AssayWellType assayWellType = AssayWellType.valueOf(((String) row[field++]).toUpperCase().replaceAll(" ", "_"));
      String textValue = (String) row[field++];
      Double numValue = (Double) row[field++];
      Integer numPrecision = row[field++] == null ? -1 : ((Integer) row[field - 1]);
      boolean exclude = row[field++] == null ? false : ((Boolean) row[field - 1]); 
      boolean positive = row[field++] == null ? false : ((Boolean) row[field - 1]);
      
      ResultValue rv;
      if (numValue != null) {
        rv = new ResultValue(assayWellType,
                             numValue,
                             numPrecision,
                             exclude,
                             positive);
      }
      else {
        rv = new ResultValue(assayWellType,
                             textValue,
                             exclude,
                             positive);
      }
      resultValues.set(rvtId2Pos.get(rvtId), rv);
    }
    return result;
  }
}
