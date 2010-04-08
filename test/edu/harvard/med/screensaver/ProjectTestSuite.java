// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ProjectTestSuite extends TestSuite
{
  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }

  public static Test suite()
  {
    return new ProjectTestSuite();
  }

  public ProjectTestSuite()
  {
    addTest(edu.harvard.med.screensaver.analysis.heatmaps.PackageTestSuite.suite());
    addTest(edu.harvard.med.screensaver.db.PackageTestSuite.suite());
    addTest(edu.harvard.med.screensaver.io.PackageTestSuite.suite());
    if (!Boolean.getBoolean("no.model.tests")) {
      addTest(edu.harvard.med.screensaver.model.PackageTestSuite.suite());
    }
    addTest(edu.harvard.med.screensaver.service.PackageTestSuite.suite());
    addTest(edu.harvard.med.screensaver.ui.PackageTestSuite.suite());
    addTest(edu.harvard.med.screensaver.util.PackageTestSuite.suite());
    addTest(edu.harvard.med.iccbl.screensaver.PackageTestSuite.suite());
  }
}
