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

import org.apache.log4j.Logger;

/**
 * Exercise the entities as JavaBeans.
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
            if (
              propertyName.equals("class") ||
              propertyName.startsWith("hbn")) {
              continue;
            }
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
        exercizor.exercizeJavaBeanEntity(
          newInstance(entityClass),
          Introspector.getBeanInfo(entityClass));
      }
      catch (IntrospectionException e) {
        e.printStackTrace();
        fail(e.getMessage());
      }
    }    
  }

  protected static Map<String, String> oddPluralToSingularPropertiesMap =
    new HashMap<String, String>();
  static {
    oddPluralToSingularPropertiesMap.put("children", "child");
    oddPluralToSingularPropertiesMap.put("copies", "copy");
    oddPluralToSingularPropertiesMap.put("typesDerivedFrom", "typeDerivedFrom");
  }
  protected static Map<String, String> oddSingularToPluralPropertiesMap =
    new HashMap<String, String>();
  static {
    oddSingularToPluralPropertiesMap.put("child", "children");
    oddSingularToPluralPropertiesMap.put("copy", "copies");
    oddSingularToPluralPropertiesMap.put("typeDerivedFrom", "typesDerivedFrom");
  }
  
  protected static Map<String, String> oddPropertyToRelatedPropertyMap =
    new HashMap<String, String>();
  protected static Map<String, String> oddPluralPropertyToRelatedPropertyMap =
    new HashMap<String, String>();
  static {
    oddPluralPropertyToRelatedPropertyMap.put("derivedTypes", "typesDerivedFrom");
    oddPluralPropertyToRelatedPropertyMap.put("typesDerivedFrom", "derivedTypes");
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
    boolean isRequiredMethod)
  {
    String fullMethodName = beanClass.getName() + "." + methodName;
    Method foundMethod = null;
    for (Method method : beanClass.getDeclaredMethods()) {
      if (method.getName().equals(methodName)) {
        foundMethod = method;
        break;
      }
    }
    if (! isRequiredMethod && foundMethod == null) {
      return null;
    }
    assertNotNull(
      "collection property missing method: " + fullMethodName,
      foundMethod);
    assertEquals(
      "collection property method returns boolean: " + fullMethodName,
      foundMethod.getReturnType(),
      Boolean.TYPE);
    return foundMethod;
  }
}
