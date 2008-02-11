// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries.rnai;

import edu.harvard.med.screensaver.io.workbook.Cell;

/**
 * 
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class DataRowParserException extends Exception
{
  private static final long serialVersionUID = 1L;

  private Cell _cell;
  
  public DataRowParserException(String message, Cell cell)
  {
    super(message);
    _cell = cell;
  }
  
  public Cell getCell()
  {
    return _cell;
  }
}

