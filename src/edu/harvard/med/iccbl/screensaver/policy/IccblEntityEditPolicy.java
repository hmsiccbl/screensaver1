// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.policy;

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
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.policy.EntityEditPolicy;
import edu.harvard.med.screensaver.ui.CurrentScreensaverUser;

public class IccblEntityEditPolicy implements EntityEditPolicy
{
  private CurrentScreensaverUser _currentScreensaverUser;
  private ScreensaverUser _screensaverUser;

  protected IccblEntityEditPolicy() {}

  public IccblEntityEditPolicy(CurrentScreensaverUser user)
  {
    _currentScreensaverUser = user;
  }
  
  /**
   * @motivation for unit tests
   */
  public IccblEntityEditPolicy(ScreensaverUser user)
                                 {
    _screensaverUser = user;
  }
  
  public ScreensaverUser getScreensaverUser() 
  {
    if (_screensaverUser == null) {
      _screensaverUser = _currentScreensaverUser.getScreensaverUser();
    }
    return _screensaverUser;
  }

  @Override
  public boolean visit(AbaseTestset entity)
  {
    return visit(entity.getScreen());
  }

  @Override
  public boolean visit(AdministratorUser administratorUser)
  {
    return false;
  }

  @Override
  public boolean visit(AdministrativeActivity administrativeActivity)
  {
    return getScreensaverUser().isUserInRole(administrativeActivity.getType().getEditableByRole());
  }

  @Override
  public boolean visit(AnnotationType annotation)
  {
    return false;
  }

  @Override
  public boolean visit(AnnotationValue annotationValue)
  {
    return false;
  }

  @Override
  public boolean visit(AssayPlate assayPlate)
  {
    return false;
  }

  @Override
  public boolean visit(AssayWell assayWell)
  {
    return false;
  }

  @Override
  public boolean visit(AttachedFile entity)
  {
    return false;
  }

  @Override
  public boolean visit(AttachedFileType attachedFileType)
  {
    return false;
  }

  @Override
  public boolean visit(BillingInformation entity)
  {
    return getScreensaverUser().isUserInRole(ScreensaverUserRole.BILLING_ADMIN);
  }

  @Override
  public boolean visit(BillingItem entity)
  {
    return getScreensaverUser().isUserInRole(ScreensaverUserRole.BILLING_ADMIN);
  }

  @Override
  public boolean visit(ChecklistItemEvent entity)
  {
    return getScreensaverUser().isUserInRole(ScreensaverUserRole.USER_CHECKLIST_ITEMS_ADMIN);
  }

  @Override
  public boolean visit(ChecklistItem entity)
  {
    return false;
  }

  @Override
  public boolean visit(CherryPickAssayPlate entity)
  {
    return false;
  }

  @Override
  public boolean visit(CherryPickLiquidTransfer entity)
  {
    return getScreensaverUser().isUserInRole(ScreensaverUserRole.CHERRY_PICK_REQUESTS_ADMIN);
  }

  @Override
  public boolean visit(SmallMoleculeReagent entity)
  {
    return false;
  }

  @Override
  public boolean visit(SmallMoleculeCherryPickRequest entity)
  {
    return getScreensaverUser().isUserInRole(ScreensaverUserRole.CHERRY_PICK_REQUESTS_ADMIN);
  }

  @Override
  public boolean visit(Copy entity)
  {
    return getScreensaverUser().isUserInRole(ScreensaverUserRole.LIBRARY_COPIES_ADMIN);
  }

  @Override
  public boolean visit(Plate entity)
  {
    return visit(entity.getCopy());
  }

  @Override
  public boolean visit(PlateLocation entity)
  {
    return false;
  }

  @Override
  public boolean visit(EquipmentUsed entity)
  {
    return false;
  }

  @Override
  public boolean visit(FundingSupport fundingSupport)
  {
    return false;
  }

  @Override
  public boolean visit(Gene entity)
  {
    return false;
  }

  @Override
  public boolean visit(LabAffiliation entity)
  {
    return getScreensaverUser().isUserInRole(ScreensaverUserRole.LAB_HEADS_ADMIN);
  }

  @Override
  public boolean visit(LabCherryPick entity)
  {
    return false;
  }

  @Override
  public boolean visit(LabHead labHead)
  {
    return getScreensaverUser().isUserInRole(ScreensaverUserRole.LAB_HEADS_ADMIN);
  }

  @Override
  public boolean visit(Library entity)
  {
    return getScreensaverUser().isUserInRole(ScreensaverUserRole.LIBRARIES_ADMIN);
  }

  @Override
  public boolean visit(LibraryContentsVersion libraryContentsVersion)
  {
    return visit(libraryContentsVersion.getLibrary());
  }

  @Override
  public boolean visit(LibraryScreening entity)
  {
    return visit(entity.getScreen());
  }

  @Override
  public boolean visit(NaturalProductReagent entity)
  {
    return false;
  }

  @Override
  public boolean visit(Publication entity)
  {
    return false;
  }

  @Override
  public boolean visit(ResultValue entity)
  {
    return false;
  }

  @Override
  public boolean visit(DataColumn entity)
  {
    return visit(entity.getScreenResult());
  }

  @Override
  public boolean visit(RNAiCherryPickRequest entity)
  {
    return getScreensaverUser().isUserInRole(ScreensaverUserRole.CHERRY_PICK_REQUESTS_ADMIN);
  }

  @Override
  public boolean visit(CherryPickScreening entity)
  {
    return visit(entity.getScreen());
  }

  @Override
  public boolean visit(RNAiKnockdownConfirmation entity)
  {
    return false;
  }

  @Override
  public boolean visit(Screen screen)
  {
    return getScreensaverUser().isUserInRole(ScreensaverUserRole.SCREENS_ADMIN);
  }

  @Override
  public boolean visit(ScreenResult screenResult)
  {
    return getScreensaverUser().isUserInRole(ScreensaverUserRole.SCREEN_RESULTS_ADMIN);
  }

  @Override
  public boolean visit(ScreenerCherryPick entity)
  {
    return false;
  }

  @Override
  public boolean visit(ScreeningRoomUser screeningRoomUser)
  {
    return getScreensaverUser().isUserInRole(ScreensaverUserRole.USERS_ADMIN);
  }

  @Override
  public boolean visit(SilencingReagent entity)
  {
    return false;
  }

  @Override
  public boolean visit(StatusItem entity)
  {
    return visit(entity.getScreen());
  }

  @Override
  public boolean visit(Study study)
  {
    return getScreensaverUser().isUserInRole(ScreensaverUserRole.SCREENS_ADMIN);
  }

  @Override
  public boolean visit(Well entity)
  {
    return false;
  }

  @Override
  public boolean visit(WellVolumeCorrectionActivity entity)
  {
    return getScreensaverUser().isUserInRole(ScreensaverUserRole.LIBRARIES_ADMIN);
  }

}
