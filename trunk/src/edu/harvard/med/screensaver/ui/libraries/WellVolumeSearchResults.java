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
import java.util.Comparator;
import java.util.List;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.libraries.WellVolumeAdjustment;
import edu.harvard.med.screensaver.ui.screens.CherryPickRequestViewer;
import edu.harvard.med.screensaver.ui.searchresults.FixedDecimalColumn;
import edu.harvard.med.screensaver.ui.searchresults.IntegerColumn;
import edu.harvard.med.screensaver.ui.searchresults.SearchResultsWithRowDetail;
import edu.harvard.med.screensaver.ui.searchresults.TextColumn;
import edu.harvard.med.screensaver.ui.table.TableColumn;

import org.apache.log4j.Logger;

public class WellVolumeSearchResults extends SearchResultsWithRowDetail<WellVolume,WellVolumeAdjustmentSearchResults>
{
  // static members

  private static Logger log = Logger.getLogger(WellVolumeSearchResults.class);

  private static final List<Integer[]> COMPOUND_SORTS = new ArrayList<Integer[]>();
  static {
    COMPOUND_SORTS.add(new Integer[] {0, 1, 2});
    COMPOUND_SORTS.add(new Integer[] {1, 2});
    COMPOUND_SORTS.add(new Integer[] {2, 1});
    COMPOUND_SORTS.add(new Integer[] {3, 1, 2});
    COMPOUND_SORTS.add(new Integer[] {4, 1, 2, 3});
    COMPOUND_SORTS.add(new Integer[] {5, 1, 2, 3});
    COMPOUND_SORTS.add(new Integer[] {6, 1, 2, 3});
    COMPOUND_SORTS.add(new Integer[] {7, 1, 2, 3});
  }


  // instance data members

  private GenericEntityDAO _dao;
  private LibraryViewer _libraryViewer;
  private WellViewer _wellViewer;
  private CherryPickRequestViewer _cherryPickRequestViewer;
  private ArrayList<TableColumn<WellVolume,?>> _columns;
  private TableColumn<WellVolume,?> _maxRemainingVolumeColumn;


  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected WellVolumeSearchResults()
  {
  }

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
    setRowDetail(rowDetail);
  }

  // public methods

  @Override
  protected List<TableColumn<WellVolume,?>> getColumns()
  {
    if (_columns == null) {
      _columns = new ArrayList<TableColumn<WellVolume,?>>();
      _columns.add(new TextColumn<WellVolume>("Library", "The library containing the well") {
        @Override
        public String getCellValue(WellVolume wellVolume) { return wellVolume.getWell().getLibrary().getLibraryName(); }

        @Override
        public boolean isCommandLink() { return true; }

        @Override
        public Object cellAction(WellVolume wellVolume) { return _libraryViewer.viewLibrary(wellVolume.getWell().getLibrary()); }
      });
      _columns.add(new IntegerColumn<WellVolume>("Plate", "The number of the plate the well is located on") {
        @Override
        public Integer getCellValue(WellVolume wellVolume) { return wellVolume.getWell().getPlateNumber(); }
      });
      _columns.add(new TextColumn<WellVolume>("Well", "The plate coordinates of the well") {
        @Override
        public String getCellValue(WellVolume wellVolume) { return wellVolume.getWell().getWellName(); }

        @Override
        public boolean isCommandLink() { return true; }

        @Override
        public Object cellAction(WellVolume wellVolume) { return _wellViewer.viewWell(wellVolume.getWell()); }
      });
      _columns.add(new TextColumn<WellVolume>("Copies", "The copies of this well") {
        @Override
        public String getCellValue(WellVolume wellVolume) { return wellVolume.getCopiesList(); }
      });
      _columns.add(new FixedDecimalColumn<WellVolume>("Initial Volume", "The initial volume of this well") {
        @Override
        public BigDecimal getCellValue(WellVolume wellVolume) { return wellVolume.getInitialMicroliterVolume(); }
      });
      _columns.add(new FixedDecimalColumn<WellVolume>("Consumed Volume", "The cumulative volume already used from this well (for all copies)") {
        @Override
        public BigDecimal getCellValue(WellVolume wellVolume) { return wellVolume.getConsumedMicroliterVolume(); }
      });
      _maxRemainingVolumeColumn = new TextColumn<WellVolume>("Max Remaining Volume", "The maximum remaining volume of this well (for all copies)") {
        @Override
        public String getCellValue(WellVolume wellVolume) { return wellVolume.getMaxWellCopyVolume().getRemainingMicroliterVolume() + " ("  + wellVolume.getMaxWellCopyVolume().getCopy().getName() + ")"; }

        @Override
        protected Comparator<WellVolume> getAscendingComparator()
        {
          return new Comparator<WellVolume>() {
            public int compare(WellVolume o1, WellVolume o2)
            {
              return o1.getMaxWellCopyVolume().getRemainingMicroliterVolume().compareTo(o2.getMaxWellCopyVolume().getRemainingMicroliterVolume());
            }
          };
        }
      };
      _columns.add(_maxRemainingVolumeColumn);
      _columns.add(new TextColumn<WellVolume>("Min Remaining Volume", "The minimum remaining volume of this well (for all copies)") {
        @Override
        public String getCellValue(WellVolume wellVolume) { return wellVolume.getMinWellCopyVolume().getRemainingMicroliterVolume() + " ("  + wellVolume.getMinWellCopyVolume().getCopy().getName() + ")"; }

        @Override
        protected Comparator<WellVolume> getAscendingComparator()
        {
          return new Comparator<WellVolume>() {
            public int compare(WellVolume o1, WellVolume o2)
            {
              return o1.getMinWellCopyVolume().getRemainingMicroliterVolume().compareTo(o2.getMinWellCopyVolume().getRemainingMicroliterVolume());
            }
          };
        }
      });
      _columns.add(new IntegerColumn<WellVolume>("Withdrawals/Adjustments", "The number of withdrawals and administrative adjustments made from this well (for all copies)") {
        @Override
        public Integer getCellValue(WellVolume wellVolume) { return wellVolume.getWellVolumeAdjustments().size(); }

        @Override
        public boolean isCommandLink() { return getRowData().getWellVolumeAdjustments().size() > 0; }

        @Override
        public Object cellAction(WellVolume wellVolume)
        {
          return showRowDetail();
        }
      });
    }
    return _columns;
  }

  @Override
  protected List<Integer[]> getCompoundSorts()
  {
    return COMPOUND_SORTS;
  }

  @Override
  protected void makeRowDetail(WellVolume wv)
  {
    List<WellVolumeAdjustment> wvas = new ArrayList<WellVolumeAdjustment>(wv.getWellVolumeAdjustments().size());
    for (WellVolumeAdjustment wva : wv.getWellVolumeAdjustments()) {
      WellVolumeAdjustment wva2 = _dao.reloadEntity(wva,
                                                    true,
                                                    "well",
                                                    "copy",
                                                    "labCherryPick.wellVolumeAdjustments",
                                                    "labCherryPick.cherryPickRequest",
                                                    "labCherryPick.assayPlate.cherryPickLiquidTransfer",
                                                    "wellVolumeCorrectionActivity.performedBy");
      wvas.add(wva2);
    }
    getRowDetail().setContents(wvas);

  }


  // private methods

}

