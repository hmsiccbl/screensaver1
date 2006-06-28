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
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collection;

import org.apache.log4j.Logger;

/**
 * Test the entities as JavaBeans.
 * <p>
 * Note from s: my test code is a bit sloppy! Ha, you can still read it.
 * Them methods are way too long! Well, "it's just test code"...
 */
public class EntityBeansTest extends EntityBeansExercizor
{
  private static Logger log = Logger.getLogger(EntityBeansTest.class);
  
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
          // TODO: check if the getter returns a collection before
          // invoking it
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
   * </ul>
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
      EntityBeansTest.oddPluralToSingularPropertiesMap.containsKey(propertyName) ||
      propertyName.endsWith("s"));

    String singularPropName =
      EntityBeansTest.oddPluralToSingularPropertiesMap.containsKey(propertyName) ?
      EntityBeansTest.oddPluralToSingularPropertiesMap.get(propertyName) :
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
    Method addMethod = findAndCheckMethod(beanClass, addMethodName, true);
      
    // has boolean remove methods with param of right type
    String removeMethodName = "remove" + capitalizedSingularPropName;
    Method removeMethod = findAndCheckMethod(beanClass, removeMethodName, false);
    
    Method getterMethod = propertyDescriptor.getReadMethod();
    
    Class propertyType = addMethod.getParameterTypes()[0];
    Object testValue = getTestValueForType(propertyType);
    
    // add the test value
    try {
      Boolean result = (Boolean) addMethod.invoke(bean, testValue);
      assertTrue(
        "adding to empty collection prop returns true: " + fullPropName,
        result.booleanValue());
    }
    catch (Exception e) {
      e.printStackTrace();
      fail("add method for prop threw exception: " + fullPropName);
    }
    
    // call the checker to test it was added
    try {
      Collection result = (Collection) getterMethod.invoke(bean);
      assertEquals(
        "collection prop with one element added has size one: " + fullPropName,
        result.size(),
        1);
      assertEquals(
        "collection prop with one element added has that element: " + fullPropName,
        testValue,
        result.iterator().next());
    }
    catch (Exception e) {
      e.printStackTrace();
      fail("getter method for prop threw exception: " + fullPropName);
    }
    
    if (removeMethod == null) {
      return;
    }
    
    // remove the test value from the collection
    try {
      Boolean result = (Boolean) removeMethod.invoke(bean, testValue);
      assertTrue(
        "removing to empty collection prop returns true: " + fullPropName,
        result.booleanValue());
    }
    catch (Exception e) {
      e.printStackTrace();
      fail("remove method for prop threw exception: " + fullPropName);
    }
    
    // call the checker to test it was removed
    try {
      Collection result = (Collection) getterMethod.invoke(bean);
      assertEquals(
        "collection prop with element removed has size zero: " + fullPropName,
        result.size(),
        0);
    }
    catch (Exception e) {
      e.printStackTrace();
      fail("getter method for prop threw exception: " + fullPropName);
    }
  }

  public void testRelationshipBidirectionality()
  {
    exercizePropertyDescriptors(new PropertyDescriptorExercizor()
      {
        public void exercizePropertyDescriptor(
          AbstractEntity bean,
          BeanInfo beanInfo,
          PropertyDescriptor propertyDescriptor)
        {
          Method getter = propertyDescriptor.getReadMethod();
          if (AbstractEntity.class.isAssignableFrom(getter.getReturnType())) {
            testBidirectionalityOfOneSideOfRelationship(
              bean,
              beanInfo,
              propertyDescriptor,
              getter);
          }
          else if (Collection.class.isAssignableFrom(getter.getReturnType())) {
            testBidirectionalityOfManySideOfRelationship(
              bean,
              beanInfo,
              propertyDescriptor,
              getter);
          }
        }
      });
  }
  
  private void testBidirectionalityOfOneSideOfRelationship(
    AbstractEntity bean,
    BeanInfo beanInfo,
    PropertyDescriptor propertyDescriptor,
    Method getter)
  {
    String propFullName = bean.getClass() + "." + propertyDescriptor.getName();
    
    // get basic objects for the other side of the reln
    Class relatedBeanClass = getter.getReturnType();
    Object relatedBean = getTestValueForType(relatedBeanClass);
    String relatedBeanClassName = relatedBeanClass.getSimpleName();
    BeanInfo relatedBeanInfo = null;
    try {
      relatedBeanInfo = Introspector.getBeanInfo(relatedBeanClass);
    }
    catch (IntrospectionException e) {
      e.printStackTrace();
      fail("failed to introspect entity class: " + relatedBeanClass);
    }
    
    // get the property name for the other side of the reln
    String relatedPropertyName = bean.getClass().getSimpleName();
    relatedPropertyName =
      relatedPropertyName.substring(0, 1).toLowerCase() +
      relatedPropertyName.substring(1);
    String relatedPluralPropertyName =
        oddSingularToPluralPropertiesMap.containsKey(relatedPropertyName) ?
        oddSingularToPluralPropertiesMap.get(relatedPropertyName) :
        relatedPropertyName + "s";
        
    // get the prop descr for the other side, and determine whether the
    // other side is one or many
    PropertyDescriptor relatedPropertyDescriptor = null;
    boolean otherSideIsMany = false;
    for (PropertyDescriptor descriptor : relatedBeanInfo.getPropertyDescriptors()) {
      if (descriptor.getName().equals(relatedPropertyName)) {
        relatedPropertyDescriptor = descriptor;
        break;
      }
      if (descriptor.getName().equals(relatedPluralPropertyName)) {
        relatedPropertyDescriptor = descriptor;
        otherSideIsMany = true;
        break;
      }
    }
    assertNotNull(
      "related bean " + relatedBeanClassName + " has property with name " +
      relatedPropertyName + " or " + relatedPluralPropertyName,
      relatedPropertyDescriptor);
    
    // invoke the setter on this side
    Method setter = propertyDescriptor.getWriteMethod();
    try {
      setter.invoke(bean, relatedBean);
    }
    catch (Exception e) {
      e.printStackTrace();
      fail("setter threw exception: " + propFullName);
    }
    
    Method relatedGetter = relatedPropertyDescriptor.getReadMethod();
    
    if (otherSideIsMany) {
      try {
        Collection result = (Collection) relatedGetter.invoke(relatedBean);
        assertEquals(
          "related.getter() returns set of size 1 for " + propFullName,
          1,
          result.size());
        assertSame(
          "related.getter() returns this after this.setter(related) for " +
          propFullName,
          bean,
          result.iterator().next());
      }
      catch (Exception e) {
        e.printStackTrace();
        fail("related getter threw exception: " + propFullName);
      }
    }
    else {
      try {
        assertSame(
          "related.getter() returns this after this.setter(related) for " +
          propFullName,
          bean,
          relatedGetter.invoke(relatedBean));
      }
      catch (Exception e) {
        e.printStackTrace();
        fail("related getter threw exception: " + propFullName);
      }
    }
  }

  private void testBidirectionalityOfManySideOfRelationship(
    AbstractEntity bean,
    BeanInfo beanInfo,
    PropertyDescriptor propertyDescriptor,
    Method getter)
  {
    // get basic objects related to the bean
    Class<? extends AbstractEntity> beanClass = bean.getClass();
    String propertyName = propertyDescriptor.getName();
    String propFullName = beanClass.getSimpleName() + "." + propertyName;
    
    // get the add method for the property
    String singularPropName =
      oddPluralToSingularPropertiesMap.containsKey(propertyName) ?
      oddPluralToSingularPropertiesMap.get(propertyName) :
      propertyName.substring(0, propertyName.length() - 1);
    String capitalizedSingularPropName =
      singularPropName.substring(0, 1).toUpperCase() +
      singularPropName.substring(1);
    String addMethodName = "add" + capitalizedSingularPropName;
    Method addMethod = findAndCheckMethod(beanClass, addMethodName, true);    
    
    // make sure this is actually a relationship!
    Class relatedBeanClass = addMethod.getParameterTypes()[0];
    if (! AbstractEntity.class.isAssignableFrom(relatedBeanClass)) {
      return;
    }
    
    // get basic objects for the other side of the reln
    Object relatedBean = getTestValueForType(relatedBeanClass);
    String relatedBeanClassName = relatedBeanClass.getSimpleName();
    BeanInfo relatedBeanInfo = null;
    try {
      relatedBeanInfo = Introspector.getBeanInfo(relatedBeanClass);
    }
    catch (IntrospectionException e) {
      e.printStackTrace();
      fail("failed to introspect entity class: " + relatedBeanClass);
    }
    
    // get the property name for the other side of the reln
    String relatedPropertyName;
    if (oddPropertyToRelatedPropertyMap.containsKey(propertyName)) {
      relatedPropertyName =
        oddPropertyToRelatedPropertyMap.get(propertyName);
    }
    else {
      relatedPropertyName = beanClass.getSimpleName();
      relatedPropertyName =
        relatedPropertyName.substring(0, 1).toLowerCase() +
        relatedPropertyName.substring(1);
    }
    String relatedPluralPropertyName;
    if (oddPluralPropertyToRelatedPropertyMap.containsKey(propertyName)) {
      relatedPluralPropertyName =
        oddPluralPropertyToRelatedPropertyMap.get(propertyName);
    }
    else {
      relatedPluralPropertyName =
        oddSingularToPluralPropertiesMap.containsKey(relatedPropertyName) ?
          oddSingularToPluralPropertiesMap.get(relatedPropertyName) :
            relatedPropertyName + "s";
    }
    
    // get the prop descr for the other side, and determine whether the
    // other side is one or many
    PropertyDescriptor relatedPropertyDescriptor = null;
    boolean otherSideIsMany = false;
    for (PropertyDescriptor descriptor : relatedBeanInfo.getPropertyDescriptors()) {
      if (descriptor.getName().equals(relatedPropertyName)) {
        relatedPropertyDescriptor = descriptor;
        break;
      }
      if (descriptor.getName().equals(relatedPluralPropertyName)) {
        relatedPropertyDescriptor = descriptor;
        otherSideIsMany = true;
        break;
      }
    }
    assertNotNull(
      "related bean " + relatedBeanClassName + " has property with name " +
      relatedPropertyName + " or " + relatedPluralPropertyName + " for " +
      propFullName,
      relatedPropertyDescriptor);
    
    // invoke the adder on this side
    try {
      addMethod.invoke(bean, relatedBean);
    }
    catch (Exception e) {
      e.printStackTrace();
      fail("adder threw exception: " + propFullName);
    }
    
    Method relatedGetter = relatedPropertyDescriptor.getReadMethod();
    
    if (otherSideIsMany) {
      try {
        Collection result = (Collection) relatedGetter.invoke(relatedBean);
        assertEquals(
          "related.getter() returns set of size 1 for " + propFullName,
          1,
          result.size());
        assertSame(
          "related.getter() returns this after this.setter(related) for " +
          propFullName,
          bean,
          result.iterator().next());
      }
      catch (Exception e) {
        e.printStackTrace();
        fail("related getter threw exception: " + propFullName);
      }
    }
    else {
      try {
        assertSame(
          "related.getter() returns this after this.setter(related) for " +
          propFullName,
          bean,
          relatedGetter.invoke(relatedBean));
      }
      catch (Exception e) {
        e.printStackTrace();
        fail("related getter threw exception: " + propFullName);
      }
    }
  }
}
