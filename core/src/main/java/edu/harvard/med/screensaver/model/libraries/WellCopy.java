// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.NonPersistentEntity;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.VolumeUnit;

public class WellCopy extends NonPersistentEntity<String> implements Comparable<WellCopy>
{
  private static final long serialVersionUID = 1L;

  private static Logger log = Logger.getLogger(WellCopy.class);


  private Well _well;
  private Copy _copy;
  private Volume _initialVolume;
  private Volume _consumedVolume;
  private Volume _remainingVolume;
  private List<WellVolumeAdjustment> _wellVolumeAdjustments;



  public WellCopy(Well well, Copy copy)
  {
    super(well.getEntityId() + ":" + copy.getName());
    _well = well;
    _copy = copy;
    Plate plate = _copy.getPlates().get(_well.getPlateNumber());
    if (plate == null || plate.getWellVolume() == null) {
      _initialVolume = VolumeUnit.ZERO;
    }
    else {
      _initialVolume = plate.getWellVolume();
    }
    _remainingVolume = _initialVolume;
    _wellVolumeAdjustments = new ArrayList<WellVolumeAdjustment>();
  }

  public void addWellVolumeAdjustment(WellVolumeAdjustment wellVolumeAdjustment)
  {
    assert wellVolumeAdjustment.getWell().equals(_well) : "all wellVolumeAdjustments must be for same well";
    assert wellVolumeAdjustment.getCopy().equals(_copy) : "all wellVolumeAdjustments must be for same copy";
    _remainingVolume = _remainingVolume.add(wellVolumeAdjustment.getVolume());
    _wellVolumeAdjustments.add(wellVolumeAdjustment);
    _consumedVolume = null; // force re-compute
  }

  public Copy getCopy()
  {
    return _copy;
  }

  public Volume getInitialVolume()
  {
    return _initialVolume;
  }

  public Volume getConsumedVolume()
  {
    if (_consumedVolume == null) {
      _consumedVolume = _initialVolume.subtract(_remainingVolume);
    }
    return _consumedVolume;
  }

  public Volume getRemainingVolume()
  {
    return _remainingVolume;
  }

  public Well getWell()
  {
    return _well;
  }

  public List<WellVolumeAdjustment> getWellVolumeAdjustments()
  {
    return _wellVolumeAdjustments;
  }

  @Override
  public String toString()
  {
    return getEntityId() + "=" + _remainingVolume;
  }

  public int compareTo(WellCopy that)
  {
    return this.getEntityId().compareTo(that.getEntityId());
  }

  @Override
  public boolean isRestricted()
  {
    return _well.isRestricted() || _copy.isRestricted();
  }

  @Override
  public WellCopy restrict()
  {
    if (isRestricted()) {
      return null;
    }
    return this;
  }
}
