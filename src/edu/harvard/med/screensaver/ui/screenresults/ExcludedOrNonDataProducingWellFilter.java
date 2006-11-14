// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults;

import edu.harvard.med.screensaver.analysis.Filter;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;

import org.apache.log4j.Logger;

public class ExcludedOrNonDataProducingWellFilter implements Filter<ResultValue>
{
  // static members

  private static Logger log = Logger.getLogger(ExcludedOrNonDataProducingWellFilter.class);

  public boolean exclude(ResultValue rv)
  {
    return rv.isExclude() || !rv.isDataProducerWell();
  }


  // instance data members

  // public constructors and methods

  // private methods

}

