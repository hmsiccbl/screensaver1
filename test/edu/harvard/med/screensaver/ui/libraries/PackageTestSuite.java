// $HeadURL:
// http://forge.abcd.harvard.edu/svn/screensaver/branches/iccbl/1.9.5-dev/test/edu/harvard/med/screensaver/ui/screenresults/PackageTestSuite.java
// $
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

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
    addTestSuite(ReagentFinderTest.class);
    addTestSuite(LibraryDetailViewerTest.class);
    addTestSuite(LibraryCopyPlatesBatchEditorTest.class);
  }
}
