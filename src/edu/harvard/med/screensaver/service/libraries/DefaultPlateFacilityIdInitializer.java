// $HeadURL: $
// $Id: $
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.libraries;

import com.google.common.base.Joiner;

import edu.harvard.med.screensaver.model.libraries.Plate;

public class DefaultPlateFacilityIdInitializer implements PlateFacilityIdInitializer
{
  private static final Joiner JOINER = Joiner.on('-');

  @Override
  public boolean initializeFacilityId(Plate plate)
  {
    plate.setFacilityId(JOINER.join(plate.getPlateNumber(), plate.getCopy().getName(), plate.getLocation()));
    return true;
  }
}
