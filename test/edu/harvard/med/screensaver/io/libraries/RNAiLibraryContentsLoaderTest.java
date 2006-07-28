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
import java.util.Set;

import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.Well;


public class RNAiLibraryContentsLoaderTest extends AbstractSpringTest
{
  public static final File TEST_INPUT_FILE_DIR =
    new File("test/edu/harvard/med/screensaver/io/libraries");
  
  protected RNAiLibraryContentsLoader rnaiLibraryContentsLoader;

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
