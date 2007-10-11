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

import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.db.ScreenResultsDAO;
import edu.harvard.med.screensaver.ui.libraries.WellViewer;
import edu.harvard.med.screensaver.ui.table.DataTableRowsPerPageUISelectOneBean;

import org.apache.log4j.Logger;

public class FullScreenResultDataTable extends ScreenResultDataTable
{
  // static members

  private static Logger log = Logger.getLogger(FullScreenResultDataTable.class);


  // instance data members

  private int _screenResultSize;


  // abstract & template method implementations

  @Override
  protected DataTableRowsPerPageUISelectOneBean buildRowsPerPageSelector()
  {
    return new DataTableRowsPerPageUISelectOneBean(Arrays.asList(16, 24, 48, 96, 384));
  }

  @Override
  protected DataModel buildDataModel()
  {
    return new FullScreenResultDataModel(getResultValueTypes(),
                                         getRowsPerPageSelector().getSelection(),
                                         getSortManager().getSortColumnIndex(),
                                         getSortManager().getSortDirection(),
                                         _screenResultsDao);
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
                                   ScreenResultsDAO screenResultsDao)
  {
    super(screenResultViewer, wellViewer, librariesDao, screenResultsDao);
  }


  // public methods

  // private methods

}

