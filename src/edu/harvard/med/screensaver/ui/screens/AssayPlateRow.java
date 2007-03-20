// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screens;

import java.util.Map;

public class AssayPlateRow
{
  public Map<String,String> values;
  public Boolean selected = true;

  public AssayPlateRow(Map<String,String> values)
  {
    this.values = values;
  }
  public Boolean getSelected()
  {
    return selected;
  }

  public void setSelected(Boolean selected)
  {
    this.selected = selected;
  }

  public Map<String,String> getValues()
  {
    return values;
  }

  public void setValues(Map<String,String> values)
  {
    this.values = values;
  }

}

