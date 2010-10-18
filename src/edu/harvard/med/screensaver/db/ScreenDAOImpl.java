// $HeadURL:
// http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/branches/iccbl/cross-screen-cmp/src/edu/harvard/med/screensaver/db/ScreenDAOImpl.java
// $
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.screens.ProjectPhase;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.ui.table.Criterion.Operator;

public class ScreenDAOImpl extends AbstractDAO implements ScreenDAO
{
  private static final Integer FIRST_SCREEN_NUMBER = 1;

  private static Logger log = Logger.getLogger(ScreenDAOImpl.class);

  private GenericEntityDAO _dao;


  /**
   * @motivation for CGLIB dynamic proxy creation
   */
  public ScreenDAOImpl()
  {}

  public ScreenDAOImpl(GenericEntityDAO dao)
  {
    _dao = dao;
  }
  
  /**
   * Quickly delete the study by first removing all of the AnnotationTypes and AnnotationValues manually.
   * Uses HQL.
   * Note: standard delete takes too long, as hibernate generates *many* delete statements.
   */
  public void deleteStudy(Screen study)
    throws DataModelViolationException
  {
    log.info("delete annotation values and and types for the study: " + study.getFacilityId());
    final Screen finalStudy = _dao.reloadEntity(study);

    // TODO: see if we can delete these using the entity delete
    getHibernateTemplate().execute(new HibernateCallback() {
      public Object doInHibernate(Session session) throws HibernateException, SQLException
  {
        String hqlStatement = "delete from AnnotationValue av " +
          "where av.annotationType in (select at from AnnotationType at where at.study = ?)";
        Query query = session.createQuery(hqlStatement);
        query.setEntity(0, finalStudy);
        int count = query.executeUpdate();
        log.info("Executed: " + hqlStatement + ", count: " + count);

        hqlStatement = "delete from AnnotationType at " +
          "where at.study = ?)";
        query = session.createQuery(hqlStatement);
        query.setEntity(0, finalStudy);
        count = query.executeUpdate();
        log.info("Executed: " + hqlStatement + ", count: " + count);


        return count;
            }
    });

    // TODO: reimplement this in the proper (performant) HQL!
    log.info("delete the study: " + finalStudy.getFacilityId());
    runQuery(new edu.harvard.med.screensaver.db.Query() {
      public List<?> execute(Session session)
      {
        String sql = "delete from study_reagent_link where study_id = :studyId";
        log.info("sql: " + sql);
        Query query = session.createSQLQuery(sql);
        query.setParameter("studyId", finalStudy.getScreenId());
        int rows = query.executeUpdate();
        if (rows == 0) {
          log.info("No rows were updated: " +
            query.getQueryString());
          }
        log.info("study_reagent_link updated: " + rows);
        return null;
            }
    });
    //study.getReagents().clear();
    //_dao.flush();
    _dao.deleteEntity(finalStudy);
      _dao.flush();
    log.info("study deleted");
    }

  @Override
  public int countScreenedExperimentalWells(Screen screen, boolean distinct)
  {
    String hql = "select count(" + (distinct ? "distinct " : "") + "w.id) " +
    		"from Well w, Screen s join s.assayPlates ap join ap.plateScreened p join ap.libraryScreening ls " +
    		"where s = ? and ap.replicateOrdinal = 0 " +
    		"and w.plateNumber = p.plateNumber and w.libraryWellType = 'experimental'";
    Long count = (Long) getHibernateTemplate().find(hql, screen).get(0);
    return count.intValue();
  }

  @Override
  public int countFulfilledLabCherryPicks(Screen screen)
  {
    String hql = "select count(*) " +
      "from Screen s " +
      "join s.cherryPickRequests cpr " +
      "join cpr.labCherryPicks lcp " +
      "join lcp.assayPlate cpap " +
      "join cpap.cherryPickLiquidTransfer cplt " +
      "where s = ? and cplt.status = 'Successful'";
    Long count = (Long) getHibernateTemplate().find(hql,screen).get(0);
    if (log.isDebugEnabled()) {
      log.debug("hql: " + hql + ", screen: " + screen + ", returns: " + count);
    }
    return count.intValue();
  }

  @Override
  public Screen findPrimaryScreen(final Screen screen)
  {
    List<Screen> result = _dao.runQuery(new edu.harvard.med.screensaver.db.Query<Screen>() {
      @Override
      public List<Screen> execute(Session session)
      {
        HqlBuilder hql = new HqlBuilder();
        hql.from(Screen.class, "s").
          where("s", "projectPhase", Operator.EQUAL, ProjectPhase.PRIMARY_SCREEN).
          where("s", "projectId", Operator.EQUAL, screen.getProjectId());
        return hql.toQuery(session, true).list();
      }
    });
    if (result.size() == 0) {
      return null;
    }
    return result.get(0);
  }
}
