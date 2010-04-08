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

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.db.hqlbuilder.JoinType;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;

import org.apache.log4j.Logger;

public class ParentedEntityDataFetcher<E extends AbstractEntity, K> extends EntityDataFetcher<E,K>
{
  // static members

  private static Logger log = Logger.getLogger(ParentedEntityDataFetcher.class);


  // instance data members

  private RelationshipPath<E> _parentEntityPath;
  private AbstractEntity _parentEntity;


  // public constructors and methods

  /**
   * Fetches data, relative to the specified root entity.
   *
   * @param parentEntityPath the path to the parent entity from the root entity.
   *          The path cannot have any restrictions.
   */
  public ParentedEntityDataFetcher(Class<E> rootEntityClass,
                                   RelationshipPath<E> parentEntityPath,
                                   AbstractEntity parentEntity,
                                   GenericEntityDAO dao)
  {
    super(rootEntityClass, dao);
    _parentEntityPath = parentEntityPath;
    _parentEntity = parentEntity;
    for (int i = 0; i < _parentEntityPath.getPathLength(); ++i) {
      if (_parentEntityPath.getRestrictionPropertyNameAndValue(i) != null) {
        throw new IllegalArgumentException("path to parent entity cannot have restrictions " +
        		"(if there are to-many relationships between root entity and parent entity, " +
        		"the parent entity is not really a parent!)");
      }
    }
  }

  protected void addDomainRestrictions(HqlBuilder hql,
                                       Map<RelationshipPath<E>,String> path2Alias)
  {
    String alias = getOrCreateJoin(hql, _parentEntityPath, path2Alias, JoinType.INNER);
    hql.where(alias, _parentEntity);
  }

  // private methods

}
