// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.propertytesters;

import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;

import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.entitytesters.AbstractEntityTester;

/**
 * An abstract class for testing various aspects of the properties of an entity class.
 * Implementing classes implement their tests as {@link AbstractPropertyTester
 * AbstractPropertyTesters}, returned by abstract method {@link
 * #createPropertyTester(AbstractEntity, BeanInfo, PropertyDescriptor)}.
 * 
 * @param <E> the entity class to test the properties of
 */
public abstract class AbstractPropertiesTester<E extends AbstractEntity>
extends AbstractEntityTester<E>
{
  
  // private static data

  private static Logger log = Logger.getLogger(AbstractPropertiesTester.class);

  
  // protected instance data
  
  protected BeanInfo _beanInfo;
  protected E _bean;


  // public constructor and instance methods

  /**
   * Construct a <code>AbstractPropertiesTester</code>.
   * @param entityClass the entity class
   * @param sessionFactory the hibernate session factory
   * @param beanInfo the <code>java.beans.BeanInfo</code> for the entity class
   * @param bean the entity instance to perform tests on
   */
  public AbstractPropertiesTester(
    Class<E> entityClass,
    SessionFactory sessionFactory,
    BeanInfo beanInfo,
    E bean)
  {
    super(entityClass, sessionFactory);
    _beanInfo = beanInfo;
    _bean = bean;
  }
  
  /**
   * Create and return an {@link AbstractPropertyTester} to test the property.
   * @param bean the entity instance to perform tests on
   * @param beanInfo the <code>java.beans.BeanInfo</code> for the entity class
   * @param propertyDescriptor the descriptor for the property to test
   * @return an <code>AbstractPropertyTester</code> to test the property
   */
  abstract public AbstractPropertyTester createPropertyTester(
    E bean,
    BeanInfo beanInfo,
    PropertyDescriptor propertyDescriptor);

  @Override
  public void testEntity()
  {
    for (PropertyDescriptor propertyDescriptor : _beanInfo.getPropertyDescriptors()) {
      String propertyName = propertyDescriptor.getName();
      String propertyFullName = _bean.getClass().getSimpleName() + "." + propertyName;

      if (propertyName.equals("class")) {
        log.debug("skipping \"class\" property " + propertyFullName);
        continue;
      }

      if (propertyDescriptor.getReadMethod().isAnnotationPresent(Transient.class)) {
        log.info("skipping @Transient property " + propertyFullName);
        continue;
      }

      log.info("testing JavaBean entity property " + propertyFullName + " with " + this.getClass());
      testProperty(propertyDescriptor);
    }
  }


  // private instance method

  /**
   * Test the property by creating an {@link AbstractPropertyTester} via method {@link
   * #createPropertyTester(AbstractEntity, BeanInfo, PropertyDescriptor)}, and calling its
   * {@link AbstractPropertyTester#testProperty()} method.
   * @param propertyDescriptor the descriptor for the property to test
   */
  private void testProperty(PropertyDescriptor propertyDescriptor)
  {
    AbstractPropertyTester propertyTester = createPropertyTester(
      _bean,
      _beanInfo,
      propertyDescriptor);
    propertyTester.testProperty();
  }
}
