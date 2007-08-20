// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.control;


/**
 * @motivation Allows Spring to create an AOP proxy for our MainControllerImpl
 *             concrete class, which can then be injected into other beans
 *             expecting a MainController type. If MainController was the
 *             concrete class, its proxy would not be injectable into other
 *             beans.
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public interface MainController
{

  @UIControllerMethod
  public String viewMain();

  @UIControllerMethod
  public String viewNews();

  @UIControllerMethod
  public String viewDownloads();

  @UIControllerMethod
  public String viewHelp();

  @UIControllerMethod
  public String logout();

}
