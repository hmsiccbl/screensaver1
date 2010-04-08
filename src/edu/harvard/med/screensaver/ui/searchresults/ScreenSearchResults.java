// $HeadURL$
// $Id$

// Copyright © 2006, 2010 by the President and Fellows of Harvard College.

// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.datafetcher.AllEntitiesOfTypeDataFetcher;
import edu.harvard.med.screensaver.db.datafetcher.EntitySetDataFetcher;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenDataSharingLevel;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.StatusItem;
import edu.harvard.med.screensaver.model.screens.StatusValue;
import edu.harvard.med.screensaver.model.screens.Study;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.ui.screens.ScreenViewer;
import edu.harvard.med.screensaver.ui.table.Criterion.Operator;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.DateEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.EnumEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.HasFetchPaths;
import edu.harvard.med.screensaver.ui.table.column.entity.IntegerEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.TextEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.TextSetEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.UserNameColumn;
import edu.harvard.med.screensaver.ui.users.UserViewer;
import edu.harvard.med.screensaver.util.CollectionUtils;
import edu.harvard.med.screensaver.util.NullSafeComparator;

import org.joda.time.LocalDate;

import com.google.common.base.Functions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;


/**
 * A {@link SearchResults} for {@link Screen Screens}.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class ScreenSearchResults extends EntitySearchResults<Screen,Integer>
{
  private GenericEntityDAO _dao;

  private UserViewer _userViewer;
    
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
    super(screenViewer);
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

  @Override
  public void searchAll()
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
    
    List<TableColumn<Screen,?>> columns = Lists.newArrayList();
    columns.addAll(buildScreenSummaryColumns());
    columns.addAll(buildScreenAdminColumns());
    columns.addAll(buildScreenResultColumns());


//    TableColumnManager<Screen> columnManager = getColumnManager();
//    columnManager.addCompoundSortColumns(columnManager.getColumn("Lab Head"),
//                                         columnManager.getColumn("Lead Screener"),
//                                         columnManager.getColumn("Screen Number"));
//    columnManager.addCompoundSortColumns(columnManager.getColumn("Lead Screener"),
//                                         columnManager.getColumn("Lab Head"),
//                                         columnManager.getColumn("Screen Number"));

    return columns;
  }

  private List<TableColumn<Screen,?>> buildScreenResultColumns()
  {
    List<TableColumn<Screen,?>> columns = Lists.newArrayList();

    // TODO: should make this a vocab list, but need support for list-of-vocab column type
    columns.add(new EnumEntityColumn<Screen,ScreenResultAvailability>(
      Screen.screenResult,
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

    columns.add(new TextSetEntityColumn<Screen>(
      Screen.screenResult.to(ScreenResult.dataColumns),
      "Assay Readout Type", "The assay readout type for the screen",
      TableColumn.UNGROUPED) {
      @Override
      public Set<String> getCellValue(Screen screen)
      {
        return Sets.newHashSet(Iterables.transform(screen.getAssayReadoutTypes(), Functions.toStringFunction())); 
      }
    });
    columns.get(columns.size() - 1).setAdministrative(true);
    columns.get(columns.size() - 1).setVisible(false);
    
    return columns;
  }

  List<TableColumn<Screen,?>> buildScreenAdminColumns()
  {
    List<TableColumn<Screen,?>> columns = Lists.newArrayList();
    
    columns.add(new DateEntityColumn<Screen>(
      new PropertyPath(Screen.class, "dateCreated"),
      "Date Created", "The date the screen was added to the database",
      TableColumn.UNGROUPED) {
      @Override
      protected LocalDate getDate(Screen screen) { return screen.getDateCreated().toLocalDate(); }
    });
    columns.get(columns.size() - 1).setAdministrative(true);
    columns.get(columns.size() - 1).setVisible(false);
    
    columns.add(new EnumEntityColumn<Screen,ScreenDataSharingLevel>(
      new PropertyPath<Screen>(Screen.class, "dataSharingLevel"),
      "Data Sharing Level", 
      "The data sharing level", 
      TableColumn.UNGROUPED,
      ScreenDataSharingLevel.values()) {
      @Override
      public ScreenDataSharingLevel getCellValue(Screen screen) { return screen.getDataSharingLevel(); }
    });
    columns.get(columns.size() - 1).setAdministrative(false);
    columns.get(columns.size() - 1).setVisible(false);
    
    columns.add(new DateEntityColumn<Screen>(
      new PropertyPath<Screen>(Screen.class, "dataPrivacyExpirationDate"),
      "Data Privacy Expiration Date", 
      "The date on which the screen will become visible to level 1 users", 
      TableColumn.UNGROUPED) {
      @Override
      public LocalDate getDate(Screen screen) { return screen.getDataPrivacyExpirationDate(); }
    });
    columns.get(columns.size() - 1).setAdministrative(true);
    columns.get(columns.size() - 1).setVisible(false);
    
    columns.add(new DateEntityColumn<Screen>(
      Screen.labActivities.toProperty("dateOfActivity"),
      "Date Of Last Activity", "The date of the last lab activity performed for this screen",
      TableColumn.UNGROUPED) {
      @Override
      protected LocalDate getDate(Screen screen) { return screen.getLabActivities().isEmpty() ? null : screen.getLabActivities().last().getDateOfActivity(); }
    });
    columns.get(columns.size() - 1).setAdministrative(true);
    columns.get(columns.size() - 1).setVisible(false);

    columns.add(new EnumEntityColumn<Screen,StatusValue>(Screen.statusItems.toProperty("statusValue"),
      "Status", "The current status of the screen, e.g., 'Completed', 'Ongoing', 'Pending', etc.",
      TableColumn.UNGROUPED,
      StatusValue.values()) {
      @Override
      public StatusValue getCellValue(Screen screen)
      {
        SortedSet<StatusItem> statusItems = screen.getStatusItems();
        return statusItems.isEmpty() ? null : statusItems.last().getStatusValue();
      }
    });
    columns.get(columns.size() - 1).setAdministrative(true);

    columns.add(new DateEntityColumn<Screen>(
      Screen.statusItems.toProperty("statusDate"),
      "Status Date", "The date of the most recent change of status for the screen",
      TableColumn.UNGROUPED) {
      @Override
      protected LocalDate getDate(Screen screen) {
        SortedSet<StatusItem> statusItems = screen.getStatusItems();
        return statusItems.isEmpty() ? null : statusItems.last().getStatusDate();
      }
    });
    columns.get(columns.size() - 1).setAdministrative(true);

    // TODO: should make this a vocab list, but need support for list-of-vocab column type
    columns.add(new TextSetEntityColumn<Screen>(
      Screen.fundingSupports.toProperty("value"),
      "Funding Supports", "The list of funding supports for the screen",
      TableColumn.UNGROUPED) {
      @Override
      public Set<String> getCellValue(Screen screen)
      {
        return Sets.newHashSet(Iterables.transform(screen.getFundingSupports(), Functions.toStringFunction())); 
      }
    });
    columns.get(columns.size() - 1).setAdministrative(true);
    columns.get(columns.size() - 1).setVisible(false);
    
    columns.add(new IntegerEntityColumn<Screen>(
      new PropertyPath<Screen>(Screen.class, "screenedExperimentalWellCount"),
      "Experimental Wells Screened (Non-unique)", 
      "The number of experimental library wells that have been screened (counting duplicate wells multiple times, ignoring replicates)", 
      TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(Screen screen) 
      { 
        return screen.getScreenedExperimentalWellCount();
      }
    });
    columns.get(columns.size() - 1).setAdministrative(true);
    columns.get(columns.size() - 1).setVisible(false);

    columns.add(new IntegerEntityColumn<Screen>(
      new PropertyPath<Screen>(Screen.class, "screenedExperimentalWellCount"),
      "Experimental Wells Screened (Unique)", 
      "The number of experimental library wells that have been screened (counting duplicate wells once, ignoring replicates)",
      TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(Screen screen) 
      { 
        return screen.getUniqueScreenedExperimentalWellCount();
      }
    });
    columns.get(columns.size() - 1).setAdministrative(true);
    columns.get(columns.size() - 1).setVisible(false);

    return columns;
  }

  List<TableColumn<Screen,?>> buildScreenSummaryColumns()
  {
    List<TableColumn<Screen,?>> columns = Lists.newArrayList();
    columns.add(new IntegerEntityColumn<Screen>(
      new PropertyPath<Screen>(Screen.class, "screenNumber"),
      "Screen Number", "The screen number", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(Screen screen) { return screen.getScreenNumber(); }

      @Override
      public Object cellAction(Screen screen) {
        // note: we explicitly call getEntityViewer(), since this column may be used in other search result types
        return getEntityViewer().viewEntity(screen);
      }

      @Override
      public boolean isCommandLink() { return true; }
    });
    columns.add(new EnumEntityColumn<Screen, ScreenType>(
      new PropertyPath<Screen>(Screen.class, "screenType"),
      "Screen Type", "'RNAi' or 'Small Molecule'", TableColumn.UNGROUPED, ScreenType.values()) {
      @Override
      public ScreenType getCellValue(Screen screen) { return screen.getScreenType(); }
    });
    columns.add(new TextEntityColumn<Screen>(
      new PropertyPath<Screen>(Screen.class, "title"),
      "Title", "The title of the screen", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(Screen screen) { return screen.getTitle(); }
    });
    columns.add(new UserNameColumn<Screen,ScreeningRoomUser>(
      Screen.labHead,
      "Lab Head", "The head of the lab performing the screen", TableColumn.UNGROUPED, _userViewer) {
      @Override
      public ScreeningRoomUser getUser(Screen screen) { return screen.getLabHead(); }
    });
    ((HasFetchPaths<Screen>) columns.get(columns.size() - 1)).addRelationshipPath(Screen.labHead.to(LabHead.labAffiliation));
    columns.add(new TextEntityColumn<Screen>(
      Screen.labHead.to(ScreensaverUser.labAffiliation).toProperty("affiliationName"),
      "Lab Affiliation", "The affiliation of the lab performing the screen", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(Screen screen) {
        if (screen.getLabHead() != null) {
          return screen.getLabHead().getLab().getLabAffiliationName(); 
        }
        return null;
      }
    });
    columns.get(columns.size() - 1).setVisible(false);
    columns.add(new UserNameColumn<Screen,ScreeningRoomUser>(
      Screen.leadScreener,
      "Lead Screener", "The scientist primarily responsible for running the screen", TableColumn.UNGROUPED, _userViewer) {
      @Override
      public ScreeningRoomUser getUser(Screen screen) { return screen.getLeadScreener(); }
    });
    return columns;
  }
}
