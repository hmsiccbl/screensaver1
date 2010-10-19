// $HeadURL: $
// $Id: $
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.libraries;

import edu.harvard.med.screensaver.model.libraries.Plate;

/**
 * Service that initializes a {@link Plate}'s {@link Plate#facilityId facility identifier} using a facility-specific
 * convention for generating these identifiers.
 * 
 * @author atolopko
 */
public interface PlateFacilityIdInitializer
{
  /**
   * @param Plate
   * @return true if the Plate's facility identifier was updated
   */
  public boolean initializeFacilityId(Plate plate);
}
