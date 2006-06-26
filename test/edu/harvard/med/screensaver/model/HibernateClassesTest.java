// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import org.hibernate.metadata.ClassMetadata;

/**
 * Test the Hibernate ClassMetadatas for the entities.
 */
public class HibernateClassesTest extends HibernateClassesExercisor
{
  public void testIsVersioned()
  {
    exercizeClassMetadatas(new ClassMetadataExercizor()
      {
        public void exercizeClassMetadata(ClassMetadata classMetadata)
        {
          String entityName = classMetadata.getEntityName();
          assertTrue(
            "hibernate class is versioned: " + entityName,
            classMetadata.isVersioned());
          int versionIndex = classMetadata.getVersionProperty();
          String versionName = classMetadata.getPropertyNames()[versionIndex];
          assertTrue(
            "name of version property is version: " + entityName,
            versionName.equals("version"));
        }
      });
  }
  
  // TODO: test getId() is public and setId() is private
  // TODO: test hbn properties have non-hbn equivalent public getters
}
