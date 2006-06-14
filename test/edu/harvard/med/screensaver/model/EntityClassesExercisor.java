// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import edu.harvard.med.screensaver.AbstractSpringTest;

/**
 * Exercise the entity classes.
 */
abstract class EntityClassesExercisor extends AbstractSpringTest
{
  private static final String [] entityPackages = {
    "edu.harvard.med.screensaver.model",
    "edu.harvard.med.screensaver.model.derivatives",
    "edu.harvard.med.screensaver.model.libraries",
    "edu.harvard.med.screensaver.model.screenresults",
    "edu.harvard.med.screensaver.model.screens",
    "edu.harvard.med.screensaver.model.users"
  };
  
  protected static interface EntityClassExercizor
  {
    void exercizeEntityClass(Class<AbstractEntity> entityClass);
  }
  
  protected void exercizeEntityClasses(EntityClassExercizor exercizor)
  {
    for (Class<AbstractEntity> entityClass : getEntityClasses()) {
      exercizor.exercizeEntityClass(entityClass);
    }
  }

  protected AbstractEntity newInstance(Class<AbstractEntity> entityClass) {
    try {
      return entityClass.newInstance();
    }
    catch (InstantiationException e) {
      e.printStackTrace();
      fail("newInstance for " + entityClass + " threw an InstantiationException: " + e.getMessage());
    }
    catch (IllegalAccessException e) {
      e.printStackTrace();
      fail("newInstance for " + entityClass + " threw an IllegalAccessException: " + e.getMessage());
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  protected List<Class<AbstractEntity>> getEntityClasses()
  {
    List<Class<AbstractEntity>> entityClasses = new ArrayList<Class<AbstractEntity>>();
    for (String entityPackage : entityPackages) {
      String packagePath = "/" + entityPackage.replace('.', '/');
      URL packageURL = getClass().getResource(packagePath);
      File directory = new File(packageURL.getFile());
      if (! directory.exists()) {
        throw new RuntimeException("directory " + directory + "doesn't exist");
      }
      for (String file : directory.list()) {
        if (! file.endsWith(".class")) {
          continue;
        }
        String classname = file.substring(0, file.length() - 6); // remove the .class extension
        Class entityClass;
        try {
          entityClass = Class.forName(entityPackage + "." + classname);
        }
        catch (ClassNotFoundException e) {
          continue;
        }
        if (AbstractEntity.class.equals(entityClass)) {
          continue;
        }
        if (AbstractEntity.class.isAssignableFrom(entityClass)) {
          entityClasses.add((Class<AbstractEntity>) entityClass);
        }
      }
    }
    return entityClasses;
  }
}
