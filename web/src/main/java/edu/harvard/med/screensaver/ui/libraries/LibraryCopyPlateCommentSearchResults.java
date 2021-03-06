// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.joda.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.Query;
import edu.harvard.med.screensaver.db.datafetcher.SetBasedDataFetcher;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.db.hqlbuilder.JoinType;
import edu.harvard.med.screensaver.model.activities.Activity;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivity;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.libraries.PlateActivity;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.ui.arch.datatable.column.DateColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.IntegerColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TableColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TextColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.UserNameColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.model.InMemoryDataModel;
import edu.harvard.med.screensaver.ui.arch.searchresults.EntityBasedEntitySearchResults;

public class LibraryCopyPlateCommentSearchResults extends EntityBasedEntitySearchResults<PlateActivity,Integer>
{
  private static Logger log = Logger.getLogger(LibraryCopyPlateCommentSearchResults.class);

  private GenericEntityDAO _dao;
  private LibraryViewer _libraryViewer;
  private LibraryCopyViewer _libraryCopyViewer;

  private enum Mode {
    ALL,
    COPY,
    LIBRARY
  };

  private Mode _mode;


  protected LibraryCopyPlateCommentSearchResults() {}

  public LibraryCopyPlateCommentSearchResults(GenericEntityDAO dao,
                                              LibraryViewer libraryViewer,
                                              LibraryCopyViewer libraryCopyViewer)
  {
    _dao = dao;
    _libraryViewer = libraryViewer;
    _libraryCopyViewer = libraryCopyViewer;
    getIsPanelCollapsedMap().put("lcpcsr", true);
  }

  @Override
  public void searchAll()
  {
    _mode = Mode.ALL;
    setTitle("");
    throw new UnsupportedOperationException("should only be used to find comment update activities for a set of entities");
  }

  @Transactional
  public void searchForCopy(Copy copy)
  {
    _mode = Mode.COPY;
    copy = _dao.reloadEntity(copy, true, Copy.plates.to(Plate.updateActivities).to(Activity.performedBy));
    setTitle("Plate Comments for Copy: " + copy.getName());
    initialize(copy.getPlates().values());
  }

  @Transactional
  public void searchForLibrary(Library library)
  {
    _mode = Mode.LIBRARY;
    library = _dao.reloadEntity(library, true, Library.copies.to(Copy.plates).to(Plate.updateActivities).to(Activity.performedBy));
    setTitle("Plate Comments for Library " + library.getLibraryName());
    List<Plate> plates = Lists.newArrayList();
    for (Copy copy : library.getCopies()) {
      plates.addAll(copy.getPlates().values());
    }
    initialize(plates);
  }

  @Transactional
  public void searchForPlates(String title, Set<Integer> plateIds)
  {
    _mode = Mode.ALL;
    setTitle("Plate Comments for: " + title);

    final HqlBuilder builder = new HqlBuilder();
    builder.select("p").distinctProjectionValues();
    builder.from(Plate.class, "p");
    builder.from("p", Plate.updateActivities, "a", JoinType.LEFT_FETCH);
    builder.from("a", Activity.performedBy, "pb", JoinType.LEFT_FETCH);
    builder.whereIn("p", "id", plateIds);
    log.info("builder.toHql(): " + builder.toHql() + ", " + plateIds);
    Query<Plate> query = new Query<Plate>() {
      @Override
      public List<Plate> execute(Session session)
      {
        return builder.toQuery(session, true).list();
      }
    };

    initialize(_dao.runQuery(query));
  }

  private void initialize(Collection<Plate> plates)
  {
    SortedSet<PlateActivity> comments = Sets.newTreeSet();
    for (Plate plate : plates) {
      //plate = _dao.reloadEntity(plate);
      for (AdministrativeActivity ae : plate.getUpdateActivitiesOfType(AdministrativeActivityType.COMMENT)) {
        comments.add(new PlateActivity(plate, ae));
      }
    }
    initialize(new InMemoryDataModel<PlateActivity>(new SetBasedDataFetcher<PlateActivity,Integer>(comments)));
  }

  @Override
  protected List<? extends TableColumn<PlateActivity,?>> buildColumns()
  {
    List<TableColumn<PlateActivity,?>> columns = Lists.newArrayList();
    columns.add(new IntegerColumn<PlateActivity>("Plate",
                                                 "Plate number",
                                                 TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(PlateActivity ae)
      {
        return ae.getPlate().getPlateNumber();
      }
    });

    columns.add(new TextColumn<PlateActivity>("Copy",
                                              "The library copy containing the plate",
                                              TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(PlateActivity ae)
      {
        return ae.getPlate().getCopy().getName();
      }

      @Override
      public boolean isCommandLink()
      {
        return true;
      }

      @Override
      public Object cellAction(PlateActivity ae)
      {
        getIsPanelCollapsedMap().put("lcpcsr", true);
        return _libraryCopyViewer.viewEntity(ae.getPlate().getCopy());

      }
    });
    Iterables.getLast(columns).setVisible(_mode != Mode.COPY);

    columns.add(new TextColumn<PlateActivity>("Library",
                                              "The library containing the plate",
                                              TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(PlateActivity ae)
    {
      return ae.getPlate().getCopy().getLibrary().getLibraryName();
    }

      @Override
      public boolean isCommandLink()
    {
      return true;
    }

      @Override
      public Object cellAction(PlateActivity ae)
    {
      getIsPanelCollapsedMap().put("lcpcsr", true);
      return _libraryViewer.viewEntity(ae.getPlate().getCopy().getLibrary());

    }
    });
    Iterables.getLast(columns).setVisible(_mode == Mode.ALL);

    columns.add(new TextColumn<PlateActivity>("Comment", "Comment", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(PlateActivity activity)
          {
            return activity.getAdministrativeActivity().getComments();
          }
    });

    columns.add(new DateColumn<PlateActivity>("Comment Date",
                                              "The date that a corresponding real-world activity was performed",
                                              TableColumn.UNGROUPED) {
      @Override
      protected LocalDate getDate(PlateActivity pa)
          {
            return pa.getAdministrativeActivity().getDateOfActivity();
          }
    });

    columns.add(new UserNameColumn<PlateActivity,ScreensaverUser>(null,
                                                                  "Comment by",
                                                                  "The administrator who made the comment",
                                                                  TableColumn.UNGROUPED,
                                                                  null) {
      @Override
      public ScreensaverUser getUser(PlateActivity ae)
      {
        return (ScreensaverUser) ae.getAdministrativeActivity().getPerformedBy();
      }
    });

    return columns;
  }
}
