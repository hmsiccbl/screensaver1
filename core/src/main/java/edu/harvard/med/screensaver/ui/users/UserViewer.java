// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.users;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;

import edu.harvard.med.iccbl.screensaver.IccblScreensaverConstants;
import edu.harvard.med.iccbl.screensaver.policy.DataSharingLevelMapper;
import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.UsersDAO;
import edu.harvard.med.screensaver.model.AttachedFile;
import edu.harvard.med.screensaver.model.AttachedFileType;
import edu.harvard.med.screensaver.model.activities.Activity;
import edu.harvard.med.screensaver.model.activities.ServiceActivity;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenDataSharingLevel;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.AffiliationCategory;
import edu.harvard.med.screensaver.model.users.ChecklistItemEvent;
import edu.harvard.med.screensaver.model.users.ChecklistItemGroup;
import edu.harvard.med.screensaver.model.users.FacilityUsageRole;
import edu.harvard.med.screensaver.model.users.Lab;
import edu.harvard.med.screensaver.model.users.LabAffiliation;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUserClassification;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserComparator;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.model.users.UserAttachedFileType;
import edu.harvard.med.screensaver.service.OperationRestrictedException;
import edu.harvard.med.screensaver.service.screens.ScreenGenerator;
import edu.harvard.med.screensaver.ui.activities.ActivitySearchResults;
import edu.harvard.med.screensaver.ui.activities.ActivityViewer;
import edu.harvard.med.screensaver.ui.arch.datatable.Criterion;
import edu.harvard.med.screensaver.ui.arch.datatable.Criterion.Operator;
import edu.harvard.med.screensaver.ui.arch.util.AttachedFiles;
import edu.harvard.med.screensaver.ui.arch.util.ChecklistItems;
import edu.harvard.med.screensaver.ui.arch.util.JSFUtils;
import edu.harvard.med.screensaver.ui.arch.util.UISelectOneBean;
import edu.harvard.med.screensaver.ui.arch.util.UISelectOneEntityBean;
import edu.harvard.med.screensaver.ui.arch.view.EditResult;
import edu.harvard.med.screensaver.ui.arch.view.SearchResultContextEditableEntityViewerBackingBean;
import edu.harvard.med.screensaver.ui.arch.view.aspects.UICommand;
import edu.harvard.med.screensaver.ui.screens.ScreenDetailViewer;
import edu.harvard.med.screensaver.ui.screens.ScreenSearchResults;
import edu.harvard.med.screensaver.util.DevelopmentException;
import edu.harvard.med.screensaver.util.NullSafeComparator;
import edu.harvard.med.screensaver.util.NullSafeUtils;
import edu.harvard.med.screensaver.util.StringUtils;

/**
 * User Viewer backing bean.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class UserViewer extends SearchResultContextEditableEntityViewerBackingBean<ScreeningRoomUser,ScreeningRoomUser>
{
  // static members

  private static Logger log = Logger.getLogger(UserViewer.class);
  private static final Comparator<ScreensaverUserRole> USER_ROLE_COMPARATOR = new Comparator<ScreensaverUserRole>() {
    public int compare(ScreensaverUserRole r1, ScreensaverUserRole r2)
    {
      return ((Integer) r1.ordinal()).compareTo(r2.ordinal());
    }
  };
  private static final AffiliationCategory DEFAULT_NEW_LAB_AFFILIATION_CATEGORY = AffiliationCategory.HMS;
  /** checklist item groups to show in user interface, per requirements */
  private static final List<ChecklistItemGroup> CHECKLIST_ITEM_GROUPS = Lists.newArrayList(ChecklistItemGroup.values());
  static { CHECKLIST_ITEM_GROUPS.remove(ChecklistItemGroup.LEGACY); }
                                                                                     

  // instance data members

  private ScreenDetailViewer _screenDetailViewer;
  private UsersDAO _usersDao;
  private ScreenGenerator _screenGenerator;
  private ScreenSearchResults _screensBrowser;
  private AttachedFiles _attachedFiles;
  private ChecklistItems _checklistItems;

  private DataModel _userRolesDataModel;
  private UISelectOneBean<ScreensaverUserRole> _newUserRole;
  private UISelectOneEntityBean<LabHead> _labName;
  private UISelectOneEntityBean<LabAffiliation> _labAffiliation;
  private LabAffiliation _newLabAffiliation;
  private HashMap<ScreenType,DataModel> _screensDataModel;
  private DataModel _labMembersDataModel;
  private DataModel _screenAssociatesDataModel;
  private Set<ScreensaverUserRole> _lastPrimaryRoles;
  private DataModel _facilityUsageRolesDataModel;
  private UISelectOneBean<FacilityUsageRole> _newFacilityUsageRole;
  private String _newPassword1;
  private String _newPassword2;
  private ActivityViewer _activityViewer;
  private ActivitySearchResults _activitiesBrowser;


  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected UserViewer()
  {
  }

  public UserViewer(UserViewer userViewerProxy,
                    ScreenDetailViewer screenDetailViewer,
                    ActivityViewer activityViewer,
                    ActivitySearchResults activitiesBrowser,
                    GenericEntityDAO dao,
                    UsersDAO usersDao,
                    ScreenGenerator screenGenerator,
                    UserSearchResults userSearchResults,
                    ScreenSearchResults screensBrowser,
                    AttachedFiles attachedFiles,
                    ChecklistItems checklistItems)
  {
    super(userViewerProxy,
          ScreeningRoomUser.class,
          BROWSE_SCREENERS,
          VIEW_USER,
          dao,
          userSearchResults);
    _screenDetailViewer = screenDetailViewer;
    _activityViewer = activityViewer;
    _activitiesBrowser = activitiesBrowser;
    _usersDao = usersDao;
    _screenGenerator = screenGenerator;
    _screensBrowser = screensBrowser;
    _attachedFiles = attachedFiles;
    _checklistItems = checklistItems;
    getIsPanelCollapsedMap().put("labMembers", true);
    getIsPanelCollapsedMap().put("screenAssociates", true);
    getIsPanelCollapsedMap().put("smallMoleculeScreens", false);
    getIsPanelCollapsedMap().put("rnaiScreens", false);
    getIsPanelCollapsedMap().put("screens", false);
    getIsPanelCollapsedMap().put("userActivities", false);
  }

  public boolean isManageAuthenticationCredentialsFeatureEnabled()
  {
    return getApplicationProperties().isFeatureEnabled("manage_authentication_credentials");
  }

  /**
   * @return true if the logged in user is the same as the user being shown by the user viewer
   *
   */
  public boolean isMe()
  {
    return getEntity().equals(getScreensaverUser());
  }

  public boolean isScreeningRoomUserViewMode()
  {
    return getScreeningRoomUser() != null;
  }

  public boolean isLabHeadViewMode()
  {
    return getLabHead() != null;
  }

  public ScreeningRoomUser getScreeningRoomUser()
  {
    if (getEntity() instanceof ScreeningRoomUser) {
      return (ScreeningRoomUser) getEntity();
    }
    return null;
  }

  public LabHead getLabHead()
  {
    if (getEntity() instanceof LabHead) {
      return (LabHead) getEntity();
    }
    return null;
  }

  @Override
  protected void initializeEntity(ScreeningRoomUser user)
  {
    getDao().need(user, ScreeningRoomUser.roles);
    getDao().need(user, ScreensaverUser.activitiesPerformed.to(Activity.performedBy));
    getDao().need(user, ScreeningRoomUser.serviceActivities.to(Activity.performedBy));
    // note: no cross-product problem with dual labMembers associations, since only one will have size > 0
    getDao().need(user, ScreeningRoomUser.LabHead.to(LabHead.labAffiliation));
    getDao().need(user, ScreeningRoomUser.LabHead.to(LabHead.labMembers));
    if (user instanceof LabHead) {
      getDao().need((LabHead) user, LabHead.labAffiliation);
      getDao().need((LabHead) user, LabHead.labMembers);
    }
    // for UserAgreementUpdater
    getDao().need(user, ScreeningRoomUser.LabHead.to(ScreensaverUser.roles));
    getDao().need(user, ScreeningRoomUser.screensLed.to(Screen.statusItems));
    getDao().need(user, ScreeningRoomUser.screensLed.to(Screen.labHead));
    getDao().need(user, ScreeningRoomUser.screensLed.to(Screen.leadScreener));
    getDao().need(user, ScreeningRoomUser.screensLed.to(Screen.collaborators));
    getDao().need(user, ScreeningRoomUser.screensLed.to(Screen.labActivities));
    getDao().need(user, ScreeningRoomUser.screensCollaborated.to(Screen.statusItems));
    getDao().need(user, ScreeningRoomUser.screensCollaborated.to(Screen.labHead));
    getDao().need(user, ScreeningRoomUser.screensCollaborated.to(Screen.leadScreener));
    getDao().need(user, ScreeningRoomUser.screensCollaborated.to(Screen.collaborators));
    getDao().need(user, ScreeningRoomUser.screensCollaborated.to(Screen.labActivities));
    if (user instanceof LabHead) {
      getDao().need((LabHead) user, LabHead.screensHeaded.to(Screen.statusItems));
      getDao().need((LabHead) user, LabHead.screensHeaded.to(Screen.labActivities));
      getDao().need((LabHead) user, LabHead.screensHeaded.to(Screen.labHead));
      getDao().need((LabHead) user, LabHead.screensHeaded.to(Screen.leadScreener));
      getDao().need((LabHead) user, LabHead.screensHeaded.to(Screen.collaborators));
    }

    getDao().need(user, ScreeningRoomUser.checklistItemEvents.to(ChecklistItemEvent.checklistItem));
    getDao().need(user, ScreeningRoomUser.checklistItemEvents.to(ChecklistItemEvent.screeningRoomUser));
    getDao().need(user, ScreeningRoomUser.checklistItemEvents.to(ChecklistItemEvent.createdBy));
    getDao().need(user, ScreeningRoomUser.attachedFiles.to(AttachedFile.fileType));
    getDao().need(user, ScreeningRoomUser.facilityUsageRoles);
  }

  @Override
  protected void initializeViewer(ScreeningRoomUser user)
  {
    if (!!!user.isTransient()) {
      if (user instanceof ScreeningRoomUser) {
        warnAdminOnMismatchedDataSharingLevel((ScreeningRoomUser) user);
      }
    }

    _userRolesDataModel = null;
    _facilityUsageRolesDataModel = null;
    _newFacilityUsageRole = null;
    _newUserRole = null;
    _labName = null;
    _labAffiliation = null;
    _newLabAffiliation = null;
    _screensDataModel = null;
    _labMembersDataModel = null;
    _screenAssociatesDataModel = null;
    _attachedFiles.initialize(user, Sets.<AttachedFileType>newTreeSet(getDao().findAllEntitiesOfType(UserAttachedFileType.class)), null);
    _checklistItems.setEntity(user);
    _checklistItems.setChecklistItemGroups(CHECKLIST_ITEM_GROUPS);
    _lastPrimaryRoles = user.getPrimaryScreensaverUserRoles();
    _newPassword1 = null;
    _newPassword2 = null;
  }

  @Override
  protected boolean validateEntity(ScreeningRoomUser entity)
  {
    boolean isNewPasswordDefined = !NullSafeUtils.toString(_newPassword1, "").isEmpty() ||
      !NullSafeUtils.toString(_newPassword2, "").isEmpty();
    if (isNewPasswordDefined && !NullSafeUtils.nullSafeEquals(_newPassword1, _newPassword2)) {
      showMessage("users.passwordsDoNotMatch");
      return false;
    }
    boolean isLoginIdDefined = !StringUtils.isEmpty(entity.getLoginId());
    boolean isExistingPasswordDefined = !NullSafeUtils.toString(entity.getDigestedPassword(), "").isEmpty();
    if (isNewPasswordDefined && !isLoginIdDefined ||
      isLoginIdDefined && !isExistingPasswordDefined && !isNewPasswordDefined) {
      showMessage("users.loginIdAndPasswordRequired");
      return false;
    }
    return true;
  }

  @Override
  public void updateEntityProperties(ScreeningRoomUser user)
  {
    if (user.isHeadOfLab()) {
      user.getLab().setLabAffiliation(getLabAffiliation().getSelection());
    }
    else {
      Lab lab = getLabName().getSelection() == null ? null : getLabName().getSelection().getLab();
      user.setLab(lab);
    }
    if (StringUtils.isEmpty(user.getLoginId())) {
      user.setDigestedPassword(null);
    }
    else if (!NullSafeUtils.toString(_newPassword1, "").isEmpty()) {
      user.updateScreensaverPassword(_newPassword1);
    }
  }

  protected void recordUpdateActivity()
  {
    Set<ScreensaverUserRole> rolesAdded = Sets.difference(getEntity().getPrimaryScreensaverUserRoles(), _lastPrimaryRoles);
    if (!!!rolesAdded.isEmpty()) {
      recordUpdateActivity("primary roles added: " + Joiner.on(", ").join(Iterables.transform(rolesAdded, ScreensaverUserRole.ToDisplayableRoleName)));
    }
    Set<ScreensaverUserRole> rolesRemoved = Sets.difference(_lastPrimaryRoles, getEntity().getPrimaryScreensaverUserRoles());
    if (!!!rolesRemoved.isEmpty()) {
      recordUpdateActivity("primary roles removed: " + Joiner.on(", ").join(Iterables.transform(rolesRemoved, ScreensaverUserRole.ToDisplayableRoleName)));
    }
    if (!StringUtils.isEmpty(_newPassword1)) {
      recordUpdateActivity("password updated");
    }
    super.recordUpdateActivity();
  }
  
  public List<SelectItem> getUserClassificationSelections()
  {
    List<ScreeningRoomUserClassification> userClassifications;
    // handle the special Principal Investigator classification, since this
    // value must be selected at creation time only, as it affects lab
    // affiliation and lab members editable
    if (((ScreeningRoomUser) getEntity()).isHeadOfLab()) {
      userClassifications = Arrays.asList(ScreeningRoomUserClassification.PRINCIPAL_INVESTIGATOR);
    }
    else {
      userClassifications = new ArrayList<ScreeningRoomUserClassification>(Arrays.asList(ScreeningRoomUserClassification.values()));
      userClassifications.remove(ScreeningRoomUserClassification.PRINCIPAL_INVESTIGATOR);
    }
    if (getEntity().getEntityId() == null) {
      return JSFUtils.createUISelectItemsWithEmptySelection(userClassifications, ScreensaverConstants.REQUIRED_VOCAB_FIELD_PROMPT);
    }
    else {
      return JSFUtils.createUISelectItems(userClassifications);
    }
  }

  public UISelectOneEntityBean<LabHead> getLabName()
  {
    assert isScreeningRoomUserViewMode() && !isLabHeadViewMode();
    if (_labName == null) {
      SortedSet<LabHead> labHeads = _usersDao.findAllLabHeads();
      _labName = new UISelectOneEntityBean<LabHead>(labHeads, getScreeningRoomUser().getLab().getLabHead(), true, getDao()) {
        @Override
        protected String makeLabel(LabHead t) { return t.getLab().getLabName(); }
        @Override
        protected String getEmptyLabel()
        {
          return getEntity().getEntityId() == null ? ScreensaverConstants.REQUIRED_VOCAB_FIELD_PROMPT
            : super.getEmptyLabel();
        }
      };
    }
    return _labName;
  }

  public UISelectOneEntityBean<LabAffiliation> getLabAffiliation()
  {
    assert isLabHeadViewMode();
    if (_labAffiliation == null) {
      SortedSet<LabAffiliation> labAffiliations = new TreeSet<LabAffiliation>(new NullSafeComparator<LabAffiliation>() {
        @Override
        protected int doCompare(LabAffiliation o1, LabAffiliation o2)
        {
          return o1.getAffiliationName().compareTo(o2.getAffiliationName());
        }
      });
      labAffiliations.addAll(getDao().findAllEntitiesOfType(LabAffiliation.class));
      _labAffiliation = new UISelectOneEntityBean<LabAffiliation>(labAffiliations, getScreeningRoomUser().getLab().getLabAffiliation(), true, getDao()) {
        @Override
        protected String makeLabel(LabAffiliation t) { return t.getAffiliationName() + " (" + t.getAffiliationCategory() + ")"; }
      };
    }
    return _labAffiliation;
  }

  public LabAffiliation getNewLabAffiliation()
  {
    if (_newLabAffiliation == null) {
      _newLabAffiliation = new LabAffiliation();
      _newLabAffiliation.setAffiliationCategory(DEFAULT_NEW_LAB_AFFILIATION_CATEGORY);
    }
    return _newLabAffiliation;
  }

  public List<SelectItem> getAffiliationCategorySelections()
  {
    return JSFUtils.createUISelectItems(Arrays.asList(AffiliationCategory.values()));
  }

  public DataModel getFacilityUsageRolesDataModel()
  {
    if (_facilityUsageRolesDataModel == null) {
      SortedSet<FacilityUsageRole> facilityUsages = Sets.newTreeSet(getScreeningRoomUser().getFacilityUsageRoles());
      _facilityUsageRolesDataModel = new ListDataModel(Lists.newArrayList(facilityUsages));
    }
    return _facilityUsageRolesDataModel;
  }

  public UISelectOneBean<FacilityUsageRole> getNewFacilityUsageRole()
  {
    if (_newFacilityUsageRole == null) {
      Collection<FacilityUsageRole> candidateNewFacilityUsageRoles = 
        Sets.difference(Sets.newHashSet(FacilityUsageRole.values()), 
                        getScreeningRoomUser().getFacilityUsageRoles());
      _newFacilityUsageRole = new UISelectOneBean<FacilityUsageRole>(candidateNewFacilityUsageRoles, null, true) {
        @Override
        protected String makeLabel(FacilityUsageRole fu) { return fu.getDisplayableName(); }
        @Override
        protected String getEmptyLabel()
        {
          return ScreensaverConstants.REQUIRED_VOCAB_FIELD_PROMPT;
        }
      };
    }
    return _newFacilityUsageRole;
  }
  
  public DataModel getUserRolesDataModel()
  {
    if (_userRolesDataModel == null) {
      List<ScreensaverUserRole> userRoles = Lists.newArrayList(getEntity().getPrimaryScreensaverUserRoles());
      Collections.sort(userRoles, USER_ROLE_COMPARATOR);
      _userRolesDataModel = new ListDataModel(userRoles);
    }
    return _userRolesDataModel;
  }

  public UISelectOneBean<ScreensaverUserRole> getNewUserRole()
  {
    if (_newUserRole == null) {
      Collection<ScreensaverUserRole> candidateNewUserRoles = new TreeSet<ScreensaverUserRole>();
      for (ScreensaverUserRole userRole : ScreensaverUserRole.values()) {
        if (!userRole.isAdministrative() &&
          // hide if a user already has this role (primary or implied)
          !getEntity().getScreensaverUserRoles().contains(userRole) &&
          // hide if this role implies an extant primary user role (forces admin to remove the lower role first, before adding the higher role)
          Sets.intersection(userRole.getImpliedRoles(), getEntity().getPrimaryScreensaverUserRoles()).isEmpty()) {
          candidateNewUserRoles.add(userRole);
        }
      }

      // At ICCB-L, the RNAi DSL 2 role is not an option, so we hide it at the UI level; we maintain it in our model for consistency with the SM DSL roles
      if (IccblScreensaverConstants.FACILITY_NAME.equals(getApplicationProperties().getFacility())) {
        candidateNewUserRoles.remove(ScreensaverUserRole.RNAI_DSL_LEVEL2_MUTUAL_POSITIVES);
      }

      _newUserRole = new UISelectOneBean<ScreensaverUserRole>(candidateNewUserRoles, null, true) {
        @Override
        protected String makeLabel(ScreensaverUserRole r) { return r.getDisplayableRoleName(); }
        @Override
        protected String getEmptyLabel()
        {
          return ScreensaverConstants.REQUIRED_VOCAB_FIELD_PROMPT;
        }
      };
    }
    return _newUserRole;
  }
  
  public DataModel getRnaiScreensDataModel()
  {
    initScreensDataModels();
    return _screensDataModel.get(ScreenType.RNAI);
  }

  public DataModel getSmallMoleculeScreensDataModel()
  {
    initScreensDataModels();
    return _screensDataModel.get(ScreenType.SMALL_MOLECULE);
  }

  private void initScreensDataModels()
  {
    if (_screensDataModel == null && isScreeningRoomUserViewMode())
    {
      List<ScreenAndRole> screensAndRoles = new ArrayList<ScreenAndRole>();
      for (Screen screen : getScreeningRoomUser().getAllAssociatedScreens()) {
        // note: if both Lead Screener and PI, show Lead Screener
        String role =
        getScreeningRoomUser().getScreensLed().contains(screen) ? "Lead Screener" :
          (getLabHead() != null && getLabHead().getScreensHeaded().contains(screen)) ? "Lab Head (PI)" :
            "Collaborator";
        if (!screen.isRestricted()) {
          screensAndRoles.add(new ScreenAndRole(screen, role));
        }
      }
      Multimap<ScreenType,ScreenAndRole> screenType2ScreenAndRole = HashMultimap.create();
      for (ScreenAndRole screenAndRole : screensAndRoles) {
        screenType2ScreenAndRole.put(screenAndRole.getScreen().getScreenType(),
                                     screenAndRole);
      }
      _screensDataModel = new HashMap<ScreenType,DataModel>();
      for (ScreenType screenType : ScreenType.values()) {
        if (screenType2ScreenAndRole.containsKey(screenType)) {
          ArrayList<ScreenAndRole> screensAndRolesOfType = new ArrayList<ScreenAndRole>(screenType2ScreenAndRole.get(screenType));
          Collections.sort(screensAndRolesOfType);
          _screensDataModel.put(screenType,
                                new ListDataModel(screensAndRolesOfType));
        }
      }
    }
  }

  public DataModel getLabMembersDataModel()
  {
    if (_labMembersDataModel == null && isScreeningRoomUserViewMode())
    {
      _labMembersDataModel = new ListDataModel(Ordering.from(ScreensaverUserComparator.getInstance()).sortedCopy(getScreeningRoomUser().getLab().getLabMembers()));
    }
    return _labMembersDataModel;
  }

  public DataModel getScreenAssociatesDataModel()
  {
    if (_screenAssociatesDataModel == null && isScreeningRoomUserViewMode())
    {
      Set<ScreeningRoomUser> screenAssociates = new TreeSet<ScreeningRoomUser>(ScreensaverUserComparator.getInstance());
      // note: we only want associates from the user's screens, and not *all* of
      // his associates, as returned by user.getAssociatedUsers(), since this
      // also returns lab members and lab head (which are shown elsewhere by
      // this viewer)
      for (Screen screen : getScreeningRoomUser().getAllAssociatedScreens()) {
        screenAssociates.addAll(screen.getAssociatedScreeningRoomUsers());
      }
      screenAssociates.remove(getScreeningRoomUser());
      _screenAssociatesDataModel = new ListDataModel(Lists.newArrayList(screenAssociates));
    }
    return _screenAssociatesDataModel;
  }

  public AttachedFiles getAttachedFiles()
  {
    return _attachedFiles;
  }

  public ChecklistItems getChecklistItems()
  {
    return _checklistItems;
  }

  public String getNewPassword1()
  {
    return _newPassword1;
  }

  public void setNewPassword1(String newPassword1)
  {
    _newPassword1 = newPassword1;
  }

  public String getNewPassword2()
  {
    return _newPassword2;
  }

  public void setNewPassword2(String newPassword2)
  {
    _newPassword2 = newPassword2;
  }

  @Override
  protected void initializeNewEntity(ScreeningRoomUser newUser)
  {
    ScreensaverUser currentUser = getScreensaverUser();
    if (newUser instanceof LabHead &&
      !(currentUser instanceof AdministratorUser &&
      ((AdministratorUser) currentUser).isUserInRole(ScreensaverUserRole.LAB_HEADS_ADMIN))) {
      throw new OperationRestrictedException("add a new lab head");
    }
  }

  // TODO: move logic to edu.harvard.med.iccbl.screensaver.policy
  private void warnAdminOnMismatchedDataSharingLevel(ScreeningRoomUser user)
  {
    if (isReadAdmin()) {
      if (!!!user.isHeadOfLab()) {
        user = getDao().reloadEntity(user, true, 
                                     ScreeningRoomUser.LabHead.to(ScreeningRoomUser.roles));
        getDao().needReadOnly(user, ScreeningRoomUser.roles);
        if (user.getLab().getLabHead() != null) {
          SortedSet<ScreensaverUserRole> labHeadSmDslRoles = Sets.newTreeSet(Sets.intersection(DataSharingLevelMapper.UserSmDslRoles, user.getLab().getLabHead().getScreensaverUserRoles()));
          SortedSet<ScreensaverUserRole> userSmDslRoles = Sets.newTreeSet(Sets.intersection(DataSharingLevelMapper.UserSmDslRoles, user.getScreensaverUserRoles()));
          if (!!!labHeadSmDslRoles.isEmpty() && !!!userSmDslRoles.isEmpty()) {
            ScreensaverUserRole labHeadLeastRestrictiveRole = labHeadSmDslRoles.last();
            ScreensaverUserRole userLeastRestrictiveRole = userSmDslRoles.last();
            if (labHeadLeastRestrictiveRole.compareTo(userLeastRestrictiveRole) > 0) {
              showMessage("users.dataSharingLevelTooRestrictive", userLeastRestrictiveRole.getDisplayableRoleName(), labHeadLeastRestrictiveRole.getDisplayableRoleName());
            }
            else if (labHeadLeastRestrictiveRole.compareTo(userLeastRestrictiveRole) < 0) {
              showMessage("users.dataSharingLevelTooLoose", userLeastRestrictiveRole.getDisplayableRoleName(), labHeadLeastRestrictiveRole.getDisplayableRoleName());
            }
          }
        }
      }
      else {
        LabHead labHead = (LabHead) user;
        for (Screen screen : labHead.getScreensHeaded()) {
          if (screen.getScreenType() == ScreenType.SMALL_MOLECULE) {
            if ((labHead.getScreensaverUserRoles().contains(ScreensaverUserRole.SM_DSL_LEVEL1_MUTUAL_SCREENS) && screen.getDataSharingLevel().compareTo(ScreenDataSharingLevel.MUTUAL_SCREENS) > 0) ||
              (labHead.getScreensaverUserRoles().contains(ScreensaverUserRole.SM_DSL_LEVEL2_MUTUAL_POSITIVES) && screen.getDataSharingLevel().compareTo(ScreenDataSharingLevel.MUTUAL_POSITIVES) > 0)) {
              showMessage("users.labHeadScreenTooRestrictive", screen.getFacilityId(), screen.getDataSharingLevel());
            }
          }
        }
      }
    }
  }

  @UICommand
  public String addFacilityUsageRole()
  {
    if (getNewFacilityUsageRole().getSelection() != null) {
      getScreeningRoomUser().getFacilityUsageRoles().add(getNewFacilityUsageRole().getSelection());
      _newFacilityUsageRole = null;
      _facilityUsageRolesDataModel = null;
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UICommand
  public String deleteFacilityUsageRole()
  {
    FacilityUsageRole facilityUsage = (FacilityUsageRole) getRequestMap().get("element");
    getScreeningRoomUser().getFacilityUsageRoles().remove(facilityUsage);
    _newFacilityUsageRole = null;
    _facilityUsageRolesDataModel = null;
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UICommand
  public String addUserRole()
  {
    if (getNewUserRole().getSelection() != null) {
      getEntity().addScreensaverUserRole(getNewUserRole().getSelection());
      _newUserRole = null;
      _userRolesDataModel = null;
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UICommand
  public String deleteUserRole()
  {
    ScreensaverUserRole primaryRole = (ScreensaverUserRole) getRequestMap().get("element");
    getEntity().removeScreensaverUserRole(primaryRole);
    _newUserRole = null;
    _userRolesDataModel = null;
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UICommand
  @Transactional
  public String addNewLabAffiliation()
  {
    if (StringUtils.isEmpty(_newLabAffiliation.getAffiliationName())) {
      reportApplicationError("new lab affiliation name is required");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    if (getDao().findEntityByProperty(LabAffiliation.class,
                                      "affiliationName",
                                      _newLabAffiliation.getAffiliationName()) != null) {
      showMessage("duplicateEntity", "lab affiliation");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    getDao().persistEntity(_newLabAffiliation);
    getDao().flush();

    // force reload of lab affiliation selections
    _labAffiliation = null;

    // set user's lab affiliation to new affiliation
    getLabAffiliation().setSelection(_newLabAffiliation);

    _newLabAffiliation = null;
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UICommand
  @Transactional(readOnly=true)
  public String addLabMember()
  {
    if (!isLabHeadViewMode()) {
      reportApplicationError("can only create lab members for lab heads");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    if (isEditMode()) {
      reportApplicationError("cannot add lab members while editing lab head");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    ScreeningRoomUser labHead = getDao().reloadEntity(getEntity());
    ScreeningRoomUser newLabMember = new ScreeningRoomUser((AdministratorUser) getScreensaverUser());
    newLabMember.setLab(labHead.getLab());
    return getThisProxy().editNewEntity(newLabMember);
  }

  @UICommand
  @Transactional(readOnly=true)
  public String addScreen()
  {
    return doAddScreen(null);
  }

  @UICommand
  @Transactional(readOnly=true)
  public String addRnaiScreen()
  {
    return doAddScreen(ScreenType.RNAI);
  }

  @UICommand
  @Transactional(readOnly=true)
  public String addSmallMoleculeScreen()
  {
    return doAddScreen(ScreenType.SMALL_MOLECULE);
  }

  private String doAddScreen(ScreenType screenType)
  {
    if (!isScreeningRoomUserViewMode()) {
      throw new DevelopmentException("cannot create screen for administrator user");
    }
    if (isEditMode()) {
      throw new DevelopmentException("cannot create screen while editing user");
    }
    Screen screen = _screenGenerator.createPrimaryScreen((AdministratorUser) getScreensaverUser(),
                                            getEntity(),
                                            screenType);
    return _screenDetailViewer.editNewEntity(screen);
  }

  @UICommand
  public String addServiceActivity()
  {
    AdministratorUser admin = (AdministratorUser) getScreensaverUser();
    if (!admin.getScreensaverUserRoles().contains(ScreensaverUserRole.SERVICE_ACTIVITY_ADMIN)) {
      throw new OperationRestrictedException("admin user does not have the " + ScreensaverUserRole.SERVICE_ACTIVITY_ADMIN +
        " role");
    }
    return _activityViewer.editNewEntity(new ServiceActivity(admin,
                                                             admin,
                                                             new LocalDate(),
                                                             null,
                                                             getEntity()));
  }

  @UICommand
  public String browseScreens()
  {
    return doBrowseScreens(null);
  }

  @UICommand
  public String browseRnaiScreens()
  {
    return doBrowseScreens(ScreenType.RNAI);
  }

  @UICommand
  public String browseSmallMoleculeScreens()
  {
    return doBrowseScreens(ScreenType.SMALL_MOLECULE);
  }

  private String doBrowseScreens(ScreenType screenType)
  {
    _screensBrowser.searchScreensForUser(getScreeningRoomUser());
    if (screenType != null) {
      _screensBrowser.getColumnManager().getColumn("Screen Type").clearCriteria().addCriterion(new Criterion(Operator.EQUAL, screenType));
    }
    return BROWSE_SCREENS;
  }

  @SuppressWarnings("unchecked")
  @UICommand
  public String browseLabMembers()
  {
    HashSet<ScreeningRoomUser> labMembers = Sets.newHashSet((List<ScreeningRoomUser>) getLabMembersDataModel().getWrappedData());
    ((UserSearchResults) getContextualSearchResults()).searchUsers(labMembers,
                                                                   "Lab Members of lab " + getEntity().getLab().getLabName());
    return BROWSE_SCREENERS;
  }

  @SuppressWarnings("unchecked")
  @UICommand
  public String browseScreenAssociates()
  {
    HashSet<ScreeningRoomUser> associates = Sets.newHashSet((List<ScreeningRoomUser>) getScreenAssociatesDataModel().getWrappedData());
    ((UserSearchResults) getContextualSearchResults()).searchUsers(associates,
                                                                   "Screening Associates of screener " +
                                                                     getEntity().getFullNameFirstLast());
    return BROWSE_SCREENERS;
  }

  @Override
  protected String postEditAction(EditResult editResult)
  {
    switch (editResult) {
    case CANCEL_EDIT: return getThisProxy().reload();
    case SAVE_EDIT: return getThisProxy().reload();
    case CANCEL_NEW: return VIEW_MAIN;
    case SAVE_NEW: return getThisProxy().reload();
    default: return null;
    }
  }
  
  public int getUserActivitiesCount()
  {
    return getEntity().getAssociatedActivities().size();
  }

  public Activity getLastUserActivity()
  {
    if (getEntity().getAssociatedActivities().isEmpty()) {
      return null;
    }
    return getEntity().getAssociatedActivities().last();
  }

  @UICommand
  public String browseUserActivities()
  {
    _activitiesBrowser.searchActivitiesForUser(getEntity());
    return BROWSE_ACTIVITIES;
  }
}

