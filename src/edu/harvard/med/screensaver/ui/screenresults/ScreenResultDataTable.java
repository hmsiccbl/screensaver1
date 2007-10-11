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
import java.util.Map;

import javax.faces.component.UIInput;

import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.db.ScreenResultsDAO;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellType;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.ui.libraries.WellViewer;
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
public abstract class ScreenResultDataTable extends DataTable<Map<String,Object>>
{
  // static members

  private static Logger log = Logger.getLogger(ScreenResultDataTable.class);

  // instance data members

  private ScreenResultViewer _screenResultViewer;
  private WellViewer _wellViewer;
  private LibrariesDAO _librariesDao;
  protected ScreenResultsDAO _screenResultsDao;

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
                               ScreenResultsDAO screenResultsDao)
  {
    _screenResultViewer = screenResultViewer;
    _wellViewer = wellViewer;
    _librariesDao = librariesDao;
    _screenResultsDao = screenResultsDao;
  }


  // public constructors and methods

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
    return ((ScreenResultDataModel) getDataModel()).isResultValueCellExcluded(getSortManager().getCurrentColumnIndex());
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

  protected List<TableColumn<Map<String,Object>>> buildColumns()
  {
    List<TableColumn<Map<String,Object>>> columns = new ArrayList<TableColumn<Map<String,Object>>>();
    columns.addAll(buildFixedColumns());
    columns.addAll(buildVariableColumns());
    return columns;
  }


  // private methods

  private List<TableColumn<Map<String,Object>>> buildFixedColumns()
  {
    List<TableColumn<Map<String,Object>>> fixedColumns = new ArrayList<TableColumn<Map<String,Object>>>(3);
    fixedColumns.add(new TableColumn<Map<String,Object>>("Plate", "The plate number", true) {
      @Override
      public Object getCellValue(Map<String,Object> row) { return row.get(getName()); }
    });
    fixedColumns.add(new TableColumn<Map<String,Object>>("Well", "The well name") {
      @Override
      public Object getCellValue(Map<String,Object> row) { return row.get(getName()); }

      @Override
      public boolean isCommandLink() { return true; }

      @Override
      public Object cellAction(Map<String,Object> row)
      {
        Integer plateNumber = (Integer) getSortManager().getColumn(0).getCellValue(row);
        String wellName = (String) getSortManager().getColumn(1).getCellValue(row);
        Well well = _librariesDao.findWell(new WellKey(plateNumber, wellName));
        return _wellViewer.viewWell(well);
      }
    });
    fixedColumns.add(new TableColumn<Map<String,Object>>("Type",
      StringUtils.makeListString(StringUtils.wrapStrings(Arrays.asList(WellType.values()), "'", "'"), ", ").toLowerCase()) {
      @Override
      public Object getCellValue(Map<String,Object> row) { return row.get(getName()); }
    });
    return fixedColumns;
  }

  private List<TableColumn<Map<String,Object>>> buildVariableColumns()
  {
    ArrayList<TableColumn<Map<String,Object>>> result = new ArrayList<TableColumn<Map<String,Object>>>();
    for (MetaDataType rvt : getResultValueTypes()) {
      result.add(new TableColumn<Map<String,Object>>(rvt.getName(), rvt.getDescription(), rvt.isNumeric()) {
        @Override
        public Object getCellValue(Map<String,Object> row)
        {
          return row.get(getName());
        }
      });
    }
    return result;
  }
}
