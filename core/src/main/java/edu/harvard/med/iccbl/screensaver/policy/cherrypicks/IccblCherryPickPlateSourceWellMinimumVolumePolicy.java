// $HeadURL$
// $Id$
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.policy.cherrypicks;

import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.VolumeUnit;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.policy.CherryPickPlateSourceWellMinimumVolumePolicy;
import edu.harvard.med.screensaver.util.DevelopmentException;

public class IccblCherryPickPlateSourceWellMinimumVolumePolicy implements CherryPickPlateSourceWellMinimumVolumePolicy
{
  private static final Volume RNAI_MINIMUM_WELL_VOLUME = new Volume(5, VolumeUnit.MICROLITERS);
  private static final Volume SM_MINIMUM_WELL_VOLUME = new Volume(-2, VolumeUnit.MICROLITERS);

  @Override
  public Volume getMinimumVolumeAllowed(Well well)
  {
    switch (well.getLibrary().getScreenType()) {
      case RNAI:
        return RNAI_MINIMUM_WELL_VOLUME;
      case SMALL_MOLECULE:
        return SM_MINIMUM_WELL_VOLUME;
      default:
        throw new DevelopmentException("unhandled library screen type: " + well.getLibrary().getScreenType());
    }
  }
}
