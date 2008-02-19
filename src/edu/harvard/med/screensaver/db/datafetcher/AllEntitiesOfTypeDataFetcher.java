// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.datafetcher;

import java.util.Map;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.hibernate.HqlBuilder;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.RelationshipPath;

import org.apache.log4j.Logger;

public class AllEntitiesOfTypeDataFetcher<E extends AbstractEntity, K> extends EntityDataFetcher<E,K>
{
  // static members

  private static Logger log = Logger.getLogger(AllEntitiesOfTypeDataFetcher.class);


  // public constructors and methods

  public AllEntitiesOfTypeDataFetcher(Class<E> rootEntityClass,
                                      GenericEntityDAO dao)
  {
    super(rootEntityClass, dao);
  }

  @Override
  protected void addDomainRestrictions(HqlBuilder hql,
                                       Map<RelationshipPath<E>,String> path2Alias)
  {
    return;
  }
}
