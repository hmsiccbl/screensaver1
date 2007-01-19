// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.ui.searchresults.SortDirection;

import org.springframework.transaction.annotation.Transactional;


// TODO: use JDK5 annotations to define AOP advice for data access policy.
/**
 * A Data Access Object for the beans in the
 * {@link edu.harvard.med.screensaver.model data model}. <i>WARNING: If you add
 * methods to this interface, you may need to update PointcutAdivsors in
 * spring-context-persistence.xml, if the method name is not properly handled by
 * the existing regexps.</i>.
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
   * @throws Exception 
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
  
  public AbstractEntity reattachEntity(AbstractEntity entity);
  
  public AbstractEntity reloadEntity(AbstractEntity entity);
  
  /**
   * Initializes (pre-loads) the relationships of an entity.
   * 
   * @motivation To prevent LazyInitializationExceptions when the entity's
   *             relationships are accessed when not attached to a Hibernate
   *             session.
   * @param entity
   * @param relationships each relationsihp argument is a dot-separated path of
   *          entity relationships names (excluding the top-level entity). If
   *          entity is a Library, and you want to preload all wells and wells'
   *          compounds, the relationship strings would be <code>"hbnWells",
   *          "hbnWells.hbnCompounds"</code>.
   *          Be careful with those "hbn" prefixes, as they are required! Also,
   *          it appears you need to explicitly specify intermediate
   *          relationship paths as independent arguments.
   */
  public void need(AbstractEntity entity, String... relationships);
  
  public int relationshipSize(final Object persistentCollection);

  public int relationshipSize(final AbstractEntity entity, final String relationship);

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
   * Delete the entity in the database. Caller must ensure that entity is not
   * participating in any relationships with other entities.
   * 
   * @param entity
   */
  public void deleteEntity(AbstractEntity entity);

  /**
   * Commit all pending changes to the database. Useful if issuing an HQL query
   * for data that has been created within the same Hibernate session (e.g.
   * within the same HTTP request).
   */
  public void flush();
  
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
    Serializable id);

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
  @Transactional(readOnly = true)
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
   * Find all the screening room users that are lab heads.
   * @return a List of {@link ScreeningRoomUsers}s.
   */
  @Transactional(readOnly = true)
  public List<ScreeningRoomUser> findAllLabHeads();
  
  public List<ScreeningRoomUser> findCandidateCollaborators();

  /**
   * Delete a screen result.
   * @param screenResult the screen result to delete.
   */
  public void deleteScreenResult(ScreenResult screenResult);

  /**
   * Delete library contents. Null out all the content-oriented content of all the
   * wells; delete any dangling {@link Compound Compounds}, {@link SilencingReagent
   * SilencingReagents}, and {@link Gene Genes}.
   * @param library the library to delete the contents of
   */
  public void deleteLibraryContents(Library library);
  
  /**
   * Find and return the well. Return null if there is no well.
   * @param plateNumber the plate number
   * @param wellName the beautiful well name. remember - A through P, and 1 through 24!
   * @return the well. Return null if there is no well.
   * @deprecated use {@link #findWell(WellKey)}
   */
  public Well findWell(Integer plateNumber, String wellName);
  
  public Well findWell(WellKey wellKey);
  
  
  /**
   * Find and return the silencing reagent. Return null if there is no matching
   * silencing reagent.
   * @param gene the gene the silencing reagent silences
   * @param silencingReagentType the type of silencing reagent
   * @param sequence the sequence of the silencing reagent
   * @return the silencing reagent. Return null if there is no matching
   * silencing reagent.
   */
  public SilencingReagent findSilencingReagent(
    Gene gene,
    SilencingReagentType silencingReagentType,
    String sequence);
  
  /**
   * Find and return the library that contains the specified plate. return null if
   * no such library contains the plate.
   * @param plateNumber the plate number
   * @return the library that contains the specified plate. return null if
   * no such library contains the plate.
   */
  public Library findLibraryWithPlate(Integer plateNumber);
  
  public static int SORT_BY_PLATE_WELL = -3;
  public static int SORT_BY_WELL_PLATE = -2;
  public static int SORT_BY_ASSAY_WELL_TYPE = -1;
  public Map<WellKey,List<ResultValue>> findSortedResultValueTableByRange(List<ResultValueType> rvts,
                                                                          int sortBy,
                                                                          SortDirection sortDirection,
                                                                          int fromIndex,
                                                                          int rowsToFetch,
                                                                          ResultValueType hitsOnlyRvt);
  
  public Map<WellKey,ResultValue> findResultValuesByPlate(Integer plateNumber, ResultValueType rvt);

  public Set<Well> findWellsForPlate(int plate);
  
  public void loadOrCreateWellsForLibrary(Library library);

  
  /**
   * Libraries Browser should not display DOS or Annotation libraries.
   * @return
   */
  public List<Library> findLibrariesDisplayedInLibrariesBrowser();
}
