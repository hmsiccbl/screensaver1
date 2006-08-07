// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.screenresultparser;

import java.io.File;
import java.io.FileOutputStream;

import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.io.screenresult.ScreenResultExporter;
import edu.harvard.med.screensaver.io.screenresult.ScreenResultParser;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class ScreenResultExporterTest extends AbstractSpringTest
{
  protected ScreenResultParser screenResultParser;
  
  public void testScreenResultExporter() throws Exception
  {
    ScreenResult originalScreenResult  = screenResultParser.parse(new File(ScreenResultParserTest.TEST_INPUT_FILE_DIR, "115.xls"));
    ScreenResultExporter exporter = new ScreenResultExporter();
    HSSFWorkbook workbook = exporter.build(originalScreenResult);
    File exportedFile = File.createTempFile("115", ".exported.xls");
    workbook.write(new FileOutputStream(exportedFile));
    
    // TODO: assert something!
  }
}
