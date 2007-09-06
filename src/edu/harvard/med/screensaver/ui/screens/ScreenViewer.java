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

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.ScreenResultsDAO;
import edu.harvard.med.screensaver.db.UsersDAO;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
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
import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.ui.WebDataAccessPolicy;
import edu.harvard.med.screensaver.ui.screenresults.ScreenResultImporter;
import edu.harvard.med.screensaver.ui.screenresults.ScreenResultViewer;
import edu.harvard.med.screensaver.ui.screenresults.heatmaps.HeatMapViewer;
import edu.harvard.med.screensaver.ui.searchresults.ScreenSearchResults;
import edu.harvard.med.screensaver.ui.util.JSFUtils;
import edu.harvard.med.screensaver.ui.util.UISelectManyBean;
import edu.harvard.med.screensaver.ui.util.UISelectManyEntityBean;
import edu.harvard.med.screensaver.ui.util.UISelectOneBean;
import edu.harvard.med.screensaver.ui.util.UISelectOneEntityBean;
import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.log4j.Logger;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataAccessException;

public class ScreenViewer extends AbstractBackingBean
{
  // TODO: disabling editing until ScreenDB is fully replaced by Screensaver
  // (otherwise changes made to screens will be overwritten when
  // ScreenDBSynchronizer is run)
  private static final ScreensaverUserRole EDITING_ROLE = ScreensaverUserRole./*SCREENS_ADMIN*/DEVELOPER;

  private static Logger log = Logger.getLogger(ScreenViewer.class);


  // instance data

  private GenericEntityDAO _dao;
  private ScreenResultsDAO _screenResultsDao;
  private UsersDAO _usersDao;
  private ScreenSearchResults _screensBrowser;
  private ScreenResultViewer _screenResultViewer;
  private CherryPickRequestViewer _cherryPickRequestViewer;
  private HeatMapViewer _heatMapViewer;
  private ScreenResultImporter _screenResultImporter;
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
  private boolean _showNavigationBar;


  // public property getter & setter methods

  /**
   * @motivation for CGLIB2
   */
  protected ScreenViewer()
  {
  }

  public ScreenViewer(GenericEntityDAO dao,
                      ScreenResultsDAO screenResultsDao,
                      UsersDAO usersDao,
                      ScreenSearchResults screensBrowser,
                      ScreenResultViewer screenResultViewer,
                      CherryPickRequestViewer cherryPickRequestViewer,
                      HeatMapViewer heatMapViewer,
                      ScreenResultImporter screenResultImporter,
                      WebDataAccessPolicy dataAccessPolicy)
  {
    _dao = dao;
    _screenResultsDao = screenResultsDao;
    _usersDao = usersDao;
    _screensBrowser = screensBrowser;
    _screenResultViewer = screenResultViewer;
    _cherryPickRequestViewer = cherryPickRequestViewer;
    _heatMapViewer = heatMapViewer;
    _screenResultImporter = screenResultImporter;
    _dataAccessPolicy = dataAccessPolicy;
  }

  public Screen getScreen()
  {
    return _screen;
  }

  public void setScreen(Screen screen)
  {
    _screen = screen;
    _screenResultImporter.setScreen(screen);
    ScreenResult screenResult = screen.getScreenResult();
    _heatMapViewer.setScreenResult(screenResult);
    _screenResultViewer.setScreenResult(screenResult);
    if (screenResult != null && screenResult.getResultValueTypes().size() > 0) {
      _screenResultViewer.setScreenResultSize(screenResult.getResultValueTypesList().get(0).getResultValues().size());
    }
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

  /**
   * @motivation for JSF saveState component
   */
  public void setShowNavigationBar(boolean showNavigationBar)
  {
    _showNavigationBar = showNavigationBar;
  }

  public boolean isShowNavigationBar()
  {
    return _showNavigationBar;
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

  @UIControllerMethod
  public String viewScreen(final Screen screenIn)
  {
    // TODO: implement as aspect
    if (screenIn.isRestricted()) {
      showMessage("restrictedEntity", "Screen " + screenIn.getScreenNumber());
      log.warn("user unauthorized to view " + screenIn);
      return REDISPLAY_PAGE_ACTION_RESULT;
    }

    try {
      _dao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          Screen screen = _dao.reloadEntity(screenIn,
                                            true,
                                            "hbnLabHead.hbnLabMembers",
                                            "hbnLeadScreener",
                                            "billingInformation");
          _dao.needReadOnly(screen, "hbnCollaborators.hbnLabAffiliation");
          _dao.needReadOnly(screen, "screeningRoomActivities");
          _dao.needReadOnly(screen, "abaseTestsets", "attachedFiles", "fundingSupports", "keywords", "lettersOfSupport", "publications");
          _dao.needReadOnly(screen, "statusItems");
          _dao.needReadOnly(screen, "cherryPickRequests");
          _dao.needReadOnly(screen, "hbnCollaborators");
          _dao.needReadOnly(screen.getScreenResult(), "plateNumbers");
          _dao.needReadOnly(screen.getScreenResult(),
                            "hbnResultValueTypes.hbnDerivedTypes",
                            "hbnResultValueTypes.hbnTypesDerivedFrom");

          setScreen(screen);
        }
      });
    }
    catch (DataAccessException e) {
      showMessage("databaseOperationFailed", e.getMessage());
      return REDISPLAY_PAGE_ACTION_RESULT;
    }

    return VIEW_SCREEN;
  }

  @UIControllerMethod
  public String viewLastScreen()
  {
    return viewScreen(_screen);
  }

  @UIControllerMethod
  public String editScreen()
  {
    _isEditMode = true;
    try {
      _dao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          _dao.reattachEntity(_screen); // checks if up-to-date
          _dao.need(_screen, "hbnLabHead.hbnLabMembers");
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
    return viewLastScreen(); // on error, reload (and not in edit mode)
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
    return viewLastScreen();
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
          _dao.reattachEntity(_screen);
          _screen.setLabHead(getLabName().getSelection());
          _screen.setLeadScreener(getLeadScreener().getSelection());
          _screen.setCollaboratorsList(getCollaborators().getSelections());
        }
      });
    }
    catch (ConcurrencyFailureException e) {
      showMessage("concurrentModificationConflict");
      viewLastScreen();
    }
    catch (DataAccessException e) {
      showMessage("databaseOperationFailed", e.getMessage());
      viewLastScreen();
    }
    _screensBrowser.invalidateSearchResult();
    return VIEW_SCREEN;
  }

  @UIControllerMethod
  public String addStatusItem()
  {
    if (_newStatusValue != null) {
      try {
        new StatusItem(_screen,
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
    _screen.getStatusItems().remove(getSelectedEntityOfType(StatusItem.class));
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String addPublication()
  {
    try {
      new Publication(_screen, "<new>", "", "", "");
    }
    catch (DuplicateEntityException e) {
      showMessage("screens.duplicateEntity", "publication");
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String deletePublication()
  {
    _screen.getPublications().remove(getSelectedEntityOfType(Publication.class));
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String addLetterOfSupport()
  {
    try {
      new LetterOfSupport(_screen, new Date(), "");
    }
    catch (DuplicateEntityException e) {
      showMessage("screens.duplicateEntity", "letter of support");
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String deleteLetterOfSupport()
  {
    _screen.getLettersOfSupport().remove(getSelectedEntityOfType(LetterOfSupport.class));
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String addAttachedFile()
  {
    try {
      new AttachedFile(_screen, "<new>", "");
    }
    catch (DuplicateEntityException e) {
      showMessage("screens.duplicateEntity", "attached file");
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String deleteAttachedFile()
  {
    _screen.getAttachedFiles().remove(getSelectedEntityOfType(AttachedFile.class));
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String addFundingSupport()
  {
    if (_newFundingSupport != null) {
      if (!_screen.addFundingSupport(_newFundingSupport)) {
        showMessage("screens.duplicateEntity", "funding support");
      }
      setNewFundingSupport(null);
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String deleteFundingSupport()
  {
    _screen.getFundingSupports().remove(getSelectedEntityOfType(FundingSupport.class));
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String addKeyword()
  {
    if (! _screen.addKeyword(_newKeyword)) {
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
    _screen.getKeywords().remove((String) getHttpServletRequest().getAttribute("keyword"));
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
    if (!_screen.getScreenType().equals(ScreenType.RNAI)) {
      showMessage("applicationError", "Cherry Pick Requests can only be created for RNAi screens, currently");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }

    final CherryPickRequest[] result = new CherryPickRequest[1];
    try {
      _dao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          Screen screen = _dao.reloadEntity(_screen);
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


  // JSF event listeners

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
