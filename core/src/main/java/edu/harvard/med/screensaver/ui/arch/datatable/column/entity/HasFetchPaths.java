// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.arch.datatable.column.entity;

import java.util.Set;

import edu.harvard.med.screensaver.model.Entity;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;


public interface HasFetchPaths<E extends Entity>
{
  Set<RelationshipPath<E>> getRelationshipPaths();

  PropertyPath<E> getPropertyPath();

  /**
   * Add additional an RelationshipPath that should be fetched by the
   * persistence layer when fetching data for this column.
   * 
   * @param path
   */
  void addRelationshipPath(RelationshipPath<E> path);

  /**
   * Whether HasFetchPaths specifies a property that can be directly fetched
   * from the database. In general, this is true if there is only a single
   * PropertyPath.
   * 
   * @motivation A non-fetchable property implies that sorting and filtering cannot
   *             be performed on the column by the database, but rather needs to
   *             be performed "client-side", in memory. The relationship paths
   *             describe the entity properties (or just entities) that are
   *             required to derive the value.
   */
  boolean isFetchableProperty();
  
}