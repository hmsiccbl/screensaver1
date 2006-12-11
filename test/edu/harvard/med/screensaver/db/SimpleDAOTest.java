// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultParserTest;
import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.ScreenType;


/**
 * Tests the {@link DAOImpl} in some simple, straightfoward ways.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class SimpleDAOTest extends AbstractSpringTest
{
  
  // public static methods
  
  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(SimpleDAOTest.class);
  }

  
  // protected instance fields
  
  /**
   * Bean property, for database access via Spring and Hibernate.
   */
  protected DAO dao;

  /**
   * For schema-related test setup tasks.
   */
  protected SchemaUtil schemaUtil;

  
  // protected instance methods

  @Override
  protected void onSetUp() throws Exception
  {
    schemaUtil.truncateTablesOrCreateSchema();
  }

  
  // public instance methods
  
  public void testCreateEntity()
  {
    Compound compound = dao.defineEntity(Compound.class, "smiles");
    assertEquals("smiles", compound.getSmiles(), "smiles");
    
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
    Compound compound = new Compound("smiles");
    compound.setSalt(true);
    dao.persistEntity(compound);
    List<Compound> compounds = dao.findAllEntitiesWithType(Compound.class);
    assertEquals("one compound in the machine", compounds.size(), 1);
    assertEquals("names match", compounds.get(0).getSmiles(), "smiles");
    assertEquals("salty match", compounds.get(0).isSalt(), true);
  }
  
  public void testFindAllEntitiesWithType()
  {
    List<Compound> compounds = dao.findAllEntitiesWithType(Compound.class);
    assertEquals("no compounds in an empty database", 0, compounds.size());
    
    dao.defineEntity(Compound.class, "smiles");
    compounds = dao.findAllEntitiesWithType(Compound.class);
    assertEquals("one compound in the machine", compounds.size(), 1);
    assertEquals("smiles match", "smiles", compounds.get(0).getSmiles());
  }
  
  public void testFindEntityById()
  {
    Compound compound = dao.defineEntity(Compound.class, "smilesZ");
    Serializable id = compound.getCompoundId();

    Compound compound2 = dao.findEntityById(Compound.class, id);

    assertEquals(compound, compound2);
    compound2 = dao.findEntityById(Compound.class, id + "'");
    assertEquals(null, compound2);
  }
  
  public void testFindEntitiesByProperties()
  {
    final ResultValueType[] rvts = new ResultValueType[4];
    dao.doInTransaction(new DAOTransaction()
                        {
      public void runTransaction()
      {
        ScreenResult screenResult = ScreenResultParserTest.makeScreenResult(new Date());
        rvts[0] = dao.defineEntity(ResultValueType.class, screenResult, "rvt0");
        rvts[0].setDerived(true);
        rvts[0].setAssayPhenotype("Mouse");
        rvts[1] = dao.defineEntity(ResultValueType.class, screenResult, "rvt1");
        rvts[1].setDerived(false);
        rvts[1].setAssayPhenotype("Mouse");
        rvts[2] = dao.defineEntity(ResultValueType.class, screenResult, "rvt2");
        rvts[2].setDerived(true);
        rvts[2].setAssayPhenotype("Mouse");
        rvts[3] = dao.defineEntity(ResultValueType.class, screenResult, "rvt3");
        rvts[3].setDerived(true);
        rvts[3].setAssayPhenotype("Human");
      }
                        });
    
    dao.doInTransaction(new DAOTransaction()
                        {
      public void runTransaction()
      {
        Map<String,Object> queryProperties = new HashMap<String,Object>();
        queryProperties.put("derived", true);
        queryProperties.put("assayPhenotype", "Mouse");
        List<ResultValueType> entities = dao.findEntitiesByProperties(ResultValueType.class,
                                                                      queryProperties);
        assertTrue(entities.contains(rvts[0]));
        assertTrue(entities.contains(rvts[2]));
        assertFalse(entities.contains(rvts[1]));
        assertFalse(entities.contains(rvts[3]));
      }
                        });
  }
  
  public void testFindEntityByProperties()
  {
    Map<String,Object> name2Value = new HashMap<String,Object>();
    name2Value.put("plateNumber", new Integer(1));
    name2Value.put("wellName", "A01");

    Library library = dao.defineEntity(Library.class,
                                       "ln1",
                                       "sn1",
                                       ScreenType.SMALL_MOLECULE,
                                       LibraryType.NATURAL_PRODUCTS,
                                       1,
                                       50);
    Well expectedWell = dao.defineEntity(Well.class,
                                         library,
                                         name2Value.get("plateNumber"),
                                         name2Value.get("wellName"));
    Well actualWell = dao.findEntityByProperties(Well.class, name2Value);
    assertTrue(actualWell.isEquivalent(expectedWell));
  }

  public void testFindEntitiesByProperty1()
  {
    Compound compound = dao.defineEntity(Compound.class, "spaz");
    
    List<Compound> compounds = dao.findEntitiesByProperty(Compound.class, "smiles", "spaz");
    assertEquals(1, compounds.size());
    assertEquals(compound, compounds.get(0));

    compounds = dao.findEntitiesByProperty(Compound.class, "smiles", "something other than spaz");
    assertEquals(0, compounds.size());
  }
  
  public void testFindEntitiesByProperty2()
  {
    dao.defineEntity(Library.class, "ln1", "sn1", ScreenType.SMALL_MOLECULE, LibraryType.NATURAL_PRODUCTS, 1, 50);
    dao.defineEntity(Library.class, "ln2", "sn2", ScreenType.SMALL_MOLECULE, LibraryType.NATURAL_PRODUCTS, 51, 100);
    dao.defineEntity(Library.class, "ln3", "sn3", ScreenType.SMALL_MOLECULE, LibraryType.DISCRETE, 101, 150);
    dao.defineEntity(Library.class, "ln4", "sn4", ScreenType.SMALL_MOLECULE, LibraryType.NATURAL_PRODUCTS, 151, 200);
    dao.defineEntity(Library.class, "ln5", "sn5", ScreenType.SMALL_MOLECULE, LibraryType.DISCRETE, 201, 250);
    
    assertEquals(3, dao.findEntitiesByProperty(Library.class, "libraryType", LibraryType.NATURAL_PRODUCTS).size());
    assertEquals(2, dao.findEntitiesByProperty(Library.class, "libraryType", LibraryType.DISCRETE).size());
    assertEquals(0, dao.findEntitiesByProperty(Library.class, "libraryType", LibraryType.COMMERCIAL).size());
  }

  public void testFindEntitybyProperty()
  {
    Compound compound = dao.defineEntity(Compound.class, "spaz");
    
    Compound compound2 = dao.findEntityByProperty(Compound.class, "smiles", "spaz");
    assertEquals(compound, compound2);

    compound2 = dao.findEntityByProperty(Compound.class, "smiles", "something other than spaz");
    assertNull(compound2);
  }
  
  public void testFindEntitiesByPropertyPattern()
  {
    dao.defineEntity(Library.class, "npln1", "sn1", ScreenType.SMALL_MOLECULE, LibraryType.NATURAL_PRODUCTS, 1, 50);
    dao.defineEntity(Library.class, "npln2", "sn2", ScreenType.SMALL_MOLECULE, LibraryType.NATURAL_PRODUCTS, 51, 100);
    dao.defineEntity(Library.class, "ln3", "sn3", ScreenType.SMALL_MOLECULE, LibraryType.DISCRETE, 101, 150);
    dao.defineEntity(Library.class, "npln4", "sn4", ScreenType.SMALL_MOLECULE, LibraryType.NATURAL_PRODUCTS, 151, 200);
    dao.defineEntity(Library.class, "ln5", "sn5", ScreenType.SMALL_MOLECULE, LibraryType.DISCRETE, 201, 250);
    
    assertEquals(3, dao.findEntitiesByPropertyPattern(Library.class, "libraryName", "npln*").size());
    assertEquals(2, dao.findEntitiesByPropertyPattern(Library.class, "libraryName", "ln*").size());
    assertEquals(5, dao.findEntitiesByPropertyPattern(Library.class, "libraryName", "*ln*").size());
    assertEquals(0, dao.findEntitiesByPropertyPattern(Library.class, "libraryName", "ZZZZZZZZZ*").size());
  }
}
