// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.control;

import org.apache.log4j.Logger;

/**
 * A controller for top-level pages and controls such as logging in and out, going to the
 * help page, and other sophisticated controls.
 * 
 * TODO: move control code from login, main, etc, to here.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class MainController extends AbstractUIController
{
  // static members

  private static Logger log = Logger.getLogger(MainController.class);


  // instance data members

  // public constructors and methods

  public String viewHelp()
  {
    return "viewHelp";
  }
  
  public String viewDownloads()
  {
    return "viewDownloads";
  }
}

