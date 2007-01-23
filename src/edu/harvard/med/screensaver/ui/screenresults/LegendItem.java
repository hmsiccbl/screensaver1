// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults;


public class LegendItem
{
  // instance data members
  
  private String _name;
  private String _cellStyle;
  
  public LegendItem(String name, String cellStyle)
  {
    _name = name;
    _cellStyle = cellStyle;
  }

  public String getName()
  {
    return _name;
  }

  public String getCellStyle()
  {
    return _cellStyle;
  }
  
  public String getCellText()
  {
    return HeatMapCell.INVISIBLE_HYPERLINK_VALUE;
  }
}

