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
import edu.harvard.med.screensaver.model.screenresults.AssayWellType;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;

import org.apache.log4j.Logger;

public class ControlWellsFilter implements Filter<ResultValue>
{
  private static Logger log = Logger.getLogger(ControlWellsFilter.class);

  public boolean exclude(ResultValue rv)
  {
    return rv.getAssayWellType().equals(AssayWellType.ASSAY_NEGATIVE_CONTROL) ||
    rv.getAssayWellType().equals(AssayWellType.ASSAY_POSITIVE_CONTROL) ||
    rv.getAssayWellType().equals(AssayWellType.LIBRARY_CONTROL);
  }
}

