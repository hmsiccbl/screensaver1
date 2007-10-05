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

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.annotations.CollectionOfElements;

/**
 * Tests that all collection properties start out with the correct size. This initial size is
 * assumed to be zero, unless the property has a {@link CollectionOfElements} annotation with
 * non-default {@link CollectionOfElements#initialCardinality()}.
 */
public class CollectionPropertiesInitialCardinalityTester<E extends AbstractEntity>
extends AbstractPropertiesTester<E>
{
  private static Logger log = Logger.getLogger(CollectionPropertiesInitialCardinalityTester.class);

  public CollectionPropertiesInitialCardinalityTester(
    Class<E> entityClass,
    SessionFactory sessionFactory,
    BeanInfo beanInfo,
    E bean)
  {
    super(entityClass, sessionFactory, beanInfo, bean);
  }

  @Override
  public AbstractPropertyTester createPropertyTester(
    E bean,
    BeanInfo beanInfo,
    PropertyDescriptor propertyDescriptor)
  {
    return new CollectionPropertyInitialCardinalityTester<E>(bean, beanInfo, propertyDescriptor);
  }

  private class CollectionPropertyInitialCardinalityTester<E2 extends AbstractEntity>
  extends AbstractPropertyTester<E2>
  {
    public CollectionPropertyInitialCardinalityTester(
      E2 bean,
      BeanInfo beanInfo,
      PropertyDescriptor propertyDescriptor)
    {
      super(bean, beanInfo, propertyDescriptor);
    }
    
    @Override
    public void testProperty()
    {
      Method getter = getReadMethod();
      if (! (getter.getReturnType().isAssignableFrom(Collection.class))) {
        return;
      }
      Object result = null;
      try {
        result = getter.invoke(getBean());
      }
      catch (Exception e) {
        e.printStackTrace();
        fail("getter for collection property threw exception: " + getReadMethodFullname() + ": " + e);
      }
      assertEquals(
        "getter for uninitialized property returns collection of expected initial size (usually 0): " +
        getReadMethodFullname(),
        getExpectedInitialCollectionSize(),
        ((Collection) result).size());
    }
  }
}
