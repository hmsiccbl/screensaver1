// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.sun.rowset.internal.Row;

/**
 * Maintains a list of error messages.
 * @author ant
 */
public class ParseErrorManager
{
  private static Logger log = Logger.getLogger(ParseErrorManager.class);
  
  private List<ParseError> _errors = new ArrayList<ParseError>();
  /**
   * The workbook that will be annotated with errors that are not specific to a cell.
   */
  private Workbook _errorsWorkbook;

  private Set<Workbook> _workbooksWithErrors = new HashSet<Workbook>();
  
  public void setErrorsWorbook(Workbook errorsWorkbook)
  {
    _errorsWorkbook = errorsWorkbook;
  }
  
  /**
   * Add a simple error.
   * 
   * @param error the error
   */
  public void addError(String errorMessage)
  {
    ParseError error = new ParseError(errorMessage);
    _errors.add(error);
    //log.info("parse error: " + error);

    // annotate workbook with non-cell-specific error by appending to a specially created "errors" sheet
    if (_errorsWorkbook != null) {
      HSSFWorkbook hssfWorkbook = _errorsWorkbook.getWorkbook();
      HSSFSheet parseErrorsSheet = hssfWorkbook.getSheet("Parse Errors");
      if (parseErrorsSheet == null) {
        parseErrorsSheet = hssfWorkbook.createSheet("Parse Errors");
      }
      HSSFRow row = parseErrorsSheet.createRow(parseErrorsSheet.getLastRowNum());
      HSSFCell cell = row.createCell((short) 0);
      cell.setCellValue(errorMessage);
      _workbooksWithErrors.add(_errorsWorkbook);
    }
  }
  
  /**
   * Add an error, noting the particular cell the error is related to.
   * 
   * @param error the error
   * @param dataHeader the data header of the cell containing the error
   * @param row the {@link Row} of the cell containing the error
   */
  public void addError(String errorMessage, Cell cell)
  {
    ParseError error = new ParseError(errorMessage, cell);
    _errors.add(error);
    cell.annotateWithError(error);
    _workbooksWithErrors.add(cell.getWorkbook());
//    log.info("parse error: " + error);
  }
  
  /**
   * Get the list of <code>ParseError</code> objects.
   * 
   * @return a list of <code>ParseError</code> objects
   */
  public List<ParseError> getErrors()
  {
    return _errors;
  }

  public Set<Workbook> getWorkbooksWithErrors()
  {
    return Collections.unmodifiableSet(_workbooksWithErrors);
  }

}
