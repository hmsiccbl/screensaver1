// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.datafetcher;

import java.util.Map;
import java.util.Set;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;

import org.apache.log4j.Logger;

public class EntitySetDataFetcher<E extends AbstractEntity, K> extends EntityDataFetcher<E,K>
{
  // static members

  private static Logger log = Logger.getLogger(EntitySetDataFetcher.class);


  // instance data members
  private Set<K> _forEntityKeys;

  // public constructors and methods

  /**
   * Fetches entities of the specified type.
   */
  public EntitySetDataFetcher(Class<E> rootEntityClass,
                              Set<K> entityKeys,
                              GenericEntityDAO dao)
  {
    super(rootEntityClass, dao);
    _forEntityKeys = entityKeys;
  }

  @Override
  protected void addDomainRestrictions(HqlBuilder hql,
                                       Map<RelationshipPath<E>,String> path2Alias)
  {
    hql.whereIn(getRootAlias(), "id", _forEntityKeys);
  }

  public Set<K> getDomain()
  {
    return _forEntityKeys;
  }

  // private methods

}
