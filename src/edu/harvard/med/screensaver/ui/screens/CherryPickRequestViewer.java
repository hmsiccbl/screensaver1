// $HeadURL$
// $Id$
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
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
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
import edu.harvard.med.screensaver.ui.screenresults.DataTableRowsPerPageUISelectOneBean;
import edu.harvard.med.screensaver.ui.table.TableColumn;
import edu.harvard.med.screensaver.ui.table.TableSortManager;
import edu.harvard.med.screensaver.ui.util.ScreensaverUserComparator;
import edu.harvard.med.screensaver.ui.util.UISelectManyBean;
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

  private static final List<TableColumn<ScreenerCherryPick>> SCREENER_CHERRY_PICKS_TABLE_COLUMNS = new ArrayList<TableColumn<ScreenerCherryPick>>();
  private static final List<List<TableColumn<ScreenerCherryPick>>> SCREENER_CHERRY_PICKS_TABLE_COMPOUND_SORTS = new ArrayList<List<TableColumn<ScreenerCherryPick>>>();  
  static {
    SCREENER_CHERRY_PICKS_TABLE_COLUMNS.add(new TableColumn<ScreenerCherryPick>("Library Plate", "The library plate number of the well that was originally screened", true) {
      @Override
      public Object getCellValue(ScreenerCherryPick scp) { return scp.getScreenedWell().getPlateNumber(); }
    });
    SCREENER_CHERRY_PICKS_TABLE_COLUMNS.add(new TableColumn<ScreenerCherryPick>("Screened Well", "The name of the well that was originally screened") {
      @Override
      public Object getCellValue(ScreenerCherryPick scp) { return scp.getScreenedWell().getWellName(); }
    });
    SCREENER_CHERRY_PICKS_TABLE_COLUMNS.add(new TableColumn<ScreenerCherryPick>("Source Wells", "The number of wells to be cherry picked for the screened well", true) {
      @Override
      public Object getCellValue(ScreenerCherryPick scp) { return scp.getLabCherryPicks().size(); }
    });
    SCREENER_CHERRY_PICKS_TABLE_COLUMNS.add(new TableColumn<ScreenerCherryPick>("Vendor ID", "The vendor ID of the screened well") {
      @Override
      public Object getCellValue(ScreenerCherryPick scp) { return scp.getScreenedWell().getVendorIdentifier(); }
    });
    SCREENER_CHERRY_PICKS_TABLE_COLUMNS.add(new TableColumn<ScreenerCherryPick>("Gene", "The name of the gene targeted by the screened well") {
      @Override
      public Object getCellValue(ScreenerCherryPick scp) 
      { 
        Gene gene = scp.getScreenedWell().getGene(); 
        return gene == null ? null : gene.getGeneName(); 
      }
    });
    SCREENER_CHERRY_PICKS_TABLE_COLUMNS.add(new TableColumn<ScreenerCherryPick>("Entrez ID", "The Entrez ID of the gene targeted by the screened well") {
      @Override
      public Object getCellValue(ScreenerCherryPick scp) 
      { 
        Gene gene = scp.getScreenedWell().getGene(); 
        return gene == null ? null : gene.getEntrezgeneId(); 
      }
    });
    SCREENER_CHERRY_PICKS_TABLE_COLUMNS.add(new TableColumn<ScreenerCherryPick>("Entrez Symbol", "The Entrez symbol of the gene targeted by the screened well") {
      @Override
      public Object getCellValue(ScreenerCherryPick scp) 
      { 
        Gene gene = scp.getScreenedWell().getGene(); 
        return gene == null ? null : gene.getEntrezgeneSymbol(); 
      }
    });
    SCREENER_CHERRY_PICKS_TABLE_COLUMNS.add(new TableColumn<ScreenerCherryPick>("Genbank AccNo", "The Genbank accession number of the gene targeted by the screened well") {
      @Override
      public Object getCellValue(ScreenerCherryPick scp) 
      { 
        Gene gene = scp.getScreenedWell().getGene(); 
        return gene == null ? null : StringUtils.makeListString(gene.getGenbankAccessionNumbers(), "; "); 
      }
    });

    // define compound sorts
    SCREENER_CHERRY_PICKS_TABLE_COMPOUND_SORTS.add(new ArrayList<TableColumn<ScreenerCherryPick>>());
    SCREENER_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(0).add(SCREENER_CHERRY_PICKS_TABLE_COLUMNS.get(0));
    SCREENER_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(0).add(SCREENER_CHERRY_PICKS_TABLE_COLUMNS.get(1));

    SCREENER_CHERRY_PICKS_TABLE_COMPOUND_SORTS.add(new ArrayList<TableColumn<ScreenerCherryPick>>());
    SCREENER_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(1).add(SCREENER_CHERRY_PICKS_TABLE_COLUMNS.get(1));
    SCREENER_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(1).add(SCREENER_CHERRY_PICKS_TABLE_COLUMNS.get(0));
    
    SCREENER_CHERRY_PICKS_TABLE_COMPOUND_SORTS.add(new ArrayList<TableColumn<ScreenerCherryPick>>());
    SCREENER_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(2).add(SCREENER_CHERRY_PICKS_TABLE_COLUMNS.get(2));
    SCREENER_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(2).add(SCREENER_CHERRY_PICKS_TABLE_COLUMNS.get(0));
    SCREENER_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(2).add(SCREENER_CHERRY_PICKS_TABLE_COLUMNS.get(1));
  }

  private static final List<TableColumn<LabCherryPick>> LAB_CHERRY_PICKS_TABLE_COLUMNS = new ArrayList<TableColumn<LabCherryPick>>();
  private static final List<List<TableColumn<LabCherryPick>>> LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS = new ArrayList<List<TableColumn<LabCherryPick>>>();  
  static {
    LAB_CHERRY_PICKS_TABLE_COLUMNS.add(new TableColumn<LabCherryPick>("Status", "'Unfulfilled', 'Reserved', 'Mapped', 'Canceled', 'Plated', 'Failed'") {
      @Override
      public Object getCellValue(LabCherryPick lcp) 
      { 
        return lcp.isPlated() ? "plated" : 
          lcp.isFailed() ? "failed" :
            lcp.isCanceled() ? "canceled" :
              lcp.isMapped() ? "mapped" : 
                lcp.isAllocated() ? "reserved" : "unfulfilled";
      }
      
      @Override
      protected Comparator<LabCherryPick> getAscendingComparator()
      {
        return new Comparator<LabCherryPick>() {
          private Integer sortValue(LabCherryPick lcp)
          {
            return lcp.isPlated() ? 5 : 
              lcp.isFailed() ? 4 :
                lcp.isCanceled() ? 3 :
                  lcp.isMapped() ? 2 : 
                    lcp.isAllocated() ? 1 : 0;
          }
          public int compare(LabCherryPick o1, LabCherryPick o2)
          {
            return sortValue(o1).compareTo(sortValue(o2));
          }
        };
      }
    });
    LAB_CHERRY_PICKS_TABLE_COLUMNS.add(new TableColumn<LabCherryPick>("Library Plate", "The library plate number of the cherry picked well", true) {
      @Override
      public Object getCellValue(LabCherryPick lcp) { return lcp.getSourceWell().getPlateNumber(); }
    });
    LAB_CHERRY_PICKS_TABLE_COLUMNS.add(new TableColumn<LabCherryPick>("Source Well", "The name of the cherry picked well") {
      @Override
      public Object getCellValue(LabCherryPick lcp) { return lcp.getSourceWell().getWellName(); }
    });
    LAB_CHERRY_PICKS_TABLE_COLUMNS.add(new TableColumn<LabCherryPick>("Source Copy", "The library plate copy of the cherry picked well") {
      @Override
      public Object getCellValue(LabCherryPick lcp) { return lcp.getSourceCopy() != null ? lcp.getSourceCopy().getName() : ""; }
    });
    LAB_CHERRY_PICKS_TABLE_COLUMNS.add(new TableColumn<LabCherryPick>("Vendor ID", "The Vendor ID of the of the cherry picked well") {
      @Override
      public Object getCellValue(LabCherryPick lcp) { return lcp.getSourceWell().getVendorIdentifier(); }
    });
    LAB_CHERRY_PICKS_TABLE_COLUMNS.add(new TableColumn<LabCherryPick>("Gene", "The name of the gene targeted by the cherry picked well") {
      @Override
      public Object getCellValue(LabCherryPick lcp) 
      { 
        Gene gene = lcp.getSourceWell().getGene(); 
        return gene == null ? null : gene.getGeneName(); 
      }
    });
    LAB_CHERRY_PICKS_TABLE_COLUMNS.add(new TableColumn<LabCherryPick>("Entrez ID", "The Entrez ID of the gene targeted by the cherry picked well") {
      @Override
      public Object getCellValue(LabCherryPick lcp) 
      { 
        Gene gene = lcp.getSourceWell().getGene(); 
        return gene == null ? null : gene.getEntrezgeneId(); 
      }
    });
    LAB_CHERRY_PICKS_TABLE_COLUMNS.add(new TableColumn<LabCherryPick>("Entrez Symbol", "The Entrez symbol of the gene targeted by the cherry picked well") {
      @Override
      public Object getCellValue(LabCherryPick lcp) 
      { 
        Gene gene = lcp.getSourceWell().getGene(); 
        return gene == null ? null : gene.getEntrezgeneSymbol(); 
      }
    });
    LAB_CHERRY_PICKS_TABLE_COLUMNS.add(new TableColumn<LabCherryPick>("Genbank AccNo", "The Genbank accession number of the gene targeted by the cherry picked well") {
      @Override
      public Object getCellValue(LabCherryPick lcp) 
      { 
        Gene gene = lcp.getSourceWell().getGene(); 
        return gene == null ? null : StringUtils.makeListString(gene.getGenbankAccessionNumbers(), "; "); 
      }
    });
    LAB_CHERRY_PICKS_TABLE_COLUMNS.add(new TableColumn<LabCherryPick>("Cherry Pick Plate #", "The cherry pick plate number that this cherry pick has been mapped to", true) {
      @Override
      public Object getCellValue(LabCherryPick lcp) { return lcp.isMapped() ? new Integer(lcp.getAssayPlate().getPlateOrdinal() + 1) : null; }
    });
    LAB_CHERRY_PICKS_TABLE_COLUMNS.add(new TableColumn<LabCherryPick>("Attempt #", "The attempt number of the cherry pick plate that this cherry pick has been mapped to", true) {
      @Override
      public Object getCellValue(LabCherryPick lcp) { return lcp.isMapped() ? new Integer(lcp.getAssayPlate().getAttemptOrdinal() + 1) : null; }
    });
    LAB_CHERRY_PICKS_TABLE_COLUMNS.add(new TableColumn<LabCherryPick>("Destination Well", "The name of the well that this cherry pick has been mapped to") {
      @Override
      public Object getCellValue(LabCherryPick lcp) { return lcp.isMapped() ? lcp.getAssayPlateWellName().toString() : null; }
    });

    // define compound sorts
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.add(new ArrayList<TableColumn<LabCherryPick>>());
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(0).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(0));
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(0).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(1));
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(0).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(2));
    
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.add(new ArrayList<TableColumn<LabCherryPick>>());
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(1).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(1));
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(1).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(2));
    
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.add(new ArrayList<TableColumn<LabCherryPick>>());
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(2).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(2));
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(2).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(1));
    
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.add(new ArrayList<TableColumn<LabCherryPick>>());
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(3).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(3));
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(3).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(1));
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(3).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(2));
    
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.add(new ArrayList<TableColumn<LabCherryPick>>());
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(4).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(4));
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(4).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(1));
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(4).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(2));
    
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.add(new ArrayList<TableColumn<LabCherryPick>>());
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(5).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(5));
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(5).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(1));
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(5).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(2));
    
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.add(new ArrayList<TableColumn<LabCherryPick>>());
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(6).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(6));
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(6).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(1));
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(6).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(2));
    
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.add(new ArrayList<TableColumn<LabCherryPick>>());
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(7).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(7));
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(7).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(1));
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(7).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(2));
    
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.add(new ArrayList<TableColumn<LabCherryPick>>());
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(8).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(8));
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(8).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(1));
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(8).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(2));
    
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.add(new ArrayList<TableColumn<LabCherryPick>>());
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(9).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(9));
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(9).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(10));
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(9).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(11));
    
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.add(new ArrayList<TableColumn<LabCherryPick>>());
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(10).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(10));
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(10).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(9));
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(10).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(11));
    
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.add(new ArrayList<TableColumn<LabCherryPick>>());
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(11).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(11));
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(11).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(9));
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(11).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(10));
  }

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

  private TableSortManager<ScreenerCherryPick> _screenerCherryPicksSortManager;
  private TableSortManager<LabCherryPick> _labCherryPicksSortManager;
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

  private DataTableRowsPerPageUISelectOneBean _labCherryPicksPerPage;
  private DataTableRowsPerPageUISelectOneBean _screenerCherryPicksPerPage;


  
  // public constructors and methods
  
  public CherryPickRequestViewer()
  {
    _isPanelCollapsedMap = new HashMap<String,Boolean>();
    _isPanelCollapsedMap.put("screenSummary", true);
    _isPanelCollapsedMap.put("cherryPickRequest", false);
    _isPanelCollapsedMap.put("screenerCherryPicks", false);
    _isPanelCollapsedMap.put("labCherryPicks", false);
    _isPanelCollapsedMap.put("cherryPickPlates", false);
    
    _labCherryPicksPerPage = new DataTableRowsPerPageUISelectOneBean(Arrays.asList(10, 20, 50, 100, DataTableRowsPerPageUISelectOneBean.SHOW_ALL_VALUE));
    _screenerCherryPicksPerPage = new DataTableRowsPerPageUISelectOneBean(Arrays.asList(10, 20, 50, 100, DataTableRowsPerPageUISelectOneBean.SHOW_ALL_VALUE));
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

  public DataTableRowsPerPageUISelectOneBean getLabCherryPicksPerPage()
  {
    return _labCherryPicksPerPage;
  }

  public DataTableRowsPerPageUISelectOneBean getScreenerCherryPicksPerPage()
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

  public TableSortManager<ScreenerCherryPick> getScreenerCherryPicksSortManager()
  {
    if (_screenerCherryPicksSortManager == null) {
      _screenerCherryPicksSortManager = new TableSortManager<ScreenerCherryPick>(SCREENER_CHERRY_PICKS_TABLE_COLUMNS);
      _screenerCherryPicksSortManager.addObserver(new Observer()
      {
        public void update(Observable o, Object obj)
        {
          _screenerCherryPicksDataModel = null;
        }
      });
      for (List<TableColumn<ScreenerCherryPick>> compoundSortColumns : SCREENER_CHERRY_PICKS_TABLE_COMPOUND_SORTS) {
        _screenerCherryPicksSortManager.addCompoundSortColumns(compoundSortColumns);
      }
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
  
  public TableSortManager<LabCherryPick> getLabCherryPicksSortManager()
  {
    if (_labCherryPicksSortManager == null) {
      _labCherryPicksSortManager = new TableSortManager<LabCherryPick>(LAB_CHERRY_PICKS_TABLE_COLUMNS);
      _labCherryPicksSortManager.addObserver(new Observer() {
        public void update(Observable o, Object obj)
        {
          _labCherryPicksDataModel = null;
        }
      });
      
      for (List<TableColumn<LabCherryPick>> compoundSortColumns : LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS) {
        _labCherryPicksSortManager.addCompoundSortColumns(compoundSortColumns);
      }
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
      
      List<ScreenerCherryPick> screenerCherryPicks = new ArrayList<ScreenerCherryPick>(_cherryPickRequest.getScreenerCherryPicks());
      Collections.sort(screenerCherryPicks,
                       getScreenerCherryPicksSortManager().getSortColumnComparator());
      _screenerCherryPicksDataModel = new ListDataModel(screenerCherryPicks);
    }
    return _screenerCherryPicksDataModel;
  }

  public DataModel getLabCherryPicksDataModel()
  {
    if (_labCherryPicksDataModel == null) {
      
      List<LabCherryPick> labCherryPicks = new ArrayList<LabCherryPick>(_cherryPickRequest.getLabCherryPicks().size());
      for (LabCherryPick cherryPick : _cherryPickRequest.getLabCherryPicks()) {
        if (!_showFailedLabCherryPicks && cherryPick.isFailed()) {
          continue;
        }
        labCherryPicks.add(cherryPick);
      }
      Collections.sort(labCherryPicks,
                       getLabCherryPicksSortManager().getSortColumnComparator());
      _labCherryPicksDataModel = new ListDataModel(labCherryPicks);
    }
    return _labCherryPicksDataModel;
  }
  
  public Object getScreenerCherryPicksCellValue()
  {
    return getScreenerCherryPicksSortManager().getCurrentColumn().getCellValue((ScreenerCherryPick) getScreenerCherryPicksDataModel().getRowData());
  }
  
  public Object getLabCherryPicksCellValue()
  {
    return getLabCherryPicksSortManager().getCurrentColumn().getCellValue((LabCherryPick) getLabCherryPicksDataModel().getRowData());
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
  
  public String viewCherryPickRequestWellVolumes()
  {
    _screensController.viewCherryPickRequestWellVolumes(_cherryPickRequest, false);
    // use the special wellVolumeSearchResult page that the cherryPickAdmin role
    // can access (the normal wellVolumeSearchResult is restricted to the
    // librariesAdmin role)
    return VIEW_CHERRY_PICK_REQUEST_WELL_VOLUMES;
  }
  
  public String viewCherryPickRequestWellVolumesForUnfulfilled()
  {
    _screensController.viewCherryPickRequestWellVolumes(_cherryPickRequest, true);
    // use the special wellVolumeSearchResult page that the cherryPickAdmin role
    // can access (the normal wellVolumeSearchResult is restricted to the
    // librariesAdmin role)
    return VIEW_CHERRY_PICK_REQUEST_WELL_VOLUMES;
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
}
