// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import javax.faces.context.FacesContext;

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
import edu.harvard.med.screensaver.model.screens.CherryPick;
import edu.harvard.med.screensaver.model.screens.EquipmentUsed;
import edu.harvard.med.screensaver.model.screens.LetterOfSupport;
import edu.harvard.med.screensaver.model.screens.PlatesUsed;
import edu.harvard.med.screensaver.model.screens.Publication;
import edu.harvard.med.screensaver.model.screens.RNAiKnockdownConfirmation;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.StatusItem;
import edu.harvard.med.screensaver.model.screens.Visit;
import edu.harvard.med.screensaver.model.users.ChecklistItem;
import edu.harvard.med.screensaver.model.users.ChecklistItemType;
import edu.harvard.med.screensaver.model.users.LabAffiliation;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.Login;

import org.apache.log4j.Logger;


// TODO: add a unit test that ensures all AbstractEntity classes have a corresponding method in this class

// TODO: implement billing permissions

public class DataAccessPolicy implements AbstractEntityVisitor
{
  // static members

  private static Logger log = Logger.getLogger(DataAccessPolicy.class);
  
  
  // instance data

  private ScreensaverUser _screensaverUser;
  
  
  // public methods

  public void setScreensaverUser(ScreensaverUser user)
  {
    _screensaverUser = user;
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

  public boolean visit(CherryPick entity)
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
    ScreensaverUser user = getScreensaverUser();
    if (user == null) {
      // non-web context, allow all permissions
      return true;
    }
    if (user.getScreensaverUserRoles().contains(ScreensaverUserRole.READ_EVERYTHING_ADMIN)) {
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
    ScreensaverUser user = getScreensaverUser();
    if (user == null) {
      // non-web context, allow all permissions
      return true;
    }
    if (user.getScreensaverUserRoles().contains(ScreensaverUserRole.READ_EVERYTHING_ADMIN)) {
      return true;
    }
    if (user instanceof ScreeningRoomUser) {
      ScreeningRoomUser screener = (ScreeningRoomUser) user;
      if (screener.getScreensLed().contains(screenResult.getScreen())) {
        return true;
      }
      if (screener.getScreensCollaborated().contains(screenResult.getScreen())) {
        return true;
      }
      if (!screenResult.isShareable()) {
        return false;
      }
      if (screener.getScreensaverUserRoles().contains(ScreensaverUserRole.COMPOUND_SCREENING_ROOM_USER)) {
        boolean hasDepositedData = false;
        for (Screen screen : screener.getScreensCollaborated()) {
          if (screen.getScreenResult() != null) {
            hasDepositedData = true;
          }
        }
        for (Screen screen : screener.getScreensLed()) {
          if (screen.getScreenResult() != null) {
            hasDepositedData = true;
          }
        }
        for (Screen screen : screener.getScreensHeaded()) {
          if (screen.getScreenResult() != null) {
            hasDepositedData = true;
          }
        }
        if (hasDepositedData) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean visit(ScreensaverUser screensaverUser)
  {
    ScreensaverUser loggedInUser = getScreensaverUser();
    if (loggedInUser == null) {
      // non-web context, allow all permissions
      return true;
    }
    if (loggedInUser.getScreensaverUserRoles().contains(ScreensaverUserRole.READ_EVERYTHING_ADMIN)) {
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

  public boolean visit(SilencingReagent entity)
  {
    return true;
  }

  public boolean visit(StatusItem entity)
  {
    return true;
  }

  public boolean visit(Visit visit)
  {
    ScreensaverUser user = getScreensaverUser();
    if (user == null) {
      // non-web context, allow all permissions
      return true;
    }
    if (user.getScreensaverUserRoles().contains(ScreensaverUserRole.READ_EVERYTHING_ADMIN)) {
      return true;
    }
    if (user instanceof ScreeningRoomUser) {
      ScreeningRoomUser screener = (ScreeningRoomUser) user;
      if (screener.getScreensLed().contains(visit.getScreen())) {
        return true;
      }
      if (screener.getScreensCollaborated().contains(visit.getScreen())) {
        return true;
      }
    }
    return false;
  }

  public boolean visit(Well entity)
  {
    return true;
  }
  

  // protected methods
  
  protected ScreensaverUser getScreensaverUser()
  {
    // handle usage within a web context
    if (_screensaverUser == null) {
      return getCurrentScreensaverUser();
    }
    // handle usage within a testing context
    return _screensaverUser;
  }

  
  // private methods
  
  // TODO: Major HACK alert! We need a better way of getting at the logged-in
  // user, but one that will work outside the JSF framework (from just a vanilla
  // servlet or servlet filter). Some form of injection...but this will require
  // making DataAccessPolicy instances session-scoped.
  private ScreensaverUser getCurrentScreensaverUser()
  {
    FacesContext facesContext = FacesContext.getCurrentInstance();
    if (facesContext == null) {
      log.warn("no current screensaver user, since not executing within a web context; all data access permissions granted");
      return null;
    }
    Login login = (Login) facesContext.getApplication().getVariableResolver().resolveVariable(facesContext, "login");
    ScreensaverUser user = login.getScreensaverUser();
    return user;
  }

}

