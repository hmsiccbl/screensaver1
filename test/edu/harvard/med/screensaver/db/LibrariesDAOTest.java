// $HeadURL:
// svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/test/edu/harvard/med/screensaver/TestHibernate.java
// $
// $Id: ComplexDAOTest.java 2359 2008-05-09 21:16:57Z ant4 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.util.Set;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.cherrypicks.LabCherryPick;
import edu.harvard.med.screensaver.model.cherrypicks.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.ScreenerCherryPick;
import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.PlateSize;
import edu.harvard.med.screensaver.model.libraries.PlateType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellType;
import edu.harvard.med.screensaver.model.libraries.WellVolumeAdjustment;
import edu.harvard.med.screensaver.model.libraries.WellVolumeCorrectionActivity;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.service.cherrypicks.CherryPickRequestAllocatorTest;


/**
 * Tests the {@link DAOImpl} in some more complicated ways than
 * {@link GenericEntityDAOTest}.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class LibrariesDAOTest extends AbstractSpringPersistenceTest
{

  private static final Logger log = Logger.getLogger(LibrariesDAOTest.class);

  protected LibrariesDAO librariesDao;


  public void testCreateAndModifyCompound()
  {
    genericEntityDao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          Compound compound = new Compound("compound P", "inchi");
          compound.addChembankId("P");
          genericEntityDao.saveOrUpdateEntity(compound);
        }
      });

    genericEntityDao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          // look up a compound and modify it
          Compound compound = genericEntityDao.findEntityByProperty(
            Compound.class,
            "smiles",
            "compound P");
          assertNotNull("compound exists", compound);
          assertEquals("chembank id", "P", compound.getChembankIds().iterator().next());
          compound.removeChembankId("P");
          compound.addChembankId("P'");
        }
      });

    genericEntityDao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          // look up a compound and modify it
          Compound compound = genericEntityDao.findEntityByProperty(
            Compound.class,
            "smiles",
            "compound P");
          assertNotNull("compound exists", compound);
          assertEquals("chembank id modified", "P'", compound.getChembankIds().iterator().next());
        }
      });
  }

  public void testCreateLibraryWellCompound()
  {
    // create a new well, add compound p to it
    genericEntityDao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          Library library = new Library(
            "library Q",
            "Q",
            ScreenType.SMALL_MOLECULE,
            LibraryType.KNOWN_BIOACTIVES,
            1,
            2);
          Compound compound = new Compound("compound P", "inchi");
          Well well = library.createWell(new WellKey(27, "A01"), WellType.EXPERIMENTAL);
          well.addCompound(compound);
          genericEntityDao.saveOrUpdateEntity(library);
        }
      });

    genericEntityDao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          Library library = genericEntityDao.findEntityByProperty(
            Library.class,
            "libraryName",
            "library Q");
          assertEquals("Library's Well count", 1, library.getWells().size());
          assertEquals("library has type", LibraryType.KNOWN_BIOACTIVES, library.getLibraryType());
          Well well = library.getWells().iterator().next();
          Compound compound = genericEntityDao.findEntityByProperty(
            Compound.class,
            "smiles",
            "compound P");
          assertEquals("library has well", "A01", well.getWellName());
          assertEquals("Well's Compound count", 1, well.getCompounds().size());
          assertEquals("Compound's Well count", 1, compound.getWells().size());
          assertEquals("Well-Compound association", "compound P", well.getCompounds().iterator().next().getSmiles());
          assertEquals("Compound-Well association", "A01", compound.getWells().iterator().next().getWellName());
      }
    });
  }

  /**
   * Tests whether a Well's compounds can be modified after it has been loaded
   * from the database. (This is more a test of Hibernate than of our
   * application.)
   */
  public void testCreateWellModifyLater()
  {
    genericEntityDao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          Library library = new Library(
            "library Q",
            "Q",
            ScreenType.SMALL_MOLECULE,
            LibraryType.KNOWN_BIOACTIVES,
            1,
            2);
          library.createWell(new WellKey(27, "A01"), WellType.EXPERIMENTAL);
          genericEntityDao.saveOrUpdateEntity(library);
        }
      });

    genericEntityDao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          Library library = genericEntityDao.findEntityByProperty(Library.class, "libraryName", "library Q");
          Well well = library.getWells().iterator().next();
          Compound compound = new Compound("compound P", "inchi");
          well.addCompound(compound);
        }
      });

    genericEntityDao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          Library library = genericEntityDao.findEntityByProperty(Library.class, "libraryName", "library Q");
          Well well = library.getWells().iterator().next();
          assertTrue(well.getCompounds().contains(new Compound("compound P", "inchi P")));
        }
      });
  }

  public void testRemainingWellVolume()
  {
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        Library library = CherryPickRequestAllocatorTest.makeRNAiDuplexLibrary("library", 1, 2, PlateSize.WELLS_384);
        Copy copyC = library.createCopy(CopyUsageType.FOR_CHERRY_PICK_SCREENING, "C");
        copyC.createCopyInfo(1, "loc1", PlateType.EPPENDORF, new Volume(10));
        copyC.createCopyInfo(2, "loc1", PlateType.EPPENDORF, new Volume(100)); // should be ignored
        Copy copyD = library.createCopy(CopyUsageType.FOR_CHERRY_PICK_SCREENING, "D");
        copyD.createCopyInfo(1, "loc1", PlateType.EPPENDORF, new Volume(10));
        copyD.createCopyInfo(2, "loc1", PlateType.EPPENDORF, new Volume(100)); // should be ignored
        Copy copyE = library.createCopy(CopyUsageType.FOR_CHERRY_PICK_SCREENING, "E");
        copyE.createCopyInfo(1, "loc1", PlateType.EPPENDORF, new Volume(10));
        copyE.createCopyInfo(2, "loc1", PlateType.EPPENDORF, new Volume(100)); // should be ignored
        Copy copyF = library.createCopy(CopyUsageType.FOR_CHERRY_PICK_SCREENING, "F");
        copyF.createCopyInfo(1, "loc1", PlateType.EPPENDORF, new Volume(10));
        copyF.createCopyInfo(2, "loc1", PlateType.EPPENDORF, new Volume(100)); // should be ignored
        Copy copyG = library.createCopy(CopyUsageType.FOR_CHERRY_PICK_SCREENING, "G");
        copyG.createCopyInfo(1, "loc1", PlateType.EPPENDORF, new Volume(10)).setDateRetired(new LocalDate());

        genericEntityDao.saveOrUpdateEntity(library);

        WellVolumeCorrectionActivity wellVolumeCorrectionActivity =
          new WellVolumeCorrectionActivity(new AdministratorUser("Joe", "Admin", "joe_admin@hms.harvard.edu", "", "", "", "", ""),
                                           new LocalDate());
        Set<WellVolumeAdjustment> wellVolumeAdjustments = wellVolumeCorrectionActivity.getWellVolumeAdjustments();
        Well wellA01 = genericEntityDao.findEntityById(Well.class, "00001:A01");
        Well wellB02 = genericEntityDao.findEntityById(Well.class, "00001:B02");
        /*Well wellC03 =*/ genericEntityDao.findEntityById(Well.class, "00001:C03");
        wellVolumeAdjustments.add(wellVolumeCorrectionActivity.createWellVolumeAdjustment(copyD, wellA01, new Volume(-1)));
        wellVolumeAdjustments.add(wellVolumeCorrectionActivity.createWellVolumeAdjustment(copyF, wellA01, new Volume(-1)));
        wellVolumeAdjustments.add(wellVolumeCorrectionActivity.createWellVolumeAdjustment(copyD, wellB02, new Volume(-1)));
        wellVolumeAdjustments.add(wellVolumeCorrectionActivity.createWellVolumeAdjustment(copyF, wellB02, new Volume(-1)));
        genericEntityDao.saveOrUpdateEntity(wellVolumeCorrectionActivity);

        RNAiCherryPickRequest cherryPickRequest = CherryPickRequestAllocatorTest.createRNAiCherryPickRequest(1, new Volume(2));
        ScreenerCherryPick dummyScreenerCherryPick = cherryPickRequest.createScreenerCherryPick(wellA01);
        LabCherryPick labCherryPick1 = cherryPickRequest.createLabCherryPick(dummyScreenerCherryPick, wellA01);
        labCherryPick1.setAllocated(copyE);
        LabCherryPick labCherryPick2 = cherryPickRequest.createLabCherryPick(dummyScreenerCherryPick, wellB02);
        labCherryPick2.setAllocated(copyF);
        genericEntityDao.saveOrUpdateEntity(cherryPickRequest.getScreen().getLeadScreener());
        genericEntityDao.saveOrUpdateEntity(cherryPickRequest.getScreen().getLabHead());
        genericEntityDao.saveOrUpdateEntity(cherryPickRequest.getScreen());
      }
    });

    Copy copyC = genericEntityDao.findEntityById(Copy.class, "library:C");
    Copy copyD = genericEntityDao.findEntityById(Copy.class, "library:D");
    Copy copyE = genericEntityDao.findEntityById(Copy.class, "library:E");
    Copy copyF = genericEntityDao.findEntityById(Copy.class, "library:F");
    Copy copyG = genericEntityDao.findEntityById(Copy.class, "library:G");
    Well wellA01 = genericEntityDao.findEntityById(Well.class, "00001:A01");
    Well wellB02 = genericEntityDao.findEntityById(Well.class, "00001:B02");
    Well wellC03 = genericEntityDao.findEntityById(Well.class, "00001:C03");

    assertEquals("C:A01", new Volume(10), librariesDao.findRemainingVolumesInWellCopies(wellA01).get(copyC));
    assertEquals("C:B02", new Volume(10), librariesDao.findRemainingVolumesInWellCopies(wellB02).get(copyC));
    assertEquals("C:C03", new Volume(10), librariesDao.findRemainingVolumesInWellCopies(wellC03).get(copyC));
    assertEquals("D:A01", new Volume(9),  librariesDao.findRemainingVolumesInWellCopies(wellA01).get(copyD));
    assertEquals("D:B02", new Volume(9),  librariesDao.findRemainingVolumesInWellCopies(wellB02).get(copyD));
    assertEquals("D:C03", new Volume(10), librariesDao.findRemainingVolumesInWellCopies(wellC03).get(copyD));
    assertEquals("E:A01", new Volume(8),  librariesDao.findRemainingVolumesInWellCopies(wellA01).get(copyE));
    assertEquals("E:B02", new Volume(10), librariesDao.findRemainingVolumesInWellCopies(wellB02).get(copyE));
    assertEquals("E:C03", new Volume(10), librariesDao.findRemainingVolumesInWellCopies(wellC03).get(copyE));
    assertEquals("F:A01", new Volume(9),  librariesDao.findRemainingVolumesInWellCopies(wellA01).get(copyF));
    assertEquals("F:B02", new Volume(7),  librariesDao.findRemainingVolumesInWellCopies(wellB02).get(copyF));
    assertEquals("F:C03", new Volume(10), librariesDao.findRemainingVolumesInWellCopies(wellC03).get(copyF));
    // note: copy G is retired
    assertEquals("G:A01", null,  librariesDao.findRemainingVolumesInWellCopies(wellA01).get(copyG));
    assertEquals("G:B02", null,  librariesDao.findRemainingVolumesInWellCopies(wellB02).get(copyG));
    assertEquals("G:C03", null,  librariesDao.findRemainingVolumesInWellCopies(wellC03).get(copyG));
  }
  
  public void testDeleteSmallMoleculeLibraryContents()
  {
    Library library = MakeDummyEntities.makeDummyLibrary(1, ScreenType.SMALL_MOLECULE, 20);
    genericEntityDao.persistEntity(library);
    doTestDeleteLibraryContents(library);
  }

  public void testDeleteRnaiLibraryContents()
  {
    Library library = MakeDummyEntities.makeDummyLibrary(1, ScreenType.RNAI, 2);
    genericEntityDao.persistEntity(library);
    doTestDeleteLibraryContents(library);
  }

  public void testCreateRNaiLibraryContentsInclOwner()
  {
    Library library = MakeDummyEntities.makeDummyLibrary(1, ScreenType.RNAI, 2);
    ScreeningRoomUser owner = new ScreeningRoomUser("A",
                                                   "B",
                                                   "a.b@c");
    genericEntityDao.saveOrUpdateEntity(owner);
       
    library.setOwner(owner);

    genericEntityDao.saveOrUpdateEntity(library);
    Library resultLibrary = genericEntityDao.findEntityById(Library.class,new Integer(library.getLibraryId()));
    ScreeningRoomUser resultOwner = resultLibrary.getOwner();
    resultOwner.equals(owner);
    
  }

  
  private void doTestDeleteLibraryContents(Library library)
  {
    int i = 0;
    for (Well well : library.getWells()) {
      if (well.getWellType() == WellType.EXPERIMENTAL) {
        if (library.getScreenType() == ScreenType.SMALL_MOLECULE && well.getCompounds().size() > 0) {
          ++i;
        }
        else if (library.getScreenType() == ScreenType.RNAI && well.getSilencingReagents().size() > 0) {
          ++i;
        }
      }
    }
    assertTrue("has library contents before delete library contents", i > 0);

    librariesDao.deleteLibraryContents(library);
    Library library2 = genericEntityDao.findEntityByProperty(Library.class,
                                                             "libraryName",
                                                             "library 1",
                                                             true,
                                                             "wells.compounds", 
                                                             "wells.silencingReagents", 
                                                             "wells.molfileList");
    doTestWellsAreEmpty(library2);
  }

  private void doTestWellsAreEmpty(Library library)
  {
    for (Well well : library.getWells()) {
      assertEquals("compounds", 0, well.getCompounds().size());
      assertEquals("silencing reagents count", 0, well.getSilencingReagents().size());
      assertEquals("genes count", 0, well.getGenes().size());
      assertNull(well.getReagent());
      assertNull(well.getGenbankAccessionNumber());
      assertNull(well.getIccbNumber());
      assertNull(well.getMolfile());
      assertNull(well.getSmiles());
      assertEquals(WellType.EMPTY, well.getWellType());
    }
  }

}
