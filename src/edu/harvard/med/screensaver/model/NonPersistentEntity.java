// $HeadURL$
// $Id$
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import java.io.Serializable;

import edu.harvard.med.screensaver.ui.libraries.WellCopy;

/**
 * Entity that is a first-class member of the domain model, but is not
 * explicitly represented in the database, usually because it is more efficient
 * to derive this entity from other persistent entities when it is needed.
 * 
 * @author atolopko
 */
public abstract class NonPersistentEntity<K extends Serializable> implements Entity<K>
{
  private K _entityId;

  public NonPersistentEntity(K entityId)
  {
    _entityId = entityId;
  }

  @Override
  public K getEntityId()
  {
    return _entityId;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class getEntityClass()
  {
    return getClass();
  }

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return null;
  }

  @Override
  public void invalidate()
  {
  }

  @Override
  public void update()
  {
  }

  @Override
  public boolean isTransient()
  {
    return true;
  }

  @Override
  public boolean equals(Object obj)
  {
    return this == obj|| (obj != null && obj instanceof WellCopy && this.getEntityId().equals(((WellCopy) obj).getEntityId()));
  }
  
  @Override
  public int hashCode()
  {
    return getEntityId().hashCode();
  }

  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "(" + getEntityId() + ")";
  }
}
