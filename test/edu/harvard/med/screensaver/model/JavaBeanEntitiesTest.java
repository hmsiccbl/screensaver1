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
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Test the entities as JavaBeans.
 * <p>
 * Note from s: my test code is a bit sloppy! Ha, you can still read it.
 * Them methods are way too long! Well, "it's just test code"...
 */
public class JavaBeanEntitiesTest extends JavaBeanEntitiesExercizor
{
  private static Logger log = Logger.getLogger(JavaBeanEntitiesTest.class);
  
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
  
  /**
   * Test collection properties:
   * <ul>
   * <li>have a pluralized name
   * <li>do not have a (public) setter
   * <li>have boolean add/remove methods with param of right type
   * <li>add;get returns set of one
   * <li>add;remove;get returns empty set
   */
  public void testCollectionProperties()
  {
    exercizePropertyDescriptors(new PropertyDescriptorExercizor()
      {
        public void exercizePropertyDescriptor(
          AbstractEntity bean,
          BeanInfo beanInfo,
          PropertyDescriptor propertyDescriptor)
        {
          Method getter = propertyDescriptor.getReadMethod();
          if (Collection.class.isAssignableFrom(getter.getReturnType())) {
            testCollectionProperty(bean, getter, propertyDescriptor);
          }
        }
      });
  }

  private static Map<String, String> oddPluralToSingularPropertiesMap =
    new HashMap<String, String>();
  static {
    oddPluralToSingularPropertiesMap.put("children", "child");
    oddPluralToSingularPropertiesMap.put("typesDerivedFrom", "typeDerivedFrom");
  }
  
  /**
   * Test collection property:
   * <ul>
   * <li>has a pluralized name
   * <li>does not have a (public) setter
   * <li>has boolean add/remove methods with param of right type
   * <li>add;get returns set of one
   * <li>add;remove;get returns empty set
   */
  @SuppressWarnings("unchecked")
  private void testCollectionProperty(
    AbstractEntity bean,
    Method getter,
    PropertyDescriptor propertyDescriptor)
  {
    Class<? extends AbstractEntity> beanClass = bean.getClass();
    String beanClassName = beanClass.getSimpleName();
    String propertyName = propertyDescriptor.getName();
    String fullPropName = beanClassName + "." + propertyName;
    
    // collection property has pluralized name
    assertTrue(
      "collection property getter has plural name: " + fullPropName,
      oddPluralToSingularPropertiesMap.containsKey(propertyName) ||
      propertyName.endsWith("s"));

    String singularPropName =
      oddPluralToSingularPropertiesMap.containsKey(propertyName) ?
      oddPluralToSingularPropertiesMap.get(propertyName) :
      propertyName.substring(0, propertyName.length() - 1);
    String capitalizedSingularPropName =
      singularPropName.substring(0, 1).toUpperCase() +
      singularPropName.substring(1);
    
    // collection property has no getter
    assertNull(
      "collection property has no setter: " + fullPropName,
      propertyDescriptor.getWriteMethod());

    // has boolean add methods with param of right type
    String addMethodName = "add" + capitalizedSingularPropName;
    Method addMethod = findAndCheckMethod(beanClass, addMethodName);
      
    // has boolean remove methods with param of right type
    String removeMethodName = "remove" + capitalizedSingularPropName;
    Method removeMethod = findAndCheckMethod(beanClass, removeMethodName);
    
    Method getterMethod = propertyDescriptor.getReadMethod();
    
    Class propertyType = addMethod.getParameterTypes()[0];
    Object testValue = getTestValueForType(propertyType);
    
    // add;get returns set of one
    try {
      Boolean result = (Boolean)
        addMethod.invoke(bean, testValue);
      assertTrue(
        "adding to empty collection prop returns true: " + fullPropName,
        result.booleanValue());
    }
    catch (Exception e) {
      e.printStackTrace();
      fail("add method for prop threw exception: " + fullPropName);
    }
    
    try {
      Collection result = (Collection) getterMethod.invoke(bean);
      assertEquals(
        "collection prop with one element added has size one: " + fullPropName,
        result.size(),
        1);
    }
    catch (Exception e) {
      e.printStackTrace();
      fail("getter method for prop threw exception: " + fullPropName);
    }
    
    // add;remove;get returns empty set
    try {
      Boolean result = (Boolean)
        removeMethod.invoke(bean, testValue);
      assertTrue(
        "removing to empty collection prop returns true: " + fullPropName,
        result.booleanValue());
    }
    catch (Exception e) {
      e.printStackTrace();
      fail("remove method for prop threw exception: " + fullPropName);
    }
    
    try {
      Collection result = (Collection) getterMethod.invoke(bean);
      assertEquals(
        "collection prop with one element added has size one: " + fullPropName,
        result.size(),
        0);
    }
    catch (Exception e) {
      e.printStackTrace();
      fail("getter method for prop threw exception: " + fullPropName);
    }
  }

  private Method findAndCheckMethod(
    Class<? extends AbstractEntity> beanClass,
    String methodName)
  {
    String fullMethodName = beanClass.getName() + "." + methodName;
    Method foundMethod = null;
    for (Method method : beanClass.getDeclaredMethods()) {
      if (method.getName().equals(methodName)) {
        foundMethod = method;
        break;
      }
    }
    assertNotNull(
      "collection property missing method: " + fullMethodName,
      foundMethod);
    assertEquals(
      "collection property method returns boolean: " + fullMethodName,
      foundMethod.getReturnType(),
      Boolean.TYPE);
    return foundMethod;
  }
}
