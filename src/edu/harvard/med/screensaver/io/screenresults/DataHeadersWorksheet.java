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
import edu.harvard.med.screensaver.model.screenresults.ActivityIndicatorType;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.contrib.HSSFCellUtil;

public class DataHeadersWorksheet implements ScreenResultWorkbookSpecification
{

  public HSSFSheet build(HSSFWorkbook workbook, ScreenResult screenResult)
  {
    HSSFSheet sheet = workbook.createSheet(DATA_HEADERS_SHEET_NAME);
    writeDataHeaderRowNames(sheet,
                            screenResult);
    writeDataHeaders(sheet, 
                     screenResult);
    return sheet;
  }

  private void writeDataHeaders(HSSFSheet sheet, ScreenResult screenResult)
  {
    Map<MetadataRow,Object> columnValues = new HashMap<MetadataRow,Object>();
    for (ResultValueType rvt : screenResult.getResultValueTypes()) {
      columnValues.clear();
      columnValues.put(MetadataRow.COLUMN_IN_DATA_WORKSHEET, makeDataWorksheetColumnLabelForDataHeader(rvt));
      columnValues.put(MetadataRow.COLUMN_TYPE, DATA_HEADER_COLUMN_TYPE); // for compatibility with legacy format (parser uses this value)
      columnValues.put(MetadataRow.NAME, rvt.getName());
      columnValues.put(MetadataRow.DESCRIPTION, rvt.getDescription());
      if (rvt.getReplicateOrdinal() != null) {
        columnValues.put(MetadataRow.REPLICATE, rvt.getReplicateOrdinal());
      }
      columnValues.put(MetadataRow.TIME_POINT, rvt.getTimePoint());
      columnValues.put(MetadataRow.RAW_OR_DERIVED, rvt.isDerived() ? DERIVED_VALUE : RAW_VALUE);
      if (rvt.isDerived()) {
        columnValues.put(MetadataRow.HOW_DERIVED, rvt.getHowDerived());
        columnValues.put(MetadataRow.COLUMNS_DERIVED_FROM, makeColumnsDerivedFromList(rvt));
      }
      columnValues.put(MetadataRow.IS_ASSAY_ACTIVITY_INDICATOR, makeYesOrNoString(rvt.isActivityIndicator()));
      if (rvt.isActivityIndicator()) {
        columnValues.put(MetadataRow.ACTIVITY_INDICATOR_TYPE, rvt.getActivityIndicatorType().getValue().toLowerCase());
        if (rvt.getActivityIndicatorType() == ActivityIndicatorType.NUMERICAL) {
          columnValues.put(MetadataRow.NUMERICAL_INDICATOR_DIRECTION, rvt.getIndicatorDirection().getValue().toLowerCase());
          columnValues.put(MetadataRow.NUMERICAL_INDICATOR_CUTOFF, rvt.getIndicatorCutoff());
        }
      }
      columnValues.put(MetadataRow.PRIMARY_OR_FOLLOWUP, (rvt.isFollowUpData() ? FOLLOWUP_VALUE : PRIMARY_VALUE).toLowerCase());
      columnValues.put(MetadataRow.ASSAY_PHENOTYPE, rvt.getAssayPhenotype());
      columnValues.put(MetadataRow.IS_CHERRY_PICK, makeYesOrNoString(rvt.isCherryPick()));
      columnValues.put(MetadataRow.COMMENTS, rvt.getComments());
      
      for (MetadataRow metadataRow : columnValues.keySet()) {
        Object value = columnValues.get(metadataRow);
        HSSFRow row = HSSFCellUtil.getRow(metadataRow.ordinal() + METADATA_FIRST_DATA_ROW_INDEX, sheet);
        Cell.setTypedCellValue(HSSFCellUtil.getCell(row, rvt.getOrdinal() + METADATA_FIRST_DATA_HEADER_COLUMN_INDEX),
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

  private void writeDataHeaderRowNames(HSSFSheet sheet, ScreenResult screenResult)
  {
    for (MetadataRow metadataRow : MetadataRow.values()) {
      HSSFRow row = HSSFCellUtil.getRow(metadataRow.ordinal() + METADATA_FIRST_DATA_ROW_INDEX,
                                        sheet);
      HSSFCell cell = HSSFCellUtil.getCell(row, METADATA_ROW_NAMES_COLUMN_INDEX);
      cell.setCellValue(metadataRow.getDisplayText());
    }
    
  }

}
