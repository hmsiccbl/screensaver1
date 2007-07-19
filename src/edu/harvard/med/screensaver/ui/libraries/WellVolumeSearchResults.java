// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.io.DataExporter;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellVolumeAdjustment;
import edu.harvard.med.screensaver.model.libraries.WellVolumeCorrectionActivity;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.control.LibrariesController;
import edu.harvard.med.screensaver.ui.control.LibrariesControllerImpl;
import edu.harvard.med.screensaver.ui.searchresults.SearchResults;
import edu.harvard.med.screensaver.ui.table.TableColumn;
import edu.harvard.med.screensaver.ui.util.Messages;

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
  private static final ScreensaverUserRole EDITING_ROLE = ScreensaverUserRole.LIBRARIES_ADMIN;

  private static Logger log = Logger.getLogger(WellVolumeSearchResults.class);


  // instance data members
  
  private LibrariesController _librariesController;
  private ArrayList<TableColumn<WellVolume>> _columns;
  private TableColumn<WellVolume> _remainingVolumeColumn;
  private Map<WellVolume,BigDecimal> _newRemainingVolumes;
  private String _wellVolumeAdjustmentActivityComments;
  private GenericEntityDAO _dao;

  
  // public constructors and methods

  public WellVolumeSearchResults(Collection<WellVolume> unsortedResults,
                                 LibrariesController librariesController,
                                 GenericEntityDAO dao,
                                 Messages messages)
  {
    super(unsortedResults);
    _librariesController = librariesController;
    _dao = dao;
    getColumns(); // force initialization of _remainingVolumeColumn
    // start with sort descending on remainingVolume column
    getSortManager().setSortColumn(_remainingVolumeColumn);
    
    setCurrentScreensaverUser(((LibrariesControllerImpl) _librariesController).getCurrentScreensaverUser());
    setMessages(messages);
  }

  @Override
  protected ScreensaverUserRole getEditableAdminRole()
  {
    return EDITING_ROLE;
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
      _columns.add(new TableColumn<WellVolume>("Withdrawals/Adjustments", "The number of withdrawals and administrative adjustment smade from this well copy", true) {
        @Override
        public Object getCellValue(WellVolume wellVolume) { return wellVolume.getWellVolumeAdjustments().size(); }

        @Override
        public boolean isVisible() { return !isEditMode(); }

        // TODO
//        @Override
//        public boolean isCommandLink() { return getEntity().getWellVolumeAdjustments().size() > 0; }
//        
//        @Override
//        public Object cellAction(WellVolume entity)
//        {
//          return REDISPLAY_PAGE_ACTION_RESULT; 
//        }
      });      
      _columns.add(new TableColumn<WellVolume>("New Remaining Volume", "Enter new remaining volume", true) {

        @Override
        public Object getCellValue(WellVolume wellVolume) { return _newRemainingVolumes.get(wellVolume); }
        
        @Override
        public void setCellValue(WellVolume wellVolume, Object value)
        {
          if (value != null && value.toString().trim().length() > 0) {
            try {
              BigDecimal newValue = new BigDecimal(value.toString()).setScale(Well.VOLUME_SCALE);
              _newRemainingVolumes.put(wellVolume, newValue);
            }
            catch (Exception e) {
              showMessage("libraries.badWellVolumeAdjustmentValue", value, wellVolume.getWell().getWellKey(), wellVolume.getCopy().getName());
            }
          }
        }
          
        
        @Override
        public boolean isEditable() { return true; }

        @Override
        public boolean isVisible() { return isEditMode(); }
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
  
  public String getWellVolumeAdjustmentActivityComments()
  {
    return _wellVolumeAdjustmentActivityComments;
  }

  public void setWellVolumeAdjustmentActivityComments(String wellVolumeAdjustmentActivityComments)
  {
    _wellVolumeAdjustmentActivityComments = wellVolumeAdjustmentActivityComments;
  }

  public void doEdit()
  {
    _newRemainingVolumes = new HashMap<WellVolume,BigDecimal>();
    _wellVolumeAdjustmentActivityComments = null;
  }

  @Override
  public void doSave()
  {
    ScreensaverUser screensaverUser = getCurrentScreensaverUser().getScreensaverUser();
    if (!(screensaverUser instanceof AdministratorUser) || !((AdministratorUser) screensaverUser).isUserInRole(ScreensaverUserRole.LIBRARIES_ADMIN)) {
      throw new BusinessRuleViolationException("only libraries administrators can edit well volumes");
    }
    final AdministratorUser administratorUser = (AdministratorUser) screensaverUser;
    _dao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        if (_newRemainingVolumes.size() > 0) {
          _dao.reattachEntity(administratorUser);
          WellVolumeCorrectionActivity wellVolumeCorrectionActivity = 
            new WellVolumeCorrectionActivity(administratorUser, new Date());
          wellVolumeCorrectionActivity.setComments(getWellVolumeAdjustmentActivityComments());
          // TODO
          //wellVolumeCorrectionActivity.setApprovedBy();
          for (Map.Entry<WellVolume,BigDecimal> entry : _newRemainingVolumes.entrySet()) {
            WellVolume wellVolume = entry.getKey();
            BigDecimal newRemainingVolume = entry.getValue();
            WellVolumeAdjustment wellVolumeAdjustment = 
              new WellVolumeAdjustment(wellVolume.getCopy(),
                                       wellVolume.getWell(),
                                       newRemainingVolume.subtract(wellVolume.getRemainingMicroliterVolume()));
            wellVolume.addWellVolumeAdjustment(wellVolumeAdjustment);
            wellVolumeCorrectionActivity.getWellVolumeAdjustments().add(wellVolumeAdjustment);
          }
          _dao.persistEntity(wellVolumeCorrectionActivity);
        }
      }
    });
    showMessage("libraries.updatedWellVolumes", new Integer(_newRemainingVolumes.size()));
  }
  
  @Override
  protected List<DataExporter<WellVolume>> getDataExporters()
  {
    return new ArrayList<DataExporter<WellVolume>>();
  }
  

  // private methods

}

