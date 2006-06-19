// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io;

/**
 * Exception for reporting problems reading cells from an <code>HSSFSheet</code>
 * worksheet.
 * 
 * @author ant
 */
public class CellUndefinedException extends Exception
{
  private static final long serialVersionUID = -6769776818247218394L;

  public static enum UndefinedInAxis { ROW, COLUMN, ROW_AND_COLUMN };
  
  /**
   * Constructs a CellUndefinedException exception object.
   * 
   * @param undefinedInAxis whether the row, or column, or both, were undefined
   *          in the worksheet
   * @param cell the cell that was undefined
   */
  public CellUndefinedException(UndefinedInAxis undefinedInAxis,
                                CellReader cell)
  {
    super("cell " + cell + " undefined at " + 
          undefinedInAxis.toString());
  }
}
