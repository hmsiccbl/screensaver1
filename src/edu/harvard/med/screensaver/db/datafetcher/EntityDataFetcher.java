// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.datafetcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.Query;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.db.hqlbuilder.JoinType;
import edu.harvard.med.screensaver.model.Entity;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;

/**
 * DataFetcher that fetches entities or entity networks from persistent storage. As entities have relationships, and
 * thus form entity networks, this DataFetcher allows the network "structure" to be specified via
 * {@link #setPropertiesToFetch}. Subclasses can also enforce additional, implicit filtering constraints on the data set
 * to be fetched via {@link #addDomainRestrictions(HqlBuilder)}.
 */
public class EntityDataFetcher<E extends Entity,K> extends PropertyPathDataFetcher<E,E,K>
{
  private static final Logger log = Logger.getLogger(EntityDataFetcher.class);

  public EntityDataFetcher(Class<E> rootEntityClass, GenericEntityDAO dao)
  {
    super(rootEntityClass, dao);
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

  /**
   * @param keys if null, fetches all entities for the root entity type (subject
   *          to normal column criteria)
   */
  protected Query buildFetchDataQuery(final Set<K> keys)
  {
    return new Query<E>() {
      public List<E> execute(Session session)
      {
        HqlBuilder hql = buildHqlQueryForPaths();
        List<E> result = hql.toQuery(session, true).list();
        log.debug("found " + result.size() + " root entities");
        return result;
      }

      private HqlBuilder buildHqlQueryForPaths()
      {
        HqlBuilder hql = new HqlBuilder();
        Map<RelationshipPath<E>,String> path2Alias = Maps.newHashMap();

        // ensure that the root entity is fetched, at a minimum
        getOrCreateJoin(hql, RelationshipPath.from(_rootEntityClass), path2Alias, null);

        for (PropertyPath<E> path : getProperties()) {
          getOrCreateJoin(hql, path.getUnrestrictedPath(), path2Alias, JoinType.LEFT_FETCH);
        }

        if (keys != null) {
          hql.whereIn(getRootAlias(), "id", keys);
        }
        else {
          // if explicit set of keys has not been provided, we must still
          // restrict result with top-level restrictions
          addDomainRestrictions(hql);
        }

        // add an explicit select clause for the root entity *only*, otherwise
        // Hibernate may return other entity types as well, which will cause the
        // 'distinct' behavior to fail (i.e., DistinctEntityResultTransformer)
        hql.select(getRootAlias());

        // we apply a 'distinct' filter in case the Relationships provided by
        // client code does not restrict each relationship to single entity
        hql.distinctRootEntities();

        if (log.isDebugEnabled()) {
          log.debug("fetch data query for properties " + getProperties() + ": " + hql);
        }

        return hql;
      }
    };
  }
}
