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
import edu.harvard.med.screensaver.model.PropertyPath;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickLiquidTransfer;
import edu.harvard.med.screensaver.model.screens.LabActivity;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.RNAiCherryPickScreening;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.ui.activities.ActivityViewer;
import edu.harvard.med.screensaver.ui.screens.ScreenViewer;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.EntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.IntegerEntityColumn;

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

  /**
   * @motivation for CGLIB2
   */
  protected LabActivitySearchResults()
  {
  }

  public void searchActivitiesForScreen(Screen screen)
  {
  }

  public void searchActivitiesForUser(ScreensaverUser user)
  {

  }

  public LabActivitySearchResults(ActivityViewer activityViewer,
                                  ScreenViewer screenViewer,
                                  GenericEntityDAO dao)
  {
    super(activityViewer, LabActivity.class, dao);
    _screenViewer = screenViewer;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected List<? extends TableColumn<LabActivity,?>> buildColumns()
  {
    List<EntityColumn<LabActivity,?>> columns = 
      (List<EntityColumn<LabActivity,?>>) super.buildColumns();
    columns.add(1, new IntegerEntityColumn<LabActivity>(
      new PropertyPath<LabActivity>(LabActivity.class, "screen", "screenNumber"),
      "Screen Number", "The screen number", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(LabActivity activity) { return activity.getScreen().getScreenNumber(); }

      @Override
      public Object cellAction(LabActivity activity) { return _screenViewer.viewScreen(activity.getScreen()); }

      @Override
      public boolean isCommandLink() { return true; }
    });
    return columns;
  }

  @Override
  protected Set<String> getActivityTypes()
  {
    return ACTIVITY_TYPES;
  }

  @Override
  protected void setEntityToView(LabActivity activity)
  {
    _activityViewer.viewActivity(activity, VIEW_ACTIVITY);
  }

}
