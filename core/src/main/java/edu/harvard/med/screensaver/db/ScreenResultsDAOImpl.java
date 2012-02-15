// $HeadURL$
// $Id$

// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.

// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections.Transformer;
import org.apache.log4j.Logger;
import org.hibernate.CacheMode;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import edu.harvard.med.screensaver.db.Criterion.Operator;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.db.hqlbuilder.JoinType;
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.libraries.LibraryWellType;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.AnnotationValue;
import edu.harvard.med.screensaver.model.screenresults.AssayWell;
import edu.harvard.med.screensaver.model.screenresults.DataColumn;
import edu.harvard.med.screensaver.model.screenresults.DataType;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.util.CollectionUtils;

public class ScreenResultsDAOImpl extends AbstractDAO implements ScreenResultsDAO
{
  private static Logger log = Logger.getLogger(ScreenResultsDAOImpl.class);

  private GenericEntityDAO _dao;

  /**
   * @motivation for CGLIB dynamic proxy creation
   */
  public ScreenResultsDAOImpl()
  {}

  public ScreenResultsDAOImpl(GenericEntityDAO dao)
  {
    _dao = dao;
  }

	public Map<WellKey, ResultValue> findResultValuesByPlate(final Integer plateNumber, final DataColumn col) {
		List<ResultValue> result = runQuery(new edu.harvard.med.screensaver.db.Query() {
			public List<?> execute(Session session) {
				// NOTE: added fetch of the library into the session to fix lazy update problem when calling rv.isEdgeWell()->well.isEdgeWell() which needs the library see: [#1376]- sde4
				String hql = "select r from ResultValue r " + "left join fetch r.well w join fetch w.library l "
						+ "where r.dataColumn.id = :colId and w.id >= :firstWellInclusive " + "and w.id < :lastWellExclusive";
				Query query = session.createQuery(hql);
				query.setParameter("colId", col.getEntityId());
				query.setParameter("firstWellInclusive", new WellKey(plateNumber, 0, 0).toString());
				query.setParameter("lastWellExclusive", new WellKey(plateNumber + 1, 0, 0).toString());
				return query.list();
			}
		});
		Map<WellKey, ResultValue> result2 = CollectionUtils.indexCollection(result,
		// note: calling rv.getWell().getWellId() does *not* require a db hit, since
		// a proxy can return its ID w/o forcing Hibernate to access the db;
		// so we use the id to instantiate the WellKey
				new Transformer() {
					public Object transform(Object rv) {
						return new WellKey(((ResultValue) rv).getWell().getWellId());
					}
				}, WellKey.class, ResultValue.class);
		return result2;
	}

  public List<DataColumn> findMutualPositiveColumns(final ScreenResult screenResult)
  {
    return findMutualPositiveColumns(new HqlBuilderCallback() {
      @Override
      public void apply(HqlBuilder hql)
      {
        hql.where("aw1", "screenResult", Operator.EQUAL, screenResult).
          where("aw2", "screenResult", Operator.NOT_EQUAL, screenResult); // ignore data columns from the screen result passed in
      }
    });
  }

  public List<DataColumn> findMutualPositiveColumns(final HqlBuilderCallback hqlBuilderCallback)
  {

    edu.harvard.med.screensaver.db.Query query = new edu.harvard.med.screensaver.db.Query() {
      public List<?> execute(Session session)
      {
        HqlBuilder hql = new HqlBuilder().
          select("dc").distinctProjectionValues().
          from(AssayWell.class, "aw1").
          from(AssayWell.class, "aw2").
          from("aw2", AssayWell.screenResult, "sr2", JoinType.INNER).
          from("sr2", ScreenResult.dataColumns, "dc", JoinType.INNER).
          where("aw1", "libraryWell", Operator.EQUAL, "aw2", "libraryWell").
          where("aw1", "positive", Operator.EQUAL, Boolean.TRUE).
          where("aw2", "positive", Operator.EQUAL, Boolean.TRUE).
          whereIn("dc", "dataType", Sets.newHashSet(DataType.POSITIVE_INDICATOR_BOOLEAN, DataType.POSITIVE_INDICATOR_PARTITION));
        hqlBuilderCallback.apply(hql);
        return hql.toQuery(session, true).list();
      }
    };

    return runQuery(query);
  }

  public void deleteScreenResult(ScreenResult screenResultIn)
  {
    Session session = getHibernateSession();
    ScreenResult screenResult = (ScreenResult) session.get(ScreenResult.class, screenResultIn.getEntityId());

    log.info("delete Assay Wells");
    Query query = session.createQuery("delete AssayWell a where a.screenResult.id = :screenResultId");
    query.setParameter("screenResultId", screenResult.getScreenResultId());
    int rows = query.executeUpdate();
    log.info("deleted " + rows + " AssayWells for " + screenResult);
    screenResult.getAssayWells().clear();

    log.info("delete Assay Plates to be orphaned (i.e., without library screenings)");
    query = session.createQuery("delete AssayPlate ap where ap.screen.id = :screenId and ap.libraryScreening is null");
    query.setParameter("screenId", screenResult.getScreen().getScreenId());
    rows = query.executeUpdate();
    log.info("deleted " + rows + " AssayPlates for " + screenResult);

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
    session.delete(screenResult);
    flush();
    log.info("deleted " + screenResult);
  }

  //TODO: move to report class
  public int createScreenedReagentCounts(final ScreenType screenType,
                                          Screen study,
                                          AnnotationType positiveAnnotationType,
                                          AnnotationType overallAnnotationType)
  {
    // Break this into two separate queries because of Hibernate bug (http://opensource.atlassian.com/projects/hibernate/browse/HHH-1615):
    // when using the "group by" clause with a full object (as opposed to an attribute of the object/table),
    // Hibernate is requiring that every attribute of the object be specified in a "group by" and not 
    // just the object itself.  so the workaround is to query once to get the id's then once again to 
    // get the objects.

    log.info("1. get the reagent id's for the positive counts");
    ScrollableResults sr = runScrollQuery(new edu.harvard.med.screensaver.db.ScrollQuery() {
      public ScrollableResults execute(Session session)
            {
        HqlBuilder builder = new HqlBuilder();
        builder.select("r", "id").
          selectExpression("count(*)").
          from(AssayWell.class, "aw").
                from("aw", AssayWell.libraryWell, "w", JoinType.INNER).
                from("w", Well.latestReleasedReagent, "r", JoinType.INNER).
                from("w", Well.library, "l", JoinType.INNER).
          where("l", "screenType", Operator.EQUAL, screenType).
          where("w", "libraryWellType", Operator.EQUAL, LibraryWellType.EXPERIMENTAL);
        builder.where("aw", "positive", Operator.EQUAL, Boolean.TRUE);
        builder.groupBy("r", "id");
        log.debug("hql: " + builder.toHql());
        return builder.toQuery(session, true).setCacheMode(CacheMode.IGNORE).
          scroll(ScrollMode.FORWARD_ONLY);
      }
    });

    Map<Integer,Long> positivesMap = Maps.newHashMap();
    while (sr.next()) {
      Object[] row = sr.get();
      positivesMap.put((Integer) row[0], (Long) row[1]);
    }

    log.info("2. get the reagent id's for the overall counts");
    sr = runScrollQuery(new edu.harvard.med.screensaver.db.ScrollQuery() {
      public ScrollableResults execute(Session session)
            {
        HqlBuilder builder = new HqlBuilder();
        builder.select("r", "id").
                selectExpression("count(*)").
                from(AssayWell.class, "aw").
                from("aw", AssayWell.libraryWell, "w", JoinType.INNER).
                from("w", Well.library, "l", JoinType.INNER).
                from("w", Well.latestReleasedReagent, "r", JoinType.INNER).
                where("l", "screenType", Operator.EQUAL, screenType).
                where("w", "libraryWellType", Operator.EQUAL, LibraryWellType.EXPERIMENTAL).
                groupBy("r", "id");
        log.debug("hql: " + builder.toHql());
        return builder.toQuery(session, true).setCacheMode(CacheMode.IGNORE).
          scroll(ScrollMode.FORWARD_ONLY);
      }
    });

    Map<Integer,Long> overallMap = Maps.newHashMap();
    while (sr.next()) {
      Object[] row = sr.get();
      overallMap.put((Integer) row[0], (Long) row[1]);
    }

    log.info("3. get the Reagents");
    sr = runScrollQuery(new edu.harvard.med.screensaver.db.ScrollQuery() {
      public ScrollableResults execute(Session session)
            {
        HqlBuilder builder = new HqlBuilder();
        builder.select("r").distinctProjectionValues().
                from(AssayWell.class, "aw").
                from("aw", AssayWell.libraryWell, "w", JoinType.INNER).
                from("w", Well.library, "l", JoinType.INNER).
                from("w", Well.latestReleasedReagent, "r", JoinType.INNER).
                where("l", "screenType", Operator.EQUAL, screenType).
                where("w", "libraryWellType", Operator.EQUAL, LibraryWellType.EXPERIMENTAL);
        log.debug("hql: " + builder.toHql());
        return builder.toQuery(session, true).setCacheMode(CacheMode.IGNORE).
          scroll(ScrollMode.FORWARD_ONLY);
      }
    });

    log.info("4. build the Study: positives: " + positivesMap.size() + ", reagents: " + overallMap.size());
    int count = 0;
    while (sr.next()) {
      Reagent r = (Reagent) sr.get()[0];

      AnnotationValue av = new AnnotationValue(overallAnnotationType,
                                               r,
                                               null,
                                               (double) overallMap.get(r.getReagentId()).intValue());
      _dao.saveOrUpdateEntity(av);
      Long positiveCount = positivesMap.get(r.getReagentId());
      if (positiveCount != null) {
        av = new AnnotationValue(positiveAnnotationType,
                                 r, null,
                                 (double) positiveCount.intValue());
        _dao.saveOrUpdateEntity(av);
      }
      // Note: due to memory performance, we will build the study_reagent_link later
      if (count++ % ROWS_TO_CACHE == 0) {
        log.debug("flushing");
        _dao.flush();
        _dao.clear();
      }
      if (count % 10000 == 0) {
        log.info("" + count + " reagents processed");
      }
    }

    log.info("save the study");
    _dao.saveOrUpdateEntity(study);
    _dao.flush();
    log.info("populateStudyReagentLinkTable");
    int reagentCount = populateStudyReagentLinkTable(study.getScreenId());
    log.info("done: positives: " + positivesMap.size() + ", reagents: " + overallMap.size());
    return reagentCount;
  }

  public AssayWell findAssayWell(ScreenResult screenResult, WellKey wellKey)
  {
    
    List result = getHibernateSession().createQuery("select a from AssayWell a where a.screenResult.id = ? and a.libraryWell.id = ?").
      setParameter(0, screenResult.getEntityId()).
      setParameter(1, wellKey.toString()).
      list();
    if (result.size() == 0) {
      return null;
    }
    if (result.size() > 1) {
      throw new DataModelViolationException("multiple assay wells found");
    }
    return (AssayWell) result.get(0); 
  }      

  /**
   * Use SQL to populate the Study-to-Reagent link table: <br>
   * Reagents could be added to the study in java-hibernate, however, this
   * poses memory performance issues.
   */
  //TODO: move this to the report class
  private int populateStudyReagentLinkTable(int screenId)
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

    javax.persistence.Query query = getEntityManager().createNativeQuery(sql);
    query.setParameter("studyId", screenId);
    int rows = query.executeUpdate();
    log.info("study_reagent_link updated: " + rows);
    return rows;
  }

  public ScreenResult getLatestScreenResult()
  {
    List<ScreenResult> result = new HqlBuilder().select("sr").distinctProjectionValues().
      from(ScreenResult.class, "sr").
      orderBy("sr", SortDirection.DESCENDING).
      toQuery(getHibernateSession(), true).list();

    if (result == null || result.isEmpty()) return null;
    return result.get(0);
  }


}
