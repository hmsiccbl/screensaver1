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
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screens.CherryPickAssayPlate;
import edu.harvard.med.screensaver.model.screens.CherryPickRequest;
import edu.harvard.med.screensaver.model.screens.LabCherryPick;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.ScreenerCherryPick;
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


  private static final String[] SCREENER_CHERRY_PICKS_TABLE_COLUMNS = { 
    "Library Plate", 
    "Screened Well", 
    "Source Wells",
    "Gene", 
    "Entrez ID", 
    "Entrez Symbol", 
    "Genbank AccNo" };
  private static final String[][] SCREENER_CHERRY_PICKS_TABLE_SECONDARY_SORT = { 
    { "Screened Well" }, 
    { "Library Plate" }, 
    { "Library Plate", "Screened Well" }, 
    {}, 
    {}, 
    {}, 
    {} };
  private static final String[] LAB_CHERRY_PICKS_TABLE_COLUMNS = { 
    "Status", 
    "Library Plate", 
    "Source Well", 
    "Source Copy", 
    "Gene", 
    "Entrez ID", 
    "Entrez Symbol", 
    "Genbank AccNo", 
    "Cherry Pick Plate", 
    "Destination Well" };
  private static final String[][] LAB_CHERRY_PICKS_TABLE_SECONDARY_SORT = { 
    { "Library Plate", "Source Well" }, 
    { "Source Well" }, 
    { "Library Plate" }, 
    { "Library Plate", "Source Well" }, 
    { "Library Plate", "Source Well" }, 
    { "Library Plate", "Source Well" }, 
    { "Library Plate", "Source Well" }, 
    { "Library Plate", "Source Well" },
    { "Destination Well" },
    { "Cherry Pick Plate" } };
  private static final String[] ASSAY_PLATES_TABLE_COLUMNS = { "Plate Name", "Date Plated", "Attempts" };
  private static final String[] ASSAY_PLATES_TABLE_SECONDARY_SORTS = { null, "Plate Name", "Plate Name" };

  private static final Collection<Integer> PLATE_COLUMNS_LIST = new ArrayList<Integer>();
  static {
    for (int i = Well.MIN_WELL_COLUMN; i <= Well.MAX_WELL_COLUMN; i++) {
      PLATE_COLUMNS_LIST.add(i);
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

  private TableSortManager _screenerCherryPicksSortManager;
  private TableSortManager _labCherryPicksSortManager;
  private DataModel _screenerCherryPicksDataModel;
  private DataModel _labCherryPicksDataModel;
  private DataModel _assayPlatesColumnModel;
  private DataModel _assayPlatesDataModel;
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

    Set<Integer> selectableEmptyColumns = new TreeSet<Integer>(PLATE_COLUMNS_LIST);
    selectableEmptyColumns.removeAll(_cherryPickRequest.getRequiredEmptyColumnsOnAssayPlate());
    _emptyColumnsOnAssayPlate = 
      new UISelectManyBean<Integer>(selectableEmptyColumns, 
                                    _cherryPickRequest.getRequestedEmptyColumnsOnAssayPlate());
    
    _assayPlatesColumnModel = new ArrayDataModel(ASSAY_PLATES_TABLE_COLUMNS);
    _screenerCherryPicksDataModel = null;
    _labCherryPicksDataModel = null;
    _assayPlatesDataModel = null;
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

  public TableSortManager getScreenerCherryPicksSortManager()
  {
    if (_screenerCherryPicksSortManager == null) {
      _screenerCherryPicksSortManager = new TableSortManager(Arrays.asList(SCREENER_CHERRY_PICKS_TABLE_COLUMNS))
      {
        @Override
        protected void sortChanged(String newSortColumnName, SortDirection newSortDirection)
        {
          _screenerCherryPicksDataModel = null;
        }
      };
    }
    return _screenerCherryPicksSortManager;
  }

  public TableSortManager getLabCherryPicksSortManager()
  {
    if (_labCherryPicksSortManager == null) {
      _labCherryPicksSortManager = new TableSortManager(Arrays.asList(LAB_CHERRY_PICKS_TABLE_COLUMNS))
      {
        @Override
        protected void sortChanged(String newSortColumnName, SortDirection newSortDirection)
        {
          _labCherryPicksDataModel = null;
        }
      };
    }
    return _labCherryPicksSortManager;
  }
  
  public DataModel getScreenerCherryPicksDataModel()
  {
    if (_screenerCherryPicksDataModel == null) {
      List<Map> rows = new ArrayList<Map>();
      for (ScreenerCherryPick cherryPick : _cherryPickRequest.getScreenerCherryPicks()) {
        Map<String,Comparable> row = new HashMap<String,Comparable>();
        int col = 0;
        row.put(SCREENER_CHERRY_PICKS_TABLE_COLUMNS[col++], cherryPick.getScreenedWell().getPlateNumber());
        row.put(SCREENER_CHERRY_PICKS_TABLE_COLUMNS[col++], cherryPick.getScreenedWell().getWellName());
        row.put(SCREENER_CHERRY_PICKS_TABLE_COLUMNS[col++], cherryPick.getLabCherryPicks().size());
        Gene gene = cherryPick.getScreenedWell().getGene();
        if (gene != null) {
          row.put(SCREENER_CHERRY_PICKS_TABLE_COLUMNS[col++], gene.getGeneName());
          row.put(SCREENER_CHERRY_PICKS_TABLE_COLUMNS[col++], gene.getEntrezgeneId());
          row.put(SCREENER_CHERRY_PICKS_TABLE_COLUMNS[col++], gene.getEntrezgeneSymbol());
          row.put(SCREENER_CHERRY_PICKS_TABLE_COLUMNS[col++], StringUtils.makeListString(gene.getGenbankAccessionNumbers(), "; "));
        }
        else {
          // TODO: handle compound screens
          row.put(SCREENER_CHERRY_PICKS_TABLE_COLUMNS[col++], "");
          row.put(SCREENER_CHERRY_PICKS_TABLE_COLUMNS[col++], "");
          row.put(SCREENER_CHERRY_PICKS_TABLE_COLUMNS[col++], "");
          row.put(SCREENER_CHERRY_PICKS_TABLE_COLUMNS[col++], "");
        }
        
        rows.add(row);
      }
      Collections.sort(rows, 
                       new CherryPickTableRowComparator(getSortColumns(_screenerCherryPicksSortManager,
                                                                       SCREENER_CHERRY_PICKS_TABLE_SECONDARY_SORT), 
                                                        _screenerCherryPicksSortManager.getCurrentSortDirection()));
      _screenerCherryPicksDataModel = new ListDataModel(rows);
    }
    return _screenerCherryPicksDataModel;
  }

  public DataModel getLabCherryPicksDataModel()
  {
    if (_labCherryPicksDataModel == null) {
      List<Map> rows = new ArrayList<Map>();
      for (LabCherryPick cherryPick : _cherryPickRequest.getLabCherryPicks()) {
        Map<String,Comparable> row = new HashMap<String,Comparable>();
        int col = 0;
        row.put(LAB_CHERRY_PICKS_TABLE_COLUMNS[col++], 
                cherryPick.isPlated() ? "plated" : 
                  cherryPick.isMapped() ? "mapped" : 
                    cherryPick.isAllocated() ? "reserved" : "unfulfilled");
        row.put(LAB_CHERRY_PICKS_TABLE_COLUMNS[col++], cherryPick.getSourceWell().getPlateNumber().toString());
        row.put(LAB_CHERRY_PICKS_TABLE_COLUMNS[col++], cherryPick.getSourceWell().getWellName());
        row.put(LAB_CHERRY_PICKS_TABLE_COLUMNS[col++], cherryPick.getSourceCopy() != null ? cherryPick.getSourceCopy().getName() : "");
        Gene gene = cherryPick.getSourceWell().getGene();
        if (gene != null) {
          row.put(LAB_CHERRY_PICKS_TABLE_COLUMNS[col++], gene.getGeneName());
          row.put(LAB_CHERRY_PICKS_TABLE_COLUMNS[col++], gene.getEntrezgeneId().toString());
          row.put(LAB_CHERRY_PICKS_TABLE_COLUMNS[col++], gene.getEntrezgeneSymbol());
          row.put(LAB_CHERRY_PICKS_TABLE_COLUMNS[col++], StringUtils.makeListString(gene.getGenbankAccessionNumbers(), "; "));
        }
        else {
          // TODO: handle compound screens
          row.put(LAB_CHERRY_PICKS_TABLE_COLUMNS[col++], "");
          row.put(LAB_CHERRY_PICKS_TABLE_COLUMNS[col++], "");
          row.put(LAB_CHERRY_PICKS_TABLE_COLUMNS[col++], "");
          row.put(LAB_CHERRY_PICKS_TABLE_COLUMNS[col++], "");
        }
        
        if (cherryPick.isMapped()) {
          row.put(LAB_CHERRY_PICKS_TABLE_COLUMNS[col++], cherryPick.getAssayPlate().getName());
          row.put(LAB_CHERRY_PICKS_TABLE_COLUMNS[col++], cherryPick.getAssayPlateWellName());
        }
        else {
          row.put(LAB_CHERRY_PICKS_TABLE_COLUMNS[col++], "");
          row.put(LAB_CHERRY_PICKS_TABLE_COLUMNS[col++], "");
        }
        
        rows.add(row);
      }
      Collections.sort(rows, 
                       new CherryPickTableRowComparator(getSortColumns(_labCherryPicksSortManager,
                                                                       LAB_CHERRY_PICKS_TABLE_SECONDARY_SORT),
                                                        _labCherryPicksSortManager.getCurrentSortDirection()));
      _labCherryPicksDataModel = new ListDataModel(rows);
    }
    return _labCherryPicksDataModel;
  }
  
  public DataModel getAssayPlatesColumnModel()
  {
    return _assayPlatesColumnModel;
  }

  public DataModel getAssayPlatesDataModel()
  {
    if (_assayPlatesDataModel == null) {
      List<AssayPlateRow> rows = new ArrayList<AssayPlateRow>();
      for (CherryPickAssayPlate assayPlate : _cherryPickRequest.getCherryPickAssayPlates()) {
        Map<String,Comparable> rowValues = new HashMap<String,Comparable>();
        rowValues.put(ASSAY_PLATES_TABLE_COLUMNS[0], assayPlate.getName());
        if (assayPlate.isPlated()) {
          rowValues.put(ASSAY_PLATES_TABLE_COLUMNS[1], assayPlate.getCherryPickLiquidTransfer().getDateOfActivity());
          rowValues.put(ASSAY_PLATES_TABLE_COLUMNS[2], assayPlate.getAttemptOrdinal() + 1);
        }
        else {
          rowValues.put(ASSAY_PLATES_TABLE_COLUMNS[1], "");
          rowValues.put(ASSAY_PLATES_TABLE_COLUMNS[2], Integer.toString(0));
        }
        AssayPlateRow row = new AssayPlateRow(rowValues);
        rows.add(row);
      }
      _assayPlatesDataModel = new ListDataModel(rows);
    }
    return _assayPlatesDataModel;
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
  
  public String addCompoundCherryPicks()
  {
    PlateWellListParserResult result = _plateWellListParser.lookupWellsFromPlateWellList(_cherryPicksInput);
    // TODO: handle errors properly
    if (result.getFatalErrors().size() > 0 ||
      result.getSyntaxErrors().size() > 0 ||
      result.getWellsNotFound().size() > 0 ||
      result.getWells().size() == 0) {
      showMessage("cherryPicks.parseError");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    return _screensController.addCherryPicksForCompoundWells(_cherryPickRequest,
                                                             result.getWells());
  }
  
  public String addPoolCherryPicks()
  {
    PlateWellListParserResult result = _plateWellListParser.lookupWellsFromPlateWellList(_cherryPicksInput);
    // TODO: handle errors properly
    if (result.getFatalErrors().size() > 0 ||
      result.getSyntaxErrors().size() > 0 ||
      result.getWellsNotFound().size() > 0 ||
      result.getWells().size() == 0) {
      showMessage("cherryPicks.parseError");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    return _screensController.addCherryPicksForPoolWells(_cherryPickRequest,
                                                         result.getWells());
  }
  
  public String addDuplexCherryPicks()
  {
    PlateWellListParserResult result = _plateWellListParser.lookupWellsFromPlateWellList(_cherryPicksInput);
    // TODO: handle errors properly
    if (result.getFatalErrors().size() > 0 ||
      result.getSyntaxErrors().size() > 0 ||
      result.getWellsNotFound().size() > 0 ||
      result.getWells().size() == 0) {
      showMessage("cherryPicks.parseError");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    return _screensController.addCherryPicksForDuplexWells(_cherryPickRequest,
                                                           result.getWells());
  }
  
  public String deleteCherryPickRequest()
  {
    return _screensController.deleteCherryPickRequest(_cherryPickRequest);
  }
  
  public String deleteAllCherryPicks()
  {
    return _screensController.deleteAllScreenerCherryPicks(_cherryPickRequest);
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
  
  public String createNewCherryPickRequestForUnfulfilledCherryPicks()
  {
    // TODO
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  public String recordFailureOfAssayPlates()
  {
    // TODO
    // create new assay plates, duplicating plate name, lab cherry picks with same layout but new copy selection, incrementing attempt ordinal
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

//  public String createNewCherryPickRequestForSelectedAssayPlates()
//  {
//    // TODO
//    return REDISPLAY_PAGE_ACTION_RESULT;
//  }

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
        selectedAssayPlates.add(row.getValues().get(ASSAY_PLATES_TABLE_COLUMNS[0]).toString());
      }
    }
    return selectedAssayPlates;
  }

  private String[] getSortColumns(TableSortManager sortManager,
                                  String[][] secondarySorts)
  {
    // TODO: cache!
    int primarySortColumnIndex = sortManager.getCurrentSortColumnIndex();
    String[] sortColumns = new String[1 + secondarySorts[primarySortColumnIndex].length];
    sortColumns[0] = sortManager.getCurrentSortColumnName();
    for (int i = 0; i < sortColumns.length - 1; ++i) {
      sortColumns[i + 1] = secondarySorts[primarySortColumnIndex][i];
    } 
    return sortColumns;
  }
}

