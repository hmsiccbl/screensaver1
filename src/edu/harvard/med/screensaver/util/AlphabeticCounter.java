// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;



/**
 * Converts between non-negative integers and alphabetic counters, which using A-Z "digits".
 * A..Z maps to 0..25, 26..51 maps to AA..AZ, etc.
 *
 * @author atolopko
 */
public class AlphabeticCounter
{
  private static List<String> alphabet = Lists.newArrayList();
  static {
    for (char c = 'A'; c <= 'Z'; ++c) {
      alphabet.add("" + (char) c);
    }
  }
  private static Map<Integer,String> seq = Maps.newLinkedHashMap();
  private static Map<String, Integer> ndx = Maps.newHashMap();
  static { 
    for (String a : alphabet) {
      ndx.put(a, seq.size());
      seq.put(seq.size(), a);
    }
  }

  private static void expandToInclude(int n)
  {
    while (seq.size() <= n) {
      ArrayList<String> lastSeq = Lists.newArrayList(seq.values());
      for (String a : alphabet) {
        for (String s : lastSeq) {
          s = a + s;
          if (!ndx.keySet().contains(s)) {
            ndx.put(s, seq.size());
            seq.put(seq.size(), s);
          }
        }
      }
    }
  }
  
  public static String toLabel(int index)
  {
    expandToInclude(index);
    return seq.get(index);
  }
  
  public static int toIndex(String label)
  {
    if (!ndx.containsKey(label)) {
      expandToInclude((int) Math.pow(27, label.length()));
    }
    return ndx.get(label);
  }
}
