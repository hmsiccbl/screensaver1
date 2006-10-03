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
import java.util.Collections;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.AbstractController;
import edu.harvard.med.screensaver.ui.screenresults.ScreenResultViewerController;
import edu.harvard.med.screensaver.ui.util.JSFUtils;
import edu.harvard.med.screensaver.ui.util.ScreensaverUserComparator;

import org.apache.log4j.Logger;
import org.springframework.dao.ConcurrencyFailureException;

public class ScreenViewerController extends AbstractController
{
  private static final String COLLABORATOR_ID_TO_VIEW_PARAM_NAME = "collaboratorIdToView";
  private static final String SCREEN_RESULT_ID_TO_VIEW_PARAM_NAME = "screenResultIdToView";

  private static final ScreensaverUserRole EDITING_ROLE = ScreensaverUserRole.SCREENS_ADMIN;


  private static Logger log = Logger.getLogger(ScreenViewerController.class);
  
  private DAO _dao;
  private Screen _screen;
  private String _usageMode; // "create" or "edit"
  private boolean _advancedMode;
  private ScreenResultViewerController _screenResultViewer;

  /* Property getter/setter methods */
  
  public void setDao(DAO dao) 
  {
    _dao = dao;
   }
  
  public void setScreen(Screen screen) 
  {
    _screen = screen;
  }

  /**
   * Return the library being managed by this controller.
   * @motivation allows properties of the Library to be bound to UI components
   * @return
   */
  public Screen getScreen() 
  {
    if (_screen == null) {
      Screen defaultScreen = _dao.findEntityById(Screen.class, 92);
      log.warn("no screen defined: defaulting to screen " + defaultScreen.getScreenNumber());
      setScreen(defaultScreen);
    }
    return _screen;
  }
  
  public ScreenResultViewerController getScreenResultViewer()
  {
    return _screenResultViewer;
  }

  public void setScreenResultViewer(
    ScreenResultViewerController screenResultViewer)
  {
    _screenResultViewer = screenResultViewer;
  }

  public boolean isReadOnly() 
  {
    return !isUserInRole(EDITING_ROLE);
  }

  public String getCollaboratorIdToViewParamName()
  {
    return COLLABORATOR_ID_TO_VIEW_PARAM_NAME;
  }
  
  public String getScreenResultIdToViewParamName()
  {
    return SCREEN_RESULT_ID_TO_VIEW_PARAM_NAME;
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
      leadScreenerSelectItems.add(new SelectItem(screener, screener.getFullName()));
    }
    return leadScreenerSelectItems;
  }

  public List<SelectItem> getCollaboratorSelectItems()
  {
    List<SelectItem> collaboratorSelectItems = new ArrayList<SelectItem>();
    List<ScreeningRoomUser> screeningRoomUsers = _dao.findAllEntitiesWithType(ScreeningRoomUser.class);
    Collections.sort(screeningRoomUsers, ScreensaverUserComparator.getInstance());
    for (ScreeningRoomUser screener : screeningRoomUsers) {
      collaboratorSelectItems.add(new SelectItem(screener, screener.getFullName()));
    }
    return collaboratorSelectItems;
  }

  
  
  /* JSF Application methods */

  /**
   * A command to save the user's edits.
   */
  public String save() {
    log.debug("ScreenViewerController.save()");
    return create();
  }
  
  public String create() {
    try {
      _dao.persistEntity(_screen);
    }
    catch (ConcurrencyFailureException e) {
      // TODO: handle this exception in a way that works with Hibernate
      //_dao.refreshEntity(_screen);
      //recreateView(false);
      showMessage("concurrentModificationConflict");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    catch (Throwable e) {
      reportSystemError(e);
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    return DONE_ACTION_RESULT;
  }
  
  public String cancel() {
    return "cancel";
  }
  
  public String viewCollaborator()
  {
    //String collaboratorIdToView = getHttpServletRequest().getParameter(COLLABORATOR_ID_TO_VIEW_PARAM_NAME);
    //_screeningRoomUserViewer.setScreensaverUserId(collaboratorIdToView);
    return VIEW_SCREENING_ROOM_USER_ACTION_RESULT;
  }

  public String viewScreenResult()
  {
    if (_screen.getScreenResult() != null ) {
      return VIEW_SCREEN_RESULT_ACTION;
    }
    else {
      reportSystemError("screen does not have a screen result");
      return ERROR_ACTION_RESULT;
    }
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
