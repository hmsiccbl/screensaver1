// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.screenresults;

import edu.harvard.med.screensaver.io.workbook2.Workbook;

/**
 * Used to report an unrecoverable parse error; indicates that further parsing
 * is not possible; causes parsing to abort immediately.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class UnrecoverableScreenResultParseException extends Exception
{

  private Workbook _workbook;
  
  /**
   * 
   */
  private static final long serialVersionUID = 5285861320482270566L;
  
  public UnrecoverableScreenResultParseException(String message)
  {
    super(message);
  }

}
