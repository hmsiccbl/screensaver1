// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/ui/screens/ScreenViewer.java $
// $Id: ScreenViewer.java 2304 2008-04-14 13:31:27Z ant4 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
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
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.UsersDAO;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.AffiliationCategory;
import edu.harvard.med.screensaver.model.users.Lab;
import edu.harvard.med.screensaver.model.users.LabAffiliation;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUserClassification;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.ui.screens.ScreenDetailViewer;
import edu.harvard.med.screensaver.ui.searchresults.ScreenSearchResults;
import edu.harvard.med.screensaver.ui.searchresults.ScreenerSearchResults;
import edu.harvard.med.screensaver.ui.searchresults.StaffSearchResults;
import edu.harvard.med.screensaver.ui.searchresults.UserSearchResults;
import edu.harvard.med.screensaver.ui.table.Criterion;
import edu.harvard.med.screensaver.ui.table.Criterion.Operator;
import edu.harvard.med.screensaver.ui.util.EditableViewer;
import edu.harvard.med.screensaver.ui.util.JSFUtils;
import edu.harvard.med.screensaver.ui.util.ScreensaverUserComparator;
import edu.harvard.med.screensaver.ui.util.UISelectOneBean;
import edu.harvard.med.screensaver.ui.util.UISelectOneEntityBean;
import edu.harvard.med.screensaver.util.NullSafeComparator;
import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * User Viewer backing bean.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class UserViewer extends AbstractBackingBean implements EditableViewer
{
  // static members

  private static Logger log = Logger.getLogger(UserViewer.class);
  private static final ScreensaverUserRole EDITING_ROLE = ScreensaverUserRole.USERS_ADMIN;
  private static final Comparator<ScreensaverUserRole> USER_ROLE_COMPARATOR = new Comparator<ScreensaverUserRole>() {
    public int compare(ScreensaverUserRole r1, ScreensaverUserRole r2)
    {
      return ((Integer) r1.ordinal()).compareTo(r2.ordinal());
    }
  };
  private static final AffiliationCategory DEFAULT_NEW_LAB_AFFILIATION_CATEGORY = AffiliationCategory.HMS;


  // instance data members

  private UserViewer _thisProxy;
  private ScreenDetailViewer _screenDetailViewer;
  private GenericEntityDAO _dao;
  private UsersDAO _usersDao;
  private ScreenerSearchResults _screenerSearchResults;
  private StaffSearchResults _staffSearchResults;

  private ScreensaverUser _user;
  private boolean _isEditMode;
  private DataModel _userRolesDataModel;
  private UISelectOneBean<ScreensaverUserRole> _newUserRole;
  private UISelectOneEntityBean<LabHead> _labName;
  private UISelectOneEntityBean<LabAffiliation> _labAffiliation;
  private LabAffiliation _newLabAffiliation;
  private HashMap<ScreenType,DataModel> _screensDataModel;
  private DataModel _labMembersDataModel;
  private DataModel _screenCollaboratorsDataModel;
  private ScreenSearchResults _screensBrowser;


  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected UserViewer()
  {
  }

  public UserViewer(UserViewer _userViewerProxy,
                    ScreenDetailViewer screenDetailViewer,
                    GenericEntityDAO dao,
                    UsersDAO usersDao,
                    ScreenerSearchResults screenerSearchResults,
                    StaffSearchResults staffSearchResults,
                    ScreenSearchResults screensBrowser)
  {
    _thisProxy = _userViewerProxy;
    _screenDetailViewer = screenDetailViewer;
    _dao = dao;
    _usersDao = usersDao;
    _screenerSearchResults = screenerSearchResults;
    _staffSearchResults = staffSearchResults;
    _screensBrowser = screensBrowser;
  }


  // public methods

  public AbstractEntity getEntity()
  {
    return getUser();
  }

  public ScreensaverUser getUser()
  {
    return _user;
  }
  
  /**
   * @return true if the logged in user is the same as the user being shown by the user viewer
   * 
   */
  public boolean isMe()
  {
    return _user.equals(getScreensaverUser());
  }
  
  public boolean isScreeningRoomUserViewMode()
  {
    return getScreeningRoomUser() != null;
  }
  
  public boolean islabHeadViewMode()
  {
    return getLabHead() != null;
  }
  
  public boolean isAdministratorUserViewMode()
  {
    return getAdministratorUser() != null;
  }

  public ScreeningRoomUser getScreeningRoomUser()
  {
    if (_user instanceof ScreeningRoomUser) {
      return (ScreeningRoomUser) _user;
    }
    return null;
  }

  public LabHead getLabHead()
  {
    if (_user instanceof LabHead) {
      return (LabHead) _user;
    }
    return null;
  }

  public AdministratorUser getAdministratorUser()
  {
    if (_user instanceof AdministratorUser) {
      return (AdministratorUser) _user;
    }
    return null;
  }

  @Transactional
  public void setUser(ScreensaverUser user)
  {
    if (user.getEntityId() != null) {
      user = _dao.reloadEntity(user,
                               true,
                               "screensaverUserRoles");
      if (user instanceof ScreeningRoomUser) {
        _dao.need(user,
                  "labHead.labAffiliation",
                  "labAffiliation",
                  "labMembers");
        _dao.need(user, "screensLed.statusItems");
        _dao.need(user, "screensHeaded.statusItems");
        _dao.need(user, "screensCollaborated.statusItems");
        _dao.need(user, "checklistItems");
      }
    }
    _user = user;
    
    resetView();
  }

  @Override
  public String reload()
  {
    if (_user == null || _user.getEntityId() == null) {
      _user = null;
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    return _thisProxy.viewUser(_user);
  }

  @Override
  protected ScreensaverUserRole getEditableAdminRole()
  {
    return EDITING_ROLE;
  }

  public boolean isEditMode()
  {
    return _isEditMode;
  }

  @UIControllerMethod
  public String edit()
  {
    _isEditMode = true;
    return VIEW_USER;
  }

  @UIControllerMethod
  public String cancel()
  {
    _isEditMode = false;
    if (_user.getEntityId() == null) {
      return VIEW_MAIN;
    }
    return _thisProxy.viewUser(_user);
  }

  @UIControllerMethod
  @Transactional
  public String save()
  {
    _isEditMode = false;
    
    if (_user.getEntityId() == null) {
      updateUserProperties();
      _dao.persistEntity(_user);
    }
    else {
      _dao.reattachEntity(_user);
      updateUserProperties();
    }
    _dao.flush();
    return _thisProxy.viewUser(_user);
  }

  private void updateUserProperties()
  {
    if (isScreeningRoomUserViewMode()) {
      ScreeningRoomUser user = getScreeningRoomUser();
      if (user.isHeadOfLab()) {
        user.getLab().setLabAffiliation(getLabAffiliation().getSelection());
      }
      else {
        Lab lab = getLabName().getSelection() == null ? null : getLabName().getSelection().getLab();
        user.setLab(lab);
      }
    }
  }

  public List<SelectItem> getUserClassificationSelections()
  {
    List<ScreeningRoomUserClassification> userClassifications;
    // handle the special Principal Investigator classification, since this
    // value must be selected at creation time only, as it affects lab
    // affiliation and lab members editable
    if (((ScreeningRoomUser) getUser()).isHeadOfLab()) {
      userClassifications = Arrays.asList(ScreeningRoomUserClassification.PRINCIPAL_INVESTIGATOR);
    }
    else {
      userClassifications = new ArrayList<ScreeningRoomUserClassification>(Arrays.asList(ScreeningRoomUserClassification.values()));
      userClassifications.remove(ScreeningRoomUserClassification.PRINCIPAL_INVESTIGATOR);
    }
    return JSFUtils.createUISelectItems(userClassifications, _user.getEntityId() == null);
  }

  public UISelectOneEntityBean<LabHead> getLabName()
  {
    assert isScreeningRoomUserViewMode() && !islabHeadViewMode();
    if (_labName == null) {
      SortedSet<LabHead> labHeads = _usersDao.findAllLabHeads();
      labHeads.add(null);
      _labName = new UISelectOneEntityBean<LabHead>(labHeads, getScreeningRoomUser().getLab().getLabHead(), _dao) {
        @Override
        protected String getLabel(LabHead t) { return t == null ? "<missing>" : t.getLab().getLabName(); }
      };
    }
    return _labName;
  }

  public UISelectOneEntityBean<LabAffiliation> getLabAffiliation()
  {
    assert islabHeadViewMode();
    if (_labAffiliation == null) {
      SortedSet<LabAffiliation> labAffiliations = new TreeSet<LabAffiliation>(new NullSafeComparator<LabAffiliation>() {
        @Override
        protected int doCompare(LabAffiliation o1, LabAffiliation o2)
        {
          return o1.getAffiliationName().compareTo(o2.getAffiliationName());
        }
      });
      labAffiliations.addAll(_dao.findAllEntitiesOfType(LabAffiliation.class));
      labAffiliations.add(null);
      _labAffiliation = new UISelectOneEntityBean<LabAffiliation>(labAffiliations, getScreeningRoomUser().getLab().getLabAffiliation(), _dao) {
        protected String getLabel(LabAffiliation t) { return t == null ? "<none>" : (t.getAffiliationName() + " (" + t.getAffiliationCategory() + ")"); }
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

  public DataModel getUserRolesDataModel()
  {
    if (_userRolesDataModel == null) {
      List<ScreensaverUserRole> userRoles = new ArrayList<ScreensaverUserRole>();
      userRoles.addAll(_user.getScreensaverUserRoles());
      userRoles.remove(ScreensaverUserRole.SCREENER); // hide this implicit role, per requirements
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
        if (!userRole.isAdministrative() && !_user.getScreensaverUserRoles().contains(userRole)) {
          candidateNewUserRoles.add(userRole);
        }
      }
      _newUserRole = new UISelectOneBean<ScreensaverUserRole>(candidateNewUserRoles) {
        @Override
        protected String getLabel(ScreensaverUserRole r) { return r.getDisplayableRoleName(); }
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
        screensAndRoles.add(new ScreenAndRole(screen, role));
      }
      Multimap<ScreenType,ScreenAndRole> screenType2ScreenAndRole = new HashMultimap<ScreenType,ScreenAndRole>();
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
      List<ScreeningRoomUser> labMembers = new ArrayList<ScreeningRoomUser>();
      labMembers.add(getScreeningRoomUser().getLab().getLabHead());
      labMembers.addAll(getScreeningRoomUser().getLab().getLabMembers());
      labMembers.remove(getScreeningRoomUser());
      _labMembersDataModel = new ListDataModel(labMembers);
    }
    return _labMembersDataModel;
  }

  public DataModel getScreenCollaboratorsDataModel()
  {
    if (_screenCollaboratorsDataModel == null && isScreeningRoomUserViewMode())
    {
      Set<ScreeningRoomUser> collaborators = new TreeSet<ScreeningRoomUser>(ScreensaverUserComparator.getInstance());
      for (Screen screen : getScreeningRoomUser().getAllAssociatedScreens()) {
        collaborators.addAll(screen.getCollaborators());
      }
      collaborators.remove(getScreeningRoomUser());
      _screenCollaboratorsDataModel = new ListDataModel(new ArrayList<ScreeningRoomUser>(collaborators));
    }
    return _screenCollaboratorsDataModel;
  }

  
  /* JSF Application methods */

  @UIControllerMethod
  @Transactional
  public String viewUser()
  {
    Integer entityId = Integer.parseInt(getRequestParameter("entityId").toString());
    if (entityId == null) {
      throw new IllegalArgumentException("missing 'entityId' request parameter");
    }
    ScreensaverUser user = _dao.findEntityById(ScreensaverUser.class, entityId);
    if (user == null) {
      throw new IllegalArgumentException(ScreensaverUser.class.getSimpleName() + " " + entityId + " does not exist");
    }
    return _thisProxy.viewUser(user);
  }

  @UIControllerMethod
  @Transactional
  public String viewUser(ScreensaverUser user)
  {
    // TODO: implement as aspect
    if (user.isRestricted()) {
      showMessage("restrictedEntity", "user " + user.getFullNameFirstLast());
      log.warn("user unauthorized to view " + user);
      return REDISPLAY_PAGE_ACTION_RESULT;
    }

    setUser(user);

    UserSearchResults searchResults =
      isScreeningRoomUserViewMode() ? _screenerSearchResults : _staffSearchResults;
    // calling viewUser() is a request to view the most up-to-date, persistent
    // version of the user, which means the usersBrowser must also be
    // updated to reflect the persistent version of the user
    searchResults.refetch();

    // all users are viewed within the context of a search results, providing
    // the user with user search options at all times
    // UserSearchResults will call our setUser() method
    if (!searchResults.viewEntity(user)) {
      searchResults.searchUsers();
      // note: calling viewEntity(user) will only work as long as
      // UserSearchResults continues to use InMemoryDataTableModel
      searchResults.viewEntity(user);
    }
    return BROWSE_SCREENERS;

  }

  @UIControllerMethod
  public String editNewUser(ScreensaverUser newUser)
  {
    ScreensaverUser currentUser = getScreensaverUser();
    if (newUser instanceof LabHead &&
      !(currentUser instanceof AdministratorUser &&
      ((AdministratorUser) currentUser).isUserInRole(ScreensaverUserRole.LAB_HEADS_ADMIN))) {
      showMessage("restrictedOperation", "add a new lab head");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    if (!(currentUser instanceof AdministratorUser &&
      ((AdministratorUser) currentUser).isUserInRole(ScreensaverUserRole.USERS_ADMIN))) {
      showMessage("restrictedOperation", "add a new user");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    // by default, we add login privileges for any new user
    newUser.addScreensaverUserRole(ScreensaverUserRole.SCREENSAVER_USER);
    
    setUser(newUser);
    _isEditMode = true;
    return VIEW_USER;
  }

  @UIControllerMethod
  public String addUserRole()
  {
    getUser().addScreensaverUserRole(getNewUserRole().getSelection());
    _newUserRole = null;
    _userRolesDataModel = null;
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String deleteUserRole()
  {
    getUser().removeScreensaverUserRole((ScreensaverUserRole) getRequestMap().get("element"));
    _newUserRole = null;
    _userRolesDataModel = null;
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  @UIControllerMethod
  @Transactional
  public String addNewLabAffiliation()
  {
    if (StringUtils.isEmpty(_newLabAffiliation.getAffiliationName())) {
      reportApplicationError("new lab affiliation name is required");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    if (_dao.findEntityByProperty(LabAffiliation.class, 
                                  "affiliationName", 
                                  _newLabAffiliation.getAffiliationName()) != null) {
      showMessage("duplicateEntity", "lab affiliation");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    _dao.persistEntity(_newLabAffiliation);
    _dao.flush();
    
    // force reload of lab affiliation selections
    _labAffiliation = null; 

    // set user's lab affiliation to new affiliation
    getLabAffiliation().setSelection(_newLabAffiliation);
    
    _newLabAffiliation = null;
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  @UIControllerMethod
  public String addLabMember()
  {
    if (!islabHeadViewMode()) {
      reportApplicationError("cannot only create lab members for lab heads");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    if (isEditMode()) {
      reportApplicationError("cannot add lab members while editing lab head");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    ScreeningRoomUser newLabMember = new ScreeningRoomUser();
    newLabMember.setLab(((LabHead) _user).getLab());
    return _thisProxy.editNewUser(newLabMember);
  }
  
  @UIControllerMethod
  public String addScreen()
  {
    return doAddScreen(null);
  }
  
  @UIControllerMethod
  public String addRnaiScreen()
  {
    return doAddScreen(ScreenType.RNAI);
  }

  @UIControllerMethod
  public String addSmallMoleculeScreen()
  {
    return doAddScreen(ScreenType.SMALL_MOLECULE);
  }

  private String doAddScreen(ScreenType screenType)
  {
    if (!isScreeningRoomUserViewMode()) {
      reportApplicationError("cannot create screen for administrator user");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    if (isEditMode()) {
      reportApplicationError("cannot create screen while editing user");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    return _screenDetailViewer.editNewScreen(getScreeningRoomUser(), 
                                             screenType);
  }
  
  @UIControllerMethod
  public String viewAllScreens()
  {
    return doViewAllScreens(null);
  }
  
  @UIControllerMethod
  public String viewAllRnaiScreens()
  {
    return doViewAllScreens(ScreenType.RNAI);
  }

  @UIControllerMethod
  public String viewAllSmallMoleculeScreens()
  {
    return doViewAllScreens(ScreenType.SMALL_MOLECULE);
  }

  private String doViewAllScreens(ScreenType screenType)
  {
    _screensBrowser.searchScreensForUser(getScreeningRoomUser());
    if (screenType != null) {
      _screensBrowser.getColumnManager().getColumn("Screen Type").clearCriteria().addCriterion(new Criterion(Operator.EQUAL, screenType));
    }
    return BROWSE_SCREENS;
  }
  
  
  // private methods

  private void resetView()
  {
    _isEditMode = false;
    _userRolesDataModel = null;
    _newUserRole = null;
    _labName = null;
    _labAffiliation = null;
    _newLabAffiliation = null;
    _screensDataModel = null;
    _labMembersDataModel = null;
    _screenCollaboratorsDataModel = null;
  }
}

