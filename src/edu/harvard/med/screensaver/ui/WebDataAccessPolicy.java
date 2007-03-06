// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import edu.harvard.med.screensaver.db.DataAccessPolicy;
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
import edu.harvard.med.screensaver.model.screens.StatusItem;
import edu.harvard.med.screensaver.model.screens.Visit;
import edu.harvard.med.screensaver.model.users.ChecklistItem;
import edu.harvard.med.screensaver.model.users.ChecklistItemType;
import edu.harvard.med.screensaver.model.users.LabAffiliation;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.CurrentScreensaverUser;

import org.apache.log4j.Logger;


// TODO: add a unit test that ensures all AbstractEntity classes have a corresponding method in this class

// TODO: implement billing permissions

// TODO: currently, we're assuming that if a user has a specific admin role,
// more than just readEverythingAdmin, that she also has the readEverythingAdmin
// role. This assumption is not verified in our data model, so we should be more careful

public class WebDataAccessPolicy implements AbstractEntityVisitor, DataAccessPolicy
{
  // static members

  private static Logger log = Logger.getLogger(WebDataAccessPolicy.class);
  
  
  // instance data

  private CurrentScreensaverUser _currentScreensaverUser;
  
  
  // public methods

  public void setCurrentScreensaverUser(CurrentScreensaverUser user)
  {
    _currentScreensaverUser = user;
  }
  
  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.db.DataAccessPolicy#visit(edu.harvard.med.screensaver.model.screens.AbaseTestset)
   */
  public boolean visit(AbaseTestset entity)
  {
    return true;
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.db.DataAccessPolicy#visit(edu.harvard.med.screensaver.model.screens.AttachedFile)
   */
  public boolean visit(AttachedFile entity)
  {
    return true;
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.db.DataAccessPolicy#visit(edu.harvard.med.screensaver.model.screens.BillingInformation)
   */
  public boolean visit(BillingInformation entity)
  {
    return true;
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.db.DataAccessPolicy#visit(edu.harvard.med.screensaver.model.screens.BillingItem)
   */
  public boolean visit(BillingItem entity)
  {
    return true;
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.db.DataAccessPolicy#visit(edu.harvard.med.screensaver.model.users.ChecklistItem)
   */
  public boolean visit(ChecklistItem entity)
  {
    return true;
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.db.DataAccessPolicy#visit(edu.harvard.med.screensaver.model.users.ChecklistItemType)
   */
  public boolean visit(ChecklistItemType entity)
  {
    return true;
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.db.DataAccessPolicy#visit(edu.harvard.med.screensaver.model.screens.CherryPick)
   */
  public boolean visit(CherryPick entity)
  {
    return true;
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.db.DataAccessPolicy#visit(edu.harvard.med.screensaver.model.libraries.Compound)
   */
  public boolean visit(Compound entity)
  {
    return true;
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.db.DataAccessPolicy#visit(edu.harvard.med.screensaver.model.libraries.Copy)
   */
  public boolean visit(Copy entity)
  {
    return true;
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.db.DataAccessPolicy#visit(edu.harvard.med.screensaver.model.libraries.CopyAction)
   */
  public boolean visit(CopyAction entity)
  {
    return true;
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.db.DataAccessPolicy#visit(edu.harvard.med.screensaver.model.libraries.CopyInfo)
   */
  public boolean visit(CopyInfo entity)
  {
    return true;
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.db.DataAccessPolicy#visit(edu.harvard.med.screensaver.model.derivatives.Derivative)
   */
  public boolean visit(Derivative entity)
  {
    return true;
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.db.DataAccessPolicy#visit(edu.harvard.med.screensaver.model.derivatives.DerivativeScreenResult)
   */
  public boolean visit(DerivativeScreenResult entity)
  {
    return true;
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.db.DataAccessPolicy#visit(edu.harvard.med.screensaver.model.screens.EquipmentUsed)
   */
  public boolean visit(EquipmentUsed entity)
  {
    return true;
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.db.DataAccessPolicy#visit(edu.harvard.med.screensaver.model.libraries.Gene)
   */
  public boolean visit(Gene entity)
  {
    return true;
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.db.DataAccessPolicy#visit(edu.harvard.med.screensaver.model.users.LabAffiliation)
   */
  public boolean visit(LabAffiliation entity)
  {
    return true;
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.db.DataAccessPolicy#visit(edu.harvard.med.screensaver.model.screens.LetterOfSupport)
   */
  public boolean visit(LetterOfSupport entity)
  {
    return true;
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.db.DataAccessPolicy#visit(edu.harvard.med.screensaver.model.libraries.Library)
   */
  public boolean visit(Library entity)
  {
    return true;
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.db.DataAccessPolicy#visit(edu.harvard.med.screensaver.model.screens.PlatesUsed)
   */
  public boolean visit(PlatesUsed entity)
  {
    return true;
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.db.DataAccessPolicy#visit(edu.harvard.med.screensaver.model.screens.Publication)
   */
  public boolean visit(Publication entity)
  {

    return true;
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.db.DataAccessPolicy#visit(edu.harvard.med.screensaver.model.screenresults.ResultValue)
   */
  public boolean visit(ResultValue entity)
  {
    return true;
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.db.DataAccessPolicy#visit(edu.harvard.med.screensaver.model.screenresults.ResultValueType)
   */
  public boolean visit(ResultValueType entity)
  {
    return true;
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.db.DataAccessPolicy#visit(edu.harvard.med.screensaver.model.screens.RNAiKnockdownConfirmation)
   */
  public boolean visit(RNAiKnockdownConfirmation entity)
  {
    return true;
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.db.DataAccessPolicy#visit(edu.harvard.med.screensaver.model.screens.Screen)
   */
  public boolean visit(Screen screen)
  {
    ScreensaverUser user = _currentScreensaverUser.getScreensaverUser();
    if (user == null) {
      // non-web context, allow all permissions
      return true;
    }
    if (user.getScreensaverUserRoles().contains(ScreensaverUserRole.READ_EVERYTHING_ADMIN)) {
      return true;
    }
    // TODO: temporary data access policy for beta, until Harvard legal dept
    // (OTL) approves pubilc visibility of screens (as of 2007-02-08)
    if (user instanceof ScreeningRoomUser) {
      ScreeningRoomUser screener = (ScreeningRoomUser) user;
      return screener.getScreensLed().contains(screen) ||
      screener.getScreensHeaded().contains(screen) ||
      screener.getScreensCollaborated().contains(screen);
    }
//    if (screen.getScreenType().equals(ScreenType.SMALL_MOLECULE) && 
//      user.getScreensaverUserRoles().contains(ScreensaverUserRole.COMPOUND_SCREENING_ROOM_USER)) {
//      return true;
//    }
//    if (screen.getScreenType().equals(ScreenType.RNAI) && 
//      user.getScreensaverUserRoles().contains(ScreensaverUserRole.RNAI_SCREENING_ROOM_USER)) {
//      return true;
//    }
    return false;
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.db.DataAccessPolicy#visit(edu.harvard.med.screensaver.model.screenresults.ScreenResult)
   */
  public boolean visit(ScreenResult screenResult)
  {
    ScreensaverUser user = _currentScreensaverUser.getScreensaverUser();
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
        for (Screen screen : screener.getScreensCollaborated()) {
          if (screen.getScreenResult() != null) {
            return true;
          }
        }
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
      }
    }
    return false;
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.db.DataAccessPolicy#visit(edu.harvard.med.screensaver.model.users.ScreensaverUser)
   */
  public boolean visit(ScreensaverUser screensaverUser)
  {
    ScreensaverUser loggedInUser = _currentScreensaverUser.getScreensaverUser();
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

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.db.DataAccessPolicy#visit(edu.harvard.med.screensaver.model.libraries.SilencingReagent)
   */
  public boolean visit(SilencingReagent entity)
  {
    return true;
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.db.DataAccessPolicy#visit(edu.harvard.med.screensaver.model.screens.StatusItem)
   */
  public boolean visit(StatusItem entity)
  {
    return true;
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.db.DataAccessPolicy#visit(edu.harvard.med.screensaver.model.screens.Visit)
   */
  public boolean visit(Visit visit)
  {
    ScreensaverUser user = _currentScreensaverUser.getScreensaverUser();
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

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.db.DataAccessPolicy#visit(edu.harvard.med.screensaver.model.libraries.Well)
   */
  public boolean visit(Well entity)
  {
    return true;
  }
  
  // private methods

}