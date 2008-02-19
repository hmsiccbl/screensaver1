// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.datafetcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import edu.harvard.med.screensaver.ui.table.Criterion;

import org.apache.log4j.Logger;

// TODO: Probably need an AggregatePropertyPath class.  
/**
 * @param AT aggregate type
 * @param AK aggregate key
 * @param BT base type: the type being aggregated into an object of type AT
 * @param BK base key
 */
public abstract class AggregateDataFetcher<AT extends Comparable<AT>,AK,BT,BK> implements DataFetcher<AT,AK,Object>
{

  // static members

  private static Logger log = Logger.getLogger(AggregateDataFetcher.class);


  // instance data members
  
  /**
   * DataFetcher that is used to fetch the underlying, non-aggregated entity data.
   */
  private DataFetcher<BT,BK,?> _baseDataFetcher;
  
  // abstract methods
  
  abstract protected SortedSet<AT> aggregateData(List<BT> nonAggregatedData);

  
  // public constructors and methods
  
  public AggregateDataFetcher(DataFetcher<BT,BK,?> baseDataFetcher)
  {
    _baseDataFetcher = baseDataFetcher; 
  }
  
  public List<AT> fetchAllData()
  {
    List<BT> nonAggregatedData = _baseDataFetcher.fetchAllData();
    return new ArrayList<AT>(aggregateData(nonAggregatedData));
  }

  public void setFilteringCriteria(Map<Object,List<? extends Criterion<?>>> criteria)
  {
    throw new UnsupportedOperationException("AggregateDataFetcher can only be used to fetch all data at once, and filtering should be done in memory");
  }

  public void setOrderBy(List<Object> orderByProperties)
  {
    throw new UnsupportedOperationException("AggregateDataFetcher can only be used to fetch all data at once, and ordering should be done in memory");
  }
  
  public Map<AK,AT> fetchData(Set<AK> keys)
  {
    throw new UnsupportedOperationException("AggregateDataFetcher can only be used to fetch all data at once");
  }


  public List<AK> findAllKeys()
  {
    throw new UnsupportedOperationException("AggregateDataFetcher can only be used to fetch all data at once");
  }
  
  // private methods
  
}