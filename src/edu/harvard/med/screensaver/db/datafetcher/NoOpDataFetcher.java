// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.datafetcher;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.harvard.med.screensaver.ui.table.Criterion;

import org.apache.log4j.Logger;

public class NoOpDataFetcher<R,K,P> implements DataFetcher<R,K,P>
{
  // static members

  private static Logger log = Logger.getLogger(NoOpDataFetcher.class);

  // instance data members

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

}

