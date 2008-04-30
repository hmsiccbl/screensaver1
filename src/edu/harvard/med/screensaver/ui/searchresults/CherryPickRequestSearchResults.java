//$HeadURL$
//$Id$

//Copyright 2006 by the President and Fellows of Harvard College.

//Screensaver is an open-source project developed by the ICCB-L and NSRB labs
//at Harvard Medical School. This software is distributed under the terms of
//the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.datafetcher.AllEntitiesOfTypeDataFetcher;
import edu.harvard.med.screensaver.db.datafetcher.EntityDataFetcher;
import edu.harvard.med.screensaver.model.PropertyPath;
import edu.harvard.med.screensaver.model.RelationshipPath;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.ui.cherrypickrequests.CherryPickRequestViewer;
import edu.harvard.med.screensaver.ui.screens.ScreenViewer;
import edu.harvard.med.screensaver.ui.table.Criterion;
import edu.harvard.med.screensaver.ui.table.Criterion.Operator;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.BooleanEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.DateEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.EntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.EnumEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.IntegerEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.UserNameColumn;


/**
 * A {@link SearchResults} for {@link CherryPickRequest CherryPickRequests}.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class CherryPickRequestSearchResults extends EntitySearchResults<CherryPickRequest,Integer>
{

  // private static final fields


  // instance fields

  private CherryPickRequestViewer _cprViewer;
  private ScreenViewer _screenViewer;
  private GenericEntityDAO _dao;


  // public constructor

  /**
   * @motivation for CGLIB2
   */
  protected CherryPickRequestSearchResults()
  {
  }

  public CherryPickRequestSearchResults(CherryPickRequestViewer cprViewer,
                                        ScreenViewer screenViewer,
                                        GenericEntityDAO dao)
  {
    _cprViewer = cprViewer;
    _screenViewer = screenViewer;
    _dao = dao;
  }

  public void searchAll()
  {
    EntityDataFetcher<CherryPickRequest,Integer> dataFetcher =
      (EntityDataFetcher<CherryPickRequest,Integer>) new AllEntitiesOfTypeDataFetcher<CherryPickRequest,Integer>(CherryPickRequest.class, _dao);
    initialize(dataFetcher);

    // default to descending sort order on cherry pick request number
    getColumnManager().setSortAscending(false);
  }

  @SuppressWarnings("unchecked")
  public void searchScreenType(ScreenType screenType)
  {
    searchAll();
    EntityColumn<CherryPickRequest,ScreenType> column = (EntityColumn<CherryPickRequest,ScreenType>) getColumnManager().getColumn("Screen Type");
    column.clearCriteria();
    column.addCriterion(new Criterion<ScreenType>(Operator.EQUAL, screenType));
  }

  public void searchForUser(ScreensaverUser screensaverUser)
  {

  }

  // implementations of the SearchResults abstract methods

  @Override
  protected List<EntityColumn<CherryPickRequest,?>> buildColumns()
  {
    List<EntityColumn<CherryPickRequest,?>> columns = new ArrayList<EntityColumn<CherryPickRequest,?>>();
    columns.add(new IntegerEntityColumn<CherryPickRequest>(
      new PropertyPath<CherryPickRequest>(CherryPickRequest.class, "cherryPickRequestId"),
      "CPR #", "The cherry pick request number", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(CherryPickRequest cpr) { return cpr.getCherryPickRequestId(); }

      @Override
      public Object cellAction(CherryPickRequest cpr) { return viewCurrentEntity(); }

      @Override
      public boolean isCommandLink() { return true; }
    });
    columns.add(new IntegerEntityColumn<CherryPickRequest>(
      new PropertyPath<CherryPickRequest>(CherryPickRequest.class, "cherryPickRequestId"),
      "Visit #", "The legacy ScreenDB visit number (for legacy, imported cherry pick requests)", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(CherryPickRequest cpr) { return cpr.getLegacyCherryPickRequestNumber(); }

      @Override
      public Object cellAction(CherryPickRequest cpr) { return viewCurrentEntity(); }

      @Override
      public boolean isCommandLink() { return true; }
    });
    columns.add(new IntegerEntityColumn<CherryPickRequest>(
      new PropertyPath<CherryPickRequest>(CherryPickRequest.class, "screen", "screenNumber"),
      "Screen #", "The screen number of the cherry pick request's screen", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(CherryPickRequest cpr) { return cpr.getScreen().getScreenNumber(); }

      @Override
      public Object cellAction(CherryPickRequest cpr) { return _screenViewer.viewScreen(cpr.getScreen()); }

      @Override
      public boolean isCommandLink() { return true; }
    });
    columns.add(new DateEntityColumn<CherryPickRequest>(
      new PropertyPath<CherryPickRequest>(CherryPickRequest.class, "dateRequested"),
      "Date Requested", "The date of the cherry pick request", TableColumn.UNGROUPED) {
      @Override
      protected Date getDate(CherryPickRequest cpr) { return cpr.getDateRequested(); }
    });
    columns.add(new UserNameColumn<CherryPickRequest>(
      new RelationshipPath<CherryPickRequest>(CherryPickRequest.class, "requestedBy"),
      "Requested By", "The person that requested the cherry picks", TableColumn.UNGROUPED) {
      @Override
      public ScreensaverUser getUser(CherryPickRequest cpr) { return cpr.getRequestedBy(); }
    });
    columns.add(new BooleanEntityColumn<CherryPickRequest>(
      new RelationshipPath<CherryPickRequest>(CherryPickRequest.class, "cherryPickAssayPlates"),
      "Completed", "Has the cherry pick request been completed, such that all cherry pick plates have been plated", 
      TableColumn.UNGROUPED) {
      @Override
      public Boolean getCellValue(CherryPickRequest cpr) { return cpr.isPlated(); }
    });
    columns.get(columns.size() - 1).addRelationshipPath(new RelationshipPath<CherryPickRequest>(CherryPickRequest.class,
      "cherryPickAssayPlates.cherryPickLiquidTransfer"));
    columns.add(new IntegerEntityColumn<CherryPickRequest>(
      new RelationshipPath<CherryPickRequest>(CherryPickRequest.class, "cherryPickAssayPlates"),
      "# Plates", "The total number of cherry pick plates", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(CherryPickRequest cpr) { return cpr.getActiveCherryPickAssayPlates().size(); }
    });
    columns.add(new IntegerEntityColumn<CherryPickRequest>(
      new RelationshipPath<CherryPickRequest>(CherryPickRequest.class, "cherryPickAssayPlates"),
      "# Plates Completed", "The number of cherry pick plates that have been plated (completed)", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(CherryPickRequest cpr) { return cpr.getCompletedCherryPickAssayPlates().size(); }
    });
    columns.add(new IntegerEntityColumn<CherryPickRequest>(
      new PropertyPath<CherryPickRequest>(CherryPickRequest.class, "numberUnfulfilledLabCherryPicks"),
      "# Unfulfilled LCPs", "The number of lab cherry picks that have are unfulfilled.", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(CherryPickRequest cpr) { return cpr.getNumberUnfulfilledLabCherryPicks(); }
    });
    columns.add(new UserNameColumn<CherryPickRequest>(
      new RelationshipPath<CherryPickRequest>(CherryPickRequest.class, "screen.labHead"),
      "Lab Head", "The head of the lab performing the screen", TableColumn.UNGROUPED) {
      @Override
      public ScreensaverUser getUser(CherryPickRequest cpr) { return cpr.getScreen().getLabHead(); }
    });
    columns.add(new UserNameColumn<CherryPickRequest>(
      new RelationshipPath<CherryPickRequest>(CherryPickRequest.class, "screen.leadScreener"),
      "Lead Screener", "The scientist primarily responsible for running the screen", TableColumn.UNGROUPED) {
      @Override
      public ScreensaverUser getUser(CherryPickRequest cpr) { return cpr.getScreen().getLeadScreener(); }
    });
    columns.add(new EnumEntityColumn<CherryPickRequest, ScreenType>(
      new PropertyPath<CherryPickRequest>(CherryPickRequest.class, "screen", "screenType"),
      "Screen Type", "'RNAi' or 'Small Molecule'", TableColumn.UNGROUPED, ScreenType.values()) {
      @Override
      public ScreenType getCellValue(CherryPickRequest cpr) { return cpr.getScreen().getScreenType(); }
    });
    return columns;
  }

//@Override
//protected List<Integer[]> getCompoundSorts()
//{
//List<Integer[]> compoundSorts = super.getCompoundSorts();
//compoundSorts.add(new Integer[] {1, 0});
//compoundSorts.add(new Integer[] {2, 1, 0});
//compoundSorts.add(new Integer[] {3, 1, 0});
//compoundSorts.add(new Integer[] {4, 1, 0});
//compoundSorts.add(new Integer[] {5, 6, 1, 0});
//compoundSorts.add(new Integer[] {6, 5, 1, 0});
//compoundSorts.add(new Integer[] {7, 1, 0});
//compoundSorts.add(new Integer[] {8, 1, 0});
//compoundSorts.add(new Integer[] {9, 1, 0});
//return compoundSorts;
//}

  @Override
  protected void setEntityToView(CherryPickRequest cpr)
  {
    _cprViewer.viewCherryPickRequest(cpr);
  }
}
