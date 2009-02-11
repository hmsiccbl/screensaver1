
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver;

import java.awt.Color;

public interface ScreensaverConstants
{
  public static final String APPLICATION_NAME_PROPERTY = "screensaver.ui.application_name";
  public static final String APPLICATION_VERSION_PROPERTY = "screensaver.ui.version";
  public static final String FEEDBACK_URL_PROPERTY = "screensaver.ui.feedback_url";
  public static final String DATATABLE_USE_COLUMN_REORDER_LIST_WIDGET = "datatable.use_column_reorder_list_widget";
  
  public static final String BUILD_NUMBER_FILE = "/build-number.txt";
  public static Color SCREENSAVER_THEME_COLOR = new Color(0x33, 0x66, 0x99);
  public static Color HEADER_COLOR = new Color(0x0, 0x94, 0xC4);

  // JSF Action Results
  // these values are returned by backing beans' action methods and are used to
  // define navigation rules in faces-config.xml

  public static final String REDISPLAY_PAGE_ACTION_RESULT = null;
  public static final String VIEW_MAIN ="viewMain";
  public static final String VIEW_GOODBYE = "goodbye";
  public static final String VIEW_HELP ="viewHelp";
  public static final String VIEW_NEWS ="viewNews";
  public static final String VIEW_DOWNLOADS ="viewDownloads";
  public static final String FIND_REAGENTS = "findReagents";
  public static final String FIND_WELLS = "findWells";
  public static final String FIND_WELL_VOLUMES = "findWellVolumes";
  public static final String BROWSE_LIBRARIES = "browseLibraries";
  public static final String VIEW_LIBRARY = "viewLibrary";
  public static final String VIEW_REAGENT_SEARCH_RESULTS = "viewReagentSearchResults";
  public static final String VIEW_WELL_SEARCH_RESULTS = "viewWellSearchResults";
  public static final String VIEW_WELL_VOLUME_SEARCH_RESULTS = "viewWellVolumeSearchResults";
  public static final String VIEW_WELL_COPY_VOLUME_SEARCH_RESULTS = "viewWellCopyVolumeSearchResults";
  public static final String VIEW_REAGENT = "viewReagent";
  public static final String VIEW_WELL = "viewWell";
  public static final String VIEW_GENE = "viewGene";
  public static final String VIEW_COMPOUND = "viewCompound";
  public static final String IMPORT_COMPOUND_LIBRARY_CONTENTS = "importCompoundLibraryContents";
  public static final String IMPORT_NATURAL_PRODUCTS_LIBRARY_CONTENTS = "importNaturalProductsLibraryContents";
  public static final String IMPORT_RNAI_LIBRARY_CONTENTS = "importRNAiLibraryContents";
  public static final String VIEW_SCREENING_ROOM_USER_ACTION_RESULT = "viewScreeningRoomUser";
  public static final String SHOW_SEARCH_RESULTS_SUMMARY_ACTION = "showSearchResultsSummary";
  public static final String BROWSE_STUDIES = "browseStudies";
  public static final String VIEW_STUDIES = "viewStudies";
  public static final String VIEW_STUDY = "viewStudy";
  public static final String BROWSE_SCREENS = "browseScreens";
  public static final String BROWSE_MY_SCREENS = "browseScreens";
  public static final String VIEW_SCREEN = "viewScreen";
  public static final String VIEW_SCREEN_RESULT_EDITOR = "viewScreenResultEditor";
  public static final String VIEW_SCREEN_DETAIL = "viewScreenDetail";
  public static final String BROWSE_CHERRY_PICK_REQUESTS = "browseCherryPickRequests";
  public static final String VIEW_CHERRY_PICK_REQUEST_ACTION_RESULT = "viewCherryPickRequest";
  public static final String VIEW_BILLING_INFORMATION_ACTION_RESULT = "viewBillingInformation";
  public static final String VIEW_ATTACHED_FILE_ACTION_RESULT = "viewAttachedFile";
  public static final String VIEW_SCREEN_RESULT_IMPORT_ERRORS = "viewScreenResultImportErrors";
  public static final String BROWSE_ACTIVITIES = "browseActivities";
  public static final String VIEW_ACTIVITY = "viewActivity";
  public static final String BROWSE_SCREENERS = "browseScreeners";
  public static final String BROWSE_STAFF = "browseStaff";
  public static final String VIEW_USER = "viewUser";
  public static final String VIEW_LIBRARY_DETAIL = "viewLibraryDetail";
  public static final String RUN_CELLHTS2 = "runCellHTS2";
  public static final String WEBAPP_ROOT = System.getProperty("webapp.root");
  public static final int LIBRARY_UPLOAD_SUCCESSFULL = 2;
  public static final int LIBRARY_UPLOAD_RUNNING = 1;
  public static final int LIBRARY_UPLOAD_FAILED = 3;
  public static final int LIBRARY_UPLOAD_IDLE = -1;
  
}
