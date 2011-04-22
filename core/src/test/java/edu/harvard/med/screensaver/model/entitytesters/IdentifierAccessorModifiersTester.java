// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.entitytesters;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.AbstractEntity;

/**
 * Tests that the identifier getter method is public, the identifier getter method is private,
 * both are instance, and the arg/return types match.
 */
public class IdentifierAccessorModifiersTester<E extends AbstractEntity>
extends AbstractEntityTester<E>
{
  private static Logger log = Logger.getLogger(IdentifierAccessorModifiersTester.class);
  
  public IdentifierAccessorModifiersTester(Class<E> entityClass, EntityManagerFactory sessionFactory)
  {
    super(entityClass, sessionFactory);
  }

  @Override
  public void testEntity()
  {
    testIdentifierAccessorModifiers();
  }
  
  /**
   * Test that the identifier getter method is public, the identifier getter method is private,
   * both are instance, and the arg/return types match.
   */
  private void testIdentifierAccessorModifiers()
  {
    if (ModelIntrospectionUtil.isEntitySubclass(_entityClass)) {
      // entity subclasses depend on their superclass for identifier methods,
      // which will be tested when that superclass is tested
      return;
    }
    
    String identifierPropertyName;
    ManagedType<? extends AbstractEntity> type = _entityManagerFactory.getMetamodel().managedType(_entityClass);
    Class idType = ((IdentifiableType) type).getIdType().getJavaType();
    SingularAttribute id = ((IdentifiableType) type).getId(idType);
    identifierPropertyName = id.getName();
    
    Method identifierGetter = ModelIntrospectionUtil.getGetterMethodForPropertyName(_entityClass, identifierPropertyName);
    assertTrue("public entity ID getter for " + _entityClass,
      Modifier.isPublic(identifierGetter.getModifiers()));
    assertFalse("instance entity ID getter for " + _entityClass,
      Modifier.isStatic(identifierGetter.getModifiers()));

    Type identifierType = identifierGetter.getGenericReturnType();
    assertNotNull("identifier getter returns type", identifierType);
    
    Method identifierSetter =
      ModelIntrospectionUtil.getSetterMethodForPropertyName(_entityClass, identifierPropertyName, (Class) identifierType);
    assertTrue("private entity ID setter for " + _entityClass,
      Modifier.isPrivate(identifierSetter.getModifiers()));
    assertFalse("instance entity ID setter for " + _entityClass,
      Modifier.isStatic(identifierSetter.getModifiers()));
  }
}
