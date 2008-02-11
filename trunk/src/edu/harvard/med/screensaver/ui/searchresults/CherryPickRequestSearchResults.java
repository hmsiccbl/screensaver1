// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/ui/searchresults/ScreenSearchResults.java $
// $Id: ScreenSearchResults.java 2038 2007-11-15 17:24:49Z ant4 $

// Copyright 2006 by the President and Fellows of Harvard College.

// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.StatusItem;
import edu.harvard.med.screensaver.model.screens.StatusValue;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.ui.screens.CherryPickRequestViewer;
import edu.harvard.med.screensaver.ui.screens.ScreenViewer;
import edu.harvard.med.screensaver.ui.table.TableColumn;

import org.apache.poi.hssf.contrib.view.SViewer;


/**
 * A {@link SearchResults} for {@link CherryPickRequest CherryPickRequests}.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class CherryPickRequestSearchResults extends EntitySearchResults<CherryPickRequest>
{

  // private static final fields


  // instance fields

  private CherryPickRequestViewer _cprViewer;
  private ScreenViewer _screenViewer;
  private GenericEntityDAO _dao;

  private ArrayList<TableColumn<CherryPickRequest,?>> _columns;


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

  @Override
  public void setContents(Collection<? extends CherryPickRequest> unsortedResults,
                          String description)
  {
    super.setContents(unsortedResults, description);
    // default to descending sort order on cherry pick request number
    getSortManager().setSortAscending(false);
  }


  // implementations of the SearchResults abstract methods

  protected List<TableColumn<CherryPickRequest,?>> getColumns()
  {
    if (_columns == null) {
      _columns = new ArrayList<TableColumn<CherryPickRequest,?>>();
      _columns.add(new IntegerColumn<CherryPickRequest>("CPR #", "The cherry pick request number") {
        @Override
        public Integer getCellValue(CherryPickRequest cpr) { return cpr.getCherryPickRequestNumber(); }

        @Override
        public Object cellAction(CherryPickRequest cpr) { return viewCurrentEntity(); }

        @Override
        public boolean isCommandLink() { return true; }
      });
      _columns.add(new IntegerColumn<CherryPickRequest>("Screen #", "The screen number of the cherry pick request's screen") {
        @Override
        public Integer getCellValue(CherryPickRequest cpr) { return cpr.getScreen().getScreenNumber(); }

        @Override
        public Object cellAction(CherryPickRequest cpr) { return _screenViewer.viewScreen(cpr.getScreen()); }

        @Override
        public boolean isCommandLink() { return true; }
      });
      _columns.add(new DateColumn<CherryPickRequest>("Date Requested", "The date of the cherry pick request") {
        @Override
        protected Date getDate(CherryPickRequest cpr) { return cpr.getDateRequested(); }
      });
      _columns.add(new UserNameColumn<CherryPickRequest>("Requested By", "The person that requested the cherry picks") {
        @Override
        public ScreensaverUser getUser(CherryPickRequest cpr) { return cpr.getRequestedBy(); }
      });
      _columns.add(new BooleanColumn<CherryPickRequest>("Completed", "Has the cherry pick request been completed, such that all cherry pick plates have been plated") {
        @Override
        public Boolean getCellValue(CherryPickRequest cpr) { return cpr.isPlated(); }
      });
      _columns.add(new IntegerColumn<CherryPickRequest>("# Plates", "The total number of cherry pick plates") {
        @Override
        public Integer getCellValue(CherryPickRequest cpr) { return cpr.getActiveCherryPickAssayPlates().size(); }
      });
      _columns.add(new IntegerColumn<CherryPickRequest>("# Plates Completed", "The number of cherry pick plates that have been plated (completed)") {
        @Override
        public Integer getCellValue(CherryPickRequest cpr) { return cpr.getCompletedCherryPickAssayPlates().size(); }
      });
      _columns.add(new IntegerColumn<CherryPickRequest>("# Unfulfilled LCPs", "The number of lab cherry picks that have are unfulfilled.") {
        @Override
        public Integer getCellValue(CherryPickRequest cpr) { return cpr.getNumberUnfulfilledLabCherryPicks(); }
      });
      _columns.add(new UserNameColumn<CherryPickRequest>("Lab Head", "The head of the lab performing the screen") {
        @Override
        public ScreensaverUser getUser(CherryPickRequest cpr) { return cpr.getScreen().getLabHead(); }
      });
      _columns.add(new UserNameColumn<CherryPickRequest>("Lead Screener", "The scientist primarily responsible for running the screen") {
        @Override
        public ScreensaverUser getUser(CherryPickRequest cpr) { return cpr.getScreen().getLeadScreener(); }
      });
      _columns.add(new EnumColumn<CherryPickRequest, ScreenType>("Screen Type", "'RNAi' or 'Small Molecule'", ScreenType.values()) {
        @Override
        public ScreenType getCellValue(CherryPickRequest cpr) { return cpr.getScreen().getScreenType(); }
      });
    }
    return _columns;
  }

  @Override
  protected List<Integer[]> getCompoundSorts()
  {
    List<Integer[]> compoundSorts = super.getCompoundSorts();
    compoundSorts.add(new Integer[] {1, 0});
    compoundSorts.add(new Integer[] {2, 1, 0});
    compoundSorts.add(new Integer[] {3, 1, 0});
    compoundSorts.add(new Integer[] {4, 1, 0});
    compoundSorts.add(new Integer[] {5, 6, 1, 0});
    compoundSorts.add(new Integer[] {6, 5, 1, 0});
    compoundSorts.add(new Integer[] {7, 1, 0});
    compoundSorts.add(new Integer[] {8, 1, 0});
    compoundSorts.add(new Integer[] {9, 1, 0});
    return compoundSorts;
  }

  @Override
  protected void setEntityToView(CherryPickRequest cpr)
  {
    _cprViewer.viewCherryPickRequest(cpr);
  }
}
