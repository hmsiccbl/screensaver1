// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit38.AbstractJUnit38SpringContextTests;

@ContextConfiguration({ "/spring-context-test.xml" })
public abstract class AbstractSpringTest
  extends AbstractJUnit38SpringContextTests
{
  @Autowired protected LogConfigurer logConfigurer;

  public AbstractSpringTest(String testName)
  {
    super(testName);
  }

  public AbstractSpringTest()
  {
  }
}
