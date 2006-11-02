// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver;

public interface ScreensaverConstants
{
  // static data members
  
  public String APPLICATION_NAME = "Screensaver";
  public String APPLICATION_VERSION = "1.00a";
  public String APPLICATION_TITLE = APPLICATION_NAME + " " + APPLICATION_VERSION;
  public String APPLICATION_SVN_REVISION = "$Revision$";

  
  // JSF Action Results 
  // these values are returned by backing beans' action methods and are used to
  // define navigation rules in faces-config.xml
  
  public static final String REDISPLAY_PAGE_ACTION_RESULT = null;
  public static final String SUCCESS_ACTION_RESULT = "success";
  public static final String ERROR_ACTION_RESULT = "error";
  public static final String DONE_ACTION_RESULT = "done";
  public static final String LOGOUT_ACTION_RESULT = "logout";
  public static final String VIEW_SCREENING_ROOM_USER_ACTION_RESULT = "viewScreeningRoomUser";
  public static final String SHOW_SEARCH_RESULTS_SUMMARY_ACTION = "showSearchResultsSummary";
  public static final String VIEW_OR_EDIT_SCREEN_RESULT_ACTION = "viewScreenResult";
  public static final String VIEW_SCREEN_ACTION = "viewScreen";
  public static final String VIEW_VISIT_ACTION_RESULT = "viewVisit";
  public static final String VIEW_BILLING_INFORMATION_ACTION_RESULT = "viewBillingInformation";
  public static final String VIEW_ATTACHED_FILE_ACTION_RESULT = "viewAttachedFile";
}
