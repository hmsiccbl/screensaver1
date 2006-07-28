// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import edu.harvard.med.screensaver.io.workbook.Cell;
import edu.harvard.med.screensaver.io.workbook.ParseError;
import edu.harvard.med.screensaver.io.workbook.ParseErrorManager;
import edu.harvard.med.screensaver.io.workbook.Workbook;
import edu.harvard.med.screensaver.io.workbook.Cell.Factory;
import edu.harvard.med.screensaver.model.libraries.Library;


/**
 * Loads the contents (either partial or complete) of an RNAi library
 * from an Excel spreadsheet.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class RNAiLibraryContentsLoader implements LibraryContentsLoader
{ 

  // private instance data
  
  private Library _library;
  private Workbook _workbook;
  private ParseErrorManager _errorManager;
  
  
  // public instance methods
  
  /**
   * Load library contents (either partial or complete) from an input
   * stream of an Excel spreadsheet into a library.
   * @param library the library to load contents of
   * @param file the name of the file that contains the library contents
   * @param stream the input stream to load library contents from
   * @return the library with the contents loaded
   */
  public synchronized Library loadLibraryContents(Library library, File file, InputStream stream)
  {
    initialize(library, file, stream);
    HSSFWorkbook hssfWorkbook = _workbook.getWorkbook();
    for (int i = 0; i < hssfWorkbook.getNumberOfSheets(); i++) {
      loadLibraryContentsFromHSSFSheet(i, hssfWorkbook.getSheetAt(i));
    }
    return _library;
  }

  /**
   * Return all errors the were detected during parsing. This class attempts to
   * parse as much of the workbook as possible, continuing on after finding an
   * error. The hope is that multiple errors will help a user/administrator
   * correct a workbook's errors in a batch fashion, rather than in a piecemeal
   * fashion.
   * 
   * @return a <code>List&lt;String&gt;</code> of all errors generated during
   *         parsing
   */
  public List<ParseError> getErrors()
  {
    return _errorManager.getErrors();
  }
  
  
  // private instance methods

  /**
   * Initialize the instance variables
   * @param library the library to load contents of
   * @param file the name of the file that contains the library contents
   * @param stream the input stream to load library contents from
   */
  private void initialize(Library library, File file, InputStream stream)
  {
    _library = library;
    _workbook = new Workbook(file, stream, _errorManager);
    _errorManager = new ParseErrorManager();
  }
  
  /**
   * Load library contents from a single worksheet
   * @param sheetIndex the index of the worksheet to load library contents from
   * @param hssfSheet the worksheet to load library contents from
   */
  private void loadLibraryContentsFromHSSFSheet(int sheetIndex, HSSFSheet hssfSheet)
  {
    Cell.Factory cellFactory = new Cell.Factory(_workbook, sheetIndex, _errorManager);
    String sheetName = _workbook.getWorkbook().getSheetName(sheetIndex);
    RNAiLibraryColumnHeaders columnHeaders =
      parseColumnHeaders(hssfSheet.getRow(0), sheetName, cellFactory);
    if (columnHeaders == null) {
      return;
    }
    for (short i = 1; i <= hssfSheet.getLastRowNum(); i++) {
      parseDataRow(columnHeaders, hssfSheet.getRow(i), i, cellFactory);
    }
  }

  /**
   * Parse the column headers. Return the resulting {@link RNAiLibraryColumnHeaders}.
   * @param columnHeaderRow the row containing the column headers
   * @param sheetName the name of the worksheet
   * @param cellFactory the cell factory 
   * @return the RequiredRNAiLibraryColumn
   */
  private RNAiLibraryColumnHeaders parseColumnHeaders(
    HSSFRow columnHeaderRow,
    String sheetName,
    Factory cellFactory)
  {
    if (columnHeaderRow == null) {
      _errorManager.addError("ecountered a sheet without any rows: " + sheetName);
      return null;
    }
    RNAiLibraryColumnHeaders columnHeaders =
      new RNAiLibraryColumnHeaders(columnHeaderRow, _errorManager, cellFactory);
    if (! columnHeaders.parseColumnHeaders()) {
      _errorManager.addError(
        "couldn't import sheet contents due to problems with column headers: " + sheetName);
      return null;
    }
    return columnHeaders;
  }
  
  /**
   * Parse the data row
   * @param columnHeaders the column headers
   * @param dataRow the data row
   * @param rowIndex the index of the data row in the sheet
   * @param cellFactory the cell factory
   */
  private void parseDataRow(
    RNAiLibraryColumnHeaders columnHeaders,
    HSSFRow dataRow,
    short rowIndex,
    Factory cellFactory)
  {
    Map<RequiredRNAiLibraryColumn,String> dataRowContents =
      columnHeaders.getDataRowContents(dataRow, rowIndex);
    if (! (hasContent(dataRowContents, RequiredRNAiLibraryColumn.PLATE) &&
           hasContent(dataRowContents, RequiredRNAiLibraryColumn.WELL))) {
      return;
    }
    // TODO: extract PlateNumberParser and WellNameParser from ScreenResultParser into the
    // workbook package. (or into the screenresult package)?
    // TODO: parse the plate & well; create the well and add it to library
    // TODO: etc
  }
  
  /**
   * Return true whenever the data row has content for the specified column
   * @param dataRowContents the data row contents
   * @param column the column to check for content for
   * @return true whenever the data row has content for the specified column
   */
  private boolean hasContent(
    Map<RequiredRNAiLibraryColumn,String> dataRowContents,
    RequiredRNAiLibraryColumn column)
  {
    String content = dataRowContents.get(column);
    if (content == null || content.equals("")) {
      return false;
    }
    return true;
  }
}
