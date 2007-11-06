// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $

// Copyright 2006 by the President and Fellows of Harvard College.

// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;

import org.hibernate.Session;

public class ScreenResultDataQuery implements Query
{
  private Well _well;
  private ResultValueType _resultValueType;
  private org.hibernate.Query _query;
  private Session _lastSession;

  public void setWell(Well well)
  {
    _well = well;
  }

  public void setResultValueType(ResultValueType resultValueType)
  {
    _resultValueType = resultValueType;
  }

  public org.hibernate.Query getQuery(Session session)
  {
    if (_query == null || session != _lastSession) {
      // note: surprisingly, query-per-resultValue is usually faster than query-per-resultValueType.
      // possibly because postgres query planner takes too long?  (assumes reusable prepared stmts are not being used)
      // or maybe postgres' in(...) operator is not well-optimized.  sometimes it's very fast (1 ms pery query), other times very slow (1 sec per query)
//    String hql = "select v from ResultValue v left join fetch v.resultValueType where v.well.id = :wellId and v.resultValueType in (:rvts)";
//    String hql = "select v from ResultValue v where v.well.id = :wellId and v.resultValueType.id in (:rvtIds)";
      String hql = "select v from ResultValue v where v.well.id = :wellId and v.resultValueType.id = :rvtId";
      _query = session.createQuery(hql);
      _query.setReadOnly(true);
      _lastSession = session;
    }
    _query.setParameter("wellId", _well.getWellId());
    _query.setParameter("rvtId", _resultValueType.getResultValueTypeId());
    return _query;
  }
}