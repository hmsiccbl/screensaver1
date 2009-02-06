// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.util;

/**
 * Report an exception that must have been due to a developer's error.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class DevelopmentException extends RuntimeException
{
  /**
   * 
   */
  private static final long serialVersionUID = 8564443137528457596L;

  public DevelopmentException(String message)
  {
    super(message);
  }
}
