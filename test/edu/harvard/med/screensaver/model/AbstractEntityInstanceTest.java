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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Transient;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.springframework.orm.hibernate3.HibernateTemplate;

import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.SchemaUtil;
import edu.harvard.med.screensaver.model.annotations.CollectionOfElements;
import edu.harvard.med.screensaver.model.annotations.Column;
import edu.harvard.med.screensaver.model.annotations.ContainedEntity;
import edu.harvard.med.screensaver.model.annotations.ManyToMany;
import edu.harvard.med.screensaver.model.annotations.ManyToOne;
import edu.harvard.med.screensaver.model.annotations.OneToMany;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.LabCherryPick;
import edu.harvard.med.screensaver.model.cherrypicks.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.ScreenerCherryPick;
import edu.harvard.med.screensaver.model.entitytesters.AbstractEntityTester;
import edu.harvard.med.screensaver.model.entitytesters.IdentifierAccessorModifiersTester;
import edu.harvard.med.screensaver.model.entitytesters.IdentifierMetadataTester;
import edu.harvard.med.screensaver.model.entitytesters.IsVersionedTester;
import edu.harvard.med.screensaver.model.entitytesters.VersionAccessorsTester;
import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellType;
import edu.harvard.med.screensaver.model.libraries.WellVolumeAdjustment;
import edu.harvard.med.screensaver.model.libraries.WellVolumeCorrectionActivity;
import edu.harvard.med.screensaver.model.propertytesters.CollectionPropertiesInitialCardinalityTester;
import edu.harvard.med.screensaver.model.propertytesters.PropertiesGetterAndSetterTester;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.Screening;
import edu.harvard.med.screensaver.model.screens.ScreeningRoomActivity;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.util.StringUtils;

public abstract class AbstractEntityInstanceTest<E extends AbstractEntity> extends AbstractSpringTest
{

  // static fields

  private static Logger log = Logger.getLogger(AbstractEntityInstanceTest.class);


  // instance fields injected by Spring

  /** The Hibernate <code>SessionFactory</code>. Used for getting <code>ClassMetadata</code> objects. */
  protected SessionFactory hibernateSessionFactory;
  protected HibernateTemplate hibernateTemplate;
  protected GenericEntityDAO genericEntityDao;
  protected SchemaUtil schemaUtil;


  // instance fields initialized in the constructor

  private Class<E> _entityClass;
  private BeanInfo _beanInfo;
  private E _bean;


  // public constructors and instance methods

  public AbstractEntityInstanceTest(Class<E> clazz)
  throws IntrospectionException
  {
    super(clazz.getName());
    _entityClass = clazz;
    _beanInfo = Introspector.getBeanInfo(_entityClass);
    _bean = newInstance(_entityClass);
  }

  /**
   * Test some basic stuff, mostly about the identifier, in the ClassMetadata.
   */
  public void testIdentifierMetadata()
  {
    new IdentifierMetadataTester<E>(_entityClass, hibernateSessionFactory).testEntity();
  }

  /**
   * Test that the identifier getter method is public, the identifier getter method is private,
   * both are instance, and the arg/return types match.
   */
  public void testIdentifierAccessorModifiers()
  {
    new IdentifierAccessorModifiersTester<E>(_entityClass, hibernateSessionFactory).testEntity();
  }

  /**
   * Test that the entity is versioned.
   */
  public void testIsVersioned()
  {
    new IsVersionedTester<E>(_entityClass, hibernateSessionFactory).testEntity();
  }

  /**
   * Test version accessor methods: modifiers, arguments, annotations, and return types.
   */
  public void testVersionAccessors()
  {
    new VersionAccessorsTester<E>(_entityClass, hibernateSessionFactory).testEntity();
  }

  /**
   * Tests for the correctness of presence or absence of getter and setter methods for the
   * entity's properties.
   */
  public void testPropertiesHaveGetterAndSetter()
  {
    AbstractEntityTester<E> propertiesGetterAndSetterTester =
      new PropertiesGetterAndSetterTester<E>(
        _entityClass,
        hibernateSessionFactory,
        _beanInfo,
        _bean);
    propertiesGetterAndSetterTester.testEntity();
  }

  /**
   * Test that all collection properties start out with the correct size. This initial size is
   * assumed to be zero, unless the property has a {@link CollectionOfElements} annotation with
   * non-default {@link CollectionOfElements#initialCardinality()}.
   */
  public void testCollectionPropertiesInitialCardinality()
  {
    createPersistentBeanForTest();
    AbstractEntityTester<E> collectionPropertiesInitialCardinalityTester =
      new CollectionPropertiesInitialCardinalityTester<E>(
        _entityClass,
        hibernateSessionFactory,
        _beanInfo,
        _bean);
    collectionPropertiesInitialCardinalityTester.testEntity();
  }

  // TODO: continue refactoring

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

        if (! isImmutableProperty(bean.getClass(), propertyDescriptor)) {
          return;
        }

        if (isNullableImmutableProperty(bean, propertyDescriptor)) {
          return;
        }

        try {
          final Object originalValue = propertyDescriptor.getReadMethod().invoke(bean);
          doTestBean(bean, new BeanTester()
          {
            public void testBean(AbstractEntity bean) {
              assertNull("immutable property " + propFullName + " has no public setter method",
                         propertyDescriptor.getWriteMethod());
              try {
                Object getterValue = propertyDescriptor.getReadMethod().invoke(bean);
                assertNotNull("constructor sets value for immutable property; " + propFullName, getterValue);

                // can no longer test the two entities against each other for equality, since
                // they do not belong to the same session. maybe should call dao.reload() here
                // on the originalValue, but just testing entityIds for equality instead
                if (originalValue instanceof AbstractEntity) {
                  assertEquals("getter for immutable property " + propFullName +
                    " returns value specified in constructor",
                    ((AbstractEntity) originalValue).getEntityId(),
                    ((AbstractEntity) getterValue).getEntityId());
                  // TODO: could run AE.isEquivalent as well..
                }
                else {
                  assertEquals("getter for immutable property " + propFullName +
                    " returns value specified in constructor",
                    originalValue,
                    getterValue);
                }
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

  private boolean isNullableImmutableProperty(
    AbstractEntity bean,
    final PropertyDescriptor propertyDescriptor)
  {

    Method readMethod = propertyDescriptor.getReadMethod();
    if (readMethod.getAnnotation(javax.persistence.ManyToOne.class) == null) {
      return false;
    }
    javax.persistence.JoinColumn joinColumn =
      readMethod.getAnnotation(javax.persistence.JoinColumn.class);
    if (joinColumn != null && joinColumn.nullable()) {
      // currently, only two cases fall here:
      // WellVolumeAdjustment.{labCherryPick,wellVolumeCorrectionActivity}.
      // WVA is strange in that exactly one of these two properties is non-null
      if (! bean.getClass().equals(WellVolumeAdjustment.class)) {
        fail(
          "non-WVA property with a nullable immutable property. (probably fine but you should check it out): " +
          bean.getClass().getSimpleName() + "." + propertyDescriptor.getName());
      }
      return true;
    }
    return false;
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
        class TestValueBeanTester implements BeanTester
        {
          Object testValue;
          public void testBean(AbstractEntity bean)
          {
            // TODO - work out a test for containedEntity.alternateContainingEntityClass as well
            ContainedEntity containedEntity = (ContainedEntity) getter.getReturnType().getAnnotation(ContainedEntity.class);
            if (containedEntity != null &&
              containedEntity.containingEntityClass().isAssignableFrom(bean.getClass())) {
              testValue = getTestValueForType(getter.getReturnType(), bean);
            }
            else {
              testValue = getTestValueForType(getter.getReturnType());
            }
          }
        }
        TestValueBeanTester testValueBeanTester = new TestValueBeanTester();
        doTestBean(bean, testValueBeanTester);
        final Object testValue = testValueBeanTester.testValue;

        // right, if the testValue happens to be a contained entity of the bean, then the above
        // call to getTestValueForType should have hooked the testValue up with the parent
        // already, so we probably shouldn't call the setter again. in fact, the setter
        // should be private for contained entities, so the setter invocation should fail

        ContainedEntity testValueClassContainedEntityAnnotation =
          testValue.getClass().getAnnotation(ContainedEntity.class);
        if (testValueClassContainedEntityAnnotation != null &&
          testValueClassContainedEntityAnnotation.containingEntityClass().equals(bean.getClass())) {
          assertNull("setter should be null for contained entity: " + propertyDescriptor, setter);
        }
        else {

        // call the setter
        doTestBean(bean, new BeanTester()
        {
          public void testBean(AbstractEntity bean)
          {
            try {
              log.info("calling setter " + setter + " on bean " + bean + " with test value " + testValue);
              if (testValue instanceof AbstractEntity) {
                setter.invoke(bean, getPersistedEntity((AbstractEntity) testValue));
              }
              else {
              setter.invoke(bean, testValue);
              }
            }
            catch (Exception e) {
              e.printStackTrace();
              fail(
                   "setter threw exception: " +
                   bean.getClass() + "." + propertyDescriptor.getName());
            }
          }
        });
        }

        // call the getter
        doTestBean(bean, new BeanTester()
        {
          public void testBean(AbstractEntity bean)
          {
            try {
              // this only makes sense for non-persistent tests; testing equality, rather than sameness, is probably good enough
//              if (AbstractEntity.class.isAssignableFrom(getter.getReturnType())) {
//                assertSame("getter returns what setter set for " +
//                           _bean.getClass() + "." + propertyDescriptor.getName(),
//                           testValue,
//                           getter.invoke(_bean));
//              }
//              else {
                assertEquals("getter returns what setter set for " +
                             bean.getClass() + "." + propertyDescriptor.getName(),
                             (testValue instanceof AbstractEntity) ?
                             genericEntityDao.reloadEntity((AbstractEntity) testValue) : testValue,
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
          if (getter.getAnnotation(Transient.class) != null) {
            log.info("skipping testCollectionProperties for @Transient method " +
              getter.getName());
            return;
          }
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
   * <li>does not have a setter
   * <li>has boolean add/remove methods with param of right type
   * <li>add;get returns set of one
   * <li>add;remove;get returns empty set
   *
   * TODO: test @OrderBy for collections
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

    // TODO: superclass @ToMany
    String singularPropName = null;
    ManyToMany manyToMany = propertyDescriptor.getReadMethod().getAnnotation(ManyToMany.class);
    OneToMany oneToMany = propertyDescriptor.getReadMethod().getAnnotation(OneToMany.class);
    if (manyToMany != null &&
      manyToMany.singularPropertyName() != null &&
      ! manyToMany.singularPropertyName().equals("")) {
      singularPropName = manyToMany.singularPropertyName();
    }
    else if (oneToMany != null &&
      oneToMany.singularPropertyName() != null &&
      ! oneToMany.singularPropertyName().equals("")) {
      singularPropName = oneToMany.singularPropertyName();
    }
    else if (propertyName.endsWith("s")) {
      singularPropName = propertyName.substring(0, propertyName.length() - 1);
    }

    // collection property has pluralized name
    assertNotNull(
      "collection property getter has plural name or a ToMany.singularPropertyName annotation: " +
      fullPropName,
      singularPropName);

    String capitalizedSingularPropName = StringUtils.capitalize(singularPropName);

    // collection property has no setter (should only have add and/or remove methods)
    assertNull(
      "collection property has no setter: " + fullPropName,
      propertyDescriptor.getWriteMethod());

    // if the getter method for the property is annotated with
    // @Column(hasNonconventionalSetterMethod=true), then there is no adder method for this
    // collection, and testing the add/remove functionality is beyond the scope of these
    // unit tests
    final Method getterMethod = propertyDescriptor.getReadMethod();
    edu.harvard.med.screensaver.model.annotations.Column getterColumnAnnotation =
      getterMethod.getAnnotation(edu.harvard.med.screensaver.model.annotations.Column.class);
    if (getterColumnAnnotation != null && getterColumnAnnotation.hasNonconventionalSetterMethod()) {
      return;
    }

    RelatedProperty relatedProperty = new RelatedProperty(beanClass, propertyDescriptor);

    // if related _bean cannot exist independently, then
    // this side should *not* have an add or remove method, since the related
    // _bean must be associated during instantation only.
    if (relatedProperty.exists() && relatedProperty.relatedPropertyIsImmutable()) {
      // do nothing
      log.info("testCollectionProperty(): skipping add/remove test for collection " + fullPropName +
               ": related property " + relatedProperty.getName() +
               " is immutable, and cannot be added/removed from this _bean");
    }
    else if (isImmutableProperty(beanClass, propertyDescriptor)) {
      // do nothing
      log.info("testCollectionProperty(): skipping add/remove test for collection " + fullPropName +
               ": collection is immutable");
    }
    else {
      log.info("testCollectionProperty(): running add/remove test for collection " + fullPropName);

      // has boolean add methods with param of right type
      final String addMethodName = "add" + capitalizedSingularPropName;
      final Method addMethod = findAndCheckMethod(beanClass, addMethodName, ExistenceRequirement.REQUIRED);

      // has boolean remove methods with param of right type
      String removeMethodName = "remove" + capitalizedSingularPropName;
      final Method removeMethod = findAndCheckMethod(beanClass, removeMethodName, ExistenceRequirement.OPTIONAL);

      final Class propertyType = addMethod.getParameterTypes()[0];
      final Object testValue;
      // nasty special casing here to avoid DataModelViolationExceptions
      // TODO: consider implementing data model validation as AOP, so we can turn it off
      // for these tests
      if (propertyType.equals(ScreensaverUserRole.class)) {
        if (beanClass.isAssignableFrom(ScreeningRoomUser.class)) {
          testValue = ScreensaverUserRole.RNAI_SCREENING_ROOM_USER;
        }
        else {
          testValue = ScreensaverUserRole.BILLING_ADMIN;
        }
      }
      else {
        class TestValueBeanTester implements BeanTester
        {
          Object testValue;
          public void testBean(AbstractEntity bean)
          {
            testValue = getTestValueForType(propertyType);
            if (testValue instanceof AbstractEntity) {
              persistEntity((AbstractEntity) testValue, new HashSet<AbstractEntity>());
            }
          }
        }
        TestValueBeanTester testValueBeanTester = new TestValueBeanTester();
        doTestBean(bean, testValueBeanTester);
        testValue = testValueBeanTester.testValue;
      }

      // add the test value
      doTestBean(bean, new BeanTester()
      {
        public void testBean(AbstractEntity bean)
        {
          log.info("calling add method " + addMethodName + " on bean " + bean);
          try {
            Boolean result = (Boolean) addMethod.invoke(bean, testValue);
            assertTrue("adding to empty collection prop returns true: " + fullPropName,
                       result.booleanValue());
            persistEntity(bean, new HashSet<AbstractEntity>());
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

//            // TODO: isContained1 is false while others are true! At least for DerivativeScreenResult. Something to do with hashCode probably, since collection is a Set...
//            boolean isContained1 = false;
//            boolean isContained2 = false;
//            boolean isContained3 = false;
//            isContained1 = result.contains(testValue);
//            for (Object x : result) {
//              if (x.equals(testValue)) {
//                isContained2 = true;
//              }
//            }
//            isContained3 = new ArrayList(result).contains(testValue);
            Object copiedTestValue = testValue;
            if (copiedTestValue instanceof AbstractEntity) {
              copiedTestValue = genericEntityDao.findEntityById(
                (Class<? extends AbstractEntity>) copiedTestValue.getClass(),
                ((AbstractEntity) copiedTestValue).getEntityId());
            }
            assertTrue("collection prop with one element added has that element: " + fullPropName,
                       result.contains(copiedTestValue));
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
    //persistEntity(_bean, new HashSet<AbstractEntity>());
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        _bean = newInstance(_entityClass, true);
      }
    });
  }

  protected void persistEntity(final AbstractEntity entity, final Set<AbstractEntity> alreadyPersisted)
  {
    if (alreadyPersisted.contains(entity)) {
      return;
    }
    alreadyPersisted.add(entity);

    Class<? extends AbstractEntity> entityClass = entity.getClass();
    BeanInfo beanInfo = null;
    try {
      beanInfo = Introspector.getBeanInfo(entityClass);
    }
    catch (IntrospectionException e) {
      fail("got an introspection exception: " + e.getMessage());
    }

    // TODO: refactor copied code from #exercizePropertyDescriptors
    for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
      String propertyName = propertyDescriptor.getName();
      //String propFullName = entityClass.getSimpleName() + "." + propertyName;
      if (propertyName.equals("class")) {
        //log.debug("skipping \"class\" property " + propFullName);
        continue;
      }

      // skip what appears to be an entity's property, but that has been
      // explicitly annotated as a non-property
      if (propertyDescriptor.getReadMethod().isAnnotationPresent(Transient.class)) {
        //log.info("skipping @Transient property " + propFullName);
        continue;
      }

      Method getter = propertyDescriptor.getReadMethod();

      if (AbstractEntity.class.isAssignableFrom(getter.getReturnType())) {
        org.hibernate.annotations.Cascade cascade = getter.getAnnotation(Cascade.class);
        Set<CascadeType> cascadeTypes = new HashSet<CascadeType>();
        if (cascade != null) {
          CollectionUtils.addAll(cascadeTypes, cascade.value());
        }
        if (cascade == null || ! cascadeTypes.contains(CascadeType.SAVE_UPDATE)) {
          try {
            AbstractEntity relatedEntity = (AbstractEntity) getter.invoke(entity);
            if (relatedEntity != null) {
              persistEntity(relatedEntity, alreadyPersisted);
            }
          }
          catch (Exception e) {
            e.printStackTrace();
            fail("calling getter " + getter + " on " + entity + " caused exception: " + e);
          }
        }
      }

      if (Collection.class.isAssignableFrom(getter.getReturnType())) {
        org.hibernate.annotations.Cascade cascade = getter.getAnnotation(Cascade.class);
        Set<CascadeType> cascadeTypes = new HashSet<CascadeType>();
        if (cascade != null) {
          CollectionUtils.addAll(cascadeTypes, cascade.value());
        }
        if (cascade == null || ! cascadeTypes.contains(CascadeType.SAVE_UPDATE)) {
          try {
            Collection relatedCollection = (Collection) getter.invoke(entity);
            for (Object relatedCollectionMember : relatedCollection) {
              if (relatedCollectionMember instanceof AbstractEntity) {
                persistEntity((AbstractEntity) relatedCollectionMember, alreadyPersisted);
              }
            }
          }
          catch (Exception e) {
            e.printStackTrace();
            fail("calling getter " + getter + " on " + entity + " caused exception: " + e);
          }
        }
      }
    }

    genericEntityDao.persistEntity(entity);
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

    assertNotNull("related _bean " + relatedProperty.getBeanClass().getSimpleName() +
                  " has property with name " +
                  relatedProperty.getExpectedName() +
                  " for " + propFullName,
                  relatedProperty.getPropertyDescriptor());

    edu.harvard.med.screensaver.model.annotations.Column getterColumnAnnotation =
      propertyDescriptor.getReadMethod().getAnnotation(edu.harvard.med.screensaver.model.annotations.Column.class);
    if (getterColumnAnnotation != null && getterColumnAnnotation.hasNonconventionalSetterMethod()) {
      return;
    }

    if (isImmutableProperty(bean.getClass(), propertyDescriptor)) {

      if (isNullableImmutableProperty(bean, propertyDescriptor)) {
        return;
      }

      // no setter method on this side; this side's _bean is associated in this _bean's constructor (e.g. ScreenResult.screen)
      log.debug("testBidirectionalityOfOneSideOfRelationship(): " + propFullName +
                " is immutable: assuming related _bean is associated in this _bean's constructor");
      final AbstractEntity[] relatedBeanHolder = new AbstractEntity[1];
      // get the relatedBean; assert this _bean's getter method returns non-null
      doTestBean(bean, new BeanTester()
      {
        public void testBean(AbstractEntity bean)
        {
          try {
            AbstractEntity relatedBean = (AbstractEntity) propertyDescriptor.getReadMethod().invoke(bean);
            assertNotNull("this.getter() returns non-null related _bean of type " +
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

      // assert related _bean property returns/contains this _bean (depending upon otherSideIsMany)
      // TODO: the rest of this block duplicates the getter-testing code below
      doTestRelatedBeans(bean, relatedBeanHolder[0], new RelatedBeansTester()
      {
        public void testBeanWithRelatedBean(AbstractEntity bean,
                                            AbstractEntity relatedBean)
        {
          try {
            if (relatedProperty.otherSideIsToMany()) {
              assertTrue(
                "getter for property " + relatedProperty.getFullName() +
                " contains this _bean",
                ((Collection) relatedProperty.invokeGetter(relatedBean)).contains(bean));
            }
            else {
              Object beanFromRelatedGetter = relatedProperty.invokeGetter(relatedBean);
              assertEquals("related getter, " + relatedProperty.getFullName() + ", returns this _bean",
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
//    // TODO: seems to me that the branch below tests the cases where the related property
//    // is immutable perfectly well. but not quite ready to commit to that yet. will leave this
//    // code here and commented out until i feel better about it
//
//    else if (relatedProperty.relatedPropertyIsImmutable()) {
//      // no setter method on the other side; this _bean would be associated with related _bean in related _bean's constructor (e.g. Screen.screenResult)
//      log.info("testBidirectionalityOfOneSideOfRelationship(): " + propFullName +
//      " has foreign key constraint on related side: nothing to test from this side");
//    }
    else if (isContainedEntityProperty(bean.getClass(), propertyDescriptor)) {
      // TODO: figure out a test for this
    }
    else {
      final AbstractEntity relatedBean = (AbstractEntity) getTestValueForType(relatedProperty.getBeanClass());

      // invoke the setter on this side
      doTestBean(bean, new BeanTester()
      {
        public void testBean(AbstractEntity bean)
        {
          log.debug("testBidirectionalityOfOneSideOfRelationship(): " + propFullName +
                    ": related _bean will be added via setter on this _bean");
          Method setter = propertyDescriptor.getWriteMethod();
          assertNotNull("property has setter method: " + propFullName, setter);
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

  @SuppressWarnings("unchecked")
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

    // get basic objects related to the _bean
    Class<? extends AbstractEntity> beanClass = bean.getClass();
    String propertyName = propertyDescriptor.getName();
    final String propFullName = beanClass.getSimpleName() + "." + propertyName;

    // determine the add method of this _bean (that adds the related _bean)
    Method readMethod = propertyDescriptor.getReadMethod();

    final String singularPropName;
    ManyToMany manyToMany = readMethod.getAnnotation(ManyToMany.class);
    OneToMany oneToMany =  readMethod.getAnnotation(OneToMany.class);
    if (manyToMany != null && ! manyToMany.singularPropertyName().equals("")) {
      singularPropName = manyToMany.singularPropertyName();
    }
    else if (oneToMany != null && ! oneToMany.singularPropertyName().equals("")) {
      singularPropName = oneToMany.singularPropertyName();
    }
    else {
      singularPropName = propertyName.substring(0, propertyName.length() - 1);
    }

    final String addMethodName = "add" + StringUtils.capitalize(singularPropName);

    assertTrue("related _bean " + relatedProperty.getBeanClass().getSimpleName() +
               " has property with name " +
               relatedProperty.getExpectedName() +
               " for " + propFullName,
               relatedProperty.exists());

    Column column = readMethod.getAnnotation(Column.class);
    if (column != null && column.hasNonconventionalSetterMethod()) {
      log.info(
        "skipping testing bidirectionality of " + propFullName +
        " because this property has a non-conventional setter method");
      return;
    }

    final AbstractEntity parentOfRelatedBean;
    final Class relatedBeanClass = relatedProperty.getBeanClass();
    ContainedEntity containedEntity = (ContainedEntity) // TODO: shouldnt need this cast
      relatedBeanClass.getAnnotation(ContainedEntity.class);
    if (containedEntity != null) {
      // TODO - work out a test for containedEntity.alternateContainingEntityClass as well
      Class<? extends AbstractEntity> containingEntityClass = containedEntity.containingEntityClass();
      if (containingEntityClass.isAssignableFrom(bean.getClass())) {
        parentOfRelatedBean = bean;
      }
      else if (
        beanClass.equals(ScreenerCherryPick.class) &&
        containingEntityClass.equals(CherryPickRequest.class) &&
        relatedBeanClass.equals(LabCherryPick.class)) {
        parentOfRelatedBean = ((ScreenerCherryPick) bean).getCherryPickRequest();
      }
      else {
        parentOfRelatedBean = newInstance(containingEntityClass, true);
      }
    }
    else {
      parentOfRelatedBean = null;
    }
    AbstractEntity relatedBean = (AbstractEntity)
      getTestValueForType(relatedBeanClass, parentOfRelatedBean, true);

    // test the add method for the property, but only if related entity can
    // also exist independently
    if (relatedProperty.relatedPropertyIsImmutable()) {
      // no adder method on this side; beans are associated in related _bean's constructor

      if (isNullableImmutableProperty(relatedBean, relatedProperty.getPropertyDescriptor())) {
        return;
      }

      final AbstractEntity[] thisSideBeanHolder = new AbstractEntity[1];
      // assert this _bean's getter method returns the related _bean that was set via the constructor
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
            fail("related _bean's getter," + relatedProperty.getFullName() + ", threw exception: " + propFullName);
          }
        }
      });

      doTestRelatedBeans(thisSideBeanHolder[0], relatedBean, new RelatedBeansTester()
      {
        public void testBeanWithRelatedBean(AbstractEntity bean,
                                            AbstractEntity relatedBean)
        {
          try {
            assertTrue("related _bean with foreign key constraint added itself to this _bean " +
                       bean.getClass().getSimpleName(),
                       ((Collection) getter.invoke(bean)).contains(relatedBean));
          }
          catch (Exception e) {
            e.printStackTrace();
            fail("this _bean's collection getter threw exception: " + propFullName);
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

  public Object getTestValueForType(Class type)
  {
    return getTestValueForType(type, false);
  }

  public Object getTestValueForType(Class type, boolean persistEntities)
  {
    return getTestValueForType(type, null, persistEntities);
  }

  public Object getTestValueForType(Class type, AbstractEntity parentBean)
  {
    return getTestValueForType(type, parentBean, false);
  }

  @SuppressWarnings("unchecked")
  public Object getTestValueForType(Class type, AbstractEntity parentBean, boolean persistEntities)
  {
    if (type.equals(Integer.class)) {
      _integerTestValue += 1;
      _integerTestValue %= 16; // prevent well row from exploding
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
      return getStringTestValue();
    }
    if (type.equals(Date.class)) {
      _dateMilliseconds += 1000 * 60 * 60 * 24 * 1.32;
      return DateUtils.round(new Date(_dateMilliseconds), Calendar.DATE);
    }
    if (AbstractEntity.class.isAssignableFrom(type)) {
      if (parentBean == null) {
        return newInstance((Class<AbstractEntity>) type, persistEntities);
      }
      return newInstance((Class<AbstractEntity>) type, parentBean, persistEntities);
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
      return nextWellKey();
    }
    if (ReagentVendorIdentifier.class.isAssignableFrom(type)) {
      return new ReagentVendorIdentifier(getStringTestValue(), getStringTestValue());
    }
    throw new IllegalArgumentException(
      "can't create test values for type: " + type.getName());
  }

  private String getStringTestValue()
  {
    return STRING_TEST_VALUE_PREFIX + Integer.toString(++_stringTestValueIndex, STRING_TEST_VALUE_RADIX);
  }

  protected WellKey nextWellKey()
  {
    int col = _wellKeyTestValue.getColumn() + 1;
    int row = _wellKeyTestValue.getRow();
    int plateNumber = _wellKeyTestValue.getPlateNumber();
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
    _concreteStandinMap.put(ScreensaverUser.class, ScreeningRoomUser.class);
    _concreteStandinMap.put(Screening.class, LibraryScreening.class);
    _concreteStandinMap.put(ScreeningRoomActivity.class, LibraryScreening.class);
    _concreteStandinMap.put(Activity.class, LibraryScreening.class);
    _concreteStandinMap.put(AdministrativeActivity.class, WellVolumeCorrectionActivity.class);
    _concreteStandinMap.put(CherryPickRequest.class, RNAiCherryPickRequest.class);
  }

  private interface EntityFactory
  {
    AbstractEntity createEntity();
    AbstractEntity createEntity(AbstractEntity parentEntity, boolean persistEntities);
  }

  private Map<Class<? extends AbstractEntity>,EntityFactory> _entityFactoryMap =
    new HashMap<Class<? extends AbstractEntity>,EntityFactory>();
  {
    _entityFactoryMap.put(LabCherryPick.class, new EntityFactory()
    {
      private int testEntrezGeneId = 0;
      public AbstractEntity createEntity()
      {
        return createEntity(null, false);
      }
      public AbstractEntity createEntity(AbstractEntity parentEntity, boolean persistEntities)
      {
        CherryPickRequest cherryPickRequest =
          parentEntity != null && parentEntity instanceof CherryPickRequest ?
          (CherryPickRequest) parentEntity :
          (CherryPickRequest) getTestValueForType(CherryPickRequest.class);
        Well well = (Well) getTestValueForType(Well.class);
        well.setWellType(WellType.EXPERIMENTAL);
        if (cherryPickRequest.getScreen().getScreenType().equals(ScreenType.SMALL_MOLECULE)) {
          // TODO: persist compound when persistEntities is true
          well.addCompound(new Compound("CCC", "inchi"));
        }
        else if (cherryPickRequest.getScreen().getScreenType().equals(ScreenType.RNAI)) {
          // TODO: persist gene and silencing reagent when persistEntities is true
          Gene gene = new Gene("AAA", ++testEntrezGeneId, "entrezSymbol" + testEntrezGeneId, "Human");
          well.addSilencingReagent(gene.createSilencingReagent(SilencingReagentType.SIRNA, "ATCG"));
        }
        ScreenerCherryPick screenerCherryPick;
        if (cherryPickRequest.getScreenerCherryPicks().isEmpty()) {
          screenerCherryPick = (ScreenerCherryPick)
            newInstance(ScreenerCherryPick.class, cherryPickRequest, persistEntities);
        }
        else {
          screenerCherryPick = cherryPickRequest.getScreenerCherryPicks().iterator().next();
        }
        LabCherryPick labCherryPick = cherryPickRequest.createLabCherryPick(
          screenerCherryPick,
          well);
        if (persistEntities) {
          genericEntityDao.persistEntity(well);
          genericEntityDao.persistEntity(labCherryPick);
        }
        return labCherryPick;
      }
    });
  }

  protected <NE extends AbstractEntity> NE newInstance(Class<NE> entityClass)
  {
    return newInstance(entityClass, false);
  }

  protected <NE extends AbstractEntity> NE newInstance(
    Class<NE> entityClass,
    boolean persistEntities)
  {
    AbstractEntity parentBean = null;
    // TODO: arrange a test for ContainedEntity.alternateContainingEntityClass as well
    ContainedEntity containedEntity = entityClass.getAnnotation(ContainedEntity.class);
    if (containedEntity != null) {
      Class<? extends AbstractEntity> parentClass = containedEntity.containingEntityClass();
      parentBean = newInstance(parentClass, persistEntities);
    }
    return newInstance(entityClass, parentBean, persistEntities);
  }

  protected <NE extends AbstractEntity> NE newInstance(
    final Class<NE> entityClass,
    final AbstractEntity parentBean,
    final boolean persistEntities)
  {
    if (! persistEntities) {
      return newInstance0(entityClass, parentBean, persistEntities);
    }
    class NewInstanceDAOTransaction implements DAOTransaction
    {
      NE newInstance;
      public void runTransaction()
      {
        newInstance = newInstance0(entityClass, parentBean, persistEntities);
      }
    }
    NewInstanceDAOTransaction newInstanceDAOTransaction = new NewInstanceDAOTransaction();
    genericEntityDao.doInTransaction(newInstanceDAOTransaction);
    return newInstanceDAOTransaction.newInstance;
  }

  protected <NE extends AbstractEntity> NE newInstance0(
    Class<NE> entityClass,
    AbstractEntity parentBean,
    boolean persistEntities)
  {
    if (Modifier.isAbstract(entityClass.getModifiers())) {
      // TODO: work out the type-safety crap below
      Class<? extends NE> concreteStandin = (Class<? extends NE>)
        _concreteStandinMap.get(entityClass);
      if (concreteStandin == null) {
        fail("missing concrete stand-in class for abstract class " + entityClass.getName() + " in _concreteStandinMap; please update!");
      }
      return newInstance(concreteStandin, persistEntities);
    }

    EntityFactory entityFactory = _entityFactoryMap.get(entityClass);
    if (entityFactory != null) {
      // TODO: work out the type-safety crap below
      return (NE) entityFactory.createEntity(parentBean, persistEntities);
    }
    ContainedEntity containedEntity = (ContainedEntity) // TODO: why do i need this cast??
      entityClass.getAnnotation(ContainedEntity.class);
    if (containedEntity != null) {
      Class<? extends AbstractEntity> parentBeanClass = containedEntity.containingEntityClass();
      if (parentBean == null) {
        parentBean = newInstance(parentBeanClass, persistEntities);
      }
      else {
        assertTrue(
          "containing entity class is assignable from parent bean class",
          parentBeanClass.isAssignableFrom(parentBean.getClass()));
      }
    }
    else {
      assertNull("no parent bean for a non-ContainedEntity", parentBean);
    }
    if (parentBean != null) {
      Method factoryMethod = getFactoryMethod(entityClass, parentBean.getClass());
      if (factoryMethod != null) {
        Object [] arguments = getArgumentsForFactoryMethod(factoryMethod, persistEntities);
        try {
          // TODO: work out the type-safety crap below
          NE newEntity = (NE) factoryMethod.invoke(parentBean, arguments);
          if (persistEntities) {
            genericEntityDao.persistEntity(newEntity);
          }
          return newEntity;
        }
        catch (Exception e) {
          e.printStackTrace();
          fail("invoke for " + parentBean.getClass() +  "." + factoryMethod.getName() +
            " threw an Exception: " + e);
        }
      }
    }

    try {
      Constructor constructor = getMaxArgConstructor(entityClass);
      assertNotNull("has public constructor: " + entityClass, constructor);
      Object[] arguments = getArgumentsForConstructor(constructor, parentBean, persistEntities);
      // TODO: work out the type-safety crap below
      NE newEntity = (NE) constructor.newInstance(arguments);
      if (persistEntities) {
        genericEntityDao.persistEntity(newEntity);
      }
      return newEntity;
    }
    catch (Exception e) {
      e.printStackTrace();
      fail("newInstance for " + entityClass + " threw an Exception: " + e);
    }
    return null;
  }

  protected Object[] getArgumentsForConstructor(
    Constructor constructor,
    AbstractEntity parentBean,
    boolean persistEntities)
  {
    Class[] parameterTypes = constructor.getParameterTypes();
    Object[] arguments = getArgumentsForParameterTypes(parameterTypes, parentBean, persistEntities);
    return arguments;
  }

  protected Object[] getArgumentsForFactoryMethod(Method method, boolean persistEntities)
  {
    Class[] parameterTypes = method.getParameterTypes();
    if (method.getName().equals("createWell") &&
      parameterTypes.length == 3 &&
      parameterTypes[0].equals(Integer.class) &&
      parameterTypes[1].equals(String.class) &&
      parameterTypes[2].equals(WellType.class)) {
      WellKey wellKey = nextWellKey();
      return new Object [] {
        wellKey.getPlateNumber(),
        wellKey.getWellName(),
        WellType.EXPERIMENTAL
      };
    }
    else if (method.getName().equals("createAnnotationValue") &&
      parameterTypes.length == 2 &&
      parameterTypes[0].equals(ReagentVendorIdentifier.class) &&
      parameterTypes[1].equals(String.class)) {
      parameterTypes[1] = BigDecimal.class;
      Object[] arguments = getArgumentsForParameterTypes(parameterTypes, null, persistEntities);
      arguments[1] = arguments[1].toString();
      return arguments;
    }

    Object[] arguments = getArgumentsForParameterTypes(parameterTypes, null, persistEntities);
    return arguments;
  }

  private Object[] getArgumentsForParameterTypes(
    Class[] parameterTypes,
    AbstractEntity parentBean)
  {
    return getArgumentsForParameterTypes(parameterTypes, parentBean, false);
  }

  protected Object[] getArgumentsForParameterTypes(
    Class[] parameterTypes,
    AbstractEntity parentBean,
    boolean persistEntities) {
    Object [] arguments = new Object[parameterTypes.length];
    for (int i = 0; i < arguments.length; i++) {
      Class parameterType = parameterTypes[i];
      if (parentBean != null && parameterType.isAssignableFrom(parentBean.getClass())) {
        arguments[i] = parentBean;
      }
      else {
        arguments[i] = getTestValueForType(parameterType, persistEntities);
      }
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

  protected Method getFactoryMethod(
    Class<? extends AbstractEntity> entityClass,
    Class<? extends AbstractEntity> parentClass)
  {
    String factoryMethodName = "create" + entityClass.getSimpleName();
    List<Method> candidateFactoryMethods = new ArrayList<Method>();
    for (Method method : parentClass.getMethods()) {
      if (method.getName().equals(factoryMethodName)) {
        candidateFactoryMethods.add(method);
      }
    }

    Method bestCandidate = null;
    int bestCandidateArgCount = -1;
    for (Method candidate : candidateFactoryMethods) {
      int candidateArgCount = candidate.getParameterTypes().length;
      if (candidateArgCount > bestCandidateArgCount) {
        bestCandidate = candidate;
        bestCandidateArgCount = candidateArgCount;
      }
    }
    return bestCandidate;
  }

  protected interface PropertyDescriptorExercizor
  {
    public void exercizePropertyDescriptor(
      AbstractEntity bean,
      BeanInfo beanInfo,
      PropertyDescriptor propertyDescriptor);
  }

  protected void exercizePropertyDescriptors(final PropertyDescriptorExercizor exercizor)
  {
    for (PropertyDescriptor propertyDescriptor : _beanInfo.getPropertyDescriptors()) {
      String propertyName = propertyDescriptor.getName();
      String propFullName = _bean.getClass().getSimpleName() + "." + propertyName;
      if (propertyName.equals("class")) {
        log.debug("skipping \"class\" property " + propFullName);
        continue;
      }

      // skip what appears to be an entity's property, but that has been
      // explicitly annotated as a non-property
      if (propertyDescriptor.getReadMethod().isAnnotationPresent(Transient.class)) {
        log.info("skipping @Transient property " + propFullName);
        continue;
      }

      log.info("excercizing JavaBean entity property " + _bean.getClass().getSimpleName() + "." + propertyName +
               " with " + exercizor.getClass().getEnclosingMethod().getName());
      exercizor.exercizePropertyDescriptor(_bean, _beanInfo, propertyDescriptor);
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
   * include: getEntityId(), getFooId() (for _bean of type Foo), and any
   * properties that may be used to define the ID (for cases where the entity ID
   * is not an auto-generated database ID, but instead correspdonds to the
   * entity's business key).
   *
   * @param _beanInfo the _bean the property belongs to
   * @param propertyDescriptor the property
   * @return true iff property is "entityId" or the property that is named the
   *         same as the entity, but with an "Id" suffix; otherwise false
   */
  @SuppressWarnings("unchecked")
  private boolean isEntityIdProperty(Class<? extends AbstractEntity> beanClass,
                                     PropertyDescriptor propertyDescriptor)
  {
    // legacy logic for finding the standard entity-ID related methods below...
    if (propertyDescriptor.getName().equals("entityId")) {
      log.debug("isEntityIdProperty(): property participates in defining entity ID: " + propertyDescriptor.getName());
      return true;
    }

    // Check whether property corresponds to the _bean's Hibernate ID method, which is named similarly to the _bean.
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
    CollectionOfElements collectionOfElements = getter.getAnnotation(CollectionOfElements.class);
    if (collectionOfElements != null) {
      return collectionOfElements.initialCardinality();
    }
    return 0;
  }

  protected boolean isUnidirectionalRelationship(Class<? extends AbstractEntity> beanClass,
                                                 PropertyDescriptor propertyDescriptor)
  {
    try {
      return isUnidirectionalRelationshipMethod(propertyDescriptor.getReadMethod());
    }
    catch (SecurityException e) {
      throw e;
    }
  }

  protected boolean setterMethodNotExpected(Class<? extends AbstractEntity> beanClass, PropertyDescriptor propertyDescriptor)
  {
    String propFullName = beanClass.getSimpleName() + "." + propertyDescriptor.getName();

    if (isTransientProperty(beanClass, propertyDescriptor)) {
      log.info("setter method not expected for transient property: " + propFullName);
      return true;
    }

    if (isImmutableProperty(beanClass, propertyDescriptor)) {
      log.info("setter method not expected for immutable property: " + propFullName);
      return true;
    }

    if (isContainedEntityProperty(beanClass, propertyDescriptor)) {
      return true;
    }

    // no setter expected if property participates in defining the entity ID
    if (isEntityIdProperty(beanClass, propertyDescriptor)) {
      log.info("setter method not expected for property that participates in defining the entity ID: " +
               propFullName);
      return true;
    }

    return false;
  }

  @SuppressWarnings("unchecked")
  private boolean isContainedEntityProperty(Class<? extends AbstractEntity> beanClass, PropertyDescriptor propertyDescriptor) {
    if (AbstractEntity.class.isAssignableFrom(propertyDescriptor.getPropertyType())) {
      Class propertyType = (Class) propertyDescriptor.getPropertyType();
      ContainedEntity containedEntity = (ContainedEntity) // TODO: why do i need this cast??
        propertyType.getAnnotation(ContainedEntity.class);
      if (containedEntity != null &&
        beanClass.isAssignableFrom(containedEntity.containingEntityClass())) {
        return true;
      }
    }
    return false;
  }

  protected boolean isTransientProperty(Class<? extends AbstractEntity> beanClass, PropertyDescriptor propertyDescriptor)
  {
    Method getter = propertyDescriptor.getReadMethod();
    if (getter.isAnnotationPresent(Transient.class)) {
      return true;
    }
    Column column = getter.getAnnotation(Column.class);
    if (column != null && column.hasNonconventionalSetterMethod()) {
      return true;
    }
    return false;
  }

  protected boolean isImmutableProperty(Class<? extends AbstractEntity> beanClass, PropertyDescriptor propertyDescriptor)
  {
    Method getter = propertyDescriptor.getReadMethod();
    if (getter.isAnnotationPresent(org.hibernate.annotations.Immutable.class)) {
      return true;
    }
    return false;
  }

  private boolean isUnidirectionalRelationshipMethod(Method getter)
  {
    ManyToOne manyToOne = getter.getAnnotation(ManyToOne.class);
    if (manyToOne != null && manyToOne.unidirectional()) {
      return true;
    }
    return false;
  }


//  protected void testBean(AbstractEntity _bean, BeanTester tester)
//  {
//    tester.testBean(_bean);
//  }
//
//  protected void testRelatedBeans(AbstractEntity _bean,
//                                  AbstractEntity relatedBean,
//                                  RelatedBeansTester tester)
//  {
//    tester.testBeanWithRelatedBean(_bean, relatedBean);
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
    genericEntityDao.doInTransaction(new DAOTransaction()
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
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        AbstractEntity localBean = getPersistedEntity(bean);
        AbstractEntity localRelatedBean = getPersistedEntity(relatedBean);
        tester.testBeanWithRelatedBean(localBean, localRelatedBean);
      }
    });
  }

  // if the _bean has already been persisted, then get the persisted copy, as the current
  // copy is stale. if it has not, persist it now so we can get the entityId
  private AbstractEntity getPersistedEntity(AbstractEntity bean)
  {
    AbstractEntity beanFromHibernate = null;
    if (bean.getEntityId() != null) {
      beanFromHibernate = (AbstractEntity) hibernateTemplate.get(bean.getClass(), bean.getEntityId());
    }
    if (beanFromHibernate == null) {
      persistEntity(bean, new HashSet<AbstractEntity>());
      beanFromHibernate = (AbstractEntity) hibernateTemplate.get(bean.getClass(), bean.getEntityId());
    }
    return beanFromHibernate;
  }
}
