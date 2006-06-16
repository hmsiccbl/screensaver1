// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver;

import java.io.FileNotFoundException;

import org.apache.log4j.Level;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.springframework.util.Log4jConfigurer;

/**
 * A simple wrapper for the
 * <code>AbstractDependencyInjectionSpringContextTests</code> that implements
 * the abstract method {@link org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations}
 * by loading our <code>spring-context-persistence.xml</code> resource file.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public abstract class AbstractSpringTest
  extends AbstractDependencyInjectionSpringContextTests
{
  
  static {
    try {
      Log4jConfigurer.initLogging("classpath:log4j.properties");
      org.apache.log4j.Logger.getRootLogger().setLevel(Level.DEBUG);
    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }
  
  /**
   * Spring configuration will be loaded from the configuration file(s)
   * specified in this constant.
   */
   private static final String[] SPRING_CONFIG_FILES = new String[] {
     "spring-context-persistence.xml"
   };
   
   
   public AbstractSpringTest() {
     // have AbstractDependencyInjectionSpringContextTests inject the properties
     // we need into protected data members that share the same name as beans in
     // our Spring configuration files.
     setPopulateProtectedVariables(true);
   }

   /* (non-Javadoc)
    * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
    */
   @Override
   protected String[] getConfigLocations() {
     return SPRING_CONFIG_FILES;
   }
}
