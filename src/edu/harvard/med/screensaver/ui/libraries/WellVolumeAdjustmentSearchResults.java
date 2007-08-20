// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.harvard.med.screensaver.io.DataExporter;
import edu.harvard.med.screensaver.model.Activity;
import edu.harvard.med.screensaver.model.libraries.WellVolumeAdjustment;
import edu.harvard.med.screensaver.ui.control.ScreensController;
import edu.harvard.med.screensaver.ui.searchresults.SearchResults;
import edu.harvard.med.screensaver.ui.table.TableColumn;

public class WellVolumeAdjustmentSearchResults extends SearchResults<WellVolumeAdjustment> 
{
  private ScreensController _screensController;

  public WellVolumeAdjustmentSearchResults(Collection<WellVolumeAdjustment> unsortedResults, 
                                           ScreensController screensController)
  {
    super(unsortedResults);
    _screensController = screensController;
  }

  @Override
  protected List<TableColumn<WellVolumeAdjustment>> getColumns()
  {
    List<TableColumn<WellVolumeAdjustment>> columns = new ArrayList<TableColumn<WellVolumeAdjustment>>();
    columns.add(new TableColumn<WellVolumeAdjustment>("Date", "The date the volume adjustment was made") {
      @Override
      public Object getCellValue(WellVolumeAdjustment wva) 
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
    columns.add(new TableColumn<WellVolumeAdjustment>("Performed By", "The person that performed the volume adjustment") {
      @Override
      public Object getCellValue(WellVolumeAdjustment wva) 
      { 
        Activity activity = wva.getRelatedActivity();
        if (activity != null) {
          return activity.getPerformedBy().getFullNameLastFirst();
        }
        return null;
      }
    });
    columns.add(new TableColumn<WellVolumeAdjustment>("Copy", "The name of the library plate copy") {
      @Override
      public Object getCellValue(WellVolumeAdjustment wva) { return wva.getCopy().getName(); }
    });
    columns.add(new TableColumn<WellVolumeAdjustment>("Volume", "The volume adjustment amount", true) {
      @Override
      public Object getCellValue(WellVolumeAdjustment wva) { return wva.getMicroliterVolume(); }
    });
    columns.add(new TableColumn<WellVolumeAdjustment>("Cherry Pick Request", "The cherry pick request that made the volume adjustment", true) {
      @Override
      public Object getCellValue(WellVolumeAdjustment wva) { return wva.getLabCherryPick() == null ? null : wva.getLabCherryPick().getCherryPickRequest().getCherryPickRequestNumber(); }
      
      @Override
      public boolean isCommandLink() { return true; }
      
      @Override
      public Object cellAction(WellVolumeAdjustment entity) { return _screensController.viewCherryPickRequest(entity.getLabCherryPick().getCherryPickRequest()); }
    });
    columns.add(new TableColumn<WellVolumeAdjustment>("Admin Adjustment", "The well volume correction activity that made the volume adjustment", true) {
      @Override
      public Object getCellValue(WellVolumeAdjustment wva) { return wva.getWellVolumeCorrectionActivity() == null ? null : wva.getWellVolumeCorrectionActivity().getEntityId(); }
    });
    return columns;
  }
  
  @Override
  protected List<DataExporter<WellVolumeAdjustment>> getDataExporters() { return null; }

  @Override
  protected void setEntityToView(WellVolumeAdjustment entity) {}

  @Override
  public String showSummaryView() { return null; }
}
