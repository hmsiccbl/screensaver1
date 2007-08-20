// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screens;

import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.control.ScreensController;

import org.apache.log4j.Logger;

/**
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class CherryPickRequestFinder extends AbstractBackingBean
{
  
  // private static final fields
  
  private static final Logger log = Logger.getLogger(CherryPickRequestFinder.class);
  
  
  // private instance fields
  
  private ScreensController _screensController;
  private Integer _cherryPickRequestNumber;

  
  // public instance methods
  
  public ScreensController getScreensController()
  {
    return _screensController;
  }
  
  public void setScreensController(ScreensController screensController)
  {
    _screensController = screensController;
  }

  public Integer getCherryPickRequestNumber()
  {
    return _cherryPickRequestNumber;
  }

  public void setCherryPickRequestNumber(Integer screenNumber)
  {
    _cherryPickRequestNumber = screenNumber;
  }

  /**
   * Find the screen with the specified screen number, and go to the appropriate next
   * page depending on the result.
   * @return the control code for the appropriate next page
   */
  public String findCherryPickRequest()
  {
    return _screensController.findCherryPickRequest(_cherryPickRequestNumber);
  }
}
