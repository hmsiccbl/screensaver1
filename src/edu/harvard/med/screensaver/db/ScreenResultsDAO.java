// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.ui.searchresults.SortDirection;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

public interface ScreenResultsDAO
{
  public static int SORT_BY_PLATE_WELL = -3;
  public static int SORT_BY_WELL_PLATE = -2;
  public static int SORT_BY_ASSAY_WELL_TYPE = -1;
  
  public Map<WellKey,ResultValue> findResultValuesByPlate(Integer plateNumber, ResultValueType rvt);

  public Map<WellKey,List<ResultValue>> findSortedResultValueTableByRange(final List<ResultValueType> selectedRvts,
                                                                          final int sortBy,
                                                                          final SortDirection sortDirection,
                                                                          final int fromIndex,
                                                                          final int rowsToFetch,
                                                                          final ResultValueType hitsOnlyRvt);
  public void deleteScreenResult(ScreenResult screenResult);
}

