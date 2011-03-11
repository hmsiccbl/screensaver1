// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.cherrypicks;

import junit.framework.TestSuite;

import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;

public class LegacyCherryPickAssayPlateTest extends AbstractEntityInstanceTest<LegacyCherryPickAssayPlate>
{
  public static TestSuite suite()
  {
    return buildTestSuite(LegacyCherryPickAssayPlateTest.class, LegacyCherryPickAssayPlate.class);
  }

  public LegacyCherryPickAssayPlateTest()
  {
    super(LegacyCherryPickAssayPlate.class);
  }
}

