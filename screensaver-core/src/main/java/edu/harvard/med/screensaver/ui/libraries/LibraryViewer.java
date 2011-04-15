// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.Activity;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryContentsVersion;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.service.libraries.LibraryContentsVersionManager;
import edu.harvard.med.screensaver.ui.arch.view.SearchResultContextEntityViewerBackingBean;
import edu.harvard.med.screensaver.ui.arch.view.aspects.UICommand;

/**
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class LibraryViewer extends SearchResultContextEntityViewerBackingBean<Library,Library>
{
  private static Logger log = Logger.getLogger(LibraryViewer.class);

  private LibrariesDAO _librariesDao;
  private WellSearchResults _wellsBrowser;
  private WellCopyVolumeSearchResults _wellCopyVolumesBrowser;
  private LibraryContentsVersionManager _libraryContentsVersionManager;
  private LibraryContentsImporter _libraryContentsImporter;
  private LibraryDetailViewer _libraryDetailViewer;
  private LibraryCopySearchResults _libraryCopiesBrowser;
  private LibraryCopyDetail _libraryCopyDetail;
  private LibraryCopyPlateSearchResults _libraryCopyPlateSearchResults;
  private LibraryCopyPlateCommentSearchResults _libraryCopyPlateCommentSearchResults;

  private DataModel _contentsVersionsDataModel;


  /**
   * @motivation for CGLIB2
   */
  protected LibraryViewer()
  {
  }

  public LibraryViewer(LibraryViewer thisProxy,
                       LibrarySearchResults librarySearchResults,
                       GenericEntityDAO dao,
                       LibrariesDAO librariesDao,
                       WellSearchResults wellsBrowser,
                       WellCopyVolumeSearchResults wellCopyVolumesBrowser,
                       LibraryContentsImporter libraryContentsImporter,
                       LibraryDetailViewer libraryDetailViewer,
                       LibraryContentsVersionManager libraryContentsVersionManager,
                       LibraryCopySearchResults libraryCopiesBrowser,
                       LibraryCopyDetail libraryCopyDetail,
                       LibraryCopyPlateSearchResults libraryCopyPlateSearchResults,
                       LibraryCopyPlateCommentSearchResults libraryCopyPlateCommentSearchResults)
  {
    super(thisProxy,
          Library.class,
          BROWSE_LIBRARIES,
          VIEW_LIBRARY,
          dao,
          librarySearchResults);
    _librariesDao = librariesDao;
    _wellsBrowser = wellsBrowser;
    _wellCopyVolumesBrowser = wellCopyVolumesBrowser;
    _libraryContentsImporter = libraryContentsImporter;
    _libraryDetailViewer = libraryDetailViewer;
    _libraryContentsVersionManager = libraryContentsVersionManager;
    _libraryCopiesBrowser = libraryCopiesBrowser;
    _libraryCopyDetail = libraryCopyDetail;
    _libraryCopyPlateSearchResults = libraryCopyPlateSearchResults;
    _libraryCopyPlateSearchResults.setNestedIn(this);
    _libraryCopyPlateCommentSearchResults = libraryCopyPlateCommentSearchResults;
    getIsPanelCollapsedMap().put("copies", true);
    getIsPanelCollapsedMap().put("plateComments", true);
    getIsPanelCollapsedMap().put("plates", true);
    getIsPanelCollapsedMap().put("contentsVersions", true);
  }


  @Override
  protected void initializeEntity(Library library)
  {
    getDao().needReadOnly(library, 
                          Library.contentsVersions.to(LibraryContentsVersion.loadingActivity).to(Activity.performedBy));
    getDao().needReadOnly(library,
                          Library.contentsVersions.to(LibraryContentsVersion.releaseActivity).to(Activity.performedBy));
    getDao().needReadOnly(library,
                          Library.copies);
  }
  
  @Override
  protected void initializeViewer(Library library)
  {
    _contentsVersionsDataModel = null;
    _libraryDetailViewer.setEntity(library);
    _libraryCopiesBrowser.searchCopiesByLibrary(library);
    getLibraryCopyPlateSearchResults().searchPlatesForLibrary(library);
    getLibraryCopyPlateCommentSearchResults().searchForLibrary(library);
  }

  public DataModel getContentsVersionsDataModel()
  {
    if (_contentsVersionsDataModel == null) {
      _contentsVersionsDataModel = new ListDataModel(Lists.newArrayList(Iterables.reverse(Lists.newArrayList(getEntity().getContentsVersions()))));
    }
    return _contentsVersionsDataModel;
  }
  
  public LibraryCopySearchResults getCopiesBrowser()
  {
    return _libraryCopiesBrowser;
  }
  
  @UICommand
  public String browseLibraryContentsVersionWells()
  {
    LibraryContentsVersion lcv = (LibraryContentsVersion) getRequestMap().get("lcv");
    _wellsBrowser.searchWellsForLibraryContentsVersion(lcv);
    return BROWSE_WELLS;
  }

  @UICommand
  public String deleteLibraryContentsVersion()
  {
    LibraryContentsVersion lcv = (LibraryContentsVersion) getRequestMap().get("lcv");
    _librariesDao.deleteLibraryContentsVersion(lcv);
    return getThisProxy().reload();
  }

  @UICommand
  public String releaseLibraryContentsVersion()
  {
    _libraryContentsVersionManager.releaseLibraryContentsVersion((LibraryContentsVersion) getRequestMap().get("lcv"),
                                                                  (AdministratorUser) getScreensaverUser());
    return getThisProxy().reload();
  }

  @UICommand
  public String viewLibraryContents()
  {
    _wellsBrowser.searchWellsForLibrary(getEntity());
    return BROWSE_WELLS;
  }

  @UICommand
  public String viewLibraryWellCopyVolumes()
  {
    _wellCopyVolumesBrowser.searchWellsForLibrary(getEntity());
    return BROWSE_WELL_VOLUMES;
  }

  @UICommand
  public String addLibraryCopy()
  {
    return _libraryCopyDetail.editNewEntity(new Copy((AdministratorUser) getScreensaverUser(),
                                                     getEntity()));
  }

  @UICommand
  public String viewLibraryContentsImporter()
  {
    if (getEntity() != null) {
      return _libraryContentsImporter.viewLibraryContentsImporter(getEntity());
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  // TODO: move to LibraryDetailViewer.delete(), and can then eliminate explicit delete button in UI, since editSaveCancel.xhtml will provide it
  @UICommand
  public String deleteLibrary()
  {
    try {
      getDao().deleteEntity(getEntity());
      showMessage("deletedEntity", getEntity().getLibraryName());
      getContextualSearchResults().reload();
      return BROWSE_LIBRARIES;
    } catch (DataIntegrityViolationException e) {
      showMessage("cannotDeleteEntityInUse", getEntity().getLibraryName());
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
  }

  public LibraryCopyPlateSearchResults getLibraryCopyPlateSearchResults()
  {
    return _libraryCopyPlateSearchResults;
  }

  public LibraryCopyPlateCommentSearchResults getLibraryCopyPlateCommentSearchResults()
  {
    return _libraryCopyPlateCommentSearchResults;
  }
}
