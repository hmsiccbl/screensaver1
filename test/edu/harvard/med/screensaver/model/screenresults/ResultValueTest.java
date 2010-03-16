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

import junit.framework.TestSuite;

import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryWellType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screens.ScreenType;

public class ResultValueTest extends AbstractEntityInstanceTest<ResultValue>
{
  public static TestSuite suite()
  {
    return buildTestSuite(ResultValueTest.class, ResultValue.class);
  }

  public ResultValueTest() throws IntrospectionException
  {
    super(ResultValue.class);
  }

  public void testResultValueNumericDecimalPlaces()
  {
    ScreenResult screenResult =
      MakeDummyEntities.makeDummyScreen(1).createScreenResult();
    Library library = MakeDummyEntities.makeDummyLibrary(1, ScreenType.SMALL_MOLECULE, 1);
    Well well = library.createWell(new WellKey("00001:A01"), LibraryWellType.EXPERIMENTAL);
    AssayWell assayWell = screenResult.createAssayWell(well, AssayWellType.EXPERIMENTAL);
    DataColumn col = screenResult.createDataColumn("col").makeNumeric(3);
    ResultValue rv = col.createResultValue(assayWell, 5.0123, true);
    assertEquals("default decimal places formatted string", "5.012", rv.getValue());
    assertEquals("default decimal places formatted string", "5.0123", rv.formatNumericValue(4));
    assertEquals("default decimal places formatted string", "5", rv.formatNumericValue(0));
    assertEquals("default decimal places formatted string", "5.0123000000", rv.formatNumericValue(10));
  }
}

