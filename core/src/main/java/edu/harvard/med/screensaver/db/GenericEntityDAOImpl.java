// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.hibernate.Query;

import edu.harvard.med.screensaver.db.Criterion.Operator;
import edu.harvard.med.screensaver.db.datafetcher.EntityDataFetcher;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.model.Entity;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;

/**
 * GenericEntityDAO that provides basic data access methods that are applicable
 * to all Entity types.
 * <p>
 * Each of the find* methods has three overloaded versions: one that takes only the basic arguments needed to find the
 * entity (or entities), a version that takes an additional <code>readOnly</code> flag parameter, and a version that
 * takes a <code>readOnly</code> flag and a <code>RelationshipPath</code> where the related entity (or entities) should
 * be eagerly fetched (i.e., within a single SQL call). This last version is useful in cases where the returned entity
 * (or entities) will later be used outside of a Hibernate session (i.e., "detached") and the specified relationship
 * will be traversed via the entities' getter methods. This is also useful for minimizing (optimizing) the number of SQL
 * calls that are used to fetch data for each of the relationship that will be traversed when using the entity within an
 * active Hibernate session. This can be used to avoid the "N+1 selects" performance problem, as discussed in Hibernate
 * documentation. If you need to retrieve more than one RelationshipPath, make calls to {@link #need} or
 * {@link #needReadOnly}, but do so within the same Hibernate session as the initial find*() call.
 * <p>
 * If <code>readOnly</code> is true, the entities that are loaded by Hibernate will not be "managed". This means that
 * they will not be dirty-checked at flush time, and modifications made to the objects will not be persisted. This is
 * beneficial for performance when fetched data will be read-only. Note that this does not in any way make the loaded
 * entity instances immutable, so if any of these entity instances happen to have already been loaded into the Hibernate
 * session as managed (read-write) entities, changes to them <i>will</i> be persisted! So the best practice is for the
 * client code to never modify entities loaded as read-only.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class GenericEntityDAOImpl extends AbstractDAO implements GenericEntityDAO
{
  private static Logger log = Logger.getLogger(GenericEntityDAOImpl.class);

  /**
   * @motivation for CGLIB dynamic proxy creation
   */
  public GenericEntityDAOImpl()
  {
  }

  /**
   * Make the specified entity persistent. The entity's ID property will be set
   * upon return. This method calls the underlying Hibernate Session.saveOrUpdate, which
   * does not have JPA semantics. We have encountered situations in which the save-or-update
   * cascades were not followed when this method was called, but only after the session
   * was flushed. If this seems like a problem to you, try {@link #persistEntity(Entity)}
   * instead.
   *
   * @param entity
   */
  public void saveOrUpdateEntity(Entity entity)
  {
    getHibernateSession().saveOrUpdate(entity);
  }

  /**
   * Make the specified entity persistent. The entity's ID property will be set
   * upon return. This is the one with JPA semantics: save-update cascades will be followed
   * at the time this method is called.
   *
   * @param entity
   */
  public void persistEntity(Entity entity)
  {
    getEntityManager().persist(entity);
  }

  /**
   * Reattach the entity to the current Hibernate session, allowing
   * modifications that have been made to it while detached, or that will be
   * made to the reattached entity, to be persisted. Note that this will cause
   * the entity (and any related entities that are reachable via "update"
   * cascades) to be version checked against the database, possibly causing a
   * ConcurrencyFailureException to be thrown if concurrent modification of this
   * or its related entities is detected.
   * <p>
   * Internally, version checking is effected by Hibernate by issuing SQL update
   * statements to increment the version field of each entity at time of this
   * method call. This can be expensive for large entity networks with "update"
   * cascades enabled.
   * </p>
   *
   * @param entity the entity to be reattached
   * @return the same entity instance passed in
   */
  public <E extends Entity> E reattachEntity(E entity)
  {
    getHibernateSession().update(entity);
    return entity;
  }
  
  public <E extends Entity> E mergeEntity(E entity)
  {
    return getEntityManager().merge(entity);
  }

  /**
   * For a given detached entity, return a <i>new, Hibernate-managed instance</i>.
   * If called within a transaction, all lazy relationships on the returned
   * entity can be safely navigated (i.e., w/o throwing
   * LazyInitializationExceptions). Note that if the entity already exists in
   * the session, the entity will <i>not</i> actually be reloaded from the
   * database; instead the returned instance will be the one already cached with
   * the session. The passed-in entity instance will be unchanged, and its
   * uninitialized relationships cannot be navigated.
   * <p>
   * Any relationships that may have been initialized in the passed-in entity (network) <i>cannot</i> be expected to be
   * pre-initialized (eagerly fetched) in the new, returned entity instance. However, you may specify relationships that
   * need to be eagerly fetched by making subsequent calls to {@link #need(Entity, RelationshipPath)} or
   * {@link #needReadOnly(Entity, RelationshipPath)}.
   * </p>
   * 
   * @param entity the entity to be reloaded
   * @return a new Hibernate-managed instance of the specified entity
   */
  public <E extends Entity> E reloadEntity(E entity)
  {
    // TODO: throw exception if entity already exists in the session
    return reloadEntity(entity, false);
  }

  /**
   * For a given detached entity, return a <i>new, Hibernate-managed instance</i>.
   * If called within a transaction, all lazy relationships on the returned
   * entity can be safely navigated (i.e., w/o throwing
   * LazyInitializationExceptions). Note that if the entity already exists in
   * the session, the entity will <i>not</i> actually be reloaded from the
   * database; instead the returned instance will be the one already cached with
   * the session. The passed-in entity instance will be unchanged, and its
   * uninitialized relationships cannot be navigated.
   * <p>
   * Any relationships that may have been initialized in the passed-in entity (network) <i>cannot</i> be expected to be
   * pre-initialized (eagerly fetched) in the new, returned entity instance. However, you may specify a relationship
   * that needs to be eagerly fetched via the <code>relationship</code> arg and you may eager fetch additional
   * collection relationships by making subsequent calls to {@link #need(Entity, RelationshipPath)} or
   * {@link #needReadOnly(Entity, RelationshipPath)}.
   * </p>
   * 
   * @param <E>
   * @param entity the entity to be reloaded
   * @param readOnly see class-level documentation of {@link GenericEntityDAO}
   * @param relationship the related entity or entities to be eagerly fetched
   * @return a new Hibernate-managed instance of the specified entity
   */
  public <E extends Entity> E reloadEntity(E entity,
                                           boolean readOnly,
                                           RelationshipPath<E> relationship)
  {
    // TODO: throw exception if entity already exists in the session
    if (entity != null) {
      log.debug("reloading entity " + entity);
      return (E) findEntityById(entity.getEntityClass(), entity.getEntityId(), readOnly, relationship);
    }
    return null;
  }

  public <E extends Entity> E reloadEntity(E entity, boolean readOnly)
  {
    return reloadEntity(entity, readOnly, null);
  }

  /**
   * Loads the specified relationship of a given entity, allowing the
   * relationship to be navigated after the entity is detached from the
   * Hibernate session.
   * 
   * @param entity the root entity
   * @param relationship the related entity or entities to be eagerly fetched
   */
  public <E extends Entity> void need(E entity,
                                      RelationshipPath<E> relationship)
  {
    if (entity == null) {
      return;
    }
    findEntityById(entity.getEntityClass(), entity.getEntityId(), false, relationship.castToSubtype(entity.getEntityClass()));
  }

  /**
   * Loads the specified relationship of a given entity, allowing the
   * relationship to be navigated after the entity is detached from the
   * Hibernate session. See class-level documentation of {@link GenericEntityDAO} for issues related to loading
   * read-only entities.
   * 
   * @param entity the root entity
   * @param relationship the related entity or entities to be eagerly fetched
   */
  public <E extends Entity> void needReadOnly(E entity,
                                              RelationshipPath<E> relationship)
  {
    if (entity == null) {
      return;
    }
    findEntityById(entity.getEntityClass(), entity.getEntityId(), true, relationship.castToSubtype(entity.getEntityClass()));
  }

  /**
   * Deletes the entity. The entity is first reloaded ({@link #reloadEntity(Entity)) to ensure that is attached to the
   * current session; this is necessary when this method is called outside of an active session. If the method is called
   * within an active session, and the entity is already managed, the reloadEntity call has no effect.
   */
  public void deleteEntity(Entity entity)
  {
    entity = reloadEntity(entity);
    getEntityManager().remove(entity);
  }

  /**
   * Retrieve and return a list of entities of the specified type.
   *
   * @param <E> The type of the entity to retrieve
   * @param entityClass the class of the entity to retrieve
   * @return a list of the entities of the specified type
   */
  public <E extends Entity> List<E> findAllEntitiesOfType(Class<E> entityClass)
  {
    return findAllEntitiesOfType(entityClass, false, null);
  }

  /**
   * @param <E>
   * @param readOnly see class-level documentation of {@link GenericEntityDAO}
   * @param relationship the related entity or entities to be eagerly fetched
   */
  public <E extends Entity> List<E> findAllEntitiesOfType(Class<E> entityClass,
                                                          boolean readOnly,
                                                          RelationshipPath<E> relationship)
  {
    return (List<E>) findEntitiesByProperties(entityClass, null, readOnly, relationship);
  }

  public <E extends Entity> List<E> findAllEntitiesOfType(Class<E> entityClass, boolean readOnly)
  {
    return (List<E>) findEntitiesByProperties(entityClass, null, readOnly, null);
  }
  
  /**
   * Retrieve and return an entity by its identifier (primary key). If the
   * requested entity is in the Hibernate session, no database I/O will occur.
   * Therefore this method provide an efficient means of retrieving entities
   * that have already been fetched from the database.
   * 
   * @param <E> the type of the entity to retrieve
   * @param id the identifier of the entity to retrieve
   * @param entityClass the class of the entity to retrieve
   * @return the entity of the specified type, with the specified identifier.
   *         Return null if there is no such entity.
   */
  @SuppressWarnings("unchecked")
  public <E extends Entity<K>,K extends Serializable> E findEntityById(Class<E> entityClass,
                                                                       K id)
  {
    return (E) getEntityManager().find(entityClass, id);
  }

  /**
   * @param readOnly see class-level documentation of {@link GenericEntityDAO}
   * @param relationship the related entity or entities to be eagerly fetched
   */
  public <E extends Entity<K>,K extends Serializable> E findEntityById(Class<E> entityClass,
                                                                       K id,
                                                                       boolean readOnly,
                                                                       RelationshipPath<E> relationship)
  {
    return findEntityByProperty(entityClass, "id", id, readOnly, relationship);
  }

  public <E extends Entity<K>,K extends Serializable> E findEntityById(Class<E> entityClass, 
                                                                       K id,
                                                                       boolean readOnly)
  {
    return findEntityById(entityClass, id, readOnly, null);
  }
  
  /**
   * Retrieve and return the entity that has specific values for the specified
   * properties. Return <code>null</code> if no entity has that value for that
   * set of properties.
   *
   * @param <E> the type of the entity to retrieve
   * @param entityClass the class of the entity to retrieve
   * @param name2Value a <code>Map</code> containing entries for each
   *          property/value pair to query against
   * @return the entity that has the specified values for the specified
   *         set of properties
   */
  public <E extends Entity> List<E> findEntitiesByProperties(Class<E> entityClass,
                                                             Map<String,Object> name2Value)
  {
    return findEntitiesByProperties(entityClass, name2Value, false);
  }

  /**
   * @param name2Value a <code>Map</code> containing entries for each
   *          property/value pair to query against
   * @param readOnly see class-level documentation of {@link GenericEntityDAO}
   * @param relationship the related entity or entities to be eagerly fetched
   */
  public <E extends Entity> List<E> findEntitiesByProperties(Class<E> entityClass,
                                                             final Map<String,Object> name2Value,
                                                             final boolean readOnly,
                                                             RelationshipPath<E> relationship)
  {
    EntityDataFetcher<E,?> entityDataFetcher = new EntityDataFetcher<E,Serializable>(entityClass, this) {
      @Override
      public void addDomainRestrictions(HqlBuilder hql)
      {
        if (name2Value != null) {
          for (Map.Entry<String,Object> criterion : name2Value.entrySet()) {
            hql.where(getRootAlias(), criterion.getKey(), Operator.EQUAL, criterion.getValue());
          }
        }
      }
    };
    PropertyPath<E> propertyPath;
    if (relationship == null) {
      propertyPath = RelationshipPath.from(entityClass).toFullEntity();
    }
    else if (relationship instanceof PropertyPath) {
      propertyPath = (PropertyPath<E>) relationship;
    }
    else {
      propertyPath = relationship.toFullEntity();
    }
    entityDataFetcher.setPropertiesToFetch(ImmutableList.of(propertyPath));
    entityDataFetcher.setReadOnly(readOnly);
    return entityDataFetcher.fetchAllData();
  }
  
  public <E extends Entity> List<E> findEntitiesByProperties(Class<E> entityClass,
                                                             Map<String,Object> name2Value,
                                                             boolean readOnly)
  {
    return findEntitiesByProperties(entityClass, name2Value, readOnly, null);
  }

  /**
   * Retrieve and return a list of entities that have specific values for the
   * specified properties.
   *
   * @param <E> the type of the entity to retrieve
   * @param entityClass the class of the entity to retrieve
   * @return a list of entities that have the specified value for the specified
   *         property
   * @param name2Value a <code>Map</code> containing entries for each
   *          property/value pair to query against
   * @exception InvalidArgumentException when there is more
   *    than one entity with the specified value for the property
   */
  public <E extends Entity> E findEntityByProperties(Class<E> entityClass,
                                                     Map<String,Object> name2Value)
  {
    return findEntityByProperties(entityClass,
                                  name2Value,
                                  false);
  }

  /**
   * @param name2Value a <code>Map</code> containing entries for each
   *          property/value pair to query against
   * @param readOnly see class-level documentation of {@link GenericEntityDAO}
   * @param relationship the related entity or entities to be eagerly fetched
   */
  public <E extends Entity> E findEntityByProperties(Class<E> entityClass,
                                                     Map<String,Object> name2Value,
                                                     boolean readOnly,
                                                     RelationshipPath<E> relationship)
  {
    List<E> entities = findEntitiesByProperties(entityClass,
                                                name2Value,
                                                readOnly,
                                                relationship);
    if (entities.size() == 0) {
      return null;
    }
    if (entities.size() > 1) {
      throw new IllegalArgumentException(
        "more than one result for GenericEntityDAO.findEntityByProperties");
    }
    return entities.get(0);
  }

  public <E extends Entity> E findEntityByProperties(Class<E> entityClass,
                                                     Map<String,Object> name2Value,
                                                     boolean readOnly)
  {
    return findEntityByProperties(entityClass, name2Value, readOnly, null);
  }

  /**
   * Retrieve and return the entities that have a specific value for the
   * specified property. Return empty list if no entity has that value for that
   * property.
   *
   * @param <E> the type of the entity to retrieve
   * @param entityClass the class of the entity to retrieve
   * @param propertyName the name of the property to query against
   * @param propertyValue the value of the property to query for
   * @return the entity that has the specified value for the specified property
   */
  public <E extends Entity> List<E> findEntitiesByProperty(Class<E> entityClass,
                                                           String propertyName,
                                                           Object propertyValue)
  {
    return findEntitiesByProperty(entityClass, propertyName, propertyValue, false);
  }

  /**
   * @param readOnly see class-level documentation of {@link GenericEntityDAO}
   * @param relationship the related entity or entities to be eagerly fetched
   */
  public <E extends Entity> List<E> findEntitiesByProperty(Class<E> entityClass,
                                                           String propertyName,
                                                           Object propertyValue,
                                                           boolean readOnly,
                                                           RelationshipPath<E> relationship)
  {
    return findEntitiesByProperties(entityClass, ImmutableMap.of(propertyName, propertyValue), readOnly, relationship);
  }

  public <E extends Entity> List<E> findEntitiesByProperty(Class<E> entityClass,
                                                           String propertyName,
                                                           Object propertyValue,
                                                           boolean readOnly)
  {
    return findEntitiesByProperty(entityClass, propertyName, propertyValue, readOnly, null);
  }


  /**
   * Retrieve and return the entity that has a specific value for the specified
   * property. Return <code>null</code> if no entity has that value for that
   * property. Throw an <code>InvalidArgumentException</code> if there is more
   * than one entity with the specified value.
   *
   * @param <E> the type of the entity to retrieve
   * @param entityClass the class of the entity to retrieve
   * @param propertyName the name of the property to query against
   * @param propertyValue the value of the property to query for
   * @return the entity that has the specified value for the specified property
   * @exception InvalidArgumentException when there is more
   *    than one entity with the specified value for the property
   */
  public <E extends Entity> E findEntityByProperty(Class<E> entityClass,
                                                   String propertyName,
                                                   Object propertyValue)
  {
    return findEntityByProperty(entityClass,
                                propertyName,
                                propertyValue,
                                false);
  }

  /**
   * @param readOnly see class-level documentation of {@link GenericEntityDAO}
   * @param relationship the related entity or entities to be eagerly fetched
   */
  public <E extends Entity> E findEntityByProperty(Class<E> entityClass,
                                                   String propertyName,
                                                   Object propertyValue,
                                                   boolean readOnly,
                                                   RelationshipPath<E> relationship)
  {
    List<E> entities = findEntitiesByProperty(
      entityClass,
      propertyName,
      propertyValue,
      readOnly,
      relationship);
    if (entities.size() == 0) {
      return null;
    }
    if (entities.size() > 1) {
      throw new IllegalArgumentException(
        "more than one result for GenericEntityDAO.findEntityByProperty");
    }
    return entities.get(0);
  }

  public <E extends Entity> E findEntityByProperty(Class<E> entityClass,
                                                   String propertyName,
                                                   Object propertyValue,
                                                   boolean readOnly)
  {
    return findEntityByProperty(entityClass, propertyName, propertyValue, readOnly, null);
  }

  public <E extends Entity> List<E> findEntitiesByHql(Class<E> entityClass,
                                                      String hql,
                                                      Object... hqlParameters)
  {
    Query query = getHibernateSession().createQuery(hql);
    for (int i = 0; i < hqlParameters.length; i++) {
      query.setParameter(i, hqlParameters[i]);
    }
    return query.list();
  }

  @Override
  public <E extends Entity,T> Set<T> findDistinctPropertyValues(Class<E> entityClass, String propertyName)
  {
    String hql = "select distinct e. " + propertyName + " from " + entityClass.getSimpleName() + " e where e." + propertyName +
      " is not null";
    List<?> result = getEntityManager().createQuery(hql).getResultList();
    return Sets.<T>newHashSet((List<T>) result);
  }
}

