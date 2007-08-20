//$HeadURL$
//$Id$

//Copyright 2006 by the President and Fellows of Harvard College.

//Screensaver is an open-source project developed by the ICCB-L and NSRB labs
//at Harvard Medical School. This software is distributed under the terms of
//the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.io.DataExporter;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.StatusItem;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.control.ScreensController;
import edu.harvard.med.screensaver.ui.table.TableColumn;
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


  // instance fields

  private ScreensController _screensController;
  private GenericEntityDAO _dao;
  private ArrayList<TableColumn<Screen>> _columns;

  // public constructor

  /**
   * Construct a new <code>ScreenSearchResult</code> object.
   * @param unsortedResults the unsorted list of the results, as they are returned from the
   * database
   */
  public ScreenSearchResults(List<Screen> unsortedResults, 
                             ScreensController screensController, 
                             GenericEntityDAO dao)
  {
    super(unsortedResults);
    _screensController = screensController;
    _dao = dao;
    setCurrentScreensaverUser(((AbstractBackingBean) screensController).getCurrentScreensaverUser());
  }
  

  // implementations of the SearchResults abstract methods

  protected List<TableColumn<Screen>> getColumns()
  {
    if (_columns == null) {
      _columns = new ArrayList<TableColumn<Screen>>();
      _columns.add(new TableColumn<Screen>("Screen Number", "The screen number", true) {
        @Override
        public Object getCellValue(Screen screen) { return screen.getScreenNumber(); }

        @Override
        public Object cellAction(Screen screen) { return _screensController.viewScreen(screen, ScreenSearchResults.this); }

        @Override
        public boolean isCommandLink() { return true; }
      });
      _columns.add(new TableColumn<Screen>("Screen Type", "'RNAi' or 'Small Molecule'") {
        @Override
        public Object getCellValue(Screen screen) { return screen.getScreenType().getValue(); }
      });
      _columns.add(new TableColumn<Screen>("Status", "The current status of the screen, e.g., 'Completed', 'Ongoing', 'Pending', etc.") {
        @Override
        public Object getCellValue(Screen screen) 
        { 
          SortedSet<StatusItem> statusItems = screen.getSortedStatusItems();
          if (statusItems.size() == 0) {
            return "";
          }
          StatusItem statusItem = statusItems.last();
          return statusItem.getStatusValue();
        }

        @Override
        public boolean isVisible() { return showStatusFields(); }
      });
      _columns.add(new TableColumn<Screen>("Status Date", "The date of the most recent change of status for the screen") {
        @Override
        public Object getCellValue(Screen screen) 
        {
          SortedSet<StatusItem> statusItems = screen.getSortedStatusItems();
          if (statusItems.size() == 0) {
            return "";
          }
          StatusItem statusItem = statusItems.last();
          return String.format("%tD", statusItem.getStatusDate());
        }

        @Override
        protected Comparator<Screen> getAscendingComparator() 
        { 
          return new Comparator<Screen>() {
            public int compare(Screen s1, Screen s2) {
              SortedSet<StatusItem> statusItems1 = s1.getSortedStatusItems();
              SortedSet<StatusItem> statusItems2 = s2.getSortedStatusItems();
              if (statusItems1.size() == 0) {
                if (statusItems2.size() == 0) {
                  return 0;
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

        @Override
        public boolean isVisible() { return showStatusFields(); }
      });
      _columns.add(new TableColumn<Screen>("Title", "The title of the screen") {
        @Override
        public Object getCellValue(Screen screen) { return screen.getTitle(); }
      });
      _columns.add(new TableColumn<Screen>("Lab Head", "The head of the lab performing the screen") {
        @Override
        public Object getCellValue(Screen screen) { return screen.getLabHead().getFullNameLastFirst(); }

        @Override
        protected Comparator<Screen> getAscendingComparator() 
        { 
          return new Comparator<Screen>() {
            public int compare(Screen s1, Screen s2) {
              return ScreensaverUserComparator.getInstance().compare(s1.getLabHead(), 
                                                                     s2.getLabHead());
            }
          };
        }
      });
      _columns.add(new TableColumn<Screen>("Lead Screener", "The scientist primarily responsible for running the screen") {
        @Override
        public Object getCellValue(Screen screen) { return screen.getLeadScreener().getFullNameLastFirst(); }

        @Override
        protected Comparator<Screen> getAscendingComparator() 
        { 
          return new Comparator<Screen>() {
            public int compare(Screen s1, Screen s2) {
              return ScreensaverUserComparator.getInstance().compare(s1.getLeadScreener(), 
                                                                     s2.getLeadScreener());
            } 
          };
        }
      });
      _columns.add(new TableColumn<Screen>("Screen Result", 
        "'available' if the screen result is loaded into Screensaver and viewable by the current user;" +
        " 'not shared' if loaded but not viewable by the current user; otherwise 'none'") {
        @Override
        public Object getCellValue(Screen screen) 
        { 
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

        @Override
        protected Comparator<Screen> getAscendingComparator() 
        {
          return new Comparator<Screen>() {
            private NullSafeComparator<ScreenResult> _srComparator =
              new NullSafeComparator<ScreenResult>(true)
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
      });
    }
    return _columns;
  }

  @Override
  protected List<Integer[]> getCompoundSorts()
  {
    List<Integer[]> compoundSorts = super.getCompoundSorts();
    compoundSorts.add(new Integer[] {2, 3, 0});
    compoundSorts.add(new Integer[] {3, 2, 0});
    return compoundSorts;
  }

  private boolean showStatusFields()
  {
    return isUserInRole(ScreensaverUserRole.SCREENS_ADMIN) ||
      isUserInRole(ScreensaverUserRole.READ_EVERYTHING_ADMIN);
  }
  @Override
  protected List<DataExporter<Screen>> getDataExporters()
  {
    return new ArrayList<DataExporter<Screen>>();
  }

  @Override
  public String showSummaryView()
  {
    return _screensController.browseScreens();
  }

  @Override
  protected void setEntityToView(Screen screen)
  {
    _screensController.viewScreen(screen, this);
  }
}
