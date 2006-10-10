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
import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.screens.NonCherryPickVisit;
import edu.harvard.med.screensaver.model.screens.Visit;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

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
          
          if (! isHibernateIdProperty(beanInfo, propertyDescriptor)) {
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
              "getter for uninitialized property returns collection of expected initial size (usually 0): " +
              bean.getClass() + "." + getter.getName(),
              getExpectedInitialCollectionSize(beanInfo.getBeanDescriptor().getName(), 
                                               propertyDescriptor),
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
          if (isHibernateIdProperty(beanInfo, propertyDescriptor)) {
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
        "collection prop with one element added has size one greater than initial size: " + fullPropName,
        getExpectedInitialCollectionSize(bean.getClass().getSimpleName(), propertyDescriptor) + 1,
        result.size());
      boolean isContained1 = false;
      boolean isContained2 = false;
      boolean isContained3 = false;

      // TODO: isContained1 is false while others are true! At least for DerivativeScreenResult. Something to do with hashCode probably, since collection is a Set...
      isContained1 = result.contains(testValue);
      for (Object x : result) {
        if (x.equals(testValue)) {
          isContained2 = true;
        }
      }
      isContained3 = new ArrayList(result).contains(testValue);
      assertTrue(
        "collection prop with one element added has that element: " + fullPropName,
        isContained1 || isContained2 || isContained3);
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
        "removing previously added element returns true: " + fullPropName,
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
        "collection prop with element removed has original size: " + fullPropName,
        result.size(),
        getExpectedInitialCollectionSize(bean.getClass().getSimpleName(), propertyDescriptor));
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
          // note: if a property is declared in a superclass, we won't test that
          // property from any subclasses that inherits it; we could, but then
          // we'll need to make our reflection code that finds methods consider
          // inherited methods as well, and then be smart when inferring the
          // expected names of these methods, as the method name will be based
          // upon the superclass name, not the subclass name.
          // For example, consider AdministratorUser, which has a bidirectional
          // relationship with ScreenSaverUserRole, but only via it's superclass
          // ScreensaverUser. In this case, we only want to require that
          // ScreensaverUserRole has a bidir relationship with ScreensaverUser,
          // NOT also with AdministratorUser (although it does via its parent).
          if (getter.getDeclaringClass().equals(bean.getClass())) {
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
        }
      });
  }
  
  private void testBidirectionalityOfOneSideOfRelationship(
    AbstractEntity bean,
    BeanInfo beanInfo,
    PropertyDescriptor propertyDescriptor,
    Method getter)
  {
    // do not test bidirectionality of relationships that are explicitly annotated as unidirectional
    if (isUnidirectionalRelationship(beanInfo, propertyDescriptor)) {
      return;
    }
    
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
    
    String propertyName = propertyDescriptor.getName();
    
    // get the property name for the other side of the reln
    String relatedPropertyName;
    if (oddPropertyToRelatedPropertyMap.containsKey(propertyName)) {
      relatedPropertyName =
        oddPropertyToRelatedPropertyMap.get(propertyName);
    }
    else {
      relatedPropertyName = bean.getClass().getSimpleName();
      relatedPropertyName =
        relatedPropertyName.substring(0, 1).toLowerCase() +
        relatedPropertyName.substring(1);
    }
    String relatedPluralPropertyName;
    if (oddPropertyToRelatedPluralPropertyMap.containsKey(propertyName)) {
      relatedPluralPropertyName =
        oddPropertyToRelatedPluralPropertyMap.get(propertyName);
    }
    else {
      relatedPluralPropertyName =
        oddSingularToPluralPropertiesMap.containsKey(relatedPropertyName) ?
          oddSingularToPluralPropertiesMap.get(relatedPropertyName) :
            relatedPropertyName + "s";
    } 
        

    // HACK: cant put "screen" into the odd maps since it is ubiquitous
    if (Visit.class.isAssignableFrom(bean.getClass()) &&
      propertyName.equals("screen")) {
      relatedPluralPropertyName = "visits";
    }
    
    // HACK: cant put "labHead" into the odd maps twice, buts it has
    // related screensHeaded + labMembers
    if (bean.getClass().equals(ScreeningRoomUser.class) &&
      propertyName.equals("labHead")) {
      relatedPluralPropertyName = "labMembers";
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
    
    // HACK - difficulty because singular and plural property names
    // are the same
    if (relatedBeanClass.equals(NonCherryPickVisit.class) &&
        (relatedPropertyName.equals("platesUsed") ||
         relatedPropertyName.equals("equipmentUsed"))) {
      otherSideIsMany = true;
    }
    
    assertNotNull(
      "related bean " + relatedBeanClassName + " has property with name " +
      relatedPropertyName + " or " + relatedPluralPropertyName + " for " +
      propFullName,
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
    // do not test bidirectionality of relationships that are explicitly annotated as unidirectional
    if (isUnidirectionalRelationship(beanInfo, propertyDescriptor)) {
      return;
    }
    
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
    if (oddPropertyToRelatedPluralPropertyMap.containsKey(propertyName)) {
      relatedPluralPropertyName =
        oddPropertyToRelatedPluralPropertyMap.get(propertyName);
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
