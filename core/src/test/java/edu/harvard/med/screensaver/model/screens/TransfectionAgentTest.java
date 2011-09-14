// $HeadURL: http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/trunk/core/src/test/java/edu/harvard/med/screensaver/model/screens/FundingSupportTest.java $
// $Id: FundingSupportTest.java 5492 2011-03-11 20:39:01Z atolopko $
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import java.beans.IntrospectionException;

import junit.framework.TestSuite;

import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;

public class TransfectionAgentTest extends AbstractEntityInstanceTest<TransfectionAgent>
{
  public static TestSuite suite()
  {
    return buildTestSuite(TransfectionAgentTest.class, TransfectionAgent.class);
  }

  public TransfectionAgentTest()
  {
    super(TransfectionAgent.class);
  }
}
