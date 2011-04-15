// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.util;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedSet;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import com.google.common.collect.ImmutableSet;
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
    expectedPowerSet.add(ImmutableSet.<Character>of());
    expectedPowerSet.add(ImmutableSet.of('a'));
    expectedPowerSet.add(ImmutableSet.of('b'));
    expectedPowerSet.add(ImmutableSet.of('c'));
    expectedPowerSet.add(ImmutableSet.of('d'));
    expectedPowerSet.add(ImmutableSet.of('a', 'b'));
    expectedPowerSet.add(ImmutableSet.of('a', 'c'));
    expectedPowerSet.add(ImmutableSet.of('a', 'd'));
    expectedPowerSet.add(ImmutableSet.of('b', 'c'));
    expectedPowerSet.add(ImmutableSet.of('b', 'd'));
    expectedPowerSet.add(ImmutableSet.of('c', 'd'));
    expectedPowerSet.add(ImmutableSet.of('a', 'b', 'c'));
    expectedPowerSet.add(ImmutableSet.of('a', 'b', 'd'));
    expectedPowerSet.add(ImmutableSet.of('a', 'c', 'd'));
    expectedPowerSet.add(ImmutableSet.of('b', 'c', 'd'));
    expectedPowerSet.add(ImmutableSet.of('a', 'b', 'c', 'd'));
    
    Iterator<Set<Character>> expectedIter = expectedPowerSet.iterator();
    Iterator<SortedSet<Character>> actualIter = powerSet.iterator();
    while (expectedIter.hasNext() && actualIter.hasNext()) {
      assertEquals(expectedIter.next(), actualIter.next());
    }
    assertFalse(expectedIter.hasNext());
    assertFalse(actualIter.hasNext());
  }

}
