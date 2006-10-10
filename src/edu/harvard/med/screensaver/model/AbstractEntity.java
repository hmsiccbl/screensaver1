// $HeadURL$
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

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;


/**
 * An abstract superclass for the entity beans in the data model.
 * 
 * <p>
 * 
 * Conventions for implementing classes:
 * 
 * <ul>
 *   <li>
 *     all public constructors must initialize the business key.
 *   </li>
 *   <li>
 *     the public constructor(s) with the maximum number of arguments
 *     must initialize all not-null properties.
 *   </li>
 *   <li>
 *     Hibernate requires a zero-parameter constructor.
 *   </li>
 *   <li>
 *     we try to put the properties in the same order in these differing
 *     contexts:
 *     <ul>
 *       <li>
 *         instance fields
 *       </li>
 *       <li>
 *         constructor parameters
 *       </li>
 *       <li>
 *         public accessors
 *       </li>
 *       <li>
 *         private Hibernate accessors
 *       </li>
 *     </ul>
 *   </li>
 *   <li>
 *     the getter for the Hibernate id is public; the setter for the Hibernate
 *     id is private.
 *   </li>
 *   <li>
 *     the Hibernate version is an Integer name <code>_version</code>. the
 *     Hibernate accessors to the version are private, and are named
 *     <code>getVersion</code> and <code>setVersion</code>
 *   </li>
 *   <li>
 *     in general, any method that is exclusively for Hibernate use is private
 *   </li>
 *   <li>
 *     boolean properties are always not-null and primitive boolean type
 *     (Boolean.TYPE and not Boolean.class).
 *   </li>
 *   <li>
 *     collection properties (properties whose getter method returns a Collection)
 *     have a public getter for the whole collection, e.g., <code>getBars</code>;
 *     have public methods <code>addBar</code> and <code>removeBar</code> that
 *     each take a <code>Bar</code> object, and behave in a similar fashion to
 *     <code>Collection.add</code> and <code>Collection.remove</code>. In
 *     particular, if the collection is a <code>Set</code>, then the add and
 *     remove operators return the boolean value <code>true</code> whenever
 *     the operation actually added or removed the <code>Bar</code> (whether it
 *     was not previously in the set, and whether it was previously in the set,
 *     respectively).
 *     <p>
 *     The add method is required. The remove method is not. Remove methods are
 *     absent when the other side of the relationship is not-null. In this case,
 *     to remove the related entity, you must call the setter on the inverse
 *     property of the related entity.
 *     <p>
 *     A setter method for the Collection property only exists when needed
 *     by Hibernate, and is private. (Only needed when the property does not
 *     represent a relationship.)
 *   </li>
 *   <li>
 *     relationship properties always maintain bidirectionality of the
 *     relationship. Because this usage conflicts with Hibernate usage,
 *     relationship properties always come in pairs: a "JavaBean" version of
 *     the property, which is named traditionally, e.g., <code>getFoo</code>
 *     and <code>setFoo</code>; and a Hibernate version of the property, which
 *     is named by prefixing the JavaBean property with "hbn", e.g.,
 *     <code>getHbnFoo</code> and <code>setHbnFoo</code>
 *   </li>
 *   <li>
 *     note that the combination of the previous two bullet items will dictate
 *     the following set of methods for a collection property <code>foos</code>
 *     that is also a relationship:
 *     <p>
 *     <pre>
 *       public Set<Foo> getFoos();
 *       public boolean addFoo(Foo foo);
 *       public boolean removeFoo(Foo foo);
 *       private Set<Foo> getHbnFoo();
 *       private void setHbnFoo(Set<Foo>);
 *     </pre>
 *   </li>
 *   <li>
 *     TODO: more stuff that just hasn't been recorded yet...
 *   </li>
 * </ul>
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */

public abstract class AbstractEntity implements Serializable
{
  private static Logger log = Logger.getLogger(AbstractEntity.class);
  
  // protected methods
  
  /**
   * Get the entity id. This is the identifier used by Hibernate, and is
   * generally implemented as a separate property in the entity classes.
   * The property is generally named by postfixing the entity name with
   * "Id". For instance, for {@link Compound}, this method delegates to
   * the property read method {@link Compound#getCompoundId()}.
   * 
   * @return the entity id
   */
  abstract public Serializable getEntityId();

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
          // TODO: following getProperty() complains that it can't find the getter method!
//          Object thisValue = PropertyUtils.getProperty(this,
//                                                       propertyName);
//          Object thatValue = PropertyUtils.getProperty(that,
//                                                       propertyName);
          if (thisValue == null ^ thatValue == null ||
            thisValue != null && !thisValue.equals(thatValue)) {
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
  
  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object object) {
    if (object == null) {
      return false;
    }
    if (! getClass().equals(object.getClass())) {
      return false;
    }
    AbstractEntity that = (AbstractEntity) object;
    return getBusinessKey().equals(that.getBusinessKey());
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return getBusinessKey().hashCode();
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "(" + getBusinessKey().toString() + ")";
  }

  
  // protected methods
  
  /**
   * Return the business key for the entity.
   * @return the business key
   */
  abstract protected Object getBusinessKey();

  /**
   * Remove the time portion of the date and return the result.
   * @param originalDate the date to truncate
   * @return the truncated date
   */
  protected Date truncateDate(Date originalDate)
  {
    if (originalDate == null) {
      return null;
    }
    return DateUtils.round(originalDate, Calendar.DATE);
  }
  
  
  // private methods
  
  /**
   * Determine if a given property should be used in determining equivalence.
   * @return boolean (see code, since this is private method)
   * @see #isEquivalent(AbstractEntity)
   */
  // TODO: can we annotate a bean's properties with "@equivalence" and do some
  // introspection to retrieve these annotated "equivalence" properties, rather
  // than relying upon the below heuristics?
  private boolean isEquivalenceProperty(PropertyDescriptor property) {
    Method method = property.getReadMethod();
    if (method == null) {
      // this can occur if there is a public setter method, but a non-public getter method
      log.debug("no corresponding getter method for property " + property.getDisplayName());
      return false;
    }
    // only test methods that are declared by subclasses of AbstractEntity 
    if (method.getDeclaringClass().equals(AbstractEntity.class) ||
      !AbstractEntity.class.isAssignableFrom(method.getDeclaringClass())) {
      return false;
    }
    if (method.getName().startsWith("getHbn")) {
      return false;
    }

    return 
      !(Collection.class.isAssignableFrom(property.getPropertyType()) ||
        Map.class.isAssignableFrom(property.getPropertyType()) ||
        AbstractEntity.class.isAssignableFrom(property.getPropertyType()));
  }
}
