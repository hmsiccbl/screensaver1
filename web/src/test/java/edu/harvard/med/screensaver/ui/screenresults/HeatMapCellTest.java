// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults;

import java.awt.Color;
import java.text.NumberFormat;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import edu.harvard.med.screensaver.io.screenresults.ScreenResultParser;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.LibraryWellType;
import edu.harvard.med.screensaver.model.libraries.PlateSize;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.AssayWell;
import edu.harvard.med.screensaver.model.screenresults.DataColumn;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.test.AbstractSpringTest;
import edu.harvard.med.screensaver.test.MakeDummyEntities;
import edu.harvard.med.screensaver.ui.screenresults.heatmaps.HeatMapCell;

public class HeatMapCellTest extends AbstractSpringTest
{
  private static Logger log = Logger.getLogger(HeatMapCellTest.class);

  @Autowired
  protected ScreenResultParser screenResultParser;

  public void testHeatMapCell()
  {
    Screen screen = MakeDummyEntities.makeDummyScreen(115);
    ScreenResult screenResult = screen.createScreenResult();
    DataColumn col = screenResult.createDataColumn("col1");
    Library library = new Library((AdministratorUser) screen.getCreatedBy(),
                                  "library 1",
                                  "lib1",
                                  ScreenType.SMALL_MOLECULE,
                                  LibraryType.COMMERCIAL,
                                  1,
                                  1,
                                  PlateSize.WELLS_384);
    Well well = library.createWell(new WellKey(1, "A01"), LibraryWellType.EXPERIMENTAL);
    AssayWell assayWell = screenResult.createAssayWell(well);
    col.createResultValue(assayWell, 1.0);

    HeatMapCell cell = new HeatMapCell(col.getWellKeyToResultValueMap().get(well.getWellKey()),
                                       well.getWellKey(),
                                       1.0,
                                       new Color(128, 0, 196),
                                       true,
                                       NumberFormat.getInstance());
    assertEquals("value", "1", cell.getCellText());
    assertEquals("style", "background-color: #8000c4", cell.getStyle());
  }

}

