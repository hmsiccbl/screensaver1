// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.ui.AbstractController;
import edu.harvard.med.screensaver.db.DAO;

import org.apache.log4j.Logger;

public class LibraryViewerController extends AbstractController
{
  private static Logger log = Logger.getLogger(LibraryViewerController.class);
  
  private DAO _dao;
  private Library _library;
  private RNAiLibraryContentsImporterController _rnaiLibraryContentsImporter;
  private String _usageMode; // "create" or "edit"
  private boolean _advancedMode;
  
  /* Property getter/setter methods */
  
  public void setDao(DAO dao)
  {
    _dao = dao;
  }
  
  public void setLibrary(Library library)
  {
    _library = library;
  }

  /**
   * Return the library being managed by this controller.
   * @motivation allows properties of the Library to be bound to UI components
   * @return
   */
  public Library getLibrary()
  {
    return _library;
  }

  public RNAiLibraryContentsImporterController getRnaiLibraryContentsImporter()
  {
    return _rnaiLibraryContentsImporter;
  }

  public void setRnaiLibraryContentsImporter(
    RNAiLibraryContentsImporterController rnaiLibraryContentsImporter)
  {
    _rnaiLibraryContentsImporter = rnaiLibraryContentsImporter;
  }

  public void setUsageMode(String usageMode)
  {
    _usageMode = usageMode;
  }
  
  public String getUsageMode()
  {
    return _usageMode;
  }
  
  public boolean isAdvancedMode()
  {
    return _advancedMode;
  }

  public void setAdvancedMode(boolean advancedMode) 
  {
    _advancedMode = advancedMode;
  }

  public boolean getIsRNAiLibrary()
  {
    return _library != null && _library.getLibraryType().equals(LibraryType.RNAI);
  }

  public boolean getIsCompoundLibrary()
  {
    return _library != null && ! _library.getLibraryType().equals(LibraryType.RNAI);
  }
  
  
  /* JSF Application methods */

  /**
   * A command to saved the user's edits.
   */
  public String save()
  {
    return create();
  }
  
  public String create()
  {
    try {
      _dao.persistEntity(_library);
    }
    catch (Exception e) {
      String msg = "error during entity save/create: " + e.getMessage();
      log.info(msg);
      FacesContext.getCurrentInstance().addMessage("libraryForm", new FacesMessage(msg));
      return REDISPLAY_PAGE_ACTION_RESULT; // redisplay
    }
    return DONE_ACTION_RESULT;
  }
  
  public String cancel() 
  {
    return "cancel";
  }

  public String goImportRNAiLibraryContents()
  {
    _rnaiLibraryContentsImporter.setLibraryViewer(this);
    _rnaiLibraryContentsImporter.setLibrary(_library);
    return "goImportRNAiLibraryContents";
  }

  public String goImportCompoundLibraryContents()
  {
    // TODO before this will work:
    // - write the compoundlibrarycontentsimporter
    // - create a controller and a viewer for it
    // - add a navigation rule for goImportCompoundLibraryContents
    
    //_rnaiLibraryContentsImporter.setLibrary(_library);
    return "goImportCompoundLibraryContents";
  }
  
  
  /* JSF Action event listeners */

  /**
   * An action event listener to revert the user's edits.
   */
  public void revertEventHandler(ActionEvent event) {
    log.debug("revert action event handled");
    FacesContext.getCurrentInstance().addMessage("libraryForm", new FacesMessage("\"Revert\" command not yet implemented!"));
  }
  
  public void showAdvancedEventListener(ActionEvent event) {
    _advancedMode = event.getComponent().getId().equals("showAdvanced");
    log.debug("show advanced action invoked: advancedMode=" + _advancedMode);
    FacesContext.getCurrentInstance().renderResponse();
  }
}
