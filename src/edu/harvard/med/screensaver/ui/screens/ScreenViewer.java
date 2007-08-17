// $HeadURL: svn+ssh://js163@orchestra/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/ui/libraries/ScreenViewer.java $
// $Id: ScreenViewer.java 449 2006-08-09 22:53:09Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screens;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.UsersDAO;
import edu.harvard.med.screensaver.model.screens.AbaseTestset;
import edu.harvard.med.screensaver.model.screens.AssayReadoutType;
import edu.harvard.med.screensaver.model.screens.AttachedFile;
import edu.harvard.med.screensaver.model.screens.CherryPickRequest;
import edu.harvard.med.screensaver.model.screens.FundingSupport;
import edu.harvard.med.screensaver.model.screens.LetterOfSupport;
import edu.harvard.med.screensaver.model.screens.Publication;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.ScreeningRoomActivity;
import edu.harvard.med.screensaver.model.screens.StatusItem;
import edu.harvard.med.screensaver.model.screens.StatusValue;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.WebDataAccessPolicy;
import edu.harvard.med.screensaver.ui.control.ScreensController;
import edu.harvard.med.screensaver.ui.util.JSFUtils;
import edu.harvard.med.screensaver.ui.util.UISelectManyBean;
import edu.harvard.med.screensaver.ui.util.UISelectManyEntityBean;
import edu.harvard.med.screensaver.ui.util.UISelectOneBean;
import edu.harvard.med.screensaver.ui.util.UISelectOneEntityBean;
import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.log4j.Logger;

public class ScreenViewer extends AbstractBackingBean
{
  // TODO: disabling editing until ScreenDB is fully replaced by Screensaver
  // (otherwise changes made to screens will be overwritten when
  // ScreenDBSynchronizer is run)
  private static final ScreensaverUserRole EDITING_ROLE = ScreensaverUserRole./*SCREENS_ADMIN*/DEVELOPER;

  private static Logger log = Logger.getLogger(ScreenViewer.class);
  
  
  // instance data

  private ScreensController _screensController;
  private GenericEntityDAO _dao;
  private UsersDAO _usersDao;
  private WebDataAccessPolicy _dataAccessPolicy;
  private Screen _screen;
  private UISelectOneEntityBean<ScreeningRoomUser> _labName;
  private UISelectOneEntityBean<ScreeningRoomUser> _leadScreener;
  private UISelectManyEntityBean<ScreeningRoomUser> _collaborators;
  private FundingSupport _newFundingSupport;
  private StatusValue _newStatusValue;
  private AssayReadoutType _newAssayReadoutType = AssayReadoutType.UNSPECIFIED; // the default (as specified in reqs)
  private String _newKeyword = "";
  private boolean _isEditMode = true;
  private boolean _isAdminViewMode = false;

  
  // public property getter & setter methods

  public void setScreensController(ScreensController screensController)
  {
    _screensController = screensController;
  }
  
  public void setDao(GenericEntityDAO dao)
  {
    _dao = dao;
  }

  public void setUsersDao(UsersDAO usersDao)
  {
    _usersDao = usersDao;
  }

  public void setDataAccessPolicy(WebDataAccessPolicy dataAccessPolicy)
  {
    _dataAccessPolicy = dataAccessPolicy;
  }

  public Screen getScreen() 
  {
    return _screen;
  }

  public void setScreen(Screen screen) 
  {
    _screen = screen;
    resetView();
  }
  
  private void resetView()
  {
    _isEditMode = false;
    _isAdminViewMode = false;
    
    _newFundingSupport = null;
    _newStatusValue = null;
    _newKeyword = "";
    _newAssayReadoutType = AssayReadoutType.UNSPECIFIED;

    _labName = null;
    _leadScreener = null;
    _collaborators = null;
  }
  
  public boolean isEditMode()
  {
    return !super.isReadOnly() && _isEditMode;
  }
  
  public boolean isAdminViewMode()
  {
    return _isAdminViewMode;
  }
  
  /**
   * Determine if the current user can view the restricted screen fields.
   */
  public boolean isAllowedAccessToScreenDetails()
  {
    return isReadOnlyAdmin() || _dataAccessPolicy.isScreenerAllowedAccessToScreenDetails(_screen);
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
    ArrayList<StatusItem> statusItems = new ArrayList<StatusItem>(_screen.getStatusItems());
    Collections.sort(statusItems,
                     new Comparator<StatusItem>() {
      public int compare(StatusItem si1, StatusItem si2) 
      { 
        return si1.getStatusDate().compareTo(si2.getStatusDate()); 
      }
    });
    return new ListDataModel(statusItems);
  }

  public DataModel getScreeningRoomActivitiesDataModel()
  {
    ArrayList<ScreeningRoomActivity> screeningRoomActivities = new ArrayList<ScreeningRoomActivity>(_screen.getScreeningRoomActivities());
    Collections.sort(screeningRoomActivities,
                     new Comparator<ScreeningRoomActivity>() {
      public int compare(ScreeningRoomActivity sra1, ScreeningRoomActivity sra2) 
      { 
        return sra1.getDateOfActivity().compareTo(sra2.getDateOfActivity()); 
      }
    });
    return new ListDataModel(screeningRoomActivities);
  }

  public DataModel getCherryPickRequestsDataModel()
  {
    ArrayList<CherryPickRequest> cherryPickRequests = new ArrayList<CherryPickRequest>(_screen.getCherryPickRequests());
    Collections.sort(cherryPickRequests,
                     new Comparator<CherryPickRequest>() {
      public int compare(CherryPickRequest cpr1, CherryPickRequest cpr2) 
      { 
        return cpr1.getCherryPickRequestNumber().compareTo(cpr2.getCherryPickRequestNumber()); 
      }
    });
    return new ListDataModel(cherryPickRequests);
  }

  public DataModel getPublicationsDataModel()
  {
    ArrayList<Publication> publications = new ArrayList<Publication>(_screen.getPublications());
    Collections.sort(publications,
                     new Comparator<Publication>() {
      public int compare(Publication p1, Publication p2) 
      { 
        return p1.getAuthors().compareTo(p2.getAuthors()); 
      }
    });
    return new ListDataModel(publications);
  }

  public DataModel getLettersOfSupportDataModel()
  {
    ArrayList<LetterOfSupport> lettersOfSupport = new ArrayList<LetterOfSupport>(_screen.getLettersOfSupport());
    Collections.sort(lettersOfSupport,
                     new Comparator<LetterOfSupport>() {
      public int compare(LetterOfSupport los1, LetterOfSupport los2) 
      { 
        return los1.getDateWritten().compareTo(los2.getDateWritten()); 
      }
    });
    return new ListDataModel(lettersOfSupport);
  }

  public DataModel getAttachedFilesDataModel()
  {
    ArrayList<AttachedFile> attachedFiles = new ArrayList<AttachedFile>(_screen.getAttachedFiles());
    Collections.sort(attachedFiles,
                     new Comparator<AttachedFile>() {
      public int compare(AttachedFile af1, AttachedFile af2) 
      { 
        return af1.getFilename().compareTo(af2.getFilename()); 
      }
    });
    return new ListDataModel(attachedFiles);
  }

  public DataModel getFundingSupportsDataModel()
  {
    return new ListDataModel(new ArrayList<FundingSupport>(_screen.getFundingSupports()));
  }

  public DataModel getAssayReadoutTypesDataModel()
  {
    ArrayList<AttachedFile> attachedFiles = new ArrayList<AttachedFile>(_screen.getAttachedFiles());
    Collections.sort(attachedFiles,
                     new Comparator<AttachedFile>() {
      public int compare(AttachedFile af1, AttachedFile af2) 
      { 
        return af1.getFilename().compareTo(af2.getFilename()); 
      }
    });
    return new ListDataModel(new ArrayList<AssayReadoutType>(_screen.getAssayReadoutTypes()));
  }

  public String getAbaseTestsets()
  {
    return StringUtils.makeListString(new ArrayList<AbaseTestset>(_screen.getAbaseTestsets()), ", ");
  }

  public DataModel getKeywordsDataModel()
  {
    ArrayList<String> keywords = new ArrayList<String>(_screen.getKeywords());
    Collections.sort(keywords);
    return new ListDataModel(keywords);
  }
  
  public String getKeywords()
  {
    ArrayList<String> keywords = new ArrayList<String>(_screen.getKeywords());
    Collections.sort(keywords);
    return StringUtils.makeListString(keywords, ", ");
  }

  public UISelectOneBean<ScreeningRoomUser> getLabName()
  {
    if (_labName == null) {
      _labName = new UISelectOneEntityBean<ScreeningRoomUser>(_usersDao.findAllLabHeads(), _screen.getLabHead(), _dao) { 
        protected String getLabel(ScreeningRoomUser t) { return t.getLabName(); } 
      };
    }
    return _labName;
  }

  /**
   * Get a list of SelectItems for selecting the screen's collaborators.
   */
  public UISelectManyBean<ScreeningRoomUser> getCollaborators()
  {
    if (_collaborators == null) {
      _collaborators =
        new UISelectManyEntityBean<ScreeningRoomUser>(_usersDao.findCandidateCollaborators(),
                                                      _screen.getCollaborators(),
                                                      _dao)
        {
          protected String getLabel(ScreeningRoomUser t)
          {
            return t.getFullNameLastFirst();
          }
        };
    }
    return _collaborators;
  }
  
  public UISelectOneBean<ScreeningRoomUser> getLeadScreener()
  {
    if (_leadScreener == null) {
      updateLeadScreenerSelectItems();
    }
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
  
  public String toggleAdminViewMode()
  {
    _isEditMode = false;
    _isAdminViewMode ^= true;
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public String cancelAdminViewMode()
  {
    _isAdminViewMode = false;
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  public String editScreen()
  {
    _isEditMode = true;
    return _screensController.editScreen(_screen);
  }
  
  public String cancelEdit() {
    // edits are discarded (and edit mode is cancelled) by virtue of controller reloading the screen entity from the database
    return _screensController.viewLastScreen();
  }
  
  /**
   * A command to save the user's edits.
   */
  public String saveScreen() {
    _isEditMode = false;
    return _screensController.saveScreen(_screen,
                                         new DAOTransaction() 
    {
      public void runTransaction() 
      {
        _screen.setLabHead(getLabName().getSelection());
        _screen.setLeadScreener(getLeadScreener().getSelection());
        _screen.setCollaboratorsList(getCollaborators().getSelections());
      }
    });
  }

  public String addStatusItem()
  {
    return _screensController.addStatusItem(_screen, _newStatusValue);
  }
  
  public String deleteStatusItem()
  {
    return _screensController.deleteStatusItem(_screen, getSelectedEntityOfType(StatusItem.class));
  }
  
  public String addCherryPickRequest()
  {
    // TODO: save screen
    return _screensController.createCherryPickRequest(_screen);
  }
  
  // TODO: save & go to screening room activity viewer
  public String addScreeningRoomActivity()
  {
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public String copyScreeningRoomActivity()
  {
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public String copyCherryPickRequest()
  {
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public String viewCherryPickRequest()
  {
    return _screensController.viewCherryPickRequest((CherryPickRequest) getRequestMap().get("cherryPickRequest"));
  }  
  
  public String viewScreeningRoomActivity()
  {
    // TODO: implement
    return VIEW_SCREENING_ROOM_ACTIVITY_ACTION_RESULT;  
  }  
  
  public String addPublication()
  {
    return _screensController.addPublication(_screen);
  }
  
  public String deletePublication()
  {
    return _screensController.deletePublication(
      _screen,
      getSelectedEntityOfType(Publication.class));
  }
  
  public String addLetterOfSupport()
  {
    return _screensController.addLetterOfSupport(_screen);
  }
  
  public String deleteLetterOfSupport()
  {
    return _screensController.deleteLetterOfSupport(
      _screen,
      getSelectedEntityOfType(LetterOfSupport.class));
  }
  
  public String addAttachedFile()
  {
    return _screensController.addAttachedFile(_screen);
  }
  
  public String deleteAttachedFile()
  {
    return _screensController.deleteAttachedFile(
      _screen,
      getSelectedEntityOfType(AttachedFile.class));
  }
  
  public String addFundingSupport()
  {
    return _screensController.addFundingSupport(_screen, _newFundingSupport);
  }
  
  public String deleteFundingSupport()
  {
    return _screensController.deleteFundingSupport(
      _screen,
      getSelectedEntityOfType(FundingSupport.class));
  }
  
  public String addKeyword()
  {
    return _screensController.addKeyword(_screen, _newKeyword);
  }
  
  public String deleteKeyword()
  {
    return _screensController.deleteKeyword(
      _screen,
      (String) getHttpServletRequest().getAttribute("keyword"));
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

  public String viewBillingInformation()
  {
    return VIEW_BILLING_INFORMATION_ACTION_RESULT;
  }

  
  /* JSF Action event listeners */

  public void update(ValueChangeEvent event) {
    // despite the Tomahawk taglib docs, this event listener is called *before*
    // the *end* of the apply request values phase, preventing the
    // _labName.value property from being updated already
    getLabName().setValue((String) event.getNewValue());
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
    _dao.doInTransaction(new DAOTransaction() 
    {
      public void runTransaction()
      {
        ScreeningRoomUser labHead = _labName.getSelection();
        ArrayList<ScreeningRoomUser> leadScreenerCandidates = new ArrayList<ScreeningRoomUser>();
        if (labHead != null) {
          leadScreenerCandidates.add(labHead);
          leadScreenerCandidates.addAll(labHead.getLabMembers());
        }
        _leadScreener = new UISelectOneEntityBean<ScreeningRoomUser>(leadScreenerCandidates, _screen.getLeadScreener(), _dao) {
          protected String getLabel(ScreeningRoomUser t) { return t.getFullNameLastFirst(); } 
        };
      }
    });
  }

  @SuppressWarnings("unchecked")
  private <E> E getSelectedEntityOfType(Class<E> entityClass)
  {
    return (E) getHttpServletRequest().getAttribute(StringUtils.uncapitalize(entityClass.getSimpleName()));
  }
}
