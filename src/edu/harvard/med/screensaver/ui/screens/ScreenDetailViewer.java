// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screens;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import edu.harvard.med.iccbl.screensaver.policy.WebDataAccessPolicy;
import edu.harvard.med.screensaver.ScreensaverProperties;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.ScreenDAO;
import edu.harvard.med.screensaver.db.UsersDAO;
import edu.harvard.med.screensaver.db.accesspolicy.DataAccessPolicy;
import edu.harvard.med.screensaver.model.AttachedFile;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.RequiredPropertyException;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.screens.AssayReadoutType;
import edu.harvard.med.screensaver.model.screens.AttachedFileType;
import edu.harvard.med.screensaver.model.screens.BillingItem;
import edu.harvard.med.screensaver.model.screens.FundingSupport;
import edu.harvard.med.screensaver.model.screens.LabActivity;
import edu.harvard.med.screensaver.model.screens.Publication;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.Screening;
import edu.harvard.med.screensaver.model.screens.StatusItem;
import edu.harvard.med.screensaver.model.screens.StatusValue;
import edu.harvard.med.screensaver.model.screens.StudyType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.service.screens.ScreeningDuplicator;
import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.ui.activities.ActivityViewer;
import edu.harvard.med.screensaver.ui.cherrypickrequests.CherryPickRequestViewer;
import edu.harvard.med.screensaver.ui.searchresults.CherryPickRequestSearchResults;
import edu.harvard.med.screensaver.ui.searchresults.LabActivitySearchResults;
import edu.harvard.med.screensaver.ui.searchresults.ScreenSearchResults;
import edu.harvard.med.screensaver.ui.util.AttachedFiles;
import edu.harvard.med.screensaver.ui.util.ScreensaverUserComparator;
import edu.harvard.med.screensaver.ui.util.UISelectOneBean;
import edu.harvard.med.screensaver.ui.util.UISelectOneEntityBean;
import edu.harvard.med.screensaver.util.eutils.PublicationInfoProvider;

import org.apache.log4j.Logger;
import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.joda.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

public class ScreenDetailViewer extends StudyDetailViewer
{
  private static final int CHILD_ENTITY_TABLE_MAX_ROWS = 10;

  private static Logger log = Logger.getLogger(ScreenDetailViewer.class);


  // instance data

  private ScreenDetailViewer _thisProxy;
  private GenericEntityDAO _dao;
  private ScreenDAO _screenDao;
  private DataAccessPolicy _dataAccessPolicy;
  private ScreenViewer _screenViewer;
  private ScreenSearchResults _screensBrowser;
  private ActivityViewer _activityViewer;
  private CherryPickRequestViewer _cherryPickRequestViewer;
  private PublicationInfoProvider _publicationInfoProvider;
  private LabActivitySearchResults _labActivitySearchResults;
  private CherryPickRequestSearchResults _cherryPickRequestSearchResults;
  private ScreeningDuplicator _screeningDuplicator;
  private AttachedFiles _attachedFiles;

  private Screen _screen;
  private boolean _isAdminViewMode = false;
  private boolean _isPublishableProtocolDetailsCollapsed = true;
  private boolean _isBillingInformationCollapsed = true;
  private UISelectOneBean<FundingSupport> _newFundingSupport;
  private UISelectOneBean<StatusValue> _newStatusItemValue;
  private LocalDate _newStatusItemDate;
  //private UISelectOneBean<AssayReadoutType> _newAssayReadoutType;

  private Publication _newPublication;
  private UploadedFile _uploadedPublicationAttachedFileContents;
  private BillingItem _newBillingItem;

  private UISelectOneEntityBean<AdministratorUser> _pinTransferApprovedBy;
  private LocalDate _pinTransferApprovalDate;
  private String _pinTransferApprovalComments;

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
                            DataAccessPolicy dataAccessPolicy,
                            ScreenViewer screenViewer,
                            ScreenSearchResults screensBrowser,
                            ActivityViewer activityViewer,
                            CherryPickRequestViewer cherryPickRequestViewer,
                            PublicationInfoProvider publicationInfoProvider,
                            LabActivitySearchResults labActivitySearchResults,
                            CherryPickRequestSearchResults cherryPickRequestSearchResults,
                            ScreeningDuplicator screeningDuplicator,
                            AttachedFiles attachedFiles)
  {
    super(dao, usersDao, ScreensaverUserRole.SCREENS_ADMIN);
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
    _screeningDuplicator = screeningDuplicator;
    _attachedFiles = attachedFiles;
  }


  // public methods

  public void setScreen(Screen screen)
  {
    setStudy(screen);
    _screen = screen;
    resetView();

    initalizeAttachedFiles(screen);
  }

  private void initalizeAttachedFiles(Screen screen)
  {
    Set<AttachedFileType> types = Sets.newHashSet(AttachedFileType.values());
    types.remove(AttachedFileType.PUBLICATION);
    types.remove(AttachedFileType.RNAI_USER_AGREEMENT);
    types.remove(AttachedFileType.SMALL_MOLECULE_USER_AGREEMENT);
    _attachedFiles.setAttachedFileTypes(types);
    _attachedFiles.setAttachedFilesFilter(new Predicate<AttachedFile>() { 
      public boolean apply(AttachedFile af) { return af.getFileType() != AttachedFileType.PUBLICATION; } 
    });
    _attachedFiles.setAttachedFilesEntity(screen);
  }

  public Screen getScreen()
  {
    return _screen;
  }

  @Override
  public String reload()
  {
    if (_screen == null || _screen.getEntityId() == null) {
      _screen = null;
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    return _screenViewer.reload();
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
      Set<FundingSupport> candidateFundingSupports = Sets.newTreeSet(_dao.findAllEntitiesOfType(FundingSupport.class));
      candidateFundingSupports.removeAll(getScreen().getFundingSupports());
      _newFundingSupport = new UISelectOneEntityBean<FundingSupport>(candidateFundingSupports, null, true, _dao) {
          @Override
          protected String getEmptyLabel() { return "<select>"; }
      };
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

  public void setUploadedPublicationAttachedFileContents(UploadedFile uploadedFile)
  {
    _uploadedPublicationAttachedFileContents = uploadedFile;
  }

  public UploadedFile getUploadedPublicationAttachedFileContents()
  {
    return _uploadedPublicationAttachedFileContents;
  }

  public AttachedFiles getAttachedFiles()
  {
    return _attachedFiles;
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
    billingItems.addAll(getScreen().getBillingItems());
    Collections.sort(billingItems,
                     new Comparator<BillingItem>() {
      public int compare(BillingItem bi1, BillingItem bi2)
      {
        LocalDate one = bi1.getDateSentForBilling();
        LocalDate two = bi2.getDateSentForBilling();
        if(one != null && two != null )
        {
          return one.compareTo(two);
        }
        else if(one != null)
        {
          return -1;
        }
        else if(two != null)
        {
          return 1;
        }
        else 
        { // amount is guaranteed non-null, but still
          return bi1.getAmount().compareTo(bi2.getAmount());
        }
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

  public UISelectOneEntityBean<AdministratorUser> getPinTransferApprovedBy()
  {
    if (_pinTransferApprovedBy == null) {
      Set<AdministratorUser> candidateApprovers = 
        Sets.filter(Sets.newTreeSet(ScreensaverUserComparator.getInstance(), _dao.findAllEntitiesOfType(AdministratorUser.class, true, "screensaverUserRoles")),
                    new Predicate<AdministratorUser>() { public boolean apply(AdministratorUser u) { return u.getScreensaverUserRoles().contains(ScreensaverUserRole.SCREENS_ADMIN); } } );
      _pinTransferApprovedBy = new UISelectOneEntityBean<AdministratorUser>(candidateApprovers,
        _screen.getPinTransferApprovalActivity() == null ? null : _screen.getPinTransferApprovalActivity().getApprovedBy(),
        true,
        _dao) { @Override public String makeLabel(AdministratorUser a) { return a.getFullNameLastFirst(); }
      };
    }
    return _pinTransferApprovedBy;
  }

  public void setPinTransferApprovedBy(UISelectOneEntityBean<AdministratorUser> pinTransferApprovedBy)
  {
    _pinTransferApprovedBy = pinTransferApprovedBy;
  }

  public LocalDate getPinTransferApprovalDate()
  {
    if (_pinTransferApprovalDate == null && _screen.getPinTransferApprovalActivity() != null) {
      _pinTransferApprovalDate = _screen.getPinTransferApprovalActivity().getDateApproved();
    }
    return _pinTransferApprovalDate;
  }

  public void setPinTransferApprovalDate(LocalDate pinTransferApprovalDate)
  {
    _pinTransferApprovalDate = pinTransferApprovalDate;
  }

  /* JSF Application methods */

  public String getPinTransferApprovalComments()
  {
    if (_pinTransferApprovalComments == null && _screen.getPinTransferApprovalActivity() != null) {
      _pinTransferApprovalComments = _screen.getPinTransferApprovalActivity().getComments();
    }
    return _pinTransferApprovalComments;
  }

  public void setPinTransferApprovalComments(String pinTransferApprovalComments)
  {
    _pinTransferApprovalComments = pinTransferApprovalComments;
  }

  @UIControllerMethod
  public String toggleAdminViewMode()
  {
    setEditMode(false);
    _isAdminViewMode ^= true;
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  @Transactional
  public String editNewScreen(ScreeningRoomUser leadScreener, ScreenType screenType)
  {
    ScreensaverUser user = getScreensaverUser();
    if (!(user instanceof AdministratorUser &&
      ((AdministratorUser) user).isUserInRole(ScreensaverUserRole.SCREENS_ADMIN))) {
      showMessage("restrictedOperation", "add a new screen");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }

    Screen screen = new Screen();
    screen.setScreenNumber(_screenDao.findNextScreenNumber());
    screen.setStudyType(StudyType.IN_VITRO);
    setScreen(screen);
    if (leadScreener != null) {
      leadScreener = _dao.reloadEntity(leadScreener,
                                       false,
                                       "labHead",
                                       "screensaverUserRoles");
      // note: we cannot set these values directly on the screen object, as it
      // causes Hibernate exceptions (the 'managed' instances returned by the
      // UISelectOneEntityBeans will not replace the instances in the screen
      // object (being equal), thus causing detached entity exceptions)
      getLabName().setSelection(leadScreener.getLab().getLabHead());
      getLeadScreener().setSelection(leadScreener);
      // infer appropriate screen type from user roles
      if (screenType == null) {
        screenType = leadScreener.isRnaiUser() && !leadScreener.isSmallMoleculeUser() ? ScreenType.RNAI : !leadScreener.isRnaiUser() && leadScreener.isSmallMoleculeUser() ? ScreenType.SMALL_MOLECULE : null;
      }
      screen.setScreenType(screenType);
    }
    setEditMode(true);
    return VIEW_SCREEN_DETAIL;
  }

  @UIControllerMethod
  public String edit()
  {
    setEditMode(true);
    return VIEW_SCREEN_DETAIL;
  }

  @UIControllerMethod
  public String cancel()
  {
    setEditMode(false);
    if (_screen.getEntityId() == null) {
      return VIEW_MAIN;
    }
    return _screenViewer.viewScreen(_screen);
  }

  @UIControllerMethod
  @Transactional
  public String save()
  {
    setEditMode(false);
    // TODO: would like to use this code, since it handles both new and extant
    // Screens, but I think UISelectOneEntityBean's auto-loading of entities is
    // actually getting in the way, since it causes NonUniqueObjectException
    // updateScreenProperties();
    // _dao.saveOrUpdateEntity(getScreen());
    // instead, we handle each case separately:
    if (getScreen().getEntityId() == null) {
      updateScreenProperties();
      _dao.persistEntity(getScreen());
    }
    else {
      _dao.reattachEntity(getScreen());
      updateScreenProperties();
    }

    _dao.flush();
    return _screenViewer.viewScreen(_screen);
  }

  private void updateScreenProperties()
  {
    getScreen().setLabHead(getLabName().getSelection());
    getScreen().setLeadScreener(getLeadScreener().getSelection());
    if (_pinTransferApprovedBy.getSelection() != null && _screen.getPinTransferApprovalActivity() == null) {
      _screen.setPinTransferApproved((AdministratorUser) _dao.reloadEntity(getScreensaverUser(), false, "activitiesPerformed"),
                                     _pinTransferApprovedBy.getSelection(),
                                     _pinTransferApprovalDate,
                                     _pinTransferApprovalComments);
    }
  }

  @UIControllerMethod
  public String addStatusItem()
  {
    if (getNewStatusItemValue().getSelection() == null) {
      showMessage("requiredValue", "Status Item Value");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    if (getNewStatusItemDate() == null) {
      showMessage("requiredValue", "Status Item Date");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    try {
      getScreen().createStatusItem(getNewStatusItemDate(),
                                   getNewStatusItemValue().getSelection());
    }
    catch (BusinessRuleViolationException e) {
      showMessage("businessError", e.getMessage());
    }
    _newStatusItemValue = null; // reset
    _newStatusItemDate = null;
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

    try {
      Publication publication = getScreen().addCopyOfPublication(_newPublication);

      if (_uploadedPublicationAttachedFileContents != null) {
        String filename;
        InputStream contentsInputStream;
        filename = _uploadedPublicationAttachedFileContents.getName();
        try {
          contentsInputStream = _uploadedPublicationAttachedFileContents.getInputStream();
        }
        catch (IOException e) {
          reportApplicationError(e.getMessage());
          return REDISPLAY_PAGE_ACTION_RESULT;
        }
        _uploadedPublicationAttachedFileContents = null;
        publication.createAttachedFile(filename, contentsInputStream);
      }
    }
    catch (IOException e) {
      reportApplicationError("could not attach the file contents");
    }

    _newPublication = null;
    _attachedFiles.reset();
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  public String lookupPublicationByPubMedId()
  {
    try {
      Integer pubmedId = _newPublication.getPubmedId();
      _newPublication =
        _publicationInfoProvider.getPublicationForPubmedId(pubmedId);
      if (_newPublication == null) {
        reportApplicationError("Publication for PubMed ID " + pubmedId + " was not found");
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
  @Transactional
  public String downloadPublicationAttachedFile() throws IOException, SQLException
  {
    Publication publication = (Publication) getRequestMap().get("element");
    if (publication != null) {
      return _attachedFiles.doDownloadAttachedFile(publication.getAttachedFile());
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
  @Transactional(readOnly=true) // readOnly to prevent saving the new screening before the user clicks Save
  public String addLibraryScreening()
  {
    Screen screen = _dao.reloadEntity(_screen);
    Screening screening = _screeningDuplicator.addLibraryScreening(screen);

    _dao.needReadOnly(screen, "leadScreener", "labHead");
    _dao.needReadOnly(screen, "collaborators");
    return _activityViewer.editNewActivity(screening, this);
  }

  @UIControllerMethod
  @Transactional(readOnly=true) // readOnly to prevent saving the new screening before the user clicks Save
  public String addRNAiCherryPickScreening()
  {
    RNAiCherryPickRequest cpr = (RNAiCherryPickRequest) getCherryPickRequestsDataModel().getRowData();
    if (cpr == null) {
      reportSystemError("missing CherryPickRequest argument");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    Screen screen = _dao.reloadEntity(_screen);
    cpr = _dao.reloadEntity(cpr);
    Screening screening = _screeningDuplicator.addRnaiCherryPickScreening(screen, cpr);

    _dao.needReadOnly(screen, "leadScreener", "labHead");
    _dao.needReadOnly(screen, "collaborators");
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
    cpr.addEmptyWellsOnAssayPlate(cpr.getAssayPlateType().getPlateSize().getEdgeWellNames(2));
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
  public String addBillingItem()
  {
    if (_newBillingItem != null) {
      try {
        getScreen().addCopyOfBillingItem(_newBillingItem);
      }
      catch (RequiredPropertyException e) {
        showMessage("invalidUserInput", e.getMessage());
      }
      _newBillingItem = null; // reset
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String deleteBillingItem()
  {
    getScreen().getBillingItems().remove(getRequestMap().get("element"));
    _newBillingItem = null;
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String browseLabActivities()
  {
    _labActivitySearchResults.searchLabActivitiesForScreen(_screen);
    return BROWSE_ACTIVITIES;
  }

  @UIControllerMethod
  public String browseCherryPickRequests()
  {
    _cherryPickRequestSearchResults.searchForScreen(_screen);
    return BROWSE_CHERRY_PICK_REQUESTS;
  }

  @UIControllerMethod
  public String viewActivityWithinScreen()
  {
    _labActivitySearchResults.searchLabActivitiesForScreen(_screen);
    return _activityViewer.viewActivity();
  }

  protected void resetView()
  {
    super.resetView();
    setEditMode(false);
    //_isAdminViewMode = false; // maintain this setting when viewing a new screen
    //_isPublishableProtocolDetailsCollapsed = true; // maintain this setting when viewing a new screen
    //_isBillingInformationCollapsed = true; // maintain this setting when viewing a new screen
    _newFundingSupport = null;
    _newStatusItemValue = null;
    _newStatusItemDate = null;
    _attachedFiles.reset();
    //_newAssayReadoutType = null;
    _newPublication = null;
    _uploadedPublicationAttachedFileContents = null;
    _newBillingItem = null;
    _pinTransferApprovalDate = null;
    _pinTransferApprovedBy = null;
    _pinTransferApprovalComments = null;
  }

  @Override
  public boolean isDeleteSupported()
  {
    return ScreensaverProperties.allowScreenDeletion();
  }
  
  @UIControllerMethod
  @Transactional
  public String delete()
  {
    if( _screen.isDataLoaded() ) 
    {
      showMessage("screens.screenDeletionFailed.containsData", _screen.getScreenNumber());
    }else {
      _screenDao.deleteStudy(_screen);
      showMessage("screens.deletedScreen", "screenDetailViewer");
    }
    _screensBrowser.searchAllScreens();
    return BROWSE_SCREENS;
  }
  
}
