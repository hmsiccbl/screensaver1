// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.datafetcher;


import java.util.Iterator;
import java.util.Set;

import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.model.Entity;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;

public class DataFetcherUtil
{
  static public <P extends Entity,R extends Entity> void addDomainRestrictions(HqlBuilder hql,
                                                                               RelationshipPath<R> _parentEntityPath,
                                                                               P parentEntity,
                                                                               String rootAlias)
  {
    if (_parentEntityPath.hasRestrictions()) {
      throw new IllegalArgumentException("path to parent entity cannot have restrictions " +
                                         "(if there are to-many relationships between root entity and parent entity, " +
                                         "the parent entity is not really a parent!)");
    }
    int n = 1;
    String alias = rootAlias;
    Iterator<String> iter = _parentEntityPath.pathIterator();
    while (iter.hasNext()) {
      String nextAlias = "p" + n++;
      hql.from(alias, iter.next(), nextAlias);
      alias = nextAlias;
    }
    hql.where(alias, parentEntity);
  }

  static public <K> void addDomainRestrictions(HqlBuilder hql, String rootAlias, Set<K> entityKeys)
  {
    hql.whereIn(rootAlias, "id", entityKeys);
  }

}
