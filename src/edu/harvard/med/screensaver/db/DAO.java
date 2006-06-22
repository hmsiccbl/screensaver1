// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import edu.harvard.med.screensaver.model.AbstractEntity;


/**
 * A Data Access Object for the beans in the
 * {@link edu.harvard.med.screensaver.model data model}.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Transactional
public interface DAO
{

  /**
   * Execute a segment of code within a transaction.
   * 
   * @param daoTransaction the object encapsulating the transactional code to
   *          execute.
   */
  public void doInTransaction(DAOTransaction daoTransaction);

  /**
   * Create, register, and return a new {@link AbstractEntity Entity} of the
   * specified type with all required fields initialized.
   * 
   * @param <E> The type of entity to create
   * @param entityClass The class of the entity to create
   * @param constructorArguments arguments for the required fields
   * @return The newly-created entity
   * @exception InvalidArgumentException when the supplied constructor arguments
   *              do not match the parameter list of the required-fields
   *              constructor
   */
  public <E extends AbstractEntity> E defineEntity(
    Class<E> entityClass,
    Object... constructorArguments);
  
  /**
   * Update the database with the values for the given Entity. If the Entity was
   * not previously in the database, then create it.
   * 
   * @motivation Used to save changes to a Entity when it has been loaded by a
   *             different thread than it was modified in (the Hibernate session
   *             is no longer managing the object and it must be "reattached").
   *             Or when the Entity was originally created in some other
   *             context.
   * @param entity the Entity to persist
   */
  public void persistEntity(AbstractEntity entity);

  /**
   * Retrieve and return a list of Entities of the specified type.
   * 
   * @param<E> The type of the entity to retrieve
   * @param entityClass the class of the entity to retrieve
   * @return a list of the entities of the specified type
   */
  @Transactional(readOnly = true)
  public <E extends AbstractEntity> List<E> findAllEntitiesWithType(Class<E> entityClass);

  /**
   * Retrieve and return an entity by its id (primary key).
   * 
   * @param <E> the type of the entity to retrieve
   * @param id the id of the entity to retrieve
   * @param entityClass the class of the entity to retrieve
   * @return the entity of the specified type, with the specified id. Return
   *         null if there is no such entity.
   */
  @Transactional(readOnly = true)
  public <E extends AbstractEntity> E findEntityById(
    Class<E> entityClass,
    Integer id);

  /**
   * Retrieve and return the entity that has a specific value for the
   * specified properties.
   * 
   * @param <E> the type of the entity to retrieve
   * @param entityClass the class of the entity to retrieve
   * @param name2Value a <code>Map</code> containing entries for each
   *          property/value pair to query against
   * @return a list of entities that have the specified values for the specified
   *         set of properties
   */
  public <E extends AbstractEntity> List<E> findEntitiesByProperties(
    Class<E> entityClass,
    Map<String,Object> name2Value);
  
  /**
   * Retrieve and return the entity that has a specific values for the specified
   * properties. Return <code>null</code> if no entity has that value for that
   * set of properties. Throw an <code>InvalidArgumentException</code> if
   * there is more than one entity with the specified values.
   * 
   * @param <E> the type of the entity to retrieve
   * @param entityClass the class of the entity to retrieve
   * @param name2Value a <code>Map</code> containing entries for each
   *          property/value pair to query against
   * @return the entity that has the specified values for the specified
   *         set of properties
   * @exception InvalidArgumentException when there is more than one entity with
   *              the specified values for the set of properties
   */
  @Transactional(readOnly = true)
  public <E extends AbstractEntity> E findEntityByProperties(
    Class<E> entityClass,
    Map<String,Object> name2Value);

  /**
   * Retrieve and return a list of entities that have a specific value for the
   * specified property.
   * 
   * @param <E> the type of the entity to retrieve
   * @param entityClass the class of the entity to retrieve
   * @param propertyName the name of the property to query against
   * @param propertyValue the value of the property to query for
   * @return a list of entities that have the specified value for the specified
   *         property
   */
  @Transactional(readOnly = true)
  public <E extends AbstractEntity> List<E> findEntitiesByProperty(
    Class<E> entityClass,
    String propertyName,
    Object propertyValue);
  
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
  @Transactional(readOnly = true)
  public <E extends AbstractEntity> E findEntityByProperty(
    Class<E> entityClass,
    String propertyName,
    Object propertyValue);

  /**
   * Retrieve and return a list of entities that match a pattern for the specified text property.
   * Patterns are specified by using the '*' character as a wildcard.
   * 
   * @param <E> the type of the entity to retrieve
   * @param entityClass the class of the entity to retrieve
   * @param propertyName the name of the text property to query against
   * @param propertyPattern the pattern to match
   * @return a list of entities that have the specified value for the specified property
   */
  @Transactional(readOnly = true)
  public <E extends AbstractEntity> List<E> findEntitiesByPropertyPattern(
    Class<E> entityClass,
    String propertyName,
    String propertyPattern);
  
  /**
   * Get the persistence identifier for the specified entity.
   * @param <E> the type of the entity to get the persistence identifier for
   * @param entity the entity to get the persistence identifier for
   * @return the persistence identifier for the entity
   */
  @Transactional(readOnly = true)
  public <E extends AbstractEntity> Integer getEntityId(E entity);
}
