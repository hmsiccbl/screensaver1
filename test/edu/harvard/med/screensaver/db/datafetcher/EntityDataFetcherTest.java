// $HeadURL:
// svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml
// $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $

// Copyright 2006 by the President and Fellows of Harvard College.

// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.datafetcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.PropertyPath;
import edu.harvard.med.screensaver.model.RelationshipPath;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.PartitionedValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.ui.table.Criterion;
import edu.harvard.med.screensaver.ui.table.Criterion.Operator;
import edu.harvard.med.screensaver.ui.table.Criterion.OperatorClass;

import org.apache.log4j.Logger;

public class EntityDataFetcherTest extends AbstractSpringPersistenceTest
{
  // static members

  private static Logger log = Logger.getLogger(EntityDataFetcherTest.class);


  // instance data members

  private EntityDataFetcher<Well,String> _screenResultWellFetcher;
  private EntityDataFetcher<Well,String> _wellSetFetcher;
  private EntityDataFetcher<Well,String> _allWellsFetcher;
  private List<RelationshipPath<Well>> _relationships = new ArrayList<RelationshipPath<Well>>();
  private List<PropertyPath<Well>> _properties = new ArrayList<PropertyPath<Well>>();
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
  protected void onSetUp() throws Exception
  {
    if (!oneTimeDataSetup) {
      oneTimeDataSetup = true;
      super.onSetUp();
      genericEntityDao.doInTransaction(new DAOTransaction() {

        public void runTransaction()
        {
          _rnaiScreen = MakeDummyEntities.makeDummyScreen(2, ScreenType.RNAI);
          _rnaiLibrary = MakeDummyEntities.makeDummyLibrary(_rnaiScreen.getScreenNumber(),
                                                            _rnaiScreen.getScreenType(),
                                                            _plates);
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
        genericEntityDao.needReadOnly(_screenResult, "resultValueTypes.resultValues");
        genericEntityDao.needReadOnly(_screenResult, "wells");
        _study = genericEntityDao.reloadEntity(_study, true, "annotationTypes.annotationValues");
        _rnaiLibrary = genericEntityDao.reloadEntity(_rnaiLibrary, true, "wells");
      }
    });

    _plateNumberPropPath = new PropertyPath<Well>(Well.class, "plateNumber");
    _wellNamePropPath = new PropertyPath<Well>(Well.class, "wellName");
    _properties.add(_plateNumberPropPath);
    _properties.add(_wellNamePropPath);
    _relationships.add(_plateNumberPropPath.getRelationshipPath());
    _relationships.add(_wellNamePropPath.getRelationshipPath());

    _screenResultWellFetcher = new ParentedEntityDataFetcher<Well,String>(Well.class,
                                                                          new RelationshipPath<Well>(Well.class,
                                                                                                     "screenResults"),
                                                                          _screenResult,
                                                                          genericEntityDao);
    Set<String> wellKeys = new HashSet<String>(Arrays.asList("02000:A01",
                                                             "02001:P24"));
    _wellSetFetcher = new EntitySetDataFetcher<Well,String>(Well.class,
                                                            wellKeys,
                                                            genericEntityDao);
    _allWellsFetcher = new AllEntitiesOfTypeDataFetcher<Well,String>(Well.class,
                                                                     genericEntityDao);
  }

  /**
   * Test fetching all data of a single-entity (shallow) entity network.
   */
  public void testShallowFetchAllData()
  {
    _screenResultWellFetcher.setRelationshipsToFetch(_relationships);
    _wellSetFetcher.setRelationshipsToFetch(_relationships);
    _allWellsFetcher.setRelationshipsToFetch(_relationships);

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
    _screenResultWellFetcher.setRelationshipsToFetch(_relationships);
    _screenResultWellFetcher.setOrderBy(_properties);

    Well[] wellsArray = _screenResult.getWells().toArray(new Well[] {});
    Set<String> wellKeys = new HashSet<String>();
    for (int i = 24; i < 48; i++) {
      wellKeys.add(wellsArray[i].getWellKey().toString());
    }

    List<String> foundKeys = _screenResultWellFetcher.findAllKeys();
    assertEquals("size", 384 * _plates, foundKeys.size());
    isSorted(foundKeys);

    Map<String,Well> data = _screenResultWellFetcher.fetchData(wellKeys);
    assertEquals("size", 48 - 24, data.size());
    for (int i = 24; i < 48; i++) {
      assertTrue(data.containsKey(wellsArray[i].getWellKey().toString()));
      assertEquals(wellsArray[i].getWellKey(),
                   data.get(wellsArray[i].getWellKey().toString()).getWellKey());
    }
  }

  /**
   * Test fetching a subset (data range) of a single-entity (shallow) entity network,
   * with filtering criteria, sorted by ID.
   */
  public void testShallowFetchDataRangeFiltered()
  {
    _screenResultWellFetcher.setRelationshipsToFetch(_relationships);
    _screenResultWellFetcher.setOrderBy(_properties);
    _criteria.put(_wellNamePropPath, Arrays.asList(new Criterion<String>(Operator.TEXT_STARTS_WITH, "B2")));
    _criteria.put(_plateNumberPropPath, Arrays.asList(new Criterion<Integer>(Operator.EQUAL, 2001)));
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
      assertEquals(expectedWellKey, data.get(expectedWellKey.toString()).getWellKey().toString());
    }
  }

  /**
   * Test fetching a subset (data range) of a multiple-entity (deep) entity network,
   * with filtering criteria on 2 properties of restricted collections.
   */
  public void testDeepFetchDataRangeFiltered()
  {
    ResultValueType rvt1 = _screenResult.getResultValueTypesList().get(0);
    ResultValueType rvt2 = _screenResult.getResultValueTypesList().get(2);
    ResultValueType rvt3 = _screenResult.getResultValueTypesList().get(6);
    ResultValueType rvt4 = _screenResult.getResultValueTypesList().get(7); // "comments", with sparse result values
    PropertyPath<Well> propertyPath1 = new PropertyPath<Well>(Well.class,
                                                              "resultValues[resultValueType]",
                                                              "value",
                                                              rvt1);
    PropertyPath<Well> propertyPath2 = new PropertyPath<Well>(Well.class,
                                                              "resultValues[resultValueType]",
                                                              "value",
                                                              rvt2);
    PropertyPath<Well> propertyPath3 = new PropertyPath<Well>(Well.class,
                                                              "resultValues[resultValueType]",
                                                              "positive",
                                                              rvt3);
    PropertyPath<Well> propertyPath4 = new PropertyPath<Well>(Well.class,
                                                              "resultValues[resultValueType]",
                                                              "value",
                                                              rvt4);
    _relationships.add(propertyPath1.getRelationshipPath());
    _relationships.add(propertyPath2.getRelationshipPath());
    _relationships.add(propertyPath3.getRelationshipPath());
    _relationships.add(propertyPath4.getRelationshipPath());
    _criteria.put(propertyPath3, Arrays.asList(new Criterion<Boolean>(Operator.EQUAL, true)));
    _criteria.put(propertyPath4, Arrays.asList(new Criterion<String>(Operator.NOT_EMPTY, null)));

    _screenResultWellFetcher.setRelationshipsToFetch(_relationships);
    _screenResultWellFetcher.setFilteringCriteria(_criteria);

    List<String> expectedWellKeys = new ArrayList<String>();
    for (Well well : _screenResult.getWells()) {
      if (rvt3.getWellKeyToResultValueMap().get(well.getWellKey()).isPositive() &&
        rvt4.getWellKeyToResultValueMap().get(well.getWellKey()) != null &&
        rvt4.getWellKeyToResultValueMap().get(well.getWellKey()).getValue().length() > 0) {
        expectedWellKeys.add(well.getWellKey().toString());
      }
    }
    Collections.sort(expectedWellKeys);

    List<String> foundWellKeys = _screenResultWellFetcher.findAllKeys();
    Collections.sort(foundWellKeys);
    assertEquals("keys", expectedWellKeys, foundWellKeys);

    Map<String,Well> data = _screenResultWellFetcher.fetchData(new HashSet<String>(expectedWellKeys));
    assertEquals("size", expectedWellKeys.size(), data.size());
    for (String expectedWellKey : expectedWellKeys) {
      assertTrue(data.containsKey(expectedWellKey));
      assertEquals(expectedWellKey,
                   data.get(expectedWellKey.toString()).getWellKey().toString());
    }
  }

  /**
   * Explicitly test that a relationship path that is longer than 2 elements (in
   * this case 4), is handled correctly. This is a regression test for a bug
   * caused by earlier misuse of Criteria.setFetchMode(), which requires *full*
   * paths to be specified from the root criteria, rather than relative paths
   * being set from each subcriteria (internal nodes in the Criteria tree). We
   * no longer use the Criteria API, but the more tests, the better.
   */
  public void testFetchLongRelationshipPath()
  {
    EntityDataFetcher<Library,Integer> librariesDataFetcher =
      new AllEntitiesOfTypeDataFetcher<Library,Integer>(Library.class, genericEntityDao);
    List<RelationshipPath<Library>> relationships = new ArrayList<RelationshipPath<Library>>();
    relationships.add(new RelationshipPath<Library>(Library.class, "wells.gene.genbankAccessionNumbers"));
    librariesDataFetcher.setRelationshipsToFetch(relationships);
    List<Library> libraries = librariesDataFetcher.fetchAllData();
    assertEquals("library.wells.gene.genbankAccessionNumbers size",
                 1,
                 libraries.iterator().next().getWells().iterator().next().getGene().getGenbankAccessionNumbers().size());
  }

  /**
   * Test that unrestricted collections are returned in their entirety, as a
   * sanity check for the "restricted collection" tests, below
   */
  public void testEagerFetchUnrestrictedCollections()
  {
    _relationships.add(new RelationshipPath<Well>(Well.class, "resultValues"));
    _allWellsFetcher.setRelationshipsToFetch(_relationships);
    List<Well> allData = _allWellsFetcher.fetchAllData();
    Collections.sort(allData);
    Well well = allData.get(0);
    assertEquals("well.resultValues unrestricted size",
                 _screenResult.getResultValueTypes().size() /*RVTs per screen result*/ * 2 /* screen results*/,
                 well.getResultValues().size());

    _relationships.clear();
    _relationships.add(new RelationshipPath<Well>(Well.class, "reagent.annotationValues"));
    _allWellsFetcher.setRelationshipsToFetch(_relationships);
    allData = _allWellsFetcher.fetchAllData();
    Collections.sort(allData);
    well = allData.get(0);
    assertEquals("well.reagent.annotationValues restricted size",
                 _study.getAnnotationTypes().size() /*ATs per study*/ * 1 /*study*/,
                 well.getReagent().getAnnotationValues().size());
  }

  /**
   * Test eager fetching of a single restricted collection with restriction at
   * path depth 2
   */
  public void testEagerFetchRestrictedCollection()
  {
    _relationships.add(new RelationshipPath<Well>(Well.class, "resultValues.resultValueType[screenResult]", _screenResult));
    _allWellsFetcher.setRelationshipsToFetch(_relationships);
    List<Well> allData = _allWellsFetcher.fetchAllData();
    Collections.sort(allData);

    // test a well that has a RV for every RVT
    Well well = allData.get(0);
    assertEquals("expected well", "02000:A01", well.getWellKey().toString());
    assertEquals("well.resultValues restricted size",
                 // NOTE: Expected size is non-restricted size. We gave up on
                 // trying to filter collections when eager fetching; Hibernate
                 // does not naturally support this via
                 // EntityDataFetcher.getOrCreateFetchJoin()
                 16, //_screenResult.getResultValueTypes().size(),
                 well.getResultValues().size());

    // test a well that does not have a RV for every RVT
    well = allData.get(3);
    assertEquals("expected well", "02000:A04", well.getWellKey().toString());
    assertEquals("well.resultValues restricted size",
                 // NOTE: Expected size is non-restricted size. We gave up on
                 // trying to filter collections when eager fetching; Hibernate
                 // does not naturally support this via
                 // EntityDataFetcher.getOrCreateFetchJoin()
                 16 - 2 /*_screenResult.getResultValueTypes().size() - 1*/,
                 well.getResultValues().size());
  }

  // TODO: implement
//  /**
//   * Test single restricted collection with restrictions at multiple depths
//   */
//  public void testEagerFetchTriplyRestrictedCollection()
//  {
//    _allWellsFetcher.setRelationshipsToFetch(Arrays.asList(new RelationshipPath<Well>(Well.class, "screenResults[screenResult].annotationTypes[annotationType].annotationValues[reagent]", _screenResult, annotationType, reagent)));
//    Well well = _allWellsFetcher.fetchAllData().get(0);
//    assertEquals("well.screenResults restricted size", 1, well.getScreenResults().size());
//    ScreenResult restrictedScreenResult = well.getScreenResults().iterator().next();
//    assertEquals("well.screenResults restricted", _screenResult, restrictedScreenResult);
//    assertTrue("well.screenResult.annotationTypes",
//               well.getScreenResults().size() == 1 && well.getScreenResults().iterator().next().equals(_screenResult));
//    assertEquals("well.screenResult[].annotationTypes[] restricted size",
//                 1,
//                 well.getScreenResults(). ResultValues().size());
//  }

  /**
   * Test eager fetching of restricted collections that are empty when restricted,
   * verifying that all root entities are still returned in the result.
   */
  public void testEagerFetchEmptyRestrictedCollection()
  {
    Set<String> wellKeys = new HashSet<String>(Arrays.asList("02000:A04", "02000:A08", "02000:A12"));
    EntityDataFetcher<Well,String> wellSetFetcher = new EntitySetDataFetcher<Well,String>(Well.class,
      wellKeys,
      genericEntityDao);
    ResultValueType commentsRvt = _screenResult.getResultValueTypesList().get(7); // has with sparse result values
    _relationships.add(new RelationshipPath<Well>(Well.class, "reagent.annotationValues"));
    _relationships.add(new RelationshipPath<Well>(Well.class, "resultValues[resultValueType]", commentsRvt));
    wellSetFetcher.setRelationshipsToFetch(_relationships);

    List<Well> allData = wellSetFetcher.fetchAllData();
    assertEquals("fetch all data size", 3, allData.size());
    for (Well well : allData) {
      assertTrue("expected well", wellKeys.contains(well.getWellKey().toString()));
      // NOTE: We gave up on trying to filter collections when eager fetching;
      // Hibernate does not naturally support this via
      // EntityDataFetcher.getOrCreateFetchJoin()
//      assertEquals("empty restricted collection (resultValues)", 0, well.getResultValues().size());
//      assertEquals("empty restricted collection (annotationValues)", 0, well.getReagent().getAnnotationValues().size());
    }
  }

  /**
   * Test eager fetching of multiple restricted collections, of same entity type
   * but with different restriction values, with restriction at path depth 1
   */
  public void testEagerFetchMultipleRestrictedCollectionsOfSameType()
  {
    _relationships.add(new RelationshipPath<Well>(Well.class, "resultValues[resultValueType]", _screenResult.getResultValueTypesList().get(0)));
    _relationships.add(new RelationshipPath<Well>(Well.class, "resultValues[resultValueType]", _screenResult.getResultValueTypesList().get(2)));
    _allWellsFetcher.setRelationshipsToFetch(_relationships);
    List<Well> allData = _allWellsFetcher.fetchAllData();
    Collections.sort(allData);

    Well well = allData.get(0);
    assertEquals("well.resultValues restricted size",
                 // NOTE: Expected size is non-restricted size. We gave up on
                 // trying to filter collections when eager fetching; Hibernate
                 // does not naturally support this via
                 // EntityDataFetcher.getOrCreateFetchJoin()
                 16, // 2 /* selected RVTs */,
                 well.getResultValues().size());

  }

  /**
   * Test eager fetching of multiple restricted collections, of different entity
   * types, with at least one of the collections having "sparse" (undefined)
   * values for the root entity
   */
  public void testEagerFetchMultipleRestrictedSparseCollectionsOfDifferentTypes()
  {
    _relationships.add(new RelationshipPath<Well>(Well.class, "resultValues[resultValueType]", _screenResult.getResultValueTypesList().get(0)));
    _relationships.add(new RelationshipPath<Well>(Well.class, "resultValues[resultValueType]", _screenResult.getResultValueTypesList().get(2)));
    _relationships.add(new RelationshipPath<Well>(Well.class, "resultValues[resultValueType]", _screenResult.getResultValueTypesList().get(7)));
    Iterator<AnnotationType> annotationTypesIter = _study.getAnnotationTypes().iterator();
    AnnotationType annotationType1 = annotationTypesIter.next();
    AnnotationType annotationType2 = annotationTypesIter.next();
    _relationships.add(new RelationshipPath<Well>(Well.class, "reagent.annotationValues[annotationType]", annotationType1));
    _relationships.add(new RelationshipPath<Well>(Well.class, "reagent.annotationValues[annotationType]", annotationType2));
    _allWellsFetcher.setRelationshipsToFetch(_relationships);
    List<Well> allData = _allWellsFetcher.fetchAllData();
    Collections.sort(allData);

    assertEquals("full data size",
                 _screenResult.getWells().size(),
                 allData.size());

    // NOTE: In below asserts, for restricted collections expected size is
    // non-restricted size. We gave up on trying to filter collections when
    // eager fetching; Hibernate does not naturally support this via
    // EntityDataFetcher.getOrCreateFetchJoin()

    // this well should have values defined for each property
    Well well = allData.get(0);
    assertEquals("expected well", "02000:A01", well.getWellKey().toString());
    assertEquals("well.resultValues restricted size",
                 16, //3 /* selected RVTs */,
                 well.getResultValues().size());
    assertEquals("well.reagent.annotationValues restricted size",
                 2 /* selected annotationTypes */,
                 well.getReagent().getAnnotationValues().size());

    // this well should have values for all RVTs, but undefined values for all AnnotTypes
    well = allData.get(2);
    assertEquals("expected well", "02000:A03", well.getWellKey().toString());
    assertEquals("well.resultValues restricted size",
                 16, //3 /* selected RVTs */,
                 well.getResultValues().size());
    assertEquals("well.reagent.annotationValues restricted size",
                 0 /* selected annotationTypes */,
                 well.getReagent().getAnnotationValues().size());

    // this well should have undefined values for one RVT and all AnnotTypes
    well = allData.get(3);
    assertEquals("expected well", "02000:A04", well.getWellKey().toString());
    assertEquals("well.resultValues restricted size",
                 14, //2 /* selected RVTs */,
                 well.getResultValues().size());
    assertEquals("well.reagent.annotationValues restricted size",
                 0 /* selected annotationTypes */,
                 well.getReagent().getAnnotationValues().size());
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
    final ResultValueType numericRvt = _screenResult.getResultValueTypesList().get(0);
    final ResultValueType textRvt = _screenResult.getResultValueTypesList().get(2);
    final ResultValueType positiveRvt = _screenResult.getResultValueTypesList().get(6);
    final ResultValueType commentRvt = _screenResult.getResultValueTypesList().get(7);
    PropertyPath<Well> numericRvtPropPath = new PropertyPath<Well>(Well.class, "resultValues[resultValueType]", "numericValue", numericRvt);
    PropertyPath<Well> textRvtPropPath = new PropertyPath<Well>(Well.class, "resultValues[resultValueType]", "value", textRvt);
    PropertyPath<Well> positiveRvtPropPath = new PropertyPath<Well>(Well.class, "resultValues[resultValueType]", "value", positiveRvt);
    // the "comments" RVT contains blank and missing (null) ResultValues, allowing us to test the EMPTY and NOT_EMPTY operators
    PropertyPath<Well> commentRvtPropPath = new PropertyPath<Well>(Well.class, "resultValues[resultValueType]", "value", commentRvt);
    _relationships.add(numericRvtPropPath.getRelationshipPath());
    _relationships.add(textRvtPropPath.getRelationshipPath());
    _relationships.add(positiveRvtPropPath.getRelationshipPath());
    _relationships.add(commentRvtPropPath.getRelationshipPath());
    _allWellsFetcher.setRelationshipsToFetch(_relationships);

    List<Well> allData = _allWellsFetcher.fetchAllData();
    assertEquals("sanity check that we're testing filtering against full data set", _screenResult.getWells().size(), allData.size());
    Getter<Well,String> wellNameGetter = new Getter<Well,String>() { public String get(Well well) { return well.getWellName(); } };
    Getter<Well,Double> rvNumericValueGetter = new Getter<Well,Double>() { public Double get(Well well) { return well.getResultValues().get(numericRvt).getNumericValue(); } };
    Getter<Well,String> rvTextValueGetter = new Getter<Well,String>() { public String get(Well well) { return well.getResultValues().get(textRvt).getValue(); } };
    Getter<Well,String> positiveValueGetter = new Getter<Well,String>() { public String get(Well well) { return well.getResultValues().get(positiveRvt).getValue(); } };
    Getter<Well,String> commentValueGetter = new Getter<Well,String>() {
      public String get(Well well) {
        ResultValue rv = well.getResultValues().get(commentRvt);
        return rv == null ? null : rv.getValue();
      }
    };
    for (Operator operator : Operator.ALL_OPERATORS) {
      doTestFilterOperator(new PropertyPath<Well>(Well.class, "wellName"), new Criterion<String>(operator, "B18"), wellNameGetter, allData);
      if (operator.getOperatorClass() == OperatorClass.EQUALITY ||
        operator.getOperatorClass() == OperatorClass.RANKING) {
        doTestFilterOperator(numericRvtPropPath, new Criterion<Double>(operator, new Double(1.0)), rvNumericValueGetter, allData);
        doTestFilterOperator(textRvtPropPath, new Criterion<String>(operator, "text00100"), rvTextValueGetter, allData);
        doTestFilterOperator(positiveRvtPropPath, new Criterion<String>(operator, PartitionedValue.MEDIUM.getValue()), positiveValueGetter, allData);
      }
      else if (operator.getOperatorClass() == OperatorClass.EXTANT) {
        // note: main purpose of next line is to test the empty/not-empty operators, since this rvt will have some empty-string values and some null values
        doTestFilterOperator(commentRvtPropPath, new Criterion<String>(operator, "so so"), commentValueGetter, allData);
       }
      else if (operator.getOperatorClass() == OperatorClass.TEXT) {
        doTestFilterOperator(textRvtPropPath, new Criterion<String>(operator, "text*"), rvTextValueGetter, allData);
        doTestFilterOperator(textRvtPropPath, new Criterion<String>(operator, "*100"), rvTextValueGetter, allData);
        doTestFilterOperator(textRvtPropPath, new Criterion<String>(operator, "*xt*"), rvTextValueGetter, allData);
        doTestFilterOperator(textRvtPropPath, new Criterion<String>(operator, "t*1"), rvTextValueGetter, allData);
        doTestFilterOperator(textRvtPropPath, new Criterion<String>(operator, "*"), rvTextValueGetter, allData);
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
    _wellSetFetcher.setRelationshipsToFetch(Arrays.asList(new RelationshipPath<Well>(Well.class, "gene.genbankAccessionNumbers")));
    List<Well> data = _wellSetFetcher.fetchAllData();
    assertEquals("well.gene.genbankAccessionNumbers size", 1, data.get(0).getGene().getGenbankAccessionNumbers().size());
  }


  /**
   * Tests that collection of elements can be filtered, using an empty string
   * for PropertyPath.propertyName.
   */
  public void testFilterCollectionOfElements()
  {
    PropertyPath<Well> propertyPath = new PropertyPath<Well>(Well.class, "gene.genbankAccessionNumbers", "");
    _wellSetFetcher.setRelationshipsToFetch(Arrays.asList(propertyPath.getRelationshipPath()));
    Map<PropertyPath<Well>,List<? extends Criterion<?>>> filteringCriteria = new HashMap<PropertyPath<Well>,List<? extends Criterion<?>>>();
    filteringCriteria.put(propertyPath, Arrays.asList(new Criterion<String>(Operator.EQUAL, "GB768767")));
    _wellSetFetcher.setFilteringCriteria(filteringCriteria);
    List<String> keys = _wellSetFetcher.findAllKeys();
    assertEquals("result size", 1, keys.size());
    assertEquals("filtered result", "02001:P24", keys.get(0));
  }
  
  public void testFetchEmptySet()
  {
    _wellSetFetcher = new EntitySetDataFetcher<Well,String>(Well.class,
      Collections.<String>emptySet(),
      genericEntityDao);
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
    criteria.put(propertyPath, Arrays.asList(criterion));
    _allWellsFetcher.setFilteringCriteria(criteria);
    Set<String> actualKeys = new HashSet<String>(_allWellsFetcher.findAllKeys());
    assertEquals(expectedKeys, actualKeys);
  }

  @SuppressWarnings("unchecked")
  private <T> void isSorted(List<T> data, Comparator<T> comparator)
  {
    T last = null;
    for (T item : data) {
      if (last != null) {
        assertTrue("sorted values", comparator.compare(last, item) <= 0);
      }
      last = item;
    }
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
