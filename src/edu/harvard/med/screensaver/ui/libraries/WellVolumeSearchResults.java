// $HeadURL:
// svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/ui/libraries/WellVolumeSearchResults.java
// $
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
import java.util.Comparator;
import java.util.List;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.datafetcher.DataFetcher;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.ui.screens.CherryPickRequestViewer;
import edu.harvard.med.screensaver.ui.searchresults.AggregateSearchResults;
import edu.harvard.med.screensaver.ui.table.column.FixedDecimalColumn;
import edu.harvard.med.screensaver.ui.table.column.IntegerColumn;
import edu.harvard.med.screensaver.ui.table.column.ListColumn;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;
import edu.harvard.med.screensaver.ui.table.column.TextColumn;
import edu.harvard.med.screensaver.ui.table.model.DataTableModel;
import edu.harvard.med.screensaver.ui.table.model.InMemoryDataModel;

import org.apache.log4j.Logger;

/**
 * Aggregates WellVolumeAdjustments into WellVolumes, and provides these
 * WellVolumes as a SearchResult. Underlying data is set via methods
 * {@link WellCopyVolumeSearchResults}.
 * 
 * @see WellCopyVolumeSearchResults
 */
public class WellVolumeSearchResults extends AggregateSearchResults<WellVolume,WellKey>
{

  // static members

  private static Logger log = Logger.getLogger(WellVolumeSearchResults.class);


  // instance data members

  private GenericEntityDAO _dao;
  private LibraryViewer _libraryViewer;
  private WellViewer _wellViewer;
  private CherryPickRequestViewer _cherryPickRequestViewer;
  private TextColumn<Well> _maxRemainingVolumeColumn;


  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected WellVolumeSearchResults() {}
    
  public WellVolumeSearchResults(GenericEntityDAO dao,
                                 LibraryViewer libraryViewer,
                                 WellViewer wellViewer,
                                 CherryPickRequestViewer cherryPickRequestViewer,
                                 WellVolumeAdjustmentSearchResults rowDetail)
  {
    _dao = dao;
    _libraryViewer = libraryViewer;
    _wellViewer = wellViewer;
    _cherryPickRequestViewer = cherryPickRequestViewer;
    // setRowDetail(rowDetail);
  }

  // public methods

  @Override
  protected DataTableModel<WellVolume> buildDataTableModel(DataFetcher<WellVolume,WellKey,Object> dataFetcher,
                                                           List<? extends TableColumn<WellVolume,?>> columns)
  {
    return new InMemoryDataModel<WellVolume>(dataFetcher);
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
        return _libraryViewer.viewLibrary(wellVolume.getWell().getLibrary());
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
        return _wellViewer.viewWell(wellVolume.getWell());
      }
    });
    columns.add(new ListColumn<WellVolume>("Copies", "The copies of this well", TableColumn.UNGROUPED) {
      @Override
      public List<String> getCellValue(WellVolume wellVolume)
      {
        return wellVolume.getCopiesList();
      }
    });
    columns.add(new FixedDecimalColumn<WellVolume>("Total Initial Copy Volume",
                                                   "The sum of initial volumes from all copies of this well", TableColumn.UNGROUPED) {
      @Override
      public BigDecimal getCellValue(WellVolume wellVolume)
      {
        return wellVolume.getTotalInitialMicroliterVolume();
      }
    });
    columns.add(new FixedDecimalColumn<WellVolume>("Consumed Volume",
                                                   "The cumulative volume already used from this well, across all copies", TableColumn.UNGROUPED) {
      @Override
      public BigDecimal getCellValue(WellVolume wellVolume)
      {
        return wellVolume.getConsumedMicroliterVolume();
      }
    });
    columns.add(new TextColumn<WellVolume>("Max Remaining Volume",
                                           "The maximum remaining volume of this well, across all copies", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(WellVolume wellVolume)
      {
        return wellVolume.getMaxWellCopyVolume().getRemainingMicroliterVolume() + " (" +
               wellVolume.getMaxWellCopyVolume().getCopy().getName() + ")";
      }

      @Override
      protected Comparator<WellVolume> getAscendingComparator()
      {
        return new Comparator<WellVolume>() {
          public int compare(WellVolume wv1, WellVolume wv2)
          {
            return wv1.getMaxWellCopyVolume()
                      .getRemainingMicroliterVolume()
                      .compareTo(wv2.getMaxWellCopyVolume()
                                    .getRemainingMicroliterVolume());
          }
        };
      }
    });
    columns.add(new TextColumn<WellVolume>("Min Remaining Volume",
                                           "The minimum remaining volume of this well, across all copies", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(WellVolume wellVolume)
      {
        return wellVolume.getMinWellCopyVolume()
                         .getRemainingMicroliterVolume() + " (" +
               wellVolume.getMinWellCopyVolume().getCopy().getName() + ")";
      }

      @Override
      protected Comparator<WellVolume> getAscendingComparator()
      {
        return new Comparator<WellVolume>() {
          public int compare(WellVolume wv1, WellVolume wv2)
          {
            return wv1.getMinWellCopyVolume()
                      .getRemainingMicroliterVolume()
                      .compareTo(wv2.getMinWellCopyVolume()
                                    .getRemainingMicroliterVolume());
          }
        };
      }
    });
    columns.add(new IntegerColumn<WellVolume>("Withdrawals/Adjustments",
                                              "The number of withdrawals and administrative adjustments made from this well, across all copies", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(WellVolume wellVolume)
      {
        return wellVolume.getWellVolumeAdjustments().size();
      }

      @Override
      public boolean isCommandLink()
      {
        return getRowData().getWellVolumeAdjustments().size() > 0;
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


  // private methods

}
