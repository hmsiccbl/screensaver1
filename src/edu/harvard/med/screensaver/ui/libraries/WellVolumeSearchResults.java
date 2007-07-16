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

import edu.harvard.med.screensaver.io.DataExporter;
import edu.harvard.med.screensaver.ui.control.LibrariesController;
import edu.harvard.med.screensaver.ui.searchresults.SearchResults;
import edu.harvard.med.screensaver.ui.table.TableColumn;

import org.apache.log4j.Logger;

public class WellVolumeSearchResults extends SearchResults<WellVolume>
{
  // static members

  private static final List<Integer[]> COMPOUND_SORTS = new ArrayList<Integer[]>();
  static {
    COMPOUND_SORTS.add(new Integer[] {0, 1, 2, 3});
    COMPOUND_SORTS.add(new Integer[] {1, 2, 3});
    COMPOUND_SORTS.add(new Integer[] {2, 1, 3});
    COMPOUND_SORTS.add(new Integer[] {4, 1, 2, 3});
    COMPOUND_SORTS.add(new Integer[] {5, 1, 2, 3});
    COMPOUND_SORTS.add(new Integer[] {6, 1, 2, 3});
    COMPOUND_SORTS.add(new Integer[] {7, 1, 2, 3});
  }

  private static Logger log = Logger.getLogger(WellVolumeSearchResults.class);


  // instance data members
  
  private LibrariesController _librariesController;
  private ArrayList<TableColumn<WellVolume>> _columns;
  private TableColumn<WellVolume> _remainingVolumeColumn;

  
  // public constructors and methods

  public WellVolumeSearchResults(Collection<WellVolume> unsortedResults,
                                 LibrariesController librariesController)
  {
    super(unsortedResults);
    _librariesController = librariesController;
    getColumns(); // force initialization of _remainingVolumeColumn
    // start with sort descending on remainingVolume column
    getSortManager().setSortColumn(_remainingVolumeColumn);
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
      _columns.add(new TableColumn<WellVolume>("Copy", "The name of the library plate copy") {
        @Override
        public Object getCellValue(WellVolume wellVolume) { return wellVolume.getCopy().getName(); }

        // TODO
//        @Override
//        public boolean isCommandLink() { return true; }
//
//        @Override
//        public Object cellAction(WellVolume wellVolume) { return _librariesController.viewLibraryCopyVolumes(wellVolume.getWell(), WellVolumeSearchResults.this); }
      });
      _columns.add(new TableColumn<WellVolume>("Initial Volume", "The initial volume of this well copy", true) {
        @Override
        public Object getCellValue(WellVolume wellVolume) { return wellVolume.getInitialMicroliterVolume(); }
      });      
      _columns.add(new TableColumn<WellVolume>("Consumed Volume", "The volume already used from this well copy", true) {
        @Override
        public Object getCellValue(WellVolume wellVolume) { return wellVolume.getConsumedMicroliterVolume(); }
      });      
      _remainingVolumeColumn = new TableColumn<WellVolume>("Remaining Volume", "The remaining volume of this well copy", true) {
        @Override
        public Object getCellValue(WellVolume wellVolume) { return wellVolume.getRemainingMicroliterVolume(); }
      };      
      _columns.add(_remainingVolumeColumn);
      _columns.add(new TableColumn<WellVolume>("Withdrawals", "The number of withdrawals made from this well copy", true) {
        @Override
        public Object getCellValue(WellVolume wellVolume) { return wellVolume.getWellVolumeAdjustments().size(); }

        @Override
        public boolean isCommandLink() { return getEntity().getWellVolumeAdjustments().size() > 0; }
        
        @Override
        public Object cellAction(WellVolume entity)
        {
          return REDISPLAY_PAGE_ACTION_RESULT; 
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
  public String showSummaryView()
  {
    return _librariesController.viewWellVolumeSearchResults(this);
  }

  @Override
  protected void setEntityToView(WellVolume wellVolume)
  {
    _librariesController.viewWell(wellVolume.getWell(), null);
  }

  @Override
  protected List<DataExporter<WellVolume>> getDataExporters()
  {
    return new ArrayList<DataExporter<WellVolume>>();
  }

  // private methods

}

