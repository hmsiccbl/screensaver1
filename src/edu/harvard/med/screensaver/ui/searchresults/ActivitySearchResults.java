// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/ui/searchresults/ActivitySearchResults.java $
// $Id: ActivitySearchResults.java 2034 2007-11-13 21:16:00Z ant4 $

// Copyright 2006 by the President and Fellows of Harvard College.

// Activitysaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.Activity;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.table.TableColumn;
import edu.harvard.med.screensaver.ui.util.VocabularlyConverter;


/**
 * A {@link SearchResults} for {@link Activity Activities}.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public abstract class ActivitySearchResults<A extends Activity> extends EntitySearchResults<A>
{

  // private static final fields


  // instance fields

  private GenericEntityDAO _dao;

  private ArrayList<TableColumn<A,?>> _columns;


  // public constructor

  /**
   * @motivation for CGLIB2
   */
  protected ActivitySearchResults()
  {
  }

  public ActivitySearchResults(//ActivityViewer activityViewer,
                               GenericEntityDAO dao)
  {
    //_activityViewer = activityViewer;
    _dao = dao;
  }


  // implementations of the SearchResults abstract methods

  protected List<TableColumn<A,?>> getColumns()
  {
    if (_columns == null) {
      _columns = new ArrayList<TableColumn<A,?>>();
      _columns.add(new VocabularlyColumn<A,String>(
        "Activity Type",
        "The type of the activity",
        new VocabularlyConverter<String>(getActivityTypes()),
        getActivityTypes()) {
        @Override
        public String getCellValue(A activity)
        {
          return (String) activity.getClass().getSimpleName();
        }
      });
      _columns.add(new DateColumn<A>("Date Performed", "The date of the activity") {
        @Override
        protected Date getDate(A activity) {
          return activity.getDateOfActivity();
        }
      });
      _columns.add(new DateColumn<A>("Date Recorded", "The date the activity was recorded") {
        @Override
        protected Date getDate(A activity) {
          return activity.getDateCreated();
        }

        @Override
        public boolean isVisible()
        {
          return showAdminStatusFields();
        }
      });
      _columns.add(new UserNameColumn<A>(
        "Performed By",
        "The person that performed the activity") {
        @Override
        public ScreensaverUser getUser(A activity) { return activity.getPerformedBy(); }
      });
    }
    return _columns;
  }

  protected abstract Set<String> getActivityTypes();

  @Override
  protected List<Integer[]> getCompoundSorts()
  {
    List<Integer[]> compoundSorts = super.getCompoundSorts();
//    compoundSorts.add(new Integer[] {2, 3, 0});
    //compoundSorts.add(new Integer[] {3, 2, 0});
    return compoundSorts;
  }

  @Override
  protected void setEntityToView(Activity activity)
  {
    //_activityViewer.viewActivity(activity);
  }

  private boolean showAdminStatusFields()
  {
    return isUserInRole(ScreensaverUserRole.SCREENS_ADMIN) ||
      isUserInRole(ScreensaverUserRole.READ_EVERYTHING_ADMIN);
  }
}
