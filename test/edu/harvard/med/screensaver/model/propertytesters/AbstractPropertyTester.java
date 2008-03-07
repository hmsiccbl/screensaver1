// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.propertytesters;

import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import javax.persistence.Transient;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.annotations.CollectionOfElements;
import edu.harvard.med.screensaver.model.annotations.Column;
import edu.harvard.med.screensaver.model.annotations.ContainedEntity;
import edu.harvard.med.screensaver.util.StringUtils;

abstract public class AbstractPropertyTester<E extends AbstractEntity>
{
  private static Logger log = Logger.getLogger(AbstractPropertyTester.class);

  protected E _bean;
  protected BeanInfo _beanInfo;
  protected PropertyDescriptor _propertyDescriptor;

  public AbstractPropertyTester(
    E bean,
    BeanInfo beanInfo,
    PropertyDescriptor propertyDescriptor)
  {
    _bean = bean;
    _beanInfo = beanInfo;
    _propertyDescriptor = propertyDescriptor;
  }
  
  abstract public void testProperty();
  
  protected E getBean()
  {
    return _bean;
  }
  
  protected Class<? extends AbstractEntity> getBeanClass()
  {
    return _bean.getClass();
  }
  
  protected String getPropertyName()
  {
    return _propertyDescriptor.getName();
  }
  
  protected String getPropertyFullname()
  {
    return _bean.getClass() + "." + _propertyDescriptor.getDisplayName();
  }

  protected Class getPropertyType()
  {
    return _propertyDescriptor.getPropertyType();
  }
  
  protected Method getReadMethod()
  {
    return _propertyDescriptor.getReadMethod();
  }
  
  protected String getReadMethodFullname()
  {
    return getBeanClass() + "." + getReadMethod().getName();
  }
  
  protected Method getWriteMethod()
  {
    return _propertyDescriptor.getWriteMethod();
  }

  protected String getWriteMethodFullname()
  {
    return getBeanClass() + "." + getWriteMethod().getName();
  }

  protected boolean setterOrAdderMethodNotExpected()
  {
    if (
      isTransientProperty() ||
      isPropertyWithNonconventionalSetterMethod() ||
      isImmutableProperty() ||
      isContainedEntityProperty() ||
      isEntityIdProperty()) {
      log.debug("setter method not expected for property: " + getPropertyFullname());
      return true;
    }
    return false;
  }

  protected boolean isTransientProperty()
  {
    return getReadMethod().isAnnotationPresent(Transient.class);  
  }
  
  protected boolean isPropertyWithNonconventionalSetterMethod()
  {
    Column column = getReadMethod().getAnnotation(Column.class);
    return column != null && column.hasNonconventionalSetterMethod();
  }

  protected boolean isImmutableProperty()
  {
    org.hibernate.annotations.Entity entityAnnotation =
      getBeanClass().getAnnotation(org.hibernate.annotations.Entity.class);
    if (entityAnnotation != null && ! entityAnnotation.mutable()) {
      return true;
    }
    return getReadMethod().isAnnotationPresent(org.hibernate.annotations.Immutable.class);
  }

  protected boolean isContainedEntityProperty()
  {
    if (AbstractEntity.class.isAssignableFrom(getPropertyType())) {
      Class propertyType = (Class) getPropertyType();
      ContainedEntity containedEntity =
        propertyType.<ContainedEntity>getAnnotation(ContainedEntity.class);
      return
        containedEntity != null &&
        getBeanClass().isAssignableFrom(containedEntity.containingEntityClass());
    }
    return false;
  }

  /**
   * Return true iff the property corresponds to the entity's ID. These properties are
   * <code>entityId</code>, and <code>fooId</code> for a bean of type <code>Foo</code>.
   * @return true iff the property corresponds to the entity's ID
   */
  protected boolean isEntityIdProperty()
  {
    if (getPropertyName().equals("entityId")) {
      log.debug("isEntityIdProperty(): property participates in defining entity ID: " + getPropertyFullname());
      return true;
    }

    // Check whether property corresponds to the _bean's Hibernate ID method, which is named
    // by convention based on the bean class name. Also check the parent classes, to handle the
    // case where the property has been inherited, as the property name will depend upon the
    // class it was declared in.
    String capitalizedPropertyName = StringUtils.capitalize(getPropertyName());
    log.info("capped name is " + capitalizedPropertyName);
    for (Class beanClass = getBeanClass(); ! beanClass.equals(AbstractEntity.class); beanClass = beanClass.getSuperclass()) {
      log.info("bean class simple name is " + beanClass.getSimpleName());
      if (capitalizedPropertyName.equals(beanClass.getSimpleName() + "Id")) {
        log.debug("isEntityIdProperty(): property participates in defining entity ID: " + getPropertyFullname());
        return true;
      }
    }
    return false;
  }

  protected boolean isCollectionProperty()
  {
   return
     Collection.class.isAssignableFrom(getPropertyType()) ||
     Map.class.isAssignableFrom(getPropertyType()); 
  }

  protected int getExpectedInitialCollectionSize()
  {
    CollectionOfElements collectionOfElements = getReadMethod().getAnnotation(CollectionOfElements.class);
    if (collectionOfElements != null) {
      return collectionOfElements.initialCardinality();
    }
    return 0;
  }
}
