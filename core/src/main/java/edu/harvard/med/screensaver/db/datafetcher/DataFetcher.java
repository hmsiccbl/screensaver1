// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.datafetcher;

import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.harvard.med.screensaver.db.Criterion;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;

/**
 * General purpose interface for fetch a set of objects from persistent storage,
 * supporting both lazy or eager fetching strategies. Eager fetching of all
 * objects is performed via {@link #fetchAllData()}. Lazy fetching of root objects
 * is performed by calling these methods:
 * <ul>
 * <li>(optional) {@link #setFilteringCriteria(Map)}</li>
 * <li>(optional) {@link #setOrderBy(List)}</li>
 * <li>{@link #findAllKeys()}: returns keys of object to be fetched,
 * respecting filtering and ordering options </li>
 * <li>{@link #fetchData(Set)}: fetches objects for the specified keys</li>
 * </ul>
 * <p>
 * Note that the filtering and ordering requests only need to be respected by
 * {@link #findAllKeys()}, and not {@link #fetchAllData()}. Presumably, if all
 * data is eagerly fetched via
 * {@link #fetchAllData()}, ordering and filtering can be handled by the client
 * code, as all required data is in memory. Of course, when data is lazily
 * fetched, the client code needs to be provided an ordered, filtered list of
 * keys, since it cannot possibly perform a total ordering and filtering on only
 * a subset of the data.
 * 
 * @param R the object type to be fetched
 * @param K the key type used to uniquely identify the fetched objects
 * @param P type used to specify "properties" of the fetched object type, which
 *          is used by filtering and ordering operations
 * @motivation Encapsulates data fetching operation, allowing a DataTableModel
 *             to remain ignorant of the data fetching strategy.
 *             InMemoryDataModel and VirtualPagingDataModel can be provided the
 *             same DataFetcher, allowing the appropriate model type to be
 *             determined at run-time (by a DataTable implementation).
 */
public interface DataFetcher<R, K, P>
{
  /**
   * Allows subclass to add a fixed set of restrictions that will narrow the
   * query result to a particular entity domain. For example, entities sharing
   * the same parent, an arbitrary set of entity keys, etc. This restriction is
   * effectively AND'ed with the criteria set via {@link #setFilteringCriteria(Map)}.
   * 
   * @motivation This method is a convenience to the client code, which will
   *             usually use {@link #setFilteringCriteria(Map)} for user-specified,
   *             column-associated criteria. Use of this method allows the
   *             client code to set a top-level restriction that is respected
   *             even as the user modifies column-based filtering criteria.
   */
  void addDomainRestrictions(HqlBuilder hql);

  void setPropertiesToFetch(List<P> properties);

  void setFilteringCriteria(Map<P,List<? extends Criterion<?>>> criteria);

  void setOrderBy(List<P> orderByProperties /* TODO: , List<SortDirection> orderByDirections */);

  List<K> findAllKeys();

  Map<K,R> fetchData(Set<K> keys);

  List<R> fetchAllData();
}
