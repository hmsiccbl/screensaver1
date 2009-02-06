// $HeadURL: http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/branches/iccbl/trunk/src/edu/harvard/med/screensaver/ui/libraries/LibraryViewer.java $
// $Id: LibraryViewer.java 2825 2008-11-03 18:56:46Z atolopko $
//
// Copyright 2008 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.faces.model.SelectItem;

import edu.harvard.med.screensaver.ScreensaverProperties;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryScreeningStatus;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.service.libraries.LibraryCreator;
import edu.harvard.med.screensaver.ui.AbstractEditableBackingBean;
import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.ui.util.JSFUtils;
import edu.harvard.med.screensaver.ui.util.UISelectOneBean;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

/**
 * Backing class for the Library creation page.
 * 
 * @author <a mailto="voonkl@bii.a-star.edu.sg">Kian Loon Voon</a>
 */
public class LibraryDetailViewer extends AbstractEditableBackingBean
{
  // static members

  private static Logger log = Logger.getLogger(LibraryDetailViewer.class);

  private GenericEntityDAO _dao;
  private LibraryCreator _libraryCreator;
  private LibraryViewer _libraryViewer;

  private Library _library;
  private boolean _isEditMode;
  
  
  /**
   * @motivation for CGLIB2
   */
  protected LibraryDetailViewer()
  {
  }
  
  public LibraryDetailViewer(GenericEntityDAO dao,
                             LibraryCreator libraryCreator,
                             LibraryViewer libraryViewer) 
  {
    super(ScreensaverUserRole.LIBRARIES_ADMIN);
    _dao = dao;
    _libraryCreator = libraryCreator;
    _libraryViewer = libraryViewer;
  }
  
  
  public void setLibrary(Library library)
  {
    _library = library;
    _isEditMode = false;
  }

  public Library getLibrary()
  {
    return _library;
  }


  public AbstractEntity getEntity()
  {
    return getLibrary();
  }
  
  public boolean isEditMode()
  {
    return !isReadOnly() && _isEditMode;
  }
  
  @Override
  public ScreensaverUserRole getEditableAdminRole()
  {
    return ScreensaverUserRole.LIBRARIES_ADMIN;
  }
  
  @UIControllerMethod
  @Transactional
  public String editNewLibrary()
  {
    ScreensaverUser user = getScreensaverUser();
    if (!(user instanceof AdministratorUser &&
      ((AdministratorUser) user).isUserInRole(ScreensaverUserRole.LIBRARIES_ADMIN))) {
      showMessage("restrictedOperation", "add a new library");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }

    _library = new Library();
    _library.setScreeningStatus(LibraryScreeningStatus.ALLOWED);
    
    _isEditMode = true;
    return VIEW_LIBRARY_DETAIL;
  }

  @UIControllerMethod
  public String cancel()
  {
    return VIEW_MAIN;
  }

  @UIControllerMethod
  public String save()
  {
      if (_library.getEntityId() == null) {
        try {
          _library = _libraryCreator.createLibrary(_library, null);
          showMessage("libraries.createdLibrary", "librariesBrowser");
        }
        catch (Exception e) 
        {
          reportApplicationError(e);
          return REDISPLAY_PAGE_ACTION_RESULT;
        }
      }
      else {
        _dao.reattachEntity(_library);
      }

      _dao.flush();
      return _libraryViewer.viewLibrary(_library);
  }
  
  public List<SelectItem> getLibraryScreeningStatusSelectItems()    
  {
    return JSFUtils.createUISelectItems(Lists.newArrayList(LibraryScreeningStatus.values()));
  }

  public List<SelectItem> getScreenTypeSelectItems()
  {
    List<ScreenType> screenTypelist = new ArrayList<ScreenType>();
    // BII (Siew Cheng) start: Hide compound
    // TODO: merge compound hide functionality from the imcb trunk
    if (Boolean.valueOf(
          ScreensaverProperties.getProperty("isCompoundHidden")) == true) {
      for(ScreenType screenType:Arrays.asList(ScreenType.values())){
        if (screenType != ScreenType.SMALL_MOLECULE) {
          screenTypelist.add(screenType);
        }
      }
      return JSFUtils.createUISelectItems(screenTypelist);
    }
    else {
      return JSFUtils.createUISelectItems(Arrays.asList(ScreenType.values()));
    }
  // BII end
  }
  
  public List<SelectItem> getLibraryTypeSelectItems()
  {
    return JSFUtils.createUISelectItems(Arrays.asList(LibraryType.values()));
  }

  public String edit()
  {
    _isEditMode = true;
    return VIEW_LIBRARY_DETAIL;
  }
}
