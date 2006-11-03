// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/ui/ScreenViewer.java $
// $Id: ScreenViewer.java 443 2006-08-09 20:43:32Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screens;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.control.ScreensController;
import edu.harvard.med.screensaver.ui.control.UIControllerMethod;
import edu.harvard.med.screensaver.ui.searchresults.ScreenSearchResults;

/**
 * TODO: add comments
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class ScreensBrowser extends AbstractBackingBean
{
  
  // private static fields
  
  private static Logger log = Logger.getLogger(ScreensBrowser.class);
  
  
  // private instance fields
  
  private ScreensController _screensController;
  private ScreenSearchResults _screenSearchResults;
  
  
  // public instance methods

  public ScreensController getScreensController()
  {
    return _screensController;
  }
  
  public void setScreensController(ScreensController screensController)
  {
    _screensController = screensController;
  }

  public ScreenSearchResults getScreenSearchResults()
  {
    return _screenSearchResults;
  }

  public void setScreenSearchResults(ScreenSearchResults searchResults)
  {
    _screenSearchResults = searchResults;
  }

  @UIControllerMethod
  public String browseScreens()
  {
    return _screensController.browseScreens();
  }
}
