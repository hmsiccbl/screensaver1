// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

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
import edu.harvard.med.screensaver.model.screens.EquipmentUsed;
import edu.harvard.med.screensaver.model.screens.LabCherryPick;
import edu.harvard.med.screensaver.model.screens.LetterOfSupport;
import edu.harvard.med.screensaver.model.screens.PlatesUsed;
import edu.harvard.med.screensaver.model.screens.Publication;
import edu.harvard.med.screensaver.model.screens.RNAiKnockdownConfirmation;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenerCherryPick;
import edu.harvard.med.screensaver.model.screens.ScreeningRoomActivity;
import edu.harvard.med.screensaver.model.screens.StatusItem;
import edu.harvard.med.screensaver.model.users.ChecklistItem;
import edu.harvard.med.screensaver.model.users.ChecklistItemType;
import edu.harvard.med.screensaver.model.users.LabAffiliation;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;

import org.apache.log4j.Logger;

public class UnrestrictedDataAccessPolicy implements DataAccessPolicy
{
  // static members

  private static Logger log = Logger.getLogger(UnrestrictedDataAccessPolicy.class);

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

  public boolean visit(LabCherryPick entity)
  {
    return true;
  }

  public boolean visit(ScreenerCherryPick entity)
  {
    return true;
  }

  public boolean visit(CherryPickRequest entity)
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
    return true;
  }

  public boolean visit(ScreeningRoomActivity visit)
  {
    return true;
  }

  public boolean visit(ScreenResult screenResult)
  {
    return true;
  }

  public boolean visit(ScreensaverUser screensaverUser)
  {
    return true;
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

  public boolean visit(CherryPickAssayPlate entity)
  {
    return true;
  }

  public boolean visit(CherryPickLiquidTransfer entity)
  {
    return true;
  }
}

