// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import edu.harvard.med.screensaver.model.activities.AdministrativeActivity;
import edu.harvard.med.screensaver.model.activities.ServiceActivity;
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
 * A visitor interface for the Screensaver domain model entity classes.
 * <p>
 * Note: <code>visit</code> methods should only be added for concrete entity
 * classes (i.e., <i>abstract</i> entity classes should not be visitable). This
 * ensures that each <i>concrete</i> <code>AbstractEntity</code> class that
 * extends a subclass of {@link AbstractEntity} can provide its own
 * {@link AbstractEntity#acceptVisitor(AbstractEntityVisitor)} method
 * implementation.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public interface AbstractEntityVisitor<R>
{
  R visit(AbaseTestset entity);

  R visit(AdministratorUser administratorUser);

  R visit(AdministrativeActivity administrativeActivity);

  R visit(AnnotationType annotation);

  R visit(AnnotationValue annotationValue);

  R visit(AssayPlate assayPlate);

  R visit(AssayWell assayWell);

  R visit(AttachedFile entity);

  R visit(AttachedFileType attachedFileType);

  //  R visit(BillingInformation entity);

  //  R visit(BillingItem entity);

  R visit(ChecklistItemEvent entity);

  R visit(ChecklistItem entity);

  R visit(CherryPickAssayPlate entity);

  R visit(CherryPickLiquidTransfer entity);

  R visit(SmallMoleculeReagent entity);

  R visit(SmallMoleculeCherryPickRequest entity);

  R visit(Copy entity);

  R visit(Plate entity);

  R visit(PlateLocation entity);

  R visit(EquipmentUsed entity);

  R visit(FundingSupport fundingSupport);

  R visit(Gene entity);

  R visit(LabAffiliation entity);

  R visit(LabCherryPick entity);

  R visit(LabHead labHead);

  R visit(Library entity);

  R visit(LibraryContentsVersion libraryContentsVersion);

  R visit(LibraryScreening entity);

  R visit(NaturalProductReagent entity);

  R visit(Publication entity);

  R visit(ResultValue entity);

  R visit(DataColumn entity);

  R visit(RNAiCherryPickRequest entity);

  R visit(CherryPickScreening entity);

  R visit(Screen screen);

  R visit(ScreenResult screenResult);

  R visit(ScreenerCherryPick entity);

  R visit(ScreeningRoomUser screeningRoomUser);

  R visit(ServiceActivity serviceActivity);

  R visit(SilencingReagent entity);

  R visit(Study study);

  R visit(Well entity);

  R visit(WellVolumeCorrectionActivity entity);

  R visit(CellLine cellLine);

  R visit(TransfectionAgent transfectionAgent);
}
