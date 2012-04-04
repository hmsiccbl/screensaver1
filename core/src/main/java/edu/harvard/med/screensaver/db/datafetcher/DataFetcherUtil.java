// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.datafetcher;


import java.util.Set;

import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.db.hqlbuilder.JoinType;
import edu.harvard.med.screensaver.model.Entity;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.util.ParallelIterator;

public class DataFetcherUtil
{
  static public <P extends Entity,R extends Entity> void addDomainRestrictions(HqlBuilder hql,
                                                                               RelationshipPath<R> parentEntityPath,
                                                                               P parentEntity,
                                                                               String rootAlias)
  {
    //    if (parentEntityPath.getCardinality() == Cardinality.TO_MANY) {
    //      throw new IllegalArgumentException("path to parent entity must have cardinality of one " +
    //                                         "(if there are to-many relationships between root entity and parent entity, " +
    //                                         "the parent entity is not really a parent)");
    //    }
    ParallelIterator<String,Class<? extends Entity>> iterator =
      new ParallelIterator<String,Class<? extends Entity>>(parentEntityPath.pathIterator(),
                                                           parentEntityPath.entityClassIterator());
    int n = 1;
    String alias = rootAlias;
    while (iterator.hasNext()) {
      String nextAlias = "p" + n++;
      iterator.next();
      hql.from(alias, RelationshipPath.from(iterator.getSecond()).to(iterator.getFirst()), nextAlias, JoinType.INNER);
      alias = nextAlias;
    }
    hql.where(alias, parentEntity);
  }

  static public <K> void addDomainRestrictions(HqlBuilder hql, String rootAlias, Set<K> entityKeys)
  {
    hql.whereIn(rootAlias, "id", entityKeys);
  }

  static public <K> void addDomainRestrictions(HqlBuilder hql, String rootAlias, String property, Set<K> entityKeys)
  {
    hql.whereIn(rootAlias, property, entityKeys);
  }

}
