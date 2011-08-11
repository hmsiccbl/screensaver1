// $HeadURL: http://forge.abcd.harvard.edu/svn/screensaver/branches/atolopko/2189/core/src/test/java/edu/harvard/med/screensaver/model/AdministrativeActivityTest.java $
// $Id: AdministrativeActivityTest.java 6042 2011-06-22 13:39:27Z atolopko $
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.activities;

import junit.framework.TestSuite;

import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;

public class ServiceActivityTest extends AbstractEntityInstanceTest<ServiceActivity>
{
  public static TestSuite suite()
  {
    return buildTestSuite(ServiceActivityTest.class, ServiceActivity.class);
  }

  public ServiceActivityTest()
  {
    super(ServiceActivity.class);
  }
}

