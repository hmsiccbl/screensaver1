// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.Activity;
import edu.harvard.med.screensaver.model.VolumeUnit;
import edu.harvard.med.screensaver.model.cherrypicks.LabCherryPick;
import edu.harvard.med.screensaver.model.libraries.WellVolumeAdjustment;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TableColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.DateEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.FixedDecimalEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.IntegerEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.TextEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.UserNameColumn;
import edu.harvard.med.screensaver.ui.arch.searchresults.EntityBasedEntitySearchResults;
import edu.harvard.med.screensaver.ui.cherrypickrequests.CherryPickRequestViewer;
import edu.harvard.med.screensaver.ui.users.UserViewer;

public class WellVolumeAdjustmentSearchResults extends EntityBasedEntitySearchResults<WellVolumeAdjustment,Integer>
{
  private CherryPickRequestViewer _cherryPickRequestViewer;
  private UserViewer _userViewer;
  private GenericEntityDAO _dao;

  /**
   * @motivation for CGLIB2
   */
  protected WellVolumeAdjustmentSearchResults()
  {
  }

  public WellVolumeAdjustmentSearchResults(CherryPickRequestViewer cherryPickRequestViewer,
                                           UserViewer userViewer,
                                           GenericEntityDAO dao)
  {
    _cherryPickRequestViewer = cherryPickRequestViewer;
    _userViewer = userViewer;
    _dao = dao;
  }

  @Override
  protected List<? extends TableColumn<WellVolumeAdjustment,?>> buildColumns()
  {
    List<TableColumn<WellVolumeAdjustment,?>> columns = new ArrayList<TableColumn<WellVolumeAdjustment,?>>();
    columns.add(new DateEntityColumn<WellVolumeAdjustment>(
      WellVolumeAdjustment.wellVolumeorrectionActivity.toProperty("dateOfActivity"),
      "Date", "The date the volume adjustment was made", TableColumn.UNGROUPED) {
      @Override
      protected LocalDate getDate(WellVolumeAdjustment wva)
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
    performedByPropertyPaths.add(WellVolumeAdjustment.wellVolumeorrectionActivity.to(Activity.performedBy).toProperty("lastName"));
    performedByPropertyPaths.add(WellVolumeAdjustment.wellVolumeorrectionActivity.to(Activity.performedBy).toProperty("firstName"));
    columns.add(new UserNameColumn<WellVolumeAdjustment,ScreensaverUser>(
      WellVolumeAdjustment.wellVolumeorrectionActivity.to(Activity.performedBy),
      "Performed By", "The person that performed the volume adjustment", TableColumn.UNGROUPED, _userViewer) {
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
      WellVolumeAdjustment.copy.toProperty("name"),
      "Copy", "The name of the library plate copy", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(WellVolumeAdjustment wva) { return wva.getCopy().getName(); }
    });
    columns.add(new FixedDecimalEntityColumn<WellVolumeAdjustment>(
                                                                   WellVolumeAdjustment.volume,
      "Volume", "The volume adjustment amount", TableColumn.UNGROUPED) {
      @Override
      public BigDecimal getCellValue(WellVolumeAdjustment wva) { return wva.getVolume().getValue(VolumeUnit.MICROLITERS); }
    });
    columns.add(new IntegerEntityColumn<WellVolumeAdjustment>(
      WellVolumeAdjustment.labCherryPick.to(LabCherryPick.cherryPickRequest).toProperty("cherryPickRequestNumber"),
      "Cherry Pick Request", "The cherry pick request that made the volume adjustment", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(WellVolumeAdjustment wva) { return wva.getLabCherryPick() == null ? null : wva.getLabCherryPick().getCherryPickRequest().getCherryPickRequestNumber(); }

      @Override
      public boolean isCommandLink() { return true; }

      @Override
      public Object cellAction(WellVolumeAdjustment entity) { return _cherryPickRequestViewer.viewEntity(entity.getLabCherryPick().getCherryPickRequest()); }
    });
    columns.add(new IntegerEntityColumn<WellVolumeAdjustment>(
      WellVolumeAdjustment.wellVolumeorrectionActivity.toProperty("activityId"),
      "Admin Adjustment", "The well volume correction activity that made the volume adjustment", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(WellVolumeAdjustment wva) { return wva.getWellVolumeCorrectionActivity() == null ? null : wva.getWellVolumeCorrectionActivity().getEntityId(); }
    });
    return columns;
  }

  @Override
  public void searchAll()
  {
    throw new UnsupportedOperationException("not implemented");
  }
}
