// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io;

public class CellUndefinedException extends Exception
{
  private static final long serialVersionUID = -6769776818247218394L;

  public static enum UndefinedInAxis { ROW, COLUMN, ROW_AND_COLUMN };
  
  public CellUndefinedException(UndefinedInAxis undefinedInAxis,
                                short row,
                                short column)
  {
    super("cell (" + Character.toString((char) (column + 'A')) + ", " + (row + 1) + ") undefined at " + 
          undefinedInAxis.toString());
  }
}
