// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screens;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.Activity;
import edu.harvard.med.screensaver.model.AttachedFile;
import edu.harvard.med.screensaver.model.AuditedAbstractEntity;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.libraries.LibraryPlate;
import edu.harvard.med.screensaver.model.screenresults.DataColumn;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenDataSharingLevel;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.UICommand;
import edu.harvard.med.screensaver.ui.screenresults.ScreenResultImporter;
import edu.harvard.med.screensaver.ui.screenresults.ScreenResultViewer;
import edu.harvard.med.screensaver.ui.screenresults.cellhts2.CellHTS2Runner;
import edu.harvard.med.screensaver.ui.screenresults.heatmaps.HeatMapViewer;
import edu.harvard.med.screensaver.ui.searchresults.CherryPickRequestSearchResults;
import edu.harvard.med.screensaver.ui.searchresults.LabActivitySearchResults;
import edu.harvard.med.screensaver.ui.searchresults.LibraryPlateSearchResults;
import edu.harvard.med.screensaver.ui.searchresults.LibrarySearchResults;
import edu.harvard.med.screensaver.ui.searchresults.ScreenSearchResults;
import edu.harvard.med.screensaver.ui.searchresults.WellSearchResults;
import edu.harvard.med.screensaver.ui.table.Criterion.Operator;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;

public class ScreenViewer extends StudyViewer<Screen>
{
  private static Logger log = Logger.getLogger(ScreenViewer.class);

  private ScreenResultViewer _screenResultViewer;
  private HeatMapViewer _heatMapViewer;
  private CellHTS2Runner _cellHTS2Runner;
  private ScreenResultImporter _screenResultImporter;
  private ScreenDetailViewer _screenDetailViewer;
  private LabActivitySearchResults _labActivitySearchResults;
  private CherryPickRequestSearchResults _cherryPickRequestSearchResults;
  private LibrarySearchResults _librarySearchResults;
  private LibraryPlateSearchResults _libraryPlateSearchResults;

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
                      CellHTS2Runner cellHTS2Runner,
                      ScreenResultImporter screenResultImporter,
                      LabActivitySearchResults labActivitiesBrowser,
                      CherryPickRequestSearchResults cprsBrowser,
                      LibrarySearchResults librarySearchResults,
                      LibraryPlateSearchResults libraryPlateSearchResults)
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
    _cellHTS2Runner = cellHTS2Runner;
    _screenResultImporter = screenResultImporter;
    _labActivitySearchResults = labActivitiesBrowser;
    _cherryPickRequestSearchResults = cprsBrowser;
    _librarySearchResults = librarySearchResults;
    _libraryPlateSearchResults = libraryPlateSearchResults;
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
    _cellHTS2Runner.setScreenResult(screenResult);

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
    getDao().needReadOnly(screen.getScreenResult(),
                          ScreenResult.dataColumns.to(DataColumn.derivedTypes).getPath());
    getDao().needReadOnly(screen.getScreenResult(),
                          ScreenResult.dataColumns.to(DataColumn.typesDerivedFrom).getPath());
    // for screen result last data import date
    getDao().needReadOnly(screen,
                          AuditedAbstractEntity.updateActivities.getPath());
    getDao().needReadOnly(screen, 
                      Screen.pinTransferApprovalActivity.to(Activity.createdBy).getPath(),
                      Screen.pinTransferApprovalActivity.to(Activity.performedBy).getPath());
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

  @UICommand
  public String browseLabActivities()
  {
    _labActivitySearchResults.searchLabActivitiesForScreen(getEntity());
    return BROWSE_ACTIVITIES;
  }

  @UICommand
  public String browseCherryPickRequests()
  {
    _cherryPickRequestSearchResults.searchForScreen(getEntity());
    return BROWSE_CHERRY_PICK_REQUESTS;
  }

  @UICommand
  public String browseProjectScreens()
  {
    String projectId = getRequestParameter("projectId").toString();
    ((ScreenSearchResults) getContextualSearchResults()).searchScreensForProject(projectId);
    return BROWSE_SCREENS;
  }

  public LibraryPlateSearchResults getPlateSearchResults()
  {
    return _libraryPlateSearchResults;
  }

  @UICommand 
  public String browseLibrariesScreened()
  {
    _librarySearchResults.searchLibrariesScreened(getEntity());
    return BROWSE_LIBRARIES;
  }
    
  @UICommand
  public String browseLibraryPlatesScreened()
  {
    _libraryPlateSearchResults.searchLibraryPlatesScreenedByScreen(getEntity());
    return BROWSE_LIBRARY_PLATES_SCREENED;
  }
    
  @UICommand
  public String browseLibraryPlatesDataLoaded()
  {
    _libraryPlateSearchResults.searchLibraryPlatesScreenedByScreen(getEntity());
    TableColumn<LibraryPlate,Boolean> isDataLoadedCcolumn = 
      (TableColumn<LibraryPlate,Boolean>) _libraryPlateSearchResults.getColumnManager().getColumn("Data Loaded");
    isDataLoadedCcolumn.getCriterion().setOperatorAndValue(Operator.EQUAL, Boolean.TRUE);
    return BROWSE_LIBRARY_PLATES_SCREENED;
  }
    
}

