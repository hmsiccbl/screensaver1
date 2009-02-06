// $HeadURL$
// $Id$

// Copyright 2006 by the President and Fellows of Harvard College.

// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.datafetcher.AllEntitiesOfTypeDataFetcher;
import edu.harvard.med.screensaver.db.datafetcher.EntitySetDataFetcher;
import edu.harvard.med.screensaver.db.hibernate.HqlBuilder;
import edu.harvard.med.screensaver.model.PropertyPath;
import edu.harvard.med.screensaver.model.RelationshipPath;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.AssayReadoutType;
import edu.harvard.med.screensaver.model.screens.FundingSupport;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.StatusItem;
import edu.harvard.med.screensaver.model.screens.StatusValue;
import edu.harvard.med.screensaver.model.screens.Study;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.ui.screens.ScreenViewer;
import edu.harvard.med.screensaver.ui.table.Criterion.Operator;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.DateEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.EntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.EnumEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.IntegerEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.ListEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.TextEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.UserNameColumn;
import edu.harvard.med.screensaver.ui.users.UserViewer;
import edu.harvard.med.screensaver.util.CollectionUtils;
import edu.harvard.med.screensaver.util.NullSafeComparator;

import org.apache.commons.collections.Transformer;
import org.joda.time.LocalDate;


/**
 * A {@link SearchResults} for {@link Screen Screens}.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class ScreenSearchResults extends EntitySearchResults<Screen,Integer>
{

  // instance fields

  private ScreenViewer _screenViewer;
  private UserViewer _userViewer;
  protected GenericEntityDAO _dao;


  // public constructor

  /**
   * @motivation for CGLIB2
   */
  protected ScreenSearchResults()
  {
  }

  public ScreenSearchResults(ScreenViewer screenViewer,
                             UserViewer userViewer,
                             GenericEntityDAO dao)
  {
    _screenViewer = screenViewer;
    _userViewer = userViewer;
    _dao = dao;
  }

  public void searchScreensForUser(ScreeningRoomUser screener)
  {
    Set<Screen> screens = new HashSet<Screen>();
    screens.addAll(screener.getAllAssociatedScreens());
    if (screens.isEmpty()) {
      showMessage("screens.noScreensForUser");
    }
    else {
      initialize(new EntitySetDataFetcher<Screen,Integer>(Screen.class, CollectionUtils.<Integer>entityIds(screens), _dao));
      // default to descending sort order on screen number
      getColumnManager().setSortAscending(false);
    }
  }

  public void searchAllScreens()
  {
    initialize(new AllEntitiesOfTypeDataFetcher<Screen,Integer>(Screen.class, _dao) {
      @Override
      protected void addDomainRestrictions(HqlBuilder hql,
                                           Map<RelationshipPath<Screen>,String> path2Alias)
      {
        super.addDomainRestrictions(hql, path2Alias);
        hql.where(getRootAlias(), "screenNumber", Operator.LESS_THAN, Study.MIN_STUDY_NUMBER);
      }
    });
  }

  public void searchScreens(Set<Screen> screens)
  {
    Set<Integer> screenIds = new HashSet<Integer>();
    for (Screen screen : screens) {
      screenIds.add(screen.getEntityId());
    }
    initialize(new EntitySetDataFetcher<Screen,Integer>(Screen.class, screenIds, _dao));
    // default to descending sort order on screen number
    getColumnManager().setSortAscending(false);
  }


  // implementations of the SearchResults abstract methods

  @SuppressWarnings("unchecked")
  protected List<? extends TableColumn<Screen,?>> buildColumns()
  {
    ArrayList<EntityColumn<Screen,?>> columns = new ArrayList<EntityColumn<Screen,?>>();
    columns.add(new IntegerEntityColumn<Screen>(
      new PropertyPath(Screen.class, "screenNumber"),
      "Screen Number", "The screen number", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(Screen screen) { return screen.getScreenNumber(); }

      @Override
      public Object cellAction(Screen screen) { return viewSelectedEntity(); }

      @Override
      public boolean isCommandLink() { return true; }
    });
    columns.add(new TextEntityColumn<Screen>(
      new PropertyPath(Screen.class, "title"),
      "Title", "The title of the screen", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(Screen screen) { return screen.getTitle(); }
    });
    columns.add(new UserNameColumn<Screen>(
      new RelationshipPath(Screen.class, "labHead"),
      "Lab Head", "The head of the lab performing the screen", TableColumn.UNGROUPED, _userViewer) {

      @Override
      public ScreensaverUser getUser(Screen screen) { return screen.getLabHead(); }
    });
    columns.get(columns.size() - 1).addRelationshipPath(new RelationshipPath(Screen.class, "labHead.labAffiliation"));
    columns.add(new TextEntityColumn<Screen>(
      new PropertyPath(Screen.class, "labHead.labAffiliation", "affiliationName"),
      "Lab Affiliation", "The affiliation of the lab performing the screen", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(Screen screen) { return screen.getLabHead().getLab().getLabAffiliationName(); }
    });
    columns.get(columns.size() - 1).setVisible(false);
    columns.add(new UserNameColumn<Screen>(
      new RelationshipPath(Screen.class, "leadScreener"),
      "Lead Screener", "The scientist primarily responsible for running the screen", TableColumn.UNGROUPED, _userViewer) {
      @Override
      public ScreensaverUser getUser(Screen screen) { return screen.getLeadScreener(); }
    });
    columns.add(new EnumEntityColumn<Screen,ScreenResultAvailability>(
      new RelationshipPath(Screen.class, "screenResult"),
      "Screen Result",
      "'available' if the screen result is loaded into Screensaver and viewable by the current user;" +
      " 'not shared' if loaded but not viewable by the current user; otherwise 'none'",
      TableColumn.UNGROUPED, ScreenResultAvailability.values()) {
      @Override
      public ScreenResultAvailability getCellValue(Screen screen)
      {
        if (screen.getScreenResult() == null) {
          return ScreenResultAvailability.NONE;
        }
        else if (screen.getScreenResult().isRestricted()) {
          return ScreenResultAvailability.NOT_SHARED;
        }
        else {
          return ScreenResultAvailability.AVAILABLE;
        }
      }

      @Override
      protected Comparator<Screen> getAscendingComparator()
      {
        return new Comparator<Screen>() {
          private NullSafeComparator<ScreenResult> _srComparator =
            new NullSafeComparator<ScreenResult>(true)
            {
            @Override
            protected int doCompare(ScreenResult sr1, ScreenResult sr2)
            {
              if (!sr1.isRestricted() && sr2.isRestricted()) {
                return -1;
              }
              if (sr1.isRestricted() && !sr2.isRestricted()) {
                return 1;
              }
              return sr1.getScreen().getScreenNumber().compareTo(sr2.getScreen().getScreenNumber());
            }
            };

            public int compare(Screen s1, Screen s2) {
              return _srComparator.compare(s1.getScreenResult(),
                                           s2.getScreenResult());
            }
        };
      }
    });
    columns.add(new EnumEntityColumn<Screen, ScreenType>(
      new PropertyPath(Screen.class, "screenType"),
      "Screen Type", "'RNAi' or 'Small Molecule'", TableColumn.UNGROUPED, ScreenType.values()) {
      @Override
      public ScreenType getCellValue(Screen screen) { return screen.getScreenType(); }
    });
    columns.add(new DateEntityColumn<Screen>(
      new PropertyPath(Screen.class, "dateCreated"),
      "Date Created", "The date the screen was added to the database",
      TableColumn.ADMIN_COLUMN_GROUP) {
      @Override
      protected LocalDate getDate(Screen screen) { return screen.getDateCreated().toLocalDate(); }
    });
    columns.get(columns.size() - 1).setVisible(false);
    columns.add(new DateEntityColumn<Screen>(
      new PropertyPath(Screen.class, "labActivities", "dateOfActivity"),
      "Date Of Last Activity", "The date of the last lab activity performed for this screen",
      TableColumn.ADMIN_COLUMN_GROUP) {
      @Override
      protected LocalDate getDate(Screen screen) { return screen.getLabActivities().isEmpty() ? null : screen.getLabActivities().last().getDateOfActivity(); }
    });
    columns.get(columns.size() - 1).setVisible(false);
    columns.add(new EnumEntityColumn<Screen,StatusValue>(
      new PropertyPath(Screen.class, "statusItems", "statusValue"),
      "Status", "The current status of the screen, e.g., 'Completed', 'Ongoing', 'Pending', etc.",
      TableColumn.ADMIN_COLUMN_GROUP,
      StatusValue.values()) {
      @Override
      public StatusValue getCellValue(Screen screen)
      {
        SortedSet<StatusItem> statusItems = screen.getStatusItems();
        return statusItems.isEmpty() ? null : statusItems.last().getStatusValue();
      }
    });
    columns.add(new DateEntityColumn<Screen>(
      new PropertyPath(Screen.class, "statusItems", "statusDate"),
      "Status Date", "The date of the most recent change of status for the screen",
      TableColumn.ADMIN_COLUMN_GROUP) {
      @Override
      protected LocalDate getDate(Screen screen) {
        SortedSet<StatusItem> statusItems = screen.getStatusItems();
        return statusItems.isEmpty() ? null : statusItems.last().getStatusDate();
      }
    });
    // TODO: should make this a vocab list, but need support for list-of-vocab column type
    columns.add(new ListEntityColumn<Screen>(
      new PropertyPath(Screen.class, "fundingSupports", "value"),
      "Funding Supports", "The list of funding supports for the screen",
      TableColumn.ADMIN_COLUMN_GROUP) {
      @Override
      public List<String> getCellValue(Screen screen)
      {
        return new ArrayList<String>(
          org.apache.commons.collections.CollectionUtils.collect(screen.getFundingSupports(), new Transformer() {
            public Object transform(Object e) { return ((FundingSupport) e).getValue(); }
          }));
      }
    });
    columns.get(columns.size() - 1).setVisible(false);

    // TODO: should make this a vocab list, but need support for list-of-vocab column type
    columns.add(new ListEntityColumn<Screen>(
      new RelationshipPath(Screen.class, "screenResult.resultValueTypes"),
      "Assay Readout Type", "The assay readout type for the screen",
      TableColumn.ADMIN_COLUMN_GROUP) {
      @Override
      public List<String> getCellValue(Screen screen)
      {
        return new ArrayList<String>(
          org.apache.commons.collections.CollectionUtils.collect(screen.getAssayReadoutTypes(), new Transformer() {
            public Object transform(Object e) { return ((AssayReadoutType) e).getValue(); }
          }));
      }
    });
    columns.get(columns.size() - 1).setVisible(false);


//    TableColumnManager<Screen> columnManager = getColumnManager();
//    columnManager.addCompoundSortColumns(columnManager.getColumn("Lab Head"),
//                                         columnManager.getColumn("Lead Screener"),
//                                         columnManager.getColumn("Screen Number"));
//    columnManager.addCompoundSortColumns(columnManager.getColumn("Lead Screener"),
//                                         columnManager.getColumn("Lab Head"),
//                                         columnManager.getColumn("Screen Number"));

    return columns;
  }

  @Override
  protected void setEntityToView(Screen screen)
  {
    _screenViewer.setScreen(screen);
  }
}
