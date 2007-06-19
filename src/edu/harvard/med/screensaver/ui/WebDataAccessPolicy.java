// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import edu.harvard.med.screensaver.db.accesspolicy.DataAccessPolicy;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.derivatives.Derivative;
import edu.harvard.med.screensaver.model.derivatives.DerivativeScreenResult;
import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyAction;
import edu.harvard.med.screensaver.model.libraries.CopyInfo;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.AbaseTestset;
import edu.harvard.med.screensaver.model.screens.AttachedFile;
import edu.harvard.med.screensaver.model.screens.BillingInformation;
import edu.harvard.med.screensaver.model.screens.BillingItem;
import edu.harvard.med.screensaver.model.screens.CherryPickAssayPlate;
import edu.harvard.med.screensaver.model.screens.CherryPickLiquidTransfer;
import edu.harvard.med.screensaver.model.screens.CherryPickRequest;
import edu.harvard.med.screensaver.model.screens.CompoundCherryPickRequest;
import edu.harvard.med.screensaver.model.screens.EquipmentUsed;
import edu.harvard.med.screensaver.model.screens.LabCherryPick;
import edu.harvard.med.screensaver.model.screens.LetterOfSupport;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.PlatesUsed;
import edu.harvard.med.screensaver.model.screens.Publication;
import edu.harvard.med.screensaver.model.screens.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.screens.RNAiCherryPickScreening;
import edu.harvard.med.screensaver.model.screens.RNAiKnockdownConfirmation;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.ScreenerCherryPick;
import edu.harvard.med.screensaver.model.screens.ScreeningRoomActivity;
import edu.harvard.med.screensaver.model.screens.StatusItem;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ChecklistItem;
import edu.harvard.med.screensaver.model.users.ChecklistItemType;
import edu.harvard.med.screensaver.model.users.LabAffiliation;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;

import org.apache.log4j.Logger;

// TODO: implement billing permissions

public class WebDataAccessPolicy implements AbstractEntityVisitor, DataAccessPolicy
{
  // static members

  private static Logger log = Logger.getLogger(WebDataAccessPolicy.class);
  
  
  // instance data

  private CurrentScreensaverUser _currentScreensaverUser;
  
  
  // public methods

  public WebDataAccessPolicy(CurrentScreensaverUser user)
  {
    _currentScreensaverUser = user;
  }
  
  public boolean visit(AbaseTestset entity)
  {
    return true;
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

  public boolean visit(ChecklistItem entity)
  {
    return true;
  }

  public boolean visit(ChecklistItemType entity)
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
    return true;
  }

  public boolean visit(Compound entity)
  {
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

  public boolean visit(Derivative entity)
  {
    return true;
  }

  public boolean visit(DerivativeScreenResult entity)
  {
    return true;
  }

  public boolean visit(EquipmentUsed entity)
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

  public boolean visit(LetterOfSupport entity)
  {
    return true;
  }

  public boolean visit(Library entity)
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
    return true;
  }

  public boolean visit(RNAiKnockdownConfirmation entity)
  {
    return true;
  }

  public boolean visit(Screen screen)
  {
    ScreensaverUser user = _currentScreensaverUser.getScreensaverUser();
    if (user.getScreensaverUserRoles().contains(ScreensaverUserRole.READ_EVERYTHING_ADMIN) ||
      user.getScreensaverUserRoles().contains(ScreensaverUserRole.SCREENS_ADMIN)) {
      return true;
    }
    if (screen.getScreenType().equals(ScreenType.SMALL_MOLECULE) && 
      user.getScreensaverUserRoles().contains(ScreensaverUserRole.COMPOUND_SCREENING_ROOM_USER)) {
      return true;
    }
    if (screen.getScreenType().equals(ScreenType.RNAI) && 
      user.getScreensaverUserRoles().contains(ScreensaverUserRole.RNAI_SCREENING_ROOM_USER)) {
      return true;
    }
    return false;
  }

  public boolean visit(ScreenResult screenResult)
  {
    ScreensaverUser user = _currentScreensaverUser.getScreensaverUser();
    if (user.getScreensaverUserRoles().contains(ScreensaverUserRole.READ_EVERYTHING_ADMIN) ||
      user.getScreensaverUserRoles().contains(ScreensaverUserRole.SCREEN_RESULTS_ADMIN)) {
      return true;
    }
    if (screenResult.getScreen().isRestricted()) {
      // if user is restricted from parent Screen, they are restricted from the Screen Result
      return false;
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
    return visit((ScreensaverUser) screeningRoomUser);
  }
  
  public boolean visit(AdministratorUser administratorUser)
  {
    return visit((ScreensaverUser) administratorUser);
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

  public boolean visit(CompoundCherryPickRequest entity) 
  {
    return visit((CherryPickRequest) entity);
  }

  public boolean visit(RNAiCherryPickRequest entity) 
  {
    return visit((CherryPickRequest) entity);
  }

  public boolean visit(LibraryScreening entity) 
  {
    return visit((ScreeningRoomActivity) entity);
  }

  public boolean visit(RNAiCherryPickScreening entity) 
  {
    return visit((ScreeningRoomActivity) entity);
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

  // private methods
  
  private boolean visit(ScreensaverUser screensaverUser)
  {
    ScreensaverUser loggedInUser = _currentScreensaverUser.getScreensaverUser();
    if (loggedInUser.getScreensaverUserRoles().contains(ScreensaverUserRole.READ_EVERYTHING_ADMIN) ||
      loggedInUser.getScreensaverUserRoles().contains(ScreensaverUserRole.USERS_ADMIN)) {
      return true;
    }
    if (screensaverUser.equals(loggedInUser)) {
      return true;
    }
    if (loggedInUser instanceof ScreeningRoomUser) {
      ScreeningRoomUser loggedInScreener = (ScreeningRoomUser) loggedInUser;
      if (loggedInScreener.isHeadOfLab()) {
        if (loggedInScreener.getLabMembers().contains(screensaverUser)) {
          // lab head can view her lab members
          return true;
        }
      }
      else {
        if (loggedInScreener.getLabHead().equals(screensaverUser)) {
          // non-lab head can view his lab head
          return true;
        }
        if (loggedInScreener.getLabHead().getLabMembers().contains(screensaverUser)) {
          // non-lab head can view his fellow lab members
          return true;
        }
      }
    }
    return false;
  }

  private boolean visit(CherryPickRequest entity) {
    return isReadEverythingAdmin() || isScreenerAllowedAccessToScreenDetails(entity.getScreen());
  }

  private boolean visit(ScreeningRoomActivity entity)
  {
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
      ScreeningRoomUser screeningUser = (ScreeningRoomUser) user;
      return 
      screeningUser.getScreensHeaded().contains(screen) ||
      screeningUser.getScreensLed().contains(screen) ||
      screeningUser.getScreensCollaborated().contains(screen);
    }
    return false;
  }

  private boolean screenerHasScreenResult(ScreeningRoomUser screener) {
    for (Screen screen : screener.getScreensLed()) {
      if (screen.getScreenResult() != null) {
        return true;
      }
    }
    for (Screen screen : screener.getScreensHeaded()) {
      if (screen.getScreenResult() != null) {
        return true;
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
