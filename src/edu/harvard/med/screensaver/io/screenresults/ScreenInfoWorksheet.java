// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.screenresults;

import java.util.HashMap;
import java.util.Map;

import edu.harvard.med.screensaver.io.workbook.Cell;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.contrib.HSSFCellUtil;

public class ScreenInfoWorksheet implements ScreenResultWorkbookSpecification
{
  
  private static short ROW_HEADER_COLUMN_WIDTH = (short) (20 * 256);

  public HSSFSheet build(HSSFWorkbook workbook, ScreenResult screenResult)
  {
    HSSFSheet sheet = workbook.createSheet(SCREEN_INFO_SHEET_NAME);
    
    // the values of this Map must be one of Date, Integer, or Number, if you
    // want the cell's type to set correctly (String is default)
    Map<ScreenInfoRow,Object> row2Value = new HashMap<ScreenInfoRow,Object>();
    Screen screen = screenResult.getScreen();
    row2Value.put(ScreenInfoRow.ID, screen.getScreenNumber());
    row2Value.put(ScreenInfoRow.TITLE, screen.getTitle());
    row2Value.put(ScreenInfoRow.SUMMARY, screen.getSummary());
    row2Value.put(ScreenInfoRow.PI_LAB, screen.getLabHead().getFullNameLastFirst());
    row2Value.put(ScreenInfoRow.LEAD_SCREENER, screen.getLeadScreener().getFullNameLastFirst());
    row2Value.put(ScreenInfoRow.COLLABORATORS, screen.getCollaboratorsString());
    // note: we're only showing the first publication PubmedId
    if (screen.getPublications().size() > 0) {
      row2Value.put(ScreenInfoRow.PUBMED_ID, screen.getPublications().iterator().next());
    }
    row2Value.put(ScreenInfoRow.DATE_FIRST_LIBRARY_SCREENING, screenResult.getDateCreated());
    row2Value.put(ScreenInfoRow.EMAIL, screen.getLabHead().getEmail());
    row2Value.put(ScreenInfoRow.LAB_AFFILIATION, screen.getLabHead().getLabAffiliationName());

    for (ScreenInfoRow screenInfoRow : ScreenInfoRow.values()) {
      HSSFRow row = HSSFCellUtil.getRow(screenInfoRow.ordinal() + SCREENINFO_FIRST_DATA_ROW_INDEX, sheet);
      HSSFCell cell = HSSFCellUtil.getCell(row, SCREENINFO_ROW_HEADER_COLUMN_INDEX);
      cell.setCellValue(screenInfoRow.getDisplayText());
      Object value = row2Value.get(screenInfoRow);
      Cell.setTypedCellValue(workbook,
                             HSSFCellUtil.getCell(row, SCREENINFO_VALUE_COLUMN_INDEX),
                             value);
    }
    sheet.setColumnWidth((short) 0, ROW_HEADER_COLUMN_WIDTH);
    return sheet;
  }

}
