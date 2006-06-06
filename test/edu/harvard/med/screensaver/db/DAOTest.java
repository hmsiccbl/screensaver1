// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.util.List;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.HibernateTransactionManager;

import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.model.libraries.Compound;

/**
 * Tests the {@link DAOImpl}.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class DAOTest extends AbstractSpringTest
{
  
  // public static methods
  
  public static void main(String[] args) {
    junit.textui.TestRunner.run(DAOTest.class);
  }

  
  // protected instance fields
  
  /**
   * Bean property, for database access via Spring and Hibernate.
   */
  protected DAO dao;

  /**
   * Bean property, for executing programmatic transactions.
   */
  protected HibernateTransactionManager txnManager;
  
  /**
   * For schema-related test setup tasks.
   */
  protected SchemaUtil schemaUtil;
  
  /**
   * The hibernate template, and how about that? 
   */
  protected HibernateTemplate hibernateTemplate;

  
  // protected instance methods

  @Override
  protected void onSetUp() throws Exception
  {
    schemaUtil.recreateSchema();
  }

  
  // public instance methods
  
  public void testCreateEntity()
  {
    Compound compound = dao.defineEntity(Compound.class, "compoundName");
    assertEquals("compound name", compound.getCompoundName(), "compoundName");
    
    try {
      dao.defineEntity(Compound.class, "foo", "bar");
      fail("no error on create entity with bad args");
    }
    catch (IllegalArgumentException e) {
    }
    catch (Exception e) {
      fail("bad error on create entity with bad args");
    }
  }
  
  public void testPersistEntity()
  {
    Compound compound = new Compound();
    compound.setCompoundName("cname");
    compound.setSalt(true);
    dao.persistEntity(compound);
    List<Compound> compounds = dao.findAllEntitiesWithType(Compound.class);
    assertEquals("one compound in the machine", compounds.size(), 1);
    assertEquals("names match", compounds.get(0).getCompoundName(), "cname");
    assertEquals("salty match", compounds.get(0).isSalt(), true);
  }
  
  public void testFindAllEntitiesWithType()
  {
    List<Compound> compounds = dao.findAllEntitiesWithType(Compound.class);
    assertEquals("no compounds in an empty database", compounds.size(), 0);
    
    dao.defineEntity(Compound.class, "compoundName");
    compounds = dao.findAllEntitiesWithType(Compound.class);
    assertEquals("one compound in the machine", compounds.size(), 1);
    assertEquals("names match", compounds.get(0).getCompoundName(), "compoundName");
  }
}
