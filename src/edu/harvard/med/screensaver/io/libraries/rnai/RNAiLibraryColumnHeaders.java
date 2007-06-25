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
 * the {@link ParsedRNAiLibraryColumn required columns} and the column indexes in the sheet.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
class RNAiLibraryColumnHeaders
{

  // static data + initialization
  
  private static Logger log = Logger.getLogger(RNAiLibraryColumnHeaders.class);
  
  private static final Map<String,ParsedRNAiLibraryColumn> _columnHeaders =
    new HashMap<String,ParsedRNAiLibraryColumn>();
  static {
    for (ParsedRNAiLibraryColumn rnaiColumn : ParsedRNAiLibraryColumn.values()) {
      _columnHeaders.put(rnaiColumn.getDefaultColumnHeader().toLowerCase(), rnaiColumn);
    }
    // some accepted synonyms:
    _columnHeaders.put("384 plate",      ParsedRNAiLibraryColumn.PLATE);
    _columnHeaders.put("384 well",       ParsedRNAiLibraryColumn.WELL);
    _columnHeaders.put("catalog number", ParsedRNAiLibraryColumn.VENDOR_IDENTIFIER);
    _columnHeaders.put("gene symbol",    ParsedRNAiLibraryColumn.ENTREZGENE_SYMBOL);
    _columnHeaders.put("locus id",       ParsedRNAiLibraryColumn.ENTREZGENE_ID);
    _columnHeaders.put("accession",      ParsedRNAiLibraryColumn.GENBANK_ACCESSION_NUMBER);
    _columnHeaders.put("sequence",       ParsedRNAiLibraryColumn.SEQUENCES);
    _columnHeaders.put("old locus ids",  ParsedRNAiLibraryColumn.OLD_ENTREZGENE_IDS);
  }

  
  // private instance data
  
  private HSSFRow _columnHeaderRow;
  private ParseErrorManager _errorManager;
  private Cell.Factory _cellFactory;
  private String _sheetName;
  private Map<ParsedRNAiLibraryColumn,Integer> _columnIndexes =
    new HashMap<ParsedRNAiLibraryColumn,Integer>();
  
  
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
    for (ParsedRNAiLibraryColumn column : ParsedRNAiLibraryColumn.values()) {
      if (! column.isRequired()) {
        // skip optional columns - they play no role in determining data row type
        continue;
      }
      short columnIndex = getColumnIndex(column);
      HSSFCell cell = dataRow.getCell(columnIndex);
      if (cell                != null                      &&
          cell.getCellType()  != HSSFCell.CELL_TYPE_BLANK  &&
          (cell.getCellType() != HSSFCell.CELL_TYPE_STRING ||
           ! cell.getStringCellValue().equals("")          )) {
        if (column.equals(ParsedRNAiLibraryColumn.PLATE)) {
          hasPlate = true;
        }
        else if (column.equals(ParsedRNAiLibraryColumn.WELL)) {
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
   * Build and return a map from {@link ParsedRNAiLibraryColumn required columns} to the
   * contents of the row for that column.
   * @param dataRow the data row to build the map for
   * @param rowIndex the index of the data row in the sheet
   * @return a map from required columns to the contents of the row for that column
   */
  Map<ParsedRNAiLibraryColumn,String> getDataRowContents(HSSFRow dataRow, int rowIndex)
  {
    Map<ParsedRNAiLibraryColumn,String> dataRowContents =
      new HashMap<ParsedRNAiLibraryColumn,String>();
    for (ParsedRNAiLibraryColumn column : ParsedRNAiLibraryColumn.values()) {
      String contents = "";
      try {
        Cell cell = _cellFactory.getCell(getColumnIndex(column), rowIndex);
        contents = cell.getAsString();
      }
      catch (IndexOutOfBoundsException e) {
        // missing column must be an optional column, gets contents "" by convention
      }
      dataRowContents.put(column, contents);      
    }
    return dataRowContents;
  }
 
  /**
   * Return the column index for the {@link ParsedRNAiLibraryColumn required library column}.
   * 
   * @param column the required library column to return an index for
   * @return the column index
   * @throws IndexOutOfBoundsException when the column is not present (only applies to
   * optional columns)
   */
  short getColumnIndex(ParsedRNAiLibraryColumn column) throws IndexOutOfBoundsException
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
   * columns refer to the same {@link ParsedRNAiLibraryColumn}.
   * @return false whenever two columns refer to the same ParsedRNAiLibraryColumn
   */
  private boolean populateColumnIndexes() {
    boolean hasNoDuplicateHeaders = true;
    for (short i = _columnHeaderRow.getFirstCellNum(); i <= _columnHeaderRow.getLastCellNum(); i++) {
      Cell cell = _cellFactory.getCell(i, 0);
      String columnHeader = cell.getString();
      if (columnHeader == null) {
        continue;
      }
      ParsedRNAiLibraryColumn column = _columnHeaders.get(columnHeader.toLowerCase());
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
    for (ParsedRNAiLibraryColumn column : ParsedRNAiLibraryColumn.values()) {
      if (_columnIndexes.get(column) == null) {
        if (column.isRequired()) {
          _errorManager.addError(
            "required column \"" + column.getDefaultColumnHeader() +
            "\" does not match any column headers in sheet: " + _sheetName);
          hasRequiredHeaders = false;
        }
        else {
          log.warn("optional column header was not found: " + column.getDefaultColumnHeader()); 
        }
      }
    }
    return hasRequiredHeaders;
  }
}
