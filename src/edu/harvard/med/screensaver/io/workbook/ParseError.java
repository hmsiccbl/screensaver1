// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/io/ParseError.java $
// $Id: ParseError.java 275 2006-06-28 15:32:40Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.workbook;

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
   _atCell = (Cell) atCell.clone();
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
