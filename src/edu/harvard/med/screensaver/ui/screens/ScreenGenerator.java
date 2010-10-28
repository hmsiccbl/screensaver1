// $HeadURL$
// $Id$
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screens;

import edu.harvard.med.screensaver.model.screens.ProjectPhase;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

/**
 * Service to create a new, non-persisted {@link Screen}, with properties initialized as appropriate for a particular
 * facility.
 * 
 * @motivation allows screens created via the user interface to be initialized in a way that supports facility workflow
 *             and policies.
 * @author atolopko
 */
public interface ScreenGenerator
{
  Screen createPrimaryScreen(AdministratorUser admin, ScreeningRoomUser leadScreener, ScreenType screenType);

  Screen createRelatedScreen(AdministratorUser admin, Screen primaryScreen, ProjectPhase projectPhase);
}
