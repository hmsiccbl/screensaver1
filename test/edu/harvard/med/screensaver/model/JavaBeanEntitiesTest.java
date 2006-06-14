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
import java.beans.PropertyDescriptor;

/**
 * Test the entities as JavaBeans.
 */
public class JavaBeanEntitiesTest extends JavaBeanEntitiesExercizor
{
  
  public void testJavaBeanEntitiesTemplate()
  {
    exercizeJavaBeanEntities(new JavaBeanEntityExercizor()
      {
        public void exercizeJavaBeanEntity(
          AbstractEntity bean,
          BeanInfo beanInfo)
        {
          // copy this method and put your code here
        }
      });
  }
  
  public void testUninitializedPropertiesReturnNull()
  {
    exercizePropertyDescriptors(new PropertyDescriptorExercizor()
      {
        public void exercizePropertyDescriptor(
          AbstractEntity bean,
          BeanInfo beanInfo,
          PropertyDescriptor propertyDescriptor)
        {
          System.out.println("bean = " + bean.getClass());
          System.out.println("info = " + beanInfo);
          System.out.println("prop = " + propertyDescriptor.getReadMethod().getName());
        }
      });
  }
}
