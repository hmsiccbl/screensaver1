// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.test.model.meta;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Immutable;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.SemanticIDAbstractEntity;
import edu.harvard.med.screensaver.model.annotations.Column;
import edu.harvard.med.screensaver.model.annotations.ContainedEntity;
import edu.harvard.med.screensaver.model.annotations.Derived;
import edu.harvard.med.screensaver.model.annotations.ElementCollection;
import edu.harvard.med.screensaver.model.annotations.IgnoreImmutabilityTest;
import edu.harvard.med.screensaver.model.annotations.ToMany;
import edu.harvard.med.screensaver.model.annotations.ToOne;
import edu.harvard.med.screensaver.util.DevelopmentException;
import edu.harvard.med.screensaver.util.StringUtils;

public class ModelIntrospectionUtil
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
    

    if (isImmutableProperty(beanClass, propertyDescriptor)) {
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
    JoinColumn jpaJoinColumn = getter.getAnnotation(JoinColumn.class);
    ManyToOne manyToOne = getter.getAnnotation(ManyToOne.class);
    OneToOne oneToOne = getter.getAnnotation(OneToOne.class);
    return (jpaColumn != null && !jpaColumn.nullable()) ||
      (jpaJoinColumn != null && !jpaJoinColumn.nullable()) ||
      (manyToOne != null && !manyToOne.optional()) ||
      (oneToOne != null && !oneToOne.optional());
  }

  public static boolean isTransientProperty(PropertyDescriptor propertyDescriptor)
  {
    return hasAnnotation(Transient.class, propertyDescriptor);
  }
  
  public static boolean isImmutableIgnoreTests(Class<? extends AbstractEntity> beanClass)
  {
  	return  beanClass.getAnnotation(Immutable.class) != null && beanClass.getAnnotation(IgnoreImmutabilityTest.class) != null;
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
    Method getter = propertyDescriptor.getReadMethod();
    javax.persistence.Column columnAnnot = getter.getAnnotation(javax.persistence.Column.class);
    return columnAnnot != null && !columnAnnot.updatable();
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
    ElementCollection elementCollection = getter.getAnnotation(ElementCollection.class);
    return elementCollection != null && elementCollection.hasNonconventionalMutation();
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
   * Check whether a method with the given name adheres to its existence requirement.
   * 
   * @return the method, if it exists and is allowed to exist; null if method does not exist and its existence is not
   *         required
   * @throws DevelopmentException if method existence is required and no such method exists OR if method existence is
   *           not
   *           allowed and the method exists
   */
  public static Method findAndCheckMethod(Class<? extends AbstractEntity> beanClass,
                                          String methodName,
                                          ExistenceRequirement requirement)
  {
    return findAndCheckMethod(beanClass, methodName, requirement, (Class<?>[]) null);
  }

  /**
   * Check whether a method with the given name and specified parameter types adheres to its existence requirement.
   * 
   * @return the method, if it exists and is allowed to exist; null if method does not exist and its existence is not
   *         required
   * @throws DevelopmentException if method existence is required and no such method exists OR if method existence is
   *           not
   *           allowed and the method exists
   */
  public static Method findAndCheckMethod(Class<? extends AbstractEntity> beanClass,
                                          String methodName, 
                                          ExistenceRequirement requirement,
                                          Class<?>... paramTypes)
  {
    String fullMethodName = beanClass.getName() + "." + methodName;
    Method foundMethod = null;
    
    if (paramTypes != null) {
      // match a specific parameter list, if provided
	    try {
	    	foundMethod = beanClass.getMethod(methodName, paramTypes);
      }
      catch (NoSuchMethodException e) {}
    }
    else if (foundMethod == null) {
      // perform a name-based search that ignores the parameter list
	    // note: we're calling getMethods() instead of getDeclaredMethods() to allow
      // inherited methods to satisfy our isRequiredMethod constraint
	    // TODO: getMethods() will only return public methods, is this okay?
	    for (Method method : beanClass.getMethods()) {
	      if (method.getName().equals(methodName)) {
	        foundMethod = method;
	        break;
	      }
	    }
    }
    
    if (requirement != ExistenceRequirement.REQUIRED && foundMethod == null) {
      log.debug("findAndCheckMethod(): non-required method was not found: " + fullMethodName);
      return null;
    }
    if (!(requirement != ExistenceRequirement.NOT_ALLOWED || foundMethod == null)) {
      throw new DevelopmentException("method not allowed: " + fullMethodName);
    }
    if (!(requirement != ExistenceRequirement.REQUIRED || foundMethod != null)) {
      throw new DevelopmentException("method must exist: " + fullMethodName);
    }
    return foundMethod;
  }

  public static boolean isDerivedProperty(PropertyDescriptor propertyDescriptor)
  {
    return hasAnnotation(Derived.class, propertyDescriptor);
  }

  public static Class<? extends AbstractEntity> getParent(Class<? extends AbstractEntity> beanClass)
  {
    ContainedEntity containedEntity = beanClass.getAnnotation(ContainedEntity.class);
    if (containedEntity != null) {
      return containedEntity.containingEntityClass();
    }
    return null;
  }

  public static boolean isAutoCreatedByParent(Class<? extends AbstractEntity> beanClass)
  {
    ContainedEntity containedEntity = beanClass.getAnnotation(ContainedEntity.class);
    if (containedEntity != null) {
      return containedEntity.autoCreated();
    }
    return false;
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
