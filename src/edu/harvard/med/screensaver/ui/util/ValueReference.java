// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.util;

/**
 * Allows passing of a <i>reference</i> to a value, rather than the fixed value
 * itself, allowing client code to have access to the current value at any time,
 * without having to be passed a new value if/when it changes. Eliminates need
 * for Observer pattern in some cases.
 * 
 * @author drew
 */
public interface ValueReference<T>
{
  T value();
}
