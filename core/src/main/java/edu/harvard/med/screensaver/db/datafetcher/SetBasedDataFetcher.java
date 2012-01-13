// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.datafetcher;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;

import edu.harvard.med.screensaver.db.Criterion;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.model.Entity;

/**
 * @author atolopko
 */
public class SetBasedDataFetcher<E extends Entity<K>,K extends Serializable>
  implements DataFetcher<E,K,Object>
{
  private Set<E> _entities;

  public SetBasedDataFetcher(Set<E> entities)
  {
    _entities = entities;
  }
  
  @Override
  public void addDomainRestrictions(HqlBuilder hql)
  {
    throw new UnsupportedOperationException();
  }
  @Override
  public void setPropertiesToFetch(List<Object> properties)
  {
    throw new UnsupportedOperationException();
  }

  public List<E> fetchAllData()
  {
    return Lists.newArrayList(_entities);
  }

  @Override
  public void setFilteringCriteria(Map<Object,List<? extends Criterion<?>>> criteria)
  {
    throw new UnsupportedOperationException("can only be used to fetch all data at once, and filtering should be done in memory");
  }

  public void setOrderBy(List<Object> orderByProperties)
  {
    throw new UnsupportedOperationException("can only be used to fetch all data at once, and ordering should be done in memory");
  }
  
  public Map<K,E> fetchData(Set<K> keys)
  {
    throw new UnsupportedOperationException("can only be used to fetch all data at once");
  }

  public List<K> findAllKeys()
  {
    throw new UnsupportedOperationException("can only be used to fetch all data at once");
  }
}