// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table.column.entity;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.PropertyPath;
import edu.harvard.med.screensaver.model.RelationshipPath;
import edu.harvard.med.screensaver.ui.table.column.ColumnType;

public abstract class BooleanEntityColumn<E extends AbstractEntity> extends EntityColumn<E,Boolean>
{
  private ArrayList<SelectItem> _selectItems;

  public BooleanEntityColumn(RelationshipPath<E> relationshipPath, String name, String description, String group)
  {
    super(relationshipPath,
          name,
          description,
          ColumnType.BOOLEAN, 
          group);
  }

  public BooleanEntityColumn(PropertyPath<E> propertyPath, String name, String description, String group)
  {
    super(propertyPath,
          name,
          description,
          ColumnType.BOOLEAN,
          group);
  }

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
