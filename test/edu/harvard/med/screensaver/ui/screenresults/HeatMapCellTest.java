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

import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultParser;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellType;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.ui.screenresults.heatmaps.HeatMapCell;

import org.apache.log4j.Logger;

public class HeatMapCellTest extends AbstractSpringTest
{
  private static Logger log = Logger.getLogger(HeatMapCellTest.class);

  protected ScreenResultParser screenResultParser;

  public void testHeatMapCell()
  {
    Screen screen = MakeDummyEntities.makeDummyScreen(115);
    ScreenResult screenResult = screen.createScreenResult();
    ResultValueType rvt = screenResult.createResultValueType("rvt1");
    Library library = new Library("library 1", "lib1", ScreenType.SMALL_MOLECULE, LibraryType.COMMERCIAL, 1, 1);
    Well well = library.createWell(new WellKey(1, "A01"), WellType.EMPTY);
    rvt.createResultValue(well, "1.0");

    HeatMapCell cell = new HeatMapCell(rvt.getWellKeyToResultValueMap().get(well.getWellKey()),
                                       well.getWellKey(),
                                       1.0,
                                       new Color(128, 0, 196),
                                       true,
                                       NumberFormat.getInstance());
    assertEquals("value", "1", cell.getCellText());
    assertEquals("style", "background-color: #8000c4", cell.getStyle());
  }

}

