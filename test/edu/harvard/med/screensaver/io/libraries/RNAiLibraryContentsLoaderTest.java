// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.io.workbook.ParseError;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.Well;


public class RNAiLibraryContentsLoaderTest extends AbstractSpringTest
{
  public static final Logger log = Logger.getLogger(RNAiLibraryContentsLoaderTest.class);
  
  public static final File TEST_INPUT_FILE_DIR =
    new File("test/edu/harvard/med/screensaver/io/libraries");
  
  protected RNAiLibraryContentsLoader rnaiLibraryContentsLoader;

  public void testColumnHeaderErrors()
  {
    Library library = new Library("Human1", "Human1", LibraryType.RNAI, 50001, 5003);
    String filename = "column header errors.xls";
    File file = new File(TEST_INPUT_FILE_DIR, filename);
    InputStream stream = null;
    try {
      stream = new FileInputStream(file);
    }
    catch (FileNotFoundException e) {
      fail("file not found: " + filename);
    }
    library = rnaiLibraryContentsLoader.loadLibraryContents(library, file, stream);
    List<ParseError> errors = rnaiLibraryContentsLoader.getErrors();
    assertEquals("workbook has 5 errors", 5, errors.size());
    ParseError error;
    
    // error 0
    error = errors.get(0);
    assertEquals(
      "error text for error 0",
      "required column \"Plate\" matches multiple column headers in the same sheet (columns 0 and 2)",
      error.getMessage());
    assertNotNull("no cell for error 0", error.getCell());
    assertEquals(
      "cell for error 0",
      "column header errors.xls:duplicate plate:(C,1)",
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
      "required column \"Plate\" does not match any column headers in sheet",
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

  public void testHuman1()
  {
    Library library = new Library("Human1", "Human1", LibraryType.RNAI, 50001, 5003);
    String filename = "Human1.xls";
    File file = new File(TEST_INPUT_FILE_DIR, filename);
    InputStream stream = null;
    try {
      stream = new FileInputStream(file);
    }
    catch (FileNotFoundException e) {
      fail("file not found: " + filename);
    }
    library = rnaiLibraryContentsLoader.loadLibraryContents(library, file, stream);
    Set<Well> wells = library.getWells();
    
    // this library has 779 wells according to
    // http://iccb.med.harvard.edu/screening/RNAi%20Libraries/index.htm
    assertEquals("well count in Human1", 779, wells.size());
  }
}
