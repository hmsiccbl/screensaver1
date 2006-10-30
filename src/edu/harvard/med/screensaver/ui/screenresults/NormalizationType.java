// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults;

import edu.harvard.med.screensaver.analysis.IdentityNormalizationFunction;
import edu.harvard.med.screensaver.analysis.NormalizationFunction;
import edu.harvard.med.screensaver.analysis.ZScoreNormalizationFunction;

public enum NormalizationType {
  NONE("None (raw values)", new IdentityNormalizationFunction()),
  ZSCORE("Z-score", new ZScoreNormalizationFunction())
  ;

  
  private String _description;
  private NormalizationFunction<Double> _function;

  private NormalizationType(String description,
                            NormalizationFunction<Double> function)
  {
    _description = description;
    _function = function;
  }

  public String getDescription()
  {
    return _description;
  }

  public NormalizationFunction<Double> getFunction()
  {
    return _function;
  }

  
}

