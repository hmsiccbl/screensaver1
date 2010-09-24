// $HeadURL: $
// $Id: $
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.util;

public class ComparableNullSafeComparator<T extends Comparable<T>> extends NullSafeComparator<T>
{
  @Override
  protected int doCompare(T o1, T o2)
  {
    return o1.compareTo(o2);
  }

}
