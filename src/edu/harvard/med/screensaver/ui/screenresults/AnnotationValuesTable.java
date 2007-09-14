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
import java.util.List;
import java.util.Map;

import javax.faces.model.DataModel;

import edu.harvard.med.screensaver.db.AnnotationsDAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.ui.libraries.WellViewer;
import edu.harvard.med.screensaver.ui.table.DataTable;
import edu.harvard.med.screensaver.ui.table.DataTableRowsPerPageUISelectOneBean;
import edu.harvard.med.screensaver.ui.table.TableColumn;

import org.apache.log4j.Logger;

public class AnnotationValuesTable extends DataTable
{

  // static members

  private static Logger log = Logger.getLogger(AnnotationValuesTable.class);

  public static final String VENDOR_ID_COLUMN_NAME = "Vendor ID";


  // instance data members

  private AnnotationsDAO _annotationsDao;
  private GenericEntityDAO _dao;
  private WellViewer _vendorProductViewer;

  private List<AnnotationType> _annotationTypes;


  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected AnnotationValuesTable()
  {
  }

  public AnnotationValuesTable(GenericEntityDAO dao,
                               AnnotationsDAO annotationsDao,
                               WellViewer wellViewer)
  {
    _dao = dao;
    _annotationsDao = annotationsDao;
    _vendorProductViewer = wellViewer;
  }


  // public methods

  @Override
  protected List<TableColumn<Map<String,Object>>> buildColumns()
  {
    List<TableColumn<Map<String,Object>>> columns = new ArrayList<TableColumn<Map<String,Object>>>();
    columns.add(new TableColumn<Map<String,Object>>(VENDOR_ID_COLUMN_NAME, "The vendor-assigned identifier") {
      @Override
      public Object getCellValue(Map<String,Object> row) { return row.get(getName()); }
    });
    for (AnnotationType annotationType : _annotationTypes) {
      columns.add(new TableColumn<Map<String,Object>>(annotationType.getName(),
                                                      annotationType.getDescription(),
                                                      annotationType.isNumeric()) {
        @Override
        public Object getCellValue(Map<String,Object> row) { return row.get(getName()); }
      });
    }
    return columns;
  }

  @Override
  protected DataModel buildDataModel()
  {
    final int[] totalRows = { 0 };
    if (_annotationTypes.size() > 0) {
      _dao.doInTransaction(new DAOTransaction() {
        public void runTransaction() {
          AnnotationType annotationType = _dao.reloadEntity(_annotationTypes.get(0), true);
          totalRows[0] = _dao.relationshipSize(annotationType.getAnnotationValues());
        }
      });
    }

    return new AnnotationValuesDataModel(_annotationTypes,
                                         getRowsPerPageSelector().getSelection(),
                                         totalRows[0],
                                         getSortManager().getSortColumnIndex(),
                                         getSortManager().getSortDirection(),
                                         _annotationsDao);
  }

  @Override
  protected List<Integer> getRowsPerPageSelections()
  {
    return Arrays.asList(10, 20, 50, 100);
  }


  // instance data members

  public void setAnnotationTypes(List<AnnotationType> annotationTypes)
  {
    _annotationTypes = annotationTypes;
    rebuildColumnsAndRows();
  }

  // private methods

}

