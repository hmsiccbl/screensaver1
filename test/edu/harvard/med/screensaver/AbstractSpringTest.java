// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver;

import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * A simple wrapper for the
 * <code>AbstractDependencyInjectionSpringContextTests</code> that implements
 * the abstract method
 * {@link org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations}
 * by loading our <code>spring-context-test.xml</code> resource file.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public abstract class AbstractSpringTest
extends AbstractDependencyInjectionSpringContextTests
{
  protected LogConfigurer logConfigurer;

  /**
   * Spring configuration will be loaded from the configuration file(s)
   * specified in this constant.
   */
  private static final String[] SPRING_CONFIG_FILES = new String [] {
    "spring-context-test.xml",
  };


  public AbstractSpringTest(String testName)
  {
    super(testName);
    // have AbstractDependencyInjectionSpringContextTests inject the properties
    // we need into protected data members that share the same name as beans in
    // our Spring configuration files.
    setPopulateProtectedVariables(true);
  }

  public AbstractSpringTest()
  {
    // have AbstractDependencyInjectionSpringContextTests inject the properties
    // we need into protected data members that share the same name as beans in
    // our Spring configuration files.
    setPopulateProtectedVariables(true);
  }

  @Override
  protected String[] getConfigLocations() {
    return SPRING_CONFIG_FILES;
  }
}
