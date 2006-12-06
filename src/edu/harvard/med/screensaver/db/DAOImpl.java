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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
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
  
  private static final Logger _logger = Logger.getLogger(DAOImpl.class);
  
  
  // public instance methods

  public void doInTransaction(DAOTransaction daoTransaction)
  {
    daoTransaction.runTransaction();
    // TODO: we should be handling exception handling and rollback in an explicit manner
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
  
  
  // public special-case data access methods
  
  @SuppressWarnings("unchecked")
  public List<ScreeningRoomUser> findAllLabHeads()
  {
    String hql = "select distinct labHead from ScreeningRoomUser u join u.hbnLabHead labHead";
    return (List<ScreeningRoomUser>) getHibernateTemplate().find(hql);
  }
  
  public void deleteScreenResult(ScreenResult screenResult)
  {
    // disassociate ScreenResult from Screen
    screenResult.getScreen().setScreenResult(null);

    getHibernateTemplate().delete(screenResult);
    _logger.debug("deleted " + screenResult);
  }
  
  public Well findWell(Integer plateNumber, String wellName)
  {
    return findEntityById(Well.class, plateNumber + ":" + wellName);
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
                                                                          final int rowsToFetch)
  {
    Map<WellKey,List<ResultValue>> mapResult = (Map<WellKey,List<ResultValue>>)
    getHibernateTemplate().execute(new HibernateCallback() 
    {
      public Object doInHibernate(Session session) throws HibernateException, SQLException
      {
        Query query = buildQueryForSortedResultValueTypeTableByRange(session,
                                                                     selectedRvts,
                                                                     sortBy,
                                                                     sortDirection);
        Map<WellKey,List<ResultValue>> mapResult = new LinkedHashMap<WellKey,List<ResultValue>>();
        ScrollableResults scrollableResults = query.scroll();
        if (scrollableResults.setRowNumber(fromIndex) && rowsToFetch > 0) {
          int rowCount = 0;
          do {
            Object[] valuesArray = scrollableResults.get();
            List<ResultValue> values = new ArrayList<ResultValue>(selectedRvts.size());
//            for (int i = 1; i < valuesArray.length; i++) {
//              values.add((ResultValue)valuesArray[i]);
//            }
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

  public void createWellsForLibrary(Library library)
  {
    for (int iPlate = library.getStartPlate(); iPlate <= library.getEndPlate(); ++iPlate) {
      Map<WellKey,Well> _extantWells = new HashMap<WellKey,Well>();
      for (Well well : findWellsForPlate(iPlate)) {
        _extantWells.put(well.getWellKey(), well);
      };
      _logger.info("creating wells for library " + library.getLibraryName() + ", plate " + iPlate);
      int wellsCreated = 0;
      for (int iRow = 0; iRow < Well.PLATE_ROWS; ++iRow) {
        for (int iCol = 0; iCol < Well.PLATE_COLUMNS; ++iCol) {
          WellKey wellKey = new WellKey(iPlate, iRow, iCol);
          if (!_extantWells.containsKey(wellKey)) {
            Well well = 
              new Well(library,
                       new Integer(iPlate),
                       wellKey.getWellName());
            persistEntity(well);
            _extantWells.put(wellKey, well);
            ++wellsCreated;
          }
        }
      }
      _logger.info("created " + wellsCreated + " wells for library " + 
                   library.getLibraryName() + ", plate " + iPlate);
      
    }
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
                                                               SortDirection sortDirection)
  {
    assert selectedRvts.size() > 0;
    assert sortBy < selectedRvts.size();

    StringBuilder hql = new StringBuilder();
    List<String> selectFields = new ArrayList<String>();
    List<String> fromClauses = new ArrayList<String>();
    List<String> whereClauses = new ArrayList<String>();
    List<String> orderByClauses = new ArrayList<String>();
    List<Integer> args = new ArrayList<Integer>();
    
    // TODO: can simplify this code now that we no longer require iteration to generate our HQL string
    String sortByRvAlias = "rv";
    selectFields.add("index(" + sortByRvAlias + ")");
    hql.append("select ").append(StringUtils.makeListString(selectFields, ", "));
    fromClauses.add("ResultValueType rvt join rvt.resultValues rv");
    hql.append(" from ").append(StringUtils.makeListString(fromClauses, ", "));
    whereClauses.add("rvt.id=?");
    args.add(selectedRvts.get(Math.max(0, sortBy)).getEntityId());
    hql.append(" where ").append(StringUtils.makeListString(whereClauses, " and "));
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
    hql.append(" order by ").append(StringUtils.makeListString(orderByClauses, ", "));
    
    if (_logger.isDebugEnabled()) {
      _logger.debug("findResultValuesByPlate executing HQL: " + hql.toString());
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
