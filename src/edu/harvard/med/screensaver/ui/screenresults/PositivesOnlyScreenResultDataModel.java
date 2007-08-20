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
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;

import org.apache.log4j.Logger;

public class PositivesOnlyScreenResultDataModel extends ScreenResultDataModel
{
  // static members

  private static Logger log = Logger.getLogger(PositivesOnlyScreenResultDataModel.class);
  private Map<WellKey,List<ResultValue>> _data;
  private ResultValueType _positivesOnlyRvt;

  
  // instance data members

  // public constructors and methods
  
  public PositivesOnlyScreenResultDataModel(ScreenResult screenResult,
                                       List<ResultValueType> resultValueTypes,
                                       int sortColumnIndex,
                                       SortDirection sortDirection,
                                       ScreenResultsDAO dao,
                                       ResultValueType positivesOnlyRvt)
  {
    super(screenResult, resultValueTypes, sortColumnIndex, sortDirection, dao);
    _positivesOnlyRvt = positivesOnlyRvt;
  }


  // protected methods

  @Override
  protected Map<WellKey,List<ResultValue>> fetchData(List<ResultValueType> selectedResultValueTypes,
                                                   int sortBy,
                                                   SortDirection sortDirection)
  {
    _data = _screenResultsDao.findSortedResultValueTableByRange(_resultValueTypes,
                                                                sortBy,
                                                                sortDirection,
                                                                0,
                                                                null,
                                                                _positivesOnlyRvt,
                                                                null);
    return _data;
  }

  // private methods

}

