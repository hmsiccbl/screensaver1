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

import edu.harvard.med.screensaver.ui.table.DataTableRowsPerPageUISelectOneBean;

import org.apache.log4j.Logger;

public class EmptyScreenResultDataTable extends ScreenResultDataTable
{
  // static members

  private static Logger log = Logger.getLogger(EmptyScreenResultDataTable.class);


  // abstract method implementations

  public EmptyScreenResultDataTable(ScreenResultViewer screenResultViewer)
  {
    super(screenResultViewer,
          null,
          null,
          null);
  }

  protected List<Integer> getRowsPerPageSelections()
  {
    return Arrays.asList(0);
  }

  // abstract method implementations

  @Override
  protected DataModel buildDataModel()
  {
    return new EmptyScreenResultDataModel();
  }

  @Override
  protected DataTableRowsPerPageUISelectOneBean buildRowsPerPageSelector()
  {
    return new DataTableRowsPerPageUISelectOneBean(Arrays.asList(0));
  }


  // instance data members

  // public constructors and methods

  // private methods

}

