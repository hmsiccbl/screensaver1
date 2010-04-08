// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screens;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.UsersDAO;
import edu.harvard.med.screensaver.model.screens.Study;
import edu.harvard.med.screensaver.ui.EditResult;

public class StudyDetailViewer extends AbstractStudyDetailViewer<Study>
{
  /**
   * @motivation for CGLIB2
   */
  protected StudyDetailViewer()
  {
  }

  public StudyDetailViewer(StudyDetailViewer thisProxy,
                           StudyViewer studyViewer,
                           GenericEntityDAO dao,
                           UsersDAO usersDao)
  {
    super(thisProxy, dao, ScreensaverConstants.VIEW_STUDY, usersDao);
  }
  
  @Override
  public boolean isEditable()
  {
    return false;
  }

  @Override
  protected String postEditAction(EditResult editResult)
  {
    return null;
  }
}

