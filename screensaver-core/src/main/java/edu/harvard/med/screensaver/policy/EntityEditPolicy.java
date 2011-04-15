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

/**
 * Defines a policy for each entity type that is used to determine whether a
 * entity instance can be edited by the current user.  
 * @see EntityViewPolicy
 */
public interface EntityEditPolicy extends AbstractEntityVisitor
{
}
