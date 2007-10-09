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

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultParserTest;
import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellType;
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
  
  private static final Logger log = Logger.getLogger(SimpleDAOTest.class);
  
  // public static methods
  
  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(SimpleDAOTest.class);
  }

  
  // protected instance fields
  
  /**
   * Bean property, for database access via Spring and Hibernate.
   */
  protected GenericEntityDAO genericEntityDao;

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
  
  public void testPersistEntity()
  {
    Compound compound = new Compound("smiles", "inchi", true);
    genericEntityDao.saveOrUpdateEntity(compound);
    List<Compound> compounds = genericEntityDao.findAllEntitiesOfType(Compound.class);
    assertEquals("one compound in the machine", compounds.size(), 1);
    assertEquals("names match", compounds.get(0).getSmiles(), "smiles");
    assertEquals("salty match", compounds.get(0).isSalt(), true);
  }
  
  public void testFindAllEntitiesWithType()
  {
    List<Compound> compounds = genericEntityDao.findAllEntitiesOfType(Compound.class);
    assertEquals("no compounds in an empty database", 0, compounds.size());
    
    genericEntityDao.saveOrUpdateEntity(new Compound("smiles", "inchi"));
    compounds = genericEntityDao.findAllEntitiesOfType(Compound.class);
    assertEquals("one compound in the machine", compounds.size(), 1);
    assertEquals("smiles match", "smiles", compounds.get(0).getSmiles());
  }
  
  public void testFindEntityById()
  {
    Compound compound = new Compound("smilesZ", "inchiZ");
    genericEntityDao.saveOrUpdateEntity(compound);
    Serializable id = compound.getCompoundId();

    Compound compound2 = genericEntityDao.findEntityById(Compound.class, id);

    assertEquals(compound, compound2);
    compound2 = genericEntityDao.findEntityById(Compound.class, id + "'");
    assertEquals(null, compound2);
  }
  
  public void testFindEntitiesByProperties()
  {
    final ResultValueType[] rvts = new ResultValueType[4];
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        ScreenResult screenResult = ScreenResultParserTest.makeScreenResult(new Date());
        rvts[0] = screenResult.createResultValueType("rvt0");
        rvts[0].setDerived(true);
        rvts[0].setAssayPhenotype("Mouse");
        rvts[1] = screenResult.createResultValueType("rvt1");
        rvts[1].setDerived(false);
        rvts[1].setAssayPhenotype("Mouse");
        rvts[2] = screenResult.createResultValueType("rvt2");
        rvts[2].setDerived(true);
        rvts[2].setAssayPhenotype("Mouse");
        rvts[3] = screenResult.createResultValueType("rvt3");
        rvts[3].setDerived(true);
        rvts[3].setAssayPhenotype("Human");
        genericEntityDao.saveOrUpdateEntity(screenResult.getScreen());
        genericEntityDao.saveOrUpdateEntity(rvts[0]);
      }
    });
    
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Map<String,Object> queryProperties = new HashMap<String,Object>();
        queryProperties.put("derived", true);
        queryProperties.put("assayPhenotype", "Mouse");
        List<ResultValueType> entities = genericEntityDao.findEntitiesByProperties(
          ResultValueType.class,
          queryProperties);
        assertEquals(2, entities.size());
        for (ResultValueType resultValueType : entities) {
          assertTrue(
            resultValueType.getName().equals("rvt0") ||
            resultValueType.getName().equals("rvt2"));
          assertEquals(true, resultValueType.isDerived());
          assertEquals("Mouse", resultValueType.getAssayPhenotype());
        }
      }
    });
  }
  
  public void testFindEntityByProperties()
  {
    final Library[] expectedLibrary = new Library[1];
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        expectedLibrary[0] = new Library(
          "ln1",
          "sn1",
          ScreenType.SMALL_MOLECULE,
          LibraryType.NATURAL_PRODUCTS,
          1,
          50);
        genericEntityDao.saveOrUpdateEntity(expectedLibrary[0]);
      }
    });
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Map<String,Object> props = new HashMap<String,Object>();
        props.put("startPlate", 1);
        props.put("endPlate", 50);
        Library actualLibrary = genericEntityDao.findEntityByProperties(Library.class, props);
        assertTrue(expectedLibrary[0].isEquivalent(actualLibrary));
      }
    });
  }

  public void testFindEntitiesByProperty1()
  {
    Compound compound = new Compound("spaz", "inchi");
    genericEntityDao.saveOrUpdateEntity(compound);
    
    List<Compound> compounds = genericEntityDao.findEntitiesByProperty(Compound.class, "smiles", "spaz");
    assertEquals(1, compounds.size());
    assertEquals(compound, compounds.get(0));

    compounds = genericEntityDao.findEntitiesByProperty(Compound.class, "smiles", "something other than spaz");
    assertEquals(0, compounds.size());
  }
  
  public void testFindEntitiesByProperty2()
  {
    genericEntityDao.defineEntity(Library.class, "ln1", "sn1", ScreenType.SMALL_MOLECULE, LibraryType.NATURAL_PRODUCTS, 1, 50);
    genericEntityDao.defineEntity(Library.class, "ln2", "sn2", ScreenType.SMALL_MOLECULE, LibraryType.NATURAL_PRODUCTS, 51, 100);
    genericEntityDao.defineEntity(Library.class, "ln3", "sn3", ScreenType.SMALL_MOLECULE, LibraryType.DISCRETE, 101, 150);
    genericEntityDao.defineEntity(Library.class, "ln4", "sn4", ScreenType.SMALL_MOLECULE, LibraryType.NATURAL_PRODUCTS, 151, 200);
    genericEntityDao.defineEntity(Library.class, "ln5", "sn5", ScreenType.SMALL_MOLECULE, LibraryType.DISCRETE, 201, 250);
    
    assertEquals(3, genericEntityDao.findEntitiesByProperty(Library.class, "libraryType", LibraryType.NATURAL_PRODUCTS).size());
    assertEquals(2, genericEntityDao.findEntitiesByProperty(Library.class, "libraryType", LibraryType.DISCRETE).size());
    assertEquals(0, genericEntityDao.findEntitiesByProperty(Library.class, "libraryType", LibraryType.COMMERCIAL).size());
  }

  public void testFindEntitybyProperty()
  {
    Compound compound = new Compound("spaz", "inchi");
    genericEntityDao.saveOrUpdateEntity(compound);
    
    Compound compound2 = genericEntityDao.findEntityByProperty(Compound.class, "smiles", "spaz");
    assertEquals(compound, compound2);

    compound2 = genericEntityDao.findEntityByProperty(Compound.class, "smiles", "something other than spaz");
    assertNull(compound2);
  }
  
//  public void testFindEntitiesByPropertyPattern()
//  {
//    genericEntityDao.defineEntity(Library.class, "npln1", "sn1", ScreenType.SMALL_MOLECULE, LibraryType.NATURAL_PRODUCTS, 1, 50);
//    genericEntityDao.defineEntity(Library.class, "npln2", "sn2", ScreenType.SMALL_MOLECULE, LibraryType.NATURAL_PRODUCTS, 51, 100);
//    genericEntityDao.defineEntity(Library.class, "ln3", "sn3", ScreenType.SMALL_MOLECULE, LibraryType.DISCRETE, 101, 150);
//    genericEntityDao.defineEntity(Library.class, "npln4", "sn4", ScreenType.SMALL_MOLECULE, LibraryType.NATURAL_PRODUCTS, 151, 200);
//    genericEntityDao.defineEntity(Library.class, "ln5", "sn5", ScreenType.SMALL_MOLECULE, LibraryType.DISCRETE, 201, 250);
//    
//    assertEquals(3, genericEntityDao.findEntitiesByPropertyPattern(Library.class, "libraryName", "npln*").size());
//    assertEquals(2, genericEntityDao.findEntitiesByPropertyPattern(Library.class, "libraryName", "ln*").size());
//    assertEquals(5, genericEntityDao.findEntitiesByPropertyPattern(Library.class, "libraryName", "*ln*").size());
//    assertEquals(0, genericEntityDao.findEntitiesByPropertyPattern(Library.class, "libraryName", "ZZZZZZZZZ*").size());
//  }
  
  public void testFindEntitiesByPropertyWithInflation()
  {
    final Library[] expectedLibrary = new Library[1];

    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        expectedLibrary[0] = new Library(
          "ln1",
          "sn1",
          ScreenType.SMALL_MOLECULE,
          LibraryType.NATURAL_PRODUCTS,
          1,
          50);
        Well well1 = expectedLibrary[0].createWell(new WellKey(1, "A01"), WellType.EXPERIMENTAL);
        Gene gene1 = new Gene("ANT1", 1, "ENTREZ-ANT1", "Human");
        gene1.addGenbankAccessionNumber("GBAN1");
        SilencingReagent siReagent1 = gene1.createSilencingReagent(SilencingReagentType.SIRNA, "AAAA");
        well1.addSilencingReagent(siReagent1);
        Gene gene2 = new Gene("ANT2", 2, "ENTREZ-ANT2", "Human");
        gene2.addGenbankAccessionNumber("GBAN2");
        SilencingReagent siReagent2 = gene2.createSilencingReagent(SilencingReagentType.SIRNA, "CCCC");
        Well well2 = expectedLibrary[0].createWell(new WellKey(2, "A01"), WellType.EXPERIMENTAL);
        well2.addSilencingReagent(siReagent2);
        Gene gene3 = new Gene("ANT3", 3, "ENTREZ-ANT3", "Human");
        gene3.addGenbankAccessionNumber("GBAN3");
        SilencingReagent siReagent3 = gene3.createSilencingReagent(SilencingReagentType.SIRNA, "TTTT");
        Well well3 = expectedLibrary[0].createWell(new WellKey(3, "A01"), WellType.EXPERIMENTAL);
        well3.addSilencingReagent(siReagent3);
        expectedLibrary[0].createCopy(CopyUsageType.FOR_LIBRARY_SCREENING, "copy1"); 
        expectedLibrary[0].createCopy(CopyUsageType.FOR_LIBRARY_SCREENING, "copy2"); 
        expectedLibrary[0].createCopy(CopyUsageType.FOR_LIBRARY_SCREENING, "copy3"); 
        genericEntityDao.saveOrUpdateEntity(expectedLibrary[0]);
      }
    });

    final Library[] actualLibrary = new Library[1];
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        actualLibrary[0] = genericEntityDao.findEntityByProperty(Library.class, 
                                                                 "startPlate", 
                                                                 1, 
                                                                 false, 
                                                                 //"wells", // implicit
                                                                 //"hbnWells.silencingReagents", // implicit
                                                                 "wells.silencingReagents.gene", // implicit
                                                                 "wells.silencingReagents.gene.genbankAccessionNumbers");
        assertTrue(expectedLibrary[0].isEquivalent(actualLibrary[0]));
      }
    });
    try {
      assertEquals("inflated wells", 3, actualLibrary[0].getWells().size());
      int i = 1;
      for (Well well : actualLibrary[0].getWells()) {
         assertEquals("inflated well", "A01", well.getWellName());
         assertEquals("inflated silencing reagent", 
                      new String[] { "AAAA", "CCCC", "TTTT" }[i - 1], 
                      well.getSilencingReagents().iterator().next().getSequence());
         assertEquals("inflated gene", 
                      "ANT" + i, 
                      well.getSilencingReagents().iterator().next().getGene().getGeneName());
         assertEquals("inflated genbankAccessionNumbers", 
                      "GBAN" + i, 
                      well.getSilencingReagents().iterator().next().getGene().getGenbankAccessionNumbers().iterator().next());
         ++i;
      }
    }
    catch (Exception e) {
      fail("inflation failed");
    }
    try {
      actualLibrary[0].getCopies().iterator();
      fail("copies inflated unexpectedly");
    }
    catch (Exception e) {
      // pass
    }
  }
}
