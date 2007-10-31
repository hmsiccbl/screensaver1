// $HeadURL$
// $Id$

// Copyright 2006 by the President and Fellows of Harvard College.

// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.util.CollectionUtils;

import org.apache.commons.collections.Transformer;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
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

  public Map<WellKey,ResultValue> findResultValuesByPlate(final Integer plateNumber, final ResultValueType rvt)
  {
    List<ResultValue> result = runQuery(new edu.harvard.med.screensaver.db.Query() {
      public Query buildQuery(Session session)
      {
        // note: use of "left join fetch" instead of just "join" avoids additional selects in indexCollection() call, below
        String hql = "select r from ResultValue r left join fetch r.well w where r.resultValueType.id = :rvtId and w.plateNumber = :plateNumber";
        Query query = session.createQuery(hql);
        query.setParameter("rvtId", rvt.getEntityId());
        query.setParameter("plateNumber", plateNumber);
        return query;
      }
    });
    Map<WellKey,ResultValue> result2 =
      CollectionUtils.indexCollection(result,
                                      new Transformer() { public Object transform(Object rv) { return ((ResultValue) rv).getWell().getWellKey(); } },
                                      WellKey.class, ResultValue.class);
    return result2;
  }

  @SuppressWarnings("unchecked")
  public List<WellKey> findSortedWellKeys(final ResultValueType rvt,
                                          final Map<String,Object> criteria)
  {
    return (List<WellKey>)
    getHibernateTemplate().execute(new HibernateCallback()
    {
      public Object doInHibernate(Session session) throws HibernateException, SQLException
      {
        List<Object> args = new ArrayList<Object>();
        String hql = "select w.id from ResultValueType t join t.resultValues v join v.well w";
        hql += " where t = ?";
        args.add(rvt);
        if (criteria != null && criteria.size() > 0) {
          for (Map.Entry<String,Object> entry : criteria.entrySet()) {
            hql += " and " + entry.getKey() + " = ?";
            args.add(entry.getValue());
          }
        }
        if (rvt.isNumeric()) {
          hql += " order by v.numericValue";
        }
        else {
          hql += " order by v.value";
        }

        Query query = session.createQuery(hql);
        for (int i = 0; i < args.size(); i++) {
          query.setParameter(i, args.get(i));
        }
        return query.list();
      }
    });
  }

  public void deleteScreenResult(ScreenResult screenResult)
  {
    // disassociate ScreenResult from Screen
    screenResult.getScreen().clearScreenResult();

    getHibernateTemplate().delete(screenResult);
    log.debug("deleted " + screenResult);
  }


  // private methods

//  private Query buildQueryForSortedResultValueTypeTableByRange(Session session,
//                                                               List<ResultValueType> selectedRvts,
//                                                               int sortBy,
//                                                               SortDirection sortDirection,
//                                                               ResultValueType positivesOnlyRvt,
//                                                               Integer plateNumber)
//  {
//    assert selectedRvts.size() > 0;
//    assert sortBy < selectedRvts.size();
//    assert positivesOnlyRvt == null || selectedRvts.contains(positivesOnlyRvt);
//
//    StringBuilder hql = new StringBuilder();
//    List<String> selectFields = new ArrayList<String>();
//    List<String> fromClauses = new ArrayList<String>();
//    List<String> whereClauses = new ArrayList<String>();
//    List<String> orderByClauses = new ArrayList<String>();
//    List<Integer> args = new ArrayList<Integer>();
//
//    // TODO: can simplify this code now that we no longer require iteration to generate our HQL string
//    String sortByRvAlias = "rv";
//    selectFields.add("index(" + sortByRvAlias + ")");
//    fromClauses.add("ResultValueType rvt join rvt.resultValues rv");
//    whereClauses.add("rvt.id=?");
//    args.add(selectedRvts.get(Math.max(0, sortBy)).getEntityId());
//
//    if (positivesOnlyRvt != null) {
//      // TODO: this makes the query quite slow, as it has to join all ResultValues for 2 ResultValueTypes
//      fromClauses.add("ResultValueType positivesOnlyRvt join positivesOnlyRvt.resultValues positivesOnlyRv");
//      whereClauses.add("positivesOnlyRv.positive = true");
//      whereClauses.add("index(positivesOnlyRv) = index(rv)");
//      whereClauses.add("positivesOnlyRvt.id=?");
//      args.add(positivesOnlyRvt.getEntityId());
//    }
//
//    if (plateNumber != null) {
//      whereClauses.add("cast(substring(index(rv),1,5),int)=?");
//      args.add(plateNumber);
//    }
//
//    // TODO: always do a secondary sort on plate/well ascending (if sorting primarily on another column)
//    String sortDirStr = sortDirection.equals(SortDirection.ASCENDING)? " asc" : " desc";
//    if (sortBy >= 0) {
//      if (selectedRvts.get(sortBy).isNumeric()) {
//        orderByClauses.add(sortByRvAlias + ".numericValue" + sortDirStr);
//      }
//      else {
//        orderByClauses.add(sortByRvAlias + ".value" + sortDirStr);
//      }
//    }
//    else if (sortBy == SORT_BY_PLATE_WELL) {
//      orderByClauses.add("index(" + sortByRvAlias + ")" + sortDirStr);
//    }
//    else if (sortBy == SORT_BY_WELL_PLATE) {
//      orderByClauses.add("substring(index(" + sortByRvAlias + "),7,3)" + sortDirStr);
//      orderByClauses.add("substring(index(" + sortByRvAlias + "),1,5)");
//    }
//    else if (sortBy == SORT_BY_ASSAY_WELL_TYPE) {
//      orderByClauses.add(sortByRvAlias + ".assayWellType" + sortDirStr);
//    }
//
//    hql.append("select ").append(StringUtils.makeListString(selectFields, ", "));
//    hql.append(" from ").append(StringUtils.makeListString(fromClauses, ", "));
//    hql.append(" where ").append(StringUtils.makeListString(whereClauses, " and "));
//    hql.append(" order by ").append(StringUtils.makeListString(orderByClauses, ", "));
//
//    if (log.isDebugEnabled()) {
//      log.debug("buildQueryForSortedResultValueTypeTableByRange() executing HQL: " + hql.toString());
//    }
//
//    Query query = session.createQuery(hql.toString());
//    for (int i = 0; i < args.size(); ++i) {
//      query.setInteger(i, args.get(i));
//    }
//    return query;
//  }
//
//  /**
//   * @motivation optimization: querying for too many wellKeys in the same query
//   *             is slower than breaking up the queries into subsets and
//   *             recombining the results in memory.
//   */
//  private Map<WellKey,List<ResultValue>> findRelatedResultValuesInParts(Session session,
//                                                                        Set<WellKey> wellKeys,
//                                                                        List<ResultValueType> selectedRvts) {
//    Map<WellKey,List<ResultValue>> result = new HashMap<WellKey,List<ResultValue>>();
//    ArrayList<WellKey> wellKeysList = new ArrayList<WellKey>(wellKeys);
//    // the size of each subset probably shouldn't be less than 384, since
//    // performance is okay at this value, and querying for <=384 wellKeys is a
//    // very common (384 well plate size)
//    int subsetSize = 384;
//    int i = 0;
//    while (i * subsetSize < wellKeys.size()) {
//      List<WellKey> wellKeysSubset =
//        wellKeysList.subList(i * subsetSize,
//                             Math.min((i + 1) * subsetSize, wellKeysList.size()));
//      result.putAll(findRelatedResultValues(session, wellKeysSubset, selectedRvts));
//      ++i;
//    }
//    assert result.size() == wellKeys.size();
//    return result;
//  }
//
}
