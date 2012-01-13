// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.analysis.heatmaps;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Provides a color function that takes values from 0.0 to 1.0, and returns a
 * color from "stack" of color gradients (of an arbitrary size), where each
 * gradient consumes an equal proportion of the 0.0-1.0 range.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class MultiGradientColorFunction implements ColorFunction
{
  // static members

  private static Logger log = Logger.getLogger(MultiGradientColorFunction.class);

  
  // instance data members
  
  private List<Color> _colors;
  private int _n;

  
  // public constructors and methods


  public MultiGradientColorFunction(Color color1, Color color2, Color... moreColors)
  {
    _colors = new ArrayList<Color>(2 + moreColors.length);
    _colors.add(color1);
    _colors.add(color2);
    for (Color color : moreColors) {
      _colors.add(color);
    }
    _n = _colors.size() - 1;
  }

  public Color getColor(double value)
  {
    if (value < 0.0 || value > 1.0) {
      throw new IllegalArgumentException("value must be in [0.0, 1.0]:" + value);
    }
    int gradientIndex = (int) (value * (double) _n);
    if (gradientIndex + 1 == _colors.size()) {
      // special case for max value (no interpolation needed)
      return _colors.get(gradientIndex);
    }
    Color color1 = _colors.get(gradientIndex);
    Color color2 = _colors.get(gradientIndex + 1);
    double intraColorPct = (value - (gradientIndex / (double) _n)) * (double) _n;
    return interpolateColor(intraColorPct, color1, color2);
  }


  // private methods

  private Color interpolateColor(double pct, Color color1, Color color2)
  {
    return new Color((int) (pct * (color2.getRed() - color1.getRed()) + color1.getRed()),
                     (int) (pct * (color2.getGreen() - color1.getGreen()) + color1.getGreen()),
                     (int) (pct * (color2.getBlue() - color1.getBlue()) + color1.getBlue()));
  }

}

