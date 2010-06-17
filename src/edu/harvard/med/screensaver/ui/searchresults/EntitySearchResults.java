// $HeadURL$
// $Id$

// Copyright © 2006, 2010 by the President and Fellows of Harvard College.

// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.faces.component.UIData;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModelListener;

import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.apache.myfaces.custom.datascroller.HtmlDataScroller;
import org.apache.myfaces.custom.datascroller.ScrollerActionEvent;

import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.io.DataExporter;
import edu.harvard.med.screensaver.io.TableDataExporter;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.Entity;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.ui.EntityViewer;
import edu.harvard.med.screensaver.ui.UICommand;
import edu.harvard.med.screensaver.ui.table.DataTableModelType;
import edu.harvard.med.screensaver.ui.table.RowsPerPageSelector;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;
import edu.harvard.med.screensaver.ui.table.model.DataTableModel;
import edu.harvard.med.screensaver.ui.util.JSFUtils;
import edu.harvard.med.screensaver.ui.util.UISelectOneBean;

/**
 * SearchResults where each row represents an {@link Entity}. Provides "Summary" and "Entity" viewing modes, where
 * Summary mode is the normal table-based view of entities, and Entity mode show a detailed, full-page view of a
 * single-entity. Provides the ability to download the search results via one or more {@link DataExporter}s.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public abstract class EntitySearchResults<E extends AbstractEntity, R, K> extends SearchResults<R,K,PropertyPath<E>>
{
  private static Logger log = Logger.getLogger(EntitySearchResults.class);
  private static final String[] CAPABILITIES = { "viewEntity", "exportData", "filter" };

  private List<DataExporter<R>> _dataExporters = Lists.newArrayList();
  private UISelectOneBean<DataExporter<R>> _dataExporterSelector;

  private Observer _rowsPerPageSelectorObserver;
  private EntityViewer<E> _entityViewer;

  public void initialize(DataTableModel<R> dataTableModel)
  {
    super.initialize(new EntitySearchResultsDataModel(dataTableModel));

    // reset to default rows-per-page, if in "entity view" mode
    if (isEntityView()) {
      getRowsPerPageSelector().setSelection(getRowsPerPageSelector().getDefaultSelection());
    }
  }

  public void initialize(DataTableModel<R> dataTableModel, List<? extends TableColumn<R,?>> columns)
  {
    super.initialize(new EntitySearchResultsDataModel(dataTableModel), columns);
    
    // reset to default rows-per-page, if in "entity view" mode
    if (isEntityView()) {
      getRowsPerPageSelector().setSelection(getRowsPerPageSelector().getDefaultSelection());
    }
  }
  
  public EntityViewer getEntityViewer()
  {
    return _entityViewer;
  }

  /**
   * View the entity currently selected in the DataTableModel in entity view
   * mode.
   *
   * @motivation To be called by a TableColumn.cellAction() method to view the
   *             current row's entity in the "entity view" mode, or by any other
   *             code that wants to switch to entity view mode.
   * @motivation To be called by a DataTableModel listener in response to
   *             rowSelected() events.
   */
  @UICommand
  final protected String viewSelectedEntity()
  {
    if (getDataTableModel().getRowCount() > 0 &&
        getDataTableModel().isRowAvailable()) {
      log.debug("viewSelectedEntity(): row " + getDataTableModel().getRowIndex());
      viewEntityAtRow(getDataTableModel().getRowIndex());
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  final protected RowsPerPageSelector buildRowsPerPageSelector()
  {
    // note: we need a special "single" (1) selection item, for viewing the
    // entity in its full viewer page
    RowsPerPageSelector rowsPerPageSelector = new RowsPerPageSelector(Arrays.asList(1,
                                                                                    10,
                                                                                    20,
                                                                                    50,
                                                                                    100),
                                                                      20) {
      @Override
      public String makeLabel(Integer value)
      {
        if (value.equals(1)) {
          return "Single";
        }
        else {
          return super.makeLabel(value);
        }
      }
    };

    _rowsPerPageSelectorObserver = new Observer() {
      public void update(Observable obs, Object o)
      {
        if (((Integer) o) == 1) {
          int firstRow = 0;
          if (getDataTableUIComponent() != null) {
            firstRow = getDataTableUIComponent().getFirst();
          }
          log.debug("entering 'entity view' mode; setting data table row to first row on page:" + firstRow);
          getDataTableModel().setRowIndex(firstRow);
          viewSelectedEntity();
        }
      }
    };
    rowsPerPageSelector.addObserver(_rowsPerPageSelectorObserver);

    return rowsPerPageSelector;
  }

  // public constructors and methods

  /**
   * @motivation for CGLIB2
   */
  public EntitySearchResults()
  {
    this(null);
  }

  /**
   * 
   *
   */
  public EntitySearchResults(EntityViewer<E> entityViewer)
  {
    this(Collections.<DataExporter<R>>emptyList(), entityViewer);
  }

  /**
   * @param dataExporters a List of DataExporters that must be one of the reified
   *          types DataExporter<DataTableModel<E>> or DataExporter<E>
   */
  public EntitySearchResults(List<DataExporter<R>> dataExporters, EntityViewer<E> entityViewer)
  {
    super(CAPABILITIES);
    _entityViewer = entityViewer;
    if (_entityViewer == null) {
      getCapabilities().remove("viewEntity");
    }
    _dataExporters.add(new GenericDataExporter<R>("searchResult"));
    _dataExporters.addAll(dataExporters);
  }

  public boolean isRowRestricted()
  {
    return rowToEntity(getRowData()).isRestricted();
  }

  public boolean isSummaryView()
  {
    return !isEntityView();
  }

  public boolean isEntityView()
  {
    return getRowsPerPage() == 1 && getRowCount() > 0;
  }

  @UICommand
  public String returnToSummaryList()
  {
    getRowsPerPageSelector().setSelection(getRowsPerPageSelector().getDefaultSelection());
    scrollToPageContainingRow(getDataTableUIComponent().getFirst());
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

 
  abstract protected E rowToEntity(R row);

  abstract public void searchAll();

  /**
   * Switch to entity view mode and show the specified entity, automatically
   * scrolling the data table to the row containing the entity.
   *
   * @param entity
   * @return true if the entity exists in the search result, otherwise false
   */
  public boolean findEntity(E entity)
  {
    int rowIndex;
    // first test whether the current row is already the one with the requested entity
    if (getDataTableModel().isRowAvailable() && getRowData().equals(entity)) {
      rowIndex = getDataTableModel().getRowIndex();
      log.debug("entity " + entity + " found at current row in entity search results");
    }
    else {
      // do linear search to find the entity (but only works for InMemoryDataModel)
      rowIndex = findRowOfEntity(entity);
      if (rowIndex < 0) {
        log.debug("entity " + entity + " not found in current entity search results");
        searchAll();
        rowIndex = findRowOfEntity(entity);
        if (rowIndex < 0) {
          log.debug("entity " + entity + " not found in full entity search results");
          return false;
        }
      }
      log.debug("entity " + entity + " found in entity search results at row " + rowIndex);
    }
    viewEntityAtRow(rowIndex);
    return true;
  }

  private void viewEntityAtRow(int rowIndex)
  {
    if (rowIndex >= 0 && rowIndex < getRowCount()) {
      getDataTableModel().setRowIndex(rowIndex);
      R row = (R) getRowData();
      E entity = rowToEntity(row);
      log.debug("viewEntityAtRow(): setting entity to view: " + entity + " at row " + rowIndex);
      scrollToRow(rowIndex);
      _entityViewer.setEntity(entity);
      switchToEntityViewMode();
    }
  }

  /**
   * Switch to entity view mode, but without notifying observer.
   *
   * @motivation for switching into entity view mode programatically, as opposed
   *             to in response to a user event
   */
  private void switchToEntityViewMode()
  {
    getRowsPerPageSelector().deleteObserver(_rowsPerPageSelectorObserver);
    getRowsPerPageSelector().setSelection(1);
    getRowsPerPageSelector().addObserver(_rowsPerPageSelectorObserver);
  }

  /**
   * Override to ensure that "table filter mode" is always disabled when in
   * "entity view" mode, since search fields would entirely hidden from user in
   * this mode.
   */
  @Override
  public boolean isTableFilterMode()
  {
    return super.isTableFilterMode() && isSummaryView();
  }

  /**
   * Override to ensure that "table filter mode" is always disabled when in
   * "entity view" mode, since search fields would entirely hidden from user in
   * this mode.
   */
  @Override
  public void setTableFilterMode(boolean isTableFilterMode)
  {
    if (isEntityView()) {
      return;
    }
    else {
      super.setTableFilterMode(isTableFilterMode);
    }
  }

  public List<DataExporter<R>> getDataExporters()
  {
    return _dataExporters;
  }

  public UISelectOneBean<DataExporter<R>> getDataExporterSelector()
  {
    if (_dataExporterSelector == null) {
      _dataExporterSelector = new UISelectOneBean<DataExporter<R>>(getDataExporters()) {
        @Override
        protected String makeLabel(DataExporter<R> dataExporter)
        {
          return dataExporter.getFormatName();
        }
      };
    }
    return _dataExporterSelector;
  }

  @SuppressWarnings("unchecked")
  @UICommand
  /* final (CGLIB2 restriction) */
  public String downloadSearchResults()
  {
    try {
      DataExporter<?> dataExporter = getDataExporterSelector().getSelection();
      InputStream inputStream;
      log.debug("starting exporting data for download");
      // TODO: TableDataExporter should be injected with the associated data table so they can retrieve the columns on demand
      if (dataExporter instanceof TableDataExporter) {
        ((TableDataExporter<R>) dataExporter).setTableColumns(getColumnManager().getVisibleColumns());
      }
      inputStream = ((DataExporter<R>) dataExporter).export(getDataTableModel().iterator());
      log.debug("finished exporting data for download");
      JSFUtils.handleUserDownloadRequest(getFacesContext(),
                                         inputStream,
                                         dataExporter.getFileName(),
                                         dataExporter.getMimeType());
    }
    catch (IOException e) {
      reportApplicationError(e.toString());
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  // private methods

  public void dataScrollerListener(ActionEvent event)
  {
    if (isEntityView()) {
      if (getDataTableUIComponent() != null) {
        UIData uiData = getDataTableUIComponent();
        HtmlDataScroller scroller = (HtmlDataScroller) event.getSource();
        ScrollerActionEvent scrollerEvent = (ScrollerActionEvent) event;
        String facet = scrollerEvent.getScrollerfacet();
        int nextRowIndex = -1;
        int originalRowIndex = uiData.getFirst();
        // the following code was copied from HtmlDataScroller.broadcast(),
        // since we must calculate the next row to be scrolled to in exactly the
        // same way, since the new row is not yet calculated for us at the time
        // this listener is called.
        if (HtmlDataScroller.FACET_FIRST.equals(facet)) {
          nextRowIndex = 0;
        }
        else if (HtmlDataScroller.FACET_PREVIOUS.equals(facet)) {
          int previous = uiData.getFirst() - uiData.getRows();
          if (previous >= 0) {
            nextRowIndex = previous;
          }
        }
        else if (HtmlDataScroller.FACET_NEXT.equals(facet)) {
          int next = uiData.getFirst() + uiData.getRows();
          if (next < uiData.getRowCount()) {
            nextRowIndex = next;
          }
        }
        else if (HtmlDataScroller.FACET_FAST_FORWARD.equals(facet)) {
          int fastStep = Math.max(1, scroller.getFastStep());
          nextRowIndex = uiData.getFirst() + uiData.getRows() * fastStep;
          int rowcount = uiData.getRowCount();
          if (nextRowIndex > rowcount) {
            nextRowIndex = (rowcount - 1) - ((rowcount - 1) % uiData.getRows());
          }
        }
        else if (HtmlDataScroller.FACET_FAST_REWIND.equals(facet)) {
          int fastStep = Math.max(1, scroller.getFastStep());
          nextRowIndex = uiData.getFirst() - uiData.getRows() * fastStep;
          nextRowIndex = Math.max(0, nextRowIndex);
        }
        else if (HtmlDataScroller.FACET_LAST.equals(facet)) {
          int rowcount = uiData.getRowCount();
          int rows = uiData.getRows();
          int delta = rowcount % rows;
          nextRowIndex = delta > 0 && delta < rows ? rowcount - delta : rowcount - rows;
          nextRowIndex = Math.max(0, nextRowIndex);
        }
        if (nextRowIndex >= 0) {
          getDataTableModel().setRowIndex(nextRowIndex);
          viewSelectedEntity();
          // revert scrolling performed by viewSelectedEntity(), since
          // HtmlDataScroller handler will expect this to be unchanged
          uiData.setFirst(originalRowIndex);
        }
      }
    }
  }

  protected void updateEntityView()
  {
    if (isEntityView()) {
      if (getDataTableUIComponent() != null) {
        getDataTableModel().setRowIndex(getDataTableUIComponent().getFirst());
      }
      viewSelectedEntity();
    }
  }

  private int findRowOfEntity(E entity)
  {
    DataTableModel model = (DataTableModel) getDataTableModel();
    if (model.getModelType() == DataTableModelType.IN_MEMORY) { 
      List<R> data = (List<R>) model.getWrappedData();
      for (int i = 0; i < data.size(); i++) {
        if (rowToEntity(data.get(i)).equals(entity)) {
          return i;
        }
      }
    }
    return -1;
  }
  
  private class EntitySearchResultsDataModel extends DataTableModel<R>
  {
    private DataTableModel<R> _baseDataModel;

    protected EntitySearchResultsDataModel(DataTableModel<R> baseDataModel)
    {
      super();
      _baseDataModel = baseDataModel;
    }

    @Override
    public void addDataModelListener(DataModelListener listener)
    {
      _baseDataModel.addDataModelListener(listener);
    }

    @Override
    public DataModelListener[] getDataModelListeners()
    {
      return _baseDataModel.getDataModelListeners();
    }

    @Override
    public void removeDataModelListener(DataModelListener listener)
    {
      _baseDataModel.removeDataModelListener(listener);
    }

    @Override
    public void fetch(List<? extends TableColumn<R,?>> columns)
    {
      _baseDataModel.fetch(columns);
    }

    @Override
    public void filter(List<? extends TableColumn<R,?>> filterColumns)
    {
      _baseDataModel.filter(filterColumns);
      updateEntityView();
    }

    @Override
    public DataTableModelType getModelType()
    {
      return _baseDataModel.getModelType();
    }

    @Override
    public void sort(List<? extends TableColumn<R,?>> sortColumns, SortDirection sortDirection)
    {
      _baseDataModel.sort(sortColumns, sortDirection);
      updateEntityView();
    }

    @Override
    public int getRowCount()
    {
      return _baseDataModel.getRowCount();
    }

    @Override
    public Object getRowData()
    {
      return _baseDataModel.getRowData();
    }

    @Override
    public int getRowIndex()
    {
      return _baseDataModel.getRowIndex();
    }

    @Override
    public Object getWrappedData()
    {
      return _baseDataModel.getWrappedData();
    }

    @Override
    public boolean isRowAvailable()
    {
      return _baseDataModel.isRowAvailable();
    }

    @Override
    public void setRowIndex(int rowIndex)
    {
      _baseDataModel.setRowIndex(rowIndex);
    }

    @Override
    public void setWrappedData(Object data)
    {
      _baseDataModel.setWrappedData(data);
    }

    @Override
    public Iterator<R> iterator()
    {
      return _baseDataModel.iterator();
    }
  }
}
