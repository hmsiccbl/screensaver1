// $HeadURL:
// svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml
// $
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.cherrypickrequests;

public class SelectableRow<E>
{
  private boolean _selected = false;
  private E _data;

  public SelectableRow(E rowData)
  {
    _data = rowData;
  }

  public SelectableRow(E rowData, boolean isSelected)
  {
    _data = rowData;
    _selected = isSelected;
  }

  public boolean isSelected()
  {
    return _selected;
  }

  public void setSelected(boolean selected)
  {
    _selected = selected;
  }

  public E getData() 
  {
    return _data;
  }
}