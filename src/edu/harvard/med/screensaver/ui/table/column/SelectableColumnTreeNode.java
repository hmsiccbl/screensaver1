// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table.column;

import org.apache.myfaces.custom.tree2.TreeNodeBase;

public class SelectableColumnTreeNode<R> extends TreeNodeBase
{
  private static final long serialVersionUID = 1L;
  
  private TableColumn<R,?> _column;
  
  public SelectableColumnTreeNode(TableColumn<R,?> column) 
  {
    super("column", column.getName(), true);
    _column = column;
  }
  
  public boolean isChecked()
  {
    return _column.isVisible();
  }
  
  public void setChecked(boolean checked) { 
    _column.setVisible(checked); 
  }

  public TableColumn<R,?> getColumn()
  {
    return _column;
  }
}