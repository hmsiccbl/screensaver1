// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/io/ParseError.java $
// $Id: ParseError.java 275 2006-06-28 15:32:40Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries.compound;

import java.io.File;

/**
 * Contains pertinent data for parse errors. Essentially, a data struct, with a
 * toString() method.
 * 
 * @author ant
 */
public class SDFileParseError
{
  private String _message;
  private File _sdFile;
  private int _sdRecordNumber;
  
  /**
   * Construct a <code>SDFileParseError</code>, containing the error message, the
   * associated SDFile, and the associated SDFile record number.
   * 
   * @param errorMessage the error message
   * @param sdFile the associated SDFile
   * @param sdRecordNumber the associated SDFile record number
   */
  public SDFileParseError(String message, File sdFile, int sdRecordNumber)
  {
    _message = message;
    _sdFile = sdFile;
    _sdRecordNumber = sdRecordNumber;
  }
  
  /**
   * Construct a <code>SDFileParseError</code>, containing the error message, but
   * not specific to any SDFile, or SDFile record number.
   * 
   * @param errorMessage the error message
   */
  public SDFileParseError(String message)
  {
    _message = message;
  }
  
  public String toString()
  {
    return _message + " @ " + _sdFile + " (line " + _sdRecordNumber + ")";
  }
  
  public String getMessage() 
  {
    return _message;
  }
  
  public File getSDFile()
  {
    return _sdFile;
  }
  
  public int getSDFileRecordNumber()
  {
    return _sdRecordNumber;
  }
  
  /**
   * @motivation for unit testing
   */
  public boolean equals(Object o)
  {
    if (!(o instanceof SDFileParseError)) {
      return false;
    }
    SDFileParseError that = (SDFileParseError) o;
    return this.toString().equals(that.toString());
  }
}
