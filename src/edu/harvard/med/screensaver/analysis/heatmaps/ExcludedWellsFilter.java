// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.analysis.heatmaps;

import edu.harvard.med.screensaver.analysis.Filter;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.util.Pair;

import org.apache.log4j.Logger;

public class ExcludedWellsFilter implements Filter<Pair<WellKey,ResultValue>>
{
  private static Logger log = Logger.getLogger(ExcludedWellsFilter.class);

  public boolean exclude(Pair<WellKey,ResultValue> pair)
  {
    return pair.getSecond().isExclude();
  }
}

