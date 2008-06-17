// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screens;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.ScreenDAO;
import edu.harvard.med.screensaver.db.UsersDAO;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.screens.AssayReadoutType;
import edu.harvard.med.screensaver.model.screens.AttachedFile;
import edu.harvard.med.screensaver.model.screens.AttachedFileType;
import edu.harvard.med.screensaver.model.screens.BillingItem;
import edu.harvard.med.screensaver.model.screens.FundingSupport;
import edu.harvard.med.screensaver.model.screens.LabActivity;
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
import edu.harvard.med.screensaver.ui.searchresults.CherryPickRequestSearchResults;
import edu.harvard.med.screensaver.ui.searchresults.LabActivitySearchResults;
import edu.harvard.med.screensaver.ui.searchresults.ScreenSearchResults;
import edu.harvard.med.screensaver.ui.util.EditableViewer;
import edu.harvard.med.screensaver.ui.util.JSFUtils;
import edu.harvard.med.screensaver.ui.util.UISelectOneBean;
import edu.harvard.med.screensaver.util.eutils.PublicationInfoProvider;

import org.apache.log4j.Logger;
import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.joda.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;

public class ScreenDetailViewer extends StudyDetailViewer implements EditableViewer
{
  private static final int CHILD_ENTITY_TABLE_MAX_ROWS = 10;

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
  private PublicationInfoProvider _publicationInfoProvider;
  private LabActivitySearchResults _labActivitySearchResults;
  private CherryPickRequestSearchResults _cherryPickRequestSearchResults;

  private Screen _screen;
  private AbstractBackingBean _returnToViewAfterEdit;
  private boolean _isEditMode = true;
  private boolean _isAdminViewMode = false;
  private boolean _isPublishableProtocolDetailsCollapsed = true;
  private boolean _isBillingInformationCollapsed = true;
  private UISelectOneBean<FundingSupport> _newFundingSupport;
  private UISelectOneBean<StatusValue> _newStatusItemValue;
  private LocalDate _newStatusItemDate;
  //private UISelectOneBean<AssayReadoutType> _newAssayReadoutType;
  // TODO: use AttachedFile DTO instead
  private String _newAttachedFileName;
  private UISelectOneBean<AttachedFileType> _newAttachedFileType;
  private UploadedFile _uploadedAttachedFileContents;
  private String _newAttachedFileContents;

  private Publication _newPublication;
  private BillingItem _newBillingItem;


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
                            CherryPickRequestViewer cherryPickRequestViewer,
                            PublicationInfoProvider publicationInfoProvider,
                            LabActivitySearchResults labActivitySearchResults,
                            CherryPickRequestSearchResults cherryPickRequestSearchResults)
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
    _publicationInfoProvider = publicationInfoProvider;
    _labActivitySearchResults = labActivitySearchResults;
    _cherryPickRequestSearchResults = cherryPickRequestSearchResults;
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
    return _screenViewer.reload();
  }

  public boolean isEditMode()
  {
    return !super.isReadOnly() && _isEditMode;
  }

  public boolean isAdminViewMode()
  {
    return _isAdminViewMode;
  }

  public boolean isPublishableProtocolDetailsCollapsed()
  {
    return _isPublishableProtocolDetailsCollapsed;
  }

  public void setPublishableProtocolDetailsCollapsed(boolean isPublishableProtocolDetailsCollapsed)
  {
    _isPublishableProtocolDetailsCollapsed = isPublishableProtocolDetailsCollapsed;
  }

  public boolean isBillingInformationCollapsed()
  {
    return _isBillingInformationCollapsed;
  }

  public void setBillingInformationCollapsed(boolean isBillingInformationCollapsed)
  {
    _isBillingInformationCollapsed = isBillingInformationCollapsed;
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

//  public UISelectOneBean<AssayReadoutType> getNewAssayReadoutType()
//  {
//    if (_newAssayReadoutType == null) {
//      Set<AssayReadoutType> candidateAssayReadoutTypes = new HashSet<AssayReadoutType>(Arrays.asList(AssayReadoutType.values()));
//      candidateAssayReadoutTypes.removeAll(getScreen().getAssayReadoutTypes());
//      _newAssayReadoutType = new UISelectOneBean<AssayReadoutType>(candidateAssayReadoutTypes, AssayReadoutType.UNSPECIFIED); // the default (as specified in reqs));
//    }
//    return _newAssayReadoutType;
//  }

  public UISelectOneBean<FundingSupport> getNewFundingSupport()
  {
    if (_newFundingSupport == null) {
      Set<FundingSupport> candidateFundingSupports = new TreeSet<FundingSupport>(Arrays.asList(FundingSupport.values()));
      candidateFundingSupports.removeAll(getScreen().getFundingSupports());
      _newFundingSupport = new UISelectOneBean<FundingSupport>(candidateFundingSupports);
    }
    return _newFundingSupport;
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
    Collections.reverse(labActivities);
    return new ListDataModel(labActivities.subList(0, Math.min(CHILD_ENTITY_TABLE_MAX_ROWS, getScreen().getLabActivities().size())));
  }

  public DataModel getCherryPickRequestsDataModel()
  {
    ArrayList<CherryPickRequest> cherryPickRequests = new ArrayList<CherryPickRequest>(getScreen().getCherryPickRequests());
    Collections.sort(cherryPickRequests,
                     new Comparator<CherryPickRequest>() {
      public int compare(CherryPickRequest cpr1, CherryPickRequest cpr2)
      {
        return -1 * cpr1.getCherryPickRequestNumber().compareTo(cpr2.getCherryPickRequestNumber());
      }
    });
    return new ListDataModel(new ArrayList<CherryPickRequest>(cherryPickRequests.subList(0, Math.min(CHILD_ENTITY_TABLE_MAX_ROWS, cherryPickRequests.size()))));
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

  public Publication getNewPublication()
  {
    if (_newPublication == null) {
      _newPublication = new Publication();
    }
    return _newPublication;
  }

  public String getNewAttachedFileName()
  {
    return _newAttachedFileName;
  }

  public void setNewAttachedFileName(String newAttachedFileName)
  {
    _newAttachedFileName = newAttachedFileName;
  }

  public UISelectOneBean<AttachedFileType> getNewAttachedFileType()
  {
    if (_newAttachedFileType == null) {
      _newAttachedFileType = new UISelectOneBean<AttachedFileType>(Arrays.asList(AttachedFileType.values()));
    }
    return _newAttachedFileType;
  }

  public String getNewAttachedFileContents()
  {
    return _newAttachedFileContents;
  }

  public void setNewAttachedFileContents(String newAttachedFileContents)
  {
    _newAttachedFileContents = newAttachedFileContents;
  }

  public void setUploadedAttachedFileContents(UploadedFile uploadedFile)
  {
    _uploadedAttachedFileContents = uploadedFile;
  }

  public UploadedFile getUploadedAttachedFileContents()
  {
    return _uploadedAttachedFileContents;
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

//  public UISelectOneBean<AssayReadoutType> getNewAssayReadoutType()
//  {
//    if (_newAssayReadoutType == null) {
//      Set<AssayReadoutType> candidateAssayReadoutTypes = new TreeSet<AssayReadoutType>(Arrays.asList(AssayReadoutType.values()));
//      candidateAssayReadoutTypes.removeAll(getScreen().getAssayReadoutTypes());
//      AssayReadoutType defaultSelection = AssayReadoutType.UNSPECIFIED; // the default (as specified in reqs));
//      if (!candidateAssayReadoutTypes.contains(defaultSelection)) {
//        defaultSelection = null;
//      }
//      _newAssayReadoutType = new UISelectOneBean<AssayReadoutType>(candidateAssayReadoutTypes, defaultSelection);
//    }
//    return _newAssayReadoutType;
//  }

  public DataModel getAssayReadoutTypesDataModel()
  {
    return new ListDataModel(new ArrayList<AssayReadoutType>(getScreen().getAssayReadoutTypes()));
  }

//  @UIControllerMethod
//  public String addAssayReadoutType()
//  {
//    if (_newAssayReadoutType != null) {
//      getScreen().addAssayReadoutType(_newAssayReadoutType.getSelection());
//      _newAssayReadoutType = null;
//    }
//    return REDISPLAY_PAGE_ACTION_RESULT;
//  }

  public DataModel getBillingItemsDataModel()
  {
    ArrayList<BillingItem> billingItems = new ArrayList<BillingItem>();
    if (getScreen().getBillingInformation() != null) {
      billingItems.addAll(getScreen().getBillingInformation().getBillingItems());
    }
    Collections.sort(billingItems,
                     new Comparator<BillingItem>() {
      public int compare(BillingItem bi1, BillingItem bi2)
      {
        return bi1.getDateFaxed().compareTo(bi2.getDateFaxed());
      }
    });
    return new ListDataModel(billingItems);
  }

  public BillingItem getNewBillingItem()
  {
    if (_newBillingItem == null) {
      _newBillingItem = new BillingItem();
    }
    return _newBillingItem;
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
    getScreen().getStatusItems().remove(getRequestMap().get("element"));
    _newStatusItemValue = null; // reset
    _newStatusItemDate = null;
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String addPublication()
  {
    getScreen().createPublication(_newPublication);
    _newPublication = null;
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  public String lookupPublicationByPubMedId()
  {
    try {
      _newPublication =
        _publicationInfoProvider.getPublicationForPubmedId(_newPublication.getPubmedId());
      if (_newPublication == null) {
        reportApplicationError("Publication for PubMed ID " + _newAttachedFileContents + " was not found");
      }
    }
    catch (Exception e) {
      reportSystemError(e.getMessage());
    }

    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String deletePublication()
  {
    Publication publication = (Publication) getRequestMap().get("element");
    if (publication != null) {
      getScreen().getPublications().remove(publication);
      _newPublication = null;
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String addAttachedFile()
  {
    String filename;
    InputStream contentsInputStream;
    if (_uploadedAttachedFileContents != null) {
      filename = _uploadedAttachedFileContents.getName();
      try {
        contentsInputStream = _uploadedAttachedFileContents.getInputStream();
      }
      catch (IOException e) {
        reportApplicationError(e.getMessage());
        return REDISPLAY_PAGE_ACTION_RESULT;
      }
      _uploadedAttachedFileContents = null;
    }
    else {
      filename = _newAttachedFileName;
      contentsInputStream = new ByteArrayInputStream(_newAttachedFileContents.getBytes());
    }

    try {
      if (filename == null || filename.trim().length() == 0) {
        showMessage("requiredValue", "Attached File Name");
        return REDISPLAY_PAGE_ACTION_RESULT;
      }
      getScreen().createAttachedFile(filename,
                                     _newAttachedFileType.getSelection(),
                                     contentsInputStream);
    }
    catch (IOException e) {
      reportApplicationError("could not attach the file contents");
    }
    _newAttachedFileName = null;
    _newAttachedFileContents = null;
    _newAttachedFileType = null;
    _uploadedAttachedFileContents = null;
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String deleteAttachedFile()
  {
    AttachedFile attachedFile = (AttachedFile) getRequestMap().get("attachedFile");
    getScreen().getAttachedFiles().remove(attachedFile);
    _newAttachedFileName = null;
    _newAttachedFileContents = null;
    _newAttachedFileType = null;
    _uploadedAttachedFileContents = null;
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  @Transactional
  public String downloadAttachedFile() throws IOException, SQLException
  {
    AttachedFile attachedFile = (AttachedFile) getRequestMap().get("attachedFile");
    // attachedFile must be Hibernate-managed in order to access the contents (a LOB type)
    // if attachedFile is transient, we should not attempt to reattach, and contents will be accessible
    if (attachedFile.getAttachedFileId() != null) {
      attachedFile = _dao.reloadEntity(attachedFile, true);
    }
    if (attachedFile != null) {
      JSFUtils.handleUserDownloadRequest(getFacesContext(),
                                         attachedFile.getFileContents().getBinaryStream(),
                                         attachedFile.getFilename(),
                                         null);
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String addFundingSupport()
  {
    if (_newFundingSupport != null && _newFundingSupport.getSelection() != null) {
      getScreen().addFundingSupport(_newFundingSupport.getSelection());
      _newFundingSupport = null;
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String deleteFundingSupport()
  {
    getScreen().getFundingSupports().remove(getRequestMap().get("element"));
    _newFundingSupport = null;
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
    Screen screen = _dao.reloadEntity(getScreen());
    CherryPickRequest cpr = screen.createCherryPickRequest();
    _dao.persistEntity(cpr);
    _dao.flush();
    reload();
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
  public String addBillingInformation()
  {
    if (_screen.getBillingInformation() == null) {
      _screen.createBillingInformation(true);
      _isBillingInformationCollapsed = false;
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String addBillingItem()
  {
    if (_newBillingItem != null) {
      try {
        getScreen().getBillingInformation().createBillingItem(_newBillingItem);
      }
      catch (BusinessRuleViolationException e) {
        showMessage("businessError", e.getMessage());
      }
      _newBillingItem = null; // reset
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String deleteBillingItem()
  {
    getScreen().getBillingInformation().getBillingItems().remove(getRequestMap().get("element"));
    _newBillingItem = null;
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  public String showAllLabActivities()
  {
    _labActivitySearchResults.searchLabActivitiesForScreen(_screen);
    return BROWSE_ACTIVITIES;
  }

  public String showAllCherryPickRequests()
  {
    _cherryPickRequestSearchResults.searchForScreen(_screen);
    return BROWSE_CHERRY_PICK_REQUESTS;
  }

  // protected methods

  protected ScreensaverUserRole getEditableAdminRole()
  {
    return EDITING_ROLE;
  }


  // private methods

  protected void resetView()
  {
    super.resetView();
    _isEditMode = false;
    _returnToViewAfterEdit = null;
    //_isAdminViewMode = false; // maintain this setting when viewing a new screen
    //_isPublishableProtocolDetailsCollapsed = true; // maintain this setting when viewing a new screen
    //_isBillingInformationCollapsed = true; // maintain this setting when viewing a new screen
    _newFundingSupport = null;
    _newStatusItemValue = null;
    _newStatusItemDate = null;
    _newAttachedFileContents = null;
    _newAttachedFileName = null;
    _newAttachedFileType = null;
    _uploadedAttachedFileContents = null;
    //_newAssayReadoutType = null;
    _newPublication = null;
    _newBillingItem = null;
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
