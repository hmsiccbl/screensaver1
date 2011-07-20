// $HeadURL$
// $Id$

// Copyright © 2006, 2010 by the President and Fellows of Harvard College.

// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.datafetcher;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.Query;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.db.hqlbuilder.JoinType;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.meta.Cardinality;
import edu.harvard.med.screensaver.model.meta.PropertyNameAndValue;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.ui.arch.datatable.Criterion.Operator;

/**
 * DataFetcher that fetches tuples from persistent storage. Each tuple property is specified via {@link PropertyPath},
 * to be specified via {@link #setPropertiesToFetch}.
 */
public class TupleDataFetcher<E extends AbstractEntity,K> extends PropertyPathDataFetcher<Tuple<K>,E,K>
{
  private static Logger log = Logger.getLogger(TupleDataFetcher.class);

  private static final String COV_ALIAS_SUFFIX = "COV";

  private PropertyPath<E> _idProperty;

  public TupleDataFetcher(Class<E> rootEntityClass, GenericEntityDAO dao)
  {
    super(rootEntityClass, dao);
    _idProperty = RelationshipPath.from(_rootEntityClass).toId();
  }

  @Override
  public List<Tuple<K>> fetchAllData()
  {
    log.debug("fetching all data");
    return Lists.newArrayList(doFetchData(Collections.<K>emptySet()).values());
  }

  @Override
  public Map<K,Tuple<K>> fetchData(Set<K> keys)
  {
    if (log.isDebugEnabled()) {
      log.debug("fetching data subset: " + keys);
    }
    Map<K,Tuple<K>> result = doFetchData(keys);
    assert result.size() == keys.size() : "fetch data query result did not return all requested entities";
    return result;
  }

  /**
   * @param keys if null, fetches all entities for the root entity type (subject
   *          to normal column criteria)
   */
  protected Map<K,Tuple<K>> doFetchData(Set<K> keys)
  {
    // collate properties into groups of PropertyPaths having same RelationshipPath; 
    // this will allow us to execute one query for each group of properties that are from the same entity type 
    Multimap<RelationshipPath<E>,PropertyPath<E>> pathGroups =
      Multimaps.index(getProperties(), new Function<PropertyPath<E>,RelationshipPath<E>>() {
        public RelationshipPath<E> apply(PropertyPath<E> p)
        {
          return p.getAncestryPath();
        }
      });

    Map<K,Tuple<K>> tuples = Maps.newHashMapWithExpectedSize(keys.size());
    for (Collection<PropertyPath<E>> propertyPaths : pathGroups.asMap().values()) {
      List<PropertyPath<E>> orderedPropertyPaths = Lists.newArrayList(propertyPaths);
      if (log.isDebugEnabled()) {
        log.debug("fetching " + keys.size() + " values for properties " + orderedPropertyPaths);
      }
      List<Object[]> result = _dao.runQuery(buildQueryForProperty(orderedPropertyPaths, keys));
      packageResultIntoTuples(tuples, orderedPropertyPaths, result);
    }
    return tuples;
  }

  private void packageResultIntoTuples(Map<K,Tuple<K>> tuples, List<PropertyPath<E>> orderedPropertyPaths, List<Object[]> result)
  {
    for (Object[] row : result) {
      assert row.length == orderedPropertyPaths.size() + 1;
      for (int i = 0; i < orderedPropertyPaths.size(); ++i) {
        setTupleProperty(getOrCreateTuple(tuples, (K) row[0]), orderedPropertyPaths.get(i), row[i + 1]);
      }
    }
  }

  private void setTupleProperty(Tuple<K> tuple, PropertyPath<E> propertyPath, Object propertyValue)
  {
    if (propertyPath.getCardinality() == Cardinality.TO_MANY) {
      tuple.addMultiPropertyElement(makePropertyKey(propertyPath), propertyValue);
    }
    else {
      tuple.addProperty(makePropertyKey(propertyPath), propertyValue);
    }
  }

  private Tuple<K> getOrCreateTuple(Map<K,Tuple<K>> tuples, K tupleKey)
  {
    if (!tuples.containsKey(tupleKey)) {
      tuples.put(tupleKey, new Tuple<K>(tupleKey));
    }
    Tuple<K> tuple = tuples.get(tupleKey);
    assert tuple != null;
    return tuple;
  }

  public static String makePropertyKey(PropertyPath<?> propertyPath)
  {
    return propertyPath.toString().split("\\.", 2)[1];
  }

  private Query buildQueryForProperty(List<PropertyPath<E>> propertyPaths, Set<K> keys)
  {
    final HqlBuilder hql = new HqlBuilder();
    Map<RelationshipPath<E>,String> path2Alias = Maps.newHashMap();
    String rootEntityIdPropertyName = "id";
    String propertyEntityAlias;
    assert propertyPaths.size() >= 1;
    RelationshipPath<E> relPath = propertyPaths.get(0).getAncestryPath();

    // if possible, eliminate the root entity from the query, saving a join operation.
    // this can only occur if the property to be retrieved is from an entity that is directly related to the root entity via a to-one relationship
    if (!!!keys.isEmpty()) { // cannot apply this optimization if we're asked to fetch all data, since eliminating the root entity can break the expectations of addDomainRestrictions() implementations, which is called below 
      Iterator<String> inversePathIter = relPath.inversePathIterator();
      if (inversePathIter.hasNext()) {
        String inverseEntityName = inversePathIter.next();
        if (inverseEntityName != null) {
          // select tuple ID property from the second entity, rather than the root entity
          path2Alias.put(relPath, getRootAlias());
          assert relPath.entityClassIterator().hasNext();
          hql.from(relPath.entityClassIterator().next(), getRootAlias());
          rootEntityIdPropertyName = inverseEntityName + "." + rootEntityIdPropertyName;

          // explicitly add restriction from rootEntity->relatedEntity, since this restriction would otherwise be lost
          Iterator<PropertyNameAndValue> restrictionIterator = relPath.restrictionIterator();
          PropertyNameAndValue restriction = restrictionIterator.hasNext() ? restrictionIterator.next() : null;
          if (restriction != null) {
            hql.where(getRootAlias(), restriction.getName(), Operator.EQUAL, restriction.getValue());
          }
        }
      }
    }

    propertyEntityAlias = getOrCreateJoin(hql,
                                          relPath,
                                          path2Alias,
                                          JoinType.LEFT);
    hql.select(getRootAlias(), rootEntityIdPropertyName);

    for (PropertyPath<E> propertyPath : propertyPaths) {
      if (propertyPath.isCollectionOfValues()) {
        // retrieve entire element as a tuple property
        String covAlias = getOrCreateJoin(hql,
                                          propertyPath,
                                          path2Alias,
                                          JoinType.LEFT);
        hql.select(covAlias);
      }
      else if (propertyPath.getPropertyName().equals(PropertyPath.FULL_ENTITY)) {
        // retrieve entire entity as a tuple property
        hql.select(propertyEntityAlias);
      }
      else {
        hql.select(propertyEntityAlias, propertyPath.getPropertyName());
      }
    }

    if (!keys.isEmpty()) {
      hql.whereIn(getRootAlias(), rootEntityIdPropertyName, keys);
    }
    else {
      // if explicit set of keys has not been provided, we must still
      // restrict result with top-level restrictions
      addDomainRestrictions(hql);
    }

    if (log.isDebugEnabled()) {
      log.debug("fetch data query for properties " + propertyPaths + ": " + hql);
    }
    
    return new Query() {
      @Override
      public List execute(Session session)
      {
        return hql.toQuery(session, true).list();
      }
    };
  }
}
