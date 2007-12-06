// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screens;

import java.io.IOException;
import java.io.InputStream;
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
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.faces.component.UIInput;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.ArrayDataModel;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import edu.harvard.med.screensaver.db.CherryPickRequestDAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.io.cherrypicks.CherryPickRequestExporter;
import edu.harvard.med.screensaver.io.libraries.PlateWellListParser;
import edu.harvard.med.screensaver.io.libraries.PlateWellListParserResult;
import edu.harvard.med.screensaver.io.workbook.Workbook;
import edu.harvard.med.screensaver.io.workbook2.Workbook2Utils;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickAssayPlate;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickLiquidTransfer;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickLiquidTransferStatus;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.InvalidCherryPickWellException;
import edu.harvard.med.screensaver.model.cherrypicks.LabCherryPick;
import edu.harvard.med.screensaver.model.cherrypicks.LegacyCherryPickAssayPlate;
import edu.harvard.med.screensaver.model.cherrypicks.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.ScreenerCherryPick;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.service.cherrypicks.CherryPickRequestAllocator;
import edu.harvard.med.screensaver.service.cherrypicks.CherryPickRequestPlateMapFilesBuilder;
import edu.harvard.med.screensaver.service.cherrypicks.CherryPickRequestPlateMapper;
import edu.harvard.med.screensaver.service.libraries.rnai.LibraryPoolToDuplexWellMapper;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.ui.libraries.WellCopyVolume;
import edu.harvard.med.screensaver.ui.libraries.WellCopyVolumeSearchResults;
import edu.harvard.med.screensaver.ui.searchresults.IntegerColumn;
import edu.harvard.med.screensaver.ui.searchresults.TextColumn;
import edu.harvard.med.screensaver.ui.table.DataTable;
import edu.harvard.med.screensaver.ui.table.DataTableRowsPerPageUISelectOneBean;
import edu.harvard.med.screensaver.ui.table.TableColumn;
import edu.harvard.med.screensaver.ui.util.JSFUtils;
import edu.harvard.med.screensaver.ui.util.ScreensaverUserComparator;
import edu.harvard.med.screensaver.ui.util.UISelectManyBean;
import edu.harvard.med.screensaver.ui.util.UISelectOneEntityBean;
import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.log4j.Logger;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataAccessException;

public class CherryPickRequestViewer extends AbstractBackingBean
{


  // static members

  private static Logger log = Logger.getLogger(CherryPickRequestViewer.class);

  private static final ScreensaverUserRole EDITING_ROLE = ScreensaverUserRole.CHERRY_PICK_ADMIN;

  private static final String VALIDATE_SELECTED_PLATES_FOR_LIQUID_TRANSFER = "for_liquid_transfer";
  private static final String VALIDATE_SELECTED_PLATES_FOR_DOWNLOAD = "for_download";
  private static final String VALIDATE_SELECTED_PLATES_FOR_DEALLOCATION = "for_deallocaton";

  private static final List<TableColumn<ScreenerCherryPick,?>> SCREENER_CHERRY_PICKS_TABLE_COLUMNS = new ArrayList<TableColumn<ScreenerCherryPick,?>>();
  private static final List<List<TableColumn<ScreenerCherryPick,?>>> SCREENER_CHERRY_PICKS_TABLE_COMPOUND_SORTS = new ArrayList<List<TableColumn<ScreenerCherryPick,?>>>();
  static {
    SCREENER_CHERRY_PICKS_TABLE_COLUMNS.add(new IntegerColumn<ScreenerCherryPick>("Library Plate", "The library plate number of the well that was originally screened") {
      @Override
      public Integer getCellValue(ScreenerCherryPick scp) { return scp.getScreenedWell().getPlateNumber(); }
    });
    SCREENER_CHERRY_PICKS_TABLE_COLUMNS.add(new TextColumn<ScreenerCherryPick>("Screened Well", "The name of the well that was originally screened") {
      @Override
      public String getCellValue(ScreenerCherryPick scp) { return scp.getScreenedWell().getWellName(); }
    });
    SCREENER_CHERRY_PICKS_TABLE_COLUMNS.add(new IntegerColumn<ScreenerCherryPick>("Source Wells", "The number of wells to be cherry picked for the screened well") {
      @Override
      public Integer getCellValue(ScreenerCherryPick scp) { return scp.getLabCherryPicks().size(); }
    });
    SCREENER_CHERRY_PICKS_TABLE_COLUMNS.add(new TextColumn<ScreenerCherryPick>("Vendor ID", "The vendor ID of the screened well") {
      @Override
      public String getCellValue(ScreenerCherryPick scp) { return scp.getScreenedWell().getSimpleVendorIdentifier(); }
    });
    SCREENER_CHERRY_PICKS_TABLE_COLUMNS.add(new TextColumn<ScreenerCherryPick>("Gene", "The name of the gene targeted by the screened well") {
      @Override
      public String getCellValue(ScreenerCherryPick scp)
      {
        Gene gene = scp.getScreenedWell().getGene();
        return gene == null ? null : gene.getGeneName();
      }
    });
    SCREENER_CHERRY_PICKS_TABLE_COLUMNS.add(new IntegerColumn<ScreenerCherryPick>("Entrez ID", "The Entrez ID of the gene targeted by the screened well") {
      @Override
      public Integer getCellValue(ScreenerCherryPick scp)
      {
        Gene gene = scp.getScreenedWell().getGene();
        return gene == null ? null : gene.getEntrezgeneId();
      }
    });
    SCREENER_CHERRY_PICKS_TABLE_COLUMNS.add(new TextColumn<ScreenerCherryPick>("Entrez Symbol", "The Entrez symbol of the gene targeted by the screened well") {
      @Override
      public String getCellValue(ScreenerCherryPick scp)
      {
        Gene gene = scp.getScreenedWell().getGene();
        return gene == null ? null : gene.getEntrezgeneSymbol();
      }
    });
    SCREENER_CHERRY_PICKS_TABLE_COLUMNS.add(new TextColumn<ScreenerCherryPick>("Genbank AccNo", "The Genbank accession number of the gene targeted by the screened well") {
      @Override
      public String getCellValue(ScreenerCherryPick scp)
      {
        Gene gene = scp.getScreenedWell().getGene();
        return gene == null ? null : StringUtils.makeListString(gene.getGenbankAccessionNumbers(), "; ");
      }
    });

    // define compound sorts
    SCREENER_CHERRY_PICKS_TABLE_COMPOUND_SORTS.add(new ArrayList<TableColumn<ScreenerCherryPick,?>>());
    SCREENER_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(0).add(SCREENER_CHERRY_PICKS_TABLE_COLUMNS.get(0));
    SCREENER_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(0).add(SCREENER_CHERRY_PICKS_TABLE_COLUMNS.get(1));

    SCREENER_CHERRY_PICKS_TABLE_COMPOUND_SORTS.add(new ArrayList<TableColumn<ScreenerCherryPick,?>>());
    SCREENER_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(1).add(SCREENER_CHERRY_PICKS_TABLE_COLUMNS.get(1));
    SCREENER_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(1).add(SCREENER_CHERRY_PICKS_TABLE_COLUMNS.get(0));

    SCREENER_CHERRY_PICKS_TABLE_COMPOUND_SORTS.add(new ArrayList<TableColumn<ScreenerCherryPick,?>>());
    SCREENER_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(2).add(SCREENER_CHERRY_PICKS_TABLE_COLUMNS.get(2));
    SCREENER_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(2).add(SCREENER_CHERRY_PICKS_TABLE_COLUMNS.get(0));
    SCREENER_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(2).add(SCREENER_CHERRY_PICKS_TABLE_COLUMNS.get(1));
  }

  private static final List<TableColumn<LabCherryPick,?>> LAB_CHERRY_PICKS_TABLE_COLUMNS = new ArrayList<TableColumn<LabCherryPick,?>>();
  private static final List<List<TableColumn<LabCherryPick,?>>> LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS = new ArrayList<List<TableColumn<LabCherryPick,?>>>();
  static {
    LAB_CHERRY_PICKS_TABLE_COLUMNS.add(new TextColumn<LabCherryPick>("Status", "'Unfulfilled', 'Reserved', 'Mapped', 'Canceled', 'Plated', 'Failed'") {
      @Override
      public String getCellValue(LabCherryPick lcp)
      {
        return lcp.isPlated() ? "plated" :
          lcp.isFailed() ? "failed" :
            lcp.isCancelled() ? "canceled" :
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
                lcp.isCancelled() ? 3 :
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
    LAB_CHERRY_PICKS_TABLE_COLUMNS.add(new IntegerColumn<LabCherryPick>("Library Plate", "The library plate number of the cherry picked well") {
      @Override
      public Integer getCellValue(LabCherryPick lcp) { return lcp.getSourceWell().getPlateNumber(); }
    });
    LAB_CHERRY_PICKS_TABLE_COLUMNS.add(new TextColumn<LabCherryPick>("Source Well", "The name of the cherry picked well") {
      @Override
      public String getCellValue(LabCherryPick lcp) { return lcp.getSourceWell().getWellName(); }
    });
    LAB_CHERRY_PICKS_TABLE_COLUMNS.add(new TextColumn<LabCherryPick>("Source Copy", "The library plate copy of the cherry picked well") {
      @Override
      public String getCellValue(LabCherryPick lcp) { return lcp.getSourceCopy() != null ? lcp.getSourceCopy().getName() : ""; }
    });
    LAB_CHERRY_PICKS_TABLE_COLUMNS.add(new TextColumn<LabCherryPick>("Vendor ID", "The Vendor ID of the of the cherry picked well") {
      @Override
      public String getCellValue(LabCherryPick lcp) { return lcp.getSourceWell().getSimpleVendorIdentifier(); }
    });
    LAB_CHERRY_PICKS_TABLE_COLUMNS.add(new TextColumn<LabCherryPick>("Gene", "The name of the gene targeted by the cherry picked well") {
      @Override
      public String getCellValue(LabCherryPick lcp)
      {
        Gene gene = lcp.getSourceWell().getGene();
        return gene == null ? null : gene.getGeneName();
      }
    });
    LAB_CHERRY_PICKS_TABLE_COLUMNS.add(new IntegerColumn<LabCherryPick>("Entrez ID", "The Entrez ID of the gene targeted by the cherry picked well") {
      @Override
      public Integer getCellValue(LabCherryPick lcp)
      {
        Gene gene = lcp.getSourceWell().getGene();
        return gene == null ? null : gene.getEntrezgeneId();
      }
    });
    LAB_CHERRY_PICKS_TABLE_COLUMNS.add(new TextColumn<LabCherryPick>("Entrez Symbol", "The Entrez symbol of the gene targeted by the cherry picked well") {
      @Override
      public String getCellValue(LabCherryPick lcp)
      {
        Gene gene = lcp.getSourceWell().getGene();
        return gene == null ? null : gene.getEntrezgeneSymbol();
      }
    });
    LAB_CHERRY_PICKS_TABLE_COLUMNS.add(new TextColumn<LabCherryPick>("Genbank AccNo", "The Genbank accession number of the gene targeted by the cherry picked well") {
      @Override
      public String getCellValue(LabCherryPick lcp)
      {
        Gene gene = lcp.getSourceWell().getGene();
        return gene == null ? null : StringUtils.makeListString(gene.getGenbankAccessionNumbers(), "; ");
      }
    });
    LAB_CHERRY_PICKS_TABLE_COLUMNS.add(new IntegerColumn<LabCherryPick>("Cherry Pick Plate #", "The cherry pick plate number that this cherry pick has been mapped to") {
      @Override
      public Integer getCellValue(LabCherryPick lcp) { return lcp.isMapped() ? new Integer(lcp.getAssayPlate().getPlateOrdinal() + 1) : null; }
    });
    LAB_CHERRY_PICKS_TABLE_COLUMNS.add(new IntegerColumn<LabCherryPick>("Attempt #", "The attempt number of the cherry pick plate that this cherry pick has been mapped to") {
      @Override
      public Integer getCellValue(LabCherryPick lcp) { return lcp.isMapped() ? new Integer(lcp.getAssayPlate().getAttemptOrdinal() + 1) : null; }
    });
    LAB_CHERRY_PICKS_TABLE_COLUMNS.add(new TextColumn<LabCherryPick>("Destination Well", "The name of the well that this cherry pick has been mapped to") {
      @Override
      public String getCellValue(LabCherryPick lcp) { return lcp.isMapped() ? lcp.getAssayPlateWellName().toString() : null; }
    });

    // define compound sorts
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.add(new ArrayList<TableColumn<LabCherryPick,?>>());
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(0).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(0));
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(0).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(1));
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(0).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(2));

    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.add(new ArrayList<TableColumn<LabCherryPick,?>>());
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(1).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(1));
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(1).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(2));

    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.add(new ArrayList<TableColumn<LabCherryPick,?>>());
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(2).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(2));
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(2).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(1));

    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.add(new ArrayList<TableColumn<LabCherryPick,?>>());
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(3).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(3));
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(3).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(1));
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(3).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(2));

    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.add(new ArrayList<TableColumn<LabCherryPick,?>>());
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(4).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(4));
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(4).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(1));
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(4).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(2));

    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.add(new ArrayList<TableColumn<LabCherryPick,?>>());
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(5).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(5));
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(5).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(1));
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(5).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(2));

    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.add(new ArrayList<TableColumn<LabCherryPick,?>>());
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(6).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(6));
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(6).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(1));
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(6).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(2));

    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.add(new ArrayList<TableColumn<LabCherryPick,?>>());
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(7).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(7));
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(7).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(1));
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(7).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(2));

    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.add(new ArrayList<TableColumn<LabCherryPick,?>>());
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(8).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(8));
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(8).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(1));
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(8).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(2));

    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.add(new ArrayList<TableColumn<LabCherryPick,?>>());
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(9).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(9));
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(9).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(10));
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(9).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(11));

    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.add(new ArrayList<TableColumn<LabCherryPick,?>>());
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(10).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(10));
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(10).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(9));
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(10).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(11));

    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.add(new ArrayList<TableColumn<LabCherryPick,?>>());
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(11).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(11));
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(11).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(9));
    LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS.get(11).add(LAB_CHERRY_PICKS_TABLE_COLUMNS.get(10));
  }

  private static final Collection<Integer> PLATE_COLUMNS_LIST = new ArrayList<Integer>();
  private static final Collection<Character> PLATE_ROWS_LIST = new ArrayList<Character>();

  static {
    for (int i = Well.MIN_WELL_COLUMN; i <= Well.MAX_WELL_COLUMN; i++) {
      PLATE_COLUMNS_LIST.add(i);
    }
    for (char i = Well.MIN_WELL_ROW; i <= Well.MAX_WELL_ROW; i++) {
      PLATE_ROWS_LIST.add(i);
    }
  }


  // instance data members

  private GenericEntityDAO _dao;
  private CherryPickRequestDAO _cherryPickRequestDao;
  private LibrariesDAO _librariesDao;
  private ScreenViewer _screenViewer;
  private WellCopyVolumeSearchResults _wellCopyVolumesBrowser;
  private PlateWellListParser _plateWellListParser;
  private CherryPickRequestAllocator _cherryPickRequestAllocator;
  private CherryPickRequestPlateMapper _cherryPickRequestPlateMapper;
  private CherryPickRequestPlateMapFilesBuilder _cherryPickRequestPlateMapFilesBuilder;
  private LibraryPoolToDuplexWellMapper _libraryPoolToDuplexWellMapper;
  private CherryPickRequestExporter _cherryPickRequestExporter;

  private CherryPickRequest _cherryPickRequest;
  private boolean _isEditMode = false;
  private Map<String,Boolean> _isPanelCollapsedMap;
  private String _cherryPicksInput;
  private UISelectOneEntityBean<ScreeningRoomUser> _requestedBy;
  private UISelectOneEntityBean<AdministratorUser> _volumeApprovedBy;
  private UISelectManyBean<Integer> _emptyColumnsOnAssayPlate;
  private UISelectManyBean<Character> _emptyRowsOnAssayPlate;

  private DataTable<ScreenerCherryPick> _screenerCherryPicksDataTable;
  private DataTable<LabCherryPick> _labCherryPicksDataTable;

  private DataModel _assayPlatesColumnModel;
  private DataModel _assayPlatesDataModel;
  private boolean _selectAllAssayPlates = true;
  private boolean _showFailedAssayPlates;
  private boolean _showFailedLabCherryPicks;

  private UISelectOneEntityBean<ScreensaverUser> _liquidTransferPerformedBy;
  private Date _dateOfLiquidTransfer = new Date();
  private String _liquidTransferComments;


  // public constructors and methods

  /**
   * @motivation for CGLIB2
   */
  protected CherryPickRequestViewer()
  {
  }

  public CherryPickRequestViewer(GenericEntityDAO dao,
                                 CherryPickRequestDAO cherryPickRequestDao,
                                 LibrariesDAO librariesDao,
                                 ScreenViewer screenViewer,
                                 WellCopyVolumeSearchResults wellCopyVolumesBrowser,
                                 PlateWellListParser plateWellListParser,
                                 CherryPickRequestAllocator cherryPickRequestAllocator,
                                 CherryPickRequestPlateMapper cherryPickRequestPlateMapper,
                                 CherryPickRequestPlateMapFilesBuilder cherryPickRequestPlateMapFilesBuilder,
                                 LibraryPoolToDuplexWellMapper libraryPoolToDuplexWellMapper,
                                 CherryPickRequestExporter cherryPickRequestExporter)
  {
    _dao = dao;
    _cherryPickRequestDao = cherryPickRequestDao;
    _librariesDao = librariesDao;
    _screenViewer = screenViewer;
    _wellCopyVolumesBrowser = wellCopyVolumesBrowser;
    _plateWellListParser = plateWellListParser;
    _cherryPickRequestAllocator = cherryPickRequestAllocator;
    _cherryPickRequestPlateMapper = cherryPickRequestPlateMapper;
    _cherryPickRequestPlateMapFilesBuilder = cherryPickRequestPlateMapFilesBuilder;
    _libraryPoolToDuplexWellMapper = libraryPoolToDuplexWellMapper;
    _cherryPickRequestExporter = cherryPickRequestExporter;

    _isPanelCollapsedMap = new HashMap<String,Boolean>();
    _isPanelCollapsedMap.put("screenSummary", true);
    _isPanelCollapsedMap.put("cherryPickRequest", false);
    _isPanelCollapsedMap.put("screenerCherryPicks", false);
    _isPanelCollapsedMap.put("labCherryPicks", false);
    _isPanelCollapsedMap.put("cherryPickPlates", false);

    _labCherryPicksDataTable = new DataTable<LabCherryPick>(){
      @Override
      protected DataTableRowsPerPageUISelectOneBean buildRowsPerPageSelector()
      {
        return new DataTableRowsPerPageUISelectOneBean(Arrays.asList(10, 20, 50, 100, DataTableRowsPerPageUISelectOneBean.SHOW_ALL_VALUE)) {
          @Override
          protected Integer getAllRowsValue() { return getRowCount(); }
        };
      }

      @Override
      protected DataModel buildDataModel()
      {
        return buildLabCherryPicksDataModel();
      }

      @Override
      protected List<TableColumn<LabCherryPick,?>> buildColumns()
      {
        return LAB_CHERRY_PICKS_TABLE_COLUMNS;
      }
    };
    _labCherryPicksDataTable.getSortManager().addAllCompoundSorts(LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS);

    _screenerCherryPicksDataTable = new DataTable<ScreenerCherryPick>(){
      @Override
      protected DataTableRowsPerPageUISelectOneBean buildRowsPerPageSelector()
      {
        return new DataTableRowsPerPageUISelectOneBean(Arrays.asList(10, 20, 50, 100, DataTableRowsPerPageUISelectOneBean.SHOW_ALL_VALUE)) {
          @Override
          protected Integer getAllRowsValue() { return getRowCount(); }
        };
      }

      @Override
      protected DataModel buildDataModel()
      {
        return buildScreenerCherryPicksDataModel();
      }

      @Override
      protected List<TableColumn<ScreenerCherryPick,?>> buildColumns()
      {
        return SCREENER_CHERRY_PICKS_TABLE_COLUMNS;
      }
    };
    _screenerCherryPicksDataTable.getSortManager().addAllCompoundSorts(SCREENER_CHERRY_PICKS_TABLE_COMPOUND_SORTS);
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

    Set<Character> selectableEmptyRows = new TreeSet<Character>(PLATE_ROWS_LIST);
    selectableEmptyRows.removeAll(_cherryPickRequest.getRequiredEmptyRowsOnAssayPlate());
    _emptyRowsOnAssayPlate =
      new UISelectManyBean<Character>(selectableEmptyRows,
                                    _cherryPickRequest.getRequestedEmptyRowsOnAssayPlate());

    _screenerCherryPicksDataTable.rebuildRows();
    _labCherryPicksDataTable.rebuildRows();
    _assayPlatesColumnModel = new ArrayDataModel(AssayPlateRow.ASSAY_PLATES_TABLE_COLUMNS);
    _assayPlatesDataModel = null;

    // set "Cherry Pick Plates" panel to initially expanded, if cherry pick plates have been created
    boolean hasCherryPickPlates = _cherryPickRequest.getCherryPickAssayPlates().size() > 0;
    _isPanelCollapsedMap.put("cherryPickPlates", !hasCherryPickPlates);
  }

  public CherryPickRequest getCherryPickRequest()
  {
    return _cherryPickRequest;
  }

  @UIControllerMethod
  public String viewCherryPickRequest(final CherryPickRequest cherryPickRequestIn)
  {
    // TODO: implement as aspect
    if (cherryPickRequestIn.isRestricted()) {
      showMessage("restrictedEntity", "Cherry Pick Request " + cherryPickRequestIn.getCherryPickRequestNumber());
      log.warn("user unauthorized to view " + cherryPickRequestIn);
      return REDISPLAY_PAGE_ACTION_RESULT;
    }

    try {
      _dao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          CherryPickRequest cherryPickRequest = _dao.reloadEntity(cherryPickRequestIn,
                                                                  true,
                                                                  "requestedBy",
                                                                  "screen.labHead",
                                                                  "screen.leadScreener",
                                                                  "screen.collaborators");
          if (cherryPickRequest.getScreen().getScreenType().equals(ScreenType.SMALL_MOLECULE)) {
            throw new UnsupportedOperationException("Sorry, but viewing compound cherry pick requests is not yet implemented.");
          }

          _dao.needReadOnly(cherryPickRequest,
                            "cherryPickAssayPlates.cherryPickLiquidTransfer.performedBy",
                            "cherryPickAssayPlates.labCherryPicks.sourceWell");
          if (cherryPickRequest.getScreen().getScreenType().equals(ScreenType.RNAI)) {
            _dao.needReadOnly(cherryPickRequest,
                              "labCherryPicks.sourceWell.silencingReagents.gene.genbankAccessionNumbers");
            _dao.needReadOnly(cherryPickRequest,
                              "screenerCherryPicks.screenedWell.silencingReagents.gene.genbankAccessionNumbers",
                              "screenerCherryPicks.rnaiKnockdownConfirmation");
            _dao.needReadOnly(cherryPickRequest,
                              "screenerCherryPicks.labCherryPicks.wellVolumeAdjustments.copy");
          }
//          else if (cherryPickRequest.getScreen().getScreenType().equals(ScreenType.SMALL_MOLECULE)) {
//            // TODO: inflate, as needed
//            _dao.needReadOnly(cherryPickRequest,
//                              "screenerCherryPicks",
//                              "screenerCherryPicks.labCherryPicks");
//          }
          setCherryPickRequest(cherryPickRequest);
        }
      });
      return VIEW_CHERRY_PICK_REQUEST_ACTION_RESULT;
    }
    catch (DataAccessException e) {
      showMessage("databaseOperationFailed", e.getMessage());
    }
    catch (UnsupportedOperationException e) {
      reportApplicationError(e);
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  public boolean isEditMode()
  {
    return _isEditMode;
  }

  public Map getIsPanelCollapsedMap()
  {
    return _isPanelCollapsedMap;
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

  public DataTable<ScreenerCherryPick> getScreenerCherryPicksDataTable()
  {
    return _screenerCherryPicksDataTable;
  }

  public DataTable<LabCherryPick> getLabCherryPicksDataTable()
  {
    return _labCherryPicksDataTable;
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

  public UISelectManyBean<Character> getEmptyRowsOnAssayPlate()
  {
    return _emptyRowsOnAssayPlate;
  }

  public String getEmptyRowsOnAssayPlateAsString()
  {
    return StringUtils.makeListString(new TreeSet<Character>(_cherryPickRequest.getRequestedEmptyRowsOnAssayPlate()), ", ");
  }

  public int getScreenerCherryPickCount()
  {
    return _cherryPickRequest.getScreenerCherryPicks().size();
  }

  public int getLabCherryPickCount()
  {
    return _cherryPickRequest.getLabCherryPicks().size();
  }

  public int getActiveCherryPickPlatesCount()
  {
    return _cherryPickRequest.getActiveCherryPickAssayPlates().size();
  }

  public int getCompletedCherryPickPlatesCount()
  {
    return _cherryPickRequest.getCompletedCherryPickAssayPlates().size();
  }

  public boolean isRnaiScreen()
  {
    return _cherryPickRequest.getScreen().getScreenType().equals(ScreenType.RNAI);
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
      _labCherryPicksDataTable.rebuildRows();
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


  // JSF listeners

  public void toggleShowFailedLabCherryPicks(ValueChangeEvent event)
  {
    _showFailedLabCherryPicks = (Boolean) event.getNewValue();
    // avoid having JSF set backing bean property with the submitted value
    ((UIInput) event.getComponent()).setLocalValueSet(false);
    // force regen of data model
    _labCherryPicksDataTable.rebuildRows();
  }

  public void toggleShowFailedAssayPlates(ValueChangeEvent event)
  {
    _showFailedAssayPlates = (Boolean) event.getNewValue();
    // avoid having JSF set backing bean property with the submitted value
    ((UIInput) event.getComponent()).setLocalValueSet(false);
    // force regen of data model
    _assayPlatesDataModel = null;
  }


  // JSF application methods

  @UIControllerMethod
  public String viewScreen()
  {
    return _screenViewer.viewScreen(_cherryPickRequest.getScreen());
  }

  @UIControllerMethod
  public String addCherryPicksForWells()
  {
    PlateWellListParserResult result = _plateWellListParser.parseWellsFromPlateWellList(_cherryPicksInput);
    // TODO: report errors
    if (result.getErrors().size() > 0) {
      showMessage("cherryPicks.parseError");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    return doAddCherryPicksForWells(_cherryPickRequest,
                                    result.getParsedWellKeys(),
                                    false);
  }

  @UIControllerMethod
  public String addCherryPicksForPoolWells()
  {
    PlateWellListParserResult result = _plateWellListParser.parseWellsFromPlateWellList(_cherryPicksInput);
    // TODO: report errors
    if (result.getErrors().size() > 0) {
      showMessage("cherryPicks.parseError");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    return doAddCherryPicksForWells(_cherryPickRequest,
                                    result.getParsedWellKeys(),
                                    true);
  }

  @UIControllerMethod
  public String deleteCherryPickRequest()
  {
    if (_cherryPickRequest != null) {
      try {
        _cherryPickRequestDao.deleteCherryPickRequest(_cherryPickRequest);
        return _screenViewer.viewScreen(_cherryPickRequest.getScreen());
      }
      catch (ConcurrencyFailureException e) {
        showMessage("concurrentModificationConflict");
      }
      catch (DataAccessException e) {
        showMessage("databaseOperationFailed", e.getMessage());
      }
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String deleteAllCherryPicks()
  {
    try {
      _dao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          CherryPickRequest cherryPickRequest = _dao.reloadEntity(_cherryPickRequest,
                                                                  false,
                                                                  "labCherryPicks.sourceWell");
          _dao.need(cherryPickRequest,
                    "screenerCherryPicks.screenedWell",
                    "screenerCherryPicks.rnaiKnockdownConfirmation");
          if (cherryPickRequest.isAllocated()) {
            throw new BusinessRuleViolationException("cherry picks cannot be deleted once a cherry pick request has been allocated");
          }
          Set<ScreenerCherryPick> cherryPicksToDelete = new HashSet<ScreenerCherryPick>(cherryPickRequest.getScreenerCherryPicks());
          for (ScreenerCherryPick cherryPick : cherryPicksToDelete) {
            _cherryPickRequestDao.deleteScreenerCherryPick(cherryPick);
          }
        }
      });
    }
    catch (DataAccessException e) {
      showMessage("databaseOperationFailed", e.getMessage());
    }

    return viewCherryPickRequest(_cherryPickRequest);
  }


  @UIControllerMethod
  public String viewCherryPickRequestWellVolumes()
  {
    return doViewCherryPickRequestWellVolumes(false);
  }

  @UIControllerMethod
  public String viewCherryPickRequestWellVolumesForUnfulfilled()
  {
    return doViewCherryPickRequestWellVolumes(true);
  }

  @UIControllerMethod
  public String allocateCherryPicks()
  {
    if (_cherryPickRequest.getMicroliterTransferVolumePerWellApproved() == null) {
      showMessage("cherryPicks.approvedCherryPickVolumeRequired");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }

    try {
      Set<LabCherryPick> unfulfillable = _cherryPickRequestAllocator.allocate(_cherryPickRequest);
      if (unfulfillable.size() == _cherryPickRequest.getLabCherryPicks().size()) {
        showMessage("cherryPicks.allCherryPicksUnfulfillable");
      }
      else if (unfulfillable.size() > 0) {
        showMessage("cherryPicks.someCherryPicksUnfulfillable");
      }
    }
    catch (ConcurrencyFailureException e) {
      showMessage("concurrentModificationConflict");
    }
    catch (DataAccessException e) {
      showMessage("databaseOperationFailed", e.getMessage());
    }
    return viewCherryPickRequest(_cherryPickRequest);
  }

  @UIControllerMethod
  public String deallocateCherryPicks()
  {
    try {
      _cherryPickRequestAllocator.deallocate(_cherryPickRequest);
    }
    catch (ConcurrencyFailureException e) {
      showMessage("concurrentModificationConflict");
    }
    catch (DataAccessException e) {
      showMessage("databaseOperationFailed", e.getMessage());
    }
    return viewCherryPickRequest(_cherryPickRequest);
  }

  @UIControllerMethod
  public String deallocateCherryPicksByPlate()
  {
    if (!validateSelectedAssayPlates(VALIDATE_SELECTED_PLATES_FOR_DEALLOCATION)) {
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    try {
      _cherryPickRequestAllocator.cancelAndDeallocateAssayPlates(_cherryPickRequest,
                                                                 getSelectedAssayPlates(),
                                                                 getLiquidTransferPerformedBy().getSelection(),
                                                                 getDateOfLiquidTransfer(),
                                                                 getLiquidTransferComments());
    }
    catch (ConcurrencyFailureException e) {
      showMessage("concurrentModificationConflict");
    }
    catch (DataAccessException e) {
      showMessage("databaseOperationFailed", e.getMessage());
    }
    return viewCherryPickRequest(_cherryPickRequest);
  }

  @UIControllerMethod
  public String plateMapCherryPicks()
  {
    try {
      _cherryPickRequestPlateMapper.generatePlateMapping(_cherryPickRequest);
    }
    catch (ConcurrencyFailureException e) {
      showMessage("concurrentModificationConflict");
    }
    catch (DataAccessException e) {
      showMessage("databaseOperationFailed", e.getMessage());
    }
    return viewCherryPickRequest(_cherryPickRequest);
  }

  @SuppressWarnings("unchecked")
  @UIControllerMethod
  public String selectAllAssayPlates()
  {
    List<AssayPlateRow> data = (List<AssayPlateRow>) getAssayPlatesDataModel().getWrappedData();
    for (AssayPlateRow row : data) {
      row.setSelected(_selectAllAssayPlates);
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String downloadPlateMappingFilesForSelectedAssayPlates()
  {
    if (!validateSelectedAssayPlates(VALIDATE_SELECTED_PLATES_FOR_DOWNLOAD)) {
      return REDISPLAY_PAGE_ACTION_RESULT;
    }

    final Set<CherryPickAssayPlate> plateNames = getSelectedAssayPlates();
    if (plateNames.size() == 0) {
      showMessage("cherryPicks.noPlatesSelected", "assayPlatesTable");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }

    try {
      _dao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          CherryPickRequest cherryPickRequest = _dao.reloadEntity(_cherryPickRequest);
          try {
            if (cherryPickRequest != null) {
              InputStream zipStream = _cherryPickRequestPlateMapFilesBuilder.buildZip(cherryPickRequest, plateNames);
              JSFUtils.handleUserDownloadRequest(getFacesContext(),
                                                 zipStream,
                                                 "CherryPickRequest" + cherryPickRequest.getEntityId() + "_PlateMapFiles.zip",
              "application/zip");
            }
          }
          catch (IOException e)
          {
            reportApplicationError(e);
          }
          finally {
          }
        }
      });
    }
    catch (DataAccessException e) {
      showMessage("databaseOperationFailed", e.getMessage());
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String recordLiquidTransferForSelectedAssayPlates()
  {
    if (!validateSelectedAssayPlates(VALIDATE_SELECTED_PLATES_FOR_LIQUID_TRANSFER)) {
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    try {
      doRecordLiquidTransferForAssayPlates(getSelectedAssayPlates(),
                                           getLiquidTransferPerformedBy().getSelection(),
                                           getDateOfLiquidTransfer(),
                                           getLiquidTransferComments(),
                                           true);
    }
    catch (DataAccessException e) {
      showMessage("databaseOperationFailed", e.getMessage());
    }
    return viewCherryPickRequest(_cherryPickRequest);
  }

  @UIControllerMethod
  public String recordFailureOfAssayPlates()
  {
    if (!validateSelectedAssayPlates(VALIDATE_SELECTED_PLATES_FOR_LIQUID_TRANSFER)) {
      return REDISPLAY_PAGE_ACTION_RESULT;
    }

    // create new assay plates, duplicating plate name, lab cherry picks with same layout but new copy selection, incrementing attempt ordinal
    try {
      final ScreensaverUser performedBy = getLiquidTransferPerformedBy().getSelection();
      _dao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          Set<CherryPickAssayPlate> selectedAssayPlates = getSelectedAssayPlates();
          doRecordLiquidTransferForAssayPlates(selectedAssayPlates,
                                               performedBy,
                                               getDateOfLiquidTransfer(),
                                               getLiquidTransferComments(),
                                               false);

          boolean someCherryPicksUnfulfillable = false;
          for (CherryPickAssayPlate assayPlate : selectedAssayPlates) {
            _dao.reattachEntity(assayPlate);

            // Construct a CherryPickAssayPlate from an existing one, preserving the
            // plate ordinal and plate type, but incrementing the attempt ordinal. The new
            // assay plate will have a new set of lab cherry picks that duplicate the
            // original plate's lab cherry picks, preserving their original well layout,
            // and allocated anew.
            // TODO: protect against race condition (should enforce at schema level)
            CherryPickAssayPlate newAssayPlate = (CherryPickAssayPlate) assayPlate.clone();
            for (LabCherryPick labCherryPick : assayPlate.getLabCherryPicks()) {
              LabCherryPick newLabCherryPick =
                labCherryPick.getScreenerCherryPick().getCherryPickRequest().createLabCherryPick(
                    labCherryPick.getScreenerCherryPick(),
                    labCherryPick.getSourceWell());
              _dao.saveOrUpdateEntity(newLabCherryPick);
              if (!_cherryPickRequestAllocator.allocate(newLabCherryPick)) {
                someCherryPicksUnfulfillable = true;
              } else {
                newLabCherryPick.setMapped(newAssayPlate,
                                           labCherryPick.getAssayPlateRow(),
                                           labCherryPick.getAssayPlateColumn());
              }
            }
          }
          if (someCherryPicksUnfulfillable) {
            showMessage("cherryPicks.someCherryPicksUnfulfillable");
          }
        }
      });
    }
    catch (DataAccessException e) {
      showMessage("databaseOperationFailed", e.getMessage());
    }
    return viewCherryPickRequest(_cherryPickRequest);
  }

  @UIControllerMethod
  public String downloadCherryPickRequest()
  {
    if (_cherryPickRequest instanceof RNAiCherryPickRequest) {
      try {
        jxl.Workbook workbook = _cherryPickRequestExporter.exportRNAiCherryPickRequest((RNAiCherryPickRequest) _cherryPickRequest);
        JSFUtils.handleUserDownloadRequest(getFacesContext(),
                                           Workbook2Utils.toInputStream(workbook),
                                           _cherryPickRequest.getClass().getSimpleName() + "-" + _cherryPickRequest.getCherryPickRequestNumber() + ".xls",
                                           Workbook.MIME_TYPE);
      }
      catch (Exception e) {
        reportSystemError(e);
      }
    }
    else {
      showMessage("systemError", "downloading of compound cherry pick requests is not yet implemented");
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }


  @UIControllerMethod
  public String createNewCherryPickRequestForUnfulfilledCherryPicks()
  {
    final CherryPickRequest[] result = new CherryPickRequest[1];
    try {
      _dao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          CherryPickRequest cherryPickRequest = (CherryPickRequest) _dao.reattachEntity(_cherryPickRequest);
          CherryPickRequest newCherryPickRequest = cherryPickRequest.getScreen().createCherryPickRequest();
          newCherryPickRequest.setComments("Created for unfulfilled cherry picks in Cherry Pick Request " +
                                           cherryPickRequest.getCherryPickRequestNumber());
          // TODO: this might be better done in a copy constructor
          newCherryPickRequest.setMicroliterTransferVolumePerWellApproved(cherryPickRequest.getMicroliterTransferVolumePerWellApproved());
          newCherryPickRequest.setMicroliterTransferVolumePerWellRequested(cherryPickRequest.getMicroliterTransferVolumePerWellRequested());
          newCherryPickRequest.setVolumeApprovedBy(cherryPickRequest.getVolumeApprovedBy());
          newCherryPickRequest.setDateVolumeApproved(cherryPickRequest.getDateVolumeApproved());
          newCherryPickRequest.setDateRequested(new Date());
          newCherryPickRequest.setRandomizedAssayPlateLayout(cherryPickRequest.isRandomizedAssayPlateLayout());
          newCherryPickRequest.addRequestedEmptyColumnsOnAssayPlate(cherryPickRequest.getRequestedEmptyColumnsOnAssayPlate());
          newCherryPickRequest.setRequestedBy(cherryPickRequest.getRequestedBy());
          // note: we can only instantiate one new ScreenerCherryPick per *set*
          // of LabCherryPicks from the same screenedWell, otherwise we'll
          // (appropriately) get a DuplicateEntityException
          for (ScreenerCherryPick screenerCherryPick : cherryPickRequest.getScreenerCherryPicks()) {
            ScreenerCherryPick newScreenerCherryPick = null;
            for (LabCherryPick labCherryPick : screenerCherryPick.getLabCherryPicks()) {
              if (!labCherryPick.isAllocated() && !labCherryPick.isCancelled()) {
                if (newScreenerCherryPick == null) {
                  newScreenerCherryPick = newCherryPickRequest.createScreenerCherryPick(labCherryPick.getScreenerCherryPick().getScreenedWell());
                }
                newCherryPickRequest.createLabCherryPick(newScreenerCherryPick, labCherryPick.getSourceWell());
              }
            }
          }
          _dao.saveOrUpdateEntity(newCherryPickRequest);
          result[0] = newCherryPickRequest;
        }
      });
      return viewCherryPickRequest(result[0]);
    }
    catch (DataAccessException e) {
      showMessage("databaseOperationFailed", e.getMessage());
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
  }

//  public String createNewCherryPickRequestForSelectedAssayPlates()
//  {
//    // TODO
//    return REDISPLAY_PAGE_ACTION_RESULT;
//  }

  @UIControllerMethod
  public String viewLeadScreener()
  {
    // TODO
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String setEditMode()
  {
    _isEditMode = true;
    try {
      _dao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          _dao.reattachEntity(_cherryPickRequest); // checks if up-to-date
          //_dao.need(cherryPickRequest, "");
        }
      });
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    catch (ConcurrencyFailureException e) {
      showMessage("concurrentModificationConflict");
    }
    catch (DataAccessException e) {
      showMessage("databaseOperationFailed", e.getMessage());
    }
    // on error, reload
    return viewCherryPickRequest(_cherryPickRequest);
  }

  @UIControllerMethod
  public String cancelEdit() {
    // edits are discarded (and edit mode is canceled) by virtue of controller reloading the screen entity from the database
    return viewCherryPickRequest(_cherryPickRequest);
  }

  @UIControllerMethod
  public String save() {
    _isEditMode = false;

    try {
      _dao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          _dao.reattachEntity(_cherryPickRequest);
          _cherryPickRequest.setRequestedBy(_requestedBy.getSelection());
          _cherryPickRequest.setVolumeApprovedBy(_volumeApprovedBy.getSelection());
          _cherryPickRequest.clearRequestedEmptyColumnsOnAssayPlate();
          _cherryPickRequest.addRequestedEmptyColumnsOnAssayPlate(_emptyColumnsOnAssayPlate.getSelections());
          _cherryPickRequest.addRequestedEmptyRowsOnAssayPlate(_emptyRowsOnAssayPlate.getSelections());
        }
      });
    }
    catch (ConcurrencyFailureException e) {
      showMessage("concurrentModificationConflict");
      viewCherryPickRequest(_cherryPickRequest);
    }
    catch (DataAccessException e) {
      showMessage("databaseOperationFailed", e.getMessage());
      viewCherryPickRequest(_cherryPickRequest);
    }
    return VIEW_CHERRY_PICK_REQUEST_ACTION_RESULT;
  }


  // protected methods

  protected ScreensaverUserRole getEditableAdminRole()
  {
    return EDITING_ROLE;
  }

  // private methods

  private DataModel buildScreenerCherryPicksDataModel()
  {
    List<ScreenerCherryPick> screenerCherryPicks = new ArrayList<ScreenerCherryPick>(_cherryPickRequest.getScreenerCherryPicks());
    Collections.sort(screenerCherryPicks,
                     getScreenerCherryPicksDataTable().getSortManager().getSortColumnComparator());
    return new ListDataModel(screenerCherryPicks);
  }

  private DataModel buildLabCherryPicksDataModel()
  {
    List<LabCherryPick> labCherryPicks = new ArrayList<LabCherryPick>(_cherryPickRequest.getLabCherryPicks().size());
    for (LabCherryPick cherryPick : _cherryPickRequest.getLabCherryPicks()) {
      if (_showFailedLabCherryPicks || !cherryPick.isFailed()) {
        labCherryPicks.add(cherryPick);
      }
    }
    Collections.sort(labCherryPicks,
                     getLabCherryPicksDataTable().getSortManager().getSortColumnComparator());
    return new ListDataModel(labCherryPicks);
  }

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
        if (assayPlate.isFailed() || assayPlate.isPlated() || assayPlate.isCancelled()) {
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
        else if (assayPlate.isPlated() || assayPlate.isFailed() || assayPlate.isCancelled()) {
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

  private String doAddCherryPicksForWells(final CherryPickRequest _cherryPickRequest,
                                          final Set<WellKey> cherryPickWellKeys,
                                          final boolean arePoolWells)
  {
    assert !arePoolWells || _cherryPickRequest.getScreen().getScreenType().equals(ScreenType.RNAI);

    try {
      _dao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          CherryPickRequest cherryPickRequest = (CherryPickRequest) _dao.reattachEntity(_cherryPickRequest);
          if (cherryPickRequest.isAllocated()) {
            throw new BusinessRuleViolationException("cherry picks cannot be added to a cherry pick request that has already been allocated");
          }

          for (WellKey wellKey : cherryPickWellKeys) {
            Well well = _dao.findEntityById(Well.class,
                                            wellKey.toString(),
                                            true,
                                            // needed by libraryPoolToDuplexWellMapper, below
                                            "silencingReagents.wells.silencingReagents.gene",
                                            "silencingReagents.gene");
            if (well == null) {
              throw new InvalidCherryPickWellException(wellKey, "no such well");
            }
            else {
              ScreenerCherryPick screenerCherryPick = cherryPickRequest.createScreenerCherryPick(well);
              if (!arePoolWells) {
                cherryPickRequest.createLabCherryPick(screenerCherryPick, well);
              }
            }
          }

          if (arePoolWells) {
            _libraryPoolToDuplexWellMapper.createDuplexLabCherryPicksforPoolScreenerCherryPicks((RNAiCherryPickRequest) cherryPickRequest);
          }


        }
      });

      doWarnOnInvalidPoolWellScreenerCherryPicks(_cherryPickRequest);
      doWarnOnDuplicateScreenerCherryPicks(_cherryPickRequest);


    }
    catch (DataAccessException e) {
      showMessage("databaseOperationFailed", e.getMessage());
    }
    catch (InvalidCherryPickWellException e) {
      showMessage("cherryPicks.invalidWell", e.getWellKey());
    }
    catch (BusinessRuleViolationException e) {
      showMessage("businessError", e.getMessage());
    }
    return viewCherryPickRequest(_cherryPickRequest);
  }

  private void doRecordLiquidTransferForAssayPlates(final Set<CherryPickAssayPlate> selectedAssayPlates,
                                                    final ScreensaverUser performedByIn,
                                                    final Date dateOfLiquidTransfer,
                                                    final String comments,
                                                    final boolean success)
  {
    _dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        CherryPickRequest cherryPickRequest = (CherryPickRequest) _dao.reattachEntity(_cherryPickRequest);
        ScreensaverUser performedBy = _dao.reloadEntity(performedByIn);
        CherryPickLiquidTransfer liquidTransfer = new CherryPickLiquidTransfer(performedBy,
                                                                               new Date(),
                                                                               dateOfLiquidTransfer,
                                                                               cherryPickRequest,
                                                                               success ? CherryPickLiquidTransferStatus.SUCCESSFUL : CherryPickLiquidTransferStatus.FAILED);
        liquidTransfer.setComments(comments);
        for (CherryPickAssayPlate assayPlate : selectedAssayPlates) {
          if (!assayPlate.getCherryPickRequest().equals(cherryPickRequest)) {
            throw new IllegalArgumentException("all assay plates must be from the specified cherry pick request");
          }
          if (assayPlate.isPlated()) {
            throw new BusinessRuleViolationException("cannot record successful liquid transfer more than once for a cherry pick assay plate");
          }
          assayPlate.setCherryPickLiquidTransfer(liquidTransfer);
        }
        _dao.saveOrUpdateEntity(liquidTransfer); // necessary?
      }
    });
  }

  private void doWarnOnInvalidPoolWellScreenerCherryPicks(CherryPickRequest _cherryPickRequest)
  {
    int n = 0;
    for (ScreenerCherryPick screenerCherryPick : _cherryPickRequest.getScreenerCherryPicks()) {
      if (screenerCherryPick.getLabCherryPicks().size() == 0) {
        ++n;
      }
    }
    if (n > 0) {
      showMessage("cherryPicks.poolWellsWithoutDuplexWells", Integer.toString(n));
    }
  }

  private void doWarnOnDuplicateScreenerCherryPicks(final CherryPickRequest _cherryPickRequest)
  {
    Map<WellKey,Number> duplicateScreenerCherryPickWellKeysMap = _cherryPickRequestDao.findDuplicateCherryPicksForScreen(_cherryPickRequest.getScreen());
    Set<WellKey> duplicateScreenerCherryPickWellKeys = duplicateScreenerCherryPickWellKeysMap.keySet();
    Set<WellKey> ourScreenerCherryPickWellsKeys = new HashSet<WellKey>();
    for (ScreenerCherryPick screenerCherryPick : _cherryPickRequest.getScreenerCherryPicks()) {
      ourScreenerCherryPickWellsKeys.add(screenerCherryPick.getScreenedWell().getWellKey());
    }
    duplicateScreenerCherryPickWellKeys.retainAll(ourScreenerCherryPickWellsKeys);
    if (duplicateScreenerCherryPickWellKeysMap.size() > 0) {
      String duplicateWellsList = StringUtils.makeListString(duplicateScreenerCherryPickWellKeys, ", ");
      showMessage("cherryPicks.duplicateCherryPicksInScreen", _cherryPickRequest.getScreen().getScreenNumber(), duplicateWellsList);
    }
  }

  private String doViewCherryPickRequestWellVolumes(boolean forUnfulfilledOnly)
  {
    Collection<WellCopyVolume> wellCopyVolumes =
      _librariesDao.findWellCopyVolumes(_cherryPickRequest, forUnfulfilledOnly);
    _wellCopyVolumesBrowser.setContents(wellCopyVolumes);
    // use the special wellVolumeSearchResult page that the cherryPickAdmin role
    // can access (the normal wellVolumeSearchResult is restricted to the
    // librariesAdmin role)
    return VIEW_CHERRY_PICK_REQUEST_WELL_VOLUMES;
  }
}
