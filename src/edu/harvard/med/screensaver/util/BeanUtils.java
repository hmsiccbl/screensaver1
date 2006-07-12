// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
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
   * @return
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
