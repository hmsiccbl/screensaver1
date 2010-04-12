// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.policy;

import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.screens.Screen;

/**
 * Defines a policy for each entity type that is used to determine whether a
 * entity instance should be accessible (viewable) by the current user.  
 * @see EntityEditPolicy
 */
public interface EntityViewPolicy extends AbstractEntityVisitor
{
  /**
   * @deprecated This is a hack, but helps to keep data access logic in single,
   *             central location. Should separate out properties that need
   *             additional protection into a related entity, in order to use
   *             the AbstractEntityVisitor to control access to these properties
   *             in a manner consistent with the rest of our EntityViewPolicy.
   */
  @Deprecated()
  public boolean isAllowedAccessToScreenDetails(Screen screen);

  /**
   * Determine whether the current user can see the Status Items, Lab
   * Activities, and Cherry Pick Requests tables. These are considered more
   * private than the screen details (see
   * {@link #isAllowedAccessToScreenDetails}).
   * @deprecated This is a hack, but helps to keep data access logic in single,
   *             central location. Should separate out properties that need
   *             additional protection into a related entity, in order to use
   *             the AbstractEntityVisitor to control access to these properties
   *             in a manner consistent with the rest of our EntityViewPolicy.
   */
  boolean isAllowedAccessToScreenActivity(Screen screen);

  /**
   * @deprecated This is a hack, but helps to keep data access logic in single,
   *             central location. Should separate out properties that need
   *             additional protection into a related entity, in order to use
   *             the AbstractEntityVisitor to control access to these properties
   *             in a manner consistent with the rest of our EntityViewPolicy.
   */
  @Deprecated()
  public boolean isAllowedAccessToSilencingReagentSequence(SilencingReagent reagent);

}
