// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.db.SchemaUtil;

/**
 * Test the Hibernate ClassMetadatas for the entities.
 */
public class ClassMetadatasTest extends ClassMetadatasExercisor
{
  /**
   * Bean property, for database access via Spring and Hibernate.
   */
  protected DAO dao;

  /**
   * For schema-related test setup tasks.
   */
  protected SchemaUtil schemaUtil;

  public void testSomething()
  {
    
  }
}
