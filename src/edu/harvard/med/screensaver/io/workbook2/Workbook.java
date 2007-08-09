// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/io/Workbook.java $
// $Id: Workbook.java 324 2006-07-12 16:58:16Z ant4 $
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

import jxl.WorkbookSettings;
import jxl.write.WritableWorkbook;

import edu.harvard.med.screensaver.util.FileUtils;

import org.apache.log4j.Logger;

/**
 * Encapsulates the (lazy) instantiation of an jxl.Workbook from a file. Also
 * allows the workbook to be associated with the filename it originated from.
 * 
 * @author ant
 */
public class Workbook
{
  private static Logger log = Logger.getLogger(Workbook.class);
  
  public static final String MIME_TYPE = "application/vnd.ms-excel";
  
  private File _workbookFile;
  private InputStream _workbookStream;
  private jxl.Workbook _workbook;
  private ParseErrorManager _errors;


  public Workbook(File workbookFile, ParseErrorManager errors) throws FileNotFoundException
  {
    this(workbookFile, new FileInputStream(workbookFile), errors);
  }

  public Workbook(File workbookFile, InputStream workbookStream, ParseErrorManager errors)
  {
    _workbookFile = workbookFile;
    _workbookStream = workbookStream;
    _errors = errors;
    if (_errors == null) {
      // to avoid NPEs
      _errors = new ParseErrorManager();
    }
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
        WorkbookSettings workbookSettings = new WorkbookSettings();
        workbookSettings.setGCDisabled(true); // when GC feature is enabled, performance is much slower!
        _workbook = jxl.Workbook.getWorkbook(_workbookStream, workbookSettings); 
      }
      catch (Exception e) {
        // TODO: on error, initialize ourself with an empty workbook
        String errorMsg = "could not read workbook '" + _workbookFile.getAbsolutePath() + "': " + e.getMessage();
        _errors.addError(errorMsg);
        log.error(errorMsg);
        return null;
      }
    }
    return _workbook;
  }
  
  public File getWorkbookFile()
  {
    return _workbookFile;
  }

  /**
   * Find a worksheet with the given name, case-insensitively. If not found,
   * adds an error and returns <code>null</code>.
   * 
   * @motivation the jxl.Workbook API is case sensitive for worksheet names
   * @param name the worksheet name
   * @return the first Sheet to match the specified name
   *         (case-insensitively)
   */
  public int findSheetIndex(String targetSheetName)
  {
    int i = 0;
    for (String sheetName : getWorkbook().getSheetNames()) {
      if (sheetName.equalsIgnoreCase(targetSheetName)) {
        return i;
      }
      ++i;
    }
    throw new IllegalArgumentException("no such sheet '" + targetSheetName + "'");
  }
  
  public String toString()
  {
    return _workbookFile.getName();
  }

  /**
   * Saves the Workbook, optionally to a new outputDirectory and/or optionally
   * with a new file extension.
   * 
   * @param newDirectory the output directory; if null the workbook's original
   *          file directory is used.
   * @param newExtension the extension to use when saving the workbook,
   *          replacing the workbook's original filename extension; if null
   *          original filename extension is used. A leading period will added
   *          iff it does not exist.
   * @return the File the workbook was saved as
   * @throws IOException
   */
  public File save(File newDirectory, String newExtension)
    throws IOException
  {
    File outputFile = FileUtils.modifyFileDirectoryAndExtension(_workbookFile,
                                                                newDirectory,
                                                                newExtension);
    WritableWorkbook outputWorkbook = jxl.Workbook.createWorkbook(outputFile, _workbook);
    outputWorkbook.write();
    log.info("saved workbook " + _workbookFile + " as " + outputFile);
    return outputFile;
  }
}
