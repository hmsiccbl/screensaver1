// $HeadURL: http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/branches/iccbl/2.2.2-dev/src/edu/harvard/med/screensaver/ui/arch/datatable/column/entity/VolumeEntityColumn.java $
// $Id: VolumeEntityColumn.java 4960 2010-11-08 14:53:52Z atolopko $
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.arch.datatable.column.entity;

import java.util.Set;

import edu.harvard.med.screensaver.model.Entity;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.ui.arch.datatable.column.MolarConcentrationColumn;

public abstract class MolarConcentrationEntityColumn<E extends Entity> extends MolarConcentrationColumn<E> implements HasFetchPaths<E>
{
  private FetchPaths<E,E> _fetchPaths;
  
  public MolarConcentrationEntityColumn(RelationshipPath<E> relationshipPath, String name, String description, String group)
  {
    super(name, description, group);
    _fetchPaths = new FetchPaths<E,E>(relationshipPath);
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

  public boolean isFetchableProperty()
  {
    return _fetchPaths.isFetchableProperty();
  }
}
