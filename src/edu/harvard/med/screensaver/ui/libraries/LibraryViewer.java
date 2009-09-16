// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.util.Map;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.Activity;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryContentsVersion;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.service.libraries.LibraryContentsVersionManager;
import edu.harvard.med.screensaver.ui.AbstractEditableBackingBean;
import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.ui.searchresults.LibrarySearchResults;
import edu.harvard.med.screensaver.ui.searchresults.WellSearchResults;

import org.apache.log4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class LibraryViewer extends AbstractEditableBackingBean
{
  private static Logger log = Logger.getLogger(LibraryViewer.class);


  // private instance methods

  private LibraryViewer _thisProxy;
  private GenericEntityDAO _dao;
  private LibrariesDAO _librariesDao;
  private WellSearchResults _wellsBrowser;
  private WellCopyVolumeSearchResults _wellCopyVolumesBrowser;
  private LibraryContentsVersionManager _libraryContentsVersionManager;

  private Library _library;
  private LibraryContentsImporter _libraryContentsImporter;
  private LibraryDetailViewer _libraryDetailViewer;
  private LibrarySearchResults _librariesBrowser;

  private int _uploadStatus;
  private Map<String,Boolean> _isPanelCollapsedMap;
  private DataModel _contentsVersionsDataModel;



  /**
   * @motivation for CGLIB2
   */
  protected LibraryViewer()
  {
  }

  public LibraryViewer(LibraryViewer thisProxy,
                       GenericEntityDAO dao,
                       LibrariesDAO librariesDao,
                       WellSearchResults wellsBrowser,
                       WellCopyVolumeSearchResults wellCopyVolumesBrowser,
                       LibraryContentsImporter libraryContentsImporter,
                       LibraryDetailViewer libraryDetailViewer,
                       LibrarySearchResults librarySearchResults,
                       LibraryContentsVersionManager libraryContentsVersionManager)
  {
    super(ScreensaverUserRole.LIBRARIES_ADMIN);
    _thisProxy = thisProxy;
    _dao = dao;
    _librariesDao = librariesDao;
    _wellsBrowser = wellsBrowser;
    _wellCopyVolumesBrowser = wellCopyVolumesBrowser;
    _libraryContentsImporter = libraryContentsImporter;
    _libraryDetailViewer = libraryDetailViewer;
    _librariesBrowser = librarySearchResults;
    _libraryContentsVersionManager = libraryContentsVersionManager;
    _isPanelCollapsedMap = Maps.newHashMap();
    _isPanelCollapsedMap.put("contentsVersions", Boolean.TRUE);
  }


  // public getters and setters

  public AbstractEntity getEntity()
  {
    return getLibrary();
  }

  public void setLibrary(Library library)
  {
    _library = library;
    _libraryDetailViewer.setLibrary(library);
  }

  public Library getLibrary()
  {
    return _library;
  }

  public Map getIsPanelCollapsedMap()
  {
    return _isPanelCollapsedMap;
  }


  @UIControllerMethod
  public String viewLibrary()
  {
    String libraryIdAsString = (String) getRequestParameter("entityId");
    Integer libraryId = Integer.parseInt(libraryIdAsString);
    Library library = _dao.findEntityById(Library.class, libraryId);
    return _thisProxy.viewLibrary(library);
  }

  @UIControllerMethod
  @Transactional
  public String viewLibrary(Library library)
  {
    setLibrary(_dao.reloadEntity(library));
    _dao.needReadOnly(_library, 
                      Library.contentsVersions.to(LibraryContentsVersion.loadingActivity).to(Activity.performedBy).getPath(),
                      Library.contentsVersions.to(LibraryContentsVersion.releaseActivity).to(Activity.performedBy).getPath());
    _contentsVersionsDataModel = null;
    return VIEW_LIBRARY;
  }

  public DataModel getContentsVersionsDataModel()
  {
    if (_contentsVersionsDataModel == null) {
      _contentsVersionsDataModel = new ListDataModel(Lists.newArrayList(Iterables.reverse(Lists.newArrayList(_library.getContentsVersions()))));
    }
    return _contentsVersionsDataModel;
  }
  
  @UIControllerMethod
  public String browseLibraryContentsVersionWells()
  {
    LibraryContentsVersion lcv = (LibraryContentsVersion) getRequestMap().get("lcv");
    _wellsBrowser.searchWellsForLibraryContentsVersion(lcv);
    return VIEW_WELL_SEARCH_RESULTS;
  }

  @UIControllerMethod
  public String deleteLibraryContentsVersion()
  {
    LibraryContentsVersion lcv = (LibraryContentsVersion) getRequestMap().get("lcv");
    _librariesDao.deleteLibraryContentsVersion(lcv);
    return _thisProxy.viewLibrary(_library);
  }

  @UIControllerMethod
  public String releaseLibraryContentsVersion()
  {
    _libraryContentsVersionManager.releaseLibraryContentsVersion((LibraryContentsVersion) getRequestMap().get("lcv"),
                                                                  (AdministratorUser) getScreensaverUser());
    return _thisProxy.viewLibrary(_library);
  }

  @UIControllerMethod
  @Transactional
  public String viewLibraryContents()
  {
    _wellsBrowser.searchWellsForLibrary(_library);
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
      return _libraryContentsImporter.viewLibraryContentsImporter(_library);
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String deleteLibrary()
  {
    try {
      _dao.doInTransaction(new DAOTransaction() {
        public void runTransaction() {

          _dao.deleteEntity(_library);
          _dao.flush();

        }
      });
      showMessage("libraries.deletedLibrary", "libraryViewer");
      
      _librariesBrowser.searchLibraryScreenType(null);
      return BROWSE_LIBRARIES;
    } catch (Exception e) {
      if (e instanceof DataIntegrityViolationException) {
        showMessage("libraries.libraryDeletionFailed", _library.getLibraryName(), "Please check that no screen results are associated with the library.");
      }
      else {
        showMessage("libraries.libraryDeletionFailed", e.getClass().getName(), e.getMessage());
      }
      log.warn("library deletion: " + _library.getLibraryName() + e.getMessage() );
      return VIEW_MAIN;
    }
  }

  public String cancel()
  {
    return null;
  }

  public String edit()
  {
    return null;
  }

  public String save()
  {
    return null;
  }
}
