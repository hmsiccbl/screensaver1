// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screens;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.faces.component.UIInput;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.ArrayDataModel;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.io.libraries.PlateWellListParser;
import edu.harvard.med.screensaver.io.libraries.PlateWellListParserResult;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screens.CherryPickAssayPlate;
import edu.harvard.med.screensaver.model.screens.CherryPickRequest;
import edu.harvard.med.screensaver.model.screens.LabCherryPick;
import edu.harvard.med.screensaver.model.screens.LegacyCherryPickAssayPlate;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.ScreenerCherryPick;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.control.ScreensController;
import edu.harvard.med.screensaver.ui.table.TableSortManager;
import edu.harvard.med.screensaver.ui.util.ScreensaverUserComparator;
import edu.harvard.med.screensaver.ui.util.UISelectManyBean;
import edu.harvard.med.screensaver.ui.util.UISelectOneBean;
import edu.harvard.med.screensaver.ui.util.UISelectOneEntityBean;
import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.log4j.Logger;
import org.apache.myfaces.custom.datascroller.HtmlDataScroller;

public class CherryPickRequestViewer extends AbstractBackingBean
{


  // static members

  private static Logger log = Logger.getLogger(CherryPickRequestViewer.class);

  private static final ScreensaverUserRole EDITING_ROLE = ScreensaverUserRole.CHERRY_PICK_ADMIN;

  private static final String VALIDATE_SELECTED_PLATES_FOR_LIQUID_TRANSFER = "for_liquid_transfer";
  private static final String VALIDATE_SELECTED_PLATES_FOR_DOWNLOAD = "for_download";
  private static final String VALIDATE_SELECTED_PLATES_FOR_DEALLOCATION = "for_deallocaton";

  private static final String[] SCREENER_CHERRY_PICKS_TABLE_COLUMNS = { 
    "Library Plate", 
    "Screened Well", 
    "Source Wells",
    "Vendor ID",
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
    {}, 
    {} };
  private static final String[] LAB_CHERRY_PICKS_TABLE_COLUMNS = { 
    "Status", 
    "Library Plate", 
    "Source Well", 
    "Source Copy", 
    "Vendor ID",
    "Gene", 
    "Entrez ID", 
    "Entrez Symbol", 
    "Genbank AccNo", 
    "Cherry Pick Plate #", 
    "Attempt #", 
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
    { "Library Plate", "Source Well" },
    { "Attempt #", "Destination Well" },
    { "Cherry Pick Plate #", "Destination Well" },
    { "Cherry Pick Plate #", "Attempt #" } };

  private static final Collection<Integer> PLATE_COLUMNS_LIST = new ArrayList<Integer>();

  static {
    for (int i = Well.MIN_WELL_COLUMN; i <= Well.MAX_WELL_COLUMN; i++) {
      PLATE_COLUMNS_LIST.add(i);
    }
  }

  
  // instance data members
  
  private GenericEntityDAO _dao;
  private ScreensController _screensController;
  private PlateWellListParser _plateWellListParser;

  private CherryPickRequest _cherryPickRequest;
  private boolean _isEditMode = false;
  private Map<String,Boolean> _isPanelCollapsedMap;
  private String _cherryPicksInput;
  private UISelectOneEntityBean<ScreeningRoomUser> _requestedBy;
  private UISelectOneEntityBean<AdministratorUser> _volumeApprovedBy;
  private UISelectManyBean<Integer> _emptyColumnsOnAssayPlate;

  private TableSortManager _screenerCherryPicksSortManager;
  private TableSortManager _labCherryPicksSortManager;
  private DataModel _screenerCherryPicksDataModel;
  private DataModel _labCherryPicksDataModel;
  private DataModel _assayPlatesColumnModel;
  private DataModel _assayPlatesDataModel;
  private boolean _selectAllAssayPlates = true;
  private boolean _showFailedAssayPlates;
  private boolean _showFailedLabCherryPicks;

  private UISelectOneEntityBean<ScreensaverUser> _liquidTransferPerformedBy;
  private Date _dateOfLiquidTransfer;
  private String _liquidTransferComments;
  
  private HtmlDataScroller _screenerCherryPicksTableDataScroller1;
  private HtmlDataScroller _screenerCherryPicksTableDataScroller2;

  private UISelectOneBean<Integer> _labCherryPicksPerPage;
  private UISelectOneBean<Integer> _screenerCherryPicksPerPage;


  
  // public constructors and methods
  
  public CherryPickRequestViewer()
  {
    _isPanelCollapsedMap = new HashMap<String,Boolean>();
    _isPanelCollapsedMap.put("screenSummary", true);
    _isPanelCollapsedMap.put("cherryPickRequest", false);
    _isPanelCollapsedMap.put("screenerCherryPicks", false);
    _isPanelCollapsedMap.put("labCherryPicks", false);
    _isPanelCollapsedMap.put("cherryPickPlates", false);
    
    _labCherryPicksPerPage = new UISelectOneBean<Integer>(new ArrayList<Integer>(Arrays.asList(10, 20, 50, 100, -1))) {
      protected String getLabel(Integer t) { if (t == -1) return "All"; else return super.getLabel(t); }
    };
    _screenerCherryPicksPerPage = new UISelectOneBean<Integer>(new ArrayList<Integer>(Arrays.asList(10, 20, 50, 100, -1))) {
      protected String getLabel(Integer t) { if (t == -1) return "All"; else return super.getLabel(t); }
    };
  }

  public void setDao(GenericEntityDAO dao)
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
      _cherryPickRequest.getRequestedBy(),
      _dao) { 
      protected String getLabel(ScreeningRoomUser u) { return u.getFullNameLastFirst(); } 
    };

    SortedSet<AdministratorUser> candidateVolumeApprovers = new TreeSet<AdministratorUser>(ScreensaverUserComparator.getInstance());
    candidateVolumeApprovers.add(null);
    candidateVolumeApprovers.addAll(_dao.findAllEntitiesOfType(AdministratorUser.class)); // TODO: filter out all but CherryPickAdmins
    _volumeApprovedBy = new UISelectOneEntityBean<AdministratorUser>(candidateVolumeApprovers, 
      _cherryPickRequest.getVolumeApprovedBy(),
      _dao) { 
      protected String getLabel(AdministratorUser a) { return a == null ? super.getLabel(a) : a.getFullNameLastFirst(); } 
    };
      
    SortedSet<ScreensaverUser> candidatePreparers = new TreeSet<ScreensaverUser>(ScreensaverUserComparator.getInstance());
    candidatePreparers.addAll(_dao.findAllEntitiesOfType(AdministratorUser.class));
    _liquidTransferPerformedBy = new UISelectOneEntityBean<ScreensaverUser>(candidatePreparers,
      candidatePreparers.contains(getScreensaverUser()) ? getScreensaverUser() : candidatePreparers.first(),
                                                        _dao) { 
      protected String getLabel(ScreensaverUser u) { return u.getFullNameLastFirst(); } 
    };

    Set<Integer> selectableEmptyColumns = new TreeSet<Integer>(PLATE_COLUMNS_LIST);
    selectableEmptyColumns.removeAll(_cherryPickRequest.getRequiredEmptyColumnsOnAssayPlate());
    _emptyColumnsOnAssayPlate = 
      new UISelectManyBean<Integer>(selectableEmptyColumns, 
                                    _cherryPickRequest.getRequestedEmptyColumnsOnAssayPlate());
    
    _assayPlatesColumnModel = new ArrayDataModel(AssayPlateRow.ASSAY_PLATES_TABLE_COLUMNS);
    _screenerCherryPicksDataModel = null;
    _labCherryPicksDataModel = null;
    _assayPlatesDataModel = null;
    
    // set "Cherry Pick Plates" panel to initially expanded, if cherry pick plates have been created
    boolean hasCherryPickPlates = _cherryPickRequest.getCherryPickAssayPlates().size() > 0;
    _isPanelCollapsedMap.put("cherryPickPlates", !hasCherryPickPlates);
  }

  public CherryPickRequest getCherryPickRequest()
  {
    return _cherryPickRequest;
  }
  
  public boolean isEditMode()
  {
    return _isEditMode;
  }

  public Map getIsPanelCollapsedMap()
  {
    return _isPanelCollapsedMap;
  }
  
  public HtmlDataScroller getScreenerCherryPicksTableDataScroller1()
  {
    return _screenerCherryPicksTableDataScroller1;
  }

  public void setScreenerCherryPicksTableDataScroller1(HtmlDataScroller screenerCherryPicksTableDataScroller1)
  {
    this._screenerCherryPicksTableDataScroller1 = screenerCherryPicksTableDataScroller1;
  }

  public HtmlDataScroller getScreenerCherryPicksTableDataScroller2()
  {
    return _screenerCherryPicksTableDataScroller2;
  }

  public void setScreenerCherryPicksTableDataScroller2(HtmlDataScroller screenerCherryPicksTableDataScroller2)
  {
    this._screenerCherryPicksTableDataScroller2 = screenerCherryPicksTableDataScroller2;
  }

  public UISelectOneBean<Integer> getLabCherryPicksPerPage()
  {
    return _labCherryPicksPerPage;
  }

  public UISelectOneBean<Integer> getScreenerCherryPicksPerPage()
  {
    return _screenerCherryPicksPerPage;
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

  public UISelectOneEntityBean<AdministratorUser> getVolumeApprovedBy()
  {
    return _volumeApprovedBy;
  }
  
  public String getDateVolumeApproved()
  {
    return DateFormat.getDateInstance(DateFormat.SHORT).format(_cherryPickRequest.getDateVolumeApproved());
  }

  public UISelectOneEntityBean<ScreensaverUser> getLiquidTransferPerformedBy()
  {
    return _liquidTransferPerformedBy;
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
  
  public int getScreenerCherryPickCount()
  {
    return _cherryPickRequest.getScreenerCherryPicks().size();
  }
  
  public int getLabCherryPickCount()
  {
    return _cherryPickRequest.getLabCherryPicks().size();
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
  
  public boolean isRnaiScreen()
  {
    return _cherryPickRequest.getScreen().getScreenType().equals(ScreenType.RNAI);
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
        row.put(SCREENER_CHERRY_PICKS_TABLE_COLUMNS[col++], cherryPick.getScreenedWell().getVendorIdentifier());
        Gene gene = cherryPick.getScreenedWell().getGene();
        if (gene != null) {
          row.put(SCREENER_CHERRY_PICKS_TABLE_COLUMNS[col++], gene.getGeneName());
          row.put(SCREENER_CHERRY_PICKS_TABLE_COLUMNS[col++], gene.getEntrezgeneId());
          row.put(SCREENER_CHERRY_PICKS_TABLE_COLUMNS[col++], gene.getEntrezgeneSymbol());
          row.put(SCREENER_CHERRY_PICKS_TABLE_COLUMNS[col++], StringUtils.makeListString(gene.getGenbankAccessionNumbers(), "; "));
        }
        else {
          // TODO: handle compound screens
          row.put(SCREENER_CHERRY_PICKS_TABLE_COLUMNS[col++], null);
          row.put(SCREENER_CHERRY_PICKS_TABLE_COLUMNS[col++], null);
          row.put(SCREENER_CHERRY_PICKS_TABLE_COLUMNS[col++], null);
          row.put(SCREENER_CHERRY_PICKS_TABLE_COLUMNS[col++], null);
        }
        
        rows.add(row);
      }
      Collections.sort(rows, 
                       new CherryPickTableRowComparator(getSortColumns(getScreenerCherryPicksSortManager(),
                                                                       SCREENER_CHERRY_PICKS_TABLE_SECONDARY_SORT), 
                                                        getScreenerCherryPicksSortManager().getSortDirection()));
      _screenerCherryPicksDataModel = new ListDataModel(rows);
    }
    return _screenerCherryPicksDataModel;
  }

  public DataModel getLabCherryPicksDataModel()
  {
    if (_labCherryPicksDataModel == null) {
      List<Map> rows = new ArrayList<Map>();
      for (LabCherryPick cherryPick : _cherryPickRequest.getLabCherryPicks()) {
        if (!_showFailedLabCherryPicks && cherryPick.isFailed()) {
          continue;
        }
        Map<String,Comparable> row = new HashMap<String,Comparable>();
        int col = 0;
        row.put(LAB_CHERRY_PICKS_TABLE_COLUMNS[col++], 
                cherryPick.isPlated() ? "plated" : 
                  cherryPick.isFailed() ? "failed" :
                    cherryPick.isCanceled() ? "canceled" :
                        cherryPick.isMapped() ? "mapped" : 
                          cherryPick.isAllocated() ? "reserved" : "unfulfilled");
        row.put(LAB_CHERRY_PICKS_TABLE_COLUMNS[col++], cherryPick.getSourceWell().getPlateNumber().toString());
        row.put(LAB_CHERRY_PICKS_TABLE_COLUMNS[col++], cherryPick.getSourceWell().getWellName());
        row.put(LAB_CHERRY_PICKS_TABLE_COLUMNS[col++], cherryPick.getSourceCopy() != null ? cherryPick.getSourceCopy().getName() : "");
        row.put(LAB_CHERRY_PICKS_TABLE_COLUMNS[col++], cherryPick.getSourceWell().getVendorIdentifier());
        Gene gene = cherryPick.getSourceWell().getGene();
        if (gene != null) {
          row.put(LAB_CHERRY_PICKS_TABLE_COLUMNS[col++], gene.getGeneName());
          row.put(LAB_CHERRY_PICKS_TABLE_COLUMNS[col++], gene.getEntrezgeneId().toString());
          row.put(LAB_CHERRY_PICKS_TABLE_COLUMNS[col++], gene.getEntrezgeneSymbol());
          row.put(LAB_CHERRY_PICKS_TABLE_COLUMNS[col++], StringUtils.makeListString(gene.getGenbankAccessionNumbers(), "; "));
        }
        else {
          // TODO: handle compound screens
          row.put(LAB_CHERRY_PICKS_TABLE_COLUMNS[col++], null);
          row.put(LAB_CHERRY_PICKS_TABLE_COLUMNS[col++], null);
          row.put(LAB_CHERRY_PICKS_TABLE_COLUMNS[col++], null);
          row.put(LAB_CHERRY_PICKS_TABLE_COLUMNS[col++], null);
        }
        
        if (cherryPick.isMapped()) {
          row.put(LAB_CHERRY_PICKS_TABLE_COLUMNS[col++], new Integer(cherryPick.getAssayPlate().getPlateOrdinal() + 1));
          row.put(LAB_CHERRY_PICKS_TABLE_COLUMNS[col++], new Integer(cherryPick.getAssayPlate().getAttemptOrdinal() + 1));
          row.put(LAB_CHERRY_PICKS_TABLE_COLUMNS[col++], cherryPick.getAssayPlateWellName().toString());
        }
        else {
          row.put(LAB_CHERRY_PICKS_TABLE_COLUMNS[col++], null);
          row.put(LAB_CHERRY_PICKS_TABLE_COLUMNS[col++], null);
          row.put(LAB_CHERRY_PICKS_TABLE_COLUMNS[col++], null);
        }
        
        rows.add(row);
      }
      Collections.sort(rows, 
                       new CherryPickTableRowComparator(getSortColumns(getLabCherryPicksSortManager(),
                                                                       LAB_CHERRY_PICKS_TABLE_SECONDARY_SORT),
                                                        getLabCherryPicksSortManager().getSortDirection()));
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
      Collection<CherryPickAssayPlate> assayPlates = 
        _showFailedAssayPlates ? _cherryPickRequest.getCherryPickAssayPlates() : 
          _cherryPickRequest.getActiveCherryPickAssayPlates();
      for (CherryPickAssayPlate assayPlate : assayPlates) {
        AssayPlateRow row = new AssayPlateRow(assayPlate);
        row.setSelected(_selectAllAssayPlates);
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

  public boolean isShowFailedAssayPlates()
  {
    return _showFailedAssayPlates;
  }

  public void setShowFailedAssayPlates(boolean showFailedAssayPlates)
  {
    if (showFailedAssayPlates != _showFailedAssayPlates) {
      _assayPlatesDataModel = null; // force regen
    }
    _showFailedAssayPlates = showFailedAssayPlates;
  }

  public boolean isShowFailedLabCherryPicks()
  {
    return _showFailedLabCherryPicks;
  }

  public void setShowFailedLabCherryPicks(boolean showFailedLabCherryPicks)
  {
    if (showFailedLabCherryPicks != _showFailedLabCherryPicks) {
      _labCherryPicksDataModel = null; // force regen
    }
    _showFailedLabCherryPicks = showFailedLabCherryPicks;

  }

  public Date getDateOfLiquidTransfer()
  {
    return _dateOfLiquidTransfer;
  }

  public void setDateOfLiquidTransfer(Date dateOfLiquidTransfer)
  {
    _dateOfLiquidTransfer = dateOfLiquidTransfer;
  }

  public String getLiquidTransferComments()
  {
    return _liquidTransferComments;
  }

  public void setLiquidTransferComments(String liquidTransferComments)
  {
    _liquidTransferComments = liquidTransferComments;
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
  
  /*
   * JSF Listeners
   */

  public void toggleShowFailedLabCherryPicks(ValueChangeEvent event)
  {
    _showFailedLabCherryPicks = (Boolean) event.getNewValue();
    // avoid having JSF set backing bean property with the submitted value
    ((UIInput) event.getComponent()).setLocalValueSet(false);
    // force regen of data model
    _labCherryPicksDataModel = null;
  }
  
  public void toggleShowFailedAssayPlates(ValueChangeEvent event)
  {
    _showFailedAssayPlates = (Boolean) event.getNewValue();
    // avoid having JSF set backing bean property with the submitted value
    ((UIInput) event.getComponent()).setLocalValueSet(false);
    // force regen of data model
    _assayPlatesDataModel = null;
  }
  

  /* JSF Application methods */
  
  public String viewScreen()
  {
    return _screensController.viewScreen(_cherryPickRequest.getScreen(), null);
  }
  
  public String addCherryPicks()
  {
    PlateWellListParserResult result = _plateWellListParser.parseWellsFromPlateWellList(_cherryPicksInput);
    // TODO: report errors
    if (result.getErrors().size() > 0) {
      showMessage("cherryPicks.parseError");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    return _screensController.addCherryPicksForWells(_cherryPickRequest,
                                                     result.getParsedWellKeys());
  }
  
  public String addPoolCherryPicks()
  {
    PlateWellListParserResult result = _plateWellListParser.parseWellsFromPlateWellList(_cherryPicksInput);
    // TODO: report errors
    if (result.getErrors().size() > 0) {
      showMessage("cherryPicks.parseError");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    return _screensController.addCherryPicksForPoolWells(_cherryPickRequest,
                                                         result.getParsedWellKeys());
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
  
  public String deallocateCherryPicksByPlate()
  {
    if (!validateSelectedAssayPlates(VALIDATE_SELECTED_PLATES_FOR_DEALLOCATION)) {
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    return _screensController.cancelAndDeallocateCherryPicksByPlate(_cherryPickRequest,
                                                                    getSelectedAssayPlates(),
                                                                    getLiquidTransferPerformedBy().getSelection(),
                                                                    getDateOfLiquidTransfer(),
                                                                    getLiquidTransferComments());
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
  
  public String downloadPlateMappingFilesForSelectedAssayPlates()
  {
    if (!validateSelectedAssayPlates(VALIDATE_SELECTED_PLATES_FOR_DOWNLOAD)) {
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    return _screensController.downloadCherryPickRequestPlateMappingFiles(_cherryPickRequest, 
                                                                         getSelectedAssayPlates());
  }
  
  public String recordLiquidTransferForSelectedAssayPlates()
  {
    if (!validateSelectedAssayPlates(VALIDATE_SELECTED_PLATES_FOR_LIQUID_TRANSFER)) {
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    return _screensController.recordLiquidTransferForAssayPlates(_cherryPickRequest, 
                                                                 getSelectedAssayPlates(),
                                                                 getLiquidTransferPerformedBy().getSelection(),
                                                                 getDateOfLiquidTransfer(),
                                                                 getLiquidTransferComments());
  }
  
  public String recordFailureOfAssayPlates()
  {
    if (!validateSelectedAssayPlates(VALIDATE_SELECTED_PLATES_FOR_LIQUID_TRANSFER)) {
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    return _screensController.recordFailureOfAssayPlates(_cherryPickRequest,
                                                         getSelectedAssayPlates(),
                                                         getLiquidTransferPerformedBy().getSelection(),
                                                         getDateOfLiquidTransfer(),
                                                         getLiquidTransferComments());
  }
  
  public String downloadCherryPickRequest()
  {
    return _screensController.downloadCherryPickRequest(_cherryPickRequest);
  }

  @SuppressWarnings("unchecked")
  private boolean validateSelectedAssayPlates(String validationType)
  {
    Set<CherryPickAssayPlate> selectedAssayPlates = getSelectedAssayPlates();
    if (selectedAssayPlates.size() == 0) {
      showMessage("cherryPicks.noPlatesSelected", "assayPlatesTable");
      return false;
    }
    
    boolean adjustSelection = false;
    for (Iterator iter = selectedAssayPlates.iterator(); iter.hasNext();) {
      CherryPickAssayPlate assayPlate = (CherryPickAssayPlate) iter.next();
      if (validationType.equals(VALIDATE_SELECTED_PLATES_FOR_DEALLOCATION)) {
        if (assayPlate.isFailed() || assayPlate.isPlated() || assayPlate.isCanceled()) {
          showMessageForComponent("cherryPicks.deallocateActiveMappedPlatesOnly", 
                                  "assayPlatesTable", 
                                  assayPlate.getName());
          iter.remove();
          adjustSelection = true;
        }
      }
      else if (validationType.equals(VALIDATE_SELECTED_PLATES_FOR_DOWNLOAD)) {
        if (assayPlate.isFailed()) {
          showMessageForComponent("cherryPicks.downloadActiveMappedPlatesOnly", 
                                  "assayPlatesTable", 
                                  assayPlate.getName());
          iter.remove();
          adjustSelection = true;
        }
      }
      else if (validationType.equals(VALIDATE_SELECTED_PLATES_FOR_LIQUID_TRANSFER)) {
        if (assayPlate.getLabCherryPicks().size() == 0 && 
          !(assayPlate instanceof LegacyCherryPickAssayPlate)) {
          // this can happen if an assay plate failed, was re-run, but no lab cherry picks could be allocated for the new plate 
          iter.remove();
          showMessageForComponent("cherryPicks.assayPlateEmpty", 
                                  "assayPlatesTable", 
                                  assayPlate.getName());
          adjustSelection = true;
        }
        else if (assayPlate.isPlated() || assayPlate.isFailed() || assayPlate.isCanceled()) {
          iter.remove();
          showMessageForComponent("cherryPicks.assayPlateAlreadyPlatedFailedCanceled", 
                                  "assayPlatesTable", 
                                  assayPlate.getName());
          adjustSelection = true;
        }
      }
    }
    
    if (adjustSelection) {
      List<AssayPlateRow> data = (List<AssayPlateRow>) getAssayPlatesDataModel().getWrappedData();
      for (AssayPlateRow row : data) {
        if (row.isSelected() && !selectedAssayPlates.contains(row.getAssayPlate())) {
          row.setSelected(false);
        }
      }
    }
    
    return !adjustSelection;
  }

  public String createNewCherryPickRequestForUnfulfilledCherryPicks()
  {
    return _screensController.createNewCherryPickRequestForUnfulfilledCherryPicks(_cherryPickRequest);
  }

//  public String createNewCherryPickRequestForSelectedAssayPlates()
//  {
//    // TODO
//    return REDISPLAY_PAGE_ACTION_RESULT;
//  }

  public String viewLeadScreener()
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
        _cherryPickRequest.setVolumeApprovedBy(_volumeApprovedBy.getSelection());
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
  private Set<CherryPickAssayPlate> getSelectedAssayPlates()
  {
    Set<CherryPickAssayPlate> selectedAssayPlates = new HashSet<CherryPickAssayPlate>();
    List<AssayPlateRow> data = (List<AssayPlateRow>) getAssayPlatesDataModel().getWrappedData();
    for (AssayPlateRow row : data) {
      if (row.isSelected()) {
        selectedAssayPlates.add(row.getAssayPlate());
      }
    }
    return selectedAssayPlates;
  }

  private String[] getSortColumns(TableSortManager sortManager,
                                  String[][] secondarySorts)
  {
    // TODO: cache!
    int primarySortColumnIndex = sortManager.getSortColumnIndex();
    String[] sortColumns = new String[1 + secondarySorts[primarySortColumnIndex].length];
    sortColumns[0] = sortManager.getSortColumnName();
    for (int i = 0; i < sortColumns.length - 1; ++i) {
      sortColumns[i + 1] = secondarySorts[primarySortColumnIndex][i];
    } 
    return sortColumns;
  }
}

