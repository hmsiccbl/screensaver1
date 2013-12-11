// $HeadURL$
// $Id$

// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.

// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.harvard.med.screensaver.db.EntityInflator;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.datafetcher.DataFetcherUtil;
import edu.harvard.med.screensaver.db.datafetcher.EntityDataFetcher;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.model.Entity;
import edu.harvard.med.screensaver.model.activities.Activity;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivity;
import edu.harvard.med.screensaver.model.activities.ServiceActivity;
import edu.harvard.med.screensaver.model.activities.ServiceActivityType;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickAssayPlate;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickLiquidTransfer;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.libraries.WellVolumeCorrectionActivity;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.screens.CherryPickScreening;
import edu.harvard.med.screensaver.model.screens.LabActivity;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TableColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TextColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.DateEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.IntegerEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.RelatedEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.TextEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.UserNameColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.VocabularyEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.model.InMemoryEntityDataModel;
import edu.harvard.med.screensaver.ui.arch.searchresults.EntityBasedEntitySearchResults;
import edu.harvard.med.screensaver.ui.arch.searchresults.SearchResults;
import edu.harvard.med.screensaver.ui.arch.util.converter.VocabularyConverter;
import edu.harvard.med.screensaver.ui.arch.view.EntityViewer;
import edu.harvard.med.screensaver.ui.cherrypickrequests.CherryPickRequestViewer;
import edu.harvard.med.screensaver.ui.screens.ScreenSearchResults;
import edu.harvard.med.screensaver.ui.screens.ScreenViewer;
import edu.harvard.med.screensaver.ui.users.UserViewer;
import edu.harvard.med.screensaver.util.CollectionUtils;


/**
 * A {@link SearchResults} for {@link Activity Activities}.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class ActivitySearchResults extends EntityBasedEntitySearchResults<Activity,Integer>
{
  private final static Set<String> activityTypes = Sets.newLinkedHashSet();
  {
    for (ServiceActivityType type : ServiceActivityType.values()) {
      activityTypes.add(type.toString());
    }
    activityTypes.add(LibraryScreening.ACTIVITY_TYPE_NAME);
    activityTypes.add(LibraryScreening.EXTERNAL_LIBRARY_SCREENING_ACTIVITY_TYPE_NAME);
    activityTypes.add(CherryPickScreening.ACTIVITY_TYPE_NAME);
    activityTypes.add(CherryPickLiquidTransfer.ACTIVITY_TYPE_NAME);
  }

  protected static final String SCREEEN_COLUMN_GROUP = "Screen";

  private static Logger log = Logger.getLogger(ActivitySearchResults.class);

  private UserViewer _userViewer;
  private GenericEntityDAO _dao;
  private ScreenViewer _screenViewer;
  private CherryPickRequestViewer _cprViewer;
  private ScreenSearchResults _screensBrowser;


  /**
   * @motivation for CGLIB2
   */
  protected ActivitySearchResults()
  {
  }

  public ActivitySearchResults(ActivityViewer activityViewer,
                               ScreenViewer screenViewer,
                               ScreenSearchResults screensBrowser,
                               CherryPickRequestViewer cprViewer,
                               UserViewer userViewer,
                               GenericEntityDAO dao)
  {
    super((EntityViewer<Activity>) activityViewer);
    _dao = dao;
    _screenViewer = screenViewer;
    _screensBrowser = screensBrowser;
    _cprViewer = cprViewer;
    _userViewer = userViewer;
  }

  @Override
  public void searchAll()
  {
    setTitle("All Activities");
    initialize(new InMemoryEntityDataModel<Activity,Integer,Activity>(new EntityDataFetcher<Activity,Integer>(Activity.class, _dao)));
  }

  public void searchAllUserActivities()
  {
    setTitle("User Activities");
    EntityDataFetcher<Activity,Integer> entityDataFetcher = (EntityDataFetcher<Activity,Integer>) new EntityDataFetcher<Activity,Integer>(Activity.class, _dao) {
      @Override
      public void addDomainRestrictions(HqlBuilder hql)
      {
        // HACK: improve this query so as to be less dependent upon the potential subclasses of AdministrativeActivities
        hql.where(new edu.harvard.med.screensaver.db.hqlbuilder.Predicate() {
          @Override
          public String toHql()
          {
            return getRootAlias() + ".class <> " + AdministrativeActivity.class.getName() + " and " +
              getRootAlias() + ".class <> " + WellVolumeCorrectionActivity.class.getName();

          }
        });
      }
    };
    initialize(new InMemoryEntityDataModel<Activity,Integer,Activity>(entityDataFetcher));
  }

  public void searchActivitiesForUser(final ScreensaverUser user)
  {
    setTitle("Activities for " + user.getFullNameFirstLast());
    initialize(new InMemoryEntityDataModel<Activity,Integer,Activity>(new EntityDataFetcher<Activity,Integer>(Activity.class, _dao) {
      @Override
      public void addDomainRestrictions(HqlBuilder hql)
      {
        if (user instanceof ScreeningRoomUser) {
          ScreeningRoomUser user2 = new EntityInflator<ScreeningRoomUser>(_dao, (ScreeningRoomUser) user, true).need(ScreeningRoomUser.serviceActivities).need(ScreeningRoomUser.activitiesPerformed).inflate();
          DataFetcherUtil.addDomainRestrictions(hql, getRootAlias(), Sets.newHashSet(Iterables.transform(((ScreeningRoomUser) user2).getAssociatedActivities(), Entity.ToEntityId)));
        }
        else {
          ScreensaverUser user2 = new EntityInflator<ScreensaverUser>(_dao, (ScreensaverUser) user, true).need(ScreeningRoomUser.activitiesPerformed).inflate();
          DataFetcherUtil.addDomainRestrictions(hql, getRootAlias(), Sets.newHashSet(Iterables.transform(((ScreensaverUser) user2).getActivitiesPerformed(), Entity.ToEntityId)));
        }
      }
    }));
  }

  public void searchLibraryScreeningActivitiesForCopy(final Copy copy)
  {
    setTitle("Library Screenings for library " + copy.getLibrary().getLibraryName() + ", copy " + copy.getName());
    initialize(new InMemoryEntityDataModel<Activity,Integer,Activity>(new EntityDataFetcher<Activity,Integer>(Activity.class, _dao) {
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
    initialize(new InMemoryEntityDataModel<Activity,Integer,Activity>(new EntityDataFetcher<Activity,Integer>(Activity.class, _dao) {
      @Override
      public void addDomainRestrictions(HqlBuilder hql)
      {
        DataFetcherUtil.addDomainRestrictions(hql, LibraryScreening.assayPlatesScreened.to("plateScreened"), plate, getRootAlias());
      }
    }));
  }

  public void searchActivities(final Set<Activity> activities, String title)
  {
    setTitle(title);
    initialize(new InMemoryEntityDataModel<Activity,Integer,Activity>(new EntityDataFetcher<Activity,Integer>(Activity.class, _dao) {
      @Override
      public void addDomainRestrictions(HqlBuilder hql)
      {
        DataFetcherUtil.addDomainRestrictions(hql, getRootAlias(), CollectionUtils.<Integer>entityIds(activities));
      }
    }));
  }


  // implementations of the SearchResults abstract methods

  @Override
  protected List<? extends TableColumn<Activity,?>> buildColumns()
  {
    ArrayList<TableColumn<Activity,?>> columns = Lists.newArrayList();
    columns.add(new IntegerEntityColumn<Activity>(RelationshipPath.from(Activity.class).toId(),
      "Activity ID",
      "The activity number",
      TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(Activity activity)
      {
        return activity.getActivityId();
      }

      @Override
      public Object cellAction(Activity activity)
      {
        return viewSelectedEntity();
      }

      @Override
      public boolean isCommandLink() { return true; }
    });
    columns.add(new VocabularyEntityColumn<Activity,String>(RelationshipPath.from(Activity.class).toProperty("activityType"),
      "Activity Type",
      "The type of the activity",
      TableColumn.UNGROUPED,
      new VocabularyConverter<String>(getActivityTypes()), getActivityTypes()) {
      @Override
      public String getCellValue(Activity activity)
      {
        return activity.getActivityTypeName();
      }
    });
    columns.add(new DateEntityColumn<Activity>(RelationshipPath.from(Activity.class).toProperty("datePerformed"),
      "Date Performed", "The date of the activity", TableColumn.UNGROUPED) {
      @Override
      protected LocalDate getDate(Activity activity) {
        return activity.getDateOfActivity();
      }
    });
    columns.add(new DateEntityColumn<Activity>(RelationshipPath.from(Activity.class).toProperty("dateCreated"),
      "Date Recorded", "The date the activity was recorded", TableColumn.UNGROUPED) {
      @Override
      protected LocalDate getDate(Activity activity)
      {
        return activity.getDateCreated().toLocalDate();
      }
    });
    columns.get(columns.size() - 1).setVisible(showAdminStatusFields());

    columns.add(new UserNameColumn<Activity,ScreensaverUser>(RelationshipPath.from(Activity.class).to(Activity.performedBy.getLeaf()),
      "Performed By", "The person that performed the activity", TableColumn.UNGROUPED, _userViewer) {
      @Override
      public ScreensaverUser getUser(Activity activity)
      {
        return (ScreensaverUser) activity.getPerformedBy();
      }
    });
    
    columns.add(new UserNameColumn<Activity,ScreeningRoomUser>(RelationshipPath.from(Activity.class).to(ServiceActivity.servicedUser),
      "Serviced User", "The user that was serviced by this activity", TableColumn.UNGROUPED, _userViewer) {
      @Override
      public ScreeningRoomUser getUser(Activity activity)
      {
        if (activity instanceof ServiceActivity) {
          return ((ServiceActivity) activity).getServicedUser();
        }
        return null;
      }
    });
    
    columns.addAll(buildLabActivityColumns());
    
    columns.add(new TextEntityColumn<Activity>(RelationshipPath.from(Activity.class).toProperty("comments"),
                                               "Comments",
                                               "Activity comments",
                                               TextColumn.UNGROUPED) {
      @Override
      public String getCellValue(Activity activity)
      {
        return activity.getComments();
      }
    });
    return columns;
  }

  private boolean showAdminStatusFields()
  {
    return isUserInRole(ScreensaverUserRole.SCREENS_ADMIN) ||
      isUserInRole(ScreensaverUserRole.READ_EVERYTHING_ADMIN);
  }

  public void searchActivitiesForScreen(final Screen screen)
  {
    setTitle("Activities for screen " + screen.getFacilityId());
    initialize(new InMemoryEntityDataModel<Activity,Integer,Activity>(new EntityDataFetcher<Activity,Integer>(Activity.class, _dao) {
      @Override
      public void addDomainRestrictions(HqlBuilder hql)
      {
        hql.from(getRootAlias(), LabActivity.Screen, "las");
        hql.from(getRootAlias(), ServiceActivity.servicedScreen, "sas");
        hql.where(
            new edu.harvard.med.screensaver.db.hqlbuilder.Predicate() {
                @Override
                public String toHql()
                {
                  return "las = " + screen.getScreenId() + " or " + " sas = " + screen.getScreenId();
                }
            }
        );
      }
    }));
  }

  @SuppressWarnings("unchecked")
  private List<? extends TableColumn<Activity,?>> buildLabActivityColumns()
  {
    List<TableColumn<Activity,?>> columns = Lists.newArrayList();
    IntegerEntityColumn<Activity> column =
      new IntegerEntityColumn<Activity>(RelationshipPath.from(Activity.class).toProperty("screenedExperimentalWellCount"),
                                           "Experimental Wells Screened (Library Screening)",
                                           "The number of experimental library wells that were screened during this activity (ignoring replicates)",
                                           TableColumn.UNGROUPED) {
      @Override
        public Integer getCellValue(Activity activity)
      { 
        if (activity instanceof LibraryScreening) {
          return ((LibraryScreening) activity).getScreenedExperimentalWellCount();
        }
        return null;
      }
    };
    column.setVisible(false);
    // TODO: reinstae ordering: 
    // columns.add(2, column);
  
    column = new IntegerEntityColumn<Activity>(CherryPickScreening.cherryPickRequest.castToSupertype(Activity.class),
      "Cherry Pick Request #", "The cherry pick request number, if applicable", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(Activity activity)
      { 
        if (activity instanceof LabActivity) {
          CherryPickRequest cherryPickRequest = getCherryPickRequest(activity);
          return cherryPickRequest == null ? null : cherryPickRequest.getCherryPickRequestNumber();
        }
        return null;
      }
  
      private CherryPickRequest getCherryPickRequest(Activity activity)
      {
        if (activity instanceof CherryPickLiquidTransfer) {
          return ((CherryPickLiquidTransfer) activity).getCherryPickRequest();
        }
        else if (activity instanceof CherryPickScreening) {
          return ((CherryPickScreening) activity).getCherryPickRequest();
        }
        return null;
      }
  
      @Override
      public Object cellAction(Activity activity)
      {
        return _cprViewer.viewEntity(getCherryPickRequest(activity));
      }
  
      @Override
      public boolean isCommandLink() { return true; }
    };
    column.addRelationshipPath(CherryPickLiquidTransfer.cherryPickAssayPlates.to(CherryPickAssayPlate.cherryPickRequest).castToSupertype(Activity.class));
    column.setVisible(false);
    // TODO: reinstate ordering
    //columns.add(2, column);
    
    Iterable<TableColumn<Screen,?>> screenColumns = Iterables.concat(_screensBrowser.buildScreenSummaryColumns(true),
                                                                     _screensBrowser.buildScreenAdminColumns());
    
    screenColumns = Iterables.filter(screenColumns,
                                     new Predicate<TableColumn<Screen,?>>() { public boolean apply(TableColumn<Screen,?> c) { return !!!c.getName().equals("Date Of Last Activity"); } });
    List<TableColumn<Activity,?>> labActivityScreenColumns = Lists.newArrayList(Iterables.transform(screenColumns,
                                                                                                    new Function<TableColumn<Screen,?>,TableColumn<Activity,?>>() {
                                                                                                      public TableColumn<Activity,?> apply(TableColumn<Screen,?> delegateColumn)
      { 
        RelatedEntityColumn<Activity,Screen,Object> column = new RelatedEntityColumn<Activity,Screen,Object>(Screen.class, LabActivity.Screen.castToSupertype(Activity.class), (TableColumn<Screen,Object>) delegateColumn, SCREEEN_COLUMN_GROUP)
        { 
          public Screen getRelatedEntity(Activity a)
          {
            if (a instanceof LabActivity) {
              return ((LabActivity) a).getScreen();
            }
            else if (a instanceof ServiceActivity) {
              // reload the activity as a service activity
              // thereby instantiate the serviced screen lazy connection
              a = _dao.findEntityById(ServiceActivity.class, a.getActivityId());
              return ((ServiceActivity) a).getServicedScreen();
            }
            return null;
          }
        };
        column.setVisible(false);
        return column;
      } 
    }));
    labActivityScreenColumns.get(0).setVisible(true);
    columns.addAll(labActivityScreenColumns);
  
    return columns;
  }

  protected Set<String> getActivityTypes()
  {
    return activityTypes;
  }
}
