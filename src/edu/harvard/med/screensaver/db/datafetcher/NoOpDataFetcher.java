// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.datafetcher;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.ui.table.Criterion;

public class NoOpDataFetcher<R,K,P> implements DataFetcher<R,K,P>
{
  public void setFilteringCriteria(Map<P,List<? extends Criterion<?>>> criteria)
  {
  }

  public void setOrderBy(List<P> orderByProperties)
  {
  }

  public List<R> fetchAllData()
  {
    return Collections.emptyList();
  }

  public Map<K,R> fetchData(Set<K> keys)
  {
    return Collections.emptyMap();
  }

  public List<K> findAllKeys()
  {
    return Collections.emptyList();
  }

  public int getSize()
  {
    return 0;
  }

  @Override
  public void addDomainRestrictions(HqlBuilder hql)
  {}

  @Override
  public void setPropertiesToFetch(List<P> properties)
  {}
}

