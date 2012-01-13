// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.rest;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;

/**
 * A visitor interface for the Screensaver domain model entity classes exposed for streaming through the REST interface.
 * Implementing classes will return the REST-ful URI for entity classes.
 * <p>
 * Note: <code>visit</code> methods should only be added for concrete entity
 * classes (i.e., <i>abstract</i> entity classes should not be visitable). This
 * ensures that each <i>concrete</i> <code>AbstractEntity</code> class that
 * extends a subclass of {@link AbstractEntity} can provide its own
 * {@link AbstractEntity#acceptVisitor(EntityUriGenerator)} method
 * implementation.
 *
 */
public interface EntityUriGenerator<R> extends AbstractEntityVisitor<R>
{

}
