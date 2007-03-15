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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.faces.model.ArrayDataModel;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.io.libraries.PlateWellListParser;
import edu.harvard.med.screensaver.io.libraries.PlateWellListParserResult;
import edu.harvard.med.screensaver.model.libraries.PlateType;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.screens.CherryPick;
import edu.harvard.med.screensaver.model.screens.CherryPickLiquidTransfer;
import edu.harvard.med.screensaver.model.screens.CherryPickRequest;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.control.ScreensController;
import edu.harvard.med.screensaver.ui.util.ScreensaverUserComparator;
import edu.harvard.med.screensaver.ui.util.UISelectOneBean;
import edu.harvard.med.screensaver.ui.util.UISelectOneEntityBean;
import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.log4j.Logger;

public class CherryPickRequestViewer extends AbstractBackingBean
{
  private static final ScreensaverUserRole EDITING_ROLE = ScreensaverUserRole.CHERRY_PICK_ADMIN;


  private static final String[] CHERRY_PICKS_TABLE_COLUMNS = { "Library Plate", "Well", "Gene", "Entrez ID", "Symbol", "AccNo" };
  private static final String[] ASSAY_PLATES_TABLE_COLUMNS = { "Assay Plate Name", "Liquid Transfers" };
  private static final String[] PLATE_MAPPING_TABLE_COLUMNS = { "Source Plate", "Source Copy", "Source Well", "Assay Plate Name", "Destination Well" };
  private static final String[] LIQUID_TRANSFER_TABLE_COLUMNS = { "Date", "By", "Assay Plates" };


  // static members

  private static Logger log = Logger.getLogger(CherryPickRequestViewer.class);


  // instance data members
  
  private DAO _dao;
  private ScreensController _screensController;
  private PlateWellListParser _plateWellListParser;

  private CherryPickRequest _cherryPickRequest;
  private boolean _isEditMode = false;
  private String _cherryPicksInput;
  private Map<String,Boolean> _assayPlateSelectionMap = new HashMap<String,Boolean>();
  private UISelectOneEntityBean<ScreeningRoomUser> _requestedBy;
  private UISelectOneBean<PlateType> _assayPlateType;

  private DataModel _cherryPicksColumnModel;
  private DataModel _cherryPicksDataModel;
  private DataModel _assayPlatesColumnModel;
  private DataModel _assayPlatesDataModel;
  private DataModel _plateMappingColumnModel;
  private DataModel _plateMappingDataModel;
  private DataModel _liquidTransferColumnModel;
  private DataModel _liquidTransferDataModel;




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

    _assayPlateType = new UISelectOneBean<PlateType>(Arrays.asList(PlateType.values()));
    
    _cherryPicksColumnModel = new ArrayDataModel(CHERRY_PICKS_TABLE_COLUMNS);
    _assayPlatesColumnModel = new ArrayDataModel(ASSAY_PLATES_TABLE_COLUMNS);
    _plateMappingColumnModel = new ArrayDataModel(PLATE_MAPPING_TABLE_COLUMNS);
    _liquidTransferColumnModel = new ArrayDataModel(LIQUID_TRANSFER_TABLE_COLUMNS);
    _cherryPicksDataModel = null;
    _assayPlatesDataModel = null;
    _plateMappingDataModel = null;
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

  public void setRequestedBy(UISelectOneEntityBean<ScreeningRoomUser> requestedBy)
  {
    _requestedBy = requestedBy;
  }

  public UISelectOneBean<PlateType> getAssayPlateType()
  {
    return _assayPlateType;
  }

  public void setAssayPlateType(UISelectOneBean<PlateType> assayPlateType)
  {
    _assayPlateType = assayPlateType;
  }
  
  public DataModel getCherryPicksColumnModel()
  {
    return _cherryPicksColumnModel;
  }
  
  public DataModel getCherryPicksDataModel()
  {
    if (_cherryPicksDataModel == null) {
      List<Map> rows = new ArrayList<Map>();
      for (CherryPick cherryPick : _cherryPickRequest.getCherryPicks()) {
        Map<String,String> row = new HashMap<String,String>();
        row.put(CHERRY_PICKS_TABLE_COLUMNS[0], cherryPick.getSourceWell().getPlateNumber().toString());
        row.put(CHERRY_PICKS_TABLE_COLUMNS[1], cherryPick.getSourceWell().getWellName());
        Iterator<SilencingReagent> silencingReagentsIter = cherryPick.getSourceWell().getSilencingReagents().iterator();
        // TODO: handle compound screens
        if (silencingReagentsIter.hasNext()) {
          SilencingReagent silencingReagent = silencingReagentsIter.next();
          row.put(CHERRY_PICKS_TABLE_COLUMNS[2], silencingReagent.getGene().getGeneName());
          row.put(CHERRY_PICKS_TABLE_COLUMNS[3], silencingReagent.getGene().getEntrezgeneId().toString());
          row.put(CHERRY_PICKS_TABLE_COLUMNS[4], silencingReagent.getGene().getEntrezgeneSymbol());
          row.put(CHERRY_PICKS_TABLE_COLUMNS[5], StringUtils.makeListString(silencingReagent.getGene().getGenbankAccessionNumbers(), "; "));
        }
        else {
          row.put(CHERRY_PICKS_TABLE_COLUMNS[2], "");
          row.put(CHERRY_PICKS_TABLE_COLUMNS[3], "");
          row.put(CHERRY_PICKS_TABLE_COLUMNS[4], "");
          row.put(CHERRY_PICKS_TABLE_COLUMNS[5], "");
        }
        rows.add(row);
      }
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
        rowValues.put(ASSAY_PLATES_TABLE_COLUMNS[0], assayPlateName);
        rowValues.put(ASSAY_PLATES_TABLE_COLUMNS[1],
                Integer.toString(_cherryPickRequest.getCherryPickLiquidTransfersForAssayPlate(assayPlateName)
                                                   .size()));
        AssayPlateRow row = new AssayPlateRow(rowValues);
        rows.add(row);
      }
      _assayPlatesDataModel = new ListDataModel(rows);
    }
    return _assayPlatesDataModel;
  }
  
  public Map<String,Boolean> getAssayPlateSelectionMap()
  {
    return _assayPlateSelectionMap;
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

  public DataModel getPlateMappingColumnModel()
  {
    return _plateMappingColumnModel;
  }

  public DataModel getPlateMappingDataModel()
  {
    if (_plateMappingDataModel == null) {
      List<Map> rows = new ArrayList<Map>();
      for (CherryPick cherryPick : _cherryPickRequest.getCherryPicks()) {
        Map<String,String> row = new HashMap<String,String>();
        row.put(PLATE_MAPPING_TABLE_COLUMNS[0], cherryPick.getSourceWell().getPlateNumber().toString());
        row.put(PLATE_MAPPING_TABLE_COLUMNS[2], cherryPick.getSourceWell().getWellName());

        if (_cherryPickRequest.isAllocated()) {
          row.put(PLATE_MAPPING_TABLE_COLUMNS[1], cherryPick.getSourceCopy().getName());
          row.put(PLATE_MAPPING_TABLE_COLUMNS[3], cherryPick.getAssayPlateName());
          row.put(PLATE_MAPPING_TABLE_COLUMNS[4], cherryPick.getAssayPlateWellName());
        }
        else {
          row.put(PLATE_MAPPING_TABLE_COLUMNS[1], "");
          row.put(PLATE_MAPPING_TABLE_COLUMNS[3], "");
          row.put(PLATE_MAPPING_TABLE_COLUMNS[4], "");
        }
        rows.add(row);
      }
      _plateMappingDataModel = new ListDataModel(rows);
    }
    return _plateMappingDataModel;
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
  
  public String allocateCherryPicks()
  {
    // TODO
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  public String deallocateCherryPicks()
  {
    return _screensController.deleteAllCherryPicks(_cherryPickRequest);
  }
  
  public String selectAllAssayPlates()
  {
    if (_assayPlateSelectionMap.size() > 0) {
      _assayPlateSelectionMap.clear();
    }
    else {
      for (String assayPlateName : _cherryPickRequest.getAssayPlates()) {
        _assayPlateSelectionMap.put(assayPlateName, true);
      }
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public String createDuplicateCherryPickRequestForSelectedAssayPlates()
  {
    // TODO
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public String downloadPlateMappingFilesForSelectedAssayPlates()
  {
    // TODO
    return REDISPLAY_PAGE_ACTION_RESULT;
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
      }
    });
  }

  // protected methods

  protected ScreensaverUserRole getEditableAdminRole()
  {
    return EDITING_ROLE;
  }

  // private methods

}

