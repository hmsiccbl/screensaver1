// $HeadURL$
// $Id$
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
import edu.harvard.med.screensaver.model.screenresults.PositiveIndicatorType;
import edu.harvard.med.screensaver.model.screenresults.PositiveIndicatorDirection;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.contrib.HSSFCellUtil;

public class DataHeadersWorksheet implements ScreenResultWorkbookSpecification
{

  private static short ROW_HEADER_COLUMN_WIDTH = (short) (40 * 256);

  public HSSFSheet build(HSSFWorkbook workbook, ScreenResult screenResult)
  {
    HSSFSheet sheet = workbook.createSheet(DATA_HEADERS_SHEET_NAME);
    sheet.setColumnWidth((short) 0, ROW_HEADER_COLUMN_WIDTH);
    writeDataHeaderRowNames(workbook, 
                            sheet,
                            screenResult);
    writeDataHeaders(workbook,
                     sheet, 
                     screenResult);
    sheet.setColumnWidth((short) 0, ROW_HEADER_COLUMN_WIDTH);
    return sheet;
  }

  private void writeDataHeaders(HSSFWorkbook workbook, HSSFSheet sheet, ScreenResult screenResult)
  {
    Map<DataHeaderRow,Object> columnValues = new HashMap<DataHeaderRow,Object>();
    for (ResultValueType rvt : screenResult.getResultValueTypes()) {
      columnValues.clear();
      columnValues.put(DataHeaderRow.COLUMN_IN_DATA_WORKSHEET, makeDataWorksheetColumnLabelForDataHeader(rvt));
      columnValues.put(DataHeaderRow.NAME, rvt.getName());
      columnValues.put(DataHeaderRow.DESCRIPTION, rvt.getDescription());
      if (rvt.getReplicateOrdinal() != null) {
        columnValues.put(DataHeaderRow.REPLICATE, rvt.getReplicateOrdinal());
      }
      columnValues.put(DataHeaderRow.TIME_POINT, rvt.getTimePoint());
      columnValues.put(DataHeaderRow.RAW_OR_DERIVED, rvt.isDerived() ? DERIVED_VALUE : RAW_VALUE);
      if (rvt.isDerived()) {
        columnValues.put(DataHeaderRow.HOW_DERIVED, rvt.getHowDerived());
        columnValues.put(DataHeaderRow.COLUMNS_DERIVED_FROM, makeColumnsDerivedFromList(rvt));
      }
      else {
        columnValues.put(DataHeaderRow.ASSAY_READOUT_TYPE, rvt.getAssayReadoutType().getValue().toLowerCase());
      }
      columnValues.put(DataHeaderRow.IS_ASSAY_ACTIVITY_INDICATOR, makeYesOrNoString(rvt.isPositiveIndicator()));
      if (rvt.isPositiveIndicator()) {
        columnValues.put(DataHeaderRow.ACTIVITY_INDICATOR_TYPE, rvt.getPositiveIndicatorType().getValue().toLowerCase());
        if (rvt.getPositiveIndicatorType() == PositiveIndicatorType.NUMERICAL) {
          columnValues.put(DataHeaderRow.NUMERICAL_INDICATOR_DIRECTION, 
                           rvt.getPositiveIndicatorDirection().equals(PositiveIndicatorDirection.HIGH_VALUES_INDICATE) 
                           ?  ScreenResultParser.NUMERICAL_INDICATOR_DIRECTION_HIGH_VALUES_INDICATE : 
                             ScreenResultParser.NUMERICAL_INDICATOR_DIRECTION_LOW_VALUES_INDICATE);
          columnValues.put(DataHeaderRow.NUMERICAL_INDICATOR_CUTOFF, rvt.getPositiveIndicatorCutoff());
        }
      }
      columnValues.put(DataHeaderRow.PRIMARY_OR_FOLLOWUP, (rvt.isFollowUpData() ? FOLLOWUP_VALUE : PRIMARY_VALUE).toLowerCase());
      columnValues.put(DataHeaderRow.ASSAY_PHENOTYPE, rvt.getAssayPhenotype());
      columnValues.put(DataHeaderRow.COMMENTS, rvt.getComments());
      
      for (DataHeaderRow metadataRow : columnValues.keySet()) {
        Object value = columnValues.get(metadataRow);
        HSSFRow row = HSSFCellUtil.getRow(metadataRow.getRowIndex(), sheet);
        Cell.setTypedCellValue(workbook,
                               HSSFCellUtil.getCell(row, rvt.getOrdinal() + DATA_HEADERS_FIRST_DATA_HEADER_COLUMN_INDEX),
                               value);
      }
    }
  }

  private String makeYesOrNoString(boolean b)
  {
    return b ? YES_VALUE : NO_VALUE;
  }

  private String makeColumnsDerivedFromList(ResultValueType rvt)
  {
    StringBuilder builder = new StringBuilder();
    for (ResultValueType rvtDerivedFrom : rvt.getTypesDerivedFrom()) {
      if (builder.length() > 0) {
        builder.append(",");
      }
      builder.append((char) (rvtDerivedFrom.getOrdinal() + DATA_SHEET__FIRST_DATA_HEADER_COLUMN_LABEL));
    }
    return builder.toString();
  }

  private String makeDataWorksheetColumnLabelForDataHeader(ResultValueType rvt)
  {
    return "" + (char) (rvt.getOrdinal() + DATA_SHEET__FIRST_DATA_HEADER_COLUMN_LABEL);
  }

  private void writeDataHeaderRowNames(HSSFWorkbook workbook, HSSFSheet sheet, ScreenResult screenResult)
  {
    HSSFCellStyle style = workbook.createCellStyle();
    style.setWrapText(true);

    for (DataHeaderRow metadataRow : DataHeaderRow.values()) {
      HSSFRow row = HSSFCellUtil.getRow(metadataRow.getRowIndex(),
                                        sheet);
      HSSFCell cell = HSSFCellUtil.getCell(row, METADATA_ROW_NAMES_COLUMN_INDEX);
      cell.setCellStyle(style);
      cell.setCellValue(metadataRow.getDisplayText());
    }
    
  }

}
