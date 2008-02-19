// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyInfo;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellVolumeAdjustment;
import edu.harvard.med.screensaver.util.Pair;

import org.apache.log4j.Logger;

public class WellCopy implements Comparable<WellCopy>
{
  // static members

  private static final long serialVersionUID = 1L;

  private static Logger log = Logger.getLogger(WellCopy.class);


  // instance data members

  private Pair<WellKey,String> _key;
  private Well _well;
  private Copy _copy;
  private BigDecimal _initialMicroliterVolume;
  private BigDecimal _consumedMicroliterVolume;
  private BigDecimal _remainingMicroliterVolume;
  private List<WellVolumeAdjustment> _wellVolumeAdjustments;


  // public constructors and methods

  public WellCopy(Well well, Copy copy)
  {
    _well = well;
    _copy = copy;
    CopyInfo copyInfo = _copy.getCopyInfo(_well.getPlateNumber());
    if (copyInfo == null) {
      _initialMicroliterVolume = BigDecimal.ZERO.setScale(Well.VOLUME_SCALE);
    }
    else {
      _initialMicroliterVolume = copyInfo.getMicroliterWellVolume();
    }
    _remainingMicroliterVolume = _initialMicroliterVolume;
    _wellVolumeAdjustments = new ArrayList<WellVolumeAdjustment>();
    _key = new Pair<WellKey,String>(_well.getWellKey(), copy.getName());
  }

  public void addWellVolumeAdjustment(WellVolumeAdjustment wellVolumeAdjustment)
  {
    assert wellVolumeAdjustment.getWell().equals(_well) : "all wellVolumeAdjustments must be for same well";
    assert wellVolumeAdjustment.getCopy().equals(_copy) : "all wellVolumeAdjustments must be for same copy";
    _remainingMicroliterVolume = _remainingMicroliterVolume.add(wellVolumeAdjustment.getMicroliterVolume());
    _wellVolumeAdjustments.add(wellVolumeAdjustment);
    _consumedMicroliterVolume = null; // force re-compute
  }

  public Copy getCopy()
  {
    return _copy;
  }

  public BigDecimal getInitialMicroliterVolume()
  {
    return _initialMicroliterVolume;
  }

  public BigDecimal getConsumedMicroliterVolume()
  {
    if (_consumedMicroliterVolume == null) {
      _consumedMicroliterVolume = _initialMicroliterVolume.subtract(_remainingMicroliterVolume);
    }
    return _consumedMicroliterVolume;
  }

  public BigDecimal getRemainingMicroliterVolume()
  {
    return _remainingMicroliterVolume;
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
  public boolean equals(Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj instanceof WellCopy) {
      return _key.equals(((WellCopy) obj)._key);
    }
    return false;
  }

  @Override
  public int hashCode()
  {
    return _key.hashCode();
  }

  @Override
  public String toString()
  {
    return _key + "=" + _remainingMicroliterVolume;
  }

  public Pair<WellKey,String> getKey()
  {
    return _key;
  }

  public int compareTo(WellCopy that)
  {
    int result = this.getWell().compareTo(that.getWell());
    if (result == 0) {
      result = this.getCopy().getName().compareTo(that.getCopy().getName());
    }
    return result;
  }

  // private methods

}
