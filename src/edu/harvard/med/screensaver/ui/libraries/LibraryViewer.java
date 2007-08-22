// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.control.LibrariesController;
import edu.harvard.med.screensaver.ui.namevaluetable.LibraryNameValueTable;

import org.apache.log4j.Logger;

/**
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class LibraryViewer extends AbstractBackingBean
{
  private static Logger log = Logger.getLogger(LibraryViewer.class);


  // private instance methods

  private LibrariesController _librariesController;

  private Library _library;
  private int _librarySize;
  private LibraryNameValueTable _libraryNameValueTable;


  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected LibraryViewer()
  {
  }

  public LibraryViewer(LibrariesController librariesController)
  {
    _librariesController = librariesController;
  }


  // public getters and setters

  public void setLibrary(Library library)
  {
    _library = library;
  }

  public Library getLibrary()
  {
    return _library;
  }

  @Override
  protected ScreensaverUserRole getEditableAdminRole()
  {
    return ScreensaverUserRole.LIBRARIES_ADMIN;
  }

  public boolean getIsRNAiLibrary()
  {
    return _library != null && _library.getScreenType().equals(ScreenType.RNAI);
  }

  public boolean getIsCompoundLibrary()
  {
    return _library != null && _library.getScreenType().equals(ScreenType.SMALL_MOLECULE);
  }

  public boolean getIsNaturalProductsLibrary()
  {
    return _library != null && _library.getLibraryType().equals(LibraryType.NATURAL_PRODUCTS);
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

  public LibraryNameValueTable getLibraryNameValueTable()
  {
    return _libraryNameValueTable;
  }

  public void setLibraryNameValueTable(LibraryNameValueTable libraryNameValueTable)
  {
    _libraryNameValueTable = libraryNameValueTable;
  }

  public String viewLibraryContents()
  {
    return _librariesController.viewLibraryContents(_library);
  }

  public String viewLibraryWellVolumes()
  {
    return _librariesController.viewLibraryWellCopyVolumes(_library);
  }

  public String importCompoundLibraryContents()
  {
    return _librariesController.importCompoundLibraryContents(_library);
  }

  public String importNaturalProductsLibraryContents()
  {
    return _librariesController.importNaturalProductsLibraryContents(_library);
  }

  public String importRNAiLibraryContents()
  {
    return _librariesController.importRNAiLibraryContents(_library);
  }

  public String unloadLibraryContents()
  {
    return _librariesController.unloadLibraryContents(_library);
  }
}
