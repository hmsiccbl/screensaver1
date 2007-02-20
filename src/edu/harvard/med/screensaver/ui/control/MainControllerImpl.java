// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.control;

import edu.harvard.med.screensaver.ui.ExceptionReporter;

import org.apache.log4j.Logger;

/**
 * A controller for top-level pages and controls such as logging in and out, going to the
 * help page, and other sophisticated controls.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class MainControllerImpl extends AbstractUIController implements MainController
{
  // static members

  private static Logger log = Logger.getLogger(MainController.class);


  // instance data members
  
  private ExceptionReporter _exceptionReporter;

  
  public void setExceptionReporter(ExceptionReporter exceptionReporter)
  {
    _exceptionReporter = exceptionReporter;
  }

  
  // public constructors and methods
  
  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.ui.control.MainController#viewMain()
   */
  @UIControllerMethod
  public String viewMain()
  {
    return VIEW_MAIN;
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.ui.control.MainController#viewInstructions()
   */
  @UIControllerMethod
  public String viewInstructions()
  {
    return VIEW_INSTRUCTIONS;
  }
  
  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.ui.control.MainController#viewDownloads()
   */
  @UIControllerMethod
  public String viewDownloads()
  {
    return VIEW_DOWNLOADS;
  }
  
  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.ui.control.MainController#logout()
   */
  public String logout()
  {
    log.info("logout for session "  + getHttpSession().getId());
    closeHttpSession();
    return VIEW_GOODBYE;
  }
}
