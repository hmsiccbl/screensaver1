// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.entitytesters;

import java.lang.reflect.Method;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.SemanticIDAbstractEntity;
import edu.harvard.med.screensaver.util.StringUtils;

/**
 * An abstract class for testing various aspects of a model entity. Implementing classes
 * provide an implementation of abstract method {@link #testEntity} to perform the test.
 * They are provided access to the {@link #_entityClass entity class} and the {@link
 * #_sessionFactory session factory}. They are also provided with some helper methods:
 * {@link #isEntitySubclass()}, {@link #getGetterMethodForPropertyName(String)}, and
 * {@link #getSetterMethodForPropertyName(String, Class)}.
 * 
 * @param <E> the type of abstract entity to be tested
 */
abstract public class AbstractEntityTester<E extends AbstractEntity> extends Assert
{
  
  // private static datum

  private static Logger log = Logger.getLogger(AbstractEntityTester.class);

  
  // protected instance data

  protected Class<? extends AbstractEntity> _entityClass;
  protected SessionFactory _sessionFactory;

  
  // public constructor and instance method
  
  /**
   * Construct an <code>AbstractEntityTester</code>.
   * @param entityClass the entity class
   * @param sessionFactory the session factory
   */
  public AbstractEntityTester(Class<E> entityClass, SessionFactory sessionFactory)
  {
    _entityClass = entityClass;
    _sessionFactory = sessionFactory;
  }
  
  /**
   * Perform the test for the entity.
   */
  abstract public void testEntity();
  
  
  // protected instance methods

  /**
   * Return true iff this entity class is a subclass of another entity class.
   * @return true iff this entity class is a subclass of another entity class
   */
  protected boolean isEntitySubclass()
  {
    Class entitySuperclass = _entityClass.getSuperclass();
    return ! (
      entitySuperclass.equals(AbstractEntity.class) ||
      entitySuperclass.equals(SemanticIDAbstractEntity.class));
  }

  /**
   * Get the getter method for a property based on the property name.
   * @param propertyName the property name
   * @return the getter method
   */
  protected Method getGetterMethodForPropertyName(String propertyName)
  {
    String getterName = "get" + StringUtils.capitalize(propertyName);
    try {
      return _entityClass.getDeclaredMethod(getterName);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Get the setter method for a property based on the property name and the property type.
   * @param propertyName the property name
   * @param propertyType the property type
   * @return the setter method
   */
  protected Method getSetterMethodForPropertyName(String propertyName, Class propertyType)
  {
    String setterName = "set" + StringUtils.capitalize(propertyName);
    try {
      return _entityClass.getDeclaredMethod(setterName, propertyType);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
