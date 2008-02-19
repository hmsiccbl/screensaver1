// $HeadURL$
// $Id$
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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataAccessException;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.UsersDAO;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.screens.AbaseTestset;
import edu.harvard.med.screensaver.model.screens.AssayReadoutType;
import edu.harvard.med.screensaver.model.screens.AttachedFile;
import edu.harvard.med.screensaver.model.screens.FundingSupport;
import edu.harvard.med.screensaver.model.screens.LetterOfSupport;
import edu.harvard.med.screensaver.model.screens.Publication;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.ScreeningRoomActivity;
import edu.harvard.med.screensaver.model.screens.StatusItem;
import edu.harvard.med.screensaver.model.screens.StatusValue;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.ui.WebDataAccessPolicy;
import edu.harvard.med.screensaver.ui.searchresults.ScreenSearchResults;
import edu.harvard.med.screensaver.ui.util.JSFUtils;
import edu.harvard.med.screensaver.util.StringUtils;

public class ScreenDetailViewer extends StudyDetailViewer
{
  // TODO: disabling editing until ScreenDB is fully replaced by Screensaver
  // (otherwise changes made to screens will be overwritten when
  // ScreenDBSynchronizer is run)
  private static final ScreensaverUserRole EDITING_ROLE = ScreensaverUserRole./*SCREENS_ADMIN*/DEVELOPER;

  private static Logger log = Logger.getLogger(ScreenDetailViewer.class);


  // instance data

  private GenericEntityDAO _dao;
  private WebDataAccessPolicy _dataAccessPolicy;
  private ScreenViewer _screenViewer;
  private ScreenSearchResults _screensBrowser;
  private CherryPickRequestViewer _cherryPickRequestViewer;

  private Screen _screen;
  private boolean _isEditMode = true;
  private boolean _isAdminViewMode = false;
  private FundingSupport _newFundingSupport;
  private StatusValue _newStatusValue;
  private AssayReadoutType _newAssayReadoutType = AssayReadoutType.UNSPECIFIED; // the default (as specified in reqs)
  private String _newKeyword = "";



  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected ScreenDetailViewer()
  {
  }

  public ScreenDetailViewer(GenericEntityDAO dao,
                            UsersDAO usersDao,
                            WebDataAccessPolicy dataAccessPolicy,
                            ScreenViewer screenViewer,
                            ScreenSearchResults screensBrowser,
                            CherryPickRequestViewer cherryPickRequestViewer)
  {
    super(dao, usersDao);
    _dao = dao;
    _dataAccessPolicy = dataAccessPolicy;
    _screenViewer = screenViewer;
    _screensBrowser = screensBrowser;
    _cherryPickRequestViewer = cherryPickRequestViewer;
  }


  // public methods

  public void setScreen(Screen screen)
  {
    setStudy(screen);
    _screen = screen;
    resetView();
  }

  public Screen getScreen()
  {
    return _screen;
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
  // TODO: when the Study/Screen/IccbScreen hierarchy is ready, we might be able
  // to use the DataAccessPolicy to restrict access to particular fields, by
  // virtue of which subclass the field is from. We might need a fourth subclass
  // IccbAdminScreen to contain admin-only fields
  public boolean isAllowedAccessToScreenDetails()
  {
    return isReadAdmin() ||
           _dataAccessPolicy.isScreenerAllowedAccessToScreenDetails(getScreen());
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

  public DataModel getStatusItemsDataModel()
  {
    ArrayList<StatusItem> statusItems = new ArrayList<StatusItem>(getScreen().getStatusItems());
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
    ArrayList<ScreeningRoomActivity> screeningRoomActivities = new ArrayList<ScreeningRoomActivity>(getScreen().getScreeningRoomActivities());
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
    ArrayList<CherryPickRequest> cherryPickRequests = new ArrayList<CherryPickRequest>(getScreen().getCherryPickRequests());
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
    ArrayList<Publication> publications = new ArrayList<Publication>(getScreen().getPublications());
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
    ArrayList<LetterOfSupport> lettersOfSupport = new ArrayList<LetterOfSupport>(getScreen().getLettersOfSupport());
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
    ArrayList<AttachedFile> attachedFiles = new ArrayList<AttachedFile>(getScreen().getAttachedFiles());
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
    return new ListDataModel(new ArrayList<FundingSupport>(getScreen().getFundingSupports()));
  }

  public DataModel getAssayReadoutTypesDataModel()
  {
    ArrayList<AttachedFile> attachedFiles = new ArrayList<AttachedFile>(getScreen().getAttachedFiles());
    Collections.sort(attachedFiles,
                     new Comparator<AttachedFile>() {
      public int compare(AttachedFile af1, AttachedFile af2)
      {
        return af1.getFilename().compareTo(af2.getFilename());
      }
    });
    return new ListDataModel(new ArrayList<AssayReadoutType>(getScreen().getAssayReadoutTypes()));
  }

  public String getAbaseTestsets()
  {
    return StringUtils.makeListString(new ArrayList<AbaseTestset>(getScreen().getAbaseTestsets()), ", ");
  }

  public DataModel getKeywordsDataModel()
  {
    ArrayList<String> keywords = new ArrayList<String>(getScreen().getKeywords());
    Collections.sort(keywords);
    return new ListDataModel(keywords);
  }

  public String getKeywords()
  {
    ArrayList<String> keywords = new ArrayList<String>(getScreen().getKeywords());
    Collections.sort(keywords);
    return StringUtils.makeListString(keywords, ", ");
  }

  public List<SelectItem> getNewStatusValueSelectItems()
  {
    Set<StatusValue> candiateStatusValues = new HashSet<StatusValue>(Arrays.asList(StatusValue.values()));
    for (StatusItem statusItem : getScreen().getStatusItems()) {
      candiateStatusValues.remove(statusItem.getStatusValue());
    }
    return JSFUtils.createUISelectItems(candiateStatusValues);
  }

  public List<SelectItem> getNewFundingSupportSelectItems()
  {
    Set<FundingSupport> candiateFundingSupports = new HashSet<FundingSupport>(Arrays.asList(FundingSupport.values()));
    candiateFundingSupports.removeAll(getScreen().getFundingSupports());
    return JSFUtils.createUISelectItems(candiateFundingSupports);
  }

  public List<SelectItem> getNewAssayReadoutTypeSelectItems()
  {
    Set<AssayReadoutType> candiateAssayReadoutTypes = new HashSet<AssayReadoutType>(Arrays.asList(AssayReadoutType.values()));
    candiateAssayReadoutTypes.removeAll(getScreen().getAssayReadoutTypes());
    return JSFUtils.createUISelectItems(candiateAssayReadoutTypes);
  }


  /* JSF Application methods */

  @UIControllerMethod
  public String editScreen()
  {
    _isEditMode = true;
    try {
      _dao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          _dao.reattachEntity(getScreen()); // checks if up-to-date
          _dao.need(getScreen(), "labHead.labMembers");
        }
      });
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    catch (ConcurrencyFailureException e) {
      showMessage("concurrentModificationConflict");
    }
    catch (DataAccessException e) {
      showMessage("databaseOperationFailed", e.getMessage());
    }
    return _screenViewer.viewLastScreen(); // on error, reload (and not in edit mode)
  }

  @UIControllerMethod
  public String toggleAdminViewMode()
  {
    _isEditMode = false;
    _isAdminViewMode ^= true;
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String cancelAdminViewMode()
  {
    _isAdminViewMode = false;
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String cancelEdit()
  {
    // edits are discarded (and edit mode is canceled) by virtue of controller
    // reloading the screen entity from the database
    return _screenViewer.viewLastScreen();
  }

  @UIControllerMethod
  public String saveScreen()
  {
    _isEditMode = false;
    try {
      _dao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          _dao.reattachEntity(getScreen());
          getScreen().setLabHead(getLabName().getSelection());
          getScreen().setLeadScreener(getLeadScreener().getSelection());
          getScreen().setCollaboratorsList(getCollaborators().getSelections());
        }
      });
    }
    catch (ConcurrencyFailureException e) {
      showMessage("concurrentModificationConflict");
      _screenViewer.viewLastScreen();
    }
    catch (DataAccessException e) {
      showMessage("databaseOperationFailed", e.getMessage());
      _screenViewer.viewLastScreen();
    }
    _screensBrowser.refetch();
    return VIEW_SCREEN;
  }

  @UIControllerMethod
  public String addStatusItem()
  {
    if (_newStatusValue != null) {
      try {
        getScreen().createStatusItem(
                       new Date(),
                       _newStatusValue);
      }
      catch (DuplicateEntityException e) {
        showMessage("screens.duplicateEntity", "status item");
      }
      setNewStatusValue(null);
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String deleteStatusItem()
  {
    getScreen().getStatusItems().remove(getSelectedEntityOfType(StatusItem.class));
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String addPublication()
  {
    try {
      getScreen().createPublication("<new>", "", "", "");
    }
    catch (DuplicateEntityException e) {
      showMessage("screens.duplicateEntity", "publication");
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String deletePublication()
  {
    getScreen().getPublications().remove(getSelectedEntityOfType(Publication.class));
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String addLetterOfSupport()
  {
    try {
      getScreen().createLetterOfSupport(new Date(), "");
    }
    catch (DuplicateEntityException e) {
      showMessage("screens.duplicateEntity", "letter of support");
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String deleteLetterOfSupport()
  {
    getScreen().getLettersOfSupport().remove(getSelectedEntityOfType(LetterOfSupport.class));
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String addAttachedFile()
  {
    try {
      getScreen().createAttachedFile("<new>", "");
    }
    catch (DuplicateEntityException e) {
      showMessage("screens.duplicateEntity", "attached file");
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String deleteAttachedFile()
  {
    getScreen().getAttachedFiles().remove(getSelectedEntityOfType(AttachedFile.class));
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String addFundingSupport()
  {
    if (_newFundingSupport != null) {
      if (!getScreen().addFundingSupport(_newFundingSupport)) {
        showMessage("screens.duplicateEntity", "funding support");
      }
      setNewFundingSupport(null);
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String deleteFundingSupport()
  {
    getScreen().getFundingSupports().remove(getSelectedEntityOfType(FundingSupport.class));
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String addKeyword()
  {
    if (! getScreen().addKeyword(_newKeyword)) {
      showMessage("screens.duplicateEntity", "keyword");
    }
    else {
      setNewKeyword("");
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String deleteKeyword()
  {
    getScreen().getKeywords().remove((String) getHttpServletRequest().getAttribute("keyword"));
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  // TODO: save & go to screening room activity viewer
  @UIControllerMethod
  public String addScreeningRoomActivity()
  {
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String copyScreeningRoomActivity()
  {
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String copyCherryPickRequest()
  {
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String addCherryPickRequest()
  {
    // TODO: save screen
    if (!getScreen().getScreenType().equals(ScreenType.RNAI)) {
      showMessage("applicationError", "Cherry Pick Requests can only be created for RNAi screens, currently");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }

    final CherryPickRequest[] result = new CherryPickRequest[1];
    try {
      _dao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          Screen screen = _dao.reloadEntity(getScreen());
          result[0] =  screen.createCherryPickRequest();
        }
      });
    }
    catch (DataAccessException e) {
      showMessage("databaseOperationFailed", e.getMessage());
    }
    return _cherryPickRequestViewer.viewCherryPickRequest(result[0]);
  }

  @UIControllerMethod
  public String viewCherryPickRequest()
  {
    // TODO: can we also use our getSelectedEntityOfType() here?
    return  _cherryPickRequestViewer.viewCherryPickRequest((CherryPickRequest) getRequestMap().get("cherryPickRequest"));
  }

  @UIControllerMethod
  public String viewScreeningRoomActivity()
  {
    // TODO: implement
    return VIEW_SCREENING_ROOM_ACTIVITY_ACTION_RESULT;
  }

  @UIControllerMethod
  public String viewCollaborator()
  {
    // TODO: implement
    return VIEW_SCREENING_ROOM_USER_ACTION_RESULT;
  }

  @UIControllerMethod
  public String viewCollaboratorLabHead()
  {
    // TODO: implement
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String viewLabHead()
  {
    // TODO: implement
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String viewLeadScreener()
  {
    // TODO: implement
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String viewBillingInformation()
  {
    return VIEW_BILLING_INFORMATION_ACTION_RESULT;
  }


  // protected methods

  protected ScreensaverUserRole getEditableAdminRole()
  {
    return EDITING_ROLE;
  }


  // private methods

  private void resetView()
  {
    _isEditMode = false;
    _isAdminViewMode = false;
    _newFundingSupport = null;
    _newStatusValue = null;
    _newKeyword = "";
    _newAssayReadoutType = AssayReadoutType.UNSPECIFIED;
  }

  @SuppressWarnings("unchecked")
  private <E> E getSelectedEntityOfType(Class<E> entityClass)
  {
    return (E) getHttpServletRequest().getAttribute(StringUtils.uncapitalize(entityClass.getSimpleName()));
  }
}
