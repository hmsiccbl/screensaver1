// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.arch.view;

import java.io.Serializable;

import edu.harvard.med.screensaver.model.Entity;

public interface EntityViewer<E extends Entity<? extends Serializable>>
{
  /** Get the current entity. */
  E getEntity();
  
  /** Set the current entity. */
  void setEntity(E entity);

  /**
   * Set the entity, then view. If called outside of a transaction/session, this will reload the latest entity
   * state from the database.
   */
  String viewEntity(E entity);
  
  /** View the current entity. */
  String view();

  /**
   * Convenience method for viewing the entity that was last being viewed in this viewer. If called outside of a
   * transaction/session, this will reload the latest entity
   * state from the database.
   */
  String reload();
}

