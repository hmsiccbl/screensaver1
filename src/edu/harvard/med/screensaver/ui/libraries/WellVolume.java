// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
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

import edu.harvard.med.screensaver.model.libraries.CopyInfo;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellVolumeAdjustment;

import org.apache.log4j.Logger;

public class WellVolume
{
  // static members

  private static Logger log = Logger.getLogger(WellVolume.class);


  // instance data members
  
  private Well _well;
  private List<WellCopyVolume> _wellCopyVolumes;
  private BigDecimal _initialMicroliterVolume;
  private BigDecimal _consumedMicroliterVolume;
  private WellCopyVolume _maxWellCopyVolume;
  private WellCopyVolume _minWellCopyVolume;
  private List<WellVolumeAdjustment> _volumeAdjustments;
  private String _copies;


  // public constructors and methods
  
  public WellVolume(Well well,
                    Collection<WellCopyVolume> wellCopyVolumes)
  {
    _well = well;
    _initialMicroliterVolume = BigDecimal.ZERO.setScale(Well.VOLUME_SCALE);  
    if (wellCopyVolumes.size() > 0) {
      CopyInfo copyInfo = wellCopyVolumes.iterator().next().getCopy().getCopyInfo(_well.getPlateNumber());
      if (copyInfo != null) {
        _initialMicroliterVolume = copyInfo.getMicroliterWellVolume();
      }
    }

    _wellCopyVolumes = new ArrayList<WellCopyVolume>(wellCopyVolumes);
    Collections.sort(_wellCopyVolumes, 
                     new Comparator<WellCopyVolume>() {
      public int compare(WellCopyVolume wcv1, WellCopyVolume wcv2) 
      {
        return wcv1.getCopy().getName().compareTo(wcv2.getCopy().getName());
      }
    });

    _consumedMicroliterVolume = BigDecimal.ZERO.setScale(Well.VOLUME_SCALE);
    if (wellCopyVolumes.size() > 0) {
      _minWellCopyVolume = _maxWellCopyVolume = wellCopyVolumes.iterator().next();
    }
    _volumeAdjustments = new ArrayList<WellVolumeAdjustment>();
    for (WellCopyVolume wellCopyVolume : wellCopyVolumes) {
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
    _copies = makeCopyNames(_wellCopyVolumes);
  }
  
  public BigDecimal getInitialMicroliterVolume()
  {
    return _initialMicroliterVolume;
  }

  public BigDecimal getConsumedMicroliterVolume()
  {
    return _consumedMicroliterVolume;
  }

  public WellCopyVolume getMaxWellCopyVolume()
  {
    return _maxWellCopyVolume;
  }

  public WellCopyVolume getMinWellCopyVolume()
  {
    return _minWellCopyVolume;
  }

  public Well getWell()
  {
    return _well;
  }

  public List<WellCopyVolume> getWellCopyVolumes()
  {
    return _wellCopyVolumes;
  }
  
  public String getCopiesList()
  {
    return _copies;
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


  // private methods

  private String makeCopyNames(List<WellCopyVolume> wellCopyVolumes)
  {
    StringBuilder s = new StringBuilder();
    for (WellCopyVolume volume : wellCopyVolumes) {
      s.append(volume.getCopy().getName()).append(' ');
    }
    return s.toString();
  }

}
