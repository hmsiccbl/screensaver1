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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellType;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.CherryPickRequest;
import edu.harvard.med.screensaver.model.screens.LabCherryPick;
import edu.harvard.med.screensaver.model.screens.ScreenerCherryPick;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.ui.searchresults.SortDirection;
import edu.harvard.med.screensaver.ui.util.ScreensaverUserComparator;
import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.TransientObjectException;
import org.hibernate.criterion.Restrictions;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.CollectionType;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;


/**
 * A Spring+Hibernate implementation of the Data Access Object. This is the
 * de-facto DAO implementation for the time being.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class DAOImpl extends HibernateDaoSupport implements DAO
{

  // private static fields
  
  private static final Logger log = Logger.getLogger(DAOImpl.class);
  private static final Logger entityInflatorLog = Logger.getLogger(DAOImpl.class.getName() + ".EntityInflator");
  
  
  // public instance methods

  public void doInTransaction(DAOTransaction daoTransaction)
  {
    daoTransaction.runTransaction();
  }
  
  public <E extends AbstractEntity> E defineEntity(
    Class<E> entityClass,
    Object... constructorArguments)
  {
    Constructor<E> constructor = getConstructor(entityClass, constructorArguments);
    E entity = newInstance(constructor, constructorArguments);
    getHibernateTemplate().save(entity);
    return entity;
  }

  public void persistEntity(AbstractEntity entity)
  {
    getHibernateTemplate().saveOrUpdate(entity);
  }
  
  public <E extends AbstractEntity> E reattachEntity(E entity)
  {
    // TODO: use lock(), instead of cascade(), after updating Hibernate entity model to cascade locks (as needed)
//    // lock() does cascade, but we haven't configured our entities to cascade on locks (we're using cascade="save-update" currently)
//    getHibernateTemplate().lock(entity, LockMode.READ);
    // update() cascades, but also increments the entity's version counter, which is not really what we want
    getHibernateTemplate().update(entity);
    return entity;
  }
  
  /**
   * Reloads an entity into the current session. The assumption is that the
   * entity does not already exist in the Hibernate session (it's okay if it
   * does). This method is really just loading the entity; we use "reload" in
   * its name to denote its intended usage, which is to load an entity from a
   * defunct session into the current session.
   * 
   * @param entity the entity to be reloaded;
   * @return a new Hibernate-managed instance of the specified entity
   */
  @SuppressWarnings("unchecked")
  public <E extends AbstractEntity> E reloadEntity(E entity)
  {
    if (entity != null) {
      log.debug("reloading entity " + entity);
      return (E) findEntityById(entity.getClass(), entity.getEntityId());
    }
    return null;
  }
  
  public void need(final AbstractEntity entity,
                   final String... relationships)
  {
    if (entity == null) {
      return;
    }
    long start = 0;
    if (entityInflatorLog.isDebugEnabled()) {
      entityInflatorLog.debug("inflating " + entity + " for relationships: " + relationships);
      start = System.currentTimeMillis();
    }
    getHibernateTemplate().execute(new HibernateCallback() 
    {
      public Object doInHibernate(Session session) throws HibernateException, SQLException
      {
        Criteria criteria = session.createCriteria(entity.getClass());
        criteria.add(Restrictions.idEq(entity.getEntityId()));
        for (String relationship : relationships) {
          if (log.isDebugEnabled()) {
            verifyEntityRelationshipExists(session, entity.getClass(), relationship);
          }
          criteria.setFetchMode(relationship, FetchMode.JOIN);
        }
        return criteria.list();
      }

      private boolean verifyEntityRelationshipExists(Session session, Class entityClass, String relationship)
      {
        ClassMetadata metadata = session.getSessionFactory().getClassMetadata(entityClass);
        if (relationship.contains(".")) {
          int next = relationship.indexOf(".");
          String nextRelationship = relationship.substring(next + 1);
          relationship = relationship.substring(0, next);
          if (!verifyEntityRelationshipExists(session, entityClass, relationship)) {
            return false;
          }
          
          org.hibernate.type.Type nextType = metadata.getPropertyType(relationship);
          if (nextType.isCollectionType()) {
            nextType = ((CollectionType) nextType).getElementType((SessionFactoryImplementor) session.getSessionFactory());
          }
          Class nextEntityClass = nextType.getReturnedClass();
          return verifyEntityRelationshipExists(session, 
                                                nextEntityClass,
                                                nextRelationship);
        }
        else {
          if (!Arrays.asList(metadata.getPropertyNames()).contains(relationship)) {
            // TODO: this should probably be a Java assert instead of just a log error msg
            log.error("relationship does not exist: " + entityClass.getSimpleName() + "." + relationship);
            return false;
          }
          return true;
        }
      }
      
    });
    if (entityInflatorLog.isDebugEnabled()) {
      entityInflatorLog.debug("inflating " + entity + " took " + (System.currentTimeMillis() - start) / 1000.0 + " seconds");
    }
  }
  
  public int relationshipSize(final Object persistentCollection)
  {
    return (Integer) getHibernateTemplate().execute(new HibernateCallback() 
    {
      public Object doInHibernate(Session session) throws HibernateException, SQLException
      {
        return ((Integer) session.createFilter(persistentCollection, "select count(*)" ).list().get(0)).intValue();
      }
    });
  }
  
  public int relationshipSize(final AbstractEntity entity, final String relationship)
  {
    return (Integer) getHibernateTemplate().execute(new HibernateCallback() 
    {
      public Object doInHibernate(Session session) throws HibernateException, SQLException
      {
        String entityName = session.getEntityName(entity);
        String idProperty = session.getSessionFactory().getClassMetadata(entityName).getIdentifierPropertyName();
        Query query = session.createQuery("select count(*) from " + entityName + " e join e." + relationship + " where e." + idProperty + " = :id");
        query.setString("id", entity.getEntityId().toString());
        return query.list().get(0);
      }
    });
  }

  public int relationshipSize(
    final AbstractEntity entity,
    final String relationship,
    final String relationshipProperty,
    final String relationshipPropertyValue)
  {
    return (Integer) getHibernateTemplate().execute(new HibernateCallback() 
    {
      public Object doInHibernate(Session session) throws HibernateException, SQLException
      {
        String entityName = session.getEntityName(entity);
        String idProperty = session.getSessionFactory().getClassMetadata(entityName).getIdentifierPropertyName();
        Query query = session.createQuery(
          "select count(*) from " + entityName + " e join e." + relationship + " r " +
          "where e." + idProperty + " = :id " +
          "and r." + relationshipProperty + " = :propValue");
        query.setString("id", entity.getEntityId().toString());
        query.setString("propValue", relationshipPropertyValue);
        return query.list().get(0);
      }
    });
  }
  
  public void deleteEntity(AbstractEntity entity)
  {
    getHibernateTemplate().delete(entity);
  }
  
  public void flush()
  {
    getHibernateTemplate().flush();
  }

  @SuppressWarnings("unchecked")
  public <E extends AbstractEntity> List<E> findAllEntitiesWithType(
    Class<E> entityClass)
  {
    return (List<E>) getHibernateTemplate().loadAll(entityClass);
  }

  @SuppressWarnings("unchecked")
  public <E extends AbstractEntity> E findEntityById(
    Class<E> entityClass,
    Serializable id)
  {
    return (E) getHibernateTemplate().get(entityClass, id);
  }

  @SuppressWarnings("unchecked")
  public <E extends AbstractEntity> List<E> findEntitiesByProperties(
    Class<E> entityClass,
    Map<String,Object> name2Value)
  {
    String entityName = entityClass.getSimpleName();
    StringBuffer hql = new StringBuffer();
    boolean first = true;
    for (String propertyName : name2Value.keySet()) {
      if (first) {
        hql.append("from " + entityName + " x where ");
        first = false;
      }
      else {
        hql.append(" and ");
      }
      hql.append("x.")
         .append(propertyName)
         .append(" = ?");
    }
    return (List<E>) getHibernateTemplate().find(hql.toString(),
                                                 name2Value.values()
                                                           .toArray());
  }
  
  public <E extends AbstractEntity> E findEntityByProperties(
    Class<E> entityClass,
    Map<String,Object> name2Value)
  {
    List<E> entities = findEntitiesByProperties(
      entityClass,
      name2Value);
    if (entities.size() == 0) {
      return null;
    }
    if (entities.size() > 1) {
      throw new IllegalArgumentException(
        "more than one result for DAO.findEntityByProperties");
    }
    return entities.get(0);
  }
  
  @SuppressWarnings("unchecked")
  public <E extends AbstractEntity> List<E> findEntitiesByProperty(
    Class<E> entityClass,
    String propertyName,
    Object propertyValue)
  {
    // note: could delegate this method body to findEntitiesByProperties, but
    // this would require wrapping up property{Name,Value} into a Map object,
    // for no good reason other than (minimal) code sharing
    String entityName = entityClass.getSimpleName();
    String hql = "from " + entityName + " x where x." + propertyName + " = ?";
    return (List<E>) getHibernateTemplate().find(hql, propertyValue);
  }
  
  public <E extends AbstractEntity> E findEntityByProperty(
    Class<E> entityClass,
    String propertyName,
    Object propertyValue)
  {
    List<E> entities = findEntitiesByProperty(
      entityClass,
      propertyName,
      propertyValue);
    if (entities.size() == 0) {
      return null;
    }
    if (entities.size() > 1) {
      throw new IllegalArgumentException(
        "more than one result for DAO.findEntityByProperty");
    }
    return entities.get(0);
  }
  
  @SuppressWarnings("unchecked")
  public <E extends AbstractEntity> List<E> findEntitiesByPropertyPattern(
    Class<E> entityClass,
    String propertyName,
    String propertyPattern)
  {
    String entityName = entityClass.getSimpleName();
    String hql = "from " + entityName + " x where x." + propertyName + " like ?";
    propertyPattern = propertyPattern.replaceAll( "\\*", "%" );
    return (List<E>) getHibernateTemplate().find(hql, propertyPattern);
  }
  
  @SuppressWarnings("unchecked")
  public <E extends AbstractEntity> List<E> findEntitiesByHql(
    Class<E> entityClass,
    String hql,
    Object [] hqlParameters)
  {
    return (List<E>) getHibernateTemplate().find(hql, hqlParameters);
  }
  
  
  // public special-case data access methods
  
  @SuppressWarnings("unchecked")
  public SortedSet<ScreeningRoomUser> findAllLabHeads()
  {
    // note: we perform sorting via a TreeSet, rather than asking persistence
    // layer to do sorting, as this keeps sorting order policy in
    // ScreensaverUserComparator, and also keeps our query simpler. Also, the
    // SortedSet return type makes return value more explicit
    String hql = "select distinct lh from ScreeningRoomUser lh left outer join lh.hbnLabHead where lh.hbnLabHead is null";
    SortedSet labHeads = new TreeSet<ScreeningRoomUser>(ScreensaverUserComparator.getInstance());
    labHeads.addAll((List<ScreeningRoomUser>) getHibernateTemplate().find(hql));
    return labHeads;
  }
  
  @SuppressWarnings("unchecked")
  public SortedSet<ScreeningRoomUser> findCandidateCollaborators()
  {
    // note: we perform sorting via a TreeSet, rather than asking persistence
    // layer to do sorting, as this keeps sorting order policy in
    // ScreensaverUserComparator, and also keeps our query simpler. Also, the
    // SortedSet return type makes return value more explicit. Performance
    // is not really an issue.
    SortedSet<ScreeningRoomUser> collaborators = 
      new TreeSet<ScreeningRoomUser>(ScreensaverUserComparator.getInstance());
    collaborators.addAll((List<ScreeningRoomUser>) 
                         getHibernateTemplate().execute(new HibernateCallback() 
                         {
                           public Object doInHibernate(Session session) throws HibernateException, SQLException
                           {
                             return new ArrayList<ScreeningRoomUser>(session.
                               createCriteria(ScreeningRoomUser.class).
                               list());
                           }
                         }));
    return collaborators;
  }

  public void deleteScreenResult(ScreenResult screenResult)
  {
    // disassociate ScreenResult from Screen
    screenResult.getScreen().setScreenResult(null);

    getHibernateTemplate().delete(screenResult);
    log.debug("deleted " + screenResult);
  }
  
  public void deleteLibraryContents(Library library)
  {
    log.error("call TODO daoImpl.deleteLibraryContents");
    for (Well well : library.getWells()) {
      well.setGenbankAccessionNumber(null);
      well.setIccbNumber(null);
      well.setMolfile(null);
      well.setSmiles(null);
      well.removeCompounds();
      well.removeSilencingReagents();
      well.setWellType(WellType.EMPTY);
    }
  }
  
  public void deleteScreenerCherryPick(ScreenerCherryPick screenerCherryPick)
  {
    if (screenerCherryPick.getCherryPickRequest().isAllocated()) {
      throw new BusinessRuleViolationException("cannot delete a screener cherry pick for a cherry pick request that has been allocated");
    }

    // disassociate from related entities
    screenerCherryPick.getCherryPickRequest().getScreenerCherryPicks().remove(screenerCherryPick);
    for (LabCherryPick cherryPick : new ArrayList<LabCherryPick>(screenerCherryPick.getLabCherryPicks())) {
      deleteLabCherryPick(cherryPick);
    }

    getHibernateTemplate().delete(screenerCherryPick);
  }

  public void deleteLabCherryPick(LabCherryPick labCherryPick)
  {
    if (labCherryPick.getCherryPickRequest().isAllocated()) {
      throw new BusinessRuleViolationException("cannot delete a lab cherry pick for a cherry pick request that has been allocated");
    }

    // disassociate from related entities
    labCherryPick.getCherryPickRequest().getLabCherryPicks().remove(labCherryPick);
    if (labCherryPick.getSourceCopy() != null) {
      labCherryPick.getSourceCopy().getHbnLabCherryPicks().remove(labCherryPick);
    }
    if (labCherryPick.getAssayPlate() != null) {
      labCherryPick.getAssayPlate().getLabCherryPicks().remove(labCherryPick);
    }

    getHibernateTemplate().delete(labCherryPick);
  }

  public void deleteCherryPickRequest(CherryPickRequest cherryPickRequest)
  {
    deleteCherryPickRequest(cherryPickRequest, false);
  }
  
  public void deleteCherryPickRequest(
    final CherryPickRequest cherryPickRequestIn,
    boolean bypassBusinessRuleViolationChecks)
  {
    CherryPickRequest cherryPickRequest = (CherryPickRequest) reattachEntity(cherryPickRequestIn);
    // note: the cherryPickRequest.screen child-to-parent relationship is not cascaded, so although
    // screen entity *will* be available (it never loaded as a proxy), the screen's
    // relationships will not be reattached and will thus be inaccessible; in
    // particular, screen.cherryPickRequests (needed below)
    reattachEntity(cherryPickRequestIn.getScreen());

    if (! bypassBusinessRuleViolationChecks) {
      if (cherryPickRequestIn.isAllocated()) {
        throw new BusinessRuleViolationException("cannot delete a cherry pick request that has been allocated");
      }
    }

    // disassociate from related entities
    
    cherryPickRequest.getRequestedBy().getHbnCherryPickRequests().remove(cherryPickRequest);
    cherryPickRequest.getScreen().getCherryPickRequests().remove(cherryPickRequest);    
    getHibernateTemplate().delete(cherryPickRequest);
  }

  public void deleteAllCherryPickRequests()
  {
    // TODO: want to do the following, as in Spring 2.0 API:
    // http://www.springframework.org/docs/api/org/springframework/orm/hibernate/HibernateTemplate.html#delete(java.lang.String)
    // but it doesn't work - it ends up treating the String as an entity, and fails.
    // are we not at Spring 2.0 for this one yet? look into upgrading to be able to use the
    // (hopefully) faster approach
    //getHibernateTemplate().delete("from CherryPickRequest");
    getHibernateTemplate().deleteAll(getHibernateTemplate().find("from CherryPickRequest"));
  }
  
  public Well findWell(WellKey wellKey)
  {
    return findEntityById(Well.class, wellKey.getKey());
  }

  public SilencingReagent findSilencingReagent(
    Gene gene,
    SilencingReagentType silencingReagentType,
    String sequence)
  {
    return findEntityById(
      SilencingReagent.class,
      gene.toString() + ":" + silencingReagentType.toString() + ":" + sequence);
  }
  
  @SuppressWarnings("unchecked")
  public Library findLibraryWithPlate(Integer plateNumber)
  {
    String hql =
      "select library from Library library where " +
      plateNumber + " between library.startPlate and library.endPlate";
    List<Library> libraries = (List<Library>) getHibernateTemplate().find(hql);
    if (libraries.size() == 0) {
      return null;
    }
    return libraries.get(0); 
  }
  
  /*
    select index(rv), rv.value, rv.assayWellType, rv.exclude from ResultValueType rvt join rvt.resultValues rv where rvt.id=? and substring(index(rv),1,5) = ?
   */
  public Map<WellKey,ResultValue> findResultValuesByPlate(Integer plateNumber, ResultValueType rvt)
  {
    String hql = "select index(rv), elements(rv) " +
        "from ResultValueType rvt join rvt.resultValues rv " +
        "where rvt.id=? and substring(index(rv),1," + Well.PLATE_NUMBER_LEN + ") = ?";
    String paddedPlateNumber = String.format("%0" + Well.PLATE_NUMBER_LEN + "d", plateNumber);
    List hqlResult = getHibernateTemplate().find(hql.toString(), new Object[] { rvt.getEntityId(), paddedPlateNumber });
    Map<WellKey,ResultValue> result = new HashMap<WellKey,ResultValue>(hqlResult.size());
    for (Iterator iter = hqlResult.iterator(); iter.hasNext();) {
      Object[] row = (Object[]) iter.next();
      result.put((WellKey) row[0],
                 (ResultValue) row[1]);
    }
    return result;
  }
                                                          
  
  /*
  For example, sorting on 2nd RVT:
    select index(rv2), elements(rv1.value), elements(rv2.value), elements(rv3.value)
    from ResultValueType rvt1 join rvt1.resultValues rv1,
         ResultValueType rvt2 join rvt2.resultValues rv2,
         ResultValueType rvt3 join rvt3.resultValues rv3,
    where
          rvt1.id=? and
          rvt2.id=? and
          rvt3.id=? and
          index(rv1)=index(rv2) and
          index(rv3)=index(rv2)
    order by rv2.value
   */
  // TODO: due to denormalized design, this method requires all 384 ResultValues to exist for each ResultValueType
  @SuppressWarnings("unchecked")
  public Map<WellKey,List<ResultValue>> findSortedResultValueTableByRange(final List<ResultValueType> selectedRvts,
                                                                          final int sortBy,
                                                                          final SortDirection sortDirection,
                                                                          final int fromIndex,
                                                                          final int rowsToFetch,
                                                                          final ResultValueType hitsOnlyRvt)
  {
    Map<WellKey,List<ResultValue>> mapResult = (Map<WellKey,List<ResultValue>>)
    getHibernateTemplate().execute(new HibernateCallback() 
    {
      public Object doInHibernate(Session session) throws HibernateException, SQLException
      {
        Query query = buildQueryForSortedResultValueTypeTableByRange(session,
                                                                     selectedRvts,
                                                                     sortBy,
                                                                     sortDirection,
                                                                     hitsOnlyRvt);
        Map<WellKey,List<ResultValue>> mapResult = new LinkedHashMap<WellKey,List<ResultValue>>();
        ScrollableResults scrollableResults = query.scroll();
        if (scrollableResults.setRowNumber(fromIndex) && rowsToFetch > 0) {
          int rowCount = 0;
          do {
            Object[] valuesArray = scrollableResults.get();
            List<ResultValue> values = new ArrayList<ResultValue>(selectedRvts.size());
            mapResult.put(new WellKey(valuesArray[0].toString()), values);
          } while (scrollableResults.next() && ++rowCount < rowsToFetch);
          
          // now add the ResultValues 
          Map<WellKey,List<ResultValue>> secondaryMapResult = 
            findRelatedResultValues(session,
                                    mapResult.keySet(),
                                    selectedRvts);
          for (Map.Entry<WellKey,List<ResultValue>> entry : mapResult.entrySet()) {
            entry.getValue().addAll(secondaryMapResult.get(entry.getKey()));
          }

          
        }
        return mapResult;
      }

    });
    return mapResult;
  }
  
  @SuppressWarnings("unchecked")
  public Set<Well> findWellsForPlate(int plate)
  {
    return new TreeSet<Well>(getHibernateTemplate().find("from Well where plateNumber = ?", plate));
  }

  @SuppressWarnings("unchecked")
  public Set<LabCherryPick> findLabCherryPicksForWell(Well well)
  {
    return new HashSet<LabCherryPick>(
      getHibernateTemplate().find("from LabCherryPick where sourceWell = ?", well));
  }

  @SuppressWarnings("unchecked")
  public Set<ScreenerCherryPick> findScreenerCherryPicksForWell(Well well)
  {
    return new HashSet<ScreenerCherryPick>(
      getHibernateTemplate().find("from ScreenerCherryPick where screenedWell = ?", well));
  }
  
  @SuppressWarnings("unchecked")
  public void loadOrCreateWellsForLibrary(Library library)
  {
    // this might not perform awesome, but:
    //   - is correct, in terms of the "load" part of method contract, since it is
    //     always possible that some but not all of the library's wells have already
    //     been loaded into the session.
    //   - presumably this method is not called in time-critical sections of code
    // further performance improvements possible by checking if a single well (or
    // something like that) was in the session, but this fails to be correct, in
    // terms of the "load" part of the method contract, although it will not cause
    // any errors, just perf problems later when code is forced to get wells one at
    // a time.
    Collection<Well> wells;
    try {
      wells = library.getWells();
    }
    catch (TransientObjectException e) {
      wells = getHibernateTemplate().find(
        "from Well where plateNumber >= ? and plateNumber <= ?",
        new Object [] { library.getStartPlate(), library.getEndPlate() });
    }
    if (wells.size() > 0) {
      return;
    }
    for (int iPlate = library.getStartPlate(); iPlate <= library.getEndPlate(); ++iPlate) {
      for (int iRow = 0; iRow < Well.PLATE_ROWS; ++iRow) {
        for (int iCol = 0; iCol < Well.PLATE_COLUMNS; ++iCol) {
          persistEntity(new Well(library, new WellKey(iPlate, iRow, iCol), WellType.EMPTY));
        }
      }
    }
    log.info("created wells for library " + library.getLibraryName());
  }
  
  @SuppressWarnings("unchecked")
  public List<Library> findLibrariesDisplayedInLibrariesBrowser()
  {
    // TODO: make this HQL type-safe by using LibraryType enum to obtain the values
    return new ArrayList<Library>(getHibernateTemplate().find(
      "from Library where libraryType not in ('Annotation', 'DOS', 'NCI', 'Discrete')")); 
  }
  
  @SuppressWarnings("unchecked")
  public List<String> findDeveloperECommonsIds()
  {
    return new ArrayList<String>(getHibernateTemplate().find(
      "select ECommonsId from ScreensaverUser where ECommonsId != null and 'developer' in elements(screensaverUserRoles)"));
  }
  

  // private instance methods

  /**
   * Get the constructor for the given Entity class and arguments.
   * @param <E> the entity type
   * @param entityClass the entity class
   * @param arguments the (possibly empty) constructor arguments
   * @return the constructor for the given Entity class and arguments
   * @exception IllegalArgumentException whenever the implied constructor
   * does not exist or is not public
   */
  private <E extends AbstractEntity> Constructor<E> getConstructor(
    Class<E> entityClass,
    Object... arguments)
  {
    Class[] argumentTypes = getArgumentTypes(arguments);
    try {
      return entityClass.getConstructor(argumentTypes);
    }
    catch (SecurityException e) {
      throw new IllegalArgumentException(e);
    }
    catch (NoSuchMethodException e) {
      throw new IllegalArgumentException(e);
    }
  }

  /**
   * Return an array of types that correspond to the array of arguments.
   *  
   * @param arguments the arguments to get the types for
   * @return an array of types that correspond to the array of arguments
   */
  private Class[] getArgumentTypes(Object [] arguments)
  {
    Class [] argumentTypes = new Class [arguments.length];
    for (int i = 0; i < arguments.length; i++) {
      Class argumentType = arguments[i].getClass();
      if (argumentType.equals(Boolean.class)) {
        argumentType = Boolean.TYPE;
      }
      argumentTypes[i] = argumentType;
    }
    return argumentTypes;
  }
  
  /**
   * Construct and return a new entity object.
   * 
   * @param <E> the entity type
   * @param constructor the constructor to invoke
   * @param constructorArguments the (possibly empty) list of arguments to
   * pass to the constructor
   * @return the newly constructed entity object
   */
  private <E extends AbstractEntity> E newInstance(
    Constructor<E> constructor,
    Object... constructorArguments)
  {
    try {
      return constructor.newInstance(constructorArguments);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(e);
    }
    catch (InstantiationException e) {
      throw new IllegalArgumentException(e);
    }
    catch (IllegalAccessException e) {
      throw new IllegalArgumentException(e);
    }
    catch (InvocationTargetException e) {
      throw new IllegalArgumentException(e);
    }
  }
  
  private Query buildQueryForSortedResultValueTypeTableByRange(Session session,
                                                               List<ResultValueType> selectedRvts,
                                                               int sortBy,
                                                               SortDirection sortDirection,
                                                               ResultValueType hitsOnlyRvt)
  {
    assert selectedRvts.size() > 0;
    assert sortBy < selectedRvts.size();
    assert hitsOnlyRvt == null || selectedRvts.contains(hitsOnlyRvt);

    StringBuilder hql = new StringBuilder();
    List<String> selectFields = new ArrayList<String>();
    List<String> fromClauses = new ArrayList<String>();
    List<String> whereClauses = new ArrayList<String>();
    List<String> orderByClauses = new ArrayList<String>();
    List<Integer> args = new ArrayList<Integer>();
    
    // TODO: can simplify this code now that we no longer require iteration to generate our HQL string
    String sortByRvAlias = "rv";
    selectFields.add("index(" + sortByRvAlias + ")");
    fromClauses.add("ResultValueType rvt join rvt.resultValues rv");
    whereClauses.add("rvt.id=?");
    args.add(selectedRvts.get(Math.max(0, sortBy)).getEntityId());

    if (hitsOnlyRvt != null) {
      // TODO: this makes the query quite slow, as it has to join all ResultValues for 2 ResultValueTypes
      fromClauses.add("ResultValueType hitsOnlyRvt join hitsOnlyRvt.resultValues hitsOnlyRv");
      whereClauses.add("hitsOnlyRv.hit = true");
      whereClauses.add("index(hitsOnlyRv) = index(rv)");
      whereClauses.add("hitsOnlyRvt.id=?");
      args.add(hitsOnlyRvt.getEntityId());
    }
    
    String sortDirStr = sortDirection.equals(SortDirection.ASCENDING)? " asc" : " desc";
    if (sortBy >= 0) {
      if (selectedRvts.get(sortBy).isNumeric()) {
        orderByClauses.add(sortByRvAlias + ".numericValue" + sortDirStr);
      }
      else {
        orderByClauses.add(sortByRvAlias + ".value" + sortDirStr);
      }
    }
    else if (sortBy == SORT_BY_PLATE_WELL) {
      orderByClauses.add("index(" + sortByRvAlias + ")" + sortDirStr);
    }
    else if (sortBy == SORT_BY_WELL_PLATE) {
      orderByClauses.add("substring(index(" + sortByRvAlias + "),7,3)" + sortDirStr);
      orderByClauses.add("substring(index(" + sortByRvAlias + "),1,5)");
    }
    else if (sortBy == SORT_BY_ASSAY_WELL_TYPE) {
      orderByClauses.add(sortByRvAlias + ".assayWellType" + sortDirStr);
    }

    hql.append("select ").append(StringUtils.makeListString(selectFields, ", "));
    hql.append(" from ").append(StringUtils.makeListString(fromClauses, ", "));
    hql.append(" where ").append(StringUtils.makeListString(whereClauses, " and "));
    hql.append(" order by ").append(StringUtils.makeListString(orderByClauses, ", "));
    
    if (log.isDebugEnabled()) {
      log.debug("buildQueryForSortedResultValueTypeTableByRange() executing HQL: " + hql.toString());
    }

    Query query = session.createQuery(hql.toString());
    for (int i = 0; i < args.size(); ++i) {
      query.setInteger(i, args.get(i));
    }
    return query;
  }

  private Map<WellKey,List<ResultValue>> findRelatedResultValues(Session session,
                                                                 Set<WellKey> wellKeys, 
                                                                 List<ResultValueType> selectedRvts)
  {
    String wellKeysList = StringUtils.makeListString(StringUtils.wrapStrings(wellKeys, "'", "'"), ",");
    Map<WellKey,List<ResultValue>> result = new HashMap<WellKey,List<ResultValue>>();
    for (int i = 0; i < selectedRvts.size(); i++) {
      ResultValueType rvt = selectedRvts.get(i);
      
      StringBuilder hql = new StringBuilder();
      // TODO: see if we can produce an equivalent HQL query that does not need to use the result_value_type table at all, as result_value_type_result_values.result_value_type_id can be used directly (at least, if we were doing this directly with sql)
      hql.append("select indices(rv), elements(rv) from ResultValueType rvt join rvt.resultValues rv where rvt.id = " + 
                 rvt.getEntityId() + " and index(rv) in (" + wellKeysList + ")");
      Query query = session.createQuery(hql.toString());
      for (Iterator iter = query.list().iterator(); iter.hasNext();) {
        Object[] row = (Object[]) iter.next();
        WellKey wellKey = new WellKey(row[0].toString());
        List<ResultValue> resultValues = result.get(wellKey);
        if (resultValues == null) {
          resultValues = new ArrayList<ResultValue>();
          result.put(wellKey, resultValues);
        }
        resultValues.add((ResultValue) row[1]);
      }
    }
    return result;
  }
  
}
