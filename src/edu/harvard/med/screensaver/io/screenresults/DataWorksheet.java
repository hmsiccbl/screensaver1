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

import edu.harvard.med.screensaver.model.screenresults.PartitionedValue;
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
  public HSSFSheet build(HSSFWorkbook workbook, ScreenResult screenResult)
  {
    HSSFSheet sheet = workbook.createSheet(DATA_SHEET_NAME);
    writeDataColumnNames(sheet,
                         screenResult);
    writeData(sheet, 
              screenResult);
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

  private void writeData(HSSFSheet sheet, ScreenResult screenResult)
  {
    for (Iterator iter = screenResult.getResultValueTypes().iterator(); iter.hasNext();) {
      ResultValueType rvt = (ResultValueType) iter.next();
      int rowIndex = RAWDATA_FIRST_DATA_ROW_INDEX;
      for (Iterator iter2 = rvt.getResultValues().iterator(); iter2.hasNext();) {
        ResultValue rv = (ResultValue) iter2.next();
        HSSFRow row = HSSFCellUtil.getRow(rowIndex++, sheet);
        if (rvt.getOrdinal() == 0) {
          writeWell(row, rv);
        }
        addExclude(row, rv);
        HSSFCell cell = HSSFCellUtil.getCell(row, rvt.getOrdinal() + RAWDATA_FIRST_DATA_HEADER_COLUMN_INDEX);
        Object typedValue = rv.generateTypedValue();
        // TODO: not proper to hardcode cases for each potential Object type
        if (typedValue instanceof Boolean) {
          cell.setCellValue((Boolean) typedValue);
        }
        else if (typedValue instanceof Double) {
          cell.setCellValue((Double) typedValue);
        }
        else if (typedValue instanceof PartitionedValue) {
          cell.setCellValue(typedValue.toString());
        }
        else {
          try {
            double d = Double.parseDouble(typedValue.toString());
            cell.setCellValue(d);
          }
          catch (NumberFormatException e) {
            cell.setCellValue(typedValue.toString());
          }
        }
      }
    }
  }

  private void addExclude(HSSFRow row, ResultValue rv)
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
    value += new Character((char) (rv.getResultValueType().getOrdinal().intValue() + 'E'));
    cell.setCellValue(value);
  }

  private void writeWell(HSSFRow row, ResultValue rv)
  {
    HSSFCellUtil.getCell(row, 0).setCellValue(makePlateNumberString(rv.getWell().getPlateNumber()));
    HSSFCellUtil.getCell(row, 1).setCellValue(rv.getWell().getWellName());
    
    // TODO: must handle 'P' (positive control), 'N' (negative control), and 'O'
    // (other) types, which are NOT available from stock-plate Well.

    //HSSFCellUtil.getCell(row, 2).setCellValue(rv.getWell().getType());
  }

  private String makePlateNumberString(Integer plateNumber)
  {
    return String.format(ScreenResultWorkbookSpecification.PLATE_NUMBER_FORMAT, plateNumber);
  }

}
