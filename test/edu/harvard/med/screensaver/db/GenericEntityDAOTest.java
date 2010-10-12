// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.hibernate.LazyInitializationException;
import org.hibernate.Session;
import org.joda.time.LocalDate;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultParserTest;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.Entity;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.LibraryWellType;
import edu.harvard.med.screensaver.model.libraries.PlateSize;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.DataColumn;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.LabActivity;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;


public class GenericEntityDAOTest extends AbstractSpringPersistenceTest
{

  private static final Logger log = Logger.getLogger(GenericEntityDAOTest.class);
  private AbstractEntity _anEntity;
  private AdministratorUser _adminUser;


  // public instance methods
  
  @Override
  protected void onSetUp() throws Exception
  {
    super.onSetUp();
    _adminUser = new AdministratorUser("Admin", "User", "", "", "", "", "", "");
    _anEntity = new Library(_adminUser, "Library Name", "libName", ScreenType.SMALL_MOLECULE, LibraryType.COMMERCIAL, 1, 1, PlateSize.WELLS_384);
  }

  @SuppressWarnings("unchecked")
  public void testPersistAndFindAllOfType()
  {
    genericEntityDao.saveOrUpdateEntity(_anEntity);
    List<AbstractEntity> result = (List<AbstractEntity>) genericEntityDao.findAllEntitiesOfType(_anEntity.getEntityClass());
    assertEquals(result.size(), 1);
    AbstractEntity e = result.get(0);
    e.isEquivalent(_anEntity);
  }

  public void testFindEntityById()
  {
    genericEntityDao.saveOrUpdateEntity(_anEntity);
    Serializable id = _anEntity.getEntityId();

    Entity e = genericEntityDao.findEntityById(_anEntity.getEntityClass(), id);
    assertEquals(_anEntity, e);

    Entity e2 = genericEntityDao.findEntityById(_anEntity.getEntityClass(), new Integer(-1));
    assertEquals(null, e2);
  }

  public void testFindEntitiesByProperties()
  {
    final DataColumn[] cols = new DataColumn[4];
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        ScreenResult screenResult = ScreenResultParserTest.makeScreenResult();
        cols[0] = screenResult.createDataColumn("col0");
        cols[0].makeDerived("", Sets.<DataColumn>newHashSet());
        cols[0].forPhenotype("Mouse");
        cols[1] = screenResult.createDataColumn("col1");
        cols[1].forPhenotype("Mouse");
        cols[2] = screenResult.createDataColumn("col2");
        cols[2].makeDerived("", Sets.<DataColumn>newHashSet());
        cols[2].forPhenotype("Mouse");
        cols[3] = screenResult.createDataColumn("col3");
        cols[3].makeDerived("", Sets.<DataColumn>newHashSet());
        cols[3].forPhenotype("Human");
        genericEntityDao.saveOrUpdateEntity(screenResult.getScreen().getLeadScreener());
        genericEntityDao.saveOrUpdateEntity(screenResult.getScreen().getLabHead());
        genericEntityDao.saveOrUpdateEntity(screenResult.getScreen());
        genericEntityDao.saveOrUpdateEntity(cols[0]);
      }
    });

    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Map<String,Object> queryProperties = new HashMap<String,Object>();
        queryProperties.put("derived", true);
        queryProperties.put("assayPhenotype", "Mouse");
        List<DataColumn> entities = genericEntityDao.findEntitiesByProperties(
          DataColumn.class,
          queryProperties);
        assertEquals(2, entities.size());
        for (DataColumn dataColumn : entities) {
          assertTrue(
            dataColumn.getName().equals("col0") ||
            dataColumn.getName().equals("col2"));
          assertEquals(true, dataColumn.isDerived());
          assertEquals("Mouse", dataColumn.getAssayPhenotype());
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
        expectedLibrary[0] = new Library(_adminUser,
                                         "ln1",
                                         "sn1",
                                         ScreenType.SMALL_MOLECULE,
                                         LibraryType.NATURAL_PRODUCTS,
                                         1,
                                         50,
                                         PlateSize.WELLS_384);
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

  @SuppressWarnings("unchecked")
  public void testFindEntitiesByProperty1()
  {
    genericEntityDao.saveOrUpdateEntity(_anEntity);
    List<AbstractEntity> result = (List<AbstractEntity>) genericEntityDao.findAllEntitiesOfType(_anEntity.getEntityClass());
    assertEquals(1, result.size());
    assertEquals(_anEntity, result.get(0));

    result = (List<AbstractEntity>) genericEntityDao.findEntitiesByProperty(_anEntity.getEntityClass(), "shortName", "something other than ID");
    assertEquals(0, result.size());
  }

  public void testFindEntitiesByProperty2()
  {
    genericEntityDao.persistEntity(new Library(_adminUser, "ln1", "sn1", ScreenType.SMALL_MOLECULE, LibraryType.NATURAL_PRODUCTS, 1, 50, PlateSize.WELLS_384));
    genericEntityDao.persistEntity(new Library(_adminUser, "ln2", "sn2", ScreenType.SMALL_MOLECULE, LibraryType.NATURAL_PRODUCTS, 51, 100, PlateSize.WELLS_384));
    genericEntityDao.persistEntity(new Library(_adminUser, "ln3", "sn3", ScreenType.SMALL_MOLECULE, LibraryType.DISCRETE, 101, 150, PlateSize.WELLS_384));
    genericEntityDao.persistEntity(new Library(_adminUser, "ln4", "sn4", ScreenType.SMALL_MOLECULE, LibraryType.NATURAL_PRODUCTS, 151, 200, PlateSize.WELLS_384));
    genericEntityDao.persistEntity(new Library(_adminUser, "ln5", "sn5", ScreenType.SMALL_MOLECULE, LibraryType.DISCRETE, 201, 250, PlateSize.WELLS_384));

    assertEquals(3, genericEntityDao.findEntitiesByProperty(Library.class, "libraryType", LibraryType.NATURAL_PRODUCTS).size());
    assertEquals(2, genericEntityDao.findEntitiesByProperty(Library.class, "libraryType", LibraryType.DISCRETE).size());
    assertEquals(0, genericEntityDao.findEntitiesByProperty(Library.class, "libraryType", LibraryType.COMMERCIAL).size());
  }

  public void testFindEntitybyProperty()
  {
    genericEntityDao.saveOrUpdateEntity(_anEntity);

    AbstractEntity e = genericEntityDao.findEntityByProperty(_anEntity.getClass(), "shortName", "libName");
    assertEquals(_anEntity, e);

    AbstractEntity e2 = genericEntityDao.findEntityByProperty(_anEntity.getClass(), "shortName", "not libName");
    assertNull(e2);
  }

  public void testFindEntitiesByPropertyWithInflation()
  {
    final Library[] expectedLibrary = new Library[1];

    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        expectedLibrary[0] = new Library(_adminUser,
                                         "ln1",
                                         "sn1",
                                         ScreenType.RNAI,
                                         LibraryType.COMMERCIAL,
                                         1,
                                         50,
                                         PlateSize.WELLS_384);
        expectedLibrary[0].createContentsVersion(new AdministrativeActivity(_adminUser, new LocalDate(), AdministrativeActivityType.LIBRARY_CONTENTS_LOADING));
        Well well1 = expectedLibrary[0].createWell(new WellKey(1, "A01"), LibraryWellType.EXPERIMENTAL);
        SilencingReagent reagent1 = well1.createSilencingReagent(new ReagentVendorIdentifier("vendor", "1a01"), SilencingReagentType.SIRNA, "AAAA");
        reagent1.getVendorGene().
        withGeneName("ANT1").
        withEntrezgeneSymbol("ENTREZ-ANT1").
        withEntrezgeneId(1).
        withSpeciesName("Human").
        withGenbankAccessionNumber("GBAN1");

        Well well2 = expectedLibrary[0].createWell(new WellKey(2, "A01"), LibraryWellType.EXPERIMENTAL);
        SilencingReagent reagent2 = well2.createSilencingReagent(new ReagentVendorIdentifier("vendor", "2a01"), SilencingReagentType.SIRNA, "CCCC");
        reagent2.getVendorGene().
        withGeneName("ANT2").
        withEntrezgeneSymbol("ENTREZ-ANT2").
        withEntrezgeneId(2).
        withSpeciesName("Human").
        withGenbankAccessionNumber("GBAN2");

        Well well3 = expectedLibrary[0].createWell(new WellKey(3, "A01"), LibraryWellType.EXPERIMENTAL);
        SilencingReagent reagent3 = well3.createSilencingReagent(new ReagentVendorIdentifier("vendor", "3a01"), SilencingReagentType.SIRNA, "TTTT");
        reagent3.getVendorGene().
        withGeneName("ANT3").
        withEntrezgeneSymbol("ENTREZ-ANT3").
        withEntrezgeneId(3).
        withSpeciesName("Human").
        withGenbankAccessionNumber("GBAN3");

        expectedLibrary[0].getLatestContentsVersion().release(new AdministrativeActivity(_adminUser, new LocalDate(), AdministrativeActivityType.LIBRARY_CONTENTS_VERSION_RELEASE));

        expectedLibrary[0].createCopy(_adminUser, CopyUsageType.LIBRARY_SCREENING_PLATES, "copy1");
        expectedLibrary[0].createCopy(_adminUser, CopyUsageType.LIBRARY_SCREENING_PLATES, "copy2");
        expectedLibrary[0].createCopy(_adminUser, CopyUsageType.LIBRARY_SCREENING_PLATES, "copy3");
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
                                                                 Library.wells.to(Well.latestReleasedReagent).to(SilencingReagent.vendorGene).to(Gene.genbankAccessionNumbers).getPath());
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
                      well.<SilencingReagent>getLatestReleasedReagent().getSequence());
         assertEquals("inflated gene",
                      "ANT" + i,
                      well.<SilencingReagent>getLatestReleasedReagent().getVendorGene().getGeneName());
         assertEquals("inflated genbankAccessionNumbers",
                      "GBAN" + i,
                      well.<SilencingReagent>getLatestReleasedReagent().getVendorGene().getGenbankAccessionNumbers().iterator().next());
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
                                                            "Member");
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
    Library library = new Library(_adminUser,
                                  "library 1",
                                  "lib1",
                                  ScreenType.SMALL_MOLECULE,
                                  LibraryType.COMMERCIAL,
                                  1,
                                  1,
                                  PlateSize.WELLS_384);
    library.createWell(new WellKey(1, "A01"), LibraryWellType.EXPERIMENTAL);
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
        screen.createLibraryScreening(_adminUser, screen.getLeadScreener(), new LocalDate());
        genericEntityDao.persistEntity(screen.getLabHead());
        genericEntityDao.persistEntity(screen.getLeadScreener());
        genericEntityDao.persistEntity(screen);
      }
    });

    class Txn implements DAOTransaction
    {
      //Screen screen;
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
        ScreeningRoomUser collab1 = new ScreeningRoomUser("Col", "Laborator1");
        ScreeningRoomUser collab2 = new ScreeningRoomUser("Col", "Laborator2");
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
  
  public void testFlushAndClearSession()
  {
    genericEntityDao.runQuery(new Query() {
      public List execute(Session session)
      {
        Library library = new Library(_adminUser,
                                      "library",
                                      "library",
                                      ScreenType.SMALL_MOLECULE,
                                      LibraryType.COMMERCIAL,
                                      1,
                                      1,
                                      PlateSize.WELLS_384);
        library.createWell(new WellKey(1, "A01"), LibraryWellType.EMPTY);
        genericEntityDao.saveOrUpdateEntity(library);
        genericEntityDao.flush();
        assertTrue(session.contains(library));
        assertEquals(2, session.getStatistics().getEntityCount());
        assertEquals(library.getLibraryId(), session.getIdentifier(library));
        genericEntityDao.clear();
        assertEquals(0, session.getStatistics().getEntityCount());
        assertFalse(session.contains(library));
        return null;
      }
    });
    
    assertNotNull(genericEntityDao.findEntityByProperty(Library.class, "libraryName", "library"));
    assertNotNull(genericEntityDao.findEntityById(Well.class, "00001:A01"));
    
  }
}
