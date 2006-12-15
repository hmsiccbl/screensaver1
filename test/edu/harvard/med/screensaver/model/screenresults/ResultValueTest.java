// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screenresults;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

public class ResultValueTest extends TestCase
{
  // static members

  private static Logger log = Logger.getLogger(ResultValueTest.class);


  // instance data members

  // public constructors and methods
  
  public void testResultValueNumercPrecision()
  {
    ResultValue rv = new ResultValue(5.0123, 3);
    assertEquals("default decimal precision formatted string", "5.012", rv.getValue());
    assertEquals("default decimal precision formatted string", "5.0123", rv.formatNumericValue(4));
    assertEquals("default decimal precision formatted string", "5", rv.formatNumericValue(0));
    assertEquals("default decimal precision formatted string", "5.0123000000", rv.formatNumericValue(10));
  }

  // private methods

}

