// $HeadURL$
// $Id$

// Copyright 2006 by the President and Fellows of Harvard College.

// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.datafetcher.AllEntitiesOfTypeDataFetcher;
import edu.harvard.med.screensaver.db.datafetcher.EntityDataFetcher;
import edu.harvard.med.screensaver.db.datafetcher.EntitySetDataFetcher;
import edu.harvard.med.screensaver.db.datafetcher.ParentedEntityDataFetcher;
import edu.harvard.med.screensaver.model.Activity;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.EntityViewer;
import edu.harvard.med.screensaver.ui.activities.ActivityViewer;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.DateEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.IntegerEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.UserNameColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.VocabularyEntityColumn;
import edu.harvard.med.screensaver.ui.users.UserViewer;
import edu.harvard.med.screensaver.ui.util.VocabularlyConverter;
import edu.harvard.med.screensaver.util.CollectionUtils;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;

import com.google.common.collect.Lists;


/**
 * A {@link SearchResults} for {@link Activity Activities}.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public abstract class ActivitySearchResults<A extends Activity> extends EntitySearchResults<A,Integer>
{
  private static final Logger log = Logger.getLogger(ActivitySearchResults.class);

  private UserViewer _userViewer;

  protected GenericEntityDAO _dao;
  private Class<A> _type;


  /**
   * @motivation for CGLIB2
   */
  protected ActivitySearchResults()
  {
  }

  public ActivitySearchResults(ActivityViewer activityViewer,
                               Class<A> type,
                               GenericEntityDAO dao,
                               UserViewer userViewer)
  {
    super((EntityViewer<A>) activityViewer);
    _type = type;
    _dao = dao;
    _userViewer = userViewer;
  }

  @Override
  public void searchAll()
  {
    EntityDataFetcher<A,Integer> dataFetcher =
      (EntityDataFetcher<A,Integer>) new AllEntitiesOfTypeDataFetcher<A,Integer>(
        _type,
        _dao);
    initialize(dataFetcher);
  }

  public void searchActivitiesForUser(ScreensaverUser user)
  {
    initialize((EntityDataFetcher<A,Integer>) new ParentedEntityDataFetcher<A,Integer>(
      _type,
      new RelationshipPath<A>(_type, "performedBy"),
      user,
      _dao));
  }

  public void searchActivities(Set<Activity> activities)
  {
    initialize((EntityDataFetcher<A,Integer>) new EntitySetDataFetcher<A,Integer>(
      _type,
      CollectionUtils.<Integer>entityIds(activities),
      _dao));
  }


  // implementations of the SearchResults abstract methods

  @Override
  protected List<? extends TableColumn<A,?>> buildColumns()
  {
    ArrayList<TableColumn<A,?>> columns = Lists.newArrayList();
    columns.add(new IntegerEntityColumn<A>(
      new PropertyPath<A>(_type, "activityId"),
      "Activity ID",
      "The activity number",
      TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(A activity) { return activity.getActivityId(); }

      @Override
      public Object cellAction(A activity) { return viewSelectedEntity(); }

      @Override
      public boolean isCommandLink() { return true; }
    });
    columns.add(new VocabularyEntityColumn<A,String>(
      new PropertyPath<A>(_type, "activityType"),
      "Activity Type",
      "The type of the activity",
      TableColumn.UNGROUPED,
      new VocabularlyConverter<String>(getActivityTypes()), getActivityTypes()) {
      @Override
      public String getCellValue(A activity)
      {
        return activity.getActivityTypeName();
      }
    });
    columns.add(new DateEntityColumn<A>(
      new PropertyPath<A>(_type, "datePerformed"),
      "Date Performed", "The date of the activity", TableColumn.UNGROUPED) {
      @Override
      protected LocalDate getDate(Activity activity) {
        return activity.getDateOfActivity();
      }
    });
    columns.add(new DateEntityColumn<A>(
      new PropertyPath<A>(_type, "dateRecorded"),
      "Date Recorded", "The date the activity was recorded", TableColumn.UNGROUPED) {
      @Override
      protected LocalDate getDate(A activity) {
        return activity.getDateCreated().toLocalDate();
      }
    });
    columns.get(columns.size() - 1).setVisible(showAdminStatusFields());
    columns.add(new UserNameColumn<A,ScreensaverUser>(
      new RelationshipPath<A>(_type, "performedBy"),
      "Performed By", "The person that performed the activity", TableColumn.UNGROUPED, _userViewer) {
      @Override
      public ScreensaverUser getUser(A activity) { return (ScreensaverUser) activity.getPerformedBy(); }
    });
    return columns;
  }

  protected abstract Set<String> getActivityTypes();

  private boolean showAdminStatusFields()
  {
    return isUserInRole(ScreensaverUserRole.SCREENS_ADMIN) ||
      isUserInRole(ScreensaverUserRole.READ_EVERYTHING_ADMIN);
  }
}
