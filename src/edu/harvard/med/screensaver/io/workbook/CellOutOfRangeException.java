// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/io/CellOutOfRangeException.java $
// $Id: CellOutOfRangeException.java 275 2006-06-28 15:32:40Z js163 $
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
 * @author ant
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
