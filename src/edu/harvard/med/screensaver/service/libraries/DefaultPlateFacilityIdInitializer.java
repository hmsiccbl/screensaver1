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
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import edu.harvard.med.screensaver.model.libraries.Plate;

public class DefaultPlateFacilityIdInitializer implements PlateFacilityIdInitializer
{
  private static final Joiner JOINER = Joiner.on('-');

  @Override
  public boolean initializeFacilityId(Plate plate)
  {
    Iterable<Object> facilityIdParts = Iterables.filter(Lists.<Object>newArrayList(plate.getPlateNumber(), plate.getCopy().getName(), plate.getLocation()), Predicates.notNull());
    plate.setFacilityId(JOINER.join(facilityIdParts));
    return true;
  }
}
