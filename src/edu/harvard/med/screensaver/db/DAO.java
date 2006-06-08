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

import org.springframework.transaction.annotation.Transactional;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.libraries.Library;

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
  public <E extends AbstractEntity> E defineEntity(Class<E> entityClass,
    Object... constructorArguments);
  
  /**
   * Update the database with the values for the given Entity. If the Entity
   * was not previously in the database, then create it.
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
  @Transactional(readOnly=true)
  public <E extends AbstractEntity> List<E> findAllEntitiesWithType(
    Class<E> entityClass);
  
  /**
   * Retrieve and return an entity by its id (primary key).
   * 
   * @param <E> the type of the entity to retrieve
   * @param id the id of the entity to retrieve
   * @return the entity of the specified type, with the specified id. Return
   * null if there is no such entity. 
   */
  @Transactional(readOnly=true)
  public <E extends AbstractEntity> E findEntityById(
    Class<E> entityClass,
    Integer id);

  // TODO: findEntityByColumns, with and without patterns
}
