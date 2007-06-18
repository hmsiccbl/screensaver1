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

import edu.harvard.med.screensaver.db.accesspolicy.DataAccessPolicy;
import edu.harvard.med.screensaver.db.accesspolicy.DataAccessPolicyInjectorPostLoadEventListener;
import edu.harvard.med.screensaver.model.libraries.Compound;

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
 *     All public constructors must initialize the business key.
 *   </li>
 *   <li>
 *     The public constructor(s) with the maximum number of arguments
 *     must initialize all not-null properties.
 *   </li>
 *   <li>
 *     Hibernate requires a zero-parameter constructor.
 *   </li>
 *   <li>
 *     We try to put the properties in the same order in these differing
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
 *     The getter for the Hibernate id is public; the setter for the Hibernate
 *     id is private.
 *   </li>
 *   <li>
 *     The Hibernate version is an Integer name <code>_version</code>. the
 *     Hibernate accessors to the version are private, and are named
 *     <code>getVersion</code> and <code>setVersion</code>
 *   </li>
 *   <li>
 *     In general, any method that is exclusively for Hibernate use is private
 *   </li>
 *   <li>
 *     Boolean properties are always not-null and primitive boolean type
 *     (Boolean.TYPE and not Boolean.class).
 *   </li>
 *   <li>
 *     Collection properties (properties whose getter method returns a Collection)
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
 *     The add method is required unless the other side of the relationship is 
 *     {many,one}-to-one and not-null (i.e., it has a foreign key constraint, 
 *     more below), in which case it is not allowed. The remove method is always 
 *     optional, but not allowed whenever the add method is not allowed.
 *     <p>
 *     A setter method for the Collection property only exists when needed
 *     by Hibernate, and is private. (Only needed when the property does not
 *     represent a relationship.)
 *   </li>
 *   <li>
 *     Relationship properties should maintain bidirectionality of the
 *     relationship. If, for performance reasons, a unidirectional relationship is desired,
 *     the getter method should be annotated with the {@link ToManyRelationship} or 
 *     {@link ToOneRelationship} annotation, with the <code>unidirectional</code> flag 
 *     set to true.
 *   <li>
 *     Bidirectional relationships require that the entity Java objects on both 
 *     sides of the relationship be updated, at the same time, to reflect a new or 
 *     deleted association between the two entities.  
 *     As Hibernate does not allow an entity's collections to be modified while it 
 *     is loading its state from the database (via its setter methods), we are 
 *     forced into having <i>two pairs</i> of getter and setter methods for each relationship:
 *     <ul>
 *       <li>
 *         A Hibernate version of the property, which is named by prefixing the 
 *         JavaBean property with "hbn", e.g., <code>getHbnFoo</code> and 
 *         <code>setHbnFoo</code>. The Hibernate pair should simply set and return 
 *         the object's data member (an AbstractEntity or Collection<AbstractEntity>), 
 *         without any side-effects to other data members or other, related entitys.  
 *       </li>
 *       <li>
 *         A "JavaBean" version of the property, which is named traditionally, e.g., 
 *         <code>getFoo</code> and <code>setFoo</code>. The JavaBean version's 
 *         setter is responsible for both setting its own entity's 
 *         relationship data member, as well as updating the related entity's 
 *         relationship, via calls to the relatedEntity.setHbnFoo() method.  
 *         Note that the JavaBean setter method must take special care if the 
 *         related entity is also used to define "this" entity's business key 
 *         or entity ID; in this case the entity must be removed from all 
 *         applicable relationships, then update the business key or entity ID,
 *         and finally reinstate then applicable relationships.  If the JavaBean 
 *         getter method represents the "many" side of a relationship, it will 
 *         return a collection, and the returned collection should always be 
 *         immutable.
 *       </li>
 *     </ul>
 *   </li>
 *   <li>
 *      If a property defines, or particpates in, the entity's ID, it should be
 *      annotated with {@link EntityIdProperty}, should have a private 
 *      setter method (for Hibernate), and must be an argument in the non-default 
 *      constructor.  Note that a business key property will not need getHbn*() 
 *      or setHbn*() methods, since relationship management logic is not needed, 
 *      and Hibernate can always call the "normal" set and get methods for the 
 *      property.
 *   </li>
 *   <li>
 *      If a property defines a {Many,One}-to-One relationship, and is non-null
 *      (i.e., it has a foreign key constraint), then the following must be 
 *      observed for entity, E, and related entity, R:
 *      <ul>
 *        <li>
 *          E must specify R as argument in its non-default constructors.  
 *          The constructor(s) must update R's relationship with E, by calling 
 *          the appropriate methods on R: for a Many-To-One relationship (E-to-R), 
 *          call R.setHbnE(); for a One-to-One relationship, call R.getHbnEs().add().
 *        </li>
 *        <li>
 *          E cannot have a setter method for its relationship property with R, 
 *          since the relationship cannot be changed after instantation (without
 *          deleting E, that is).
 *        </li>
 *        <li>
 *          If E has a Many-to-One relationship with R, R cannot have an addE() or 
 *          removeE() method, but must have an E-accesible getHbnEs().
 *        </li>
 *        <li>
 *          If E has a One-to-One relationship with R, R should have an E-accessible 
 *          setE() method (and no setHbnE() method).
 *        </li>
 *        <li>
 *          E must have a private setR() method (for Hibernate).
 *        </li>
 *      </ul>
 *   </li>
 *   <li>
 *     Note that the combination of the previous five bullet items will dictate
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
    // can't do this now that we're using CGLIB2 proxies for (some of) our Hibernate entities...two proxied instances of the same entity can have different classes, since the proxy classes are generated dynamically! 
//    if (! getClass().equals(object.getClass())) {
//      return false;
//    }
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
  
  /**
   * To enable visitor to visit a particular subclass, override this method and
   * insert <code>visitor.acceptVisitor(this);</code>
   * 
   * @param visitor
   * @return
   * @motivation to keep most of our AbstractEntity subclasses clean, as we
   *             currently only have the DataAccessPolicy visitor, which does
   *             not actually need to visit every subclass.
   */
  abstract public Object acceptVisitor(AbstractEntityVisitor visitor);
  
  private DataAccessPolicy _dataAccessPolicy;

  /**
   * Get whether this entity is restricted, based upon the data access policy
   * that was provided (if any). It is up to the controller and/or UI layers to
   * check for and determine how to handle restricted entities. In general, a
   * restricted entity is one whose data cannot be displayed to the current
   * user. However, the semantics of "restricted" is really defined by the data
   * access policy that was set.
   * @see DataAccessPolicyInjectorPostLoadEventListener 
   */
  @DerivedEntityProperty
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
   *  
   */
  @DerivedEntityProperty
  public DataAccessPolicy getDataAccessPolicy()
  {
    return _dataAccessPolicy;
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
    if (method.getAnnotation(DerivedEntityProperty.class) != null) {
      return false;
    }

    return 
      !(Collection.class.isAssignableFrom(property.getPropertyType()) ||
        Map.class.isAssignableFrom(property.getPropertyType()) ||
        AbstractEntity.class.isAssignableFrom(property.getPropertyType()));
  }
}
