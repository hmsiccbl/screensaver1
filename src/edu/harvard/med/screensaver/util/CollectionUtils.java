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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.harvard.med.screensaver.model.AbstractEntity;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.Transformer;
import org.apache.log4j.Logger;

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


  public static <T> void fill(Collection<T> c, T o, int i)
  {
    for (; i > 0; i--) {
      c.add(o);
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
  public static <I> Set<I> entityIds(Collection<? extends AbstractEntity> entities)
  {
    Set<I> ids = new HashSet<I>(entities.size());
    for (AbstractEntity entity : entities) {
      ids.add((I) entity.getEntityId());
    }
    return ids;
  }


  // private methods

}

