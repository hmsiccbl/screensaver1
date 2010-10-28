// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.screenresult;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.ScreenResultsDAO;
import edu.harvard.med.screensaver.model.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.users.AdministratorUser;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;


/**
 * Deletes the {@link ScreenResult} from a {@link Screen}, and adds update
 * activity.
 */
public class ScreenResultDeleter
{
  private static final String COMMENTS = "screen result deleted";

  private static Logger log = Logger.getLogger(ScreenResultDeleter.class);

  private GenericEntityDAO _dao;
  private ScreenResultsDAO _screenResultsDao;

  
  /**
   * @motivation for CGLIB2
   */
  protected ScreenResultDeleter() {}

  public ScreenResultDeleter(GenericEntityDAO dao,
                             ScreenResultsDAO screenResultsDao)
  {
    _dao = dao;
    _screenResultsDao = screenResultsDao;
  }

  @Transactional
  public Screen deleteScreenResult(ScreenResult screenResult, AdministratorUser admin)
  {
    Screen screen = _dao.reloadEntity(screenResult.getScreen());
    admin = _dao.reloadEntity(admin);
    _screenResultsDao.deleteScreenResult(screenResult);
    screen.createUpdateActivity(AdministrativeActivityType.SCREEN_RESULT_DATA_DELETION, admin, COMMENTS);
    screen.update();
    
    return screen;
  }
}
