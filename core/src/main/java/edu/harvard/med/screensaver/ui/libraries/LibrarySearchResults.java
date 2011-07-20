// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;

import edu.harvard.med.lincs.screensaver.LincsScreensaverConstants;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.db.datafetcher.EntityDataFetcher;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryScreeningStatus;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.libraries.Solvent;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.screenresults.AssayPlate;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.arch.datatable.Criterion;
import edu.harvard.med.screensaver.ui.arch.datatable.Criterion.Operator;
import edu.harvard.med.screensaver.ui.arch.datatable.column.DateColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TableColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.BooleanEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.DateEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.EnumEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.IntegerEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.TextEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.TextSetEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.model.InMemoryEntityDataModel;
import edu.harvard.med.screensaver.ui.arch.searchresults.EntityBasedEntitySearchResults;
import edu.harvard.med.screensaver.ui.arch.searchresults.SearchResults;


/**
 * A {@link SearchResults} for {@link Library Libraries}.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class LibrarySearchResults extends EntityBasedEntitySearchResults<Library,Integer>
{
  private static final Logger log = Logger.getLogger(LibrarySearchResults.class);
  public static final Set<LibraryType> LIBRARY_TYPES_TO_DISPLAY =
    new HashSet<LibraryType>(Arrays.asList(LibraryType.COMMERCIAL,
                                           LibraryType.KNOWN_BIOACTIVES,
                                           LibraryType.NATURAL_PRODUCTS,
                                           LibraryType.SIRNA,
                                           LibraryType.MIRNA_INHIBITOR,
                                           LibraryType.MIRNA_MIMIC,
                                           LibraryType.OTHER));


  private GenericEntityDAO _dao;

  private LibraryViewer _libraryViewer;
  private WellSearchResults _wellsBrowser;
  
  /**
   * @motivation for CGLIB2
   */
  protected LibrarySearchResults()
  {
  }

  public LibrarySearchResults(GenericEntityDAO dao,
                              LibraryViewer libraryViewer,
                              WellSearchResults wellsBrowser)
  {
    super(libraryViewer);
    _dao = dao;
    _libraryViewer = libraryViewer;
    _wellsBrowser = wellsBrowser;
  }

  @Override
  public void searchAll()
  {
    setTitle("Libraries");
    initialize(new InMemoryEntityDataModel<Library,Integer,Library>(new EntityDataFetcher<Library,Integer>(Library.class, _dao) {
      @Override
      public void addDomainRestrictions(HqlBuilder hql)
      {
        hql.whereIn(getRootAlias(), "libraryType", LIBRARY_TYPES_TO_DISPLAY);
      }
    }));
  }

  public void searchLibraryScreenType(ScreenType screenType)
  {
    setTitle(screenType + " Libraries");
    initialize(new InMemoryEntityDataModel<Library,Integer,Library>(new EntityDataFetcher<Library,Integer>(Library.class, _dao) {
      @Override
      public void addDomainRestrictions(HqlBuilder hql)
      {
        hql.whereIn(getRootAlias(), "libraryType", LIBRARY_TYPES_TO_DISPLAY);
      }
    }));

    TableColumn<Library,ScreenType> column = (TableColumn<Library,ScreenType>) getColumnManager().getColumn("Screen Type");
    column.clearCriteria();
    column.addCriterion(new Criterion<ScreenType>(Operator.EQUAL, screenType));

    // [#2867]
    if (getScreensaverUser().isUserInRole(ScreensaverUserRole.LIBRARIES_ADMIN)) {
      getColumnManager().setSortColumnName("Start Plate");
      getColumnManager().setSortDirection(SortDirection.DESCENDING);
    }
  }

  @SuppressWarnings("unchecked")
  protected List<? extends TableColumn<Library,?>> buildColumns()
  {
    ArrayList<TableColumn<Library,?>> columns = Lists.newArrayList();
    columns.add(new TextEntityColumn<Library>(RelationshipPath.from(Library.class).toProperty("shortName"),
      "Short Name", "The abbreviated name for the library", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(Library library) 
      {
        return library.getShortName();
      }

      @Override
      public boolean isCommandLink() { return true; }

      @Override
      public Object cellAction(Library library) { return viewSelectedEntity(); }
    });
    columns.add(new TextEntityColumn<Library>(RelationshipPath.from(Library.class).toProperty("libraryName"),
                                              "Library Name", "The full name of the library", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(Library library)
      {
        return library.getLibraryName();
      }
    });

    IntegerEntityColumn column = new IntegerEntityColumn<Library>(Library.startPlate,
                                                                  "Experimental Well Count",
                                                                  "The number of experimental wells in the library (click link to browse experimental wells)",
                                                                  TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(Library library)
      {
        return library.getExperimentalWellCount();
      }

      @Override
      public boolean isCommandLink()
      {
        return true;
      }

      public Object cellAction(Library library)
      {
        _wellsBrowser.searchWellsForLibrary(library);
        return BROWSE_WELLS;
      }
    };
    columns.add(column);
    
    if (!!!LincsScreensaverConstants.FACILITY_NAME.equals(getApplicationProperties().getFacility()) ||
      !!!getScreensaverUser().isUserInRole(ScreensaverUserRole.GUEST)) {
      columns.add(new TextEntityColumn<Library>(RelationshipPath.from(Library.class).toProperty("provider"),
                                                "Provider",
                                                "The vendor or source that provided the library",
                                                TableColumn.UNGROUPED) {
        @Override
        public String getCellValue(Library library)
        {
          return library.getProvider();
        }
      });
    }

    columns.add(new EnumEntityColumn<Library,ScreenType>(RelationshipPath.from(Library.class).toProperty("screenType"),
                                                         "Screen Type", "'RNAi' or 'Small Molecule'",
                                                         TableColumn.UNGROUPED, ScreenType.values()) {
      @Override
      public ScreenType getCellValue(Library library)
      {
        return library.getScreenType();
      }
    });

    columns.add(new EnumEntityColumn<Library,Solvent>(RelationshipPath.from(Library.class).toProperty("solvent"),
                                                      "Solvent",
                                                      "Solvent used in the library wells",
                                                      TableColumn.UNGROUPED,
                                                      Solvent.values()) {
      @Override
      public Solvent getCellValue(Library library)
      {
        return library.getSolvent();
      }
    });
    Iterables.getLast(columns).setVisible(false);

    if (!!!LincsScreensaverConstants.FACILITY_NAME.equals(getApplicationProperties().getFacility())) {
      columns.add(new EnumEntityColumn<Library,LibraryType>(RelationshipPath.from(Library.class).toProperty("libraryType"),
                                                            "Library Type", "The type of library, e.g., 'Commercial', 'Known Bioactives', 'siRNA', etc.",
                                                            TableColumn.UNGROUPED, LibraryType.values()) {
        @Override
        public LibraryType getCellValue(Library library)
        {
          return library.getLibraryType();
        }
      });

      columns.add(new BooleanEntityColumn<Library>(RelationshipPath.from(Library.class).toProperty("pool"),
                                                   "Is Pool",
                                                   "Whether wells contains pools of reagents or single reagents",
                                                   TableColumn.UNGROUPED) {
        @Override
        public Boolean getCellValue(Library library)
        {
          return library.isPool();
        }
      });
    }
    columns.add(new IntegerEntityColumn<Library>(
      Library.startPlate,
      "Start Plate", 
      "The plate number for the first plate in the library", 
      TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(Library library)
      {
        return library.getStartPlate();
      }
    });

    columns.add(new IntegerEntityColumn<Library>(Library.endPlate,
                                                 "End Plate",
                                                 "The plate number for the last plate in the library",
                                                 TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(Library library)
      {
        return library.getEndPlate();
      }
    });

    if (!!!LincsScreensaverConstants.FACILITY_NAME.equals(getApplicationProperties().getFacility())) {
      columns.add(new TextSetEntityColumn<Library>(Library.copies.toProperty("name"),
                                                   "Copies",
                                                   "The copies that have been made of this library",
                                                   TableColumn.UNGROUPED) {
        @Override
        public Set<String> getCellValue(Library library)
        {
          if (library == null) {
            return null;
          }
          return Sets.newTreeSet(Iterables.transform(library.getCopies(), Copy.ToName));
        }
      });
      columns.get(columns.size() - 1).setAdministrative(true);
    }

    columns.add(new EnumEntityColumn<Library,LibraryScreeningStatus>(RelationshipPath.from(Library.class).toProperty("screeningStatus"),
                                                                     "Screening Status",
                                                                     "Screening status for the library, e.g., 'Allowed', 'Not Allowed', 'Not Yet Plated', etc.",
                                                                     TableColumn.UNGROUPED,
                                                                     LibraryScreeningStatus.values()) {
      @Override
      public LibraryScreeningStatus getCellValue(Library library)
      {
        return library == null ? null : library.getScreeningStatus();
      }
    });
    columns.get(columns.size() - 1).setAdministrative(true);

    columns.add(new DateColumn<Library>("Date First Plated",
      "The earliest date on which a copy of this library was plated",
      TableColumn.UNGROUPED) {
      @Override
      public LocalDate getDate(Library library)
      {
        return library.getDateScreenable();
      }
    });
    
    if (LincsScreensaverConstants.FACILITY_NAME.equals(getApplicationProperties().getFacility())) {
      columns.add(new DateEntityColumn<Library>(RelationshipPath.from(Library.class).toProperty("dateCreated"),
                                                "Date Data Received",
                                                "The date the data was received",
                                                TableColumn.UNGROUPED) {
        @Override
        protected LocalDate getDate(Library library)
        {
          return library.getDateCreated().toLocalDate();
        }
      });
    }

//    TableColumnManager<Library> columnManager = getColumnManager();
//    columnManager.addCompoundSortColumns(columnManager.getColumn("Screen Type"),
//                                         columnManager.getColumn("Library Type"),
//                                         columnManager.getColumn("Short Name"));
//    columnManager.addCompoundSortColumns(columnManager.getColumn("Library Type"),
//                                         columnManager.getColumn("Short HName"));

    return columns;
  }

  public void searchLibrariesScreened(final Screen screen)
  {
    setTitle("Libraries Screened by screen " + screen.getFacilityId());
    EntityDataFetcher<Library,Integer> dataFetcher = new EntityDataFetcher<Library,Integer>(Library.class, _dao) {
      @Override
      public void addDomainRestrictions(HqlBuilder hql)
      {
        hql.
          from(Screen.class, "s").
          from("s", Screen.assayPlates, "ap").
          from("ap", AssayPlate.plateScreened, "p").
          from("p", Plate.copy, "c").
          from("c", Copy.library, "l").
          where("s", screen).
          where("l", Operator.EQUAL, getRootAlias());
      }
    };
    initialize(new InMemoryEntityDataModel<Library,Integer,Library>(dataFetcher));
    getColumnManager().getColumn("Copies").setVisible(false);
  }

  public void searchLibrariesScreened(final LibraryScreening libraryScreening)
  {
    setTitle("Libraries Screened by library screening " + libraryScreening.getActivityId());
    EntityDataFetcher<Library,Integer> dataFetcher = new EntityDataFetcher<Library,Integer>(Library.class, _dao) {
      @Override
      public void addDomainRestrictions(HqlBuilder hql)
      {
        hql.
          from(LibraryScreening.class, "ls").
          from("ls", LibraryScreening.assayPlatesScreened, "ap").
          from("ap", AssayPlate.plateScreened, "p").
          from("p", Plate.copy, "c").
          from("c", Copy.library, "l").
          where("ls", libraryScreening).
          where("l", Operator.EQUAL, getRootAlias());
      }
    };
    initialize(new InMemoryEntityDataModel<Library,Integer,Library>(dataFetcher));
    getColumnManager().getColumn("Copies").setVisible(false);
  }

  public LibraryCopySearchResults getCopiesBrowser()
  {
    return _libraryViewer.getCopiesBrowser();
  }
}
