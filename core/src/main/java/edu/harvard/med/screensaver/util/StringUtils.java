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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

  /**
   * Tokenize the input word list by grouping on words, and words delineated by quotes.<br/>
   * I.e. 'word1 word2 "word 3"' becomes ['word1', 'word2', '"word 3"']<br/>
   * <br/>
   * Uses the following regex to group and split:<br/>
   * "('.*?'|\".*?\"|[^\\s,;]+)"<br/>
   * <br/>
   * <b>Note:</b> Quote characters are left in place and should be removed from the tokens if desired.
   * 
   * </p>
   * Courtesy of http://stackoverflow.com/questions/366202/regex-for-splitting-a-string-using-space-when-not-surrounded-by-single-or-double
   * 
   * @param quotedWordList list of words, or quoted word phrases that will be treated as one token.
   * @return the list of tokens
   */
  public static List<String> tokenizeQuotedWordList(String quotedWordList)
  {
    // Group on words, and words delineated by quotes; 
    // courtesy of http://stackoverflow.com/questions/366202/regex-for-splitting-a-string-using-space-when-not-surrounded-by-single-or-double
    // ( this works also [^\W"']+|"([^"]*)"|'([^']*)'  )
    // pattern: ('.*?'|\".*?\"|[^\\s,;]+)  - try for single or double quoted strings or for NOT [whitespace comma semicolon] 
    Pattern quotedWordPattern = Pattern.compile("('.*?'|\".*?\"|[^\\s,;]+)");
    Matcher matcher = quotedWordPattern.matcher(quotedWordList);
    List<String> list = Lists.newArrayList();
    while (matcher.find()) {
      list.add(matcher.group());
    }
    return list;
  }
  
   
}
