// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.datafetcher;


import java.util.Set;

import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.meta.PropertyNameAndValue;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.util.Pair;

public class DataFetcherUtil
{
  static public <P extends AbstractEntity,R extends AbstractEntity> void addDomainRestrictions(HqlBuilder hql,
                                                                                               RelationshipPath<R> _parentEntityPath,
                                                                                               P _parentEntity,
                                                                                               String rootAlias)
  {
    if (_parentEntityPath.hasRestrictions()) {
      throw new IllegalArgumentException("path to parent entity cannot have restrictions " +
                                         "(if there are to-many relationships between root entity and parent entity, " +
                                         "the parent entity is not really a parent!)");
    }
    int n = 1;
    String alias = rootAlias;
    for (Pair<String,PropertyNameAndValue> node : _parentEntityPath) {
      String nextAlias = "p" + n++;
      hql.from(alias, node.getFirst(), nextAlias);
      alias = nextAlias;
    }
    hql.where(alias, _parentEntity);
  }

  static public <K> void addDomainRestrictions(HqlBuilder hql, String rootAlias, Set<K> entityKeys)
  {
    hql.whereIn(rootAlias, "id", entityKeys);
  }

}
