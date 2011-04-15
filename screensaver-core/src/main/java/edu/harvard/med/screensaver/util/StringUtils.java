// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import edu.harvard.med.screensaver.model.VocabularyTerm;

public class StringUtils
{

	/**
   * Makes a delimited list of items from a Collection, just like Perl's join()
   * function.
   *
   * @param items a <code>Collection</code> of <code>Object</code>s with
   *          appropriate <code>toString</code> methods
   * @param delimiter a <code>String</code>
   * @return a <code>String</code> containing the string representation of the
   *         Collection elements, delimited by <code>delimiter</code>
   */
  /* TODO: consider replacing with com.google.common.base.Join method(s)
  /* TODO: provide escaping of specified characters */
  /* TODO: overload method signature for default parameters */
  public static String makeListString(Collection items, String delimiter)
  {
    StringBuilder buf = new StringBuilder();
    if (items != null) {
      boolean isFirst = true;
      for (Iterator iter = items.iterator(); iter.hasNext();) {
        Object item = iter.next();
        String s = "<null>";
        if (item != null) {
          s = item.toString();
        }
        if (isFirst) {
          isFirst = false;
        }
        else {
          buf.append(delimiter);
        }
        buf.append(s);
      }
    }
    return buf.toString();
  }

	public static String makeRepeatedString(String segment, int count)
  {
    int targetLength = segment.length() * count;
    StringBuffer buf = new StringBuffer(targetLength);
    for (int i = 0; buf.length() < targetLength; ++i) {
      // for efficiency, we'll double the size of our repeated string, if
      // possible
      if (buf.length() > 0 && buf.length() * 2 <= targetLength) {
        buf.append(buf);
      }
      else {
        buf.append(segment);
      }
    }
    return buf.toString();
  }

	@SuppressWarnings("unchecked")
  public static List wrapStrings(Collection elements, String left, String right)
  {
    StringBuffer buf = new StringBuffer();
    List result = new ArrayList(elements.size());
    for (Iterator iter = elements.iterator(); iter.hasNext();) {
      buf.append(left)
         .append(iter.next())
         .append(right);
      result.add(buf.toString());
      buf.setLength(0);
    }
    return result;
  }

  public static String capitalize(String s)
  {
    if (s != null && s.length() > 0) {
      return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
    return s;
  }

  public static String uncapitalize(String s)
  {
    if (s != null && s.length() > 0) {
      return s.substring(0, 1).toLowerCase() + s.substring(1);
    }
    return s;
  }
  

  //  /**
  //   * @return true if s is zero-length or null
  //   * @see #isBlank
  //   */
  //  public static boolean isEmpty(String s)
  //  {
  //    return s == null || s.length() == 0;
  //  }

  /**
   * @return true if s contains only whitespace, is zero-length, or null
   */
  // TODO: rename to isBlank and add a proper isEmpty method, above
  public static boolean isEmpty(String s)
  {
    return s == null || s.trim().length() == 0;
  }

  public static List<String> getVocabularyTerms(VocabularyTerm[] terms)
  {
    return Lists.transform(Lists.newArrayList(terms), 
                                                         new Function<VocabularyTerm,String>() {
                                                           @Override
                                                           public String apply(VocabularyTerm arg0)
                                                        {
                                                          return arg0.getValue();
                                                        }
                                                         }
                                                        );
  }
}
