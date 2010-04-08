// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.entitytesters;

import junit.framework.Assert;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.SemanticIDAbstractEntity;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;

/**
 * An abstract class for testing various aspects of a model entity. Implementing classes
 * provide an implementation of abstract method {@link #testEntity} to perform the test.
 * They are provided access to the {@link #_entityClass entity class} and the {@link
 * #_sessionFactory session factory}. They are also provided with some helper methods:
 * {@link #isEntitySubclass()}, {@link #getGetterMethodForPropertyName(String)}, and
 * {@link #getSetterMethodForPropertyName(String, Class)}.
 * 
 * @param <E> the type of abstract entity to be tested
 */
abstract public class AbstractEntityTester<E extends AbstractEntity> extends Assert
{
  
  // private static datum

  private static Logger log = Logger.getLogger(AbstractEntityTester.class);

  
  // protected instance data

  protected Class<? extends AbstractEntity> _entityClass;
  protected SessionFactory _sessionFactory;

  
  // public constructor and instance method
  
  /**
   * Construct an <code>AbstractEntityTester</code>.
   * @param entityClass the entity class
   * @param sessionFactory the session factory
   */
  public AbstractEntityTester(Class<E> entityClass, SessionFactory sessionFactory)
  {
    _entityClass = entityClass;
    _sessionFactory = sessionFactory;
  }
  
  /**
   * Perform the test for the entity.
   */
  abstract public void testEntity();
}
