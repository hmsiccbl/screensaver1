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

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.Activity;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryContentsVersion;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.service.libraries.LibraryContentsVersionManager;
import edu.harvard.med.screensaver.ui.SearchResultContextEntityViewerBackingBean;
import edu.harvard.med.screensaver.ui.UICommand;
import edu.harvard.med.screensaver.ui.searchresults.LibrarySearchResults;
import edu.harvard.med.screensaver.ui.searchresults.WellSearchResults;

import org.apache.log4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class LibraryViewer extends SearchResultContextEntityViewerBackingBean<Library>
{
  private static Logger log = Logger.getLogger(LibraryViewer.class);

  private LibrariesDAO _librariesDao;
  private WellSearchResults _wellsBrowser;
  private WellCopyVolumeSearchResults _wellCopyVolumesBrowser;
  private LibraryContentsVersionManager _libraryContentsVersionManager;
  private LibraryContentsImporter _libraryContentsImporter;
  private LibraryDetailViewer _libraryDetailViewer;

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
                       LibraryContentsVersionManager libraryContentsVersionManager)
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
    getIsPanelCollapsedMap().put("contentsVersions", Boolean.TRUE);
  }


  @Override
  protected void initializeEntity(Library library)
  {
    getDao().needReadOnly(library, 
                          Library.contentsVersions.to(LibraryContentsVersion.loadingActivity).to(Activity.performedBy).getPath(),
                          Library.contentsVersions.to(LibraryContentsVersion.releaseActivity).to(Activity.performedBy).getPath());
  }
  
  @Override
  protected void initializeViewer(Library library)
  {
    _contentsVersionsDataModel = null;
    _libraryDetailViewer.setEntity(library);
  }

  public DataModel getContentsVersionsDataModel()
  {
    if (_contentsVersionsDataModel == null) {
      _contentsVersionsDataModel = new ListDataModel(Lists.newArrayList(Iterables.reverse(Lists.newArrayList(getEntity().getContentsVersions()))));
    }
    return _contentsVersionsDataModel;
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
  public String viewLibraryContentsImporter()
  {
    if (getEntity() != null) {
      return _libraryContentsImporter.viewLibraryContentsImporter(getEntity());
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UICommand
  public String deleteLibrary()
  {
    try {
      getDao().doInTransaction(new DAOTransaction() {
        public void runTransaction() {

          getDao().deleteEntity(getEntity());
          getDao().flush();

        }
      });
      showMessage("libraries.deletedLibrary", "libraryViewer");
      
      getContextualSearchResults().refetch();
      return BROWSE_LIBRARIES;
    } catch (Exception e) {
      if (e instanceof DataIntegrityViolationException) {
        showMessage("libraries.libraryDeletionFailed", getEntity().getLibraryName(), "Please check that no screen results are associated with the library.");
      }
      else {
        showMessage("libraries.libraryDeletionFailed", e.getClass().getName(), e.getMessage());
      }
      log.warn("library deletion: " + getEntity().getLibraryName() + e.getMessage() );
      return VIEW_MAIN;
    }
  }
}
