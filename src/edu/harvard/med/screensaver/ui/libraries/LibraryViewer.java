// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.ui.namevaluetable.LibraryNameValueTable;
import edu.harvard.med.screensaver.ui.searchresults.WellSearchResults;

import org.apache.log4j.Logger;

/**
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class LibraryViewer extends AbstractBackingBean
{
  private static Logger log = Logger.getLogger(LibraryViewer.class);


  // private instance methods

  private GenericEntityDAO _dao;
  private LibrariesDAO _librariesDao;
  private WellSearchResults _wellsBrowser;
  private WellCopyVolumeSearchResults _wellCopyVolumesBrowser;

  private Library _library;
  private int _librarySize;
  private LibraryNameValueTable _libraryNameValueTable;
  private CompoundLibraryContentsImporter _compoundLibraryContentsImporter;
  private RNAiLibraryContentsImporter _rnaiLibraryContentsImporter;
  private NaturalProductsLibraryContentsImporter _naturalProductsLibraryContentsImporter;


  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected LibraryViewer()
  {
  }

  public LibraryViewer(GenericEntityDAO dao,
                       LibrariesDAO librariesDao,
                       WellSearchResults wellsBrowser,
                       WellCopyVolumeSearchResults wellCopyVolumesBrowser,
                       CompoundLibraryContentsImporter compoundLibraryContentsImporter,
                       RNAiLibraryContentsImporter rnaiLibraryContentsImporter,
                       NaturalProductsLibraryContentsImporter naturalProductsLibraryContentsImporter)
  {
    _dao = dao;
    _librariesDao = librariesDao;
    _wellsBrowser = wellsBrowser;
    _wellCopyVolumesBrowser = wellCopyVolumesBrowser;
    _compoundLibraryContentsImporter = compoundLibraryContentsImporter;
    _rnaiLibraryContentsImporter = rnaiLibraryContentsImporter;
    _naturalProductsLibraryContentsImporter = naturalProductsLibraryContentsImporter;
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

  public LibraryNameValueTable getLibraryNameValueTable()
  {
    return _libraryNameValueTable;
  }

  public void setLibraryNameValueTable(LibraryNameValueTable libraryNameValueTable)
  {
    _libraryNameValueTable = libraryNameValueTable;
  }

  @UIControllerMethod
  public String viewLibrary()
  {
    String libraryIdAsString = (String) getRequestParameter("libraryId");
    Integer libraryId = Integer.parseInt(libraryIdAsString);
    Library library = _dao.findEntityById(Library.class, libraryId);
    return viewLibrary(library);
  }

  @UIControllerMethod
  public String viewLibrary(Library library)
  {
    return viewLibrary(library, false);
  }

  @UIControllerMethod
  public String viewLibrary(final Library libraryIn, boolean showNavigationBar)
  {
    _dao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Library library = _dao.reloadEntity(libraryIn, true);
        setLibrary(library);
        setLibrarySize(
          _dao.relationshipSize(library, "wells", "wellType", "experimental"));
        setLibraryNameValueTable(new LibraryNameValueTable(library, getLibrarySize()));
      }
    });
    return VIEW_LIBRARY;
  }

  @UIControllerMethod
  public String viewLibraryContents()
  {
    _dao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
//        Library library = _dao.reloadEntity(_library, true);
//        _dao.needReadOnly(library,
//                          "wells.silencingReagents.gene.genbankAccessionNumbers",
//                          "wells.compounds");
        _wellsBrowser.searchWellsForLibrary(_library);
      }
    });

    return VIEW_WELL_SEARCH_RESULTS;
  }

  @UIControllerMethod
  public String viewLibraryWellCopyVolumes()
  {
    _wellCopyVolumesBrowser.searchWellsForLibrary(_library);
    return VIEW_WELL_VOLUME_SEARCH_RESULTS;
  }

  @UIControllerMethod
  public String viewLibraryContentsImporter()
  {
    if (_library != null) {
      if (_library.getScreenType().equals(ScreenType.RNAI)) {
        return _rnaiLibraryContentsImporter.viewRNAiLibraryContentsImporter(_library);
      }
      if (_library.getScreenType().equals(ScreenType.SMALL_MOLECULE)) {
        if (_library.getLibraryType().equals(LibraryType.NATURAL_PRODUCTS)) {
          return _naturalProductsLibraryContentsImporter.viewNaturalProductsLibraryContentsImporter(_library);

        }
        else {
          return _compoundLibraryContentsImporter.viewCompoundLibraryContentsImporter(_library);
        }
      }
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String unloadLibraryContents()
  {
    _dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Library library = _dao.reloadEntity(_library);
        _librariesDao.deleteLibraryContents(library);
      }
    });
    showMessage("libraries.unloadedLibraryContents", "libraryViewer");
    return viewLibrary(_library);
  }
}
