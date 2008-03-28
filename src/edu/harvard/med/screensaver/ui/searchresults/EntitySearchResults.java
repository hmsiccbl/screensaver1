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
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.faces.model.DataModelEvent;
import javax.faces.model.DataModelListener;

import edu.harvard.med.screensaver.db.datafetcher.DataFetcher;
import edu.harvard.med.screensaver.db.datafetcher.EntityDataFetcher;
import edu.harvard.med.screensaver.db.datafetcher.EntitySetDataFetcher;
import edu.harvard.med.screensaver.io.DataExporter;
import edu.harvard.med.screensaver.io.TableDataExporter;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.PropertyPath;
import edu.harvard.med.screensaver.ui.UIControllerMethod;
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

  private List<DataExporter<E,K>> _dataExporters = new ArrayList<DataExporter<E,K>>();
  private UISelectOneBean<DataExporter<E,K>> _dataExporterSelector;
  /**
   * @motivation to prevent redundant calls to setEntityToView
   */
  private E entityToView = null;



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
   * To be called by a TableColumn.cellAction() method to view the current row's
   * entity in the "entity view" mode, or by any other code that wants to switch
   * to entity view mode. The current row is determined by
   * getDataTableModel().getRowIndex().
   */
  @UIControllerMethod
  final protected String viewCurrentEntity()
  {
    if (getDataTableModel().getRowCount() == 0 ||
        !getDataTableModel().isRowAvailable()) {
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    E rowData = (E) getRowData();
    if (rowData != entityToView && getDataTableUIComponent() != null) {
      // first, scroll the data table so that the user-selected row is the first on the page,
      // otherwise rowsPerPageSelector's observer will switch to whatever entity was previously in the first row
      int rowIndex = getDataTableModel().getRowIndex();
      log.debug("viewCurrentEntity(): scrolling table to row " + rowIndex);
      getDataTableUIComponent().setFirst(rowIndex);

      // switch to entity view mode, officially
      getRowsPerPageSelector().setSelection(1);

      // set the entity to be viewed
      log.debug("viewCurrentEntity(): setting entity to view: " + rowData);
      setEntityToView(rowData);
      entityToView = rowData;
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

    rowsPerPageSelector.addObserver(new Observer() {
      public void update(Observable obs, Object o)
      {
        if (((Integer) o) == 1) {
          if (getDataTableUIComponent() != null) {
            // this will cause our DataModel listener to set the entity to be viewed
            log.debug("entering 'entity view' mode; setting data table row to first row on page:" +
                      getDataTableUIComponent().getFirst());
            getDataTableModel().setRowIndex(getDataTableUIComponent().getFirst());
          }
        }
      }
    });

    return rowsPerPageSelector;
  }

  // public constructors and methods

  /**
   * @motivation for CGLIB2
   */
  protected EntitySearchResults()
  {
    this(Collections.<DataExporter<E,K>>emptyList());
  }

  public EntitySearchResults(List<DataExporter<E,K>> dataExporters)
  {
    super(CAPABILITIES);
    _dataExporters.add(new GenericDataExporter<E,K>("searchResult"));
    _dataExporters.addAll(dataExporters);
  }

  @Override
  public void resort()
  {
    super.resort();
    if (isEntityView()) {
      if (getDataTableUIComponent() != null) {
        getDataTableModel().setRowIndex(getDataTableUIComponent().getFirst());
      }
      viewCurrentEntity();
    }
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

  public List<DataExporter<E,K>> getDataExporters()
  {
    return _dataExporters;
  }

  public UISelectOneBean<DataExporter<E,K>> getDataExporterSelector()
  {
    if (_dataExporterSelector == null) {
      _dataExporterSelector = new UISelectOneBean<DataExporter<E,K>>(getDataExporters()) {
        @Override
        protected String getLabel(DataExporter<E,K> dataExporter)
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
      DataExporter dataExporter = getDataExporterSelector().getSelection();
      if (dataExporter instanceof TableDataExporter) {
        ((TableDataExporter) dataExporter).setTableColumns(getColumnManager().getVisibleColumns());
      }
      InputStream inputStream = dataExporter.export(getDataFetcher());
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
      DataTableModel<E> dataTableModel = doBuildDataModel((EntityDataFetcher<E,K>) dataFetcher, columns);
      dataTableModel.addDataModelListener(new DataModelListener() {
        public void rowSelected(DataModelEvent event)
        {
          if (isEntityView()) {
            viewCurrentEntity();
          }
        }
      });
      return dataTableModel;
    }
    // for no-op (empty data model)
    return new InMemoryDataModel<E>(dataFetcher);
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
      model = new InMemoryEntityDataModel<E>(dataFetcher);
    }
    else {
      log.debug("using VirtualPagingDataModel (sweet!)");
      model = new VirtualPagingEntityDataModel<K,E>(dataFetcher,
        new ValueReference<Integer>() { public Integer value() { return getRowsPerPage(); }
      });
    }
    return model;
  }

}
