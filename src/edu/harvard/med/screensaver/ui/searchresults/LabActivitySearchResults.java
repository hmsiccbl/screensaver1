// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.datafetcher.ParentedEntityDataFetcher;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickAssayPlate;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickLiquidTransfer;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.screens.LabActivity;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.RNAiCherryPickScreening;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.ui.activities.ActivityViewer;
import edu.harvard.med.screensaver.ui.cherrypickrequests.CherryPickRequestViewer;
import edu.harvard.med.screensaver.ui.screens.ScreenViewer;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.IntegerEntityColumn;
import edu.harvard.med.screensaver.ui.users.UserViewer;

import org.apache.log4j.Logger;

public class LabActivitySearchResults extends ActivitySearchResults<LabActivity>
{
  // static members

  private static final Set<String> ACTIVITY_TYPES = new HashSet<String>();
  static {
    ACTIVITY_TYPES.add(CherryPickLiquidTransfer.ACTIVITY_TYPE_NAME);
    ACTIVITY_TYPES.add(LibraryScreening.ACTIVITY_TYPE_NAME);
    ACTIVITY_TYPES.add(RNAiCherryPickScreening.ACTIVITY_TYPE_NAME);;
  }

  private static Logger log = Logger.getLogger(LabActivitySearchResults.class);
  private ScreenViewer _screenViewer;
  private CherryPickRequestViewer _cprViewer;

  /**
   * @motivation for CGLIB2
   */
  protected LabActivitySearchResults()
  {
  }

  public LabActivitySearchResults(ActivityViewer activityViewer,
                                  ScreenViewer screenViewer,
                                  UserViewer userViewer,
                                  CherryPickRequestViewer cprViewer,
                                  GenericEntityDAO dao)
  {
    super(activityViewer, userViewer, LabActivity.class, dao);
    _screenViewer = screenViewer;
    _cprViewer = cprViewer;
  }

  public void searchLabActivitiesForScreen(Screen screen)
  {
    initialize(new ParentedEntityDataFetcher<LabActivity,Integer>(LabActivity.class,
      new RelationshipPath<LabActivity>(LabActivity.class, "screen"),
      screen,
      _dao));
  }

  @SuppressWarnings("unchecked")
  @Override
  protected List<? extends TableColumn<LabActivity,?>> buildColumns()
  {
    List<TableColumn<LabActivity,?>> columns =
      (List<TableColumn<LabActivity,?>>) super.buildColumns();
    columns.add(1, new IntegerEntityColumn<LabActivity>(
      LabActivity.Screen.toProperty("screenNumber"),
      "Screen Number", "The screen number", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(LabActivity activity) { return activity.getScreen().getScreenNumber(); }

      @Override
      public Object cellAction(LabActivity activity) { return _screenViewer.viewScreen(activity.getScreen()); }

      @Override
      public boolean isCommandLink() { return true; }
    });
    IntegerEntityColumn<LabActivity> column = new IntegerEntityColumn<LabActivity>(
      (RelationshipPath) RNAiCherryPickScreening.rnaiCherryPickRequest,
      "Cherry Pick Request #", "The cherry pick request number, if applicable", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(LabActivity activity) 
      { 
        CherryPickRequest cherryPickRequest = getCherryPickRequest(activity);
        return cherryPickRequest == null ? null : cherryPickRequest.getCherryPickRequestNumber();
      }

      private CherryPickRequest getCherryPickRequest(LabActivity activity)
      {
        if (activity instanceof CherryPickLiquidTransfer) {
          return ((CherryPickLiquidTransfer) activity).getCherryPickRequest();
        }
        else if (activity instanceof RNAiCherryPickScreening) {
          return ((RNAiCherryPickScreening) activity).getRnaiCherryPickRequest();
        }
        return null;
      }

      @Override
      public Object cellAction(LabActivity activity) { return _cprViewer.viewCherryPickRequest(getCherryPickRequest(activity)); }

      @Override
      public boolean isCommandLink() { return true; }
    };
    column.addRelationshipPath((RelationshipPath) CherryPickLiquidTransfer.cherryPickAssayPlates.to(CherryPickAssayPlate.cherryPickRequest));
    column.setVisible(false);
    columns.add(2, column);
    return columns;
  }

  @Override
  protected Set<String> getActivityTypes()
  {
    return ACTIVITY_TYPES;
  }
}
