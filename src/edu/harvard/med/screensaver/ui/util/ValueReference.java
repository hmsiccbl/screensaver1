// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
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
