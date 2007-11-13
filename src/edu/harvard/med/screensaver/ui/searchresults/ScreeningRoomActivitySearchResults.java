// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
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
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.Activity;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickLiquidTransfer;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.RNAiCherryPickScreening;
import edu.harvard.med.screensaver.model.screens.ScreeningRoomActivity;
import edu.harvard.med.screensaver.ui.screens.ScreenViewer;
import edu.harvard.med.screensaver.ui.table.TableColumn;

import org.apache.log4j.Logger;

import sun.security.krb5.internal.ac;

public class ScreeningRoomActivitySearchResults extends ActivitySearchResults
{
  // static members

  private static final Set<String> ACTIVITY_TYPES = new HashSet<String>();
  static {
    // TODO: automate this w/reflection
    // TODO: use activityTypeName, not class name!
    ACTIVITY_TYPES.add(CherryPickLiquidTransfer.class.getSimpleName());
    ACTIVITY_TYPES.add(LibraryScreening.class.getSimpleName());
    ACTIVITY_TYPES.add(RNAiCherryPickScreening.class.getSimpleName());
  }

  private static Logger log = Logger.getLogger(ScreeningRoomActivitySearchResults.class);
  private List<TableColumn<? extends Activity,?>> _columns;
  private ScreenViewer _screenViewer;

  /**
   * @motivation for CGLIB2
   */
  protected ScreeningRoomActivitySearchResults()
  {
  }

  public ScreeningRoomActivitySearchResults(//ActivityViewer activityViewer,
                                            ScreenViewer screenViewer,
                                            GenericEntityDAO dao)
  {
    super(dao);
    _screenViewer = screenViewer;
    //_activityViewer = activityViewer;
  }

  @Override
  protected List getColumns()
  {
    if (_columns == null) {
      _columns = super.getColumns();
      _columns.add(0, new IntegerColumn<ScreeningRoomActivity>("Screen Number", "The screen number") {
        @Override
        public Integer getCellValue(ScreeningRoomActivity activity) { return activity.getScreen().getScreenNumber(); }

        @Override
        public Object cellAction(ScreeningRoomActivity activity) { return _screenViewer.viewScreen(activity.getScreen()); }

        @Override
        public boolean isCommandLink() { return true; }
      });
    }
    return _columns;
  }

  @Override
  protected Set getActivityTypes()
  {
    return ACTIVITY_TYPES;
  }

  @Override
  protected void setEntityToView(AbstractEntity entity)
  {

  }

}
