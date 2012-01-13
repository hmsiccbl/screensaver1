// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.arch.datatable.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import junit.framework.TestCase;
import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.db.datafetcher.Tuple;
import edu.harvard.med.screensaver.db.datafetcher.TupleDataFetcher;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.test.MakeDummyEntities;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.TextTupleColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.model.VirtualPagingEntityDataModel;
import edu.harvard.med.screensaver.util.ValueReference;


public class VirtualPagingEntityDataModelTest extends TestCase
{
  private static Logger log = Logger.getLogger(VirtualPagingEntityDataModelTest.class);
  
  static final class MockEntityDataFetcher extends TupleDataFetcher<Well,String>
  {
    public int _findAllKeysCount;
    public int _fetchAllDataCount;
    public int _fetchDataCount;
    public Library _library;
    public Map<String,Tuple<String>> _wells = Maps.newHashMap();
    

    MockEntityDataFetcher(int libraryPlates)
    {
      super(Well.class, null);
      _library = MakeDummyEntities.makeDummyLibrary(1, ScreenType.SMALL_MOLECULE, libraryPlates);
      for (Well well : _library.getWells()) {
        _wells.put(well.getWellKey().toString(), new Tuple<String>(well.getWellKey().toString()));
      }
    }

    @Override
    public void addDomainRestrictions(HqlBuilder hql)
    {
    }

    @Override
    public List<String> findAllKeys()
    {
      ++_findAllKeysCount;
      return new ArrayList<String>(_wells.keySet());
    }

    @Override
    public List<Tuple<String>> fetchAllData()
    {
      ++_fetchAllDataCount;
      return new ArrayList<Tuple<String>>(_wells.values());
    }

    @Override
    public Map<String,Tuple<String>> fetchData(Set<String> keys)
    {
      ++_fetchDataCount;
      Map<String,Tuple<String>> result = Maps.newHashMap();
      for (String key : keys) {
        result.put(key, _wells.get(key));
      }
      return result;
    }
  }

  @SuppressWarnings("unchecked")
  public void testNoQueryOnOnlySortDirectionChange()
  {
    MockEntityDataFetcher fetcher = new MockEntityDataFetcher(1);
    
    TextTupleColumn<Well,String> column1 = new TextTupleColumn<Well,String>(RelationshipPath.from(Well.class).toProperty("id"), "column1", "", "");
    TextTupleColumn<Well,String> column2 = new TextTupleColumn<Well,String>(Well.latestReleasedReagent.toProperty("smiles"), "column2", "", "");

    ValueReference<Integer> rowsToFetch = new ValueReference<Integer>() { public Integer value() { return 10; } };
    VirtualPagingEntityDataModel<String,Well,Tuple<String>> model = new VirtualPagingEntityDataModel<String,Well,Tuple<String>>(fetcher, rowsToFetch);
    
    int prevFetchAllKeysCount = fetcher._findAllKeysCount;
    model.sort(Lists.newArrayList(column1), SortDirection.ASCENDING);
    model.setRowIndex(0); model.getRowData();
    assertEquals("query on initial sort", prevFetchAllKeysCount + 1, fetcher._findAllKeysCount);
    model.sort(Lists.newArrayList(column2), SortDirection.ASCENDING);
    model.setRowIndex(0); 
    model.getRowData();
    assertEquals("query on sort column changed", prevFetchAllKeysCount + 2, fetcher._findAllKeysCount);
    model.sort(Lists.newArrayList(column2), SortDirection.DESCENDING);
    model.setRowIndex(0); model.getRowData();
    assertEquals("no query on only sort direction changed", prevFetchAllKeysCount + 2, fetcher._findAllKeysCount);
  }

  public void testIterator()
  {
    MockEntityDataFetcher fetcher = new MockEntityDataFetcher(10);
    ValueReference<Integer> rowsToFetch = new ValueReference<Integer>() {
      public Integer value()
      {
        return 10;
      }
    };
    VirtualPagingEntityDataModel<String,Well,Tuple<String>> model = new VirtualPagingEntityDataModel<String,Well,Tuple<String>>(fetcher, rowsToFetch);
    model.getRowCount(); // initialize
    Iterator<Tuple<String>> iter = model.iterator();
    Set<String> result = Sets.newHashSet();
    while (iter.hasNext()) {
      result.add(iter.next().getKey());
    }
    log.info("consumed all iterator elements");
    assertEquals(fetcher._wells.keySet(), result);
  }

}
