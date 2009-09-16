// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/test/edu/harvard/med/screensaver/model/PackageTestSuite.java $
// $Id: PackageTestSuite.java 1654 2007-08-02 14:02:19Z s $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

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
    // test entity model classes
    addTest(CopyTest.suite());
    addTest(CopyActionTest.suite());
    addTest(CopyInfoTest.suite());
    addTest(GeneTest.suite());
    addTest(LibraryTest.suite());
    addTest(LibraryContentsVersionTest.suite());
    addTest(ReagentTest.suite());
    addTest(SilencingReagentTest.suite());
    addTest(SmallMoleculeReagentTest.suite());
    addTest(NaturalProductReagentTest.suite());
    addTest(WellTest.suite());
    addTest(WellVolumeAdjustmentTest.suite());
    addTest(WellVolumeCorrectionActivityTest.suite());

    // test non-entity model classes
    addTestSuite(WellNameTest.class);
    addTestSuite(WellKeyTest.class);
  }
}