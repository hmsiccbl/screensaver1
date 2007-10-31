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

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.ScreenResultSortQuery;
import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.ui.table.TableColumn;

import org.apache.log4j.Logger;
public class SinglePlateScreenResultDataModel extends ScreenResultDataModel
{
  // static members

  private static Logger log = Logger.getLogger(SinglePlateScreenResultDataModel.class);


  // instance data members

  private Map<WellKey,Well> _data;
  private Integer _size;
  private int _plateNumber;


  // public constructors and methods

  public SinglePlateScreenResultDataModel(ScreenResult screenResult,
                                          List<ResultValueType> resultValueTypes,
                                          TableColumn<Well> sortColumn,
                                          SortDirection sortDirection,
                                          GenericEntityDAO dao,
                                          int plateNumber)
  {
    super(screenResult, resultValueTypes, -1, -1, sortColumn, sortDirection, dao);
    _plateNumber = plateNumber;
  }

  @Override
  public int getRowCount()
  {
    if (_size == null) {
      if (_resultValueTypes.size() > 0) {
        _size = _dao.runQuery(new ScreenResultSortQuery(_resultValueTypes.get(0).getScreenResult(),
                                                        _plateNumber)).size();
      }
    }
    return _size;
  }
}

