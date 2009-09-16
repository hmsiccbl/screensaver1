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

package edu.harvard.med.screensaver.ui.table.column.entity;

import java.util.Set;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.ui.table.column.EnumColumn;


public abstract class EnumEntityColumn<E extends AbstractEntity, ENUM extends Enum<ENUM>> extends EnumColumn<E,ENUM> implements HasFetchPaths<E>
{
  private FetchPaths<E> _fetchPaths;
  
  public EnumEntityColumn(RelationshipPath<E> relationshipPath,
                          String name,
                          String description,
                          String group,
                          ENUM[] items)
  {
    super(name,
          description,
          group,
          items);
    _fetchPaths = new FetchPaths<E>(relationshipPath);
  }

  public void addRelationshipPath(RelationshipPath<E> path)
  {
    _fetchPaths.addRelationshipPath(path);
  }

  public PropertyPath<E> getPropertyPath()
  {
    return _fetchPaths.getPropertyPath();
  }

  public Set<RelationshipPath<E>> getRelationshipPaths()
  {
    return _fetchPaths.getRelationshipPaths();
  }
}
