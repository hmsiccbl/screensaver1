// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.workbook2;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.harvard.med.screensaver.io.screenresults.ScreenResultWorkbookSpecification;

import jxl.Sheet;
import jxl.format.Colour;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import org.apache.log4j.Logger;

/**
 * Maintains a list of error messages.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class ParseErrorManager
{
  private static Logger log = Logger.getLogger(ParseErrorManager.class);
  
  private List<WorkbookParseError> _errors = new ArrayList<WorkbookParseError>();
  /**
   * The workbook being parsed.
   */
  private Workbook _workbook;


  /**
   * Set the workbook that is to be annotated with errors. A copy of this
   * workbook will be made, so the specified workbook will not be modified.
   * 
   * @param workbook
   * @throws IOException 
   */
  public void setWorbook(Workbook workbook)
  {
    _workbook = workbook;
  }
  
  /**
   * Add a simple error.
   */
  public void addError(String errorMessage)
  {
    WorkbookParseError error = new WorkbookParseError(errorMessage);
    _errors.add(error);

  }
  
  /**
   * Add an error, noting the particular cell the error is related to.
   * 
   * @param errorMessage the error
   * @param cell the cell containing the error
   */
  public void addError(String errorMessage, Cell cell)
  {
    WorkbookParseError error = new WorkbookParseError(errorMessage, cell);
    _errors.add(error);
  }
  
  /**
   * Get the list of <code>ParseError</code> objects.
   * 
   * @return a list of <code>ParseError</code> objects
   */
  public List<WorkbookParseError> getErrors()
  {
    return _errors;
  }
  
  /**
   * @motivation For JSF EL expressions
   */
  public boolean getHasErrors()
  {
    return _errors.size() > 0;
  }

  public WritableWorkbook getErrorAnnotatedWorkbook()
  {
    try {
      BufferedOutputStream dummyOutputStream = new BufferedOutputStream(new ByteArrayOutputStream());
      WritableWorkbook errorAnnotatedWorkbook = jxl.Workbook.createWorkbook(dummyOutputStream);
      if (_workbook != null) {
        errorAnnotatedWorkbook = jxl.Workbook.createWorkbook(dummyOutputStream);
        
        // TODO: this sheet is being created, but the data itself is not always
        // being imported (see RT #50154). Not going to fix unless a user runs
        // across this problem!
        errorAnnotatedWorkbook.importSheet(ScreenResultWorkbookSpecification.SCREEN_INFO_SHEET_NAME,
                                           0,
                                           _workbook.getWorkbook().getSheet(ScreenResultWorkbookSpecification.SCREEN_INFO_SHEET_NAME));
        errorAnnotatedWorkbook.importSheet(ScreenResultWorkbookSpecification.DATA_HEADERS_SHEET_NAME,
                                           1,
                                           _workbook.getWorkbook().getSheet(ScreenResultWorkbookSpecification.DATA_HEADERS_SHEET_NAME));
      }

      // annotate workbook with non-cell-specific error by appending to a specially created "errors" sheet
       for (WorkbookParseError error : _errors) {
        if (error.getCell() == null) {
          WritableSheet generalParseErrorsSheet = errorAnnotatedWorkbook.getSheet("Parse Errors");
          if (generalParseErrorsSheet == null) {
            generalParseErrorsSheet = errorAnnotatedWorkbook.createSheet("Parse Errors", 0);
          }
          generalParseErrorsSheet.addCell(new Label(0, generalParseErrorsSheet.getRows(), error.getErrorMessage()));
        }
        else {
          
          Sheet parsedSheet = error.getCell().getSheet();
          WritableSheet errorAnnotatedSheet = errorAnnotatedWorkbook.getSheet(parsedSheet.getName());
          if (errorAnnotatedSheet == null) {
            errorAnnotatedSheet = errorAnnotatedWorkbook.importSheet(parsedSheet.getName(),
                                                                     Integer.MAX_VALUE,
                                                                     parsedSheet);
          }
          annotateCellWithError(error.getCell().getJxlCell(),
                                errorAnnotatedSheet,
                                error);
        }
      }
      return errorAnnotatedWorkbook;
    }
    catch (Exception e) {
      log.error(e);
      e.printStackTrace();
      return null;
    }
  }
  
  private void annotateCellWithError(jxl.Cell parsedCell, jxl.write.WritableSheet annotatedErrorSheet, WorkbookParseError error) throws WriteException
  {
    Label errorLabel = new Label(parsedCell.getColumn(),
                                 parsedCell.getRow(),
                                 (parsedCell.getContents().trim().length() > 0 ? parsedCell.getContents() + ": " : "") + error.getErrorMessage());
    annotatedErrorSheet.addCell(errorLabel);
    
//  WritableCellFeatures cellFeatures = new WritableCellFeatures();
//  cellFeatures.setComment(error.getMessage());
//  errorAnnotatedCell.setCellFeatures(cellFeatures);
    
    WritableCellFormat newFormat = new WritableCellFormat();
    newFormat.setBackground(Colour.RED);
    errorLabel.setCellFormat(newFormat);
  }
}
