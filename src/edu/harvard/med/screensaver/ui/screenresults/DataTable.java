// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults;

import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.faces.component.UIData;
import javax.faces.component.UIInput;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.DataModel;

import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.table.DataTableRowsPerPageUISelectOneBean;
import edu.harvard.med.screensaver.ui.table.TableColumn;
import edu.harvard.med.screensaver.ui.table.TableSortManager;

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
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public abstract class DataTable extends AbstractBackingBean implements Observer
{
  // static members

  private static Logger log = Logger.getLogger(DataTable.class);


  // instance data members

  private DataModel _dataModel;
  private UIData _dataTableUIComponent;
  private TableSortManager<Map<String,Object>> _sortManager;
  private UIInput _rowsPerPageUIComponent;
  private DataTableRowsPerPageUISelectOneBean _rowsPerPageSelector;

  // abstract methods

  abstract protected List<TableColumn<Map<String,Object>>> buildColumns();

  abstract protected DataModel buildDataModel();

  abstract protected List<Integer> getRowsPerPageSelections();


  // public constructors and methods

  public UIData getDataTableUIComponent()
  {
    return _dataTableUIComponent;
  }

  public void setDataTableUIComponent(UIData dataTableUIComponent)
  {
    _dataTableUIComponent = dataTableUIComponent;
  }

  public DataModel getDataModel()
  {
    if (_dataModel == null) {
      _dataModel = buildDataModel();
    }
    return _dataModel;
  }

  public TableSortManager<Map<String,Object>> getSortManager()
  {
    if (_sortManager == null) {
      List<TableColumn<Map<String,Object>>> columns = buildColumns();
      _sortManager = new TableSortManager<Map<String,Object>>(columns);
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
    return (String) getSortManager().getCurrentColumn().cellAction((Map<String,Object>) getDataModel().getRowData());
  }

  public DataTableRowsPerPageUISelectOneBean getRowsPerPageSelector()
  {
    if (_rowsPerPageSelector == null) {
      _rowsPerPageSelector = new DataTableRowsPerPageUISelectOneBean(getRowsPerPageSelections());
      _rowsPerPageSelector.setAllRowsValue(getRawDataSize()); // only has an effect in case where getRowsPerPageSelections() contains SHOW_ALL_VALUE
    }
    return _rowsPerPageSelector;
  }

  public void rowNumberListener(ValueChangeEvent event)
  {
    if (event.getNewValue() != null && event.getNewValue().toString().trim().length() > 0) {
      log.debug("row number changed to " + event.getNewValue());
      gotoDataTableRowIndex(Integer.parseInt(event.getNewValue().toString()) - 1);
      getFacesContext().renderResponse();
    }
  }

  public boolean isNumericColumn()
  {
    return getSortManager().getCurrentColumn().isNumeric();
  }

  public int getRawDataSize()
  {
    return getDataModel().getRowCount();
  }

  public void rowsPerPageListener(ValueChangeEvent event)
  {
    log.debug("rowsPerPage changed to " + event.getNewValue());
    getRowsPerPageSelector().setValue((String) event.getNewValue());
    ((ScreenResultDataModel) getDataModel()).setRowsToFetch(getRowsPerPageSelector().getSelection());
    getFacesContext().renderResponse();
  }

  @SuppressWarnings("unchecked")
  public void update(Observable o, Object obj)
  {
    // sort column changed
    resort();
  }

  public void resort()
  {
    // TODO: full rebuild is only strictly needed by FullScreenResultDataModel, other ScreenResultDataModel classes could have a callback method called to avoid database calls (as they could do their own in-memory sorting)
    rebuildRows();
  }

  public void rebuildColumnsAndRows()
  {
    _sortManager = null;
    rebuildRows();
  }

  public void rebuildRows()
  {
    _dataModel = null; // force rebuild, but lazily
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


  private void gotoDataTableRowIndex(int rowIndex)
  {
    log.debug("goto data table row index " + rowIndex);
    // ensure value is within valid range, and in particular that we never show
    // less than the table's configured row count (unless it's more than the
    // total number of rows)
    rowIndex = Math.max(0,
                        Math.min(rowIndex,
                                 getRawDataSize() - getDataTableUIComponent().getRows()));
    getDataTableUIComponent().setFirst(rowIndex);
  }
}
