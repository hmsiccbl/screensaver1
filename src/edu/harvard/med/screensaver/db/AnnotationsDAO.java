// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/db/ScreenResultsDAOImpl.java $
// $Id: ScreenResultsDAOImpl.java 1725 2007-08-20 20:43:25Z ant4 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.AnnotationValue;
import edu.harvard.med.screensaver.util.CollectionUtils;
import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

public class AnnotationsDAO extends AbstractDAO
{
  // static members

  private static Logger log = Logger.getLogger(AnnotationsDAO.class);

  public static int SORT_BY_VENDOR_ID = -1;

  // instance data members

  // public constructors and methods

  /**
   * @motivation for CGLIB dynamic proxy creation
   */
  public AnnotationsDAO()
  {
  }

  @SuppressWarnings("unchecked")
  public Map<String,List<AnnotationValue> > findSortedAnnotationValuesTableByRange(final List<AnnotationType> annotationTypes,
                                                                                   final int sortBy,
                                                                                   final SortDirection sortDirection,
                                                                                   final int fromIndex,
                                                                                   final Integer rowsToFetch,
                                                                                   final Map<String,Object> criteria) {
    Map<String,List<AnnotationValue>> mapResult = (Map<String,List<AnnotationValue>>)
    getHibernateTemplate().execute(new HibernateCallback()
    {
      public Object doInHibernate(Session session) throws HibernateException, SQLException
      {
        Query query = buildQueryForSortedAnnotationValuesTableByRange(session,
                                                                      annotationTypes,
                                                                      sortBy,
                                                                      sortDirection,
                                                                      criteria);
        Map<String,List<AnnotationValue>> mapResult = new LinkedHashMap<String,List<AnnotationValue>>();
        ScrollableResults scrollableResults = query.scroll();
        if (scrollableResults.setRowNumber(fromIndex) && (rowsToFetch == null || rowsToFetch > 0)) {
          int rowCount = 0;
          do {
            Object[] valuesArray = scrollableResults.get();
            List<AnnotationValue> values = new ArrayList<AnnotationValue>(annotationTypes.size());
            mapResult.put(valuesArray[0].toString(), values);
          } while (scrollableResults.next() && (rowsToFetch == null || ++rowCount < rowsToFetch));

          // now add the AnnotationValues
          Map<String,List<AnnotationValue>> secondaryMapResult =
            findRelatedAnnotationValues/*InParts*/(session,
                                                   mapResult.keySet(),
                                                   annotationTypes);
          for (Map.Entry<String,List<AnnotationValue>> entry : mapResult.entrySet()) {
            entry.getValue().addAll(secondaryMapResult.get(entry.getKey()));
          }
        }
        return mapResult;
      }
    });
    return mapResult;
  }


  // private methods

  private Query buildQueryForSortedAnnotationValuesTableByRange(Session session,
                                                                List<AnnotationType> annotationTypes,
                                                                int sortBy,
                                                                SortDirection sortDirection,
                                                                Map<String,Object> criteria)
  {
    assert annotationTypes.size() > 0;
    assert sortBy < annotationTypes.size();

    StringBuilder hql = new StringBuilder();
    List<String> selectFields = new ArrayList<String>();
    List<String> fromClauses = new ArrayList<String>();
    List<String> whereClauses = new ArrayList<String>();
    List<String> orderByClauses = new ArrayList<String>();
    List<Object> args = new ArrayList<Object>();

    String sortByAvAlias = "av";
    selectFields.add("av.vendorIdentifier");
    fromClauses.add("AnnotationType at join at.annotationValues " + sortByAvAlias);
    whereClauses.add("at.id=?");
    args.add(annotationTypes.get(Math.max(0, sortBy)).getEntityId());

    if (criteria != null) {
      for (String property : criteria.keySet()) {
        whereClauses.add(property + "=?");
        args.add(criteria.get(property));
      }
    }

    String sortDirStr = sortDirection.equals(SortDirection.ASCENDING)? " asc" : " desc";
    if (sortBy >= 0) {
      if (annotationTypes.get(sortBy).isNumeric()) {
        orderByClauses.add(sortByAvAlias + ".numericValue" + sortDirStr);
      }
      else {
        orderByClauses.add(sortByAvAlias + ".value" + sortDirStr);
      }
    }
    else {
      orderByClauses.add(sortByAvAlias + ".vendorIdentifier " + sortDirStr);
    }

    hql.append("select ").append(StringUtils.makeListString(selectFields, ", "));
    hql.append(" from ").append(StringUtils.makeListString(fromClauses, ", "));
    hql.append(" where ").append(StringUtils.makeListString(whereClauses, " and "));
    hql.append(" order by ").append(StringUtils.makeListString(orderByClauses, ", "));

    if (log.isDebugEnabled()) {
      log.debug("buildQueryForSortedAnnotationValuesTableByRange() executing HQL: " + hql.toString());
    }

    Query query = session.createQuery(hql.toString());
    for (int i = 0; i < args.size(); ++i) {
      query.setParameter(i, args.get(i));
    }
    return query;
  }

  /**
   * @motivation optimization: querying for too many wellKeys in the same query
   *             is slower than breaking up the queries into subsets and
   *             recombining the results in memory.
   */
  private Map<String,List<AnnotationValue>> findRelatedAnnotationValuesInParts(Session session,
                                                                               Set<String> vendorIds,
                                                                               List<AnnotationType> annotationTypes) {
    Map<String,List<AnnotationValue>> result = new HashMap<String,List<AnnotationValue>>();
    ArrayList<String> vendorIdList = new ArrayList<String>(vendorIds);
    // the size of each subset probably shouldn't be less than 384, since
    // performance is okay at this value, and querying for <=384 wellKeys is a
    // very common (384 well plate size)
    int subsetSize = 384;
    int i = 0;
    while (i * subsetSize < vendorIds.size()) {
      List<String> vendorIdsSubset =
        vendorIdList.subList(i * subsetSize,
                             Math.min((i + 1) * subsetSize, vendorIdList.size()));
      result.putAll(findRelatedAnnotationValues(session, vendorIdsSubset, annotationTypes));
      ++i;
    }
    assert result.size() == vendorIds.size();
    return result;
  }

  private Map<String,List<AnnotationValue>> findRelatedAnnotationValues(Session session,
                                                                        Collection<String> vendorIds,
                                                                        List<AnnotationType> annotationTypes)
  {
    Map<Number,Integer> atId2Pos = new HashMap<Number,Integer>(annotationTypes.size());
    String vendorIdsList = StringUtils.makeListString(StringUtils.wrapStrings(vendorIds, "'", "'"), ",");
    List<Number> atIds = new ArrayList<Number>();
    for (int i = 0; i < annotationTypes.size(); i++) {
      AnnotationType at = annotationTypes.get(i);
      atIds.add((Number) at.getEntityId());
      atId2Pos.put((Number) at.getEntityId(), i);
    }
    String atIdsList = StringUtils.makeListString(StringUtils.wrapStrings(atIds, "'", "'"), ",");

    StringBuilder sql = new StringBuilder();
    sql.append("select av.annotation_type_id, av.vendor_identifier, av.value, av.numeric_value ").
    append("from annotation_value av ").
    append("where (av.annotation_type_id in (").append(atIdsList).
    append(")) and (av.vendor_identifier in (").append(vendorIdsList).append("))");

    Map<String,List<AnnotationValue>> result = new HashMap<String,List<AnnotationValue>>();
    Query query = session.createSQLQuery(sql.toString());
    for (Iterator<?> iter = query.list().iterator(); iter.hasNext();) {
      int field = 0;
      Object[] row = (Object[]) iter.next();
      Number atId = (Number) row[field++];
      String vendorId = new String(row[field++].toString());
      List<AnnotationValue> annotationValues = result.get(vendorId);
      if (annotationValues == null) {
        annotationValues = new ArrayList<AnnotationValue>(annotationTypes.size());
        CollectionUtils.fill(annotationValues, null, annotationTypes.size());
        result.put(vendorId, annotationValues);
      }
      String textValue = (String) row[field++];
      BigDecimal numValue = (BigDecimal) row[field++];

      AnnotationType at = annotationTypes.get(atId2Pos.get(atId));
      AnnotationValue annotationValue = new AnnotationValue(at, vendorId, textValue, numValue);
      annotationValues.set(atId2Pos.get(atId), annotationValue);
    }
    return result;
  }
}
