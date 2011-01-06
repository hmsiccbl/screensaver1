// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screens;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.ui.arch.view.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.arch.view.aspects.UICommand;

/**
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class ScreenFinder extends AbstractBackingBean
{
  private static final Logger log = Logger.getLogger(ScreenFinder.class);

  private GenericEntityDAO _dao;
  private ScreenViewer _screenViewer;
  private ScreenDetailViewer _screenDetailViewer;

  private String _screenFacilityId;


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

  public String getScreenFacilityId()
  {
    return _screenFacilityId;
  }

  public void setScreenFacilityId(String screenFacilityId)
  {
    _screenFacilityId = screenFacilityId;
  }

  @UICommand
  public String findScreen()
  {
    if (_screenFacilityId != null) {
      Screen screen = _dao.findEntityByProperty(Screen.class,
                                                Screen.facilityId.getPropertyName(),
                                                _screenFacilityId);
      if (screen != null) {
        resetSearchFields();
        return _screenViewer.viewEntity(screen);
      }
      else {
        showMessage("noSuchEntity", "Screen " + _screenFacilityId);
      }
    }
    else {
      showMessage("screens.screenFacilityIdRequired", _screenFacilityId);
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  private void resetSearchFields()
  {
    _screenFacilityId = null;
  }

}

