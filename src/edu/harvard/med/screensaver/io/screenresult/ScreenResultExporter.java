// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.screenresult;

import java.io.IOException;

import edu.harvard.med.screensaver.model.screenresults.ScreenResult;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * Write a ScreenResult to a workbook file.
 * @author ant
 */
// TODO: place shared file format information in one place, and share with ScreenResultParser
public class ScreenResultExporter
{

  public ScreenResultExporter() throws IOException
  {
  }
  
  public HSSFWorkbook build(ScreenResult screenResult)
  {
    return writeData(screenResult);
  }
  
  private HSSFWorkbook writeData(ScreenResult screenResult)
  {
    HSSFWorkbook workbook = new HSSFWorkbook();
    new ScreenInfoWorksheet().build(workbook, screenResult);
    new DataHeadersWorksheet().build(workbook, screenResult);
    new DataWorksheet().build(workbook, screenResult);
    return workbook;
  }
}
