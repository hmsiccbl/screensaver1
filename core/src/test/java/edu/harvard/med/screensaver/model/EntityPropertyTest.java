// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Collection;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.annotations.ElementCollection;
import edu.harvard.med.screensaver.model.annotations.ToMany;
import edu.harvard.med.screensaver.test.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.test.model.meta.DomainModelDefinitionException;
import edu.harvard.med.screensaver.test.model.meta.ExistenceRequirement;
import edu.harvard.med.screensaver.test.model.meta.ModelIntrospectionUtil;
import edu.harvard.med.screensaver.test.model.meta.RelatedProperty;
import edu.harvard.med.screensaver.util.StringUtils;

public class EntityPropertyTest<E extends AbstractEntity> extends AbstractSpringPersistenceTest
{
  private static Logger log = Logger.getLogger(EntityPropertyTest.class);
  
  private Class<E> _entityClass;
  private PropertyDescriptor propertyDescriptor;
  private E entity;
  private String testName;

  public EntityPropertyTest(String name)
  {
    super(name);
  }

  public EntityPropertyTest(Class<E> entityClass, PropertyDescriptor propertyDescriptor)
  {
    super("testEntityProperty"); // unfortunately, the test name has to match the method name that implements the test, in order to work with Spring's unit test runner
    testName = getName() + ":" + propertyDescriptor.getDisplayName();
    _entityClass = entityClass;
    this.propertyDescriptor = propertyDescriptor;
  }
  
  @Override
  protected void setUp() throws Exception
  {
    //super.setup();
  }

  private E initTestEntity()
  {
    schemaUtil.truncateTables();
    entity = dataFactory.newInstance(_entityClass, testName);
    return entity;
  }

  protected void doTestBeanInTransaction(final AbstractEntity bean,
                                         final BeanTester tester)
  {
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        AbstractEntity localBean = getPersistedEntity(bean);
        tester.testEntity(localBean);
      }
    });
  }

  /**
   * Test two entity instances together. Ensures that both entities are managed
   * in the same Hibernate session. In particular, this allows
   * entity1.equals(entity2) to work properly, since both must be reloaded if
   * they were just instantiated, in order to for equals() and hashCode() to
   * work as expected.
   * 
   * @param bean
   * @param relatedBean
   * @param tester
   */
  private void doTestRelatedBeansInTransaction(final AbstractEntity bean,
                                               final AbstractEntity relatedBean,
                                               final RelatedBeansTester tester)
  {
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        AbstractEntity localBean = getPersistedEntity(bean);
        AbstractEntity localRelatedBean = getPersistedEntity(relatedBean);
        tester.testRelatedEntities(localBean, localRelatedBean);
      }
    });
  }

  //@IfProfileValue(name = "never.to.be.run.explicitly", value = "never.to.be.run.explicitly")
  public void testEntityProperty() throws Throwable
  {
  	if(propertyDescriptor == null)
  	{
  		log.error(new Exception("propertyDescriptor was null here"));
  		return;  // TODO: this implies that this test method is being run explicitly, so skip -sde4
  	}
    log.info("testing entity property " + fullPropName(propertyDescriptor));

    Method getter = propertyDescriptor.getReadMethod();

    if (ModelIntrospectionUtil.isCollectionOfElements(propertyDescriptor)) {
      doTestCollectionOfElements(_entityClass, propertyDescriptor);
    }
    else if (ModelIntrospectionUtil.isToManyEntityRelationship(propertyDescriptor)) { 
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
      if (getter.getDeclaringClass().equals(_entityClass)) {
        doTestToManyRelationship(_entityClass, propertyDescriptor);
      }
    }
    else if (ModelIntrospectionUtil.isToOneEntityRelationship(propertyDescriptor)) {
      doTestToOneRelationship(_entityClass, propertyDescriptor);
    }
    else if (ModelIntrospectionUtil.isEntityIdProperty(_entityClass, propertyDescriptor)) {
    }
    else if (ModelIntrospectionUtil.isImmutableProperty(_entityClass, propertyDescriptor)) {
      doTestImmutableProperty(propertyDescriptor);
    }
    else {
      doTestMutableProperty(propertyDescriptor);
    }
  }
  
  /**
   * If a property is immutable, test that it is set via constructor (simply by
   * testing that the getter does not return null), and that is has no public setter
   * method.
   */
  private void doTestImmutableProperty(final PropertyDescriptor propertyDescriptor)
  {
    assertNull("immutable property " + fullPropName(propertyDescriptor) + " should not have a public setter method",
               propertyDescriptor.getWriteMethod());
    
    if (ModelIntrospectionUtil.isPropertyWithNonconventionalSetterMethod(propertyDescriptor)) {
      log.warn("skipping testing setter of immutable property " + fullPropName(propertyDescriptor) + ": has non-conventional setter method");
      return;
    }
    
    try {
      initTestEntity();
      final Object originalValue = propertyDescriptor.getReadMethod().invoke(entity);
      doTestBeanInTransaction(entity, new BeanTester()
      {
        public void testEntity(AbstractEntity bean) {
          try {
            Object getterValue = propertyDescriptor.getReadMethod().invoke(bean);
            assertNotNull("constructor sets value for immutable property; " + fullPropName(propertyDescriptor), getterValue);
            assertPropertyValuesEquals("getter for immutable property " + fullPropName(propertyDescriptor) +
                                       " returns value specified in constructor",
                                       originalValue,
                                       getterValue);
          }
          catch (Exception e) {
            e.printStackTrace();
            fail("this.getter() threw exception: " + fullPropName(propertyDescriptor));
          }
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
      fail("this.getter() threw exception: " + fullPropName(propertyDescriptor));
    }
  }

  /**
   * Test that a call to the getter for a property returns the same thing
   * that the property was just set to with the setter.
   */
  private void doTestMutableProperty(final PropertyDescriptor propertyDescriptor)
  {
    final Method getter = propertyDescriptor.getReadMethod();
    final Method setter = propertyDescriptor.getWriteMethod();
    
    if (ModelIntrospectionUtil.isPropertyWithNonconventionalSetterMethod(propertyDescriptor)) {
      log.warn("skipping testing setter of mutable property " + fullPropName(propertyDescriptor) + ": has non-conventional setter method");
      return;
    }
    if (ModelIntrospectionUtil.isDerivedProperty(propertyDescriptor)) {
      log.warn("skipping testing setter of derived property " + fullPropName(propertyDescriptor));
      return;
    }

    assertNotNull("setter exists for property " + propertyDescriptor.getName(), setter);

    initTestEntity();
    Object testValue;
    int attemptsToFindDifferingTestValue = 2;
    do {
      testValue = dataFactory.newInstance(getter.getReturnType(), testName);
      try {
        Object existingValue = getter.invoke(entity);
        // test would be inconsequential if existing property value is same as new value...find another value!
        if (!testValue.equals(existingValue)) {
          break;
        }
      }
      catch (Exception e) {
        throw new DomainModelDefinitionException(e);
      }
    } while (--attemptsToFindDifferingTestValue > 0);

    // call the setter
    assert !(testValue instanceof AbstractEntity) : "doTestMutableProperty() should only be called for non-relationship properties (non-entity types); see doTestToOneRelationship()";
    log.info("calling setter " + setter + " on bean " + entity + " with test value " + testValue);
    final Object finalTestValue = testValue; 
    doTestBeanInTransaction(entity, new BeanTester() {
      public void testEntity(AbstractEntity bean) { 
        try {
          setter.invoke(bean, finalTestValue); 
        }
        catch (Exception e) {
          throw new DomainModelDefinitionException(e);
        }
      }
    });

    // call the getter
    doTestBeanInTransaction(entity, new BeanTester() {
      public void testEntity(AbstractEntity bean) {       
        Object actualValue;
        try {
          actualValue = getter.invoke(bean);
        }
        catch (Exception e) {
          throw new DomainModelDefinitionException(e);
        }
        assertPropertyValuesEquals("getter returns what setter set for " +
                                   fullPropName(propertyDescriptor),
                                   finalTestValue,
                                   actualValue);
      }
    });
  }
  

  /**
   * Test collection property:
   * <ul>
   * <li>has boolean add/remove methods with param of right type
   * <li>add;get returns set of one
   * <li>add;remove;get returns empty set
   */
  private void doTestCollectionOfElements(Class<? extends AbstractEntity> beanClass,
                                          final PropertyDescriptor propertyDescriptor) 
  {
    String propertyName = propertyDescriptor.getName();
    
    if (ModelIntrospectionUtil.isCollectionWithNonConventionalMutation(propertyDescriptor)) {
      log.warn("skipping add/remove test for collection of elements " + fullPropName(propertyDescriptor) + ": non-conventional mutation");
      return;
    }

    if (ModelIntrospectionUtil.isImmutableProperty(beanClass, propertyDescriptor)) {
      // can't easily test immutable collections of elements; 
      // if set via-constructor we could easily test this case, but we never actually do this;
      // otherwise, we would have to populate the collection before bean is persisted
      fail("sorry, testing of immutable collection of elements is not yet supported " +
      		"(hint: annotate property getter with @ElementCollection(hasNonconventionalMutation=true) to avoid test failure)");
    }
    if (ModelIntrospectionUtil.isMapBasedProperty(propertyDescriptor)) {
      fail("sorry, testing of map-based collection of elements is not yet supported " +
           "(hint: annotate property getter with @ElementCollection(hasNonconventionalMutation=true) to avoid test failure)");
    }
    

    ElementCollection elementCollection = propertyDescriptor.getReadMethod().getAnnotation(ElementCollection.class);
    String singularPropertyName = null;
    if (elementCollection != null && elementCollection.singularPropertyName().length() > 0) {
      singularPropertyName = elementCollection.singularPropertyName(); 
    }
    else if (propertyName.endsWith("s")) {
      singularPropertyName = propertyName.substring(0, propertyName.length() - 1);
    }
    else {
      fail("could not determine add/remove methods for collection of elements: " + fullPropName(propertyDescriptor));
    }
    final String addMethodName = "add" + StringUtils.capitalize(singularPropertyName); 
    final String removeMethodName = "remove" + StringUtils.capitalize(singularPropertyName);
    final Method getterMethod = propertyDescriptor.getReadMethod();
    final Method addMethod = ModelIntrospectionUtil.findAndCheckMethod(beanClass, addMethodName, ExistenceRequirement.OPTIONAL, ModelIntrospectionUtil.getCollectionOfElementsType(propertyDescriptor));
    final Method removeMethod = ModelIntrospectionUtil.findAndCheckMethod(beanClass, 
                                                                          removeMethodName, 
                                                                          addMethod == null ? ExistenceRequirement.NOT_ALLOWED : ExistenceRequirement.OPTIONAL);

    log.info("running add/remove test for collection of elements: " + fullPropName(propertyDescriptor));
    initTestEntity();
    Object testElement = dataFactory.newInstance(ModelIntrospectionUtil.getCollectionOfElementsType(propertyDescriptor), testName);
    
    
    class CollectionElementAdder implements BeanTester
    {
      Object testElement;
      
      CollectionElementAdder(Object testElement) { this.testElement = testElement; }
      
      public void testEntity(AbstractEntity bean)
      {
        try {
          boolean addResult; 
          if (addMethod == null) {
            log.debug("calling collection.add(" + testElement + ") on bean " + bean);
            Collection<Object> collection = (Collection<Object>) getterMethod.invoke(bean);
            addResult = collection.add(testElement);
          }
          else {
            log.debug("calling " + bean + ".add(" + testElement + ")");
            addResult = (Boolean) addMethod.invoke(bean, testElement);
          }
          assertTrue("adding to empty collection returns true: " + fullPropName(propertyDescriptor), addResult);
        }
        catch (Exception e) {
          e.printStackTrace();
          fail("add method for collection threw exception: " + fullPropName(propertyDescriptor) + ": " + e);
        }
      }
    };
    
    class CollectionElementRemover implements BeanTester
    {
      Object testElement;
      
      CollectionElementRemover(Object testElement) { this.testElement = testElement; }
      
      public void testEntity(AbstractEntity bean)
      {
        try {
          boolean removeResult; 
          if (addMethod == null) {
            log.debug("calling collection.add(" + testElement + ") on bean " + bean);
            Collection<Object> collection = (Collection<Object>) getterMethod.invoke(bean);
            removeResult = collection.remove(testElement);
          }
          else {
            log.debug("calling " + bean + ".remove(" + testElement + ")");
            removeResult = (Boolean) removeMethod.invoke(bean, testElement);
          }
          assertTrue("removing to empty collection returns true: " + fullPropName(propertyDescriptor), removeResult);
        }
        catch (Exception e) {
          e.printStackTrace();
          fail("remove method for collection threw exception: " + fullPropName(propertyDescriptor) + ": " + e);
        }
      }
    };
    
    class CollectionAsserter implements BeanTester
    {
      int size;
      Object testElement;
      boolean containsElement;
      
      CollectionAsserter(int size, Object testElement, boolean containsElement)
      {
        this.size = size;
        this.testElement = testElement;
        this.containsElement = containsElement;
      }

      public void testEntity(AbstractEntity bean)
      {
        try {
          Collection result = (Collection) getterMethod.invoke(bean);
          assertEquals("collection " + fullPropName(propertyDescriptor) + " has size " + size,
                       size, 
                       result.size());

          
          if (containsElement) {
            assertTrue("collection contains element: " + fullPropName(propertyDescriptor),
                       result.contains(testElement));
          }
          else {
            assertFalse("collection does not contain element: " + fullPropName(propertyDescriptor),
                        result.contains(testElement));
          }
        }
        catch (Exception e) {
          e.printStackTrace();
          fail("getter method for collection threw exception: " + fullPropName(propertyDescriptor) + ": " + e);
        }
      }
    };

    doTestBeanInTransaction(entity, new CollectionElementAdder(testElement));
    doTestBeanInTransaction(entity, new CollectionAsserter(1, testElement, true));
    if (removeMethod != null) {
      doTestBeanInTransaction(entity, new CollectionElementRemover(testElement));
      doTestBeanInTransaction(entity, new CollectionAsserter(0, testElement, false));
    }
  }

  private void doTestToOneRelationship(Class<? extends AbstractEntity> beanClass,
                                       final PropertyDescriptor propertyDescriptor) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
                                       
  {
    log.info("testing to-one entity relationship " + fullPropName(propertyDescriptor));

    if (ModelIntrospectionUtil.isToOneRelationshipWithNonConventionalSetter(propertyDescriptor)) {
      log.warn("skipping test for to-one entity relationship " + fullPropName(propertyDescriptor) +
        ": non-conventional mutation");
      return;
    }

    final Method getterMethod = propertyDescriptor.getReadMethod();
    final Method setterMethod = propertyDescriptor.getWriteMethod();
    
    if (ModelIntrospectionUtil.isImmutableProperty(beanClass, propertyDescriptor)) {
      assertNull("immutable to-one entity relationship property has no setter: " + fullPropName(propertyDescriptor), setterMethod);
    }
    // TODO: what if non-nullable? setter okay, but must be set in constructor too?
    
    final AbstractEntity entity = initTestEntity();
    final Class<? extends AbstractEntity> relatedEntityType = ModelIntrospectionUtil.getRelationshipEntityType(propertyDescriptor);
    AbstractEntity relatedEntity = null;
    RelatedProperty relatedProperty = new RelatedProperty(beanClass, propertyDescriptor);
    if (ModelIntrospectionUtil.isToOneRelationshipRequired(propertyDescriptor)) {
      // do nothing here, test entity should already have been instantiated with related entity;
      // note: this getter is pulling from in-memory value, while below we are asserting that it matches the persisted value 
      relatedEntity = (AbstractEntity) getterMethod.invoke(entity);
    }
    else if (relatedProperty.getPropertyDescriptor() != null && 
      ModelIntrospectionUtil.isToOneRelationshipRequired(relatedProperty.getPropertyDescriptor())) {
      log.info("skipping set/get test for one-to-one entity relationship since relationship is required on the related side, and it will be tested using the related entity type");
      return;
    }
    else if (!!!ModelIntrospectionUtil.isImmutableProperty(beanClass, propertyDescriptor)) {
      relatedEntity = dataFactory.newInstance(relatedEntityType, testName);
      setterMethod.invoke(entity, relatedEntity);
      genericEntityDao.mergeEntity(entity);
    }
    else {
      fail("sorry, testing of nullable, immutable, {one,many}-to-one relationships not implemented " +
           "(hint: annotate property getter with @ToOne(hasNonconventionalSetterMethod=true) to avoid test failure)");
    }

    class ToOneAsserter implements RelatedBeansTester
    {
      public void testRelatedEntities(AbstractEntity entity,
                                      AbstractEntity expectedRelatedEntity)
      {
        try {
          AbstractEntity actualRelatedEntity = (AbstractEntity) getterMethod.invoke(entity);
          assertEquals("to-one entity relationship is set to related entity: " + fullPropName(propertyDescriptor),
                       expectedRelatedEntity,
                       actualRelatedEntity);
        }
        catch (Exception e) {
          e.printStackTrace();
          fail("getter method for to-one entity relationship threw exception: " + fullPropName(propertyDescriptor) + ": " + e);
        }
      }
    };
    doTestRelatedBeansInTransaction(entity, relatedEntity, new ToOneAsserter());
  }

  private void doTestToManyRelationship(Class<? extends AbstractEntity> beanClass, 
                                        final PropertyDescriptor propertyDescriptor)
  {
    log.info("testing to-many entity relationship " + fullPropName(propertyDescriptor));

    String propertyName = propertyDescriptor.getName();
    
    assertNull("collection property has no setter: " + fullPropName(propertyDescriptor), propertyDescriptor.getWriteMethod());
    
    if (ModelIntrospectionUtil.isToManyRelationshipWithNonConventionalMutation(propertyDescriptor)) {
      log.warn("skipping add/remove test for to-many entity relationship " + fullPropName(propertyDescriptor) + ": non-conventional mutation");
      return;
    }

    if (ModelIntrospectionUtil.isImmutableProperty(beanClass, propertyDescriptor)) {
      // can't easily test immutable to-many relationship collections; 
      // if set via-constructor we could easily test this case, but we never actually do this;
      // otherwise, we would have to populate the relationship collection before bean is persisted
      fail("sorry, testing of immutable to-many entity relationship is not yet supported " +
           "(hint: annotate property getter with @ToMany(hasNonconventionalMutation=true) to avoid test failure)");
    }
    if (ModelIntrospectionUtil.isMapBasedProperty(propertyDescriptor)) {
      fail("sorry, testing of map-based to-many entity relationships is not yet supported" + 
           "(hint: annotate property getter with @ToMany(hasNonconventionalMutation=true) to avoid test failure)");
    }
    
    final Method getterMethod = propertyDescriptor.getReadMethod();
    
    ToMany toMany = getterMethod.getAnnotation(ToMany.class);
    String singularPropertyName = null;
    if (toMany != null && toMany.singularPropertyName().length() > 0) {
      singularPropertyName = toMany.singularPropertyName(); 
    }
    else if (propertyName.endsWith("s")) {
      singularPropertyName = propertyName.substring(0, propertyName.length() - 1);
    }
    else {
      fail("could not determine add/remove methods for to-many entity relationship: " + fullPropName(propertyDescriptor));
    }
    final String addMethodName = "add" + StringUtils.capitalize(singularPropertyName); 
    final String removeMethodName = "remove" + StringUtils.capitalize(singularPropertyName);
    
    final AbstractEntity entity = initTestEntity();
    final Class<? extends AbstractEntity> relatedEntityType = ModelIntrospectionUtil.getRelationshipEntityType(propertyDescriptor);
    AbstractEntity relatedEntity;
    if (ModelIntrospectionUtil.isOneToManyRelationshipRequired(beanClass, propertyDescriptor)) {
      ModelIntrospectionUtil.findAndCheckMethod(beanClass, addMethodName, ExistenceRequirement.NOT_ALLOWED);
      ModelIntrospectionUtil.findAndCheckMethod(beanClass, removeMethodName, ExistenceRequirement.NOT_ALLOWED);
      log.info("skipping add/remove test for one-to-many entity relationship since relationship is required on the related side, and it will be tested using the related entity type");
      return;
    }
    else {
      relatedEntity = dataFactory.newInstance(relatedEntityType, testName);
      final Method addMethod = ModelIntrospectionUtil.findAndCheckMethod(beanClass, addMethodName, ExistenceRequirement.REQUIRED, relatedEntityType);
      /*final Method removeMethod = */ModelIntrospectionUtil.findAndCheckMethod(beanClass, removeMethodName, 
                                                                            addMethod == null ? ExistenceRequirement.NOT_ALLOWED : ExistenceRequirement.OPTIONAL);
      class ToManyAdder implements RelatedBeansTester
      {
        public void testRelatedEntities(AbstractEntity entity,
                                        AbstractEntity relatedEntity)
        {
          try {
            boolean addResult;
            log.debug("calling " + entity + ".add(" + relatedEntity + ")");
            addResult = (Boolean) addMethod.invoke(entity, relatedEntity);
            assertTrue("adding to to-many entity relationship returns true: " + fullPropName(propertyDescriptor), addResult);
          }
          catch (Exception e) {
            e.printStackTrace();
            fail("add method for to-many entity relationship threw exception: " + fullPropName(propertyDescriptor) + ": " + e);
          }
        }
      };
      doTestRelatedBeansInTransaction(entity, relatedEntity, new ToManyAdder());
    }
    
    class ToManyAsserter implements RelatedBeansTester
    {
      int size;
      boolean isElementExpected;
      
      ToManyAsserter(int size, boolean isElementExpected)
      {
        this.size = size;
        this.isElementExpected = isElementExpected;
      }

      public void testRelatedEntities(AbstractEntity entity,
                                          AbstractEntity relatedEntity)
      {
        try {
          Collection result = (Collection) getterMethod.invoke(entity);
          assertEquals("to-many entity relationship " + fullPropName(propertyDescriptor) + " has size " + size,
                       size, 
                       result.size());

          if (isElementExpected) {
            assertTrue("to-many entity relationship contains entity: " + fullPropName(propertyDescriptor),
                       result.contains(relatedEntity));
          }
          else {
            assertFalse("to-many entity relationship does not contain entity: " + fullPropName(propertyDescriptor),
                        result.contains(relatedEntity));
          }
        }
        catch (Exception e) {
          e.printStackTrace();
          fail("getter method for to-many entity relationship threw exception: " + fullPropName(propertyDescriptor) + ": " + e);
        }
      }
    };
    doTestRelatedBeansInTransaction(entity, relatedEntity, new ToManyAsserter(1, true));

  }

  /**
   * Get the Hibernate-managed instance of the specified entity.
   *
   * @return the same entity if already managed by the current Hibernate
   *         session, otherwise loads from the database.
   */
  @SuppressWarnings("unchecked")
  private AbstractEntity getPersistedEntity(AbstractEntity entity)
  {
    if (entity == null) {
      return entity;
    }
    return genericEntityDao.<AbstractEntity,Serializable>findEntityById(entity.getEntityClass(), entity.getEntityId());
  }
  

  static interface BeanTester
  {
    public void testEntity(AbstractEntity entity);
  }

  static private interface RelatedBeansTester
  {
    public void testRelatedEntities(AbstractEntity entity, AbstractEntity relatedEntity);
  }

  protected String fullPropName(PropertyDescriptor prop)
  {
    return _entityClass.getName() + "." + prop.getName();
  }
  
  protected void assertPropertyValuesEquals(String assertMessage,
                                            Object expectedValue,
                                            Object actualValue)
  {
    if (expectedValue instanceof BigDecimal) {
      // need special case for BigDecimal, since scale is taken into account
      // by BigDecimal.equals(), but we want to ignore scale, since we can't
      // reliably instantiate test values with the correct scale
      assertTrue(assertMessage + ":" + expectedValue + ", " + actualValue, ((BigDecimal) expectedValue).compareTo((BigDecimal) actualValue) == 0);
    }
    else if (expectedValue instanceof byte[]) {
      assertEquals(assertMessage,
                   new String((byte[]) expectedValue),
                   new String((byte[]) actualValue));
    }
    else {
      assertEquals(assertMessage,
                   expectedValue,
                   actualValue);
    }
  }


}
