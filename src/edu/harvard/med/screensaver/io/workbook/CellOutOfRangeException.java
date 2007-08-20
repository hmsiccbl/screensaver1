// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.workbook;

/**
 * Exception for reporting attempt to read a cell that is outside of the range
 * of valid cells in an <code>HSSFSheet</code> worksheet.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class CellOutOfRangeException extends Exception
{
  private static final long serialVersionUID = -6769776818247218394L;

  public static enum UndefinedInAxis {ROW, COLUMN};
  
  /**
   * Constructs a CellOutOfRangeException exception object.
   * 
   * @param undefinedInAxis whether the row, or column, or both, were undefined
   *          in the worksheet
   * @param cell the cell that was undefined
   */
  public CellOutOfRangeException(UndefinedInAxis undefinedInAxis,
                                 Cell cell)
  {
    super("cell " + cell + " undefined at " + undefinedInAxis.toString());
  }
}
