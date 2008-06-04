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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.ScreenDAO;
import edu.harvard.med.screensaver.db.UsersDAO;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.screens.AbaseTestset;
import edu.harvard.med.screensaver.model.screens.AssayReadoutType;
import edu.harvard.med.screensaver.model.screens.AttachedFile;
import edu.harvard.med.screensaver.model.screens.FundingSupport;
import edu.harvard.med.screensaver.model.screens.LabActivity;
import edu.harvard.med.screensaver.model.screens.LetterOfSupport;
import edu.harvard.med.screensaver.model.screens.Publication;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.Screening;
import edu.harvard.med.screensaver.model.screens.StatusItem;
import edu.harvard.med.screensaver.model.screens.StatusValue;
import edu.harvard.med.screensaver.model.screens.StudyType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.ui.WebDataAccessPolicy;
import edu.harvard.med.screensaver.ui.activities.ActivityViewer;
import edu.harvard.med.screensaver.ui.cherrypickrequests.CherryPickRequestViewer;
import edu.harvard.med.screensaver.ui.searchresults.ScreenSearchResults;
import edu.harvard.med.screensaver.ui.util.EditableViewer;
import edu.harvard.med.screensaver.ui.util.JSFUtils;
import edu.harvard.med.screensaver.ui.util.UISelectOneBean;
import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;

public class ScreenDetailViewer extends StudyDetailViewer implements EditableViewer
{
  private static final ScreensaverUserRole EDITING_ROLE = ScreensaverUserRole.SCREENS_ADMIN;

  private static Logger log = Logger.getLogger(ScreenDetailViewer.class);


  // instance data

  private ScreenDetailViewer _thisProxy;
  private GenericEntityDAO _dao;
  private ScreenDAO _screenDao;
  private WebDataAccessPolicy _dataAccessPolicy;
  private ScreenViewer _screenViewer;
  private ScreenSearchResults _screensBrowser;
  private ActivityViewer _activityViewer;
  private CherryPickRequestViewer _cherryPickRequestViewer;
  private AbstractBackingBean _returnToViewAfterEdit;

  private Screen _screen;
  private boolean _isEditMode = true;
  private boolean _isAdminViewMode = false;
  private FundingSupport _newFundingSupport;
  private UISelectOneBean<StatusValue> _newStatusItemValue;
  private LocalDate _newStatusItemDate;
  private AssayReadoutType _newAssayReadoutType = AssayReadoutType.UNSPECIFIED; // the default (as specified in reqs)



  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected ScreenDetailViewer()
  {
  }

  public ScreenDetailViewer(ScreenDetailViewer thisProxy,
                            GenericEntityDAO dao,
                            ScreenDAO screenDao,
                            UsersDAO usersDao,
                            WebDataAccessPolicy dataAccessPolicy,
                            ScreenViewer screenViewer,
                            ScreenSearchResults screensBrowser,
                            ActivityViewer activityViewer,
                            CherryPickRequestViewer cherryPickRequestViewer)
  {
    super(dao, usersDao);
    _thisProxy = thisProxy;
    _dao = dao;
    _screenDao = screenDao;
    _dataAccessPolicy = dataAccessPolicy;
    _screenViewer = screenViewer;
    _screensBrowser = screensBrowser;
    _activityViewer = activityViewer;
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

  @Override
  public String reload()
  {
    return _screenViewer.viewScreen(getScreen());
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

  public UISelectOneBean<StatusValue> getNewStatusItemValue()
  {
    if (_newStatusItemValue == null) {
      _newStatusItemValue = new UISelectOneBean<StatusValue>(_screen.getCandidateStatusValues());
    }
    return _newStatusItemValue;
  }

  public LocalDate getNewStatusItemDate()
  {
    return _newStatusItemDate;
  }

  public void setNewStatusItemDate(LocalDate newStatusItemDate)
  {
    _newStatusItemDate = newStatusItemDate;
  }

  public DataModel getStatusItemsDataModel()
  {
    return new ListDataModel(new ArrayList<StatusItem>(getScreen().getStatusItems()));
  }

  public DataModel getLabActivitiesDataModel()
  {
    ArrayList<LabActivity> labActivities = new ArrayList<LabActivity>(getScreen().getLabActivities());
    Collections.sort(labActivities,
                     new Comparator<LabActivity>() {
      public int compare(LabActivity sra1, LabActivity sra2)
      {
        return sra1.getDateOfActivity().compareTo(sra2.getDateOfActivity());
      }
    });
    return new ListDataModel(labActivities);
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
  public String editNewScreen(AbstractBackingBean returnToViewerAfterEdit)
  {
    ScreensaverUser user = getScreensaverUser();
    if (!(user instanceof AdministratorUser &&
      ((AdministratorUser) user).isUserInRole(ScreensaverUserRole.SCREENS_ADMIN))) {
      showMessage("unauthorizedOperation", "add a new screen");
    }

    Screen screen = new Screen();
    screen.setStudyType(StudyType.IN_VITRO);
    screen.setScreenNumber(_screenDao.findNextScreenNumber());
    setScreen(screen);
    _isEditMode = true;
    _returnToViewAfterEdit = returnToViewerAfterEdit;
    return VIEW_SCREEN_DETAIL;
  }

  @UIControllerMethod
  public String edit()
  {
    _isEditMode = true;
    return VIEW_SCREEN_DETAIL;
  }

  @UIControllerMethod
  public String cancel()
  {
    _isEditMode = false;
    if (_returnToViewAfterEdit != null) {
      return _returnToViewAfterEdit.reload();
    }
    return _screenViewer.viewScreen(_screen);
  }

  @UIControllerMethod
  @Transactional
  public String save()
  {
    _isEditMode = false;
    // TODO: would like to use this code, since it handles both new and extant
    // Screens, but I think UISelectOneEntityBean's auto-loading of entities is
    // actually getting in the way, since it causes NonUniqueObjectException
//  getScreen().setLabHead(getLabName().getSelection());
//  getScreen().setLeadScreener(getLeadScreener().getSelection());
//  _dao.saveOrUpdateEntity(getScreen());
    // instead, we handle each case separately:
    if (getScreen().getEntityId() == null) {
      getScreen().setLabHead(getLabName().getSelection());
      getScreen().setLeadScreener(getLeadScreener().getSelection());
      _dao.persistEntity(getScreen());
    }
    else {
      _dao.reattachEntity(getScreen());
      getScreen().setLabHead(getLabName().getSelection());
      getScreen().setLeadScreener(getLeadScreener().getSelection());
    }

    _dao.flush();
    //_screensBrowser.refetch(); // TODO: confuses viewScreen() when attempting to redisplay same screen within screens browser
    return _screenViewer.viewScreen(_screen);
  }

  @UIControllerMethod
  public String addStatusItem()
  {
    if (getNewStatusItemValue().getSelection() != null && getNewStatusItemDate() != null) {
      try {
        getScreen().createStatusItem(getNewStatusItemDate(),
                                     getNewStatusItemValue().getSelection());
      }
      catch (BusinessRuleViolationException e) {
        showMessage("businessError", e.getMessage());
      }
      _newStatusItemValue = null; // reset
      _newStatusItemDate = null;
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String deleteStatusItem()
  {
    getScreen().getStatusItems().remove(getSelectedEntityOfType(StatusItem.class));
    _newStatusItemValue = null; // reset
    _newStatusItemDate = null;
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
      getScreen().createLetterOfSupport(new LocalDate(), "");
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
  public String addLibraryScreening()
  {
    Screening screening = _screen.createLibraryScreening(_screen.getLeadScreener(),
                                                         new LocalDate());
    duplicateMostRecentScreening(screening);
    return _activityViewer.editNewActivity(screening, this);
  }

  @UIControllerMethod
  public String addRNAiCherryPickScreening()
  {
    RNAiCherryPickRequest cpr = (RNAiCherryPickRequest) getCherryPickRequestsDataModel().getRowData();
    if (cpr == null) {
      reportSystemError("missing CherryPickRequest argument");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    cpr = _dao.reloadEntity(cpr, true, "requestedBy", "RNAiCherryPickScreenings");
    Screening screening = _screen.createRNAiCherryPickScreening(cpr.getRequestedBy(),
                                                                new LocalDate(),
                                                                cpr);
    duplicateMostRecentScreening(screening);
    return _activityViewer.editNewActivity(screening, this);
  }

  @UIControllerMethod
  public String copyLabActivity()
  {
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String copyCherryPickRequest()
  {
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  @Transactional
  public String addCherryPickRequest()
  {
    // TODO: save screen
    Screen screen = _dao.reloadEntity(getScreen());
    CherryPickRequest cpr = screen.createCherryPickRequest();
    _dao.persistEntity(cpr);
    _dao.flush();
    return _cherryPickRequestViewer.viewCherryPickRequest(cpr);
  }

  @UIControllerMethod
  public String viewCherryPickRequest()
  {
    return _cherryPickRequestViewer.viewCherryPickRequest((CherryPickRequest) getRequestMap().get("cherryPickRequest"));
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
    _returnToViewAfterEdit = null;
    //_isAdminViewMode = false; // maintain this setting when viewing a new screen
    _newFundingSupport = null;
    _newStatusItemValue = null;
    _newStatusItemDate = null;
    _newAssayReadoutType = AssayReadoutType.UNSPECIFIED;
  }

  @SuppressWarnings("unchecked")
  private <E> E getSelectedEntityOfType(Class<E> entityClass)
  {
    return (E) getHttpServletRequest().getAttribute(StringUtils.uncapitalize(entityClass.getSimpleName()));
  }

  private void duplicateMostRecentScreening(Screening currentScreening)
  {
    SortedSet<Screening> screenings = new TreeSet<Screening>(_screen.getLabActivitiesOfType(Screening.class));
    if (screenings.isEmpty()) {
      return;
    }
    screenings.remove(currentScreening);
    if (screenings.isEmpty()) {
      return;
    }
    Screening previousScreening = screenings.last();
    currentScreening.setAssayProtocol(previousScreening.getAssayProtocol());
    currentScreening.setAssayProtocolLastModifiedDate(previousScreening.getAssayProtocolLastModifiedDate());
    currentScreening.setAssayProtocolType(previousScreening.getAssayProtocolType());
    currentScreening.setNumberOfReplicates(previousScreening.getNumberOfReplicates());
    currentScreening.setVolumeTransferredPerWell(previousScreening.getVolumeTransferredPerWell());
  }

}
