// $HeadURL$
// $Id$
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.datafetcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.screenresults.DataColumn;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.test.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.test.MakeDummyEntities;

public class TupleDataFetcherTest extends AbstractSpringPersistenceTest
{
  public void testSimpleEntity()
  {
    LabHead labHead = dataFactory.newInstance(LabHead.class);
    ScreeningRoomUser user1 = dataFactory.newInstance(ScreeningRoomUser.class);
    user1.setLab(labHead.getLab());
    ScreeningRoomUser user2 = dataFactory.newInstance(ScreeningRoomUser.class);
    genericEntityDao.saveOrUpdateEntity(labHead);
    genericEntityDao.saveOrUpdateEntity(user1);
    genericEntityDao.saveOrUpdateEntity(user2);

    TupleDataFetcher<ScreeningRoomUser,Integer> dataFetcher =
      new TupleDataFetcher<ScreeningRoomUser,Integer>(ScreeningRoomUser.class, genericEntityDao);
    
    ArrayList<PropertyPath<ScreeningRoomUser>> properties = 
      Lists.newArrayList(RelationshipPath.from(ScreeningRoomUser.class).toProperty("id"),
                         RelationshipPath.from(ScreeningRoomUser.class).toProperty("lastName"),
                         RelationshipPath.from(ScreeningRoomUser.class).toProperty("firstName"),
                         RelationshipPath.from(ScreeningRoomUser.class).toProperty("loginId"),
                         RelationshipPath.from(ScreeningRoomUser.class).toProperty("dateCreated"),
                         ScreeningRoomUser.LabHead.toProperty("lastName"));
    dataFetcher.setPropertiesToFetch(properties);
      
    List<Tuple<Integer>> result = dataFetcher.fetchAllData();
    Collections.sort(result, new Comparator<Tuple<Integer>>() {
      @Override
      public int compare(Tuple<Integer> o1, Tuple<Integer> o2)
      {
        return o1.getKey().compareTo(o2.getKey());
      }
    });
    assertEquals(3, result.size());
    assertEquals(labHead.getEntityId(), result.get(0).getKey());
    assertEquals(labHead.getEntityId(), result.get(0).getProperty("id"));
    assertEquals(labHead.getLastName(), result.get(0).getProperty("lastName"));
    assertEquals(labHead.getDateCreated(), result.get(0).getProperty("dateCreated"));
    assertNull(result.get(0).getProperty("labHead.lastName"));

    assertEquals(user1.getEntityId(), result.get(1).getKey());
    assertEquals(user1.getEntityId(), result.get(1).getProperty("id"));
    assertEquals(user1.getLastName(), result.get(1).getProperty("lastName"));
    assertEquals(user1.getDateCreated(), result.get(1).getProperty("dateCreated"));
    assertEquals(labHead.getLastName(), result.get(1).getProperty("labHead.lastName"));

    assertEquals(user2.getEntityId(), result.get(2).getKey());
    assertEquals(user2.getEntityId(), result.get(2).getProperty("id"));
    assertEquals(user2.getLastName(), result.get(2).getProperty("lastName"));
    assertEquals(user2.getDateCreated(), result.get(2).getProperty("dateCreated"));
    assertNull(result.get(0).getProperty("labHead.lastName"));
    
    Map<Integer,Tuple<Integer>> result2 = dataFetcher.fetchData(ImmutableSet.of(user1.getEntityId()));
    assertEquals(result.get(1), result2.get(user1.getEntityId()));
  }

  public void testRestrictedToManyRelationship()
  {
    Library library = MakeDummyEntities.makeDummyLibrary(1, ScreenType.RNAI, 1);
    genericEntityDao.saveOrUpdateEntity(library);
    Screen screen = MakeDummyEntities.makeDummyScreen(1, ScreenType.RNAI);
    ScreenResult screenResult = MakeDummyEntities.makeDummyScreenResult(screen, library);
    genericEntityDao.saveOrUpdateEntity(screen);

    TupleDataFetcher<Well,String> dataFetcher =
      new TupleDataFetcher<Well,String>(Well.class, genericEntityDao);
    DataColumn numericDataColumn = screenResult.getDataColumnsList().get(0);
    assert numericDataColumn != null && numericDataColumn.getName().equals("numeric_repl1");
    DataColumn positiveDataColumn = screenResult.getDataColumnsList().get(6);
    assert positiveDataColumn != null && positiveDataColumn.getName().equals("positive");
    ArrayList<PropertyPath<Well>> properties =
      Lists.newArrayList(RelationshipPath.from(Well.class).toProperty("id"),
                         Well.library.toProperty("libraryName"),
                         Well.resultValues.restrict(ResultValue.DataColumn.getLeaf(), numericDataColumn).toProperty("numericValue"),
                         Well.resultValues.restrict(ResultValue.DataColumn.getLeaf(), numericDataColumn).toProperty("positive"),
                         Well.resultValues.restrict(ResultValue.DataColumn.getLeaf(), positiveDataColumn).toProperty("value"),
                         Well.resultValues.restrict(ResultValue.DataColumn.getLeaf(), positiveDataColumn).toProperty("positive"));
    dataFetcher.setPropertiesToFetch(properties);

    List<Tuple<String>> result = dataFetcher.fetchAllData();
    assertEquals(384, result.size());
    Map<String,Double> expectedNumericValues = Maps.newHashMap();
    for (ResultValue resultValue : numericDataColumn.getResultValues()) {
      expectedNumericValues.put(resultValue.getWell().getWellId(), resultValue.getNumericValue());
    }
    assertColumnEquals(expectedNumericValues,
                       result,
                       TupleDataFetcher.makePropertyKey(properties.get(2)));
    Map<String,Boolean> expectedPositiveValues = Maps.newHashMap();
    for (ResultValue resultValue : positiveDataColumn.getResultValues()) {
      expectedPositiveValues.put(resultValue.getWell().getWellId(), resultValue.isPositive());
    }
    assertColumnEquals(expectedPositiveValues,
                       result,
                       TupleDataFetcher.makePropertyKey(properties.get(5)));

    String tupleKey = result.get(0).getKey();
    Map<String,Tuple<String>> result2 = dataFetcher.fetchData(ImmutableSet.of(tupleKey));
    assertEquals(result.get(0), result2.get(tupleKey));
  }

  public void testFetchEntityAsTupleValue()
  {
    Library library = MakeDummyEntities.makeDummyLibrary(1, ScreenType.RNAI, 1);
    genericEntityDao.saveOrUpdateEntity(library);

    TupleDataFetcher<Well,String> dataFetcher =
      new TupleDataFetcher<Well,String>(Well.class, genericEntityDao);
    ArrayList<PropertyPath<Well>> properties =
      Lists.newArrayList(RelationshipPath.from(Well.class).toProperty("id"),
                         Well.latestReleasedReagent.toProperty("id"),
                         Well.latestReleasedReagent.toFullEntity());
    dataFetcher.setPropertiesToFetch(properties);
    List<Tuple<String>> result = dataFetcher.fetchAllData();
    assertNotNull(result.get(0).getProperty("latestReleasedReagent.*"));
    assertTrue(result.get(0).getProperty("latestReleasedReagent.*") instanceof Reagent);
    assertEquals(result.get(0).getProperty("latestReleasedReagent.id"),
                 ((Reagent) result.get(0).getProperty("latestReleasedReagent.*")).getEntityId());
  }

  public void testFetchCollectionOfValuesAsTupleValue()
  {
    Library library = MakeDummyEntities.makeDummyLibrary(1, ScreenType.RNAI, 1);
    Well well = library.getWells().iterator().next();
    ((SilencingReagent) well.getLatestReleasedReagent()).getFacilityGene().withEntrezgeneSymbol("xxx").withEntrezgeneSymbol("yyy");
    genericEntityDao.saveOrUpdateEntity(library);

    TupleDataFetcher<Well,String> dataFetcher =
      new TupleDataFetcher<Well,String>(Well.class, genericEntityDao);
    ArrayList<PropertyPath<Well>> properties =
      Lists.newArrayList(RelationshipPath.from(Well.class).toProperty("id"),
                         Well.latestReleasedReagent.toProperty("id"),
                         Well.latestReleasedReagent.to(SilencingReagent.facilityGene).to(Gene.entrezgeneSymbols));
    dataFetcher.setPropertiesToFetch(properties);
    Map<String,Tuple<String>> result = dataFetcher.fetchData(Sets.newHashSet(well.getWellId()));
    Tuple<String> tuple = result.get(well.getWellId());
    assertNotNull(tuple);
    Object propValue = tuple.getProperty("latestReleasedReagent.facilityGene.entrezgeneSymbols");
    assertNotNull(propValue);
    assertTrue(propValue instanceof List);
    assertEquals(Sets.newHashSet("xxx", "yyy"), Sets.newHashSet((List<String>) propValue));
  }

  private <K,V> void assertColumnEquals(Map<String,V> expectedNumericValues, List<Tuple<K>> result, String propertyKey)
  {
    Map<K,V> actualValues = Maps.newHashMap();
    for (Tuple<K> tuple : result) {
      actualValues.put(tuple.getKey(), (V) tuple.getProperty(propertyKey));
    }
    assertEquals(expectedNumericValues, actualValues);
  }
}
