// $HeadURL$
// $Id$
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.datafetcher;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.Query;
import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.db.hqlbuilder.JoinType;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.Entity;
import edu.harvard.med.screensaver.model.meta.PropertyNameAndValue;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.ui.arch.datatable.Criterion;
import edu.harvard.med.screensaver.ui.arch.datatable.Criterion.Operator;

public abstract class PropertyPathDataFetcher<R,E extends Entity,K> implements DataFetcher<R,K,PropertyPath<E>>
{
  protected static Logger log = Logger.getLogger(EntityDataFetcher.class);

  private static final String COV_ALIAS_SUFFIX = "COV";

  protected final Class<E> _rootEntityClass;
  protected final GenericEntityDAO _dao;
  private Set<PropertyPath<E>> _properties;
  private Map<PropertyPath<E>,List<? extends Criterion<?>>> _criteria = Collections.emptyMap();
  private List<PropertyPath<E>> _orderByProperties = Collections.emptyList();

  public PropertyPathDataFetcher(Class<E> rootEntityClass, GenericEntityDAO dao)
  {
    _rootEntityClass = rootEntityClass;
    _dao = dao;
  }

  protected Set<PropertyPath<E>> getProperties()
  {
    if (_properties == null) {
      throw new IllegalStateException("properties to fetch is not set");
    }
    return _properties;
  }

  protected Map<PropertyPath<E>,List<? extends Criterion<?>>> getCriteria()
  {
    return _criteria;
  }

  protected List<PropertyPath<E>> getOrderByProperties()
  {
    return _orderByProperties;
  }

  @Override
  public void addDomainRestrictions(HqlBuilder hql)
  {}

  public void setPropertiesToFetch(List<PropertyPath<E>> properties)
  {
    _properties = new LinkedHashSet<PropertyPath<E>>();
    _properties.addAll(properties);
  }

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

  private Query<K> buildFetchKeysQuery()
  {
    return new Query<K>() {
      public List<K> execute(Session session)
      {
        HqlBuilder hql = new HqlBuilder();
        Map<RelationshipPath<E>,String> path2Alias = Maps.newHashMap();

        // ensure that the root entity is fetched, at a minimum
        getOrCreateJoin(hql, RelationshipPath.from(_rootEntityClass), path2Alias, null);

        // order by
        // note: we add 'order by' clauses before restrictions, to ensure that
        // any left joins that we need are in fact created as left joins
        for (PropertyPath<E> property : _orderByProperties) {
          String alias = getOrCreateJoin(hql, property, path2Alias, JoinType.LEFT);
          if (property.isCollectionOfValues()) {
            hql.orderBy(alias, SortDirection.ASCENDING);
          }
          else {
            hql.orderBy(alias, property.getPropertyName());
          }
        }

        // filtering restrictions
        for (Map.Entry<PropertyPath<E>,List<? extends Criterion<?>>> entry : _criteria.entrySet()) {
          PropertyPath<E> propertyPath = entry.getKey();
          for (Criterion<?> criterion : entry.getValue()) {
            if (!criterion.isUndefined()) {
              String alias = getOrCreateJoin(hql,
                                             propertyPath,
                                             path2Alias,
                                             criterion.getOperator() == Operator.EMPTY ? JoinType.LEFT : JoinType.INNER);
              if (propertyPath.isCollectionOfValues()) {
                hql.where(alias, criterion.getOperator(), criterion.getValue());
              }
              else {
                hql.where(alias, propertyPath.getPropertyName(), criterion.getOperator(), criterion.getValue());
              }
            }
          }
        }

        // restrict to a domain
        addDomainRestrictions(hql);

        // projection
        hql.select(getRootAlias(), "id");

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

  protected String getRootAlias()
  {
    return "x";
  }

  protected String getOrCreateJoin(HqlBuilder hql,
                                   PropertyPath<E> path,
                                   Map<RelationshipPath<E>,String> path2Alias,
                                   JoinType joinType)
  {
    String alias = getOrCreateJoin(hql, path.getAncestryPath(), path2Alias, joinType);
    if (path.isCollectionOfValues()) {
      String parentAlias = alias;
      alias = makeCollectionOfValuesAlias(alias, path2Alias);
      path2Alias.put(path, alias);
      hql.from(parentAlias, ((PropertyPath) path).getPropertyName(), alias, joinType);
    }
    return alias;
  }

  /**
   * @motivation support joining of multiple collections of values belonging to the same entity
   */
  private String makeCollectionOfValuesAlias(String alias, Map<RelationshipPath<E>,String> path2Alias)
  {
    int n = 0;
    String aliasCandidate;
    do {
      aliasCandidate = alias + COV_ALIAS_SUFFIX + n++;
    }
    while (path2Alias.values().contains(aliasCandidate));
    return aliasCandidate;
  }

  protected String getOrCreateJoin(HqlBuilder hql,
                                   RelationshipPath<E> path,
                                   Map<RelationshipPath<E>,String> path2Alias,
                                   JoinType joinType)
  {
    if (!path2Alias.containsKey(path)) {
      createJoin(hql, path, path2Alias, joinType);
    }
    return path2Alias.get(path);
  }

  private String createJoin(HqlBuilder hql,
                            RelationshipPath<E> path,
                            Map<RelationshipPath<E>,String> path2Alias,
                            JoinType joinType)
  {
    String parentAlias;
    if (path.getAncestryPath() == null) {
      parentAlias = getRootAlias();
      path2Alias.put(path, parentAlias);
      hql.from(path.getRootEntityClass(), parentAlias);
      return parentAlias;
    }
    parentAlias = getOrCreateJoin(hql, path.getAncestryPath(), path2Alias, joinType);
    String alias = "x" + path2Alias.size();
    path2Alias.put(path, alias);
    hql.from(parentAlias, path.getLeaf(), alias, joinType);

    if (joinType != JoinType.LEFT_FETCH) {
      addRestriction(hql, alias, path.getLeafRestriction());
    }
    return alias;
  }

  private void addRestriction(HqlBuilder hql, String alias, PropertyNameAndValue restriction)
  {
    if (restriction != null) {
      String propName = restriction.getName();
      Object value = restriction.getValue();
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

}
