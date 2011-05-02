
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver;

import java.awt.Color;

import edu.harvard.med.screensaver.model.libraries.PlateSize;

public interface ScreensaverConstants
{
  /**
   * The name of the Java system property that specified the location (path and file name) of the screensaver.properties
   * file, which can take on any name. If this property is not defined, the default file location, as provided via the
   * {@link ScreensaverProperties#ScreensaverProperties(String)} constructor, will be used.
   */
  public static final String SCREENSAVER_PROPERTIES_FILE_PROPERTY_NAME = "screensaver.properties.file";
  public static final String VERSION_PROPERTIES_RESOURCE = "/version.properties";
  public static final String BUILD_NUMBER_PROPERTY = "build.number";
  public static final String VERSION_PROPERTY = "version";

  /* Constants for application properties that are found in the screensaver.properties file.  Use these constants to retrieve property values using getApplicationProperties(). */
  public static final String APPLICATION_NAME_PROPERTY = "screensaver.ui.application_name";
  public static final String FEEDBACK_URL_PROPERTY = "screensaver.ui.feedback_url";
  public static final String RELEASE_NOTES_URL = "screensaver.ui.release_notes_url";
  public static final String FACILITY_NAME = "screensaver.ui.facility_name";
  public static final String FACILITY_URL = "screensaver.ui.facility_url";
  public static final String STRUCTURE_IMAGES_BASE_URL = "screensaver.ui.structure_images_base_url";
  /** Prefix for properties that define whether a particular UI feature should be enabled or disabled.  Property value must TRUE or FALSE. */
  public static final String SCREENSAVER_UI_FEATURE_PREFIX = "screensaver.ui.feature.";


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
  public static final String EDIT_LIBRARY = "editLibrary";
  public static final String BROWSE_WELLS = "browseWells";
  public static final String BROWSE_WELL_VOLUMES = "browseWellVolumes";
  public static final String BROWSE_WELL_COPY_VOLUMES = "browseWellCopyVolumes";
  public static final String VIEW_WELL = "viewWell";
  public static final String IMPORT_LIBRARY_CONTENTS = "importLibraryContents";
  public static final String VIEW_SCREENING_ROOM_USER_ACTION_RESULT = "viewScreeningRoomUser";
  public static final String SHOW_SEARCH_RESULTS_SUMMARY_ACTION = "showSearchResultsSummary";
  public static final String BROWSE_STUDIES = "browseStudies";
  public static final String VIEW_STUDY = "viewStudy";
  public static final String BROWSE_SCREENS = "browseScreens";
  public static final String BROWSE_MY_SCREENS = "browseScreens";
  public static final String VIEW_SCREEN = "viewScreen";
  public static final String EDIT_SCREEN = "editScreen";
  public static final String EDIT_SCREEN_RESULT = "editScreenResult";
  public static final String BROWSE_CHERRY_PICK_REQUESTS = "browseCherryPickRequests";
  public static final String VIEW_CHERRY_PICK_REQUEST = "viewCherryPickRequest";
  public static final String EDIT_CHERRY_PICK_REQUEST = "editCherryPickRequest";
  public static final String IMPORT_SCREEN_RESULT_DATA = "importScreenResultData";
  public static final String BROWSE_ACTIVITIES = "browseActivities";
  public static final String VIEW_ACTIVITY = "viewActivity";
  public static final String BROWSE_SCREENERS = "browseScreeners";
  public static final String BROWSE_STAFF = "browseStaff";
  public static final String VIEW_USER = "viewUser";
  public static final String UPDATE_USER_AGREEMENT = "updateUserAgreement";
  public static final String RUN_CELLHTS2 = "runCellHTS2";
  public static final String BROWSE_LIBRARY_PLATES_SCREENED = "browseLibraryPlatesScreened";
  public static final String BROWSE_ENTITY_UPDATE_HISTORY = "browseEntityUpdateHistory";
  public static final String BROWSE_LIBRARY_COPIES = "browseLibraryCopies";
  public static final String EDIT_LIBRARY_COPY = "editLibraryCopy";
  public static final String VIEW_LIBRARY_COPY = "viewLibraryCopy";
  public static final String BROWSE_LIBRARY_COPY_PLATES = "browseLibraryCopyPlates";

  public static final String CELLHTS2_REPORTS_BASE_URL = "/screensaver/cellHTS2/";

  /**
   * Note: If you configure a larger default PlateSize, be sure to update
   * &#64;Column(length=3) on WellName.getName() so that length is large enough to
   * accommodate multi-letter row labels.
   */
  public static final PlateSize DEFAULT_PLATE_SIZE = PlateSize.WELLS_384;
  public static final int PLATE_NUMBER_LEN = 5;
  public static final int VOLUME_PRECISION = 10;
  public static final int VOLUME_SCALE = 9;
  public static final int MOLAR_CONCENTRATION_PRECISION = 12;
  public static final int MOLAR_CONCENTRATION_SCALE = 9;
  public static final int MG_ML_CONCENTRATION_PRECISION = 4;
  public static final int MG_ML_CONCENTRATION_SCALE = 1;
  public static final int MOLECULAR_MASS_PRECISION = 15;
  public static final int MOLECULAR_MASS_SCALE = 9;
  public static final int MOLECULAR_WEIGHT_PRECISION = 15;
  public static final int MOLECULAR_WEIGHT_SCALE = 9;

  public static final String PUBCHEM_BIOASSAY_ID_URL_PREFIX = "http://pubchem.ncbi.nlm.nih.gov/assay/assay.cgi?aid=";

  /**
   * The label to display in UI selection lists for fields that require a value, but that initially have no value
   * (null). This is intended to help the user understand that a value must be selected before proceeding.
   */
  public static final String REQUIRED_VOCAB_FIELD_PROMPT = "<select>";

  // Keep track of the study facility ID's being used for the batch process studies
  public static final String DEFAULT_BATCH_STUDY_ID_POSITIVE_COUNT_SM = "200001";
  public static final String DEFAULT_BATCH_STUDY_ID_POSITIVE_COUNT_RNAI = "200002";
  public static final String DEFAULT_BATCH_STUDY_ID_CONFIRMATION_SUMMARY = "200003";
}
