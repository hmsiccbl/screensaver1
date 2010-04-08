// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.workbook2;

import edu.harvard.med.screensaver.io.ParseError;

/**
 * Pairs a workbook parsing error message with the (optional) affected cell, and
 * can generated a human-readable error message.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class WorkbookParseError extends ParseError
{
  private Cell _atCell;

  /**
   * Constructs a ParseError object, associated with a particular cell location.
   *
   * @param message the error message
   * @param atCell the cell associated with the error message
   */
  public WorkbookParseError(String message, Cell atCell)
  {
    super(message, atCell.toString());
    _atCell = (Cell) atCell.clone();
  }

  public WorkbookParseError(String message, Row atRow)
  {
    this(message, atRow.getCell(0));
  }

  /**
   * Constructs a general ParseError object, not specific to a particular cell
   * location.
   *
   * @param message the error message
   */
  public WorkbookParseError(String message)
  {
    super(message, "general error");
  }

  public Cell getCell()
  {
    return _atCell;
  }
}
