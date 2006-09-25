// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries.rnai;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;

import edu.harvard.med.screensaver.io.libraries.DataRowType;
import edu.harvard.med.screensaver.io.workbook.Cell;
import edu.harvard.med.screensaver.io.workbook.ParseErrorManager;


/**
 * Parses the Column Headers in an RNAi Library Contents worksheet. Maintains a mapping between
 * the {@link RequiredRNAiLibraryColumn required columns} and the column indexes in the sheet.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
class RNAiLibraryColumnHeaders
{

  // static data + initialization
  
  private static Logger log = Logger.getLogger(RNAiLibraryColumnHeaders.class);
  
  private static final Map<String,RequiredRNAiLibraryColumn> _columnHeaders =
    new HashMap<String,RequiredRNAiLibraryColumn>();
  static {
    for (RequiredRNAiLibraryColumn rnaiColumn : RequiredRNAiLibraryColumn.values()) {
      _columnHeaders.put(rnaiColumn.getDefaultColumnHeader().toLowerCase(), rnaiColumn);
    }
    // some accepted synonyms:
    _columnHeaders.put("384 plate",      RequiredRNAiLibraryColumn.PLATE);
    _columnHeaders.put("384 well",       RequiredRNAiLibraryColumn.WELL);
    _columnHeaders.put("catalog number", RequiredRNAiLibraryColumn.VENDOR_IDENTIFIER);
    _columnHeaders.put("gene symbol",    RequiredRNAiLibraryColumn.ENTREZGENE_SYMBOL);
    _columnHeaders.put("locus id",       RequiredRNAiLibraryColumn.ENTREZGENE_ID);
    _columnHeaders.put("accession",      RequiredRNAiLibraryColumn.GENBANK_ACCESSION_NUMBER);
    _columnHeaders.put("sequence",       RequiredRNAiLibraryColumn.SEQUENCES);
  }

  
  // private instance data
  
  private HSSFRow _columnHeaderRow;
  private ParseErrorManager _errorManager;
  private Cell.Factory _cellFactory;
  private String _sheetName;
  private Map<RequiredRNAiLibraryColumn,Integer> _columnIndexes =
    new HashMap<RequiredRNAiLibraryColumn,Integer>();
  
  
  // package constructor and methods
  
  /**
   * Construct a <code>RNAiLibraryColumnHeaders</code> object.
   * @param columnHeaderRow the worksheet to get the column headers from
   * @param errorManager the parse error manager to report errors to
   * @param cellFactory the cell factory
   */
  RNAiLibraryColumnHeaders(
    HSSFRow columnHeaderRow,
    ParseErrorManager errorManager,
    Cell.Factory cellFactory,
    String sheetName)
  {
    _columnHeaderRow = columnHeaderRow;
    _errorManager = errorManager;
    _cellFactory = cellFactory;
    _sheetName = sheetName;
  }
  
  /**
   * Parse the column headers. Return true.
   * @return self
   */
  boolean parseColumnHeaders()
  {
    return populateColumnIndexes() && checkRequiredHeaders();
  }
  
  /**
   * Get the {@link DataRowType} of the specified data row
   * @param dataRow the data row to get the type of
   * @return the data row type
   */
  DataRowType getDataRowType(HSSFRow dataRow)
  {
    boolean hasPlate = false;
    boolean hasWell = false;
    boolean hasOther = false;
    for (RequiredRNAiLibraryColumn column : RequiredRNAiLibraryColumn.values()) {
      short columnIndex = getColumnIndex(column);
      HSSFCell cell = dataRow.getCell(columnIndex);
      if (cell                != null                      &&
          cell.getCellType()  != HSSFCell.CELL_TYPE_BLANK  &&
          (cell.getCellType() != HSSFCell.CELL_TYPE_STRING ||
           ! cell.getStringCellValue().equals("")          )) {
        if (column.equals(RequiredRNAiLibraryColumn.PLATE)) {
          hasPlate = true;
        }
        else if (column.equals(RequiredRNAiLibraryColumn.WELL)) {
          hasWell = true;
        }
        else {
          hasOther = true;
        }
      }
    }
    if (! (hasPlate || hasWell || hasOther)) {
      return DataRowType.EMPTY;
    }
    if (hasPlate && hasWell && ! hasOther) {
      return DataRowType.PLATE_WELL_ONLY;
    }
    return DataRowType.NON_EMPTY;
  }
  
  /**
   * Build and return a map from {@link RequiredRNAiLibraryColumn required columns} to the
   * contents of the row for that column.
   * @param dataRow the data row to build the map for
   * @param rowIndex the index of the data row in the sheet
   * @return a map from required columns to the contents of the row for that column
   */
  Map<RequiredRNAiLibraryColumn,String> getDataRowContents(HSSFRow dataRow, short rowIndex)
  {
    Map<RequiredRNAiLibraryColumn,String> dataRowContents =
      new HashMap<RequiredRNAiLibraryColumn,String>();
    for (RequiredRNAiLibraryColumn column : RequiredRNAiLibraryColumn.values()) {
      Cell cell = _cellFactory.getCell(getColumnIndex(column), (short) rowIndex);
      dataRowContents.put(column, cell.getAsString());      
    }
    return dataRowContents;
  }
 
  /**
   * Return the column index for the {@link RequiredRNAiLibraryColumn required library column}.
   * @param column the required library column to return an index for
   * @return the column index
   */
  short getColumnIndex(RequiredRNAiLibraryColumn column)
  {
    Integer index = _columnIndexes.get(column);
    if (index == null) {
      throw new IndexOutOfBoundsException();
    }
    return index.shortValue();
  }
 
  
  // private instance methods
  
  /**
   * Parse the column headers, populating the {@link #_columnIndexes}. Return false whenever two
   * columns refer to the same {@link RequiredRNAiLibraryColumn}.
   * @return false whenever two columns refer to the same RequiredRNAiLibraryColumn
   */
  private boolean populateColumnIndexes() {
    boolean hasNoDuplicateHeaders = true;
    for (short i = _columnHeaderRow.getFirstCellNum(); i <= _columnHeaderRow.getLastCellNum(); i++) {
      Cell cell = _cellFactory.getCell(i, (short) 0);
      String columnHeader = cell.getString();
      if (columnHeader == null) {
        continue;
      }
      RequiredRNAiLibraryColumn column = _columnHeaders.get(columnHeader.toLowerCase());
      if (column == null) {
        continue;
      }
      if (_columnIndexes.get(column) != null) {
        _errorManager.addError(
          "required column \"" + column.getDefaultColumnHeader() +
          "\" matches multiple column headers in the same sheet",
          cell);
        hasNoDuplicateHeaders = false;
      }
      _columnIndexes.put(column, (int) i);
    }
    return hasNoDuplicateHeaders;
  }

  /**
   * Make sure that all the required column headers have been parsed. Log every missing required
   * column header as an error. Return true whenever all required column headers are present
   * @return true whenever all required column headers are present
   */
  private boolean checkRequiredHeaders()
  {
    boolean hasRequiredHeaders = true;
    for (RequiredRNAiLibraryColumn column : RequiredRNAiLibraryColumn.values()) {
      if (_columnIndexes.get(column) == null) {
        _errorManager.addError(
          "required column \"" + column.getDefaultColumnHeader() +
          "\" does not match any column headers in sheet: " + _sheetName);
        hasRequiredHeaders = false;
      }
    }
    return hasRequiredHeaders;
  }
}
