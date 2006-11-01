// $HeadURL: svn+ssh://js163@orchestra/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/ui/libraries/ScreenViewer.java $
// $Id: ScreenViewer.java 449 2006-08-09 22:53:09Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.view.screens;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;
import org.springframework.dao.ConcurrencyFailureException;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.model.screens.AbaseTestset;
import edu.harvard.med.screensaver.model.screens.AssayReadoutType;
import edu.harvard.med.screensaver.model.screens.AttachedFile;
import edu.harvard.med.screensaver.model.screens.FundingSupport;
import edu.harvard.med.screensaver.model.screens.LetterOfSupport;
import edu.harvard.med.screensaver.model.screens.Publication;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.StatusItem;
import edu.harvard.med.screensaver.model.screens.StatusValue;
import edu.harvard.med.screensaver.model.screens.Visit;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.searchresults.SearchResults;
import edu.harvard.med.screensaver.ui.util.JSFUtils;
import edu.harvard.med.screensaver.ui.util.ScreeningRoomUserByLabComparator;
import edu.harvard.med.screensaver.ui.view.screenresults.ScreenResultViewer;
import edu.harvard.med.screensaver.util.StringUtils;

public class ScreenViewer extends AbstractBackingBean
{
  private static final ScreensaverUserRole EDITING_ROLE = ScreensaverUserRole.SCREENS_ADMIN;

  private static Logger log = Logger.getLogger(ScreenViewer.class);
  
  
  // instance data
  
  private DAO _dao;
  private Screen _screen;
  private SearchResults<Screen> _searchResults;
  private ScreenResultViewer _screenResultViewer;
  private List<SelectItem> _leadScreenerSelectItems;
  private FundingSupport _newFundingSupport;
  private StatusValue _newStatusValue;
  private AssayReadoutType _newAssayReadoutType = AssayReadoutType.UNSPECIFIED; // the default (as specified in reqs)
  private String _newKeyword = "";

  
  // public property getter & setter methods
  
  public void setDao(DAO dao) 
  {
    _dao = dao;
   }
  
  public void setScreen(Screen screen) 
  {
    _screen = screen;
    updateLeadScreenerSelectItems(screen.getLabHead());
  }

  public Screen getScreen() 
  {
    if (_screen == null) {
      Screen defaultScreen = _dao.findEntityById(Screen.class, 92);
      log.warn("no screen defined: defaulting to screen " + defaultScreen.getScreenNumber());
      setScreen(defaultScreen);
    }
    return _screen;
  }
  
  public SearchResults<Screen> getSearchResults()
  {
    return _searchResults;
  }

  public void setSearchResults(SearchResults<Screen> searchResults)
  {
    _searchResults = searchResults;
  }

  public ScreenResultViewer getScreenResultViewer()
  {
    return _screenResultViewer;
  }

  public void setScreenResultViewer(
    ScreenResultViewer screenResultViewer)
  {
    _screenResultViewer = screenResultViewer;
  }

  public AssayReadoutType getNewAssayReadoutType()
  {
    return _newAssayReadoutType;
  }

  public void setNewAssayReadoutType(AssayReadoutType newAssayReadoutTypeController)
  {
    _newAssayReadoutType = newAssayReadoutTypeController;
  }

  public FundingSupport getNewFundingSupport()
  {
    return _newFundingSupport;
  }

  public void setNewFundingSupport(FundingSupport newFundingSupportController)
  {
    _newFundingSupport = newFundingSupportController;
  }

  public StatusValue getNewStatusValue()
  {
    return _newStatusValue;
  }

  public void setNewStatusValue(StatusValue newStatusValueController)
  {
    _newStatusValue = newStatusValueController;
  }

  public String getNewKeyword()
  {
    return _newKeyword;
  }

  public void setNewKeyword(String newKeyword)
  {
    _newKeyword = newKeyword;
  }
  
  public DataModel getCollaboratorsDataModel()
  {
    return new ListDataModel(new ArrayList<ScreeningRoomUser>(_screen.getCollaborators()));
  }

  public DataModel getStatusItemsDataModel()
  {
    return new ListDataModel(new ArrayList<StatusItem>(_screen.getStatusItems()));
  }

  public DataModel getVisitsDataModel()
  {
    return new ListDataModel(new ArrayList<Visit>(_screen.getVisits()));
  }

  public DataModel getPublicationsDataModel()
  {
    return new ListDataModel(new ArrayList<Publication>(_screen.getPublications()));
  }

  public DataModel getLettersOfSupportDataModel()
  {
    return new ListDataModel(new ArrayList<LetterOfSupport>(_screen.getLettersOfSupport()));
  }

  public DataModel getAttachedFilesDataModel()
  {
    return new ListDataModel(new ArrayList<AttachedFile>(_screen.getAttachedFiles()));
  }

  public DataModel getFundingSupportsDataModel()
  {
    return new ListDataModel(new ArrayList<FundingSupport>(_screen.getFundingSupports()));
  }

  public DataModel getAssayReadoutTypesDataModel()
  {
    return new ListDataModel(new ArrayList<AssayReadoutType>(_screen.getAssayReadoutTypes()));
  }

  public DataModel getAbaseTestsetsDataModel()
  {
    return new ListDataModel(new ArrayList<AbaseTestset>(_screen.getAbaseTestsets()));
  }

  public DataModel getKeywordsDataModel()
  {
    return new ListDataModel(new ArrayList<String>(_screen.getKeywords()));
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
                                            labHead.getLabName()));
    }
    return labHeadSelectItems;
  }

  /**
   * Get a list of SelectItems for the screen's collaborators. Collaborators are
   * grouped (and indented) by lab, and the (unindented) lab SelectItem maps to
   * the lab head.
   */
  public List<SelectItem> getCollaboratorSelectItems()
  {
    // TODO: move the logic for determing potential collaborators to Screen
    List<SelectItem> collaboratorSelectItems = new ArrayList<SelectItem>();
    List<ScreeningRoomUser> screeningRoomUsers = _dao.findAllEntitiesWithType(ScreeningRoomUser.class);
    Collections.sort(screeningRoomUsers,
                     ScreeningRoomUserByLabComparator.getInstance());
    ScreeningRoomUser lastLabHead = null;
    String indent = "...";
    for (ScreeningRoomUser screener : screeningRoomUsers) {
      if (lastLabHead == null || !screener.getLabHead().equals(lastLabHead)) {
        collaboratorSelectItems.add(new SelectItem(screener.getLabHead(),
                                                   screener.getLabName()));
        lastLabHead = screener.getLabHead();
      } 
      else {
        collaboratorSelectItems.add(new SelectItem(screener,
                                                   indent + screener.getFullName()));
      }
    }
    return collaboratorSelectItems;
  }
  

  public List<SelectItem> getLeadScreenerSelectItems()
  {
    return _leadScreenerSelectItems;
  }

  public List<SelectItem> getNewStatusValueSelectItems()
  {
    Set<StatusValue> candiateStatusValues = new HashSet<StatusValue>(Arrays.asList(StatusValue.values()));
    for (StatusItem statusItem : _screen.getStatusItems()) {
      candiateStatusValues.remove(statusItem.getStatusValue());
    }
    return JSFUtils.createUISelectItems(candiateStatusValues);
  }

  public List<SelectItem> getNewFundingSupportSelectItems()
  {
    Set<FundingSupport> candiateFundingSupports = new HashSet<FundingSupport>(Arrays.asList(FundingSupport.values()));
    candiateFundingSupports.removeAll(_screen.getFundingSupports());
    return JSFUtils.createUISelectItems(candiateFundingSupports);
  }

  public List<SelectItem> getNewAssayReadoutTypeSelectItems()
  {
    Set<AssayReadoutType> candiateAssayReadoutTypes = new HashSet<AssayReadoutType>(Arrays.asList(AssayReadoutType.values()));
    candiateAssayReadoutTypes.removeAll(_screen.getAssayReadoutTypes());
    return JSFUtils.createUISelectItems(candiateAssayReadoutTypes);
  }
  
  
  /* JSF Application methods */

  /**
   * A command to save the user's edits.
   */
  public String save() {
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
  
  public String addStatusItem()
  {
    if (_newStatusValue != null) {
      _dao.defineEntity(StatusItem.class,
                        _screen,
                        new Date(),
                        _newStatusValue);
      _newStatusValue = null;
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public String deleteStatusItem()
  {
    return deleteEntity(StatusItem.class);
  }
  
  // TODO: save & go to visit viewer
  public String addCherryPickVisitItem()
  {
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  // TODO: save & go to visit viewer
  public String addNonCherryPickVisitItem()
  {
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public String copyVisit()
  {
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public String viewVisit()
  {
    //String visitIdToView = (String) getRequestParameterMap().get(VISIT_ID_PARAM_NAME);
    //_visitViewer.setVisitId(visitIdToView);
    // TODO: implement when Visit Viewer is implemented
    return VIEW_VISIT_ACTION_RESULT;  
  }  
  
  public String viewAttachedFile()
  {
    // TODO: implement when Visit Viewer is implemented
    return VIEW_ATTACHED_FILE_ACTION_RESULT;
  }  
  
  public String addPublication()
  {
    _dao.defineEntity(Publication.class, _screen, "<new>", "", "", "");
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public String deletePublication()
  {
    return deleteEntity(Publication.class);
  }
  
  public String addLetterOfSupport()
  {
    _dao.defineEntity(LetterOfSupport.class, _screen, new Date(), "");
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public String deleteLetterOfSupport()
  {
    return deleteEntity(LetterOfSupport.class);
  }
  
  public String addAttachedFile()
  {
    _dao.defineEntity(AttachedFile.class, _screen, "<new>", "");
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public String deleteAttachedFile()
  {
    return deleteEntity(AttachedFile.class);
  }
  
  public String addFundingSupport()
  {
    if (_newFundingSupport != null) {
      _screen.addFundingSupport(_newFundingSupport);
      _newFundingSupport = null;
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public String deleteFundingSupport()
  {
    return deleteEntity(FundingSupport.class);
  }
  
  public String addAssayReadoutType()
  {
    if (_newAssayReadoutType != null) {
      _screen.addAssayReadoutType(_newAssayReadoutType);
      _newAssayReadoutType = null;
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public String deleteAssayReadoutTypes()
  {
    return deleteEntity(AssayReadoutType.class);
  }
  
  public String addAbaseTestset()
  {
    _dao.defineEntity(AbaseTestset.class, _screen, "<new>");
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public String deleteAbaseTestset()
  {
    return deleteEntity(AbaseTestset.class);
  }
  
  public String addKeyword()
  {
    if (!_screen.addKeyword(_newKeyword)) {
      showMessage("screens.duplicateKeyword", "newKeyword", _newKeyword);
    }
    else {
      _newKeyword = "";
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public String deleteKeyword()
  {
    return deleteEntity(String.class, "Keyword");
  }
  
  public String viewCollaborator()
  {
    //String collaboratorIdToView = (String) getRequestParameterMap().get(COLLABORATOR_ID_PARAM_NAME);
    //_screeningRoomUserViewer.setScreensaverUserId(collaboratorIdToView);
    return VIEW_SCREENING_ROOM_USER_ACTION_RESULT;
  }

  public String viewCollaboratorLabHead()
  {
    // TODO: implement
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  public String viewLabHead()
  {
    // TODO: implement
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  public String viewLeadScreener()
  {
    // TODO: implement
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  public String viewScreenResult()
  {
    _screenResultViewer.setScreenResult(_screen.getScreenResult());
    return VIEW_OR_EDIT_SCREEN_RESULT_ACTION;
  }
  
  public String viewBillingInformation()
  {
    return VIEW_BILLING_INFORMATION_ACTION_RESULT;
  }

  
  /* JSF Action event listeners */

  public void update(ValueChangeEvent event) {
    updateLeadScreenerSelectItems((ScreeningRoomUser) event.getNewValue());
    getFacesContext().renderResponse();
  }
  
  
  // protected methods

  protected ScreensaverUserRole getEditableAdminRole()
  {
    return EDITING_ROLE;
  }


  // private methods
  
  /**
   * Updates the set of lead screeners that can be selected for this screen.
   * Depends upon the lab head.
   * 
   * @motivation to update the list of lead screeners in the UI, in response to
   *             a new lab head selection, but without updating the entity
   *             before the user saves his edits
   */
  private void updateLeadScreenerSelectItems(ScreeningRoomUser labHead) {
    _leadScreenerSelectItems = new ArrayList<SelectItem>();
    for (ScreeningRoomUser screener : labHead.getLabMembers()) {
      _leadScreenerSelectItems.add(new SelectItem(screener, screener.getFullName()));
    }
  }

  @SuppressWarnings("unchecked")
  private <E> String deleteEntity(Class<E> clazz)
  {
    return deleteEntity(clazz, clazz.getSimpleName());
  }

  @SuppressWarnings("unchecked")
  private <E> String deleteEntity(Class<E> clazz, String entityPropertyName)
  {
    E entity = null;
    try {
      entity = (E) getHttpServletRequest().getAttribute(StringUtils.uncapitalize(entityPropertyName));
      // TODO: This is not a great use of reflection, as it's only being used to compress code size; we also lose compile-time checking of the methods we're relying upon; remove at some point...
      Method deleteMethod = Screen.class.getMethod("remove" + entityPropertyName, clazz);
      boolean deleted = (Boolean) deleteMethod.invoke(_screen, entity);
      if (!deleted) {
        throw new IllegalStateException(entity + " appears to have already been deleted");
      }
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    catch (Exception e) {
      reportSystemError("error deleting " + entity + ": " + e.getMessage());
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
  }

}
