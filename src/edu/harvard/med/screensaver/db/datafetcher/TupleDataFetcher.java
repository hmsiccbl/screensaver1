// $HeadURL:$
// $Id$

// Copyright © 2006, 2010 by the President and Fellows of Harvard College.

// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.datafetcher;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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
import edu.harvard.med.screensaver.ui.table.Criterion;
import edu.harvard.med.screensaver.ui.table.Criterion.Operator;

/**
 * DataFetcher that fetches tuples from persistent storage. Each tuple property is specified via {@link PropertyPath},
 * to be specified via {@link #setPropertiesToFetch}.
 */
public class TupleDataFetcher<E extends AbstractEntity,K> implements DataFetcher<Tuple<K>,K,PropertyPath<E>>
{
  private static Logger log = Logger.getLogger(TupleDataFetcher.class);

  protected GenericEntityDAO _dao;

  private Class<E> _rootEntityClass;
  private LinkedHashSet<PropertyPath<E>> _properties;
  private Map<PropertyPath<E>,List<? extends Criterion<?>>> _criteria = Collections.emptyMap();
  private List<PropertyPath<E>> _orderByProperties = Collections.emptyList();

  private PropertyPath<E> _idProperty;

  public TupleDataFetcher(Class<E> rootEntityClass, GenericEntityDAO dao)
  {
    _rootEntityClass = rootEntityClass;
    _dao = dao;
    _idProperty = new PropertyPath<E>(_rootEntityClass, "id");
  }

  /**
   * Allows subclass to add a fixed set of restrictions that will narrow the
   * query result to a particular entity domain. For example, entities sharing
   * the same parent, an arbitrary set of entity keys, etc. This restriction is
   * effectively AND'ed with the criteria set via {@link #setFilteringCriteria(Map)}.
   * 
   * @motivation This method is a convenience to the client code, which will
   *             usually use {@link #setFilteringCriteria(Map)} for user-specified,
   *             column-associated criteria. Use of this method allows the
   *             client code to set a top-level restriction that is respected
   *             even as the user modifies column-based filtering criteria.
   */
  public void addDomainRestrictions(HqlBuilder hql)
  {}

  public void setPropertiesToFetch(List<PropertyPath<E>> properties)
  {
    _properties = Sets.newLinkedHashSet();
    //_properties.add(_idProperty);
    _properties.addAll(properties);
  }

  @Override
  public void setFilteringCriteria(Map<PropertyPath<E>,List<? extends Criterion<?>>> criteria)
  {
    _criteria = criteria;
  }

  @Override
  public void setOrderBy(List<PropertyPath<E>> orderByProperties)
  {
    _orderByProperties = orderByProperties;
  }

  @Override
  public List<K> findAllKeys()
  {
    log.debug("fetching sorted & filtered key set");
    return _dao.runQuery(buildFetchKeysQuery());
  }

  @Override
  public List<Tuple<K>> fetchAllData()
  {
    log.debug("fetching all data");
    return Lists.newArrayList(doFetchData(Collections.<K>emptySet()).values());
  }

  @Override
  @SuppressWarnings("unchecked")
  public Map<K,Tuple<K>> fetchData(Set<K> keys)
  {
    if (log.isDebugEnabled()) {
      log.debug("fetching data subset: " + keys);
    }
    Map<K,Tuple<K>> result = doFetchData(keys);
    assert result.size() == keys.size() : "fetch data query result did not return all requested entities";
    return result;
  }

  private Query<E> buildFetchKeysQuery()
  {
    return new Query<E>() {
      public List<E> execute(Session session)
      {
        HqlBuilder hql = new HqlBuilder();
        Map<RelationshipPath<E>,String> path2Alias = new HashMap<RelationshipPath<E>,String>();
        String propertyEntityAlias = getOrCreateJoin(hql, new RelationshipPath<E>(_rootEntityClass, ""), path2Alias);

        // projection
        hql.select(propertyEntityAlias, "id");

        // order by
        // note: we add 'order by' clauses before restrictions, to ensure that
        // any left joins that we need are in fact created as left joins
        for (PropertyPath<E> property : _orderByProperties) {
          String alias = getOrCreateJoin(hql, property.getRelationshipPath(), path2Alias, JoinType.LEFT);
          hql.orderBy(alias, property.getPropertyName());
        }

        // filtering restrictions
        for (Map.Entry<PropertyPath<E>,List<? extends Criterion<?>>> entry : _criteria.entrySet()) {
          PropertyPath<E> propertyPath = entry.getKey();
          for (Criterion<?> criterion : entry.getValue()) {
            if (!criterion.isUndefined()) {
              String alias = getOrCreateJoin(hql, 
                                             propertyPath.getRelationshipPath(), 
                                             path2Alias,
                                             criterion.getOperator() == Operator.EMPTY ? JoinType.LEFT : JoinType.INNER);
              hql.where(alias, propertyPath.getPropertyName(), criterion.getOperator(), criterion.getValue());
            }
          }
        }

        // restrict to a domain
        addDomainRestrictions(hql);

        // we add a group by clause in case the Relationships provided by
        // client code does not restrict each relationship to single entity
        // (note: we can't use 'distinct', w/o also adding the order by fields to the select clause, which would be inefficient)
        hql.groupBy(getRootEntityAlias(), "id");

        if (log.isDebugEnabled()) {
          log.debug("fetch keys query: " + hql);
        }
        return hql.toQuery(session, true).list();
      }
    };
  }

  /**
   * @param keys if null, fetches all entities for the root entity type (subject
   *          to normal column criteria)
   */
  protected Map<K,Tuple<K>> doFetchData(Set<K> keys)
  {
    if (_properties == null) {
      throw new IllegalStateException("properties to fetch is not set");
    }
    Map<K,Tuple<K>> tuples = Maps.newHashMapWithExpectedSize(keys.size());
    for (PropertyPath<E> propertyPath : _properties) {
      if (log.isDebugEnabled()) {
        log.debug("fetching " + keys.size() + " values for property " + propertyPath);
      }
      List<Object[]> result = _dao.runQuery(buildQueryForProperty(propertyPath, keys));
      for (Object[] row : result) {
        assert row.length == 2;
        setTupleProperty(getOrCreateTuple(tuples, (K) row[0]), propertyPath, row[1]);
      }
    }
    return tuples;
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

  private Query buildQueryForProperty(PropertyPath<E> path, Set<K> keys)
  {
    final HqlBuilder hql = new HqlBuilder();

    Map<RelationshipPath<E>,String> path2Alias = Maps.newHashMap();
    String propertyEntityAlias = getOrCreateJoin(hql,
                                                 path.getRelationshipPath(),
                                                 path2Alias);

    hql.select(getRootEntityAlias(), "id");
    if (path.getPropertyName().equals(PropertyPath.COLLECTION_OF_VALUES)) {
      // retrieve entire element as a tuple property
      hql.select(propertyEntityAlias);
    }
    else if (path.getPropertyName().equals(PropertyPath.FULL_ENTITY)) {
      // retrieve entire entity as a tuple property
      hql.select(propertyEntityAlias);
    }
    else {
      hql.select(propertyEntityAlias, path.getPropertyName());
    }

    if (!keys.isEmpty()) {
      hql.whereIn(getRootEntityAlias(), "id", keys);
    }
    else {
      // if explicit set of keys has not been provided, we must still
      // restrict result with top-level restrictions
      addDomainRestrictions(hql);
    }

    // we apply a 'distinct' filter in case the Relationships provided by
    // client code does not restrict each relationship to single entity
    //hql.distinctRootEntities();

    if (log.isDebugEnabled()) {
      log.debug("fetch data query for property " + path + ": " + hql);
    }
    
    return new Query() {
      @Override
      public List execute(Session session)
      {
        return hql.toQuery(session, true).list();
      }
    };
  }

  protected String getRootEntityAlias()
  {
    return "x";
  }

  /**
   * Update the HQL to retrieve data for the specified relationship path. All
   * intermediate paths are added recursively, and path2Alias is updated
   * accordingly.
   * <p>
   * A new LEFT_FETCH join will be created iff the <i>basic<i> path does
   * not match the <i>basic</i> path of a previously created join. A new
   * INNER or LEFT join will be created iff the <i>restricted<i> path does not
   * match the <i>restricted</i> path of a previously created join. The effect
   * of this is that INNER and LEFT joins will be created unique joins for paths
   * that have the same path nodes, but have different restrictions on this path
   * nodes; LEFT_FETCH joins will only be created once for each distinct path,
   * ignoring any restrictions on the path nodes.
   * <p>
   * Why the special behavior for LEFT_FETCH joins? In short, because Hibernate
   * HQL does not support the use of WITH with a LEFT JOIN FETCH. This
   * ultimately prevents us from being able to retrieve rows that have null
   * values in one of the joined entity tables. So we have to "stack up" all
   * data for a given entity type that we could otherwise joined multiple times.
   *
   * @param hql
   * @param path the relationship path, from the root entity of this
   *          DataFetcher, that will be represented by the returned Criteria
   *          object
   * @param path2Alias
   * @param joinType
   * @return the alias for leaf node in the relationship path
   */
  protected String getOrCreateJoin(HqlBuilder hql,
                                   RelationshipPath<E> path,
                                   Map<RelationshipPath<E>,String> path2Alias,
                                   JoinType joinType)
  {
    if (path instanceof PropertyPath) {
      throw new IllegalArgumentException("path arg must not be a PropertyPath; use path.getRelationshipPath() instead");
    }
    if (joinType != JoinType.INNER && joinType != JoinType.LEFT) {
      throw new IllegalArgumentException("only INNER and LEFT join types are valid");
    }

    String alias = null;
    alias = path2Alias.get(path);

    if (alias == null) {
      if (path.getPathLength() == 0) {
        alias = getRootEntityAlias();
        path2Alias.put(path, alias);
        hql.from(path.getRootEntityClass(), alias);
        log.debug("created alias for root entity: " + path);
        return alias;
      }
      // create joins for path ancestry nodes, recursively
      String parentAlias = getOrCreateJoin(hql, path.getAncestryPath(), path2Alias, joinType);
      // create join for path leaf node
      alias = "x" + path2Alias.size();
      path2Alias.put(path, alias);
      hql.from(parentAlias, path.getLeaf(), alias, joinType);

      // add relationship path restrictions
      PropertyNameAndValue restrictionPropertyNameAndValue =
        path.getLeafRestrictionPropertyNameAndValue();
      if (restrictionPropertyNameAndValue != null) {
        String propName = restrictionPropertyNameAndValue.getName();
        Object value = restrictionPropertyNameAndValue.getValue();
        if (value instanceof AbstractEntity) {
          // handle restriction values that are AbstractEntity types;
          // arguably, Hibernate could handle this case with its WITH keyword,
          // but alas it does not! Argh!
          propName += ".id";
          value = ((AbstractEntity) value).getEntityId();
        }
        hql.restrictFrom(alias, propName, Operator.EQUAL, value);
      }
    }
    return alias;
  }

  protected String getOrCreateJoin(HqlBuilder hql,
                                   RelationshipPath<E> path,
                                   Map<RelationshipPath<E>,String> path2Alias)
  {
    return getOrCreateJoin(hql, path, path2Alias, JoinType.LEFT);
  }
}
