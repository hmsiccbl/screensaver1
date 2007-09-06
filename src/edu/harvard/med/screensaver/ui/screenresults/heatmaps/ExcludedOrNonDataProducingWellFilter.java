// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults.heatmaps;

import edu.harvard.med.screensaver.analysis.Filter;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.util.Pair;

import org.apache.log4j.Logger;

public class ExcludedOrNonDataProducingWellFilter implements Filter<Pair<WellKey,ResultValue>>
{
  private static Logger log = Logger.getLogger(ExcludedOrNonDataProducingWellFilter.class);

  public boolean exclude(Pair<WellKey,ResultValue> pair)
  {
    ResultValue rv = pair.getSecond();
    return 
      rv.isExclude() || 
      !rv.isDataProducerWell() || 
      // accept case that data was simply missing in the screen result file
      rv.isNull();
  }
}

