// $HeadURL$
// $Id$
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.screenresults;

import java.math.BigDecimal;
import java.math.BigInteger;
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
import edu.harvard.med.screensaver.db.ScreenDAO;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.db.hqlbuilder.JoinType;
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
	public static final int NUMBER_OF_BINS = 5;  // TODO: 5 bins are needed because there are 4 duplexes for a pool reagent, and we are counting the # of screens with X duplexes confirming (plus one bin for "zero").

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
  private ScreenDAO _screenDao;

  public ScreenResultReporter()
  {}

  public ScreenResultReporter(GenericEntityDAO dao, ScreenDAO screenDao)
  {
    _dao = dao;
    _screenDao = screenDao;
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
    if(report.getDuplexReagents().size() > NUMBER_OF_BINS -1)
    {
    	throw new RuntimeException("Error, " + report.getDuplexReagents().size() + " duplexes found, " + (NUMBER_OF_BINS -1) + " allowed per pool reagent.  Pool reagent: " + poolReagent.getWell() +  ", this is not allowed for pool libraries, and is incompatible with this study"); // for [#3451]
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

      int[] binToScreenCount = report.getBinToScreenCount(poolReagent);
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
    _screenDao.populateStudyReagentLinkTable(study.getScreenId());
    log.info("Study created: " + study.getTitle() + ", reagents: " + countOfDuplexReagentsConfirmed);
    return countOfDuplexReagentsConfirmed;
  }

  /**
   * @return the count of reagents
   */
  @Transactional
  public int[] createReagentCountStudy(AdministratorUser admin,
                                     LabHead labHead,
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
   * - new version using sql insert resolves #166
   */
  @Transactional
  public int[] createScreenedReagentCounts(final ScreenType screenType,
                                          Screen study,
                                          final AnnotationType positiveAnnotationType,
                                          final AnnotationType overallAnnotationType)
  {

    
    final int[] result = new int[2];
    _dao.runQuery(new edu.harvard.med.screensaver.db.Query() {
      public List<?> execute(Session session)
      {
        String sql = 
            "insert into annotation_value  " + 
            "    (annotation_value_id, annotation_type_id, numeric_value, reagent_id) " + 
            "    select nextval('annotation_value_id_seq'), :annotation_type_id, count, reagent_id  " + 
            "    from ( " +
            "    select " + 
            "    r.reagent_id," + 
            "    count(*) " + 
            "    from assay_well aw " + 
            "    join well w using(well_id) " + 
            "    join reagent r on w.latest_released_reagent_id=r.reagent_id " + 
            "    join library l using(library_id) " + 
            "    where  " + 
            "    aw.is_positive = true " + 
            "    and l.screen_type = :screenType " + 
            "    and w.library_well_type = 'experimental' " + 
            "    group by r.reagent_id ) a;  "; 

        log.info("sql: " + sql);

        Query query = session.createSQLQuery(sql);
        query.setParameter("screenType", screenType.getValue());
        query.setParameter("annotation_type_id", positiveAnnotationType.getAnnotationTypeId());

        int rows = query.executeUpdate();
        log.info("rows: " + rows);
        
        if (rows == 0) {
          log.warn("No screened positives counts rows were updated: " + 
            query.getQueryString());
        }
        log.info("positive count annotation values created: " + rows);
        result[0] = rows;
        
        // exact same as before, except without is_positive constraint
        String sql2 = 
            "insert into annotation_value  " + 
            "    (annotation_value_id, annotation_type_id, numeric_value, reagent_id) " + 
            "    select nextval('annotation_value_id_seq'), :annotation_type_id, count, reagent_id  " + 
            "    from ( " +
            "    select " + 
            "    r.reagent_id," + 
            "    count(*) " + 
            "    from assay_well aw " + 
            "    join well w using(well_id) " + 
            "    join reagent r on w.latest_released_reagent_id=r.reagent_id " + 
            "    join library l using(library_id) " + 
            "    where  " + 
            "    l.screen_type = :screenType " + 
            "    and w.library_well_type = 'experimental' " + 
            "    group by r.reagent_id ) a;  "; 
            
        log.info("sql2: " + sql2);

        query = session.createSQLQuery(sql2);
        query.setParameter("screenType", screenType.getValue() );
        query.setParameter("annotation_type_id", overallAnnotationType.getAnnotationTypeId());
        rows = query.executeUpdate();
        log.info("rows: " + rows);

        if (rows == 0) {
          log.warn("No screened counts rows were updated: " +
            query.getQueryString());
        }
        log.info("screened count annotation values created: " + rows);
        result[1] = rows;
        
        return null;
      }
    });
    int screened_positives_counts = result[0];
    int screened_counts = result[1];
    
    _dao.flush();
    log.info("populateStudyReagentLinkTable");
    int reagentCount = _screenDao.populateStudyReagentLinkTable(study.getScreenId());
    log.info("done: positives: " + screened_positives_counts 
        + ", screened reagents: " + screened_counts);
    return result;
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
     * per pool in any library, (currently = 4). The value in each column is the number of follow-up screens that
     * confirmed the well as a positive with N duplexes
     * </ul>
     * <ul>
     * For SM (TODO)
     * <li>Count of follow-up screens for well
     * <li># follow-up screens confirming well as a positive
     * </ul>
     */
    public int[] getBinToScreenCount()
    {    
    	return getBinToScreenCount(null);
    }
    public int[] getBinToScreenCount(SilencingReagent poolReagent)
    {
      if (_results.isEmpty()) return new int[0];
//    int[] binCountArray = new int[_duplexReagents.size() + 1];
      int[] binCountArray = new int[NUMBER_OF_BINS]; // temp fix for  [#3451], since we've found a pool well that has > 4 duplexes assigned (this is probably an error, and ok to set this to 4, since we'll remove the other 4) -sde4
      for (Screen screen : _results.keySet()) {
        int bin = 0;
        // iterate through each screen's - map[reagent->value], if value is positive, then that counts as a confirmation for that screen; tally the confirmations for the screen, and increment the bin
        for (ConfirmedPositiveValue cpv : _results.get(screen).values()) {
          if (cpv == ConfirmedPositiveValue.CONFIRMED_POSITIVE) bin++;
        }
        if(bin > NUMBER_OF_BINS) 
        	throw new RuntimeException("too many confirmations for screen: " + screen.getFacilityId() + ", pool: " + (poolReagent==null?"" : poolReagent.getWell()));
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
