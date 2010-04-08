// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.users;

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
    addTest(ChecklistItemEventTest.suite());
    addTest(ChecklistItemTest.suite());
    addTest(LabAffiliationTest.suite());
    addTest(ScreeningRoomUserTest.suite());
    addTest(LabHeadTest.suite());
    addTest(AdministratorUserTest.suite());
    
    addTestSuite(ScreensaverUserTest.class);
    addTestSuite(ScreensaverUserRoleTest.class);
  }
}