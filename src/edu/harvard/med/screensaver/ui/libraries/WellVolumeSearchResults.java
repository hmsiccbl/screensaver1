// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
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

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.io.DataExporter;
import edu.harvard.med.screensaver.model.libraries.WellVolumeAdjustment;
import edu.harvard.med.screensaver.ui.control.LibrariesController;
import edu.harvard.med.screensaver.ui.control.ScreensController;
import edu.harvard.med.screensaver.ui.searchresults.SearchResults;
import edu.harvard.med.screensaver.ui.table.TableColumn;

import org.apache.log4j.Logger;

public class WellVolumeSearchResults extends SearchResults<WellVolume>
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
  
  private LibrariesController _librariesController;
  private ScreensController _screensController;
  private GenericEntityDAO _dao;
  private ArrayList<TableColumn<WellVolume>> _columns;
  private TableColumn<WellVolume> _maxRemainingVolumeColumn;
  
  
  // public constructors and methods

  public WellVolumeSearchResults(Collection<WellVolume> unsortedResults,
                                 LibrariesController librariesController,
                                 ScreensController screensController,
                                 GenericEntityDAO dao)
  {
    super(unsortedResults);
    _librariesController = librariesController;
    _screensController = screensController;
    _dao = dao;
  }

  @Override
  protected List<TableColumn<WellVolume>> getColumns()
  {
    if (_columns == null) {
      _columns = new ArrayList<TableColumn<WellVolume>>();
      _columns.add(new TableColumn<WellVolume>("Library", "The library containing the well") {
        @Override
        public Object getCellValue(WellVolume wellVolume) { return wellVolume.getWell().getLibrary().getLibraryName(); }

        @Override
        public boolean isCommandLink() { return true; }

        @Override
        public Object cellAction(WellVolume wellVolume) { return _librariesController.viewLibrary(wellVolume.getWell().getLibrary(), null); }
      });
      _columns.add(new TableColumn<WellVolume>("Plate", "The number of the plate the well is located on", true) {
        @Override
        public Object getCellValue(WellVolume wellVolume) { return wellVolume.getWell().getPlateNumber(); }
      });      
      _columns.add(new TableColumn<WellVolume>("Well", "The plate coordinates of the well") {
        @Override
        public Object getCellValue(WellVolume wellVolume) { return wellVolume.getWell().getWellName(); }

        @Override
        public boolean isCommandLink() { return true; }

        @Override
        public Object cellAction(WellVolume wellVolume) { return _librariesController.viewWell(wellVolume.getWell(), null); }
      });
      _columns.add(new TableColumn<WellVolume>("Copies", "The copies of this well") {
        @Override
        public Object getCellValue(WellVolume wellVolume) { return wellVolume.getCopiesList(); }
      });
      _columns.add(new TableColumn<WellVolume>("Initial Volume", "The initial volume of this well", true) {
        @Override
        public Object getCellValue(WellVolume wellVolume) { return wellVolume.getInitialMicroliterVolume(); }
      });      
      _columns.add(new TableColumn<WellVolume>("Consumed Volume", "The cumulative volume already used from this well (for all copies)", true) {
        @Override
        public Object getCellValue(WellVolume wellVolume) { return wellVolume.getConsumedMicroliterVolume(); }
      });      
      _maxRemainingVolumeColumn = new TableColumn<WellVolume>("Max Remaining Volume", "The maximum remaining volume of this well (for all copies)", true) {
        @Override
        public Object getCellValue(WellVolume wellVolume) { return wellVolume.getMaxWellCopyVolume().getRemainingMicroliterVolume() + " ("  + wellVolume.getMaxWellCopyVolume().getCopy().getName() + ")"; }
      };      
      _columns.add(_maxRemainingVolumeColumn);
      _columns.add(new TableColumn<WellVolume>("Min Remaining Volume", "The minimum remaining volume of this well (for all copies)", true) {
        @Override
        public Object getCellValue(WellVolume wellVolume) { return wellVolume.getMinWellCopyVolume().getRemainingMicroliterVolume() + " ("  + wellVolume.getMinWellCopyVolume().getCopy().getName() + ")"; }
      });      
      _columns.add(new TableColumn<WellVolume>("Withdrawals/Adjustments", "The number of withdrawals and administrative adjustments made from this well (for all copies)", true) {
        @Override
        public Object getCellValue(WellVolume wellVolume) { return wellVolume.getWellVolumeAdjustments().size(); }

        @Override
        public boolean isCommandLink() { return getEntity().getWellVolumeAdjustments().size() > 0; }
        
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
  protected SearchResults<WellVolumeAdjustment> makeRowDetail(WellVolume wv)
  {
    List<WellVolumeAdjustment> wvas = new ArrayList<WellVolumeAdjustment>(wv.getWellVolumeAdjustments().size());
    for (WellVolumeAdjustment wva : wv.getWellVolumeAdjustments()) {
      WellVolumeAdjustment wva2 = _dao.reloadEntity(wva, 
                                                    true, 
                                                    "well", 
                                                    "copy", 
                                                    "labCherryPick.wellVolumeAdjustments", 
                                                    "labCherryPick.cherryPickRequest",
                                                    "labCherryPick.assayPlate.hbnCherryPickLiquidTransfer",
                                                    "wellVolumeCorrectionActivity.hbnPerformedBy");
      wvas.add(wva2);
    }
    return new WellVolumeAdjustmentSearchResults(wvas, _screensController);
  }
  
  @Override
  public String showSummaryView()
  {
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @Override
  protected void setEntityToView(WellVolume wellCopyVolume)
  {
    _librariesController.viewWell(wellCopyVolume.getWell(), null);
  }
  
  @Override
  protected List<DataExporter<WellVolume>> getDataExporters()
  {
    return new ArrayList<DataExporter<WellVolume>>();
  }
  

  // private methods

}

