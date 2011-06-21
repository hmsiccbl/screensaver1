// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import java.io.Serializable;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import edu.harvard.med.screensaver.policy.EntityViewPolicy;

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

  public static final Predicate<Entity> NotRestricted = new Predicate<Entity>() {
    public boolean apply(Entity e)
    {
      return !!!e.isRestricted();
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

  Entity<K> restrict();

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
  <R> R acceptVisitor(AbstractEntityVisitor<R> visitor);
}
