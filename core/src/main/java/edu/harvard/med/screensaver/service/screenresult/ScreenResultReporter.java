// $HeadURL$
// $Id$
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.screenresult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.hibernate.CacheMode;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

import edu.harvard.med.screensaver.db.AbstractDAO;
import edu.harvard.med.screensaver.db.Criterion.Operator;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.db.hqlbuilder.JoinType;
import edu.harvard.med.screensaver.io.screens.StudyCreator;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryWellType;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.AnnotationValue;
import edu.harvard.med.screensaver.model.screenresults.AssayWell;
import edu.harvard.med.screensaver.model.screenresults.ConfirmedPositiveValue;
import edu.harvard.med.screensaver.model.screenresults.DataColumn;
import edu.harvard.med.screensaver.model.screenresults.DataType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.ProjectPhase;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenDataSharingLevel;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.StudyType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.util.NullSafeComparator;
import edu.harvard.med.screensaver.util.Triple;

/**
 * TODO: this service should be represented by an interface
 * for<br>
 * [#1476] RNAi duplex validation report: <br>
 * and<br>
 * [#2610] Confirmed Positives study creator <br>
 * and<br>
 * [#2268] New column to display # overlapping screens
 */
@Transactional
public class ScreenResultReporter
{
  public static final String DEFAULT_ANNOTATION_TITLE_WEIGHTED_AVERAGE = "Average number of confirmed positives per screen";
  public static final String DEFAULT_ANNOTATION_NAME_WEIGHTED_AVERAGE = "Weighted Average";
  public static final String DEFAULT_ANNOTATION_NAME_NUMBER_OF_SCREENS = "Number of screens";
  public static final String DEFAULT_ANNOTATION_TITLE_NUMBER_OF_SCREENS = "Number of screens";

  // utility class, allows args in format(args) to be interpreted as Object[] 
  public static class MessageFormatter
  {
    private MessageFormat format;
    private MessageFormatter(String pattern)
    {
      this.format = new MessageFormat(pattern);
    }
    public String format(Object... args)
    {
      return this.format.format(args);
    }
  }

  public static final MessageFormatter DEFAULT_ANNOTATION_NAME_COUNT_OF_SCREENS_N = new MessageFormatter("Number of screens confirming with {0} duplexes");
  public static final MessageFormatter DEFAULT_ANNOTATION_TITLE_COUNT_OF_SCREENS_N = new MessageFormatter("Number of screens confirming with {0} duplexes");

  private static Logger log = Logger.getLogger(ScreenResultReporter.class);

  private GenericEntityDAO _dao;

  public ScreenResultReporter()
  {}

  public ScreenResultReporter(GenericEntityDAO dao)
  {
    _dao = dao;
  }

  /**
   * Get a confirmed positive report for a single (pool) silencing reagent.<br>
   * For a given pool siRNA reagent; having 4 duplex reagents; query for screens of these duplex reagents,
   * and report on whether the results score a {@link DataType#CONFIRMED_POSITIVE_INDICATOR}<br>
   * <br>
   * re: [#1476] RNAi duplex validation report<br>
   */
  public ConfirmationReport getDuplexReconfirmationReport(final SilencingReagent poolReagent)
  {
    //    if (!poolReagent.getLibraryContentsVersion().getLibrary().isPool()) {
    //      throw new IllegalArgumentException("Duplex Confirmation Report is available for pool reagents (wells) only.");
    //    }

    ConfirmationReport report = new ConfirmationReport();
    List<Triple<SilencingReagent,Screen,ConfirmedPositiveValue>> poolConfirmationResults =
      findDuplexConfirmationResultsForPool(poolReagent);
    for (Triple<SilencingReagent,Screen,ConfirmedPositiveValue> row : poolConfirmationResults) {
      report.addDuplexConfirmatonResult(row.getFirst(),
                                        row.getSecond(),
                                        row.getThird());
    }

    return report;
  }

  /**
   * Create a study of the &quot;Confirmed Positives&quot; for all the pool SilencingReagents in the DB.
   * (re: {@link DataType#CONFIRMED_POSITIVE_INDICATOR} ) <br>
   * <ul>
   * For RNAi
   * <li>Count of follow-up screens for well
   * <li>M+1 columns named "N duplexes confirming positive", where 0 <= N <= M, and M is the max number of duplexes per
   * pool in any library, currently = 4). The value in each column is the number of follow-up screens that confirmed the
   * well as a positive with N duplexes
   * </ul>
   * see [#2610] Confirmed Positives study creator<br>
   * 
   * @return total count of confirmed positives considered in this study (informational)
   */
  public int createSilencingReagentConfirmedPositiveSummary(Screen study)
  {
    log.info("Get all of the pool reagents...");
    ScrollableResults sr = _dao.runScrollQuery(new edu.harvard.med.screensaver.db.ScrollQuery() {
      public ScrollableResults execute(Session session)
      {
        HqlBuilder builder = new HqlBuilder();
        builder.
          select("pr").
          from(Library.class, "l").
          from("l", Library.wells, "w", JoinType.INNER).
          from("w", Well.latestReleasedReagent, "pr", JoinType.INNER).
          where("l", "pool", Operator.EQUAL, Boolean.TRUE);
        return builder.toQuery(session, true).setCacheMode(CacheMode.IGNORE).scroll(ScrollMode.FORWARD_ONLY);
      }
    });

    log.info("Create the annotation types for the study.");
    AnnotationType averageConfirmedPositivesPerScreen =
      study.createAnnotationType(DEFAULT_ANNOTATION_NAME_WEIGHTED_AVERAGE,
                                 DEFAULT_ANNOTATION_TITLE_WEIGHTED_AVERAGE, true);
    _dao.persistEntity(averageConfirmedPositivesPerScreen);
    AnnotationType numberOfScreensAT =
      study.createAnnotationType(DEFAULT_ANNOTATION_NAME_NUMBER_OF_SCREENS,
                                 DEFAULT_ANNOTATION_TITLE_NUMBER_OF_SCREENS, true);
    _dao.persistEntity(numberOfScreensAT);
    // Create the bin-count annotation types (for "screens confirming # duplexes...")
    Map<Integer,AnnotationType> binToAnnotationTypeMap = Maps.newHashMap();
    for (int i = 0; i <= 4; i++) // todo: make this a dynamic cardinality 
    {
      AnnotationType screenCounter = study.createAnnotationType(DEFAULT_ANNOTATION_NAME_COUNT_OF_SCREENS_N.format(i),
                                                                DEFAULT_ANNOTATION_TITLE_COUNT_OF_SCREENS_N.format(i),
                                                                true);
      binToAnnotationTypeMap.put(i, screenCounter);
      _dao.persistEntity(screenCounter);
    }
    _dao.flush();
    _dao.clear();

    log.info("scroll through the pool reagents...");
    int countOfDuplexReagentsConfirmed = 0;
    int count = 0;

    while (sr.next()) {
      SilencingReagent poolReagent = (SilencingReagent) sr.get(0);

      ConfirmationReport report = getDuplexReconfirmationReport(poolReagent);

      int[] binToScreenCount = report.getBinToScreenCount();
      int numberOfScreens = 0;
      for (int bin = 0; bin < binToScreenCount.length; bin++) {
        int screenCount = binToScreenCount[bin];

        AnnotationType at = binToAnnotationTypeMap.get(bin);
        // note: for memory performance, we're side-stepping the AnnotationType.createAnnotationValue() method
        AnnotationValue av = new AnnotationValue(at, poolReagent, null, (double) screenCount);
        _dao.saveOrUpdateEntity(av);

        numberOfScreens += screenCount;
        countOfDuplexReagentsConfirmed += screenCount * bin;
      }

      if (numberOfScreens > 0) {
        // note: for memory performance, we're side-stepping the AnnotationType.createAnnotationValue() method
        AnnotationValue av = new AnnotationValue(averageConfirmedPositivesPerScreen, poolReagent, null, new Double("" +
                  report.getWeightedAverage()));
        _dao.saveOrUpdateEntity(av);

      }
      // note: for memory performance, we're side-stepping the AnnotationType.createAnnotationValue() method
      AnnotationValue av = new AnnotationValue(numberOfScreensAT, poolReagent, null, (double) numberOfScreens);
      _dao.saveOrUpdateEntity(av);

      // for memory performance clear the session every CACHE_SIZE number of iterations
      if (count++ % AbstractDAO.ROWS_TO_CACHE == 0) {
        log.debug("clearing & flushing session");
        _dao.flush();
        _dao.clear();
      }
      if (count % 1000 == 0) {
        log.info("" + count + " reagents processed");
      }
    }
    log.info("" + count + " reagents processed");
    _dao.flush();
    _dao.clear();

    log.info("countOfDuplexReagentsConfirmed: " + countOfDuplexReagentsConfirmed);
    log.info("populateStudyReagentLinkTable");
    populateStudyReagentLinkTable(study.getScreenId());
    log.info("Study created: " + study.getTitle() + ", reagents: " + countOfDuplexReagentsConfirmed);
    return countOfDuplexReagentsConfirmed;
  }

  /**
   * @return the count of reagents
   */
  @Transactional
  public int createReagentCountStudy(AdministratorUser admin,
                                     String studyName,
                                     String title,
                                     String summary,
                                     String positiveCountAnnotationName,
                                     String positiveCountAnnotationDescription,
                                     String overallCountAnnotationName,
                                     String overallCountAnnotationDesc,
                                     ScreenType screenType)
  {
    admin = _dao.mergeEntity(admin);
    Screen study = _dao.findEntityByProperty(Screen.class,
                                            Screen.facilityId.getPropertyName(),
                                            studyName);
    if (study != null) {
      String errMsg = "study " + studyName +
        " already exists (use --replace flag to delete existing study first)";
      throw new IllegalArgumentException(errMsg);
    }

    LabHead labHead = (LabHead) StudyCreator.findOrCreateScreeningRoomUser(_dao,
                                                                           admin.getFirstName(),
                                                                           admin.getLastName(),
                                                                           admin.getEmail(),
                                                                           true,
                                                                           null);
    study = new Screen(admin,
                       studyName,
                       labHead,
                       labHead,
                       screenType,
                       StudyType.IN_SILICO,
                       ProjectPhase.ANNOTATION,
                       title);
    study.setDataSharingLevel(ScreenDataSharingLevel.SHARED);
    study.setSummary(summary);
    _dao.persistEntity(study);

    AnnotationType positivesCountAnnotType = study.createAnnotationType(positiveCountAnnotationName,
                                                                        positiveCountAnnotationDescription,
                                                                        true);
    AnnotationType overallCountAnnotType = study.createAnnotationType(overallCountAnnotationName,
                                                                      overallCountAnnotationDesc,
                                                                      true);
    _dao.persistEntity(overallCountAnnotType);
    _dao.persistEntity(positivesCountAnnotType);

    _dao.flush();
    return createScreenedReagentCounts(screenType, study, positivesCountAnnotType, overallCountAnnotType);
  }

  /**
   * for [#2268] new column to display # overlapping screens
   */
  @Transactional
  public int createScreenedReagentCounts(final ScreenType screenType,
                                          Screen study,
                                          AnnotationType positiveAnnotationType,
                                          AnnotationType overallAnnotationType)
  {
    // Break this into two separate queries because of an apparent Hibernate bug:
    // when using the "group by" clause with a full object (as opposed to an attribute of the object/table),
    // Hibernate is requiring that every attribute of the object be specified in a "group by" and not 
    // just the object itself.  so the workaround is to query once to get the id's then once again to 
    // get the objects.
//    study = _dao.mergeEntity(study);
//    positiveAnnotationType = _dao.mergeEntity(positiveAnnotationType);
//    overallAnnotationType = _dao.mergeEntity(overallAnnotationType);
//    _dao.flush();

    log.info("1. get the reagent id's for the positive counts");
    ScrollableResults sr = _dao.runScrollQuery(new edu.harvard.med.screensaver.db.ScrollQuery() {
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
    sr = _dao.runScrollQuery(new edu.harvard.med.screensaver.db.ScrollQuery() {
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

    log.info("begin assigning values to the study");
    int overallCount = 0;
    Map<Integer,Long> overallMap = Maps.newHashMap();
    while (sr.next()) {
      Object[] row = sr.get();
      Integer r_id = (Integer) row[0];
      Long count = (Long) row[1];
      Reagent r = _dao.findEntityById(Reagent.class, r_id,true);
      // note: for memory performance, we're side-stepping the AnnotationType.createAnnotationValue() method
      AnnotationValue av = new AnnotationValue(overallAnnotationType,
                                               r,
                                               null,
                                               (double) count);
      _dao.persistEntity(av);
      Long positiveCount = positivesMap.get(r_id);
      if (positiveCount != null) {
        // note: for memory performance, we're side-stepping the AnnotationType.createAnnotationValue() method
        av = new AnnotationValue(positiveAnnotationType,
                                 r, null,
                                 (double) positiveCount.intValue());
        _dao.persistEntity(av);
      }
      // Note: due to memory performance, we will build the study_reagent_link later
      if (count++ % AbstractDAO.ROWS_TO_CACHE == 0) {
        log.debug("flushing");
        _dao.flush();
        _dao.clear();
      }
      if (++overallCount % 10000 == 0) {
        log.info("" + overallCount + " reagents processed");
      }
    }
    
    log.info("save the study");
    // unnecessary since study is already persisted, and the reagents will be linked by the populateStudyReagentLinkTable - sde4
    // _dao.mergeEntity(study);
    _dao.flush();
    log.info("populateStudyReagentLinkTable");
    int reagentCount = populateStudyReagentLinkTable(study.getScreenId());
    log.info("done: positives: " + positivesMap.size() + ", reagents: " + overallCount);
    return reagentCount;
  }

  /**
   * Use SQL to populate the Study-to-Reagent link table: <br>
   * Reagents can be added to the study in java-hibernate, (using the standard
   * {@link AnnotationType#createAnnotationValue(Reagent, String)}) however, when
   * this is done, the AnnotationType will maintain a collection of AnnotationTypes in
   * memory.<br>
   * <br>
   * <b>
   * NOTE: do not use this method if creating the Annotations through the
   * {@link AnnotationType#createAnnotationValue(Reagent, String)} method.
   * </b>
   */
  private int populateStudyReagentLinkTable(final int screenId)
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

  public static class ConfirmationReport
  {
    public ConfirmationReport()
    {
    }

    public void addDuplexConfirmatonResult(SilencingReagent duplexReagent,
                                           Screen screen,
                                           ConfirmedPositiveValue cpv)
    {
      if (!_results.containsKey(screen)) {
        Map<SilencingReagent,ConfirmedPositiveValue> map = Maps.newHashMap();
        _results.put(screen, map);
      }
      _results.get(screen).put(duplexReagent, cpv);
      _duplexReagents.add(duplexReagent);
    }

    public Map<Screen,Map<SilencingReagent,ConfirmedPositiveValue>> getResults()
    {
      return _results;
    }

    public List<SilencingReagent> getDuplexReagents()
    {
      return reagentOrdering.sortedCopy(_duplexReagents);
    }

    private Set<SilencingReagent> _duplexReagents = Sets.newHashSet();
    private Map<Screen,Map<SilencingReagent,ConfirmedPositiveValue>> _results = Maps.newHashMap();

    private static final Ordering<Screen> screenOrdering = Ordering.from(new NullSafeComparator<Screen>() {
      @Override
      public int doCompare(Screen o1, Screen o2)
      {
        return new NullSafeComparator<String>() {
          @Override
          protected int doCompare(String o1, String o2)
          {
            return o1.compareTo(o2);
          }
        }.compare(o1.getFacilityId(), o2.getFacilityId());
      }
    });

    private static final Ordering<SilencingReagent> reagentOrdering = Ordering.from(new NullSafeComparator<SilencingReagent>() {
      @Override
      public int doCompare(SilencingReagent o1, SilencingReagent o2)
      {
        return new NullSafeComparator<ReagentVendorIdentifier>() {
          @Override
          protected int doCompare(ReagentVendorIdentifier o1, ReagentVendorIdentifier o2)
          {
            return o1.compareTo(o2);
          }
        }.compare(o1.getVendorId(), o2.getVendorId());
      }
    });

    public List<Screen> getScreens() {
      return screenOrdering.sortedCopy(_results.keySet());
    }
    
    /**
     * Create the array of [# of confirmed positives]->[# of screens]
     * For [#2610] Confirmed Positives study creator<br>
     * <br>
     * <ul>
     * For RNAi
     * <li>Count of follow-up screens for well
     * <li>M+1 columns named "N duplexes confirming positive", where 0 <= N <= M, and M is the max number of duplexes
     * per pool in any library, currently = 4). The value in each column is the number of follow-up screens that
     * confirmed the well as a positive with N duplexes
     * </ul>
     * <ul>
     * For SM (TODO)
     * <li>Count of follow-up screens for well
     * <li># follow-up screens confirming well as a positive
     * <li>A column for every follow-up screen and duplex, containing the confirmation result of the well
     * </ul>
     */
    public int[] getBinToScreenCount()
    {
      if (_results.isEmpty()) return new int[0];
      int[] binCountArray = new int[_duplexReagents.size() + 1];
      for (Screen screen : _results.keySet()) {
        int bin = 0;
        for (ConfirmedPositiveValue cpv : _results.get(screen).values()) {
          if (cpv == ConfirmedPositiveValue.CONFIRMED_POSITIVE) bin++;
        }
        binCountArray[bin]++;
      }
      return binCountArray;
    }

    public float getWeightedAverage()
    {
      int[] binToScreenCount = getBinToScreenCount();
      return getWeightedAverage(binToScreenCount, 2);
    }

    /**
     * Average of an array, where the index is the unweighted value, the bin content is the weight<br>
     * <br>
     * for 0<i<k, where k=maxValue; D[i]=weight;
     * WeightedAverage = sum(D[i]*i)/sum(D[i])
     * 
     * @param binArray - for each bin: index=itemValue, bin[index]=itemWeight
     */
    public static float getWeightedAverage(int[] binArray, int decimalPlaces)
    {
      if (binArray == null || binArray.length < 2) return 0;
      int weightedValueSum = 0;
      int weightSum = 0;
      for (int value = 0; value < binArray.length; value++) {
        int weight = binArray[value];
        weightedValueSum += value * weight;
        weightSum += weight;
      }
      return new BigDecimal(weightedValueSum / (float) weightSum).setScale(decimalPlaces, RoundingMode.HALF_EVEN).floatValue();
    }

    //TODO: setting this value may cause an inconsistent state - consider creating an alternate constructor, so that the results collection is cleared if this is set -sde4
    public void setDuplexReagents(Collection<SilencingReagent> duplexReagents)
    {
      _duplexReagents = Sets.newHashSet(duplexReagents);
    }
  }

  @SuppressWarnings("unchecked")
  private List<Triple<SilencingReagent,Screen,ConfirmedPositiveValue>> findDuplexConfirmationResultsForPool(final SilencingReagent poolReagent)
  {
    List<Object[]> rows =
    _dao.runQuery(new edu.harvard.med.screensaver.db.Query() {
      public List<?> execute(Session session)
      {
        HqlBuilder builder = new HqlBuilder();
        builder.select("dr").select("s").select("aw", "confirmedPositiveValue").
          from(SilencingReagent.class, "pr").
          from("pr", SilencingReagent.duplexWells, "dw").
          from("dw", Well.latestReleasedReagent, "dr").
          from(AssayWell.class, "aw").
          from("aw", DataColumn.ScreenResult, "sr").
          from("sr", ScreenResult.screen, "s").
          where("aw", "libraryWell", Operator.EQUAL, "dr", "well").
          whereIn("aw", "confirmedPositiveValue", Sets.newHashSet(ConfirmedPositiveValue.CONFIRMED_POSITIVE,
                                                                  ConfirmedPositiveValue.FALSE_POSITIVE)).
          where("pr", Operator.EQUAL, poolReagent);
        return builder.toQuery(session, true).list();
      }
    });
    List<Triple<SilencingReagent,Screen,ConfirmedPositiveValue>> result = Lists.newArrayList();
    for (Object[] row : rows) {
      result.add(new Triple<SilencingReagent,Screen,ConfirmedPositiveValue>((SilencingReagent) ((SilencingReagent) row[0]).restrict(),
                                                                            (Screen) row[1],
                                                                            (ConfirmedPositiveValue) row[2]));
    }
    return result;
  }
}
