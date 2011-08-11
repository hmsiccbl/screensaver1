// $HeadURL: http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/branches/iccbl/2.1.0-dev/src/edu/harvard/med/screensaver/model/libraries/LibraryPlate.java $
// $Id: LibraryPlate.java 4689 2010-09-24 18:40:57Z atolopko $
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import edu.harvard.med.screensaver.model.NonPersistentEntity;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivity;

public class PlateActivity extends NonPersistentEntity<Integer> implements Comparable<PlateActivity>
{
  private AdministrativeActivity _administrativeActivity;
  private Plate _plate;

  public PlateActivity(Integer entityId)
  {
    super(entityId);
  }

    public PlateActivity(Plate p, AdministrativeActivity ae)
  {
    this(ae.getEntityId());
    _administrativeActivity = ae;
    _plate = p;
    }

  @Override
  public int compareTo(PlateActivity pa)
  {
    return _administrativeActivity.compareTo(pa.getAdministrativeActivity());
    }

  @Override
  public boolean isRestricted()
  {
    return _administrativeActivity.isRestricted();
  }

  @Override
  public PlateActivity restrict()
  {
    if (isRestricted()) {
      return null;
    }
    return this;
  }

    public Plate getPlate()
  {
    return _plate;
  }

    public AdministrativeActivity getAdministrativeActivity()
  {
    return _administrativeActivity;
  }

  public String toString()
  {
    return "Plate: " + _plate + " Activity: " + _administrativeActivity;
  }
}
