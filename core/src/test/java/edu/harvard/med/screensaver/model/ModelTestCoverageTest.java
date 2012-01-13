// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import java.lang.reflect.Modifier;

import javax.persistence.Embeddable;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.ManagedType;

import org.springframework.beans.factory.annotation.Autowired;

import edu.harvard.med.screensaver.test.AbstractSpringTest;

/**
 * Tests that all entity classes have corresponding unit test classes.
 */
public class ModelTestCoverageTest extends AbstractSpringTest
{
  @Autowired
  protected EntityManagerFactory entityManagerFactory;
  
  public void testModelTestCoverage()
  {
    assertNotNull(entityManagerFactory);
    for (ManagedType<?> managedType : entityManagerFactory.getMetamodel().getManagedTypes()) {
      Class<?> entityClass = managedType.getJavaType();
      String entityClassName = entityClass.getSimpleName();
      if (Modifier.isAbstract(entityClass.getModifiers())) {
        continue;
      }
      if (entityClass.getAnnotation(Embeddable.class) != null) {
        continue;
      }
      try {
        Class.forName(entityClass.getName() + "Test");
      }
      catch (ClassNotFoundException e) {
        fail("missing test class for " + entityClassName);
      }
    }
  }
}
