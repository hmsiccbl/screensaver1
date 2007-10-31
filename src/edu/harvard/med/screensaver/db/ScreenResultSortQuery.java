// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.util.ArrayList;
import java.util.List;

import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;

import org.hibernate.Query;
import org.hibernate.Session;

public class ScreenResultSortQuery implements edu.harvard.med.screensaver.db.Query
{
  public enum SortByWellProperty {
    PLATE_NUMBER,
    WELL_NAME,
    WELL_TYPE;
  };

  // filtering criteria
  private ScreenResult _screenResult;
  private ResultValueType _positivesOnlyRvt;
  private Integer _plateNumber;

  // sorting criteria
  private SortByWellProperty _sortByWellProperty;
  private ResultValueType _sortByResultValueType;

  public ScreenResultSortQuery(ScreenResult screenResult,
                               ResultValueType positivesOnlyRvt)
  {
    _screenResult = screenResult;
    _positivesOnlyRvt = positivesOnlyRvt;
  }

  public ScreenResultSortQuery(ScreenResult screenResult,
                               Integer plateNumber)
  {
    _screenResult = screenResult;
    _plateNumber = plateNumber;
  }

  public ScreenResultSortQuery(ScreenResult screenResult)
  {
    _screenResult = screenResult;
  }

  public Query buildQuery(Session session)
  {
    String hql = "select w.id from ScreenResult sr join sr.wells w where sr = ? order by w.id";
    List<Object> args = new ArrayList<Object>();
    args.add(_screenResult);
    Query query = session.createQuery(hql);
    for (int i = 0; i < args.size(); i++) {
      query.setParameter(i, args.get(i));
    }
    return query;
  }


  public ResultValueType getPositivesOnlyRvt()
  {
    return _positivesOnlyRvt;
  }

  public Integer getPlateNumber()
  {
    return _plateNumber;
  }

  public SortByWellProperty getSortByWellProperty()
  {
    return _sortByWellProperty;
  }

  public ResultValueType getSortByResultValueType()
  {
    return _sortByResultValueType;
  }

  public void setSortByWellProperty(SortByWellProperty sortByWellProperty)
  {
    _sortByWellProperty = sortByWellProperty;
  }

  public void setSortByResultValueType(ResultValueType sortByResultValueType)
  {
    _sortByResultValueType = sortByResultValueType;
  }
}
