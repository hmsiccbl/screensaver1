// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.log4j.Logger;

public class RelatedProperty
{
  // static members

  private static Logger log = Logger.getLogger(RelatedProperty.class);


  // instance data members

  private Class<? extends AbstractEntity> _beanClass;
  private PropertyDescriptor _propertyDescriptor;


  private String _relatedPropertyName;
  private PropertyDescriptor _relatedPropertyDescriptor;
  private Class _relatedBeanClass;
  private BeanInfo _relatedBeanInfo;
  private boolean _relatedSideIsToMany;

  
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
    _relatedBeanClass = _propertyDescriptor.getReadMethod().getReturnType();
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
    
    ToOneRelationship toOneRelationship = _propertyDescriptor.getReadMethod().getAnnotation(ToOneRelationship.class);
    ToManyRelationship toManyRelationship = _propertyDescriptor.getReadMethod().getAnnotation(ToManyRelationship.class);
    if (toOneRelationship != null && toOneRelationship.inverseProperty().length() > 0) {
      _relatedPropertyName = toOneRelationship.inverseProperty();
      _relatedPropertyDescriptor = findRelatedPropertyDescriptor(_relatedBeanInfo, _relatedPropertyName);
    }
    else if (toManyRelationship != null && toManyRelationship.inverseProperty().length() > 0) {
      _relatedPropertyName = toManyRelationship.inverseProperty();
      _relatedPropertyDescriptor = findRelatedPropertyDescriptor(_relatedBeanInfo, _relatedPropertyName);
    }
    else {
      _relatedPropertyName = StringUtils.uncapitalize(_beanClass.getSimpleName());
      _relatedPropertyDescriptor = findRelatedPropertyDescriptor(_relatedBeanInfo, _relatedPropertyName);
      if (_relatedPropertyDescriptor == null) {
        _relatedPropertyName = _relatedPropertyName + "s";
        _relatedPropertyDescriptor = findRelatedPropertyDescriptor(_relatedBeanInfo, _relatedPropertyName);
      }
    }

    if (_relatedPropertyDescriptor != null) {
      ToManyRelationship relatedToManyRelationship = _relatedPropertyDescriptor.getReadMethod().getAnnotation(ToManyRelationship.class);
      if (relatedToManyRelationship != null ||
        Collection.class.isAssignableFrom(_relatedPropertyDescriptor.getReadMethod().getReturnType())) {
        _relatedSideIsToMany = true;
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
    
  public boolean hasForeignKeyConstraint()
  {
    if (!exists()) {
      return false;
    }
    ToOneRelationship toOneRelationship = _relatedPropertyDescriptor.getReadMethod().getAnnotation(ToOneRelationship.class);
    return toOneRelationship != null && !toOneRelationship.nullable();
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

