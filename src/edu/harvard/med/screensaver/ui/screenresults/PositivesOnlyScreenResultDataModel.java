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

public class PositivesOnlyScreenResultDataModel extends ScreenResultDataModel
{
  // static members

  private static Logger log = Logger.getLogger(PositivesOnlyScreenResultDataModel.class);


  // instance data members

  private ResultValueType _positivesOnlyRvt;


  // public constructors and methods

  public PositivesOnlyScreenResultDataModel(List<ResultValueType> resultValueTypes,
                                            int sortColumnIndex,
                                            SortDirection sortDirection,
                                            ScreenResultsDAO dao,
                                            ResultValueType positivesOnlyRvt)
  {
    super(resultValueTypes,
          positivesOnlyRvt.getPositivesCount(),
          -1,
          sortColumnIndex,
          sortDirection,
          dao);
    _positivesOnlyRvt = positivesOnlyRvt;
  }


  // protected methods

  @Override
  protected Map<WellKey,List<ResultValue>> fetchData(int firstRowIndex, int rowsToFetch)
  {
    return _screenResultsDao.findSortedResultValueTableByRange(_resultValueTypes,
                                                               _sortColumnIndex,
                                                               _sortDirection,
                                                                0,
                                                                null,
                                                                _positivesOnlyRvt,
                                                                null);
  }

  // private methods

}

