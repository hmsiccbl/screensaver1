// $HeadURL: $
// $Id: $
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import edu.harvard.med.screensaver.model.Volume;

public class VolumeStatistics
{
  public static final VolumeStatistics Null = new VolumeStatistics();

  private Volume _minRemaining;
  private Volume _maxRemaining;
  private Volume _averageRemaining;

  public Volume getAverageRemaining()
  {
    return _averageRemaining;
  }

  public void setAverageRemaining(Volume averageRemaining)
  {
    _averageRemaining = averageRemaining;
  }

  public Volume getMinRemaining()
  {
    return _minRemaining;
  }

  public void setMinRemaining(Volume minRemaining)
  {
    _minRemaining = minRemaining;
  }

  public Volume getMaxRemaining()
  {
    return _maxRemaining;
  }

  public void setMaxRemaining(Volume maxRemaining)
  {
    _maxRemaining = maxRemaining;
  }
}
