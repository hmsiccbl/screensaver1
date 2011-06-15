// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import edu.harvard.med.screensaver.model.Entity;

public class NoSuchEntityException extends RuntimeException
{
  private static final long serialVersionUID = 1L;
  private static final Function<Entry<String,Object>,String> ToPropertyValueString =
    new Function<Entry<String,Object>,String>()
    {
      public String apply(Entry<String,Object> entry)
      {
        return entry.getKey() + " " + entry.getValue();
      }
    };
  private Map<String,Object> _propertyValues;
  
  public static NoSuchEntityException forEntityId(Class<? extends Entity> entityClass, Serializable entityId)
  {
    return forProperty(entityClass, "id", entityId);
  }
  
  public static NoSuchEntityException forProperty(Class<? extends Entity> entityClass, String propertyName, Object propertyValue)
  {
    return new NoSuchEntityException(entityClass, ImmutableMap.of(propertyName, propertyValue));
  }

  public static NoSuchEntityException forProperties(Class<? extends Entity> entityClass, Map<String,Object> propertyValues)
  {
    return new NoSuchEntityException(entityClass, propertyValues);
  }

  private NoSuchEntityException(Class<? extends Entity> entityClass, Map<String,Object> propertyValues)
  {
    super("no such " + entityClass.getSimpleName() + " for " +
          Joiner.on(", ").join(Iterables.transform(propertyValues.entrySet(), ToPropertyValueString)));
    _propertyValues = propertyValues;
  }

  public Map<String,Object> getPropertyValues()
  {
    return _propertyValues;
  }
}
