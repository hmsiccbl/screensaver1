// $HeadURL: svn+ssh://js163@orchestra/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/ui/libraries/ScreenViewer.java $
// $Id: ScreenViewer.java 449 2006-08-09 22:53:09Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.view.screens;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

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
import edu.harvard.med.screensaver.ui.util.UISelectManyBean;
import edu.harvard.med.screensaver.ui.util.UISelectOneBean;
import edu.harvard.med.screensaver.ui.view.screenresults.ScreenResultViewer;
import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.log4j.Logger;
import org.springframework.dao.ConcurrencyFailureException;

public class ScreenViewer extends AbstractBackingBean
{
  private static final ScreensaverUserRole EDITING_ROLE = ScreensaverUserRole.SCREENS_ADMIN;

  private static Logger log = Logger.getLogger(ScreenViewer.class);
  
  
  // instance data
  
  private DAO _dao;
  private Screen _screen;
  private SearchResults<Screen> _searchResults;
  private ScreenResultViewer _screenResultViewer;
  private UISelectOneBean<ScreeningRoomUser> _leadScreener;
  private UISelectOneBean<ScreeningRoomUser> _labName;
  private UISelectManyBean<ScreeningRoomUser> _collaborators;
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
    _labName = new UISelectOneBean<ScreeningRoomUser>(_dao.findAllLabHeads(), _screen.getLabHead()) { 
      protected String getLabel(ScreeningRoomUser t) { return t.getLabName(); } 
    };
    _collaborators = new UISelectManyBean<ScreeningRoomUser>(_dao.findAllEntitiesWithType(ScreeningRoomUser.class),
                                                            _screen.getCollaborators()) {
      protected String getLabel(ScreeningRoomUser t) { return t.getFullName(); }
    };
    updateLeadScreenerSelectItems();
  }

  public Screen getScreen() 
  {
    if (_screen == null) {
      Screen defaultScreen = _dao.findEntityById(Screen.class, 107);
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

  public UISelectOneBean<ScreeningRoomUser> getLabName()
  {
    return _labName;
  }

  /**
   * Get a list of SelectItems for the screen's collaborators. Collaborators are
   * grouped (and indented) by lab, and the (unindented) lab SelectItem maps to
   * the lab head.
   */
  public UISelectManyBean<ScreeningRoomUser> getCollaborators()
  {
    return _collaborators;
  }
  

  public UISelectOneBean getLeadScreener()
  {
    return _leadScreener;
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
      _screen.setLabHead(_labName.getSelection());
      _screen.setLeadScreener(_leadScreener.getSelection());
      _screen.setCollaboratorsList(_collaborators.getSelections());
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
    _screen.getStatusItems().remove(getSelectedEntityOfType(StatusItem.class));
    return REDISPLAY_PAGE_ACTION_RESULT;
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
    _screen.getPublications().remove(getSelectedEntityOfType(Publication.class));
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public String addLetterOfSupport()
  {
    _dao.defineEntity(LetterOfSupport.class, _screen, new Date(), "");
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public String deleteLetterOfSupport()
  {
    _screen.getLettersOfSupport().remove(getSelectedEntityOfType(LetterOfSupport.class));
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public String addAttachedFile()
  {
    _dao.defineEntity(AttachedFile.class, _screen, "<new>", "");
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public String deleteAttachedFile()
  {
    _screen.getAttachedFiles().remove(getSelectedEntityOfType(AttachedFile.class));
    return REDISPLAY_PAGE_ACTION_RESULT;
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
    _screen.getFundingSupports().remove(getSelectedEntityOfType(FundingSupport.class));
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public String addAssayReadoutType()
  {
    if (_newAssayReadoutType != null) {
      _screen.addAssayReadoutType(_newAssayReadoutType);
      _newAssayReadoutType = null;
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public String deleteAssayReadoutType()
  {
    _screen.getAssayReadoutTypes().remove(getSelectedEntityOfType(AssayReadoutType.class));
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public String addAbaseTestset()
  {
    _dao.defineEntity(AbaseTestset.class, _screen, "<new>");
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public String deleteAbaseTestset()
  {
    _screen.getAbaseTestsets().remove(getSelectedEntityOfType(AbaseTestset.class));
    return REDISPLAY_PAGE_ACTION_RESULT;
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
    _screen.getKeywords().remove(getHttpServletRequest().getAttribute("keyword"));
    return REDISPLAY_PAGE_ACTION_RESULT;
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
    // despite the Tomahawk taglib docs, this event listener is called *before*
    // the *end* of the apply request values phase, preventing the
    // _labName.value property from being updated already
    _labName.setValue((String) event.getNewValue());
    updateLeadScreenerSelectItems();
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
  private void updateLeadScreenerSelectItems() {
    ScreeningRoomUser labHead = _labName.getSelection();
    ArrayList<ScreeningRoomUser> leadScreenerCandidates = new ArrayList<ScreeningRoomUser>();
    leadScreenerCandidates.add(labHead);
    leadScreenerCandidates.addAll(labHead.getLabMembers());
    _leadScreener = new UISelectOneBean<ScreeningRoomUser>(leadScreenerCandidates, _screen.getLeadScreener()) {
      protected String getLabel(ScreeningRoomUser t) { return t.getFullName(); } 
    };
  }

  @SuppressWarnings("unchecked")
  private <E> E getSelectedEntityOfType(Class<E> entityClass)
  {
    return (E) getHttpServletRequest().getAttribute(StringUtils.uncapitalize(entityClass.getSimpleName()));
  }

}
