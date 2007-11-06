// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults;

import java.util.Arrays;
import java.util.List;

import javax.faces.model.DataModel;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.ui.libraries.WellViewer;
import edu.harvard.med.screensaver.ui.table.DataTableRowsPerPageUISelectOneBean;

import org.apache.log4j.Logger;

public class FullScreenResultDataTable extends ScreenResultDataTable
{
  // static members

  private static Logger log = Logger.getLogger(FullScreenResultDataTable.class);


  // instance data members


  // abstract & template method implementations

  @Override
  protected DataTableRowsPerPageUISelectOneBean buildRowsPerPageSelector()
  {
    return new DataTableRowsPerPageUISelectOneBean(Arrays.asList(10, 24, 48, 96));
  }

  @Override
  protected DataModel buildDataModel()
  {
    final List<ResultValueType> resultValueTypes = getResultValueTypes();
    int totalRows = findTotalRows(resultValueTypes);
    return new FullScreenResultDataModel(getScreenResult(),
                                         resultValueTypes,
                                         totalRows,
                                         getRowsPerPage(),
                                         getSortManager().getSortColumn(),
                                         getSortManager().getSortDirection(),
                                         _dao);
  }

  private int findTotalRows(final List<ResultValueType> resultValueTypes)
  {
    final int[] totalRows = new int[1];
    if (resultValueTypes != null && resultValueTypes.size() > 0) {
      _dao.doInTransaction(new DAOTransaction() {
        public void runTransaction() {
          ResultValueType rvt = resultValueTypes.get(0);
          rvt = _dao.reloadEntity(rvt, true);
          totalRows[0] = _dao.relationshipSize(rvt, "resultValues");
        }
      });
    }
    return totalRows[0];
  }

  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected FullScreenResultDataTable()
  {
  }

  public FullScreenResultDataTable(ScreenResultViewer screenResultViewer,
                                   WellViewer wellViewer,
                                   LibrariesDAO librariesDao,
                                   GenericEntityDAO dao)
  {
    super(screenResultViewer, wellViewer, librariesDao, dao);
  }


  // public methods

  // private methods

}

