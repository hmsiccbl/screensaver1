// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultParser;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultParserTest;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;

public class UniqueDataHeaderNamesTest extends AbstractSpringTest
{
  
  protected ScreenResultParser screenResultParser;

  public void testClass()
  {
    List<String> expectedUniqueNames = new ArrayList<String>();
    expectedUniqueNames.add("Luminescence (1)");
    expectedUniqueNames.add("Luminescence (2)");
    expectedUniqueNames.add("Luminescence (3)");
    expectedUniqueNames.add("Luminescence (4)");
    expectedUniqueNames.add("FI (1)");
    expectedUniqueNames.add("FI (2)");
    expectedUniqueNames.add("FI (3)");
    expectedUniqueNames.add("FI (4)");
    expectedUniqueNames.add("AssayIndicator");
    expectedUniqueNames.add("Cherry Pick");
    
    ScreenResult screenResult = screenResultParser.parse(new File(ScreenResultParserTest.TEST_INPUT_FILE_DIR, "115.xls"));
    assertNotNull("pretest: screenResult parsed", screenResult);
    UniqueDataHeaderNames uniqueDataHeaderNames = new UniqueDataHeaderNames(screenResult);
    for (ResultValueType rvt : screenResult.getResultValueTypes()) {
      assertEquals(expectedUniqueNames.get(rvt.getOrdinal()),
                   uniqueDataHeaderNames.get(rvt));
    }
    assertEquals(expectedUniqueNames,
                 uniqueDataHeaderNames.asList());
    
  }
}
