// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.policy;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Predicates;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import edu.harvard.med.screensaver.db.Criterion.Operator;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.Query;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.db.hqlbuilder.JoinType;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivity;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickLiquidTransfer;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.SmallMoleculeCherryPickRequest;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.AnnotationValue;
import edu.harvard.med.screensaver.model.screenresults.AssayWell;
import edu.harvard.med.screensaver.model.screenresults.DataColumn;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.CherryPickScreening;
import edu.harvard.med.screensaver.model.screens.LabActivity;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenDataSharingLevel;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.Study;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.policy.CurrentScreensaverUser;
import edu.harvard.med.screensaver.policy.DefaultEntityViewPolicy;

/**
 * An EntityViewPolicy implementation for ICCB-Longwood that is used by the production web application. 
 */
public class IccblEntityViewPolicy extends DefaultEntityViewPolicy
{
  public static final String GRAY_LIBRARY_SCREEN_FUNDING_SUPPORT_NAME = "Gray Library Screen";

  private static Logger log = Logger.getLogger(IccblEntityViewPolicy.class);

  private CurrentScreensaverUser _currentScreensaverUser;
  private ScreensaverUser _screensaverUser;
  private GenericEntityDAO _dao;
  private Set<Screen> _myScreens;
  private Set<Screen> _publicScreens;
  private HashMultimap<String,Screen> _screensForFundingSupport = HashMultimap.create();
  private Set<Screen> _othersVisibleScreens;
  private Map<ScreenType,Set<Screen>> _level1AndLevel2Screens;
  private Set<Screen> _mutualScreens;
  private Set<Screen> _mutualPositiveScreens;
  private Set<String> _mutualPositiveWellIds;
  private Set<Integer> _mutualPositiveScreenResultIds;
  
  protected IccblEntityViewPolicy() {}

  public IccblEntityViewPolicy(CurrentScreensaverUser user,
                               GenericEntityDAO dao)
  {
    _currentScreensaverUser = user;
    _dao = dao;
  }
  
  /**
   * @motivation for unit tests
   */
  public IccblEntityViewPolicy(ScreensaverUser user,
                               GenericEntityDAO dao)
  {
    _screensaverUser = user;
    _dao = dao;
  }
  
  public ScreensaverUser getScreensaverUser() 
  {
    if (_screensaverUser == null) {
      _screensaverUser = _currentScreensaverUser.getScreensaverUser();
    }
    return _screensaverUser;
  }

  public AdministrativeActivity visit(AdministrativeActivity entity)
  {
    return getScreensaverUser().getScreensaverUserRoles().contains(ScreensaverUserRole.READ_EVERYTHING_ADMIN) ? entity : null;
  }

  public AnnotationType visit(AnnotationType entity)
  {
    return visit((Study) entity.getStudy()) == null ? null : entity;
  }

  public AnnotationValue visit(AnnotationValue entity)
  {
    return visit(entity.getAnnotationType()) == null ? null : entity;
  }

  public CherryPickLiquidTransfer visit(CherryPickLiquidTransfer entity)
  {
    return visit((LabActivity) entity) == null ? null : entity;
  }

  public Library visit(Library entity)
  {
    ScreeningRoomUser owner = entity.getOwner();
    ScreensaverUser user = getScreensaverUser();
   
    // Equals is based on EntityId if present, otherwise by instance equality
    //In this example case Entity id is empty, so comparison is based on instance
    //I assume that normally this field is not empty
    
    //if owner == null : not a validation library 
    //TODO add || isLabheadLibraryOwner(owner) , however this gives currently "No session" error.
    return owner == null || owner.equals(user) || user.getScreensaverUserRoles().contains(ScreensaverUserRole.LIBRARIES_ADMIN)
      ? entity : null;
  }
    
  public ResultValue visit(ResultValue entity)
  {
    // exceptions for positive RVs, allowing a subset of RVs to be visible, even if screen result is not visible
    if (isAllowedAccessToResultValueDueToMutualPositive(entity.isPositive(),
                                                        entity.getDataColumn().getScreenResult().getScreen(),
                                                        entity.getWell().getWellId())) {
      return entity;
    }

    return visit(entity.getDataColumn().getScreenResult()) == null ? null : entity;
  }

  @Override
  public boolean isAllowedAccessToResultValueDueToMutualPositive(boolean isPositive, Screen screen, String wellId)
  {
    if (isPositive) {
      if (findOthersVisibleScreens().contains(screen)) {
        if (findMutualPositiveWellIds().contains(wellId)) {
          return true;
        }
      }
    }
    return false;
  }

  public DataColumn visit(DataColumn entity)
  {
    if (visit(entity.getScreenResult()) != null) {
      return entity;
    }
    else if (isAllowedAccessToDataColumnDueToMutualPositives(entity)) {
      return entity;
    }
    return null;
  }

  @Override
  public boolean isAllowedAccessToDataColumnDueToMutualPositives(DataColumn entity)
  {
    // allow DataColumn containing mutual positives to be visible, even if the parent screen result is not visible
    // note: currently, we show all positives DataColumns from a given screen to be visible if any *one* of its positives DataColumns 
    // have a mutual positive; we could make this even more strict by restricting the positives DataColumns that have no mutual positives
    if (entity.isPositiveIndicator()) {
      if (visit(entity.getScreenResult()) == null) {
        if (findOthersVisibleScreens().contains(entity.getScreenResult().getScreen())) {
          return findMutualPositiveScreenResultIds().contains(entity.getScreenResult().getEntityId());
        }
      }
    }
    return false;
  }

  public Study visit(Study entity)
  {
    return visit((Screen) entity);
  }

  public Screen visit(Screen screen)
  {
    ScreensaverUser user = getScreensaverUser();
    if (user.getScreensaverUserRoles().contains(ScreensaverUserRole.GRAY_ADMIN)) {
      return findScreensForFundingSupport(GRAY_LIBRARY_SCREEN_FUNDING_SUPPORT_NAME).contains(screen) ? screen : null;
    }
    if (user.getScreensaverUserRoles().contains(ScreensaverUserRole.READ_EVERYTHING_ADMIN) ||
      user.getScreensaverUserRoles().contains(ScreensaverUserRole.SCREENS_ADMIN)) {
      return screen;
    }
    if (findMyScreens().contains(screen)) {
      log.debug("screen " + screen.getFacilityId() + " is visible: \"my screen\"");
      return screen;
    }
    if (findPublicScreens().contains(screen)) {
      log.debug("screen " + screen.getFacilityId() + " is visible: \"public\"");
      return screen;
    }
    if (findOthersVisibleScreens().contains(screen)) {
      log.debug("screen " + screen.getFacilityId() + " is visible: \"screen shared by others\"");
      return screen;
    }
    return null;
  }

  private Set<Screen> findOthersVisibleScreens()
  {
    if (_othersVisibleScreens == null) {
      _othersVisibleScreens = Sets.newHashSet();
      if (getScreensaverUser().getScreensaverUserRoles().contains(ScreensaverUserRole.SM_DSL_LEVEL2_MUTUAL_POSITIVES)) { // note: this implies level 1 users too! 
        if (userHasQualifiedDepositedData(ScreenType.SMALL_MOLECULE)) {
          _othersVisibleScreens.addAll(findOthersLevel1AndLevel2Screens(ScreenType.SMALL_MOLECULE));
        }
      }
      if (getScreensaverUser().getScreensaverUserRoles().contains(ScreensaverUserRole.RNAI_DSL_LEVEL2_MUTUAL_POSITIVES)) { // note: this implies level 1 users too! 
        if (userHasQualifiedDepositedData(ScreenType.RNAI)) {
          _othersVisibleScreens.addAll(findOthersLevel1AndLevel2Screens(ScreenType.RNAI));
        }
      }
    }
    return _othersVisibleScreens;
  }

  private boolean userHasQualifiedDepositedData(ScreenType screenType)
  {
    // level 1 users must have at least one level 0 or 1 screen to qualify (i.e., a level 1 user does NOT qualify with only a level 2 screen)
    // level 2 users must have at least one level 0, 1, or 2 screen to qualify
    ScreenDataSharingLevel maxQualifyingScreenDataSharingLevel = ScreenDataSharingLevel.MUTUAL_POSITIVES;
    if (getScreensaverUser().getScreensaverUserRoles().contains(DataSharingLevelMapper.getUserDslRoleForScreenTypeAndLevel(screenType, 1))) {
      maxQualifyingScreenDataSharingLevel = ScreenDataSharingLevel.MUTUAL_SCREENS;
    }
    for (Screen myScreen : findMyScreens()) {
      if (myScreen.getScreenType() == screenType) {
        if (myScreen.getDataSharingLevel().compareTo(maxQualifyingScreenDataSharingLevel) <= 0) {
          if (myScreen.getScreenResult() != null) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private Set<String> findMutualPositiveWellIds()
  {
    if (_mutualPositiveWellIds == null) {
      _mutualPositiveWellIds = Sets.newHashSet();
      if (getScreensaverUser().getScreensaverUserRoles().contains(ScreensaverUserRole.SM_DSL_LEVEL2_MUTUAL_POSITIVES)) {
        _mutualPositiveWellIds.addAll(findMutualPositiveWellIds(ScreenType.SMALL_MOLECULE));
      }
      if (getScreensaverUser().getScreensaverUserRoles().contains(ScreensaverUserRole.RNAI_DSL_LEVEL2_MUTUAL_POSITIVES)) {
        _mutualPositiveWellIds.addAll(findMutualPositiveWellIds(ScreenType.RNAI));
      }
    }
    return _mutualPositiveWellIds;
  }

  @SuppressWarnings("unchecked")
  private Set<String> findMutualPositiveWellIds(final ScreenType screenType)
  {
    return Sets.newHashSet(_dao.<String>runQuery(new Query() {
      public List execute(Session session)
      {
        return
        new HqlBuilder().
          select("lw2", "id").distinctProjectionValues().
          from(AssayWell.class, "aw1").from("aw1", AssayWell.screenResult, "sr1", JoinType.INNER).from("sr1", ScreenResult.screen, "s1", JoinType.INNER).
          from(AssayWell.class, "aw2").from("aw2", AssayWell.screenResult, "sr2", JoinType.INNER).from("sr2", ScreenResult.screen, "s2", JoinType.INNER).from("aw2", AssayWell.libraryWell, "lw2", JoinType.INNER).
          where("aw1", "libraryWell", Operator.EQUAL, "aw2", "libraryWell").
          where("aw1", "positive", Operator.EQUAL, Boolean.TRUE).
          where("aw2", "positive", Operator.EQUAL, Boolean.TRUE).
          where("s1", "screenType", Operator.EQUAL, screenType).
          where("s2", "screenType", Operator.EQUAL, screenType).
          where("s1", Operator.NOT_EQUAL, "s2"). // don't consider my screens as "others" screens
          whereIn("s1", findMyScreens()).
          whereIn("s1", "dataSharingLevel", Sets.newHashSet(ScreenDataSharingLevel.SHARED, ScreenDataSharingLevel.MUTUAL_POSITIVES, ScreenDataSharingLevel.MUTUAL_SCREENS)).
          whereIn("s2", "dataSharingLevel", Sets.newHashSet(ScreenDataSharingLevel.MUTUAL_POSITIVES, ScreenDataSharingLevel.MUTUAL_SCREENS)).
          toQuery(session, true).list();
      }
    }));
  }
  
  private Set<Screen> findMutualPositiveScreens(){
    // NOTE: #148 - Level 2 screeners can see details for mutual positive screens
    if(_mutualPositiveScreens == null){
      Set<Screen> screens = Sets.newHashSet();
      Set<Integer> mprs = findMutualPositiveScreenResultIds();
      Set<Screen> others = findOthersVisibleScreens();
      for( Screen s : others){
        if(s.getScreenResult() != null && mprs.contains(s.getScreenResult().getScreenResultId())){
          screens.add(s);
        }
      }
      _mutualPositiveScreens=screens;
    }
    return _mutualPositiveScreens;
  }

  private Set<Integer> findMutualPositiveScreenResultIds()
  {
    if (_mutualPositiveScreenResultIds == null) {
      _mutualPositiveScreenResultIds = Sets.newHashSet();
      if (getScreensaverUser().getScreensaverUserRoles().contains(ScreensaverUserRole.SM_DSL_LEVEL2_MUTUAL_POSITIVES)) {
        _mutualPositiveScreenResultIds.addAll(findMutualPositiveScreenResultIds(ScreenType.SMALL_MOLECULE));
      }
      if (getScreensaverUser().getScreensaverUserRoles().contains(ScreensaverUserRole.RNAI_DSL_LEVEL2_MUTUAL_POSITIVES)) {
        _mutualPositiveScreenResultIds.addAll(findMutualPositiveScreenResultIds(ScreenType.RNAI));
      }
    }
    return _mutualPositiveScreenResultIds;
  }

  @SuppressWarnings("unchecked")
  private Set<Integer> findMutualPositiveScreenResultIds(final ScreenType screenType)
  {
    return Sets.newHashSet(_dao.<Integer>runQuery(new Query() {
      public List execute(Session session)
      {
        // TODO: can probably optimize by using ANY operator to determine if at least one mutual positive hit occurs in another screen
        return
        new HqlBuilder().
          select("sr2", "id").distinctProjectionValues().
          from(AssayWell.class, "aw1").from("aw1", AssayWell.screenResult, "sr1", JoinType.INNER).from("sr1", ScreenResult.screen, "s1", JoinType.INNER).
          from(AssayWell.class, "aw2").from("aw2", AssayWell.screenResult, "sr2", JoinType.INNER).from("sr2", ScreenResult.screen, "s2", JoinType.INNER).
          where("aw1", "libraryWell", Operator.EQUAL, "aw2", "libraryWell").
          where("aw1", "positive", Operator.EQUAL, Boolean.TRUE).
          where("aw2", "positive", Operator.EQUAL, Boolean.TRUE).
          where("s1", "screenType", Operator.EQUAL, screenType).
          where("s2", "screenType", Operator.EQUAL, screenType).
          where("s1", Operator.NOT_EQUAL, "s2"). // don't consider my screens as "others" screens
          whereIn("s1", findMyScreens()).
          whereIn("s1", "dataSharingLevel", Sets.newHashSet(ScreenDataSharingLevel.SHARED, ScreenDataSharingLevel.MUTUAL_POSITIVES, ScreenDataSharingLevel.MUTUAL_SCREENS)).
          whereIn("s2", "dataSharingLevel", Sets.newHashSet(ScreenDataSharingLevel.MUTUAL_POSITIVES, ScreenDataSharingLevel.MUTUAL_SCREENS)).
          toQuery(session, true).list();
      }
    }));
  }

  private Set<Screen> findOthersLevel1AndLevel2Screens(final ScreenType screenType)
  {
    if (_level1AndLevel2Screens == null) {
      _level1AndLevel2Screens = Maps.newHashMap();
    }
    if (!_level1AndLevel2Screens.containsKey(screenType)) {
      HashSet<Screen> screens = Sets.<Screen>newHashSet();
      _level1AndLevel2Screens.put(screenType, screens);
      screens.addAll(_dao.<Screen>runQuery(new Query() { 
        public List execute(Session session)
        {
          org.hibernate.Query query = session.createQuery("select distinct s from Screen s where s.screenType = :screenType and s.dataSharingLevel in (:dataSharingLevels) and s not in (:myScreens)");
          query.setParameter("screenType", screenType);
          query.setParameterList("dataSharingLevels", Sets.newHashSet(ScreenDataSharingLevel.MUTUAL_POSITIVES, ScreenDataSharingLevel.MUTUAL_SCREENS));
          query.setParameterList("myScreens", findMyScreens());
          return query.list();
        }
      }));
      if (log.isDebugEnabled()) {
        log.debug("others' level 1 and level 2 " + screenType + " screens : " +
                  Joiner.on(", ").join(Iterables.transform(_level1AndLevel2Screens.get(screenType), Screen.ToNameFunction)));
      }
    }
    return _level1AndLevel2Screens.get(screenType);
  }

  private Set<Screen> findMutualScreens()
  {
    if (_mutualScreens == null) {
      _mutualScreens = Sets.newHashSet();
      if (getScreensaverUser().getScreensaverUserRoles().contains(ScreensaverUserRole.SM_DSL_LEVEL1_MUTUAL_SCREENS)) {
        _mutualScreens.addAll(findOthersLevel1AndLevel2Screens(ScreenType.SMALL_MOLECULE));
      }
      if (getScreensaverUser().getScreensaverUserRoles().contains(ScreensaverUserRole.RNAI_DSL_LEVEL1_MUTUAL_SCREENS)) {
        _mutualScreens.addAll(findOthersLevel1AndLevel2Screens(ScreenType.RNAI));
      }
      // filter out the level 2 screens, since we've called findOthersLevel1AndLevel2Screens() for code reuse, even though it returns a superset of screens that we need in this method
      _mutualScreens = Sets.newHashSet(Iterables.filter(_mutualScreens, Predicates.compose(Predicates.equalTo(ScreenDataSharingLevel.MUTUAL_SCREENS), Screen.ToDataSharingLevel)));
      if (log.isDebugEnabled()) {
        log.debug("other's mutually shared screens: " +
                  Joiner.on(", ").join(Iterables.transform(_mutualScreens, Screen.ToNameFunction)));
      }
    }
    return _mutualScreens;
  }

  private Set<Screen> findMyScreens()
  {
    if (_myScreens == null) {
      if (getScreensaverUser() instanceof ScreeningRoomUser) {
        _myScreens = ((ScreeningRoomUser) getScreensaverUser()).getAllAssociatedScreens();
      } 
      else {
        _myScreens = Sets.newHashSet();
      }
    }
    return _myScreens;
  }

  private Set<Screen> findPublicScreens()
  {
    if (_publicScreens == null) {
      _publicScreens = Sets.newHashSet();
      _publicScreens.addAll(_dao.<Screen>runQuery(new Query() { 
        public List execute(Session session)
        {
          Set<ScreenType> screenTypes = Sets.newHashSet();
          if (getScreensaverUser().isUserInRole(ScreensaverUserRole.RNAI_DSL_LEVEL3_SHARED_SCREENS)) {
            screenTypes.add(ScreenType.RNAI);
          }
          if (getScreensaverUser().isUserInRole(ScreensaverUserRole.SM_DSL_LEVEL3_SHARED_SCREENS)) {
            screenTypes.add(ScreenType.SMALL_MOLECULE);
          }
          return new HqlBuilder().
          from(Screen.class, "s").
          where("s", "dataSharingLevel", Operator.EQUAL, ScreenDataSharingLevel.SHARED).
          whereIn("s", "screenType", screenTypes).
          toQuery(session, true).list();
        }
      }));
    }
    return _publicScreens;
  }

  private Set<Screen> findScreensForFundingSupport(final String fundingSupportName)
  {
    if (!!!_screensForFundingSupport.containsKey(fundingSupportName)) {
      _screensForFundingSupport.putAll(fundingSupportName,
                                       _dao.<Screen>runQuery(new Query() { 
                                         public List execute(Session session)
                                         {
                                           return new HqlBuilder().select("s").from(Screen.class, "s").from("s", Screen.fundingSupports, "fs").
                                           where("fs", "value", Operator.EQUAL, fundingSupportName).
                                             toQuery(session, true).list();
                                         }
                                       }));
    }
    return _screensForFundingSupport.get(fundingSupportName);
  }

  public ScreenResult visit(ScreenResult entity)
  {
    return isAllowedAccessToScreenDetails(entity.getScreen()) ? entity : null;
  }

  public ScreeningRoomUser visit(ScreeningRoomUser entity)
  {
    ScreensaverUser loggedInUser = getScreensaverUser();
    if (loggedInUser.getScreensaverUserRoles().contains(ScreensaverUserRole.READ_EVERYTHING_ADMIN)) {
      return entity;
    }
    if (loggedInUser.equals(entity)) {
      return entity;
    }
    if (loggedInUser instanceof ScreeningRoomUser) {
      return ((ScreeningRoomUser) loggedInUser).getAssociatedUsers().contains(entity) ? entity : null;
    }
    return null;
  }

  public LabHead visit(LabHead entity)
  {
    return visit((ScreeningRoomUser) entity) == null ? null : entity;
  }

  public AdministratorUser visit(AdministratorUser entity)
  {
    ScreensaverUser loggedInUser = getScreensaverUser();
    if (loggedInUser.getScreensaverUserRoles().contains(ScreensaverUserRole.READ_EVERYTHING_ADMIN)) {
      return entity;
    }
    if (loggedInUser.equals(entity)) {
      return entity;
    }
    return null;
  }

  public SilencingReagent visit(SilencingReagent entity)
  {
    if (!!!getScreensaverUser().isUserInRole(ScreensaverUserRole.READ_EVERYTHING_ADMIN) && entity.isRestrictedSequence()) {
      SequenceRestrictedSilencingReagent sequenceRestrictedSilencingReagent = new SequenceRestrictedSilencingReagent(entity);
      if (log.isDebugEnabled()) {
        log.debug("returning sequence-restricted silencing reagent: " + sequenceRestrictedSilencingReagent);
      }
      return sequenceRestrictedSilencingReagent;
    }
    return entity;
  }

  public SmallMoleculeReagent visit(SmallMoleculeReagent entity)
  {
    if (!!!getScreensaverUser().isUserInRole(ScreensaverUserRole.READ_EVERYTHING_ADMIN) && entity.isRestrictedStructure()) {
      StructureRestrictedSmallMoleculeReagent structureRestrictedSmallMoleculeReagent = new StructureRestrictedSmallMoleculeReagent(entity);
      if (log.isDebugEnabled()) {
        log.debug("returning structure-restricted small molecule reagent: " + structureRestrictedSmallMoleculeReagent);
      }
      return structureRestrictedSmallMoleculeReagent;
    }
    return entity;
  }

  public SmallMoleculeCherryPickRequest visit(SmallMoleculeCherryPickRequest entity)
  {
    return (SmallMoleculeCherryPickRequest) visit((CherryPickRequest) entity);
  }

  public RNAiCherryPickRequest visit(RNAiCherryPickRequest entity)
  {
    return (RNAiCherryPickRequest) visit((CherryPickRequest) entity);
  }

  public LibraryScreening visit(LibraryScreening entity)
  {
    return (LibraryScreening) visit((LabActivity) entity);
  }

  public CherryPickScreening visit(CherryPickScreening entity)
  {
    return (CherryPickScreening) visit((LabActivity) entity);
  }

  /**
   * @return true if user should be allowed to view the Screen's summary,
   *         publishable protocol, screening summary, and screen result data.
   */
  public boolean isAllowedAccessToScreenDetails(Screen screen)
  // NOTE: #148 - This method allows access to Screen properties and results, everything
  // , therefore, it is not appropriate for level2 screeners viewing mutual
  // positive screens
  {
    if (visit(screen) == null) {
      return false;
    }
    return isReadEverythingAdmin() ||
    findMyScreens().contains(screen) || 
    findPublicScreens().contains(screen) || 
      findMutualScreens().contains(screen) ;
    // Note: if mutual positive screens were to be completely visible to level2 screeners, 
    // then add this clause.
    //      || findMutualPositiveScreens().contains(screen);
  }
  
  public boolean isAllowedAccessToMutualScreenDetails(Screen screen){
    // NOTE: added for #148 - Level 2 screeners can see details for mutual positive screens
    if (visit(screen) == null) {
      return false;
    }
    return isReadEverythingAdmin() ||
    findMyScreens().contains(screen) || 
    findPublicScreens().contains(screen) || 
      findMutualScreens().contains(screen)
      || findMutualPositiveScreens().contains(screen);
  }

  
  /**
   * Determine whether the current user can see the Status Items, Lab
   * Activities, and Cherry Pick Requests tables. These are considered more
   * private than the screen details (see
   * {@link IccblEntityViewPolicy#isAllowedAccessToScreenDetails(Screen)}).
   */
  public boolean isAllowedAccessToScreenActivity(Screen screen)
  {
    if (visit(screen) == null) {
      return false;
    }
    return isReadEverythingAdmin() || 
    findMyScreens().contains(screen);
  }

  private CherryPickRequest visit(CherryPickRequest entity)
  {
    return isAllowedAccessToScreenActivity(entity.getScreen()) ? entity : null;
  }

  private LabActivity visit(LabActivity entity)
  {
    return isAllowedAccessToScreenActivity(entity.getScreen()) ? entity : null;
  }

  private boolean isReadEverythingAdmin()
  {
    return getScreensaverUser().getScreensaverUserRoles().contains(ScreensaverUserRole.READ_EVERYTHING_ADMIN);
  }

  /**
   * Causes access permissions for the user to be recomputed.  Should be called when the user's associations with entities have changed.  
   */
  public void update()
  {
    _screensForFundingSupport = HashMultimap.create();
    _level1AndLevel2Screens = null;
    _mutualPositiveWellIds = null;
    _mutualPositiveScreenResultIds = null;
    _mutualScreens = null;
    _myScreens = null;
    _othersVisibleScreens = null;
    _publicScreens = null;
  }

}
