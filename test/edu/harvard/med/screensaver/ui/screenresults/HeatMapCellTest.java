// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults;

import java.awt.Color;
import java.io.File;
import java.text.NumberFormat;
import java.util.Iterator;

import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultParser;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultParserTest;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;

import org.apache.log4j.Logger;

public class HeatMapCellTest extends AbstractSpringTest
{
  // static members

  private static Logger log = Logger.getLogger(HeatMapCellTest.class);
  
  protected ScreenResultParser screenResultParser;

  public void testHeatMapCell()
  {
    // we need to obtain a ResultValue to pass into our HeatMapCell; the easiest
    // way to do this is simply to parse a ScreenResult data file! Messy, but it
    // works, and it's "just test code", afterall.
    ScreenResult screenResult = 
      screenResultParser.parse(ScreenResultParser.makeDummyScreen(115),
                               new File(ScreenResultParserTest.TEST_INPUT_FILE_DIR, "NewFormatTest.xls"));
    ResultValue anyNonControlWellResultValue = null;
    for (Iterator iter = screenResult.getResultValueTypesList().get(0).getResultValues().iterator(); iter.hasNext();) {
      anyNonControlWellResultValue = (ResultValue) iter.next();
      if (!anyNonControlWellResultValue.isControlWell()) {
        break;
      }
    }
    assert anyNonControlWellResultValue != null : "could not find a non-control result value in test data";

    HeatMapCell cell = new HeatMapCell(anyNonControlWellResultValue,
                                       1.0,
                                       new Color(128, 0, 196),
                                       true,
                                       NumberFormat.getInstance());
    assertEquals("value", "1", cell.getCellText());
    assertEquals("style", "background-color: #8000c4", cell.getStyle());
  }

}

