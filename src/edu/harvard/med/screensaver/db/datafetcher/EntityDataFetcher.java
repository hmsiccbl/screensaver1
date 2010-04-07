// $HeadURL:
// svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml
// $
// $Id$

// Copyright 2006 by the President and Fellows of Harvard College.

// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.datafetcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.Query;
import edu.harvard.med.screensaver.db.hqlbuilder.Disjunction;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.db.hqlbuilder.JoinType;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.meta.PropertyNameAndValue;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.ui.table.Criterion;
import edu.harvard.med.screensaver.ui.table.Criterion.Operator;

import org.apache.log4j.Logger;
import org.hibernate.Session;

/**
 * DataFetcher that fetches entities or entity networks from persistent storage.
 * As entities have relationships, and thus form entity networks, this
 * DataFetcher allows the network "structure" to be specified via
 * {@link #setRelationshipsToFetch}. Subclasses can also enforce
 * additional, implicit filtering constraints on the data set to be fetched via
 * {@link #addDomainRestrictions(HqlBuilder, Map)}.
 */
public abstract class EntityDataFetcher<E extends AbstractEntity,K> implements DataFetcher<E,K,PropertyPath<E>>
{
  // static members

  private static Logger log = Logger.getLogger(EntityDataFetcher.class);


  // instance data members

  protected GenericEntityDAO _dao;

  private Set<RelationshipPath<E>> _relationships;
  private Map<PropertyPath<E>,List<? extends Criterion<?>>> _criteria = Collections.emptyMap();
  private List<PropertyPath<E>> _orderByProperties = Collections.emptyList();
  private Class<E> _rootEntityClass;


  // public constructors and methods

  protected EntityDataFetcher(Class<E> rootEntityClass, GenericEntityDAO dao)
  {
    _rootEntityClass = rootEntityClass;
    _dao = dao;
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
  protected abstract void addDomainRestrictions(HqlBuilder hql,
                                                Map<RelationshipPath<E>,String> path2Alias);

  public void setRelationshipsToFetch(List<RelationshipPath<E>> relationships)
  {
    _relationships = new HashSet<RelationshipPath<E>>(relationships);
  }

  // DataFetcher interface methods

  public void setFilteringCriteria(Map<PropertyPath<E>,List<? extends Criterion<?>>> criteria)
  {
    _criteria = criteria;
  }

  public void setOrderBy(List<PropertyPath<E>> orderByProperties)
  {
    _orderByProperties = orderByProperties;
  }

  public List<K> findAllKeys()
  {
    log.debug("fetching sorted & filtered key set");
    return _dao.runQuery(buildFetchKeysQuery());
  }

  public List<E> fetchAllData()
  {
    log.debug("fetching all data");
    return _dao.runQuery(buildFetchDataQuery(null));
  }

  @SuppressWarnings("unchecked")
  public Map<K,E> fetchData(Set<K> keys)
  {
    if (log.isDebugEnabled()) {
      log.debug("fetching data subset: " + keys);
    }
    Map<K,E> result = new HashMap<K,E>(keys.size());
    List<E> queryResult = _dao.runQuery(buildFetchDataQuery(keys));
    for (E entity : queryResult) {
      result.put((K) entity.getEntityId(), entity);
    }
    assert result.size() == keys.size() : "fetch data query result did not return all requested entities";
    return result;
  }


  // private methods

  private Query<E> buildFetchKeysQuery()
  {
    return new Query<E>() {
      public List<E> execute(Session session)
      {
        HqlBuilder hql = new HqlBuilder();
        Map<RelationshipPath<E>,String> path2Alias = new HashMap<RelationshipPath<E>,String>();
        getOrCreateJoin(hql, new RelationshipPath<E>(_rootEntityClass, ""), path2Alias);

        // projection
        hql.select(getRootAlias(), "id");

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
        addDomainRestrictions(hql, path2Alias);

        // we add a group by clause in case the Relationships provided by
        // client code does not restrict each relationship to single entity
        // (note: we can't use 'distinct', w/o also adding the order by fields to the select clause, which would be inefficient)
        hql.groupBy(getRootAlias(), "id");

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
  protected Query buildFetchDataQuery(final Set<K> keys)
  {
    if (_relationships == null) {
      throw new IllegalStateException("relationships to fetch is not set");
    }
    return new Query<E>() {
      /**
       * Executes a data fetch query for each path group. This ensures that 1)
       * cross-product result sets are avoided, since different collections are
       * fetched in different queries, 2) a root entity instance is returned for
       * each requested entity key, even if each of the root entity's
       * collections (if any) are empty [in fact, this is only a problem if we
       * attempt to restrict the collections, since doing so breaks left-join
       * semantics; but we're not doing this anymore]
       */  
      public List<E> execute(Session session)
      {
        Map<String,Set<RelationshipPath<E>>> groupedPaths = buildPathGroups();
        Set<E> rootEntities = new HashSet<E>();
        for (String groupName : groupedPaths.keySet()) {
          HqlBuilder hql = buildHqlQueryForPaths(groupedPaths.get(groupName));
          List<E> result = hql.toQuery(session, true).list();
          log.debug("found " + result.size() + " root entities for query group " + groupName); 
          rootEntities.addAll(result);
        }
        
        return new ArrayList<E>(rootEntities);
      }

      private HqlBuilder buildHqlQueryForPaths(Set<RelationshipPath<E>> paths)
      {
        HqlBuilder hql = new HqlBuilder();
        Map<RelationshipPath<E>,String> path2Alias = new HashMap<RelationshipPath<E>,String>();
        Map<String,Disjunction> alias2Restriction = new HashMap<String,Disjunction>();
        getOrCreateFetchJoin(hql, new RelationshipPath<E>(_rootEntityClass, ""), path2Alias, alias2Restriction);

        // add an explicit select clause for the root entity *only*, otherwise
        // Hibernate may return other entity types as well, which will cause the
        // 'distinct' behavior to fail (i.e., DistinctEntityResultTransformer)
        hql.select(getRootAlias());

        for (RelationshipPath<E> path : paths) {
          getOrCreateFetchJoin(hql, path, path2Alias, alias2Restriction);
        }

        if (keys != null) {
          hql.whereIn(getRootAlias(), "id", keys);
        }
        else {
          // if explicit set of keys has not been provided, we must still
          // restrict result with top-level restrictions
          addDomainRestrictions(hql, path2Alias);
        }

        // we apply a 'distinct' filter in case the Relationships provided by
        // client code does not restrict each relationship to single entity
        hql.distinctRootEntities();

        if (log.isDebugEnabled()) {
          log.debug("fetch data query for paths " + paths + ": " + hql);
        }

        return hql;
      }

      /**
       * Group relationship paths together by their basic path (i.e., the path
       * w/o its restrictions). Create one group for all paths that have no
       * restrictions, then one group for each set of restricted paths that have
       * the same basic path.
       * 
       * @return a Map of the grouped paths, one entry per group, keyed on the
       *         basic path string representing each group.
       */ 
      private Map<String,Set<RelationshipPath<E>>> buildPathGroups()
      {
        Map<String,Set<RelationshipPath<E>>> groupedPaths =
          new HashMap<String,Set<RelationshipPath<E>>>();
        for (RelationshipPath<E> path : _relationships) {
          String groupName = "<unrestricted>";
          if (path.hasRestrictions()) {
            groupName = path.getPath();
          }
          Set<RelationshipPath<E>> group = groupedPaths.get(groupName);
          if (group == null) {
            group = new HashSet<RelationshipPath<E>>();
            groupedPaths.put(groupName, group);
          }
          group.add(path);
        }
        return groupedPaths;
      }
    };
  }

  protected String getRootAlias()
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
        alias = getRootAlias();
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
  
  /**
   * @motivation HQL does not support "LEFT JOIN FETCH...WITH" Argh!!! Instead,
   *             we must create a disjunction of restrictions for each join
   *             alias, since we're "stacking" multiple, distinct paths (of the
   *             same type, but of different restrictions)
   */
  protected String getOrCreateFetchJoin(HqlBuilder hql,
                                        RelationshipPath<E> path,
                                        Map<RelationshipPath<E>,String> path2Alias,
                                        Map<String,Disjunction> alias2Restriction)
  {
    assert !(path instanceof PropertyPath) : "use path.getRelationshipPath() for path arg, when path is a PropertyPath";

    String alias = null;
    // manually search the path2Alias map to find alias for any extant join
    // that satisfies the *basic* path (i.e., ignoring any restrictions on
    // the path's nodes)
    for (Map.Entry<RelationshipPath<E>,String> entry : path2Alias.entrySet()) {
      if (entry.getKey().getPath().equals(path.getPath())) {
        alias = entry.getValue();
        //log.debug("using alias " + alias + " for " + path);
        break;
      }
    }

    if (alias == null) {
      if (path.getPathLength() == 0) {
        alias = getRootAlias();
        path2Alias.put(path, alias);
        hql.from(path.getRootEntityClass(), alias);
        log.debug("created alias for root entity: " + path);
        return alias;
      }
      // create joins for path ancestry nodes, recursively
      String parentAlias = getOrCreateFetchJoin(hql, path.getAncestryPath(), path2Alias, alias2Restriction);
      // create join for path leaf node
      alias = "x" + path2Alias.size();
      hql.from(parentAlias, path.getLeaf(), alias, JoinType.LEFT_FETCH);
    }
    
    // TODO: Hibernate doesn't support restricting (filtering) collections when
    // doing a 'left join fetch' operator. Really, I tried.
    
//    // add relationship path restrictions, adding to the disjunctive clause for this alias
//    if (path.getPathLength() > 0 && !path2Alias.containsKey(path)) {
//      PropertyNameAndValue restrictionPropertyNameAndValue =
//        path.getLeafRestrictionPropertyNameAndValue();
//      if (restrictionPropertyNameAndValue != null) {
//        Disjunction or = alias2Restriction.get(alias);
//        if (or == null) {
//          or = hql.disjunction();
//          hql.where(or);
//          alias2Restriction.put(alias, or);
//        }
//        String propName = restrictionPropertyNameAndValue.getName();
//        Object value = restrictionPropertyNameAndValue.getValue();
//        or.add(hql.predicate(alias + "." + propName, Operator.EQUAL, value));
//      }
//    }

    path2Alias.put(path, alias);

    return alias;
  }
}
