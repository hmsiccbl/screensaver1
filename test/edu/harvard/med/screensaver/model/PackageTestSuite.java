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

public class PackageTestSuite extends TestSuite
{
  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }
  
  public static Test suite()
  {
    return new PackageTestSuite();
  }

  public PackageTestSuite()
  {
    addTestSuite(AbstractEntityTest.class);
    addTestSuite(ModelTestCoverageTest.class);

    // ignored abstract classes
    // addTestSuite(ActivityTest.class);
    // addTestSuite(AdministrativeActivityTest.class);
    
    // test model subpackages
    addTest(edu.harvard.med.screensaver.model.cherrypicks.PackageTestSuite.suite());
    addTest(edu.harvard.med.screensaver.model.derivatives.PackageTestSuite.suite());
    addTest(edu.harvard.med.screensaver.model.libraries.PackageTestSuite.suite());
    addTest(edu.harvard.med.screensaver.model.screens.PackageTestSuite.suite());
    addTest(edu.harvard.med.screensaver.model.screenresults.PackageTestSuite.suite());
    addTest(edu.harvard.med.screensaver.model.users.PackageTestSuite.suite());
  }
}