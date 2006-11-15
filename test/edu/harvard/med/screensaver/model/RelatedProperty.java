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

import edu.harvard.med.screensaver.model.screens.NonCherryPickVisit;
import edu.harvard.med.screensaver.model.screens.Visit;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.log4j.Logger;

public class RelatedProperty
{
  // static members

  private static Logger log = Logger.getLogger(RelatedProperty.class);


  // instance data members

  private Class<? extends AbstractEntity> _beanClass;
  private PropertyDescriptor _propertyDescriptor;


  private String _expectedRelatedPropertyName;
  private String _expectedRelatedPluralPropertyName;

  private PropertyDescriptor _relatedPropertyDescriptor;
  private Class _relatedBeanClass;
  private BeanInfo _relatedBeanInfo;
  private boolean _relatedSideIsMany;

  
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
          // collection property does have to be for an entity relationship!
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
    
    String propertyName = _propertyDescriptor.getName();

    if (EntityBeansExercizor.oddPropertyToRelatedPropertyMap.containsKey(propertyName)) {
      _expectedRelatedPropertyName =
        EntityBeansExercizor.oddPropertyToRelatedPropertyMap.get(propertyName);
    }
    else {
      _expectedRelatedPropertyName = StringUtils.uncapitalize(_beanClass.getSimpleName());
    }
    
    if (EntityBeansExercizor.oddPropertyToRelatedPluralPropertyMap.containsKey(propertyName)) {
      _expectedRelatedPluralPropertyName =
        EntityBeansExercizor.oddPropertyToRelatedPluralPropertyMap.get(propertyName);
    }
    else {
      _expectedRelatedPluralPropertyName =
        EntityBeansExercizor.oddSingularToPluralPropertiesMap.containsKey(_expectedRelatedPropertyName) ?
          EntityBeansExercizor.oddSingularToPluralPropertiesMap.get(_expectedRelatedPropertyName) :
            _expectedRelatedPropertyName + "s";
    } 


    // HACK: cant put "screen" into the odd maps since it is ubiquitous
    if (Visit.class.isAssignableFrom(_beanClass) &&
      propertyName.equals("screen")) {
      _expectedRelatedPluralPropertyName = "visits";
    }

    // HACK: cant put "labHead" into the odd maps twice, buts it has
    // related screensHeaded + labMembers
    if (_beanClass.equals(ScreeningRoomUser.class) &&
      propertyName.equals("labHead")) {
      _expectedRelatedPluralPropertyName = "labMembers";
    }

    // get the prop descr for the other side, and determine whether the
    // other side is one or many
    for (PropertyDescriptor descriptor : _relatedBeanInfo.getPropertyDescriptors()) {
      if (descriptor.getName().equals(_expectedRelatedPropertyName)) {
        _relatedPropertyDescriptor = descriptor;
        break;
      }
      if (descriptor.getName().equals(_expectedRelatedPluralPropertyName)) {
        _relatedPropertyDescriptor = descriptor;
        _relatedSideIsMany = true;
        break;
      }
    }
    
    // HACK - difficulty because singular and plural property names
    // are the same
    if (_relatedBeanClass.equals(NonCherryPickVisit.class) &&
        (_expectedRelatedPropertyName.equals("platesUsed") ||
         _expectedRelatedPropertyName.equals("equipmentUsed"))) {
      _relatedSideIsMany = true;
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
    return _expectedRelatedPropertyName;
  }

  public String getExpectedPluralName()
  {
    return _expectedRelatedPluralPropertyName;
  }
  
  public String getName()
  {
    return _relatedPropertyDescriptor == null ? "<none>" : _relatedPropertyDescriptor.getDisplayName();
  }
  
  public String getFullName()
  {
    return _relatedPropertyDescriptor == null ? "<none>" : _relatedBeanClass.getSimpleName() + "." + _relatedPropertyDescriptor.getDisplayName();
  }

  public boolean otherSideIsMany()
  {
    return _relatedSideIsMany;
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

}

