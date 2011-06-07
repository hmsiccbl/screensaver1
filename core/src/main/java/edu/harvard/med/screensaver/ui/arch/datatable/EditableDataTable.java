// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.arch.datatable;

import java.util.List;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TableColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.model.DataTableModel;
import edu.harvard.med.screensaver.ui.arch.util.UISelectOneBean;
import edu.harvard.med.screensaver.ui.arch.view.aspects.UICommand;


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
  private static final Logger log = Logger.getLogger(EditableDataTable.class);

  private ScreensaverUserRole _editingRole;
  private boolean _editMode;
  private Boolean _hasEditableColumns;

  public EditableDataTable()
  {
    super();
  }

  @Override
  public void initialize(DataTableModel<R> dataTableModel,
                         List<? extends TableColumn<R,?>> columns,
                         UISelectOneBean<Integer> rowsPerPageSelector,
                         boolean useReorderListWidget)
  {
    super.initialize(dataTableModel, columns, rowsPerPageSelector, useReorderListWidget);
    _editMode = false;
  }

  public <T> void setCellValue(Object value)
  {
    if (log.isDebugEnabled()) {
      log.debug("setting value on " + getRowData() + " from column " +
                getColumnManager().getCurrentColumn().getName() + ": " + value);
    }
    ((TableColumn<R,T>) getColumnManager().getCurrentColumn()).setCellValue(getRowData(), (T) value);
  }

  public boolean isEditMode()
  {
    return _editMode;
  }

  public ScreensaverUserRole getEditingRole()
  {
    return _editingRole;
  }

  public void setEditingRole(ScreensaverUserRole editingRole)
  {
    _editingRole = editingRole;
    _hasEditableColumns = null; // force re-calc
  }

  public boolean isEditable()
  {
    return getHasEditableColumns() && getScreensaverUser().isUserInRole(_editingRole);
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


  private void initializeHasEditableColumns(List<? extends TableColumn<R,?>> columns)
  {
    _hasEditableColumns = false;
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
