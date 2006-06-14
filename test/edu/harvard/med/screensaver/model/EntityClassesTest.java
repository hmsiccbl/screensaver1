// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Test the entity classes.
 */
public class EntityClassesTest extends EntityClassesExercisor
{
  public static void main(String [] args)
  {
    junit.textui.TestRunner.run(EntityClassesTest.class);
  }
  
  public void testEntityClassesHavePublicNoArgConstructor()
  {
    exercizeEntityClasses(new EntityClassExercizor()
      {
        public void exercizeEntityClass(Class<AbstractEntity> entityClass)
        {
          newInstance(entityClass);          
        }
      });
  }

  /**
   * Test version accessors modifiers, arguments, and return types.
   * This test might be a little excessive, but I had to put <i>something</i>
   * here!  ;-)
   */
  public void testVersionAccessors()
  {
    exercizeEntityClasses(new EntityClassExercizor()
      {
        public void exercizeEntityClass(Class<AbstractEntity> entityClass)
        {
          // getVersion
          try {
            Method getVersionMethod = entityClass.getDeclaredMethod("getVersion");
            assertTrue("private getVersion for " + entityClass, Modifier.isPrivate(getVersionMethod.getModifiers()));
            assertFalse("instance getVersion for " + entityClass, Modifier.isStatic(getVersionMethod.getModifiers()));
            assertEquals("getVersion return type for " + entityClass, getVersionMethod.getReturnType(), Integer.class);
          }
          catch (SecurityException e) {
            e.printStackTrace();
            fail("getting declared method getVersion for " + entityClass + ": " + e);
          }
          catch (NoSuchMethodException e) {
            fail("getting declared method getVersion for " + entityClass + ": " + e);
          }
          
          // setVersion
          try {
            Method setVersionMethod = entityClass.getDeclaredMethod("setVersion", Integer.class);
            assertTrue("private setVersion for " + entityClass, Modifier.isPrivate(setVersionMethod.getModifiers()));
            assertFalse("instance setVersion for " + entityClass, Modifier.isStatic(setVersionMethod.getModifiers()));
            assertEquals("setVersion return type for " + entityClass, setVersionMethod.getReturnType(), void.class);
          }
          catch (SecurityException e) {
            e.printStackTrace();
            fail("getting declared method getVersion for " + entityClass + ": " + e);
          }
          catch (NoSuchMethodException e) {
            fail("getting declared method getVersion for " + entityClass + ": " + e);
          }
        }
      });
  }
}
