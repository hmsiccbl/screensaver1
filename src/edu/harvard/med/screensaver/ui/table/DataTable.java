// $HeadURL:
// svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml
// $
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table;

import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.faces.component.UIData;
import javax.faces.component.UIInput;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.ui.searchresults.WellSearchResults;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;
import edu.harvard.med.screensaver.ui.table.column.TableColumnManager;
import edu.harvard.med.screensaver.ui.table.model.DataTableModel;

/**
 * JSF backing bean for data tables. Provides the following functionality:
 * <ul>
 * <li>manages DataModel, UIData, and {@link TableColumnManager} objects
 * <li>handles (re)sorting, (re)filtering, and changes to column composition,
 * in response to notifications from its {@link TableColumnManager}</li>
 * <li>handles "rows per page" command (via JSF listener method)</li>
 * <li>handles "goto row" command (via JSF listener method)</li>
 * <li>reports whether the "current" column is numeric
 * {@link #isNumericColumn()}</li>
 * </ul>
 *
 * @param R the type of the data object associated with each row
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class DataTable<R> extends AbstractBackingBean implements Observer
{
  // static members

  private static Logger log = Logger.getLogger(DataTable.class);


  // instance data members

  private DataTableModelLazyUpdateDecorator<R> _dataTableModel;
  private UIData _dataTableUIComponent;
  private Integer _pendingFirstRow;
  private TableColumnManager<R> _columnManager;
  private UIInput _rowsPerPageUIComponent;
  private RowsPerPageSelector _rowsPerPageSelector = new RowsPerPageSelector(Collections.<Integer>emptyList());
  private boolean _isTableFilterMode;
  /**
   * @motivation for unit tests
   */
  private DataTableModel<R> _baseDataTableModel;





  // public constructors and methods

  /**
   * @motivation for CGLIB2
   */
  public DataTable()
  {}

  public void initialize(DataTableModel<R> dataTableModel,
                         List<? extends TableColumn<R,?>> columns,
                         RowsPerPageSelector rowsPerPageSelector)
  {
    _columnManager = new TableColumnManager<R>(columns, getCurrentScreensaverUser());
    _columnManager.addObserver(this);
    _rowsPerPageSelector = rowsPerPageSelector;

    _baseDataTableModel = dataTableModel;
    _dataTableModel = new DataTableModelLazyUpdateDecorator<R>(_baseDataTableModel);
    _pendingFirstRow = null;
    refetch();
    refilter();
    resort();
  } 

  public UIData getDataTableUIComponent()
  {
    return _dataTableUIComponent;
  }

  public void setDataTableUIComponent(UIData dataTableUIComponent)
  {
    _dataTableUIComponent = dataTableUIComponent;
    if (_pendingFirstRow != null) {
      _dataTableUIComponent.setFirst(_pendingFirstRow);
      _pendingFirstRow = null;
    }
  }

  /**
   * @motivation MyFaces dataScroller component's 'for' attribute needs the
   *             (absolute) clientId of the dataTable component, if the
   *             dataScroller is in a different (or nested) subView.
   */
  public String getClientId()
  {
    if (_dataTableUIComponent != null) {
      return _dataTableUIComponent.getClientId(getFacesContext());
    }
    return null;
  }

  /**
   * Get the DataTableModel. Lazy instantiate, re-sort, and re-filter, as
   * necessary.
   *
   * @return the table's DataTableModel, sorted and filtered according to the
   *         latest column settings
   */
  public DataTableModel<R> getDataTableModel()
  {
    verifyIsInitialized();
    return _dataTableModel;
  }

  public TableColumnManager<R> getColumnManager()
  {
    verifyIsInitialized();
    return _columnManager;
  }

  public UIInput getRowsPerPageUIComponent()
  {
    return _rowsPerPageUIComponent;
  }

  public void setRowsPerPageUIComponent(UIInput rowsPerPageUIComponent)
  {
    _rowsPerPageUIComponent = rowsPerPageUIComponent;
  }

  public boolean isTableFilterMode()
  {
    return _isTableFilterMode;
  }

  public void setTableFilterMode(boolean isTableFilterMode)
  {
    _isTableFilterMode = isTableFilterMode;
  }

  /**
   * Convenience method that invokes the JSF action for the "current" row and
   * column (as set by JSF during the invoke application phase). Equivalent to
   * <code>getSortManager().getCurrentColumn().cellAction((E) getDataModel().getRowData())</code>.
   */
  @SuppressWarnings("unchecked")
  @UIControllerMethod
  public String cellAction()
  {
    return (String) getColumnManager().getCurrentColumn().cellAction(getRowData());
  }

  /**
   * Convenience method that returns the value of the "current" row and column
   * (as set by JSF during the render phase, when rendering each table cell).
   * Equivalent to
   * <code>getSortManager().getCurrentColumn().getCellValue(getDataModel().getRowData())</code>.
   */
  public Object getCellValue()
  {
    return getColumnManager().getCurrentColumn().getCellValue(getRowData());
  }

  /**
   * Get the data object associated with the "current" row (i.e., as set by JSF
   * at render time).
   */
  @SuppressWarnings("unchecked")
  final protected R getRowData()
  {
    return (R) getDataTableModel().getRowData();
  }

  public int getRowsPerPage()
  {
    return getRowsPerPageSelector().getSelection();
  }

  public RowsPerPageSelector getRowsPerPageSelector()
  {
    verifyIsInitialized();
    return _rowsPerPageSelector;
  }

  public void pageNumberListener(ValueChangeEvent event)
  {
    if (event.getNewValue() != null &&
      event.getNewValue().toString().trim().length() > 0) {
      log.debug("page number changed to " + event.getNewValue());
      gotoPageIndex(Integer.parseInt(event.getNewValue().toString()) - 1);
      getFacesContext().renderResponse();
    }
  }

  public void gotoPageIndex(int pageIndex)
  {
    scrollToRow(pageIndex * getRowsPerPage());
  }

  /**
   * Scrolls data table to a page boundary.
   *
   * @motivation ensures that previous & next commands do not have problems
   *             moving to previous & last page
   */
  public void scrollToPageContainingRow(int rowIndex)
  {
    int rowsPerPage = getRowsPerPage();
    if (rowIndex % rowsPerPage != 0) {
      int pageBoundaryRowIndex = rowsPerPage * (rowIndex / rowsPerPage);
      log.debug("scrolling to page boundary row: " + pageBoundaryRowIndex);
      scrollToRow(pageBoundaryRowIndex);
    }
  }

  /**
   * Scroll to the specified row by setting the UIData component's 'first' row
   * (the row displayed at the top of the current data table page). Does <i>not</i>
   * update the DataTableModel's current row index.
   *
   * @param rowIndex
   */
  public void scrollToRow(int rowIndex)
  {
    log.debug("scrollToRow(): requested row: " + rowIndex);
    // ensure value is within valid range
    rowIndex = Math.max(0, Math.min(rowIndex, getRowCount() - 1));
    setRowIndex(rowIndex);
    log.debug("scrollToRow(): actual row: " + rowIndex);
  }

  public boolean isNumericColumn()
  {
    return getColumnManager().getCurrentColumn().isNumeric();
  }

  public int getRowCount()
  {
    return getDataTableModel().getRowCount();
  }

  public void rowsPerPageListener(ValueChangeEvent event)
  {
    String rowsPerPageValue = (String) event.getNewValue();
    log.debug("rowsPerPage changed to " + rowsPerPageValue);
    getRowsPerPageSelector().setValue(rowsPerPageValue);
    scrollToPageContainingRow(_dataTableUIComponent.getFirst());
    getFacesContext().renderResponse();
  }

  public boolean isMultiPaged()
  {
    return getRowCount() > getRowsPerPage();
  }

  public void dataScrollerListener(ActionEvent event)
  {
  }

  /**
   * Resets each column's criteria to a single, non-restricting criterion. This
   * is useful for a user interface that wants to present a single criterion per
   * column, that can be edited by the user (without having to explicitly add a
   * criterion first).
   */
  @UIControllerMethod
  public String resetFilter()
  {
    for (TableColumn<R,?> column : getColumnManager().getAllColumns()) {
      column.resetCriteria();
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  /**
   * Resets the current column's criteria to a single, non-restricting
   * criterion. This is useful for a user interface that wants to present a
   * single criterion per column, that can be edited by the user (without having
   * to explicitly add a criterion first).
   */
  @UIControllerMethod
  public String resetColumnFilter()
  {
    TableColumn<R,?> column = (TableColumn<R,?>) getRequestMap().get("column");
    if (column != null) {
      column.resetCriteria();
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  /**
   * Delete all criteria from each column.
   */
  @UIControllerMethod
  public String clearFilter()
  {
    for (TableColumn<R,?> column : getColumnManager().getAllColumns()) {
      column.clearCriteria();
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @SuppressWarnings("unchecked")
  public void update(Observable o, Object arg)
  {
    if (o == getColumnManager()) {
      if (arg instanceof SortChangedEvent) {
        log.debug("DataTable notified of sort column change: " + arg);
        resort();
      }
      else if (arg instanceof Criterion) {
        log.debug("DataTable notified of criterion change: " + arg);
        refilter();
      }
      else if (arg instanceof ColumnVisibilityChangedEvent) {
        log.debug("DataTable notified of column visibility change: " + arg);
        ColumnVisibilityChangedEvent event = (ColumnVisibilityChangedEvent) arg;
        if (event.getColumnsAdded().size() > 0) {
          // TODO: only refetch if the added columns add new RelationshipPaths! (we may already have fetched the data for this column)
          refetch();
          for (TableColumn<?,?> addedColumn : event.getColumnsAdded()) {
            if (addedColumn.hasCriteria()) {
              refilter();
              break;
            }
          }
        }
        if (event.getColumnsRemoved().size() > 0) {
          for (TableColumn<?,?> removedColumn : event.getColumnsRemoved()) {
            if (removedColumn.hasCriteria()) {
              refilter();
              break;
            }
          }
          // note: if removedColumn is also a sort column, TableColumnManager
          // will handle issuing the event that forces a resort(), as necessary
        }
      }
    }
  }
  
  // making the refetch(), refilter(), and resort() methods final prevents subclasses from side-stepping the "lazy benefits" DTMLUD   

  final public void refetch()
  {
    getDataTableModel().fetch(getColumnManager().getVisibleColumns());
  }

  final public void refilter()
  {
    getDataTableModel().filter(getColumnManager().getVisibleColumns());
    // note: we cannot call {@link #scrollToRow}, as this will cause DTMLUD to trigger
    setRowIndex(0);
  }

  final public void resort()
  {
    getDataTableModel().sort(getColumnManager().getSortColumns(),
                             getColumnManager().getSortDirection());
    setRowIndex(0);
  }

  /**
   * This is a design hook that can be used by sub classes to implement Screensaver search workflow.
   * @see WellSearchResults#searchCommandListener(ActionEvent)
   */
  public void searchCommandListener(javax.faces.event.ActionEvent e) {}

  // private methods

  private void verifyIsInitialized()
  {
    if (_columnManager == null ||
      _dataTableModel == null ||
      _rowsPerPageSelector == null) {
      throw new IllegalStateException("DataTable not initialized");
    }
  }


  /**
   * Low-level setter method for changing the current row index. Client code
   * should call {@link #scrollToRow(int)}
   *
   * @motivation handle the case where client code wants to goto a row index,
   *             but the _dataTableUIComponent has not yet been set by JSF
   * @param rowIndex the row index
   */
  private void setRowIndex(int rowIndex)
  {
    if (_dataTableUIComponent != null) {
      _dataTableUIComponent.setFirst(rowIndex);
    }
    else {
      _pendingFirstRow = rowIndex;
    }
  }
}
