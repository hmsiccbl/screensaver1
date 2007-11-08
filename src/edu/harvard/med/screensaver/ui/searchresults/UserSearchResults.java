// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/ui/searchresults/ScreensaverUserSearchResults.java $
// $Id: ScreensaverUserSearchResults.java 1945 2007-10-10 16:45:09Z ant4 $

// Copyright 2006 by the President and Fellows of Harvard College.

// ScreensaverUsersaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.ui.table.TableColumn;
import edu.harvard.med.screensaver.util.NullSafeComparator;


/**
 * A {@link SearchResults} for {@link ScreensaverUser Users}.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class UserSearchResults extends EntitySearchResults<ScreensaverUser>
{

  // private static final fields


  // instance fields

  /*private UserViewer _screenViewer;*/

  private ArrayList<TableColumn<ScreensaverUser>> _columns;


  // public constructor

  /**
   * @motivation for CGLIB2
   */
//  protected UserSearchResults()
//  {
//  }

  public UserSearchResults(/*ScreensaverUserViewer userViewer*/)
  {
    //_userViewer = userViewer;
  }


  // implementations of the SearchResults abstract methods

  protected List<TableColumn<ScreensaverUser>> getColumns()
  {
    if (_columns == null) {
      _columns = new ArrayList<TableColumn<ScreensaverUser>>();
      _columns.add(new TableColumn<ScreensaverUser>("Last", "The last name of the user") {
        @Override
        public Object getCellValue(ScreensaverUser user) { return user.getLastName(); }
      });
      _columns.add(new TableColumn<ScreensaverUser>("First", "The first name of the user") {
        @Override
        public Object getCellValue(ScreensaverUser user) { return user.getFirstName(); }
      });
      _columns.add(new TableColumn<ScreensaverUser>("Date Created", "The date the user's account was created") {
        @Override
        public Object getCellValue(ScreensaverUser user)
        {
          return String.format("%tD", user.getDateCreated());
        }

        @Override
        protected Comparator<ScreensaverUser> getAscendingComparator()
        {
          return new NullSafeComparator<ScreensaverUser>() {
            @Override
            protected int doCompare(ScreensaverUser u1, ScreensaverUser u2) { return u1.getDateCreated().compareTo(u2.getDateCreated()); }
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
    compoundSorts.add(new Integer[] {0, 1, 2});
    compoundSorts.add(new Integer[] {1, 0, 2});
    compoundSorts.add(new Integer[] {2, 1, 2});
    return compoundSorts;
  }

  @Override
  protected void setEntityToView(ScreensaverUser user)
  {
//    _userViewer.viewScreensaverUser(user);
  }
}
