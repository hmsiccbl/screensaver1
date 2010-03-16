// $HeadURL$
// $Id$

// Copyright 2006 by the President and Fellows of Harvard College.

// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.AssayWell;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.DataColumn;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.util.CollectionUtils;

import org.apache.commons.collections.Transformer;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

public class ScreenResultsDAOImpl extends AbstractDAO implements ScreenResultsDAO
{
  // static members

  private static Logger log = Logger.getLogger(ScreenResultsDAOImpl.class);

  public static final int SORT_BY_PLATE_WELL = -3;
  public static final int SORT_BY_WELL_PLATE = -2;
  public static final int SORT_BY_ASSAY_WELL_TYPE = -1;

  // instance data members

  // public constructors and methods

  /**
   * @motivation for CGLIB dynamic proxy creation
   */
  public ScreenResultsDAOImpl()
  {
  }

  public Map<WellKey,ResultValue> findResultValuesByPlate(final Integer plateNumber, final DataColumn col)
  {
    List<ResultValue> result = runQuery(new edu.harvard.med.screensaver.db.Query() {
      public List<?> execute(Session session)
      {
        String hql = "select r from ResultValue r where r.dataColumn.id = :colId and r.well.id >= :firstWellInclusive and r.well.id < :lastWellExclusive";
        Query query = session.createQuery(hql);
        query.setParameter("colId", col.getEntityId());
        query.setParameter("firstWellInclusive", new WellKey(plateNumber, 0, 0).toString());
        query.setParameter("lastWellExclusive", new WellKey(plateNumber + 1, 0, 0).toString());
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

  public void deleteScreenResult(final ScreenResult screenResultIn)
  {
    screenResultIn.getScreen().clearScreenResult();
    runQuery(new edu.harvard.med.screensaver.db.Query() 
    {
      public List execute(Session session)
      {
        ScreenResult screenResult = (ScreenResult) getHibernateTemplate().get(ScreenResult.class, screenResultIn.getEntityId());

        log.info("delete from screen_result_well_link");
        Query query = session.createSQLQuery("delete from screen_result_well_link where screen_result_id = :screenResultId" );
        query.setParameter("screenResultId", screenResult.getScreenResultId());
        int rows = query.executeUpdate();
        log.info("deleted " + rows + " rows from screen_result_well_link");
        
        log.info("delete Assay Wells");
        query = session.createQuery("delete AssayWell a where a.screenResult.id = :screenResultId");
        query.setParameter("screenResultId", screenResult.getScreenResultId());
        rows = query.executeUpdate();
        log.info("deleted " + rows + " AssayWells for " + screenResult);
        screenResult.getAssayWells().clear();
        
        log.info("delete ResultValues");
        int cumRows = 0;
        query = session.createQuery("delete ResultValue v where v.dataColumn.id = :col");
        for (DataColumn col : screenResult.getDataColumns()) {
          query.setParameter("col", col.getDataColumnId());
          rows = query.executeUpdate();
          cumRows += rows;
          log.debug("deleted " + rows + " result values for " + col);
          col.getResultValues().clear();
        }
        log.info("deleted a total of " + cumRows + " result values");
        //screenResult.getPlateNumbers().clear();
        // dissociate ScreenResult from Screen
        screenResult.getScreen().clearScreenResult();
        getHibernateTemplate().delete(screenResult);
        log.info("deleted " + screenResult);
        return null;
      }
    });
  }
  
  public AssayWell findAssayWell(ScreenResult screenResult, WellKey wellKey)
  {
    // TODO 
    return null;
  }

  public void populateScreenResultWellLinkTable(final int screenResultId)
    throws DataModelViolationException
  {
    // TODO: hack to avoid memory problems when building this with HibernateTemplate
    runQuery(new edu.harvard.med.screensaver.db.Query() 
    {
      public List execute(Session session)
      {
        String sql =  "insert into screen_result_well_link (screen_result_id, well_id) " +
                                "select :screenResultId  as screen_result_id," +
                      "well_id from (select distinct(well_id) from result_value " +
                                    "join data_column using(data_column_id) " +
                      "where screen_result_id = :screenResultId and well_id is not null) as well_id";
        
        
        log.debug("sql: " + sql);
        
        Query query = session.createSQLQuery(sql);
        query.setParameter("screenResultId", screenResultId );
        int rows = query.executeUpdate();
        if(rows == 0 )
        {
          throw new DataModelViolationException("No rows were updated: " + query.getQueryString() );
        }
        log.info("screen_result_well_link updated: " + rows );
        return null;
      }
    });    
  }
}
