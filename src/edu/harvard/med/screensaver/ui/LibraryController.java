// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.db.DAO;

import org.apache.log4j.Logger;

public class LibraryController extends AbstractController
{
  private static Logger log = Logger.getLogger(LibraryController.class);
  
  private DAO _dao;
  private Library _library;
  private String _usageMode; // "create" or "edit"
  private boolean _advancedMode;
  
  /* Property getter/setter methods */
  
  public void setDAO(DAO dao) {
    _dao = dao;
  }
  
  public void setLibrary(Library library) {
    _library = library;
  }

  /**
   * Return the library being managed by this controller.
   * @motivation allows properties of the Library to be bound to UI components
   * @return
   */
  public Library getLibrary() {
    return _library;
  }

  public void setUsageMode(String usageMode) {
    _usageMode = usageMode;
  }
  
  public String getUsageMode() {
    return _usageMode;
  }
  
  
  public boolean isAdvancedMode() {
    return _advancedMode;
  }

  public void setAdvancedMode(boolean advancedMode) {
    _advancedMode = advancedMode;
  }

  
  /* JSF Application methods */

  /**
   * A command to saved the user's edits.
   */
  public String save() {
    return create();
  }
  
  public String create() {
    try {
      _dao.persistEntity(_library);
    }
    catch (Exception e) {
      String msg = "error during entity save/create: " + e.getMessage();
      log.info(msg);
      FacesContext.getCurrentInstance().addMessage("libraryForm", new FacesMessage(msg));
      return null; // redisplay
    }
    return "done";
  }
  
  public String cancel() {
    return "cancel";
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
