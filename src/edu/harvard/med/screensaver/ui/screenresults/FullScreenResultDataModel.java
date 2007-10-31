// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults;

import java.util.List;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.ui.table.TableColumn;

import org.apache.log4j.Logger;

public class FullScreenResultDataModel extends ScreenResultDataModel
{
  // static members

  private static Logger log = Logger.getLogger(FullScreenResultDataModel.class);

  // instance data members

  // public constructors and methods

  public FullScreenResultDataModel(ScreenResult screenResult,
                                   List<ResultValueType> resultValueTypes,
                                   int totalRows,
                                   int rowsToFetch,
                                   TableColumn<Well> sortColumn,
                                   SortDirection sortDirection,
                                   GenericEntityDAO dao)
  {
    super(screenResult,
          resultValueTypes,
          totalRows,
          rowsToFetch,
          sortColumn,
          sortDirection,
          dao);
  }
}
