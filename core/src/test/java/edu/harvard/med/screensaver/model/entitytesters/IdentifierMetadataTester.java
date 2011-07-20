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

import javax.persistence.EntityManagerFactory;
import javax.persistence.GeneratedValue;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.SemanticIDAbstractEntity;
import edu.harvard.med.screensaver.test.model.meta.ModelIntrospectionUtil;

/**
 * Tests some basic stuff, mostly about the identifier, in the ClassMetadata:
 * <ul>
 * <li>entity class has an identifier
 * <li>entity class does not have natural identifier
 * <li>identifier property name is non-null
 * <li>class metadata has non-null mapped class
 * <li>inheritance/non-inheritance through {@link SemanticIDAbstractEntity} matches {@link
 * GeneratedValue} settings.
 * </ul>
 */
public class IdentifierMetadataTester<E extends AbstractEntity>
extends AbstractEntityTester<E>
{
  private static Logger log = Logger.getLogger(IdentifierMetadataTester.class);

  public IdentifierMetadataTester(Class<E> entityClass, EntityManagerFactory sessionFactory)
  {
    super(entityClass, sessionFactory);
  }

  @Override
  public void testEntity()
  {
    testIdentifierMetadata();
  }

  private void testIdentifierMetadata()
  {
    if (ModelIntrospectionUtil.isEntitySubclass(_entityClass)) {
      // entity subclasses depend on their superclass for identifier methods
      // TODO: run this test on the superclasses
      return;
    }

    ManagedType<? extends AbstractEntity> type = _entityManagerFactory.getMetamodel().managedType(_entityClass);
    assertTrue("hibernate class has an identifier: " + _entityClass, ((IdentifiableType) type).hasSingleIdAttribute());
    
    Class idType = ((IdentifiableType) type).getIdType().getJavaType();
    String idName = ((IdentifiableType) type).getId(idType).getName();
    testGeneratedValueAppropriateness(_entityClass.toString(), idName);
  }

  private void testGeneratedValueAppropriateness(String entityName, String identifierPropertyName)
  {
    boolean isSemanticIDAbstractEntity = isSemanticIDAbstractEntity();
    boolean hasGeneratedValue = hasGeneratedValueAnnotation(entityName, identifierPropertyName);
    
    if (hasGeneratedValue && isSemanticIDAbstractEntity) {
      fail("SemanticIDAbstractEntities should not have @GeneratedValue annotations on their identifier setters: " + entityName);
    }
    if (! hasGeneratedValue && ! isSemanticIDAbstractEntity) {
      fail("non-SemanticIDAbstractEntities should have @GeneratedValue annotations on their identifier setters: " + entityName);
    }
  }

  private boolean isSemanticIDAbstractEntity()
  {
    return SemanticIDAbstractEntity.class.isAssignableFrom(_entityClass);
  }

  private boolean hasGeneratedValueAnnotation(String entityName, String identifierPropertyName)
  {
    Method identifierGetter = ModelIntrospectionUtil.getGetterMethodForPropertyName(_entityClass, identifierPropertyName);
    return identifierGetter.isAnnotationPresent(GeneratedValue.class);
  }
}
