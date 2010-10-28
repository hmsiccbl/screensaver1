// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.joda.time.LocalDate;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.Query;
import edu.harvard.med.screensaver.db.datafetcher.EntityDataFetcher;
import edu.harvard.med.screensaver.db.datafetcher.SetBasedDataFetcher;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.model.Activity;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.libraries.ScreeningStatistics;
import edu.harvard.med.screensaver.model.screenresults.AssayPlate;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.ui.libraries.LibraryCopyViewer;
import edu.harvard.med.screensaver.ui.libraries.LibraryViewer;
import edu.harvard.med.screensaver.ui.table.Criterion.Operator;
import edu.harvard.med.screensaver.ui.table.column.DateColumn;
import edu.harvard.med.screensaver.ui.table.column.EnumColumn;
import edu.harvard.med.screensaver.ui.table.column.FixedDecimalColumn;
import edu.harvard.med.screensaver.ui.table.column.IntegerColumn;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;
import edu.harvard.med.screensaver.ui.table.column.TextColumn;
import edu.harvard.med.screensaver.ui.table.model.InMemoryDataModel;

/**
 * A {@link SearchResults} for {@link Copy Copies}.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class LibraryCopySearchResults extends EntityBasedEntitySearchResults<Copy,Integer>
{
  private static final Logger log = Logger.getLogger(LibraryCopySearchResults.class);

  protected static final int DECIMAL_SCALE = 1; // number of decimal digits (to the right of decimal point) for plate screening count average

  private GenericEntityDAO _dao;
  private LibraryCopyViewer _libraryCopyViewer;
  private LibraryViewer _libraryViewer;
  private ActivitySearchResults<Activity> _activitesBrowser;
  private LibraryCopyPlateSearchResults _libraryCopyPlateSearchResults;


  /**
   * @motivation for CGLIB2
   */
  protected LibraryCopySearchResults()
  {
  }

  public LibraryCopySearchResults(GenericEntityDAO dao,
                                  LibraryCopyViewer libraryCopyViewer,
                                  LibraryViewer libraryViewer,
                                  ActivitySearchResults<Activity> activitiesBrowser,
                                  LibraryCopyPlateSearchResults libraryCopyPlateSearchResults)
  {
    super(libraryCopyViewer);
    _dao = dao;
    _libraryCopyViewer = libraryCopyViewer;
    _libraryViewer = libraryViewer;
    _activitesBrowser = activitiesBrowser;
    _libraryCopyPlateSearchResults = libraryCopyPlateSearchResults;
  }

  @Override
  public void searchAll()
  {
    setTitle("Library Copies");
    initialize(new EntityDataFetcher<Copy,Integer>(Copy.class, _dao));
  }

  public void searchCopiesByLibrary(final Library library)
  {
    setTitle("Library Copies for library " + library.getLibraryName());
    EntityDataFetcher<Copy,Integer> copyDataFetcher =
      new EntityDataFetcher<Copy,Integer>(Copy.class, _dao) {
        @Override
        public void addDomainRestrictions(HqlBuilder hql)
        {
          hql.where(getRootAlias(), Copy.library.getLeaf(), Operator.EQUAL, library);
        }
      };
    initialize(copyDataFetcher);
  }

  private void initialize(EntityDataFetcher<Copy,Integer> copyDataFetcher)
  {
    copyDataFetcher.setPropertiesToFetch(Lists.newArrayList(Copy.library.toFullEntity(), Copy.plates.toFullEntity()));
    List<Copy> copies = copyDataFetcher.fetchAllData();
    SortedSet<Copy> copiesWithStatistics = calculateScreeningStatistics(copies);
    initialize(new InMemoryDataModel<Copy>(new SetBasedDataFetcher<Copy,Integer>(copiesWithStatistics)));
  }

  private SortedSet<Copy> calculateScreeningStatistics(List<Copy> copies)
  {
    // get copy-based statistics: screening_count, assay_plate_count, first/last date screened, data_loading_count
    final HqlBuilder builder = new HqlBuilder();

    Map<Integer,Copy> result = Maps.newHashMap();
    for (Copy c : copies) {
      c.setScreeningStatistics(new ScreeningStatistics());
      result.put(c.getEntityId(), c);
    }

    builder.from(Plate.class, "p")
           .from(AssayPlate.class, "ap")
           .from("ap", AssayPlate.libraryScreening.getPath(), "ls")
           .from("ap", AssayPlate.screenResultDataLoading.getPath(), "dl")
           .whereIn("p", Plate.copy.getLeaf() + ".id", result.keySet())
           .where("ap", AssayPlate.plateScreened.getLeaf(), Operator.EQUAL, "p", "id")
           .groupBy("p", "copy")
           .select("p", "copy.id")
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
      Integer copyId = (Integer) ((Object[]) o)[i++];
      ScreeningStatistics css = result.get(copyId).getScreeningStatistics();
      css.setScreeningCount(((Long) ((Object[]) o)[i++]).intValue());
      css.setAssayPlateCount(((Long) ((Object[]) o)[i++]).intValue());
      css.setDataLoadingCount(((Long) ((Object[]) o)[i++]).intValue());
      css.setFirstDateDataLoaded(((LocalDate) ((Object[]) o)[i++]));
      css.setLastDateDataLoaded(((LocalDate) ((Object[]) o)[i++]));
      css.setFirstDateScreened(((LocalDate) ((Object[]) o)[i++]));
      css.setLastDateScreened(((LocalDate) ((Object[]) o)[i++]));
    }

    // calculate plate count - the number of plates per copy 
    final HqlBuilder builder1 = new HqlBuilder();
    builder1.from(Plate.class, "p")
           .whereIn("p", Plate.copy.getLeaf() + ".id", result.keySet())
           .groupBy("p", Plate.copy.getLeaf() + ".id")
           .select("p", Plate.copy.getLeaf() + ".id")
           .selectExpression("count(*)");

    results = _dao.runQuery(new Query() {
      @Override
      public List<Object> execute(Session session)
        {
          return builder1.toQuery(session, true).list();
        }
    });

    for (Object o : results) {
      Integer copyId = (Integer) ((Object[]) o)[0];
      Integer count = ((Long) ((Object[]) o)[1]).intValue();
      ScreeningStatistics css = result.get(copyId).getScreeningStatistics();
      css.setPlateCount(count);
    }

    // calculate plate_screening_count - the total number of times individual plates from this copy have been screened, ignoring replicates)
    final HqlBuilder builder2 = new HqlBuilder();
    builder2.from(LibraryScreening.class, "ls")
           .from("ls", LibraryScreening.assayPlatesScreened.getPath(), "ap")
           .from("ap", AssayPlate.plateScreened.getPath(), "p")
           .from("p", Plate.copy.getPath(), "c")
           .whereIn("p", Plate.copy.getLeaf() + ".id", result.keySet())
           .where("ap", "replicateOrdinal", Operator.EQUAL, 0)
           .groupBy("c", "id")
           .select("c", "id")
           .selectExpression("count(*)");

    results = _dao.runQuery(new Query() {
      @Override
      public List<Object> execute(Session session)
        {
          return builder2.toQuery(session, true).list();
        }
    });

    for (Object o : results) {
      Integer copyId = (Integer) ((Object[]) o)[0];
      Integer count = ((Long) ((Object[]) o)[1]).intValue();
      ScreeningStatistics css = result.get(copyId).getScreeningStatistics();
      css.setPlateScreeningCount(count);
    }

    return Sets.newTreeSet(result.values());
  }

  @Override
  protected List<TableColumn<Copy,?>> buildColumns()
  {
    List<TableColumn<Copy,?>> columns = Lists.newArrayList();
    TableColumn<Copy,?> c;

    columns.add(new TextColumn<Copy>("Library", "The library that this copy represents", TableColumn.UNGROUPED) {

      @Override
      public String getCellValue(Copy copy)
      {
        return copy.getLibrary().getLibraryName();
      }

      @Override
      public boolean isCommandLink()
      {
        return true;
      }

      @Override
      public Object cellAction(Copy copy)
      {
        return _libraryViewer.viewEntity(copy.getLibrary());
      }
    });
    Iterables.getLast(columns).setVisible(!isNested());

    columns.add(new TextColumn<Copy>("Copy Name", "The name of the the copy", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(Copy copy)
      {
        return copy.getName();
      }

      @Override
      public boolean isCommandLink()
      {
        return true;
      }

      @Override
      public Object cellAction(Copy copy)
      {
        if (isNested()) {
          LibraryCopySearchResults libraryCopySearchResults = (LibraryCopySearchResults) _libraryCopyViewer.getContextualSearchResults();
          libraryCopySearchResults.searchCopiesByLibrary(copy.getLibrary());
          libraryCopySearchResults.findEntity(copy);
          return BROWSE_LIBRARY_COPIES;
        }
        return viewSelectedEntity();
      }
    });

    columns.add(new EnumColumn<Copy,CopyUsageType>("Usage Type", "The usage type of the copy", TableColumn.UNGROUPED, CopyUsageType.values()) {
      @Override
      public CopyUsageType getCellValue(Copy copy)
      {
        return copy.getUsageType();
      }
    });

    columns.add(new IntegerColumn<Copy>("Start Plate", "The first plate number of the library", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(Copy copy)
      {
        return copy.getLibrary().getStartPlate();
      }
    });
    Iterables.getLast(columns).setVisible(!isNested());

    columns.add(new IntegerColumn<Copy>("End Plate", "The last plate number of the library", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(Copy copy)
      {
        return copy.getLibrary().getEndPlate();
      }
    });
    Iterables.getLast(columns).setVisible(!isNested());

    columns.add(new IntegerColumn<Copy>("Library Plate Count", "The number of plates in the library", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(Copy copy)
      {
        return copy.getLibrary().getEndPlate() - copy.getLibrary().getStartPlate() + 1;
      }
    });
    Iterables.getLast(columns).setVisible(false);

    columns.add(new IntegerColumn<Copy>("Copy Plate Count", "The number of plates in this copy", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(Copy copy)
      {
        return copy.getScreeningStatistics().getPlateCount();
      }

      @Override
      public boolean isCommandLink()
      {
        return true;
      }

      @Override
      public Object cellAction(Copy copy)
      {
        _libraryCopyPlateSearchResults.searchPlatesForCopy(copy);
        return BROWSE_LIBRARY_COPY_PLATES;
      }
    });

    columns.add(new IntegerColumn<Copy>("Assay Plate Count", "The number of assay plates screened for this copy", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(Copy copy)
      {
        return copy.getScreeningStatistics().getAssayPlateCount();
      }
    });

    columns.add(new IntegerColumn<Copy>("Screening Count", "The total number of times this copy has been screened, ignoring replicates",
                                        TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(Copy copy)
      {
        return copy.getScreeningStatistics().getScreeningCount();
      }
      @Override
      public boolean isCommandLink()
      {
        return true;
      }
      @Override
      public Object cellAction(Copy copy)
      {
        _activitesBrowser.searchLibraryScreeningActivitiesForCopy(copy);
        return BROWSE_ACTIVITIES;
      }
    });

    columns.add(new IntegerColumn<Copy>("Plate Screening Count", "The total number of times individual plates from this copy have been screened, ignoring replicates",
                                        TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(Copy copy)
      {
        return copy.getScreeningStatistics().getPlateScreeningCount();
      }
    });
    Iterables.getLast(columns).setVisible(false);

    columns.add(new FixedDecimalColumn<Copy>("Plate Screening Count Average", "The average number of times this copy's plates have been screened, ignoring replicates",
                                             TableColumn.UNGROUPED) {
      @Override
      public BigDecimal getCellValue(Copy copy)
      {
        if (copy.getScreeningStatistics().getPlateCount() == 0) return null;
        if (copy.getScreeningStatistics().getPlateScreeningCount() == 0) return BigDecimal.ZERO;
        BigDecimal val = new BigDecimal(copy.getScreeningStatistics().getPlateScreeningCount()).divide(new BigDecimal(copy.getScreeningStatistics().getPlateCount()),
                                                                                                   DECIMAL_SCALE,
                                                                                                   RoundingMode.CEILING);
        return val;
      }
    });

    columns.add(new DateColumn<Copy>("First Date Screened", "The date the copy was first screened", TableColumn.UNGROUPED) {
      @Override
      public LocalDate getDate(Copy copy)
      {
        return copy.getScreeningStatistics().getFirstDateScreened();
      }
    });

    columns.add(new DateColumn<Copy>("Last Date Screened", "The date the copy was last screened", TableColumn.UNGROUPED) {
      @Override
      public LocalDate getDate(Copy copy)
      {
        return copy.getScreeningStatistics().getLastDateScreened();
      }
    });

    columns.add(new IntegerColumn<Copy>("Data Loading Count", "The number of screen results loaded for screens of this copy", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(Copy copy)
      {
        return copy.getScreeningStatistics().getDataLoadingCount();
      }
    });

    columns.add(new DateColumn<Copy>("First Date Data Loaded", "The date of the first screen result data loading activity", TableColumn.UNGROUPED) {
      @Override
      public LocalDate getDate(Copy copy)
      {
        return copy.getScreeningStatistics().getFirstDateDataLoaded();
      }
    });
    Iterables.getLast(columns).setVisible(false);

    columns.add(new DateColumn<Copy>("Last Date Data Loaded", "The date of the last screen result data loading activity", TableColumn.UNGROUPED) {
      @Override
      public LocalDate getDate(Copy copy)
      {
        return copy.getScreeningStatistics().getLastDateDataLoaded();
      }
    });
    Iterables.getLast(columns).setVisible(false);

    return columns;
  }

  @Override
  protected Copy rowToEntity(Copy row)
  {
    return row;
  }

}
