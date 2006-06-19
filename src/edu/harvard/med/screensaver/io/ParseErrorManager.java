// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io;

import java.util.ArrayList;
import java.util.List;

/**
 * Maintains a list of error messages.
 * @author ant
 */
public class ParseErrorManager
{
  private List<String> _errors = new ArrayList<String>();
  
  /**
   * Add a simple error.
   * 
   * @param error the error
   */
  public void addError(String error)
  {
    _errors.add(error);
  }
  
  /**
   * Add an error, noting the particular cell the error is related to.
   * 
   * @param error the error
   * @param dataHeader the data header of the cell containing the error
   * @param row the {@link Row} of the cell containing the error
   */
  public void addError(String error,
                       CellReader cell)
  {
    _errors.add(error + " @ " + cell);
  }
  
  public List<String> getErrors()
  {
    return _errors;
  }

}
