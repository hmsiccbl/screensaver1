// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TestSuite extends TestSuite
{

  public static void main(String[] args) {
    junit.textui.TestRunner.run(TestSuite.class);
  }

  public static Test suite() {
    return new TestSuite();
  }

  public TestSuite() {
    addTestSuite(JavaBeanEntitiesTest.class);
    addTestSuite(EntityClassesTest.class);
    addTestSuite(ClassMetadatasTest.class);
  }
}
