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
import java.text.NumberFormat;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.ui.view.screenresults.HeatMapCell;

public class HeatMapCellTest extends TestCase
{
  // static members

  private static Logger log = Logger.getLogger(HeatMapCellTest.class);

  public void testHeatMapCell()
  {
    HeatMapCell cell = new HeatMapCell(null,
                                       1.0,
                                       new Color(128, 0, 196),
                                       NumberFormat.getInstance());
    assertEquals("value", "1.000", cell.getValue());
    assertEquals("hex color", "#8000C4".toLowerCase(), cell.getHexColor());
  }

}

