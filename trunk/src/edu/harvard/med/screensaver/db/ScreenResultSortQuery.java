// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

public class ScreenResultSortQuery implements edu.harvard.med.screensaver.db.Query
{
  private static final Logger log = Logger.getLogger(ScreenResultSortQuery.class);

  public enum SortByWellProperty {
    PLATE_NUMBER,
    WELL_NAME,
    ASSAY_WELL_TYPE;
  };

  // filtering criteria
  private ScreenResult _screenResult;
  private ResultValueType _positivesOnlyRvt;
  private Integer _plateNumber;

  // sorting criteria
  private SortByWellProperty _sortByWellProperty;
  private ResultValueType _sortByResultValueType;
  private Query _query;
  private HqlBuilder _hql = new HqlBuilder();


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

  public Query getQuery(Session session)
  {
    if (_sortByWellProperty == null && _sortByResultValueType == null) {
      _sortByWellProperty = SortByWellProperty.PLATE_NUMBER;
    }

    _hql.select("w", "id").from(ScreenResult.class, "sr").from("sr", "wells", "w").where("sr", _screenResult);

    if (_sortByWellProperty != null) {
      if (_sortByWellProperty == SortByWellProperty.PLATE_NUMBER) {
        _hql.orderBy("w", "id");
      }
      else if (_sortByWellProperty == SortByWellProperty.WELL_NAME) {
        _hql.orderBy("w", "wellName").orderBy("w", "plateNumber");
      }
      else if (_sortByWellProperty == SortByWellProperty.ASSAY_WELL_TYPE) {
        _hql.orderBy("w", "wellType").orderBy("w", "id");
      }
      else {
        throw new IllegalArgumentException("unhandled well property ordering: " + _sortByWellProperty);
      }
    }
    else if (_sortByResultValueType != null) {
      _hql.from("w", "resultValues", "v");
      _hql.where("v", "resultValueType.id", _sortByResultValueType.getResultValueTypeId());
      if (_sortByResultValueType.isNumeric()) {
        _hql.orderBy("v", "numericValue").orderBy("w", "id");
      }
      else {
        _hql.orderBy("v", "value").orderBy("w", "id");
      }
    }
    addFilterRestriction();
    if (log.isDebugEnabled()) {
      log.debug(_hql.hql());
    }
    _query = session.createQuery(_hql.hql());
    for (int i = 0; i < _hql.args().size(); i++) {
      _query.setParameter(i, _hql.arg(i));
    }
    return _query;
  }


  private void addFilterRestriction()
  {
    if (_plateNumber != null) {
      _hql.where("w", "plateNumber", _plateNumber);
    }
    else if (_positivesOnlyRvt != null) {
      _hql.from("w", "resultValues", "pv");
      _hql.where("pv", "resultValueType.id", _positivesOnlyRvt.getResultValueTypeId());
      _hql.where("pv", "positive", true);
    }
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
