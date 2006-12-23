// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries.rnai;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.SchemaUtil;
import edu.harvard.med.screensaver.io.workbook.ParseError;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screens.ScreenType;


public class RNAiLibraryContentsParserTest extends AbstractSpringTest
{

  // static fields
  
  private static final Logger log = Logger.getLogger(RNAiLibraryContentsParserTest.class);
  private static final File TEST_INPUT_FILE_DIR =
    new File("test/edu/harvard/med/screensaver/io/libraries/rnai");
  
  
  // instance fields
  
  protected RNAiLibraryContentsParser rnaiLibraryContentsParser;
  protected DAO dao;
  protected SchemaUtil schemaUtil;
  
  
  // constructor and test methods
  
  protected void onSetUp() throws Exception
  {
    super.onSetUp();
    schemaUtil.truncateTablesOrCreateSchema();
  }
  
  public void testColumnHeaderErrors()
  {
    Library library = new Library(
      "Human1",
      "Human1",
      ScreenType.SMALL_MOLECULE,
      LibraryType.SIRNA,
      50001,
      50001);
    String filename = "column header errors.xls";
    File file = new File(TEST_INPUT_FILE_DIR, filename);
    InputStream stream = null;
    try {
      stream = new FileInputStream(file);
    }
    catch (FileNotFoundException e) {
      fail("file not found: " + filename);
    }
    library = rnaiLibraryContentsParser.parseLibraryContents(library, file, stream);
    List<ParseError> errors = rnaiLibraryContentsParser.getErrors();
    assertEquals("workbook has 5 errors", 5, errors.size());
    ParseError error;
    
    // error 0
    error = errors.get(0);
    assertEquals(
      "error text for error 0",
      "required column \"Plate\" matches multiple column headers in the same sheet",
      error.getMessage());
    assertNotNull("error 0 has cell", error.getCell());
    assertEquals(
      "cell for error 0",
      "duplicate plate:(C,1)",
      error.getCell().toString());
  
    // error 1
    error = errors.get(1);
    assertEquals(
      "error text for error 1",
      "couldn't import sheet contents due to problems with column headers: duplicate plate",
      error.getMessage());
    assertNull("no cell for error 1", error.getCell());
  
    // error 2
    error = errors.get(2);
    assertEquals(
      "error text for error 2",
      "required column \"Plate\" does not match any column headers in sheet: missing plate",
      error.getMessage());
    assertNull("no cell for error 2", error.getCell());
  
    // error 3
    error = errors.get(3);
    assertEquals(
      "error text for error 3",
      "couldn't import sheet contents due to problems with column headers: missing plate",
      error.getMessage());
    assertNull("no cell for error 3", error.getCell());
  
    // error 4
    error = errors.get(4);
    assertEquals(
      "error text for error 4",
      "ecountered a sheet without any rows: empty sheet",
      error.getMessage());
    assertNull("no cell for error 4", error.getCell());
    
    // if any minor changes in the error formatting break this test, you can uncomment this code,
    // see what it prints, and correct the hardcoded tests above:
    // for (ParseError error1 : errors) {
    //   log.info("error: " + error1.getMessage());
    //   log.info("cell:  " + error1.getCell());
    // }
  }

  public void testDataRowErrors()
  {
    Library library = new Library(
      "Human1",
      "Human1",
      ScreenType.SMALL_MOLECULE,
      LibraryType.SIRNA,
      50001,
      5003);
    String filename = "data row errors.xls";
    File file = new File(TEST_INPUT_FILE_DIR, filename);
    InputStream stream = null;
    try {
      stream = new FileInputStream(file);
    }
    catch (FileNotFoundException e) {
      fail("file not found: " + filename);
    }
    library = rnaiLibraryContentsParser.parseLibraryContents(library, file, stream);
    List<ParseError> errors = rnaiLibraryContentsParser.getErrors();
    assertEquals("workbook has 9 errors", 9, errors.size());
    assertEquals("library has no wells", 0, library.getWells().size());
    ParseError error;
    
    for (ParseError error1 : errors) {
      log.info("error: " + error1.getMessage());
      log.info("cell:  " + error1.getCell());
    }
    
    // error 0
    error = errors.get(0);
    assertEquals(
      "error text for error 0",
      "unparseable plate number '50001-zappa'",
      error.getMessage());
    assertNotNull("error 0 has cell", error.getCell());
    assertEquals(
      "cell for error 0",
      "Human Kinases:(A,2)",
      error.getCell().toString());

    // error 1
    error = errors.get(1);
    assertEquals(
      "error text for error 1",
      "unparseable plate number ''",
      error.getMessage());
    assertNotNull("error 1 has cell", error.getCell());
    assertEquals(
      "cell for error 1",
      "data row errors.xls:Human Kinases:(A,3)",
      error.getCell().toString());

    // error 2
    error = errors.get(2);
    assertEquals(
      "error text for error 2",
      "unparseable well name 'A09-zappa'",
      error.getMessage());
    assertNotNull("error 2 has cell", error.getCell());
    assertEquals(
      "cell for error 2",
      "data row errors.xls:Human Kinases:(B,4)",
      error.getCell().toString());

    // error 3
    error = errors.get(3);
    assertEquals(
      "error text for error 3",
      "well name cell is empty",
      error.getMessage());
    assertNotNull("error 3 has cell", error.getCell());
    assertEquals(
      "cell for error 3",
      "data row errors.xls:Human Kinases:(B,5)",
      error.getCell().toString());

    // error 4
    error = errors.get(4);
    assertEquals(
      "error text for error 4",
      "value required",
      error.getMessage());
    assertNotNull("error 4 has cell", error.getCell());
    assertEquals(
      "cell for error 4",
      "data row errors.xls:Human Kinases:(F,6)",
      error.getCell().toString());

    // error 5
    error = errors.get(5);
    assertEquals(
      "error text for error 5",
      "value required",
      error.getMessage());
    assertNotNull("error 5 has cell", error.getCell());
    assertEquals(
      "cell for error 5",
      "data row errors.xls:Human Kinases:(G,7)",
      error.getCell().toString());

    // error 6
    error = errors.get(6);
    assertEquals(
      "error text for error 6",
      "value required",
      error.getMessage());
    assertNotNull("error 6 has cell", error.getCell());
    assertEquals(
      "cell for error 6",
      "data row errors.xls:Human Kinases:(H,8)",
      error.getCell().toString());

    // error 7
    error = errors.get(7);
    assertEquals(
      "error text for error 7",
      "NCBI EFetch did not return Description for 99999999",
      error.getMessage());
    assertNotNull("error 7 has cell", error.getCell());
    assertEquals(
      "cell for error 7",
      "data row errors.xls:Human Kinases:(H,9)",
      error.getCell().toString());

    // error 8
    error = errors.get(8);
    assertEquals(
      "error text for error 8",
      "NCBI EFetch did not return Orgname for 99999999",
      error.getMessage());
    assertNotNull("error 8 has cell", error.getCell());
    assertEquals(
      "cell for error 8",
      "data row errors.xls:Human Kinases:(H,9)",
      error.getCell().toString());
  }

  public void testCleanData()
  {
    Library library = new Library(
      "Human1",
      "Human1",
      ScreenType.SMALL_MOLECULE,
      LibraryType.SIRNA,
      50001,
      50001);
    String filename = "clean data.xls";
    File file = new File(TEST_INPUT_FILE_DIR, filename);
    InputStream stream = null;
    try {
      stream = new FileInputStream(file);
    }
    catch (FileNotFoundException e) {
      fail("file not found: " + filename);
    }
    library = rnaiLibraryContentsParser.parseLibraryContents(library, file, stream);

    List<ParseError> errors = rnaiLibraryContentsParser.getErrors();
    assertEquals("workbook has no errors", 0, errors.size());
    assertEquals("library has all wells for all plates", 
                 384, 
                 library.getWells().size());

    Well a05 = null, a07 = null, a09 = null, a11 = null, a15 = null;
    for (Well well: library.getWells()) {
      String wellName = well.getWellName();
      log.info("well name " + wellName);
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
      }
    }
      
    assertNotNull("library has well A05", a05);
    assertNotNull("library has well A07", a07);
    assertNotNull("library has well A09", a09);
    assertNotNull("library has well A11", a11);
    assertNotNull("library has well A15", a15);
      
    assertNull("well A11 has no vendor id", a11.getVendorIdentifier());
    assertEquals("well A05 vendor id", "M-005300-00", a05.getVendorIdentifier());
    assertNull("well A07 has no vendor id", a07.getVendorIdentifier());
    assertEquals("well A09 vendor id", "M-004061-00", a09.getVendorIdentifier());
    assertNull("well A11 has no vendor id", a11.getVendorIdentifier());
    assertEquals("well A15 vendor id", "M-003256-05", a15.getVendorIdentifier());
    
    // silencing reagents and genes for A05 and A09
    assertEquals("well A05 has 4 silencing reagents", 4, a05.getSilencingReagents().size());
    for (SilencingReagent a05reagent: a05.getSilencingReagents()) {
      assertEquals("a05 silencing reagent type",
        SilencingReagentType.SIRNA,
        a05reagent.getSilencingReagentType());
      // TODO: test the SilencingReagent.sequence more
      assertEquals("a05 silencing reagent sequence length", 19, a05reagent.getSequence().length());
      
      Gene a05gene = a05reagent.getGene();
      assertEquals("a05 gene symbol",     "AAK1",                    a05gene.getEntrezgeneSymbol());
      assertEquals("a05 gene id",         new Integer(22848),        a05gene.getEntrezgeneId());
      assertEquals("a05 gene name",       "AP2 associated kinase 1", a05gene.getGeneName());
      assertEquals("a05 species name",    "Homo sapiens",            a05gene.getSpeciesName());

      assertEquals("a05 accession count", 1,                         a05gene.getGenbankAccessionNumbers().size());
      assertEquals("a05 accession",       "NM_014911",               a05gene.getGenbankAccessionNumbers().iterator().next());
    }
    
    assertEquals("well A09 has 1 silencing reagent", 1, a09.getSilencingReagents().size());
    SilencingReagent a09reagent = a09.getSilencingReagents().iterator().next();
    assertTrue("a09 is pool of unknown sequences",
      a09reagent.isPoolOfUnknownSequences());
    assertEquals("a09 silencing reagent sequence",
      "",
      a09reagent.getSequence());
    Gene a09gene = a09reagent.getGene();
    assertEquals("a09 gene symbol", "CERK", a09gene.getEntrezgeneSymbol());
  }
  
  /**
   * test that previously loaded wells, silencing reagents, and genes are appropriately reused.
   * test:
   * <ol>
   * <li> duplicate well/sr/gene
   * <li> duplicate well/gene, new sr
   * <li> duplicate well, new sr/gene
   * <li> new well, duplicate sr/gene
   * <li> new well/sr, duplicate gene
   * </ol>
  */
  public void testUseOfExistingEntities()
  {
    dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Library library = new Library(
          "Human1",
          "Human1",
          ScreenType.SMALL_MOLECULE,
          LibraryType.SIRNA,
          50001,
          50001);
        dao.persistEntity(library);
  
        // parse the first spreadsheet
        String filename = "existing entities 1.xls";
        File file = new File(TEST_INPUT_FILE_DIR, filename);
        InputStream stream = null;
        try {
          stream = new FileInputStream(file);
        }
        catch (FileNotFoundException e) {
          fail("file not found: " + filename);
        }
        library = rnaiLibraryContentsParser.parseLibraryContents(library, file, stream);
        List<ParseError> errors = rnaiLibraryContentsParser.getErrors();
        assertEquals("workbook has no errors", 0, errors.size());
        assertEquals("library has all wells", 384, library.getWells().size());
  
        // persist the new well/sr/genes, so the next
        dao.persistEntity(library);
  
        // parse the second spreadsheet
        filename = "existing entities 2.xls";
        file = new File(TEST_INPUT_FILE_DIR, filename);
        stream = null;
        try {
          stream = new FileInputStream(file);
        }
        catch (FileNotFoundException e) {
          fail("file not found: " + filename);
        }
        library = rnaiLibraryContentsParser.parseLibraryContents(library, file, stream);
        errors = rnaiLibraryContentsParser.getErrors();
        assertEquals("workbook has no errors", 0, errors.size());
        assertEquals("library has all wells", 384, library.getWells().size());
  
        // test the overlaps
        Well a05 = null, a07 = null, a09 = null, a11 = null, a13 = null, a15 = null, a17 = null;
        for (Well well: library.getWells()) {
          String wellName = well.getWellName();
          log.info("well name " + wellName);
          if (wellName.equals("A05"))      { a05 = well; }
          else if (wellName.equals("A07")) { a07 = well; }
          else if (wellName.equals("A09")) { a09 = well; }
          else if (wellName.equals("A11")) { a11 = well; }
          else if (wellName.equals("A13")) { a13 = well; }
          else if (wellName.equals("A15")) { a15 = well; }
          else if (wellName.equals("A17")) { a17 = well; }
        }
  
        assertNotNull("library has well A05", a05);
        assertNotNull("library has well A07", a07);
        assertNotNull("library has well A09", a09);
        assertNotNull("library has well A11", a11);
        assertNotNull("library has well A13", a13);
        assertNotNull("library has well A15", a15);
        assertNotNull("library has well A17", a17);
  
        // a05 has one sr
        assertEquals("a05 has 1 sr", 1, a05.getSilencingReagents().size());
  
        // a07 has two srs with same gene
        assertEquals("a07 has 2 srs", 2, a07.getSilencingReagents().size());
        Iterator<SilencingReagent> srs = a07.getSilencingReagents().iterator();
        SilencingReagent sr1 = srs.next();
        SilencingReagent sr2 = srs.next();
        assertSame("a07 srs have same gene", sr1.getGene(), sr2.getGene());
  
        // a09 has two srs with different genes
        assertEquals("a09 has 2 srs", 2, a09.getSilencingReagents().size());
        srs = a09.getSilencingReagents().iterator();
        sr1 = srs.next();
        sr2 = srs.next();
        assertNotSame("a09 srs have different genes", sr1.getGene(), sr2.getGene());
  
        // a11 and a15 have same sr
        assertEquals("a11 has 1 sr", 1, a11.getSilencingReagents().size());
        assertEquals("a15 has 1 sr", 1, a15.getSilencingReagents().size());
        assertSame("a11 and a15 have same sr",
          a11.getSilencingReagents().iterator().next(),
          a15.getSilencingReagents().iterator().next());
  
        // a13 and 17 have different srs, same gene
        assertEquals("a13 has 1 sr", 1, a13.getSilencingReagents().size());
        assertEquals("a17 has 1 sr", 1, a17.getSilencingReagents().size());
        sr1 = a13.getSilencingReagents().iterator().next();
        sr2 = a17.getSilencingReagents().iterator().next();
        assertNotSame("a13 and a17 have different srs", sr1, sr2);
        assertSame("a13 and a17 have same gene", sr1.getGene(), sr2.getGene());
      }
    });
  }

  public void IGNORE_testHuman1()
  {
    dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Library library = new Library(
          "Human1",
          "Human1",
          ScreenType.SMALL_MOLECULE,
          LibraryType.SIRNA,
          50001,
          5003);
        String filename = "Human1.xls";
        File file = new File(TEST_INPUT_FILE_DIR, filename);
        InputStream stream = null;
        try {
          stream = new FileInputStream(file);
        }
        catch (FileNotFoundException e) {
          fail("file not found: " + filename);
        }
        library = rnaiLibraryContentsParser.parseLibraryContents(library, file, stream);
        Set<Well> wells = library.getWells();

        List<ParseError> errors = rnaiLibraryContentsParser.getErrors();
        assertEquals("workbook has no errors", 0, errors.size());

        dao.persistEntity(library);
        
        // this library has 779 wells according to
        // http://iccb.med.harvard.edu/screening/RNAi%20Libraries/index.htm
        // but add 18 for the controls - 6 controls on each of 3 plates
        assertEquals("well count in Human1", 797, wells.size());
      }
    });
  }
}
