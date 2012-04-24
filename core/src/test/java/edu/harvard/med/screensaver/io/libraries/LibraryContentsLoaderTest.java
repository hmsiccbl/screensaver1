// $HeadURL:
// http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/branches/iccbl/2.3.1-dev/test/edu/harvard/med/screensaver/io/libraries/LibraryContentsLoaderTest.java
// $
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.db.NoSuchEntityException;
import edu.harvard.med.screensaver.io.ParseError;
import edu.harvard.med.screensaver.io.ParseErrorsException;
import edu.harvard.med.screensaver.model.MolarConcentration;
import edu.harvard.med.screensaver.model.MolarUnit;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivity;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryContentsVersion;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.LibraryWellType;
import edu.harvard.med.screensaver.model.libraries.MolecularFormula;
import edu.harvard.med.screensaver.model.libraries.NaturalProductReagent;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.libraries.PlateSize;
import edu.harvard.med.screensaver.model.libraries.PlateStatus;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screens.Publication;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.service.libraries.LibraryContentsLoader;
import edu.harvard.med.screensaver.service.libraries.LibraryContentsVersionManager;
import edu.harvard.med.screensaver.service.libraries.LibraryCreatorTest;
import edu.harvard.med.screensaver.test.AbstractSpringPersistenceTest;

/**
 * Check the test {@link LibraryCreatorTest} as there is overlap between these tests.
 * @author serickson
 */
// TODO: files w/errors for SM and NP libraries
public class LibraryContentsLoaderTest extends AbstractSpringPersistenceTest
{
  private static final Logger log = Logger.getLogger(LibraryContentsLoaderTest.class);

  @Autowired
  protected edu.harvard.med.screensaver.service.libraries.LibraryCreator libraryCreator;
  @Autowired
  protected LibraryContentsLoader libraryContentsLoader;
  @Autowired
  protected GenericEntityDAO genericEntityDao;
  @Autowired
  protected LibrariesDAO librariesDao;
  @Autowired
  protected LibraryContentsVersionManager libraryContentsVersionManager;
  private AdministratorUser _admin;

  protected void setUp() throws Exception
  {
    super.setUp();
    _admin = new AdministratorUser("firstname",
                                   "lastname");
    _admin.addScreensaverUserRole(ScreensaverUserRole.LIBRARIES_ADMIN);
    genericEntityDao.saveOrUpdateEntity(_admin);
    _admin = genericEntityDao.reloadEntity(_admin, false, ScreensaverUser.activitiesPerformed.castToSubtype(AdministratorUser.class));
  }

  /**
   * Do tests in a transaction since the @Transactional tag is ineffective when running the 
   * unit tests
   * @throws IOException 
   */
  public void testCleanDataRNAi() throws IOException
  {
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        _admin = genericEntityDao.reloadEntity(_admin, false, ScreensaverUser.activitiesPerformed.castToSubtype(AdministratorUser.class));
        Library library = new Library(_admin,
                                  "Human1",
                                  "Human1",
                                  ScreenType.RNAI,
                                  LibraryType.SIRNA,
                                  50001,
                                  50001,
                                  PlateSize.WELLS_384);
        libraryCreator.createLibrary(library);
        library = genericEntityDao.reloadEntity(library, false);

        // make sure that there is at least one other library in the system 
        Library otherLibrary = new Library(_admin,
                                           "Human2",
                                           "Human2",
                                           ScreenType.RNAI,
                                           LibraryType.SIRNA,
                                           50002,
                                           50002,
                                           PlateSize.WELLS_384);
        libraryCreator.createWells(otherLibrary);
        otherLibrary.createContentsVersion(_admin);
        otherLibrary.getLatestContentsVersion().release(new AdministrativeActivity(_admin, new LocalDate(), AdministrativeActivityType.LIBRARY_CONTENTS_VERSION_RELEASE));
        genericEntityDao.saveOrUpdateEntity(otherLibrary);

        try {
          LibraryContentsVersion lcv = libraryContentsLoader.loadLibraryContents(library, _admin, "clean data rnai",
                                                                                 new ClassPathResource("/libraries/clean_data_rnai.xls").getInputStream());
          libraryContentsVersionManager.releaseLibraryContentsVersion(lcv, _admin);
        }
        catch (ParseErrorsException e) {
          log.warn(e.getErrors());
          fail("workbook has errors");
        }
        catch (IOException e) {
          log.warn(e);
          fail("workbook has errors");
        }
      }
    });
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Library library = genericEntityDao.findEntityByProperty(Library.class, "shortName", "Human1");
        assertEquals("library has all wells for all plates",384,library.getWells().size());

        Well a05 = null, a07 = null, a09 = null, a11 = null, a15 = null, a20 = null;
        for (Well well: library.getWells()) {
          String wellName = well.getWellName();
          //log.info("well name " + wellName);
          if (well.getPlateNumber() == 50001) {
            if (wellName.equals("A05")) {
              a05 = well;
            }
            else if (wellName.equals("A07")) {
              a07 = well;
            }
            else if (wellName.equals("A09")) {
              a09 = well;
            }
            else if (wellName.equals("A11")) {
              a11 = well;
            }
            else if (wellName.equals("A15")) {
              a15 = well;
            }
            else if (wellName.equals("A20")) {
              a20 = well;
            }
          }
        }

        assertNotNull("library has well A05", a05);
        assertNotNull("library has well A07", a07);
        assertNotNull("library has well A09", a09);
        assertNotNull("library has well A11", a11);
        assertNotNull("library has well A15", a15);
        assertNotNull("library has well A20", a20);

        // DO a full test of a05
        SilencingReagent sr = (SilencingReagent)a05.getLatestReleasedReagent();
        assertEquals("vendorX", sr.getVendorId().getVendorName());
        assertEquals("M-005300-00", sr.getVendorId().getVendorIdentifier());
        assertEquals(MolarConcentration.makeConcentration("1", MolarUnit.MICROMOLAR),a05.getMolarConcentration());
        assertEquals("F-005300-00", a05.getFacilityId());
        assertEquals(SilencingReagentType.SIRNA, sr.getSilencingReagentType());
        assertEquals("GACAUGCACUGCCUAAUUA;GUACAGAACUCUCCCAUUC;GAUGAAAUGUGCCUUGAAA;GAAGGUGGAUUUGCUAUUG",
                     ((SilencingReagent)a05.getLatestReleasedReagent()).getSequence() );

        Gene gene = sr.getVendorGene();
        assertEquals(new Integer(22848),gene.getEntrezgeneId() );
        assertTrue("actual: " + gene.getEntrezgeneSymbols() + ", expected: " + Sets.newHashSet("AAK1","AAK2") 
                   , Lists.newArrayList("AAK1","AAK2").equals(gene.getEntrezgeneSymbols()));
        assertEquals("VendorGeneNameX", gene.getGeneName());
        assertTrue("actual: " + gene.getGenbankAccessionNumbers() + ", expected: " + Sets.newHashSet("NM_014911","NM_014912")
                   , Sets.newHashSet("NM_014911","NM_014912").containsAll(gene.getGenbankAccessionNumbers()));
        assertEquals("VendorSpeciesX", gene.getSpeciesName());

        gene = sr.getFacilityGene();
        assertEquals(new Integer(1111),gene.getEntrezgeneId() );
        assertTrue("List is:" + gene.getEntrezgeneSymbols(), 
        					Lists.newArrayList("AAK3","AAK4").equals(gene.getEntrezgeneSymbols()));
        assertEquals("FacilityGeneNameX", gene.getGeneName());
        assertTrue(Sets.newHashSet("F_014911", "F_014914").containsAll(gene.getGenbankAccessionNumbers()));
        assertEquals("FacilitySpeciesX", gene.getSpeciesName());

        //test random other values

        assertEquals(LibraryWellType.LIBRARY_CONTROL, a07.getLibraryWellType());
        assertEquals("GAAAUUGCACUGUCACUAA;CUCAGGAACUCUAUUCUAU;AAACGCCGUCCUUUGAAUA;GCUAAAUCAUCCUUGCAUC",
                     ((SilencingReagent)a15.getLatestReleasedReagent()).getSequence() );
        assertEquals(SilencingReagentType.ESIRNA, ((SilencingReagent)a15.getLatestReleasedReagent()).getSilencingReagentType());
        assertEquals(SilencingReagentType.SIRNA, ((SilencingReagent)a07.getLatestReleasedReagent()).getSilencingReagentType());
        assertEquals(LibraryWellType.EMPTY, a09.getLibraryWellType());
        assertEquals(LibraryWellType.RNAI_BUFFER, a11.getLibraryWellType());
        assertEquals(LibraryWellType.LIBRARY_CONTROL, a20.getLibraryWellType());
        assertEquals(MolarConcentration.makeConcentration("111.11", MolarUnit.MILLIMOLAR),a15.getMolarConcentration());

      }
    });
  }

  public void testDirtyDataRNAi()
  {
    try {
      genericEntityDao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          _admin = genericEntityDao.reloadEntity(_admin, false, ScreensaverUser.activitiesPerformed.castToSubtype(AdministratorUser.class));
          Library library = new Library(_admin,
                                        "Human1",
                                        "Human1",
                                        ScreenType.RNAI,
                                        LibraryType.SIRNA,
                                        50001,
                                        50003,
                                        PlateSize.WELLS_384);
          libraryCreator.createLibrary(library);
          library = genericEntityDao.reloadEntity(library, false);

          try {
            libraryContentsLoader.loadLibraryContents(library, _admin, "dirty data RNAi", new ClassPathResource("/libraries/dirty_data_rnai.xls").getInputStream());
            fail("no error exception found");
          }
          catch (ParseErrorsException e) {
            assertFalse("no lcv added to database", genericEntityDao.findAllEntitiesOfType(LibraryContentsVersion.class).isEmpty());
            assertTrue("reagents were added to database", genericEntityDao.findAllEntitiesOfType(Reagent.class).isEmpty());

            // expected error cells, [row,col]
            Set<ParseError> expectedErrors = Sets.newHashSet();
            expectedErrors.add(new ParseError("Vendor and Vendor Reagent ID must be null for well type: empty", "Human Kinases:(F,4)"));
            expectedErrors.add(new ParseError("'buffer1' must be one of: [DMSO, empty, experimental, <undefined>, RNAi buffer, library control]", "Human Kinases:(C,5)"));
            expectedErrors.add(new ParseError("Vendor and Vendor Reagent ID must both be specified, or neither should be specified", "Human Kinases:(F,6)"));
            expectedErrors.add(new ParseError("if sequence is specified, Vendor Reagent ID and Silencing Reagent Type must be specified", "Human Kinases:(I,7)"));
            expectedErrors.add(new ParseError("Well is required", "Human Kinases:(B,8)"));
            expectedErrors.add(new ParseError("Well Type is required", "Human Kinases:(C,9)"));
            expectedErrors.add(new ParseError("Vendor is required", "Human Kinases:(E,10)"));
            expectedErrors.add(new ParseError("Silencing Reagent Type is required", "Human Kinases:(H,11)"));
            expectedErrors.add(new ParseError("NumberFormatException", "Human Kinases 2:(J,12)"));

            log.debug("expected errors that were not reported: " +
              Sets.difference(expectedErrors, Sets.newHashSet(e.getErrors())));
            log.debug("unexpected errors that were reported: " +
              Sets.difference(Sets.newHashSet(e.getErrors()), expectedErrors));
            assertEquals("expected errors same as reported errors",
                         expectedErrors, Sets.newHashSet(e.getErrors()));
          }
          catch (Exception e) {
            fail("expected ParseErrorsException, not " + e);
          }
        }
      });
      fail("A transaction exception was expected!");
    }
    catch (Exception e) {}
    assertTrue("database unchanged when errors exist in input file",
               genericEntityDao.findAllEntitiesOfType(LibraryContentsVersion.class).isEmpty());
  }

  public void testCleanDataRNAi_withDuplex() throws IOException
  {
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        _admin = genericEntityDao.reloadEntity(_admin, false, ScreensaverUser.activitiesPerformed.castToSubtype(AdministratorUser.class));
        // Load all of the duplex libraries needed for the pool library
        Library library = new Library(_admin,
                                      "Human1_duplex",
                                      "Human1_duplex",
                                      ScreenType.RNAI,
                                      LibraryType.SIRNA,
                                      50440,
                                      50443,
                                      PlateSize.WELLS_384);
        libraryCreator.createLibrary(library);
        library = genericEntityDao.reloadEntity(library, false);

        try {
          LibraryContentsVersion lcv = libraryContentsLoader.loadLibraryContents(library, _admin, "clean data rnai with duplex",
                                                                                 new ClassPathResource("/libraries/clean_rnai_duplex_50440_50443.xls").getInputStream());
          libraryContentsVersionManager.releaseLibraryContentsVersion(lcv, _admin);
        }
        catch (ParseErrorsException e) {
          log.warn(e.getErrors());
          fail("workbook has errors");
        }
        catch (IOException e) {
          log.error(e);
          fail("could not load the file");
        }

        // now load the pool library that references the duplex library
        _admin = genericEntityDao.reloadEntity(_admin, false, ScreensaverUser.activitiesPerformed.castToSubtype(AdministratorUser.class));
        library = new Library(_admin,
                              "Human1_pool",
                              "Human1_pool",
                              ScreenType.RNAI,
                              LibraryType.SIRNA,
                              50439,
                              50439,
                              PlateSize.WELLS_384);
        library.setPool(true);
        libraryCreator.createLibrary(library);
        library = genericEntityDao.reloadEntity(library, false);

        try {
          LibraryContentsVersion lcv = libraryContentsLoader.loadLibraryContents(library, _admin, "clean data rnai with duplex",
                                                                                 new ClassPathResource("/libraries/clean_rnai_pool.xls").getInputStream());
          libraryContentsVersionManager.releaseLibraryContentsVersion(lcv, _admin);
        }
        catch (ParseErrorsException e) {
          log.warn(e.getErrors());
          fail("workbook has errors");
        }
        catch (IOException e) {
          log.error(e);
          fail("could not load the file");
        }

        // find out if this vendorReagentId is on a silencing reagent for the following well
        String vendorReagentId = "M-024927-00";
        WellKey wellKey = new WellKey(50439, "A05");
        Well well = genericEntityDao.findEntityById(
                                                    Well.class,
                                                    wellKey.getKey(),
                                                    false,
                                                    Well.latestReleasedReagent.to(SilencingReagent.duplexWells));
    
        assertNotNull(well);

        Set<SilencingReagent> srs = ((SilencingReagent) well.getLatestReleasedReagent()).getDuplexSilencingReagents();
        assertNotNull(srs);
        assertEquals(4, srs.size());

        boolean found = false;
        for (SilencingReagent sr : srs)
        {
          log.info("Sr for well: " + sr.getWell() + ": " + sr.getVendorId());
          if (sr.getWell().getColumn() == 4 && sr.getWell().getRow() == 0) // "A05"
          {
            assertEquals(vendorReagentId, sr.getVendorId().getVendorIdentifier());
            found = true;
          }
        }
        assertTrue("Could not find the duplex well, expected for " + well
            + ", namely the one having the ReagentVendorIdentifer: \"" + vendorReagentId + "\"", found);

        // find out if this vendorReagentId is on a silencing reagent for the following well
        vendorReagentId = "M-016115-00";
        wellKey = new WellKey(50439, "E07");
        well = genericEntityDao.findEntityById(
                                               Well.class,
                                               wellKey.getKey(),
                                               false,
                                               Well.latestReleasedReagent.to(SilencingReagent.duplexWells));
    
        assertNotNull(well);

        srs = ((SilencingReagent) well.getLatestReleasedReagent()).getDuplexSilencingReagents();

        assertNotNull(srs);
        assertEquals(4, srs.size());
        
        found = false;
        for (SilencingReagent sr : srs)
        {
          log.info("Sr for well: " + sr.getWell() + ": " + sr.getVendorId());
          if (sr.getWell().getColumn() == 6 && sr.getWell().getRow() == 4) // "E07" 
          {
            assertEquals(vendorReagentId, sr.getVendorId().getVendorIdentifier());
            found = true;
          }
        }
        assertTrue("Could not find the duplex well, expected for " + well
            + ", namely the one having the ReagentVendorIdentifer: \"" + vendorReagentId + "\"", found);
      }
    });
  }  
  
  public void testCleanDataNaturalProduct() throws IOException
  {
    final int TEST_PLATE = 2037;
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        _admin = genericEntityDao.reloadEntity(_admin, false, ScreensaverUser.activitiesPerformed.castToSubtype(AdministratorUser.class));
        Library library = new Library(_admin,
                                      "NaturalProdTest",
                                      "Natprod",
                                      ScreenType.SMALL_MOLECULE,
                                      LibraryType.NATURAL_PRODUCTS,
                                      TEST_PLATE,
                                      TEST_PLATE,
                                      PlateSize.WELLS_384);
        library.setProvider("test vendor");
        libraryCreator.createLibrary(library);
        library = genericEntityDao.reloadEntity(library, false);

        try {
          libraryContentsLoader.loadLibraryContents(library, _admin, "clean data natural product",
                                                    new ClassPathResource("/libraries/clean_data_natural_product.xls").getInputStream());
        }
        catch (ParseErrorsException e)
        {
          log.warn(e.getErrors());
          fail("workbook has errors");
        }
        catch (IOException e) {
          log.error(e);
          fail("could not load the file");
        }
      }
    });
    
    assertEquals("natural product reagent count", 351, genericEntityDao.findAllEntitiesOfType(NaturalProductReagent.class).size());

    // wells w/o reagent ID are empty 
    Well well = librariesDao.findWell(new WellKey(TEST_PLATE, "A21"));
    assertNotNull(well);
    assertEquals(LibraryWellType.EMPTY, well.getLibraryWellType());
    
    // unspecified wells are undefined 
    well = librariesDao.findWell(new WellKey(TEST_PLATE, "A23"));
    assertNotNull(well);
    assertEquals(LibraryWellType.UNDEFINED, well.getLibraryWellType());
  }

  public void testCleanDataSmallMolecule() throws IOException
  {
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        _admin = genericEntityDao.reloadEntity(_admin, false, ScreensaverUser.activitiesPerformed.castToSubtype(AdministratorUser.class));
        Library library = new Library(_admin,
                                      "COMP",
                                      "COMP",
                                      ScreenType.SMALL_MOLECULE,
                                      LibraryType.OTHER,
                                      1534,
                                      1534,
                                      PlateSize.WELLS_384);
        libraryCreator.createLibrary(library);

        // for [#2920] create the plate and copy so that the library loading will invoke the plateUpdater
        Plate plate = findPlateAndCreateCopyIfNecessary(genericEntityDao, librariesDao, 1534, "A", CopyUsageType.STOCK_PLATES, _admin);
        library = genericEntityDao.reloadEntity(library, false, Library.copies);

        String filename = "clean_data_small_molecule.sdf";
        library = genericEntityDao.reloadEntity(library, false);

        try {
          LibraryContentsVersion lcv = libraryContentsLoader.loadLibraryContents(library, _admin, "clean data small molecule",
                                                                                 new ClassPathResource("/libraries/clean_data_small_molecule.sdf").getInputStream());
          libraryContentsVersionManager.releaseLibraryContentsVersion(lcv, _admin);
          log.info("added library definition for " + library);
        }
        catch (ParseErrorsException e)
        {
          log.warn(e.getErrors());
          fail("input file has errors: " + e.getErrors());
        }
        catch (IOException e) {
          log.error(e);
          fail("could not load the file");
        }
      }
    });
    
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        _admin = genericEntityDao.reloadEntity(_admin, false, ScreensaverUser.activitiesPerformed.castToSubtype(AdministratorUser.class));
        // test non-specified well is undefined
        Well well = librariesDao.findWell(new WellKey(1534, "A23"));
        assertNotNull(well);
        assertEquals(LibraryWellType.UNDEFINED, well.getLibraryWellType());
  
        // test empty well is empty
        well = librariesDao.findWell(new WellKey(1534, "A09"));
        assertNotNull(well);
        assertEquals(LibraryWellType.EMPTY, well.getLibraryWellType());
  
        // test sd record with empty molfile
        well = librariesDao.findWell(new WellKey(1534, "A01"));
        SmallMoleculeReagent compound = well.<SmallMoleculeReagent>getLatestReleasedReagent();
        assertNotNull(well);
        assertEquals(LibraryWellType.EXPERIMENTAL, well.getLibraryWellType());
        assertEquals(new ReagentVendorIdentifier("Biomol-TimTec", "SPL000058"), well.<Reagent>getLatestReleasedReagent().getVendorId());
        assertEquals("ICCB-00589081", well.getFacilityId());
        assertEquals("chembl id", Sets.newHashSet(100001, 100002, 111102), compound.getChemblIds());

        // new LINCS fields
        assertEquals("vendor batch", "HM-001_TM-20090805", compound.getVendorBatchId());
        assertEquals("facility batch", new Integer(8), compound.getFacilityBatchId());
        assertEquals("salt form id", new Integer(101), compound.getSaltFormId());
  
        assertTrue(!well.<Reagent>getLatestReleasedReagent().getPublications().isEmpty());
        Set<Integer> expectedPubmedIds = Sets.newHashSet(20653109, 20653081);
        for (Publication publication : well.<Reagent>getLatestReleasedReagent().getPublications()) {
          expectedPubmedIds.remove(publication.getPubmedId());
        }
        assertTrue("pubmed ids not saved: " + expectedPubmedIds, expectedPubmedIds.isEmpty());
  
        // doTestNonstructuralCompoundAttributes
        // ICCB_NUM; CompoundName
        well = librariesDao.findWell(new WellKey(1534, "A02"));
        assertNotNull(well);
        assertEquals(LibraryWellType.EXPERIMENTAL, well.getLibraryWellType());
        assertEquals(new ReagentVendorIdentifier("Biomol-TimTec", "ST001215"), well.<Reagent>getLatestReleasedReagent().getVendorId());
        assertEquals("ICCB-00589082", well.getFacilityId());
        compound = well.<SmallMoleculeReagent>getLatestReleasedReagent();
        List<String> compoundNames = compound.getCompoundNames();
        assertEquals("compound must have one compound name", compoundNames.size(), 1);
        String compoundName = compoundNames.iterator().next();
        assertEquals("fake compound name 1", compoundName);
  
        // ICCB_Num; compound_identifier
        well = librariesDao.findWell(new WellKey(1534, "A03"));
        compound = well.<SmallMoleculeReagent>getLatestReleasedReagent();
        compoundNames = compound.getCompoundNames();
        assertEquals("compound must have one compound name", 1, compoundNames.size());
        compoundName = compoundNames.iterator().next();
        assertEquals("fake compound name 2", compoundName);
        assertEquals("mg/mL concentration", new BigDecimal("2.460"), well.getMgMlConcentration());  
 
        // ChemicalName
        well = librariesDao.findWell(new WellKey(1534, "A04"));
        compound = well.<SmallMoleculeReagent>getLatestReleasedReagent();
        compoundNames = compound.getCompoundNames();
        assertEquals("compound must have one compound name", 1, compoundNames.size());
        compoundName = compoundNames.iterator().next();
        assertEquals("fake compound name 3", compoundName);
        assertEquals("mg/mL concentration", new BigDecimal("0.123"), well.getMgMlConcentration());
  
        // Chemical_Name
        well = librariesDao.findWell(new WellKey(1534, "A05"));
        compound = well.<SmallMoleculeReagent>getLatestReleasedReagent();
        compoundNames = compound.getCompoundNames();
        assertEquals("compound has one compound name", 1, compoundNames.size());
        compoundName = compoundNames.iterator().next();
        assertEquals("fake compound name 4", compoundName);
        assertEquals("concentration", MolarConcentration.makeConcentration("115", MolarUnit.PICOMOLAR), well.getMolarConcentration());
        assertEquals("molar concentration", MolarConcentration.makeConcentration("115", MolarUnit.PICOMOLAR), well.getMolarConcentration());
  
        // doTestStructuralCompoundAttributes()
        well = librariesDao.findWell(new WellKey(1534, "A08"));
        compound = well.<SmallMoleculeReagent>getLatestReleasedReagent();
        assertTrue("molfile contents", compound.getMolfile().contains("Structure113") &&
                   compound.getMolfile().contains("15 14  1  0  0  0  0") &&
                   compound.getMolfile().contains("M  END"));
        assertEquals("compound smiles", "O=C1CC(C)(C)CC(=O)C1C(c1ccccc1)C1=C(O)CC(C)(C)CC1=O", compound.getSmiles());
        assertEquals("compound inchi", "InChI=1/C23H28O4/c1-22(2)10-15(24)20(16(25)11-22)19(14-8-6-5-7-9-14)21-17(26)12-23(3,4)13-18(21)27/h5-9,19-20,26H,10-13H2,1-4H3", compound.getInchi());
        assertEquals("molecular mass", new BigDecimal("368.46602"), compound.getMolecularMass().setScale(5));
        assertEquals("molecular weight", new BigDecimal("369.00020"), compound.getMolecularWeight().setScale(5));
        assertEquals("concentration", MolarConcentration.makeConcentration("115", MolarUnit.NANOMOLAR), well.getMolarConcentration());
        
        assertEquals("molecular formula", new MolecularFormula("C23H28O4"), compound.getMolecularFormula());
        assertEquals("pubchem cid", Sets.newHashSet(558309, 7335957), compound.getPubchemCids());
        assertEquals("chembank id", Sets.newHashSet(6066882,1665724), compound.getChembankIds());
        
        // test the plate concentration values
        
        Plate plate = genericEntityDao.findEntityByProperty(Plate.class, "plateNumber" , 1534, true, Plate.copy);
        assertNotNull(plate);
        assertEquals(MolarConcentration.makeConcentration("115", MolarUnit.NANOMOLAR), plate.getMaxMolarConcentration());
        assertEquals(MolarConcentration.makeConcentration("115", MolarUnit.PICOMOLAR), plate.getMinMolarConcentration());
        assertEquals(MolarConcentration.makeConcentration("115", MolarUnit.NANOMOLAR), plate.getPrimaryWellMolarConcentration());
        assertEquals(new BigDecimal("2.460"), plate.getMaxMgMlConcentration());
        assertEquals(new BigDecimal("0.123"), plate.getMinMgMlConcentration());
        assertEquals(new BigDecimal("2.460"), plate.getPrimaryWellMgMlConcentration()); // note: 2.46 occurs once, as does 0.123, so the comparator chooses the greater value

        // test the copy concentration values

        Copy copy = genericEntityDao.reloadEntity(plate.getCopy());
        assertNotNull(copy);
        assertEquals(MolarConcentration.makeConcentration("115", MolarUnit.NANOMOLAR), copy.getMaxMolarConcentration());
        assertEquals(MolarConcentration.makeConcentration("115", MolarUnit.PICOMOLAR), copy.getMinMolarConcentration());
        assertEquals(MolarConcentration.makeConcentration("115", MolarUnit.NANOMOLAR), copy.getPrimaryWellMolarConcentration());
        assertEquals(new BigDecimal("2.460"), copy.getMaxMgMlConcentration());
        assertEquals(new BigDecimal("0.123"), copy.getMinMgMlConcentration());
        assertEquals(new BigDecimal("2.460"), copy.getPrimaryWellMgMlConcentration()); // note: 2.46 occurs once, as does 0.123, so the comparator chooses the greater value
        
        // for [#3439] Old well values are not nulled out before reloading new library content versions
        well.setMgMlConcentration(new BigDecimal(".001"));
      }
    });
    
    
    // for [#3439] Old well values are not nulled out before reloading new library content versions
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        _admin = genericEntityDao.reloadEntity(_admin, false, ScreensaverUser.activitiesPerformed.castToSubtype(AdministratorUser.class));
        Library library = librariesDao.findLibraryWithPlate(1534);
        library = genericEntityDao.reloadEntity(library, false, Library.wells);
        Well well = librariesDao.findWell(new WellKey(1534, "A08"));
        assertEquals(new BigDecimal(".001"), well.getMgMlConcentration());
        try {
          LibraryContentsVersion lcv = libraryContentsLoader.loadLibraryContents(library, _admin, "clean data small molecule",
                                                                                 new ClassPathResource("/libraries/clean_data_small_molecule.sdf").getInputStream());
          libraryContentsVersionManager.releaseLibraryContentsVersion(lcv, _admin);
          log.info("added library definition for " + library);
        }catch(Exception e) {
        	log.error("on library load", e);
        	fail("on library load: " + e);
        }
        //flushAndClear();

      }
    });
    
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
    
    Well well = librariesDao.findWell(new WellKey(1534, "A08"));
    assertNotNull(well.getMolarConcentration());
    assertNull(well.getMgMlConcentration());
    assertEquals("concentration", MolarConcentration.makeConcentration("115", MolarUnit.NANOMOLAR), well.getMolarConcentration());
  }
});

  }
    
  public static Plate findPlateAndCreateCopyIfNecessary(GenericEntityDAO dao,
                                                        LibrariesDAO librariesDao,
                                                        Integer plateNumber,
                                                        String copyName,
                                                        CopyUsageType copyUsageType,
                                                        AdministratorUser recordedBy)
  {
    Plate plate = librariesDao.findPlate(plateNumber, copyName);
    if (plate == null) {
      Library library = librariesDao.findLibraryWithPlate(plateNumber);
      if (library == null) {
        throw NoSuchEntityException.forProperty(Library.class, "plateNumber", plateNumber);
      }
      Copy copy = library.createCopy(recordedBy, copyUsageType, copyName);
      log.info("created new copy " + copyName + " for library " + library.getLibraryName());
      dao.flush();
      plate = copy.findPlate(plateNumber);
    }
    plate.setStatus(PlateStatus.AVAILABLE);
    return plate;
  }  
}
