// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screens;

import org.apache.log4j.Logger;

import edu.harvard.med.iccbl.screensaver.policy.DataSharingLevelMapper;
import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.Criterion.Operator;
import edu.harvard.med.screensaver.model.AttachedFile;
import edu.harvard.med.screensaver.model.AuditedAbstractEntity;
import edu.harvard.med.screensaver.model.activities.Activity;
import edu.harvard.med.screensaver.model.activities.ServiceActivity;
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
import edu.harvard.med.screensaver.ui.activities.ActivitySearchResults;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TableColumn;
import edu.harvard.med.screensaver.ui.arch.view.aspects.UICommand;
import edu.harvard.med.screensaver.ui.cherrypickrequests.CherryPickRequestSearchResults;
import edu.harvard.med.screensaver.ui.libraries.LibraryPlateSearchResults;
import edu.harvard.med.screensaver.ui.libraries.LibrarySearchResults;
import edu.harvard.med.screensaver.ui.libraries.WellSearchResults;
import edu.harvard.med.screensaver.ui.screenresults.ScreenResultImporter;
import edu.harvard.med.screensaver.ui.screenresults.ScreenResultViewer;
import edu.harvard.med.screensaver.ui.screenresults.cellhts2.CellHTS2Runner;
import edu.harvard.med.screensaver.ui.screenresults.heatmaps.HeatMapViewer;

public class ScreenViewer extends StudyViewer<Screen>
{
  private static Logger log = Logger.getLogger(ScreenViewer.class);

  private ScreenResultViewer _screenResultViewer;
  private HeatMapViewer _heatMapViewer;
  private CellHTS2Runner _cellHTS2Runner;
  private ScreenResultImporter _screenResultImporter;
  private ScreenDetailViewer _screenDetailViewer;
  private ActivitySearchResults _activitiesBrowser;
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
                      ActivitySearchResults activitiesBrowser,
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
    _activitiesBrowser = activitiesBrowser;
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
    getDao().needReadOnly(screen, Screen.labHead.to(LabHead.labAffiliation));
    getDao().needReadOnly(screen, Screen.labHead.to(LabHead.labMembers));
    getDao().needReadOnly(screen, Screen.leadScreener);
    getDao().needReadOnly(screen, Screen.collaborators.to(ScreeningRoomUser.LabHead));
    getDao().needReadOnly(screen, Screen.billingItems);
    getDao().needReadOnly(screen, Screen.labActivities.to(Activity.performedBy));
    //    Note: explicit load of serviceActivity screen; however this doesn't solve the need
    // to reload the screeen when displaying the activity in the ActivitySearchResults - sde4
    getDao().needReadOnly(screen, Screen.serviceActivities.to(Activity.performedBy));
    getDao().needReadOnly(screen, Screen.serviceActivities.to(ServiceActivity.servicedUser));
    getDao().needReadOnly(screen, Screen.serviceActivities.to(ServiceActivity.servicedScreen));
    getDao().needReadOnly(screen, Screen.attachedFiles.to(AttachedFile.fileType));
    getDao().needReadOnly(screen, Screen.fundingSupports);
    getDao().needReadOnly(screen, Screen.publications);
    getDao().needReadOnly(screen, Screen.keywords);
    getDao().needReadOnly(screen, Screen.statusItems);
    getDao().needReadOnly(screen, Screen.cherryPickRequests.to(CherryPickRequest.requestedBy));
    //getDao().needReadOnly(screen, Screen.annotationTypes.to(AnnotationType.annotationValues));
    getDao().needReadOnly(screen.getScreenResult(), ScreenResult.dataColumns.to(DataColumn.derivedTypes));
    getDao().needReadOnly(screen.getScreenResult(), ScreenResult.dataColumns.to(DataColumn.typesDerivedFrom));
    // for screen result last data import date
    getDao().needReadOnly(screen, AuditedAbstractEntity.updateActivities);
    getDao().needReadOnly(screen, Screen.pinTransferApprovalActivity.to(Activity.createdBy));
    getDao().needReadOnly(screen, Screen.pinTransferApprovalActivity.to(Activity.performedBy));
  }

  private void warnAdminOnMismatchedDataSharingLevel(Screen screen)
  {
    if (isReadAdmin()) {
      if (screen.getLabHead() != null) {
        ScreenDataSharingLevel screenDslForLabHead = DataSharingLevelMapper.getScreenDataSharingLevelForUser(screen.getScreenType(), screen.getLabHead());
        if (screen.getDataSharingLevel().compareTo(screenDslForLabHead) > 0) {
          showMessage("screens.dataSharingLevelTooRestrictive",
                      screen.getDataSharingLevel().toString(),
                      DataSharingLevelMapper.getPrimaryDataSharingLevelRoleForUser(screen.getScreenType(), screen.getLabHead()));
        }
      }
    }
  }

  @UICommand
  public String browseActivities()
  {
    _activitiesBrowser.searchActivitiesForScreen(getEntity());
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

