// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.users;

import java.beans.IntrospectionException;

import junit.framework.TestSuite;

import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;

public class ChecklistItemEventTest extends AbstractEntityInstanceTest<ChecklistItemEvent>
{
  public static TestSuite suite()
  {
    return buildTestSuite(ChecklistItemEventTest.class, ChecklistItemEvent.class);
  }

  public ChecklistItemEventTest() throws IntrospectionException
  {
    super(ChecklistItemEvent.class);
  }
}

