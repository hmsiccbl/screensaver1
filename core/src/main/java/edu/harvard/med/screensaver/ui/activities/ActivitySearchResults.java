// $HeadURL$
// $Id$

// Copyright © 2006, 2010 by the President and Fellows of Harvard College.

// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.datafetcher.DataFetcherUtil;
import edu.harvard.med.screensaver.db.datafetcher.EntityDataFetcher;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.model.Activity;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TableColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.DateEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.IntegerEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.UserNameColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.VocabularyEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.model.InMemoryEntityDataModel;
import edu.harvard.med.screensaver.ui.arch.searchresults.EntityBasedEntitySearchResults;
import edu.harvard.med.screensaver.ui.arch.searchresults.SearchResults;
import edu.harvard.med.screensaver.ui.arch.util.converter.VocabularyConverter;
import edu.harvard.med.screensaver.ui.arch.view.EntityViewer;
import edu.harvard.med.screensaver.ui.users.UserViewer;
import edu.harvard.med.screensaver.util.CollectionUtils;


/**
 * A {@link SearchResults} for {@link Activity Activities}.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public abstract class ActivitySearchResults<A extends Activity> extends EntityBasedEntitySearchResults<A,Integer>
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

  public ActivitySearchResults(LabActivityViewer labActivityViewer,
                               Class<A> type,
                               GenericEntityDAO dao,
                               UserViewer userViewer)
  {
    super((EntityViewer<A>) labActivityViewer);
    _type = type;
    _dao = dao;
    _userViewer = userViewer;
  }

  @Override
  public void searchAll()
  {
    setTitle("Lab Activities");
    initialize(new InMemoryEntityDataModel<A,Integer,A>(new EntityDataFetcher<A,Integer>(_type, _dao)));
  }

  public void searchActivitiesForUser(final ScreensaverUser user)
  {
    setTitle("Lab Activities for " + user.getFullNameFirstLast());
    initialize(new InMemoryEntityDataModel<A,Integer,A>(new EntityDataFetcher<A,Integer>(_type, _dao) {
      @Override
      public void addDomainRestrictions(HqlBuilder hql)
      {
        DataFetcherUtil.addDomainRestrictions(hql, Activity.performedBy, user, getRootAlias());
      }
    }));
  }

  public void searchLibraryScreeningActivitiesForCopy(final Copy copy)
  {
    setTitle("Library Screenings for library " + copy.getLibrary().getLibraryName() + ", copy " + copy.getName());
    initialize(new InMemoryEntityDataModel<A,Integer,A>(new EntityDataFetcher<A,Integer>(_type, _dao) {
      @Override
      public void addDomainRestrictions(HqlBuilder hql)
      {
        DataFetcherUtil.addDomainRestrictions(hql, LibraryScreening.assayPlatesScreened.to("plateScreened").to("copy"), copy, getRootAlias());
      }
    }));
  }

  public void searchLibraryScreeningActivitiesForPlate(final Plate plate)
  {
    setTitle("Library Screenings for plate " + plate.getPlateNumber());
    initialize(new InMemoryEntityDataModel<A,Integer,A>(new EntityDataFetcher<A,Integer>(_type, _dao) {
      @Override
      public void addDomainRestrictions(HqlBuilder hql)
      {
        DataFetcherUtil.addDomainRestrictions(hql, LibraryScreening.assayPlatesScreened.to("plateScreened"), plate, getRootAlias());
      }
    }));
  }

  public void searchActivities(final Set<A> activities, String title)
  {
    setTitle(title);
    initialize(new InMemoryEntityDataModel<A,Integer,A>(new EntityDataFetcher<A,Integer>(_type, _dao) {
      @Override
      public void addDomainRestrictions(HqlBuilder hql)
      {
        DataFetcherUtil.addDomainRestrictions(hql, getRootAlias(), CollectionUtils.<Integer>entityIds(activities));
      }
    }));
  }


  // implementations of the SearchResults abstract methods

  @Override
  protected List<? extends TableColumn<A,?>> buildColumns()
  {
    ArrayList<TableColumn<A,?>> columns = Lists.newArrayList();
    columns.add(new IntegerEntityColumn<A>(RelationshipPath.from(_type).toId(),
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
    columns.add(new VocabularyEntityColumn<A,String>(RelationshipPath.from(_type).toProperty("activityType"),
      "Activity Type",
      "The type of the activity",
      TableColumn.UNGROUPED,
      new VocabularyConverter<String>(getActivityTypes()), getActivityTypes()) {
      @Override
      public String getCellValue(A activity)
      {
        return activity.getActivityTypeName();
      }
    });
    columns.add(new DateEntityColumn<A>(RelationshipPath.from(_type).toProperty("datePerformed"),
      "Date Performed", "The date of the activity", TableColumn.UNGROUPED) {
      @Override
      protected LocalDate getDate(Activity activity) {
        return activity.getDateOfActivity();
      }
    });
    columns.add(new DateEntityColumn<A>(RelationshipPath.from(_type).toProperty("dateCreated"),
      "Date Recorded", "The date the activity was recorded", TableColumn.UNGROUPED) {
      @Override
      protected LocalDate getDate(A activity) {
        return activity.getDateCreated().toLocalDate();
      }
    });
    columns.get(columns.size() - 1).setVisible(showAdminStatusFields());
    columns.add(new UserNameColumn<A,ScreensaverUser>(RelationshipPath.from(_type).to(Activity.performedBy.getLeaf()),
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
