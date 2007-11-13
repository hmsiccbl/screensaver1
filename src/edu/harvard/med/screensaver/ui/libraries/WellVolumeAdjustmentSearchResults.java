// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.Activity;
import edu.harvard.med.screensaver.model.libraries.WellVolumeAdjustment;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.ui.screens.CherryPickRequestViewer;
import edu.harvard.med.screensaver.ui.searchresults.DateColumn;
import edu.harvard.med.screensaver.ui.searchresults.FixedDecimalColumn;
import edu.harvard.med.screensaver.ui.searchresults.IntegerColumn;
import edu.harvard.med.screensaver.ui.searchresults.SearchResults;
import edu.harvard.med.screensaver.ui.searchresults.TextColumn;
import edu.harvard.med.screensaver.ui.searchresults.UserNameColumn;
import edu.harvard.med.screensaver.ui.table.TableColumn;

public class WellVolumeAdjustmentSearchResults extends SearchResults<WellVolumeAdjustment>
{
  private CherryPickRequestViewer _cherryPickRequestViewer;
  private GenericEntityDAO _dao;

  /**
   * @motivation for CGLIB2
   */
  protected WellVolumeAdjustmentSearchResults()
  {
  }

  public WellVolumeAdjustmentSearchResults(CherryPickRequestViewer cherryPickRequestViewer,
                                           GenericEntityDAO dao)
  {
    _cherryPickRequestViewer = cherryPickRequestViewer;
    _dao = dao;
  }

  @Override
  protected List<TableColumn<WellVolumeAdjustment,?>> getColumns()
  {
    List<TableColumn<WellVolumeAdjustment,?>> columns = new ArrayList<TableColumn<WellVolumeAdjustment,?>>();
    columns.add(new DateColumn<WellVolumeAdjustment>("Date", "The date the volume adjustment was made") {
      @Override
      protected Date getDate(WellVolumeAdjustment wva)
      {
        Activity activity = wva.getRelatedActivity();
        if (activity != null) {
          return activity.getDateOfActivity();
        }
        if (wva.getLabCherryPick() != null) {
          // TODO: this should really be the date that liquid allocation was made, but we're not currently recording that
          return wva.getLabCherryPick().getCherryPickRequest().getDateRequested();
        }
        return null;
      }
    });
    columns.add(new UserNameColumn<WellVolumeAdjustment>("Performed By", "The person that performed the volume adjustment", _dao) {
      @Override
      protected ScreensaverUser getUser(WellVolumeAdjustment wva)
      {
        Activity activity = wva.getRelatedActivity();
        if (activity != null) {
          return activity.getPerformedBy();
        }
        return null;
      }
    });
    columns.add(new TextColumn<WellVolumeAdjustment>("Copy", "The name of the library plate copy") {
      @Override
      public String getCellValue(WellVolumeAdjustment wva) { return wva.getCopy().getName(); }
    });
    columns.add(new FixedDecimalColumn<WellVolumeAdjustment>("Volume", "The volume adjustment amount") {
      @Override
      public BigDecimal getCellValue(WellVolumeAdjustment wva) { return wva.getMicroliterVolume(); }
    });
    columns.add(new IntegerColumn<WellVolumeAdjustment>("Cherry Pick Request", "The cherry pick request that made the volume adjustment") {
      @Override
      public Integer getCellValue(WellVolumeAdjustment wva) { return wva.getLabCherryPick() == null ? null : wva.getLabCherryPick().getCherryPickRequest().getCherryPickRequestNumber(); }

      @Override
      public boolean isCommandLink() { return true; }

      @Override
      public Object cellAction(WellVolumeAdjustment entity) { return _cherryPickRequestViewer.viewCherryPickRequest(entity.getLabCherryPick().getCherryPickRequest()); }
    });
    columns.add(new IntegerColumn<WellVolumeAdjustment>("Admin Adjustment", "The well volume correction activity that made the volume adjustment") {
      @Override
      public Integer getCellValue(WellVolumeAdjustment wva) { return wva.getWellVolumeCorrectionActivity() == null ? null : wva.getWellVolumeCorrectionActivity().getEntityId(); }
    });
    return columns;
  }
}
