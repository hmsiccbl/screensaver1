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
import java.util.ArrayList;
import java.util.List;

import edu.harvard.med.screensaver.io.ParseError;
import edu.harvard.med.screensaver.io.ParseErrors;

import org.apache.log4j.Logger;

/**
 * Maintains a list of error messages.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class FileParseErrorManager implements ParseErrors
{
  private static Logger log = Logger.getLogger(FileParseErrorManager.class);

  private List<FileParseError> _errors = new ArrayList<FileParseError>();

  /**
   * Add a simple error.
   */
  public void addError(String errorMessage)
  {
    FileParseError error = new FileParseError(errorMessage);
    _errors.add(error);
  }

  /**
   * Add an error, noting the file and record number associated with it.
   *
   * @param errorMessage the error
   * @param sdFile the SDFile associated with the error
   * @param sdRecordNumber the SDFile record number associated with the error
   */
  public void addError(String errorMessage, File sdFile, int sdRecordNumber)
  {
    FileParseError error =
      new FileParseError(errorMessage, sdFile, sdRecordNumber);
    _errors.add(error);
  }

  /**
   * Get the list of <code>FileParseError</code> objects.
   *
   * @return a list of <code>FileParseError</code> objects
   */
  public List<? extends ParseError> getErrors()
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
}
