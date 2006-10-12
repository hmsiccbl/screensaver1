// $HeadURL$
// $Id$
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
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.log4j.Logger;

/**
 * Exercise the entities as JavaBeans.
 * 
 * Info-level log output is for special cases caused by JDK5 annotations in our model.
 * Debug-level log output is for special cases that are language-related and for "here's what I'm doing at every step" output.
 */
abstract class EntityBeansExercizor extends EntityClassesExercisor
{
  
  private static Logger log = Logger.getLogger(EntityBeansExercizor.class);
  
  protected static interface PropertyDescriptorExercizor
  {
    public void exercizePropertyDescriptor(
      AbstractEntity bean,
      BeanInfo beanInfo,
      PropertyDescriptor propertyDescriptor);
  }
  
  protected void exercizePropertyDescriptors(final PropertyDescriptorExercizor exercizor)
  {
    exercizeJavaBeanEntities(new JavaBeanEntityExercizor()
      {
        public void exercizeJavaBeanEntity(AbstractEntity bean, BeanInfo beanInfo)
        {
          for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
            String propertyName = propertyDescriptor.getName();
            String propFullName = bean.getClass().getSimpleName() + "." + propertyName;
            if (propertyName.equals("class")) {
              log.debug("skipping \"class\" property " + propFullName);
              continue;
            }

            if (propertyName.startsWith("hbn")) {
              log.info("skipping Hibernate property " + propFullName);
              continue;
            }

            // skip what appears to be an entity's property, but that has been
            // explicitly annotated as a non-property
            if (propertyDescriptor.getReadMethod().isAnnotationPresent(DerivedEntityProperty.class)) {
              log.info("skipping derived property " + propFullName);
              continue;
            }
            
            log.debug("excercizing JavaBean entity property " + bean.getClass().getSimpleName() + "." + propertyName + 
                      " with " + exercizor.getClass().getEnclosingMethod().getName());
            exercizor.exercizePropertyDescriptor(bean, beanInfo, propertyDescriptor);
          }
        }
      });
  }
  
  protected static interface JavaBeanEntityExercizor
  {
    void exercizeJavaBeanEntity(AbstractEntity bean, BeanInfo beanInfo);
  }
  
  protected void exercizeJavaBeanEntities(JavaBeanEntityExercizor exercizor)
  {
    for (Class<AbstractEntity> entityClass : getEntityClasses()) {
      try {
        log.debug("excercizing JavaBean entity " + entityClass.getSimpleName());
        exercizor.exercizeJavaBeanEntity(newInstance(entityClass),
                                         Introspector.getBeanInfo(entityClass));
      }
      catch (IntrospectionException e) {
        e.printStackTrace();
        fail(e.getMessage());
      }
    }
  }

  static Map<String, String> oddPluralToSingularPropertiesMap =
    new HashMap<String, String>();
  static {
    oddPluralToSingularPropertiesMap.put("children", "child");
    oddPluralToSingularPropertiesMap.put("copies", "copy");
    oddPluralToSingularPropertiesMap.put("typesDerivedFrom", "typeDerivedFrom");
    oddPluralToSingularPropertiesMap.put("lettersOfSupport", "letterOfSupport");
    oddPluralToSingularPropertiesMap.put("equipmentUsed", "equipmentUsed");
    oddPluralToSingularPropertiesMap.put("visitsPerformed", "visitPerformed");
    oddPluralToSingularPropertiesMap.put("screensLed", "screenLed");
    oddPluralToSingularPropertiesMap.put("screensHeaded", "screenHeaded");
    oddPluralToSingularPropertiesMap.put("screensCollaborated", "screenCollaborated");
    oddPluralToSingularPropertiesMap.put("platesUsed", "platesUsed");
  }
  static Map<String, String> oddSingularToPluralPropertiesMap =
    new HashMap<String, String>();
  static {
    oddSingularToPluralPropertiesMap.put("child", "children");
    oddSingularToPluralPropertiesMap.put("copy", "copies");
    oddSingularToPluralPropertiesMap.put("typeDerivedFrom", "typesDerivedFrom");
    oddSingularToPluralPropertiesMap.put("letterOfSupport", "lettersOfSupport");
    oddSingularToPluralPropertiesMap.put("equipmentUsed", "equipmentUsed");
    oddSingularToPluralPropertiesMap.put("visitPerformed", "visitPerformed");
    oddSingularToPluralPropertiesMap.put("screenLed", "screensLed");
    oddSingularToPluralPropertiesMap.put("screenHeaded", "screensHeaded");
    oddSingularToPluralPropertiesMap.put("screenCollaborated", "screensCollaborated");
    oddSingularToPluralPropertiesMap.put("platesUsed", "platesUsed");
  }
  
  static Map<String, String> oddPropertyToRelatedPropertyMap =
    new HashMap<String, String>();
  static {
    oddPropertyToRelatedPropertyMap.put("cherryPick", "RNAiKnockdownConfirmation");
    oddPropertyToRelatedPropertyMap.put("equipmentUsed", "visit");
    oddPropertyToRelatedPropertyMap.put("platesUsed", "visit");
    oddPropertyToRelatedPropertyMap.put("labMembers", "labHead");
    oddPropertyToRelatedPropertyMap.put("screensHeaded", "labHead");
    oddPropertyToRelatedPropertyMap.put("screensLed", "leadScreener");
    oddPropertyToRelatedPropertyMap.put("visitsPerformed", "performedBy");
  }
  static Map<String, String> oddPropertyToRelatedPluralPropertyMap =
    new HashMap<String, String>();
  static {
    oddPropertyToRelatedPluralPropertyMap.put("derivedTypes", "typesDerivedFrom");
    oddPropertyToRelatedPluralPropertyMap.put("typesDerivedFrom", "derivedTypes");
    oddPropertyToRelatedPluralPropertyMap.put("collaborators", "screensCollaborated");
    oddPropertyToRelatedPluralPropertyMap.put("screensCollaborated", "collaborators");
    oddPropertyToRelatedPluralPropertyMap.put("labHead", "screensHeaded");
    oddPropertyToRelatedPluralPropertyMap.put("leadScreener", "screensLed");
    oddPropertyToRelatedPluralPropertyMap.put("performedBy", "visitsPerformed");
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
                 foundMethod.getReturnType(),
                 Boolean.TYPE);
    return foundMethod;
  }
  
  /**
   * Returns true iff the property corresponds to the entity's ID. Such methods
   * include: getEntityId(), getFooId() (for bean of type Foo), and any
   * properties that may be used to define the ID (for cases where the entity ID
   * is not an auto-generated database ID, but instead correspdonds to the
   * entity's business key).
   * 
   * @param beanInfo the bean the property belongs to
   * @param propertyDescriptor the property
   * @return true iff property is "entityId" or the property that is named the
   *         same as the entity, but with an "Id" suffix; otherwise false
   */
  @SuppressWarnings("unchecked")
  public boolean isEntityIdProperty(Class<? extends AbstractEntity> beanClass,
                                    PropertyDescriptor propertyDescriptor)
  {
    Method getter = propertyDescriptor.getReadMethod();
    if (getter.isAnnotationPresent(EntityIdProperty.class)) {
      log.debug("isEntityIdProperty(): property participates in defining entity ID: " + propertyDescriptor.getName());
      return true;
    }

    // legacy logic for finding the standard entity-ID related methods below...
    if (propertyDescriptor.getName().equals("entityId")) {
      log.debug("isEntityIdProperty(): property participates in defining entity ID: " + propertyDescriptor.getName());
      return true;
    }

    // Check whether property corresponds to the bean's Hibernate ID method, which is named similarly to the bean.
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
    String beanName,
    PropertyDescriptor propertyDescriptor)
  {
    Method getter = propertyDescriptor.getReadMethod();
    ToManyRelationship toManyRelationship = getter.getAnnotation(ToManyRelationship.class);
    if (toManyRelationship != null) {
      return toManyRelationship.minCardinality();
    }
    return 0;
  }
  
  protected boolean isUnidirectionalRelationship(Class<? extends AbstractEntity> beanClass, 
                                                 PropertyDescriptor propertyDescriptor)
  {
    try {
      return isUnidirectionalRelationshipMethod(propertyDescriptor.getReadMethod()) ||
      isUnidirectionalRelationshipMethod(beanClass.
                                         getDeclaredMethod("getHbn" + 
                                                           StringUtils.capitalize(propertyDescriptor.getName())));
    }
    catch (SecurityException e) {
      throw e;
    }
    catch (NoSuchMethodException e) {
      return false;
    }
  }

  protected boolean hasForeignKeyConstraint(PropertyDescriptor propertyDescriptor)
  {
    Method getter = propertyDescriptor.getReadMethod();
    ToOneRelationship toOneRelationship = getter.getAnnotation(ToOneRelationship.class);
    return toOneRelationship != null && !toOneRelationship.nullable();
  }

  protected boolean setterMethodNotExpected(Class<? extends AbstractEntity> beanClass, PropertyDescriptor propertyDescriptor)
  {
    String propFullName = beanClass.getSimpleName() + 
    "." + propertyDescriptor.getName();

    // no setter expected if property participates in defining the entity ID
    if (isEntityIdProperty(beanClass, propertyDescriptor)) {
      log.info("setter method not expected for property that participates in defining the entity ID: " + 
               propFullName);
      return true;
    }

    // no setter expected if property is for a *-to-one relationship and has a foreign key constraint
    if (hasForeignKeyConstraint(propertyDescriptor)) {
      log.info("setter method not expected for property that is a *-to-one relationship with a foreign key constraint: " + 
               propFullName);
      return true;
    }
    
    // no setter expected if property is for a one-to-one relationship and related side has a foreign key constraint relationship
    RelatedProperty relatedProperty = new RelatedProperty(beanClass, propertyDescriptor);
    if (relatedProperty.exists() && relatedProperty.hasForeignKeyConstraint()) {
      log.info("setter method not expected for property that is on the \"one\" side of a relationship with a foreign key constraint: " + 
               propFullName);
      return true;
    }
    return false;
  }

  // private methods
  
  private boolean isUnidirectionalRelationshipMethod(Method getter)
  {
    ToOneRelationship toOneRelationship = getter.getAnnotation(ToOneRelationship.class);
    ToManyRelationship toManyRelationship = getter.getAnnotation(ToManyRelationship.class);
    if ((toOneRelationship != null && toOneRelationship.unidirectional()) ||
      (toManyRelationship != null && toManyRelationship.unidirectional())) {
      return true;
    }
    return false;
  }
}
