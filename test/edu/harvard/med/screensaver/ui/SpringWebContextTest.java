// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import java.io.FileReader;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Validates the Spring context web configuration files. When the files contain
 * errors, Tomcat only reports a mysterious "SEVERE: Error listenerStart" error
 * in the catalina.out log.  Run this test to identify the real error.
 */
public class SpringWebContextTest extends TestCase
{
  private static Logger log = Logger.getLogger(SpringWebContextTest.class);

  public void testSpringWebContext()
  {
    try {
      System.getProperties().load(new FileReader("cfg/screensaver.properties.web"));
      ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring-context-web.xml");
      context.start();
    }
    catch(Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }
}
