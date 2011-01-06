// $HeadURL$
// $Id$
//
// Copyright © 2008, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.util.Arrays;
import java.util.List;

import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryScreeningStatus;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.Solvent;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.service.libraries.LibraryCreator;
import edu.harvard.med.screensaver.ui.arch.util.JSFUtils;
import edu.harvard.med.screensaver.ui.arch.util.UISelectOneBean;
import edu.harvard.med.screensaver.ui.arch.util.UISelectOneEntityBean;
import edu.harvard.med.screensaver.ui.arch.view.EditResult;
import edu.harvard.med.screensaver.ui.arch.view.EditableEntityViewerBackingBean;
import edu.harvard.med.screensaver.ui.arch.view.aspects.UICommand;

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
  private LibraryPlateSearchResults _libraryPlatesBrowser;

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
                             LibraryViewer libraryViewer,
                             LibraryPlateSearchResults libraryPlatesBrowser)
  {
    super(thisProxy,
          Library.class,
          EDIT_LIBRARY,
          dao);
    _libraryCreator = libraryCreator;
    _libraryViewer = libraryViewer;
    _libraryPlatesBrowser = libraryPlatesBrowser;
  }
  
  public List<SelectItem> getLibraryScreeningStatusSelectItems()    
  {
    List<LibraryScreeningStatus> values = Arrays.asList(LibraryScreeningStatus.values());
    if (getEntity().getScreeningStatus() == null) {
      return JSFUtils.createUISelectItemsWithEmptySelection(values, REQUIRED_VOCAB_FIELD_PROMPT);
    }
    return JSFUtils.createUISelectItems(values);
  }

  public List<SelectItem> getScreenTypeSelectItems()
  {
    List<ScreenType> values = Arrays.asList(ScreenType.values());
    if (getEntity().getScreenType() == null) {
      return JSFUtils.createUISelectItemsWithEmptySelection(values, REQUIRED_VOCAB_FIELD_PROMPT);
    }
    return JSFUtils.createUISelectItems(values);
  }

  public List<SelectItem> getSolventSelectItems()
  {
    List<Solvent> values = Arrays.asList(Solvent.values());
    if (getEntity().getSolvent() == null) {
      return JSFUtils.createUISelectItemsWithEmptySelection(values, REQUIRED_VOCAB_FIELD_PROMPT);
    }
    return JSFUtils.createUISelectItems(values);
  }

  public List<SelectItem> getLibraryTypeSelectItems()
  {
    List<LibraryType> values = Arrays.asList(LibraryType.values());
    if (getEntity().getLibraryType() == null) {
      return JSFUtils.createUISelectItemsWithEmptySelection(values, REQUIRED_VOCAB_FIELD_PROMPT);
    }
    return JSFUtils.createUISelectItems(values);
  }
  
  @Override
  protected void initializeEntity(Library entity)
  {
  }
  
  @Override
  protected void initializeViewer(Library entity)
  {}

  @Override
  protected void initializeNewEntity(Library library)
  {
    library.setPlateSize(ScreensaverConstants.DEFAULT_PLATE_SIZE);
    library.setSolvent(Solvent.getDefaultSolventType(library.getScreenType()));
  }

  @Override
  protected boolean validateEntity(Library entity)
  {
    try {
      if (entity.isTransient()) {
        _libraryCreator.validateLibrary(entity);
      }
    }
    catch (DataModelViolationException e) {
      showMessage("libraries.libraryCreationFailed", e.getMessage());
      return false;
    }

    if (entity.getSolvent() != null && !entity.getSolvent().isValidForScreenType(entity.getScreenType())) {
      showMessage("libraries.invalidSolventType", entity.getSolvent(), entity.getScreenType());
      return false;
    }

    return true;
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

  @UICommand
  public String browseLibraryPlates()
  {
    _libraryPlatesBrowser.searchLibraryPlatesByLibrary(getEntity());
    return BROWSE_LIBRARY_PLATES_SCREENED;
  }
}
