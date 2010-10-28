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

package edu.harvard.med.screensaver.ui.searchresults;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.hibernate.Session;
import org.joda.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.db.Query;
import edu.harvard.med.screensaver.db.datafetcher.DataFetcherUtil;
import edu.harvard.med.screensaver.db.datafetcher.EntityDataFetcher;
import edu.harvard.med.screensaver.db.datafetcher.SetBasedDataFetcher;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.model.Activity;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.libraries.PlateType;
import edu.harvard.med.screensaver.model.libraries.ScreeningStatistics;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.screenresults.AssayPlate;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.libraries.LibraryCopyViewer;
import edu.harvard.med.screensaver.ui.libraries.LibraryViewer;
import edu.harvard.med.screensaver.ui.table.Criterion.Operator;
import edu.harvard.med.screensaver.ui.table.column.DateColumn;
import edu.harvard.med.screensaver.ui.table.column.IntegerColumn;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.DateEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.EnumEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.IntegerEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.TextEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.VolumeEntityColumn;
import edu.harvard.med.screensaver.ui.table.model.InMemoryDataModel;

/**
 * A SearchResult that provides detailed information for library copy {@link Plate}s. Each row represents one physical
 * plate.
 * 
 * @author atolopko
 */
public class LibraryCopyPlateSearchResults extends EntityBasedEntitySearchResults<Plate,Integer>
{
  private GenericEntityDAO _dao;
  private LibrariesDAO _librariesDao;
  private LibraryViewer _libraryViewer;
  private LibraryCopyViewer _libraryCopyViewer;
  private ActivitySearchResults<Activity> _activitiesBrowser;

  protected LibraryCopyPlateSearchResults()
  {}

  public LibraryCopyPlateSearchResults(GenericEntityDAO dao,
                                       LibrariesDAO librariesDao,
                                       LibraryViewer libraryViewer,
                                       LibraryCopyViewer libraryCopyViewer,
                                       ActivitySearchResults<Activity> activitiesBrowser)
  {
    _dao = dao;
    _librariesDao = librariesDao;
    _libraryViewer = libraryViewer;
    _libraryCopyViewer = libraryCopyViewer;
    _activitiesBrowser = activitiesBrowser;
    setEditingRole(ScreensaverUserRole.LIBRARY_COPIES_ADMIN);
  }

  @Override
  public void searchAll()
  {
    // TODO 
  }

  public void searchPlatesForCopy(final Copy copy)
  {
    setTitle("Library Copy Plates for copy " + copy.getLibrary().getLibraryName() + ", copy " + copy.getName());
    EntityDataFetcher<Plate,Integer> plateDataFetcher =
      new EntityDataFetcher<Plate,Integer>(Plate.class, _dao) {
      @Override
        public void addDomainRestrictions(HqlBuilder hql)
      {
        DataFetcherUtil.addDomainRestrictions(hql, Plate.copy, copy, getRootAlias());
      }
      };
    initialize(plateDataFetcher);
  }

  private void initialize(EntityDataFetcher<Plate,Integer> plateDataFetcher)
  {
    plateDataFetcher.setPropertiesToFetch(Lists.newArrayList(Plate.copy.to(Copy.library).toFullEntity()));
    List<Plate> plates = plateDataFetcher.fetchAllData();
    SortedSet<Plate> platesWithStatistics = calculateScreeningStatistics(plates);
    initialize(new InMemoryDataModel<Plate>(new SetBasedDataFetcher<Plate,Integer>(platesWithStatistics)));
  }

  private SortedSet<Plate> calculateScreeningStatistics(List<Plate> plates)
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
      ScreeningStatistics css = result.get(plateId).getScreeningStatistics();
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

    return Sets.newTreeSet(result.values());
  }

  @Override
  protected List<TableColumn<Plate,?>> buildColumns()
  {
    List<TableColumn<Plate,?>> columns = Lists.newArrayList();

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
      public boolean isCommandLink() { return true; }

      @Override
      public Object cellAction(Plate plate)
      {
        return _libraryViewer.viewEntity(plate.getCopy().getLibrary());

      }
    });
    Iterables.getLast(columns).setVisible(!isNested());

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
    Iterables.getLast(columns).setVisible(!isNested());

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

    columns.add(new TextEntityColumn<Plate>(PropertyPath.from(Plate.class).toProperty("location"),
                                            "Location",
                                            "The storage location of the plate",
                                            TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(Plate plate)
      {
        return plate.getLocation();
      }

      @Override
      public void setCellValue(Plate plate, String location)
      {
        plate.setLocation(location);
      }

      @Override
      public boolean isEditable()
      {
        return true;
      }
    });

    columns.add(new TextEntityColumn<Plate>(PropertyPath.from(Plate.class).toProperty("facilityId"),
                                            "Facility ID",
                                            "The identifier used by the facility to uniquely identify the plate",
                                            TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(Plate plate)
      {
        return plate.getFacilityId();
      }

      @Override
      public void setCellValue(Plate plate, String facilityId)
      {
        plate.setFacilityId(facilityId);
      }

      @Override
      public boolean isEditable()
      {
        return true;
      }
    });

    columns.add(new DateEntityColumn<Plate>(PropertyPath.from(Plate.class).toProperty("datePlated"),
                                            "Date Plated",
                                            "The date the plate was created",
                                            TableColumn.UNGROUPED) {
      @Override
      protected LocalDate getDate(Plate plate)
      {
        return plate.getDatePlated();
      }

      @Override
      public void setCellValue(Plate plate, LocalDate datePlated)
      {
        plate.setDatePlated(datePlated);
      }

      @Override
      public boolean isEditable()
      {
        return true;
      }
    });

    columns.add(new DateEntityColumn<Plate>(PropertyPath.from(Plate.class).toProperty("dateRetired"),
                                            "Date Retired",
                                            "The date the plate was retired",
                                            TableColumn.UNGROUPED) {
      @Override
      protected LocalDate getDate(Plate plate)
      {
        return plate.getDateRetired();
      }

      @Override
      public void setCellValue(Plate plate, LocalDate dateRetired)
      {
        plate.setDateRetired(dateRetired);
      }

      @Override
      public boolean isEditable()
      {
        return true;
      }
    });

    columns.add(new TextEntityColumn<Plate>(PropertyPath.from(Plate.class).toProperty("comments"),
                                            "Comments",
                                            "Comments for the plate",
                                            TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(Plate plate)
      {
        return plate.getComments();
      }

      @Override
      public void setCellValue(Plate plate, String comments)
      {
        plate.setComments(comments);
      }

      @Override
      public boolean isEditable()
      {
        return true;
      }
    });

    columns.add(new IntegerColumn<Plate>("Assay Plate Count", "The number of assay plates screened for this plate", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(Plate plate)
      {
        return plate.getScreeningStatistics().getAssayPlateCount();
      }
    });

    columns.add(new IntegerColumn<Plate>("Screening Count", "The total number of times this plate has been screened, ignoring replicates",
                                         TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(Plate plate)
      {
        return plate.getScreeningStatistics().getScreeningCount();
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

    columns.add(new DateColumn<Plate>("First Date Screened", "The date the copy was first screened", TableColumn.UNGROUPED) {
      @Override
      public LocalDate getDate(Plate plate)
      {
        return plate.getScreeningStatistics().getFirstDateScreened();
      }
    });

    columns.add(new DateColumn<Plate>("Last Date Screened", "The date the copy was last screened", TableColumn.UNGROUPED) {
      @Override
      public LocalDate getDate(Plate plate)
      {
        return plate.getScreeningStatistics().getLastDateScreened();
      }
    });

    columns.add(new IntegerColumn<Plate>("Data Loading Count", "The number of screen results loaded for screens of this copy", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(Plate plate)
      {
        return plate.getScreeningStatistics().getDataLoadingCount();
      }
    });

    columns.add(new DateColumn<Plate>("First Date Data Loaded", "The date of the first screen result data loading activity", TableColumn.UNGROUPED) {
      @Override
      public LocalDate getDate(Plate plate)
      {
        return plate.getScreeningStatistics().getFirstDateDataLoaded();
      }
    });
    Iterables.getLast(columns).setVisible(false);

    columns.add(new DateColumn<Plate>("Last Date Data Loaded", "The date of the last screen result data loading activity", TableColumn.UNGROUPED) {
      @Override
      public LocalDate getDate(Plate plate)
      {
        return plate.getScreeningStatistics().getLastDateDataLoaded();
      }
    });
    Iterables.getLast(columns).setVisible(false);

    return columns;
  }

  @Override
  @Transactional
  public void doSave()
  {
    final ScreensaverUser screensaverUser = getCurrentScreensaverUser().getScreensaverUser();
    if (!(screensaverUser instanceof AdministratorUser) ||
      !((AdministratorUser) screensaverUser).isUserInRole(ScreensaverUserRole.LIBRARY_COPIES_ADMIN)) {
      throw new BusinessRuleViolationException("only library copies administrators can edit library copy plates");
    }
    Iterator<Plate> rowIter = getDataTableModel().iterator();
    while (rowIter.hasNext()) {
      _dao.saveOrUpdateEntity(rowIter.next());
    }
  }
}
