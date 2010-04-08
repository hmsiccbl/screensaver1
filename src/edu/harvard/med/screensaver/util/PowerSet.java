// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.util;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;

/**
 * Utility class for generating powersets. 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class PowerSet
{
  private static final Logger log = Logger.getLogger(PowerSet.class);
  
  /**
   * Return an iterator that generates the powerset of the specified set.  
   */
  public static <T> Iterator<Set<T>> powerSetIterator(final Set<T> values)
  {
    return new Iterator<Set<T>>() {
      private int i;
      private long n = 1 << Math.max(0, values.size());

      public boolean hasNext()
      {
        return i < n; 
      }
      
      public Set<T> next()
      {
        Set<T> set = new HashSet<T>();
        int j = 0; 
        for (T e : values) {
          if (((1 << j++) & i) != 0) {
            set.add(e);
          }
        }
        ++i;
        return set;
      }

      public void remove()
      {
        throw new UnsupportedOperationException();
      }
    };
  }
  
  
  public static <T> Set<Set<T>> powerSet(Set<T> values)
  {
    Set<Set<T>> powerSet = new HashSet<Set<T>>(); 
    Iterators.addAll(powerSet, powerSetIterator(values));
    return powerSet;
  }

  /**
   * Returns the power set of the specified set, ordering the power set elements
   * (themselves sets) by size, then minimum value. Each the power set elements
   * (sets) are also ordered by the natural ordering of their values.
   */
  public static <T extends Comparable<T>> SortedSet<SortedSet<T>> orderedPowerset(Set<T> values)
  {
    SortedSet<SortedSet<T>> orderedPowerset = 
      new TreeSet<SortedSet<T>>(new Comparator<SortedSet<T>>() { 
        public int compare(SortedSet<T> s1, SortedSet<T> s2) { 
          if (s1.size() < s2.size()) {
            return -1;
          }
          else if (s1.size() > s2.size()) {
            return 1;
          }
          else {
            Iterator<T> i1 = s1.iterator();
            Iterator<T> i2 = s2.iterator();
            int c = 0;
            while (i1.hasNext() && i2.hasNext() && c == 0) {
              c = i1.next().compareTo(i2.next());
            }
            return c;
          }
        } 
      });
    Iterator<Set<T>> powerSetIterator = powerSetIterator(values);
    while (powerSetIterator.hasNext()) {
      TreeSet<T> orderedSetElement = Sets.newTreeSet(powerSetIterator.next());
      orderedPowerset.add(orderedSetElement);
      if (log.isDebugEnabled()) {
        log.debug("added " + orderedSetElement);
        log.debug("powerset = " + orderedPowerset);
      }
    }
    return orderedPowerset;
  }
  
}
