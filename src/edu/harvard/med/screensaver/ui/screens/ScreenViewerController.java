// $HeadURL: svn+ssh://js163@orchestra/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/ui/libraries/ScreenViewerController.java $
// $Id: ScreenViewerController.java 449 2006-08-09 22:53:09Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screens;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.ui.AbstractController;

public class ScreenViewerController extends AbstractController
{
  private static Logger log = Logger.getLogger(ScreenViewerController.class);
  
  private DAO _dao;
  private Screen _screen;
  private String _usageMode; // "create" or "edit"
  private boolean _advancedMode;
  
  /* Property getter/setter methods */
  
  public void setDao(DAO dao) {
    _dao = dao;
  }
  
  public void setScreen(Screen screen) {
    _screen = screen;
  }

  /**
   * Return the library being managed by this controller.
   * @motivation allows properties of the Library to be bound to UI components
   * @return
   */
  public Screen getScreen() {
    return _screen;
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
      _dao.persistEntity(_screen);
    }
    catch (Exception e) {
      String msg = "error during entity save/create: " + e.getMessage();
      log.info(msg);
      FacesContext.getCurrentInstance().addMessage("screenForm", new FacesMessage(msg));
      return REDISPLAY_PAGE_ACTION_RESULT; // redisplay
    }
    return DONE_ACTION_RESULT;
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
    FacesContext.getCurrentInstance().addMessage("screenForm", new FacesMessage("\"Revert\" command not yet implemented!"));
  }
  
  public void showAdvancedEventListener(ActionEvent event) {
    _advancedMode = event.getComponent().getId().equals("showAdvanced");
    log.debug("show advanced action invoked: advancedMode=" + _advancedMode);
    FacesContext.getCurrentInstance().renderResponse();
  }
  

}
