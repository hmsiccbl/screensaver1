// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.arch.datatable.column;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

public abstract class BooleanColumn<R> extends TableColumn<R,Boolean>
{
  private ArrayList<SelectItem> _selectItems;

  public BooleanColumn(String name, String description, String group)
  {
    super(name,
          description,
          ColumnType.BOOLEAN,
          group);
  }

  /**
   * For <t:selectOneMenu> <f:selectItems> JSF element.
   */
  public List<SelectItem> getBooleanSelections()
  {
    if (_selectItems == null) {
      _selectItems = new ArrayList<SelectItem>();
      _selectItems.add(new SelectItem("", ""));
      _selectItems.add(new SelectItem(true, "true"));
      _selectItems.add(new SelectItem(false, "false"));
    }
    return _selectItems;
  }

}
