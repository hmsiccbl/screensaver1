// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults;

import java.awt.Color;
import java.text.NumberFormat;

import org.apache.log4j.Logger;

/**
 * For use in HeatMapViewer's row data model.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class HeatMapCell
{
  // static members

  private static Logger log = Logger.getLogger(HeatMapCell.class);

  // instance data members
  
  private String _value;
  private String _hexColor;

  
  // public constructors and methods

  public HeatMapCell(double value, Color color, NumberFormat format)
  {
    if (format != null) {
      _value = format.format(value);
    }
    else {
      _value = "";
    }
    
    _hexColor = String.format("#%02x%02x%02x", 
                              color.getRed(),
                              color.getGreen(),
                              color.getBlue());
  }

  public HeatMapCell()
  {
    _value = "";
    _hexColor = "#000000";
  }

  public String getHexColor()
  {
    return _hexColor;
  }

  public String getValue()
  {
    return _value;
  }
}

