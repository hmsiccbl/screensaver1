// $HeadURL:
// svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml
// $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.entitytesters;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import junit.framework.TestCase;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.DomainModelDefinitionException;
import edu.harvard.med.screensaver.model.ExistenceRequirement;
import edu.harvard.med.screensaver.model.SemanticIDAbstractEntity;
import edu.harvard.med.screensaver.model.annotations.CollectionOfElements;
import edu.harvard.med.screensaver.model.annotations.Column;
import edu.harvard.med.screensaver.model.annotations.ContainedEntity;
import edu.harvard.med.screensaver.model.annotations.ToMany;
import edu.harvard.med.screensaver.model.annotations.ToOne;
import edu.harvard.med.screensaver.model.meta.RelatedProperty;
import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Immutable;

public class ModelIntrospectionUtil extends TestCase
{

  private static Logger log = Logger.getLogger(ModelIntrospectionUtil.class);
  
  /**
   * Get the getter method for a property based on the property name.
   * 
   * @param propertyName the property name
   * @param entityClass
   * @return the getter method
   */
  public static Method getGetterMethodForPropertyName(Class entityClass, String propertyName)
  {
    String getterName = "get" + StringUtils.capitalize(propertyName);
    try {
      return entityClass.getDeclaredMethod(getterName);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Get the setter method for a property based on the property name and the property type.
   * @param propertyName the property name
   * @param propertyType the property type
   * @return the setter method
   */
  public static Method getSetterMethodForPropertyName(Class entityClass, String propertyName, Class propertyType)
  {
    String setterName = "set" + StringUtils.capitalize(propertyName);
    try {
      return entityClass.getDeclaredMethod(setterName, propertyType);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns true iff the property corresponds to the entity's ID. Such methods
   * include: getEntityId(), getFooId() (for _bean of type Foo), and any
   * properties that may be used to define the ID (for cases where the entity ID
   * is not an auto-generated database ID, but instead correspdonds to the
   * entity's business key).
   *
   * @param propertyDescriptor the property
   * @return true iff property is "entityId" or the property that is named the
   *         same as the entity, but with an "Id" suffix; otherwise false
   */
  @SuppressWarnings("unchecked")
  public static boolean isEntityIdProperty(Class<? extends AbstractEntity> beanClass,
                                           PropertyDescriptor propertyDescriptor)
  {
    // legacy logic for finding the standard entity-ID related methods below...
    if (propertyDescriptor.getName().equals("entityId")) {
      log.debug("isEntityIdProperty(): property participates in defining entity ID: " + propertyDescriptor.getName());
      return true;
    }

    // Check whether property corresponds to the _bean's Hibernate ID method, which is named similarly to the _bean.
    // We also check the parent classes, to handle the case where the property
    // has been inherited, as the property name will depend upon the class it
    // was declared in.
    String capitalizedPropertyName = propertyDescriptor.getName().substring(0, 1).toUpperCase() + propertyDescriptor.getName().substring(1);
    while (!AbstractEntity.class.equals(beanClass)) {
      if (capitalizedPropertyName.endsWith(beanClass.getSimpleName() + "Id")) {
        log.debug("isEntityIdProperty(): property participates in defining entity ID: " + propertyDescriptor.getName() +
                  " in " + beanClass.getSimpleName());
        return true;
      }
      beanClass = (Class<? extends AbstractEntity>) beanClass.getSuperclass();
    }
    return false;
  }
  
  public static Class<?> getCollectionElementType(PropertyDescriptor propertyDescriptor)
  {
    assert isCollectionBasedProperty(propertyDescriptor);
    Type[] actualTypeArguments = ((ParameterizedType) propertyDescriptor.getReadMethod().getGenericReturnType()).getActualTypeArguments();
    return (Class<?>) actualTypeArguments[0];
  }
  
  public static Class<?> getMapKeyType(PropertyDescriptor propertyDescriptor)
  {
    assert isMapBasedProperty(propertyDescriptor);
    Type[] actualTypeArguments = ((ParameterizedType) propertyDescriptor.getReadMethod().getGenericReturnType()).getActualTypeArguments();
    return (Class<?>) actualTypeArguments[0];
  }

  public static Class<?> getMapValueType(PropertyDescriptor propertyDescriptor)
  {
    assert isMapBasedProperty(propertyDescriptor);
    Type[] actualTypeArguments = ((ParameterizedType) propertyDescriptor.getReadMethod().getGenericReturnType()).getActualTypeArguments();
    return (Class<?>) actualTypeArguments[1];
  }

  /**
   * Determine whether this property represents a *-to-1 relationship, based
   * upon the return type of the property (and not based upon annotations)
   */
  public static boolean isToOneEntityRelationship(PropertyDescriptor propertyDescriptor)
  {
    return AbstractEntity.class.isAssignableFrom(propertyDescriptor.getPropertyType());
  }

  /**
   * Determine whether this property represents a *-to-N relationship, based
   * upon the return type of the property (and not based upon annotations)
   */
  public static boolean isToManyEntityRelationship(PropertyDescriptor propertyDescriptor)
  {
    if (isCollectionBasedProperty(propertyDescriptor)) {
      return AbstractEntity.class.isAssignableFrom(getCollectionElementType(propertyDescriptor));
    }
    else if (isMapBasedProperty(propertyDescriptor)) {
      return AbstractEntity.class.isAssignableFrom(getMapValueType(propertyDescriptor));
    }
    return false;
  }

  public static boolean isUnidirectionalRelationship(Class<? extends AbstractEntity> beanClass,
                                                     PropertyDescriptor propertyDescriptor)
  {
    try {
      return isUnidirectionalRelationshipMethod(propertyDescriptor.getReadMethod());
    }
    catch (SecurityException e) {
      throw e;
    }
  }

  public static boolean isSetterMethodNotExpected(Class<? extends AbstractEntity> beanClass, PropertyDescriptor propertyDescriptor)
  {
    String propFullName = beanClass.getSimpleName() + "." + propertyDescriptor.getName();

    if (isTransientProperty(propertyDescriptor)) {
      log.info("setter method not expected for transient property: " + propFullName);
      return true;
    }
    
    if (isPropertyWithNonconventionalSetterMethod(propertyDescriptor)) {
      log.info("setter method not expected for transient property: " + propFullName);
      return true;
    }
    

    if (isImmutableProperty(beanClass, propertyDescriptor) || beanClass.getAnnotation(Immutable.class) != null) {
      log.info("setter method not expected for immutable property: " + propFullName);
      return true;
    }

    if (isToOneRelationshipRequired(propertyDescriptor)) {
      return true;
    }

    // no setter expected if property participates in defining the entity ID
    if (isEntityIdProperty(beanClass, propertyDescriptor)) {
      log.info("setter method not expected for property that participates in defining the entity ID: " +
               propFullName);
      return true;
    }
    
    if (isEmbeddableProperty(beanClass, propertyDescriptor)) {
      return true;
    }

    return false;
  }

  public static boolean isToOneRelationshipRequired(PropertyDescriptor propertyDescriptor) 
  {
    if (isToOneEntityRelationship(propertyDescriptor)) {
      return isNonNullableProperty(propertyDescriptor);
    }
    return false;
  }

  /**
   * @return true if the many-side of this one-to-many relationship is required (non-nullable), so that the related entity must always have beanClass as (one of) its parents
   */
  public static boolean isOneToManyRelationshipRequired(Class<? extends AbstractEntity> beanClass,
                                                        PropertyDescriptor propertyDescriptor)
  {
    if (isToManyEntityRelationship(propertyDescriptor)) {
      // ignore many-to-many, which can never be required, containment relationship
      if (hasAnnotation(OneToMany.class, propertyDescriptor)) {
        RelatedProperty relatedProperty = new RelatedProperty(beanClass, propertyDescriptor);
        PropertyDescriptor relatedPropertyDescriptor = relatedProperty.getPropertyDescriptor();
        return isToOneRelationshipRequired(relatedPropertyDescriptor);
      }
    }
    return false;
  }

  public static boolean isNonNullableProperty(PropertyDescriptor propertyDescriptor)
  {
    Method getter = propertyDescriptor.getReadMethod();
    javax.persistence.Column jpaColumn = getter.getAnnotation(javax.persistence.Column.class);
    if (jpaColumn != null) {
      return !jpaColumn.nullable();
    }
    JoinColumn jpaJoinColumn = getter.getAnnotation(JoinColumn.class);
    if (jpaJoinColumn != null) {
      return !jpaJoinColumn.nullable();
    }
    return false;
  }

  public static boolean isTransientProperty(PropertyDescriptor propertyDescriptor)
  {
    return hasAnnotation(Transient.class, propertyDescriptor);
  }

  public static boolean isImmutableProperty(Class<? extends AbstractEntity> beanClass, PropertyDescriptor propertyDescriptor)
  {
    org.hibernate.annotations.Entity entityAnnotation =
      beanClass.getAnnotation(org.hibernate.annotations.Entity.class);
    if (entityAnnotation != null && ! entityAnnotation.mutable()) {
      return true;
    }
    if (beanClass.getAnnotation(Immutable.class) != null) {
      return true;
    }
    return hasAnnotation(Immutable.class, propertyDescriptor);
  }

  public static boolean isEmbeddableProperty(Class<? extends AbstractEntity> beanClass, PropertyDescriptor propertyDescriptor)
  {
    Class propertyType = (Class) propertyDescriptor.getPropertyType();
    if (!AbstractEntity.class.isAssignableFrom(propertyType)) {
      Embeddable embeddable =
        (Embeddable) propertyType.getAnnotation(Embeddable.class);
      return embeddable != null;
    }
    return false;
  }

  /**
   * Determine whether this property represents a collection-based property, based
   * upon the return type of the property (and not based upon annotations). Note
   * that a collection property may either represent an entity relationship (
   * {@link #isToManyEntityRelationship(PropertyDescriptor)} or
   * {@link #isToOneEntityRelationship(PropertyDescriptor)}) or a collection of
   * elements ({@link #isCollectionOfElements(PropertyDescriptor)}.
   */
  public static boolean isCollectionBasedProperty(PropertyDescriptor propertyDescriptor)
  {
    Class<?> propertyType = propertyDescriptor.getPropertyType();
    return Collection.class.isAssignableFrom(propertyType); 
  }

  /**
   * Determine whether this property represents a map-based property, based
   * upon the return type of the property (and not based upon annotations). Note
   * that a collection property may either represent an entity relationship (
   * {@link #isToManyEntityRelationship(PropertyDescriptor)} or
   * {@link #isToOneEntityRelationship(PropertyDescriptor)}) or a collection of
   * elements ({@link #isCollectionOfElements(PropertyDescriptor)}.
   */
  public static boolean isMapBasedProperty(PropertyDescriptor propertyDescriptor)
  {
    Class<?> propertyType = propertyDescriptor.getPropertyType();
    return Map.class.isAssignableFrom(propertyType); 
  }

  public static boolean isCollectionOrMapBasedProperty(PropertyDescriptor propertyDescriptor)
  {
    return isCollectionBasedProperty(propertyDescriptor) || isMapBasedProperty(propertyDescriptor);
  }

  /**
   * Determine whether this property represents a collection-of-elements, based
   * upon the return type of the property (and not based upon annotations),
   * which must be a collection- or map-based property with element/value type
   * that is not AbstractEntity (in which case it would be an entity
   * relationship property: {@link #isToManyEntityRelationship(PropertyDescriptor)} or
   * {@link #isToOneEntityRelationship(PropertyDescriptor)}).
   */
  public static boolean isCollectionOfElements(PropertyDescriptor propertyDescriptor)
  {
    return (isCollectionBasedProperty(propertyDescriptor) && !AbstractEntity.class.isAssignableFrom(getCollectionElementType(propertyDescriptor))) ||
           (isMapBasedProperty(propertyDescriptor) && !AbstractEntity.class.isAssignableFrom(getMapValueType(propertyDescriptor)));
  }
  
  public static Class<?> getCollectionOfElementsType(PropertyDescriptor propertyDescriptor)
  {
    assert isCollectionOfElements(propertyDescriptor);
    if (isCollectionBasedProperty(propertyDescriptor)) {
      return getCollectionElementType(propertyDescriptor);
    } 
    else if (isMapBasedProperty(propertyDescriptor)) {
      return getMapValueType(propertyDescriptor);
    }
    throw new DomainModelDefinitionException("not sure how to find the element type for: " + propertyDescriptor.getDisplayName());
  }
  
  public static Class<? extends AbstractEntity> getRelationshipEntityType(PropertyDescriptor propertyDescriptor)
  {
    Class<? extends AbstractEntity> relatedEntityType = null;
    if (isToManyEntityRelationship(propertyDescriptor)) {
      if (isCollectionBasedProperty(propertyDescriptor)) {
        relatedEntityType = (Class<? extends AbstractEntity>) getCollectionElementType(propertyDescriptor);
      }
      else if (isMapBasedProperty(propertyDescriptor)) {
        relatedEntityType = (Class<? extends AbstractEntity>) getMapValueType(propertyDescriptor);
      }
    }
    else if (isToOneEntityRelationship(propertyDescriptor)) {
      relatedEntityType = (Class<? extends AbstractEntity>) propertyDescriptor.getPropertyType();
    }
    if (relatedEntityType == null) {
      throw new DomainModelDefinitionException("not sure how to find the entity type for: " + propertyDescriptor.getDisplayName());
    }
    return relatedEntityType;
  }
  
  public static boolean hasAnnotation(Class<? extends Annotation> annotationClass, PropertyDescriptor propertyDescriptor)
  {
    Method getter = propertyDescriptor.getReadMethod();
    return getter.isAnnotationPresent(annotationClass);
  }

  public static boolean isUnidirectionalRelationshipMethod(Method getter)
  {
    ToOne toOne = getter.getAnnotation(ToOne.class);
    if (toOne != null && toOne.unidirectional()) {
      return true;
    }
    return false;
  }
  
  /**
   * Return true iff this entity class is a subclass of another entity class.
   * @return true iff this entity class is a subclass of another entity class
   */
  public static boolean isEntitySubclass(Class entityClass)
  {
    Class entitySuperclass = entityClass.getSuperclass();
    return ! (
      entitySuperclass.equals(AbstractEntity.class) ||
      entitySuperclass.equals(SemanticIDAbstractEntity.class));
  }
  
  public static boolean isPropertyWithNonconventionalSetterMethod(PropertyDescriptor propertyDescriptor)
  {
    Method getter = propertyDescriptor.getReadMethod();
    Column column = getter.getAnnotation(Column.class);
    return column != null && column.hasNonconventionalSetterMethod();
  }
  
  public static boolean isCollectionWithNonConventionalMutation(PropertyDescriptor propertyDescriptor)
  {
    Method getter = propertyDescriptor.getReadMethod();
    CollectionOfElements collectionOfElements = getter.getAnnotation(CollectionOfElements.class);
    return collectionOfElements != null && collectionOfElements.hasNonconventionalMutation();
  }
  
  public static boolean isToOneRelationshipWithNonConventionalSetter(PropertyDescriptor propertyDescriptor)
  {
    Method getter = propertyDescriptor.getReadMethod();
    ToOne toOne = getter.getAnnotation(ToOne.class);
    return toOne != null && toOne.hasNonconventionalSetterMethod();
  }

  public static boolean isToManyRelationshipWithNonConventionalMutation(PropertyDescriptor propertyDescriptor)
  {
    Method getter = propertyDescriptor.getReadMethod();
    ToMany toMany = getter.getAnnotation(ToMany.class);
    return toMany != null && toMany.hasNonconventionalMutation();
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
  public static Method findAndCheckMethod(Class<? extends AbstractEntity> beanClass,
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
    assertTrue("method not allowed: " + fullMethodName,
               requirement != ExistenceRequirement.NOT_ALLOWED || foundMethod == null);
    assertTrue("method must exist: " + fullMethodName,
               requirement != ExistenceRequirement.REQUIRED || foundMethod != null);
    return foundMethod;
  }

//  public static boolean isContainmentRelationship(Class<? extends AbstractEntity> beanClass,
//                                                  Class<? extends AbstractEntity> relatedEntityType)
//  {
//    ContainedEntity containedEntity = relatedEntityType.getAnnotation(ContainedEntity.class);
//    if (containedEntity != null && containedEntity.containingEntityClass().isAssignableFrom(beanClass)) {
//      return true;
//    }
//    return false;
//  }
}
