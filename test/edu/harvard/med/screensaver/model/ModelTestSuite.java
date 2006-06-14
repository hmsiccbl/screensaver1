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

public class ModelTestSuite extends TestSuite
{

  public static void main(String[] args) {
    junit.textui.TestRunner.run(ModelTestSuite.class);
  }

  public static Test suite() {
    return new ModelTestSuite();
  }

  public ModelTestSuite() {
    addTestSuite(EntityClassesTest.class);
    addTestSuite(JavaBeanEntitiesTest.class);
    addTestSuite(ClassMetadatasTest.class);
  }
}
