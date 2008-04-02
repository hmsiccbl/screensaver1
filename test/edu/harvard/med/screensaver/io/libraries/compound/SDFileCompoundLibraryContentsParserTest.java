// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries.compound;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.db.SchemaUtil;
import edu.harvard.med.screensaver.io.ParseError;
import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellType;
import edu.harvard.med.screensaver.model.screens.ScreenType;


public class SDFileCompoundLibraryContentsParserTest extends AbstractSpringTest
{

  // static fields

  private static final Logger log = Logger.getLogger(SDFileCompoundLibraryContentsParserTest.class);
  private static final File TEST_INPUT_FILE_DIR =
    new File("test/edu/harvard/med/screensaver/io/libraries/compound");


  // instance fields

  protected SDFileCompoundLibraryContentsParser compoundLibraryContentsParser;
  protected GenericEntityDAO genericEntityDao;
  protected LibrariesDAO librariesDao;
  protected SchemaUtil schemaUtil;


  // constructor and test methods

  // we only want to parse the file one time, 
  protected void onSetUp() throws Exception
  {
    schemaUtil.truncateTablesOrCreateSchema();
  }

  /**
   * Do all SD file parser testing in a single test, to avoid the overhead of parsing multiple files.
   */
  public void testSDFileCompoundLibraryCompoundParser()
  {
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        createLibraryAndParseTestInput();
        doTestEmptyWellIsEmpty();
        doTestSDRecordWithEmptyMolfile();
        doTestNonstructuralCompoundAttributes();
        doTestStructuralCompoundAttributes();    
        doTestTwoCompoundsInWell();
      }
    });
  }
  
  private Library createLibraryAndParseTestInput() {
    Library library = new Library("COMP", "COMP", ScreenType.OTHER, LibraryType.OTHER, 1534, 1534);
    String filename = "compound-library-contents-parser-test.sdf";
    File file = new File(TEST_INPUT_FILE_DIR, filename);
    InputStream stream = null;
    try {
      stream = new FileInputStream(file);
    }
    catch (FileNotFoundException e) {
      fail("file not found: " + filename);
    }
    library = compoundLibraryContentsParser.parseLibraryContents(library, file, stream);
    genericEntityDao.saveOrUpdateEntity(library);

    List<? extends ParseError> errors = compoundLibraryContentsParser.getErrors();
    if (errors.size() > 0) {
      log.debug(errors);
    }
    assertEquals("workbook has no errors", 0, errors.size());
    return library;
  }
  
  /**
   * Test that no SD record for a well still gives an empty well.
   */
  public void doTestEmptyWellIsEmpty()
  {
    Well well = librariesDao.findWell(new WellKey(1534, "A23"));
    assertNotNull(well);
    assertEquals(well.getWellType(), WellType.EMPTY);
  }

  /**
   * Test that an SD record with an empty molfile works, and that it appropriately sets the following attributes:
   * <ul>
   * <li>well.wellType
   * <li>well.reagent
   * <li>well.iccbNumber
   * </ul>
   */
  public void doTestSDRecordWithEmptyMolfile()
  {
    Well well = librariesDao.findWell(new WellKey(1534, "A01"));
    assertNotNull(well);
    assertEquals(well.getWellType(), WellType.EXPERIMENTAL);
    assertEquals(well.getReagent().getReagentId(), new ReagentVendorIdentifier("Biomol-TimTec", "SPL000058"));
    assertEquals(well.getIccbNumber(), "ICCB-00589081");
  }

  /**
   * Test that non-structural attributes for compound and well are appropriately set when the molfile is present:
   * <ul>
   * <li>well.wellType
   * <li>well.reagent
   * <li>well.iccbNumber, via column headers: ICCB_NUM; ICCB_Num
   * <li>compound.compoundName, via column headers: CompoundName; compound_identifier; ChemicalName; Chemical_Name
   * <li>compound.casNumber, via column headers: CAS_Number; CAS_number
   * </ul>
   */
  public void doTestNonstructuralCompoundAttributes()
  {
    // ICCB_NUM; CompoundName; CAS_Number
    Well well = librariesDao.findWell(new WellKey(1534, "A02"));
    assertNotNull(well);
    assertEquals(well.getWellType(), WellType.EXPERIMENTAL);
    assertEquals(well.getReagent().getReagentId(), new ReagentVendorIdentifier("Biomol-TimTec", "ST001215"));
    assertEquals(well.getIccbNumber(), "ICCB-00589082");
    Set<Compound> compounds = well.getCompounds();
    assertEquals("well has one compound", 1, compounds.size());
    Compound compound = compounds.iterator().next();
    Set<String> compoundNames = compound.getCompoundNames();
    assertEquals("compound has one compound name", 1, compoundNames.size());
    String compoundName = compoundNames.iterator().next();
    assertEquals("fake compound name 1", compoundName);
    Set<String> casNumbers = compound.getCasNumbers();
    assertEquals("compound has one cas number", 1, casNumbers.size());
    String casNumber = casNumbers.iterator().next();
    assertEquals("fake cas number 1", casNumber);
    
    // ICCB_Num; compound_identifier; CAS_number
    well = librariesDao.findWell(new WellKey(1534, "A03"));
    compounds = well.getCompounds();
    assertEquals("well has one compound", 1, compounds.size());
    compound = compounds.iterator().next();
    compoundNames = compound.getCompoundNames();
    assertEquals("compound has one compound name", 1, compoundNames.size());
    compoundName = compoundNames.iterator().next();
    assertEquals("fake compound name 2", compoundName);
    casNumbers = compound.getCasNumbers();
    assertEquals("compound has one cas number", 1, casNumbers.size());
    casNumber = casNumbers.iterator().next();
    assertEquals("fake cas number 2", casNumber);
    
    // ChemicalName
    well = librariesDao.findWell(new WellKey(1534, "A04"));
    compounds = well.getCompounds();
    assertEquals("well has one compound", 1, compounds.size());
    compound = compounds.iterator().next();
    compoundNames = compound.getCompoundNames();
    assertEquals("compound has one compound name", 1, compoundNames.size());
    compoundName = compoundNames.iterator().next();
    assertEquals("fake compound name 3", compoundName);
    
    // Chemical_Name
    well = librariesDao.findWell(new WellKey(1534, "A05"));
    compounds = well.getCompounds();
    assertEquals("well has one compound", 1, compounds.size());
    compound = compounds.iterator().next();
    compoundNames = compound.getCompoundNames();
    assertEquals("compound has one compound name", 1, compoundNames.size());
    compoundName = compoundNames.iterator().next();
    assertEquals("fake compound name 4", compoundName);
  }

  /**
   * Test that structural attributes for compound and well are appropriately set when the molfile is present:
   * <ul>
   * <li>compound.smiles
   * <li>compound.inchi
   * <li>compound.pubchemCids
   * </ul>
   */
  public void doTestStructuralCompoundAttributes()
  {
    Well well = librariesDao.findWell(new WellKey(1534, "A08"));
    Set<Compound> compounds = well.getCompounds();
    assertEquals("well has one compound", 1, compounds.size());
    Compound compound = compounds.iterator().next();
    assertEquals("compound has smiles", "O=C1CC(C)(C)CC(=O)C1C(c1ccccc1)C1=C(O)CC(C)(C)CC1=O", compound.getSmiles());
    assertEquals("compound has inchi", "InChI=1/C23H28O4/c1-22(2)10-15(24)20(16(25)11-22)19(14-8-6-5-7-9-14)21-17(26)12-23(3,4)13-18(21)27/h5-9,19-20,26H,10-13H2,1-4H3", compound.getInchi());
    Set<String> pubchemCids = compound.getPubchemCids();
    assertEquals("compound has 1 pubchem cid", 1, pubchemCids.size());
    String pubchemCid = pubchemCids.iterator().next();
    assertEquals("compound has pubchem cid", "558309", pubchemCid);
  }
  
  public void doTestTwoCompoundsInWell()
  {
    Well well = librariesDao.findWell(new WellKey(1534, "A06"));
    Set<Compound> compounds = well.getCompounds();
    assertEquals("well has two compounds", 2, compounds.size());
    Compound primaryCompound = well.getPrimaryCompound();
    Iterator<Compound> compoundIterator = compounds.iterator();
    Compound oneCompound = compoundIterator.next();
    Compound anotherCompound = compoundIterator.next();
    Compound nonPrimaryCompound;
    if (primaryCompound.equals(oneCompound)) {
      nonPrimaryCompound = anotherCompound;
    }
    else {
      nonPrimaryCompound = oneCompound;
    }

    assertEquals("primary compound has smiles", "CC(=C)C[N+](C)(C)Cc1c(C)ccc2ccccc12", primaryCompound.getSmiles());
    assertEquals("primary compound has inchi", "InChI=1/C18H24N/c1-14(2)12-19(4,5)13-18-15(3)10-11-16-8-6-7-9-17(16)18/h6-11H,1,12-13H2,2-5H3/q+1", primaryCompound.getInchi());
    Set<String> pubchemCids = primaryCompound.getPubchemCids();
    assertEquals("primary compound has 1 pubchem cid", 1, pubchemCids.size());
    String pubchemCid = pubchemCids.iterator().next();
    assertEquals("compound has pubchem cid", "4560856", pubchemCid);

    assertEquals("non primary compound has smiles", "[Br-]", nonPrimaryCompound.getSmiles());
    assertEquals("non primary compound has inchi", "InChI=1/BrH/h1H/p-1", nonPrimaryCompound.getInchi());
    pubchemCids = primaryCompound.getPubchemCids();
    assertEquals("non primary compound has 1 pubchem cid", 1, pubchemCids.size());
    pubchemCid = pubchemCids.iterator().next();
    assertEquals("ncompound has pubchem cid", "4560856", pubchemCid);
  }
}
