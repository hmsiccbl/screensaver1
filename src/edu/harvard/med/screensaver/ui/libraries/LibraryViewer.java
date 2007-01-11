// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/ui/libraries/LibraryViewer.java $
// $Id: LibraryViewer.java 711 2006-10-31 23:40:24Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.control.LibrariesController;
import edu.harvard.med.screensaver.ui.control.UIControllerMethod;
import edu.harvard.med.screensaver.ui.searchresults.LibrarySearchResults;

public class LibraryViewer extends AbstractBackingBean
{
  private static Logger log = Logger.getLogger(LibraryViewer.class);
  
  
  // private instance methods
  
  private Library _library;
  private int _librarySize;
  private LibrarySearchResults _librarySearchResults;
  private LibrariesController _librariesController;
  

  // public getters and setters
  
  public void setLibrary(Library library)
  {
    _library = library;
  }

  public Library getLibrary()
  {
    return _library;
  }

  public LibrarySearchResults getLibrarySearchResults()
  {
    return _librarySearchResults;
  }

  public void setLibrarySearchResults(LibrarySearchResults librarySearchResults)
  {
    _librarySearchResults = librarySearchResults;
  }

  public LibrariesController getLibrariesController()
  {
    return _librariesController;
  }
  
  public void setLibrariesController(LibrariesController librariesController)
  {
    _librariesController = librariesController;
  }
  
  public boolean getIsRNAiLibrary()
  {
    return _library != null && _library.getScreenType().equals(ScreenType.RNAI);
  }

  public boolean getIsCompoundLibrary()
  {
    return _library != null && _library.getScreenType().equals(ScreenType.SMALL_MOLECULE);
  }
  
  public int getLibrarySize()
  {
    // note: do not call _library.getWells().size(), as this is very expensive, as it loads all wells
    return _librarySize;
  }

  public void setLibrarySize(int librarySize)
  {
    _librarySize = librarySize;
  }

  @UIControllerMethod
  public String importCompoundLibraryContents()
  {
    return _librariesController.importCompoundLibraryContents(_library);
  }

  @UIControllerMethod
  public String importRNAiLibraryContents()
  {
    return _librariesController.importRNAiLibraryContents(_library);
  }
  
  @UIControllerMethod
  public String viewLibraryContents()
  {
    return _librariesController.viewLibraryContents(_library);
  }

}
