// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.policy;

import edu.harvard.med.screensaver.model.AttachedFile;
import edu.harvard.med.screensaver.model.AttachedFileType;
import edu.harvard.med.screensaver.model.Entity;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivity;
import edu.harvard.med.screensaver.model.activities.ServiceActivity;
import edu.harvard.med.screensaver.model.cells.Cell;
import edu.harvard.med.screensaver.model.cells.ExperimentalCellInformation;
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
import edu.harvard.med.screensaver.model.screens.CellLine;
import edu.harvard.med.screensaver.model.screens.CherryPickScreening;
import edu.harvard.med.screensaver.model.screens.EquipmentUsed;
import edu.harvard.med.screensaver.model.screens.FundingSupport;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.Publication;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.Study;
import edu.harvard.med.screensaver.model.screens.TransfectionAgent;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ChecklistItem;
import edu.harvard.med.screensaver.model.users.ChecklistItemEvent;
import edu.harvard.med.screensaver.model.users.LabAffiliation;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

/**
 * A {@link EntityViewPolicy} that allows any user to view any entity in the system.
 */
public class DefaultEntityViewPolicy implements EntityViewPolicy<Entity>
{
  @Override
  public Entity visit(AbaseTestset entity)
  {
    return entity;
  }

  @Override
  public Entity visit(AdministrativeActivity entity)
  {
    return entity;
  }

  @Override
  public Entity visit(AnnotationType entity)
  {
    return entity;
  }

  @Override
  public Entity visit(AnnotationValue entity)
  {
    return entity;
  }

  @Override
  public Entity visit(AssayPlate entity)
  {
    return entity;
  }

  @Override
  public Entity visit(AssayWell entity)
  {
    return entity;
  }

  @Override
  public Entity visit(AttachedFile entity)
  {
    return entity;
  }

  @Override
  public Entity visit(AttachedFileType entity)
  {
    return entity;
  }

  @Override
  public Entity visit(ChecklistItemEvent entity)
  {
    return entity;
  }

  @Override
  public Entity visit(ChecklistItem entity)
  {
    return entity;
  }

  @Override
  public Entity visit(LabCherryPick entity)
  {
    return entity;
  }

  @Override
  public Entity visit(ScreenerCherryPick entity)
  {
    return entity;
  }

  @Override
  public Entity visit(SmallMoleculeReagent entity)
  {
    return entity;
  }

  @Override
  public Entity visit(Copy entity)
  {
    return entity;
  }

  @Override
  public Entity visit(Plate entity)
  {
    return entity;
  }

  @Override
  public Entity visit(PlateLocation entity)
  {
    return entity;
  }

  @Override
  public Entity visit(EquipmentUsed entity)
  {
    return entity;
  }

  @Override
  public Entity visit(Gene entity)
  {
    return entity;
  }

  @Override
  public Entity visit(LabAffiliation entity)
  {
    return entity;
  }

  @Override
  public Entity visit(Library entity)
  {
    return entity;
  }

  @Override
  public Entity visit(LibraryContentsVersion entity)
  {
    return entity;
  }

  @Override
  public Entity visit(Publication entity)
  {
    return entity;
  }

  @Override
  public Entity visit(ResultValue entity)
  {
    return entity;
  }

  @Override
  public Entity visit(DataColumn entity)
  {
    return entity;
  }

  @Override
  public Entity visit(Screen entity)
  {
    return entity;
  }

  @Override
  public Entity visit(ScreenResult entity)
  {
    return entity;
  }

  @Override
  public Entity visit(SilencingReagent entity)
  {
    return entity;
  }

  @Override
  public Entity visit(Well entity)
  {
    return entity;
  }

  @Override
  public Entity visit(CherryPickAssayPlate entity)
  {
    return entity;
  }

  @Override
  public Entity visit(CherryPickLiquidTransfer entity)
  {
    return entity;
  }

  @Override
  public Entity visit(SmallMoleculeCherryPickRequest entity)
  {
    return entity;
  }

  @Override
  public Entity visit(LibraryScreening entity)
  {
    return entity;
  }

  @Override
  public Entity visit(RNAiCherryPickRequest entity)
  {
    return entity;
  }

  @Override
  public Entity visit(CherryPickScreening entity)
  {
    return entity;
  }

  @Override
  public Entity visit(AdministratorUser entity)
  {
    return entity;
  }

  @Override
  public Entity visit(ScreeningRoomUser entity)
  {
    return entity;
  }

  @Override
  public Entity visit(WellVolumeCorrectionActivity entity)
  {
    return entity;
  }

  @Override
  public Entity visit(Study entity)
  {
    return entity;
  }

  @Override
  public Entity visit(LabHead entity)
  {
    return entity;
  }

  @Override
  public Entity visit(NaturalProductReagent entity)
  {
    return entity;
  }

  @Override
  public Entity visit(FundingSupport entity)
  {
    return entity;
  }

  @Override
  public Entity visit(ServiceActivity entity)
  {
    return entity;
  }

  @Override
  public Entity visit(CellLine entity)
  {
    return entity;
  }

  @Override
  public Entity visit(TransfectionAgent entity)
  {
     return entity;
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
  public boolean isAllowedAccessToDataColumnDueToMutualPositives(DataColumn dataColumn)
  {
    return true;
  }

  @Override
  public boolean isAllowedAccessToResultValueDueToMutualPositive(boolean isPositive, Screen screen, String wellId)
  {
    return true;
  }

	@Override
	public Entity visit(ExperimentalCellInformation entity) {
		return entity;
	}

	@Override
	public Entity visit(Cell entity) {
		return entity;
	}

}

