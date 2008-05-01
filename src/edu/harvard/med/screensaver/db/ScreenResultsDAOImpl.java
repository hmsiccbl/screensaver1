// $HeadURL$
// $Id$

// Copyright 2006 by the President and Fellows of Harvard College.

// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.util.CollectionUtils;

import org.apache.commons.collections.Transformer;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

public class ScreenResultsDAOImpl extends AbstractDAO implements ScreenResultsDAO
{
  // static members

  private static Logger log = Logger.getLogger(ScreenResultsDAOImpl.class);

  public static int SORT_BY_PLATE_WELL = -3;
  public static int SORT_BY_WELL_PLATE = -2;
  public static int SORT_BY_ASSAY_WELL_TYPE = -1;

  // instance data members

  // public constructors and methods

  /**
   * @motivation for CGLIB dynamic proxy creation
   */
  public ScreenResultsDAOImpl()
  {
  }

  public Map<WellKey,ResultValue> findResultValuesByPlate(final Integer plateNumber, final ResultValueType rvt)
  {
    List<ResultValue> result = runQuery(new edu.harvard.med.screensaver.db.Query() {
      public List<?> execute(Session session)
      {
        String hql = "select r from ResultValue r where r.resultValueType.id = :rvtId and r.well.id >= :firstWell and r.well.id <= :lastWell";
        Query query = session.createQuery(hql);
        query.setParameter("rvtId", rvt.getEntityId());
        query.setParameter("firstWell", new WellKey(plateNumber, 0, 0).toString());
        query.setParameter("lastWell", new WellKey(plateNumber, 
                                                      Well.MAX_WELL_ROW - Well.MIN_WELL_ROW, 
                                                      Well.MAX_WELL_COLUMN - Well.MIN_WELL_COLUMN).toString());
        return query.list();
      }
    });
    Map<WellKey,ResultValue> result2 =
      CollectionUtils.indexCollection(result,
                                      // note: calling rv.getWell().getWellId() does *not* require a db hit, since a proxy can return its ID w/o forcing Hibernate to access the db;
                                      // so we use the id to instantiate the WellKey
                                      new Transformer() { public Object transform(Object rv) { return new WellKey(((ResultValue) rv).getWell().getWellId()); } },
                                      WellKey.class, ResultValue.class);
    return result2;
  }

  public void deleteScreenResult(final ScreenResult screenResult)
  {
    // disassociate ScreenResult from Screen
    screenResult.getScreen().clearScreenResult();

//    getHibernateTemplate().execute(new HibernateCallback() {
//      public Object doInHibernate(Session session)
//        throws HibernateException, SQLException
//      {
//        Query query = session.createQuery("delete ResultValue v where v.resultValueType.id = :rvt");
//        for (ResultValueType rvt : screenResult.getResultValueTypes()) {
//          query.setParameter("rvt", rvt.getResultValueTypeId());
//          int rows = query.executeUpdate();
//          log.debug("deleted " + rows + " result values for " + rvt);
//          rvt.setResultValues(Collections.<WellKey,ResultValue>emptyMap());
//          //rvt.getResultValues().clear();
//        }
//        return null;
//      }
//    });
    getHibernateTemplate().delete(screenResult);
    log.debug("deleted " + screenResult);
  }
}
