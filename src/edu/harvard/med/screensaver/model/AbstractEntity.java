// $HeadURL:
// svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/model/AbstractEntity.java
// $
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import javax.persistence.Transient;

import edu.harvard.med.screensaver.db.accesspolicy.DataAccessPolicy;
import edu.harvard.med.screensaver.db.accesspolicy.DataAccessPolicyInjectorPostLoadEventListener;
import edu.harvard.med.screensaver.model.annotations.Column;
import edu.harvard.med.screensaver.model.libraries.Compound;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.hibernate.proxy.HibernateProxyHelper;

import sun.reflect.Reflection;


/**
 * An abstract superclass for the entity beans in the domain model. Provides an
 * abstract method {@link #getEntityId()} to define an entity id for all entity
 * classes.
 * <p>
 * Provides abstract method {@link #acceptVisitor(AbstractEntityVisitor)} to
 * implement a visitor pattern over the entity classes. The visitor pattern is
 * currently used to implement a core part of the data access policy for the
 * entity model. In the future, we may also use a similar AOP-style visitor
 * pattern to implement business rule violation checks.
 * <p>
 * Provides various helper methods, including
 * {@link #isEquivalent(AbstractEntity) a way to compare to entities for
 * equivalence}, and methods for truncating dates. (These date truncation
 * methods should be replaced in the future by fixing the data model to use a
 * type-mapping for date properties that does not include time-of-day.)
 * <p>
 * See {@link #equals} for important information on determining equality of
 * AbstractEntity objects.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public abstract class AbstractEntity implements Serializable
{

  // static fields

  private static Logger log = Logger.getLogger(AbstractEntity.class);


  // instance fields

  private DataAccessPolicy _dataAccessPolicy;
  private Integer _hashCode;


  // protected methods

  /**
   * Get the entity id. This is the identifier used by Hibernate, and is
   * generally implemented as a separate property in the entity classes. The
   * property is generally named by postfixing the entity name with "Id". For
   * instance, for {@link Compound}, this method delegates to the property read
   * method {@link Compound#getCompoundId()}.
   *
   * @return the entity id
   */
  abstract public Serializable getEntityId();

  /**
   * Equality is determined by the entity IDs, <i>if <code>this</code> entity
   * object has already been assigned an entity ID when it becomes "managed" by
   * the current Hibernate session (i.e., either it is was loaded by Hibernate
   * from the database or is an {@link SemanticIDAbstractEntity}). Otherwise,
   * equality is determined by "instance equality".
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
   * Performs a shallow compare of this <code>AbstractEntity</code> with
   * another and returns <code>true</code> iff they are the exact same class
   * and have matching values for each property, excluding properties that
   * return <code>Collection</code>, <code>Map</code>, and
   * <code>AbstractEntity</code>, which, presumably, return entity
   * relationships.
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
          if (thisValue == null ^ thatValue == null || thisValue != null &&
              !thisValue.equals(thatValue)) {
            log.debug("property '" + propertyName + "' differs: this='" +
                      thisValue + "', that='" + thatValue + "'");
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
   * To enable visitor to visit a particular subclass, override this method and
   * insert <code>visitor.acceptVisitor(this);</code>
   *
   * @param visitor
   * @motivation to keep most of our AbstractEntity subclasses clean, as we
   *             currently only have the DataAccessPolicy visitor, which does
   *             not actually need to visit every subclass.
   */
  abstract public Object acceptVisitor(AbstractEntityVisitor visitor);

  /**
   * Get whether this entity is restricted, based upon the data access policy
   * that was provided (if any). It is up to the controller and/or UI layers to
   * check for and determine how to handle restricted entities. In general, a
   * restricted entity is one whose data cannot be displayed to the current
   * user. However, the semantics of "restricted" is really defined by the data
   * access policy that is in force.
   *
   * @see DataAccessPolicyInjectorPostLoadEventListener
   */
  @Transient
  public boolean isRestricted()
  {
    if (_dataAccessPolicy == null) {
      return false;
    }
    Boolean isAllowed = (Boolean) acceptVisitor(_dataAccessPolicy);
    return isAllowed == null ? true : !isAllowed;
  }

  /**
   * @see DataAccessPolicyInjectorPostLoadEventListener
   */
  public void setDataAccessPolicy(DataAccessPolicy dataAccessPolicy)
  {
    _dataAccessPolicy = dataAccessPolicy;
  }

  /**
   * @see DataAccessPolicyInjectorPostLoadEventListener
   */
  @Transient
  public DataAccessPolicy getDataAccessPolicy()
  {
    return _dataAccessPolicy;
  }

  /**
   * @motivation sometimes we need the "real" class of the concrete
   *             AbstractEntity, and will not tolerate having the proxy subclass
   * @return the concrete Class of this AbstractEntity, with the caveat that is
   *         not a proxy subclass
   */
  @SuppressWarnings("unchecked")
  @Transient
  final public Class<? extends AbstractEntity> getEntityClass()
  {
    return HibernateProxyHelper.getClassWithoutInitializingProxy(this);
  }


  // protected methods

  /**
   * Remove the time portion of the date and return the result.
   *
   * @param originalDate the date to truncate
   * @return the truncated date
   */
  protected Date truncateDate(Date originalDate)
  {
    return truncateDate(originalDate, Calendar.DATE);
  }

  /**
   * Remove the time portion of the date that is less significant than
   * <code>mostSignificantField</code>.
   *
   * @param originalDate the date to truncate
   * @param mostSignificantField the field from Calendar
   * @return the truncated date
   */
  protected Date truncateDate(Date originalDate, int mostSignificantField)
  {
    if (originalDate == null) {
      return null;
    }
    return DateUtils.truncate(originalDate, mostSignificantField);
  }

  /**
   * Return true iff the caller of the method that is calling this method is
   * from the hibernate world. Specifically, we test the package name of the
   * calling class for a "org.hibernate." prefix.
   *
   * @return true iff the caller of the method that is calling this method is
   *         from the hibernate world
   */
  // TODO: try to replace this with a listener on the hibernate event model. see
  // DataAccessPolicyInjectorPostLoadEventListener for example
  protected boolean isHibernateCaller()
  {
    return getCallingClassName().startsWith("org.hibernate.");
  }

  @SuppressWarnings("unchecked")
  public <P> P getPropertyValue(String propertyName, Class<P> propertyType)
  {
    try {
      PropertyDescriptor propertyDescriptor =
        PropertyUtils.getPropertyDescriptor(this, propertyName);
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
      log.debug("no corresponding getter method for property " +
                property.getDisplayName());
      return false;
    }
    // only test methods that are declared by subclasses of AbstractEntity
    if (method.getDeclaringClass()
              .equals(AbstractEntity.class) ||
        !AbstractEntity.class.isAssignableFrom(method.getDeclaringClass())) {
      return false;
    }
    if (method.getAnnotation(Transient.class) != null) {
      return false;
    }
    if (method.getAnnotation(Column.class) != null &&
        method.getAnnotation(Column.class)
              .isNotEquivalenceProperty()) {
      return false;
    }

    return !(Collection.class.isAssignableFrom(property.getPropertyType()) ||
             Map.class.isAssignableFrom(property.getPropertyType()) || AbstractEntity.class.isAssignableFrom(property.getPropertyType()));
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
