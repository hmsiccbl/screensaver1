// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screens;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.UIControllerMethod;

import org.apache.log4j.Logger;

/**
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class ScreenFinder extends AbstractBackingBean
{

  // private static final fields

  private static final Logger log = Logger.getLogger(ScreenFinder.class);


  // private instance fields

  private GenericEntityDAO _dao;
  private ScreenViewer _screenViewer;
  private ScreenDetailViewer _screenDetailViewer;

  private Integer _screenNumber;


  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected ScreenFinder()
  {
  }

  public ScreenFinder(GenericEntityDAO dao,
                      ScreenViewer screenViewer,
                      ScreenDetailViewer screenDetailViewer)
  {
    _dao = dao;
    _screenViewer = screenViewer;
    _screenDetailViewer = screenDetailViewer;
  }


  // public instance methods

  public Integer getScreenNumber()
  {
    return _screenNumber;
  }

  public void setScreenNumber(Integer screenNumber)
  {
    _screenNumber = screenNumber;
  }

  @UIControllerMethod
  public String findScreen()
  {
    if (_screenNumber != null) {
      Screen screen = _dao.findEntityByProperty(Screen.class,
                                                "screenNumber",
                                                _screenNumber);
      if (screen != null) {
        resetSearchFields();
        return _screenViewer.viewScreen(screen);
      }
      else {
        showMessage("noSuchEntity",
                    "Screen " + _screenNumber);
      }
    }
    else {
      showMessage("screens.screenNumberRequired", _screenNumber);
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  public String addLibraryScreening()
  {
    String result = findScreen();
    if (result.equals(BROWSE_SCREENS)) {
      return _screenDetailViewer.addLibraryScreening();
    }
    return result;
  }

  public String addCherryPickRequest()
  {
    String result = findScreen();
    if (result.equals(BROWSE_SCREENS)) {
      return _screenDetailViewer.addCherryPickRequest();
    }
    return result;
  }
  
  private void resetSearchFields()
  {
    _screenNumber = null;
  }

}

