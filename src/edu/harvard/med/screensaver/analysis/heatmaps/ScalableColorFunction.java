// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.analysis.heatmaps;

import java.awt.Color;

import org.apache.log4j.Logger;

/**
 * Converts a color values in the range of [0.0, 1.0] to a new scale (linearly).
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class ScalableColorFunction implements ColorFunction
{
  // static members

  private static Logger log = Logger.getLogger(ScalableColorFunction.class);
  private ColorFunction _baseColorFunction;
  private double _lowerLimit;
  private double _upperLimit;


  // instance data members

  // public constructors and methods
  

  public ScalableColorFunction(ColorFunction baseColorFunction)
  {
    _baseColorFunction = baseColorFunction;
  }
  
  public double getLowerLimit()
  {
    return _lowerLimit;
  }

  public void setLowerLimit(double lowerLimitController)
  {
    _lowerLimit = lowerLimitController;
  }

  public double getUpperLimit()
  {
    return _upperLimit;
  }

  public void setUpperLimit(double upperLimitController)
  {
    _upperLimit = upperLimitController;
  }

  public Color getColor(double value)
  {
    double standardizedValue = (value - _lowerLimit) / (_upperLimit - _lowerLimit);
    // can be "out of range" if asking for a well that was excluded from normalization calculation
    standardizedValue = Math.max(0, Math.min(standardizedValue, 1.0));
    return _baseColorFunction.getColor(standardizedValue);
  }

  // private methods

}

