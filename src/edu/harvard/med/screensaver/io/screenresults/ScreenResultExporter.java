// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.screenresults;

import edu.harvard.med.screensaver.model.screenresults.ScreenResult;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * Write a ScreenResult to a workbook file.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class ScreenResultExporter
{

  public HSSFWorkbook build(ScreenResult screenResult)
  {
    HSSFWorkbook workbook = new HSSFWorkbook();
    new ScreenInfoWorksheet().build(workbook, screenResult);
    new DataHeadersWorksheet().build(workbook, screenResult);
    for (Integer plateNumber : screenResult.getPlateNumbers()) {
      new DataWorksheet().build(workbook, screenResult, plateNumber);
    }
    return workbook;
  }

}
