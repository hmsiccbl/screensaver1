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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.collect.Lists;
import org.apache.commons.collections.Closure;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.math.IntRange;
import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.Entity;

public class CollectionUtils
{
  // static members

  private static Logger log = Logger.getLogger(CollectionUtils.class);


  // instance data members

  // public constructors and methods

  /**
   * Indexes a collection by creating a map that allows each element of the
   * specified collection to be looked up by its key. The key of each element is
   * determined by calling the <code>makeKey</code> Transformer on that
   * element. I sure miss Lisp.
   *
   * @return a Map
   */
  public static <K, E> Map<K,E> indexCollection(Collection c,
                                                final Transformer getKey,
                                                Class<K> keyClass,
                                                Class<E> elementClass)
  {
    final Map<K,E> map = new HashMap<K,E>(c.size());
    org.apache.commons.collections.CollectionUtils.forAllDo(c, new Closure() {
      @SuppressWarnings("unchecked")
      public void execute(Object e)
      {
        map.put((K) getKey.transform(e), (E) e);
      }
    });
    return map;
  }


  public static <T> List<T> listOf(T e, int i)
  {
    ArrayList<T> l = Lists.newArrayListWithCapacity(i);
    fill(l, e, i);
    return l;
  }

  public static <T> void fill(Collection<T> c, T e, int i)
  {
    for (; i > 0; i--) {
      c.add(e);
    }
  }

  public static List<String> toStrings(Collection<?> c)
  {
    List<String> result = new ArrayList<String>(c.size());
    for (Object e : c) {
      result.add(e.toString());
    }
    return result;
  }

  public static SortedSet<String> toUniqueStrings(Collection<?> c)
  {
    SortedSet<String> result = new TreeSet<String>();
    for (Object e : c) {
      result.add(e.toString());
    }
    return result;
  }
  
  @SuppressWarnings("unchecked")
  public static <I> Set<I> entityIds(Collection<? extends Entity> entities)
  {
    Set<I> ids = new HashSet<I>(entities.size());
    for (Entity entity : entities) {
      ids.add((I) entity.getEntityId());
    }
    return ids;
  }


  public static List<IntRange> splitIntoSequentialRanges(SortedSet<Integer> integers)
  {
    List<IntRange> ranges = Lists.newArrayList();
    Iterator<Integer> iter = integers.iterator();
    if (iter.hasNext()) {
      IntRange range = new IntRange(iter.next());
      while (iter.hasNext()) {
        int next = iter.next();
        if (next - 1 == range.getMaximumInteger()) {
          range = new IntRange(range.getMinimumInteger(), next);
        }
        else {
          ranges.add(range);
          range = new IntRange(next);
        }
      }
      ranges.add(range);
    }
    return ranges;
  }


  // private methods

}

