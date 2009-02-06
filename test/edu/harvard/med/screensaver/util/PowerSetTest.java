// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.util;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import com.google.common.collect.Sets;

public class PowerSetTest extends TestCase
{
  private static Logger log = Logger.getLogger(PowerSetTest.class);
  
  public void testEmptyPowerSet()
  {
    Set<Set<Object>> emptyPowerSet = PowerSet.powerSet(Sets.newHashSet());
    Set<Set<Object>> expectedEmptyPowerSet = Sets.newHashSet();
    expectedEmptyPowerSet.add(Sets.newHashSet());
    assertEquals(expectedEmptyPowerSet, emptyPowerSet);
  }

  public void testOrderedPowerSet()
  {
    SortedSet<SortedSet<Character>> powerSet = PowerSet.orderedPowerset(Sets.newHashSet('d', 'a', 'c', 'b'));
    Set<Set<Character>> expectedPowerSet = new LinkedHashSet<Set<Character>>();
    expectedPowerSet.add(new TreeSet<Character>());
    expectedPowerSet.add(Sets.newTreeSet('a'));
    expectedPowerSet.add(Sets.newTreeSet('b'));
    expectedPowerSet.add(Sets.newTreeSet('c'));
    expectedPowerSet.add(Sets.newTreeSet('d'));
    expectedPowerSet.add(Sets.newTreeSet('a', 'b'));
    expectedPowerSet.add(Sets.newTreeSet('a', 'c'));
    expectedPowerSet.add(Sets.newTreeSet('a', 'd'));
    expectedPowerSet.add(Sets.newTreeSet('b', 'c'));
    expectedPowerSet.add(Sets.newTreeSet('b', 'd'));
    expectedPowerSet.add(Sets.newTreeSet('c', 'd'));
    expectedPowerSet.add(Sets.newTreeSet('a', 'b', 'c'));
    expectedPowerSet.add(Sets.newTreeSet('a', 'b', 'd'));
    expectedPowerSet.add(Sets.newTreeSet('a', 'c', 'd'));
    expectedPowerSet.add(Sets.newTreeSet('b', 'c', 'd'));
    expectedPowerSet.add(Sets.newTreeSet('a', 'b', 'c', 'd'));
    
    Iterator<Set<Character>> expectedIter = expectedPowerSet.iterator();
    Iterator<SortedSet<Character>> actualIter = powerSet.iterator();
    while (expectedIter.hasNext() && actualIter.hasNext()) {
      assertEquals(expectedIter.next(), actualIter.next());
    }
    assertFalse(expectedIter.hasNext());
    assertFalse(actualIter.hasNext());
  }

}
