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
import edu.harvard.med.screensaver.model.PropertyPath;
import edu.harvard.med.screensaver.model.RelationshipPath;
import edu.harvard.med.screensaver.model.libraries.WellVolumeAdjustment;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.ui.screens.CherryPickRequestViewer;
import edu.harvard.med.screensaver.ui.searchresults.EntitySearchResults;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.DateEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.EntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.FixedDecimalEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.IntegerEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.TextEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.UserNameColumn;

public class WellVolumeAdjustmentSearchResults extends EntitySearchResults<WellVolumeAdjustment,Integer>
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
  protected List<? extends TableColumn<WellVolumeAdjustment,?>> buildColumns()
  {
    List<EntityColumn<WellVolumeAdjustment,?>> columns = new ArrayList<EntityColumn<WellVolumeAdjustment,?>>();
    columns.add(new DateEntityColumn<WellVolumeAdjustment>(
      new PropertyPath<WellVolumeAdjustment>(WellVolumeAdjustment.class, "activity", "dateOfActivity"),
      "Date", "The date the volume adjustment was made", TableColumn.UNGROUPED) {
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
    List<PropertyPath<WellVolumeAdjustment>> performedByPropertyPaths = new ArrayList<PropertyPath<WellVolumeAdjustment>>();
    performedByPropertyPaths.add(new PropertyPath<WellVolumeAdjustment>(WellVolumeAdjustment.class, "activity.performedBy", "lastName"));
    performedByPropertyPaths.add(new PropertyPath<WellVolumeAdjustment>(WellVolumeAdjustment.class, "activity.performedBy", "firstName"));
    columns.add(new UserNameColumn<WellVolumeAdjustment>(
      new RelationshipPath<WellVolumeAdjustment>(WellVolumeAdjustment.class, "activity.performedBy"),
      "Performed By", "The person that performed the volume adjustment", TableColumn.UNGROUPED) {
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
    columns.add(new TextEntityColumn<WellVolumeAdjustment>(
      new PropertyPath<WellVolumeAdjustment>(WellVolumeAdjustment.class, "copy", "name"),
      "Copy", "The name of the library plate copy", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(WellVolumeAdjustment wva) { return wva.getCopy().getName(); }
    });
    columns.add(new FixedDecimalEntityColumn<WellVolumeAdjustment>(
      new PropertyPath<WellVolumeAdjustment>(WellVolumeAdjustment.class, "microliterVolume"),
      "Volume", "The volume adjustment amount", TableColumn.UNGROUPED) {
      @Override
      public BigDecimal getCellValue(WellVolumeAdjustment wva) { return wva.getMicroliterVolume(); }
    });
    columns.add(new IntegerEntityColumn<WellVolumeAdjustment>(
      new PropertyPath<WellVolumeAdjustment>(WellVolumeAdjustment.class, "labCherryPick.cherryPickRequest", "cherryPickRequestNumber"),
      "Cherry Pick Request", "The cherry pick request that made the volume adjustment", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(WellVolumeAdjustment wva) { return wva.getLabCherryPick() == null ? null : wva.getLabCherryPick().getCherryPickRequest().getCherryPickRequestNumber(); }

      @Override
      public boolean isCommandLink() { return true; }

      @Override
      public Object cellAction(WellVolumeAdjustment entity) { return _cherryPickRequestViewer.viewCherryPickRequest(entity.getLabCherryPick().getCherryPickRequest()); }
    });
    columns.add(new IntegerEntityColumn<WellVolumeAdjustment>(new PropertyPath<WellVolumeAdjustment>(WellVolumeAdjustment.class, "wellVolumeCorrectionActivity", "activityId"),
      "Admin Adjustment", "The well volume correction activity that made the volume adjustment", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(WellVolumeAdjustment wva) { return wva.getWellVolumeCorrectionActivity() == null ? null : wva.getWellVolumeCorrectionActivity().getEntityId(); }
    });
    return columns;
  }

  @Override
  protected void setEntityToView(WellVolumeAdjustment entity)
  {
    // TODO Auto-generated method stub
  }
}
