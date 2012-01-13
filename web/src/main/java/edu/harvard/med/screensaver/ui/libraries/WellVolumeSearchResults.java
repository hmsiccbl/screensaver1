// $HeadURL:
// svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/ui/libraries/WellVolumeSearchResults.java
// $
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.libraries.WellVolume;
import edu.harvard.med.screensaver.ui.arch.datatable.column.ColumnType;
import edu.harvard.med.screensaver.ui.arch.datatable.column.IntegerColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.SetColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TableColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TextColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.VolumeColumn;
import edu.harvard.med.screensaver.ui.arch.searchresults.EntityBasedEntitySearchResults;
import edu.harvard.med.screensaver.ui.cherrypickrequests.CherryPickRequestViewer;

/**
 * Aggregates WellVolumeAdjustments into WellVolumes, and provides these
 * WellVolumes as a SearchResult. Underlying data is set via methods
 * {@link WellCopyVolumeSearchResults}.
 *
 * @see WellCopyVolumeSearchResults
 */
public class WellVolumeSearchResults extends EntityBasedEntitySearchResults<WellVolume,String>
{
  private static Logger log = Logger.getLogger(WellVolumeSearchResults.class);

  private GenericEntityDAO _dao;
  private LibraryViewer _libraryViewer;
  private WellViewer _wellViewer;
  private CherryPickRequestViewer _cherryPickRequestViewer;
  //private TextColumn<Well> _maxRemainingVolumeColumn;


  /**
   * @motivation for CGLIB2
   */
  protected WellVolumeSearchResults() {}

  public WellVolumeSearchResults(GenericEntityDAO dao,
                                 LibraryViewer libraryViewer,
                                 WellViewer wellViewer,
                                 CherryPickRequestViewer cherryPickRequestViewer)
  {
    _dao = dao;
    _libraryViewer = libraryViewer;
    _wellViewer = wellViewer;
    _cherryPickRequestViewer = cherryPickRequestViewer;
  }

  @Override
  protected List<? extends TableColumn<WellVolume,?>> buildColumns()
  {
    List<TableColumn<WellVolume,?>> columns = new ArrayList<TableColumn<WellVolume,?>>();
    columns.add(new TextColumn<WellVolume>("Library",
                                           "The library containing the well", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(WellVolume wellVolume)
      {
        return wellVolume.getWell().getLibrary().getLibraryName();
      }

      @Override
      public boolean isCommandLink()
      {
        return true;
      }

      @Override
      public Object cellAction(WellVolume wellVolume)
      {
        return _libraryViewer.viewEntity(wellVolume.getWell().getLibrary());
      }
    });
    columns.add(new IntegerColumn<WellVolume>("Plate",
                                              "The number of the plate the well is located on", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(WellVolume wellVolume)
      {
        return wellVolume.getWell().getPlateNumber();
      }
    });
    columns.add(new TextColumn<WellVolume>("Well",
                                           "The plate coordinates of the well", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(WellVolume wellVolume)
      {
        return wellVolume.getWell().getWellName();
      }

      @Override
      public boolean isCommandLink()
      {
        return true;
      }

      @Override
      public Object cellAction(WellVolume wellVolume)
      {
        return _wellViewer.viewEntity(wellVolume.getWell());
      }
    });
    columns.add(new SetColumn<WellVolume,String>("Copies",
      "The copies of this well", TableColumn.UNGROUPED, ColumnType.TEXT_SET) {
      @Override
      public Set<String> getCellValue(WellVolume wellVolume)
      {
        return Sets.newTreeSet(wellVolume.getActiveWellInfo().getCopiesList());
      }
    });
    columns.add(new VolumeColumn<WellVolume>("Total Initial Copy Volume",
      "The sum of initial volumes from all copies of this well", TableColumn.UNGROUPED) {
      @Override
      public Volume getCellValue(WellVolume wellVolume)
      {
        return wellVolume.getActiveWellInfo().getTotalInitialVolume();
      }
    });
    columns.add(new VolumeColumn<WellVolume>("Consumed Volume",
      "The cumulative volume already used from this well, across all copies", TableColumn.UNGROUPED) {
      @Override
      public Volume getCellValue(WellVolume wellVolume)
      {
        return wellVolume.getActiveWellInfo().getConsumedVolume();
      }
    });
    columns.add(new VolumeColumn<WellVolume>("Max Remaining Volume",
      "The maximum remaining volume of this well, across all copies", TableColumn.UNGROUPED) {
      @Override
      public Volume getCellValue(WellVolume wellVolume)
      {
        return wellVolume.getActiveWellInfo().getMaxWellCopyVolume().getRemainingVolume();
      }
    });
    columns.add(new TextColumn<WellVolume>("Max Remaining Volume Copy",
      "The copy with the maximum remaining volume of this well", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(WellVolume wellVolume)
      {
        return wellVolume.getActiveWellInfo().getMaxWellCopyVolume().getCopy().getName();
      }
    });
    columns.add(new VolumeColumn<WellVolume>("Min Remaining Volume",
      "The minimum remaining volume of this well, across all copies", TableColumn.UNGROUPED) {
      @Override
      public Volume getCellValue(WellVolume wellVolume)
      {
        return wellVolume.getActiveWellInfo().getMinWellCopyVolume().getRemainingVolume();

      }
    });
    columns.add(new TextColumn<WellVolume>("Min Remaining Volume Copy",
      "The copy with the minimum remaining volume of this well", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(WellVolume wellVolume)
      {
        return wellVolume.getActiveWellInfo().getMinWellCopyVolume().getCopy().getName();
      }
    });

    // Retired column info (hidden by default) //
    
    TableColumn<WellVolume,?> c = new SetColumn<WellVolume,String>("Copies (Retired)",
      "The retired copies of this well", TableColumn.UNGROUPED, ColumnType.TEXT_SET) {
      @Override
      public Set<String> getCellValue(WellVolume wellVolume)
      {
        return Sets.newTreeSet(wellVolume.getRetiredWellInfo().getCopiesList());
      }
    };
    c.setVisible(false);
    columns.add(c);

    c = new VolumeColumn<WellVolume>("Total Initial Copy Volume (Retired)",
      "The sum of initial volumes from all copies of this well", TableColumn.UNGROUPED) {
      @Override
      public Volume getCellValue(WellVolume wellVolume)
      {
        return wellVolume.getRetiredWellInfo().getTotalInitialVolume();
      }
    };
    c.setVisible(false);
    columns.add(c);
    
    c = new VolumeColumn<WellVolume>("Consumed Volume (Retired)",
      "The cumulative volume already used from this well, across all copies", TableColumn.UNGROUPED) {
      @Override
      public Volume getCellValue(WellVolume wellVolume)
      {
        return wellVolume.getRetiredWellInfo().getConsumedVolume();
      }
    };
    c.setVisible(false);
    columns.add(c);

    c = new VolumeColumn<WellVolume>("Max Remaining Volume (Retired)",
      "The maximum remaining volume of this well, across all copies", TableColumn.UNGROUPED) {
      @Override
      public Volume getCellValue(WellVolume wellVolume)
      {
        return wellVolume.getRetiredWellInfo().getMaxWellCopyVolume().getRemainingVolume();
      }
    };
    c.setVisible(false);
    columns.add(c);
    
    c = new TextColumn<WellVolume>("Max Remaining Volume Copy (Retired)",
      "The copy with the maximum remaining volume of this well", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(WellVolume wellVolume)
      {
        return wellVolume.getRetiredWellInfo().getMaxWellCopyVolume().getCopy().getName();
      }
    };
    c.setVisible(false);
    columns.add(c);

    c = new VolumeColumn<WellVolume>("Min Remaining Volume (Retired)",
      "The minimum remaining volume of this well, across all copies", TableColumn.UNGROUPED) {
      @Override
      public Volume getCellValue(WellVolume wellVolume)
      {
        return wellVolume.getRetiredWellInfo().getMinWellCopyVolume().getRemainingVolume();

      }
    };
    c.setVisible(false);
    columns.add(c);

    c = new TextColumn<WellVolume>("Min Remaining Volume Copy (Retired)",
      "The copy with the minimum remaining volume of this well", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(WellVolume wellVolume)
      {
        return wellVolume.getRetiredWellInfo().getMinWellCopyVolume().getCopy().getName();
      }
    };
    c.setVisible(false);
    columns.add(c);

    // done - retired hidden columns - //
    
    columns.add(new IntegerColumn<WellVolume>("Withdrawals/Adjustments",
      "The number of withdrawals and administrative adjustments made from this well, across all copies", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(WellVolume wellVolume)
      {
        return wellVolume.getWellVolumeAdjustmentCount();
      }

      @Override
      public boolean isCommandLink()
      {
        return getRowData().getWellVolumeAdjustmentCount() > 0;
      }

      @Override
      public Object cellAction(WellVolume wellVolume)
      {
        return null;
        // return showRowDetail();
      }
    });

// TableColumnManager<Well> columnManager = getColumnManager();
// columnManager.addCompoundSortColumns(columnManager.getColumn("Library"),
// columnManager.getColumn("Plate"), columnManager.getColumn("Well"));
// columnManager.addCompoundSortColumns(columnManager.getColumn("Plate"),
// columnManager.getColumn("Well"));
// columnManager.addCompoundSortColumns(columnManager.getColumn("Well"),
// columnManager.getColumn("Plate"));
// columnManager.addCompoundSortColumns(columnManager.getColumn("Copies"),
// columnManager.getColumn("Plate"), columnManager.getColumn("Well"),
// columnManager.getColumn("Copies"));
// columnManager.addCompoundSortColumns(columnManager.getColumn("Initial
// Volume"), columnManager.getColumn("Plate"), columnManager.getColumn("Well"),
// columnManager.getColumn("Copies"));
// columnManager.addCompoundSortColumns(columnManager.getColumn("Consumed
// Volume"), columnManager.getColumn("Plate"), columnManager.getColumn("Well"),
// columnManager.getColumn("Copies"));
// columnManager.addCompoundSortColumns(columnManager.getColumn("Max Remaining
// Volume"), columnManager.getColumn("Plate"), columnManager.getColumn("Well"),
// columnManager.getColumn("Copies"));
// columnManager.addCompoundSortColumns(columnManager.getColumn("Min Remaining
// Volume"), columnManager.getColumn("Plate"), columnManager.getColumn("Well"),
// columnManager.getColumn("Copies"));

    return columns;
  }

  @Override
  public void searchAll()
  {
    // TODO Auto-generated method stub
    
  }

// @Override
// protected void makeRowDetail(WellVolume wv)
// {
// List<WellVolumeAdjustment> wvas = new
// ArrayList<WellVolumeAdjustment>(wv.getWellVolumeAdjustments().size());
// for (WellVolumeAdjustment wva : wv.getWellVolumeAdjustments()) {
// WellVolumeAdjustment wva2 = _dao.reloadEntity(wva,
// true,
// "well",
// "copy",
// "labCherryPick.wellVolumeAdjustments",
// "labCherryPick.cherryPickRequest",
// "labCherryPick.assayPlate.cherryPickLiquidTransfer",
// "wellVolumeCorrectionActivity.performedBy");
// wvas.add(wva2);
// }
// getRowDetail().setContents(wvas);
//
// }
}
