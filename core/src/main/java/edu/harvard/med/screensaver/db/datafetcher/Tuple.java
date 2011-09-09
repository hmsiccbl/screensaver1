// $HeadURL$
// $Id$
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.datafetcher;

import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class Tuple<K>
{

  private K _key;
  private Map<String,Object> _properties = Maps.newHashMap();

  public Tuple(K key)
  {
    _key = key;
  }

  public static <K> Function<Tuple<K>,K> toKey()
  {
    return new Function<Tuple<K>,K>() {
      @Override
      public K apply(Tuple<K> t)
      {
        return t._key;
      }
    };
  }

  public K getKey()
  {
    return _key;
  }

  public Object getProperty(String propertyKey)
  {
    return _properties.get(propertyKey);
  }

  public void addProperty(String propertyKey, Object propertyValue)
  {
    _properties.put(propertyKey, propertyValue);
  }

  public void addMultiPropertyElement(String propertyKey, Object propertyValue)
  {
    if (!_properties.containsKey(propertyKey)) {
      List<Object> values = Lists.newArrayList();
      _properties.put(propertyKey, values);
    }
    if (propertyValue != null) {
      ((List<Object>) _properties.get(propertyKey)).add(propertyValue);
    }
  }

  @Override
  public int hashCode()
  {
    return getKey().hashCode();
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj != null) {
      if (obj == this) {
        return true;
      }
      if (obj instanceof Tuple) {
        return getKey().equals(((Tuple) obj).getKey());
      }
    }
    return false;
  }
}
