// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
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
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.SchemaUtil;
import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellType;
import edu.harvard.med.screensaver.model.screens.CherryPick;
import edu.harvard.med.screensaver.model.screens.CherryPickRequest;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.Screening;
import edu.harvard.med.screensaver.model.screens.ScreeningRoomActivity;
import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;
import org.springframework.orm.hibernate3.HibernateTemplate;

public abstract class AbstractEntityInstanceTest extends AbstractSpringTest
{
  // static members

  private static Logger log = Logger.getLogger(AbstractEntityInstanceTest.class);

  
  // instance data members

  /**
   * The Hibernate <code>SessionFactory</code>. Used for getting
   * <code>ClassMetadata</code> objects 
   */
  protected SessionFactory hibernateSessionFactory;
  protected HibernateTemplate hibernateTemplate;
  protected DAO dao;
  protected SchemaUtil schemaUtil;

  private Class<? extends AbstractEntity> entityClass;
  private BeanInfo beanInfo;
  private AbstractEntity bean;
  
  @Override
  protected void onSetUp() throws Exception
  {
    super.onSetUp();
  }
  

  // public constructors and methods

  public AbstractEntityInstanceTest(Class<? extends AbstractEntity> clazz) throws IntrospectionException
  {
    super(clazz.getName());
    entityClass = clazz;
    beanInfo = Introspector.getBeanInfo(entityClass);
    bean = newInstance(entityClass);
  }
  
  // Hibernate tests
  
  public void testIsVersioned()
  {
    ClassMetadata classMetadata = hibernateSessionFactory.getClassMetadata(entityClass);
    String entityName = classMetadata.getEntityName();
    assertTrue(
               "hibernate class is versioned: " + entityName,
               classMetadata.isVersioned());
    int versionIndex = classMetadata.getVersionProperty();
    String versionName = classMetadata.getPropertyNames()[versionIndex];
    assertTrue(
               "name of version property is version: " + entityName,
               versionName.equals("version"));
  }
  
  // TODO: test getId() is public and setId() is private
  // TODO: test hbn properties have non-hbn equivalent public getters

  
  // Class-level tests
  
  public void testHasAtLeastOnePublicConstructor()
  {
    boolean hasPublicConstructor = false;
    for (Constructor constructor : entityClass.getConstructors()) {
      if (Modifier.isPublic(constructor.getModifiers())) {
        hasPublicConstructor = true;
        break;
      }
    }
    assertTrue(
               "at least one public constructor in " + entityClass.getName(),
               hasPublicConstructor);
  }

  public void testPublicConstructorHasAtLeastOneParameter()
  {
    for (Constructor constructor : entityClass.getConstructors()) {
      if (Modifier.isPublic(constructor.getModifiers())) {
        assertTrue(
                   "public constructors have at least one param in " + entityClass.getName(),
                   constructor.getParameterTypes().length > 0);
      }
    }
  }

  /**
   * Test version accessors modifiers, arguments, and return types.
   * This test might be a little excessive, but I had to put <i>something</i>
   * here!  ;-)
   */
  public void testVersionAccessors()
  {
    // skip classes that have a getVersion from a superclass
    if (! entityClass.getSuperclass().equals(AbstractEntity.class)) {
      return;
    }

    // getVersion
    try {
      Method getVersionMethod = entityClass.getDeclaredMethod("getVersion");
      assertTrue("private getVersion for " + entityClass, Modifier.isPrivate(getVersionMethod.getModifiers()));
      assertFalse("instance getVersion for " + entityClass, Modifier.isStatic(getVersionMethod.getModifiers()));
      assertEquals("getVersion return type for " + entityClass, getVersionMethod.getReturnType(), Integer.class);
    }
    catch (SecurityException e) {
      e.printStackTrace();
      fail("getting declared method getVersion for " + entityClass + ": " + e);
    }
    catch (NoSuchMethodException e) {
      fail("getting declared method getVersion for " + entityClass + ": " + e);
    }

    // setVersion
    try {
      Method setVersionMethod = entityClass.getDeclaredMethod("setVersion", Integer.class);
      assertTrue("private setVersion for " + entityClass, Modifier.isPrivate(setVersionMethod.getModifiers()));
      assertFalse("instance setVersion for " + entityClass, Modifier.isStatic(setVersionMethod.getModifiers()));
      assertEquals("setVersion return type for " + entityClass, setVersionMethod.getReturnType(), void.class);
    }
    catch (SecurityException e) {
      e.printStackTrace();
      fail("getting declared method getVersion for " + entityClass + ": " + e);
    }
    catch (NoSuchMethodException e) {
      fail("getting declared method getVersion for " + entityClass + ": " + e);
    }
  }
  
  
  // Entity tests

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
            if (! (Collection.class.isAssignableFrom(propertyDescriptor.getPropertyType()) ||
              Map.class.isAssignableFrom(propertyDescriptor.getPropertyType()))) {
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
    createPersistentBeanForTest();
    exercizePropertyDescriptors(new PropertyDescriptorExercizor()
    {
      public void exercizePropertyDescriptor(
        AbstractEntity bean,
        final BeanInfo beanInfo,
        final PropertyDescriptor propertyDescriptor)
      {
        doTestBean(bean, new BeanTester() {
          public void testBean(AbstractEntity bean) 
          {
            if (isImmutableProperty(bean.getClass(), propertyDescriptor)) {
              // do nothing
              String fullPropName = bean.getClass().getSimpleName() + "." + propertyDescriptor.getDisplayName();
              log.info("testCollectionPropertiesStartOutEmpty(): skipping test for collection " + fullPropName + 
                       ": collection is immutable");
              return;
            }
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
                           getExpectedInitialCollectionSize(propertyDescriptor),
                                                            ((Collection) result).size());
            }
          }
        });
      }
    });
  }
  
  /**
   * If a property is immutable, test that it is set via constructor (simply by
   * testing that the getter does not return null), and that no public setter
   * method.
   */
  public void testGettersForImmutableProperties()
  {
    createPersistentBeanForTest();
    exercizePropertyDescriptors(new PropertyDescriptorExercizor()
    {
      public void exercizePropertyDescriptor(
        AbstractEntity bean,
        BeanInfo beanInfo,
        final PropertyDescriptor propertyDescriptor)
      {
        final String propFullName = bean.getClass() + "." + propertyDescriptor.getName();

        if (!isImmutableProperty(bean.getClass(), propertyDescriptor)) {
          return;
        }
        
        try {
          final Object originalValue = propertyDescriptor.getReadMethod().invoke(bean);
          doTestBean(bean, new BeanTester()
          {
            public void testBean(AbstractEntity bean) {
              assertNull("immutable property has no public setter method",
                         propertyDescriptor.getWriteMethod());
              try {
                Object getterValue = propertyDescriptor.getReadMethod().invoke(bean);
                assertNotNull("constructor sets value for immutable property; " + propFullName, getterValue);
                assertEquals("getter for immutable property " + propFullName + 
                             " returns value specified in constructor",
                             originalValue,
                             getterValue);
              }
              catch (Exception e) {
                e.printStackTrace();
                fail("this.getter() threw exception: " + propFullName);
              }
            }
          });
        } catch (Exception e) {
          e.printStackTrace();
          fail("this.getter() threw exception: " + propFullName);
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
    createPersistentBeanForTest();
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
        if (Collection.class.isAssignableFrom(getter.getReturnType()) || 
          Map.class.isAssignableFrom(getter.getReturnType())) {
          return;
        }
        final Method setter = propertyDescriptor.getWriteMethod();
        final Object testValue = getTestValueForType(getter.getReturnType());

        // call the setter
        doTestBean(bean, new BeanTester() 
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
        doTestBean(bean, new BeanTester() 
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
    createPersistentBeanForTest();
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
   * <li><del>has a pluralized name</del>
   * <li>does not have a (public) setter
   * <li>has boolean add/remove methods with param of right type
   * <li>add;get returns set of one
   * <li>add;remove;get returns empty set
   */
  @SuppressWarnings("unchecked")
  private void doTestCollectionProperty(AbstractEntity bean,
                                        Method getter,
                                        final PropertyDescriptor propertyDescriptor)
  {
    Class<? extends AbstractEntity> beanClass = bean.getClass();
    String beanClassName = beanClass.getSimpleName();
    String propertyName = propertyDescriptor.getName();
    final String fullPropName = beanClassName + "." + propertyName;
    
    
//    // collection property has pluralized name
//    assertTrue(
//      "collection property getter has plural name: " + fullPropName,
//      EntityBeansTest.oddPluralToSingularPropertiesMap.containsKey(propertyName) ||
//      propertyName.endsWith("s"));

    String singularPropName = propertyName.substring(0, propertyName.length() - 1);
    if (propertyDescriptor.getReadMethod().getAnnotation(CollectionElementName.class) != null) {
      singularPropName = propertyDescriptor.getReadMethod().getAnnotation(CollectionElementName.class).value();
    }
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
    else if (isImmutableProperty(beanClass, propertyDescriptor)) {
      // do nothing
      log.info("testCollectionProperty(): skipping add/remove test for collection " + fullPropName + 
               ": collection is immutable");
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
      doTestBean(bean, new BeanTester()
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
      doTestBean(bean, new BeanTester()
      {
        public void testBean(AbstractEntity bean)
        {
          try {
            Collection result = (Collection) getterMethod.invoke(bean);
            assertEquals("collection prop with one element added has size one greater than initial size: " + fullPropName,
                         getExpectedInitialCollectionSize(propertyDescriptor) + 1,
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
        doTestRelatedBeans(bean, (AbstractEntity) testValue, new RelatedBeansTester()
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
        doTestBean(bean, new BeanTester()
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
      doTestBean(bean, new BeanTester()
      {
        public void testBean(AbstractEntity bean)
        {
          try {
            Collection result = (Collection) getterMethod.invoke(bean);
            assertEquals("collection prop with element removed has original size: " + fullPropName,
                         result.size(),
                         getExpectedInitialCollectionSize(propertyDescriptor));
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
    createPersistentBeanForTest();
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


  private void createPersistentBeanForTest()
  {
    schemaUtil.truncateTablesOrCreateSchema();
    dao.persistEntity(bean);
  }
  
  private void doTestBidirectionalityOfOneSideOfRelationship(AbstractEntity bean,
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
                  " for " + propFullName,
                  relatedProperty.getPropertyDescriptor());
    
    if (hasForeignKeyConstraint(propertyDescriptor)) {
      // no setter method on this side; this side's bean is associated in this bean's constructor (e.g. ScreenResult.screen)
      log.debug("testBidirectionalityOfOneSideOfRelationship(): " + propFullName + 
                " has foreign key constraint: assuming related bean is associated in this bean's constructor");
      final AbstractEntity[] relatedBeanHolder = new AbstractEntity[1];
      // assert this bean's getter method returns the related bean that was set via the constructor
      doTestBean(bean, new BeanTester()
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
      doTestRelatedBeans(bean, relatedBeanHolder[0], new RelatedBeansTester()
      {
        public void testBeanWithRelatedBean(AbstractEntity bean,
                                            AbstractEntity relatedBean)
        {
          try {
            if (relatedProperty.otherSideIsToMany()) {
              assertTrue("related getter contains this bean",
                         ((Collection) relatedProperty.invokeGetter(relatedBean)).contains(bean));
            }
            else {
              Object beanFromRelatedGetter = relatedProperty.invokeGetter(relatedBean);
              assertEquals("related getter, " + relatedProperty.getFullName() + ", returns this bean",
                           bean,
                           beanFromRelatedGetter);
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
      doTestBean(bean, new BeanTester()
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
      doTestRelatedBeans(bean, relatedBean, new RelatedBeansTester()
      {
        public void testBeanWithRelatedBean(AbstractEntity bean,
                                            AbstractEntity relatedBean)
        {
          if (relatedProperty.otherSideIsToMany()) {
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

  private void doTestBidirectionalityOfManySideOfRelationship(AbstractEntity bean,
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

    // determine the add method of this bean (that adds the related bean)
    Method readMethod = propertyDescriptor.getReadMethod();
    final String singularPropName = 
      readMethod.getAnnotation(CollectionElementName.class) != null 
      ? readMethod.getAnnotation(CollectionElementName.class).value() :
        propertyName.substring(0, propertyName.length() - 1);
    final String addMethodName = "add" + StringUtils.capitalize(singularPropName);
    
    assertTrue("related bean " + relatedProperty.getBeanClass().getSimpleName() + 
               " has property with name " +
               relatedProperty.getExpectedName() + 
               " for " + propFullName,
               relatedProperty.exists());
    
    AbstractEntity relatedBean = (AbstractEntity) getTestValueForType(relatedProperty.getBeanClass());

    // test the add method for the property, but only if related entity can
    // also exist independently
    if (relatedProperty.hasForeignKeyConstraint()) {
      // no adder method on this side; beans are associated in related bean's constructor

      final AbstractEntity[] thisSideBeanHolder = new AbstractEntity[1];
      // assert this bean's getter method returns the related bean that was set via the constructor
      doTestBean(relatedBean, new BeanTester()
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
      
      doTestRelatedBeans(thisSideBeanHolder[0], relatedBean, new RelatedBeansTester()
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
      doTestRelatedBeans(bean, relatedBean, new RelatedBeansTester()
      {
        public void testBeanWithRelatedBean(AbstractEntity bean,
                                            AbstractEntity relatedBean)
        {
          try {
            // find the addMethod
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

      doTestRelatedBeans(bean, relatedBean, new RelatedBeansTester()
      {
        public void testBeanWithRelatedBean(AbstractEntity bean,
                                            AbstractEntity relatedBean)
        {
          try {
            if (relatedProperty.otherSideIsToMany()) {
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
              assertSame("related getter, " + relatedProperty.getFullName() + ", returns this after this.add(related) for " +
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
  

  // private methods
  
  private static String STRING_TEST_VALUE_PREFIX = "test:";
  private static int STRING_TEST_VALUE_RADIX = 36;
  
  private Integer _integerTestValue = 77;
  private double  _doubleTestValue = 77.1;
  private boolean _booleanTestValue = true;
  private int     _stringTestValueIndex = Integer.parseInt("antz", STRING_TEST_VALUE_RADIX);
  private long    _dateMilliseconds = 0;
  private int     _vocabularyTermCounter = 0;
  private int     _wellNameTestValueIndex = 0;
  private WellKey _wellKeyTestValue = new WellKey("00001:A01");
  
  @SuppressWarnings("unchecked")
  public Object getTestValueForType(Class type)
  {
    if (type.equals(Integer.class)) {
      _integerTestValue += 1;
      return _integerTestValue;
    }
    if (type.equals(Double.class)) {
      _doubleTestValue *= 1.32;
      return new Double(new Double(_doubleTestValue * 1000).intValue() / 1000);
    }
    if (type.equals(BigDecimal.class)) {
      BigDecimal val = new BigDecimal(((Double) getTestValueForType(Double.class)).doubleValue());
      // 2 is the default scale used in our Hibernate mapping, not sure how to change it via xdoclet
      val = val.setScale(2);
      return val;
    }
    if (type.equals(Boolean.TYPE)) {
      _booleanTestValue = ! _booleanTestValue;
      return _booleanTestValue;
    }
    if (type.equals(String.class)) {
      return STRING_TEST_VALUE_PREFIX + Integer.toString(++_stringTestValueIndex, STRING_TEST_VALUE_RADIX);
    }
    if (type.equals(Date.class)) {
      _dateMilliseconds += 1000 * 60 * 60 * 24 * 1.32;
      return DateUtils.round(new Date(_dateMilliseconds), Calendar.DATE);
    }
    if (AbstractEntity.class.isAssignableFrom(type)) {
      return newInstance((Class<AbstractEntity>) type);
    }
    if (VocabularyTerm.class.isAssignableFrom(type)) {
      try {
        Method valuesMethod = type.getMethod("values");
        Object values = (Object) valuesMethod.invoke(null);
        int numValues = Array.getLength(values);
        int valuesIndex = ++ _vocabularyTermCounter % numValues;
        return Array.get(values, valuesIndex);
      }
      catch (Exception e) {
        e.printStackTrace();
        fail("vocabulary term test value code threw an exception");
      }
    }    
    if (WellKey.class.isAssignableFrom(type)) {
      return nextWellKey(_wellKeyTestValue);
    }
    throw new IllegalArgumentException(
      "can't create test values for type: " + type.getName());
  }
  
  private Object nextWellKey(WellKey wellKey)
  {
    int col = wellKey.getColumn() + 1;
    int row = wellKey.getRow();
    int plateNumber = wellKey.getPlateNumber();
    if (col >= Well.PLATE_COLUMNS) {
      col = 0;
      ++row;
    }
    if (row >= Well.PLATE_ROWS) {
      row = 0;
      ++plateNumber;
    }
    _wellKeyTestValue = new WellKey(plateNumber, row, col);
    return _wellKeyTestValue;
  }

  private Object getTestValueForWellName()
  {
    String wellName = String.format("%c%02d",
                                    'A' + (_wellNameTestValueIndex / 24),
                                    (_wellNameTestValueIndex % 24) + 1);
    ++_wellNameTestValueIndex;
    return wellName;
  }

  private static Map<Class<? extends AbstractEntity>,Class<? extends AbstractEntity>> _concreteStandinMap =
      new HashMap<Class<? extends AbstractEntity>,Class<? extends AbstractEntity>>();
  static {
    _concreteStandinMap.put(Screening.class, LibraryScreening.class);
    _concreteStandinMap.put(ScreeningRoomActivity.class, LibraryScreening.class);
    _concreteStandinMap.put(CherryPickRequest.class, RNAiCherryPickRequest.class);
  }
  
  private static interface EntityFactory
  {
    AbstractEntity newInstance();
  }
  
  private Map<Class<? extends AbstractEntity>,EntityFactory> _entityFactoryMap =
    new HashMap<Class<? extends AbstractEntity>,EntityFactory>();
  {
    _entityFactoryMap.put(CherryPick.class, new EntityFactory() 
    {
      private int testEntrezGeneId = 0;
      public AbstractEntity newInstance()
      {
        CherryPickRequest cherryPickRequest = (CherryPickRequest) getTestValueForType(CherryPickRequest.class);
        Well well = (Well) getTestValueForType(Well.class);
        well.setWellType(WellType.EXPERIMENTAL);
        if (cherryPickRequest.getScreen().getScreenType().equals(ScreenType.SMALL_MOLECULE)) {
          well.addCompound(new Compound("CCC"));
        }
        else if (cherryPickRequest.getScreen().getScreenType().equals(ScreenType.RNAI)) {
          Gene gene = new Gene("AAA", ++testEntrezGeneId, "entrezSymbol" + testEntrezGeneId, "Human");
          well.addSilencingReagent(new SilencingReagent(gene, SilencingReagentType.SIRNA, "ATCG"));
        }
        return new CherryPick(cherryPickRequest, well);
      }
    });
  }
  
  protected AbstractEntity newInstance(Class<? extends AbstractEntity> entityClass) {
    if (Modifier.isAbstract(entityClass.getModifiers())) {
      Class<? extends AbstractEntity> concreteStandin =
        _concreteStandinMap.get(entityClass);
      return newInstance(concreteStandin);
    }
    
    EntityFactory entityFactory = _entityFactoryMap.get(entityClass);
    if (entityFactory != null) {
      return entityFactory.newInstance();
    }

    try {
      Constructor constructor = getMaxArgConstructor(entityClass);
      Object[] arguments = getArgumentsForConstructor(constructor);
      return (AbstractEntity) constructor.newInstance(arguments);
    }
    catch (Exception e) {
      e.printStackTrace();
      fail("newInstance for " + entityClass + " threw an Exception: " + e);
    }
    return null;
  }
  
  protected Object[] getArgumentsForConstructor(Constructor constructor)
  {
    Class[] parameterTypes = constructor.getParameterTypes();
    Object[] arguments = getArgumentsForParameterTypes(parameterTypes);
    return arguments;
  }

  private Object[] getArgumentsForParameterTypes(Class[] parameterTypes) {
    
    Object [] arguments = new Object[parameterTypes.length];
    for (int i = 0; i < arguments.length; i++) {
      arguments[i] = getTestValueForType(parameterTypes[i]);
    }
    
    return arguments;
  }

  protected Constructor getMaxArgConstructor(Class<? extends AbstractEntity> entityClass)
  {
    int maxArgs = 0;
    Constructor maxArgConstructor = null;
    for (Constructor constructor : entityClass.getConstructors()) {
      if (Modifier.isPublic(constructor.getModifiers())) {
        int numArgs = constructor.getParameterTypes().length;
        if (numArgs > maxArgs) {
          maxArgs = numArgs;
          maxArgConstructor = constructor;
        }
      }
    }
    return maxArgConstructor;
  }
  
  
  protected static interface PropertyDescriptorExercizor
  {
    public void exercizePropertyDescriptor(
      AbstractEntity bean,
      BeanInfo beanInfo,
      PropertyDescriptor propertyDescriptor);
  }
  
  protected void exercizePropertyDescriptors(final PropertyDescriptorExercizor exercizor)
  {
    for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
      String propertyName = propertyDescriptor.getName();
      String propFullName = bean.getClass().getSimpleName() + "." + propertyName;
      if (propertyName.equals("class")) {
        log.debug("skipping \"class\" property " + propFullName);
        continue;
      }

      if (propertyName.startsWith("hbn")) {
        log.info("skipping Hibernate property " + propFullName);
        continue;
      }

      // skip what appears to be an entity's property, but that has been
      // explicitly annotated as a non-property
      if (propertyDescriptor.getReadMethod().isAnnotationPresent(DerivedEntityProperty.class)) {
        log.info("skipping derived property " + propFullName);
        continue;
      }

      log.debug("excercizing JavaBean entity property " + bean.getClass().getSimpleName() + "." + propertyName + 
                " with " + exercizor.getClass().getEnclosingMethod().getName());
      exercizor.exercizePropertyDescriptor(bean, beanInfo, propertyDescriptor);
    }
  }
  

  /**
   * Find the method with the given name, and unspecified arguments. If no such
   * method exists, and the isRequiredMethod parameter is true, then fail. If no
   * such method exists, and isRequiredMethod is false, then return null. Fail if
   * the method does not return a boolen. Return the method.
   * @param beanClass the class to find the method in
   * @param methodName the name of the method to find
   * @param isRequiredMethod true iff the method is required to exist
   * @return the method. Return null if isRequiredMethod is false and no such
   * method exists.
   */
  protected Method findAndCheckMethod(
    Class<? extends AbstractEntity> beanClass,
    String methodName,
    ExistenceRequirement requirement)
  {
    String fullMethodName = beanClass.getName() + "." + methodName;
    Method foundMethod = null;
    // note: we're calling getMethods() instead of getDeclaredMethods() to allow
    // inherited methods to satisfy our isRequiredMethod constraint (e.g. for
    // AdministratorUser.addRole())
    // TODO: getMethods() will only return public methods, is this okay?
    for (Method method : beanClass.getMethods()) {
      if (method.getName().equals(methodName)) {
        foundMethod = method;
        break;
      }
    }
    
    if (requirement != ExistenceRequirement.REQUIRED && foundMethod == null) {
      log.debug("findAndCheckMethod(): non-required method was not found: " + fullMethodName);
      return null;
    }
    assertTrue("collection property not allowed: " + fullMethodName, 
               requirement != ExistenceRequirement.NOT_ALLOWED || foundMethod == null);
    assertTrue("collection property missing method: " + fullMethodName,
               requirement != ExistenceRequirement.REQUIRED || foundMethod != null);

    assertEquals("collection property method returns boolean: " + fullMethodName,
                 Boolean.TYPE,
                 foundMethod.getReturnType());
    return foundMethod;
  }
  
  /**
   * Returns true iff the property corresponds to the entity's ID. Such methods
   * include: getEntityId(), getFooId() (for bean of type Foo), and any
   * properties that may be used to define the ID (for cases where the entity ID
   * is not an auto-generated database ID, but instead correspdonds to the
   * entity's business key).
   * 
   * @param beanInfo the bean the property belongs to
   * @param propertyDescriptor the property
   * @return true iff property is "entityId" or the property that is named the
   *         same as the entity, but with an "Id" suffix; otherwise false
   */
  @SuppressWarnings("unchecked")
  private boolean isEntityIdProperty(Class<? extends AbstractEntity> beanClass,
                                     PropertyDescriptor propertyDescriptor)
  {
    Method getter = propertyDescriptor.getReadMethod();
    if (getter.isAnnotationPresent(EntityIdProperty.class)) {
      log.debug("isEntityIdProperty(): property participates in defining entity ID: " + propertyDescriptor.getName());
      return true;
    }

    // legacy logic for finding the standard entity-ID related methods below...
    if (propertyDescriptor.getName().equals("entityId")) {
      log.debug("isEntityIdProperty(): property participates in defining entity ID: " + propertyDescriptor.getName());
      return true;
    }

    // Check whether property corresponds to the bean's Hibernate ID method, which is named similarly to the bean.
    // We also check the parent classes, to handle the case where the property
    // has been inherited, as the property name will depend upon the class it
    // was declared in.
    String capitalizedPropertyName = propertyDescriptor.getName().substring(0, 1).toUpperCase() + propertyDescriptor.getName().substring(1);
    while (!beanClass.equals(AbstractEntity.class) && beanClass != null) {
      if (capitalizedPropertyName.endsWith(beanClass.getSimpleName() + "Id")) {
        log.debug("isEntityIdProperty(): property participates in defining entity ID: " + propertyDescriptor.getName() + 
                  " in " + beanClass.getSimpleName());
        return true;
      }
      beanClass = (Class<? extends AbstractEntity>) beanClass.getSuperclass();
    }
    return false;
  }
  
  
  // HACK: special case handling 
  protected int getExpectedInitialCollectionSize(
    PropertyDescriptor propertyDescriptor)
  {
    Method getter = propertyDescriptor.getReadMethod();
    ToManyRelationship toManyRelationship = getter.getAnnotation(ToManyRelationship.class);
    if (toManyRelationship != null) {
      return toManyRelationship.minCardinality();
    }
    return 0;
  }
  
  protected boolean isUnidirectionalRelationship(Class<? extends AbstractEntity> beanClass, 
                                                 PropertyDescriptor propertyDescriptor)
  {
    try {
      return isUnidirectionalRelationshipMethod(propertyDescriptor.getReadMethod()) ||
      isUnidirectionalRelationshipMethod(beanClass.
                                         getDeclaredMethod("getHbn" + 
                                                           StringUtils.capitalize(propertyDescriptor.getName())));
    }
    catch (SecurityException e) {
      throw e;
    }
    catch (NoSuchMethodException e) {
      return false;
    }
  }

  protected boolean hasForeignKeyConstraint(PropertyDescriptor propertyDescriptor)
  {
    Method getter = propertyDescriptor.getReadMethod();
    ToOneRelationship toOneRelationship = getter.getAnnotation(ToOneRelationship.class);
    return toOneRelationship != null && !toOneRelationship.nullable();
  }

  protected boolean setterMethodNotExpected(Class<? extends AbstractEntity> beanClass, PropertyDescriptor propertyDescriptor)
  {
    String propFullName = beanClass.getSimpleName() + "." + propertyDescriptor.getName();
    
    if (isImmutableProperty(beanClass, propertyDescriptor)) {
      log.info("setter method not expected for immutable property: " + propFullName);
      return true;
    }

    // no setter expected if property participates in defining the entity ID
    if (isEntityIdProperty(beanClass, propertyDescriptor)) {
      log.info("setter method not expected for property that participates in defining the entity ID: " + 
               propFullName);
      return true;
    }

    // no setter expected if property is for a *-to-one relationship and has a foreign key constraint
    if (hasForeignKeyConstraint(propertyDescriptor)) {
      log.info("setter method not expected for property that is a *-to-one relationship with a foreign key constraint: " + 
               propFullName);
      return true;
    }
    
    // no public setter expected if property is for a one-to-one relationship and related side has a foreign key constraint relationship
    // (in this case, either a package or public setter method is required, but we're not verifying this currently)
    RelatedProperty relatedProperty = new RelatedProperty(beanClass, propertyDescriptor);
    if (relatedProperty.exists() && relatedProperty.hasForeignKeyConstraint()) {
      log.info("setter method not expected for property that is on the \"one\" side of a relationship with a foreign key constraint: " + 
               propFullName);
      return true;
    }
    return false;
  }

  protected boolean isImmutableProperty(Class<? extends AbstractEntity> beanClass, PropertyDescriptor propertyDescriptor)
  {
    Method getter = propertyDescriptor.getReadMethod();
    if (getter.isAnnotationPresent(ImmutableProperty.class)) {
      return true;
    }
    return false;
  }

  private boolean isUnidirectionalRelationshipMethod(Method getter)
  {
    ToOneRelationship toOneRelationship = getter.getAnnotation(ToOneRelationship.class);
    ToManyRelationship toManyRelationship = getter.getAnnotation(ToManyRelationship.class);
    if ((toOneRelationship != null && toOneRelationship.unidirectional()) ||
      (toManyRelationship != null && toManyRelationship.unidirectional())) {
      return true;
    }
    return false;
  }
  
  
  
//  protected void testBean(AbstractEntity bean, BeanTester tester)
//  {
//    tester.testBean(bean);
//  }
//
//  protected void testRelatedBeans(AbstractEntity bean,
//                                  AbstractEntity relatedBean,
//                                  RelatedBeansTester tester)
//  {
//    tester.testBeanWithRelatedBean(bean, relatedBean);
//  }

  
  static private interface BeanTester
  {
    public void testBean(AbstractEntity bean);
  }

  static private interface RelatedBeansTester
  {
    public void testBeanWithRelatedBean(AbstractEntity bean,
                                        AbstractEntity relatedBean);
  }  

  private void doTestBean(final AbstractEntity bean,
                          final BeanTester tester)
  {
    dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        AbstractEntity localBean = getPersistedEntity(bean);
        tester.testBean(localBean);
      }
    });
  }
  
  private void doTestRelatedBeans(final AbstractEntity bean,
                                final AbstractEntity relatedBean,
                                final RelatedBeansTester tester)
  {
    dao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        AbstractEntity localBean = getPersistedEntity(bean);
        AbstractEntity localRelatedBean = getPersistedEntity(relatedBean);
        tester.testBeanWithRelatedBean(localBean, localRelatedBean);
      }
    });
  }  

  // if the bean has already been persisted, then get the persisted copy, as the current
  // copy is stale. if it has not, persist it now so we can get the entityId
  private AbstractEntity getPersistedEntity(AbstractEntity bean)
  {
    AbstractEntity beanFromHibernate = null;
    if (bean.getEntityId() != null) {
      beanFromHibernate = (AbstractEntity) hibernateTemplate.get(bean.getClass(), bean.getEntityId());
    }
    if (beanFromHibernate == null) {
      hibernateTemplate.saveOrUpdate(bean);
      beanFromHibernate = (AbstractEntity) hibernateTemplate.get(bean.getClass(), bean.getEntityId());
    }
    return beanFromHibernate;
  }
  
}
