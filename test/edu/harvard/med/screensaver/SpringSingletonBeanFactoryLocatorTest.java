// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver;

import junit.framework.TestCase;

import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.beans.factory.access.SingletonBeanFactoryLocator;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringSingletonBeanFactoryLocatorTest extends TestCase
{
  /**
   * Tests whether the bean instances returned by SingletonBeanFactoryLocator
   * are the same instances returned by the Spring ApplicationContext that was
   * instantiated programatically. This test is really just for pedagogical
   * purposes, and not to confirm the correct behavior of our application code.
   */
  public void testSingletonBeanFactoryLocator()
  {
    String someSingletonBeanName = "eCommonsAuthenticationClient";
    
    ClassPathXmlApplicationContext appCtx = new ClassPathXmlApplicationContext("classpath*:spring-context.xml");
    Object bean1 = appCtx.getBean(someSingletonBeanName);

    BeanFactoryLocator bfl = SingletonBeanFactoryLocator.getInstance("meta-spring-context.xml");
    BeanFactoryReference bf = bfl.useBeanFactory("edu.harvard.med.screensaver.Screensaver");
    Object bean2 = bf.getFactory().getBean(someSingletonBeanName);
    
    assertSame(bean1, bean2);
  }
}
