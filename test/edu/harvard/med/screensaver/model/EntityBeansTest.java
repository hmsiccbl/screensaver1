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
import java.util.ArrayList;
import java.util.Collection;

import edu.harvard.med.screensaver.util.StringUtils;

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
          
          if (setterMethodNotExpected(bean.getClass(), propertyDescriptor)) {
            return;
          }
          
          if (! isEntityIdProperty(bean.getClass(), propertyDescriptor)) {
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
        final BeanInfo beanInfo,
        final PropertyDescriptor propertyDescriptor)
      {
        testBean(bean, new BeanTester() {
          public void testBean(AbstractEntity bean) 
          {
            Method getter = propertyDescriptor.getReadMethod();
            // TODO: check if the getter returns a collection before invoking it
            Object result = null;
            try {
              // note that result will be Boolean when the getter returns boolean
              result = getter.invoke(bean);
            }
            catch (Exception e) {
              e.printStackTrace();
              fail("getter for collection property threw exception: " +
                   bean.getClass() + "." + getter.getName() + ": " + e);
            }
            if (result instanceof Collection) {
              assertEquals("getter for uninitialized property returns collection of expected initial size (usually 0): " +
                           bean.getClass() + "." + getter.getName(),
                           getExpectedInitialCollectionSize(beanInfo.getBeanDescriptor().getName(), 
                                                            propertyDescriptor),
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
        AbstractEntity bean,
        BeanInfo beanInfo,
        final PropertyDescriptor propertyDescriptor)
      {
        if (setterMethodNotExpected(bean.getClass(), propertyDescriptor)) {
          return;
        }

        final Method getter = propertyDescriptor.getReadMethod();
        if (Collection.class.isAssignableFrom(getter.getReturnType())) {
          return;
        }
        final Method setter = propertyDescriptor.getWriteMethod();
        final Object testValue = getTestValueForType(getter.getReturnType());

        // call the setter
        testBean(bean, new BeanTester() 
        {
          public void testBean(AbstractEntity bean) 
          {
            try {
              setter.invoke(bean, testValue);
            }
            catch (Exception e) {
              e.printStackTrace();
              fail(
                   "setter threw exception: " +
                   bean.getClass() + "." + propertyDescriptor.getName());
            }
          }
        });

        // call the getter
        testBean(bean, new BeanTester() 
        {
          public void testBean(AbstractEntity bean) 
          {
            try {
              // this only makes sense for non-persistent tests; testing equality, rather than sameness, is probably good enough
//              if (AbstractEntity.class.isAssignableFrom(getter.getReturnType())) {
//                assertSame("getter returns what setter set for " +
//                           bean.getClass() + "." + propertyDescriptor.getName(),
//                           testValue,
//                           getter.invoke(bean));
//              }
//              else {
                assertEquals("getter returns what setter set for " +
                             bean.getClass() + "." + propertyDescriptor.getName(),
                             testValue,
                             getter.invoke(bean));
//              }
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
            doTestCollectionProperty(bean, getter, propertyDescriptor);
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
  protected void doTestCollectionProperty(
    AbstractEntity bean,
    Method getter,
    final PropertyDescriptor propertyDescriptor)
  {
    Class<? extends AbstractEntity> beanClass = bean.getClass();
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
    String capitalizedSingularPropName = StringUtils.capitalize(singularPropName);
    
    // collection property has no setter (should only have add and/or remove methods)
    assertNull(
      "collection property has no setter: " + fullPropName,
      propertyDescriptor.getWriteMethod());
    
    RelatedProperty relatedProperty = new RelatedProperty(beanClass, propertyDescriptor);
    // if related bean cannot exist independently, then
    // this side should *not* have an add or remove method, since the related
    // bean must be associated during instantation only.
    if (relatedProperty.exists() && relatedProperty.hasForeignKeyConstraint()) {
      // do nothing
      log.info("testCollectionProperty(): skipping add/remove test for collection " + fullPropName + 
               ": related property " + relatedProperty.getName() + 
               " has foreign key constraint, and cannot be added/removed from this bean");
    }
    else {
      // has boolean add methods with param of right type
      String addMethodName = "add" + capitalizedSingularPropName;
      final Method addMethod = findAndCheckMethod(beanClass, addMethodName, ExistenceRequirement.REQUIRED);
      
      // has boolean remove methods with param of right type
      String removeMethodName = "remove" + capitalizedSingularPropName;
      final Method removeMethod = findAndCheckMethod(beanClass, removeMethodName, ExistenceRequirement.OPTIONAL);
    
      final Method getterMethod = propertyDescriptor.getReadMethod();
      
      Class propertyType = addMethod.getParameterTypes()[0];
      final Object testValue = getTestValueForType(propertyType);
      
      // add the test value
      testBean(bean, new BeanTester()
      {
        public void testBean(AbstractEntity bean)
        {
          try {
            Boolean result = (Boolean) addMethod.invoke(bean, testValue);
            assertTrue("adding to empty collection prop returns true: " + fullPropName,
                       result.booleanValue());
          }
          catch (Exception e) {
            e.printStackTrace();
            fail("add method for prop threw exception: " + fullPropName);
          }
        }
      });

      
      // call the getter to test it was added
      testBean(bean, new BeanTester()
      {
        public void testBean(AbstractEntity bean)
        {
          try {
            Collection result = (Collection) getterMethod.invoke(bean);
            assertEquals("collection prop with one element added has size one greater than initial size: " + fullPropName,
                         getExpectedInitialCollectionSize(bean.getClass().getSimpleName(), propertyDescriptor) + 1,
                         result.size());
        
            // TODO: isContained1 is false while others are true! At least for DerivativeScreenResult. Something to do with hashCode probably, since collection is a Set...
            boolean isContained1 = false;
            boolean isContained2 = false;
            boolean isContained3 = false;
            isContained1 = result.contains(testValue);
            for (Object x : result) {
              if (x.equals(testValue)) {
                isContained2 = true;
              }
            }
            isContained3 = new ArrayList(result).contains(testValue);
            assertTrue("collection prop with one element added has that element: " + fullPropName,
                       isContained1 || isContained2 || isContained3);
          }
          catch (Exception e) {
            e.printStackTrace();
            fail("getter method for prop threw exception: " + fullPropName);
          }
        };
      });
    
      if (removeMethod == null) {
        return;
      }
      
      // remove the test value from the collection
      if (testValue instanceof AbstractEntity) {
        // treat the testValue as an AbstractEntity, rather than just an Object;
        // for persistence tests, this enables Hibernate to update the testValue 
        // entity during flush (if just an Object, this doesn't occur)
        testRelatedBeans(bean, (AbstractEntity) testValue, new RelatedBeansTester()
        {
          public void testBeanWithRelatedBean(AbstractEntity bean, 
                                              AbstractEntity testValue)
          {
            try {
              Boolean result = (Boolean) removeMethod.invoke(bean, testValue);
              assertTrue("removing previously added element returns true: " + fullPropName,
                         result.booleanValue());
            }
            catch (Exception e) {
              e.printStackTrace();
              fail("remove method for prop threw exception: " + fullPropName);
            }
          }
        });
      }
      else {
        testBean(bean, new BeanTester()
        {
          public void testBean(AbstractEntity bean)
          {
            try {
              Boolean result = (Boolean) removeMethod.invoke(bean, testValue);
              assertTrue("removing previously added element returns true: " + fullPropName,
                         result.booleanValue());
            }
            catch (Exception e) {
              e.printStackTrace();
              fail("remove method for prop threw exception: " + fullPropName);
            }
          }
        });
      }

      // call the getter to test it was removed
      testBean(bean, new BeanTester()
      {
        public void testBean(AbstractEntity bean)
        {
          try {
            Collection result = (Collection) getterMethod.invoke(bean);
            assertEquals("collection prop with element removed has original size: " + fullPropName,
                         result.size(),
                         getExpectedInitialCollectionSize(bean.getClass().getSimpleName(), propertyDescriptor));
          }
          catch (Exception e) {
            e.printStackTrace();
            fail("getter method for prop threw exception: " + fullPropName);
          }
        }
      });
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
              doTestBidirectionalityOfOneSideOfRelationship(bean,
                                                            beanInfo,
                                                            propertyDescriptor,
                                                            getter);
            }
            else if (Collection.class.isAssignableFrom(getter.getReturnType())) {
              doTestBidirectionalityOfManySideOfRelationship(bean,
                                                             beanInfo,
                                                             propertyDescriptor,
                                                             getter);
            }
          }
        }
      });
  }
  
  protected void doTestBidirectionalityOfOneSideOfRelationship(
    AbstractEntity bean,
    BeanInfo beanInfo,
    final PropertyDescriptor propertyDescriptor,
    Method getter)
  {
    final String propFullName = bean.getClass() + "." + propertyDescriptor.getName();
    
    // do not test bidirectionality of relationships that are explicitly annotated as unidirectional
    if (isUnidirectionalRelationship(bean.getClass(), propertyDescriptor)) {
      log.info("testBidirectionalityOfOneSideOfRelationship(): skipping " + 
               propFullName + ": unidirectional");
      return;
    }

    final RelatedProperty relatedProperty = new RelatedProperty(bean.getClass(), propertyDescriptor);

    assertNotNull("related bean " + relatedProperty.getBeanClass().getSimpleName() + 
                  " has property with name " +
                  relatedProperty.getExpectedName() + 
                  " or " + relatedProperty.getExpectedPluralName() + 
                  " for " + propFullName,
                  relatedProperty.getPropertyDescriptor());
    
    if (hasForeignKeyConstraint(propertyDescriptor)) {
      // no setter method on this side; this side's bean is associated in this bean's constructor (e.g. ScreenResult.screen)
      log.debug("testBidirectionalityOfOneSideOfRelationship(): " + propFullName + 
                " has foreign key constraint: assuming related bean is associated in this bean's constructor");
      final AbstractEntity[] relatedBeanHolder = new AbstractEntity[1];
      // assert this bean's getter method returns the related bean that was set via the constructor
      testBean(bean, new BeanTester()
      {
        public void testBean(AbstractEntity bean)
        {
          try {
            AbstractEntity relatedBean = (AbstractEntity) propertyDescriptor.getReadMethod().invoke(bean);
            assertNotNull("this.getter() returns non-null related bean of type " + 
                          relatedProperty.getBeanClass().getSimpleName(),
                          relatedBean);
            relatedBeanHolder[0] = relatedBean;
          }
          catch (Exception e) {
            e.printStackTrace();
            fail("this.getter() threw exception: " + propFullName);
          }
        }
      });

      // assert related bean property returns/contains this bean (depending upon otherSideIsMany)
      // TODO: the rest of this block duplicates the getter-testing code below
      testRelatedBeans(bean, relatedBeanHolder[0], new RelatedBeansTester()
      {
        public void testBeanWithRelatedBean(AbstractEntity bean,
                                            AbstractEntity relatedBean)
        {
          try {
            if (relatedProperty.otherSideIsMany()) {
              assertTrue("related.getter() contains this bean",
                         ((Collection) relatedProperty.invokeGetter(relatedBean)).contains(bean));
            }
            else {
              assertEquals("related getter, " + relatedProperty.getFullName() + ", returns this bean",
                           bean,
                           relatedProperty.invokeGetter(relatedBean));
            }
          }
          catch (Exception e) {
            e.printStackTrace();
            fail("related getter, " + relatedProperty.getFullName() + ", threw exception: " + propFullName);
          }

        }
      });
    }
    else if (relatedProperty.hasForeignKeyConstraint()) { 
      // no setter method on this side; this bean would be associated with related bean in related bean's constructor (e.g. Screen.screenResult)
      log.info("testBidirectionalityOfOneSideOfRelationship(): " + propFullName + 
      " has foreign key constraint on related side: nothing to test from this side");
    }
    else {
      final AbstractEntity relatedBean = (AbstractEntity) getTestValueForType(relatedProperty.getBeanClass());

      // invoke the setter on this side
      testBean(bean, new BeanTester()
      {
        public void testBean(AbstractEntity bean)
        {
          log.debug("testBidirectionalityOfOneSideOfRelationship(): " + propFullName + 
                    ": related bean will be added via setter on this bean");
          Method setter = propertyDescriptor.getWriteMethod();
          try {
            setter.invoke(bean, relatedBean);
          }
          catch (Exception e) {
            e.printStackTrace();
            fail("setter threw exception: " + propFullName);
          }
        }
      });

      // invoke the getter on the other (related) side
      testRelatedBeans(bean, relatedBean, new RelatedBeansTester()
      {
        public void testBeanWithRelatedBean(AbstractEntity bean,
                                            AbstractEntity relatedBean)
        {
          if (relatedProperty.otherSideIsMany()) {
            try {
              Collection result = (Collection) relatedProperty.invokeGetter(relatedBean);
              assertEquals("related getter, " + relatedProperty.getFullName() + ", returns set of size 1 for " + propFullName,
                           1,
                           result.size());
              assertSame("related getter, " + relatedProperty.getFullName() + ", returns this after this.setter(related) for " +
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
                         "related getter, " + relatedProperty.getFullName() + ", returns this after this.setter(related) for " +
                         propFullName,
                         bean,
                         relatedProperty.invokeGetter(relatedBean));
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

  protected void doTestBidirectionalityOfManySideOfRelationship(
    AbstractEntity bean,
    BeanInfo beanInfo,
    PropertyDescriptor propertyDescriptor,
    final Method getter)
  {
    // do not test bidirectionality of relationships that are explicitly annotated as unidirectional
    if (isUnidirectionalRelationship(bean.getClass(), propertyDescriptor)) {
      return;
    }

    final RelatedProperty relatedProperty = new RelatedProperty(bean.getClass(), propertyDescriptor);
    // make sure this is actually a relationship!
    if (!relatedProperty.exists()) {
      return;
    }

    // get basic objects related to the bean
    Class<? extends AbstractEntity> beanClass = bean.getClass();
    String propertyName = propertyDescriptor.getName();
    final String propFullName = beanClass.getSimpleName() + "." + propertyName;
    
    final String singularPropName = oddPluralToSingularPropertiesMap.containsKey(propertyName) ? 
      oddPluralToSingularPropertiesMap.get(propertyName) : 
        propertyName.substring(0, propertyName.length() - 1);
        

    assertTrue("related bean " + relatedProperty.getBeanClass().getSimpleName() + 
               " has property with name " +
               relatedProperty.getExpectedName() + 
               " or " + relatedProperty.getExpectedPluralName() + 
               " for " + propFullName,
               relatedProperty.exists());
    
    AbstractEntity relatedBean = (AbstractEntity) getTestValueForType(relatedProperty.getBeanClass());

    // test the add method for the property, but only if related entity can
    // also exist independently
    if (relatedProperty.hasForeignKeyConstraint()) {
      // no adder method on this side; beans are associated in related bean's constructor

      final AbstractEntity[] thisSideBeanHolder = new AbstractEntity[1];
      // assert this bean's getter method returns the related bean that was set via the constructor
      testBean(relatedBean, new BeanTester()
      {
        public void testBean(AbstractEntity relatedBean)
        {
          try {
            AbstractEntity thisSideBean = (AbstractEntity) relatedProperty.invokeGetter(relatedBean);
            assertNotNull("related getter, " + relatedProperty.getFullName() + ", returns non-null", thisSideBean);
            thisSideBeanHolder[0] = thisSideBean;
          }
          catch (Exception e) {
            e.printStackTrace();
            fail("related bean's getter," + relatedProperty.getFullName() + ", threw exception: " + propFullName);
          }
        }
      });
      
      testRelatedBeans(thisSideBeanHolder[0], relatedBean, new RelatedBeansTester()
      {
        public void testBeanWithRelatedBean(AbstractEntity bean,
                                            AbstractEntity relatedBean)
        {
          try {
            assertTrue("related bean with foreign key constraint added itself to this bean " + 
                       bean.getClass().getSimpleName(),
                       ((Collection) getter.invoke(bean)).contains(relatedBean));
          }
          catch (Exception e) {
            e.printStackTrace();
            fail("this bean's collection getter threw exception: " + propFullName);
          }
        }
      });
    }
    else {
      // invoke the adder on this side
      testRelatedBeans(bean, relatedBean, new RelatedBeansTester()
      {
        public void testBeanWithRelatedBean(AbstractEntity bean,
                                            AbstractEntity relatedBean)
        {
          try {
            // find the addMethod
            String addMethodName = "add" + StringUtils.capitalize(singularPropName);
            Method addMethod = findAndCheckMethod(bean.getClass(), 
                                                  addMethodName,
                                                  ExistenceRequirement.REQUIRED);
            addMethod.invoke(bean, relatedBean);
          }
          catch (Exception e) {
            e.printStackTrace();
            fail("adder threw exception: " + propFullName);
          }
        }
      });

      testRelatedBeans(bean, relatedBean, new RelatedBeansTester()
      {
        public void testBeanWithRelatedBean(AbstractEntity bean,
                                            AbstractEntity relatedBean)
        {
          try {
            if (relatedProperty.otherSideIsMany()) {
              Collection result = (Collection) relatedProperty.invokeGetter(relatedBean);
              assertEquals("related getter, " + relatedProperty.getFullName() + ", returns set of size 1 for " + propFullName,
                           1,
                           result.size());
              assertSame("related getter, " + relatedProperty.getFullName() + ", returns this after this.setter(related) for " +
                         propFullName,
                         bean,
                         result.iterator().next());
            }
            else {
              assertSame("related getter, " + relatedProperty.getFullName() + ", returns this after this.setter(related) for " +
                         propFullName,
                         bean,
                         relatedProperty.invokeGetter(relatedBean));
            }
          }
          catch (Exception e) {
            e.printStackTrace();
            fail("related getter, " + relatedProperty.getFullName() + ", threw exception: " + propFullName);
          }
        }
      });
    }
  }
  

  // protected methods

  protected void testBean(AbstractEntity bean, BeanTester tester)
  {
    tester.testBean(bean);
  }

  protected void testRelatedBeans(AbstractEntity bean,
                                  AbstractEntity relatedBean,
                                  RelatedBeansTester tester)
  {
    tester.testBeanWithRelatedBean(bean, relatedBean);
  }
}
