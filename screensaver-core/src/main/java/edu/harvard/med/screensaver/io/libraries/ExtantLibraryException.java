// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries;

/**
 * Used to report a failed attempt to find an extant
 * {@link edu.harvard.med.screensaver.model.libraries.Library} or
 * {@link edu.harvard.med.screensaver.model.libraries.Well} or
 * {@link edu.harvard.med.screensaver.model.libraries.Reagent}, indicating
 * that the Library was expected to have been loaded, but apparently was not.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class ExtantLibraryException extends Exception
{
  /**
   * 
   */
  private static final long serialVersionUID = -1176739726988293758L;

  public ExtantLibraryException(String message)
  {
    super(message);
  }
}
