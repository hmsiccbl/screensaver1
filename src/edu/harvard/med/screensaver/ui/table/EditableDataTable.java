// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table;

import java.util.List;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.ui.UICommand;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;


/**
 * DataTable subclass that adds methods for editing, saving, and canceling
 * pending edits.
 * 
 * @param E the type of each row's data object
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
// TODO: replace implementation with AbstractEntitableBackingBean
public abstract class EditableDataTable<R> extends DataTable<R>
{

  // public static final data

  private static final Logger log = Logger.getLogger(EditableDataTable.class);


  // private instance data

  private boolean _editMode;
  private Boolean _hasEditableColumns;


  // public constructor


  // public methods


  // public action command methods & action listeners

  public void setCellValue(Object value)
  {
    if (log.isDebugEnabled()) {
      log.debug("setting value on " + getRowData() + " from column " +
                getColumnManager().getCurrentColumn().getName() + ": " + value);
    }
    getColumnManager().getCurrentColumn().setCellValue(getRowData(), value);
  }

  public boolean isEditMode()
  {
    return _editMode;
  }

  public boolean getHasEditableColumns()
  {
    if (_hasEditableColumns == null) {
      initializeHasEditableColumns(getColumnManager().getAllColumns());
    }
    return _hasEditableColumns;
  }

  @UICommand
  /* final (CGLIB2 restriction) */public String edit()
  {
    setEditMode(true);
    doEdit();
    return REDISPLAY_PAGE_ACTION_RESULT;
  }


  // protected instance methods

  protected void doEdit()
  {}

  @UICommand
  /* final (CGLIB2 restriction) */public String save()
  {
    setEditMode(false);
    doSave();
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  protected void doSave()
  {}

  /* final (CGLIB2 restriction) */public String cancel()
  {
    setEditMode(false);
    doCancel();
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  protected void doCancel()
  {}


  // private instance methods


  private void initializeHasEditableColumns(List<? extends TableColumn<R,?>> columns)
  {
    for (TableColumn<R,?> column : columns) {
      if (column.isEditable()) {
        _hasEditableColumns = true;
        break;
      }
    }
  }

  protected void setEditMode(boolean isEditMode)
  {
    _editMode = isEditMode;
  }
}
