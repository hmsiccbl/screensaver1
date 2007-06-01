// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.util;

import java.util.Comparator;

/**
 * Base class for comparators that want null values to be implicitly handled for
 * them.
 * 
 * @see {@link org.springframework.util.comparator.NullSafeComparator}
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public abstract class NullSafeComparator<T> implements Comparator<T>
{
  private boolean _nullsLast = false;

  public NullSafeComparator()
  {
  }
  
  public NullSafeComparator(boolean nullsLast)
  {
    _nullsLast = nullsLast; 
  }
  
  final public int compare(T o1, T o2)
  {
    if (o1 == null) {
      if (o2 == null) {
        return 0;
      }
      return _nullsLast ? 1 : -1;
    }
    if (o2 == null) {
      return _nullsLast ? -1 : 1;
    }
    return doCompare(o1, o2);
  }

  protected abstract int doCompare(T o1, T o2);
}

