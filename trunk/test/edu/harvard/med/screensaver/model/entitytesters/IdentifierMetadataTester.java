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

import javax.persistence.GeneratedValue;

import org.apache.log4j.Logger;
import org.hibernate.EntityMode;
import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.SemanticIDAbstractEntity;

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

  public IdentifierMetadataTester(Class<E> entityClass, SessionFactory sessionFactory)
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
    if (isEntitySubclass()) {
      // entity subclasses depend on their superclass for identifier methods
      // TODO: run this test on the superclasses
      return;
    }

    ClassMetadata classMetadata = _sessionFactory.getClassMetadata(_entityClass);
    String entityName = classMetadata.getEntityName();
    assertTrue(
        "hibernate class has an identifier: " + entityName,
        classMetadata.hasIdentifierProperty());
    assertFalse(
        "hibernate class does not have natural identifier: " + entityName,
        classMetadata.hasNaturalIdentifier());
    String identifierPropertyName = classMetadata.getIdentifierPropertyName();
    assertNotNull("identifier property name is non-null", identifierPropertyName);
    Class mappedClass = classMetadata.getMappedClass(EntityMode.POJO);
    assertNotNull("class metadata has non-null mapped class", mappedClass);
    
    testGeneratedValueAppropriateness(entityName, identifierPropertyName);
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
    Method identifierGetter = getGetterMethodForPropertyName(identifierPropertyName);
    return identifierGetter.isAnnotationPresent(GeneratedValue.class);
  }
}
