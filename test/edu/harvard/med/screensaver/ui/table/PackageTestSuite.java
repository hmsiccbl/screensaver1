// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/test/edu/harvard/med/screensaver/ui/PackageTestSuite.java $
// $Id: PackageTestSuite.java 691 2006-10-25 01:47:13Z ant4 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table;

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
    addTestSuite(CriterionTest.class);
    addTestSuite(TableColumnManagerTest.class);
    addTestSuite(DataTableTest.class);
    addTest(edu.harvard.med.screensaver.ui.table.model.PackageTestSuite.suite());
  }
}
