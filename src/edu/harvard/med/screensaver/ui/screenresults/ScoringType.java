// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults;

import edu.harvard.med.screensaver.analysis.IdentityFunction;
import edu.harvard.med.screensaver.analysis.AggregateFunction;
import edu.harvard.med.screensaver.analysis.ZScoreFunction;

public enum ScoringType {
  NONE("Raw values", new IdentityFunction()),
  ZSCORE("Z-score", new ZScoreFunction())
  ;

  
  private String _description;
  private AggregateFunction<Double> _function;

  private ScoringType(String description,
                      AggregateFunction<Double> function)
  {
    _description = description;
    _function = function;
  }

  public String getDescription()
  {
    return _description;
  }

  public AggregateFunction<Double> getFunction()
  {
    return _function;
  }

  public String toString()
  {
    return _description;
  }
}

