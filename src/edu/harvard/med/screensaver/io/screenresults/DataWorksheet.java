// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.screenresults;

import java.util.Iterator;
import java.util.Map;

import edu.harvard.med.screensaver.io.workbook.Cell;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.AssayWellType;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.contrib.HSSFCellUtil;

public class DataWorksheet implements ScreenResultWorkbookSpecification
{

  private static short ROW_HEADER_COLUMN_WIDTH = (short) (14 * 256);

  public HSSFSheet build(HSSFWorkbook workbook, ScreenResult screenResult, Integer plateNumber)
  {
    HSSFSheet sheet = workbook.createSheet(makePlateNumberString(plateNumber));
    writeDataColumnNames(sheet,
                         screenResult);
    writeData(workbook,
              sheet, 
              screenResult,
              plateNumber);
    sheet.setColumnWidth((short) 0, ROW_HEADER_COLUMN_WIDTH);
    return sheet;
  }

  private void writeDataColumnNames(HSSFSheet sheet, ScreenResult screenResult)
  {
    HSSFRow row = sheet.createRow(RAWDATA_HEADER_ROW_INDEX);
    for (DataColumn dataColumn : DataColumn.values()) {
      row.createCell((short) dataColumn.ordinal()).setCellValue(dataColumn.getDisplayText());
    }
    for (Iterator iter = screenResult.getResultValueTypes().iterator(); iter.hasNext();) {
      ResultValueType rvt = (ResultValueType) iter.next();
      row.createCell((short) (rvt.getOrdinal() + RAWDATA_FIRST_DATA_HEADER_COLUMN_INDEX)).setCellValue(rvt.getName());
    }
  }

  private void writeData(
    HSSFWorkbook workbook,
    HSSFSheet sheet,
    ScreenResult screenResult,
    Integer plateNumber)
  {
    for (Iterator iter = screenResult.getResultValueTypes()
                                     .iterator(); iter.hasNext();) {
      ResultValueType rvt = (ResultValueType) iter.next();
      int rowIndex = RAWDATA_FIRST_DATA_ROW_INDEX;
      Map<WellKey,ResultValue> resultValues = rvt.getResultValues();
      for (Iterator iter2 = resultValues.keySet().iterator(); iter2.hasNext();) {
        WellKey wellKey = (WellKey) iter2.next();
        ResultValue rv = resultValues.get(wellKey);
        if (plateNumber.equals(wellKey.getPlateNumber())) {
          HSSFRow row = HSSFCellUtil.getRow(rowIndex++, sheet);
          if (rvt.getOrdinal() == 0) {
            writeWell(row, wellKey, rv.getAssayWellType());
          }
          addExclude(row, rvt, rv);
          HSSFCell cell = HSSFCellUtil.getCell(row,
                                               rvt.getOrdinal() + RAWDATA_FIRST_DATA_HEADER_COLUMN_INDEX);
          Object typedValue = ResultValue.getTypedValue(rv, rvt);
          Cell.setTypedCellValue(workbook, cell, typedValue);
        }
      }
    }
  }

  private void addExclude(HSSFRow row, ResultValueType rvt, ResultValue rv)
  {
    if (!rv.isExclude()) {
      return;
    }
    HSSFCell cell = HSSFCellUtil.getCell(row, 3);
    String value = cell.getStringCellValue();
    if (value == null) {
      value = "";
    }
    if (value.length() > 0) {
      value += ",";
    }
    value += new Character((char) (rvt.getOrdinal().intValue() + 'E'));
    cell.setCellValue(value);
  }

  private void writeWell(HSSFRow row, WellKey wellKey, AssayWellType assayWellType)
  {
    HSSFCellUtil.getCell(row, 0).setCellValue(makePlateNumberString(wellKey.getPlateNumber()));
    HSSFCellUtil.getCell(row, 1).setCellValue(wellKey.getWellName());
    HSSFCellUtil.getCell(row, 2).setCellValue(assayWellType.getAbbreviation());
  }

  private String makePlateNumberString(Integer plateNumber)
  {
    return String.format(ScreenResultWorkbookSpecification.PLATE_NUMBER_FORMAT, plateNumber);
  }

}
