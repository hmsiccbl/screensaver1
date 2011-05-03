// $HeadURL$
// $Id$
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.screens;

import edu.harvard.med.screensaver.model.screens.Screen;

/**
 * Service that initialized a {@link Screen}'s {@link Screen#facilityId facility identifier} using a facility-specific
 * convention for generating these identifiers.
 * 
 * @author atolopko
 */
public interface ScreenFacilityIdInitializer
{
  /**
   * @param screen
   * @return true if the Screen's facility identifier was updated
   */
  public boolean initializeFacilityId(Screen screen);
}
