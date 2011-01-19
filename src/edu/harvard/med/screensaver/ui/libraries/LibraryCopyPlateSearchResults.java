// $HeadURL:
// http://forge.abcd.harvard.edu/svn/screensaver/branches/iccbl/library-copy-mgmt/src/edu/harvard/med/screensaver/ui/searchresults/PlateSearchResults.java
// $
// $Id$
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.db.Query;
import edu.harvard.med.screensaver.db.datafetcher.DataFetcherUtil;
import edu.harvard.med.screensaver.db.datafetcher.EntityDataFetcher;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.model.Activity;
import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.MolarConcentration;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.libraries.PlateLocation;
import edu.harvard.med.screensaver.model.libraries.PlateStatus;
import edu.harvard.med.screensaver.model.libraries.PlateType;
import edu.harvard.med.screensaver.model.libraries.ScreeningStatistics;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.screenresults.AssayPlate;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.activities.ActivitySearchResults;
import edu.harvard.med.screensaver.ui.arch.datatable.Criterion.Operator;
import edu.harvard.med.screensaver.ui.arch.datatable.column.DateColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.DateTimeColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.IntegerColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TableColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.DateEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.EnumEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.FixedDecimalEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.HasFetchPaths;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.IntegerEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.MolarConcentrationEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.TextEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.UserNameColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.VolumeEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.model.InMemoryEntityDataModel;
import edu.harvard.med.screensaver.ui.arch.searchresults.EntityBasedEntitySearchResults;
import edu.harvard.med.screensaver.ui.arch.searchresults.EntityUpdateSearchResults;
import edu.harvard.med.screensaver.ui.arch.view.aspects.UICommand;

/**
 * A SearchResult that provides detailed information for library copy {@link Plate}s. Each row represents one physical
 * plate.
 * 
 * @author atolopko
 */
public class LibraryCopyPlateSearchResults extends EntityBasedEntitySearchResults<Plate,Integer>
{
  private static final Logger log = Logger.getLogger(LibraryCopyPlateFinder.class);

  private static final ScreeningStatistics NullScreeningStatistics = new ScreeningStatistics();

  private GenericEntityDAO _dao;
  private LibrariesDAO _librariesDao;
  private LibraryViewer _libraryViewer;
  private LibraryCopyViewer _libraryCopyViewer;
  private ActivitySearchResults<Activity> _activitiesBrowser;
  private LibraryCopyPlateBatchEditor _libraryCopyPlateBatchEditor;
  private String _reviewMessage;
  private EntityUpdateSearchResults<Plate,Integer> _entityUpdateHistoryBrowser;

  protected LibraryCopyPlateSearchResults()
  {}

  public LibraryCopyPlateSearchResults(GenericEntityDAO dao,
                                       LibrariesDAO librariesDao,
                                       LibraryViewer libraryViewer,
                                       LibraryCopyViewer libraryCopyViewer,
                                       ActivitySearchResults<Activity> activitiesBrowser,
                                       LibraryCopyPlateBatchEditor libraryCopyPlateBatchEditor)
  {
    _dao = dao;
    _librariesDao = librariesDao;
    _libraryViewer = libraryViewer;
    _libraryCopyViewer = libraryCopyViewer;
    _activitiesBrowser = activitiesBrowser;
    _libraryCopyPlateBatchEditor = libraryCopyPlateBatchEditor;
    setEditingRole(ScreensaverUserRole.LIBRARY_COPIES_ADMIN);
  }

  private void addLibraryTypeRestriction(HqlBuilder hql, String rootAlias)
  {
    hql.from(rootAlias, "copy", "c").from("c", "library", "l").
      whereIn("l", "libraryType", LibrarySearchResults.LIBRARY_TYPES_TO_DISPLAY);
  }

  @Override
  public void searchAll()
  {
    _mode = Mode.ALL;
    setTitle("Library Copy Plates Browser");
    EntityDataFetcher<Plate,Integer> plateDataFetcher = new EntityDataFetcher<Plate,Integer>(Plate.class, _dao) {
      @Override
      public void addDomainRestrictions(HqlBuilder hql)
      {
        addLibraryTypeRestriction(hql, getRootAlias());
      }
    };

    
    initialize(plateDataFetcher);
    setTableFilterMode(true);
  }

  public void searchPlatesForCopy(final Copy copy)
  {
    _mode = Mode.COPY;
    setTitle("Library Copy Plates for copy " + copy.getLibrary().getLibraryName() + ", copy " + copy.getName());
    EntityDataFetcher<Plate,Integer> plateDataFetcher =
      new EntityDataFetcher<Plate,Integer>(Plate.class, _dao) {
      @Override
        public void addDomainRestrictions(HqlBuilder hql)
      {
        DataFetcherUtil.addDomainRestrictions(hql, Plate.copy, copy, getRootAlias());
        addLibraryTypeRestriction(hql, getRootAlias());
      }
      };
    initialize(plateDataFetcher);
  }

  public void searchPlatesForLibrary(final Library library)
  {
    _mode = Mode.LIBRARY;
    setTitle("Library Copy Plates for library " + library.getLibraryName());
    EntityDataFetcher<Plate,Integer> plateDataFetcher =
      new EntityDataFetcher<Plate,Integer>(Plate.class, _dao) {
        @Override
        public void addDomainRestrictions(HqlBuilder hql)
        {
          DataFetcherUtil.addDomainRestrictions(hql, Plate.copy.to(Copy.library), library, getRootAlias());
          addLibraryTypeRestriction(hql, getRootAlias());
        }
      };
    initialize(plateDataFetcher);
  }

  public void searchForPlates(String searchDescription, final Set<Integer> plateIds)
  {
    _mode = Mode.SET;
    setTitle("Library Copy Plates search for " + searchDescription);
    EntityDataFetcher<Plate,Integer> plateDataFetcher =
      new EntityDataFetcher<Plate,Integer>(Plate.class, _dao) {
      @Override
        public void addDomainRestrictions(HqlBuilder hql)
        {
          DataFetcherUtil.addDomainRestrictions(hql, getRootAlias(), plateIds);
          addLibraryTypeRestriction(hql, getRootAlias());
        }
      };
    initialize(plateDataFetcher);
  }

  private Set<TableColumn<Plate,?>> screeningStatisticColumns;

  private enum Mode {
    ALL,
    SET,
    COPY,
    LIBRARY,
  };

  private Mode _mode;

  private void initialize(EntityDataFetcher<Plate,Integer> plateDataFetcher)
  {
    initialize(new InMemoryEntityDataModel<Plate,Integer>(plateDataFetcher) {
      private Predicate<TableColumn<Plate,?>> isScreeningStatisticsColumnWithCriteria =
        new Predicate<TableColumn<Plate,?>>() {
          public boolean apply(TableColumn<Plate,?> column)
          {
            return screeningStatisticColumns.contains(column) && column.hasCriteria();
          }
        };

      @Override
      public void fetch(List<? extends TableColumn<Plate,?>> columns)
      {
        // add fetch properties that are needed for review message generation
        if (columns.size() > 0) {
          ((HasFetchPaths<Plate>) columns.get(0)).addRelationshipPath(Plate.location);
          ((HasFetchPaths<Plate>) columns.get(0)).addRelationshipPath(Plate.copy.to(Copy.library));
        }

        super.fetch(columns);
      }

      @Override
      public void filter(List<? extends TableColumn<Plate,?>> columns)
      {
        if (_mode == Mode.ALL && !hasCriteriaDefined(getColumnManager().getAllColumns())) {
          setWrappedData(Collections.EMPTY_LIST);
        }
        else {
          if (Iterables.any(columns, isScreeningStatisticsColumnWithCriteria)) {
            // calculate for all rows, prior to filtering 
            calculateScreeningStatistics(_unfilteredData);
            super.filter(columns);
          }
          else {
            // only calculate for filtered rows 
            super.filter(columns);
            calculateScreeningStatistics(getData());
          }
        }
        updateReviewMessage();
      }

      private boolean hasCriteriaDefined(List<? extends TableColumn<?,?>> columns)
      {
        for (TableColumn<?,?> column : columns) {
          if (column.hasCriteria()) return true;
        }
        return false;
      }

      private void calculateScreeningStatistics(Iterable<Plate> plates)
      {
        List<Plate> platesWithoutStatistics = Lists.newArrayList(Iterables.filter(plates,
                                                                                  new Predicate<Plate>() {
                                                                                    public boolean apply(Plate plate)
          {
            return plate.getScreeningStatistics() == null;
          }
                                                                                  }));
        for (List<Plate> partition : Lists.partition(platesWithoutStatistics, 1024)) {
          doCalculateScreeningStatistics(partition);
        }
      }

      private void doCalculateScreeningStatistics(List<Plate> plates)
      {
        // get plate-based statistics: screening_count, assay_plate_count, first/last date screened, data_loading_count
        final HqlBuilder builder = new HqlBuilder();

        Map<Integer,Plate> result = Maps.newHashMap();
        for (Plate p : plates) {
          p.setScreeningStatistics(new ScreeningStatistics());
          result.put(p.getEntityId(), p);
        }

        builder.from(Plate.class, "p")
               .from(AssayPlate.class, "ap")
               .from("ap", AssayPlate.libraryScreening.getPath(), "ls")
               .from("ap", AssayPlate.screenResultDataLoading.getPath(), "dl")
               .whereIn("p", "id", result.keySet())
               .where("ap", AssayPlate.plateScreened.getLeaf(), Operator.EQUAL, "p", "id")
               .groupBy("p", "id")
               .select("p", "id")
               .selectExpression("count(distinct ls)")
               .selectExpression("count(distinct ap)")
               .selectExpression("count(distinct dl)")
               .selectExpression("min(dl.dateOfActivity)")
               .selectExpression("max(dl.dateOfActivity)")
               .selectExpression("min(ls.dateOfActivity)")
               .selectExpression("max(ls.dateOfActivity)");

        List<Object> results = _dao.runQuery(new Query() {
          @Override
          public List<Object> execute(Session session)
            {
              return builder.toQuery(session, true).list();
            }
        });
        for (Object o : results) {
          int i = 0;
          Integer plateId = (Integer) ((Object[]) o)[i++];
          ScreeningStatistics css = new ScreeningStatistics();
          result.get(plateId).setScreeningStatistics(css);
          css.setPlateCount(1);
          css.setScreeningCount(((Long) ((Object[]) o)[i++]).intValue());
          css.setAssayPlateCount(((Long) ((Object[]) o)[i++]).intValue());
          css.setDataLoadingCount(((Long) ((Object[]) o)[i++]).intValue());
          css.setFirstDateDataLoaded(((LocalDate) ((Object[]) o)[i++]));
          css.setLastDateDataLoaded(((LocalDate) ((Object[]) o)[i++]));
          css.setFirstDateScreened(((LocalDate) ((Object[]) o)[i++]));
          css.setLastDateScreened(((LocalDate) ((Object[]) o)[i++]));
        }

        // calculate plate_screening_count - the total number of times individual plates from this copy have been screened, ignoring replicates)
        final HqlBuilder builder1 = new HqlBuilder();
        builder1.from(LibraryScreening.class, "ls")
               .from("ls", LibraryScreening.assayPlatesScreened.getPath(), "ap")
               .from("ap", AssayPlate.plateScreened.getPath(), "p")
               .from("p", Plate.copy.getPath(), "c")
               .whereIn("p", "id", result.keySet())
               .where("ap", "replicateOrdinal", Operator.EQUAL, 0)
               .groupBy("p", "id");
        builder1.select("p", "id");
        builder1.selectExpression("count(*)");

        results = _dao.runQuery(new Query() {
          @Override
          public List<Object> execute(Session session)
            {
              return builder1.toQuery(session, true).list();
            }
        });

        for (Object o : results) {
          Integer plateId = (Integer) ((Object[]) o)[0];
          Integer count = ((Long) ((Object[]) o)[1]).intValue();

          ScreeningStatistics css = result.get(plateId).getScreeningStatistics();
          css.setPlateScreeningCount(css.getPlateScreeningCount() + count);
        }
      }
    });
    _libraryCopyPlateBatchEditor.initialize();
  }

  @Override
  protected List<TableColumn<Plate,?>> buildColumns()
  {
    List<TableColumn<Plate,?>> columns = Lists.newArrayList();
    screeningStatisticColumns = Sets.newHashSet();

    columns.add(new IntegerEntityColumn<Plate>(PropertyPath.from(Plate.class).toProperty("plateNumber"),
                                               "Plate",
                                               "Plate number",
                                               TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(Plate plate)
      {
        return plate.getPlateNumber();
      }
    });

    columns.add(new TextEntityColumn<Plate>(Plate.copy.toProperty("name"),
                                            "Copy",
                                            "The library copy containing the plate",
                                            TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(Plate plate)
      {
        return plate.getCopy().getName();
      }

      @Override
      public boolean isCommandLink()
      {
        return true;
      }

      @Override
      public Object cellAction(Plate plate)
      {
        return _libraryCopyViewer.viewEntity(plate.getCopy());

      }
    });
    Iterables.getLast(columns).setVisible(_mode != Mode.COPY);

    columns.add(new EnumEntityColumn<Plate,CopyUsageType>(Plate.copy.toProperty("usageType"),
                                                          "Copy Usage Type",
                                                          "The usage type of the copy containing the plate",
                                                          TableColumn.UNGROUPED,
                                                          CopyUsageType.values()) {
      @Override
      public CopyUsageType getCellValue(Plate plate)
      {
        return plate.getCopy().getUsageType();
      }
    });
    Iterables.getLast(columns).setVisible(false);

    columns.add(new TextEntityColumn<Plate>(Plate.copy.to(Copy.library).toProperty("libraryName"),
                                            "Library",
                                            "The library containing the plate",
                                            TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(Plate plate)
      {
        return plate.getCopy().getLibrary().getLibraryName();
      }

      @Override
      public boolean isCommandLink()
      {
        return true;
      }

      @Override
      public Object cellAction(Plate plate)
      {
        return _libraryViewer.viewEntity(plate.getCopy().getLibrary());

      }
    });
    Iterables.getLast(columns).setVisible(_mode == Mode.ALL);

    columns.add(new EnumEntityColumn<Plate,ScreenType>(Plate.copy.to(Copy.library).toProperty("screenType"),
                                                       "Screen Type",
                                                       "'RNAi' or 'Small Molecule'",
                                                       TableColumn.UNGROUPED,
                                                       ScreenType.values()) {
      @Override
      public ScreenType getCellValue(Plate plate)
      {
        return plate.getCopy().getLibrary().getScreenType();
      }
    });
    Iterables.getLast(columns).setVisible(false);

    columns.add(new EnumEntityColumn<Plate,PlateStatus>(PropertyPath.from(Plate.class).toProperty("status"),
                                                        "Status",
                                                        "The plate status",
                                                        TableColumn.UNGROUPED,
                                                        PlateStatus.values()) {
      @Override
      public PlateStatus getCellValue(Plate plate)
      {
        return plate.getStatus();
      }
    });
    
    columns.add(new DateEntityColumn<Plate>(PropertyPath.from(Plate.class).to(Plate.updateActivities).to(Activity.dateOfActivity),
      "Status Date",
      "The date on which the status took effect",
      TableColumn.UNGROUPED) {
      @Override
      protected LocalDate getDate(Plate plate)
      {
        // note: we call getLastRecordedUpdateActivityOfType() instead of getLastUpdateActivityOfType(), as PlateBatchUpdater may
        // have created an status update activity with an activity date that is more recent than the current activity's status date
        return plate.getLastRecordedUpdateActivityOfType(AdministrativeActivityType.PLATE_STATUS_UPDATE).getDateOfActivity();
      }
    });

    columns.add(new UserNameColumn<Plate,ScreensaverUser>(PropertyPath.from(Plate.class).to(Plate.updateActivities).to(Activity.performedBy),
                                                          "Status Change Performed By",
                                                          "The person that performed the activity appropriate for the plate status",
                                                          TableColumn.UNGROUPED,
                                                          null/* TODO */) {
      @Override
      protected ScreensaverUser getUser(Plate plate)
      {
        return plate.getLastUpdateActivityOfType(AdministrativeActivityType.PLATE_STATUS_UPDATE).getPerformedBy();
      }
    });
    Iterables.getLast(columns).setVisible(false);

    columns.add(new TextEntityColumn<Plate>(Plate.location.toProperty("room"),
      "Room",
      "The room where the plate is stored",
      TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(Plate plate)
      {
        if (plate.getLocation() == null) {
          return null;
        }
        return plate.getLocation().getRoom();
      }
    });

    columns.add(new TextEntityColumn<Plate>(Plate.location.toProperty("freezer"),
                                            "Freezer",
                                            "The freezer where the plate is stored",
                                            TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(Plate plate)
      {
        if (plate.getLocation() == null) {
          return null;
        }
        return plate.getLocation().getFreezer();
      }
    });

    columns.add(new TextEntityColumn<Plate>(Plate.location.toProperty("shelf"),
                                            "Shelf",
                                            "The freezer shelf upon which plate is stored",
                                            TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(Plate plate)
      {
        if (plate.getLocation() == null) {
          return null;
        }
        return plate.getLocation().getShelf();
      }
    });

    columns.add(new TextEntityColumn<Plate>(Plate.location.toProperty("bin"),
                                            "Bin",
                                            "The bin in which the plate is stored",
                                            TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(Plate plate)
      {
        if (plate.getLocation() == null) {
          return null;
        }
        return plate.getLocation().getBin();
      }
    });

    columns.add(new EnumEntityColumn<Plate,PlateType>(PropertyPath.from(Plate.class).toProperty("plateType"),
                                                      "Plate Type",
                                                      "The plate type",
                                                      TableColumn.UNGROUPED,
                                                      PlateType.values()) {
      @Override
      public PlateType getCellValue(Plate plate)
      {
        return plate.getPlateType();
      }
    });

    columns.add(new VolumeEntityColumn<Plate>(PropertyPath.from(Plate.class).toProperty("wellVolume"),
                                              "Initial Well Volume",
                                              "The volume of each well of the plate when it was created",
                                              TableColumn.UNGROUPED) {
      @Override
      public Volume getCellValue(Plate plate)
      {
        return plate.getWellVolume();
      }
    });

    columns.add(new MolarConcentrationEntityColumn<Plate>(PropertyPath.from(Plate.class).toProperty("molarConcentration"),
                                                          "Molar Concentration",
                                                          "The molar concentration of each well of the plate when it was created",
                                                          TableColumn.UNGROUPED) {
      @Override
      public MolarConcentration getCellValue(Plate plate)
      {
        return plate.getMolarConcentration();
      }
    });

    columns.add(new FixedDecimalEntityColumn<Plate>(PropertyPath.from(Plate.class).toProperty("mgMlConcentration"),
                                                    "Concentration (mg/mL)",
                                                    "The mg/mL concentration of each well of the plate when it was created",
                                                    TableColumn.UNGROUPED) {
      @Override
      public BigDecimal getCellValue(Plate plate)
      {
        return plate.getMgMlConcentration();
      }
    });

    columns.add(new DateEntityColumn<Plate>(PropertyPath.from(Plate.class).to(Plate.updateActivities).to(Activity.dateOfActivity),
                                            "Location Transfer Date",
                                            "The last time the plate was transfered to a new location",
                                            TableColumn.UNGROUPED) {
      @Override
      protected LocalDate getDate(Plate plate)
      {
        return plate.getLastUpdateActivityOfType(AdministrativeActivityType.PLATE_LOCATION_TRANSFER).getDateOfActivity();
      }
    });
    Iterables.getLast(columns).setVisible(false);

    columns.add(new UserNameColumn<Plate,ScreensaverUser>(PropertyPath.from(Plate.class).to(Plate.updateActivities).to(Activity.performedBy),
                                                          "Location Transfer Performed By ",
                                                          "The person that transfered the plate to a new location",
                                                          TableColumn.UNGROUPED,
                                                          null/* TODO */) {
      @Override
      protected ScreensaverUser getUser(Plate plate)
      {
        return plate.getLastUpdateActivityOfType(AdministrativeActivityType.PLATE_LOCATION_TRANSFER).getPerformedBy();
      }
    });
    Iterables.getLast(columns).setVisible(false);

    columns.add(new TextEntityColumn<Plate>(PropertyPath.from(Plate.class).toProperty("facilityId"),
                                            "Facility ID",
                                            "The identifier used by the facility to uniquely identify the plate",
                                            TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(Plate plate)
      {
        return plate.getFacilityId();
      }
    });

    columns.add(new DateEntityColumn<Plate>(Plate.platedActivity.toProperty("dateOfActivity"),
                                            "Date Plated",
                                            "The date the plate was created",
                                            TableColumn.UNGROUPED) {
      @Override
      protected LocalDate getDate(Plate plate)
      {
        return plate.getPlatedActivity() == null ? null : plate.getPlatedActivity().getDateOfActivity();
      }
    });

    columns.add(new DateEntityColumn<Plate>(Plate.retiredActivity.toProperty("dateOfActivity"),
                                            "Date Retired",
                                            "The date the plate was retired",
                                            TableColumn.UNGROUPED) {
      @Override
      protected LocalDate getDate(Plate plate)
    {
      return plate.getRetiredActivity() == null ? null : plate.getRetiredActivity().getDateOfActivity();
    }
    });

    columns.add(new IntegerColumn<Plate>("Assay Plate Count", "The number of assay plates screened for this plate", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(Plate plate)
      {
        return getNullSafeScreeningStatistics(plate).getAssayPlateCount();
      }
    });
    screeningStatisticColumns.add(Iterables.getLast(columns));

    columns.add(new IntegerColumn<Plate>("Screening Count", "The total number of times this plate has been screened, ignoring replicates",
                                         TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(Plate plate)
      {
        return getNullSafeScreeningStatistics(plate).getScreeningCount();
      }

      @Override
      public boolean isCommandLink()
      {
        return true;
      }

      @Override
      public Object cellAction(Plate plate)
      {
        _activitiesBrowser.searchLibraryScreeningActivitiesForPlate(plate);
        return BROWSE_ACTIVITIES;
      }
    });
    screeningStatisticColumns.add(Iterables.getLast(columns));

    columns.add(new DateColumn<Plate>("First Date Screened", "The date the copy was first screened", TableColumn.UNGROUPED) {
      @Override
      public LocalDate getDate(Plate plate)
      {
        return getNullSafeScreeningStatistics(plate).getFirstDateScreened();
      }
    });
    screeningStatisticColumns.add(Iterables.getLast(columns));

    columns.add(new DateColumn<Plate>("Last Date Screened", "The date the copy was last screened", TableColumn.UNGROUPED) {
      @Override
      public LocalDate getDate(Plate plate)
      {
        return getNullSafeScreeningStatistics(plate).getLastDateScreened();
      }
    });
    screeningStatisticColumns.add(Iterables.getLast(columns));

    columns.add(new IntegerColumn<Plate>("Data Loading Count", "The number of screen results loaded for screens of this copy", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(Plate plate)
      {
        return getNullSafeScreeningStatistics(plate).getDataLoadingCount();
      }
    });
    screeningStatisticColumns.add(Iterables.getLast(columns));

    columns.add(new DateColumn<Plate>("First Date Data Loaded", "The date of the first screen result data loading activity", TableColumn.UNGROUPED) {
      @Override
      public LocalDate getDate(Plate plate)
      {
        return getNullSafeScreeningStatistics(plate).getFirstDateDataLoaded();
      }
    });
    screeningStatisticColumns.add(Iterables.getLast(columns));
    Iterables.getLast(columns).setVisible(false);

    columns.add(new DateColumn<Plate>("Last Date Data Loaded", "The date of the last screen result data loading activity", TableColumn.UNGROUPED) {
      @Override
      public LocalDate getDate(Plate plate)
      {
        return getNullSafeScreeningStatistics(plate).getLastDateDataLoaded();
      }
    });
    screeningStatisticColumns.add(Iterables.getLast(columns));
    Iterables.getLast(columns).setVisible(false);

    columns.add(new DateTimeColumn<Plate>("Last Updated", "The date on which the plate's most recent administrative update was recorded", TableColumn.UNGROUPED) {
      @Override
      public DateTime getDateTime(Plate plate)
      {
        SortedSet<AdministrativeActivity> activities = plate.getUpdateActivities();
        if (activities.isEmpty()) {
          return null;
        }
        // TODO: should order by dateCreated, rather than dateOfActivity
        return activities.last().getDateCreated();
      }

      @Override
      public boolean isCommandLink()
      {
        return true;
      }

      @Override
      public Object cellAction(Plate plate)
      {
        _entityUpdateHistoryBrowser.searchForParentEntity(plate);
        return BROWSE_ENTITY_UPDATE_HISTORY;
      }
    });

    columns.add(new DateColumn<Plate>("Last Comment Date", "The date of the last comment added on this plate", TableColumn.UNGROUPED) {
      @Override
      public LocalDate getDate(Plate plate)
      {
        return plate.getLastUpdateActivityOfType(AdministrativeActivityType.COMMENT).getDateOfActivity();
      }

      @Override
      public boolean isCommandLink()
      {
        return true;
      }

      @Override
      public Object cellAction(Plate plate)
      {
        _entityUpdateHistoryBrowser.searchForParentEntity(plate);
        _entityUpdateHistoryBrowser.setTitle("Comments for " + plate); // TODO: need user-friendly toString(), see [#2560]
        ((TableColumn<AdministrativeActivity,AdministrativeActivityType>) _entityUpdateHistoryBrowser.getColumnManager().getColumn("Update Type")).
          resetCriteria().setOperatorAndValue(Operator.EQUAL, AdministrativeActivityType.COMMENT);
        return BROWSE_ENTITY_UPDATE_HISTORY;
      }
    });

    columns.add(new TextEntityColumn<Plate>(PropertyPath.from(Plate.class).to(Plate.updateActivities).toProperty("comments"),
                                            "Last Comment",
                                            "Last Comment",
                                            TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(Plate plate)
      {
        return plate.getLastUpdateActivityOfType(AdministrativeActivityType.COMMENT).getComments();
      }
    });
    Iterables.getLast(columns).setVisible(false);

    return columns;
  }

  public ScreeningStatistics getNullSafeScreeningStatistics(Plate plate)
  {
    ScreeningStatistics screeningStatistics = plate.getScreeningStatistics();
    if (screeningStatistics == null) {
      return NullScreeningStatistics;
    }
    return screeningStatistics;
  }

  //  @Override
  //  @Transactional
  //  public void doSave()
  //  {
  //    Iterator<Plate> rowIter = getDataTableModel().iterator();
  //    while (rowIter.hasNext()) {
  //     _dao.saveOrUpdateEntity(rowIter.next());
  //    }
  //  }

  public boolean isBatchEditable()
  {
    return getScreensaverUser().isUserInRole(getEditingRole());
  }

  @UICommand
  public String batchClear()
  {
    _libraryCopyPlateBatchEditor.initialize();
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  public String getReviewMessage()
  {
    return _reviewMessage;
  }
  
  private void updateReviewMessage()
  {
    if (!isBatchEditable() || getRowCount() == 0) {
      _reviewMessage = "";
    }
    Set<Plate> plates = Sets.newHashSet(getDataTableModel().iterator());
    int nLibraries = Sets.newHashSet(Iterables.transform(plates, Functions.compose(Copy.ToLibrary, Plate.ToCopy))).size();
    int nCopies = Sets.newHashSet(Iterables.transform(plates, Plate.ToCopy)).size();
    Set<PlateLocation> locations = Sets.newHashSet(Iterables.filter(Iterables.transform(plates, Plate.ToLocation), Predicates.notNull()));
    int nRooms = Sets.newHashSet(Iterables.transform(locations, PlateLocation.ToRoom)).size();
    int nFreezers = Sets.newHashSet(Iterables.transform(locations, PlateLocation.ToRoomFreezer)).size();
    int nShelves = Sets.newHashSet(Iterables.transform(locations, PlateLocation.ToRoomFreezerShelf)).size();
    StringBuilder msg = new StringBuilder("Updating ").append(getRowCount()).append(" plate");
    if (getRowCount() != 1) {
      msg.append('s');
    }
    msg.append(" from ");
    msg.append(nLibraries).append(" librar").append(nLibraries == 1 ? "y" : "ies");
    msg.append(" and ").append(nCopies).append(" cop").append(nCopies == 1 ? "y" : "ies");
    msg.append(" across ").append(locations.size()).append(" bin location").append(locations.size() == 1 ? "" : "s").append(" on ");
    msg.append(nShelves).append(" shel").append(nShelves == 1 ? "f" : "ves");
    msg.append(" in ").append(nFreezers).append(" freezer").append(nFreezers == 1 ? "" : "s");
    msg.append(" in ").append(nRooms).append(" room").append(nRooms == 1 ? "" : "s");
    msg.append(". Proceed?");
    _reviewMessage = msg.toString();
  }

  @UICommand
  public String batchUpdate()
  {
    if (isBatchEditable()) {
      Iterator<Plate> rowIter = getDataTableModel().iterator();
      try {
        _libraryCopyPlateBatchEditor.updatePlates(Sets.newHashSet(rowIter));
      }
      catch (Exception e) {
        showMessage("applicationError", e.getMessage());
      }
      // note: we reload()after success *or* failure, since we want the Plate entities to be reloaded in either case
      reload();

      _libraryCopyViewer.reload();
    }

    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  public LibraryCopyPlateBatchEditor getBatchEditor()
  {
    return _libraryCopyPlateBatchEditor;
  }

  public void setEntityUpdateHistoryBrowser(EntityUpdateSearchResults<Plate,Integer> entityUpdateHistoryBrowser)
  {
    _entityUpdateHistoryBrowser = entityUpdateHistoryBrowser;
  }

  public EntityUpdateSearchResults<Plate,Integer> getEntityUpdateHistoryBrowser()
  {
    return _entityUpdateHistoryBrowser;
  }
}
