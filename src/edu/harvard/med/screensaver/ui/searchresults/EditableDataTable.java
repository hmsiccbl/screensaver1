// $HeadURL:
// svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/ui/searchresults/SearchResults.java
// $
// $Id: SearchResults.java 2033 2007-11-13 00:04:20Z ant4 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.util.List;

import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.ui.table.DataTable;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;

import org.apache.log4j.Logger;


/**
 * DataTable subclass that adds methods for editing, saving, and canceling
 * pending edits.
 * 
 * @param E the type of each row's data object
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
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

  @UIControllerMethod
  /* final (CGLIB2 restriction) */public String edit()
  {
    setEditMode(true);
    doEdit();
    return REDISPLAY_PAGE_ACTION_RESULT;
  }


  // protected instance methods

  protected void doEdit()
  {}

  @UIControllerMethod
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
