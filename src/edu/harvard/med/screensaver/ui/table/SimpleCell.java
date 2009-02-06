// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table;

import edu.harvard.med.screensaver.ui.table.column.TableColumn;

/**
 * Simple abstraction for a cell in a JSF DataTable
 * @see TableColumn for more robust implementation
 */
public class SimpleCell
{
  private String _title;
  private String _value;
  private String _description;
  private Object _groupId;
  
  public SimpleCell(String title, String value, String description)
  {
    _title = title;
    _value = value;
    _description = description;
  }
  
  public String getTitle()
  {
    return _title;
  }
  
  public String getValue() { return _value; }
  
  public Object cellAction()
  {
    return null;
  }

  public boolean isCommandLink()
  {
    return false;
  }
  
  public String getDescription()
  {
    return _description;
  }
  
  public Object getGroupId()
  {
    return _groupId;
  }
  
  public SimpleCell setGroupId(Object groupId)
  {
    _groupId = groupId;
    return this;
  }
  
  public String toString() { return getValue(); }
}
