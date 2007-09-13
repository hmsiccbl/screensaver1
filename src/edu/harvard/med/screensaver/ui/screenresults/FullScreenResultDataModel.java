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
import java.util.Map;

import edu.harvard.med.screensaver.db.ScreenResultsDAO;
import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;

import org.apache.log4j.Logger;

public class FullScreenResultDataModel extends ScreenResultDataModel
{
  // static members

  private static Logger log = Logger.getLogger(FullScreenResultDataModel.class);

  // instance data members

  // public constructors and methods

  public FullScreenResultDataModel(List<ResultValueType> resultValueTypes,
                                   int rowsToFetch,
                                   int sortColumnIndex,
                                   SortDirection sortDirection,
                                   ScreenResultsDAO dao)
  {
    super(resultValueTypes, rowsToFetch, sortColumnIndex, sortDirection, dao);
  }

  @Override
  protected Map<WellKey,List<ResultValue>> fetchData(int firstRowIndex, int rowsToFetch)
  {
    Map<WellKey,List<ResultValue>> rvData =
      _screenResultsDao.findSortedResultValueTableByRange(_resultValueTypes,
                                                          _sortColumnIndex,
                                                          _sortDirection,
                                                          firstRowIndex,
                                                          rowsToFetch,
                                                          null,
                                                          null);
    return rvData;
  }
}
