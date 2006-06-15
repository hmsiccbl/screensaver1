// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Test the entities as JavaBeans.
 */
public class JavaBeanEntitiesTest extends JavaBeanEntitiesExercizor
{
  
  public void testJavaBeanEntitiesTemplate()
  {
    exercizeJavaBeanEntities(new JavaBeanEntityExercizor()
      {
        public void exercizeJavaBeanEntity(
          AbstractEntity bean,
          BeanInfo beanInfo)
        {
          // copy this method and put your code here
        }
      });
  }
  
  /**
   * Test that all properties have a getter, and all properties aside from
   * set-based properties and hibernate ids have a setter
   */
  public void testPropertiesHaveGetterAndSetter()
  {
    exercizePropertyDescriptors(new PropertyDescriptorExercizor()
      {
        public void exercizePropertyDescriptor(
          AbstractEntity bean,
          BeanInfo beanInfo,
          PropertyDescriptor propertyDescriptor)
        {
          assertNotNull(
            "property has getter: " +
            bean.getClass() + "." + propertyDescriptor.getDisplayName(),
            propertyDescriptor.getReadMethod());
          
          // HACK: kludging endsWith("Id") <=> is hibernate id. should really
          // make sure this is the hibernate id
          if (! propertyDescriptor.getName().endsWith("Id")) {
            if (! Collection.class.isAssignableFrom(propertyDescriptor.getPropertyType())) {
              assertNotNull(
                "property has setter: " +
                bean.getClass() + "." + propertyDescriptor.getDisplayName(),
                propertyDescriptor.getWriteMethod());
            }
          }
        }
      });
  }
  
  /**
   * Test that all properties start out uninitialized when a bean is first
   * created.
   */
  public void testCollectionPropertiesStartOutEmpty()
  {
    exercizePropertyDescriptors(new PropertyDescriptorExercizor()
      {
        public void exercizePropertyDescriptor(
          AbstractEntity bean,
          BeanInfo beanInfo,
          PropertyDescriptor propertyDescriptor)
        {
          Method getter = propertyDescriptor.getReadMethod();
          Object result = null;
          try {
            // note that result will be Boolean when the getter returns boolean
            result = getter.invoke(bean);
          }
          catch (Exception e) {
            e.printStackTrace();
            fail(
              "getter for collection property threw exception: " +
              bean.getClass() + "." + getter.getName() + ": " + e);
          }
          if (result instanceof Collection) {
            assertEquals(
              "getter for uninitialized property returns empty collection: " +
              bean.getClass() + "." + getter.getName(),
              0,
              ((Collection) result).size());
          }
        }
      });
  }
  
  /**
   * Test that a call to the getter for a property returns the same thing
   * that the property was just set to with the setter.
   * <p>
   * Don't test set-based properties.
   */
  public void testGetterReturnsWhatSetterSet()
  {
    exercizePropertyDescriptors(new PropertyDescriptorExercizor()
      {
        public void exercizePropertyDescriptor(
          AbstractEntity bean,
          BeanInfo beanInfo,
          PropertyDescriptor propertyDescriptor)
        {
          // HACK: kludging endsWith("Id") <=> is hibernate id. should really
          // make sure this is the hibernate id
          if (propertyDescriptor.getName().endsWith("Id")) {
            return;
          }
          
          Method getter = propertyDescriptor.getReadMethod();
          if (Collection.class.isAssignableFrom(getter.getReturnType())) {
            return;
          }
          Method setter = propertyDescriptor.getWriteMethod();
          Object testValue = getTestValueForType(getter.getReturnType());
          System.out.println("test " + bean.getClass() + "." + propertyDescriptor.getName());
          try {
            setter.invoke(bean, testValue);
            if (AbstractEntity.class.isAssignableFrom(getter.getReturnType())) {
              assertSame(
                "getter returns what setter set for " +
                bean.getClass() + "." + propertyDescriptor.getName(),
                testValue,
                getter.invoke(bean));
            }
            else {
              assertEquals(
                "getter returns what setter set for " +
                bean.getClass() + "." + propertyDescriptor.getName(),
                testValue,
                getter.invoke(bean));
            }
          }
          catch (Exception e) {
            e.printStackTrace();
            fail(
              "getter or setter threw exception: " +
              bean.getClass() + "." + propertyDescriptor.getName());
          }
        }
      });
  }
}
