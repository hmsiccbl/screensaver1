// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
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
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.ui.searchresults.SortDirection;
import edu.harvard.med.screensaver.ui.util.TableSortManager;

import org.apache.log4j.Logger;

public class HitsOnlyScreenResultDataModel extends ScreenResultDataModel
{
  // static members

  private static Logger log = Logger.getLogger(HitsOnlyScreenResultDataModel.class);
  private Map<WellKey,List<ResultValue>> _data;
  private ResultValueType _hitsOnlyRvt;

  
  // instance data members

  // public constructors and methods
  
  public HitsOnlyScreenResultDataModel(ScreenResult screenResult,
                                          TableSortManager sortManager,
                                          List<ResultValueType> selectedResultValueTypes,
                                          ScreenResultsDAO dao,
                                          ResultValueType hitsOnlyRvt)
  {
    super(screenResult, sortManager, selectedResultValueTypes, dao);
    _hitsOnlyRvt = hitsOnlyRvt;
  }


  // protected methods

  @Override
  protected Map<WellKey,List<ResultValue>> fetchData(List<ResultValueType> selectedResultValueTypes,
                                                   int sortBy,
                                                   SortDirection sortDirection)
  {
    _data = _screenResultsDao.findSortedResultValueTableByRange(_selectedResultValueTypes,
                                                                sortBy,
                                                                sortDirection,
                                                                0,
                                                                null,
                                                                _hitsOnlyRvt,
                                                                null);
    return _data;
  }

  // private methods

}
