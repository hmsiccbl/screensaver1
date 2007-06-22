//$HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
//$Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $

//Copyright 2006 by the President and Fellows of Harvard College.

//Screensaver is an open-source project developed by the ICCB-L and NSRB labs
//at Harvard Medical School. This software is distributed under the terms of
//the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import edu.harvard.med.screensaver.model.AbstractEntity;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.CollectionType;
import org.springframework.orm.hibernate3.HibernateCallback;

public interface GenericEntityDAO
{
  /**
   * This method can be called before invoking other GenericEntityDAO methods that issue HQL
   * (or Criteria-based) queries to ensure that newly instantiated and persisted
   * entities are considered by the query. This is never necessary if the
   * Hibernate session flush mode is AUTO or or ALWAYS, since in these cases
   * Hibernate will ensure the session is always flushed prior to executing an
   * HQL query.
   */
  public void flush();

  /**
   * Executes a block of code, presumably with multiple GenericEntityDAO calls, into a single
   * transactions.
   * 
   * @param daoTransaction the object encapsulating the transactional code to
   *          execute.
   */
  public void doInTransaction(DAOTransaction daoTransaction);
  
  /**
   * @deprecated Use this method prevents compile-time checking of constructor
   *             signature. Instantiate the entity via its constructor and use
   *             {@link #persistEntity(AbstractEntity)} instead.
   */
  public <E extends AbstractEntity> E defineEntity(Class<E> entityClass,
                                                   Object... constructorArguments);

  /**
   * Make the specified entity persistent. The entity's ID property will be set
   * upon return.
   * 
   * @param entity
   */
  public void persistEntity(AbstractEntity entity);


  /**
   * Reattach the entity to the current Hibernate session, allowing
   * modifications that have been made or that will be made to the entity to be
   * persisted. In particular, this will cause the entity (and any related
   * entities that are reachable via "update" cascades) to be version checked
   * against the database and causing a ConcurrencyFailureException to be thrown
   * if concurrent modification of this or its related entities is detected.
   * <p>
   * Internally, Hibernate will issue SQL calls to increment the version field
   * of each entity. This can be expensive for large entity networks.
   * </p>
   * 
   * @param <E>
   * @param entity
   * @return
   */
  public <E extends AbstractEntity> E reattachEntity(E entity);


  /**
   * Reattach a <i>new instance</i> of a previously persistent, but dettached
   * entity to the current Hibernate session, allowing its previously
   * uninitialized lazy relationships to be navigated (without throwing
   * LazyInitializationExceptions). If the entity already exists in the session,
   * the entity will <i>not</i> be reloaded from the database. The specified
   * entity instance is unchanged, and its uninitialized relationships cannot be
   * navigated.
   * <p>
   * Relationships that were initialized in the specified entity
   * (network) will <i>not</i> be pre-initialized in the new instance of the
   * returned entity.
   * </p>
   * 
   * @param entity the entity to be reloaded
   * @return a new Hibernate-managed instance of the specified entity
   */
  public <E extends AbstractEntity> E reloadEntity(E entity);


  /**
   * Reattach a <i>new instance</i> of a previously persistent, but dettached
   * entity to the current Hibernate session, allowing its previously
   * uninitialized lazy relationships to be navigated (without throwing
   * LazyInitializationExceptions). If the entity already exists in the session,
   * the entity will <i>not</i> be reloaded from the database. The specified
   * entity instance is unchanged, and its uninitialized relationships cannot be
   * navigated.
   * <p>
   * Relationships that were initialized in the specified entity
   * (network) will <i>not</i> be pre-initialized in the new instance of the
   * returned entity.
   * </p>
   * 
   * @param <E>
   * @param entity the entity to be reloaded
   * @param readOnly see class-level documentation of {@link GenericEntityDAO} 
   * @param relationships the relationships to loaded, relative to the root
   *          entity, specified as a dot-separated path of relationship property
   *          names; see class-level documentation of {@link GenericEntityDAO}
   * @return a new Hibernate-managed instance of the specified entity
   */
  public <E extends AbstractEntity> E reloadEntity(E entity, boolean readOnly, String... relationships);


  /**
   * Loads the specified relationships of a given entity, allowing these
   * relationships to be navigated after the entity is dettached from the
   * Hibernate session.
   * 
   * @param entity the root entity
   * @param relationships the relationships to loaded, relative to the root
   *          entity, specified as a dot-separated path of relationship property
   *          names; see class-level documentation of {@link GenericEntityDAO}
   */
  public void need(AbstractEntity entity,
                   String ... relationships);

  /**
   * Loads the specified relationships of a given entity, allowing these
   * relationships to be navigated after the entity is dettached from the
   * Hibernate session. See class-level documentation of
   * {@link GenericEntityDAO} for issues related to loading read-only entities.
   * 
   * @param entity the root entity
   * @param relationships the relationships to loaded, relative to the root
   *          entity, specified as a dot-separated path of relationship property
   *          names; see class-level documentation of {@link GenericEntityDAO}
   */
  public void needReadOnly(AbstractEntity entity,
                           String... relationships);

  /**
   * Returns the size of a to-many relationship collection, and does so
   * efficiently, without loading the entities in the relationship.
   * 
   * @param persistentCollection
   * @return
   */
  public int relationshipSize(final Object persistentCollection);


  /**
   * Returns the size of a to-many relationship collection, and does so
   * efficiently, without loading the entities in the relationship.
   *
   * @param entity
   * @param relationship
   * @return
   */
  public int relationshipSize(final AbstractEntity entity, final String relationship);


  public int relationshipSize(
                              final AbstractEntity entity,
                              final String relationship,
                              final String relationshipProperty,
                              final String relationshipPropertyValue);


  public void deleteEntity(AbstractEntity entity);


  /**
   * Retrieve and return a list of entities of the specified type.
   * 
   * @param<E> The type of the entity to retrieve
   * @param entityClass the class of the entity to retrieve
   * @return a list of the entities of the specified type
   */
  public <E extends AbstractEntity> List<E> findAllEntitiesOfType(Class<E> entityClass);


  /**
   * 
   * @param <E>
   * @param readOnly see class-level documentation of {@link GenericEntityDAO}
   * @param relationships the relationships to loaded, relative to the root
   *          entity, specified as a dot-separated path of relationship property
   *          names; see class-level documentation of {@link GenericEntityDAO}
   * @return
   */
  public <E extends AbstractEntity> List<E> findAllEntitiesOfType(Class<E> entityClass,
                                                                  boolean readOnly,
                                                                  String... relationships);


  /**
   * Retrieve and return an entity by its identifier (primary key).
   * 
   * @param <E> the type of the entity to retrieve
   * @param id the identifier of the entity to retrieve
   * @param entityClass the class of the entity to retrieve
   * @return the entity of the specified type, with the specified identifier.
   *         Return null if there is no such entity.
   */
  public <E extends AbstractEntity> E findEntityById(Class<E> entityClass, Serializable id);


  /**
   * See @{@link #findEntityById(Class, Serializable)}.
   * @param readOnly see class-level documentation of {@link GenericEntityDAO}
   * @param relationships the relationships to loaded, relative to the root
   *          entity, specified as a dot-separated path of relationship property
   *          names; see class-level documentation of {@link GenericEntityDAO}
   */
  public <E extends AbstractEntity> E findEntityById(Class<E> entityClass,
                                                     Serializable id,
                                                     boolean readOnly,
                                                     String... relationships);


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
  public <E extends AbstractEntity> List<E> findEntitiesByProperties(Class<E> entityClass, 
                                                                     Map<String,Object> name2Value);


  /**
   * See @{@link #findEntitiesByProperties(Class, Map)}.
   * @param readOnly see class-level documentation of {@link GenericEntityDAO}
   * @param relationships the relationships to loaded, relative to the root
   *          entity, specified as a dot-separated path of relationship property
   *          names; see class-level documentation of {@link GenericEntityDAO}
   */
  public <E extends AbstractEntity> List<E> findEntitiesByProperties(Class<E> entityClass,
                                                                     Map<String,Object> name2Value,
                                                                     final boolean readOnly,
                                                                     String... relationshipsIn);


  /**
   * Retrieve and return a list of entities that have specific values for the
   * specified properties.
   * 
   * @param <E> the type of the entity to retrieve
   * @param entityClass the class of the entity to retrieve
   * @param propertyName the name of the property to query against
   * @param propertyValue the value of the property to query for
   * @return a list of entities that have the specified value for the specified
   *         property
   * @exception InvalidArgumentException when there is more
   *    than one entity with the specified value for the property
   */
  public <E extends AbstractEntity> E findEntityByProperties(Class<E> entityClass, 
                                                             Map<String,Object> name2Value);


  /**
   * See @{@link #findEntityByProperties(Class, Map)}.
   * @param readOnly see class-level documentation of {@link GenericEntityDAO}
   * @param relationships the relationships to loaded, relative to the root
   *          entity, specified as a dot-separated path of relationship property
   *          names; see class-level documentation of {@link GenericEntityDAO}
   */
  public <E extends AbstractEntity> E findEntityByProperties(Class<E> entityClass,
                                                             Map<String,Object> name2Value,
                                                             boolean readOnly,
                                                             String... relationships);


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
  public <E extends AbstractEntity> List<E> findEntitiesByProperty(Class<E> entityClass, 
                                                                   String propertyName, 
                                                                   Object propertyValue);


  /**
   * See @{@link #findEntitiesByProperty(Class, String, Object)}.
   * @param readOnly see class-level documentation of {@link GenericEntityDAO}
   * @param relationships the relationships to loaded, relative to the root
   *          entity, specified as a dot-separated path of relationship property
   *          names; see class-level documentation of {@link GenericEntityDAO}
   */
  public <E extends AbstractEntity> List<E> findEntitiesByProperty(Class<E> entityClass,
                                                                   String propertyName,
                                                                   Object propertyValue,
                                                                   boolean readOnly,
                                                                   String... relationships);

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
  public <E extends AbstractEntity> E findEntityByProperty(Class<E> entityClass,
                                                           String propertyName,
                                                           Object propertyValue);

  /**
   * See @{@link #findEntityByProperty(Class, String, Object)}.
   * @param readOnly see class-level documentation of {@link GenericEntityDAO}
   * @param relationships the relationships to loaded, relative to the root
   *          entity, specified as a dot-separated path of relationship property
   *          names; see class-level documentation of {@link GenericEntityDAO}
   */
  public <E extends AbstractEntity> E findEntityByProperty(Class<E> entityClass,
                                                           String propertyName,
                                                           Object propertyValue,
                                                           boolean readOnly,
                                                           String... relationships);

  public <E extends AbstractEntity> List<E> findEntitiesByHql(
                                                              Class<E> entityClass,
                                                              String hql,
                                                              Object... hqlParameters);
}

