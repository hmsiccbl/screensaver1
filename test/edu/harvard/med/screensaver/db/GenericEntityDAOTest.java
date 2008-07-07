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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultParserTest;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
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
import edu.harvard.med.screensaver.model.screens.LabActivity;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

import org.apache.log4j.Logger;
import org.hibernate.LazyInitializationException;
import org.joda.time.LocalDate;


public class GenericEntityDAOTest extends AbstractSpringPersistenceTest
{

  private static final Logger log = Logger.getLogger(GenericEntityDAOTest.class);


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
        ScreenResult screenResult = ScreenResultParserTest.makeScreenResult();
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
        genericEntityDao.saveOrUpdateEntity(screenResult.getScreen().getLeadScreener());
        genericEntityDao.saveOrUpdateEntity(screenResult.getScreen().getLabHead());
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
    genericEntityDao.persistEntity(new Library("ln1", "sn1", ScreenType.SMALL_MOLECULE, LibraryType.NATURAL_PRODUCTS, 1, 50));
    genericEntityDao.persistEntity(new Library("ln2", "sn2", ScreenType.SMALL_MOLECULE, LibraryType.NATURAL_PRODUCTS, 51, 100));
    genericEntityDao.persistEntity(new Library("ln3", "sn3", ScreenType.SMALL_MOLECULE, LibraryType.DISCRETE, 101, 150));
    genericEntityDao.persistEntity(new Library("ln4", "sn4", ScreenType.SMALL_MOLECULE, LibraryType.NATURAL_PRODUCTS, 151, 200));
    genericEntityDao.persistEntity(new Library("ln5", "sn5", ScreenType.SMALL_MOLECULE, LibraryType.DISCRETE, 201, 250));

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

  public void testEntityInflation()
  {
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Screen screen = MakeDummyEntities.makeDummyScreen(1);
        ScreeningRoomUser labMember = new ScreeningRoomUser("Lab",
                                                            "Member",
                                                            "lab_member@hms.harvard.edu");
        labMember.setLab(screen.getLabHead().getLab());
        screen.addKeyword("keyword1");
        screen.addKeyword("keyword2");
        genericEntityDao.saveOrUpdateEntity(labMember);
        genericEntityDao.saveOrUpdateEntity(screen.getLeadScreener());
        genericEntityDao.saveOrUpdateEntity(screen.getLabHead());
        genericEntityDao.saveOrUpdateEntity(screen);
      }
    });

    final Screen[] screenOut = new Screen[1];
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Screen screen = genericEntityDao.findEntityByProperty(Screen.class, "screenNumber", 1);
        genericEntityDao.need(screen,
                              "keywords",
                              "labHead.labMembers");
        screenOut[0] = screen;
      }
    });

    // note: the Hibernate session/txn *must* be closed before we can make our assertions
    Screen screen = screenOut[0];
    try {
      assertEquals("keywords size", 2, screen.getKeywords().size());
      assertEquals("labHead last name", "Head_1", screen.getLabHead().getLastName());
      assertEquals("labHead.labMembers size", 1, screen.getLabHead().getLab().getLabMembers().size());
      assertEquals("labHead.labMembers[0].lastName", "Member", screen.getLabHead().getLab().getLabMembers().iterator().next().getLastName());
    }
    catch (LazyInitializationException e) {
      e.printStackTrace();
      fail("screen relationships were not initialized by genericEntityDao.need(AbstractEntity, String...)");
    }
    try {
      screen.getCollaborators().iterator().next();
      fail("expected LazyInitializationException for screen.collaborators access");
    }
    catch (LazyInitializationException e) {}
  }

  public void testEntityInflationInvalidRelationship()
  {
    Library library = new Library("library 1",
                                  "lib1",
                                  ScreenType.SMALL_MOLECULE,
                                  LibraryType.COMMERCIAL,
                                  1,
                                  1);
    library.createWell(new WellKey(1, "A01"), WellType.EXPERIMENTAL);
    genericEntityDao.saveOrUpdateEntity(library);
    try {
        // oops...should've been "wells"!
      genericEntityDao.need(library, "hbnWells");
      fail("invalid relationship name was not detected!");
    }
    catch (Exception e) {}
  }

  /**
   * Tests that reloadEntity() will eager fetch any specified relationships,
   * even if the entity is already managed by the current Hibernate session in
   * which reloadEntity() is called.
   */
  public void testReloadOfManagedEntityEagerFetchesRequestedRelationships()
  {
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Screen screen = MakeDummyEntities.makeDummyScreen(1);
        screen.createLibraryScreening(screen.getLeadScreener(), new LocalDate());
        genericEntityDao.persistEntity(screen.getLabHead());
        genericEntityDao.persistEntity(screen.getLeadScreener());
        genericEntityDao.persistEntity(screen);
      }
    });

    class Txn implements DAOTransaction
    {
      Screen screen;
      LabActivity activity;
      public void runTransaction()
      {
        //screen = genericEntityDao.findEntityByProperty(Screen.class, "screenNumber", 1);
        //screen = genericEntityDao.reloadEntity(screen, true, "leadScreener");
        activity = genericEntityDao.findEntityByProperty(LabActivity.class, "dateOfActivity", new LocalDate());
        activity = genericEntityDao.reloadEntity(activity, true, "performedBy");
      }
    };
    Txn txn = new Txn();
    genericEntityDao.doInTransaction(txn);
    //txn.screen.getLeadScreener().getFullNameLastFirst(); // no LazyInitExc
    txn.activity.getPerformedBy().getFullNameLastFirst(); // no LazyInitExc
    try {
      //txn.screen.getLabHead().getFullNameLastFirst();
      txn.activity.getScreen().getTitle();
      fail("expected LazyInitializationException on screen.getLabHead()");
    } catch (LazyInitializationException e) {}
  }

  public void testRelationshipSize()
  {
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Screen screen = MakeDummyEntities.makeDummyScreen(1);
        screen.createPublication();
        screen.createPublication();
        ScreeningRoomUser collab1 = new ScreeningRoomUser("Col",
                                                          "Laborator1",
                                                          "collab1@hms.harvard.edu");
        ScreeningRoomUser collab2 = new ScreeningRoomUser("Col",
                                                          "Laborator2",
                                                          "collab2@hms.harvard.edu");
        genericEntityDao.saveOrUpdateEntity(collab1);
        genericEntityDao.saveOrUpdateEntity(collab2);
        screen.addCollaborator(collab1);
        screen.addCollaborator(collab2);
        genericEntityDao.saveOrUpdateEntity(screen.getLeadScreener());
        genericEntityDao.saveOrUpdateEntity(screen.getLabHead());
        genericEntityDao.saveOrUpdateEntity(screen);
      }
    });

    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Screen screen = genericEntityDao.findEntityByProperty(Screen.class, "screenNumber", 1);
        assertEquals("publications size", 2, genericEntityDao.relationshipSize(screen.getPublications()));
        assertEquals("collaborators size", 2, genericEntityDao.relationshipSize(screen.getCollaborators()));
        // TODO: test that this relationshipSize() method is invocable outside of a Hibernate session
        assertEquals("publications size", 2, genericEntityDao.relationshipSize(screen, "publications"));
        assertEquals("collaborators size w/criteria",
                     1,
                     genericEntityDao.relationshipSize(screen, "collaborators", "lastName", "Laborator2"));
      }
    });
  }
}
