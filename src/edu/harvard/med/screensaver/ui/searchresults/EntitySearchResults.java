//$HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
//$Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $

//Copyright 2006 by the President and Fellows of Harvard College.

//Screensaver is an open-source project developed by the ICCB-L and NSRB labs
//at Harvard Medical School. This software is distributed under the terms of
//the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.faces.model.DataModel;
import javax.faces.model.DataModelEvent;
import javax.faces.model.DataModelListener;

import edu.harvard.med.screensaver.io.DataExporter;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.ui.table.DataTableRowsPerPageUISelectOneBean;
import edu.harvard.med.screensaver.ui.util.JSFUtils;
import edu.harvard.med.screensaver.ui.util.UISelectOneBean;

import org.apache.log4j.Logger;

/**
 * SearchResults subclass that adds support for:
 * <ul>
 * <li>"Summary" and "Entity" viewing modes, corresponding to a multi-entity list
 * view and a single-entity full page view, respectively.</li>
 * <li>Downloading of search results via one or more {@link DataExporter}s.</li>
 * </ul>
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public abstract class EntitySearchResults<E extends AbstractEntity> extends SearchResults<E>
{
  // static members

  private static Logger log = Logger.getLogger(EntitySearchResults.class);

  private static final String[] CAPABILITIES = { "viewEntity", "exportData" };


  // instance data members

  private List<DataExporter<E>> _dataExporters = Collections.emptyList();
  private UISelectOneBean<DataExporter<E>> _dataExporterSelector;


  // abstract methods

  abstract protected void setEntityToView(E entity);


  // protected methods

  /**
   * To be called by a TableColumn.cellAction() method to view the current row's
   * entity in the "entity view" mode.
   */
  final protected String viewCurrentEntity()
  {
    getDataTable().getRowsPerPageSelector().setSelection(1);
    getDataTable().gotoRowIndex(getDataTable().getDataModel().getRowIndex());
    updateEntityToView();
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected DataModel buildDataModel()
  {
    DataModel dataModel = super.buildDataModel();
    dataModel.addDataModelListener(new DataModelListener() {
      @SuppressWarnings("unchecked")
      public void rowSelected(DataModelEvent event)
      {
        assert event.getRowData() == getDataTable().getDataModel().getRowData();
        updateEntityToView();
      }
    });
    return dataModel;
  }

  protected DataTableRowsPerPageUISelectOneBean buildRowsPerPageSelector()
  {
    List<Integer> rowsPerPageSelections = new ArrayList<Integer>(SearchResults.DEFAULT_ROWS_PER_PAGE_SELECTIONS);
    // add special "single" selection item, for viewing the entity in its full viewer page
    rowsPerPageSelections.add(0, 1);
    DataTableRowsPerPageUISelectOneBean rowsPerPageSelector =
      new DataTableRowsPerPageUISelectOneBean(rowsPerPageSelections,
                                              rowsPerPageSelections.get(2))
    {
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

      @Override
      protected Integer getAllRowsValue()
      {
        return getDataTable().getRowCount();
      }
    };

    rowsPerPageSelector.addObserver(new Observer() {
      public void update(Observable obs, Object o)
      {
        if (((Integer) o) == 1) {
          updateEntityToView();
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
    super(CAPABILITIES);
  }

  public EntitySearchResults(List<DataExporter<E>> dataExporters)
  {
    super(CAPABILITIES);
    _dataExporters = dataExporters;
  }

  @Override
  public void setContents(Collection<E> unsortedResults, String description)
  {
    super.setContents(unsortedResults, description);

    // intercept re-sorts, to update the entity being viewed in "entity view" mode
    getDataTable().addObserver(new Observer() {
      public void update(Observable obs, Object o)
      {
        updateEntityToView();
      }
    });
  }

  public boolean isSummaryView()
  {
    return getDataTable().getRowsPerPage() > 1;
  }

  public boolean isEntityView()
  {
    return !isSummaryView();
  }

  public List<DataExporter<E>> getDataExporters()
  {
    return _dataExporters;
  }


  public UISelectOneBean<DataExporter<E>> getDataExporterSelector()
  {
    if (_dataExporterSelector == null) {
      _dataExporterSelector = new UISelectOneBean<DataExporter<E>>(getDataExporters()) {
        @Override
        protected String getLabel(DataExporter<E> dataExporter)
        {
          return dataExporter.getFormatName();
        }
      };
    }
    return _dataExporterSelector;
  }

  @SuppressWarnings("unchecked")
  @UIControllerMethod
  /*final (CGLIB2 restriction)*/ public String downloadSearchResults()
  {
    try {
      DataExporter dataExporter = getDataExporterSelector().getSelection();
      InputStream inputStream = dataExporter.export(getCurrentSort());
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

  /**
   * @motivation to prevent redundant calls to setEntityToView
   */
  private E entityToView = null;

  /**
   * This method should be called whenever the current row changes (due to
   * re-sort, re-creation of data model, etc.), so that if we're in "entity
   * view" mode, the current entity can be updated.
   */
  @SuppressWarnings("unchecked")
  private void updateEntityToView()
  {
    if (isEntityView()) {
      if (getDataTable().getDataModel().isRowAvailable()) {
        E rowData = (E) getRowData();
        if (rowData != null && rowData != entityToView) {
          log.debug("setting entity for single-view mode: " + rowData);
          setEntityToView(rowData);
          entityToView = rowData;
        }
      }
    }
  }

}
