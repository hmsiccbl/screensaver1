// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.datafetcher;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import edu.harvard.med.screensaver.db.Criterion;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.model.Entity;
import edu.harvard.med.screensaver.model.meta.PropertyPath;

/**
 * @param AT aggregate type
 * @param AK aggregate key
 * @param BT base type: the type being aggregated into an object of type AT
 * @param BK base key
 * @deprecated use {@link SetBasedDataFetcher}
 */
@Deprecated
public abstract class AggregateDataFetcher<AT extends Entity<AK>,AK extends Serializable,BT extends Entity<BK>,BK extends Serializable> 
  extends EntityDataFetcher<AT,AK>
{
  /**
   * DataFetcher that is used to fetch the underlying, non-aggregated entity data.
   */
  private EntityDataFetcher<BT,BK> _baseDataFetcher;
  
  abstract protected SortedSet<AT> aggregateData(List<BT> nonAggregatedData);

  
  public AggregateDataFetcher(Class<AT> entityClass, GenericEntityDAO dao, EntityDataFetcher<BT,BK> dataFetcher)
  {
    super(entityClass, dao);
    _baseDataFetcher = dataFetcher; 
  }
  
  @Override
  public void addDomainRestrictions(HqlBuilder hql)
  {
    throw new UnsupportedOperationException("use setRelationshipsToFetch() on base data fetcher");
  }

  @Override
  public void setPropertiesToFetch(List<PropertyPath<AT>> properties)
  {
    throw new UnsupportedOperationException("AggregateDataFetcher expects data to be fetched by base data fetcher");
  }

  public List<AT> fetchAllData()
  {
    List<BT> nonAggregatedData = _baseDataFetcher.fetchAllData();
    return new ArrayList<AT>(aggregateData(nonAggregatedData));
  }

  @Override
  public void setFilteringCriteria(Map<PropertyPath<AT>,List<? extends Criterion<?>>> criteria)
  {
    throw new UnsupportedOperationException("AggregateDataFetcher can only be used to fetch all data at once, and filtering should be done in memory");
  }

  public void setOrderBy(List<PropertyPath<AT>> orderByProperties)
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
}