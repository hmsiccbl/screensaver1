// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.analysis;

import java.util.Collection;

import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math.util.ResizableDoubleArray;
import org.apache.log4j.Logger;

public class ZScoreFunction implements AggregateFunction<Double>
{
  // static members

  private static Logger log = Logger.getLogger(ZScoreFunction.class);
  private boolean _initialized = false;
  private double _stdDev = Double.NaN;
  private double _mean = Double.NaN;

  public Double compute(Double valueController)
  {
    if (!_initialized) {
      throw new IllegalStateException("uninitialized");
    }
    return (valueController - _mean) / _stdDev;
  }

  public void initializeAggregates(Collection<Double> valuesToNormalizeOverController)
  {
    ResizableDoubleArray tmpDoubleValues = new ResizableDoubleArray();
    for (Double value : valuesToNormalizeOverController) {
      tmpDoubleValues.addElement(value);
    }
    _stdDev = new StandardDeviation().evaluate(tmpDoubleValues.getElements());
    _mean = new Mean().evaluate(tmpDoubleValues.getElements());
    _initialized = true;
  }

}

