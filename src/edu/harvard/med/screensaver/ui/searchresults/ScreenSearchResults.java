// $HeadURL: svn+ssh://js163@orchestra/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.StatusItem;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.control.ScreensController;
import edu.harvard.med.screensaver.ui.util.ScreensaverUserComparator;
import edu.harvard.med.screensaver.util.NullSafeComparator;


/**
 * A {@link SearchResults} for {@link Screen Screens}.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class ScreenSearchResults extends SearchResults<Screen>
{
  
  // private static final fields
  
  private static final String SCREEN_NUMBER = "Screen Number";
  private static final String SCREEN_TYPE = "Screen Type";
  private static final String SCREEN_STATUS = "Status";
  private static final String SCREEN_STATUS_DATE = "Status Date";
  private static final String TITLE = "Title";
  private static final String LAB_HEAD = "Lab Head";
  private static final String LEAD_SCREENER = "Lead Screener";
  private static final String SCREEN_RESULT = "Screen Result";
  
  
  // instance fields
  
  private ScreensController _screensController;
  private GenericEntityDAO _dao;
  
  // public constructor
  
  /**
   * Construct a new <code>ScreenSearchResult</code> object.
   * @param unsortedResults the unsorted list of the results, as they are returned from the
   * database
   * @param genericEntityDao 
   */
  public ScreenSearchResults(List<Screen> unsortedResults, 
                             ScreensController screensController, 
                             GenericEntityDAO dao)
  {
    super(unsortedResults);
    _screensController = screensController;
    _dao = dao;
  }


  // implementations of the SearchResults abstract methods
  
  @Override
  public String showSummaryView()
  {
    return _screensController.browseScreens();
  }
  
  @Override
  protected List<String> getColumnHeaders()
  {
    List<String> columnHeaders = new ArrayList<String>();
    columnHeaders.add(SCREEN_NUMBER);
    columnHeaders.add(SCREEN_TYPE);
    if (isUserInRole(ScreensaverUserRole.SCREEN_RESULTS_ADMIN) ||
      isUserInRole(ScreensaverUserRole.READ_EVERYTHING_ADMIN)) {
      columnHeaders.add(SCREEN_STATUS);
      columnHeaders.add(SCREEN_STATUS_DATE);
    }
    columnHeaders.add(TITLE);
    columnHeaders.add(LAB_HEAD);
    columnHeaders.add(LEAD_SCREENER);
    columnHeaders.add(SCREEN_RESULT);
    return columnHeaders;
  }

  @Override
  protected boolean isCommandLink(String columnName)
  {
    return columnName.equals(SCREEN_NUMBER);
  }
  
  @Override
  protected boolean isCommandLinkList(String columnName)
  {
    return false;
  }

  @Override
  protected String getColumnDescription(String columnName)
  {
    if (columnName.equals(SCREEN_NUMBER)) {
      return "The screen number";
    }
    if (columnName.equals(SCREEN_TYPE)) {
      return "'RNAi' or 'Small Molecule'";
    }
    if (columnName.equals(SCREEN_STATUS)) {
      return "The current status of the screen, e.g., 'Completed', 'Ongoing', 'Pending', etc.";
    }
    if (columnName.equals(SCREEN_STATUS_DATE)) {
      return "The date of the most recent change of status for the screen";
    }
    if (columnName.equals(TITLE)) {
      return "The title of the screen";
    }
    if (columnName.equals(LAB_HEAD)) {
      return "The head of the lab performing the screen";
    }
    if (columnName.equals(LEAD_SCREENER)) {
      return "The scientist primarily responsible for running the screen";
    }
    if (columnName.equals(SCREEN_RESULT)) {
      return "'available' if the screen result is loaded into Screensaver and viewable by the current user;" +
          " 'not shared' if loaded but not viewable by the current user; otherwise 'none'";
    }
    return null;
  }
  
  @Override
  protected Object getCellValue(Screen screen, String columnName)
  {
    if (columnName.equals(SCREEN_NUMBER)) {
      return screen.getScreenNumber();
    }
    if (columnName.equals(SCREEN_TYPE)) {
      return screen.getScreenType().getValue();
    }
    if (columnName.equals(SCREEN_STATUS)) {
      SortedSet<StatusItem> statusItems = screen.getSortedStatusItems();
      if (statusItems.size() == 0) {
        return "";
      }
      StatusItem statusItem = statusItems.last();
      return statusItem.getStatusValue();
    }
    if (columnName.equals(SCREEN_STATUS_DATE)) {
      SortedSet<StatusItem> statusItems = screen.getSortedStatusItems();
      if (statusItems.size() == 0) {
        return "";
      }
      StatusItem statusItem = statusItems.last();
      return String.format("%tD", statusItem.getStatusDate());
    }
    if (columnName.equals(TITLE)) {
      return screen.getTitle();
    }
    if (columnName.equals(LAB_HEAD)) {
      return screen.getLabHead().getFullNameLastFirst();
    }
    if (columnName.equals(LEAD_SCREENER)) {
      return screen.getLeadScreener().getFullNameLastFirst();
    }
    if (columnName.equals(SCREEN_RESULT)) {
      if (screen.getScreenResult() == null) {
        return "none";
      }
      else if (screen.getScreenResult().isRestricted()) {
        return "not shared";
      }
      else {
        return "available";
      }
    }
    return null;
  }

  @Override
  protected Object cellAction(Screen screen, String columnName)
  {
    return _screensController.viewScreen(screen, this);
  }
  
  @Override
  protected Comparator<Screen> getComparatorForColumnName(String columnName)
  {
    if (columnName.equals(SCREEN_NUMBER)) {
      return new Comparator<Screen>() {
        public int compare(Screen s1, Screen s2) {
          return s1.getScreenNumber().compareTo(s2.getScreenNumber());
        }
      };
    }
    if (columnName.equals(TITLE)) {
      return new Comparator<Screen>() {
        public int compare(Screen s1, Screen s2) {
          return s1.getTitle().compareTo(s2.getTitle());
        }
      };
    }
    if (columnName.equals(SCREEN_TYPE)) {
      return new Comparator<Screen>() {
        public int compare(Screen s1, Screen s2) {
          return s1.getScreenType().getValue().compareTo(s2.getScreenType().getValue());
        }
      };
    }
    if (columnName.equals(SCREEN_STATUS)) {
      return new Comparator<Screen>() {
        public int compare(Screen s1, Screen s2) {
          SortedSet<StatusItem> statusItems1 = s1.getSortedStatusItems();
          SortedSet<StatusItem> statusItems2 = s2.getSortedStatusItems();
          if (statusItems1.size() == 0) {
            if (statusItems2.size() == 0) {
              return s1.getScreenNumber().compareTo(s2.getScreenNumber());
            }
            return -1;
          }
          if (statusItems2.size() == 0) {
            return 1;
          }
          StatusItem statusItem1 = statusItems1.last();
          StatusItem statusItem2 = statusItems2.last();
          return statusItem1.compareTo(statusItem2);
        }
      };
    }
    if (columnName.equals(SCREEN_STATUS_DATE)) {
      return new Comparator<Screen>() {
        public int compare(Screen s1, Screen s2) {
          SortedSet<StatusItem> statusItems1 = s1.getSortedStatusItems();
          SortedSet<StatusItem> statusItems2 = s2.getSortedStatusItems();
          if (statusItems1.size() == 0) {
            if (statusItems2.size() == 0) {
              return s1.getScreenNumber().compareTo(s2.getScreenNumber());
            }
            return -1;
          }
          if (statusItems2.size() == 0) {
            return 1;
          }
          StatusItem statusItem1 = statusItems1.last();
          StatusItem statusItem2 = statusItems2.last();
          return statusItem1.getStatusDate().compareTo(statusItem2.getStatusDate());
        }
      };
    }
    if (columnName.equals(LAB_HEAD)) {
      return new Comparator<Screen>() {
        public int compare(Screen s1, Screen s2) {
          return ScreensaverUserComparator.getInstance().compare(s1.getLabHead(), 
                                                                 s2.getLabHead());
        }
      };
    }
    if (columnName.equals(LEAD_SCREENER)) {
      return new Comparator<Screen>() {
        public int compare(Screen s1, Screen s2) {
          return ScreensaverUserComparator.getInstance().compare(s1.getLeadScreener(), 
                                                                 s2.getLeadScreener());
        }
      };
    }
    if (columnName.equals(SCREEN_RESULT)) {
      return new Comparator<Screen>() {

        private NullSafeComparator<ScreenResult> _srComparator = new NullSafeComparator<ScreenResult>()
        {
          @Override
          protected int doCompare(ScreenResult sr1, ScreenResult sr2)
          {
            if (!sr1.isRestricted() && sr2.isRestricted()) {
              return -1;
            }
            if (sr1.isRestricted() && !sr2.isRestricted()) {
              return 1;
            }
            return sr1.getScreen().getScreenNumber().compareTo(sr2.getScreen().getScreenNumber());
          }
        };
        
        public int compare(Screen s1, Screen s2) {
          return _srComparator.compare(s1.getScreenResult(),
                                      s2.getScreenResult());
        }
      };
    }
    return null;
  }


  @Override
  protected void setEntityToView(Screen screen)
  {
    _screensController.viewScreen(screen, this);
  }
}
