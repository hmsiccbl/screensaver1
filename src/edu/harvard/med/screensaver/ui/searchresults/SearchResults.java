// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.ui.table.DataTable;
import edu.harvard.med.screensaver.ui.table.DataTableRowsPerPageUISelectOneBean;
import edu.harvard.med.screensaver.ui.table.TableColumn;
import edu.harvard.med.screensaver.ui.table.TableSortManager;

import org.apache.log4j.Logger;


/**
 * Backing bean for search result pages. Provides:
 * <ul>
 * <li>Sorting and paging of a search result.</li>
 * <li>Compound sort orders over multiple columns (primary, secondary,
 * tertiary, etc. sort orders).</li>
 * <li>Editable data fields.</li>
 * </ul>
 *
 * @param E the type of each row's data object
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
abstract public class SearchResults<E> extends AbstractBackingBean
{

  // public static final data

  private static final Logger log = Logger.getLogger(SearchResults.class);

  public static List<Integer> DEFAULT_ROWS_PER_PAGE_SELECTIONS = Arrays.asList(10, 20, 50, 100,
                                                                               DataTableRowsPerPageUISelectOneBean.SHOW_ALL_VALUE);

  /**
   * Workaround for JSF suckiness. Two things: first, I need to use the returning a Map trick to
   * get around the problem that JSF EL doesn't allow parameterized methods. Second, I gotta
   * escape the backslashes in the f:param, since the JSF EL is evaluating that backslash as an
   * escape character somewhere.
   */
  private final Map<String,String> _backslashEscaper = new HashMap<String,String>() {
    private static final long serialVersionUID = 1L;
    public String get(Object key)
    {
      if (key instanceof String) {
        return ((String) key).replace("\\", "\\\\");
      }
      else {
        return key.toString();
      }
    }
  };


  // private instance data


  private Map<String,Boolean> _capabilities = new HashMap<String,Boolean>();
  private String _description;
  private Collection<? extends E> _unsortedResults;
  private DataTable<E> _dataTable;
  private List<E> _sortedData;
  private boolean _editMode;
  private boolean _hasEditableColumns;


  // public constructor

  /**
   * @motivation for CGLIB2
   */
  protected SearchResults()
  {
  }

  protected SearchResults(String[] capabilities)
  {
    if (capabilities != null) {
      for (String capability : capabilities) {
        _capabilities.put(capability, true);
      }
    }
    // HACK: create a dummy data table as soon as possible to allow JSF component bindings to work
    _dataTable = new DataTable<E>() {
      @Override
      protected List<TableColumn<E>> buildColumns()
      {
        return Collections.emptyList();
      }

      @Override
      protected DataModel buildDataModel()
      {
        return new ListDataModel();
      }

      @Override
      protected DataTableRowsPerPageUISelectOneBean buildRowsPerPageSelector()
      {
        return new DataTableRowsPerPageUISelectOneBean(Collections.<Integer>emptyList());
      }
    };
  }

  // abstract methods

  /**
   * Create and return a list of the column header values.
   *
   * @return a list of the column headers
   */
  abstract protected List<TableColumn<E>> getColumns();


  // public methods

  /**
   * @motivation to allow JSF pages to know what subclass methods are available;
   *             this is a hack that is the JSF-equivalent of using the Java
   *             'instanceof' operator before downcasting an object to get at
   *             its subclass methods
   */
  public Map<String,Boolean> getCapabilities()
  {
    return _capabilities;
  }

  public void setContents(Collection<? extends E> unsortedResults)
  {
    setContents(unsortedResults, null);
  }

  /**
   * Set the contents of the search results.
   *
   * @param unsortedResults the unsorted list of the results, as they are
   *          returned from the database
   */
  public void setContents(Collection<? extends E> unsortedResults, String description)
  {
    _unsortedResults = unsortedResults;
    _description = description;
    DataTable oldDataTable = _dataTable;
    _dataTable = new DataTable<E>()
    {
      @Override
      protected List<TableColumn<E>> buildColumns()
      {
        return getColumns();
      }

      @Override
      protected DataModel buildDataModel()
      {
        return SearchResults.this.buildDataModel();
      }

      @Override
      protected DataTableRowsPerPageUISelectOneBean buildRowsPerPageSelector()
      {
        return SearchResults.this.buildRowsPerPageSelector();
      }
    };
    // HACK: we use DataTable to maintain JSF component bindings, so if we
    // recreate the DataTable, we have to preserve these bindings, since JSF
    // will not give them to us again during the processing of this requests
    if (oldDataTable != null) {
      _dataTable.setDataTableUIComponent(oldDataTable.getDataTableUIComponent());
      _dataTable.setRowsPerPageUIComponent(oldDataTable.getRowsPerPageUIComponent());
    }

    initializeCompoundSorts();
    initializeHasEditableColumns(getSortManager().getColumns());
  }

  /**
   * If the application knowingly updates one or more items in the search
   * results, they can call this method to cause the search result to be
   * regenerated the next time this viewer is visited.
   */
  public void invalidateSearchResult()
  {
    // TODO: implement
  }

  public DataTable<E> getDataTable()
  {
    return _dataTable;
  }


  // public getters and setters - used by searchResults.jspf

  public TableSortManager<E> getSortManager()
  {
    return _dataTable.getSortManager();
  }

  protected List<Integer[]> getCompoundSorts()
  {
    return new ArrayList<Integer[]>();
  }

  public Collection<? extends E> getContents()
  {
    return _unsortedResults;
  }

  public String getDescription()
  {
    return _description;
  }


  // public action command methods & action listeners

  /**
   * Get the value to be displayed for the current cell.
   * @return the value to be displayed for the current cell
   */
  public Object getCellValue()
  {
    return getCurrentColumn().getCellValue(getRowData());
  }

  public void setCellValue(Object value)
  {
    if (log.isDebugEnabled()) {
      log.debug("setting value on " + getRowData() + " from column " + getCurrentColumn().getName() + ": " + value);
    }
    getCurrentColumn().setCellValue(getRowData(), value);
  }

  public boolean isEditMode()
  {
    return _editMode;
  }

  public boolean getHasEditableColumns()
  {
    return _hasEditableColumns;
  }

  @UIControllerMethod
  /*final (CGLIB2 restriction)*/ public String edit()
  {
    setEditMode(true);
    doEdit();
    return REDISPLAY_PAGE_ACTION_RESULT;
  }


  // protected instance methods

  protected void doEdit() {}

  @UIControllerMethod
  /*final (CGLIB2 restriction)*/ public String save()
  {
    setEditMode(false);
    doSave();
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  protected void doSave() {}

  /*final (CGLIB2 restriction)*/ public String cancel()
  {
    setEditMode(false);
    doCancel();
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  protected void doCancel() {}

  public Map<String,String> getEscapeBackslashes()
  {
    return _backslashEscaper;
  }

  final protected TableColumn<E> getCurrentColumn()
  {
    return getSortManager().getCurrentColumn();
  }

  /**
   * Get the entity in the current cell.
   * @return the entity in the current cell
   */
  @SuppressWarnings("unchecked")
  final protected E getRowData()
  {
    return (E) _dataTable.getDataModel().getRowData();
  }

  final protected List<E> getCurrentSort()
  {
    buildDataModel();
    return _sortedData;
  }

  protected DataModel buildDataModel()
  {
    _sortedData = new ArrayList<E>(_unsortedResults);
    Collections.sort(_sortedData, getSortManager().getSortColumnComparator());
    DataModel dataModel = new ListDataModel(_sortedData);
    return dataModel;
  }

  /**
   * Subclass should override if it needs to specify a custom
   * DataTableRowsPerPageUISelectOneBean
   *
   * @return a DataTableRowsPerPageUISelectOneBean or null if the default
   *         DataTableRowsPerPageUISelectOneBean, as built by DataTable, is
   *         acceptable.
   */
  protected DataTableRowsPerPageUISelectOneBean buildRowsPerPageSelector()
  {
    DataTableRowsPerPageUISelectOneBean rowsPerPageSelector =
      new DataTableRowsPerPageUISelectOneBean(DEFAULT_ROWS_PER_PAGE_SELECTIONS,
                                              DEFAULT_ROWS_PER_PAGE_SELECTIONS.get(1)) {
      @Override
      protected Integer getAllRowsValue()
      {
        return getDataTable().getRowCount();
      }
    };
    return rowsPerPageSelector;
  }


  // private instance methods


  private void initializeCompoundSorts()
  {
    for (Integer[] compoundSortIndexes : getCompoundSorts()) {
      getSortManager().addCompoundSortColumns(compoundSortIndexes);
    }
  }

  private void initializeHasEditableColumns(List<TableColumn<E>> columns)
  {
    for (TableColumn<E> column : columns) {
      if (column.isEditable()) {
        _hasEditableColumns = true;
        break;
      }
    }
  }

  protected void setEditMode(boolean isEditMode)
  {
    _editMode = isEditMode;
    getSortManager().getColumnModel().updateVisibleColumns();
  }
}
