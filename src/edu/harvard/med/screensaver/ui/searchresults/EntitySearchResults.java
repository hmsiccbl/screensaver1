// $HeadURL:
// svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml
// $
// $Id$

// Copyright 2006 by the President and Fellows of Harvard College.

// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.faces.component.UIData;
import javax.faces.event.ActionEvent;

import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.db.datafetcher.DataFetcher;
import edu.harvard.med.screensaver.db.datafetcher.EntityDataFetcher;
import edu.harvard.med.screensaver.db.datafetcher.EntitySetDataFetcher;
import edu.harvard.med.screensaver.io.DataExporter;
import edu.harvard.med.screensaver.io.TableDataExporter;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.PropertyPath;
import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.ui.table.DataTableModelType;
import edu.harvard.med.screensaver.ui.table.RowsPerPageSelector;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.EntityColumn;
import edu.harvard.med.screensaver.ui.table.model.DataTableModel;
import edu.harvard.med.screensaver.ui.table.model.InMemoryDataModel;
import edu.harvard.med.screensaver.ui.table.model.InMemoryEntityDataModel;
import edu.harvard.med.screensaver.ui.table.model.VirtualPagingEntityDataModel;
import edu.harvard.med.screensaver.ui.util.JSFUtils;
import edu.harvard.med.screensaver.ui.util.UISelectOneBean;
import edu.harvard.med.screensaver.ui.util.ValueReference;

import org.apache.log4j.Logger;
import org.apache.myfaces.custom.datascroller.HtmlDataScroller;
import org.apache.myfaces.custom.datascroller.ScrollerActionEvent;

/**
 * SearchResults subclass that presents a particular type of domain model
 * entity. Subclass adds:
 * <ul>
 * <li>"Summary" and "Entity" viewing modes, corresponding to a multi-entity
 * list view and a single-entity full page view, respectively.</li>
 * <li>Dynamically decides whether to use InMemoryDataModel of
 * VirtualPagingDataModel, based upon data size and column composition.</li>
 * <li>Management of "filter mode"</li>
 * <li>Downloading of search results via one or more {@link DataExporter}s.</li>
 * </ul>
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public abstract class EntitySearchResults<E extends AbstractEntity, K> extends SearchResults<E,K,PropertyPath<E>>
{
  // static members

  /**
   * The maximum number of entities that can be loaded as a single batch search
   * result, using InMemoryDataModel. If more than this number is to be loaded,
   * VirtualPagingDataModel will be used instead.
   */
  public static final int ALL_IN_MEMORY_THRESHOLD = 1024;

  private static Logger log = Logger.getLogger(EntitySearchResults.class);

  private static final String[] CAPABILITIES = { "viewEntity", "exportData", "filter" };

  // instance data members

  private List<DataExporter<?>> _dataExporters = new ArrayList<DataExporter<?>>();
  private UISelectOneBean<DataExporter<?>> _dataExporterSelector;

  private Observer _rowsPerPageSelectorObserver;


  // abstract methods

  abstract protected void setEntityToView(E entity);

  // protected methods

  @Override
  public void initialize(DataFetcher<E,K,PropertyPath<E>> dataFetcher)
  {
    super.initialize(dataFetcher);

    // reset to default rows-per-page, if in "entity view" mode
    if (isEntityView()) {
      getRowsPerPageSelector().setSelection(getRowsPerPageSelector().getDefaultSelection());
    }
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
  @UIControllerMethod
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
      public String getLabel(Integer value)
      {
        if (value.equals(1)) {
          return "Single";
        }
        else {
          return super.getLabel(value);
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
    this(Collections.<DataExporter<?>>emptyList());
  }

  /**
   * Constructs a EntitySearchResults object.
   *
   * @param dataExporters a List of DataExporters that must be one of the reified
   *          types DataExporter<DataTableModel<E>> or DataExporter<E>
   */
  public EntitySearchResults(List<DataExporter<?>> dataExporters)
  {
    super(CAPABILITIES);
    _dataExporters.add(new GenericDataExporter<E>("searchResult"));
    _dataExporters.addAll(dataExporters);
  }

  public boolean isRowRestricted()
  {
    return getRowData().isRestricted();
  }

  public boolean isSummaryView()
  {
    return !isEntityView();
  }

  public boolean isEntityView()
  {
    return getRowsPerPage() == 1 && getRowCount() > 0;
  }

  @UIControllerMethod
  public String returnToSummaryList()
  {
    getRowsPerPageSelector().setSelection(getRowsPerPageSelector().getDefaultSelection());
    scrollToPageContainingRow(getDataTableUIComponent().getFirst());
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  /**
   * Switch to entity view mode and show the specified entity, automatically
   * scrolling the data table to the row containing the entity.
   *
   * @param entity
   * @return true if the entity exists in the search result, otherwise false
   */
  public boolean viewEntity(E entity)
  {
//    // first test whether the current row is already the one with the requested entity
//    E currentEntityInSearchResults = null;
//    if (getDataTableUIComponent() != null) {
//      int currentRow = getDataTableUIComponent().getFirst();
//      getDataTableModel().setRowIndex(currentRow);
//      if (getDataTableModel().isRowAvailable()) {
//        currentEntityInSearchResults = (E) getDataTableModel().getRowData();
//        if (entity.equals(currentEntityInSearchResults)) {
//          return true;
//        }
//      }
//    }

    // else, do linear search to find the entity (but only works for InMemoryDataModel)
    log.debug("viewEntity(): entity " + entity);
    int rowIndex = findRowOfEntity(entity);
    if (rowIndex < 0) {
      log.debug("entity " + entity + " not found in entity search results");
      return false;
    }
    log.debug("entity " + entity + " found in entity search results at row " + rowIndex);
    viewEntityAtRow(rowIndex);
    return true;
  }

  private void viewEntityAtRow(int rowIndex)
  {
    if (rowIndex >= 0 && rowIndex < getRowCount()) {
      getDataTableModel().setRowIndex(rowIndex);
      E entity = (E) getRowData();
      log.debug("viewEntityAtRow(): setting entity to view: " + entity + " at row " + rowIndex);
      scrollToRow(rowIndex);
      setEntityToView(entity);
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

  /**
   * @motivation type safety of return type
   */
  @Override
  public EntityDataFetcher<E,K> getDataFetcher()
  {
    return (EntityDataFetcher<E,K>) super.getDataFetcher();
  }

  /**
   * @return a List of DataExporters that will be one of the reified types
   *         DataExporter<DataTableModel<E>> or DataExporter<E>
   */
  public List<DataExporter<?>> getDataExporters()
  {
    return _dataExporters;
  }

  public UISelectOneBean<DataExporter<?>> getDataExporterSelector()
  {
    if (_dataExporterSelector == null) {
      _dataExporterSelector = new UISelectOneBean<DataExporter<?>>(getDataExporters()) {
        @Override
        protected String getLabel(DataExporter<?> dataExporter)
        {
          return dataExporter.getFormatName();
        }
      };
    }
    return _dataExporterSelector;
  }

  @SuppressWarnings("unchecked")
  @UIControllerMethod
  /* final (CGLIB2 restriction) */
  public String downloadSearchResults()
  {
    try {
      DataExporter<?> dataExporter = getDataExporterSelector().getSelection();
      InputStream inputStream;
      if (dataExporter instanceof TableDataExporter) {
        ((TableDataExporter<E>) dataExporter).setTableColumns(getColumnManager().getVisibleColumns());
        inputStream = ((TableDataExporter<E>) dataExporter).export(getDataTableModel());
      }
      else {
        inputStream = ((DataExporter<Collection<K>>) dataExporter).export(getDataFetcher().findAllKeys());
      }
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

  @Override
  final protected DataTableModel<E> buildDataTableModel(DataFetcher<E,K,PropertyPath<E>> dataFetcher,
                                                        List<? extends TableColumn<E,?>> columns)
  {
    if (dataFetcher instanceof EntityDataFetcher) {
      return doBuildDataModel((EntityDataFetcher<E,K>) dataFetcher, columns);
    }
    // for no-op (empty data model)
    return new InMemoryDataModel<E>(dataFetcher);
  }

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

  /**
   * Factory method to build the data model, allowing subclasses to determine
   * customize decision to use in-memory or virtual paging data model (or some
   * other data access strategy altogether).
   *
   * @param dataFetcher the DataFetcher associated with this SearchResults
   *          object.
   * @return a DataTableModel
   */
  protected DataTableModel<E> doBuildDataModel(EntityDataFetcher<E,K> dataFetcher,
                                               List<? extends TableColumn<E,?>> columns)
  {
    boolean allColumnsHavePropertyPaths = true;
    for (TableColumn<E,?> column : columns) {
      EntityColumn entityColumn = (EntityColumn) column;
      if (entityColumn.getPropertyPath() == null) {
        allColumnsHavePropertyPaths = false;
        break;
      }
    }

    DataTableModel<E> model;
    if (!allColumnsHavePropertyPaths ||
        (dataFetcher instanceof EntitySetDataFetcher &&
          ((EntitySetDataFetcher) dataFetcher).getDomain().size() <= ALL_IN_MEMORY_THRESHOLD)) {
      if (!allColumnsHavePropertyPaths) {
        log.debug("using InMemoryDataModel due to having some columns that do not map directly to database fields");
      }
      else {
        log.debug("using InMemoryDataModel due to domain size");
      }
      model = new InMemoryEntityDataModel<E>(dataFetcher) {
        @Override
        public void filter(List<? extends TableColumn<E,?>> columns)
        {
          super.filter(columns);
          updateEntityView();
        }
        
        @Override
        public void sort(List<? extends TableColumn<E,?>> sortColumns,
                         SortDirection sortDirection)
        {
          super.sort(sortColumns, sortDirection);
          updateEntityView();
        }
      };
    }
    else {
      log.debug("using VirtualPagingDataModel (sweet!)");
      model = new VirtualPagingEntityDataModel<K,E>(dataFetcher,
        new ValueReference<Integer>() { public Integer value() { return getRowsPerPage(); }
      }) {
        @Override
        public void filter(List<? extends TableColumn<E,?>> columns)
        {
          super.filter(columns);
          updateEntityView();
        }
        
        @Override
        public void sort(List<? extends TableColumn<E,?>> sortColumns,
                         SortDirection sortDirection)
        {
          super.sort(sortColumns, sortDirection);
          updateEntityView();
        }
      };
    }
    return model;
  }
  
  private void updateEntityView()
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
      List<E> data = (List<E>) model.getWrappedData();
      for (int i = 0; i < data.size(); i++) {
        if (data.get(i).equals(entity)) {
          return i;
        }
      }
    }
    return -1;
  }
}
