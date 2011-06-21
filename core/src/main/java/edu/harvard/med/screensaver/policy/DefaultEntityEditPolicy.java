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
import edu.harvard.med.screensaver.model.screens.CherryPickScreening;
import edu.harvard.med.screensaver.model.screens.EquipmentUsed;
import edu.harvard.med.screensaver.model.screens.FundingSupport;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.Publication;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.Study;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ChecklistItem;
import edu.harvard.med.screensaver.model.users.ChecklistItemEvent;
import edu.harvard.med.screensaver.model.users.LabAffiliation;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

/**
 * A {@link EntityEditPolicy} that allows any user to edit any entity in the system.
 */
public class DefaultEntityEditPolicy implements EntityEditPolicy
{
  @Override
  public Boolean visit(AbaseTestset entity)
  {
    return true;
  }

  @Override
  public Boolean visit(AdministrativeActivity entity)
  {
    return true;
  }

  @Override
  public Boolean visit(AnnotationType entity)
  {
    return true;
  }

  @Override
  public Boolean visit(AnnotationValue entity)
  {
    return true;
  }

  @Override
  public Boolean visit(AssayPlate entity)
  {
    return true;
  }

  @Override
  public Boolean visit(AssayWell entity)
  {
    return true;
  }

  @Override
  public Boolean visit(AttachedFile entity)
  {
    return true;
  }

  @Override
  public Boolean visit(AttachedFileType entity)
  {
    return true;
  }

  //  @Override
  //  public Boolean visit(BillingInformation entity)
  //  {
  //    return true;
  //  }
  //
  //  @Override
  //  public Boolean visit(BillingItem entity)
  //  {
  //    return true;
  //  }

  @Override
  public Boolean visit(ChecklistItemEvent entity)
  {
    return true;
  }

  @Override
  public Boolean visit(ChecklistItem entity)
  {
    return true;
  }

  @Override
  public Boolean visit(LabCherryPick entity)
  {
    return true;
  }

  @Override
  public Boolean visit(ScreenerCherryPick entity)
  {
    return true;
  }

  @Override
  public Boolean visit(SmallMoleculeReagent entity)
  {
    return true;
  }

  @Override
  public Boolean visit(Copy entity)
  {
    return true;
  }

  @Override
  public Boolean visit(Plate entity)
  {
    return true;
  }

  @Override
  public Boolean visit(PlateLocation entity)
  {
    return true;
  }

  @Override
  public Boolean visit(EquipmentUsed entity)
  {
    return true;
  }

  @Override
  public Boolean visit(Gene entity)
  {
    return true;
  }

  @Override
  public Boolean visit(LabAffiliation entity)
  {
    return true;
  }

  @Override
  public Boolean visit(Library entity)
  {
    return true;
  }

  @Override
  public Boolean visit(LibraryContentsVersion entity)
  {
    return true;
  }

  @Override
  public Boolean visit(Publication entity)
  {
    return true;
  }

  @Override
  public Boolean visit(ResultValue entity)
  {
    return true;
  }

  @Override
  public Boolean visit(DataColumn entity)
  {
    return true;
  }

  @Override
  public Boolean visit(Screen entity)
  {
    return true;
  }

  @Override
  public Boolean visit(ScreenResult entity)
  {
    return true;
  }

  @Override
  public Boolean visit(SilencingReagent entity)
  {
    return true;
  }

  @Override
  public Boolean visit(Well entity)
  {
    return true;
  }

  @Override
  public Boolean visit(CherryPickAssayPlate entity)
  {
    return true;
  }

  @Override
  public Boolean visit(CherryPickLiquidTransfer entity)
  {
    return true;
  }

  @Override
  public Boolean visit(SmallMoleculeCherryPickRequest entity)
  {
    return true;
  }

  @Override
  public Boolean visit(LibraryScreening entity)
  {
    return true;
  }

  @Override
  public Boolean visit(RNAiCherryPickRequest entity)
  {
    return true;
  }

  @Override
  public Boolean visit(CherryPickScreening entity)
  {
    return true;
  }

  @Override
  public Boolean visit(AdministratorUser entity)
  {
    return true;
  }

  @Override
  public Boolean visit(ScreeningRoomUser entity)
  {
    return true;
  }

  @Override
  public Boolean visit(WellVolumeCorrectionActivity entity)
  {
    return true;
  }

  @Override
  public Boolean visit(Study entity)
  {
    return true;
  }

  @Override
  public Boolean visit(LabHead entity)
  {
    return true;
  }

  @Override
  public Boolean visit(NaturalProductReagent entity)
  {
    return true;
  }

  @Override
  public Boolean visit(FundingSupport entity)
  {
    return true;
  }
}

