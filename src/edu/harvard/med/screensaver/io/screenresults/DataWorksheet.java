// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.screenresults;

import java.util.Iterator;
import java.util.SortedSet;

import edu.harvard.med.screensaver.io.workbook.Cell;
import edu.harvard.med.screensaver.model.libraries.Well;
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
    HSSFSheet sheet = workbook.createSheet(WellKey.getPlateNumberLabel(plateNumber));
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
    int rowIndex = RAWDATA_FIRST_DATA_ROW_INDEX;

    SortedSet<Well> wells = screenResult.getWells();
    for (Well well : wells) {
      AssayWellType assayWellType = AssayWellType.EXPERIMENTAL;
      StringBuilder excludeBuf = new StringBuilder();
      HSSFRow row = HSSFCellUtil.getRow(rowIndex, sheet);
      for (ResultValueType rvt : screenResult.getResultValueTypes()) {
        ResultValue rv = rvt.getWellKeyToResultValueMap().get(well.getWellKey());
        if (rv != null) {
          assayWellType = rv.getAssayWellType(); // overwrites, but should be same for all resultValues of a well
          addExclude(excludeBuf, row, rvt, rv);
          HSSFCell cell =
            HSSFCellUtil.getCell(row,
                                 rvt.getOrdinal() +
                                 RAWDATA_FIRST_DATA_HEADER_COLUMN_INDEX);
          Object typedValue = ResultValue.getTypedValue(rv, rvt);
          Cell.setTypedCellValue(workbook, cell, typedValue);
          // TODO: set numeric format
//            if (rvt.isNumeric()) {
//              HSSFDataFormat dataFormat = workbook.createDataFormat();
//              short format = dataFormat.getFormat("#.##");
//              HSSFCellStyle cellStyle = workbook.createCellStyle();
//              cellStyle.setDataFormat(format);
//            }
        }
      }
      writeWell(row, well.getWellKey(), assayWellType);
      if (excludeBuf.length() > 0) {
        HSSFCell excludeCell = HSSFCellUtil.getCell(row, 3);
        excludeCell.setCellValue(excludeBuf.toString());
      }
      ++rowIndex;
    }
  }

  private void addExclude(StringBuilder buf, HSSFRow row, ResultValueType rvt, ResultValue rv)
  {
    if (!rv.isExclude()) {
      return;
    }
    if (buf.length() > 0) {
      buf.append(",");
    }
    // TODO: replace literal 'E' with constant
    buf.append(new Character((char) (rvt.getOrdinal().intValue() + 'E')));
  }

  private void writeWell(HSSFRow row, WellKey wellKey, AssayWellType assayWellType)
  {
    HSSFCellUtil.getCell(row, 0).setCellValue(wellKey.getPlateNumberLabel());
    HSSFCellUtil.getCell(row, 1).setCellValue(wellKey.getWellName());
    if (assayWellType != null) {
      HSSFCellUtil.getCell(row, 2).setCellValue(assayWellType.getAbbreviation());
    }
  }
}
