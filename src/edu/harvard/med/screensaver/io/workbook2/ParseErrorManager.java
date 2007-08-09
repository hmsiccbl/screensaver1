// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/io/ParseErrorManager.java $
// $Id: ParseErrorManager.java 275 2006-06-28 15:32:40Z js163 $
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
 * @author ant
 */
public class ParseErrorManager
{
  private static Logger log = Logger.getLogger(ParseErrorManager.class);
  
  private List<ParseError> _errors = new ArrayList<ParseError>();
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
   * 
   * @param error the error
   */
  public void addError(String errorMessage)
  {
    ParseError error = new ParseError(errorMessage);
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
    ParseError error = new ParseError(errorMessage, cell);
    _errors.add(error);
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
  
  /**
   * @motivation jsp inspection
   * @return
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
       for (ParseError error : _errors) {
        if (error.getCell() == null) {
          WritableSheet generalParseErrorsSheet = errorAnnotatedWorkbook.getSheet("Parse Errors");
          if (generalParseErrorsSheet == null) {
            generalParseErrorsSheet = errorAnnotatedWorkbook.createSheet("Parse Errors", 0);
          }
          generalParseErrorsSheet.addCell(new Label(0, generalParseErrorsSheet.getRows(), error.getMessage()));
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
  
  private void annotateCellWithError(jxl.Cell parsedCell, jxl.write.WritableSheet annotatedErrorSheet, ParseError error) throws WriteException
  {
    Label errorLabel = new Label(parsedCell.getColumn(),
                                 parsedCell.getRow(),
                                 (parsedCell.getContents().trim().length() > 0 ? parsedCell.getContents() + ": " : "") + error.getMessage());
    annotatedErrorSheet.addCell(errorLabel);
    
//  WritableCellFeatures cellFeatures = new WritableCellFeatures();
//  cellFeatures.setComment(error.getMessage());
//  errorAnnotatedCell.setCellFeatures(cellFeatures);
    
    WritableCellFormat newFormat = new WritableCellFormat();
    newFormat.setBackground(Colour.RED);
    errorLabel.setCellFormat(newFormat);
  }
}
