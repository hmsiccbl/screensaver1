// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.workbook;

import edu.harvard.med.screensaver.io.ParseError;

/**
 * Pairs a workbook parsing error message with the (optional) affected cell, and
 * can generated a human-readable error message.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class WorkbookParseError implements ParseError
{
  private String _message;
  private Cell _atCell;

  /**
   * Constructs a ParseError object, associated with a particular cell location.
   *
   * @param errorMessage the error message
   * @param atCell the cell associated with the error message
   */
  public WorkbookParseError(String message, Cell atCell)
  {
   _message = message;
   _atCell = (Cell) atCell.clone();
  }

  /**
   * Constructs a general ParseError object, not specific to a particular cell
   * location.
   *
   * @param message the error message
   */
  public WorkbookParseError(String message)
  {
    _message = message;
  }

  public String toString()
  {
    return _message + (_atCell == null ? "" : " @ " + _atCell);
  }

  public String getErrorMessage()
  {
    return _message;
  }
  
  public String getErrorLocation()
  {
    return getCell() == null ? "" : getCell().toString();
  }

  public Cell getCell()
  {
    return _atCell;
  }

  /**
   * @motivation for unit testing
   */
  public boolean equals(Object o)
  {
    if (!(o instanceof WorkbookParseError)) {
      return false;
    }
    WorkbookParseError that = (WorkbookParseError) o;
    return this.toString().equals(that.toString());
  }
}
