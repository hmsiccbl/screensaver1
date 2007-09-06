// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults.heatmaps;

import java.awt.Color;
import java.text.NumberFormat;

import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;

import org.apache.log4j.Logger;

/**
 * For use in HeatMapViewer's row data model. Controls text value, color, and
 * CSS style of rendered cell.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class HeatMapCell
{
  // static members

  public static final String INVISIBLE_HYPERLINK_VALUE = "&nbsp;&nbsp;&nbsp;&nbsp;";

  private static Logger log = Logger.getLogger(HeatMapCell.class);

  // instance data members
  
  private String _cellText;
  private ResultValue _resultValue;
  private String _popupText;
  private String _style;
  private WellKey _wellKey;

  
  // public constructors and methods

  public HeatMapCell(ResultValue resultValue,
                     WellKey wellKey,
                     double scoredValue,
                     Color color,
                     boolean showValues,
                     NumberFormat format)
  {
    _resultValue = resultValue;
    _wellKey = wellKey;
    _popupText = "<missing>";
    String formattedValue = null;
    if (resultValue != null) {
      if (resultValue.isExclude()) {
        _popupText = "<exclude>";
      }
      else if (resultValue.isDataProducerWell() && !resultValue.isNull()) {
        formattedValue = _popupText = format.format(scoredValue);
      }
      else if (resultValue.isEmptyWell()) {
        _popupText = "<empty>";
      }
    }
    
    _cellText = showValues && formattedValue != null ? formattedValue : INVISIBLE_HYPERLINK_VALUE;
    _popupText = _wellKey.getWellName() + ": " + _popupText;
    _style = getStyle(resultValue, color);
  }

  public HeatMapCell()
  {
    _cellText = "";
    _popupText = "";
  }

  public String getCellText()
  {
    return _cellText;
  }
  
  public String getPopupText()
  {
    return _popupText;
  }
  
  public String getStyle()
  {
    return _style;
  }
  
  public WellKey getWellKey()
  {
    return _wellKey;
  }
  
  
  // protected methods
  
  public static String getStyle(ResultValue resultValue, Color color)
  {
    String hexColor = String.format("#%02x%02x%02x",
                                    color.getRed(),
                                    color.getGreen(),
                                    color.getBlue());

    if (resultValue != null && resultValue.getValue() != null && !resultValue.isExclude()) {
      if (resultValue.isExperimentalWell()) {
        return "background-color: " + hexColor;
      }
      else if (resultValue.isControlWell()) {
        // note: if you change the border-width, also change heatMapCell.border-width in screensaver.css
        return "padding: 0px; border-width: 4px; border-style: double; border-color: black; background-color: " + hexColor;
      }
    }

    // non-data-producing well
    return "background-color: #eeeeee";
  }
}

