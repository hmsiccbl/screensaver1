// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/io/screenresults/MockLibrariesDaoForScreenResultImporter.java $
// $Id: MockLibrariesDaoForScreenResultImporter.java 1071 2007-02-14 14:26:14Z ant4 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.screenresults;


import java.util.List;
import java.util.Set;

import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellType;
import edu.harvard.med.screensaver.model.screens.ScreenType;

import org.apache.log4j.Logger;

/**
 * Enable invocation of ScreenResultParser in non-database environment.
 * 
 * @motivation for command-line ScreenResultImporter, when used in validation
 *             mode (i.e., non-import mode).
 * @motivation testing ScreenResultImporter
 * @author ant
 */
public class MockLibrariesDaoForScreenResultImporter implements LibrariesDAO
{
  private static final Logger log = Logger.getLogger(MockLibrariesDaoForScreenResultImporter.class);
  
  private static final String NAME_OF_PSEUDO_LIBRARY_FOR_IMPORT = "PseudoLibraryForScreenResultImport";

  private Library _library;

  public MockLibrariesDaoForScreenResultImporter()
  {
    _library = 
      new Library(NAME_OF_PSEUDO_LIBRARY_FOR_IMPORT,
                  NAME_OF_PSEUDO_LIBRARY_FOR_IMPORT,
                  ScreenType.OTHER,
                  LibraryType.OTHER,
                  1,
                  Integer.MAX_VALUE);
  }

  public Well findWell(WellKey wellKey)
  {
    return new Well(_library, wellKey, WellType.EXPERIMENTAL);
  }
  
  public Library findLibraryWithPlate(Integer plateNumber)
  {
    return _library;
  }
  
  public void deleteLibraryContents(Library library)
  {
  }

  public List<Library> findLibrariesDisplayedInLibrariesBrowser()
  {
    return null;
  }

  public SilencingReagent findSilencingReagent(Gene gene, SilencingReagentType silencingReagentType, String sequence)
  {
    return null;
  }

  public Set<Well> findWellsForPlate(int plate)
  {
    return null;
  }

  public void loadOrCreateWellsForLibrary(Library library)
  {
  }
}
