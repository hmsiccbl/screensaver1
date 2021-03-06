// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.cherrypickrequests;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.harvard.med.iccbl.screensaver.policy.cherrypicks.RNAiCherryPickRequestAllowancePolicy;
import edu.harvard.med.iccbl.screensaver.policy.cherrypicks.SmallMoleculeCherryPickRequestAllowancePolicy;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.db.datafetcher.DataFetcherUtil;
import edu.harvard.med.screensaver.db.datafetcher.EntityDataFetcher;
import edu.harvard.med.screensaver.db.datafetcher.NoOpDataFetcher;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.io.libraries.PlateWellListParser;
import edu.harvard.med.screensaver.io.libraries.PlateWellListParserResult;
import edu.harvard.med.screensaver.model.Entity;
import edu.harvard.med.screensaver.model.activities.Activity;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickAssayPlate;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickLiquidTransfer;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickLiquidTransferStatus;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.LabCherryPick;
import edu.harvard.med.screensaver.model.cherrypicks.ScreenerCherryPick;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.screens.CherryPickScreening;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.service.cherrypicks.CherryPickRequestAllocator;
import edu.harvard.med.screensaver.service.cherrypicks.CherryPickRequestCherryPicksAdder;
import edu.harvard.med.screensaver.service.cherrypicks.CherryPickRequestPlateMapFilesBuilder;
import edu.harvard.med.screensaver.service.cherrypicks.CherryPickRequestPlateMapper;
import edu.harvard.med.screensaver.service.screens.ScreeningDuplicator;
import edu.harvard.med.screensaver.ui.activities.ActivityViewer;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TableColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.BooleanEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.IntegerEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.IntegerSetEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.TextEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.TextSetEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.model.DataTableModel;
import edu.harvard.med.screensaver.ui.arch.datatable.model.InMemoryDataModel;
import edu.harvard.med.screensaver.ui.arch.datatable.model.InMemoryEntityDataModel;
import edu.harvard.med.screensaver.ui.arch.searchresults.EntityBasedEntitySearchResults;
import edu.harvard.med.screensaver.ui.arch.searchresults.EntitySearchResults;
import edu.harvard.med.screensaver.ui.arch.util.JSFUtils;
import edu.harvard.med.screensaver.ui.arch.view.SearchResultContextEntityViewerBackingBean;
import edu.harvard.med.screensaver.ui.arch.view.aspects.UICommand;
import edu.harvard.med.screensaver.ui.libraries.WellCopyVolumeSearchResults;

/**
 * Backing bean for Cherry Pick Request Viewer page.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class CherryPickRequestViewer extends SearchResultContextEntityViewerBackingBean<CherryPickRequest,CherryPickRequest>
{
  private static Logger log = Logger.getLogger(CherryPickRequestViewer.class);
  static final String RNAI_COLUMNS_GROUP = "RNAi";
  static final String SMALL_MOLECULE_COLUMNS_GROUP = "Small Molecule";

  protected static final Predicate<String> NOT_EMPTY = Predicates.and(Predicates.notNull(), Predicates.not(Predicates.equalTo("")));
  
  protected enum AssayPlateValidationType {
    LIQUID_TRANSFER,
    DOWNLOAD,
    DEALLOCATION,
    SCREENING,
    RECREATE_FAILED
  };



  private CherryPickRequestDetailViewer _cherryPickRequestDetailViewer;
  private LibrariesDAO _librariesDao;
  private WellCopyVolumeSearchResults _wellCopyVolumesBrowser;
  private ActivityViewer _activityViewer;
  private PlateWellListParser _plateWellListParser;
  private CherryPickRequestCherryPicksAdder _cherryPickRequestCherryPicksAdder;
  private CherryPickRequestAllocator _cherryPickRequestAllocator;
  private CherryPickRequestPlateMapper _cherryPickRequestPlateMapper;
  private CherryPickRequestPlateMapFilesBuilder _cherryPickRequestPlateMapFilesBuilder;
  private ScreeningDuplicator _screeningDuplicator;
  private SmallMoleculeCherryPickRequestAllowancePolicy _smallMoleculeCherryPickRequestAllowancePolicy;
  private RNAiCherryPickRequestAllowancePolicy _rnaiCherryPickRequestAllowancePolicy;

  private String _cherryPicksInput;

  private EntitySearchResults<ScreenerCherryPick,ScreenerCherryPick,Integer> _screenerCherryPicksSearchResult;
  private LabCherryPicksSearchResult _labCherryPicksSearchResult;

  private DataModel _assayPlatesDataModel;
  private boolean _selectAllAssayPlates = true;
  private int _maxQueryInputItems;


  /**
   * @motivation for CGLIB2
   */
  protected CherryPickRequestViewer() {}

  public CherryPickRequestViewer(CherryPickRequestViewer thisProxy,
                                 CherryPickRequestDetailViewer cherryPickRequestDetailViewer,
                                 EntitySearchResults<CherryPickRequest,CherryPickRequest,?> cherryPickRequestsBrowser,
                                 LabCherryPicksSearchResult labCherryPicksSearchResult,
                                 ActivityViewer activityViewer,
                                 GenericEntityDAO dao,
                                 LibrariesDAO librariesDao,
                                 WellCopyVolumeSearchResults wellCopyVolumesBrowser,
                                 CherryPickRequestCherryPicksAdder cherryPickRequestCherryPicksAdder,
                                 CherryPickRequestAllocator cherryPickRequestAllocator,
                                 CherryPickRequestPlateMapper cherryPickRequestPlateMapper,
                                 CherryPickRequestPlateMapFilesBuilder cherryPickRequestPlateMapFilesBuilder,
                                 ScreeningDuplicator screeningDuplicator)
  {
    super(thisProxy,
          CherryPickRequest.class,
          BROWSE_CHERRY_PICK_REQUESTS,
          VIEW_CHERRY_PICK_REQUEST,
          dao,
          cherryPickRequestsBrowser);
    _cherryPickRequestDetailViewer = cherryPickRequestDetailViewer;
    _labCherryPicksSearchResult = labCherryPicksSearchResult;
    _activityViewer = activityViewer;
    _librariesDao = librariesDao;
    _wellCopyVolumesBrowser = wellCopyVolumesBrowser;
    _cherryPickRequestCherryPicksAdder = cherryPickRequestCherryPicksAdder;
    _cherryPickRequestAllocator = cherryPickRequestAllocator;
    _cherryPickRequestPlateMapper = cherryPickRequestPlateMapper;
    _cherryPickRequestPlateMapFilesBuilder = cherryPickRequestPlateMapFilesBuilder;
    _screeningDuplicator = screeningDuplicator;
    getIsPanelCollapsedMap().put("screenerCherryPicks", true);
    getIsPanelCollapsedMap().put("labCherryPicks", true);
    getIsPanelCollapsedMap().put("cherryPickPlates", true);
    _labCherryPicksSearchResult.searchForCherryPickRequest(null);
    _labCherryPicksSearchResult.setNestedIn(this);
  }

  protected void buildScreenerCherryPickSearchResult()
  {
    _screenerCherryPicksSearchResult = new EntityBasedEntitySearchResults<ScreenerCherryPick,Integer>() {
      @Override
      public void searchAll()
      {
        initialize();
      }
      
      @Override
      public List<? extends TableColumn<ScreenerCherryPick,?>> buildColumns()
      {
        List<TableColumn<ScreenerCherryPick,?>> screenerCherryPicksTableColumns = buildScreenerCherryPicksTableColumns();
        _screenerCherryPicksSearchResult.getColumnManager().addAllCompoundSorts(buildScreenerCherryPicksTableCompoundSorts(screenerCherryPicksTableColumns));
        return screenerCherryPicksTableColumns;
      }
    };
    _screenerCherryPicksSearchResult.setCurrentScreensaverUser(getCurrentScreensaverUser());
    _screenerCherryPicksSearchResult.setMessages(getMessages());
    _screenerCherryPicksSearchResult.setApplicationProperties(getApplicationProperties());
    _screenerCherryPicksSearchResult.searchAll();
  }

  protected List<TableColumn<ScreenerCherryPick,?>> buildScreenerCherryPicksTableColumns()
  {
    List<TableColumn<ScreenerCherryPick,?>> screenerCherryPicksTableColumns = Lists.newArrayList();
    screenerCherryPicksTableColumns.add(new TextEntityColumn<ScreenerCherryPick>(
      ScreenerCherryPick.screenedWell.to(Well.library).toProperty("shortName"),
      "Library Name", "The library name of the well that was originally screened", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(ScreenerCherryPick scp) { return scp.getScreenedWell().getLibrary().getShortName(); }
    });

    screenerCherryPicksTableColumns.add(new IntegerEntityColumn<ScreenerCherryPick>(
      ScreenerCherryPick.screenedWell.toProperty("plateNumber"),
      "Library Plate", "The library plate number of the well that was originally screened", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(ScreenerCherryPick scp) { return scp.getScreenedWell().getPlateNumber(); }
    });
    screenerCherryPicksTableColumns.add(new TextEntityColumn<ScreenerCherryPick>(
      ScreenerCherryPick.screenedWell.toProperty("wellName"),
      "Screened Well", "The name of the well that was originally screened", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(ScreenerCherryPick scp) { return scp.getScreenedWell().getWellName(); }
    });
    screenerCherryPicksTableColumns.add(new IntegerEntityColumn<ScreenerCherryPick>(
      ScreenerCherryPick.labCherryPicks,
      "Source wells", "The number of wells to be cherry picked for the screened well", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(ScreenerCherryPick scp) { return scp.getLabCherryPicks().size(); }
    });

    screenerCherryPicksTableColumns.add(new ScreenerCherryPickReagentEntityColumn<Reagent,String>(
      Reagent.class, 
      new TextEntityColumn<Reagent>(
        Reagent.vendorName,
        "Vendor Name", 
        "The vendor of the reagent in this well", 
        TableColumn.UNGROUPED) {
          @Override
          public String getCellValue(Reagent r) 
          { 
            return r.getVendorId().getVendorName(); 
          }
      }));

    screenerCherryPicksTableColumns.add(new ScreenerCherryPickReagentEntityColumn<Reagent,String>(
      Reagent.class, 
      new TextEntityColumn<Reagent>(
        Reagent.vendorIdentifier,
        "Reagent ID", 
        "The vendor-assigned identifier for the reagent in this well",
        TableColumn.UNGROUPED) {
          @Override
          public String getCellValue(Reagent r) 
          { 
            return r.getVendorId().getVendorIdentifier(); 
          }
      }));

    screenerCherryPicksTableColumns.add(new BooleanEntityColumn<ScreenerCherryPick>(
      ScreenerCherryPick.screenedWell.toProperty("deprecated"),
      "Deprecated", "Whether the cherry picked well has been deprecated (and should not have been cherry picked)", TableColumn.UNGROUPED) {
      @Override
      public Boolean getCellValue(ScreenerCherryPick scp)
      {
        return scp.getScreenedWell().isDeprecated();
      }
    });
    screenerCherryPicksTableColumns.get(screenerCherryPicksTableColumns.size() - 1).setVisible(false);
    screenerCherryPicksTableColumns.add(new TextEntityColumn<ScreenerCherryPick>(
      ScreenerCherryPick.screenedWell.to(Well.deprecationActivity).toProperty("comments"),
      "Deprecation Reason", "Why the cherry picked well has been deprecated (and should not have been cherry picked)", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(ScreenerCherryPick scp)
      {
        return scp.getScreenedWell().isDeprecated() ? scp.getScreenedWell().getDeprecationActivity().getComments() : null;
      }
    });
    screenerCherryPicksTableColumns.get(screenerCherryPicksTableColumns.size() - 1).setVisible(false);
    screenerCherryPicksTableColumns.add(new ScreenerCherryPickReagentEntityColumn<SilencingReagent,Set<String>>(
      SilencingReagent.class,
      new TextSetEntityColumn<SilencingReagent>(
        SilencingReagent.facilityGenes.toProperty("geneName"),
        "Gene", 
        "The name of the gene targeted by the screened well", 
        RNAI_COLUMNS_GROUP) {
        @Override
        public Set<String> getCellValue(SilencingReagent r)
        {
          return r.getFacilityGenes() == null ? null : Sets.newLinkedHashSet(Iterables.filter(Iterables.transform(Iterables.filter(r.getFacilityGenes(), Predicates.notNull()), Gene.ToGeneName), NOT_EMPTY));
        }
      }));
    screenerCherryPicksTableColumns.add(new ScreenerCherryPickReagentEntityColumn<SilencingReagent,Set<Integer>>(
      SilencingReagent.class,
      new IntegerSetEntityColumn<SilencingReagent>(
        SilencingReagent.facilityGenes.toProperty("entrezgeneId"),
        "Entrez ID", 
        "The Entrez ID of the gene targeted by the screened well", 
        RNAI_COLUMNS_GROUP) {
        @Override
        public Set<Integer> getCellValue(SilencingReagent r)
        {
          return r.getFacilityGenes() == null ? null : Sets.newLinkedHashSet(Iterables.filter(Iterables.transform(Iterables.filter(r.getFacilityGenes(), Predicates.notNull()), Gene.ToEntrezgeneId), Predicates.notNull()));
        }
      }));
    screenerCherryPicksTableColumns.add(new ScreenerCherryPickReagentEntityColumn<SilencingReagent,Set<String>>(SilencingReagent.class,
                                                                                                           new TextSetEntityColumn<SilencingReagent>(SilencingReagent.facilityGenes.to(Gene.entrezgeneSymbols),
        "Entrez Symbol", 
        "The Entrez symbol of the gene targeted by the screened well", 
        RNAI_COLUMNS_GROUP) {
        @Override
        public Set<String> getCellValue(SilencingReagent r)
        {
          return r.getFacilityGenes() == null ? null : Sets.newLinkedHashSet(Iterables.filter(Iterables.concat(Iterables.filter(Iterables.transform(Iterables.filter(r.getFacilityGenes(), Predicates.notNull()), Gene.ToEntrezgeneSymbols), Predicates.notNull())), NOT_EMPTY));
        }
      }));
    screenerCherryPicksTableColumns.add(new ScreenerCherryPickReagentEntityColumn<SilencingReagent,Set<String>>(SilencingReagent.class,
                                                                                                           new TextSetEntityColumn<SilencingReagent>(SilencingReagent.facilityGenes.to(Gene.genbankAccessionNumbers),
        "Genbank AccNo", 
        "The Genbank accession number of the gene targeted by the screened well",
        RNAI_COLUMNS_GROUP) {
        @Override
        public Set<String> getCellValue(SilencingReagent r)
        {
          return r.getFacilityGenes() == null ? null : Sets.newLinkedHashSet(Iterables.filter(Iterables.concat(Iterables.filter(Iterables.transform(Iterables.filter(r.getFacilityGenes(), Predicates.notNull()), Gene.ToGenbankAccessionNumbers), Predicates.notNull())), NOT_EMPTY));
        }
      }));
  
    return screenerCherryPicksTableColumns;
  }

  protected List<List<TableColumn<ScreenerCherryPick,?>>> buildScreenerCherryPicksTableCompoundSorts(List<TableColumn<ScreenerCherryPick,?>> _screenerCherryPicksTableColumns)
  {
    List<List<TableColumn<ScreenerCherryPick,?>>> _screenerCherryPicksTableCompoundSorts = Lists.newArrayList();
    // define compound sorts
    _screenerCherryPicksTableCompoundSorts.add(new ArrayList<TableColumn<ScreenerCherryPick,?>>());
    _screenerCherryPicksTableCompoundSorts.get(0).add(_screenerCherryPicksTableColumns.get(0));
    _screenerCherryPicksTableCompoundSorts.get(0).add(_screenerCherryPicksTableColumns.get(1));

    _screenerCherryPicksTableCompoundSorts.add(new ArrayList<TableColumn<ScreenerCherryPick,?>>());
    _screenerCherryPicksTableCompoundSorts.get(1).add(_screenerCherryPicksTableColumns.get(1));
    _screenerCherryPicksTableCompoundSorts.get(1).add(_screenerCherryPicksTableColumns.get(0));

    _screenerCherryPicksTableCompoundSorts.add(new ArrayList<TableColumn<ScreenerCherryPick,?>>());
    _screenerCherryPicksTableCompoundSorts.get(2).add(_screenerCherryPicksTableColumns.get(2));
    _screenerCherryPicksTableCompoundSorts.get(2).add(_screenerCherryPicksTableColumns.get(0));
    _screenerCherryPicksTableCompoundSorts.get(2).add(_screenerCherryPicksTableColumns.get(1));

    return _screenerCherryPicksTableCompoundSorts;
  }

  protected <T extends Entity<Integer>> DataTableModel<T> buildCherryPicksDataTableModel(final Class<T> clazz,
                                                                                       final CherryPickRequest cpr)
  {
    if (cpr == null) {
      return new InMemoryDataModel<T>(new NoOpDataFetcher<T,Integer,PropertyPath<T>>());
    }
    else {
      return new InMemoryEntityDataModel<T,Integer,T>(new EntityDataFetcher<T,Integer>(clazz, getDao()) {
        @Override
        public void addDomainRestrictions(HqlBuilder hql)
        {
          DataFetcherUtil.addDomainRestrictions(hql, RelationshipPath.from(clazz).to("cherryPickRequest"), cpr, getRootAlias());
        }
      });
    }
  }
  
  @Override
  public void initializeEntity(CherryPickRequest cherryPickRequest)
  {
    // note: eager fetching screenedWell and latestReleasedReagent, since Hibernate otherwise loads them individually, and slowly
    getDao().needReadOnly(cherryPickRequest, CherryPickRequest.screenerCherryPicks.to(ScreenerCherryPick.screenedWell).to(Well.latestReleasedReagent));
    getDao().needReadOnly(cherryPickRequest, CherryPickRequest.labCherryPicks.to(LabCherryPick.wellVolumeAdjustments));
  }
  
  @Override
  public void initializeViewer(CherryPickRequest cherryPickRequest)
  {
    _cherryPickRequestDetailViewer.setEntity(cherryPickRequest);
    _labCherryPicksSearchResult.searchForCherryPickRequest(cherryPickRequest);
    _cherryPicksInput = null;
    getScreenerCherryPicksSearchResult().initialize(buildCherryPicksDataTableModel(ScreenerCherryPick.class, cherryPickRequest));
    _screenerCherryPicksSearchResult.getColumnManager().setVisibilityOfColumnsInGroup(RNAI_COLUMNS_GROUP, false); // cherryPickRequest.getScreen().getScreenType() == ScreenType.RNAI
    _screenerCherryPicksSearchResult.getColumnManager().setVisibilityOfColumnsInGroup(SMALL_MOLECULE_COLUMNS_GROUP, false);// cherryPickRequest.getScreen().getScreenType() == ScreenType.SMALL_MOLECULE 

    _assayPlatesDataModel = null;
    
    // set "Cherry Pick Plates" panel to initially expanded, if cherry pick plates have been created
    boolean hasCherryPickPlates = cherryPickRequest.getCherryPickAssayPlates().size() > 0;
    getIsPanelCollapsedMap().put("cherryPickPlates", !hasCherryPickPlates);
  }
  
  public String getCherryPicksInput()
  {
    return _cherryPicksInput;
  }

  public void setCherryPicksInput(String cherryPicksInput)
  {
    _cherryPicksInput = cherryPicksInput;
  }

  public EntitySearchResults<ScreenerCherryPick,ScreenerCherryPick,Integer> getScreenerCherryPicksSearchResult()
  {
    if (_screenerCherryPicksSearchResult == null) {
      buildScreenerCherryPickSearchResult();
    }
    return _screenerCherryPicksSearchResult;
  }

  public LabCherryPicksSearchResult getLabCherryPicksSearchResult()
  {
    return _labCherryPicksSearchResult;
  }

  public int getActiveCherryPickPlatesCount()
  {
    return getEntity().getActiveCherryPickAssayPlates().size();
  }

  public int getCompletedCherryPickPlatesCount()
  {
    return getEntity().getCompletedCherryPickAssayPlates().size();
  }

  public boolean isRnaiScreen()
  {
    return getEntity().getScreen().getScreenType().equals(ScreenType.RNAI);
  }

  public DataModel getAssayPlatesDataModel()
  {
    if (_assayPlatesDataModel == null) {
      getDao().doInTransaction(new DAOTransaction() {
        @Override
        public void runTransaction()
        {
          CherryPickRequest cpr = getDao().reloadEntity(getEntity(), true);
          getDao().needReadOnly(cpr, CherryPickRequest.cherryPickAssayPlates.to(CherryPickAssayPlate.cherryPickLiquidTransfer).to(CherryPickLiquidTransfer.performedBy));
          getDao().needReadOnly(cpr, CherryPickRequest.cherryPickAssayPlates.to(CherryPickAssayPlate.cherryPickScreenings).to(Activity.performedBy));
          getDao().needReadOnly(cpr, CherryPickRequest.cherryPickAssayPlates.to(CherryPickAssayPlate.cherryPickRequest).to(CherryPickRequest.requestedBy));
          getDao().needReadOnly(cpr, CherryPickRequest.requestedBy);
          // TODO: this is definitely a bug, but the following call will populate the cpr.requestedBy since it is not working otherwise, outside of this tx; 
          // an analysis of the output SQL indicates that the cpr.requestedBy fields are being fetched into the returned resultset, so not clear why this is causing a lazy init ex
          // Longer term: dispense with this issue by building the assay plate model in memory?  see: [#3527] Lazy init exception when viewing Cherry Pick Request
          log.info("======================= CPR.requested by: " + cpr.getRequestedBy());
          getDao().needReadOnly(cpr, CherryPickRequest.screen);
          // HACK: following reln is (only) needed by validateSelectedAssayPlates() in LIQUID_TRANSFER case 
          getDao().needReadOnly(cpr, CherryPickRequest.cherryPickAssayPlates.to(CherryPickAssayPlate.labCherryPicks).to(LabCherryPick.screenerCherryPick).to(ScreenerCherryPick.screenedWell).to(Well.latestReleasedReagent));
          List<AssayPlateRow> rows = new ArrayList<AssayPlateRow>(); 
          for (CherryPickAssayPlate assayPlate : cpr.getCherryPickAssayPlates()) {
            AssayPlateRow row = new AssayPlateRow(assayPlate);
            row.setSelected(_selectAllAssayPlates);
            rows.add(row);
          }
          _assayPlatesDataModel = new ListDataModel(rows);
        }
      });
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

  // JSF application methods

  @UICommand
  public String addCherryPicksForWells()
  {
    return doAddCherryPicksForPoolWells(false);
  }

  @UICommand
  public String addCherryPicksForPoolWells()
  {
    return doAddCherryPicksForPoolWells(true);
  }
  
  protected String doAddCherryPicksForPoolWells(boolean deconvoluteToDuplexWells)
  {
    PlateWellListParserResult result = PlateWellListParser.parseWellsFromPlateWellList(_cherryPicksInput);
    if (result.getErrors().size() > 0) {
      showMessage("cherryPicks.parseError");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }

    _cherryPickRequestCherryPicksAdder.addCherryPicksForWells(getEntity(),
                                                              result.getParsedWellKeys(),
                                                              deconvoluteToDuplexWells);
    _cherryPickRequestDetailViewer.showAdminWarnings();
    return getThisProxy().reload();
  }

  @UICommand
  public String viewCherryPickRequestWellVolumes()
  {
    return doViewCherryPickRequestWellVolumes(false);
  }

  @UICommand
  public String viewCherryPickRequestWellVolumesForUnfulfilled()
  {
    return doViewCherryPickRequestWellVolumes(true);
  }

	private boolean isSomeUnfulfilled() {
		for (LabCherryPick lcp : getEntity().getLabCherryPicks()) {
			if (lcp.isUnfulfilled())
				return true;
		}
		return false;
	}

	// For [#3380] Add prompt to manually assign unreserved source copies before
	// mapping in the Cherry Pick Request workflow
	public String getUnfulfilledPrompt() {
		return isSomeUnfulfilled() ? "javascript: return confirm('"
				+ getMessage("cherryPicks.someCherryPicksUnfulfillable.overridePrompt") + "');" : "";
	}

  @UICommand
  public String allocateCherryPicks()
  {
    if (getEntity().getTransferVolumePerWellApproved() == null) {
      showMessage("cherryPicks.approvedCherryPickVolumeRequired");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }

    Set<LabCherryPick> unfulfillable = _cherryPickRequestAllocator.allocate(getEntity());
    if (unfulfillable.size() == getEntity().getLabCherryPicks().size()) {
      showMessage("cherryPicks.allCherryPicksUnfulfillable");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    if (unfulfillable.size() > 0) {
      showMessage("cherryPicks.someCherryPicksUnfulfillable");
    }
    return getThisProxy().reload();
  }

  @UICommand
  public String deallocateCherryPicks()
  {
    _cherryPickRequestAllocator.deallocate(getEntity());
    return getThisProxy().reload();
  }

  @UICommand
  @Transactional
  public String deallocateCherryPicksByPlate()
  {
    if (!validateSelectedAssayPlates(AssayPlateValidationType.DEALLOCATION)) {
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    CherryPickLiquidTransfer cplt = _screeningDuplicator.addCherryPickLiquidTransfer(getEntity().getScreen(), 
                                                                                     getScreensaverUser(),
                                                                                     (AdministratorUser) getScreensaverUser(),
                                                                                     CherryPickLiquidTransferStatus.CANCELED);
    for (CherryPickAssayPlate plate : getSelectedAssayPlates()) {
      cplt.addCherryPickAssayPlate(getDao().reloadEntity(plate));
    }
    _cherryPickRequestAllocator.deallocateAssayPlates(cplt.getCherryPickAssayPlates());
    getDao().clear(); // detach new Activity, as it should only be persisted if user invokes "save" command 
    return _activityViewer.editNewEntity(cplt);
  }

  @UICommand
  public String plateMapCherryPicks()
  {
    if (getEntity().getAssayPlateType() == null) {
      showMessage("cherryPicks.assayPlateTypeRequired");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }

    _cherryPickRequestPlateMapper.generatePlateMapping(getEntity());
    return getThisProxy().reload();
  }

  @SuppressWarnings("unchecked")
  @UICommand
  public String selectAllAssayPlates()
  {
    List<AssayPlateRow> data = (List<AssayPlateRow>) getAssayPlatesDataModel().getWrappedData();
    for (AssayPlateRow row : data) {
      row.setSelected(_selectAllAssayPlates);
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UICommand
  @Transactional
  public String downloadPlateMappingFilesForSelectedAssayPlates() throws IOException
  {
    if (!validateSelectedAssayPlates(AssayPlateValidationType.DOWNLOAD)) {
      return REDISPLAY_PAGE_ACTION_RESULT;
    }

    final Set<CherryPickAssayPlate> plateNames = getSelectedAssayPlates();
    if (plateNames.size() == 0) {
      showMessage("cherryPicks.noPlatesSelected", "assayPlatesTable");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }

    CherryPickRequest cherryPickRequest = getDao().reloadEntity(getEntity());
    if (cherryPickRequest != null) {
      InputStream zipStream = _cherryPickRequestPlateMapFilesBuilder.buildZip(cherryPickRequest, plateNames);
      JSFUtils.handleUserDownloadRequest(getFacesContext(),
                                         zipStream,
                                         "CherryPickRequest" + cherryPickRequest.getEntityId() + "_PlateMapFiles.zip",
                                         "application/zip");
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UICommand
  @Transactional
  public String recordSuccessfulCreationOfAssayPlates()
  {
    if (!validateSelectedAssayPlates(AssayPlateValidationType.LIQUID_TRANSFER)) {
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    CherryPickLiquidTransfer cplt = _screeningDuplicator.addCherryPickLiquidTransfer(getEntity().getScreen(), 
                                                                                     getScreensaverUser(),
                                                                                     (AdministratorUser) getScreensaverUser(),
                                                                                     CherryPickLiquidTransferStatus.SUCCESSFUL);
    for (CherryPickAssayPlate plate : getSelectedAssayPlates()) {
      cplt.addCherryPickAssayPlate(plate);
    }
    getDao().clear(); // detach new Activity, as it should only be persisted if user invokes "save" command 
    return _activityViewer.editNewEntity(cplt);
  }

  @UICommand
  @Transactional
  public String recordScreeningOfAssayPlates()
  {
    if (!validateSelectedAssayPlates(AssayPlateValidationType.SCREENING)) {
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    CherryPickScreening screening = _screeningDuplicator.addCherryPickScreening(getEntity().getScreen(), 
                                                                                getEntity().getRequestedBy(),
                                                                                (AdministratorUser) getScreensaverUser(),
                                                                                getEntity());
    for (CherryPickAssayPlate plate : getSelectedAssayPlates()) {
      screening.addCherryPickAssayPlateScreened(getDao().reloadEntity(plate));
    }
    getDao().clear(); // detach new Activity, as it should only be persisted if user invokes "save" command 
    return _activityViewer.editNewEntity(screening);
  }

  @UICommand
  @Transactional
  public String recordFailedCreationOfAssayPlates()
  {
    if (!validateSelectedAssayPlates(AssayPlateValidationType.LIQUID_TRANSFER)) {
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    CherryPickLiquidTransfer cplt = _screeningDuplicator.addCherryPickLiquidTransfer(getEntity().getScreen(), 
                                                                                     getScreensaverUser(),
                                                                                     (AdministratorUser) getScreensaverUser(),
                                                                                     CherryPickLiquidTransferStatus.FAILED);
    for (CherryPickAssayPlate plate : getSelectedAssayPlates()) {
      cplt.addCherryPickAssayPlate(plate);
    }
    getDao().clear(); // detach new Activity, as it should only be persisted if user invokes "save" command 
    return _activityViewer.editNewEntity(cplt);
  }

  @UICommand
  public String createNewAssayPlatesForFailed()
  {
    if (!validateSelectedAssayPlates(AssayPlateValidationType.RECREATE_FAILED)) {
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    
    Set<LabCherryPick> unfulfillable = _cherryPickRequestAllocator.reallocateAssayPlates(getSelectedAssayPlates());
    if (!unfulfillable.isEmpty()) {
      showMessage("cherryPicks.someCherryPicksUnfulfillable");
    }
    getDao().flush(); // HACK: only necessary because we're called from LabActivityViewer.postEditAction(), which is already in a txn 
    return reload();
  }

  @UICommand
  @Transactional
  public String createNewCherryPickRequestForUnfulfilledCherryPicks()
  {
    CherryPickRequest cherryPickRequest = getDao().reloadEntity(getEntity());
    // Note reload of cherry pick may have created a proxy for the current user already: reload detached user into session
    AdministratorUser currentUser = getDao().reloadEntity((AdministratorUser) getScreensaverUser());
    CherryPickRequest newCherryPickRequest = cherryPickRequest.getScreen().createCherryPickRequest(currentUser);
    newCherryPickRequest.setComments("Created for unfulfilled cherry picks in Cherry Pick Request " +
                                     cherryPickRequest.getCherryPickRequestNumber());
    // TODO: this might be better done in a copy constructor
    newCherryPickRequest.setTransferVolumePerWellApproved(cherryPickRequest.getTransferVolumePerWellApproved());
    newCherryPickRequest.setTransferVolumePerWellRequested(cherryPickRequest.getTransferVolumePerWellRequested());
    newCherryPickRequest.setVolumeApprovedBy(cherryPickRequest.getVolumeApprovedBy());
    newCherryPickRequest.setDateVolumeApproved(cherryPickRequest.getDateVolumeApproved());
    newCherryPickRequest.setDateRequested(new LocalDate());
    newCherryPickRequest.setRandomizedAssayPlateLayout(cherryPickRequest.isRandomizedAssayPlateLayout());
    newCherryPickRequest.addEmptyWellsOnAssayPlate(cherryPickRequest.getEmptyWellsOnAssayPlate());
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
          newScreenerCherryPick.createLabCherryPick(labCherryPick.getSourceWell());
        }
      }
    }
    getDao().saveOrUpdateEntity(newCherryPickRequest);
    getDao().flush();
    return getThisProxy().viewEntity(newCherryPickRequest);
  }

  @SuppressWarnings("unchecked")
  protected Set<CherryPickAssayPlate> getSelectedAssayPlates()
  {
    Set<CherryPickAssayPlate> selectedAssayPlates = new HashSet<CherryPickAssayPlate>();
    List<AssayPlateRow> data = (List<AssayPlateRow>) getAssayPlatesDataModel().getWrappedData();
    for (AssayPlateRow row : data) {
      if (row.isSelected()) {
        selectedAssayPlates.add(row.getData());
      }
    }
    return selectedAssayPlates;
  }

  @SuppressWarnings("unchecked")
  protected boolean validateSelectedAssayPlates(AssayPlateValidationType validationType)
  {
    Set<CherryPickAssayPlate> selectedAssayPlates = getSelectedAssayPlates();
    if (selectedAssayPlates.size() == 0) {
      showMessage("cherryPicks.noPlatesSelected", "assayPlatesTable");
      return false;
    }

    boolean adjustSelection = false;
    for (Iterator<CherryPickAssayPlate> iter = selectedAssayPlates.iterator(); iter.hasNext();) {
      CherryPickAssayPlate assayPlate = iter.next();
      switch (validationType) {
      case DEALLOCATION: {
        if (assayPlate.isFailed() || assayPlate.isPlated() || assayPlate.isCancelled()) {
          showMessageForComponent("cherryPicks.deallocateActiveMappedPlatesOnly",
                                  "assayPlatesTable",
                                  assayPlate.getName());
          iter.remove();
          adjustSelection = true;
        }
        break;
      }
      case DOWNLOAD: {
        if (assayPlate.isFailed()) {
          showMessageForComponent("cherryPicks.downloadActiveMappedPlatesOnly",
                                  "assayPlatesTable",
                                  assayPlate.getName());
          iter.remove();
          adjustSelection = true;
        }
        break;
      }
      case LIQUID_TRANSFER: {
        if (assayPlate.getLabCherryPicks().size() == 0) {
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
        break;
      }
        case RECREATE_FAILED: {
          if (!assayPlate.isFailed() || !getEntity().getActiveCherryPickAssayPlates().contains(assayPlate)) {
            iter.remove();
            showMessageForComponent("cherryPicks.assayPlateNotFailedOrAlreadyRecreated",
                                    "assayPlatesTable",
                                    assayPlate.getName());
            adjustSelection = true;
          }

          break;
        }
      case SCREENING: {
        if (!assayPlate.isPlated() && !assayPlate.isPlatedAndScreened()) {
          iter.remove();
          showMessageForComponent("cherryPicks.assayPlateNotScreenable",
                                  "assayPlatesTable",
                                  assayPlate.getName());
          adjustSelection = true;
        }
        break;
      }
      }
    }

    if (adjustSelection) {
      List<AssayPlateRow> data = (List<AssayPlateRow>) getAssayPlatesDataModel().getWrappedData();
      for (AssayPlateRow row : data) {
        if (row.isSelected() && !selectedAssayPlates.contains(row.getData())) {
          row.setSelected(false);
        }
      }
    }

    return !adjustSelection;
  }
  
  protected String doViewCherryPickRequestWellVolumes(boolean forUnfulfilledOnly)
  {
    _wellCopyVolumesBrowser.searchWellsForCherryPickRequest(getEntity(), forUnfulfilledOnly);
    return BROWSE_WELL_VOLUMES;
  }

  public boolean isSourcePlateReloadRequired()
  {
    return _cherryPickRequestPlateMapper.getAssayPlatesRequiringSourcePlateReload(getEntity()).size() > 0;
  }
  
}
