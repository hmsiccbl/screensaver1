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
import java.util.List;
import java.util.Map;

import javax.persistence.Transient;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;
import org.hibernate.proxy.HibernateProxyHelper;
import sun.reflect.Reflection;

import edu.harvard.med.screensaver.db.accesspolicy.EntityViewPolicyInjectorPostLoadEventListener;
import edu.harvard.med.screensaver.domainlogic.EntityUpdater;
import edu.harvard.med.screensaver.model.annotations.Column;
import edu.harvard.med.screensaver.policy.EntityViewPolicy;
import edu.harvard.med.screensaver.util.DevelopmentException;

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
  
  private EntityViewPolicy _entityViewPolicy;
  private List<EntityUpdater> _entityUpdaters;
  private K _entityId;
  private Integer _hashCode;
  private boolean _needsUpdate;

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
   * Get whether this entity is restricted, based upon the data access policy
   * that was provided (if any). This is a passive data access policy
   * enforcement mechanism, in that it is up to the service and/or UI layers to
   * check for and determine how to handle restricted entities. A
   * restricted entity is one whose data should not be made visible to the current
   * user.
   * 
   * @see EntityViewPolicyInjectorPostLoadEventListener
   * @throws UnsupportedOperationException if entityViewPolicy not set
   */
  @Transient
  public boolean isRestricted()
  {
    if (_entityViewPolicy == null) {
      throw new UnsupportedOperationException("entityViewPolicy not set");
    }
    Boolean isAllowed = (Boolean) acceptVisitor(_entityViewPolicy);
    return isAllowed == null ? true : !isAllowed;
  }

  /**
   * @see EntityViewPolicyInjectorPostLoadEventListener
   */
  public void setEntityViewPolicy(EntityViewPolicy entityViewPolicy)
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
  
  public void setEntityUpdaters(List<EntityUpdater> entityUpdaters)
  {
    _entityUpdaters = entityUpdaters;
  }

  @Transient
  public List<EntityUpdater> getEntityUpdaters()
  {
    return _entityUpdaters;
  }

  @Override
  public void invalidate()
  {
    log.debug("invalidated " + this + " (domain logic updaters will be invoked on flush)");
    
    _needsUpdate = true;
  }

  @Override
  public void update()
  {
    if (_needsUpdate) {
      log.debug(this + " domain logic updaters will be invoked now");
      if (_entityUpdaters == null) {
        throw new DevelopmentException("entity has not been injected with EntityUpdaters");
      }
      for (EntityUpdater entityUpdater : _entityUpdaters) {
        log.info("invoking domain logic updater " + entityUpdater.getClass() + " on " + this);
        entityUpdater.apply((Entity) this);
      }
      _needsUpdate = false;
    }
    else {
      log.debug(this + " does not need domain logic updaters to be invoked"); 
    }
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
    return getCallingClassName().startsWith("org.hibernate.");
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

    return !(Collection.class.isAssignableFrom(property.getPropertyType()) || Map.class.isAssignableFrom(property.getPropertyType()) || AbstractEntity.class.isAssignableFrom(property.getPropertyType()));
  }

  /**
   * Return the name of the calling class of the caller of the method of the
   * caller of this method that is calling this method. Specifically, we intend
   * the caller of this method to be {@link #isHibernateCaller()} or
   * {@link #isTestCaller()}, and the caller of that method to be an entity
   * method that is trying to determine <i>its</i> caller.
   * 
   * @return the name of the calling class of the caller of the method of the
   *         caller of this method that is calling this method
   */
  private String getCallingClassName()
  {
    // TODO: try to do this with a java.* class
    return Reflection.getCallerClass(4).getName();
  }
}
