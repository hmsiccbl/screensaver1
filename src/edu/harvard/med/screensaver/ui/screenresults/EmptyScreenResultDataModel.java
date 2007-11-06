// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.harvard.med.screensaver.db.ScreenResultSortQuery;
import edu.harvard.med.screensaver.db.SortDirection;

import org.apache.log4j.Logger;

public class EmptyScreenResultDataModel extends ScreenResultDataModel
{
  // static members

  private static final List<Map<String,Object>> EMPTY_RESULT = new ArrayList<Map<String,Object>>();
  private static final Map<String,Object> EMTPY_ROW = new HashMap<String,Object>(0);
  private static Logger log = Logger.getLogger(EmptyScreenResultDataModel.class);

  public EmptyScreenResultDataModel()
  {
    super(null, null, 0, 0, null, SortDirection.ASCENDING, null);
  }

  @Override
  ScreenResultSortQuery getScreenResultSortQuery()
  {
    return null;
  }
}
