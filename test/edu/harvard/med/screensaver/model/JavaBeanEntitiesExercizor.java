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

/**
 * Exercise the entities as JavaBeans.
 */
abstract class JavaBeanEntitiesExercizor extends EntityClassesExercisor
{
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
}
