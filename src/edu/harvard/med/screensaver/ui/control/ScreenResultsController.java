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

import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.ui.screenresults.ScreenResultViewer;
import edu.harvard.med.screensaver.ui.screens.ScreenViewer;

/**
 * 
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class ScreenResultsController extends AbstractUIController
{
  
  // private static final fields
  
  private static final Logger log = Logger.getLogger(ScreenResultsController.class);
  private static String VIEW_SCREEN_RESULT = "viewScreenResult";
  
  
  // private instance fields
  
  private ScreenViewer _screenViewer;
  private ScreenResultViewer _screenResultViewer;
  
  
  // public getters and setters

  public ScreenViewer getScreenViewer()
  {
    return _screenViewer;
  }
  
  public void setScreenViewer(ScreenViewer screenViewer)
  {
    _screenViewer = screenViewer;
    _screenViewer.setScreenResultsController(this);
  }
  
  public ScreenResultViewer getScreenResultViewer()
  {
    return _screenResultViewer;
  }
  
  public void setScreenResultViewer(ScreenResultViewer screenResultViewer)
  {
    _screenResultViewer = screenResultViewer;
  }
  
  
  // public control methods
  
  public String viewScreenResult(ScreenResult screenResult)
  {
    _screenResultViewer.setScreenResult(screenResult);
    return "viewScreenResult"; 
  }
}

