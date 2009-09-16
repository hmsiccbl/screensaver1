// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.accesspolicy;

import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.AttachedFile;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickAssayPlate;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickLiquidTransfer;
import edu.harvard.med.screensaver.model.cherrypicks.SmallMoleculeCherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.LabCherryPick;
import edu.harvard.med.screensaver.model.cherrypicks.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.RNAiKnockdownConfirmation;
import edu.harvard.med.screensaver.model.cherrypicks.ScreenerCherryPick;
import edu.harvard.med.screensaver.model.libraries.LibraryContentsVersion;
import edu.harvard.med.screensaver.model.libraries.NaturalProductReagent;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyAction;
import edu.harvard.med.screensaver.model.libraries.CopyInfo;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
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
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.PlatesUsed;
import edu.harvard.med.screensaver.model.screens.Publication;
import edu.harvard.med.screensaver.model.screens.RNAiCherryPickScreening;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.StatusItem;
import edu.harvard.med.screensaver.model.screens.Study;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ChecklistItem;
import edu.harvard.med.screensaver.model.users.ChecklistItemEvent;
import edu.harvard.med.screensaver.model.users.LabAffiliation;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
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

  public boolean visit(AdministrativeActivity administrativeActivity)
  {
    return true;
  }

  public boolean visit(AnnotationType annotation)
  {
    return true;
  }

  public boolean visit(AnnotationValue annotationValue)
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

  public boolean visit(ChecklistItemEvent entity)
  {
    return true;
  }

  public boolean visit(ChecklistItem entity)
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

  public boolean visit(SmallMoleculeReagent entity)
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

  public boolean visit(Library entity)
  {
    return true;
  }

  public boolean visit(LibraryContentsVersion libraryContentsVersion)
  {
    return false;
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

  public boolean visit(SmallMoleculeCherryPickRequest entity)
  {
    return true;
  }

  public boolean visit(LibraryScreening entity)
  {
    return true;
  }

  public boolean visit(Reagent reagent)
  {
    return true;
  }

  public boolean visit(RNAiCherryPickRequest entity)
  {
    return true;
  }

  public boolean visit(RNAiCherryPickScreening entity)
  {
    return true;
  }

  public boolean visit(AdministratorUser administratorUser)
  {
    return true;
  }

  public boolean visit(ScreeningRoomUser screeningRoomUser)
  {
    return true;
  }

  public boolean visit(WellVolumeCorrectionActivity entity)
  {
    return true;
  }

  public boolean visit(Study study)
  {
    return true;
  }

  public boolean visit(LabHead labHead)
  {
    return false;
  }

  public boolean visit(NaturalProductReagent entity)
  {
    return true;
  }

  public boolean visit(FundingSupport fundingSupport)
  {
    return true;
  }

  public boolean isScreenerAllowedAccessToScreenDetails(Screen screen)
  {
    return true;
  }

  public boolean isAllowedAccessToSilencingReagentSequence(SilencingReagent reagent)
  {
    return true;
  }
}

