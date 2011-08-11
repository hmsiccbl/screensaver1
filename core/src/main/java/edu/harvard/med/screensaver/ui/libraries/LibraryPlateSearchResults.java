// $HeadURL$
// $Id$
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.joda.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.db.datafetcher.SetBasedDataFetcher;
import edu.harvard.med.screensaver.model.activities.Activity;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryPlate;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.ui.activities.ActivitySearchResults;
import edu.harvard.med.screensaver.ui.arch.datatable.column.BooleanColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.DateColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.IntegerColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TableColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TextColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TextSetColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.model.InMemoryDataModel;
import edu.harvard.med.screensaver.ui.arch.searchresults.EntityBasedEntitySearchResults;

/**
 * A SearchResult that provides screening status and data loading summaries for a set of library plate numbers.
 * 
 * @author atolopko
 */
public class LibraryPlateSearchResults extends EntityBasedEntitySearchResults<LibraryPlate,Integer>
{
  private GenericEntityDAO _dao;
  private LibrariesDAO _librariesDao;
  private LibraryViewer _libraryViewer;

  private ActivitySearchResults _activitesBrowser;
  private LibraryCopySearchResults _libraryCopiesBrowser;


  protected LibraryPlateSearchResults() {}
  
  public LibraryPlateSearchResults(GenericEntityDAO dao,
                                   LibrariesDAO librariesDao,
                                   LibraryViewer libraryViewer,
                                   ActivitySearchResults activitiesBrowser,
                                   LibraryCopySearchResults copiesBrowser)
  {
    _dao = dao;
    _librariesDao = librariesDao;
    _libraryViewer = libraryViewer;
    _activitesBrowser = activitiesBrowser;
    _libraryCopiesBrowser = copiesBrowser;
  }

  @Override
  public void searchAll()
  {
    // TODO 
  }
  
  @Transactional
  public void searchLibraryPlatesByLibrary(Library library)
  {
    setTitle("Library Plates for library " + library.getLibraryName());
    library = _dao.reloadEntity(library);
    SortedSet<LibraryPlate> libraryPlates = library.getLibraryPlates();
    initialize(new InMemoryDataModel<LibraryPlate>(new SetBasedDataFetcher<LibraryPlate,Integer>(libraryPlates)));
  }

  @Transactional
  public void searchLibraryPlatesScreenedByScreen(Screen screen)
  {
    setTitle("Library Plates Screened by screen " + screen.getFacilityId());
    screen = _dao.reloadEntity(screen);
    SortedSet<LibraryPlate> libraryPlates = screen.getLibraryPlatesScreened();

    // HACK: update missing libraries in LibraryPlates
    for (LibraryPlate libraryPlate : libraryPlates) {
      if (libraryPlate.getLibrary() == null) {
        libraryPlate.setLibrary(_librariesDao.findLibraryWithPlate(libraryPlate.getPlateNumber()));
      }
    }

    initialize(new InMemoryDataModel<LibraryPlate>(new SetBasedDataFetcher<LibraryPlate,Integer>(libraryPlates)));
  }

  @Transactional
  public void searchLibraryPlatesScreenedByLibraryScreening(LibraryScreening libraryScreening)
  {
    setTitle("Library Plates Screened by library screening " + libraryScreening.getActivityId());
    libraryScreening = _dao.reloadEntity(libraryScreening);
    SortedSet<LibraryPlate> libraryPlates = libraryScreening.getLibraryPlatesScreened();
    initialize(new InMemoryDataModel<LibraryPlate>(new SetBasedDataFetcher<LibraryPlate,Integer>(libraryPlates)));
  }

  @Override
  protected List<TableColumn<LibraryPlate,?>> buildColumns()
  {

    List<TableColumn<LibraryPlate,?>> columns = Lists.newArrayList();
    
    columns.add(new IntegerColumn<LibraryPlate>(
      "Plate",
      "Plate number",
      TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(LibraryPlate plate)
      {
        return plate.getPlateNumber();
      }
    });
    
    columns.add(new TextColumn<LibraryPlate>(
      "Library",
      "The library containing the plate",
      TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(LibraryPlate plate)
      {
        return plate.getLibrary().getLibraryName();
      }

      @Override
      public boolean isCommandLink() { return true; }

      @Override
      public Object cellAction(LibraryPlate plate)
      {
        return _libraryViewer.viewEntity(plate.getLibrary());

      }
    });

    columns.add(new IntegerColumn<LibraryPlate>(
      "Screening Count",
      "The number of times this plate has been screened, ignoring replicates",
      TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(LibraryPlate plate)
      {
        return plate.getScreeningCount();
      }
      
      @Override public boolean isCommandLink() { return true; }
      
      @Override
      public Object cellAction(LibraryPlate plate)
      {
        _activitesBrowser.searchActivities(Sets.<Activity>newHashSet(plate.getLibraryScreenings()),
                                           "Library Screenings for library plate " + plate.getPlateNumber());
        return BROWSE_ACTIVITIES;
      }
    });
    
    columns.add(new IntegerColumn<LibraryPlate>(
      "Assay Plate Count",
      "The number of assay plates that have been screened, including replicates and re-screenings",
      TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(LibraryPlate plate)
      {
        return plate.getAssayPlateCount();
      }
    });
    

    columns.add(new TextSetColumn<LibraryPlate>("Copies Screened",
      "The library copies that were used to screen the plate",
      TableColumn.UNGROUPED) {
      @Override
      public Set<String> getCellValue(LibraryPlate plate)
      {
        return Sets.newTreeSet(Iterables.transform(plate.getCopiesScreened(), Copy.ToName));
      }
      

      //      @Override
      //      public boolean isCommandLink()
      //      {
      //        return true;
      //      }
      //      
      //      @Override
      //      public Object cellAction(LibraryPlate plate)
      //      {
      //        // TODO: link to Copy Viewer for the user-selected copy, see [#2561] 
      //      }        
    });

    columns.add(new DateColumn<LibraryPlate>(
      "Last Date Screened",
      "The date the plate was last screened",
      TableColumn.UNGROUPED) {
      @Override
      public LocalDate getDate(LibraryPlate plate)
      {
        return plate.getLastDateScreened();
      }
    });
    
    columns.add(new DateColumn<LibraryPlate>(
      "First Date Screened",
      "The date the plate was first screened",
      TableColumn.UNGROUPED) {
      @Override
      public LocalDate getDate(LibraryPlate plate)
      {
        return plate.getFirstDateScreened();
      }
    });
    
    columns.add(new BooleanColumn<LibraryPlate>(
      "Data Loaded",
      "Indicates whether screen result data has been loaded for the plate",
      TableColumn.UNGROUPED) {
      @Override
      public Boolean getCellValue(LibraryPlate plate)
      {
        return plate.isDataLoaded();
      }
    });
    
    columns.add(new DateColumn<LibraryPlate>(
      "Date Data Loaded",
      "The date the data was loaded for the plate",
      TableColumn.UNGROUPED) {
      @Override
      public LocalDate getDate(LibraryPlate plate)
      {
        return plate.getLastDateDataLoaded();
      }
    });
    
    return columns;
  }
}
