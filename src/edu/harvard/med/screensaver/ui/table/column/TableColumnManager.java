// $HeadURL:
// svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/ui/table/TableSortManager.java
// $
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table.column;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import javax.faces.model.ListDataModel;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.CurrentScreensaverUser;
import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.ui.table.ColumnVisibilityChangedEvent;
import edu.harvard.med.screensaver.ui.table.Criterion;
import edu.harvard.med.screensaver.ui.table.SortChangedEvent;
import edu.harvard.med.screensaver.ui.table.SortDirectionSelector;
import edu.harvard.med.screensaver.ui.util.UISelectOneBean;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.apache.myfaces.custom.tree2.TreeModel;
import org.apache.myfaces.custom.tree2.TreeModelBase;
import org.apache.myfaces.custom.tree2.TreeNode;
import org.apache.myfaces.custom.tree2.TreeNodeBase;

/**
 * Notifies observers when set of available columns are changed, either from
 * setColumns() being called, or a column's setVisible() being called.
 *
 * @author drew
 */
public class TableColumnManager<R> extends Observable implements Observer
{
  public static final String GROUP_NODE_DELIMITER = "::";


  // static members

  private static Logger log = Logger.getLogger(TableColumnManager.class);


  // instance data

  private CurrentScreensaverUser _currentScreensaverUser;
  private List<TableColumn<R,?>> _columns = new ArrayList<TableColumn<R,?>>();
  private List<TableColumn<R,?>> _visibleColumns = new ArrayList<TableColumn<R,?>>();
  private ListDataModel _columnModel; // contains visible columns only
  private TreeModel _columnsSelectionTree;
  private UISelectOneBean<SortDirection> _sortDirectionSelector;
  private UISelectOneBean<TableColumn<R,?>> _sortColumnSelector;
  private Map<TableColumn<R,?>,List<TableColumn<R,?>>> _compoundSortColumnsMap = new HashMap<TableColumn<R,?>,List<TableColumn<R,?>>>();
  private Map<String,TableColumn<R,?>> _name2Column = new HashMap<String,TableColumn<R,?>>();



  // public constructors and methods

  public TableColumnManager(List<? extends TableColumn<R,?>> columns,
                            CurrentScreensaverUser currentScreensaverUser)
  {
    _currentScreensaverUser = currentScreensaverUser;
    setColumns(columns);
  }

  /**
   * Get the current sort column.
   *
   * @motivation allow sort column to be set from a drop-down list UI component
   *             (in addition to clicking on table column headers)
   * @return the current sort column
   */
  public TableColumn<R,?> getSortColumn()
  {
    return getSortColumnSelector().getSelection();
  }

  public List<TableColumn<R,?>> getSortColumns()
  {
    if (getSortColumnSelector().getSelection() == null) {
      return Collections.emptyList();
    }
    List<TableColumn<R,?>> sortColumns = _compoundSortColumnsMap.get(getSortColumn());
    if (sortColumns == null) {
      sortColumns = new ArrayList<TableColumn<R,?>>();
      sortColumns.add(getSortColumnSelector().getSelection());
    }
    return sortColumns;
  }

  public void addCompoundSortColumns(List<TableColumn<R,?>> compoundSortColumns)
  {
    _compoundSortColumnsMap.put(compoundSortColumns.get(0), compoundSortColumns);
  }

  public void addCompoundSortColumns(TableColumn<R,?>... compoundSortColumns)
  {
    addCompoundSortColumns(Arrays.asList(compoundSortColumns));
  }

  public void addAllCompoundSorts(List<List<TableColumn<R,?>>> allCompoundSorts)
  {
    for (List<TableColumn<R,?>> compoundSort : allCompoundSorts) {
      addCompoundSortColumns(compoundSort);
    }
  }

  @SuppressWarnings("unchecked")
  public TreeModel getColumnsTreeModel()
  {
    if (_columnsSelectionTree == null) {
      TreeNodeBase root = new TreeNodeBase("root", "Columns", false);
      Map<String,TreeNode> groups = new HashMap<String,TreeNode>();
      for (TableColumn<R,?> column : _columns) {
        if (!isColumnRestricted(column)) {
          TreeNode groupNode = getOrCreateGroupNode(root, groups, column.getGroup());
          groupNode.getChildren().add(new SelectableColumnTreeNode<R>(column));
        }
      }
      _columnsSelectionTree = new TreeModelBase(root);
    }
    return _columnsSelectionTree;
  }

  public boolean isColumnsTreeOpen()
  {
    return getColumnsTreeModel().getTreeState().isNodeExpanded("0");
  }

  public int getSortColumnIndex()
  {
    return getSortColumnSelector().getSelectionIndex();
  }

  /**
   * Get the index of the column currently being rendered by JSF.
   */
  public int getCurrentColumnIndex()
  {
    return getVisibleColumnModel().getRowIndex();
  }

  /**
   * Get the column currently being rendered by JSF.
   */
  @SuppressWarnings("unchecked")
  public TableColumn<R,?> getCurrentColumn()
  {
    return (TableColumn<R,?>) getVisibleColumnModel().getRowData();
  }

  public TableColumn<R,?> getColumn(String columnName)
  {
    return _name2Column.get(columnName);
  }

  /**
   * @return the n'th visible column (zero-based)
   */
  public TableColumn<R,?> getColumn(int i)
  {
    return getVisibleColumns().get(i);
  }

  /**
   * Set the current sort column.
   *
   * @motivation allow sort column to be set from a drop-down list UI component
   *             (in addition to clicking on table column headers)
   * @param currentSortColumn the new current sort column
   */
  public void setSortColumn(TableColumn<R,?> newSortColumn)
  {
    if (newSortColumn != null) {
      if (!newSortColumn.equals(getSortColumns().get(0))) {
        getSortColumnSelector().setSelection(newSortColumn);
      }
    }
  }

  /**
   * @motivation for use by dataTable JSF component.
   * @param sortColumnName the name of the new sort column
   */
  public void setSortColumnName(String sortColumnName)
  {
    if (sortColumnName != null) {
      setSortColumn(getColumn(sortColumnName));
    }
    else {
      setSortColumn(null);
    }
  }

  /**
   * @motivation for use by dataTable JSF component.
   * @return true the name of the current sort column
   */
  public String getSortColumnName()
  {
    if (getSortColumn() == null) {
      return null;
    }
    return getSortColumn().getName();
  }

  /**
   * @motivation for use by dataTable JSF component.
   * @param sortAscending true if new sort direction is ascending; false if
   *          descending
   */
  public void setSortAscending(boolean sortAscending)
  {
    if (sortAscending) {
      setSortDirection(SortDirection.ASCENDING);
    }
    else {
      setSortDirection(SortDirection.DESCENDING);
    }
  }

  /**
   * @motivation for use by dataTable JSF component.
   * @return true if current sort direction is ascending; false if descending
   */
  public boolean isSortAscending()
  {
    return getSortDirection().equals(SortDirection.ASCENDING);
  }

  /**
   * Get the current sort direction.
   *
   * @motivation allow sort direction to be set from a drop-down list UI
   *             component (in addition to clicking on table column headers)
   * @return the current sort column name
   */
  public SortDirection getSortDirection()
  {
    return getSortDirectionSelector().getSelection();
  }

  /**
   * Set the current sort direction.
   *
   * @motivation allow sort direction to be set from a drop-down list UI
   *             component (in addition to clicking on table column headers)
   * @param currentSortDirection the new current sort direction
   */
  public void setSortDirection(SortDirection currentSortDirection)
  {
    if (!getSortDirection().equals(currentSortDirection)) {
      getSortDirectionSelector().setSelection(currentSortDirection);
    }
  }

  /**
   * Get the JSF column model, containing visible columns.
   *
   * @return the data header column model
   */
  public ListDataModel getVisibleColumnModel()
  {
    return _columnModel;
  }

  public void setColumns(List<? extends TableColumn<R,?>> columns)
  {
    Set<TableColumn<R,?>> oldColumns = new HashSet<TableColumn<R,?>>(_columns);

    _columns.clear();
    _columns.addAll(columns);
    _columnsSelectionTree = null; // force re-create

    _name2Column.clear();
    for (TableColumn<R,?> column : columns) {
      column.addObserver(this);
      if (_name2Column.containsKey(column.getName())) {
        throw new IllegalArgumentException("column " + column + " has non-unique name");
      }
      _name2Column.put(column.getName(), column);
    }

    updateVisibleColumns(new ColumnVisibilityChangedEvent(CollectionUtils.subtract(columns, oldColumns),
                                                          CollectionUtils.subtract(oldColumns, columns)));
  }

  @SuppressWarnings("unchecked")
  public List<TableColumn<R,?>> getAllColumns()
  {
    return _columns;
  }

  public List<TableColumn<R,?>> getVisibleColumns()
  {
    return _visibleColumns;
  }

  public void setVisibilityOfColumnsInGroup(String columnGroupName, boolean isVisible)
  {
    for (TableColumn<R,?> column : getAllColumns()) {
      if (column.getGroup().equals(columnGroupName)) {
        column.setVisible(isVisible);
      }
    }
  }

  public UISelectOneBean<TableColumn<R,?>> getSortColumnSelector()
  {
    if (_sortColumnSelector == null) {
      _sortColumnSelector = new UISelectOneBean<TableColumn<R,?>>(getVisibleColumns()) {
        @Override
        protected String getLabel(TableColumn<R,?> t)
        {
          return t.getName();
        }
      };
      _sortColumnSelector.addObserver(this);
    }
    return _sortColumnSelector;
  }

  public UISelectOneBean<SortDirection> getSortDirectionSelector()
  {
    if (_sortDirectionSelector == null) {
      _sortDirectionSelector = new SortDirectionSelector();
      _sortDirectionSelector.addObserver(this);
    }
    return _sortDirectionSelector;
  }


  // JSF application methods

  @UIControllerMethod
  public String updateColumnSelections()
  {
    getColumnsTreeModel().getTreeState().collapsePath(new String[] { "0" });
    return ScreensaverConstants.REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String selectAllColumns()
  {
    // TODO
    return ScreensaverConstants.REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String unselectAllColumns()
  {
    // TODO
    return ScreensaverConstants.REDISPLAY_PAGE_ACTION_RESULT;
  }


  // Observer methods

  public void update(Observable o, Object arg)
  {
    if (o == _sortColumnSelector) {
      setChanged();
      notifyObservers(new SortChangedEvent<R>(getSortColumn()));
    }
    else if (o == _sortDirectionSelector) {
      setChanged();
      notifyObservers(new SortChangedEvent<R>(getSortDirection()));
    }
    else if (o instanceof TableColumn) {
      if (arg instanceof ColumnVisibilityChangedEvent) {
        log.debug("TableColumnManager notified of column visibility change: " + o);
        updateVisibleColumns((ColumnVisibilityChangedEvent) arg);
      }
      else if (arg instanceof Criterion) {
        // column's filtering criteria changed
        setChanged();
        notifyObservers(arg);
      }
    }
  }


  // private methods

  private void updateVisibleColumns(ColumnVisibilityChangedEvent event)
  {
    if (event.getColumnsAdded().size() > 0 || event.getColumnsRemoved().size() > 0) {
      if (log.isDebugEnabled()) {
        log.debug("column selections changed: " + event);
      }

      // rebuild _visibleColumns, maintaining the fixed order of the columns
      // we ignore the event's take on added & removed columns, since we can determine this reliably by inspecting each column
      _visibleColumns.clear();
      for (TableColumn<R,?> column : getAllColumns()) {
        if (column.isVisible() && !isColumnRestricted(column)) {
          _visibleColumns.add(column);
        }
      }

      getSortColumnSelector().setDomain(_visibleColumns);
      _columnModel = new ListDataModel(_visibleColumns);
      setChanged();
      notifyObservers(event);
    }
  }

  private boolean isColumnRestricted(TableColumn<?,?> column)
  {
    if (!column.isAdministrative()) { return false; }
    if (_currentScreensaverUser != null && _currentScreensaverUser.getScreensaverUser().isUserInRole(ScreensaverUserRole.READ_EVERYTHING_ADMIN)) { return false; }
    return true;
  }

  private TreeNode getOrCreateGroupNode(TreeNodeBase root,
                                        Map<String,TreeNode> groups,
                                        String groupPath)
  {
    TreeNode groupNode = groups.get(groupPath);
    if (groupNode == null) {
      if (groupPath.equals(TableColumn.UNGROUPED)) {
        return root;
      }
      TreeNode parent;
      String groupName;
      int lastPathDelimPos = groupPath.lastIndexOf(GROUP_NODE_DELIMITER);
      if (lastPathDelimPos < 0) {
        // leaf group node
        parent = root;
        groupName = groupPath;
      }
      else {
        // internal group node
        parent = getOrCreateGroupNode(root,
                                      groups,
                                      groupPath.substring(0, lastPathDelimPos));
        groupName = groupPath.substring(lastPathDelimPos + GROUP_NODE_DELIMITER.length());
      }
      groupNode = new TreeNodeBase("group", groupName, false);
      parent.getChildren().add(groupNode);
      groups.put(groupPath, groupNode);
    }
    return groupNode;
  }
}
