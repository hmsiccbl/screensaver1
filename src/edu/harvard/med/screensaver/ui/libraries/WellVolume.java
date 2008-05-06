// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import edu.harvard.med.screensaver.model.Volume;
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
  private Volume _totalInitialVolume;
  private Volume _consumedVolume;
  private WellCopy _maxWellCopyVolume;
  private WellCopy _minWellCopyVolume;
  private List<WellVolumeAdjustment> _volumeAdjustments;
  private List<String> _copyNames;


  // public constructors and methods
  
  public WellVolume(Well well,
                    Collection<WellCopy> wellCopies)
  {
    _well = well;

    _totalInitialVolume = calcTotalInitialVolume(wellCopies);

    _wellCopyVolumes = new ArrayList<WellCopy>(wellCopies);
    Collections.sort(_wellCopyVolumes, 
                     new Comparator<WellCopy>() {
      public int compare(WellCopy wcv1, WellCopy wcv2) 
      {
        return wcv1.getCopy().getName().compareTo(wcv2.getCopy().getName());
      }
    });

    _consumedVolume = Volume.ZERO;
    if (wellCopies.size() > 0) {
      _minWellCopyVolume = _maxWellCopyVolume = wellCopies.iterator().next();
    }
    _volumeAdjustments = new ArrayList<WellVolumeAdjustment>();
    for (WellCopy wellCopyVolume : wellCopies) {
      assert wellCopyVolume.getWell().equals(_well) : "all wellCopyVolumes must be for same well";
      _consumedVolume = _consumedVolume.add(wellCopyVolume.getConsumedVolume());
      if (wellCopyVolume.getRemainingVolume().compareTo(_minWellCopyVolume.getRemainingVolume()) < 0) {
        _minWellCopyVolume = wellCopyVolume;
      }
      if (wellCopyVolume.getRemainingVolume().compareTo(_maxWellCopyVolume.getRemainingVolume()) > 0) {
        _maxWellCopyVolume = wellCopyVolume;
      }
      _volumeAdjustments.addAll(wellCopyVolume.getWellVolumeAdjustments());
    }
    
    _copyNames = makeCopyNames(_wellCopyVolumes);
  }

  public Volume getTotalInitialVolume()
  {
    return _totalInitialVolume;
  }

  public Volume getConsumedVolume()
  {
    return _consumedVolume;
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

  private Volume calcTotalInitialVolume(Collection<WellCopy> wellCopies)
  {
    Volume totalInitialVolume = new Volume(0);
    if (wellCopies.size() > 0) {
      for (WellCopy wellCopy : wellCopies) {
        CopyInfo copyInfoForWell = wellCopy.getCopy().getCopyInfo(_well.getPlateNumber());
        if (copyInfoForWell != null) {
          totalInitialVolume = totalInitialVolume.add(copyInfoForWell.getWellVolume());
        }
      }
    }
    return totalInitialVolume;
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
