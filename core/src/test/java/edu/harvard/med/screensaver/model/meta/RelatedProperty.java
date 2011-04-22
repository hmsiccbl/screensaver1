// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.meta;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.annotations.ToMany;
import edu.harvard.med.screensaver.model.annotations.ToOne;
import edu.harvard.med.screensaver.model.entitytesters.ModelIntrospectionUtil;
import edu.harvard.med.screensaver.util.StringUtils;

public class RelatedProperty
{
  // static members

  private static Logger log = Logger.getLogger(RelatedProperty.class);


  // instance data members

  private Class<? extends AbstractEntity> _beanClass;
  private PropertyDescriptor _propertyDescriptor;


  private String _relatedPropertyName;
  private PropertyDescriptor _relatedPropertyDescriptor;
  private Class<? extends AbstractEntity> _relatedBeanClass;
  private BeanInfo _relatedBeanInfo;
  private boolean _relatedSideIsToMany;
  private boolean _relatedSideIsMappedToMany;

  
  // public constructors and methods
  
  public RelatedProperty(
    Class<? extends AbstractEntity> beanClass,
    PropertyDescriptor propertyDescriptor)
  {
    _beanClass = beanClass;
    _propertyDescriptor = propertyDescriptor;
    initialize();
  }
  
  private void initialize()
  {
    _relatedBeanClass = (Class<? extends AbstractEntity>) _propertyDescriptor.getReadMethod().getReturnType();
    // if this property is a collection property, then we must determine the
    // related property by looking at the parameterized type of the collection!
    if (Collection.class.isAssignableFrom(_relatedBeanClass)) {
      Type genericReturnType = _propertyDescriptor.getReadMethod().getGenericReturnType();
      if (genericReturnType instanceof ParameterizedType) {
        ParameterizedType parameterizedType = (ParameterizedType) genericReturnType;
        _relatedBeanClass = (Class) parameterizedType.getActualTypeArguments()[0];
        if (!AbstractEntity.class.isAssignableFrom(_relatedBeanClass)) {
          // collection property does not have to be for an entity relationship!
          _relatedBeanClass = null;
          return;
        }
      }
      else {
        _relatedBeanClass = null;
        return;
      }
    }
    try {
      _relatedBeanInfo = Introspector.getBeanInfo(_relatedBeanClass);
    }
    catch (IntrospectionException e) {
      return;
    }
    
    // TODO: if unidirectional from this side, no need to calculate related property info, below.

    // TODO: see which annotation checks below are really necessary

    javax.persistence.OneToMany jpaOneToMany =
      _propertyDescriptor.getReadMethod().getAnnotation(javax.persistence.OneToMany.class);
    ToOne toOne = _propertyDescriptor.getReadMethod().getAnnotation(ToOne.class);
    ToMany toMany = _propertyDescriptor.getReadMethod().getAnnotation(ToMany.class);
    if (jpaOneToMany != null && jpaOneToMany.mappedBy().length() > 0) {
      _relatedPropertyName = jpaOneToMany.mappedBy();
      _relatedPropertyDescriptor = findRelatedPropertyDescriptor(_relatedBeanInfo, _relatedPropertyName);
    }
    else if (toOne != null && toOne.inverseProperty().length() > 0) {
      _relatedPropertyName = toOne.inverseProperty();
      _relatedPropertyDescriptor = findRelatedPropertyDescriptor(_relatedBeanInfo, _relatedPropertyName);
    }
    else if (toMany != null && toMany.inverseProperty().length() > 0) {
      _relatedPropertyName = toMany.inverseProperty();
      _relatedPropertyDescriptor = findRelatedPropertyDescriptor(_relatedBeanInfo, _relatedPropertyName);
    }
    else {
      _relatedPropertyName = StringUtils.uncapitalize(_propertyDescriptor.getReadMethod().getDeclaringClass().getSimpleName());
      _relatedPropertyDescriptor = findRelatedPropertyDescriptor(_relatedBeanInfo, _relatedPropertyName);
      if (_relatedPropertyDescriptor == null) {
        _relatedPropertyName = _relatedPropertyName + "s";
        _relatedPropertyDescriptor = findRelatedPropertyDescriptor(_relatedBeanInfo, _relatedPropertyName);
      }
    }

    if (_relatedPropertyDescriptor != null) {
      if (Collection.class.isAssignableFrom(_relatedPropertyDescriptor.getReadMethod().getReturnType())) {
        _relatedSideIsToMany = true;
      }
      else if (Map.class.isAssignableFrom(_relatedPropertyDescriptor.getReadMethod().getReturnType())) {
        _relatedSideIsMappedToMany = true;
      }
    }
  }
  
  public boolean exists()
  {
    return _relatedPropertyDescriptor != null;
  }
  
  public PropertyDescriptor getPropertyDescriptor()
  {
    return _relatedPropertyDescriptor;
  }
    
  public boolean relatedPropertyIsImmutable()
  {
    if (! exists()) {
      return false;
    }
    return ModelIntrospectionUtil.isImmutableProperty(_relatedBeanClass, _relatedPropertyDescriptor);
  }

  public String getExpectedName()
  {
    return _relatedPropertyName;
  }

  public String getName()
  {
    return _relatedPropertyDescriptor == null ? "<none>" : _relatedPropertyDescriptor.getDisplayName();
  }
  
  public String getFullName()
  {
    return _relatedPropertyDescriptor == null ? "<none>" : _relatedBeanClass.getSimpleName() + "." + _relatedPropertyDescriptor.getDisplayName();
  }

  public boolean otherSideIsToMany()
  {
    return _relatedSideIsToMany;
  }

  public boolean otherSideIsMappedToMany()
  {
    return _relatedSideIsMappedToMany;
  }

  public Class getBeanClass()
  {
    return _relatedBeanClass;
  }
  
  public Object invokeGetter(AbstractEntity relatedBean) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
  {
    return _relatedPropertyDescriptor.getReadMethod().invoke(relatedBean);
  }

  // private methods

  private static PropertyDescriptor findRelatedPropertyDescriptor(BeanInfo beanInfo,
                                                                  String propertyName)
  {
    for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {
      if (descriptor.getName().equals(propertyName)) {
        return descriptor;
      }
    }
    return null;
  }

}

