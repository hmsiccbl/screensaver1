// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import javax.persistence.Embeddable;
import javax.persistence.Transient;

import com.google.common.base.Function;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;
import org.hibernate.proxy.HibernateProxyHelper;
import sun.reflect.Reflection;

import edu.harvard.med.screensaver.db.accesspolicy.EntityViewPolicyInjectorPostLoadEventListener;
import edu.harvard.med.screensaver.model.annotations.Column;
import edu.harvard.med.screensaver.policy.EntityViewPolicy;

/**
 * An abstract superclass for the entity beans in the domain model. Provides an
 * abstract method {@link #getEntityId()} to define an entity id for all entity
 * classes.
 * <p>
 * Provides a passive mechanism for enforcing a data access policy via the
 * {@link #isRestricted()} method.
 * <p>
 * Provides abstract method {@link #acceptVisitor(AbstractEntityVisitor)} to
 * implement a visitor pattern over the entity classes. The visitor pattern is
 * currently used to implement a core part of the data access policy for the
 * entity model. (In the future, we may also use a similar AOP to implement
 * business rule violation checks.)
 * <p>
 * See {@link #equals} and {@link #hashCode()} for important information on
 * determining equality of AbstractEntity objects.
 * <p>
 * Provides various helper methods, including
 * {@link #isEquivalent(AbstractEntity) a way to compare to entities for
 * equivalence}, and a way to {@link #isHibernateCaller() determine if a
 * subclasses's setter or getter methods are being called by Hibernate}, both of
 * which happen to be hacks.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public abstract class AbstractEntity<K extends Serializable> implements Entity<K>, Serializable
{
  private static final long serialVersionUID = 1L;

  private static Logger log = Logger.getLogger(AbstractEntity.class);
  
  private EntityViewPolicy<Entity> _entityViewPolicy;
  private K _entityId;
  private Integer _hashCode;
  private boolean _needsUpdate;

  public static <E extends Entity> Function<E,E> ToRestricted()
  {
    return new Function<E,E>() {
      @Override
      public E apply(E from)
      {
        return (E) from.restrict();
      }
    };
  }

  protected void traceEvent(String event)
  {
    if (log.isDebugEnabled()) {
      log.debug(event + " " + this);
    }
  }

  @Transient
  public K getEntityId()
  {
    return _entityId;
  }

  protected void setEntityId(K entityId)
  {
    _entityId = entityId;
  }

  @Transient
  public boolean isTransient()
  {
    return getEntityId() == null;
  }

  /**
   * Equality is determined by the entity IDs, <i>if</i> the entity object has
   * already been assigned an entity ID when it becomes "managed" by the current
   * Hibernate session (i.e., either it is was loaded by Hibernate from the
   * database or is a {@link SemanticIDAbstractEntity}). Otherwise, equality is
   * determined by "instance equality".
   * <p>
   * Thus, if an entity that is transient is then made persistent within a
   * Hibernate session, it will <i>never</i> be considered equal to an instance
   * <i>with the same entity ID</i> that is loaded in a subsequent Hibernate
   * session. To avoid problems, any transient entity that is persisted in a
   * Hibernate session should never be referenced again once the session ends.
   * Obeying this rule avoids the unexpected result of two different entity
   * objects that represent the "same" entity (in terms of ID) being considered
   * unequal.
   * 
   * @see #hashCode
   */
  @Override
  public boolean equals(Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof AbstractEntity)) {
      return false;
    }
    AbstractEntity other = (AbstractEntity) obj;
    return hashCode() == other.hashCode();
  }

  /**
   * Returns the hashCode for this AbstractEntity, which will be the hashCode of
   * the entity ID, if the ID is already assigned, otherwise it is
   * Object.hashCode(). If the hashCode is requested while this entity is
   * transient, and thus without an assigned entity ID, the hashCode will not
   * change even if the entity later becomes persistent and acquires an ID. This
   * is necessary to obey the contract of hashCode(). See equals() for the
   * implications of this design.
   * 
   * @see #equals
   */
  @Override
  public int hashCode()
  {
    if (_hashCode == null) {
      if (getEntityId() == null) {
        _hashCode = super.hashCode();
      }
      else {
        _hashCode = getEntityId().hashCode();
      }
    }
    return _hashCode;
  }

  /**
   * Performs a shallow compare of this <code>AbstractEntity</code> with another
   * and returns <code>true</code> iff they are the exact same class and have
   * matching values for each property, excluding properties that return
   * <code>Collection</code>, <code>Map</code>, and <code>AbstractEntity</code>,
   * which, presumably, return entity relationships.
   * 
   * @motivation for comparing entities in test code
   * @param that the other AbstractEntity to compare equivalency with
   * @return true iff the two AbstractEntities are equivalent
   */
  public boolean isEquivalent(AbstractEntity that)
  {
    if (!this.getClass().equals(that.getClass())) {
      return false;
    }
    PropertyDescriptor[] beanProperties = PropertyUtils.getPropertyDescriptors(this.getClass());

    for (int i = 0; i < beanProperties.length; i++) {
      PropertyDescriptor beanProperty = beanProperties[i];
      if (isEquivalenceProperty(beanProperty)) {
        String propertyName = beanProperty.getName();
        try {
          Object thisValue = beanProperty.getReadMethod().invoke(this);
          Object thatValue = beanProperty.getReadMethod().invoke(that);
          if (thisValue == null ^ thatValue == null || thisValue != null && !thisValue.equals(thatValue)) {
            log.debug("property '" + propertyName + "' differs: this='" + thisValue + "', that='" + thatValue + "'");
            return false;
          }
        }
        catch (Exception e) {
          log.error("error comparing bean properties: " + e.getMessage());
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "(" + getEntityId() + ")";
  }

  /**
   * Get whether this entity is fully restricted, based upon the {@link EntityViewPolicy} that was provided (if any). A
   * restricted entity is one whose data should not be made visible to the current user. Note that this is a passive
   * data access policy enforcement mechanism, in that it is up to the service and/or UI layers to check for and
   * determine how to handle restricted entities.
   * 
   * @see #restrict()
   * @see EntityViewPolicyInjectorPostLoadEventListener
   * @throws UnsupportedOperationException if entityViewPolicy not set
   */
  @Transient
  public boolean isRestricted()
  {
    return restrict() == null;
  }

  /**
   * Get a partially-restricted version of this entity, based upon the {@link EntityViewPolicy} that was provided (if
   * any). A partially-restricted entity is one that may have a subset of properties that should not be visible to the
   * current user. Note that this is a passive data access policy enforcement mechanism, in that it is up to the service
   * and/or UI layers to check for and determine how to handle restricted entities.
   * 
   * @return if entity has partial restrictions on its properties, a new instance of this entity will be returned; if a
   *         new instance is returned it will not be persistence-managed instance; null will be returned if
   *         {@link #isRestricted()} return true; the same instance (<code>this</code>) may be returned if the entity is
   *         fully unrestricted, but this is not guaranteed
   * @see #isRestricted()
   * @see EntityViewPolicyInjectorPostLoadEventListener
   * @throws UnsupportedOperationException if entityViewPolicy not set
   */
  @Transient
  public Entity<K> restrict()
  {
    if (isTransient()) {
      // new entities will not have a entityViewPolicy injected yet, and it should not be restricted in any case, if the user created it 
      return this;
    }
    if (_entityViewPolicy == null) {
      throw new UnsupportedOperationException("entityViewPolicy not set");
    }
    return acceptVisitor(_entityViewPolicy);
  }

  /**
   * @see EntityViewPolicyInjectorPostLoadEventListener
   */
  public void setEntityViewPolicy(EntityViewPolicy<Entity> entityViewPolicy)
  {
    _entityViewPolicy = entityViewPolicy;
  }

  /**
   * @see EntityViewPolicyInjectorPostLoadEventListener
   */
  @Transient
  public EntityViewPolicy getEntityViewPolicy()
  {
    return _entityViewPolicy;
  }

  @SuppressWarnings("unchecked")
  @Transient
  final public Class<Entity<K>> getEntityClass()
  {
    return HibernateProxyHelper.getClassWithoutInitializingProxy(this);
  }

  // protected methods

  /**
   * Return true iff the caller of the method that is calling this method is
   * from the hibernate world. Specifically, we test the package name of the
   * calling class for a "org.hibernate." prefix.
   * 
   * @return true iff the caller of the method that is calling this method is
   *         from the hibernate world
   */
  // TODO: try to replace this with a listener on the hibernate event model. see
  // EntityViewPolicyInjectorPostLoadEventListener for example
  protected boolean isHibernateCaller()
  {
    // TODO: HACK: fix this awful hack!
    for (int i = 1; i <= 6; i++) {
      String callingClass = Reflection.getCallerClass(i).getName();
      if (log.isDebugEnabled()) {
        log.debug("caller " + i + ": " + callingClass);
      }
      if (callingClass.startsWith("org.hibernate.")) {
        return true;
      }
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  public <P> P getPropertyValue(String propertyName, Class<P> propertyType)
  {
    try {
      PropertyDescriptor propertyDescriptor = PropertyUtils.getPropertyDescriptor(this, propertyName);
      return (P) propertyDescriptor.getReadMethod().invoke(this);
    }
    catch (Exception e) {
      log.error(e);
      return null;
    }
  }

  // private methods

  /**
   * Determine if a given property should be used in determining equivalence.
   * 
   * @return boolean (see code, since this is private method)
   * @see #isEquivalent(AbstractEntity)
   */
  // TODO: can we annotate a bean's properties with "@equivalence" and do some
  // introspection to retrieve these annotated "equivalence" properties, rather
  // than relying upon the below heuristics?
  private boolean isEquivalenceProperty(PropertyDescriptor property)
  {
    Method method = property.getReadMethod();
    if (method == null) {
      // this can occur if there is a public setter method, but a non-public
      // getter method
      log.debug("no corresponding getter method for property " + property.getDisplayName());
      return false;
    }
    // only test methods that are declared by subclasses of AbstractEntity
    if (method.getDeclaringClass().equals(AbstractEntity.class) || !AbstractEntity.class.isAssignableFrom(method.getDeclaringClass())) {
      return false;
    }
    if (method.getAnnotation(Transient.class) != null) {
      return false;
    }
    if (method.getAnnotation(Column.class) != null && method.getAnnotation(Column.class).isNotEquivalenceProperty()) {
      return false;
    }
    // do not check embeddable types (as this would require descending into the embeddable to check equivalence)
    if (property.getPropertyType().getAnnotation(Embeddable.class) != null) {
      return false;
    }

    return !(Collection.class.isAssignableFrom(property.getPropertyType()) ||
      Map.class.isAssignableFrom(property.getPropertyType()) || AbstractEntity.class.isAssignableFrom(property.getPropertyType()));
  }
}
