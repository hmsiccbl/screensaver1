// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/db/ScreenResultsDAOImpl.java $
// $Id: ScreenResultsDAOImpl.java 1725 2007-08-20 20:43:25Z ant4 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
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
  public List<AnnotationType> findAllAnnotationTypes()
  {
    return getHibernateTemplate().find("from AnnotationType");
  }

  @SuppressWarnings("unchecked")
  public List<AnnotationType> findAllAnnotationTypesForReagent(ReagentVendorIdentifier rvi)
  {
    return getHibernateTemplate().find("select at from AnnotationValue av join av.annotationType at " +
                                       "where av.reagentVendorIdentifier.vendorName=? " +
                                       "and av.reagentVendorIdentifier.vendorIdentifier=?",
                                       new Object[] { rvi.getVendorName(), rvi.getVendorIdentifier() } );
  }

  @SuppressWarnings("unchecked")
  public Map<ReagentVendorIdentifier,List<AnnotationValue>> findAnnotationValues(final Collection<ReagentVendorIdentifier> reagentVendorIds,
                                                                                 final List<AnnotationType> annotationTypes)
  {
    return (Map<ReagentVendorIdentifier,List<AnnotationValue>>) getHibernateTemplate().execute(new HibernateCallback()
    {
      public Object doInHibernate(Session session) throws HibernateException, SQLException
      {
        return findRelatedAnnotationValuesInParts(session,
                                                  reagentVendorIds,
                                                  annotationTypes);
      }
    });
  }

  @SuppressWarnings("unchecked")
  public List<AnnotationValue> findAnnotationValuesForReagent(ReagentVendorIdentifier rvi)
  {
    // note: we eager fetch the related AnnotationTypes, as a courtesy to the calling code (it's reasonable to expect the calling code will need the AnnotationType)
    return getHibernateTemplate().find("from AnnotationValue av left join fetch av.annotationType " +
      "where av.reagentVendorIdentifier.vendorName=? " +
      "and av.reagentVendorIdentifier.vendorIdentifier=?",
      new Object[] { rvi.getVendorName(), rvi.getVendorIdentifier() } );
    // this simpler version doesn't work, for some reason
    // return getHibernateTemplate().find("from AnnotationValue av where av.reagentVendorIdentifier=?", rvi);
  }

  @SuppressWarnings("unchecked")
  public Map<ReagentVendorIdentifier,List<AnnotationValue> > findSortedAnnotationValuesTableByRange(final List<AnnotationType> annotationTypes,
                                                                                                    final int sortBy,
                                                                                                    final SortDirection sortDirection,
                                                                                                    final int fromIndex,
                                                                                                    final Integer rowsToFetch,
                                                                                                    final Map<String,Object> criteria) {
    Map<ReagentVendorIdentifier,List<AnnotationValue>> mapResult = (Map<ReagentVendorIdentifier,List<AnnotationValue>>)
    getHibernateTemplate().execute(new HibernateCallback()
    {
      public Object doInHibernate(Session session) throws HibernateException, SQLException
      {
        Query query = buildQueryForSortedAnnotationValuesTableByRange(session,
                                                                      annotationTypes,
                                                                      sortBy,
                                                                      sortDirection,
                                                                      criteria);
        Map<ReagentVendorIdentifier,List<AnnotationValue>> mapResult = new LinkedHashMap<ReagentVendorIdentifier,List<AnnotationValue>>();
        ScrollableResults scrollableResults = query.scroll();
        if (scrollableResults.setRowNumber(fromIndex) && (rowsToFetch == null || rowsToFetch > 0)) {
          int rowCount = 0;
          do {
            Object[] valuesArray = scrollableResults.get();
            List<AnnotationValue> values = new ArrayList<AnnotationValue>(annotationTypes.size());
            mapResult.put((ReagentVendorIdentifier) valuesArray[0], values);
          } while (scrollableResults.next() && (rowsToFetch == null || ++rowCount < rowsToFetch));

          // now add the AnnotationValues
          Map<ReagentVendorIdentifier,List<AnnotationValue>> secondaryMapResult =
            findRelatedAnnotationValuesInParts(session,
                                               mapResult.keySet(),
                                               annotationTypes);
          assert secondaryMapResult.keySet().equals(mapResult.keySet()) : "primary and secondary queries returned different results that do not have 1-1 mapping of keys";
          for (Map.Entry<ReagentVendorIdentifier,List<AnnotationValue>> entry : mapResult.entrySet()) {
            List<AnnotationValue> relatedAnnotationValues = secondaryMapResult.get(entry.getKey());
            entry.getValue().addAll(relatedAnnotationValues);
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
    selectFields.add("av.reagentVendorIdentifier");
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
      // secondary sort order
      orderByClauses.add(sortByAvAlias + ".reagentVendorIdentifier asc");
    }
    else {
      orderByClauses.add(sortByAvAlias + ".reagentVendorIdentifier " + sortDirStr);
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
  private Map<ReagentVendorIdentifier,List<AnnotationValue>> findRelatedAnnotationValuesInParts(Session session,
                                                                                                Collection<ReagentVendorIdentifier> reagentVendorIds,
                                                                                                List<AnnotationType> annotationTypes) {
    Map<ReagentVendorIdentifier,List<AnnotationValue>> result = new HashMap<ReagentVendorIdentifier,List<AnnotationValue>>();
    ArrayList<ReagentVendorIdentifier> reagentVendorIdList = new ArrayList<ReagentVendorIdentifier>(reagentVendorIds);
    int subsetSize = 128;
    int i = 0;
    while (i * subsetSize < reagentVendorIds.size()) {
      List<ReagentVendorIdentifier> reagentVendorIdsSubset =
        reagentVendorIdList.subList(i * subsetSize,
                                    Math.min((i + 1) * subsetSize, reagentVendorIdList.size()));
      result.putAll(findRelatedAnnotationValues(session, reagentVendorIdsSubset, annotationTypes));
      ++i;
    }
    assert result.size() == reagentVendorIds.size();
    return result;
  }

  private Map<ReagentVendorIdentifier,List<AnnotationValue>> findRelatedAnnotationValues(Session session,
                                                                                         Collection<ReagentVendorIdentifier> reagentVendorIds,
                                                                                         List<AnnotationType> annotationTypes)
  {
    Map<ReagentVendorIdentifier,List<AnnotationValue>> result = new HashMap<ReagentVendorIdentifier,List<AnnotationValue>>();

    if (reagentVendorIds.size() == 0 || annotationTypes.size() == 0) {
      return result;
    }

    Map<Number,Integer> atId2Pos = new HashMap<Number,Integer>(annotationTypes.size());

    List<String> rviIds = new ArrayList<String>();
    for (ReagentVendorIdentifier rviId : reagentVendorIds) {
      rviIds.add(rviId.getId());
    }

    List<Number> atIds = new ArrayList<Number>();
    for (int i = 0; i < annotationTypes.size(); i++) {
      AnnotationType at = annotationTypes.get(i);
      atIds.add((Number) at.getEntityId());
      atId2Pos.put((Number) at.getEntityId(), i);
    }

    StringBuilder sql = new StringBuilder();
    sql.append("select av.annotation_type_id, av.vendor_name, av.vendor_identifier, av.value ").
    append("from annotation_value av ").
    append("where (av.annotation_type_id in (:atIds)) ").
    append("and (av.vendor_name || ':' || av.vendor_identifier in (:rviIds))");

    Query query = session.createSQLQuery(sql.toString());
    query.setParameterList("atIds", atIds);
    query.setParameterList("rviIds", rviIds);
    for (Iterator<?> iter = query.list().iterator(); iter.hasNext();) {
      int field = 0;
      Object[] row = (Object[]) iter.next();
      Number atId = (Number) row[field++];
      ReagentVendorIdentifier reagentVendorId = new ReagentVendorIdentifier(row[field++].toString(),
                                                                            row[field++].toString());
      List<AnnotationValue> annotationValues = result.get(reagentVendorId);
      if (annotationValues == null) {
        annotationValues = new ArrayList<AnnotationValue>(annotationTypes.size());
        CollectionUtils.fill(annotationValues, null, annotationTypes.size());
        result.put(reagentVendorId, annotationValues);
      }
      String textValue = (String) row[field++];

      AnnotationType at = annotationTypes.get(atId2Pos.get(atId));
      AnnotationValue annotationValue = at.createAnnotationValueDTO(reagentVendorId, textValue);
      annotationValues.set(atId2Pos.get(atId), annotationValue);
    }
    return result;
  }
}
