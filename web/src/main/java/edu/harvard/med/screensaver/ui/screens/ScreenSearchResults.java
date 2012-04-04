// $HeadURL$
// $Id$

// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.

// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screens;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import com.google.common.base.Functions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import edu.harvard.med.lincs.screensaver.LincsScreensaverConstants;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.Criterion.Operator;
import edu.harvard.med.screensaver.db.datafetcher.DataFetcherUtil;
import edu.harvard.med.screensaver.db.datafetcher.EntityDataFetcher;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.model.Entity;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.ProjectPhase;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenDataSharingLevel;
import edu.harvard.med.screensaver.model.screens.ScreenStatus;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.StatusItem;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TableColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.DateEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.DateTimeEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.EnumEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.HasFetchPaths;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.IntegerEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.TextEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.TextSetEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.UserNameColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.model.InMemoryEntityDataModel;
import edu.harvard.med.screensaver.ui.arch.searchresults.EntityBasedEntitySearchResults;
import edu.harvard.med.screensaver.ui.arch.searchresults.SearchResults;
import edu.harvard.med.screensaver.ui.users.UserViewer;
import edu.harvard.med.screensaver.util.NullSafeComparator;


/**
 * A {@link SearchResults} for {@link Screen Screens}.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class ScreenSearchResults extends EntityBasedEntitySearchResults<Screen,Integer>
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
    setTitle("Screens for screener " + screener.getFullNameFirstLast());
    final Set<Screen> screens = new HashSet<Screen>();
    screens.addAll(screener.getAllAssociatedScreens());
    if (screens.isEmpty()) {
      showMessage("screens.noScreensForUser");
    }
    else {
      initialize(new InMemoryEntityDataModel<Screen,Integer,Screen>(new EntityDataFetcher<Screen,Integer>(Screen.class, _dao) {
        @Override
        public void addDomainRestrictions(HqlBuilder hql)
        {
          DataFetcherUtil.addDomainRestrictions(hql, getRootAlias(), Sets.newHashSet(Iterables.transform(screens, Entity.ToEntityId)));
        }
      }));

      sortByCreationDateDesc();
    }
  }

  public void searchScreensForProject(final String projectId)
  {
    setTitle("Screens for project " + projectId);
    initialize(new InMemoryEntityDataModel<Screen,Integer,Screen>(new EntityDataFetcher<Screen,Integer>(Screen.class, _dao) {
      @Override
      public void addDomainRestrictions(HqlBuilder hql)
      {
        hql.where(getRootAlias(), "projectId", Operator.EQUAL, projectId);
      }
    }));

    if (getColumnManager().getColumn("Project Phase") != null) {
      getColumnManager().setSortColumnName("Project Phase");
      getColumnManager().setSortAscending(true);
    }
  }

  @Override
  public void searchAll()
  {
    setTitle("Screens");
    initialize(new InMemoryEntityDataModel<Screen,Integer,Screen>(new EntityDataFetcher<Screen,Integer>(Screen.class, _dao) {
      @Override
      public void addDomainRestrictions(HqlBuilder hql)
      {
        super.addDomainRestrictions(hql);
        hql.where(getRootAlias(), "projectPhase", Operator.NOT_EQUAL, ProjectPhase.ANNOTATION);
      }
    }));
    sortByCreationDateDesc();
  }

  private void sortByCreationDateDesc()
  {
    if (getColumnManager().getColumn("Date Recorded") != null) {
      getColumnManager().setSortColumnName("Date Recorded");
      getColumnManager().setSortAscending(false);
    }
  }

  // implementations of the SearchResults abstract methods

  @SuppressWarnings("unchecked")
  protected List<? extends TableColumn<Screen,?>> buildColumns()
  {
    
    List<TableColumn<Screen,?>> columns = Lists.newArrayList();
    columns.addAll(buildScreenSummaryColumns(false));
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
              return sr1.getScreen().getFacilityId().compareTo(sr2.getScreen().getFacilityId());
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
        return Sets.newHashSet(Iterables.transform(screen.getScreenResult().getAssayReadoutTypes(), Functions.toStringFunction())); 
      }
    });
    columns.get(columns.size() - 1).setAdministrative(true);
    columns.get(columns.size() - 1).setVisible(false);
    
    return columns;
  }

  public List<TableColumn<Screen,?>> buildScreenAdminColumns()
  {
    List<TableColumn<Screen,?>> columns = Lists.newArrayList();
    
    columns.add(new EnumEntityColumn<Screen,ScreenDataSharingLevel>(RelationshipPath.from(Screen.class).toProperty("dataSharingLevel"),
      "Data Sharing Level", 
      "The data sharing level", 
      TableColumn.UNGROUPED,
      ScreenDataSharingLevel.values()) {
      @Override
      public ScreenDataSharingLevel getCellValue(Screen screen) { return screen.getDataSharingLevel(); }
    });
    columns.get(columns.size() - 1).setAdministrative(false);
    columns.get(columns.size() - 1).setVisible(false);
    
    columns.add(new DateEntityColumn<Screen>(RelationshipPath.from(Screen.class).toProperty("dataPrivacyExpirationDate"),
      "Data Privacy Expiration Date", 
      "The date on which the screen will become visible to level 1 users", 
      TableColumn.UNGROUPED) {
      @Override
      public LocalDate getDate(Screen screen) { return screen.getDataPrivacyExpirationDate(); }
    });
    columns.get(columns.size() - 1).setAdministrative(true);
    columns.get(columns.size() - 1).setVisible(false);
    
    columns.add(new DateEntityColumn<Screen>(Screen.labActivities.toProperty("dateOfActivity"),
                                             "Date of First Activity", "The date of the first lab activity performed for this screen",
                                             TableColumn.UNGROUPED) {
      @Override
      protected LocalDate getDate(Screen screen)
      {
        return screen.getLabActivities().isEmpty() ? null : screen.getLabActivities().first().getDateOfActivity();
      }
    });
    columns.get(columns.size() - 1).setAdministrative(true);
    columns.get(columns.size() - 1).setVisible(false);

    columns.add(new DateEntityColumn<Screen>(Screen.labActivities.toProperty("dateOfActivity"),
      "Date Of Last Activity", "The date of the last lab activity performed for this screen",
      TableColumn.UNGROUPED) {
      @Override
      protected LocalDate getDate(Screen screen) { return screen.getLabActivities().isEmpty() ? null : screen.getLabActivities().last().getDateOfActivity(); }
    });
    columns.get(columns.size() - 1).setAdministrative(true);
    columns.get(columns.size() - 1).setVisible(false);

    if (!!!getApplicationProperties().isFacility(LincsScreensaverConstants.FACILITY_KEY)) {
      columns.add(new EnumEntityColumn<Screen,ScreenStatus>(Screen.statusItems.toProperty("status"),
                                                            "Status", "The current status of the screen, e.g., 'Completed', 'Ongoing', 'Pending', etc.",
                                                            TableColumn.UNGROUPED,
                                                            ScreenStatus.values()) {
        @Override
        public ScreenStatus getCellValue(Screen screen)
        {
          SortedSet<StatusItem> statusItems = screen.getStatusItems();
          return statusItems.isEmpty() ? null : statusItems.last().getStatus();
        }
      });
      columns.get(columns.size() - 1).setAdministrative(true);

      columns.add(new DateEntityColumn<Screen>(
                                               Screen.statusItems.toProperty("statusDate"),
                                               "Status Date", "The date of the most recent change of status for the screen",
                                               TableColumn.UNGROUPED) {
        @Override
        protected LocalDate getDate(Screen screen)
        {
          SortedSet<StatusItem> statusItems = screen.getStatusItems();
          return statusItems.isEmpty() ? null : statusItems.last().getStatusDate();
        }
      });
      columns.get(columns.size() - 1).setAdministrative(true);
    }

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
    
    columns.add(new IntegerEntityColumn<Screen>(RelationshipPath.from(Screen.class).toProperty("screenedExperimentalWellCount"),
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

    columns.add(new IntegerEntityColumn<Screen>(RelationshipPath.from(Screen.class).toProperty("screenedExperimentalWellCount"),
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
    
    columns.add(new IntegerEntityColumn<Screen>(RelationshipPath.from(Screen.class).toProperty("totalPlatedLabCherryPicks"),
                                                "Total Plated Cherry Picks",
                                                "The number of lab cherry picks that have been plated to date, for all cherry pick requests",
                                                TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(Screen screen) 
      { 
        return screen.getTotalPlatedLabCherryPicks();
      }
    });
    columns.get(columns.size() - 1).setAdministrative(true);
    columns.get(columns.size() - 1).setVisible(false);
        
    columns.add(new TextEntityColumn<Screen>(RelationshipPath.from(Screen.class).toProperty("summary"),
                                                "Summary",
                                                "A summary of the screen",
                                                TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(Screen screen) 
      { 
        return screen.getSummary();
      }
    });
    columns.get(columns.size() - 1).setAdministrative(true);
    columns.get(columns.size() - 1).setVisible(false);    
    
    return columns;
  }

  public List<TableColumn<Screen,?>> buildScreenSummaryColumns(boolean isNonScreenSearchResults)
  {
    List<TableColumn<Screen,?>> columns = Lists.newArrayList();
    columns.add(new TextEntityColumn<Screen>(Screen.facilityId,
                                             "Screen ID",
                                             "The facility-assigned screen identifier",
                                             TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(Screen screen)
      {
        return screen.getFacilityId();
      }

      @Override
      public Object cellAction(Screen screen) {
        // note: we explicitly call getEntityViewer(), since this column may be used in other search result types
        return getEntityViewer().viewEntity(screen);
      }

      @Override
      public boolean isCommandLink() { return true; }
    });

    columns.add(new EnumEntityColumn<Screen,ScreenType>(RelationshipPath.from(Screen.class).toProperty("screenType"),
      "Screen Type", "'RNAi' or 'Small Molecule'", TableColumn.UNGROUPED, ScreenType.values()) {
      @Override
      public ScreenType getCellValue(Screen screen) { return screen.getScreenType(); }
    });

    if (!!!getApplicationProperties().isFacility(LincsScreensaverConstants.FACILITY_KEY)) {
      columns.add(new EnumEntityColumn<Screen,ProjectPhase>(RelationshipPath.from(Screen.class).toProperty("projectPhase"),
                                                            "Project Phase", "'Primary','Counter', or 'Follow-Up'", TableColumn.UNGROUPED, ProjectPhase.values()) {
        @Override
        public ProjectPhase getCellValue(Screen screen)
        {
          return screen.getProjectPhase();
        }
      });
      columns.add(new TextEntityColumn<Screen>(RelationshipPath.from(Screen.class).toProperty("projectId"),
                                               "Project ID", "The project ID of the screen", TableColumn.UNGROUPED) {
        @Override
        public String getCellValue(Screen screen)
        {
          return screen.getProjectId();
        }

        @Override
        public boolean isCommandLink()
        {
          return true;
        }

        @Override
        public Object cellAction(Screen screen)
        {
          searchScreensForProject(screen.getProjectId());
          return BROWSE_SCREENS;
        }
      });
    }

    columns.add(new TextEntityColumn<Screen>(RelationshipPath.from(Screen.class).toProperty("title"),
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

    if (!!!getApplicationProperties().isFacility(LincsScreensaverConstants.FACILITY_KEY)) {
      columns.add(new DateTimeEntityColumn<Screen>(Screen.thisEntity.toProperty("dateCreated"),
                                                   isNonScreenSearchResults ? "Date Screen Recorded" : "Date Recorded",
                                                   "The date the screen was first recorded in the Screensaver",
                                                   TableColumn.UNGROUPED) {
        @Override
        protected DateTime getDateTime(Screen screen)
        {
          return screen.getDateCreated();
        }
      });
    }
    else {
      columns.add(new DateEntityColumn<Screen>(Screen.thisEntity.toProperty("dateCreated"),
        "Date Data Received",
        "The date the screen data was received",
        TableColumn.UNGROUPED) {
        @Override
        protected LocalDate getDate(Screen screen)
        {
          return screen.getDateCreated().toLocalDate();
        }
      });
      
    }   
    
    if (getApplicationProperties().isFacility(LincsScreensaverConstants.FACILITY_KEY)) {
      columns.add(new DateEntityColumn<Screen>(Screen.thisEntity.toProperty("dateLoaded"),
                                                   "Date Loaded",
                                                   "The date the screen data was loaded",
                                                   TableColumn.UNGROUPED) {
        @Override
        protected LocalDate getDate(Screen screen)
        {
          return screen.getDateLoaded()==null ? null : screen.getDateLoaded().toLocalDate();
        }
      });
    }    
    
    if (getApplicationProperties().isFacility(LincsScreensaverConstants.FACILITY_KEY)) {
      columns.add(new DateEntityColumn<Screen>(Screen.thisEntity.toProperty("datePubliclyAvailable"),
                                                   "Date Publicly Available",
                                                   "The date the screen data was made publicly available",
                                                   TableColumn.UNGROUPED) {
        @Override
        protected LocalDate getDate(Screen screen)
        {
          return screen.getDatePubliclyAvailable() == null ? null : screen.getDatePubliclyAvailable().toLocalDate();
        }
      });
    }
    
    // RNAi fields
    columns.add(new TextEntityColumn<Screen>(RelationshipPath.from(Screen.class).toProperty("species"),
      "Species", "The species of the cell organism", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(Screen screen)
      {
        return screen.getSpecies() == null ? null : screen.getSpecies().getValue();
      }
    });
    columns.get(columns.size() - 1).setVisible(false);

    // Non-LINCS Cell Line
    columns.add(new TextEntityColumn<Screen>(RelationshipPath.from(Screen.class).toProperty("cellLine"),
      "Cell Line", "The cell line", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(Screen screen)
      {
        return screen.getCellLine() == null ? null : screen.getCellLine().getValue();
      }
    });
    columns.get(columns.size() - 1).setVisible(false);

    columns.add(new TextEntityColumn<Screen>(RelationshipPath.from(Screen.class).toProperty("transfectionAgent"),
      "Transfection Agent", "The transfection agent", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(Screen screen)
      {
        return screen.getTransfectionAgent() == null ? null : screen.getTransfectionAgent().getValue();
      }
    });
    columns.get(columns.size() - 1).setVisible(false);

    return columns;
  }
}
