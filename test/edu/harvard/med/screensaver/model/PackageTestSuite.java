// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import edu.harvard.med.screensaver.model.screenresults.ResultValueTest;

import junit.framework.Test;
import junit.framework.TestSuite;

public class PackageTestSuite extends TestSuite
{

  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(PackageTestSuite.class);
  }
  
  public static Test suite()
  {
    return new PackageTestSuite();
  }

  public PackageTestSuite()
  {
    addTestSuite(AbstractEntityClassTest.class);

    // test entity model classes (ignoring abstract classes)
    addTestSuite(edu.harvard.med.screensaver.model.users.ChecklistItemTypeTest.class);
    addTestSuite(edu.harvard.med.screensaver.model.users.LabAffiliationTest.class);
    //addTestSuite(edu.harvard.med.screensaver.model.users.ScreensaverUserTest.class);
    addTestSuite(edu.harvard.med.screensaver.model.users.ScreeningRoomUserTest.class);
    addTestSuite(edu.harvard.med.screensaver.model.users.AdministratorUserTest.class);
    addTestSuite(edu.harvard.med.screensaver.model.users.ChecklistItemTest.class);
    addTestSuite(edu.harvard.med.screensaver.model.libraries.LibraryTest.class);
    addTestSuite(edu.harvard.med.screensaver.model.libraries.CopyTest.class);
    addTestSuite(edu.harvard.med.screensaver.model.libraries.CopyActionTest.class);
    addTestSuite(edu.harvard.med.screensaver.model.libraries.CompoundTest.class);
    addTestSuite(edu.harvard.med.screensaver.model.libraries.SilencingReagentTest.class);
    addTestSuite(edu.harvard.med.screensaver.model.libraries.GeneTest.class);
    addTestSuite(edu.harvard.med.screensaver.model.libraries.WellTest.class);
    addTestSuite(edu.harvard.med.screensaver.model.libraries.CopyInfoTest.class);
    addTestSuite(edu.harvard.med.screensaver.model.screens.AbaseTestsetTest.class);
    addTestSuite(edu.harvard.med.screensaver.model.screens.ScreenerCherryPickTest.class);
    addTestSuite(edu.harvard.med.screensaver.model.screens.LabCherryPickTest.class);
    addTestSuite(edu.harvard.med.screensaver.model.screens.ScreenTest.class);
    addTestSuite(edu.harvard.med.screensaver.model.screens.PlatesUsedTest.class);
    //addTestSuite(edu.harvard.med.screensaver.model.screens.CherryPickRequestTest.class);
    addTestSuite(edu.harvard.med.screensaver.model.screens.CompoundCherryPickRequestTest.class);
    addTestSuite(edu.harvard.med.screensaver.model.screens.RNAiCherryPickRequestTest.class);
    addTestSuite(edu.harvard.med.screensaver.model.screens.BillingInformationTest.class);
    addTestSuite(edu.harvard.med.screensaver.model.screens.PublicationTest.class);
    //addTestSuite(edu.harvard.med.screensaver.model.screens.ScreeningRoomActivityTest.class);
    addTestSuite(edu.harvard.med.screensaver.model.screens.CherryPickAssayPlateTest.class);
    addTestSuite(edu.harvard.med.screensaver.model.screens.CherryPickLiquidTransferTest.class);
    addTestSuite(edu.harvard.med.screensaver.model.screens.LibraryScreeningTest.class);
    addTestSuite(edu.harvard.med.screensaver.model.screens.RNAiCherryPickScreeningTest.class);
    addTestSuite(edu.harvard.med.screensaver.model.screens.AbaseTestsetTest.class);
    addTestSuite(edu.harvard.med.screensaver.model.screens.BillingItemTest.class);
    addTestSuite(edu.harvard.med.screensaver.model.screens.LetterOfSupportTest.class);
    addTestSuite(edu.harvard.med.screensaver.model.screens.EquipmentUsedTest.class);
    addTestSuite(edu.harvard.med.screensaver.model.screens.RNAiKnockdownConfirmationTest.class);
    addTestSuite(edu.harvard.med.screensaver.model.screens.CherryPickRequestTest.class);
    addTestSuite(edu.harvard.med.screensaver.model.screens.AttachedFileTest.class);
    addTestSuite(edu.harvard.med.screensaver.model.screens.StatusItemTest.class);
    addTestSuite(edu.harvard.med.screensaver.model.screenresults.ResultValueTypeTest.class);
    addTestSuite(edu.harvard.med.screensaver.model.screenresults.ResultValueTest.class);
    addTestSuite(edu.harvard.med.screensaver.model.screenresults.ScreenResultTest.class);
    addTestSuite(edu.harvard.med.screensaver.model.derivatives.DerivativeTest.class);
    addTestSuite(edu.harvard.med.screensaver.model.derivatives.DerivativeScreenResultTest.class);

    // test non-entity model classes
    addTestSuite(edu.harvard.med.screensaver.model.libraries.WellKeyTest.class);
    addTestSuite(ResultValueTest.class);
  }
}