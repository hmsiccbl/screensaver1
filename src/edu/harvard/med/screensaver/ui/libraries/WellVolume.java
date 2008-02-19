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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import edu.harvard.med.screensaver.model.libraries.CopyInfo;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellVolumeAdjustment;

import org.apache.log4j.Logger;

public class WellVolume implements Comparable<WellVolume>
{
  // static members

  private static Logger log = Logger.getLogger(WellVolume.class);


  // instance data members
  
  private Well _well;
  private List<WellCopy> _wellCopyVolumes;
  private BigDecimal _totalInitialMicroliterVolume;
  private BigDecimal _consumedMicroliterVolume;
  private WellCopy _maxWellCopyVolume;
  private WellCopy _minWellCopyVolume;
  private List<WellVolumeAdjustment> _volumeAdjustments;
  private List<String> _copyNames;


  // public constructors and methods
  
  public WellVolume(Well well,
                    Collection<WellCopy> wellCopies)
  {
    _well = well;

    _totalInitialMicroliterVolume = calcTotalInitialMicroliterVolume(wellCopies);

    _wellCopyVolumes = new ArrayList<WellCopy>(wellCopies);
    Collections.sort(_wellCopyVolumes, 
                     new Comparator<WellCopy>() {
      public int compare(WellCopy wcv1, WellCopy wcv2) 
      {
        return wcv1.getCopy().getName().compareTo(wcv2.getCopy().getName());
      }
    });

    _consumedMicroliterVolume = BigDecimal.ZERO.setScale(Well.VOLUME_SCALE);
    if (wellCopies.size() > 0) {
      _minWellCopyVolume = _maxWellCopyVolume = wellCopies.iterator().next();
    }
    _volumeAdjustments = new ArrayList<WellVolumeAdjustment>();
    for (WellCopy wellCopyVolume : wellCopies) {
      assert wellCopyVolume.getWell().equals(_well) : "all wellCopyVolumes must be for same well";
      _consumedMicroliterVolume = _consumedMicroliterVolume.add(wellCopyVolume.getConsumedMicroliterVolume());
      if (wellCopyVolume.getRemainingMicroliterVolume().compareTo(_minWellCopyVolume.getRemainingMicroliterVolume()) < 0) {
        _minWellCopyVolume = wellCopyVolume;
      }
      if (wellCopyVolume.getRemainingMicroliterVolume().compareTo(_maxWellCopyVolume.getRemainingMicroliterVolume()) > 0) {
        _maxWellCopyVolume = wellCopyVolume;
      }
      _volumeAdjustments.addAll(wellCopyVolume.getWellVolumeAdjustments());
    }
    
    _copyNames = makeCopyNames(_wellCopyVolumes);
  }

  public BigDecimal getTotalInitialMicroliterVolume()
  {
    return _totalInitialMicroliterVolume;
  }

  public BigDecimal getConsumedMicroliterVolume()
  {
    return _consumedMicroliterVolume;
  }

  public WellCopy getMaxWellCopyVolume()
  {
    return _maxWellCopyVolume;
  }

  public WellCopy getMinWellCopyVolume()
  {
    return _minWellCopyVolume;
  }

  public Well getWell()
  {
    return _well;
  }

  public List<WellCopy> getWellCopyVolumes()
  {
    return _wellCopyVolumes;
  }
  
  public List<String> getCopiesList()
  {
    return _copyNames;
  }
  
  public List<WellVolumeAdjustment> getWellVolumeAdjustments()
  {
    return _volumeAdjustments;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj instanceof WellVolume) {
      return _well.equals(_well);
    }
    return false;
  }
  
  @Override
  public int hashCode()
  {
    return _well.hashCode();
  }
  
  @Override
  public String toString()
  {
    return _well.getWellKey().toString(); 
  }

  public int compareTo(WellVolume that)
  {
    return this.getWell().compareTo(that.getWell());
  }

  // private methods

  private BigDecimal calcTotalInitialMicroliterVolume(Collection<WellCopy> wellCopies)
  {
    BigDecimal totalInitialMicroliterVolume = BigDecimal.ZERO.setScale(Well.VOLUME_SCALE);  
    if (wellCopies.size() > 0) {
      for (WellCopy wellCopy : wellCopies) {
        CopyInfo copyInfoForWell = wellCopy.getCopy().getCopyInfo(_well.getPlateNumber());
        if (copyInfoForWell != null) {
          totalInitialMicroliterVolume = totalInitialMicroliterVolume.add(copyInfoForWell.getMicroliterWellVolume());
        }
      }
    }
    return totalInitialMicroliterVolume;
  }
  
  private List<String> makeCopyNames(List<WellCopy> wellCopyVolumes)
  {
    Set<String> names = new TreeSet<String>();
    for (WellCopy volume : wellCopyVolumes) {
      names.add(volume.getCopy().getName());
    }
    return new ArrayList<String>(names);
  }

}
