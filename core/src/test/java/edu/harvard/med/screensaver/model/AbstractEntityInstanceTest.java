// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;

import javax.persistence.EntityManagerFactory;

import junit.framework.TestSuite;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

import edu.harvard.med.screensaver.db.EntityInflator;
import edu.harvard.med.screensaver.model.entitytesters.IdentifierAccessorModifiersTester;
import edu.harvard.med.screensaver.model.entitytesters.IdentifierMetadataTester;
import edu.harvard.med.screensaver.model.entitytesters.IsVersionedTester;
import edu.harvard.med.screensaver.model.entitytesters.VersionAccessorsTester;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.test.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.test.model.meta.ModelIntrospectionUtil;

public abstract class AbstractEntityInstanceTest<E extends AbstractEntity> extends AbstractSpringPersistenceTest
{
  private static Logger log = Logger.getLogger(AbstractEntityInstanceTest.class);

  /**
   * Subclasses should call this method to build their TestSuite, as it will
   * include tests for the test methods declared in this class, as well as tests
   * for each entity property found in the specified AbstractEntity class.
   * 
   * @param entityTestClass
   * @param entityClass
   * @return
   */
  public static TestSuite buildTestSuite(Class<? extends AbstractEntityInstanceTest> entityTestClass,
                                         Class<? extends AbstractEntity> entityClass)
  {
    TestSuite testSuite = new TestSuite(entityTestClass);
    BeanInfo beanInfo;
    try {
      beanInfo = Introspector.getBeanInfo(entityClass);
      // add all the property-specific tests for this entity class
      for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
        if (propertyDescriptor.getName().equals("class")) {
          log.debug("not creating test for \"class\" property " + propertyDescriptor.getDisplayName());
        }
        else if (ModelIntrospectionUtil.isTransientProperty(propertyDescriptor)) {
          log.debug("not creating test for transient (non-persistent) property " + propertyDescriptor.getDisplayName());
        }
        else /*if (ModelIntrospectionUtil.isToManyEntityRelationship(propertyDescriptor))*/ {
          testSuite.addTest(new EntityPropertyTest(entityClass, propertyDescriptor));
        }
      }
    }
    catch (IntrospectionException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
    return testSuite;
  }

  // instance fields injected by Spring
  @Autowired protected EntityManagerFactory entityManagerFactory;

  private Class<E> _entityClass;

  // public constructors and instance methods

  public AbstractEntityInstanceTest(Class<E> clazz)
  {
    super(clazz.getName());
    
    _entityClass = clazz;
  }

  // TODO: this test is no tests what it used to test, now that TestDataFactory returns persistent instances
//  public void testEqualsAndHashCode()
//  {
//    E transientEntity = dataFactory.newInstance(_entityClass);
//    Set<E> set = new HashSet<E>();
//    set.add(transientEntity);
//    assertTrue(set.contains(transientEntity));
//
//    log.debug("transient entity " + transientEntity);
//    log.debug("transient entity hashcode " + transientEntity.hashCode());
//    E reloadedEntity = genericEntityDao.mergeEntity(transientEntity);
//    assertNotSame(reloadedEntity, transientEntity);
//    assertTrue(set.contains(transientEntity));
//    boolean isSemanticId = SemanticIDAbstractEntity.class.isAssignableFrom(_entityClass);
//    log.debug("transient = " + transientEntity);
//    log.debug("reloaded = " + reloadedEntity);
//    if (isSemanticId) {
//      assertEquals(reloadedEntity, transientEntity);
//      assertEquals(reloadedEntity.hashCode(), transientEntity.hashCode());
//      assertTrue(set.contains(reloadedEntity));
//    }
//    else {
//      log.debug("reloaded entity " + reloadedEntity);
//      log.debug("reloaded entity hashcode " + reloadedEntity.hashCode());
//      log.debug("transient entity " + transientEntity);
//      log.debug("transient entity hashcode " + transientEntity.hashCode());
//      assertFalse("reloaded entity " + reloadedEntity + " does not equal " + transientEntity, reloadedEntity.equals(transientEntity));
//      assertFalse(reloadedEntity.hashCode() == transientEntity.hashCode());
//      assertFalse(set.contains(reloadedEntity));
//    }
//  }

  /**
   * Test some basic stuff, mostly about the identifier, in the ClassMetadata.
   */
  public void testIdentifierMetadata()
  {
    new IdentifierMetadataTester<E>(_entityClass, entityManagerFactory).testEntity();
  }

  /**
   * Test that the identifier getter method is public, the identifier getter method is private,
   * both are instance, and the arg/return types match.
   */
  public void testIdentifierAccessorModifiers()
  {
    new IdentifierAccessorModifiersTester<E>(_entityClass, entityManagerFactory).testEntity();
  }

  /**
   * Test that the entity is versioned.
   */
  public void testIsVersioned()
  {
    new IsVersionedTester<E>(_entityClass, entityManagerFactory).testEntity();
  }

  /**
   * Test version accessor methods: modifiers, arguments, annotations, and return types.
   */
  public void testVersionAccessors()
  {
    new VersionAccessorsTester<E>(_entityClass, entityManagerFactory).testEntity();
  }

  // this serves as the general test for AuditedAbstractEntity.createdBy, since
  // we must test it from a concrete instance, if we're going to verify
  // persistence is working
  public void testAuditProperties() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
  {
    if (!!!AuditedAbstractEntity.class.isAssignableFrom(_entityClass)) {
      log.info("not an \"audited\" entity type: skipping testing 'createdBy' property");
      return;
    }
      
    AuditedAbstractEntity auditedEntity = (AuditedAbstractEntity) dataFactory.newInstance(_entityClass, getName());
    ScreensaverUser dataEntryAdmin = auditedEntity.getCreatedBy();
    DateTime expectedDateCreated = auditedEntity.getDateCreated();
    AdministratorUser dataEntryUpdateAdmin = dataFactory.newInstance(AdministratorUser.class);
    AdministrativeActivity updateActivity = auditedEntity.createUpdateActivity(dataEntryUpdateAdmin, "updated!");
    auditedEntity = genericEntityDao.mergeEntity(auditedEntity);
    
    auditedEntity = new EntityInflator<AuditedAbstractEntity>(genericEntityDao, auditedEntity, true).
      need(AuditedAbstractEntity.createdBy.castToSubtype((Class<AuditedAbstractEntity>) _entityClass)).
      need(AuditedAbstractEntity.updateActivities.to(Activity.performedBy).castToSubtype((Class<AuditedAbstractEntity>) _entityClass)).inflate();
    assertEquals("dateCreated", expectedDateCreated, auditedEntity.getDateCreated());
    if (dataEntryAdmin == null) {
      // TODO: we still need to add 'createdBy' params to all of our AuditedAbstractEntity concrete class constructors; until then the createdBy property will be null
      log.warn("audited entity does not yet support 'createdBy' property; skipping test of this property");
    } 
    else {
      assertEquals("data entry admin", dataEntryAdmin.getEntityId(), auditedEntity.getCreatedBy().getEntityId());
    }
    AdministrativeActivity actualUpdateActivity = (AdministrativeActivity) auditedEntity.getUpdateActivities().first();
    assertEquals("update activity type", AdministrativeActivityType.ENTITY_UPDATE, actualUpdateActivity.getType());
    assertEquals("update activity admin", dataEntryUpdateAdmin.getEntityId(), actualUpdateActivity.getPerformedBy().getEntityId());
    assertEquals("update activity date", updateActivity.getDateOfActivity(), actualUpdateActivity.getDateOfActivity());
    assertEquals("update activity comment", "updated!", actualUpdateActivity.getComments());
  }

  protected String fullPropName(PropertyDescriptor prop)
  {
    return _entityClass.getName() + "." + prop.getName();
  }
}
