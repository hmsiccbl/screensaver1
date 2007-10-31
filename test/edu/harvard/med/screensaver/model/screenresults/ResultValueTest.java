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

import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screens.ScreenType;

import org.apache.log4j.Logger;

public class ResultValueTest extends TestCase
{
  private static Logger log = Logger.getLogger(ResultValueTest.class);

  public void testResultValueNumericPrecision()
  {
    ScreenResult screenResult =
      MakeDummyEntities.makeDummyScreenResult(MakeDummyEntities.makeDummyScreen(1),
                                              MakeDummyEntities.makeDummyLibrary(1,
                                                                                 ScreenType.SMALL_MOLECULE,
                                                                                 1));
    Well well = screenResult.getWells().first();
    ResultValue rv = screenResult.getResultValueTypes().first().createResultValue(well, AssayWellType.EXPERIMENTAL, 5.0123, 3, true);
    assertEquals("default decimal precision formatted string", "5.012", rv.getValue());
    assertEquals("default decimal precision formatted string", "5.0123", rv.formatNumericValue(4));
    assertEquals("default decimal precision formatted string", "5", rv.formatNumericValue(0));
    assertEquals("default decimal precision formatted string", "5.0123000000", rv.formatNumericValue(10));
  }
}

