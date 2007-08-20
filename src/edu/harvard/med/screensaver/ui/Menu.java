// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;


import edu.harvard.med.screensaver.ui.control.UIControllerMethod;

import org.apache.log4j.Logger;


public class Menu extends AbstractBackingBean
{

  // static data members

  private static Logger log = Logger.getLogger(Menu.class);


  // JSF application methods

  @UIControllerMethod
  public String viewMain()
  {
    return VIEW_MAIN;
  }

  @UIControllerMethod
  public String viewNews()
  {
    return VIEW_NEWS;
  }

  @UIControllerMethod
  public String viewDownloads()
  {
    return VIEW_DOWNLOADS;
  }

  @UIControllerMethod
  public String viewHelp()
  {
    return VIEW_HELP;
  }

  public String logout()
  {
    log.info("logout for session "  + getHttpSession().getId());
    closeHttpSession();
    return VIEW_GOODBYE;
  }
}
