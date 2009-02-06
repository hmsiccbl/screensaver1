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

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.VolumeUnit;
import edu.harvard.med.screensaver.model.libraries.CopyInfo;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellVolumeAdjustment;

public class WellVolume implements Comparable<WellVolume>
{
  // static members

  private static Logger log = Logger.getLogger(WellVolume.class);

  // instance data members
  
  private Well _well;
  private AggregateWellInfo _activeWellInfo;
  private AggregateWellInfo _retiredWellInfo;

  public WellVolume(Well well,
                    Collection<WellCopy> wellCopies)
  {
    _well = well;
    
    List<WellCopy> activeVolumes = new ArrayList<WellCopy>();
    List<WellCopy> retiredVolumes = new ArrayList<WellCopy>();
    for(WellCopy wc:wellCopies)
    {
      CopyInfo ci = wc.getCopy().getCopyInfo(_well.getPlateNumber());
      if(ci == null || !ci.isRetired())
      {
        activeVolumes.add(wc);
      }
      else if(ci != null && ci.isRetired())
      {
        retiredVolumes.add(wc);
      }
    }
    
    _activeWellInfo = new AggregateWellInfo(activeVolumes);
    _retiredWellInfo = new AggregateWellInfo(retiredVolumes);
  }
  
  public Well getWell()
  {
    return _well;
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

  public AggregateWellInfo getActiveWellInfo()
  {
    return _activeWellInfo;
  }

  public AggregateWellInfo getRetiredWellInfo()
  {
    return _retiredWellInfo;
  }
  
  public int getWellVolumeAdjustmentCount()
  {
    return getActiveWellInfo().getWellVolumeAdjustments().size()
        + getRetiredWellInfo().getWellVolumeAdjustments().size();
  }
  
  // inner class that separates the well volume information into two containers
  public class AggregateWellInfo
  {
    private List<WellCopy> _wellCopyVolumes;
    private Volume _totalInitialVolume;
    private Volume _consumedVolume;
    private WellCopy _maxWellCopyVolume;
    private WellCopy _minWellCopyVolume;
    private List<WellVolumeAdjustment> _volumeAdjustments;
    private List<String> _copyNames;
    
    private AggregateWellInfo (List<WellCopy> wellCopies)
    {
      _wellCopyVolumes = wellCopies; 
      
      Collections.sort(_wellCopyVolumes, 
                       new Comparator<WellCopy>() {
        public int compare(WellCopy wcv1, WellCopy wcv2) 
        {
          return wcv1.getCopy().getName().compareTo(wcv2.getCopy().getName());
        }
      });

      _totalInitialVolume = calcTotalInitialVolume(_wellCopyVolumes);

      _consumedVolume = VolumeUnit.ZERO;
      if (_wellCopyVolumes.size() > 0) {
        _minWellCopyVolume = _maxWellCopyVolume = _wellCopyVolumes.iterator().next();
      }
      _volumeAdjustments = new ArrayList<WellVolumeAdjustment>();
      for (WellCopy wellCopyVolume : _wellCopyVolumes) {
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
  };
  
}
