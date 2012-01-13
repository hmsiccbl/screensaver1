// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.entitytesters;

import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Immutable;

import edu.harvard.med.screensaver.model.AbstractEntity;

/**
 * Tests that the entity is versioned, that the name of the version property is "version",
 * and that the version property is not nullable.
 */
public class IsVersionedTester<E extends AbstractEntity>
extends AbstractEntityTester<E>
{
  private static Logger log = Logger.getLogger(IsVersionedTester.class);

  public IsVersionedTester(Class<E> entityClass, EntityManagerFactory sessionFactory)
  {
    super(entityClass, sessionFactory);
  }

  @Override
  public void testEntity()
  {
    testIsVersioned();
  }

  /**
   * Test that the entity is versioned, that the name of the version property is "version",
   * and that the version property is not nullable.
   */
  private void testIsVersioned()
  {
    
    org.hibernate.annotations.Entity entityAnnotation =
      _entityClass.getAnnotation(org.hibernate.annotations.Entity.class);
    if (entityAnnotation != null && ! entityAnnotation.mutable()) {
      return;
    }
    if (_entityClass.getAnnotation(Immutable.class) != null) {
      return;
    }
      
    ManagedType<? extends AbstractEntity> type = _entityManagerFactory.getMetamodel().managedType(_entityClass);
    SingularAttribute id = ((IdentifiableType) type).getId(((IdentifiableType) type).getIdType().getJavaType());
    assertTrue("hibernate class is versioned: " + _entityClass, ((IdentifiableType) type).hasVersionAttribute());
    
    assertFalse("version property is not nullable: " + _entityClass, ((IdentifiableType) type).getVersion(Integer.class).isOptional());
  }
}
