// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.policy;

import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.AttachedFile;
import edu.harvard.med.screensaver.model.AttachedFileType;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickAssayPlate;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickLiquidTransfer;
import edu.harvard.med.screensaver.model.cherrypicks.LabCherryPick;
import edu.harvard.med.screensaver.model.cherrypicks.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.RNAiKnockdownConfirmation;
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
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.Publication;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.StatusItem;
import edu.harvard.med.screensaver.model.screens.Study;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ChecklistItem;
import edu.harvard.med.screensaver.model.users.ChecklistItemEvent;
import edu.harvard.med.screensaver.model.users.LabAffiliation;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

/**
 * A {@link EntityViewPolicy} that allows any user to view any entity in the system.
 */
public class DefaultEntityViewPolicy implements EntityViewPolicy
{
  @Override
  public boolean visit(AbaseTestset entity)
  {
    return true;
  }

  @Override
  public boolean visit(AdministrativeActivity administrativeActivity)
  {
    return true;
  }

  @Override
  public boolean visit(AnnotationType annotation)
  {
    return true;
  }

  @Override
  public boolean visit(AnnotationValue annotationValue)
  {
    return true;
  }

  @Override
  public boolean visit(AssayPlate assayPlate)
  {
    return true;
  }

  @Override
  public boolean visit(AssayWell assayWell)
  {
    return true;
  }

  @Override
  public boolean visit(AttachedFile entity)
  {
    return true;
  }

  @Override
  public boolean visit(AttachedFileType entity)
  {
    return true;
  }

  @Override
  public boolean visit(BillingInformation entity)
  {
    return true;
  }

  @Override
  public boolean visit(BillingItem entity)
  {
    return true;
  }

  @Override
  public boolean visit(ChecklistItemEvent entity)
  {
    return true;
  }

  @Override
  public boolean visit(ChecklistItem entity)
  {
    return true;
  }

  @Override
  public boolean visit(LabCherryPick entity)
  {
    return true;
  }

  @Override
  public boolean visit(ScreenerCherryPick entity)
  {
    return true;
  }

  @Override
  public boolean visit(SmallMoleculeReagent entity)
  {
    return true;
  }

  @Override
  public boolean visit(Copy entity)
  {
    return true;
  }

  @Override
  public boolean visit(Plate entity)
  {
    return true;
  }

  @Override
  public boolean visit(PlateLocation entity)
  {
    return true;
  }

  @Override
  public boolean visit(EquipmentUsed entity)
  {
    return true;
  }

  @Override
  public boolean visit(Gene entity)
  {
    return true;
  }

  @Override
  public boolean visit(LabAffiliation entity)
  {
    return true;
  }

  @Override
  public boolean visit(Library entity)
  {
    return true;
  }

  @Override
  public boolean visit(LibraryContentsVersion libraryContentsVersion)
  {
    return true;
  }

  @Override
  public boolean visit(Publication entity)
  {
    return true;
  }

  @Override
  public boolean visit(ResultValue entity)
  {
    return true;
  }

  @Override
  public boolean visit(DataColumn entity)
  {
    return true;
  }

  @Override
  public boolean visit(RNAiKnockdownConfirmation entity)
  {
    return true;
  }

  @Override
  public boolean visit(Screen screen)
  {
    return true;
  }

  @Override
  public boolean visit(ScreenResult screenResult)
  {
    return true;
  }

  @Override
  public boolean visit(SilencingReagent entity)
  {
    return true;
  }

  @Override
  public boolean visit(StatusItem entity)
  {
    return true;
  }

  @Override
  public boolean visit(Well entity)
  {
    return true;
  }

  @Override
  public boolean visit(CherryPickAssayPlate entity)
  {
    return true;
  }

  @Override
  public boolean visit(CherryPickLiquidTransfer entity)
  {
    return true;
  }

  @Override
  public boolean visit(SmallMoleculeCherryPickRequest entity)
  {
    return true;
  }

  @Override
  public boolean visit(LibraryScreening entity)
  {
    return true;
  }

  @Override
  public boolean visit(RNAiCherryPickRequest entity)
  {
    return true;
  }

  @Override
  public boolean visit(CherryPickScreening entity)
  {
    return true;
  }

  @Override
  public boolean visit(AdministratorUser administratorUser)
  {
    return true;
  }

  @Override
  public boolean visit(ScreeningRoomUser screeningRoomUser)
  {
    return true;
  }

  @Override
  public boolean visit(WellVolumeCorrectionActivity entity)
  {
    return true;
  }

  @Override
  public boolean visit(Study study)
  {
    return true;
  }

  @Override
  public boolean visit(LabHead labHead)
  {
    return true;
  }

  @Override
  public boolean visit(NaturalProductReagent entity)
  {
    return true;
  }

  @Override
  public boolean visit(FundingSupport fundingSupport)
  {
    return true;
  }

  @Override
  public boolean isAllowedAccessToScreenDetails(Screen screen)
  {
    return true;
  }

  @Override
  public boolean isAllowedAccessToScreenActivity(Screen screen)
  {
    return true;
  }

  @Override
  public boolean isAllowedAccessToSilencingReagentSequence(SilencingReagent reagent)
  {
    return true;
  }

  @Override
  public boolean isAllowedAccessToDataColumnDueToMutualPositives(DataColumn dataColumn)
  {
    return true;
  }

  @Override
  public boolean isAllowedAccessToResultValueDueToMutualPositive(boolean isPositive, Screen screen, String wellId)
  {
    return true;
  }
}

