// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.analysis.heatmaps;

import edu.harvard.med.screensaver.analysis.Filter;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.util.Pair;

import org.apache.log4j.Logger;

public class EdgeWellsFilter implements Filter<Pair<WellKey,ResultValue>>
{
  private static Logger log = Logger.getLogger(EdgeWellsFilter.class);

  public boolean exclude(Pair<WellKey,ResultValue> pair)
  {
    WellKey wellKey = pair.getFirst();
    return 
      wellKey.getRow() + Well.MIN_WELL_ROW == Well.MIN_WELL_ROW ||
      wellKey.getRow() + Well.MIN_WELL_ROW == Well.MAX_WELL_ROW ||
      wellKey.getColumn() + 1 == Well.MIN_WELL_COLUMN ||
      wellKey.getColumn() + 1 == Well.MAX_WELL_COLUMN;
  }
  
  public String toString() 
  {
    return "Edge wells";
  }
}

