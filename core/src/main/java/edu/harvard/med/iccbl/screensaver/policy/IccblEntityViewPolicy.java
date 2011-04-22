// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.policy;

import java.util.List;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.Query;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.db.hqlbuilder.JoinType;
import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.AttachedFile;
import edu.harvard.med.screensaver.model.AttachedFileType;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickAssayPlate;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickLiquidTransfer;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.LabCherryPick;
import edu.harvard.med.screensaver.model.cherrypicks.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.ScreenerCherryPick;
import edu.harvard.med.screensaver.model.cherrypicks.SmallMoleculeCherryPickRequest;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryContentsVersion;
import edu.harvard.med.screensaver.model.libraries.NaturalProductReagent;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.libraries.PlateLocation;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellVolumeCorrectionActivity;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.AnnotationValue;
import edu.harvard.med.screensaver.model.screenresults.AssayPlate;
import edu.harvard.med.screensaver.model.screenresults.AssayWell;
import edu.harvard.med.screensaver.model.screenresults.DataColumn;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.AbaseTestset;
import edu.harvard.med.screensaver.model.screens.BillingInformation;
import edu.harvard.med.screensaver.model.screens.BillingItem;
import edu.harvard.med.screensaver.model.screens.CherryPickScreening;
import edu.harvard.med.screensaver.model.screens.EquipmentUsed;
import edu.harvard.med.screensaver.model.screens.FundingSupport;
import edu.harvard.med.screensaver.model.screens.LabActivity;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.Publication;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenDataSharingLevel;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.Study;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ChecklistItem;
import edu.harvard.med.screensaver.model.users.ChecklistItemEvent;
import edu.harvard.med.screensaver.model.users.LabAffiliation;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.policy.EntityViewPolicy;
import edu.harvard.med.screensaver.ui.CurrentScreensaverUser;
import edu.harvard.med.screensaver.ui.arch.datatable.Criterion.Operator;

/**
 * An EntityViewPolicy implementation for ICCB-Longwood that is used by the production web application. 
 */
public class IccblEntityViewPolicy implements EntityViewPolicy
{
  public static final String MARCUS_LIBRARY_SCREEN_FUNDING_SUPPORT_NAME = "Marcus Library Screen";
  public static final String GRAY_LIBRARY_SCREEN_FUNDING_SUPPORT_NAME = "Gray Library Screen";

  private static Logger log = Logger.getLogger(IccblEntityViewPolicy.class);

  private CurrentScreensaverUser _currentScreensaverUser;
  private ScreensaverUser _screensaverUser;
  private GenericEntityDAO _dao;
  private Set<Screen> _myScreens;
  private Set<Screen> _publicScreens;
  private HashMultimap<String,Screen> _screensForFundingSupport = HashMultimap.create();
  private Set<Screen> _othersVisibleSmallMoleculeScreens;
  private Set<Screen> _smallMoleculeLevel1AndLevel2Screens;
  private Set<Screen> _mutualSmallMoleculeScreens;
  private Set<String> _smallMoleculeMutualPositiveWellIds;
  
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

  public boolean visit(AbaseTestset entity)
  {
    return true;
  }

  public boolean visit(AdministrativeActivity administrativeActivity)
  {
    return getScreensaverUser().getScreensaverUserRoles().contains(ScreensaverUserRole.READ_EVERYTHING_ADMIN);
  }

  public boolean visit(AnnotationType annotation)
  {
    return visit((Study) annotation.getStudy());
  }

  public boolean visit(AnnotationValue annotationValue)
  {
    return visit(annotationValue.getAnnotationType());
  }

  public boolean visit(AssayPlate assayPlate)
  {
    return true;
  }

  public boolean visit(AssayWell assayWell)
  {
    return true;
  }

  public boolean visit(AttachedFile entity)
  {
    return true;
  }

  public boolean visit(AttachedFileType entity)
  {
    return true;
  }

  public boolean visit(BillingInformation entity)
  {
    return true;
  }

  public boolean visit(BillingItem entity)
  {
    return true;
  }

  public boolean visit(ChecklistItemEvent entity)
  {
    return true;
  }

  public boolean visit(ChecklistItem entity)
  {
    return true;
  }

  public boolean visit(ScreenerCherryPick entity)
  {
    return true;
  }

  public boolean visit(LabCherryPick entity)
  {
    return true;
  }

  public boolean visit(CherryPickAssayPlate entity)
  {
    return true;
  }

  public boolean visit(CherryPickLiquidTransfer entity)
  {
    return visit((LabActivity) entity);
  }

  public boolean visit(SmallMoleculeReagent entity)
  {
    return true;
  }

  public boolean visit(NaturalProductReagent entity)
  {
    return true;
  }

  public boolean visit(Copy entity)
  {
    return true;
  }

  public boolean visit(Plate entity)
  {
    return true;
  }

  @Override
  public boolean visit(PlateLocation entity)
  {
    return true;
  }

  public boolean visit(EquipmentUsed entity)
  {
    return true;
  }

  public boolean visit(FundingSupport fundingSupport) 
  {
    return true;
  }

  public boolean visit(Gene entity)
  {
    return true;
  }

  public boolean visit(LabAffiliation entity)
  {
    return true;
  }

  public boolean visit(Library entity)
  {
    ScreeningRoomUser owner = entity.getOwner();
    ScreensaverUser user = getScreensaverUser();
   
    // Equals is based on EntityId if present, otherwise by instance equality
    //In this example case Entity id is empty, so comparison is based on instance
    //I assume that normally this field is not empty
    
    //if owner == null : not a validation library 
    //TODO add || isLabheadLibraryOwner(owner) , however this gives currently "No session" error.
    return owner == null || owner.equals(user) || user.getScreensaverUserRoles().contains(ScreensaverUserRole.LIBRARIES_ADMIN);
  }
    
  public boolean visit(LibraryContentsVersion libraryContentsVersion)
  {
    return true;
  }

  public boolean visit(Publication entity)
  {
    return true;
  }

  public boolean visit(ResultValue entity)
  {
    // exceptions for SM positive RVs, allowing a subset of RVs to be visible, even if screen result is not visible
    if (isAllowedAccessToResultValueDueToMutualPositive(entity.isPositive(),
                                                     entity.getDataColumn().getScreenResult().getScreen(),
                                                     entity.getWell().getWellId())) {
      return true;
    }

    return visit(entity.getDataColumn().getScreenResult());
  }

  @Override
  public boolean isAllowedAccessToResultValueDueToMutualPositive(boolean isPositive, Screen screen, String wellId)
  {
    if (isPositive) {
      if (screen.getScreenType().equals(ScreenType.SMALL_MOLECULE)) {
        if (findOthersVisibleSmallMoleculeScreens().contains(screen)) {
          if (findSmallMoleculeMutualPositiveWellIds().contains(wellId)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public boolean visit(DataColumn entity)
  {
    if (isAllowedAccessToDataColumnDueToMutualPositives(entity)) {
      return true;
    }
    return visit(entity.getScreenResult());
  }

  @Override
  public boolean isAllowedAccessToDataColumnDueToMutualPositives(DataColumn entity)
  {
    // allow DataColumn containing mutual positives to be visible, even if the parent screen result is not visible
    // note: currently, we show all positives DataColumns from a given screen to be visible if any *one* of its positives DataColumns 
    // have a mutual positive; we could make this even more strict by restricting the positives DataColumns that have no mutual positives
    if (!!!visit(entity.getScreenResult())) {
      if (entity.isPositiveIndicator()) {
        Screen othersScreen = entity.getScreenResult().getScreen();
        if (othersScreen.getScreenType().equals(ScreenType.SMALL_MOLECULE)) {
          ScreenDataSharingLevel myScreenDataSharingLevel =
            DataSharingLevelMapper.getScreenDataSharingLevelForUser(ScreenType.SMALL_MOLECULE, getScreensaverUser());
          if (myScreenDataSharingLevel == ScreenDataSharingLevel.MUTUAL_POSITIVES ||
            othersScreen.getDataSharingLevel() == ScreenDataSharingLevel.MUTUAL_POSITIVES) {
            if (findOthersVisibleSmallMoleculeScreens().contains(entity.getScreenResult().getScreen())) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  public boolean visit(Study study)
  {
    return visit((Screen) study);
  }

  public boolean visit(Screen screen)
  {
    ScreensaverUser user = getScreensaverUser();
    if (user.getScreensaverUserRoles().contains(ScreensaverUserRole.MARCUS_ADMIN)) {
      return findScreensForFundingSupport(MARCUS_LIBRARY_SCREEN_FUNDING_SUPPORT_NAME).contains(screen);
    }
    if (user.getScreensaverUserRoles().contains(ScreensaverUserRole.GRAY_ADMIN)) {
      return findScreensForFundingSupport(GRAY_LIBRARY_SCREEN_FUNDING_SUPPORT_NAME).contains(screen);
    }
    if (user.getScreensaverUserRoles().contains(ScreensaverUserRole.READ_EVERYTHING_ADMIN) ||
      user.getScreensaverUserRoles().contains(ScreensaverUserRole.SCREENS_ADMIN)) {
      return true;
    }
    if (findMyScreens().contains(screen)) {
      log.debug("screen " + screen.getFacilityId() + " is visible: \"my screen\"");
      return true;
    }
    if (findPublicScreens().contains(screen)) {
      log.debug("screen " + screen.getFacilityId() + " is visible: \"public\"");
      return true;
    }
    if (screen.getScreenType().equals(ScreenType.SMALL_MOLECULE)) {
      boolean isOthersVisibleScreen = findOthersVisibleSmallMoleculeScreens().contains(screen);
      if (isOthersVisibleScreen) {
        log.debug("screen is " + screen.getFacilityId() + " visible: \"small molecule screen shared by others\"");
      }
      return isOthersVisibleScreen;
    }
    if (screen.getScreenType().equals(ScreenType.RNAI) &&
      user.getScreensaverUserRoles().contains(ScreensaverUserRole.RNAI_SCREENS)) {
      log.debug("screen " + screen.getFacilityId() + " is visible: \"rnai screen for rnai screener\"");
      return true;
    }
    return false;
  }

  private Set<Screen> findOthersVisibleSmallMoleculeScreens()
  {
    if (_othersVisibleSmallMoleculeScreens == null) {
      _othersVisibleSmallMoleculeScreens = Sets.newHashSet();
      if (getScreensaverUser().getScreensaverUserRoles().contains(ScreensaverUserRole.SM_DSL_LEVEL2_MUTUAL_POSITIVES)) { // note: this implies level 1 users too! 
        if (userHasQualifiedDepositedSmallMoleculeData()) {
          _othersVisibleSmallMoleculeScreens.addAll(findAllSmallMoleculeLevel1AndLevel2Screens());
        }
      }
    }
    return _othersVisibleSmallMoleculeScreens;
  }

  private boolean userHasQualifiedDepositedSmallMoleculeData()
  {
    for (Screen myScreen : findMyScreens()) {
      // level 1 users must have at least one level 0 or 1 screen to qualify (i.e., a level 1 user does NOT qualify with only a level 2 screen)
      // level 2 users must have at least one level 0, 1, or 2 screen to qualify
      ScreenDataSharingLevel maxQualifyingScreenDataSharingLevel = ScreenDataSharingLevel.MUTUAL_POSITIVES;
      if (getScreensaverUser().getScreensaverUserRoles().contains(ScreensaverUserRole.SM_DSL_LEVEL1_MUTUAL_SCREENS)) {
        maxQualifyingScreenDataSharingLevel = ScreenDataSharingLevel.MUTUAL_SCREENS;
      }
      if (myScreen.getDataSharingLevel().compareTo(maxQualifyingScreenDataSharingLevel) <= 0) {
        if (myScreen.getScreenResult() != null) {
          return true;
        }
      }
    }
    return false;
  }

  private Set<String> findSmallMoleculeMutualPositiveWellIds()
  {
    if (_smallMoleculeMutualPositiveWellIds == null) {
      _smallMoleculeMutualPositiveWellIds = Sets.newHashSet();
      if (getScreensaverUser().getScreensaverUserRoles().contains(ScreensaverUserRole.SM_DSL_LEVEL2_MUTUAL_POSITIVES)) {
        _smallMoleculeMutualPositiveWellIds.addAll(_dao.<String>runQuery(new Query() {
          public List execute(Session session)
          {
            // TODO: can probably optimize by using ANY operator to determine if at least one mutual positive hit occurs in another screen
            return
            new HqlBuilder().
              select("lw2", "id").distinctProjectionValues().
              from(AssayWell.class, "aw1").from("aw1", AssayWell.screenResult, "sr1", JoinType.INNER).from("sr1", ScreenResult.screen, "s1", JoinType.INNER).
              from(AssayWell.class, "aw2").from("aw2", AssayWell.screenResult, "sr2", JoinType.INNER).from("sr2", ScreenResult.screen, "s2", JoinType.INNER).from("aw2", AssayWell.libraryWell, "lw2", JoinType.INNER).
              where("aw1", "libraryWell", Operator.EQUAL, "aw2", "libraryWell").
              where("aw1", "positive", Operator.EQUAL, Boolean.TRUE).
              where("aw2", "positive", Operator.EQUAL, Boolean.TRUE).
              where("s1", "screenType", Operator.EQUAL, ScreenType.SMALL_MOLECULE).
              where("s2", "screenType", Operator.EQUAL, ScreenType.SMALL_MOLECULE).
              where("s1", Operator.NOT_EQUAL, "s2"). // don't consider my screens as "others" screens
              whereIn("s1", findMyScreens()).
              whereIn("s1", "dataSharingLevel", Sets.newHashSet(ScreenDataSharingLevel.SHARED, ScreenDataSharingLevel.MUTUAL_POSITIVES, ScreenDataSharingLevel.MUTUAL_SCREENS)).
              whereIn("s2", "dataSharingLevel", Sets.newHashSet(ScreenDataSharingLevel.MUTUAL_POSITIVES, ScreenDataSharingLevel.MUTUAL_SCREENS)).
              toQuery(session, true).list();
          }
        }));
      }
    }
    return _smallMoleculeMutualPositiveWellIds;
  }

  private Set<Screen> findAllSmallMoleculeLevel1AndLevel2Screens()
  {
    if (_smallMoleculeLevel1AndLevel2Screens == null) { 
      _smallMoleculeLevel1AndLevel2Screens = Sets.newHashSet();
      _smallMoleculeLevel1AndLevel2Screens.addAll(_dao.<Screen>runQuery(new Query() { 
        public List execute(Session session)
        {
          org.hibernate.Query query = session.createQuery("select distinct s from Screen s where s.screenType = :screenType and s.dataSharingLevel in (:dataSharingLevels) and s not in (:myScreens)");
          query.setParameter("screenType", ScreenType.SMALL_MOLECULE);
          query.setParameterList("dataSharingLevels", Sets.newHashSet(ScreenDataSharingLevel.MUTUAL_POSITIVES, ScreenDataSharingLevel.MUTUAL_SCREENS));
          query.setParameterList("myScreens", findMyScreens());
          return query.list();
        }
      }));
      if (log.isDebugEnabled()) {
        log.debug("others' level 1 and level 2 small molecule screens : " + 
                  Joiner.on(", ").join(Iterables.transform(_smallMoleculeLevel1AndLevel2Screens, Screen.ToNameFunction)));
      }
    }
    return _smallMoleculeLevel1AndLevel2Screens;
  }

  private Set<Screen> findMutualSmallMoleculeScreens()
  {
    if (_mutualSmallMoleculeScreens == null) { 
      _mutualSmallMoleculeScreens = Sets.newHashSet();
      if (getScreensaverUser().getScreensaverUserRoles().contains(ScreensaverUserRole.SM_DSL_LEVEL1_MUTUAL_SCREENS)) {
        _mutualSmallMoleculeScreens.addAll(_dao.<Screen>runQuery(new Query() { 
          public List execute(Session session)
          {
            return new HqlBuilder().
            from(Screen.class, "s").
            where("s", "dataSharingLevel", Operator.EQUAL, ScreenDataSharingLevel.MUTUAL_SCREENS).
            where("s", "screenType", Operator.EQUAL, ScreenType.SMALL_MOLECULE).toQuery(session, true).list();
          }
        }));
      }
      if (log.isDebugEnabled()) {
        log.debug("other's mutually shared small molecule screens: " + 
                  Joiner.on(", ").join(Iterables.transform(_mutualSmallMoleculeScreens, Screen.ToNameFunction)));
      }
    }
    return _mutualSmallMoleculeScreens;
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
          if (getScreensaverUser().isUserInRole(ScreensaverUserRole.RNAI_SCREENS)) {
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

  public boolean visit(ScreenResult screenResult)
  {
    return isAllowedAccessToScreenDetails(screenResult.getScreen());
  }

  public boolean visit(ScreeningRoomUser screeningRoomUser)
  {
    ScreensaverUser loggedInUser = getScreensaverUser();
    if (loggedInUser.getScreensaverUserRoles().contains(ScreensaverUserRole.READ_EVERYTHING_ADMIN)) {
      return true;
    }
    if (loggedInUser.equals(screeningRoomUser)) {
      return true;
    }
    if (loggedInUser instanceof ScreeningRoomUser) {
      return ((ScreeningRoomUser) loggedInUser).getAssociatedUsers().contains(screeningRoomUser);
    }
    return false;
  }

  public boolean visit(LabHead labHead)
  {
    return visit((ScreeningRoomUser) labHead);
  }

  public boolean visit(AdministratorUser administratorUser)
  {
    ScreensaverUser loggedInUser = getScreensaverUser();
    if (loggedInUser.getScreensaverUserRoles().contains(ScreensaverUserRole.READ_EVERYTHING_ADMIN)) {
      return true;
    }
    if (loggedInUser.equals(administratorUser)) {
      return true;
    }
    return false;
  }

  public boolean visit(SilencingReagent entity)
  {
    return true;
  }

  public boolean visit(Well entity)
  {
    return true;
  }

  public boolean visit(SmallMoleculeCherryPickRequest entity)
  {
    return visit((CherryPickRequest) entity);
  }

  public boolean visit(RNAiCherryPickRequest entity)
  {
    return visit((CherryPickRequest) entity);
  }

  public boolean visit(LibraryScreening entity)
  {
    return visit((LabActivity) entity);
  }

  public boolean visit(CherryPickScreening entity)
  {
    return visit((LabActivity) entity);
  }

  public boolean visit(WellVolumeCorrectionActivity entity)
  {
    return true;
  }

  /**
   * @return true if user should be allowed to view the Screen's summary,
   *         publishable protocol, screening summary, and screen result data.
   */
  public boolean isAllowedAccessToScreenDetails(Screen screen)
  {
    if (!!!visit(screen)) {
      return false;
    }
    return isReadEverythingAdmin() ||
    findMyScreens().contains(screen) || 
    findPublicScreens().contains(screen) || 
    findMutualSmallMoleculeScreens().contains(screen);
  }
  
  /**
   * Determine whether the current user can see the Status Items, Lab
   * Activities, and Cherry Pick Requests tables. These are considered more
   * private than the screen details (see
   * {@link #isAllowedAccessToScreenDetails()}).
   */
  public boolean isAllowedAccessToScreenActivity(Screen screen)
  {
    if (!!!visit(screen)) {
      return false;
    }
    return isReadEverythingAdmin() || 
    findMyScreens().contains(screen);
  }

  public boolean isAllowedAccessToSilencingReagentSequence(SilencingReagent reagent)
  {
    if (!!!visit(reagent)) {
      return false;
    }
    return isReadEverythingAdmin();
  }

  private boolean visit(CherryPickRequest entity) {
    return isAllowedAccessToScreenActivity(entity.getScreen());
  }

  private boolean visit(LabActivity entity)
  {
    return isAllowedAccessToScreenActivity(entity.getScreen());
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
    _smallMoleculeLevel1AndLevel2Screens = null;
    _smallMoleculeMutualPositiveWellIds = null;
    _mutualSmallMoleculeScreens = null;
    _myScreens = null;
    _othersVisibleSmallMoleculeScreens = null;
    _publicScreens = null;
  }

}
