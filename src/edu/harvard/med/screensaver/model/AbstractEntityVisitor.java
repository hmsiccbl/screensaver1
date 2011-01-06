// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

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
public interface AbstractEntityVisitor
{
  public boolean visit(AbaseTestset entity);
  public boolean visit(AdministratorUser administratorUser);
  public boolean visit(AdministrativeActivity administrativeActivity);
  public boolean visit(AnnotationType annotation);
  public boolean visit(AnnotationValue annotationValue);
  public boolean visit(AssayPlate assayPlate);
  public boolean visit(AssayWell assayWell);
  public boolean visit(AttachedFile entity);
  public boolean visit(AttachedFileType attachedFileType);
  public boolean visit(BillingInformation entity);
  public boolean visit(BillingItem entity);
  public boolean visit(ChecklistItemEvent entity);
  public boolean visit(ChecklistItem entity);
  public boolean visit(CherryPickAssayPlate entity);
  public boolean visit(CherryPickLiquidTransfer entity);
  public boolean visit(SmallMoleculeReagent entity);
  public boolean visit(SmallMoleculeCherryPickRequest entity);
  public boolean visit(Copy entity);
  public boolean visit(Plate entity);
  public boolean visit(PlateLocation entity);
  public boolean visit(EquipmentUsed entity);
  public boolean visit(FundingSupport fundingSupport);
  public boolean visit(Gene entity);
  public boolean visit(LabAffiliation entity);
  public boolean visit(LabCherryPick entity);
  public boolean visit(LabHead labHead);
  public boolean visit(Library entity);
  public boolean visit(LibraryContentsVersion libraryContentsVersion);
  public boolean visit(LibraryScreening entity);
  public boolean visit(NaturalProductReagent entity);
  public boolean visit(Publication entity);
  public boolean visit(ResultValue entity);
  public boolean visit(DataColumn entity);
  public boolean visit(RNAiCherryPickRequest entity);
  public boolean visit(CherryPickScreening entity);
  public boolean visit(RNAiKnockdownConfirmation entity);
  public boolean visit(Screen screen);
  public boolean visit(ScreenResult screenResult);
  public boolean visit(ScreenerCherryPick entity);
  public boolean visit(ScreeningRoomUser screeningRoomUser);
  public boolean visit(SilencingReagent entity);
  public boolean visit(StatusItem entity);
  public boolean visit(Study study);
  public boolean visit(Well entity);
  public boolean visit(WellVolumeCorrectionActivity entity);
}
