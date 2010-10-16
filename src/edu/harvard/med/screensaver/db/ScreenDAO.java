// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import edu.harvard.med.screensaver.model.screens.Screen;

public interface ScreenDAO
{
  void deleteStudy(Screen study);
  int countScreenedExperimentalWells(Screen screen, boolean distinct);
  int countFulfilledLabCherryPicks(Screen screen);
}
