// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults.heatmaps;

import java.text.NumberFormat;

import edu.harvard.med.screensaver.analysis.Filter;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.DataColumn;
import edu.harvard.med.screensaver.ui.arch.util.UISelectManyBean;
import edu.harvard.med.screensaver.ui.arch.util.UISelectOneBean;
import edu.harvard.med.screensaver.util.Pair;

import org.apache.log4j.Logger;

public class HeatMapConfiguration
{
  // static members

  private static Logger log = Logger.getLogger(HeatMapConfiguration.class);


  // instance data members

  private UISelectOneBean<DataColumn> _dataColumns;
  private UISelectOneBean<ScoringType> _scoringType;
  private UISelectOneBean<NumberFormat> _numericFormat;
  private UISelectManyBean<Filter<Pair<WellKey,ResultValue>>> _excludedWellFilters;


  // public constructors and methods

  public UISelectOneBean<DataColumn> getDataColumns()
  {
    return _dataColumns;
  }

  public void setDataColumns(UISelectOneBean<DataColumn> dataColumns)
  {
    _dataColumns = dataColumns;
  }

  public UISelectManyBean<Filter<Pair<WellKey,ResultValue>>> getExcludedWellFilters()
  {
    return _excludedWellFilters;
  }

  public void setExcludedWellFilters(UISelectManyBean<Filter<Pair<WellKey,ResultValue>>> excludedWellFiltersController)
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

