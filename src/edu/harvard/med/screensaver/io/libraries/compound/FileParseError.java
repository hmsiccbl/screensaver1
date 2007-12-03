// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries.compound;

import java.io.File;

import edu.harvard.med.screensaver.io.ParseError;

/**
 * Contains pertinent data for parse errors. Essentially, a data struct, with a
 * toString() method.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class FileParseError implements ParseError
{
  private String _message;
  private File _file;
  private int _recordNumber;
  
  /**
   * Construct a <code>FileParseError</code>, containing the error message, the
   * associated SDFile, and the associated SDFile record number.
   * 
   * @param message the error message
   * @param file the associated file
   * @param recordNumber the associated record number in the file
   */
  public FileParseError(String message, File file, int recordNumber)
  {
    _message = message;
    _file = file;
    _recordNumber = recordNumber;
  }
  
  /**
   * Construct a <code>FileParseError</code>, containing the error message, but
   * not specific to any SDFile, or SDFile record number.
   * 
   * @param message the error message
   */
  public FileParseError(String message)
  {
    _message = message;
  }
  
  public String toString()
  {
    return _message + " @ " + _file + " (line " + _recordNumber + ")";
  }
  
  public String getErrorMessage() 
  {
    return _message;
  }
  
  public File getSDFile()
  {
    return _file;
  }
  
  public int getRecordNumber()
  {
    return _recordNumber;
  }
  
  /**
   * @motivation for unit testing
   */
  public boolean equals(Object o)
  {
    if (!(o instanceof FileParseError)) {
      return false;
    }
    FileParseError that = (FileParseError) o;
    return this.toString().equals(that.toString());
  }
}
