// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/test/edu/harvard/med/screensaver/model/EntityBeansTest.java $
// $Id: EntityBeansTest.java 223 2006-06-21 21:56:57Z js163 $
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

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.SchemaUtil;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;

import org.apache.log4j.Logger;

/**
 * Tests for persisting the entities.
 * <p>
 * These tests are mostly just copies of tests that exist in {@link
 * EntityBeansTest}, but with persistence added between the set and get.
 */
public class EntityBeansPersistenceTest extends EntityBeansExercizor
{
  private static Logger log = Logger.getLogger(EntityBeansPersistenceTest.class);
    
  /**
   * Bean property, for database access via Spring and Hibernate.
   */
  protected DAO dao;

  /**
   * For schema-related test setup tasks.
   */
  protected SchemaUtil schemaUtil;

  @Override
  protected void onSetUp() throws Exception
  {
    schemaUtil.recreateSchema();
  }
  
  private Integer _beanId;
  private void setBeanId(Integer beanId)
  {
    _beanId = beanId;
  }
  private Integer getBeanId()
  {
    return _beanId;
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
          final AbstractEntity bean,
          BeanInfo beanInfo,
          final PropertyDescriptor propertyDescriptor)
        {
          dao.doInTransaction(new DAOTransaction()
            {
              public void runTransaction()
              {
                if (dao.getEntityId(bean) == null) {
                  dao.persistEntity(bean);
                  setBeanId(dao.getEntityId(bean));
                }
              }
            });
          final Class<? extends AbstractEntity> beanClass = bean.getClass();
          dao.doInTransaction(new DAOTransaction()
            {
              public void runTransaction()
              {
                AbstractEntity bean =
                  dao.findEntityById(beanClass, getBeanId());

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
          final AbstractEntity bean,
          BeanInfo beanInfo,
          final PropertyDescriptor propertyDescriptor)
        {
          // HACK: kludging endsWith("Id") <=> is hibernate id. should really
          // make sure this is the hibernate id
          if (propertyDescriptor.getName().endsWith("Id")) {
            return;
          }
          
          final Method getter = propertyDescriptor.getReadMethod();
          if (Collection.class.isAssignableFrom(getter.getReturnType())) {
            return;
          }
          final Method setter = propertyDescriptor.getWriteMethod();
          final Object testValue = getTestValueForType(getter.getReturnType());

          if (bean.getClass().equals(Well.class)) {
            log.info("PROPS = Well." + propertyDescriptor.getName());
          }
          
          // transaction to call the setter
          dao.doInTransaction(new DAOTransaction()
            {
              public void runTransaction()
              {
                AbstractEntity localBean = bean;
                if (dao.getEntityId(localBean) != null) {
                  log.info("findE = " + localBean.getClass() + ":" + getBeanId());
                  localBean = dao.findEntityById(
                    localBean.getClass(),
                    getBeanId());
                }
                else {
                  dao.persistEntity(localBean);
                  setBeanId(dao.getEntityId(localBean));
                  log.info("setBeanId = " + localBean.getClass() + ":" + getBeanId());
                }
                try {
                  setter.invoke(localBean, testValue);
                }
                catch (Exception e) {
                  e.printStackTrace();
                  fail(
                    "setter threw exception: " +
                    bean.getClass() + "." + propertyDescriptor.getName());
                }
              }
            });
          
          // transaction to call the getter
          dao.doInTransaction(new DAOTransaction()
            {
              public void runTransaction()
              {
                log.info("findE = " + bean.getClass() + ":" + getBeanId());
                AbstractEntity localBean = dao.findEntityById(
                  bean.getClass(),
                  getBeanId());
                log.info("LOCAL = " + localBean);
                try {
                  if (bean.getClass().equals(ResultValue.class)) {
                    log.info("result value");
                  }
                  Object getterResult = getter.invoke(localBean); 
                  assertEquals(
                    "getter returns what setter set for " +
                    bean.getClass().getSimpleName() + "." +
                    propertyDescriptor.getName(),
                    testValue,
                    getterResult);
                }
                catch (Exception e) {
                  e.printStackTrace();
                  fail(
                    "getter threw exception: " +
                    bean.getClass() + "." + propertyDescriptor.getName());
                }
              }
            });
        }
      });
  }
  
//  /**
//   * Test collection properties:
//   * <ul>
//   * <li>have a pluralized name
//   * <li>do not have a (public) setter
//   * <li>have boolean add/remove methods with param of right type
//   * <li>add;get returns set of one
//   * <li>add;remove;get returns empty set
//   * </ul>
//   */
//  public void testCollectionProperties()
//  {
//    exercizePropertyDescriptors(new PropertyDescriptorExercizor()
//      {
//        public void exercizePropertyDescriptor(
//          AbstractEntity bean,
//          BeanInfo beanInfo,
//          PropertyDescriptor propertyDescriptor)
//        {
//          Method getter = propertyDescriptor.getReadMethod();
//          if (Collection.class.isAssignableFrom(getter.getReturnType())) {
//            testCollectionProperty(bean, getter, propertyDescriptor);
//          }
//        }
//      });
//  }
//
//  private static Map<String, String> oddPluralToSingularPropertiesMap =
//    new HashMap<String, String>();
//  static {
//    oddPluralToSingularPropertiesMap.put("children", "child");
//    oddPluralToSingularPropertiesMap.put("typesDerivedFrom", "typeDerivedFrom");
//  }
//  private static Map<String, String> oddSingularToPluralPropertiesMap =
//    new HashMap<String, String>();
//  static {
//    oddSingularToPluralPropertiesMap.put("child", "children");
//    oddSingularToPluralPropertiesMap.put("typeDerivedFrom", "typesDerivedFrom");
//  }
//  
//  /**
//   * Test collection property:
//   * <ul>
//   * <li>has a pluralized name
//   * <li>does not have a (public) setter
//   * <li>has boolean add/remove methods with param of right type
//   * <li>add;get returns set of one
//   * <li>add;remove;get returns empty set
//   */
//  @SuppressWarnings("unchecked")
//  private void testCollectionProperty(
//    AbstractEntity bean,
//    Method getter,
//    PropertyDescriptor propertyDescriptor)
//  {
//    Class<? extends AbstractEntity> beanClass = bean.getClass();
//    String beanClassName = beanClass.getSimpleName();
//    String propertyName = propertyDescriptor.getName();
//    String fullPropName = beanClassName + "." + propertyName;
//    
//    // collection property has pluralized name
//    assertTrue(
//      "collection property getter has plural name: " + fullPropName,
//      oddPluralToSingularPropertiesMap.containsKey(propertyName) ||
//      propertyName.endsWith("s"));
//
//    String singularPropName =
//      oddPluralToSingularPropertiesMap.containsKey(propertyName) ?
//      oddPluralToSingularPropertiesMap.get(propertyName) :
//      propertyName.substring(0, propertyName.length() - 1);
//    String capitalizedSingularPropName =
//      singularPropName.substring(0, 1).toUpperCase() +
//      singularPropName.substring(1);
//    
//    // collection property has no getter
//    assertNull(
//      "collection property has no setter: " + fullPropName,
//      propertyDescriptor.getWriteMethod());
//
//    // has boolean add methods with param of right type
//    String addMethodName = "add" + capitalizedSingularPropName;
//    Method addMethod = findAndCheckMethod(beanClass, addMethodName);
//      
//    // has boolean remove methods with param of right type
//    String removeMethodName = "remove" + capitalizedSingularPropName;
//    Method removeMethod = findAndCheckMethod(beanClass, removeMethodName);
//    
//    Method getterMethod = propertyDescriptor.getReadMethod();
//    
//    Class propertyType = addMethod.getParameterTypes()[0];
//    Object testValue = getTestValueForType(propertyType);
//    
//    // add;get returns set of one
//    try {
//      Boolean result = (Boolean) addMethod.invoke(bean, testValue);
//      assertTrue(
//        "adding to empty collection prop returns true: " + fullPropName,
//        result.booleanValue());
//    }
//    catch (Exception e) {
//      e.printStackTrace();
//      fail("add method for prop threw exception: " + fullPropName);
//    }
//    
//    try {
//      Collection result = (Collection) getterMethod.invoke(bean);
//      assertEquals(
//        "collection prop with one element added has size one: " + fullPropName,
//        result.size(),
//        1);
//    }
//    catch (Exception e) {
//      e.printStackTrace();
//      fail("getter method for prop threw exception: " + fullPropName);
//    }
//    
//    // add;remove;get returns empty set
//    try {
//      Boolean result = (Boolean)
//        removeMethod.invoke(bean, testValue);
//      assertTrue(
//        "removing to empty collection prop returns true: " + fullPropName,
//        result.booleanValue());
//    }
//    catch (Exception e) {
//      e.printStackTrace();
//      fail("remove method for prop threw exception: " + fullPropName);
//    }
//    
//    try {
//      Collection result = (Collection) getterMethod.invoke(bean);
//      assertEquals(
//        "collection prop with element removed has size zero: " + fullPropName,
//        result.size(),
//        0);
//    }
//    catch (Exception e) {
//      e.printStackTrace();
//      fail("getter method for prop threw exception: " + fullPropName);
//    }
//  }
//
//  private Method findAndCheckMethod(
//    Class<? extends AbstractEntity> beanClass,
//    String methodName)
//  {
//    String fullMethodName = beanClass.getName() + "." + methodName;
//    Method foundMethod = null;
//    for (Method method : beanClass.getDeclaredMethods()) {
//      if (method.getName().equals(methodName)) {
//        foundMethod = method;
//        break;
//      }
//    }
//    assertNotNull(
//      "collection property missing method: " + fullMethodName,
//      foundMethod);
//    assertEquals(
//      "collection property method returns boolean: " + fullMethodName,
//      foundMethod.getReturnType(),
//      Boolean.TYPE);
//    return foundMethod;
//  }
//  
//  public void testRelationshipBidirectionality()
//  {
//    exercizePropertyDescriptors(new PropertyDescriptorExercizor()
//      {
//        public void exercizePropertyDescriptor(
//          AbstractEntity bean,
//          BeanInfo beanInfo,
//          PropertyDescriptor propertyDescriptor)
//        {
//          Method getter = propertyDescriptor.getReadMethod();
//          if (AbstractEntity.class.isAssignableFrom(getter.getReturnType())) {
//            testBidirectionalityOfOneSideOfRelationship(
//              bean,
//              beanInfo,
//              propertyDescriptor,
//              getter);
//          }
//          else if (Collection.class.isAssignableFrom(getter.getReturnType())) {
//            testBidirectionalityOfManySideOfRelationship(
//              bean,
//              beanInfo,
//              propertyDescriptor,
//              getter);
//          }
//        }
//      });
//  }
//  
//  private void testBidirectionalityOfOneSideOfRelationship(
//    AbstractEntity bean,
//    BeanInfo beanInfo,
//    PropertyDescriptor propertyDescriptor,
//    Method getter)
//  {
//    String propFullName = bean.getClass() + "." + propertyDescriptor.getName();
//    
//    // get basic objects for the other side of the reln
//    Class relatedBeanClass = getter.getReturnType();
//    Object relatedBean = getTestValueForType(relatedBeanClass);
//    String relatedBeanClassName = relatedBeanClass.getSimpleName();
//    BeanInfo relatedBeanInfo = null;
//    try {
//      relatedBeanInfo = Introspector.getBeanInfo(relatedBeanClass);
//    }
//    catch (IntrospectionException e) {
//      e.printStackTrace();
//      fail("failed to introspect entity class: " + relatedBeanClass);
//    }
//    
//    // get the property name for the other side of the reln
//    String relatedPropertyName = bean.getClass().getSimpleName();
//    relatedPropertyName =
//      relatedPropertyName.substring(0, 1).toLowerCase() +
//      relatedPropertyName.substring(1);
//    String relatedPluralPropertyName =
//        oddSingularToPluralPropertiesMap.containsKey(relatedPropertyName) ?
//        oddSingularToPluralPropertiesMap.get(relatedPropertyName) :
//        relatedPropertyName + "s";
//        
//    // get the prop descr for the other side, and determine whether the
//    // other side is one or many
//    PropertyDescriptor relatedPropertyDescriptor = null;
//    boolean otherSideIsMany = false;
//    for (PropertyDescriptor descriptor : relatedBeanInfo.getPropertyDescriptors()) {
//      if (descriptor.getName().equals(relatedPropertyName)) {
//        relatedPropertyDescriptor = descriptor;
//        break;
//      }
//      if (descriptor.getName().equals(relatedPluralPropertyName)) {
//        relatedPropertyDescriptor = descriptor;
//        otherSideIsMany = true;
//        break;
//      }
//    }
//    assertNotNull(
//      "related bean " + relatedBeanClassName + " has property with name " +
//      relatedPropertyName + " or " + relatedPluralPropertyName,
//      relatedPropertyDescriptor);
//    
//    // invoke the setter on this side
//    Method setter = propertyDescriptor.getWriteMethod();
//    try {
//      setter.invoke(bean, relatedBean);
//    }
//    catch (Exception e) {
//      e.printStackTrace();
//      fail("setter threw exception: " + propFullName);
//    }
//    
//    Method relatedGetter = relatedPropertyDescriptor.getReadMethod();
//    
//    if (otherSideIsMany) {
//      try {
//        Collection result = (Collection) relatedGetter.invoke(relatedBean);
//        assertEquals(
//          "related.getter() returns set of size 1 for " + propFullName,
//          1,
//          result.size());
//        assertSame(
//          "related.getter() returns this after this.setter(related) for " +
//          propFullName,
//          bean,
//          result.iterator().next());
//      }
//      catch (Exception e) {
//        e.printStackTrace();
//        fail("related getter threw exception: " + propFullName);
//      }
//    }
//    else {
//      try {
//        assertSame(
//          "related.getter() returns this after this.setter(related) for " +
//          propFullName,
//          bean,
//          relatedGetter.invoke(relatedBean));
//      }
//      catch (Exception e) {
//        e.printStackTrace();
//        fail("related getter threw exception: " + propFullName);
//      }
//    }
//  }
//
//  private static Map<String, String> oddPropertyToRelatedPropertyMap =
//    new HashMap<String, String>();
//  private static Map<String, String> oddPluralPropertyToRelatedPropertyMap =
//    new HashMap<String, String>();
//  static {
//    oddPluralPropertyToRelatedPropertyMap.put("derivedTypes", "typesDerivedFrom");
//    oddPluralPropertyToRelatedPropertyMap.put("typesDerivedFrom", "derivedTypes");
//  }
//  
//  private void testBidirectionalityOfManySideOfRelationship(
//    AbstractEntity bean,
//    BeanInfo beanInfo,
//    PropertyDescriptor propertyDescriptor,
//    Method getter)
//  {
//    // get basic objects related to the bean
//    Class<? extends AbstractEntity> beanClass = bean.getClass();
//    String propertyName = propertyDescriptor.getName();
//    String propFullName = beanClass.getSimpleName() + "." + propertyName;
//    
//    // get the add method for the property
//    String singularPropName =
//      oddPluralToSingularPropertiesMap.containsKey(propertyName) ?
//      oddPluralToSingularPropertiesMap.get(propertyName) :
//      propertyName.substring(0, propertyName.length() - 1);
//    String capitalizedSingularPropName =
//      singularPropName.substring(0, 1).toUpperCase() +
//      singularPropName.substring(1);
//    String addMethodName = "add" + capitalizedSingularPropName;
//    Method addMethod = findAndCheckMethod(beanClass, addMethodName);    
//    
//    // make sure this is actually a relationship!
//    Class relatedBeanClass = addMethod.getParameterTypes()[0];
//    if (! AbstractEntity.class.isAssignableFrom(relatedBeanClass)) {
//      return;
//    }
//    
//    // get basic objects for the other side of the reln
//    Object relatedBean = getTestValueForType(relatedBeanClass);
//    String relatedBeanClassName = relatedBeanClass.getSimpleName();
//    BeanInfo relatedBeanInfo = null;
//    try {
//      relatedBeanInfo = Introspector.getBeanInfo(relatedBeanClass);
//    }
//    catch (IntrospectionException e) {
//      e.printStackTrace();
//      fail("failed to introspect entity class: " + relatedBeanClass);
//    }
//    
//    // get the property name for the other side of the reln
//    String relatedPropertyName;
//    if (oddPropertyToRelatedPropertyMap.containsKey(propertyName)) {
//      relatedPropertyName =
//        oddPropertyToRelatedPropertyMap.get(propertyName);
//    }
//    else {
//      relatedPropertyName = beanClass.getSimpleName();
//      relatedPropertyName =
//        relatedPropertyName.substring(0, 1).toLowerCase() +
//        relatedPropertyName.substring(1);
//    }
//    String relatedPluralPropertyName;
//    if (oddPluralPropertyToRelatedPropertyMap.containsKey(propertyName)) {
//      relatedPluralPropertyName =
//        oddPluralPropertyToRelatedPropertyMap.get(propertyName);
//    }
//    else {
//      relatedPluralPropertyName =
//        oddSingularToPluralPropertiesMap.containsKey(relatedPropertyName) ?
//          oddSingularToPluralPropertiesMap.get(relatedPropertyName) :
//            relatedPropertyName + "s";
//    }
//    
//    // get the prop descr for the other side, and determine whether the
//    // other side is one or many
//    PropertyDescriptor relatedPropertyDescriptor = null;
//    boolean otherSideIsMany = false;
//    for (PropertyDescriptor descriptor : relatedBeanInfo.getPropertyDescriptors()) {
//      if (descriptor.getName().equals(relatedPropertyName)) {
//        relatedPropertyDescriptor = descriptor;
//        break;
//      }
//      if (descriptor.getName().equals(relatedPluralPropertyName)) {
//        relatedPropertyDescriptor = descriptor;
//        otherSideIsMany = true;
//        break;
//      }
//    }
//    assertNotNull(
//      "related bean " + relatedBeanClassName + " has property with name " +
//      relatedPropertyName + " or " + relatedPluralPropertyName + " for " +
//      propFullName,
//      relatedPropertyDescriptor);
//    
//    // invoke the adder on this side
//    try {
//      addMethod.invoke(bean, relatedBean);
//    }
//    catch (Exception e) {
//      e.printStackTrace();
//      fail("adder threw exception: " + propFullName);
//    }
//    
//    Method relatedGetter = relatedPropertyDescriptor.getReadMethod();
//    
//    if (otherSideIsMany) {
//      try {
//        Collection result = (Collection) relatedGetter.invoke(relatedBean);
//        assertEquals(
//          "related.getter() returns set of size 1 for " + propFullName,
//          1,
//          result.size());
//        assertSame(
//          "related.getter() returns this after this.setter(related) for " +
//          propFullName,
//          bean,
//          result.iterator().next());
//      }
//      catch (Exception e) {
//        e.printStackTrace();
//        fail("related getter threw exception: " + propFullName);
//      }
//    }
//    else {
//      try {
//        assertSame(
//          "related.getter() returns this after this.setter(related) for " +
//          propFullName,
//          bean,
//          relatedGetter.invoke(relatedBean));
//      }
//      catch (Exception e) {
//        e.printStackTrace();
//        fail("related getter threw exception: " + propFullName);
//      }
//    }
//  }
}
