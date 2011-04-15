// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.annotations;

import javax.persistence.Transient;

/**
 * Marks a model property as being derived (computed). Implies that a service-layer method will be used to compute and
 * store its value. Unless the property is also {@link Transient}, the computed property value will be persisted in the
 * database.
 * 
 * @author atolopko
 */
public @interface Derived {
}
