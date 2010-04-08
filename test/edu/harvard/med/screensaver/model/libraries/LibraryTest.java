// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import java.beans.IntrospectionException;
import java.math.BigDecimal;

import junit.framework.TestSuite;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;
import edu.harvard.med.screensaver.model.Activity;
import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;

import org.joda.time.LocalDate;

public class LibraryTest extends AbstractEntityInstanceTest<Library>
{
  public static TestSuite suite()
  {
    return buildTestSuite(LibraryTest.class, Library.class);
  }

  public LibraryTest() throws IntrospectionException
  {
    super(Library.class);
  }
  
  public void testSmallMoleculeLibraryAndReagents()
  {
    schemaUtil.truncateTablesOrCreateSchema();
    
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Library library = new Library("Small Molecule Library", "smLib", ScreenType.SMALL_MOLECULE, LibraryType.COMMERCIAL, 1, 2, PlateSize.WELLS_384);
        AdministratorUser adminUser = new AdministratorUser("Admin", "User", "", "", "", "", "", "");
        library.createContentsVersion(new AdministrativeActivity(adminUser, new LocalDate(), AdministrativeActivityType.LIBRARY_CONTENTS_LOADING));
        PlateSize plateSize = library.getPlateSize();
        for (int plate = library.getStartPlate(); plate <= library.getEndPlate(); ++plate) {
          for (int row = 0; row < plateSize.getRows(); ++row) {
            for (int col = 0; col < plateSize.getColumns(); ++col) {
              Well well = library.createWell(new WellKey(plate, row, col),
                                             LibraryWellType.UNDEFINED);

              /*SmallMoleculeReagent reagent =*/ 
              well.createSmallMoleculeReagent(new ReagentVendorIdentifier("vendor", well.getWellKey().toString()),
                                              "molfile",
                                              "smiles",
                                              "inchi",
                                              new BigDecimal("1.000"),
                                              new BigDecimal("1.002"),
                                              new MolecularFormula("M1F2"));
            }
          }
        }

        genericEntityDao.persistEntity(adminUser);
        genericEntityDao.persistEntity(library);
      }
    });

    // test with no released contents version
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Library library = genericEntityDao.findEntityByProperty(Library.class, "shortName", "smLib");
        assertEquals("library versions", 1, library.getContentsVersions().size());
        assertNull("no released contents versions", library.getLatestReleasedContentsVersion());
        assertEquals("first library version's library", library, library.getContentsVersions().iterator().next().getLibrary());
        assertEquals("first library version's number", new Integer(1), library.getContentsVersions().iterator().next().getVersionNumber());
        assertEquals("library wells count", PlateSize.WELLS_384.getWellCount() * 2, library.getWells().size());
        for (Well well : library.getWells()) {
          assertEquals("well reagents count", 1, well.getReagents().size());
          assertNull("no released reagent", well.<Reagent>getLatestReleasedReagent());
          Reagent reagent = well.getReagents().get(library.getContentsVersions().first());
          assertTrue("well reagents are small molecule", reagent instanceof SmallMoleculeReagent);
          SmallMoleculeReagent smReagent = (SmallMoleculeReagent) reagent;
          assertEquals("reagent molfile", "molfile", smReagent.getMolfile());
          assertEquals("reagent smiles", "smiles", smReagent.getSmiles());
          assertEquals("reagent inchi", "inchi", smReagent.getInchi());
          assertEquals("reagent mol mass", new BigDecimal("1.000"), smReagent.getMolecularMass().setScale(3));
          assertEquals("reagent mol weight", new BigDecimal("1.002"), smReagent.getMolecularWeight().setScale(3));
          assertEquals("reagent mol form", "M1F2", smReagent.getMolecularFormula().toString());
        }
        
        // setup for next block of tests
        LibraryContentsVersion lcv = library.getContentsVersions().first();
        AdministratorUser adminUser = genericEntityDao.findAllEntitiesOfType(AdministratorUser.class).get(0);
        lcv.release(new AdministrativeActivity(adminUser, new LocalDate(), AdministrativeActivityType.LIBRARY_CONTENTS_VERSION_RELEASE));
      }
    });

    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Library library = genericEntityDao.findEntityByProperty(Library.class, "shortName", "smLib");
        assertEquals("library versions", 1, library.getContentsVersions().size());
        assertEquals("released contents version", library.getContentsVersions().first(), library.getLatestReleasedContentsVersion());
        assertEquals("first library version's library", library, library.getContentsVersions().first().getLibrary());
        assertEquals("first library version's number", new Integer(1), library.getContentsVersions().first().getVersionNumber());
        assertEquals("library wells count", PlateSize.WELLS_384.getWellCount() * 2, library.getWells().size());
        for (Well well : library.getWells()) {
          assertEquals("well reagents count", 1, well.getReagents().size());
          Reagent reagent = well.getReagents().get(library.getLatestReleasedContentsVersion());
          assertTrue("well reagents are small molecule", reagent instanceof SmallMoleculeReagent);
          SmallMoleculeReagent smReagent = (SmallMoleculeReagent) reagent;
          assertEquals("reagent molfile", "molfile", smReagent.getMolfile());
          assertEquals("reagent smiles", "smiles", smReagent.getSmiles());
          assertEquals("reagent inchi", "inchi", smReagent.getInchi());
          assertEquals("reagent mol mass", new BigDecimal("1.000"), smReagent.getMolecularMass().setScale(3));
          assertEquals("reagent mol weight", new BigDecimal("1.002"), smReagent.getMolecularWeight().setScale(3));
          assertEquals("reagent mol form", "M1F2", smReagent.getMolecularFormula().toString());
        }
      }
    });
    
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Library library = genericEntityDao.findEntityByProperty(Library.class, "shortName", "smLib");
        AdministratorUser admin = genericEntityDao.findAllEntitiesOfType(AdministratorUser.class).get(0);
        library.createContentsVersion(new AdministrativeActivity(admin, 
                                                                 new LocalDate(), 
                                                                 AdministrativeActivityType.LIBRARY_CONTENTS_LOADING));
        for (Well well : library.getWells()) {
          /*SmallMoleculeReagent reagent =*/ 
          well.createSmallMoleculeReagent(new ReagentVendorIdentifier("vendor", well.getWellKey().toString()),
                                          "molfile2",
                                          "smiles2",
                                          "inchi2",
                                          new BigDecimal("2.000"),
                                          new BigDecimal("2.002"),
                                          new MolecularFormula("M2F2"));
        }
        LibraryContentsVersion lcv = library.getLatestContentsVersion();
        lcv.release(new AdministrativeActivity((AdministratorUser) lcv.getLoadingActivity().getPerformedBy(), new LocalDate(), AdministrativeActivityType.LIBRARY_CONTENTS_VERSION_RELEASE));
      }
    });

    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Library library = genericEntityDao.findEntityByProperty(Library.class, "shortName", "smLib");
        assertEquals("library versions", 2, library.getContentsVersions().size());
        assertEquals("released contents version", library.getContentsVersions().last(), library.getLatestReleasedContentsVersion());
        assertEquals("released contents version number", 2, library.getLatestReleasedContentsVersion().getVersionNumber().intValue());
        assertEquals("last library version's library", library, library.getContentsVersions().last().getLibrary());
        assertEquals("last library version's number", new Integer(2), library.getContentsVersions().last().getVersionNumber());
        assertEquals("library wells count", PlateSize.WELLS_384.getWellCount() * 2, library.getWells().size());
        for (Well well : library.getWells()) {
          assertEquals("well reagents count", 2, well.getReagents().size());
          Reagent reagent = well.getReagents().get(library.getLatestReleasedContentsVersion());
          assertTrue("well reagents are small molecule", reagent instanceof SmallMoleculeReagent);
          SmallMoleculeReagent smReagent = (SmallMoleculeReagent) reagent;
          assertEquals("reagent molfile", "molfile2", smReagent.getMolfile());
          assertEquals("reagent smiles", "smiles2", smReagent.getSmiles());
          assertEquals("reagent inchi", "inchi2", smReagent.getInchi());
          assertEquals("reagent mol mass", new BigDecimal("2.000"), smReagent.getMolecularMass().setScale(3));
          assertEquals("reagent mol mass", new BigDecimal("2.002"), smReagent.getMolecularWeight().setScale(3));
          assertEquals("reagent mol form", "M2F2", smReagent.getMolecularFormula().toString());
          assertEquals(new Integer(1), well.getReagents().get(library.getContentsVersions().first()).getLibraryContentsVersion().getVersionNumber());
          assertEquals(new Integer(2), well.getReagents().get(library.getContentsVersions().last()).getLibraryContentsVersion().getVersionNumber());
        }
      }
    });
  }
  
  public void testExperimentalWellCount()
  {
    schemaUtil.truncateTablesOrCreateSchema();
    Library library = dataFactory.newInstance(Library.class);
    assertEquals(new Integer(0), library.getExperimentalWellCount());
    library.createWell(new WellKey(library.getStartPlate(), "A01"), LibraryWellType.DMSO);
    assertEquals(new Integer(0), library.getExperimentalWellCount());
    library.createWell(new WellKey(library.getStartPlate(), "A02"), LibraryWellType.EXPERIMENTAL);
    assertEquals(new Integer(1), library.getExperimentalWellCount());
    Well well = library.createWell(new WellKey(library.getStartPlate(), "A03"), LibraryWellType.EXPERIMENTAL);
    assertEquals(new Integer(2), library.getExperimentalWellCount());
    well.setLibraryWellType(LibraryWellType.BUFFER);
    assertEquals(new Integer(1), library.getExperimentalWellCount());
    library.createWell(new WellKey(library.getStartPlate(), "A04"), LibraryWellType.EXPERIMENTAL);
    
    genericEntityDao.persistEntity(library);
    library = genericEntityDao.reloadEntity(library);
    assertEquals(new Integer(2), library.getExperimentalWellCount());
  }
}

