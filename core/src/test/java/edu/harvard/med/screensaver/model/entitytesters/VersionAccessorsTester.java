// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.entitytesters;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.persistence.Column;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Version;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Immutable;

import edu.harvard.med.screensaver.model.AbstractEntity;

/**
 * Test version accessor methods: modifiers, arguments, annotations, and return types:
 * <ul>
 * <li>getter and setter are both instance methods
 * <li>getter and setter are both private methods
 * <li>getter return type is integer
 * <li>setter return type is void
 * <li>getter has javax.persistence.Column annotation with nullable=false
 * <li>getter has javax.persistence.Version annotation
 * </ul>
 */
public class VersionAccessorsTester<E extends AbstractEntity>
extends AbstractEntityTester<E>
{
  private static Logger log = Logger.getLogger(VersionAccessorsTester.class);

  public VersionAccessorsTester(Class<E> entityClass, EntityManagerFactory sessionFactory)
  {
    super(entityClass, sessionFactory);
  }

  @Override
  public void testEntity()
  {
    testVersionAccessors();
  }

  /**
   * Test version accessor methods: modifiers, arguments, annotations, and return types.
   */
  private void testVersionAccessors()
  {
    // skip classes that have a getVersion from a superclass
    if (! _entityClass.getSuperclass().equals(AbstractEntity.class)) {
      return;
    }

    org.hibernate.annotations.Entity entityAnnotation =
      _entityClass.getAnnotation(org.hibernate.annotations.Entity.class);
    if (entityAnnotation != null && ! entityAnnotation.mutable()) {
      return;
    }
    if (_entityClass.getAnnotation(Immutable.class) != null) {
      return;
    }
  
    // getVersion
    try {
      Method getVersionMethod = _entityClass.getDeclaredMethod("getVersion");
      assertTrue("private getVersion for " + _entityClass, Modifier.isPrivate(getVersionMethod.getModifiers()));
      assertFalse("instance getVersion for " + _entityClass, Modifier.isStatic(getVersionMethod.getModifiers()));
      assertEquals("getVersion return type for " + _entityClass, getVersionMethod.getReturnType(), Integer.class);
      
      Column column = getVersionMethod.getAnnotation(Column.class);
      assertNotNull("getVersion has @javax.persistence.Column", column);
      assertFalse("getVersion has @javax.persistence.Column(nullable=false)", column.nullable());

      Version version = getVersionMethod.getAnnotation(Version.class);
      assertNotNull("getVersion has @javax.persistence.Version", version);
    }
    catch (SecurityException e) {
      e.printStackTrace();
      fail("getting declared method getVersion for " + _entityClass + ": " + e);
    }
    catch (NoSuchMethodException e) {
      fail("getting declared method getVersion for " + _entityClass + ": " + e);
    }
  
    // setVersion
    try {
      Method setVersionMethod = _entityClass.getDeclaredMethod("setVersion", Integer.class);
      assertTrue("private setVersion for " + _entityClass, Modifier.isPrivate(setVersionMethod.getModifiers()));
      assertFalse("instance setVersion for " + _entityClass, Modifier.isStatic(setVersionMethod.getModifiers()));
      assertEquals("setVersion return type for " + _entityClass, setVersionMethod.getReturnType(), void.class);
    }
    catch (SecurityException e) {
      e.printStackTrace();
      fail("getting declared method getVersion for " + _entityClass + ": " + e);
    }
    catch (NoSuchMethodException e) {
      fail("getting declared method getVersion for " + _entityClass + ": " + e);
    }
  }
}
