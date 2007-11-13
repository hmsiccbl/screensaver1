// $HeadURL$
// $Id$

// Copyright 2006 by the President and Fellows of Harvard College.

// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.StatusItem;
import edu.harvard.med.screensaver.model.screens.StatusValue;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.screens.ScreenViewer;
import edu.harvard.med.screensaver.ui.table.TableColumn;
import edu.harvard.med.screensaver.util.NullSafeComparator;


/**
 * A {@link SearchResults} for {@link Screen Screens}.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class ScreenSearchResults extends EntitySearchResults<Screen>
{

  // private static final fields


  // instance fields

  private ScreenViewer _screenViewer;
  private GenericEntityDAO _dao;

  private ArrayList<TableColumn<Screen,?>> _columns;


  // public constructor

  /**
   * @motivation for CGLIB2
   */
  protected ScreenSearchResults()
  {
  }

  public ScreenSearchResults(ScreenViewer screenViewer,
                             GenericEntityDAO dao)
  {
    _screenViewer = screenViewer;
    _dao = dao;
    getCapabilities().remove("filter");
  }


  // implementations of the SearchResults abstract methods

  protected List<TableColumn<Screen,?>> getColumns()
  {
    if (_columns == null) {
      _columns = new ArrayList<TableColumn<Screen,?>>();
      _columns.add(new IntegerColumn<Screen>("Screen Number", "The screen number") {
        @Override
        public Integer getCellValue(Screen screen) { return screen.getScreenNumber(); }

        @Override
        public Object cellAction(Screen screen) { return viewCurrentEntity(); }

        @Override
        public boolean isCommandLink() { return true; }
      });
      _columns.add(new TextColumn<Screen>("Title", "The title of the screen") {
        @Override
        public String getCellValue(Screen screen) { return screen.getTitle(); }
      });
      _columns.add(new UserNameColumn<Screen>("Lab Head", "The head of the lab performing the screen", _dao) {
        @Override
        public ScreensaverUser getUser(Screen screen) { return screen.getLabHead(); }
      });
      _columns.add(new UserNameColumn<Screen>("Lead Screener", "The scientist primarily responsible for running the screen", _dao) {
        @Override
        public ScreensaverUser getUser(Screen screen) { return screen.getLeadScreener(); }
      });
      _columns.add(new EnumColumn<Screen,ScreenResultAvailability>("Screen Result",
        "'available' if the screen result is loaded into Screensaver and viewable by the current user;" +
        " 'not shared' if loaded but not viewable by the current user; otherwise 'none'",
        ScreenResultAvailability.values()) {
        @Override
        public ScreenResultAvailability getCellValue(Screen screen)
        {
          if (screen.getScreenResult() == null) {
            return ScreenResultAvailability.NONE;
          }
          else if (screen.getScreenResult().isRestricted()) {
            return ScreenResultAvailability.NOT_SHARED;
          }
          else {
            return ScreenResultAvailability.AVAILABLE;
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
      _columns.add(new EnumColumn<Screen, ScreenType>("Screen Type", "'RNAi' or 'Small Molecule'", ScreenType.values()) {
        @Override
        public ScreenType getCellValue(Screen screen) { return screen.getScreenType(); }
      });
      _columns.add(new EnumColumn<Screen,StatusValue>("Status", "The current status of the screen, e.g., 'Completed', 'Ongoing', 'Pending', etc.", StatusValue.values()) {
        @Override
        public StatusValue getCellValue(Screen screen)
        {
          SortedSet<StatusItem> statusItems = screen.getSortedStatusItems();
          if (statusItems.size() == 0) {
            return null;
          }
          StatusItem statusItem = statusItems.last();
          return statusItem.getStatusValue();
        }

        @Override
        public boolean isVisible() { return showStatusFields(); }
      });
      _columns.add(new DateColumn<Screen>("Status Date", "The date of the most recent change of status for the screen") {
        @Override
        protected Date getDate(Screen screen) {
          SortedSet<StatusItem> statusItems = screen.getSortedStatusItems();
          return statusItems.size() == 0 ? null : statusItems.last().getStatusDate();
        }

        @Override
        public boolean isVisible() { return showStatusFields(); }
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

  @Override
  protected void setEntityToView(Screen screen)
  {
    _screenViewer.viewScreen(screen);
  }

  private boolean showStatusFields()
  {
    return isUserInRole(ScreensaverUserRole.SCREENS_ADMIN) ||
      isUserInRole(ScreensaverUserRole.READ_EVERYTHING_ADMIN);
  }
}
