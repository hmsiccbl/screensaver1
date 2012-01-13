// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.util;


public class BeanUtils
{
  /**
   * Convert a property name to Title Case with spaces between words.
   * For example "aPropertyName" becomes "A Property Name".
   * @param propertyName
   */
  public static String formatPropertyName(String propertyName)
  {
    return 
      new StringBuilder(propertyName.replaceAll("(\\p{Upper})", " $1")).
        replace(0,
                1,
                propertyName.substring(0, 1).toUpperCase()).toString();
  }
 
}
