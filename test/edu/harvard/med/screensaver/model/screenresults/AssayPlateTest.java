// $HeadURL: http://forge.abcd.harvard.edu/svn/screensaver/branches/iccbl/screening-status/test/edu/harvard/med/screensaver/model/screenresults/AssayWellTest.java $
// $Id: AssayWellTest.java 3968 2010-04-08 17:04:35Z atolopko $
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screenresults;

import java.beans.IntrospectionException;

import junit.framework.TestSuite;

import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;

public class AssayPlateTest extends AbstractEntityInstanceTest<AssayPlate>
{
  public static TestSuite suite()
  {
    return buildTestSuite(AssayPlateTest.class, AssayPlate.class);
  }

  public AssayPlateTest() throws IntrospectionException
  {
    super(AssayPlate.class);
  }
}

