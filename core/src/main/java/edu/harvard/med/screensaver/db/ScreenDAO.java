// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.util.List;
import java.util.Set;

import edu.harvard.med.screensaver.model.screens.LabActivity;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;

public interface ScreenDAO
{
  void deleteStudy(Screen study);
  int countScreenedExperimentalWells(Screen screen, boolean distinct);
  int countTotalPlatedLabCherryPicks(Screen screen);

  List<Screen> findRelatedScreens(Screen screen);

  boolean isScreenFacilityIdUnique(Screen screen);

  Set<ScreensaverUser> findLabActivityPerformedByCandidates(LabActivity a);

  int countLoadedExperimentalWells(Screen screen);
}
