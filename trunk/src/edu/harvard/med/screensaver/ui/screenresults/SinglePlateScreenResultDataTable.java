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

import javax.faces.model.DataModel;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.db.ScreenResultSortQuery;
import edu.harvard.med.screensaver.ui.libraries.WellViewer;
import edu.harvard.med.screensaver.ui.table.DataTableRowsPerPageUISelectOneBean;

import org.apache.log4j.Logger;

public class SinglePlateScreenResultDataTable extends ScreenResultDataTable
{
  // static members

  private static Logger log = Logger.getLogger(SinglePlateScreenResultDataTable.class);


  // instance data members

  private Integer _plateNumber;


  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected SinglePlateScreenResultDataTable()
  {
  }

  public SinglePlateScreenResultDataTable(ScreenResultViewer screenResultViewer,
                                          WellViewer wellViewer,
                                          LibrariesDAO librariesDao,
                                          GenericEntityDAO dao)
  {
    super(screenResultViewer, wellViewer, librariesDao, dao);
  }


  // public methods

  public Integer getPlateNumber()
  {
    return _plateNumber;
  }

  public void setPlateNumber(Integer plateNumber)
  {
    _plateNumber = plateNumber;
  }


  // abstract method implementations

  @Override
  protected DataTableRowsPerPageUISelectOneBean buildRowsPerPageSelector()
  {
    return new DataTableRowsPerPageUISelectOneBean(Arrays.asList(16, 24, 48, 96, 384));
  }

  @Override
  protected DataModel buildDataModel()
  {
    int totalRowCount = _dao.runQuery(new ScreenResultSortQuery(getScreenResult(), _plateNumber)).size();
    return new SinglePlateScreenResultDataModel(getScreenResult(),
                                                getResultValueTypes(),
                                                totalRowCount,
                                                getRowsPerPageSelector().getSelection(),
                                                getSortManager().getSortColumn(),
                                                getSortManager().getSortDirection(),
                                                _dao,
                                                _plateNumber);

  }



  // private methods

}

