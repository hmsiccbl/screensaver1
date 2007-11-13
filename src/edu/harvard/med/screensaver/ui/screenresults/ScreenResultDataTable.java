// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.faces.component.UIInput;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.db.ScreenResultSortQuery.SortByWellProperty;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellType;
import edu.harvard.med.screensaver.model.screenresults.AssayWellType;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.ui.libraries.WellViewer;
import edu.harvard.med.screensaver.ui.searchresults.ResultValueTypeColumn;
import edu.harvard.med.screensaver.ui.searchresults.WellColumn;
import edu.harvard.med.screensaver.ui.table.DataTable;
import edu.harvard.med.screensaver.ui.table.TableColumn;
import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.log4j.Logger;

/**
 * Abstract backing bean base class for data tables that display ResultValues
 * for a set of ResultValueType columns. First three columns are always "Plate",
 * "Well", and "Well Type". Subsequent columns are determined by the set of
 * ResultValueTypes passed to this object. Provides the following common
 * functionality to subclasses:
 * <ul>
 * <li>defines fixed columns common to all screen result data tables</li>
 * <li>provides bean property for excluded flag of "current" result value</li>
 * </ul>
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public abstract class ScreenResultDataTable extends DataTable<Well>
{
  // static members

  private static Logger log = Logger.getLogger(ScreenResultDataTable.class);

  // instance data members

  private ScreenResultViewer _screenResultViewer;
  private WellViewer _wellViewer;
  private LibrariesDAO _librariesDao;
  protected GenericEntityDAO _dao;

  private ScreenResult _screenResult;
  private List<ResultValueType> _resultValueTypes = Collections.emptyList();


  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected ScreenResultDataTable()
  {
  }

  public ScreenResultDataTable(ScreenResultViewer screenResultViewer,
                               WellViewer wellViewer,
                               LibrariesDAO librariesDao,
                               GenericEntityDAO dao)
  {
    _screenResultViewer = screenResultViewer;
    _wellViewer = wellViewer;
    _librariesDao = librariesDao;
    _dao = dao;
  }


  // public constructors and methods

  public void setScreenResult(ScreenResult screenResult)
  {
    _screenResult = screenResult;
  }

  public ScreenResult getScreenResult()
  {
    return _screenResult;
  }

  public List<ResultValueType> getResultValueTypes()
  {
    return _resultValueTypes;
  }

  public void setResultValueTypes(List<ResultValueType> resultValueTypes)
  {
    _resultValueTypes = resultValueTypes;
    rebuildColumnsAndRows();
    if (getRowsPerPageUIComponent() != null) {
      getRowsPerPageUIComponent().setValue(getRowsPerPageSelector().getValue());
    }
  }

  public boolean isResultValueExcluded()
  {
    TableColumn<Well,?> column = getSortManager().getCurrentColumn();
    if (column instanceof ResultValueTypeColumn) {
      ResultValueTypeColumn rvtColumn = (ResultValueTypeColumn) column;
      ResultValue resultValue = getRowData().getResultValues().get(rvtColumn.getResultValueType());
      if (resultValue != null) {
        return resultValue.isExclude();
      }
    }
    return false;
  }

  /**
   * @motivation We need to give all instances of ScreenResultDataTable the same
   *             UIInput object. See
   *             {@link ScreenResultViewer#setSharedDataTableUIComponent(javax.faces.component.UIData)}.
   *             Since we use dataTableNavigator.jspf, we can't customize the
   *             UIInput component's binding attribute to be
   *             screenViewer.sharedDataTableUIComponent, which would eliminate
   *             need for this method; but it is bound to this method, and so we
   *             delegate.
   */
  public void setRowsPerPageUIComponent(UIInput rowsPerPageUIComponent)
  {
    if (getRowsPerPageUIComponent() != rowsPerPageUIComponent) {
      super.setRowsPerPageUIComponent(rowsPerPageUIComponent);
      _screenResultViewer.setSharedRowsPerPageUIComponent(rowsPerPageUIComponent);
    }
  }

  // abstract method implementations

  protected List<TableColumn<Well,?>> buildColumns()
  {
    List<TableColumn<Well,?>> columns = new ArrayList<TableColumn<Well,?>>();
    columns.addAll(buildFixedColumns());
    columns.addAll(buildVariableColumns());
    return columns;
  }


  // private methods

  private List<TableColumn<Well,?>> buildFixedColumns()
  {
    List<TableColumn<Well,?>> fixedColumns = new ArrayList<TableColumn<Well,?>>(3);
    fixedColumns.add(new WellColumn<Integer>(SortByWellProperty.PLATE_NUMBER,
                                    "Plate",
                                    "The plate number") {
      @Override
      public Integer getCellValue(Well well) { return well.getWellKey().getPlateNumber(); }
    });
    fixedColumns.add(new WellColumn<String>(SortByWellProperty.WELL_NAME,
                                    "Well",
                                    "The well name") {
      @Override
      public String getCellValue(Well well) { return well.getWellKey().getWellName(); }

      @Override
      public boolean isCommandLink() { return true; }

      @Override
      public Object cellAction(Well well)
      {
        return _wellViewer.viewWell(well);
      }
    });
    fixedColumns.add(new WellColumn<AssayWellType>(SortByWellProperty.ASSAY_WELL_TYPE,
                                    "Assay Well Type",
                                    StringUtils.makeListString(StringUtils.wrapStrings(Arrays.asList(WellType.values()), "'", "'"), ", ").toLowerCase()) {
      @Override
      public AssayWellType getCellValue(Well well) { return well.getResultValues().values().iterator().next().getAssayWellType(); }
    });
    return fixedColumns;
  }

  private List<TableColumn<Well,?>> buildVariableColumns()
  {
    ArrayList<TableColumn<Well,?>> result = new ArrayList<TableColumn<Well,?>>();
    for (ResultValueType rvt : getResultValueTypes()) {
      if (rvt.isNumeric()) {
        result.add(new ResultValueTypeColumn<Double>(rvt));
      }
      else {
        result.add(new ResultValueTypeColumn<String>(rvt));
      }
    }
    return result;
  }
}
