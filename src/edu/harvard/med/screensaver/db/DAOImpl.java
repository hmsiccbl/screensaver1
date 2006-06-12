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
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.ObjectNotFoundException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import edu.harvard.med.screensaver.model.AbstractEntity;


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

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.db.DAO#doInTransaction(edu.harvard.med.screensaver.db.DAOTransaction)
   */
  public void doInTransaction(DAOTransaction daoTransaction) {
    daoTransaction.runTransaction();
  }
  
  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.db.DAO#defineEntity(java.lang.Class, java.lang.Object[])
   */
  public <E extends AbstractEntity> E defineEntity(
    Class<E> entityClass,
    Object... constructorArguments)
  {
    Constructor<E> constructor = getConstructor(entityClass, constructorArguments);
    E entity = newInstance(constructor, constructorArguments);
    getHibernateTemplate().save(entity);
    return entity;
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.db.DAO#persistEntity(edu.harvard.med.screensaver.model.AbstractEntity)
   */
  public void persistEntity(AbstractEntity entity)
  {
    getHibernateTemplate().saveOrUpdate(entity);
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.db.DAO#findAllEntitiesWithType(java.lang.Class)
   */
  @SuppressWarnings("unchecked")
  public <E extends AbstractEntity> List<E> findAllEntitiesWithType(
    Class<E> entityClass)
  {
    return (List<E>) getHibernateTemplate().loadAll(entityClass);
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.db.DAO#findEntityById(java.lang.Class, java.lang.Integer)
   */
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
            return session.load(entityClass, id);
          }
          catch (ObjectNotFoundException e) {
            return null;
          }
        } 
      });
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
      argumentTypes[i] = arguments[i].getClass();
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
}
