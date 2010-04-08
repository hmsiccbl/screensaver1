// $HeadURL$
// $Id$
//
// Copyright 2008 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.util.Arrays;
import java.util.List;

import javax.faces.model.SelectItem;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryScreeningStatus;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.service.libraries.LibraryCreator;
import edu.harvard.med.screensaver.ui.EditResult;
import edu.harvard.med.screensaver.ui.EditableEntityViewerBackingBean;
import edu.harvard.med.screensaver.ui.util.JSFUtils;
import edu.harvard.med.screensaver.ui.util.UISelectOneBean;
import edu.harvard.med.screensaver.ui.util.UISelectOneEntityBean;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;

/**
 * Backing class for the Library creation page.
 * 
 * @author <a mailto="voonkl@bii.a-star.edu.sg">Kian Loon Voon</a>
 */
public class LibraryDetailViewer extends EditableEntityViewerBackingBean<Library>
{
  // static members

  private static Logger log = Logger.getLogger(LibraryDetailViewer.class);

  private LibraryCreator _libraryCreator;
  private LibraryViewer _libraryViewer;

  
  private UISelectOneEntityBean<ScreeningRoomUser> owner;
  
  
  /**
   * @motivation for CGLIB2
   */
  protected LibraryDetailViewer()
  {
  }
  
  public LibraryDetailViewer(LibraryDetailViewer thisProxy,
                             GenericEntityDAO dao,
                             LibraryCreator libraryCreator,
                             LibraryViewer libraryViewer) 
  {
    super(thisProxy,
          Library.class,
          EDIT_LIBRARY,
          dao);
    _libraryCreator = libraryCreator;
    _libraryViewer = libraryViewer;
  }
  
  public List<SelectItem> getLibraryScreeningStatusSelectItems()    
  {
    return JSFUtils.createUISelectItems(Lists.newArrayList(LibraryScreeningStatus.values()));
  }

  public List<SelectItem> getScreenTypeSelectItems()
  {
    return JSFUtils.createUISelectItems(Arrays.asList(ScreenType.values()));
  }

  public List<SelectItem> getLibraryTypeSelectItems()
  {
    return JSFUtils.createUISelectItems(Arrays.asList(LibraryType.values()));
  }
  
  @Override
  protected void initializeEntity(Library entity)
  {
  }
  
  @Override
  protected void initializeViewer(Library entity)
  {
    }

  @Override
  protected void initializeNewEntity(Library library)
  {
    library.setScreeningStatus(LibraryScreeningStatus.ALLOWED);
    library.setPlateSize(ScreensaverConstants.DEFAULT_PLATE_SIZE);
  }

  @Override
  protected boolean validateEntity(Library entity)
  {
    try {
      if (entity.isTransient()) {
        _libraryCreator.validateLibrary(entity);
      }
      return true;
    }
    catch (DataModelViolationException e) {
      showMessage("libraries.libraryCreationFailed", e.getMessage());
      return false;
    }
  }
  
  @Override
  protected void updateEntityProperties(Library entity)
  {
    if (entity.getWells().size() == 0) {
      _libraryCreator.createLibrary(entity);
    }
    entity.setOwner(getOwner().getSelection());
  }

  @Override
  protected String postEditAction(EditResult editResult)
  {
    switch (editResult) {
    case CANCEL_EDIT: return _libraryViewer.reload();
    case SAVE_EDIT: return _libraryViewer.reload();
    case CANCEL_NEW: return VIEW_MAIN;
    case SAVE_NEW: return _libraryViewer.viewEntity(getEntity()); // note: can't call reload() since parent viewer is not yet configured with our new library    
    default: return null;
    }
  }
  
  public UISelectOneBean<ScreeningRoomUser> getOwner()
  {
    if (owner == null) {
      //TODO convert to sortedSet
      List<ScreeningRoomUser> owners = getDao().findAllEntitiesOfType(ScreeningRoomUser.class);
      owner = new UISelectOneEntityBean<ScreeningRoomUser>(owners, getEntity().getOwner(), true, getDao()) {
        @Override
        protected String makeLabel(ScreeningRoomUser o) { return o.getFullNameLastFirst(); }
        @Override
        protected String getEmptyLabel() { return "<empty>"; }
      };
    }
    return owner;
  }
}
