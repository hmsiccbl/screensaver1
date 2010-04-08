// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screens;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.Activity;
import edu.harvard.med.screensaver.model.AttachedFile;
import edu.harvard.med.screensaver.model.AuditedAbstractEntity;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenDataSharingLevel;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.screenresults.ScreenResultImporter;
import edu.harvard.med.screensaver.ui.screenresults.ScreenResultViewer;
import edu.harvard.med.screensaver.ui.screenresults.heatmaps.HeatMapViewer;
import edu.harvard.med.screensaver.ui.searchresults.ScreenSearchResults;
import edu.harvard.med.screensaver.ui.searchresults.WellSearchResults;

import org.apache.log4j.Logger;

public class ScreenViewer extends StudyViewer<Screen>
{
  // static members

  private static Logger log = Logger.getLogger(ScreenViewer.class);


  // instance data members

  private ScreenResultViewer _screenResultViewer;
  private HeatMapViewer _heatMapViewer;
  private ScreenResultImporter _screenResultImporter;
  private ScreenDetailViewer _screenDetailViewer;

  
  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected ScreenViewer()
  {
  }

  public ScreenViewer(ScreenViewer thisProxy,
                      ScreenSearchResults screensBrowser,
                      GenericEntityDAO dao,
                      ScreenDetailViewer screenDetailViewer,
                      WellSearchResults wellsBrowser,
                      ScreenResultViewer screenResultViewer,
                      HeatMapViewer heatMapViewer,
                      ScreenResultImporter screenResultImporter)
  {
    super(Screen.class,
          thisProxy,
          screensBrowser,
          ScreensaverConstants.BROWSE_SCREENS,
          ScreensaverConstants.VIEW_SCREEN,
          dao,
          null,
          wellsBrowser);
    _screenDetailViewer = screenDetailViewer;
    _screenResultViewer = screenResultViewer;
    _heatMapViewer = heatMapViewer;
    _screenResultImporter = screenResultImporter;
  }

  public void initializeViewer(Screen screen)
  {
    super.initializeViewer(screen);

    _screenDetailViewer.setEntity(screen);
    ScreenResult screenResult = screen.getScreenResult();
    if (screenResult != null && screenResult.isRestricted()) {
      screenResult = null;
    }
    _screenResultViewer.setEntity(screenResult);
    _heatMapViewer.setScreenResult(screenResult);
    
    warnAdminOnMismatchedDataSharingLevel(screen);
  }
  
  @Override
  protected void initializeEntity(Screen screen)
  {
    super.initializeEntity(screen);
    getDao().needReadOnly(screen,
                          Screen.labHead.to(LabHead.labAffiliation).getPath(),
                          Screen.labHead.to(LabHead.labMembers).getPath(),
                          Screen.leadScreener.getPath());
    getDao().needReadOnly(screen, Screen.collaborators.to(ScreeningRoomUser.LabHead).getPath());
    getDao().needReadOnly(screen, Screen.billingItems.getPath());
    getDao().needReadOnly(screen, Screen.labActivities.to(Activity.performedBy).getPath());
    getDao().needReadOnly(screen, Screen.attachedFiles.to(AttachedFile.fileType).getPath());
    getDao().needReadOnly(screen, Screen.fundingSupports.getPath());
    getDao().needReadOnly(screen, Screen.publications.getPath());
    getDao().needReadOnly(screen, Screen.keywords.getPath());
    getDao().needReadOnly(screen, Screen.statusItems.getPath());
    getDao().needReadOnly(screen, Screen.cherryPickRequests.to(CherryPickRequest.requestedBy).getPath());
    getDao().needReadOnly(screen, "annotationTypes.annotationValues");
    getDao().needReadOnly(screen.getScreenResult(), "plateNumbers");
    getDao().needReadOnly(screen.getScreenResult(),
                      "dataColumns.derivedTypes",
                      "dataColumns.typesDerivedFrom");
    getDao().needReadOnly(screen, 
                      Screen.pinTransferApprovalActivity.to(Activity.createdBy).getPath(),
                      Screen.pinTransferApprovalActivity.to(Activity.performedBy).getPath());
    getDao().needReadOnly(screen, AuditedAbstractEntity.updateActivities.getPath());    
  }


  // TODO: move logic to edu.harvard.med.iccbl.screensaver.policy
  private void warnAdminOnMismatchedDataSharingLevel(Screen screen)
  {
    if (screen.getScreenType() == ScreenType.SMALL_MOLECULE) {
      if (isReadAdmin()) {
        if (screen.getLabHead() != null) {
          boolean dslTooRestrictive = false;
          ScreensaverUserRole labHeadDsl = null;
          if (screen.getLabHead().getScreensaverUserRoles().contains(ScreensaverUserRole.SM_DSL_LEVEL1_MUTUAL_SCREENS) && screen.getDataSharingLevel().compareTo(ScreenDataSharingLevel.MUTUAL_SCREENS) > 0) {
            labHeadDsl = ScreensaverUserRole.SM_DSL_LEVEL1_MUTUAL_SCREENS;
            dslTooRestrictive = true;
          }
          else if (screen.getLabHead().getScreensaverUserRoles().contains(ScreensaverUserRole.SM_DSL_LEVEL2_MUTUAL_POSITIVES) && screen.getDataSharingLevel().compareTo(ScreenDataSharingLevel.MUTUAL_POSITIVES) > 0) {
            labHeadDsl = ScreensaverUserRole.SM_DSL_LEVEL2_MUTUAL_POSITIVES;
            dslTooRestrictive = true;
          }
          if (dslTooRestrictive) {
            showMessage("screens.dataSharingLevelTooRestrictive", screen.getDataSharingLevel().toString(), labHeadDsl.getDisplayableRoleName());
          }
        }
      }
    }
  }
}

