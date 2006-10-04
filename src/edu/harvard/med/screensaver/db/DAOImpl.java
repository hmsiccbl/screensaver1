// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

import org.apache.log4j.Logger;
import org.hibernate.ObjectNotFoundException;
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

  @SuppressWarnings("unchecked")
  public <E extends AbstractEntity> List<E> findAllEntitiesWithType(
    Class<E> entityClass)
  {
    return (List<E>) getHibernateTemplate().loadAll(entityClass);
  }

  @SuppressWarnings("unchecked")
  public <E extends AbstractEntity> E findEntityById(
    final Class<E> entityClass,
    final Integer id)
  {
    return (E) getHibernateTemplate().execute(new HibernateCallback()
      {
        public Object doInHibernate(org.hibernate.Session session)
          throws org.hibernate.HibernateException, java.sql.SQLException
        {
          try {
            return session.get(entityClass, id);
          }
          catch (ObjectNotFoundException e) {
            return null;
          }
        } 
      });
  }
  
//  /**
//   * Broken. Do not use.
//   * @deprecated
//   */
//  public void refreshEntity(final AbstractEntity e)
//  {
//    getHibernateTemplate().execute(new HibernateCallback()
//    {
//      public Object doInHibernate(org.hibernate.Session session)
//        throws org.hibernate.HibernateException, java.sql.SQLException
//      {
//        AbstractEntity localEntity = e; // for debug inspection 
//        _logger.debug("entity " + e + " is " +
//                      (session.contains(e) ? "" : "NOT ") +
//                      "already in Hibernate session cache");
//        session.evict(e);
//        _logger.debug("entity " + e + "is " +
//                      (session.contains(e) ? " still " : "no longer ") +
//                      "in Hibernate session cache after evict");
//        session.load(e, e.getEntityId());
//        //session.refresh(e, LockMode.NONE);
//        _logger.debug("entity " + e + " is " +
//                      (session.contains(e) ? "no " : "STILL NOT ") + 
//                      " in Hibernate session cache after reload");
//        return e;
//      }
//    });
//  }
  
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
    diassociateScreenResult(screenResult);
    getHibernateTemplate().delete(screenResult);
    _logger.debug("deleted " + screenResult);
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
  
  /**
   * Break relationships between ScreenResult's object network and any entities
   * that do not have a "contained-in" relationship with this ScreenResult
   * object. For example, Wells, which are associated via
   * ScreenResult.ResultValueType.ResultValue.Well.
   */
  private void diassociateScreenResult(ScreenResult screenResult)
  {
    for (ResultValueType rvt : screenResult.getResultValueTypes()) {
      // we copy collection of result values to avoid ConcurrentModificationException during iteration!
      Collection<ResultValue> resultValues = new ArrayList<ResultValue>(rvt.getResultValues());
      for (ResultValue rv : resultValues) {
        rv.setWell(null);
      }
    }
    
    screenResult.getScreen().setScreenResult(null);
    screenResult.setHbnScreen(null);
  }

}
