// $HeadURL:
// svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml
// $
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.datafetcher;

import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.harvard.med.screensaver.ui.table.Criterion;

/**
 * General purpose interface for operations that fetch an object network from
 * persistent storage, either lazily or eagerly. Eager fetching of all root
 * objects is performed via {@link fetchAllData}. Lazy fetching of root objects
 * is performed by first calling {@link findAllKeys}, followed by
 * {@link fetchData(Set<K>)}.
 * <p>
 * The root objects of the network can be filtered and ordered by specifying
 * filtering criteria via {@link setFilteringCriteria} and ordering via
 * {@link setOrderBy}. The filtering and ordering requirements only need to be
 * respected by {@link findAllKeys}, and not {@link fetchAllData}. Presumably,
 * if all data is eagerly fetched, ordering and filtering can be handled by the
 * client code, as all required data is in memory. When data is lazily fetched,
 * the client code needs to be provided an ordered, filtered list of keys, since
 * it cannot possibly perform a total ordering and filtering on only a subset of
 * the data.
 *
 * @param R the row type of the data to be fetched and returned
 * @param K the key type used to uniquely identify the root objects
 * @param P type used to identify "properties" to be filtered and ordered
 * @motivation Encapsulates data fetching operation, allowing a DataTableModel
 *             to remain ignorant of the data fetching strategy.
 *             InMemoryDataModel and VirtualPagingDataModel can be provided the
 *             same DataFetcher, allowing the appropriate model type to be
 *             determined at run-time (by a DataTable implementation).
 */
public interface DataFetcher<R, K, P>
{
  void setFilteringCriteria(Map<P,List<? extends Criterion<?>>> criteria);

  void setOrderBy(List<P> orderByProperties);

  List<K> findAllKeys();

  Map<K,R> fetchData(Set<K> keys);

  List<R> fetchAllData();
}
