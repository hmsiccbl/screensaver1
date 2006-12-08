// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultParser;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;

public class UniqueDataHeaderNamesTest extends AbstractSpringTest
{

  public void testClass()
  {
    List<String> expectedUniqueNames = new ArrayList<String>();
    expectedUniqueNames.add("Luminescence (1)");
    expectedUniqueNames.add("Luminescence (2)");
    expectedUniqueNames.add("FI (1)");
    expectedUniqueNames.add("FI (2)");
    expectedUniqueNames.add("Assay Indicator");

    Screen screen = ScreenResultParser.makeDummyScreen(115);
    ScreenResult screenResult = new ScreenResult(screen, new Date());
    new ResultValueType(screenResult, "Luminescence");
    new ResultValueType(screenResult, "Luminescence");
    new ResultValueType(screenResult, "FI");
    new ResultValueType(screenResult, "FI");
    new ResultValueType(screenResult, "Assay Indicator");
    new Library("library 1", "lib1", ScreenType.SMALL_MOLECULE, LibraryType.COMMERCIAL, 1, 1);

    UniqueDataHeaderNames uniqueDataHeaderNames = new UniqueDataHeaderNames(screenResult);
    int i = 0;
    for (ResultValueType rvt : screenResult.getResultValueTypes()) {
      assertEquals("lookup name", 
                   expectedUniqueNames.get(rvt.getOrdinal()),
                   uniqueDataHeaderNames.get(rvt));
      assertEquals("lookup ResultValueType", 
                   uniqueDataHeaderNames.get(rvt),
                   expectedUniqueNames.get(i++));
    }
    assertEquals(expectedUniqueNames,
                 uniqueDataHeaderNames.asList());
  }
}
