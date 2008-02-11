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

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;

import edu.harvard.med.screensaver.model.AbstractEntity;

/**
 * Tests for the correctness of presence or absence of getter and setter methods for the entity's
 * properties. Performs the following tests:
 * <ul>
 * <li>all properties have a getter method
 * <li>{@link AbstractPropertyTester#isCollectionProperty() collection properties} do not have a setter method
 * <li>{@link AbstractPropertyTester#setterOrAdderMethodNotExpected() properties that are not expected to have a testable
 * setter or adder method} do not have a setter method
 * </ul>
 */
public class PropertiesGetterAndSetterTester<E extends AbstractEntity>
extends AbstractPropertiesTester<E>
{
  private static Logger log = Logger.getLogger(PropertiesGetterAndSetterTester.class);

  public PropertiesGetterAndSetterTester(
    Class<E> entityClass,
    SessionFactory sessionFactory,
    BeanInfo beanInfo,
    E bean)
  {
    super(entityClass, sessionFactory, beanInfo, bean);
  }

  @Override
  public AbstractPropertyTester createPropertyTester(
    E bean,
    BeanInfo beanInfo,
    PropertyDescriptor propertyDescriptor)
  {
    return new PropertyGetterAndSetterTester<E>(bean, beanInfo, propertyDescriptor);
  }

  private class PropertyGetterAndSetterTester<E2 extends AbstractEntity>
  extends AbstractPropertyTester<E2>
  {
    public PropertyGetterAndSetterTester(
      E2 bean,
      BeanInfo beanInfo,
      PropertyDescriptor propertyDescriptor)
    {
      super(bean, beanInfo, propertyDescriptor);
    }
    
    @Override
    public void testProperty()
    {
      assertNotNull("property has public getter: " + getPropertyFullname(), getReadMethod());
      
      if (setterOrAdderMethodNotExpected() || isCollectionProperty()) {
        assertNull("property does not have public setter: " + getPropertyFullname(), getWriteMethod());
      }
      else {
        assertNotNull("property has public setter: " + getPropertyFullname(), getWriteMethod());
      }
    }
  }
}
