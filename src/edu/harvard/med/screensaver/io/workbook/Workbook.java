// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/io/Workbook.java $
// $Id: Workbook.java 324 2006-07-12 16:58:16Z ant4 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.workbook;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * Encapsulates the (lazy) instantiation of an HSSFWorkbook from a file. Also
 * allows the workbook to be associated with the filename it originated from.
 * 
 * @motivation HSSFWorkbook does not store the file whence it orignated
 * @author ant
 */
public class Workbook
{
  private static Logger log = Logger.getLogger(Workbook.class);
  
  public static final String MIME_TYPE = "application/vnd.ms-excel";
  
  private File _workbookFile;
  private HSSFWorkbook _workbook;
  private ParseErrorManager _errors;


  public Workbook(File workbookFile, ParseErrorManager errors)
  {
    _workbookFile = workbookFile;
    _errors = errors;
  }
  
  /**
   * Returns an HSSFWorkbook, with lazy instantiation.
   * @return an HSSFWorkbook
   * @throws IOException if file is not found or cannot be read
   */
  public HSSFWorkbook getWorkbook() 
  {
    if (_workbook == null) {
      try {
        InputStream inputStream = new FileInputStream(_workbookFile);
        // TODO: do we need this check?
        if (inputStream == null) {
          throw new FileNotFoundException("could not find file " + _workbookFile);
        }
        POIFSFileSystem dataFs = new POIFSFileSystem(new BufferedInputStream(inputStream));
        _workbook = new HSSFWorkbook(dataFs);
      }
      catch (IOException e) {
        String errorMsg = "could not read workbook '" + _workbookFile.getAbsolutePath() + "': " + e.getMessage();
        _errors.addError(errorMsg);
        log.error(errorMsg);

        // avoid NPE in calling code
        _workbook = new HSSFWorkbook();
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
   * @motivation the HSSFWorkbook API is case sensitive for worksheet names
   * @param name the worksheet name
   * @return the first HSSFSheet to match the specified name
   *         (case-insensitively)
   */
  public int findSheetIndex(String targetSheetName)
  {
    for (int i = 0; i < getWorkbook().getNumberOfSheets(); ++i) {
      String name = getWorkbook().getSheetName(i);
      if (targetSheetName.equalsIgnoreCase(name)) {
        return i;
      }
    }
    throw new IllegalArgumentException("no such sheet '" + targetSheetName + "'");
  }
  
  public String toString()
  {
    return _workbookFile.getName();
  }

  public File save(String copyExtension) throws IOException
  {
    if (copyExtension.startsWith(".")) {
      copyExtension = copyExtension.substring(1);
    }
    String outputFileName = _workbookFile.getAbsoluteFile() + "." + copyExtension;
    FileOutputStream out = new FileOutputStream(outputFileName);
    _workbook.write(out);
    out.close();
    log.info("saved workbook " + _workbookFile + " as " + outputFileName);
    return new File(outputFileName);
  }
  
}
