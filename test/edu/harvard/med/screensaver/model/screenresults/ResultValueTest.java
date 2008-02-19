// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screenresults;

import java.beans.IntrospectionException;
import java.util.Date;

import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellType;
import edu.harvard.med.screensaver.model.screens.ScreenType;

import org.apache.log4j.Logger;

public class ResultValueTest extends AbstractEntityInstanceTest<ResultValue>
{
  private static Logger log = Logger.getLogger(ResultValueTest.class);

  public ResultValueTest() throws IntrospectionException
  {
    super(ResultValue.class);
  }

  public void testResultValueNumericPrecision()
  {
    ScreenResult screenResult =
      MakeDummyEntities.makeDummyScreen(1).createScreenResult(new Date());
    Library library = MakeDummyEntities.makeDummyLibrary(1, ScreenType.SMALL_MOLECULE, 1);
    Well well = library.createWell(new WellKey("00001:A01"), WellType.EXPERIMENTAL);
    ResultValueType rvt = screenResult.createResultValueType("rvt");
    ResultValue rv = rvt.createResultValue(well, AssayWellType.EXPERIMENTAL, 5.0123, 3, true);
    assertEquals("default decimal precision formatted string", "5.012", rv.getValue());
    assertEquals("default decimal precision formatted string", "5.0123", rv.formatNumericValue(4));
    assertEquals("default decimal precision formatted string", "5", rv.formatNumericValue(0));
    assertEquals("default decimal precision formatted string", "5.0123000000", rv.formatNumericValue(10));
  }
}

