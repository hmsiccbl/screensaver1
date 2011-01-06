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

import org.springframework.transaction.annotation.Transactional;

import edu.harvard.med.screensaver.model.Entity;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.arch.searchresults.EntityUpdateSearchResults;
import edu.harvard.med.screensaver.ui.arch.view.aspects.UICommand;

public interface EditableEntityViewer<E extends Entity<? extends Serializable>> /*extends EntityViewer<E>*/
{
  /**
   * Determine whether this viewer is currently in edit mode. This method should
   * always return false if {@link #isEditable()} returns false.
   * 
   * @return true if the viewer is currently in edit mode
   */
  boolean isEditMode();

  @UICommand
  String edit();

  @UICommand
  String cancel();

  @UICommand
  @Transactional
  String save();

  @UICommand
  @Transactional
  String delete();

  /**
   * Override this method to indicate whether the current user is allowed to
   * perform editing operations on the data in the view. Components in the JSF
   * view can call this method/property via the JSF EL, to set component
   * attributes, such as 'displayValueOnly', 'rendered', 'readonly', 'disables',
   * etc.
   *
   * @motivation the Tomahawk components have 'enabledOnUserRole' and
   *             'visibleOnUserRole' attributes, which are convenient, but 1)
   *             'enabledOnUserRole' shows a grayed-out component, rather than a
   *             nice plain text value, as does 'displayValueOnly'; 2) cannot
   *             handle the case where the visibility/enabling of a component is
   *             controlled by the user *not* being in a particular role (e.g.
   *             user is not an admin of any type)
   * @return true iff the view is read-only for the current user, based upon the
   *         user's roles. Defaults to false, unless a subclass overrides this
   *         method.
   * @see AbstractBackingBean#isUserInRole(ScreensaverUserRole)
   */
  boolean isReadOnly();

  /**
   * Determine whether this viewer is editable by the current user, usually
   * based upon what roles have assigned to the user.
   * 
   * @return true if the viewer can be edited by the current user
   */
  boolean isEditable();

  EntityUpdateSearchResults getEntityUpdateSearchResults();
  
  boolean isUpdateHistoryCapable();
  String viewUpdateHistory();
}
