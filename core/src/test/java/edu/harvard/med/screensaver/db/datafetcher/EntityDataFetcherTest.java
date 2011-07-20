// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.datafetcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.model.Entity;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.AnnotationValue;
import edu.harvard.med.screensaver.model.screenresults.AssayWell;
import edu.harvard.med.screensaver.model.screenresults.DataColumn;
import edu.harvard.med.screensaver.model.screenresults.PartitionedValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.test.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.test.MakeDummyEntities;
import edu.harvard.med.screensaver.ui.arch.datatable.Criterion;
import edu.harvard.med.screensaver.ui.arch.datatable.Criterion.Operator;
import edu.harvard.med.screensaver.ui.arch.datatable.Criterion.OperatorClass;

public class EntityDataFetcherTest extends AbstractSpringPersistenceTest
{
  // static members

  private static Logger log = Logger.getLogger(EntityDataFetcherTest.class);


  // instance data members

  private EntityDataFetcher<Well,String> _screenResultWellFetcher;
  private EntityDataFetcher<Well,String> _wellSetFetcher;
  private EntityDataFetcher<Well,String> _allWellsFetcher;
  private List<PropertyPath<Well>> _pathsToFetch = Lists.newArrayList();
  private List<PropertyPath<Well>> _sortProperties = Lists.newArrayList();
  private Map<PropertyPath<Well>,List<? extends Criterion<?>>> _criteria = new HashMap<PropertyPath<Well>,List<? extends Criterion<?>>>();;
  private PropertyPath<Well> _plateNumberPropPath;
  private PropertyPath<Well> _wellNamePropPath;

  private static boolean oneTimeDataSetup = false;
  private static Screen _rnaiScreen;
  private static Screen _study;
  private static Library _rnaiLibrary;
  private static ScreenResult _screenResult;
  private static int _plates = 2;


  // public constructors and methods

  @Override
  protected void setUp() throws Exception
  {
    if (!oneTimeDataSetup) {
      oneTimeDataSetup = true;
      super.setUp();
      genericEntityDao.doInTransaction(new DAOTransaction() {

        public void runTransaction()
        {
          _rnaiScreen = MakeDummyEntities.makeDummyScreen(2, ScreenType.RNAI);
          _rnaiLibrary = MakeDummyEntities.makeDummyLibrary(2,
                                                            _rnaiScreen.getScreenType(),
                                                            _plates);
          genericEntityDao.saveOrUpdateEntity(_rnaiLibrary.getContentsVersions().first().getLoadingActivity().getCreatedBy());
          genericEntityDao.saveOrUpdateEntity(_rnaiLibrary);
          _screenResult = MakeDummyEntities.makeDummyScreenResult(_rnaiScreen,
                                                                  _rnaiLibrary);
          genericEntityDao.saveOrUpdateEntity(_rnaiScreen.getLeadScreener());
          genericEntityDao.saveOrUpdateEntity(_rnaiScreen.getLabHead());
          genericEntityDao.saveOrUpdateEntity(_rnaiScreen);

          // make a study, for its annotations
          _study = MakeDummyEntities.makeDummyStudy(_rnaiLibrary);
          genericEntityDao.persistEntity(_study.getLeadScreener());
          genericEntityDao.persistEntity(_study.getLabHead());
          genericEntityDao.persistEntity(_study);

          // make another screen result, to ensure tests that are looking for
          // the data from a particular screen result are not also retrieving
          // data from another screen result
          Screen otherRnaiScreen = MakeDummyEntities.makeDummyScreen(3,
                                                                     ScreenType.RNAI);
          MakeDummyEntities.makeDummyScreenResult(otherRnaiScreen, _rnaiLibrary);
          genericEntityDao.saveOrUpdateEntity(otherRnaiScreen.getLeadScreener());
          genericEntityDao.saveOrUpdateEntity(otherRnaiScreen.getLabHead());
          genericEntityDao.saveOrUpdateEntity(otherRnaiScreen);
        }
      });
    }

    // note: we reload everything, even if it was just created (above), since we
    // want entity-ID-based hashCodes, not Object hashCodes
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        _rnaiScreen = genericEntityDao.reloadEntity(_rnaiScreen, true);
        _screenResult = _rnaiScreen.getScreenResult();
        genericEntityDao.needReadOnly(_screenResult, ScreenResult.dataColumns.to(DataColumn.resultValues));
        genericEntityDao.needReadOnly(_screenResult, ScreenResult.assayWells);
        _study = genericEntityDao.reloadEntity(_study, true, Screen.annotationTypes.to(AnnotationType.annotationValues));
        _rnaiLibrary = genericEntityDao.reloadEntity(_rnaiLibrary, true, Library.wells);
      }
    });

    _plateNumberPropPath = RelationshipPath.from(Well.class).toProperty("plateNumber");
    _wellNamePropPath = RelationshipPath.from(Well.class).toProperty("wellName");
    _sortProperties.add(_plateNumberPropPath);
    _sortProperties.add(_wellNamePropPath);
    _pathsToFetch.add(_plateNumberPropPath);
    _pathsToFetch.add(_wellNamePropPath);

    _screenResultWellFetcher = new EntityDataFetcher<Well,String>(Well.class, genericEntityDao) {
      @Override
      public void addDomainRestrictions(HqlBuilder hql)
      {
        hql.from(AssayWell.class, "aw");
        hql.where(getRootAlias(), "wellId", Operator.EQUAL, "aw", "libraryWell");
        hql.where("aw", "screenResult", Operator.EQUAL, _screenResult);
      }
    };
    final Set<String> wellKeys = Sets.newHashSet("02000:A01", "02001:P24");
    _wellSetFetcher = new EntityDataFetcher<Well,String>(Well.class, genericEntityDao) {
      @Override
      public void addDomainRestrictions(HqlBuilder hql)
      {
        DataFetcherUtil.addDomainRestrictions(hql, getRootAlias(), wellKeys);
      }
    };
    _allWellsFetcher = new EntityDataFetcher<Well,String>(Well.class, genericEntityDao);
  }

  /**
   * Test fetching all data of a single-entity (shallow) entity network.
   */
  public void testShallowFetchAllData()
  {
    _screenResultWellFetcher.setPropertiesToFetch(_pathsToFetch);
    _wellSetFetcher.setPropertiesToFetch(_pathsToFetch);
    _allWellsFetcher.setPropertiesToFetch(_pathsToFetch);

    assertEquals("fetch parented",
                 384 * _plates,
                 _screenResultWellFetcher.fetchAllData().size());
    assertEquals("fetch set", 2, _wellSetFetcher.fetchAllData().size());
    assertEquals("fetch all", 384 * _plates, _allWellsFetcher.fetchAllData().size());
  }

  /**
   * Test fetching a subset (data range) of a single-entity (shallow) entity network,
   * unrestricted, sorted by ID.
   */
  @SuppressWarnings("unchecked")
  public void testShallowFetchDataRange()
  {
    _screenResultWellFetcher.setPropertiesToFetch(_pathsToFetch);
    _screenResultWellFetcher.setOrderBy(_sortProperties);

    AssayWell[] assayWells = Iterables.toArray(_screenResult.getAssayWells(), AssayWell.class);
    Set<String> wellIds = Sets.newHashSet();
    for (int i = 24; i < 48; i++) {
      wellIds.add(assayWells[i].getLibraryWell().getWellId());
    }

    List<String> foundKeys = _screenResultWellFetcher.findAllKeys();
    assertEquals("size", 384 * _plates, foundKeys.size());
    isSorted(foundKeys);

    Map<String,Well> data = _screenResultWellFetcher.fetchData(wellIds);
    assertEquals(wellIds, data.keySet());
    assertEquals(wellIds, Sets.newHashSet(Iterables.transform(data.values(), Entity.ToEntityId)));
  }

  /**
   * Test fetching a subset (data range) of a single-entity (shallow) entity network,
   * with filtering criteria, sorted by ID.
   */
  public void testShallowFetchDataRangeFiltered()
  {
    _screenResultWellFetcher.setPropertiesToFetch(_pathsToFetch);
    _screenResultWellFetcher.setOrderBy(_sortProperties);
    _criteria.put(_wellNamePropPath, Collections.singletonList(new Criterion<String>(Operator.TEXT_STARTS_WITH, "B2")));
    _criteria.put(_plateNumberPropPath, Collections.singletonList(new Criterion<Integer>(Operator.EQUAL, 2001)));
    _screenResultWellFetcher.setFilteringCriteria(_criteria);

    List<String> expectedWellKeys = new ArrayList<String>();
    expectedWellKeys.add(new WellKey(2001, "B20").toString());
    expectedWellKeys.add(new WellKey(2001, "B21").toString());
    expectedWellKeys.add(new WellKey(2001, "B22").toString());
    expectedWellKeys.add(new WellKey(2001, "B23").toString());
    expectedWellKeys.add(new WellKey(2001, "B24").toString());

    List<String> foundKeys = _screenResultWellFetcher.findAllKeys();
    assertEquals("keys", expectedWellKeys, foundKeys);

    Map<String,Well> data = _screenResultWellFetcher.fetchData(new HashSet<String>(expectedWellKeys));
    assertEquals("size", expectedWellKeys.size(), data.size());
    for (String expectedWellKey : expectedWellKeys) {
      assertTrue(data.containsKey(expectedWellKey));
      assertEquals(expectedWellKey, data.get(expectedWellKey).getWellKey().toString());
    }
  }

  /**
   * Test fetching a subset (data range) of a multiple-entity (deep) entity network,
   * with filtering criteria on 2 properties of restricted collections.
   */
  public void testDeepFetchDataRangeFiltered()
  {
    DataColumn col1 = _screenResult.getDataColumnsList().get(0);
    DataColumn col2 = _screenResult.getDataColumnsList().get(2);
    DataColumn col3 = _screenResult.getDataColumnsList().get(6);
    DataColumn col4 = _screenResult.getDataColumnsList().get(7); // "comments", with sparse result values
    PropertyPath<Well> propertyPath1 = Well.resultValues.restrict("dataColumn", col1).toProperty("value");
    PropertyPath<Well> propertyPath2 = Well.resultValues.restrict("dataColumn", col2).toProperty("value");
    PropertyPath<Well> propertyPath3 = Well.resultValues.restrict("dataColumn", col3).toProperty("positive");
    PropertyPath<Well> propertyPath4 = Well.resultValues.restrict("dataColumn", col4).toProperty("value");
    _pathsToFetch.add(propertyPath1);
    _pathsToFetch.add(propertyPath2);
    _pathsToFetch.add(propertyPath3);
    _pathsToFetch.add(propertyPath4);
    _criteria.put(propertyPath3, Collections.singletonList(new Criterion<Boolean>(Operator.EQUAL, true)));
    _criteria.put(propertyPath4, Collections.singletonList(new Criterion<String>(Operator.NOT_EMPTY, null)));

    _screenResultWellFetcher.setPropertiesToFetch(_pathsToFetch);
    _screenResultWellFetcher.setFilteringCriteria(_criteria);

    Set<String> expectedWellIds = Sets.newHashSet();
    for (ResultValue rv : col3.getResultValues()) {
      if (rv.isPositive()) {
        expectedWellIds.add(rv.getWell().getWellId());
      }
    }
    for (ResultValue rv : col4.getResultValues()) {
      if (rv.isNull() || rv.getValue().isEmpty()) {
        expectedWellIds.remove(rv.getWell().getWellId());
      }
    }

    Set<String> foundWellIds = Sets.newHashSet(_screenResultWellFetcher.findAllKeys());
    log.debug("not found: " + Sets.difference(expectedWellIds, foundWellIds));
    log.debug("found unexpectedly: " + Sets.difference(foundWellIds, expectedWellIds));
    assertEquals("findAllKeys", expectedWellIds, foundWellIds);

    Map<String,Well> data = _screenResultWellFetcher.fetchData(new HashSet<String>(expectedWellIds));
    assertEquals("fetchData", expectedWellIds.size(), data.size());
    for (String expectedWellKey : expectedWellIds) {
      assertTrue("fetchData", data.containsKey(expectedWellKey));
      assertEquals("fetchData", expectedWellKey, data.get(expectedWellKey).getWellKey().toString());
    }
  }

  public void testEagerFetchCollections()
  {
    _allWellsFetcher.setPropertiesToFetch(Lists.newArrayList(Well.resultValues.toFullEntity(), Well.library.to(Library.contentsVersions).toFullEntity()));
    List<Well> allData = _allWellsFetcher.fetchAllData();
    Collections.sort(allData);
    Well well = allData.get(0);
    assertEquals("well.resultValues unrestricted size",
                 _screenResult.getDataColumns().size() /*DataColumns per screen result*/ * 2 /* screen results*/,
                 well.getResultValues().size());

    _allWellsFetcher.setPropertiesToFetch(Lists.newArrayList(Well.latestReleasedReagent.to(Reagent.annotationValues).toFullEntity(), Well.library.to(Library.contentsVersions).toFullEntity()));
    allData = _allWellsFetcher.fetchAllData();
    Collections.sort(allData);
    well = allData.get(0);
    assertEquals("well.reagent.annotationValues restricted size",
                 _study.getAnnotationTypes().size() /*ATs per study*/ * 1 /*study*/,
                 well.<Reagent>getLatestReleasedReagent().getAnnotationValues().size());

    _pathsToFetch.add(Well.resultValues.to(ResultValue.DataColumn).restrict(DataColumn.ScreenResult.getLeaf(), _screenResult).toFullEntity());
    _allWellsFetcher.setPropertiesToFetch(_pathsToFetch);
    allData = _allWellsFetcher.fetchAllData();
    Collections.sort(allData);

    // test a well that has a RV for every DataColumn
    well = allData.get(0);
    assertEquals("expected well", "02000:A01", well.getWellKey().toString());
    assertEquals("well.resultValues restricted size",
                 // NOTE: Expected size is non-restricted size. We gave up on
                 // trying to filter collections when eager fetching; Hibernate
                 // does not naturally support this via
                 // EntityDataFetcher.getOrCreateFetchJoin()
                 16, //_screenResult.getDataColumns().size(),
                 well.getResultValues().size());

    // test a well that does not have a RV for every DataColumn
    well = allData.get(3);
    assertEquals("expected well", "02000:A04", well.getWellKey().toString());
    assertEquals("well.resultValues restricted size",
                 // NOTE: Expected size is non-restricted size. We gave up on
                 // trying to filter collections when eager fetching; Hibernate
                 // does not naturally support this via
                 // EntityDataFetcher.getOrCreateFetchJoin()
                 16 - 2 /*_screenResult.getDataColumns().size() - 1*/,
                 well.getResultValues().size());
  }

  /**
   * Test eager fetching of restricted collections that are empty when restricted,
   * verifying that all root entities are still returned in the result.
   */
  public void testEagerFetchEmptyRestrictedCollection()
  {
    final Set<String> wellKeys = Sets.newHashSet("02000:A04", "02000:A08", "02000:A12");
    EntityDataFetcher<Well,String> wellSetFetcher = new EntityDataFetcher<Well,String>(Well.class, genericEntityDao) {
      @Override
      public void addDomainRestrictions(HqlBuilder hql)
      {
        DataFetcherUtil.addDomainRestrictions(hql, getRootAlias(), wellKeys);
      }
    };
    DataColumn commentsCol = _screenResult.getDataColumnsList().get(7); // has with sparse result values
    _pathsToFetch.add(Well.latestReleasedReagent.to(Reagent.annotationValues).toFullEntity());
    _pathsToFetch.add(Well.resultValues.restrict(ResultValue.DataColumn.getLeaf(), commentsCol).toFullEntity());
    wellSetFetcher.setPropertiesToFetch(_pathsToFetch);

    List<Well> allData = wellSetFetcher.fetchAllData();
    assertEquals("fetch all data size", 3, allData.size());
    for (Well well : allData) {
      assertTrue("expected well", wellKeys.contains(well.getWellKey().toString()));
      // NOTE: We gave up on trying to filter collections when eager fetching;
      // Hibernate does not naturally support this via
      // EntityDataFetcher.getOrCreateFetchJoin()
//      assertEquals("empty restricted collection (resultValues)", 0, well.getResultValues().size());
//      assertEquals("empty restricted collection (annotationValues)", 0, well.<Reagent>getLatestReleasedReagent().getAnnotationValues().size());
    }
  }

  /**
   * Test eager fetching of multiple restricted collections, of same entity type
   * but with different restriction values, with restriction at path depth 1
   */
  public void testEagerFetchMultipleRestrictedCollectionsOfSameType()
  {
    _pathsToFetch.add(Well.resultValues.restrict(ResultValue.DataColumn.getLeaf(), _screenResult.getDataColumnsList().get(0)).toFullEntity());
    _pathsToFetch.add(Well.resultValues.restrict(ResultValue.DataColumn.getLeaf(), _screenResult.getDataColumnsList().get(2)).toFullEntity());
    _allWellsFetcher.setPropertiesToFetch(_pathsToFetch);
    List<Well> allData = _allWellsFetcher.fetchAllData();
    Collections.sort(allData);

    Well well = allData.get(0);
    assertEquals("well.resultValues restricted size",
                 // NOTE: Expected size is non-restricted size. We gave up on
                 // trying to filter collections when eager fetching; Hibernate
                 // does not naturally support this via
                 // EntityDataFetcher.getOrCreateFetchJoin()
                 16, // 2 /* selected DataColumns */,
                 well.getResultValues().size());

  }

  /**
   * Test eager fetching of multiple restricted collections, of different entity
   * types, with at least one of the collections having "sparse" (undefined)
   * values for the root entity
   */
  public void testEagerFetchMultipleRestrictedSparseCollectionsOfDifferentTypes()
  {
    _pathsToFetch.add(Well.resultValues.restrict(ResultValue.DataColumn.getLeaf(), _screenResult.getDataColumnsList().get(0)).toFullEntity());
    _pathsToFetch.add(Well.resultValues.restrict(ResultValue.DataColumn.getLeaf(), _screenResult.getDataColumnsList().get(2)).toFullEntity());
    _pathsToFetch.add(Well.resultValues.restrict(ResultValue.DataColumn.getLeaf(), _screenResult.getDataColumnsList().get(7)).toFullEntity());
    Iterator<AnnotationType> annotationTypesIter = _study.getAnnotationTypes().iterator();
    AnnotationType annotationType1 = annotationTypesIter.next();
    AnnotationType annotationType2 = annotationTypesIter.next();
    _pathsToFetch.add(Well.latestReleasedReagent.to(Reagent.annotationValues).restrict(AnnotationValue.annotationType.getLeaf(), annotationType1).toFullEntity());
    _pathsToFetch.add(Well.latestReleasedReagent.to(Reagent.annotationValues).restrict(AnnotationValue.annotationType.getLeaf(), annotationType2).toFullEntity());
    _pathsToFetch.add(Well.library.to(Library.contentsVersions).toFullEntity());
    _allWellsFetcher.setPropertiesToFetch(_pathsToFetch);
    List<Well> allData = _allWellsFetcher.fetchAllData();
    Collections.sort(allData);

    assertEquals("full data size",
                 _screenResult.getAssayWells().size(),
                 allData.size());

    // NOTE: In below asserts, for restricted collections expected size is
    // non-restricted size. We gave up on trying to filter collections when
    // eager fetching; Hibernate does not naturally support this via
    // EntityDataFetcher.getOrCreateFetchJoin()

    // this well should have values defined for each property
    Well well = allData.get(0);
    assertEquals("expected well", "02000:A01", well.getWellKey().toString());
    assertEquals("well.resultValues restricted size",
                 16, //3 /* selected DataColumns */,
                 well.getResultValues().size());
    assertEquals("well.reagent.annotationValues restricted size",
                 2 /* selected annotationTypes */,
                 well.<Reagent>getLatestReleasedReagent().getAnnotationValues().size());

    // this well should have values for all DataColumns, but undefined values for all AnnotTypes
    well = allData.get(2);
    assertEquals("expected well", "02000:A03", well.getWellKey().toString());
    assertEquals("well.resultValues restricted size",
                 16, //3 /* selected DataColumns */,
                 well.getResultValues().size());
    assertEquals("well.reagent.annotationValues restricted size",
                 0 /* selected annotationTypes */,
                 well.<Reagent>getLatestReleasedReagent().getAnnotationValues().size());

    // this well should have undefined values for one DataColumn and all AnnotTypes
    well = allData.get(3);
    assertEquals("expected well", "02000:A04", well.getWellKey().toString());
    assertEquals("well.resultValues restricted size",
                 14, //2 /* selected DataColumns */,
                 well.getResultValues().size());
    assertEquals("well.reagent.annotationValues restricted size",
                 0 /* selected annotationTypes */,
                 well.<Reagent>getLatestReleasedReagent().getAnnotationValues().size());
  }

  /**
   * Tests database-level filtering of EntityDataFetcher. The test is performed
   * by comparing the result of in-memory filtering of full result (the
   * "expected" result) to the restricted result returned by the database. So note
   * that this test is only verifying that the restricted result matches the
   * behavior of Criterion.match(). But this is okay, since we have explicit
   * tests for Criterion.match().
   */
  public void testAllFilterOperators()
  {
    final DataColumn numericCol = _screenResult.getDataColumnsList().get(0);
    final DataColumn textCol = _screenResult.getDataColumnsList().get(2);
    final DataColumn positiveCol = _screenResult.getDataColumnsList().get(6);
    final DataColumn commentCol = _screenResult.getDataColumnsList().get(7);
    PropertyPath<Well> numericColPropPath = Well.resultValues.restrict(ResultValue.DataColumn.getLeaf(), numericCol).toProperty("numericValue");
    PropertyPath<Well> textColPropPath = Well.resultValues.restrict(ResultValue.DataColumn.getLeaf(), textCol).toProperty("value");
    PropertyPath<Well> positiveColPropPath = Well.resultValues.restrict(ResultValue.DataColumn.getLeaf(), positiveCol).toProperty("value");
    // the "comments" DataColumn contains blank and missing (null) resultValues, allowing us to test the EMPTY and NOT_EMPTY operators
    PropertyPath<Well> commentColPropPath = Well.resultValues.restrict(ResultValue.DataColumn.getLeaf(), commentCol).toProperty("value");
    _pathsToFetch.add(numericColPropPath);
    _pathsToFetch.add(textColPropPath);
    _pathsToFetch.add(positiveColPropPath);
    _pathsToFetch.add(commentColPropPath);
    _allWellsFetcher.setPropertiesToFetch(_pathsToFetch);

    List<Well> allData = _allWellsFetcher.fetchAllData();
    assertEquals("sanity check that we're testing filtering against full data set", _screenResult.getAssayWells().size(), allData.size());
    Getter<Well,String> wellNameGetter = new Getter<Well,String>() { public String get(Well well) { return well.getWellName(); } };
    Getter<Well,Double> rvNumericValueGetter = new Getter<Well,Double>() { public Double get(Well well) { return well.getResultValues().get(numericCol).getNumericValue(); } };
    Getter<Well,String> ColextValueGetter = new Getter<Well,String>() { public String get(Well well) { return well.getResultValues().get(textCol).getValue(); } };
    Getter<Well,String> positiveValueGetter = new Getter<Well,String>() { public String get(Well well) { return well.getResultValues().get(positiveCol).getValue(); } };
    Getter<Well,String> commentValueGetter = new Getter<Well,String>() {
      public String get(Well well) {
        ResultValue rv = well.getResultValues().get(commentCol);
        return rv == null ? null : rv.getValue();
      }
    };
    for (Operator operator : Operator.ALL_OPERATORS) {
      doTestFilterOperator(RelationshipPath.from(Well.class).toProperty("wellName"), new Criterion<String>(operator, "B18"), wellNameGetter, allData);
      if (operator.getOperatorClass() == OperatorClass.EQUALITY ||
        operator.getOperatorClass() == OperatorClass.RANKING) {
        doTestFilterOperator(numericColPropPath, new Criterion<Double>(operator, new Double(1.0)), rvNumericValueGetter, allData);
        doTestFilterOperator(textColPropPath, new Criterion<String>(operator, "text00100"), ColextValueGetter, allData);
        doTestFilterOperator(positiveColPropPath, new Criterion<String>(operator, PartitionedValue.MEDIUM.getValue()), positiveValueGetter, allData);
      }
      else if (operator.getOperatorClass() == OperatorClass.EXTANT) {
        // note: main purpose of next line is to test the empty/not-empty operators, since this col will have some empty-string values and some null values
        doTestFilterOperator(commentColPropPath, new Criterion<String>(operator, "so so"), commentValueGetter, allData);
       }
      else if (operator.getOperatorClass() == OperatorClass.TEXT) {
        doTestFilterOperator(textColPropPath, new Criterion<String>(operator, "text*"), ColextValueGetter, allData);
        doTestFilterOperator(textColPropPath, new Criterion<String>(operator, "*100"), ColextValueGetter, allData);
        doTestFilterOperator(textColPropPath, new Criterion<String>(operator, "*xt*"), ColextValueGetter, allData);
        doTestFilterOperator(textColPropPath, new Criterion<String>(operator, "t*1"), ColextValueGetter, allData);
        doTestFilterOperator(textColPropPath, new Criterion<String>(operator, "*"), ColextValueGetter, allData);
      }
      else {
        fail("unhandled operator class: " + operator.getOperatorClass());
      }
    }
  }

  /**
   * Tests that collection of elements can be eager fetched just like collection
   * of entities. Thought this was a problem at one point, but isn't, but
   * keeping test around anyway.
   */
  public void testFetchCollectionOfElements()
  {
    _wellSetFetcher.setPropertiesToFetch(Lists.newArrayList(Well.latestReleasedReagent.to(SilencingReagent.facilityGene).to(Gene.genbankAccessionNumbers),
                                                            Well.library.to(Library.contentsVersions).toFullEntity()));
    List<Well> data = _wellSetFetcher.fetchAllData();
    assertEquals("well.reagents.facilityGene.genbankAccessionNumbers size", 1, data.get(0).<SilencingReagent>getLatestReleasedReagent().getFacilityGene().getGenbankAccessionNumbers().size());
  }


  /**
   * Tests that collection of elements can be filtered, using an empty string
   * for PropertyPath.propertyName.
   */
  public void testFilterCollectionOfElements()
  {
    PropertyPath<Well> propPath = Well.latestReleasedReagent.to(SilencingReagent.facilityGene).to(Gene.genbankAccessionNumbers);
    _wellSetFetcher.setPropertiesToFetch(Collections.singletonList(propPath));
    Map<PropertyPath<Well>,List<? extends Criterion<?>>> filteringCriteria = new HashMap<PropertyPath<Well>,List<? extends Criterion<?>>>();
    filteringCriteria.put(propPath, Collections.singletonList(new Criterion<String>(Operator.EQUAL, "GB3074279")));
    _wellSetFetcher.setFilteringCriteria(filteringCriteria);
    List<String> keys = _wellSetFetcher.findAllKeys();
    assertEquals("result size", 1, keys.size());
    assertEquals("filtered result", "02001:P24", keys.get(0));
  }
  
  public void testFetchEmptySet()
  {
    _wellSetFetcher = new EntityDataFetcher<Well,String>(Well.class, genericEntityDao) {
      @Override
      public void addDomainRestrictions(HqlBuilder hql)
      {
        DataFetcherUtil.addDomainRestrictions(hql, getRootAlias(), Collections.<String>emptySet());
      }
    };
    List<String> keys = _wellSetFetcher.findAllKeys();
    assertEquals("result size", 0, keys.size());
  }


  // private methods

  private void doTestFilterOperator(PropertyPath<Well> propertyPath,
                                    Criterion<?> criterion,
                                    Getter<Well,?> getter,
                                    List<Well> allData)
  {
    log.debug("testing criterion " + criterion + " against property " + propertyPath);
    Set<String> expectedKeys = new HashSet<String>();
    for (Well well : allData) {
      if (criterion.matches(getter.get(well))) {
        expectedKeys.add(well.getWellKey().toString());
      }
    }
    log.debug("expected data size = " + expectedKeys.size());

    Map<PropertyPath<Well>,List<? extends Criterion<?>>> criteria = new HashMap<PropertyPath<Well>,List<? extends Criterion<?>>>();
    criteria.put(propertyPath, Collections.singletonList(criterion));
    _allWellsFetcher.setFilteringCriteria(criteria);
    Set<String> actualKeys = new HashSet<String>(_allWellsFetcher.findAllKeys());
    assertEquals(expectedKeys, actualKeys);
  }

  @SuppressWarnings("unchecked")
  private <T extends Comparable> void isSorted(List<T> data)
  {
    T last = null;
    for (T item : data) {
      if (last != null) {
        assertTrue("sorted values", last.compareTo(item) <= 0);
      }
      last = item;
    }
  }
}
