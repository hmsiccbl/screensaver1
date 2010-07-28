// $HeadURL$
// $Id$
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import org.joda.time.LocalDate;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.db.datafetcher.AggregateDataFetcher;
import edu.harvard.med.screensaver.db.datafetcher.DataFetcher;
import edu.harvard.med.screensaver.db.datafetcher.EntityDataFetcher;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.model.Activity;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.screenresults.AssayPlate;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.PlateScreeningStatus;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.ui.libraries.LibraryViewer;
import edu.harvard.med.screensaver.ui.table.Criterion.Operator;
import edu.harvard.med.screensaver.ui.table.column.BooleanColumn;
import edu.harvard.med.screensaver.ui.table.column.DateColumn;
import edu.harvard.med.screensaver.ui.table.column.IntegerColumn;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;
import edu.harvard.med.screensaver.ui.table.column.TextColumn;
import edu.harvard.med.screensaver.ui.table.column.TextSetColumn;
import edu.harvard.med.screensaver.ui.table.model.InMemoryDataModel;

public class PlateScreeningStatusSearchResults extends EntityBasedEntitySearchResults<PlateScreeningStatus,Integer>
{
  private GenericEntityDAO _dao;
  private LibrariesDAO _librariesDao;
  private LibraryViewer _libraryViewer;

  private Screen _screen;
  private LibraryScreening _libraryScreening;
  protected ActivitySearchResults<Activity> _activitesBrowser;


  protected PlateScreeningStatusSearchResults() {}
  
  public PlateScreeningStatusSearchResults(GenericEntityDAO dao,
                                           LibrariesDAO librariesDao,
                                           LibraryViewer libraryViewer,
                                           ActivitySearchResults<Activity> activitiesBrowser)
  {
    _dao = dao;
    _librariesDao = librariesDao;
    _libraryViewer = libraryViewer;
    _activitesBrowser = activitiesBrowser;
  }

  @Override
  public void searchAll()
  {
    // TODO 
  }
  
  public void searchPlates(Set<Plate> plates)
  {
    // TODO
  }
  
  public void searchPlatesScreenedByScreen(Screen screen)
  {
    _screen = screen;
    
    EntityDataFetcher<AssayPlate,Integer> assayPlateDataFetcher = 
      new EntityDataFetcher<AssayPlate,Integer>(AssayPlate.class, _dao) { 
      @Override
        public void addDomainRestrictions(HqlBuilder hql)
      {
        hql.
        from(Screen.class, "s").
        from("s", Screen.assayPlates.getPath(), "ap").
        where("s", _screen).
        where("ap", Operator.EQUAL, getRootAlias());
      }
    };
    initialize(new InMemoryDataModel<PlateScreeningStatus>(buildAggregateDataFetcher(assayPlateDataFetcher)));
  }

  public void searchPlatesScreenedByLibraryScreening(LibraryScreening libraryScreening)
  {
    _libraryScreening = libraryScreening;
    EntityDataFetcher<AssayPlate,Integer> assayPlateDataFetcher = 
      new EntityDataFetcher<AssayPlate,Integer>(AssayPlate.class, _dao) { 
      @Override
        public void addDomainRestrictions(HqlBuilder hql)
      {
        hql.
        from(LibraryScreening.class, "ls").
        from("ls", LibraryScreening.assayPlatesScreened.getPath(), "ap").
        where("ls", _libraryScreening).
        where("ap", Operator.EQUAL, getRootAlias());
      }
    };
    initialize(new InMemoryDataModel<PlateScreeningStatus>(buildAggregateDataFetcher(assayPlateDataFetcher)));
  }

  private DataFetcher<PlateScreeningStatus,Integer,PropertyPath<PlateScreeningStatus>> buildAggregateDataFetcher(EntityDataFetcher<AssayPlate,Integer> assayPlateDataFetcher)
  {
    AggregateDataFetcher<PlateScreeningStatus,Integer,AssayPlate,Integer> aggregateDataFetcher = 
      new AggregateDataFetcher<PlateScreeningStatus,Integer,AssayPlate,Integer>(PlateScreeningStatus.class, _dao, assayPlateDataFetcher) {
      @Override
      protected SortedSet<PlateScreeningStatus> aggregateData(List<AssayPlate> assayPlates)
      {
        SortedSet<PlateScreeningStatus> result = Sets.newTreeSet();
        ListMultimap<Integer,AssayPlate> map = Multimaps.index(assayPlates, AssayPlate.ToPlateNumber);
        for (Integer plateNumber : map.keys()) {
          result.add(new PlateScreeningStatus(plateNumber, 
                                              _librariesDao.findLibraryWithPlate(plateNumber), 
                                              Sets.newHashSet(map.get(plateNumber))));
        }

        return result; 
      } 
    };
    
    assayPlateDataFetcher.setPropertiesToFetch(Lists.newArrayList(AssayPlate.libraryScreening.toFullEntity(),
                                                                  AssayPlate.screenResultDataLoading.toFullEntity(),
                                                                  AssayPlate.plateScreened.to(Plate.copy).toFullEntity()));
    
    return aggregateDataFetcher;
  }

  @Override
  protected List<TableColumn<PlateScreeningStatus,?>> buildColumns()
  {
    
    List<TableColumn<PlateScreeningStatus,?>> columns = Lists.newArrayList();
    
    columns.add(new IntegerColumn<PlateScreeningStatus>(
      "Plate",
      "Plate number",
      TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(PlateScreeningStatus plate)
      {
        return plate.getPlateNumber();
      }
    });
    
    columns.add(new TextColumn<PlateScreeningStatus>(
      "Library",
      "The library containing the plate",
      TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(PlateScreeningStatus plate)
      {
        return plate.getLibrary().getLibraryName();
      }

      @Override
      public boolean isCommandLink() { return true; }

      @Override
      public Object cellAction(PlateScreeningStatus plate)
      {
        return _libraryViewer.viewEntity(plate.getLibrary());
      }
    });

    columns.add(new IntegerColumn<PlateScreeningStatus>(
      "Screening Count",
      "The number of times this plate has been screened, ignoring replicates",
      TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(PlateScreeningStatus plate)
      {
        return plate.getScreeningCount();
      }
      
      @Override public boolean isCommandLink() { return true; }
      
      @Override
      public Object cellAction(PlateScreeningStatus plate)
      {
        _activitesBrowser.searchActivities(Sets.<Activity>newHashSet(plate.getLibraryScreenings()));
        return BROWSE_ACTIVITIES;
      }
    });
    
    columns.add(new IntegerColumn<PlateScreeningStatus>(
      "Assay Plate Count",
      "The number of assay plates that have been screened, including replicates and re-screenings",
      TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(PlateScreeningStatus plate)
      {
        return plate.getAssayPlateCount();
      }
    });
    
    columns.add(new TextSetColumn<PlateScreeningStatus>(
      "Copies",
      "The library copies that were used to screen the plate",
      TableColumn.UNGROUPED) {
        @Override
        public Set<String> getCellValue(PlateScreeningStatus plate)
        {
          return plate.getCopiesScreened();
        }
    });

    columns.add(new DateColumn<PlateScreeningStatus>(
      "Last Date Screened",
      "The date the plate was last screened",
      TableColumn.UNGROUPED) {
      @Override
      public LocalDate getDate(PlateScreeningStatus plate)
      {
        return plate.getLastDateScreened();
      }
    });
    
    columns.add(new DateColumn<PlateScreeningStatus>(
      "First Date Screened",
      "The date the plate was first screened",
      TableColumn.UNGROUPED) {
      @Override
      public LocalDate getDate(PlateScreeningStatus plate)
      {
        return plate.getFirstDateScreened();
      }
    });
    
    columns.add(new BooleanColumn<PlateScreeningStatus>(
      "Data Loaded",
      "Indicates whether screen result data has been loaded for the plate",
      TableColumn.UNGROUPED) {
      @Override
      public Boolean getCellValue(PlateScreeningStatus plate)
      {
        return plate.isDataLoaded();
      }
    });
    
    columns.add(new DateColumn<PlateScreeningStatus>(
      "Date Data Loaded",
      "The date the data was loaded for the plate",
      TableColumn.UNGROUPED) {
      @Override
      public LocalDate getDate(PlateScreeningStatus plate)
      {
        return plate.getLastDateDataLoaded();
      }
    });
    
    return columns;
  }
  
}
