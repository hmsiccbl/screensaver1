// $HeadURL: $
// $Id: $
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.domainlogic;

import org.springframework.transaction.annotation.Transactional;

import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.service.libraries.PlateFacilityIdInitializer;

public class PlateEntityUpdater implements EntityUpdater<Plate>
{
  private PlateFacilityIdInitializer _plateFacilityIdInitializer;

  /** for CGLIB2 */
  protected PlateEntityUpdater()
  {}

  public PlateEntityUpdater(PlateFacilityIdInitializer plateFacilityIdInitializer)
  {
    _plateFacilityIdInitializer = plateFacilityIdInitializer;
  }

  @Override
  public Class<Plate> getEntityClass()
  {
    return Plate.class;
  }

  @Override
  @Transactional
  public void apply(Plate plate)
  {
    _plateFacilityIdInitializer.initializeFacilityId(plate);
  }
}
