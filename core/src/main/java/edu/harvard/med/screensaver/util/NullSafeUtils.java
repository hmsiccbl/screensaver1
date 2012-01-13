// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.util;

import com.google.common.base.Function;

public class NullSafeUtils
{
  public static final String DEFAULT_NULL_LABEL = "<none>";

  public static boolean nullSafeEquals(Object o1, Object o2)
  {
    if (o1 == null) {
      return o2 == null;
    }
    if (o2 == null) {
      return false;
    }
    return o1.equals(o2);
  }
  
  public static String toString(Object s)
  {
    return toString(s, DEFAULT_NULL_LABEL);
  }
  
  public static String toString(Object s, String nullLabel)
  {
    if (s == null) {
      return nullLabel;
    }
    return s.toString();
  }

  public static <T> String toString(T o, Function<T,String> toStringFunction)
  {
    return toString(o, toStringFunction, DEFAULT_NULL_LABEL);
  }

  public static <T> String toString(T o, Function<T,String> toStringFunction, String nullLabel)
  {
    if (o == null) {
      return nullLabel;
    }
    return toStringFunction.apply(o);
  }
  
  public static <T> T value(T o, T defaultValue)
  {
    if (o == null) {
      return defaultValue;
    }
    return o;
  }
}
