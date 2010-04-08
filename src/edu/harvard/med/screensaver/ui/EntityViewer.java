// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import edu.harvard.med.screensaver.model.Entity;

public interface EntityViewer<E extends Entity>
{
  /** Get the current entity */
  E getEntity();
  
  /** Set the current entity */
  void setEntity(E entity);

  /** Set the entity, then view */
  String viewEntity(E entity);
  
  /** View the current entity */
  String view();

  /**
   * Initialize the viewer with the same entity it is currently showing, but
   * reloaded from the database.
   */
  String reload();
}

