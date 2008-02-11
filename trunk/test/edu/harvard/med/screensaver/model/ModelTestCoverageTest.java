// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/test/edu/harvard/med/screensaver/model/AbstractEntityTest.java $
// $Id: AbstractEntityTest.java 1655 2007-08-02 15:02:06Z s $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import java.lang.reflect.Modifier;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;

import edu.harvard.med.screensaver.AbstractSpringTest;

/**
 * Tests that all non-abstract subclasses of {@link AbstractEntity} have corresponding
 * unit tests.
 */
public class ModelTestCoverageTest extends AbstractSpringTest
{
  private static Logger log = Logger.getLogger(ModelTestCoverageTest.class);
  
  protected SessionFactory hibernateSessionFactory;
  
  public void testModelTestCoverage()
  {
    assertNotNull(hibernateSessionFactory);
    Iterator classMetadatas = hibernateSessionFactory.getAllClassMetadata().values().iterator();
    while (classMetadatas.hasNext()) {
      ClassMetadata classMetadata = (ClassMetadata) classMetadatas.next();
      log.info("meta = " + classMetadata.getEntityName());
      String entityClassName = classMetadata.getEntityName();
      Class entityClass = null;
      try {
        entityClass = Class.forName(entityClassName);
      }
      catch (ClassNotFoundException e) {
        fail("couldnt find entity class " + entityClassName);
      }
      if (Modifier.isAbstract(entityClass.getModifiers())) {
        log.info("skipping abstract class " + entityClass.getSimpleName());
        continue;
      }
      try {
        Class.forName(entityClassName + "Test");
      }
      catch (ClassNotFoundException e) {
        fail("couldnt find test class for " + entityClass.getSimpleName());
      }
    }
  }
}
