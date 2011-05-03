// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Validates the top-level Spring context configuration files. This is particularly useful for the web application
 * context files, since errors are only reported by Tomcat as a mysterious "SEVERE: Error listenerStart" line
 * in the catalina.out log. These tests should help to identify the underlying error. The system property
 * "test.spring.contexts" must be set to true for these tests to be exercised.
 * <i><b>DO NOT RUN THIS TEST WHEN RUNNING OTHER TESTS (AS A SUITE)</b></i>
 */
// WARNING: running this test when running full project test suite causes problems, 
// because we are recreating an application context using different system properties, 
// which is a very ill-behaved thing to do as it effects subsequent test fixtures!
public class SpringContextTest extends TestCase
{
  private static Logger log = Logger.getLogger(SpringContextTest.class);

  public void testDefaultSpringWebContext()
  {
    doTestSpringContext("spring-context-web-default.xml", "cfg/screensaver.properties.web");
  }

  public void testIccblSpringWebContext()
  {
    doTestSpringContext("spring-context-web-iccbl.xml", "cfg/screensaver.properties.web");
  }

  public void testCommandLineApplicationSpringContext()
  {
    doTestSpringContext("spring-context-cmdline.xml", "cfg/screensaver.properties.util");
  }

  private void doTestSpringContext(String contextFile, String propertiesFile)
  {
    if (!Boolean.getBoolean("test.spring.contexts")) return;
    try {
      ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(contextFile);
      context.start();
    }
    catch(Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }
}
