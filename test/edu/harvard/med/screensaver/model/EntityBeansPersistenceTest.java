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

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.SchemaUtil;

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
                if (bean.getEntityId() == null) {
                  dao.persistEntity(bean);
                }
              }
            });
          final Class<? extends AbstractEntity> beanClass = bean.getClass();
          log.info("bean class = " + beanClass.getSimpleName());
          dao.doInTransaction(new DAOTransaction()
            {
              public void runTransaction()
              {
                AbstractEntity localBean =
                  dao.findEntityById(beanClass, bean.getEntityId());

                Method getter = propertyDescriptor.getReadMethod();
                // TODO: check if the getter returns a collection before
                // invoking it
                Object result = null;
                try {
                  // note that result will be Boolean when the getter returns boolean
                  result = getter.invoke(localBean);
                }
                catch (Exception e) {
                  e.printStackTrace();
                  fail(
                    "getter for collection property threw exception: " +
                    localBean.getClass() + "." + getter.getName() + ": " + e);
                }
                if (result instanceof Collection) {
                  assertEquals(
                    "getter for uninitialized property returns empty collection: " +
                    localBean.getClass() + "." + getter.getName(),
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

          // transaction to call the setter
          dao.doInTransaction(new DAOTransaction()
            {
              public void runTransaction()
              {
                AbstractEntity localBean = bean;

                // if the bean has already been persisted, then get the persisted copy, as the current
                // copy is stale. if it has not, persist it now so we can get the entityId
                if (localBean.getEntityId() != null) {
                  localBean = dao.findEntityById(
                    localBean.getClass(),
                    localBean.getEntityId());
                }
                else {
                  dao.persistEntity(localBean);
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
                AbstractEntity localBean = dao.findEntityById(
                  bean.getClass(),
                  bean.getEntityId());

                try {
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
    final AbstractEntity bean,
    Method getter,
    PropertyDescriptor propertyDescriptor)
  {
    final Class<? extends AbstractEntity> beanClass = bean.getClass();
    String beanClassName = beanClass.getSimpleName();
    String propertyName = propertyDescriptor.getName();
    final String fullPropName = beanClassName + "." + propertyName;
    
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
    final Method addMethod = findAndCheckMethod(beanClass, addMethodName, true);
      
    // has boolean remove methods with param of right type
    String removeMethodName = "remove" + capitalizedSingularPropName;
    final Method removeMethod = findAndCheckMethod(beanClass, removeMethodName, false);
    
    final Method getterMethod = propertyDescriptor.getReadMethod();
    
    Class propertyType = addMethod.getParameterTypes()[0];
    final Object testValue = getTestValueForType(propertyType);
    
    // add the testValue in a transaction
    dao.doInTransaction(
      new DAOTransaction()
      {
        public void runTransaction()
        {
          AbstractEntity localBean = bean;
          
          // if the bean has already been persisted, then get the persisted copy, as
          // the current copy is stale. if it has not, persist it now so we can get
          // the entityId
          if (localBean.getEntityId() != null) {
            localBean = dao.findEntityById(beanClass, localBean.getEntityId());
          }
          else {
            dao.persistEntity(localBean);
          }
          
          try {
            Boolean result = (Boolean) addMethod.invoke(localBean, testValue);
            assertTrue(
              "adding to empty collection prop returns true: " + fullPropName,
              result.booleanValue());
          }
          catch (Exception e) {
            e.printStackTrace();
            fail("add method for prop threw exception: " + fullPropName);
          }
        }
      });
    
    // transaction to call the getter
    dao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          AbstractEntity localBean =
            dao.findEntityById(beanClass, bean.getEntityId());
          
          try {
            Collection result = (Collection) getterMethod.invoke(localBean);
            assertEquals(
              "collection prop with one element added has size one: " + fullPropName,
              1,
              result.size());
            assertEquals(
              "collection prop with one element added has that element: " + fullPropName,
              testValue,
              result.iterator().next());
          }
          catch (Exception e) {
            e.printStackTrace();
            fail("getter method for prop threw exception: " + fullPropName);
          }
        }
      });
    
    if (removeMethod == null) {
      return;
    }
    
    // transaction to remove the testValue
    dao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          Object localTestValue = testValue;
          try {
            // GOOD IDEA: don't try to use the same entity across transactions!
            localTestValue = dao.findEntityById(
              (Class<? extends AbstractEntity>) testValue.getClass(),
              ((AbstractEntity) testValue).getEntityId());
          }
          catch (ClassCastException e) {
            // don't worry - it's just that the test value isn't an entity
          }
          
          AbstractEntity localBean =
            dao.findEntityById(beanClass, bean.getEntityId());
          
          try {
            Boolean result = (Boolean) removeMethod.invoke(localBean, localTestValue);
            assertTrue(
              "removing to empty collection prop returns true: " + fullPropName,
              result.booleanValue());
          }
          catch (Exception e) {
            e.printStackTrace();
            fail("remove method for prop threw exception: " + fullPropName);
          }
        }
      });
    
    // transaction to invoke the getter
    dao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          AbstractEntity localBean =
            dao.findEntityById(beanClass, bean.getEntityId());
          
          try {
            Collection result = (Collection) getterMethod.invoke(localBean);
            assertEquals(
              "collection prop with element removed has size zero: " + fullPropName,
              0,
              result.size());
          }
          catch (Exception e) {
            e.printStackTrace();
            fail("getter method for prop threw exception: " + fullPropName);
          }
        }
      });
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
  
  @SuppressWarnings("unchecked")
  private void testBidirectionalityOfOneSideOfRelationship(
    final AbstractEntity bean,
    BeanInfo beanInfo,
    PropertyDescriptor propertyDescriptor,
    Method getter)
  {
    final String propFullName = bean.getClass() + "." + propertyDescriptor.getName();
    final Class<? extends AbstractEntity> beanClass = bean.getClass();
    
    // get basic objects for the other side of the reln
    final Class<? extends AbstractEntity> relatedBeanClass =
      (Class<? extends AbstractEntity>) getter.getReturnType();
    final AbstractEntity relatedBean = (AbstractEntity)
      getTestValueForType(relatedBeanClass);
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
    String relatedPropertyName = beanClass.getSimpleName();
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
    boolean otherSideIsManyPre = false;
    for (PropertyDescriptor descriptor : relatedBeanInfo.getPropertyDescriptors()) {
      if (descriptor.getName().equals(relatedPropertyName)) {
        relatedPropertyDescriptor = descriptor;
        break;
      }
      if (descriptor.getName().equals(relatedPluralPropertyName)) {
        relatedPropertyDescriptor = descriptor;
        otherSideIsManyPre = true;
        break;
      }
    }
    final boolean otherSideIsMany = otherSideIsManyPre;
    assertNotNull(
      "related bean " + relatedBeanClassName + " has property with name " +
      relatedPropertyName + " or " + relatedPluralPropertyName,
      relatedPropertyDescriptor);
    
    // invoke the setter on this side
    final Method setter = propertyDescriptor.getWriteMethod();
    dao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          AbstractEntity localBean = bean;
          
          // if the bean has already been persisted, then get the persisted copy, as
          // the current copy is stale. if it has not, persist it now so we can get
          // the entityId
          if (localBean.getEntityId() != null) {
            localBean = dao.findEntityById(beanClass, bean.getEntityId());
          }
          
          try {
            setter.invoke(localBean, relatedBean);
          }
          catch (Exception e) {
            e.printStackTrace();
            fail("setter threw exception: " + propFullName);
          }
          dao.persistEntity(localBean);         
        }
      });
    
    final Method relatedGetter = relatedPropertyDescriptor.getReadMethod();
    
    // invoke the getter on the other (related) side
    dao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          AbstractEntity localBean =
            dao.findEntityById(beanClass, bean.getEntityId());
          AbstractEntity localRelatedBean =
            dao.findEntityById(relatedBeanClass, relatedBean.getEntityId());
          
          if (otherSideIsMany) {
            try {
              Collection result = (Collection)
                relatedGetter.invoke(localRelatedBean);
              assertEquals(
                "related.getter() returns set of size 1 for " + propFullName,
                1,
                result.size());
              assertSame(
                "related.getter() returns this after this.setter(related) for " +
                propFullName,
                localBean,
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
                localBean,
                relatedGetter.invoke(relatedBean));
            }
            catch (Exception e) {
              e.printStackTrace();
              fail("related getter threw exception: " + propFullName);
            }
          }
        }
      });
  }
  
  private void testBidirectionalityOfManySideOfRelationship(
    final AbstractEntity bean,
    BeanInfo beanInfo,
    PropertyDescriptor propertyDescriptor,
    Method getter)
  {
    // get basic objects related to the bean
    final Class<? extends AbstractEntity> beanClass = bean.getClass();
    String propertyName = propertyDescriptor.getName();
    final String propFullName = beanClass.getSimpleName() + "." + propertyName;
    
    // get the add method for the property
    String singularPropName =
      oddPluralToSingularPropertiesMap.containsKey(propertyName) ?
      oddPluralToSingularPropertiesMap.get(propertyName) :
      propertyName.substring(0, propertyName.length() - 1);
    String capitalizedSingularPropName =
      singularPropName.substring(0, 1).toUpperCase() +
      singularPropName.substring(1);
    String addMethodName = "add" + capitalizedSingularPropName;
    final Method addMethod = findAndCheckMethod(beanClass, addMethodName, true);    
    
    // make sure this is actually a relationship!
    final Class relatedBeanClass = addMethod.getParameterTypes()[0];
    if (! AbstractEntity.class.isAssignableFrom(relatedBeanClass)) {
      return;
    }
    
    // get basic objects for the other side of the reln
    final AbstractEntity relatedBean = (AbstractEntity)
      getTestValueForType(relatedBeanClass);
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
    boolean otherSideIsManyPre = false;
    for (PropertyDescriptor descriptor : relatedBeanInfo.getPropertyDescriptors()) {
      if (descriptor.getName().equals(relatedPropertyName)) {
        relatedPropertyDescriptor = descriptor;
        break;
      }
      if (descriptor.getName().equals(relatedPluralPropertyName)) {
        relatedPropertyDescriptor = descriptor;
        otherSideIsManyPre = true;
        break;
      }
    }
    final boolean otherSideIsMany = otherSideIsManyPre;
    assertNotNull(
      "related bean " + relatedBeanClassName + " has property with name " +
      relatedPropertyName + " or " + relatedPluralPropertyName + " for " +
      propFullName,
      relatedPropertyDescriptor);
    
    // invoke the adder on this side
    dao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          AbstractEntity localBean = bean;
          
          // if the bean has already been persisted, then get the persisted copy, as
          // the current copy is stale. if it has not, persist it now so we can get
          // the entityId
          if (localBean.getEntityId() != null) {
            localBean = dao.findEntityById(beanClass, bean.getEntityId());
          }    
    
          try {
            addMethod.invoke(localBean, relatedBean);
          }
          catch (Exception e) {
            e.printStackTrace();
            fail("adder threw exception: " + propFullName);
          }
          dao.persistEntity(localBean);
        }
      });
    
    final Method relatedGetter = relatedPropertyDescriptor.getReadMethod();
    
    dao.doInTransaction(new DAOTransaction()
      {
        @SuppressWarnings("unchecked")
        public void runTransaction()
        {
          AbstractEntity localBean =
            dao.findEntityById(beanClass, bean.getEntityId());
          AbstractEntity localRelatedBean =
            dao.findEntityById(relatedBeanClass, relatedBean.getEntityId());
          
          if (otherSideIsMany) {
            try {
              Collection result = (Collection) relatedGetter.invoke(localRelatedBean);
              assertEquals(
                "related.getter() returns set of size 1 for " + propFullName,
                1,
                result.size());
              assertSame(
                "related.getter() returns this after this.setter(related) for " +
                propFullName,
                localBean,
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
                localBean,
                relatedGetter.invoke(localRelatedBean));
            }
            catch (Exception e) {
              e.printStackTrace();
              fail("related getter threw exception: " + propFullName);
            }
          }
        }
      });
  }
}
