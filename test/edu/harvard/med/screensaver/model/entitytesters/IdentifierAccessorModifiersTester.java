// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.entitytesters;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;

import edu.harvard.med.screensaver.model.AbstractEntity;

/**
 * Tests that the identifier getter method is public, the identifier getter method is private,
 * both are instance, and the arg/return types match.
 */
public class IdentifierAccessorModifiersTester<E extends AbstractEntity>
extends AbstractEntityTester<E>
{
  private static Logger log = Logger.getLogger(IdentifierAccessorModifiersTester.class);
  
  public IdentifierAccessorModifiersTester(Class<E> entityClass, SessionFactory sessionFactory)
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
    
    ClassMetadata classMetadata = _sessionFactory.getClassMetadata(_entityClass);
    String identifierPropertyName = classMetadata.getIdentifierPropertyName();
    
    Method identifierGetter = ModelIntrospectionUtil.getGetterMethodForPropertyName(_entityClass, identifierPropertyName);
    assertTrue("private getId for " + _entityClass,
      Modifier.isPublic(identifierGetter.getModifiers()));
    assertFalse("instance getId for " + _entityClass,
      Modifier.isStatic(identifierGetter.getModifiers()));

    Type identifierType = identifierGetter.getGenericReturnType();
    assertNotNull("identifier getter returns type", identifierType);
    
    Method identifierSetter =
      ModelIntrospectionUtil.getSetterMethodForPropertyName(_entityClass, identifierPropertyName, (Class) identifierType);
    assertTrue("private getId for " + _entityClass,
      Modifier.isPrivate(identifierSetter.getModifiers()));
    assertFalse("instance getId for " + _entityClass,
      Modifier.isStatic(identifierSetter.getModifiers()));
  }
}
