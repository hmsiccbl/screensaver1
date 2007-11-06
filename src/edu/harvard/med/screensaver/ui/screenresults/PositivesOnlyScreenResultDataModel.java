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
import edu.harvard.med.screensaver.db.ScreenResultSortQuery;
import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.ui.table.TableColumn;

import org.apache.log4j.Logger;

public class PositivesOnlyScreenResultDataModel extends ScreenResultDataModel
{
  // static members

  private static Logger log = Logger.getLogger(PositivesOnlyScreenResultDataModel.class);


  // instance data members

  private ResultValueType _positivesOnlyRvt;


  // public constructors and methods

  public PositivesOnlyScreenResultDataModel(ScreenResult screenResult,
                                            List<ResultValueType> resultValueTypes,
                                            int rowsToFetch,
                                            TableColumn<Well> sortColumn,
                                            SortDirection sortDirection,
                                            GenericEntityDAO dao,
                                            ResultValueType positivesOnlyRvt)
  {
    super(screenResult,
          resultValueTypes,
          positivesOnlyRvt.getPositivesCount() == null ? 0 : positivesOnlyRvt.getPositivesCount(),
          rowsToFetch,
          sortColumn,
          sortDirection,
          dao);
    _positivesOnlyRvt = positivesOnlyRvt;
  }


  @Override
  protected ScreenResultSortQuery getScreenResultSortQuery()
  {
    return new ScreenResultSortQuery(_screenResult, _positivesOnlyRvt);
  }
}

