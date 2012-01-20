// $HeadURL:
// http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/branches/iccbl/cross-screen-cmp/src/edu/harvard/med/screensaver/db/ScreenDAOImpl.java
// $
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

import edu.harvard.med.screensaver.db.Criterion.Operator;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screens.LabActivity;
import edu.harvard.med.screensaver.model.screens.ProjectPhase;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.Screening;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;

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
    String hql = "delete from AnnotationValue av " +
      "where av.annotationType in (select at from AnnotationType at where at.study = ?)";
    Query query = getHibernateSession().createQuery(hql);
    query.setEntity(0, finalStudy);
    int count = query.executeUpdate();
    log.info("Executed: " + hql + ", count: " + count);

    hql = "delete from AnnotationType at " +
      "where at.study = ?)";
    query = getHibernateSession().createQuery(hql);
    query.setEntity(0, finalStudy);
    count = query.executeUpdate();
    log.info("Executed: " + hql + ", count: " + count);

    // TODO: reimplement this in the proper (performant) HQL!
    log.info("delete the study: " + finalStudy.getFacilityId());
    String sql = "delete from study_reagent_link where study_id = :studyId";
    log.info("sql: " + sql);
    javax.persistence.Query sqlQuery = getEntityManager().createNativeQuery(sql);
    sqlQuery.setParameter("studyId", finalStudy.getScreenId());
    count = sqlQuery.executeUpdate();
    log.info("study_reagent_link updated: " + count);

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
    Long count = (Long) getHibernateSession().createQuery(hql).setEntity(0, screen).list().get(0);
    return count.intValue();
  }

  @Override
  public int countTotalPlatedLabCherryPicks(Screen screen)
  {
    String hql = "select count(*) " +
      "from Screen s " +
      "join s.cherryPickRequests cpr " +
      "join cpr.labCherryPicks lcp " +
      "join lcp.assayPlate cpap " +
      "join cpap.cherryPickLiquidTransfer cplt " +
      "where s = ? and cplt.status = 'Successful'";
    Long count = (Long) getHibernateSession().createQuery(hql).setEntity(0, screen).list().get(0);
    return count.intValue();
  }

  @Override
  public List<Screen> findRelatedScreens(final Screen screen)
  {
    if (screen.getProjectId() == null) {
      return Collections.emptyList();
    }
    List<Screen> result = _dao.runQuery(new edu.harvard.med.screensaver.db.Query<Screen>() {
      @Override
      public List<Screen> execute(Session session)
      {
        HqlBuilder hql = new HqlBuilder();
        hql.from(Screen.class, "s").
          where("s", "projectId", Operator.EQUAL, screen.getProjectId()).
          orderBy("s", "dateCreated");
        return hql.toQuery(session, true).list();
      }
    });
    return result;
  }
  
  @Override
  public List<Screen> findAllScreens()
  {
    return _dao.findEntitiesByHql(Screen.class, "from Screen s where s.projectPhase <> ? order by facilityId", ProjectPhase.ANNOTATION);
  }

  @Override
  public List<Screen> findAllStudies()
  {
    return _dao.findEntitiesByHql(Screen.class, "from Screen s where s.projectPhase = ? order by facilityId", ProjectPhase.ANNOTATION);
  }

  @Override
  public boolean isScreenFacilityIdUnique(final Screen screen)
  {
    List<Integer> screenIds = _dao.runQuery(new edu.harvard.med.screensaver.db.Query<Integer>() {
      @Override
      public List<Integer> execute(Session session)
      {
        HqlBuilder hql = new HqlBuilder().
          select("s", "id").
          from(Screen.class, "s").
          where("s", Screen.facilityId.getPropertyName(), Operator.EQUAL, screen.getFacilityId());
        if (!screen.isTransient()) {
          hql.where("s", "id", Operator.NOT_EQUAL, screen.getScreenId());
        }
        return hql.toQuery(session, true).list();
      }
    });

    if (screenIds.size() > 0) {
      if (!screenIds.get(0).equals(screen.getScreenId())) {
        return false;
      }
    }
    return true;
  }

  @Override
  public Set<ScreensaverUser> findLabActivityPerformedByCandidates(LabActivity a)
  {
    Set<ScreensaverUser> performedByCandidates = Sets.newTreeSet();
    if (a instanceof Screening) {
      Screen screen = _dao.reloadEntity(a.getScreen()); // note: we have to reload the associated screen for this method to work for a transient (new) activity
      performedByCandidates.addAll(screen.getAssociatedScreeningRoomUsers());
      // add the current performedBy user, even if it's no longer a valid candidate
      if (a.getPerformedBy() != null) {
        performedByCandidates.add(a.getPerformedBy());
      }
    }
    else {
      performedByCandidates.addAll(_dao.findAllEntitiesOfType(ScreensaverUser.class));
    }
    return performedByCandidates;
  }

  @Override
  public int countLoadedExperimentalWells(Screen screen)
  {
    String hql = "select count(*) " +
      "from ScreenResult sr join sr.assayWells aw join aw.libraryWell w " +
      "where sr.screen = :screen " +
      "and w.libraryWellType = 'experimental'";
    javax.persistence.Query query = getEntityManager().createQuery(hql);
    query.setParameter("screen", screen);
    return ((Long) query.getSingleResult()).intValue();
  }

  @Override
  public int populateStudyReagentLinkTable(final int screenId)
  {
    final int[] result = new int[1];
    _dao.runQuery(new edu.harvard.med.screensaver.db.Query() {
      public List<?> execute(Session session)
      {
        String sql =
          "insert into study_reagent_link " +
            "(study_id,reagent_id) " +
            "select :studyId as study_id, " +
            "reagent_id from " +
            "(select distinct(reagent_id) " +
            "from reagent " +
            "join annotation_value using(reagent_id) " +
            "join annotation_type using(annotation_type_id) " +
            "where study_id = :studyId ) a";

        log.debug("sql: " + sql);

        Query query = session.createSQLQuery(sql);
        query.setParameter("studyId", screenId);
        int rows = query.executeUpdate();
        if (rows == 0) {
          log.warn("No rows were updated: " +
            query.getQueryString());
        }
        log.info("study_reagent_link updated: " + rows);
        result[0] = rows;
        return null;
      }
    });
    return result[0];
  }
  
}
