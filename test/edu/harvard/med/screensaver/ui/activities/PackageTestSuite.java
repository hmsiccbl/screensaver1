// $HeadURL: http://forge.abcd.harvard.edu/svn/screensaver/branches/iccbl/screening-status/test/edu/harvard/med/screensaver/ui/PackageTestSuite.java $
// $Id: PackageTestSuite.java 3968 2010-04-08 17:04:35Z atolopko $
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.activities;

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
    addTestSuite(PlateRangeTest.class);
  }
}
