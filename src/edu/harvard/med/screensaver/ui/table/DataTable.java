// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.faces.component.UIData;
import javax.faces.component.UIInput;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.DataModel;

import edu.harvard.med.screensaver.ui.AbstractBackingBean;

import org.apache.log4j.Logger;

/**
 * Provide the following common functionality for backing beans of JSF data
 * tables:
 * <ul>
 * <li>maintains DataModel, UIData, and TableSortManager objects
 * <li>manages (re)sorting in response to notifications from its TableSortManager</li>
 * <li>handles "rows per page" command (via JSF listener)</li>
 * <li>handles "goto row" command (via JSF listener)</li>
 * <li>reports whether the "current" column is numeric {@link #isNumericColumn()}</li>
 * </ul>
 *
 * @param E the type of the data object associated with each row
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public abstract class DataTable<E> extends AbstractBackingBean implements Observer
{
  // static members

  private static Logger log = Logger.getLogger(DataTable.class);


  // instance data members

  private DataModel _dataModel;
  private UIData _dataTableUIComponent;
  private TableSortManager<E> _sortManager;
  private UIInput _rowsPerPageUIComponent;
  private DataTableRowsPerPageUISelectOneBean _rowsPerPageSelector;
  private List<Observer> _observers = new ArrayList<Observer>();


  // abstract & template methods

  abstract protected List<TableColumn<E>> buildColumns();

  abstract protected DataModel buildDataModel();

  abstract protected DataTableRowsPerPageUISelectOneBean buildRowsPerPageSelector();


  // public constructors and methods

  public UIData getDataTableUIComponent()
  {
    return _dataTableUIComponent;
  }

  public void setDataTableUIComponent(UIData dataTableUIComponent)
  {
    _dataTableUIComponent = dataTableUIComponent;
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

  public DataModel getDataModel()
  {
    if (_dataModel == null) {
      _dataModel = buildDataModel();
    }
    return _dataModel;
  }

  public TableSortManager<E> getSortManager()
  {
    if (_sortManager == null) {
      List<TableColumn<E>> columns = buildColumns();
      _sortManager = new TableSortManager<E>(columns);
      _sortManager.addObserver(this);
    }
    return _sortManager;
  }

  public UIInput getRowsPerPageUIComponent()
  {
    return _rowsPerPageUIComponent;
  }

  public void setRowsPerPageUIComponent(UIInput rowsPerPageUIComponent)
  {
    _rowsPerPageUIComponent = rowsPerPageUIComponent;
  }

  @SuppressWarnings("unchecked")
  public String cellAction()
  {
    return (String) getSortManager().getCurrentColumn().cellAction((E) getDataModel().getRowData());
  }

  public int getRowsPerPage()
  {
    return getRowsPerPageSelector().getSelection();
  }

  public DataTableRowsPerPageUISelectOneBean getRowsPerPageSelector()
  {
    if (_rowsPerPageSelector == null) {
      _rowsPerPageSelector = buildRowsPerPageSelector();
    }
    return _rowsPerPageSelector;
  }

  public void pageNumberListener(ValueChangeEvent event)
  {
    if (event.getNewValue() != null && event.getNewValue().toString().trim().length() > 0) {
      log.debug("page number changed to " + event.getNewValue());
      gotoPageIndex(Integer.parseInt(event.getNewValue().toString()) - 1);
//      _rowsPerPageUIComponent.setSubmittedValue(null); // clear
//      _rowsPerPageUIComponent.setValue(null); // clear
      getFacesContext().renderResponse();
    }
  }

  public void gotoPageIndex(int pageIndex)
  {
    gotoRowIndex(pageIndex * getRowsPerPage());
  }

  public void gotoRowIndex(int rowIndex)
  {
    if (_dataTableUIComponent != null) {
      // ensure value is within valid range, and in particular that we never show
      // less than the table's configured row count (unless it's more than the
      // total number of rows)
      rowIndex = Math.max(0,
                          Math.min(rowIndex,
                                   getRowCount() - getDataTableUIComponent().getRows()));
      _dataTableUIComponent.setFirst(rowIndex);
    }
  }

  public boolean isNumericColumn()
  {
    return getSortManager().getCurrentColumn().isNumeric();
  }

  public int getRowCount()
  {
    return getDataModel().getRowCount();
  }

  public void rowsPerPageListener(ValueChangeEvent event)
  {
    log.debug("rowsPerPage changed to " + event.getNewValue());
    getRowsPerPageSelector().setValue((String) event.getNewValue());
    if (!isMultiPaged()) {
      gotoRowIndex(0);
    }
    if (getDataModel() instanceof VirtualPagingDataModel) {
      ((VirtualPagingDataModel<?,?>) getDataModel()).setRowsToFetch(getRowsPerPageSelector().getSelection());
    }
    getFacesContext().renderResponse();
  }

  public boolean isMultiPaged()
  {
    return getRowCount() > getRowsPerPage();
  }

  @SuppressWarnings("unchecked")
  public void update(Observable o, Object obj)
  {
    resort();
    for (Observer observer : _observers) {
      observer.update(o, obj);
    }
  }

  public void resort()
  {
    // TODO: full rebuild is only strictly needed by VirtualPagingDataModel,
    // other DataModel classes could have a callback method called to avoid
    // database calls (as they could do their own in-memory sorting)
    rebuildRows();
  }

  public void rebuildColumnsAndRows()
  {
    _sortManager = null;
    rebuildRows();
  }

  public void rebuildRows()
  {
    _dataModel = null; // force rebuild
    getRowsPerPageSelector().setAllRowsValue(getDataModel().getRowCount());
    if (_dataTableUIComponent != null) {
      _dataTableUIComponent.setFirst(0);
    }
  }


  // private methods

  private void reset()
  {
    _dataTableUIComponent = null;
    _rowsPerPageUIComponent = null;
    _rowsPerPageSelector = null;
    _dataModel = null;
    _sortManager = null;
  }

  public void addObserver(Observer observer)
  {
    _observers.add(observer);
  }
}
