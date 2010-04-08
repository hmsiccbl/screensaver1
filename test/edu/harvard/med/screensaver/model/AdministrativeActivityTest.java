// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import java.beans.IntrospectionException;

import junit.framework.TestSuite;

import edu.harvard.med.screensaver.model.users.AdministratorUser;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

public class AdministrativeActivityTest extends AbstractEntityInstanceTest<AdministrativeActivity>
{
  public static TestSuite suite()
  {
    return buildTestSuite(AdministrativeActivityTest.class, AdministrativeActivity.class);
  }

  public AdministrativeActivityTest() throws IntrospectionException
  {
    super(AdministrativeActivity.class);
  }
}

