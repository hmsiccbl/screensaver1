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
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestSuite;

import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.SchemaUtil;
import edu.harvard.med.screensaver.model.EntityNetworkPersister.EntityNetworkPersisterException;
import edu.harvard.med.screensaver.model.entitytesters.IdentifierAccessorModifiersTester;
import edu.harvard.med.screensaver.model.entitytesters.IdentifierMetadataTester;
import edu.harvard.med.screensaver.model.entitytesters.IsVersionedTester;
import edu.harvard.med.screensaver.model.entitytesters.ModelIntrospectionUtil;
import edu.harvard.med.screensaver.model.entitytesters.VersionAccessorsTester;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;

public abstract class AbstractEntityInstanceTest<E extends AbstractEntity> extends AbstractSpringTest
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
  public static TestSuite buildTestSuite(Class entityTestClass,
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
  /** The Hibernate <code>SessionFactory</code>. Used for getting <code>ClassMetadata</code> objects. */
  protected SessionFactory hibernateSessionFactory;
  protected HibernateTemplate hibernateTemplate;
  protected GenericEntityDAO genericEntityDao;
  protected SchemaUtil schemaUtil;

  private Class<E> _entityClass;
  private BeanInfo _beanInfo;
  protected TestDataFactory dataFactory = new TestDataFactory();

  // public constructors and instance methods

  public AbstractEntityInstanceTest(Class<E> clazz)
  throws IntrospectionException
  {
    super(clazz.getName());
    
    _entityClass = clazz;
    _beanInfo = Introspector.getBeanInfo(_entityClass);
  }

  public void testEqualsAndHashCode()
  {
    schemaUtil.truncateTablesOrCreateSchema();
    E transientEntity = dataFactory.newInstance(_entityClass);
    Set<E> set = new HashSet<E>();
    set.add(transientEntity);
    assertTrue(set.contains(transientEntity));

    log.debug("transient entity " + transientEntity);
    log.debug("transient entity hashcode " + transientEntity.hashCode());
    persistEntityNetwork(transientEntity);
    E detachedEntity = transientEntity;
    transientEntity = null; // no longer transient!
    E reloadedEntity = genericEntityDao.reloadEntity(detachedEntity);
    assertNotSame(reloadedEntity, detachedEntity);
    assertTrue(set.contains(detachedEntity));
    boolean isSemanticId = SemanticIDAbstractEntity.class.isAssignableFrom(_entityClass);
    log.debug("detached = " + detachedEntity);
    log.debug("reloaded = " + reloadedEntity);
    if (isSemanticId) {
      assertEquals(reloadedEntity, detachedEntity);
      assertEquals(reloadedEntity.hashCode(), detachedEntity.hashCode());
      assertTrue(set.contains(reloadedEntity));
    }
    else {
      log.debug("reloaded entity " + reloadedEntity);
      log.debug("reloaded entity hashcode " + reloadedEntity.hashCode());
      log.debug("detached entity " + detachedEntity);
      log.debug("detached entity hashcode " + detachedEntity.hashCode());
      assertFalse("reloaded entity " + reloadedEntity + " does not equal " + detachedEntity, reloadedEntity.equals(detachedEntity));
      assertFalse(reloadedEntity.hashCode() == detachedEntity.hashCode());
      assertFalse(set.contains(reloadedEntity));
    }
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
   * @motivation lazy init database and test entity, to save time by not
   *             invoking unless test is actually going to do something with the
   *             database (many tests are skipped for various reasons)
   */
  protected E initTestEntity()
  {
    schemaUtil.truncateTablesOrCreateSchema();
    E entity = dataFactory.newInstance(_entityClass);
    persistEntityNetwork(entity);
    return entity;
  }

  protected void persistEntityNetwork(final AbstractEntity root)
  {
    try {
      new EntityNetworkPersister(genericEntityDao, root).persistEntityNetwork();
    }
    catch (EntityNetworkPersisterException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  protected String fullPropName(PropertyDescriptor prop)
  {
    return _entityClass.getName() + "." + prop.getName();
  }
}
