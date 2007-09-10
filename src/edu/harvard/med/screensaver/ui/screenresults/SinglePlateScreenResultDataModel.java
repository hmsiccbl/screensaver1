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
public class SinglePlateScreenResultDataModel extends ScreenResultDataModel
{
  // static members

  private static Logger log = Logger.getLogger(SinglePlateScreenResultDataModel.class);
  private Map<WellKey,List<ResultValue>> _data;
  private int _plateNumber;

  // instance data members

  // public constructors and methods

  public SinglePlateScreenResultDataModel(List<ResultValueType> resultValueTypes,
                                          int sortColumnIndex,
                                          SortDirection sortDirection,
                                          ScreenResultsDAO dao,
                                          int plateNumber)
  {
    super(resultValueTypes, sortColumnIndex, sortDirection, dao);
    _plateNumber = plateNumber;
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
                                                                null,
                                                                _plateNumber);
    return _data;
  }


  // private methods

}

