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
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.SortedSet;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import edu.harvard.med.iccbl.screensaver.policy.DataSharingLevelMapper;
import edu.harvard.med.screensaver.ScreensaverProperties;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.ScreenDAO;
import edu.harvard.med.screensaver.db.UsersDAO;
import edu.harvard.med.screensaver.db.accesspolicy.DataAccessPolicy;
import edu.harvard.med.screensaver.model.AttachedFile;
import edu.harvard.med.screensaver.model.AttachedFileType;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.RequiredPropertyException;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.screens.AssayReadoutType;
import edu.harvard.med.screensaver.model.screens.BillingItem;
import edu.harvard.med.screensaver.model.screens.FundingSupport;
import edu.harvard.med.screensaver.model.screens.LabActivity;
import edu.harvard.med.screensaver.model.screens.Publication;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenAttachedFileType;
import edu.harvard.med.screensaver.model.screens.ScreenDataSharingLevel;
import edu.harvard.med.screensaver.model.screens.Screening;
import edu.harvard.med.screensaver.model.screens.StatusItem;
import edu.harvard.med.screensaver.model.screens.StatusValue;
import edu.harvard.med.screensaver.model.screens.StudyType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.service.OperationRestrictedException;
import edu.harvard.med.screensaver.service.screens.ScreeningDuplicator;
import edu.harvard.med.screensaver.ui.EditResult;
import edu.harvard.med.screensaver.ui.UICommand;
import edu.harvard.med.screensaver.ui.activities.ActivityViewer;
import edu.harvard.med.screensaver.ui.cherrypickrequests.CherryPickRequestDetailViewer;
import edu.harvard.med.screensaver.ui.searchresults.CherryPickRequestSearchResults;
import edu.harvard.med.screensaver.ui.searchresults.EntityUpdateSearchResults;
import edu.harvard.med.screensaver.ui.searchresults.LabActivitySearchResults;
import edu.harvard.med.screensaver.ui.util.AttachedFiles;
import edu.harvard.med.screensaver.ui.util.ScreensaverUserComparator;
import edu.harvard.med.screensaver.ui.util.UISelectOneBean;
import edu.harvard.med.screensaver.ui.util.UISelectOneEntityBean;
import edu.harvard.med.screensaver.util.NullSafeUtils;
import edu.harvard.med.screensaver.util.eutils.EutilsException;
import edu.harvard.med.screensaver.util.eutils.PublicationInfoProvider;

import org.apache.log4j.Logger;
import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.joda.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class ScreenDetailViewer extends AbstractStudyDetailViewer<Screen>
{
  private static final int CHILD_ENTITY_TABLE_MAX_ROWS = 10;

  private static Logger log = Logger.getLogger(ScreenDetailViewer.class);

  private ScreenDAO _screenDao;
  private DataAccessPolicy _dataAccessPolicy;
  private ScreenViewer _screenViewer;
  private ActivityViewer _activityViewer;
  private CherryPickRequestDetailViewer _cherryPickRequestDetailViewer;
  private PublicationInfoProvider _publicationInfoProvider;
  private LabActivitySearchResults _labActivitySearchResults;
  private CherryPickRequestSearchResults _cherryPickRequestSearchResults;
  private ScreeningDuplicator _screeningDuplicator;
  private AttachedFiles _attachedFiles;
  private EntityUpdateSearchResults<Screen,Integer> _screenUpdateSearchResults; 

  private boolean _isAdminViewMode = false;
  private boolean _isPublishableProtocolDetailsCollapsed = true;
  private boolean _isBillingInformationCollapsed = true;
  private UISelectOneBean<FundingSupport> _newFundingSupport;
  private UISelectOneBean<StatusValue> _newStatusItemValue;
  private LocalDate _newStatusItemDate;

  private Publication _newPublication;
  private UploadedFile _uploadedPublicationAttachedFileContents;
  private BillingItem _newBillingItem;

  private UISelectOneEntityBean<AdministratorUser> _pinTransferApprovedBy;
  private LocalDate _pinTransferApprovalDate;
  private String _pinTransferApprovalComments;

  private UISelectOneBean<ScreenDataSharingLevel> _dataSharingLevel;
  private ScreenDataSharingLevel _lastDataSharingLevel;
  private LabHead _lastLabHead;
  private ScreeningRoomUser _lastLeadScreener;


  /**
   * @motivation for CGLIB2
   */
  protected ScreenDetailViewer()
  {
  }

  public ScreenDetailViewer(ScreenDetailViewer thisProxy,
                            ScreenViewer screenViewer,
                            GenericEntityDAO dao,
                            ScreenDAO screenDao,
                            UsersDAO usersDao,
                            DataAccessPolicy dataAccessPolicy,
                            ActivityViewer activityViewer,
                            CherryPickRequestDetailViewer cherryPickRequestDetailViewer,
                            PublicationInfoProvider publicationInfoProvider,
                            LabActivitySearchResults labActivitiesBrowser,
                            CherryPickRequestSearchResults cprsBrowser,
                            ScreeningDuplicator screeningDuplicator,
                            AttachedFiles attachedFiles,
                            EntityUpdateSearchResults<Screen,Integer> screenUpdateSearchResults)
  {
    super(thisProxy,
          dao,
          EDIT_SCREEN,
          usersDao);
    _screenDao = screenDao;
    _dataAccessPolicy = dataAccessPolicy;
    _screenViewer = screenViewer;
    _activityViewer = activityViewer;
    _cherryPickRequestDetailViewer = cherryPickRequestDetailViewer;
    _publicationInfoProvider = publicationInfoProvider;
    _labActivitySearchResults = labActivitiesBrowser;
    _cherryPickRequestSearchResults = cprsBrowser;
    _screeningDuplicator = screeningDuplicator;
    _attachedFiles = attachedFiles;
    _screenUpdateSearchResults = screenUpdateSearchResults;
    getIsPanelCollapsedMap().put("screenDetail", false);
  }

  @Override
  protected void initializeEntity(Screen screen)
  {
    super.initializeEntity(screen);
  }
  
  @Override
  protected void initializeViewer(Screen screen)
  {
    super.initializeViewer(screen);
    //_isAdminViewMode = false; // maintain this setting when viewing a new screen
    //_isPublishableProtocolDetailsCollapsed = true; // maintain this setting when viewing a new screen
    //_isBillingInformationCollapsed = true; // maintain this setting when viewing a new screen
    _newFundingSupport = null;
    _newStatusItemValue = null;
    _newStatusItemDate = null;
    _newPublication = null;
    _uploadedPublicationAttachedFileContents = null;
    _newBillingItem = null;
    _pinTransferApprovalDate = null;
    _pinTransferApprovedBy = null;
    _pinTransferApprovalComments = null;
    _dataSharingLevel = null;
    _lastDataSharingLevel = screen.getDataSharingLevel();
    _lastLabHead = screen.getLabHead();
    _lastLeadScreener = screen.getLeadScreener();
    initalizeAttachedFiles(screen);
    _screenUpdateSearchResults.searchForParentEntity(screen);
    _labActivitySearchResults.searchLabActivitiesForScreen(screen);
    _cherryPickRequestSearchResults.searchForScreen(screen);
  }

  private void initalizeAttachedFiles(Screen screen)
  {
    SortedSet<AttachedFileType> attachedFileTypes = 
      Sets.<AttachedFileType>newTreeSet(Iterables.filter(getDao().findAllEntitiesOfType(ScreenAttachedFileType.class, true),
                                                         new Predicate<AttachedFileType>() { 
        public boolean apply(AttachedFileType aft) { return !!!aft.getValue().equals(Publication.PUBLICATION_ATTACHED_FILE_TYPE_VALUE); }
      }));
    _attachedFiles.reset();
    _attachedFiles.setAttachedFileTypes(attachedFileTypes);
    _attachedFiles.setAttachedFilesFilter(new Predicate<AttachedFile>() { 
      public boolean apply(AttachedFile af) { return !!!af.getFileType().getValue().equals(Publication.PUBLICATION_ATTACHED_FILE_TYPE_VALUE); }
    });
    _attachedFiles.setAttachedFilesEntity(screen);
  }
  
  public boolean isAdminViewMode()
  {
    return _isAdminViewMode;
  }

  @Override
  public EntityUpdateSearchResults<Screen,Integer> getEntityUpdateSearchResults()
  {
    return _screenUpdateSearchResults;
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
    return _dataAccessPolicy.isAllowedAccessToScreenDetails(getEntity());
  }

  /**
   * Determine whether the current user can see the Status Items, Lab
   * Activities, and Cherry Pick Requests tables. These are considered more
   * private than the screen details (see
   * {@link #isAllowedAccessToScreenDetails()}).
   */
  public boolean isAllowedAccessToScreenActivity()
  {
    return _dataAccessPolicy.isAllowedAccessToScreenActivity(getEntity());
  }

  public UISelectOneBean<FundingSupport> getNewFundingSupport()
  {
    if (_newFundingSupport == null) {
      Set<FundingSupport> candidateFundingSupports = Sets.newTreeSet(getDao().findAllEntitiesOfType(FundingSupport.class));
      candidateFundingSupports.removeAll(getEntity().getFundingSupports());
      _newFundingSupport = new UISelectOneEntityBean<FundingSupport>(candidateFundingSupports, null, true, getDao()) {
          @Override
          protected String getEmptyLabel() { return "<select>"; }
      };
    }
    return _newFundingSupport;
  }

  public UISelectOneBean<StatusValue> getNewStatusItemValue()
  {
    if (_newStatusItemValue == null) {
      _newStatusItemValue = new UISelectOneBean<StatusValue>(getEntity().getCandidateStatusValues());
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
    return new ListDataModel(new ArrayList<StatusItem>(getEntity().getStatusItems()));
  }

  public DataModel getLabActivitiesDataModel()
  {
    ArrayList<LabActivity> labActivities = new ArrayList<LabActivity>(getEntity().getLabActivities());
    Collections.reverse(labActivities);
    return new ListDataModel(labActivities.subList(0, Math.min(CHILD_ENTITY_TABLE_MAX_ROWS, getEntity().getLabActivities().size())));
  }

  public DataModel getCherryPickRequestsDataModel()
  {
    ArrayList<CherryPickRequest> cherryPickRequests = new ArrayList<CherryPickRequest>(getEntity().getCherryPickRequests());
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
    ArrayList<Publication> publications = new ArrayList<Publication>(getEntity().getPublications());
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
    return new ListDataModel(new ArrayList<FundingSupport>(getEntity().getFundingSupports()));
  }

  public DataModel getAssayReadoutTypesDataModel()
  {
    return new ListDataModel(new ArrayList<AssayReadoutType>(getEntity().getAssayReadoutTypes()));
  }

  public DataModel getBillingItemsDataModel()
  {
    ArrayList<BillingItem> billingItems = new ArrayList<BillingItem>();
    billingItems.addAll(getEntity().getBillingItems());
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
        Sets.filter(ImmutableSortedSet.orderedBy(ScreensaverUserComparator.<AdministratorUser>getInstance()).addAll(getDao().findAllEntitiesOfType(AdministratorUser.class, true, "screensaverUserRoles")).build(),
                    new Predicate<AdministratorUser>() { public boolean apply(AdministratorUser u) { return u.getScreensaverUserRoles().contains(ScreensaverUserRole.SCREENS_ADMIN); } } );
      // note: we must reload 'performedBy', since it can otherwise be a proxy, which will not allow us to cast it to AdministratorUser
      AdministratorUser defaultSelection = getEntity().getPinTransferApprovalActivity() == null ? null : (AdministratorUser) getDao().reloadEntity(getEntity().getPinTransferApprovalActivity().getPerformedBy());
      _pinTransferApprovedBy = new UISelectOneEntityBean<AdministratorUser>(candidateApprovers,
        defaultSelection,
        true,
        getDao()) { @Override public String makeLabel(AdministratorUser a) { return a.getFullNameLastFirst(); }
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
    if (_pinTransferApprovalDate == null && getEntity().getPinTransferApprovalActivity() != null) {
      _pinTransferApprovalDate = getEntity().getPinTransferApprovalActivity().getDateOfActivity();
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
    if (_pinTransferApprovalComments == null && getEntity().getPinTransferApprovalActivity() != null) {
      _pinTransferApprovalComments = getEntity().getPinTransferApprovalActivity().getComments();
    }
    return _pinTransferApprovalComments;
  }

  public void setPinTransferApprovalComments(String pinTransferApprovalComments)
  {
    _pinTransferApprovalComments = pinTransferApprovalComments;
  }

  public UISelectOneBean<ScreenDataSharingLevel> getDataSharingLevel()
  {
    if (_dataSharingLevel == null) {
      _dataSharingLevel = new UISelectOneBean<ScreenDataSharingLevel>(Lists.newArrayList(ScreenDataSharingLevel.values()), getEntity().getDataSharingLevel(), false);
      _dataSharingLevel.addObserver(new Observer() {
        public void update(Observable arg0, Object dataSharingLevel)
        {
          getEntity().setDataSharingLevel((ScreenDataSharingLevel) dataSharingLevel);
        }
      });
    }
    return _dataSharingLevel;
  }
  
  @UICommand
  public String toggleAdminViewMode()
  {
    setEditMode(false);
    _isAdminViewMode ^= true;
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @Override
  protected void initializeNewEntity(Screen screen)
  {
    screen.setScreenNumber(_screenDao.findNextScreenNumber());
    screen.setStudyType(StudyType.IN_VITRO);
    if (screen.getLabHead() != null) {
      screen.setDataSharingLevel(DataSharingLevelMapper.getScreenDataSharingLevelForUser(screen.getScreenType(), screen.getLabHead()));
    }
    else {
      screen.setDataSharingLevel(ScreenDataSharingLevel.PRIVATE);
    }
  }

  @Override
  protected void recordUpdateActivity()
  {
    if (_lastDataSharingLevel != getDataSharingLevel().getSelection()) {
      recordUpdateActivity("updated screen data sharing level from '" + NullSafeUtils.toString(_lastDataSharingLevel) + 
                           "' to '" + NullSafeUtils.toString(getDataSharingLevel().getSelection()) + "'");
    }
    if (!!!NullSafeUtils.nullSafeEquals(_lastLabHead, getEntity().getLabHead())) {
      String lastLabHead = NullSafeUtils.toString(_lastLabHead, ScreensaverUser.ToDisplayStringFunction);
      String newLabHead = NullSafeUtils.toString(getEntity().getLabHead(), ScreensaverUser.ToDisplayStringFunction);
      recordUpdateActivity("changed lab from '" + lastLabHead + "' to '" + newLabHead + "'");
    }
    if (!!!NullSafeUtils.nullSafeEquals(_lastLeadScreener, getEntity().getLeadScreener())) {
      String lastLeadScreener = NullSafeUtils.toString(_lastLeadScreener, ScreensaverUser.ToDisplayStringFunction);
      String newLeadScreener = NullSafeUtils.toString(getEntity().getLeadScreener(), ScreensaverUser.ToDisplayStringFunction);
      recordUpdateActivity("changed lead screener from '" + lastLeadScreener + "' to '" + newLeadScreener + "'");
    }
    super.recordUpdateActivity();
  }

  @Override
  protected void updateEntityProperties(Screen screen)
  {
    super.updateEntityProperties(screen);
    screen.setLabHead(getLabName().getSelection());
    screen.setLeadScreener(getLeadScreener().getSelection());
    Set<ScreeningRoomUser> extantCollaborators = Sets.newHashSet(screen.getCollaborators());
    for (ScreeningRoomUser collaborator : Sets.difference(_collaborators, extantCollaborators)) {
      screen.addCollaborator(getDao().reloadEntity(collaborator));
    }
    for (ScreeningRoomUser collaborator : Sets.difference(extantCollaborators, _collaborators)) {
      screen.removeCollaborator(getDao().reloadEntity(collaborator));
    }
    
    if (_pinTransferApprovedBy.getSelection() != null && screen.getPinTransferApprovalActivity() == null) {
      screen.setPinTransferApproved((AdministratorUser) getDao().reloadEntity(getScreensaverUser(), false, "activitiesPerformed"),
                                    _pinTransferApprovedBy.getSelection(),
                                    _pinTransferApprovalDate,
                                    _pinTransferApprovalComments);
    }
  }

  @UICommand
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
      getEntity().createStatusItem(getNewStatusItemDate(),
                                   getNewStatusItemValue().getSelection());
    }
    catch (BusinessRuleViolationException e) {
      showMessage("businessError", e.getMessage());
    }
    _newStatusItemValue = null; // reset
    _newStatusItemDate = null;
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UICommand
  public String deleteStatusItem()
  {
    getEntity().getStatusItems().remove(getRequestMap().get("element"));
    _newStatusItemValue = null; // reset
    _newStatusItemDate = null;
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UICommand
  public String addPublication()
  {

    try {
      Publication publication = getEntity().addCopyOfPublication(_newPublication);

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
        AttachedFileType publicationAttachedFileType = getDao().findEntityByProperty(AttachedFileType.class, "value", Publication.PUBLICATION_ATTACHED_FILE_TYPE_VALUE);
        if (publicationAttachedFileType == null) {
          reportApplicationError("'publication' attached file type does not exist");
          return REDISPLAY_PAGE_ACTION_RESULT;
        }
        publication.createAttachedFile(filename, contentsInputStream, publicationAttachedFileType);
      }
    }
    catch (IOException e) {
      reportApplicationError("could not attach the file contents");
    }

    _newPublication = null;
    _attachedFiles.reset();
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  public String lookupPublicationByPubMedId() throws EutilsException
  {
    Integer pubmedId = _newPublication.getPubmedId();
    _newPublication =
      _publicationInfoProvider.getPublicationForPubmedId(pubmedId);
    if (_newPublication == null) {
      reportApplicationError("Publication for PubMed ID " + pubmedId + " was not found");
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UICommand
  public String deletePublication()
  {
    Publication publication = (Publication) getRequestMap().get("element");
    if (publication != null) {
      getEntity().getPublications().remove(publication);
      _newPublication = null;
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UICommand
  @Transactional
  public String downloadPublicationAttachedFile() throws IOException, SQLException
  {
    Publication publication = (Publication) getRequestMap().get("element");
    if (publication != null) {
      return _attachedFiles.doDownloadAttachedFile(publication.getAttachedFile());
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UICommand
  public String addFundingSupport()
  {
    if (_newFundingSupport != null && _newFundingSupport.getSelection() != null) {
      getEntity().addFundingSupport(_newFundingSupport.getSelection());
      _newFundingSupport = null;
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UICommand
  public String deleteFundingSupport()
  {
    getEntity().getFundingSupports().remove(getRequestMap().get("element"));
    _newFundingSupport = null;
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UICommand
  @Transactional(readOnly=true) // readOnly to prevent saving the new screening before the user clicks Save
  public String addLibraryScreening()
  {
    if (!!!getScreensaverUser().isUserInRole(ScreensaverUserRole.SCREENS_ADMIN)) {
      throw new OperationRestrictedException("add Library Screening");
    }
    Screening screening = _screeningDuplicator.addLibraryScreening(getEntity(),
                                                                   (AdministratorUser) getScreensaverUser());
    return _activityViewer.editNewEntity(screening);
  }

  @UICommand
  public String copyLabActivity()
  {
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UICommand
  public String copyCherryPickRequest()
  {
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UICommand
  @Transactional(readOnly=true) // readOnly to prevent saving the new screening before the user clicks Save
  public String addCherryPickRequest()
  {
    if (!!!getScreensaverUser().isUserInRole(ScreensaverUserRole.CHERRY_PICK_REQUESTS_ADMIN)) {
      throw new OperationRestrictedException("add Cherry Pick Request");
    }
    Screen screen = getDao().reloadEntity(getEntity(), 
                                          true, 
                                          Screen.cherryPickRequests.getPath(),
                                          Screen.labActivities.getPath());
    getDao().needReadOnly(screen, Screen.labHead.getPath());
    getDao().needReadOnly(screen, Screen.leadScreener.getPath());
    getDao().needReadOnly(screen, Screen.collaborators.getPath());
    CherryPickRequest cpr = screen.createCherryPickRequest((AdministratorUser) getScreensaverUser());
    return _cherryPickRequestDetailViewer.editNewEntity(cpr);
  }

  @UICommand
  public String addBillingItem()
  {
    if (_newBillingItem != null) {
      try {
        getEntity().addCopyOfBillingItem(_newBillingItem);
      }
      catch (RequiredPropertyException e) {
        showFieldInputError("", e.getMessage());
      }
      _newBillingItem = null; // reset
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UICommand
  public String deleteBillingItem()
  {
    getEntity().getBillingItems().remove(getRequestMap().get("element"));
    _newBillingItem = null;
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UICommand
  public String browseLabActivities()
  {
    _labActivitySearchResults.searchLabActivitiesForScreen(getEntity());
    return BROWSE_ACTIVITIES;
  }

  @UICommand
  public String browseCherryPickRequests()
  {
    _cherryPickRequestSearchResults.searchForScreen(getEntity());
    return BROWSE_CHERRY_PICK_REQUESTS;
  }

  @Override
  public boolean isDeleteSupported()
  {
    return getFeaturesEnabled().get("delete_screen");
  }
  
  @UICommand
  @Transactional
  public String delete()
  {
    if (getEntity().isDataLoaded()) {
      showMessage("screens.screenDeletionFailed.containsData", getEntity().getScreenNumber());
    }
    else {
      _screenDao.deleteStudy(getEntity());
      showMessage("screens.deletedScreen", "screenDetailViewer");
    }
    return VIEW_MAIN;
  }

  @Override
  protected String postEditAction(EditResult editResult)
  {
    switch (editResult) {
    case CANCEL_EDIT: return _screenViewer.reload();
    case SAVE_EDIT: return _screenViewer.reload();
    case CANCEL_NEW: return VIEW_MAIN;
    case SAVE_NEW: return _screenViewer.viewEntity(getEntity()); // note: can't call reload() since parent viewer is not yet configured with our new screen
    default: return null;
    }
  }
}
