// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;

/**
 * Exercize the Hibernate ClassMetadatas for the entities.
 */
abstract class ClassMetadatasExercisor extends EntityClassesExercisor
{
  
  /**
   * The Hibernate <code>SessionFactory</code>. Used for getting
   * <code>ClassMetadata</code> objects 
   */
  protected SessionFactory hibernateSessionFactory;

  protected static interface ClassMetadataExercizor
  {
    void exercizeClassMetadata(ClassMetadata classMetadata);
  }
  
  protected void exercizeClassMetadatas(ClassMetadataExercizor excersizor)
  {
    for (Class<AbstractEntity> entityClass : getEntityClasses()) {
      ClassMetadata classMetadata = getClassMetadataForEntityClass(entityClass);
      excersizor.exercizeClassMetadata(classMetadata);
    }
  }

  protected ClassMetadata getClassMetadataForEntityClass(Class<AbstractEntity> entityClass)
  {
    return hibernateSessionFactory.getClassMetadata(entityClass);
  }
}
