// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.faces.model.SelectItem;

import edu.harvard.med.screensaver.ui.table.TableColumn;

public abstract class BooleanColumn<T> extends TableColumn<T,Boolean>
{
  private ArrayList<SelectItem> _selectItems;

  public BooleanColumn(String name, String description)
  {
    super(name,
          description,
          ColumnType.BOOLEAN);
    _selectItems = new ArrayList<SelectItem>();
    _selectItems.add(new SelectItem("", ""));
    _selectItems.add(new SelectItem(true, "true"));
    _selectItems.add(new SelectItem(false, "false"));
  }

  public List<SelectItem> getBooleanSelections()
  {
    return _selectItems;
  }
}
