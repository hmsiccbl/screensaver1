// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screens;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.faces.model.ArrayDataModel;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.io.libraries.PlateWellListParser;
import edu.harvard.med.screensaver.io.libraries.PlateWellListParserResult;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screens.CherryPick;
import edu.harvard.med.screensaver.model.screens.CherryPickLiquidTransfer;
import edu.harvard.med.screensaver.model.screens.CherryPickRequest;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.control.ScreensController;
import edu.harvard.med.screensaver.ui.searchresults.SortDirection;
import edu.harvard.med.screensaver.ui.util.ScreensaverUserComparator;
import edu.harvard.med.screensaver.ui.util.TableSortManager;
import edu.harvard.med.screensaver.ui.util.UISelectManyBean;
import edu.harvard.med.screensaver.ui.util.UISelectOneEntityBean;
import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.log4j.Logger;

public class CherryPickRequestViewer extends AbstractBackingBean
{
  private static final ScreensaverUserRole EDITING_ROLE = ScreensaverUserRole.CHERRY_PICK_ADMIN;


  private static final String[] CHERRY_PICKS_TABLE_COLUMNS = { "Status", "Library Plate", "Source Copy", "Source Well", "Gene", "Entrez ID", "Symbol", "AccNo", "Cherry Pick Plate", "Destination Well" };
  private static final String[] ASSAY_PLATES_TABLE_COLUMNS = { "Plate Name", "Date Plated" };
  private static final String[] LIQUID_TRANSFER_TABLE_COLUMNS = { "Date", "By", "Assay Plates" };

  private static final Collection<Integer> COLUMNS_LIST = new ArrayList<Integer>();
  static {
    for (int i = Well.MIN_WELL_COLUMN; i <= Well.MAX_WELL_COLUMN; i++) {
      COLUMNS_LIST.add(i);
    }
  }


  // static members

  private static Logger log = Logger.getLogger(CherryPickRequestViewer.class);


  // instance data members
  
  private DAO _dao;
  private ScreensController _screensController;
  private PlateWellListParser _plateWellListParser;

  private CherryPickRequest _cherryPickRequest;
  private boolean _isEditMode = false;
  private String _cherryPicksInput;
  private UISelectOneEntityBean<ScreeningRoomUser> _requestedBy;
  private UISelectManyBean<Integer> _emptyColumnsOnAssayPlate;

  private TableSortManager _cherryPicksSortManager;
  private DataModel _cherryPicksDataModel;
  private DataModel _assayPlatesColumnModel;
  private DataModel _assayPlatesDataModel;
  private DataModel _liquidTransferColumnModel;
  private DataModel _liquidTransferDataModel;
  private boolean _selectAllAssayPlates;


  // public constructors and methods

  public void setDao(DAO dao)
  {
    _dao = dao;
  }
  
  public void setPlateWellListParser(PlateWellListParser plateWellListParser)
  {
    _plateWellListParser = plateWellListParser;
  }

  public void setScreensController(ScreensController screensController)
  {
    _screensController = screensController;
  }
  
  public void setCherryPickRequest(CherryPickRequest cherryPickRequest)
  {
    _cherryPickRequest = cherryPickRequest;
    
    _isEditMode = false;
    _cherryPicksInput = null;
    
    SortedSet<ScreeningRoomUser> candidateRequestors = new TreeSet<ScreeningRoomUser>(ScreensaverUserComparator.getInstance());
    candidateRequestors.add(_cherryPickRequest.getScreen().getLabHead());
    candidateRequestors.add(_cherryPickRequest.getScreen().getLeadScreener());
    candidateRequestors.addAll(_cherryPickRequest.getScreen().getCollaborators());
    _requestedBy = new UISelectOneEntityBean<ScreeningRoomUser>(candidateRequestors,
                                                                _cherryPickRequest.getScreen()
                                                                                  .getLeadScreener(),
                                                                _dao)
    {
      @Override
      protected String getLabel(ScreeningRoomUser u)
      {
        return u.getFullNameLastFirst(); 
      }
    };

    Set<Integer> selectableEmptyColumns = new TreeSet<Integer>(COLUMNS_LIST);
    selectableEmptyColumns.removeAll(_cherryPickRequest.getRequiredEmptyColumnsOnAssayPlate());
    _emptyColumnsOnAssayPlate = 
      new UISelectManyBean<Integer>(selectableEmptyColumns, 
                                    _cherryPickRequest.getRequestedEmptyColumnsOnAssayPlate());
    
    _assayPlatesColumnModel = new ArrayDataModel(ASSAY_PLATES_TABLE_COLUMNS);
    _liquidTransferColumnModel = new ArrayDataModel(LIQUID_TRANSFER_TABLE_COLUMNS);
    _cherryPicksDataModel = null;
    _assayPlatesDataModel = null;
    _liquidTransferDataModel = null;
  }

  public CherryPickRequest getCherryPickRequest()
  {
    return _cherryPickRequest;
  }
  
  public boolean isEditMode()
  {
    return _isEditMode;
  }

  public String getCherryPicksInput()
  {
    return _cherryPicksInput;
  }

  public void setCherryPicksInput(String cherryPicksInput)
  {
    _cherryPicksInput = cherryPicksInput;
  }

  public UISelectOneEntityBean<ScreeningRoomUser> getRequestedBy()
  {
    return _requestedBy;
  }

  public UISelectManyBean<Integer> getEmptyColumnsOnAssayPlate()
  {
    return _emptyColumnsOnAssayPlate;
  }

  public String getEmptyColumnsOnAssayPlateAsString()
  {
    return StringUtils.makeListString(new TreeSet<Integer>(_cherryPickRequest.getRequestedEmptyColumnsOnAssayPlate()), ", ");
  }

  public TableSortManager getCherryPicksSortManager()
  {
    if (_cherryPicksSortManager == null) {
      _cherryPicksSortManager = new TableSortManager(Arrays.asList(CHERRY_PICKS_TABLE_COLUMNS)) 
      {
        @Override
        protected void sortChanged(String newSortColumnName, SortDirection newSortDirection)
        {
          _cherryPicksDataModel = null;
        }
      };
    }
    return _cherryPicksSortManager;
  }

  public void setCherryPicksSortManager(TableSortManager cherryPicksSortManager)
  {
    _cherryPicksSortManager = cherryPicksSortManager;
  }

  public DataModel getCherryPicksDataModel()
  {
    if (_cherryPicksDataModel == null) {
      List<Map> rows = new ArrayList<Map>();
      boolean isRequestAllocated = _cherryPickRequest.isAllocated();
      for (CherryPick cherryPick : _cherryPickRequest.getCherryPicks()) {
        Map<String,String> row = new HashMap<String,String>();
        int col = 0;
        row.put(CHERRY_PICKS_TABLE_COLUMNS[col++], cherryPick.isPlated() ? "plated" : cherryPick.isMapped() ? "mapped" : cherryPick.isAllocated() ? "reserved" : isRequestAllocated ? "unfulfillable" : "new");
        row.put(CHERRY_PICKS_TABLE_COLUMNS[col++], cherryPick.getSourceWell().getPlateNumber().toString());
        row.put(CHERRY_PICKS_TABLE_COLUMNS[col++], cherryPick.getSourceCopy() != null ? cherryPick.getSourceCopy().getName() : "");
        row.put(CHERRY_PICKS_TABLE_COLUMNS[col++], cherryPick.getSourceWell().getWellName());
        Iterator<SilencingReagent> silencingReagentsIter = cherryPick.getSourceWell().getSilencingReagents().iterator();
        // TODO: handle compound screens
        if (silencingReagentsIter.hasNext()) {
          SilencingReagent silencingReagent = silencingReagentsIter.next();
          row.put(CHERRY_PICKS_TABLE_COLUMNS[col++], silencingReagent.getGene().getGeneName());
          row.put(CHERRY_PICKS_TABLE_COLUMNS[col++], silencingReagent.getGene().getEntrezgeneId().toString());
          row.put(CHERRY_PICKS_TABLE_COLUMNS[col++], silencingReagent.getGene().getEntrezgeneSymbol());
          row.put(CHERRY_PICKS_TABLE_COLUMNS[col++], StringUtils.makeListString(silencingReagent.getGene().getGenbankAccessionNumbers(), "; "));
        }
        else {
          row.put(CHERRY_PICKS_TABLE_COLUMNS[col++], "");
          row.put(CHERRY_PICKS_TABLE_COLUMNS[col++], "");
          row.put(CHERRY_PICKS_TABLE_COLUMNS[col++], "");
          row.put(CHERRY_PICKS_TABLE_COLUMNS[col++], "");
        }
        
        if (cherryPick.isMapped()) {
          row.put(CHERRY_PICKS_TABLE_COLUMNS[col++], cherryPick.getAssayPlateName());
          row.put(CHERRY_PICKS_TABLE_COLUMNS[col++], cherryPick.getAssayPlateWellName());
        }
        else {
          row.put(CHERRY_PICKS_TABLE_COLUMNS[col++], "");
          row.put(CHERRY_PICKS_TABLE_COLUMNS[col++], "");
        }
        
        rows.add(row);
      }
      Collections.sort(rows, 
                       new CherryPickTableRowComparator(_cherryPicksSortManager.getCurrentSortColumnName(), 
                                                        _cherryPicksSortManager.getCurrentSortDirection()));
      _cherryPicksDataModel = new ListDataModel(rows);
    }
    return _cherryPicksDataModel;
  }
  
  public DataModel getAssayPlatesColumnModel()
  {
    return _assayPlatesColumnModel;
  }

  public DataModel getAssayPlatesDataModel()
  {
    if (_assayPlatesDataModel == null) {
      List<AssayPlateRow> rows = new ArrayList<AssayPlateRow>();
      for (String assayPlateName : _cherryPickRequest.getAssayPlates()) {
        Map<String,String> rowValues = new HashMap<String,String>();
        Set<CherryPickLiquidTransfer> cherryPickLiquidTransfersForAssayPlate = 
          _cherryPickRequest.getCherryPickLiquidTransfersForAssayPlate(assayPlateName);
        rowValues.put(ASSAY_PLATES_TABLE_COLUMNS[0], assayPlateName);
        rowValues.put(ASSAY_PLATES_TABLE_COLUMNS[1],
                      StringUtils.makeListString(cherryPickLiquidTransfersForAssayPlate, ", "));
        AssayPlateRow row = new AssayPlateRow(rowValues);
        rows.add(row);
      }
      _assayPlatesDataModel = new ListDataModel(rows);
    }
    return _assayPlatesDataModel;
  }
  
  public DataModel getLiquidTransferColumnModel()
  {
    return _liquidTransferColumnModel;
  }

  public DataModel getLiquidTransferDataModel()
  {
    if (_liquidTransferDataModel == null) {
      List<Map> rows = new ArrayList<Map>();
      for (CherryPickLiquidTransfer liquidTransfer : _cherryPickRequest.getCherryPickLiquidTransfers()) {
        Map<String,String> row = new HashMap<String,String>();
        row.put(LIQUID_TRANSFER_TABLE_COLUMNS[0], liquidTransfer.getDateOfActivity().toString());
        row.put(LIQUID_TRANSFER_TABLE_COLUMNS[1], liquidTransfer.getPerformedBy().getFullNameLastFirst());
        row.put(LIQUID_TRANSFER_TABLE_COLUMNS[2], StringUtils.makeListString(liquidTransfer.getAssayPlates(), ", "));
        rows.add(row);
      }
      _liquidTransferDataModel = new ListDataModel(rows);
    }
    return _liquidTransferDataModel;
  }

  public boolean isSelectAllAssayPlates()
  {
    return _selectAllAssayPlates;
  }

  public void setSelectAllAssayPlates(boolean selectAllAssayPlates)
  {
    _selectAllAssayPlates = selectAllAssayPlates;
  }

  /**
   * @motivation tune the UI labels to reflect the type of screen being cherry picked
   */
  public String getLiquidTerm()
  {
    ScreenType screenType = _cherryPickRequest.getScreen().getScreenType();
    // TODO: implement polymorphically
    if (screenType.equals(ScreenType.RNAI)) {
      return "Reagent";
    }
    if (screenType.equals(ScreenType.SMALL_MOLECULE)) {
      return "Compound";
    }
    return "Liquid";
  }

  
  /* JSF Application methods */
  
  public String viewScreen()
  {
    return _screensController.viewScreen(_cherryPickRequest.getScreen(), null);
  }
  
  public String addCherryPicks()
  {
    PlateWellListParserResult result = _plateWellListParser.lookupWellsFromPlateWellList(_cherryPicksInput);
    // TODO: handle errors properly
    if (result.getFatalErrors().size() > 0 ||
      result.getSyntaxErrors().size() > 0 ||
      result.getWellsNotFound().size() > 0 ||
      result.getWells().size() == 0) {
      showMessage("screens.cherryPicksParseError");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    return _screensController.addCherryPicksForWells(_cherryPickRequest,
                                                     result.getWells());
  }
  
  public String deleteCherryPickRequest()
  {
    return _screensController.deleteCherryPickRequest(_cherryPickRequest);
  }
  
  public String deleteAllCherryPicks()
  {
    return _screensController.deleteAllCherryPicks(_cherryPickRequest);
  }
  
  public String allocateCherryPicks()
  {
    return _screensController.allocateCherryPicks(_cherryPickRequest);
  }

  public String deallocateCherryPicks()
  {
    return _screensController.deallocateCherryPicks(_cherryPickRequest);
  }
  
  public String plateMapCherryPicks()
  {
    return _screensController.plateMapCherryPicks(_cherryPickRequest);
  }

  @SuppressWarnings("unchecked")
  public String selectAllAssayPlates()
  {
    List<AssayPlateRow> data = (List<AssayPlateRow>) getAssayPlatesDataModel().getWrappedData();
    for (AssayPlateRow row : data) {
      row.setSelected(_selectAllAssayPlates);
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public String createDuplicateCherryPickRequestForSelectedAssayPlates()
  {
    // TODO
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  public String createCherryPickRequestForUnfulfilledCherryPicks()
  {
    // TODO
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  public String downloadPlateMappingFilesForSelectedAssayPlates()
  {
    Set<String> plateNames = getSelectedAssayPlates();
      return _screensController.downloadCherryPickRequestPlateMappingFiles(_cherryPickRequest, 
                                                                           plateNames);
  }
  
  public String viewLeadScreener()
  {
    // TODO
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public String recordLiquidTransferForSelectedAssayPlates()
  {
    // TODO
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public String setEditMode()
  {
    _isEditMode = true;
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public String cancelEdit() {
    // edits are discarded (and edit mode is cancelled) by virtue of controller reloading the screen entity from the database
    return _screensController.viewCherryPickRequest(_cherryPickRequest);
  }
  
  public String save() {
    _isEditMode = false;
    return _screensController.saveCherryPickRequest(_cherryPickRequest,
                                                    new DAOTransaction() 
    {
      public void runTransaction() 
      {
        _cherryPickRequest.setRequestedBy(_requestedBy.getSelection());
        _cherryPickRequest.setRequestedEmptyColumnsOnAssayPlate(new HashSet<Integer>(_emptyColumnsOnAssayPlate.getSelections()));
      }
    });
  }

  // protected methods

  protected ScreensaverUserRole getEditableAdminRole()
  {
    return EDITING_ROLE;
  }

  // private methods

  @SuppressWarnings("unchecked")
  private Set<String> getSelectedAssayPlates()
  {
    Set<String> selectedAssayPlates = new HashSet<String>();
    List<AssayPlateRow> data = (List<AssayPlateRow>) getAssayPlatesDataModel().getWrappedData();
    for (AssayPlateRow row : data) {
      if (row.isSelected()) {
        selectedAssayPlates.add(row.getValues().get(ASSAY_PLATES_TABLE_COLUMNS[0]));
      }
    }
    return selectedAssayPlates;
  }
}

