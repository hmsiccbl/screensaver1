// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import junit.framework.TestCase;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.math.IntRange;
import org.apache.log4j.Logger;

public class CollectionUtilsTest extends TestCase
{
  private static Logger log = Logger.getLogger(CollectionUtilsTest.class);

  private static class Struct
  {
    Integer key;
    String value;

    Struct(Integer key, String value) 
    {
      this.key = key; 
      this.value = value;
    }
  }
  
  public void testIndexCollection()
  {
    Transformer getKey = new Transformer() {
      public Object transform(Object e)
      {
        return ((Struct) e).key;
      }
    };
    List<Struct> c = new ArrayList<Struct>();
    c.add(new Struct(1, "A"));
    c.add(new Struct(2, "B"));
    c.add(new Struct(3, "C"));

    Map<Integer,Struct> index = CollectionUtils.indexCollection(c,
                                                                getKey,
                                                                Integer.class,
                                                                Struct.class);
    assertEquals(c.get(0), index.get(1));
    assertEquals(c.get(1), index.get(2));
    assertEquals(c.get(2), index.get(3));
  }

  public void testSplitIntoSequentialRanges()
  {
    assertEquals(ImmutableList.of(new IntRange(1)),
                 CollectionUtils.splitIntoSequentialRanges(ImmutableSortedSet.of(1)));
    assertEquals(ImmutableList.of(new IntRange(1, 3)),
                 CollectionUtils.splitIntoSequentialRanges(ImmutableSortedSet.of(1, 2, 3)));
    assertEquals(ImmutableList.of(new IntRange(1, 1),
                                  new IntRange(3, 3)),
                 CollectionUtils.splitIntoSequentialRanges(ImmutableSortedSet.of(1, 3)));
    assertEquals(ImmutableList.of(new IntRange(1, 2),
                                  new IntRange(4, 5)),
                 CollectionUtils.splitIntoSequentialRanges(ImmutableSortedSet.of(1, 2, 4, 5)));
    assertEquals(ImmutableList.of(new IntRange(1, 2),
                                  new IntRange(4, 4)),
                                  CollectionUtils.splitIntoSequentialRanges(ImmutableSortedSet.of(1, 2, 4)));
    assertEquals(ImmutableList.of(new IntRange(1, 2),
                                  new IntRange(4, 4),
                                  new IntRange(6, 8)),
                                  CollectionUtils.splitIntoSequentialRanges(ImmutableSortedSet.of(1, 2, 4, 6, 7, 8)));
  }
}

