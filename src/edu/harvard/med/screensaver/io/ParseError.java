// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io;

/**
 * Contains pertinent data for parse errors. Essentially, a data struct, with a
 * toString() method.
 * 
 * @author ant
 */
public class ParseError
{
  private String _message;
  private Cell _atCell;
  
  /**
   * Constructs a ParseError object, associated with a particular cell location.
   * 
   * @param errorMessage the error message
   * @param atCell the cell associated with the error message
   */
  public ParseError(String message, Cell atCell)
  {
   _message = message;
   _atCell = atCell;
  }
  
  /**
   * Constructs a general ParseError object, not specific to a particular cell
   * location.
   * 
   * @param errorMessage the error message
   */
  public ParseError(String message)
  {
    _message = message;
  }
  
  public String toString()
  {
    return _message + " @ " + _atCell;
  }
  
  public String getMessage() 
  {
    return _message;
  }
  
  public Cell getCell()
  {
    return _atCell;
  }
}
