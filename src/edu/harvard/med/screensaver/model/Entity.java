// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import java.io.Serializable;

import edu.harvard.med.screensaver.policy.EntityViewPolicy;

import com.google.common.base.Function;

/** 
 * Interface for domain model entity classes.
 */
public interface Entity<K extends Serializable>
{
  public static final Function<Entity,Serializable> ToEntityId = new Function<Entity,Serializable>() {
    public Serializable apply(Entity entity)
    {
      return entity.getEntityId();
    }
  }; 

  /**
   * @return a unique identifier for this entity
   */
  K getEntityId();
  
  /**
   * @return true if this entity has not been persisted, which implies that {@link #getEntityId()} returns null
   */
  boolean isTransient();

  /**
   * @return true if the {@link EntityViewPolicy} that is currently in use has
   *         determined that the current user should not be allowed to access
   *         this entity
   */
  boolean isRestricted();

  /**
   * Invoke domain model logic to update derived properties that can only be
   * calculated when the entity is in a consistent state. This method should
   * only perform the updates if {@link #invalidate()} has been called.
   */
  void update();

  /**
   * Flag this entity as being in need of an update of its derived properties.
   * The {@link Entity#update()} method must be called to actually perform the
   * updates when the entity is in a consistent state.
   */
  void invalidate();

  /**
   * Return the "real" class of this entity, even when the object is a proxy.
   * 
   * @motivation sometimes entities are returned as proxies by Hibernate and we
   *             need the "real" class of the concrete Entity, and will not
   *             tolerate having the proxy subclass
   * @return the concrete Class of this Entity, with the caveat that it
   *         is not a proxy subclass
   */
  Class<Entity<K>> getEntityClass();

  /**
   * To enable visitor to visit a particular subclass, override this method and
   * insert <code>visitor.acceptVisitor(this);</code>
   * 
   * @param visitor
   * @motivation to keep most of our AbstractEntity subclasses clean, as we
   *             currently only have the EntityViewPolicy visitor, which does
   *             not actually need to visit every subclass.
   */
   Object acceptVisitor(AbstractEntityVisitor visitor);
}
