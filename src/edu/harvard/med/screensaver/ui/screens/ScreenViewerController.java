// $HeadURL: svn+ssh://js163@orchestra/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/ui/libraries/ScreenViewerController.java $
// $Id: ScreenViewerController.java 449 2006-08-09 22:53:09Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screens;

import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.ui.AbstractController;
import edu.harvard.med.screensaver.ui.util.JSFUtils;

public class ScreenViewerController extends AbstractController
{
  private static Logger log = Logger.getLogger(ScreenViewerController.class);
  
  private DAO _dao;
  private Screen _screen;
  private String _usageMode; // "create" or "edit"
  private boolean _advancedMode;

  private List<ScreeningRoomUser> _selectedCollaborators;
  private List<ScreeningRoomUser> _selectedNonCollaborators;
  

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
    if (_screen == null) {
      Screen defaultScreen = _dao.findEntityById(Screen.class, 92);
      log.warn("no screen defined: defaulting to screen " + defaultScreen.getScreenNumber());
      _screen = defaultScreen;
    }
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
  
  public List<SelectItem> getScreenTypeSelectItems()
  {
    return JSFUtils.createUISelectItems(ScreenType.values());
  }

  public List<SelectItem> getLabNameSelectItems()
  {
    List<SelectItem> labHeadSelectItems = new ArrayList<SelectItem>();
    List<ScreeningRoomUser> labHeads = _dao.findAllLabHeads();
    for (ScreeningRoomUser labHead : labHeads) {
      labHeadSelectItems.add(new SelectItem(labHead,
                                            // requirements state that "lab name" is last name of the "lab head"
                                            labHead.getLastName()));
    }
    return labHeadSelectItems;
  }

  public List<SelectItem> getLeadScreenerSelectItems()
  {
    List<SelectItem> leadScreenerSelectItems = new ArrayList<SelectItem>();
    for (ScreeningRoomUser screener : _screen.getLabHead().getLabMembers()) {
      leadScreenerSelectItems.add(new SelectItem(screener, screener.generateFullName()));
    }
    return leadScreenerSelectItems;
  }

  public List<SelectItem> getCollaboratorSelectItems()
  {
    List<SelectItem> collaboratorSelectItems = new ArrayList<SelectItem>();
    // TODO: what is the valid set of collaborators?
    for (ScreeningRoomUser screener : _screen.getCollaborators()) {
      collaboratorSelectItems.add(new SelectItem(screener, screener.generateFullName()));
    }
    return collaboratorSelectItems;
  }

  public List<ScreeningRoomUser> getSelectedCollaborators()
  {
    if (_selectedCollaborators == null) {
      return new ArrayList<ScreeningRoomUser>();
    }
    return _selectedCollaborators;
  }

  public void setSelectedCollaborators(List<ScreeningRoomUser> selectedCollaborators)
  {
    _selectedCollaborators = selectedCollaborators;
  }

  public List<SelectItem> getNonCollaboratorSelectItems()
  {
    List<SelectItem> nonCollaboratorSelectItems = new ArrayList<SelectItem>();
    List<ScreeningRoomUser> nonCollaborators = _dao.findAllEntitiesWithType(ScreeningRoomUser.class);
    nonCollaborators.removeAll(_screen.getCollaborators());
    for (ScreeningRoomUser screener : nonCollaborators) {
      nonCollaboratorSelectItems.add(new SelectItem(screener, screener.generateFullName()));
    }
    return nonCollaboratorSelectItems;
  }

  public List<ScreeningRoomUser> getSelectedNonCollaborators()
  {
    if (_selectedNonCollaborators == null) {
      return new ArrayList<ScreeningRoomUser>();
    }
    return _selectedNonCollaborators;
  }

  public void setSelectedNonCollaborators(List<ScreeningRoomUser> selectedNonCollaborators)
  {
    _selectedNonCollaborators = selectedNonCollaborators;
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
  
  public String addSelectedNonCollaborators() {
    for (ScreeningRoomUser screener : getSelectedNonCollaborators()) {
      _screen.addCollaborator(screener);
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public String removeSelectedCollaborators() {
    for (ScreeningRoomUser screener : getSelectedCollaborators()) {
      _screen.removeCollaborator(screener);
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
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
