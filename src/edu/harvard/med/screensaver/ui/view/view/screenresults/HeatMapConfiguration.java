// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.view.screenresults;

import java.text.NumberFormat;

import edu.harvard.med.screensaver.analysis.Filter;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.ui.util.UISelectManyBean;
import edu.harvard.med.screensaver.ui.util.UISelectOneBean;

import org.apache.log4j.Logger;

public class HeatMapConfiguration
{
  // static members

  private static Logger log = Logger.getLogger(HeatMapConfiguration.class);


  // instance data members

  private UISelectOneBean<ResultValueType> _dataHeaders;
  private UISelectOneBean<ScoringType> _scoringType;
  private UISelectOneBean<NumberFormat> _numericFormat;
  private UISelectManyBean<Filter<ResultValue>> _excludedWellFilters;


  // public constructors and methods

  public UISelectOneBean<ResultValueType> getDataHeaders()
  {
    return _dataHeaders;
  }

  public void setDataHeaders(UISelectOneBean<ResultValueType> dataHeadersController)
  {
    _dataHeaders = dataHeadersController;
  }

  public UISelectManyBean<Filter<ResultValue>> getExcludedWellFilters()
  {
    return _excludedWellFilters;
  }

  public void setExcludedWellFilters(UISelectManyBean<Filter<ResultValue>> excludedWellFiltersController)
  {
    _excludedWellFilters = excludedWellFiltersController;
  }

  public UISelectOneBean<NumberFormat> getNumericFormat()
  {
    return _numericFormat;
  }

  public void setNumericFormat(UISelectOneBean<NumberFormat> numericFormatController)
  {
    _numericFormat = numericFormatController;
  }

  public UISelectOneBean<ScoringType> getScoringType()
  {
    return _scoringType;
  }

  public void setScoringType(UISelectOneBean<ScoringType> scoringTypeController)
  {
    _scoringType = scoringTypeController;
  }

  
  // private methods

}

