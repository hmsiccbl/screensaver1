// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.policy;

import edu.harvard.med.screensaver.db.accesspolicy.DataAccessPolicy;
import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.AttachedFile;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickAssayPlate;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickLiquidTransfer;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.LabCherryPick;
import edu.harvard.med.screensaver.model.cherrypicks.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.RNAiKnockdownConfirmation;
import edu.harvard.med.screensaver.model.cherrypicks.ScreenerCherryPick;
import edu.harvard.med.screensaver.model.cherrypicks.SmallMoleculeCherryPickRequest;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyAction;
import edu.harvard.med.screensaver.model.libraries.CopyInfo;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryContentsVersion;
import edu.harvard.med.screensaver.model.libraries.NaturalProductReagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellVolumeCorrectionActivity;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.AnnotationValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.AbaseTestset;
import edu.harvard.med.screensaver.model.screens.BillingInformation;
import edu.harvard.med.screensaver.model.screens.BillingItem;
import edu.harvard.med.screensaver.model.screens.EquipmentUsed;
import edu.harvard.med.screensaver.model.screens.FundingSupport;
import edu.harvard.med.screensaver.model.screens.LabActivity;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.PlatesUsed;
import edu.harvard.med.screensaver.model.screens.Publication;
import edu.harvard.med.screensaver.model.screens.RNAiCherryPickScreening;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.StatusItem;
import edu.harvard.med.screensaver.model.screens.Study;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ChecklistItem;
import edu.harvard.med.screensaver.model.users.ChecklistItemEvent;
import edu.harvard.med.screensaver.model.users.LabAffiliation;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.CurrentScreensaverUser;
import edu.harvard.med.screensaver.ui.WebCurrentScreensaverUser;

import org.apache.log4j.Logger;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * A DataAccessPolicy implementation that is used by the web
 * application.  It is parameterized with a {@link
 * WebCurrentScreensaverUser} when instantiated, allowing a single
 * instance of this class (per web application instance) to determine
 * entity usage authorizations for the current web user.
 */
public class WebDataAccessPolicy implements DataAccessPolicy
{
  private static final String CHEMDIV6_LIBRARY_NAME = "ChemDiv6";
  private static final String MARCUS_LIBRARY_PREFIX = "Marcus";
  public static final String MARCUS_LIBRARY_SCREEN_FUNDING_SUPPORT_NAME = "Marcus Library Screen";

  private static Logger log = Logger.getLogger(WebDataAccessPolicy.class);

  private CurrentScreensaverUser _currentScreensaverUser;

  
  public WebDataAccessPolicy(CurrentScreensaverUser user)
  {
    _currentScreensaverUser = user;
  }

  public boolean visit(AbaseTestset entity)
  {
    return true;
  }

  public boolean visit(AdministrativeActivity administrativeActivity)
  {
    return _currentScreensaverUser.getScreensaverUser().getScreensaverUserRoles().contains(ScreensaverUserRole.READ_EVERYTHING_ADMIN);
  }

  public boolean visit(AnnotationType annotation)
  {
    return visit((Study) annotation.getStudy());
  }

  public boolean visit(AnnotationValue annotationValue)
  {
    return visit(annotationValue.getAnnotationType());
  }

  public boolean visit(AttachedFile entity)
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
    if (entity.getWell().getLibrary().getLibraryName().equals(CHEMDIV6_LIBRARY_NAME)) {
      return isReadEverythingAdmin();
    }
    return true;
  }

  public boolean visit(NaturalProductReagent entity)
  {
    if (entity.getWell().getLibrary().getLibraryName().startsWith(MARCUS_LIBRARY_PREFIX)) {
      return isReadEverythingAdmin() || 
      _currentScreensaverUser.getScreensaverUser().getScreensaverUserRoles().contains(ScreensaverUserRole.MARCUS_ADMIN);
    }
    return true;
  }

  public boolean visit(Copy entity)
  {
    return true;
  }

  public boolean visit(CopyAction entity)
  {
    return true;
  }

  public boolean visit(CopyInfo entity)
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
    ScreensaverUser user = _currentScreensaverUser.getScreensaverUser();
   
    // Equals is based on EntityId if present, otherwise by instance equality
    //In this example case Entity id is empty, so comparison is based on instance
    //I assume that normally this field is not empty
    
    //if owner == null : not a validation library 
    //TODO add || isLabheadLibraryOwner(owner) , however this gives currently "No session" error.
    if ((owner == null) || owner.equals(user)  || user.getScreensaverUserRoles().contains(ScreensaverUserRole.LIBRARIES_ADMIN)) {
      return true;
    }else {
       return false;
    }
  }
    
  public boolean visit(LibraryContentsVersion libraryContentsVersion)
  {
    return true;
  }

  public boolean visit(PlatesUsed entity)
  {
    return true;
  }

  public boolean visit(Publication entity)
  {

    return true;
  }

  public boolean visit(ResultValue entity)
  {
    return true;
  }

  public boolean visit(ResultValueType entity)
  {
    return visit(entity.getScreenResult());
  }

  public boolean visit(RNAiKnockdownConfirmation entity)
  {
    return true;
  }

  public boolean visit(Study study)
  {
    if (study.isShareable()) {
      return true;
    }
    return isReadEverythingAdmin() || isScreenerAssociatedWithScreen((Screen) study);
  }

  public boolean visit(Screen screen)
  {
    ScreensaverUser user = _currentScreensaverUser.getScreensaverUser();
    if (user.getScreensaverUserRoles().contains(ScreensaverUserRole.MARCUS_ADMIN)) {
      if (_currentScreensaverUser instanceof WebCurrentScreensaverUser) {
        // TODO: HACK: Screen.fundingSupports is not always initialized in a web
        // context, so we need to explicitly load from the database
        WebCurrentScreensaverUser webUser = (WebCurrentScreensaverUser) _currentScreensaverUser;
        screen = webUser.getDao().reloadEntity(screen, true, Screen.fundingSupports.getPath());
      }
      // note: marcusAdmins are NOT allowed access to non-Marcus screens, even though they are also readEverythingAdmins
      return Iterables.any(screen.getFundingSupports(), 
                           new Predicate<FundingSupport>() { 
        public boolean apply(FundingSupport fs) { return fs.toString().equals(MARCUS_LIBRARY_SCREEN_FUNDING_SUPPORT_NAME); } });
    }
    if (user.getScreensaverUserRoles().contains(ScreensaverUserRole.READ_EVERYTHING_ADMIN) ||
      user.getScreensaverUserRoles().contains(ScreensaverUserRole.SCREENS_ADMIN)) {
      return true;
    }
    if (screen.getScreenType().equals(ScreenType.SMALL_MOLECULE) &&
      user.getScreensaverUserRoles().contains(ScreensaverUserRole.SMALL_MOLECULE_SCREENER)) {
      return true;
    }
    if (screen.getScreenType().equals(ScreenType.RNAI) &&
      user.getScreensaverUserRoles().contains(ScreensaverUserRole.RNAI_SCREENER)) {
      return true;
    }
    return false;
  }

  public boolean visit(ScreenResult screenResult)
  {
    ScreensaverUser user = _currentScreensaverUser.getScreensaverUser();
    // if user is restricted from parent Screen, they are restricted from the Screen Result
    if (screenResult.getScreen().isRestricted()) {
      return false;
    }
    if (user.getScreensaverUserRoles().contains(ScreensaverUserRole.READ_EVERYTHING_ADMIN) ||
      user.getScreensaverUserRoles().contains(ScreensaverUserRole.SCREEN_RESULTS_ADMIN)) {
      return true;
    }
    if (user instanceof ScreeningRoomUser) {
      ScreeningRoomUser screener = (ScreeningRoomUser) user;
      if (isScreenerAllowedAccessToScreenDetails(screenResult.getScreen())) {
        return true;
      }
      if (screenResult.isShareable() && screenerHasScreenResult(screener)) {
        return true;
      }
    }
    return false;
  }

  public boolean visit(ScreeningRoomUser screeningRoomUser)
  {
    ScreensaverUser loggedInUser = _currentScreensaverUser.getScreensaverUser();
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
    ScreensaverUser loggedInUser = _currentScreensaverUser.getScreensaverUser();
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

  public boolean visit(StatusItem entity)
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

  public boolean visit(RNAiCherryPickScreening entity)
  {
    return visit((LabActivity) entity);
  }

  public boolean visit(WellVolumeCorrectionActivity entity)
  {
    return true;
  }

  public boolean isScreenerAllowedAccessToScreenDetails(Screen screen)
  {
    if (isScreenerAssociatedWithScreen(screen) &&
      // not strictly necessary to check screen.restricted w/current policy, but safer in case policy changes
      !screen.isRestricted()) {
      return true;
    }
    return false;
  }
  
  public boolean isAllowedAccessToSilencingReagentSequence(SilencingReagent reagent)
  {
    if (reagent.isRestricted()) {
      return false;
    }
    return isReadEverythingAdmin();
  }

  private boolean visit(CherryPickRequest entity) {
    // if user is restricted from parent Screen, they are restricted from the CherryPickRequest
    if (entity.getScreen().isRestricted()) {
      return false;
    }
    return isReadEverythingAdmin() || isScreenerAllowedAccessToScreenDetails(entity.getScreen());
  }

  private boolean visit(LabActivity entity)
  {
    // if user is restricted from parent Screen, they are restricted from the CherryPickRequest
    if (entity.getScreen().isRestricted()) {
      return false;
    }
    return isReadEverythingAdmin() || isScreenerAllowedAccessToScreenDetails(entity.getScreen());
  }

  private boolean isReadEverythingAdmin()
  {
    return _currentScreensaverUser.getScreensaverUser().getScreensaverUserRoles().contains(ScreensaverUserRole.READ_EVERYTHING_ADMIN);
  }

  private boolean isScreenerAssociatedWithScreen(Screen screen)
  {
    ScreensaverUser user = _currentScreensaverUser.getScreensaverUser();
    if (user instanceof ScreeningRoomUser) {
      ScreeningRoomUser screener = (ScreeningRoomUser) user;
      return screener.getAllAssociatedScreens().contains(screen);
    }
    return false;
  }

  private boolean screenerHasScreenResult(ScreeningRoomUser screener) {
    for (Screen screen : screener.getScreensLed()) {
      if (screen.getScreenResult() != null) {
        return true;
      }
    }
    if (screener instanceof LabHead) {
      for (Screen screen : ((LabHead) screener).getScreensHeaded()) {
        if (screen.getScreenResult() != null) {
          return true;
        }
      }
    }
    for (Screen screen : screener.getScreensCollaborated()) {
      if (screen.getScreenResult() != null) {
        return true;
      }
    }
    return false;
  }

  

}
