// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.cherrypicks;

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
    // ignored abstract classes
    // addTest(CherryPickRequestTest.suite());
    
    // test entity model classes (ignoring abstract classes)
    addTest(CherryPickAssayPlateTest.suite());
    addTest(CherryPickLiquidTransferTest.suite());
    addTest(SmallMoleculeCherryPickRequestTest.suite());
    addTest(LabCherryPickTest.suite());
    addTest(LegacyCherryPickAssayPlateTest.suite());
    addTest(RNAiCherryPickRequestTest.suite());
    addTest(RNAiKnockdownConfirmationTest.suite());
    addTest(ScreenerCherryPickTest.suite());
  }
}