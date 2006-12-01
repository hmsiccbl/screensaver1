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

  private static final String INVISIBLE_HYPERLINK_VALUE = "&nbsp;&nbsp;&nbsp;&nbsp;";

  private static Logger log = Logger.getLogger(HeatMapCell.class);

  // instance data members
  
  private String _cellText;
  private ResultValue _resultValue;
  private boolean _containsValue;
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
    _containsValue = resultValue != null && !resultValue.isEmptyWell();
    String formattedValue = _containsValue ? format.format(scoredValue) : "<empty>";
    _cellText = showValues ? formattedValue : INVISIBLE_HYPERLINK_VALUE;
    _popupText = _resultValue == null ? "" :
      _wellKey.getWellName() + ": " + formattedValue;
    
    updateStyle(resultValue, color);
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
  
  protected void updateStyle(ResultValue resultValue, Color color)
  {
    String hexColor = String.format("#%02x%02x%02x",
      color.getRed(),
      color.getGreen(),
      color.getBlue());

    if (resultValue != null &&
      !resultValue.isExclude()) {
      if (resultValue.isExperimentalWell()) {
        _style = "background-color: " + hexColor;
        return;
      }
      else if (_resultValue.isControlWell()) {
        _style = "background-color: " + hexColor + "; border-style: double";
        return;
      }
    }

    // non-data-producing well
    _style = "background-color: transparent";
  }

}

