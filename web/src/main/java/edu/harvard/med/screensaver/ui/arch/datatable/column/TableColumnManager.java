// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.arch.datatable.column;

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
import javax.faces.model.SelectItem;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.apache.myfaces.custom.tree2.TreeModel;
import org.apache.myfaces.custom.tree2.TreeModelBase;
import org.apache.myfaces.custom.tree2.TreeNode;
import org.apache.myfaces.custom.tree2.TreeNodeBase;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.db.Criterion;
import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.policy.CurrentScreensaverUser;
import edu.harvard.med.screensaver.ui.arch.datatable.ColumnVisibilityChangedEvent;
import edu.harvard.med.screensaver.ui.arch.datatable.SortChangedEvent;
import edu.harvard.med.screensaver.ui.arch.datatable.SortDirectionSelector;
import edu.harvard.med.screensaver.ui.arch.util.UISelectOneBean;
import edu.harvard.med.screensaver.ui.arch.view.aspects.UICommand;

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
  private List<TableColumn<R,?>> _sortableSearchableColumns = new ArrayList<TableColumn<R,?>>();
  private ListDataModel _columnModel; // contains visible columns only
  private TreeModel _columnsSelectionTree;
  private UISelectOneBean<SortDirection> _sortDirectionSelector;
  private UISelectOneBean<TableColumn<R,?>> _sortColumnSelector;
  private Map<TableColumn<R,?>,List<TableColumn<R,?>>> _compoundSortColumnsMap = new HashMap<TableColumn<R,?>,List<TableColumn<R,?>>>();
  private Map<String,TableColumn<R,?>> _name2Column = new HashMap<String,TableColumn<R,?>>();


  // For ReorderListWidget: List and arrays for the pick list
  private List<SelectItem> allItemsLeft;
  private List<SelectItem> allItemsRight;
  private List<SelectItem> defaultItemsRight; // the default columns displayed at the start
  private List<SelectItem> defaultItemsLeft; // the default columns not displayed at the start
  private String[] selectedItemsLeft = {};
  private String[] selectedItemsRight = {};
  private boolean _useReorderListWidget = false;
  // ReorderListWidget: End

  /**
   * @param columns
   * @param currentScreensaverUser
   * @param useReorderListWidget if true use the dual list based column selector with the ability to re-orde
   *        (do NOT use the tree based column selector)
   */
  public TableColumnManager(List<? extends TableColumn<R,?>> columns,
                            CurrentScreensaverUser currentScreensaverUser,
                            boolean useReorderListWidget)
  {

  	// For ReorderListWidget: Initialize the lists
    _useReorderListWidget = useReorderListWidget;
    if(isUseReorderListWidget())
    {
    	allItemsLeft = new ArrayList<SelectItem>();
    	allItemsRight = new ArrayList<SelectItem>();
    	defaultItemsRight = new ArrayList<SelectItem>();
    	defaultItemsLeft = new ArrayList<SelectItem>();
    	
    	for (int i=0; i<columns.size(); i++) {
    		if (columns.get(i).isVisible()) {
    			allItemsRight.add(new SelectItem(columns.get(i).getName(), columns.get(i).getName()));
    			defaultItemsRight.add(new SelectItem(columns.get(i).getName(), columns.get(i).getName()));
    		}
    		else {
    			allItemsLeft.add(new SelectItem(columns.get(i).getName(), columns.get(i).getName()));
    			defaultItemsLeft.add(new SelectItem(columns.get(i).getName(), columns.get(i).getName()));
    		}
    			
    	}
    }  	
    // For ReorderListWidget: End
  	
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
   * @param newSortColumn the new current sort column
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
   * @return the data columns column model
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

  public List<TableColumn<R,?>> getSortableSearchableColumns()
  {
    return _sortableSearchableColumns;
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
      _sortColumnSelector = new UISelectOneBean<TableColumn<R,?>>(getSortableSearchableColumns()) {
        @Override
        protected String makeLabel(TableColumn<R,?> t)
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

  @UICommand
  public String updateColumnSelections()
  {
    if(isUseReorderListWidget())
    {
      // For ReorderListWidget: To arrange the columns according to the order specified by user
      List<TableColumn<R,?>> columns = getAllColumns();
      List<TableColumn<R,?>> tempColumns = new ArrayList<TableColumn<R,?>>();
      
  		// Reset all columns to hidden
      for (int j=0; j<columns.size(); j++) {
      	columns.get(j).setVisible(false);
      }
      for (int i = 0; i < allItemsRight.size(); i++) {
  			
      	for (int j=0; j<columns.size(); j++) {
  				
  				// Add visible columns to the front of list. Will be used in updateVisibleColumns
  				// to obtain all visible columns
      		if (allItemsRight.get(i).getValue().equals(columns.get(j).getName())) {
  					columns.get(j).setVisible(true);
  					tempColumns.add(columns.get(j));
  					
  					break;
  				}
  			}
  		}
      
      // add remaining (hidden) columns to the back
      for (int j=0; j<columns.size(); j++) {
      	if (!columns.get(j).isVisible()) {
      		tempColumns.add(columns.get(j));
      	}
      }
      _columns = tempColumns;
      
      updateVisibleColumns(new ColumnVisibilityChangedEvent());
    }
    
    return ScreensaverConstants.REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UICommand
  public String selectAllColumns()
  {
    // TODO
    return ScreensaverConstants.REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UICommand
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
    // for reorder mode, update should be done even if columnsAdded 
    // and columnsRemoved are unchanged
    if (event.getColumnsAdded().size() > 0 
        || event.getColumnsRemoved().size() > 0 
        || isUseReorderListWidget() ) {
      if (log.isDebugEnabled()) {
        log.debug("column selections changed: " + event);
      }

      // rebuild _visibleColumns, maintaining the fixed order of the columns
      // we ignore the event's take on added & removed columns, since we can determine this reliably by inspecting each column
      _visibleColumns.clear();
      _sortableSearchableColumns.clear();
      for (TableColumn<R,?> column : getAllColumns()) {
        if (column.isVisible() && !isColumnRestricted(column)) {
          _visibleColumns.add(column);
          if (column.isSortableSearchable()) {
            _sortableSearchableColumns.add(column);
          }
        }
      }

      getSortColumnSelector().setDomain(_sortableSearchableColumns);
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
      // TODO: remove special cases here by adding appropriate flag param 
      if (parent.getDescription().contains("Annotations") || parent.getDescription().contains("Data Columns")) {
        groupNode = new SelectableColumnGroupTreeNode(groupName);
      }
      else {
        groupNode = new TreeNodeBase("group", groupName, false);
      }
      parent.getChildren().add(groupNode);
      groups.put(groupPath, groupNode);
    }
    return groupNode;
  }
  
  // For ReorderListWidget: Getters and setters
  
  public boolean isUseReorderListWidget() { return _useReorderListWidget; }
  public boolean isUseTreeWidget() { return !_useReorderListWidget; }
  
	public List<SelectItem> getAllItemsLeft() {
		return allItemsLeft;
	}

	public void setAllItemsLeft(List<SelectItem> allItemsLeft) {
		this.allItemsLeft = allItemsLeft;
	}

	public List<SelectItem> getAllItemsRight() {
		return allItemsRight;
	}

	public void setAllItemsRight(List<SelectItem> allItemsRight) {
		this.allItemsRight = allItemsRight;
	}

	public String[] getSelectedItemsLeft() {
		return selectedItemsLeft;
	}

	public void setSelectedItemsLeft(String[] selectedItemsLeft) {
		this.selectedItemsLeft = selectedItemsLeft;
	}

	public String[] getSelectedItemsRight() {
		return selectedItemsRight;
	}

	public void setSelectedItemsRight(String[] selectedItemsRight) {
		this.selectedItemsRight = selectedItemsRight;
	}
	
	public String leftToRight() {
		for (int i = 0; i < selectedItemsLeft.length; i++) {
			for (int j=0; j<allItemsLeft.size(); j++) {
				if (selectedItemsLeft[i].equals(allItemsLeft.get(j).getValue())) {
					allItemsRight.add(allItemsLeft.get(j));
					allItemsLeft.remove(j);
				}
			}
		}
		
		return null;
	}

	public String allLeftToRight() {
		allItemsRight.addAll(allItemsLeft);
		allItemsLeft.clear();
		return null;
	}

	public String rightToLeft() {
		for (int i = 0; i < selectedItemsRight.length; i++) {
			for (int j=0; j<allItemsRight.size(); j++) {
				if (selectedItemsRight[i].equals(allItemsRight.get(j).getValue())) {
					allItemsLeft.add(allItemsRight.get(j));
					allItemsRight.remove(j);
				}
			}
		}
		
		return null;
	}

	public String allRightToLeft() {
		allItemsLeft.addAll(allItemsRight);
		allItemsRight.clear();
		return null;
	}
  
  public String moveUp() {

    int j = 0;
    for (int i=0; i<allItemsRight.size(); i++) {
      if (selectedItemsRight.length <= j) break;
      
      if (selectedItemsRight[j].equals(allItemsRight.get(i).getValue())) {
        j++;
        if (i != 0)
          allItemsRight.add(i-1, allItemsRight.remove(i));
      }
    }
    
    return ScreensaverConstants.REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public String moveDown() {
    
    int j = selectedItemsRight.length-1;
    for (int i=allItemsRight.size()-1; i>=0; i--) {
      if (j < 0) break;
      
      if (selectedItemsRight[j].equals(allItemsRight.get(i).getValue())) {
        j--;
        if (i != allItemsRight.size()-1)
          allItemsRight.add(i, allItemsRight.remove(i+1));
      }
    }

    return ScreensaverConstants.REDISPLAY_PAGE_ACTION_RESULT;
  }
	
	// JSF UI method to restore columns to the default settings
  public String updateDefaultColumns() {
	  if (log.isDebugEnabled()) {
		  log.debug("BII: column selections changed back to default");
	  }
	  
	  // clear the current selection lists
	  allItemsRight.clear();
	  allItemsLeft.clear();
	  
	  // Set both selection list to default settings
	  for (int i = 0; i < defaultItemsRight.size(); i++) {
	  	allItemsRight.add(new SelectItem(defaultItemsRight.get(i).getValue(),defaultItemsRight.get(i).getLabel()));
		}
	  for (int i = 0; i < defaultItemsLeft.size(); i++) {
	  	allItemsLeft.add(new SelectItem(defaultItemsLeft.get(i).getValue(),defaultItemsLeft.get(i).getLabel()));
		}
	  
	  updateColumnSelections();
	  
	  return ScreensaverConstants.REDISPLAY_PAGE_ACTION_RESULT;
  }
	// For ReorderListWidget: End
  
}
