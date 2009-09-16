// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.workbook2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import jxl.WorkbookSettings;
import jxl.write.WritableWorkbook;

import org.apache.log4j.Logger;

/**
 * Encapsulates the (lazy) instantiation of an jxl.Workbook from a file. Also
 * allows the workbook to be associated with the filename it originated from.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class Workbook implements Iterable<Worksheet>
{
  private static Logger log = Logger.getLogger(Workbook.class);
  
  public static final String MIME_TYPE = "application/vnd.ms-excel";
  
  private InputStream _workbookStream;
  private jxl.Workbook _workbook;
  private ParseErrorManager _errors;
  private WorkbookSettings _workbookSettings;
  private String _name;


  public Workbook(File workbookFile) throws FileNotFoundException
  {
    this(workbookFile.getAbsolutePath(), new FileInputStream(workbookFile));
  }
  
  public Workbook(String name, InputStream workbookStream, WorkbookSettings workbookSettings) throws FileNotFoundException
  {
    this(name, workbookStream);
    _workbookSettings = workbookSettings;
  }
  
  /**
   * @param name - as the encapsulated workbook does not infer the name
   * @param workbookStream
   */
  public Workbook(String name, InputStream workbookStream)
  {
    _workbookStream = workbookStream;
    _name = name;
    _errors = new ParseErrorManager();
  }

  private WorkbookSettings getWorkbookSettings() 
  {
    if(_workbookSettings == null) _workbookSettings = new WorkbookSettings();
    return _workbookSettings;
  }
  
  /**
   * Returns a jxl.Workbook, with lazy instantiation.
   * @return an jxl.Workbook
   * @throws IOException if file is not found or cannot be read
   */
  public jxl.Workbook getWorkbook() 
  {
    if (_workbook == null) {
      try {
        WorkbookSettings workbookSettings = getWorkbookSettings();
        workbookSettings.setGCDisabled(true); // when GC feature is enabled, performance is much slower!
        _workbook = jxl.Workbook.getWorkbook(_workbookStream, workbookSettings); 
      }
      catch (Exception e) {
        // TODO: on error, initialize ourself with an empty workbook
        String errorMsg = "could not read workbook: " + e.getMessage();
        _errors.addError(errorMsg);
        log.error(errorMsg);
        return null;
      }
    }
    return _workbook;
  }
  
  /**
   * Find a worksheet with the given name, case-insensitively. 
   * 
   * @motivation the jxl.Workbook API is case sensitive for worksheet names
   * @param targetSheetName the worksheet name
   * @return the first Sheet to match the specified name (case-insensitively), otherwise null
   */
  public Worksheet getWorksheet(String targetSheetName)
  {
    int i = 0;
    for (String sheetName : getWorkbook().getSheetNames()) {
      if (sheetName.equalsIgnoreCase(targetSheetName)) {
        return getWorksheet(i);
      }
      ++i;
    }
    return null;
  }
  
  /**
   * Find a worksheet with a name that matches the specified regular expression.
   * 
   * @param nameRegex
   * @return the first Sheet to match the specified regex, otherwise null
   */
  public Worksheet getWorksheet(Pattern nameRegex)
  {
    int i = 0;
    for (String sheetName : getWorkbook().getSheetNames()) {
      if (nameRegex.matcher(sheetName).matches()) {
        return getWorksheet(i);
      }
      ++i;
    }
    return null;
  }
  
  public Worksheet getWorksheet(int i)
  {
    return new Worksheet(this, i);
  }

  public WritableWorkbook save(OutputStream out)
    throws IOException
  {
    WritableWorkbook outputWorkbook = jxl.Workbook.createWorkbook(out, getWorkbook());
    outputWorkbook.write();
    log.info("saved workbook");
    return outputWorkbook;
  }

  /**
   * Add a workbook-global parsing error message. For cell-specific parsing
   * error messages, use {@link Cell#addError}.
   * 
   * @param msg the error message
   */
  public void addError(String msg)
  {
    _errors.addError(msg);
  }

  /**
   * @motivation for usage by Cell (avoid having to pass in a ParseErrorManager
   *             when instantiating Cell objects, since Workbook is already
   *             passed in)
   */
  /*package*/ ParseErrorManager getParseErrorManager() 
  {
    return _errors;
  }

  public WritableWorkbook writeErrorAnnotatedWorkbook(File out)
  {
    if (_errors.getHasErrors()) {
      try {
        WritableWorkbook errorAnnotatedWorkbook = _errors.getErrorAnnotatedWorkbook();
        errorAnnotatedWorkbook.setOutputFile(out);
        errorAnnotatedWorkbook.write();
        errorAnnotatedWorkbook.close();
        return errorAnnotatedWorkbook; 
      }
      catch (Exception e) {
        log.error("could not write error annotated workbook: " + e.getMessage());
      }
    }
    return null;
  }

  public Iterator<Worksheet> iterator()
  {
    return new WorksheetIterator(0,getWorkbook().getNumberOfSheets()-1) ;
  }
  
  public class WorksheetIterator implements Iterator<Worksheet>
  {
    private int _currentSheet;
    private int _end;
      
    /**
     * Create RowIterator that iterates between the specified range of rows.
     * @param fromRow the first row index to be iterated, zero-based, inclusive
     * @param toRow the last row index to be iterated, zero-based, inclusive; must be <= max supported row index by worksheet
     */
    public WorksheetIterator(int begin, int end)
    {
      _currentSheet = begin;
      _end = end;
    }
    
    public boolean hasNext()
    {
      return _end >= 0 && _currentSheet <= _end;
    }

    public Worksheet next()
    {
      return Workbook.this.getWorksheet(_currentSheet++);
    }

    public void remove()
    {
      throw new UnsupportedOperationException();
    }
  }

  public boolean getHasErrors()
  {
    return _errors.getHasErrors();
  }

  public List<WorkbookParseError> getErrors()
  {
    return _errors.getErrors();
  }

  public WritableWorkbook getErrorAnnotatedWorkbook()
  {
    return _errors.getErrorAnnotatedWorkbook();
  } 
  
  public String getName() { return _name; }
}
