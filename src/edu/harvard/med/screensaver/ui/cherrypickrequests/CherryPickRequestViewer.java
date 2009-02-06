// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.cherrypickrequests;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.ArrayDataModel;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import edu.harvard.med.iccbl.screensaver.policy.cherrypicks.CompoundCherryPickRequestAllowancePolicy;
import edu.harvard.med.iccbl.screensaver.policy.cherrypicks.RNAiCherryPickRequestAllowancePolicy;
import edu.harvard.med.screensaver.db.CherryPickRequestDAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.db.datafetcher.DataFetcher;
import edu.harvard.med.screensaver.db.datafetcher.NoOpDataFetcher;
import edu.harvard.med.screensaver.db.datafetcher.ParentedEntityDataFetcher;
import edu.harvard.med.screensaver.io.cherrypicks.CherryPickRequestExporter;
import edu.harvard.med.screensaver.io.libraries.PlateWellListParser;
import edu.harvard.med.screensaver.io.libraries.PlateWellListParserResult;
import edu.harvard.med.screensaver.io.workbook2.Workbook;
import edu.harvard.med.screensaver.io.workbook2.Workbook2Utils;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.PropertyPath;
import edu.harvard.med.screensaver.model.RelationshipPath;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.VolumeUnit;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickAssayPlate;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickLiquidTransfer;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickLiquidTransferStatus;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.CompoundCherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.InvalidCherryPickWellException;
import edu.harvard.med.screensaver.model.cherrypicks.LabCherryPick;
import edu.harvard.med.screensaver.model.cherrypicks.LegacyCherryPickAssayPlate;
import edu.harvard.med.screensaver.model.cherrypicks.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.ScreenerCherryPick;
import edu.harvard.med.screensaver.model.cherrypicks.LabCherryPick.LabCherryPickStatus;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryScreeningStatus;
import edu.harvard.med.screensaver.model.libraries.PlateType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellName;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.policy.CherryPickRequestAllowancePolicy;
import edu.harvard.med.screensaver.service.cherrypicks.CherryPickRequestAllocator;
import edu.harvard.med.screensaver.service.cherrypicks.CherryPickRequestPlateMapFilesBuilder;
import edu.harvard.med.screensaver.service.cherrypicks.CherryPickRequestPlateMapper;
import edu.harvard.med.screensaver.service.cherrypicks.CherryPickRequestPlateStatusUpdater;
import edu.harvard.med.screensaver.service.libraries.rnai.LibraryPoolToDuplexWellMapper;
import edu.harvard.med.screensaver.ui.AbstractEditableBackingBean;
import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.ui.libraries.WellCopyVolumeSearchResults;
import edu.harvard.med.screensaver.ui.screens.ScreenViewer;
import edu.harvard.med.screensaver.ui.searchresults.EntitySearchResults;
import edu.harvard.med.screensaver.ui.table.Criterion;
import edu.harvard.med.screensaver.ui.table.Criterion.Operator;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.BooleanEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.EntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.EnumEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.IntegerEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.TextEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.VocabularyEntityColumn;
import edu.harvard.med.screensaver.ui.util.EditableViewer;
import edu.harvard.med.screensaver.ui.util.JSFUtils;
import edu.harvard.med.screensaver.ui.util.ScreensaverUserComparator;
import edu.harvard.med.screensaver.ui.util.UISelectOneBean;
import edu.harvard.med.screensaver.ui.util.UISelectOneEntityBean;
import edu.harvard.med.screensaver.util.DevelopmentException;
import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Join;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

/**
 * Backing bean for Cherry Pick Request Viewer page.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class CherryPickRequestViewer extends AbstractEditableBackingBean implements EditableViewer
{
  // static members

  private static Logger log = Logger.getLogger(CherryPickRequestViewer.class);

  private static final String VALIDATE_SELECTED_PLATES_FOR_LIQUID_TRANSFER = "for_liquid_transfer";
  private static final String VALIDATE_SELECTED_PLATES_FOR_DOWNLOAD = "for_download";
  private static final String VALIDATE_SELECTED_PLATES_FOR_DEALLOCATION = "for_deallocaton";


  private static final String RNAI_COLUMNS_GROUP = "RNAi";
  private static final String SMALL_MOLECULE_COLUMNS_GROUP = "Small Molecule";


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
  private CherryPickRequestPlateStatusUpdater _cherryPickRequestPlateStatusUpdater;
  private LibraryPoolToDuplexWellMapper _libraryPoolToDuplexWellMapper;
  private CherryPickRequestExporter _cherryPickRequestExporter;
  private CompoundCherryPickRequestAllowancePolicy _compoundCherryPickRequestAllowancePolicy;
  private RNAiCherryPickRequestAllowancePolicy _rnaiCherryPickRequestAllowancePolicy;

  private CherryPickRequest _cherryPickRequest;
  private boolean _isEditMode = false;
  private Map<String,Boolean> _isPanelCollapsedMap;
  private String _cherryPicksInput;
  private UISelectOneBean<PlateType> _assayPlateType;
  private UISelectOneEntityBean<ScreeningRoomUser> _requestedBy;
  private UISelectOneEntityBean<AdministratorUser> _volumeApprovedBy;

  private EntitySearchResults<ScreenerCherryPick,Integer> _screenerCherryPicksSearchResult;
  private EntitySearchResults<LabCherryPick,Integer> _labCherryPicksSearchResult;

  private DataModel _assayPlatesColumnModel;
  private DataModel _assayPlatesDataModel;
  private boolean _selectAllAssayPlates = true;
  private boolean _showFailedAssayPlates;
  private boolean _showFailedLabCherryPicks;

  private UISelectOneEntityBean<ScreensaverUser> _liquidTransferPerformedBy;
  private LocalDate _dateOfLiquidTransfer = new LocalDate();
  private String _liquidTransferComments;

  private EmptyWellsConverter _emptyWellsConverter;

  private UISelectOneBean<VolumeUnit> _transferVolumePerWellRequestedType;
  private String _transferVolumePerWellRequestedValue;

  private UISelectOneBean<VolumeUnit> _transferVolumePerWellApprovedType;
  private String _transferVolumePerWellApprovedValue;


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
                                 CherryPickRequestPlateStatusUpdater cherryPickRequestPlateStatusUpdater,
                                 LibraryPoolToDuplexWellMapper libraryPoolToDuplexWellMapper,
                                 CherryPickRequestExporter cherryPickRequestExporter,
                                 CompoundCherryPickRequestAllowancePolicy compoundCherryPickRequestAllowancePolicy,
                                 RNAiCherryPickRequestAllowancePolicy rnaiCherryPickRequestAllowancePolicy)
  {
    super(ScreensaverUserRole.CHERRY_PICK_REQUESTS_ADMIN);
    _dao = dao;
    _cherryPickRequestDao = cherryPickRequestDao;
    _librariesDao = librariesDao;
    _screenViewer = screenViewer;
    _wellCopyVolumesBrowser = wellCopyVolumesBrowser;
    _plateWellListParser = plateWellListParser;
    _cherryPickRequestAllocator = cherryPickRequestAllocator;
    _cherryPickRequestPlateMapper = cherryPickRequestPlateMapper;
    _cherryPickRequestPlateMapFilesBuilder = cherryPickRequestPlateMapFilesBuilder;
    _cherryPickRequestPlateStatusUpdater = cherryPickRequestPlateStatusUpdater;
    _libraryPoolToDuplexWellMapper = libraryPoolToDuplexWellMapper;
    _cherryPickRequestExporter = cherryPickRequestExporter;
    _compoundCherryPickRequestAllowancePolicy = compoundCherryPickRequestAllowancePolicy;
    _rnaiCherryPickRequestAllowancePolicy = rnaiCherryPickRequestAllowancePolicy;

    _isPanelCollapsedMap = new HashMap<String,Boolean>();
    _isPanelCollapsedMap.put("screenSummary", true);
    _isPanelCollapsedMap.put("cherryPickRequest", false);
    _isPanelCollapsedMap.put("screenerCherryPicks", true);
    _isPanelCollapsedMap.put("labCherryPicks", true);
    _isPanelCollapsedMap.put("cherryPickPlates", true);
  }

  private void buildLabCherryPickSearchResult()
  {
    _labCherryPicksSearchResult = new EntitySearchResults<LabCherryPick,Integer>() {
      @Override
      protected List<? extends TableColumn<LabCherryPick,?>> buildColumns() {
        List<TableColumn<LabCherryPick,?>> labCherryPicksTableColumns = buildLabCherryPicksTableColumns();
        _labCherryPicksSearchResult.getColumnManager().addAllCompoundSorts(buildLabCherryPicksTableCompoundSorts(labCherryPicksTableColumns));
        return labCherryPicksTableColumns;
      }

      @Override
      protected void setEntityToView(LabCherryPick entity) {}
    };

    _labCherryPicksSearchResult.initialize(buildCherryPicksDataFetcher(LabCherryPick.class));
    _labCherryPicksSearchResult.setCurrentScreensaverUser(getCurrentScreensaverUser());
    _labCherryPicksSearchResult.setMessages(getMessages());
  }

  private void buildScreenerCherryPickSearchResult()
  {
    _screenerCherryPicksSearchResult = new EntitySearchResults<ScreenerCherryPick,Integer>() {
      @Override
      protected List<? extends TableColumn<ScreenerCherryPick,?>> buildColumns() {
        List<EntityColumn<ScreenerCherryPick,?>> screenerCherryPicksTableColumns = buildScreenerCherryPicksTableColumns();
        _screenerCherryPicksSearchResult.getColumnManager().addAllCompoundSorts(buildScreenerCherryPicksTableCompoundSorts(screenerCherryPicksTableColumns));
        return screenerCherryPicksTableColumns;
      }

      @Override
      protected void setEntityToView(ScreenerCherryPick entity) {}
    };
    _screenerCherryPicksSearchResult.initialize(buildCherryPicksDataFetcher(ScreenerCherryPick.class));
    _screenerCherryPicksSearchResult.setCurrentScreensaverUser(getCurrentScreensaverUser());
    _screenerCherryPicksSearchResult.setMessages(getMessages());
  }

  private List<EntityColumn<ScreenerCherryPick,?>> buildScreenerCherryPicksTableColumns()
  {
    List<EntityColumn<ScreenerCherryPick,?>> _screenerCherryPicksTableColumns = new ArrayList<EntityColumn<ScreenerCherryPick,?>>();
    _screenerCherryPicksTableColumns.add(new IntegerEntityColumn<ScreenerCherryPick>(
      new PropertyPath<ScreenerCherryPick>(ScreenerCherryPick.class, "screenedWell", "plateNumber"),
      "Library Plate", "The library plate number of the well that was originally screened", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(ScreenerCherryPick scp) { return scp.getScreenedWell().getPlateNumber(); }
    });
    _screenerCherryPicksTableColumns.add(new TextEntityColumn<ScreenerCherryPick>(
      new PropertyPath<ScreenerCherryPick>(ScreenerCherryPick.class, "screenedWell", "wellName"),
      "Screened Well", "The name of the well that was originally screened", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(ScreenerCherryPick scp) { return scp.getScreenedWell().getWellName(); }
    });
    _screenerCherryPicksTableColumns.add(new IntegerEntityColumn<ScreenerCherryPick>(
      new RelationshipPath<ScreenerCherryPick>(ScreenerCherryPick.class, "labCherryPicks"),
      "Source Wells", "The number of wells to be cherry picked for the screened well", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(ScreenerCherryPick scp) { return scp.getLabCherryPicks().size(); }
    });
    _screenerCherryPicksTableColumns.add(new TextEntityColumn<ScreenerCherryPick>(
      new PropertyPath<ScreenerCherryPick>(ScreenerCherryPick.class, "screenedWell", "simpleVendorIdentifier"),
      "Vendor ID", "The vendor ID of the screened well", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(ScreenerCherryPick scp) { return scp.getScreenedWell().getSimpleVendorIdentifier(); }
    });
    _screenerCherryPicksTableColumns.add(new BooleanEntityColumn<ScreenerCherryPick>(
      new PropertyPath<ScreenerCherryPick>(ScreenerCherryPick.class, "screenedWell", "deprecated"),
      "Deprecated", "Whether the cherry picked well has been deprecated (and should not have been cherry picked)", TableColumn.UNGROUPED) {
      @Override
      public Boolean getCellValue(ScreenerCherryPick scp)
      {
        return scp.getScreenedWell().isDeprecated();
      }
    });
    _screenerCherryPicksTableColumns.get(_screenerCherryPicksTableColumns.size() - 1).setVisible(false);
    _screenerCherryPicksTableColumns.add(new TextEntityColumn<ScreenerCherryPick>(
      new PropertyPath<ScreenerCherryPick>(ScreenerCherryPick.class, "screenedWell.deprecationActivity", "comments"),
      "Deprecation Reason", "Why the cherry picked well has been deprecated (and should not have been cherry picked)", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(ScreenerCherryPick scp)
      {
        return scp.getScreenedWell().isDeprecated() ? scp.getScreenedWell().getDeprecationActivity().getComments() : null;
      }
    });
    _screenerCherryPicksTableColumns.get(_screenerCherryPicksTableColumns.size() - 1).setVisible(false);
    _screenerCherryPicksTableColumns.add(new TextEntityColumn<ScreenerCherryPick>(
      new PropertyPath<ScreenerCherryPick>(ScreenerCherryPick.class, "screenedWell", "iccbNumber"),
      "ICCB Number", "The identifier assigned by ICCB-L to the contents of this well", SMALL_MOLECULE_COLUMNS_GROUP) {
      @Override
      public String getCellValue(ScreenerCherryPick scp)
      {
        return scp.getScreenedWell().getIccbNumber();
      }
    });
    _screenerCherryPicksTableColumns.add(new TextEntityColumn<ScreenerCherryPick>(
      new PropertyPath<ScreenerCherryPick>(ScreenerCherryPick.class, "screenedWell.gene", "geneName"),
      "Gene", "The name of the gene targeted by the screened well", RNAI_COLUMNS_GROUP) {
      @Override
      public String getCellValue(ScreenerCherryPick scp)
      {
        Gene gene = scp.getScreenedWell().getGene();
        return gene == null ? null : gene.getGeneName();
      }
    });
    _screenerCherryPicksTableColumns.add(new IntegerEntityColumn<ScreenerCherryPick>(
      new PropertyPath<ScreenerCherryPick>(ScreenerCherryPick.class, "screenedWell.gene", "entrezgeneId"),
      "Entrez ID", "The Entrez ID of the gene targeted by the screened well", RNAI_COLUMNS_GROUP) {
      @Override
      public Integer getCellValue(ScreenerCherryPick scp)
      {
        Gene gene = scp.getScreenedWell().getGene();
        return gene == null ? null : gene.getEntrezgeneId();
      }
    });
    _screenerCherryPicksTableColumns.add(new TextEntityColumn<ScreenerCherryPick>(
      new PropertyPath<ScreenerCherryPick>(ScreenerCherryPick.class, "screenedWell.gene", "entrezgeneSymbol"),
      "Entrez Symbol", "The Entrez symbol of the gene targeted by the screened well", RNAI_COLUMNS_GROUP) {
      @Override
      public String getCellValue(ScreenerCherryPick scp)
      {
        Gene gene = scp.getScreenedWell().getGene();
        return gene == null ? null : gene.getEntrezgeneSymbol();
      }
    });
    _screenerCherryPicksTableColumns.add(new TextEntityColumn<ScreenerCherryPick>(
      new PropertyPath<ScreenerCherryPick>(ScreenerCherryPick.class, "screenedWell", "genbankAccessionNumber"),
      "Genbank AccNo", "The Genbank accession number of the gene targeted by the screened well", RNAI_COLUMNS_GROUP) {
      @Override
      public String getCellValue(ScreenerCherryPick scp)
      {
        return scp.getScreenedWell().getGenbankAccessionNumber();
      }
    });

    return _screenerCherryPicksTableColumns;
  }

  private List<List<TableColumn<ScreenerCherryPick,?>>> buildScreenerCherryPicksTableCompoundSorts(List<EntityColumn<ScreenerCherryPick,?>> _screenerCherryPicksTableColumns)
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

  private List<TableColumn<LabCherryPick,?>> buildLabCherryPicksTableColumns()
  {
    List<TableColumn<LabCherryPick,?>> labCherryPicksTableColumns = Lists.newArrayList();
    labCherryPicksTableColumns.add(new EnumEntityColumn<LabCherryPick,LabCherryPickStatus>(
      new RelationshipPath<LabCherryPick>(LabCherryPick.class, "wellVolumeAdjustments"),
      "Status", "Status",
      TableColumn.UNGROUPED, LabCherryPickStatus.values()) {
      @Override
      public LabCherryPickStatus getCellValue(LabCherryPick lcp)
      {
        return lcp.getStatus();
      }
    });
    ((EntityColumn) labCherryPicksTableColumns.get(0)).addRelationshipPath(new RelationshipPath<LabCherryPick>(LabCherryPick.class,
      "assayPlate.cherryPickLiquidTransfer"));
    labCherryPicksTableColumns.add(new IntegerEntityColumn<LabCherryPick>(
      new PropertyPath<LabCherryPick>(LabCherryPick.class, "sourceWell", "plateNumber"),
      "Library Plate", "The library plate number of the cherry picked well", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(LabCherryPick lcp) { return lcp.getSourceWell().getPlateNumber(); }
    });
    labCherryPicksTableColumns.add(new TextEntityColumn<LabCherryPick>(
      new PropertyPath<LabCherryPick>(LabCherryPick.class, "sourceWell", "wellName"),
      "Source Well", "The name of the cherry picked well", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(LabCherryPick lcp) { return lcp.getSourceWell().getWellName(); }
    });
    labCherryPicksTableColumns.add(new TextEntityColumn<LabCherryPick>(
      new RelationshipPath<LabCherryPick>(LabCherryPick.class, "wellVolumeAdjustments.copy"),
      "Source Copy", "The library plate copy of the cherry picked well", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(LabCherryPick lcp) { return lcp.getSourceCopy() != null ? lcp.getSourceCopy().getName() : ""; }
    });
    labCherryPicksTableColumns.add(new TextEntityColumn<LabCherryPick>(
      new PropertyPath<LabCherryPick>(LabCherryPick.class, "sourceWell", "simpleVendorIdentifier"),
      "Vendor ID", "The Vendor ID of the of the cherry picked well", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(LabCherryPick lcp) { return lcp.getSourceWell().getSimpleVendorIdentifier(); }
    });
    labCherryPicksTableColumns.add(new TextEntityColumn<LabCherryPick>(
      new PropertyPath<LabCherryPick>(LabCherryPick.class, "sourceWell", "iccbNumber"),
      "ICCB Number", "The identifier assigned by ICCB-L to the contents of this well", SMALL_MOLECULE_COLUMNS_GROUP) {
      @Override
      public String getCellValue(LabCherryPick lcp)
      {
        return lcp.getSourceWell().getIccbNumber();
      }
    });
    labCherryPicksTableColumns.add(new TextEntityColumn<LabCherryPick>(
      new PropertyPath<LabCherryPick>(LabCherryPick.class, "sourceWell.gene", "geneName"),
      "Gene", "The name of the gene targeted by the cherry picked well", RNAI_COLUMNS_GROUP) {
      @Override
      public String getCellValue(LabCherryPick lcp)
      {
        Gene gene = lcp.getSourceWell().getGene();
        return gene == null ? null : gene.getGeneName();
      }
    });
    labCherryPicksTableColumns.add(new IntegerEntityColumn<LabCherryPick>(
      new PropertyPath<LabCherryPick>(LabCherryPick.class, "sourceWell.gene", "entrezgeneId"),

      "Entrez ID", "The Entrez ID of the gene targeted by the cherry picked well", RNAI_COLUMNS_GROUP) {
      @Override
      public Integer getCellValue(LabCherryPick lcp)
      {
        Gene gene = lcp.getSourceWell().getGene();
        return gene == null ? null : gene.getEntrezgeneId();
      }
    });
    labCherryPicksTableColumns.add(new TextEntityColumn<LabCherryPick>(
      new PropertyPath<LabCherryPick>(LabCherryPick.class, "sourceWell.gene", "entrezgeneSymbol"),
      "Entrez Symbol", "The Entrez symbol of the gene targeted by the cherry picked well", RNAI_COLUMNS_GROUP) {
      @Override
      public String getCellValue(LabCherryPick lcp)
      {
        Gene gene = lcp.getSourceWell().getGene();
        return gene == null ? null : gene.getEntrezgeneSymbol();
      }
    });
    labCherryPicksTableColumns.add(new TextEntityColumn<LabCherryPick>(
      new PropertyPath<LabCherryPick>(LabCherryPick.class, "sourceWell", "genbankAccessionNumber"),
      "Genbank AccNo", "The Genbank accession number of the gene targeted by the cherry picked well", RNAI_COLUMNS_GROUP) {
      @Override
      public String getCellValue(LabCherryPick lcp)
      {
        return lcp.getSourceWell().getGenbankAccessionNumber();
      }
    });
    labCherryPicksTableColumns.add(new BooleanEntityColumn<LabCherryPick>(
      new PropertyPath<LabCherryPick>(LabCherryPick.class, "sourceWell", "deprecated"),
      "Deprecated", "Whether the cherry picked well has been deprecated (and should not have been cherry picked)", TableColumn.UNGROUPED) {
      @Override
      public Boolean getCellValue(LabCherryPick lcp)
      {
        return lcp.getSourceWell().isDeprecated();
      }
    });
    labCherryPicksTableColumns.get(labCherryPicksTableColumns.size() - 1).setVisible(false);
    labCherryPicksTableColumns.add(new TextEntityColumn<LabCherryPick>(
      new PropertyPath<LabCherryPick>(LabCherryPick.class, "sourceWell.deprecationActivity", "comments"),
      "Deprecation Reason", "Why the cherry picked well has been deprecated (and should not have been cherry picked)", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(LabCherryPick lcp)
      {
        return lcp.getSourceWell().isDeprecated() ? lcp.getSourceWell().getDeprecationActivity().getComments() : null;
      }
    });
    labCherryPicksTableColumns.get(labCherryPicksTableColumns.size() - 1).setVisible(false);
    labCherryPicksTableColumns.add(new IntegerEntityColumn<LabCherryPick>(
      new PropertyPath<LabCherryPick>(LabCherryPick.class, "assayPlate", "plateOrdinal"),
      "Cherry Pick Plate #", "The cherry pick plate number that this cherry pick has been mapped to", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(LabCherryPick lcp) { return lcp.isMapped() ? new Integer(lcp.getAssayPlate().getPlateOrdinal() + 1) : null; }
    });
    labCherryPicksTableColumns.add(new IntegerEntityColumn<LabCherryPick>(
      new PropertyPath<LabCherryPick>(LabCherryPick.class, "assayPlate", "attemptOrdinal"),
      "Attempt #", "The attempt number of the cherry pick plate that this cherry pick has been mapped to", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(LabCherryPick lcp) { return lcp.isMapped() ? new Integer(lcp.getAssayPlate().getAttemptOrdinal() + 1) : null; }
    });
    labCherryPicksTableColumns.add(new TextEntityColumn<LabCherryPick>(
      new RelationshipPath<LabCherryPick>(LabCherryPick.class, "assayPlate"),
      "Destination Well", "The name of the well that this cherry pick has been mapped to", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(LabCherryPick lcp) { return lcp.isMapped() ? lcp.getAssayPlateWellName().toString() : null; }
    });

    return labCherryPicksTableColumns;
  }

  private List<List<TableColumn<LabCherryPick,?>>> buildLabCherryPicksTableCompoundSorts(List<TableColumn<LabCherryPick,?>> labCherryPicksTableColumns)
  {
    List<List<TableColumn<LabCherryPick,?>>> labCherryPicksTableCompoundSorts = Lists.newArrayList();

    // define compound sorts
    labCherryPicksTableCompoundSorts.add(new ArrayList<TableColumn<LabCherryPick,?>>());
    labCherryPicksTableCompoundSorts.get(0).add(labCherryPicksTableColumns.get(0));
    labCherryPicksTableCompoundSorts.get(0).add(labCherryPicksTableColumns.get(1));
    labCherryPicksTableCompoundSorts.get(0).add(labCherryPicksTableColumns.get(2));

    labCherryPicksTableCompoundSorts.add(new ArrayList<TableColumn<LabCherryPick,?>>());
    labCherryPicksTableCompoundSorts.get(1).add(labCherryPicksTableColumns.get(1));
    labCherryPicksTableCompoundSorts.get(1).add(labCherryPicksTableColumns.get(2));

    labCherryPicksTableCompoundSorts.add(new ArrayList<TableColumn<LabCherryPick,?>>());
    labCherryPicksTableCompoundSorts.get(2).add(labCherryPicksTableColumns.get(2));
    labCherryPicksTableCompoundSorts.get(2).add(labCherryPicksTableColumns.get(1));

    labCherryPicksTableCompoundSorts.add(new ArrayList<TableColumn<LabCherryPick,?>>());
    labCherryPicksTableCompoundSorts.get(3).add(labCherryPicksTableColumns.get(3));
    labCherryPicksTableCompoundSorts.get(3).add(labCherryPicksTableColumns.get(1));
    labCherryPicksTableCompoundSorts.get(3).add(labCherryPicksTableColumns.get(2));

    labCherryPicksTableCompoundSorts.add(new ArrayList<TableColumn<LabCherryPick,?>>());
    labCherryPicksTableCompoundSorts.get(4).add(labCherryPicksTableColumns.get(4));
    labCherryPicksTableCompoundSorts.get(4).add(labCherryPicksTableColumns.get(1));
    labCherryPicksTableCompoundSorts.get(4).add(labCherryPicksTableColumns.get(2));

    labCherryPicksTableCompoundSorts.add(new ArrayList<TableColumn<LabCherryPick,?>>());
    labCherryPicksTableCompoundSorts.get(5).add(labCherryPicksTableColumns.get(5));
    labCherryPicksTableCompoundSorts.get(5).add(labCherryPicksTableColumns.get(1));
    labCherryPicksTableCompoundSorts.get(5).add(labCherryPicksTableColumns.get(2));

    labCherryPicksTableCompoundSorts.add(new ArrayList<TableColumn<LabCherryPick,?>>());
    labCherryPicksTableCompoundSorts.get(6).add(labCherryPicksTableColumns.get(6));
    labCherryPicksTableCompoundSorts.get(6).add(labCherryPicksTableColumns.get(1));
    labCherryPicksTableCompoundSorts.get(6).add(labCherryPicksTableColumns.get(2));

    labCherryPicksTableCompoundSorts.add(new ArrayList<TableColumn<LabCherryPick,?>>());
    labCherryPicksTableCompoundSorts.get(7).add(labCherryPicksTableColumns.get(7));
    labCherryPicksTableCompoundSorts.get(7).add(labCherryPicksTableColumns.get(1));
    labCherryPicksTableCompoundSorts.get(7).add(labCherryPicksTableColumns.get(2));

    labCherryPicksTableCompoundSorts.add(new ArrayList<TableColumn<LabCherryPick,?>>());
    labCherryPicksTableCompoundSorts.get(8).add(labCherryPicksTableColumns.get(8));
    labCherryPicksTableCompoundSorts.get(8).add(labCherryPicksTableColumns.get(1));
    labCherryPicksTableCompoundSorts.get(8).add(labCherryPicksTableColumns.get(2));

    labCherryPicksTableCompoundSorts.add(new ArrayList<TableColumn<LabCherryPick,?>>());
    labCherryPicksTableCompoundSorts.get(9).add(labCherryPicksTableColumns.get(9));
    labCherryPicksTableCompoundSorts.get(9).add(labCherryPicksTableColumns.get(10));
    labCherryPicksTableCompoundSorts.get(9).add(labCherryPicksTableColumns.get(11));

    labCherryPicksTableCompoundSorts.add(new ArrayList<TableColumn<LabCherryPick,?>>());
    labCherryPicksTableCompoundSorts.get(10).add(labCherryPicksTableColumns.get(10));
    labCherryPicksTableCompoundSorts.get(10).add(labCherryPicksTableColumns.get(9));
    labCherryPicksTableCompoundSorts.get(10).add(labCherryPicksTableColumns.get(11));

    labCherryPicksTableCompoundSorts.add(new ArrayList<TableColumn<LabCherryPick,?>>());
    labCherryPicksTableCompoundSorts.get(11).add(labCherryPicksTableColumns.get(11));
    labCherryPicksTableCompoundSorts.get(11).add(labCherryPicksTableColumns.get(9));
    labCherryPicksTableCompoundSorts.get(11).add(labCherryPicksTableColumns.get(10));

    return labCherryPicksTableCompoundSorts;
  }

  private <T extends AbstractEntity> DataFetcher<T,Integer,PropertyPath<T>> buildCherryPicksDataFetcher(Class<T> clazz)
  {
    if (_cherryPickRequest == null) {
      return new NoOpDataFetcher<T,Integer,PropertyPath<T>>();
    }
    else {
      return new ParentedEntityDataFetcher<T,Integer>(
        clazz,
        new RelationshipPath<T>(clazz, "cherryPickRequest"),
        _cherryPickRequest,
        _dao);
    }
  }

//  private RowsPerPageSelector buildRowsPerPageSelector(final int totalRowCount)
//  {
//    return new RowsPerPageSelector(Arrays.asList(10, 20, 50, 100, RowsPerPageSelector.SHOW_ALL_VALUE)) {
//      @Override
//      protected Integer getAllRowsValue() { return totalRowCount; }
//    };
//  }

  public void setCherryPickRequest(CherryPickRequest cherryPickRequest)
  {
    _cherryPickRequest = cherryPickRequest;

    _isEditMode = false;
    _cherryPicksInput = null;
    _transferVolumePerWellRequestedType = null;
    _transferVolumePerWellRequestedValue = null;
    _transferVolumePerWellApprovedType = null;
    _transferVolumePerWellApprovedValue = null;

    initializeCherryPicksTables();

    _assayPlateType = new UISelectOneBean<PlateType>(Arrays.asList(PlateType.values()), _cherryPickRequest.getAssayPlateType()) {
      @Override
      protected String makeLabel(PlateType plateType) { return plateType.getFullName(); }
    };

    _requestedBy = new UISelectOneEntityBean<ScreeningRoomUser>(_cherryPickRequest.getRequestedByCandidates(),
      _cherryPickRequest.getRequestedBy(),
      _dao) {
      @Override
      protected String makeLabel(ScreeningRoomUser u) { return u.getFullNameLastFirst(); }
    };

    SortedSet<AdministratorUser> candidateVolumeApprovers = new TreeSet<AdministratorUser>(ScreensaverUserComparator.getInstance());
    candidateVolumeApprovers.addAll(_dao.findAllEntitiesOfType(AdministratorUser.class)); // TODO: filter out all but CherryPickAdmins
    _volumeApprovedBy = new UISelectOneEntityBean<AdministratorUser>(candidateVolumeApprovers,
      _cherryPickRequest.getVolumeApprovedBy(),
      true,
      _dao) {
      @Override
      protected String makeLabel(AdministratorUser a) { return a.getFullNameLastFirst(); }
    };

    SortedSet<ScreensaverUser> candidatePreparers = new TreeSet<ScreensaverUser>(ScreensaverUserComparator.getInstance());
    candidatePreparers.addAll(_dao.findAllEntitiesOfType(AdministratorUser.class));
    _liquidTransferPerformedBy = new UISelectOneEntityBean<ScreensaverUser>(candidatePreparers,
      candidatePreparers.contains(getScreensaverUser()) ? getScreensaverUser() : candidatePreparers.first(),
                                                        _dao) {
      @Override
      protected String makeLabel(ScreensaverUser u) { return u.getFullNameLastFirst(); }
    };

    _emptyWellsConverter = new EmptyWellsConverter();
    _assayPlatesColumnModel = new ArrayDataModel(AssayPlateRow.ASSAY_PLATES_TABLE_COLUMNS);
    _assayPlatesDataModel = null;

    // set "Cherry Pick Plates" panel to initially expanded, if cherry pick plates have been created
    boolean hasCherryPickPlates = _cherryPickRequest.getCherryPickAssayPlates().size() > 0;
    _isPanelCollapsedMap.put("cherryPickPlates", !hasCherryPickPlates);
  }

  private void initializeCherryPicksTables()
  {
    if (_cherryPickRequest != null) {
      getScreenerCherryPicksSearchResult().getColumnManager().setVisibilityOfColumnsInGroup(RNAI_COLUMNS_GROUP, _cherryPickRequest.getScreen().getScreenType() == ScreenType.RNAI);
      getScreenerCherryPicksSearchResult().getColumnManager().setVisibilityOfColumnsInGroup(SMALL_MOLECULE_COLUMNS_GROUP, _cherryPickRequest.getScreen().getScreenType() == ScreenType.SMALL_MOLECULE);
      getLabCherryPicksSearchResult().getColumnManager().setVisibilityOfColumnsInGroup(RNAI_COLUMNS_GROUP, _cherryPickRequest.getScreen().getScreenType() == ScreenType.RNAI);
      getLabCherryPicksSearchResult().getColumnManager().setVisibilityOfColumnsInGroup(SMALL_MOLECULE_COLUMNS_GROUP, _cherryPickRequest.getScreen().getScreenType() == ScreenType.SMALL_MOLECULE);

      // initialize LCP search result to initially respect 'show failed LCPs' checkbox
      if (!_showFailedLabCherryPicks) {
        ((VocabularyEntityColumn) getLabCherryPicksSearchResult().getColumnManager().getColumn(0)).clearCriteria().addCriterion(new Criterion<LabCherryPickStatus>(Operator.NOT_EQUAL, LabCherryPickStatus.Failed));
      }
    }
    getScreenerCherryPicksSearchResult().initialize(buildCherryPicksDataFetcher(ScreenerCherryPick.class));
    getLabCherryPicksSearchResult().initialize(buildCherryPicksDataFetcher(LabCherryPick.class));
  }

  public AbstractEntity getEntity()
  {
    return getCherryPickRequest();
  }

  public CherryPickRequest getCherryPickRequest()
  {
    return _cherryPickRequest;
  }

  @UIControllerMethod
  public String viewCherryPickRequest()
  {
    Integer entityId = Integer.parseInt(getRequestParameter("entityId").toString());
    if (entityId == null) {
      throw new IllegalArgumentException("missing 'entityId' request parameter");
    }
    CherryPickRequest cpr = _dao.findEntityById(CherryPickRequest.class, entityId);
    if (cpr == null) {
      throw new IllegalArgumentException(CherryPickRequest.class.getSimpleName() + " " + entityId + " does not exist");
    }
    return viewCherryPickRequest(cpr);
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

          _dao.needReadOnly(cherryPickRequest.getScreen(), "labActivities");
          _dao.needReadOnly(cherryPickRequest.getScreen(), "cherryPickRequests");
          _dao.needReadOnly(cherryPickRequest, "emptyWellsOnAssayPlate");
//          if (cherryPickRequest.getScreen().getScreenType() == ScreenType.SMALL_MOLECULE) {
//            // for CompoundCherryPickRequest.getCherryPickAllowance()
//            _dao.needReadOnly(cherryPickRequest, "screenResult.wells");
//          }
          _eagerFetchCpltPerformedByHack(cherryPickRequest);
          _dao.needReadOnly(cherryPickRequest,
                            "cherryPickAssayPlates.cherryPickLiquidTransfer.performedBy");
          _dao.needReadOnly(cherryPickRequest,
                            "screenerCherryPicks.screenedWell.deprecationActivity",
                            "screenerCherryPicks.screenedWell.silencingReagents",
                            "screenerCherryPicks.screenedWell.compounds");
          _dao.needReadOnly(cherryPickRequest,
                            "labCherryPicks.assayPlate",
                            "labCherryPicks.sourceWell.silencingReagents",
                            "labCherryPicks.sourceWell.compounds",
                            "labCherryPicks.wellVolumeAdjustments.copy");
          _dao.needReadOnly(cherryPickRequest,
                            "cherryPickAssayPlates.labCherryPicks");
          if (cherryPickRequest instanceof RNAiCherryPickRequest) {
            _dao.needReadOnly(cherryPickRequest,
                              "RNAiCherryPickScreenings");
          }

          setCherryPickRequest(cherryPickRequest);
        }

        /**
         the following code:

         _dao.needReadOnly(cherryPickRequest, "cherryPickAssayPlates.cherryPickLiquidTransfer.performedBy");

         fails to load the performedBy user, leading to a "org.hibernate.LazyInitializationException: could not
         initialize proxy - the owning Session was closed" error when constructing the AssayPlatesDataModel. im
         reasonably certain this is due to a bug in Hibernate. turning on logging debug for:

         log4j.logger.org.hibernate.event=debug

         reveals that, even though the HQL generated by GenericDAOImpl specifies fetching the performedBy
         relationship, an attempt to resolve the performedBy in the session cache never occurs. Turning on:

         log4j.logger.org.hibernate.hql.ast=debug

         further reveals that there seems to be some silenced exception going on in the processing of the
         left join fetch against performedBy:

         12:04:09,858 DEBUG org.hibernate.hql.ast.HqlSqlWalker:320 - createFromJoinElement() : -- join tree --
         \-[JOIN_FRAGMENT] FromElement: 'screensaver_user screensave3_' FromElement{explicit,not a collection join,fetch join,fetch non-lazy properties,classAlias=x3,role=null,tableName=screensaver_user,tableAlias=screensave3_,origin=cherry_pick_liquid_transfer cherrypick2_,colums={cherrypick2_2_.performed_by_id ,className=edu.harvard.med.screensaver.model.users.ScreensaverUser}}

          12:04:09,859 DEBUG org.hibernate.hql.ast.tree.FromElement:266 - attempt to disable subclass-inclusions
          java.lang.Exception: stack-trace source
                  at org.hibernate.hql.ast.tree.FromElement.setIncludeSubclasses(FromElement.java:266)
                  at org.hibernate.hql.ast.HqlSqlWalker.beforeSelectClause(HqlSqlWalker.java:761)
                  at org.hibernate.hql.antlr.HqlSqlBaseWalker.selectClause(HqlSqlBaseWalker.java:1346)
                  at org.hibernate.hql.antlr.HqlSqlBaseWalker.query(HqlSqlBaseWalker.java:553)

         i am speculating here that some key code missed getting executed due to this exception getting thrown.
         i dont feel like debugging this HQL parsing problem, since our HQL here looks perfectly kosher:

         select distinct x from edu.harvard.med.screensaver.model.cherrypicks.RNAiCherryPickRequest x
           left join fetch x.cherryPickAssayPlates x1 left join fetch x1.cherryPickLiquidTransfer x2
           left join fetch x2.performedBy x3 where x.id = ?

         instead, i wrote up the following workaround that is reasonably close to the performance of the original:
         */
        private void _eagerFetchCpltPerformedByHack(CherryPickRequest cherryPickRequest)
        {
          _dao.needReadOnly(cherryPickRequest, "cherryPickAssayPlates.cherryPickLiquidTransfer");
          for (CherryPickAssayPlate plate : cherryPickRequest.getCherryPickAssayPlates()) {
            CherryPickLiquidTransfer transfer = plate.getCherryPickLiquidTransfer();
            if (transfer != null) {
              transfer.getPerformedBy().getFullNameLastFirst();
            }
          }
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

  @Override
  public String reload()
  {
    if (_cherryPickRequest == null || _cherryPickRequest.getEntityId() == null) {
      _cherryPickRequest = null;
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    return viewCherryPickRequest(_cherryPickRequest);
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

  public UISelectOneBean<PlateType> getAssayPlateType()
  {
    return _assayPlateType;
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
    if (_cherryPickRequest.getDateVolumeApproved() == null) {
      return null;
    }
    return DateFormat.getDateInstance(DateFormat.SHORT).format(_cherryPickRequest.getDateVolumeApproved());
  }

  public EntitySearchResults<ScreenerCherryPick,Integer> getScreenerCherryPicksSearchResult()
  {
    if (_screenerCherryPicksSearchResult == null) {
      buildScreenerCherryPickSearchResult();
    }
    return _screenerCherryPicksSearchResult;
  }

  public EntitySearchResults<LabCherryPick,Integer> getLabCherryPicksSearchResult()
  {
    if (_labCherryPicksSearchResult == null) {
      buildLabCherryPickSearchResult();
    }
    return _labCherryPicksSearchResult;
  }

  public UISelectOneEntityBean<ScreensaverUser> getLiquidTransferPerformedBy()
  {
    return _liquidTransferPerformedBy;
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

  public EmptyWellsConverter getEmptyWellsConverter()
  {
    return _emptyWellsConverter;
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


  public UISelectOneBean<VolumeUnit> getTransferVolumePerWellRequestedType()
  {
    try {
      if (_transferVolumePerWellRequestedType == null)
      {
        Volume v = _cherryPickRequest.getTransferVolumePerWellRequested();
        VolumeUnit unit = ( v == null ? null: v.getUnits());
        _transferVolumePerWellRequestedType =
          new UISelectOneBean<VolumeUnit>( Arrays.asList(VolumeUnit.DISPLAY_VALUES), unit )
          {
            @Override
            protected String makeLabel(VolumeUnit t)
            {
              return t.getValue();
            }
          };
      }
      return _transferVolumePerWellRequestedType;
    } catch (Exception e) {
      log.error("err: " + e);
      return null;
    }
  }

  /**
   * This method exists to grab the value portion of the Quantity stored
  */
  public String getTransferVolumePerWellRequestedValue()
  {
    if (_transferVolumePerWellRequestedValue == null)
    {
      if(_cherryPickRequest.getTransferVolumePerWellRequested() != null)
        _transferVolumePerWellRequestedValue =
           _cherryPickRequest.getTransferVolumePerWellRequested().getDisplayValue().toString();
      else
        _transferVolumePerWellRequestedValue = null;
    }
    return _transferVolumePerWellRequestedValue;
  }

  /**
   * This method exists to set the value portion of the Quantity stored
   * @see #save()
  */
  public void setTransferVolumePerWellRequestedValue( String value )
  {
    _transferVolumePerWellRequestedValue = value;
  }

  public UISelectOneBean<VolumeUnit> getTransferVolumePerWellApprovedType()
  {
    try {
      if (_transferVolumePerWellApprovedType == null)
      {
        Volume v = _cherryPickRequest.getTransferVolumePerWellApproved();
        VolumeUnit unit = ( v == null ? null: v.getUnits());
        _transferVolumePerWellApprovedType =
          new UISelectOneBean<VolumeUnit>( Arrays.asList(VolumeUnit.DISPLAY_VALUES), unit )
          {
            @Override
            protected String makeLabel(VolumeUnit t)
            {
              return t.getValue();
            }
          };
      }
      return _transferVolumePerWellApprovedType;
    } catch (Exception e) {
      log.error("err: " + e);
      return null;
    }
  }

  /**
   * This method exists to grab the value portion of the Quantity stored
  */
  public String getTransferVolumePerWellApprovedValue()
  {
    if (_transferVolumePerWellApprovedValue == null)
    {
      if(_cherryPickRequest.getTransferVolumePerWellApproved() != null)
        _transferVolumePerWellApprovedValue =
           _cherryPickRequest.getTransferVolumePerWellApproved().getDisplayValue().toString();
      else
        _transferVolumePerWellApprovedValue = null;
    }
    return _transferVolumePerWellApprovedValue;
  }

  /**
   * This method exists to set the value portion of the Quantity stored
   * @see #save()
  */
  public void setTransferVolumePerWellApprovedValue( String value )
  {
    _transferVolumePerWellApprovedValue = value;
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

  @SuppressWarnings("unchecked")
  public void setShowFailedLabCherryPicks(boolean showFailedLabCherryPicks)
  {
    if (showFailedLabCherryPicks != _showFailedLabCherryPicks) {
      VocabularyEntityColumn statusColumn = (VocabularyEntityColumn) getLabCherryPicksSearchResult().getColumnManager().getColumn(0);
      statusColumn.clearCriteria();
      if (!showFailedLabCherryPicks) {
        statusColumn.addCriterion(new Criterion<LabCherryPickStatus>(Operator.NOT_EQUAL, LabCherryPickStatus.Failed));
      }
      getLabCherryPicksSearchResult().refilter();
    }
    _showFailedLabCherryPicks = showFailedLabCherryPicks;

  }

  public LocalDate getDateOfLiquidTransfer()
  {
    return _dateOfLiquidTransfer;
  }

  public void setDateOfLiquidTransfer(LocalDate dateOfLiquidTransfer)
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


  // JSF listeners

  public void toggleShowFailedLabCherryPicks(ValueChangeEvent event)
  {
    setShowFailedLabCherryPicks((Boolean) event.getNewValue());
    // avoid having JSF set backing bean property with the submitted value
    ((UIInput) event.getComponent()).setLocalValueSet(false);
    // force regen of data model
    getLabCherryPicksSearchResult().refilter();
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
      _cherryPickRequestDao.deleteCherryPickRequest(_cherryPickRequest);
      return _screenViewer.viewScreen(_cherryPickRequest.getScreen());
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String deleteAllCherryPicks()
  {
    _cherryPickRequestDao.deleteAllCherryPicks(_cherryPickRequest);
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
    if (_cherryPickRequest.getTransferVolumePerWellApproved() == null) {
      showMessage("cherryPicks.approvedCherryPickVolumeRequired");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }

    Set<LabCherryPick> unfulfillable = _cherryPickRequestAllocator.allocate(_cherryPickRequest);
    if (unfulfillable.size() == _cherryPickRequest.getLabCherryPicks().size()) {
      showMessage("cherryPicks.allCherryPicksUnfulfillable");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    if (unfulfillable.size() > 0) {
      showMessage("cherryPicks.someCherryPicksUnfulfillable");
    }
    return viewCherryPickRequest(_cherryPickRequest);
  }

  @UIControllerMethod
  public String deallocateCherryPicks()
  {
    _cherryPickRequestAllocator.deallocate(_cherryPickRequest);
    return viewCherryPickRequest(_cherryPickRequest);
  }

  @UIControllerMethod
  @Transactional
  public String deallocateCherryPicksByPlate()
  {
    if (!validateSelectedAssayPlates(VALIDATE_SELECTED_PLATES_FOR_DEALLOCATION)) {
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    // note: must deallocate first, otherwise the 'canceled' status will prevent deallocation from being performed
    _cherryPickRequestAllocator.deallocateAssayPlates(getSelectedAssayPlates());
    _cherryPickRequestPlateStatusUpdater.updateAssayPlatesStatus(getSelectedAssayPlates(),
                                                                 getLiquidTransferPerformedBy().getSelection(),
                                                                 getDateOfLiquidTransfer(),
                                                                 getLiquidTransferComments(),
                                                                 CherryPickLiquidTransferStatus.CANCELED);

    return viewCherryPickRequest(_cherryPickRequest);
  }

  @UIControllerMethod
  public String plateMapCherryPicks()
  {
    if (_cherryPickRequest.getAssayPlateType() == null) {
      showMessage("cherryPicks.assayPlateTypeRequired");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }

    _cherryPickRequestPlateMapper.generatePlateMapping(_cherryPickRequest);
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
  @Transactional
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
    catch (IOException e) {
      reportApplicationError(e);
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String recordSuccessfulCreationOfAssayPlates()
  {
    if (!validateSelectedAssayPlates(VALIDATE_SELECTED_PLATES_FOR_LIQUID_TRANSFER)) {
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    _cherryPickRequestPlateStatusUpdater.updateAssayPlatesStatus(getSelectedAssayPlates(),
                                                                 getLiquidTransferPerformedBy().getSelection(),
                                                                 getDateOfLiquidTransfer(),
                                                                 getLiquidTransferComments(),
                                                                 CherryPickLiquidTransferStatus.SUCCESSFUL);
    return viewCherryPickRequest(_cherryPickRequest);
  }

  @UIControllerMethod
  @Transactional
  public String recordFailedCreationOfAssayPlates()
  {
    if (!validateSelectedAssayPlates(VALIDATE_SELECTED_PLATES_FOR_LIQUID_TRANSFER)) {
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    _cherryPickRequestPlateStatusUpdater.updateAssayPlatesStatus(getSelectedAssayPlates(),
                                                                 getLiquidTransferPerformedBy().getSelection(),
                                                                 getDateOfLiquidTransfer(),
                                                                 getLiquidTransferComments(),
                                                                 CherryPickLiquidTransferStatus.FAILED);
    Set<LabCherryPick> unfulfillable =
      _cherryPickRequestAllocator.reallocateAssayPlates(getSelectedAssayPlates());
    if (!unfulfillable.isEmpty()) {
      showMessage("cherryPicks.someCherryPicksUnfulfillable");
    }
    return viewCherryPickRequest(_cherryPickRequest);
  }

  @UIControllerMethod
  public String downloadCherryPickRequest()
  {
    try {
      jxl.Workbook workbook = _cherryPickRequestExporter.exportCherryPickRequest(_cherryPickRequest);
      JSFUtils.handleUserDownloadRequest(getFacesContext(),
                                         Workbook2Utils.toInputStream(workbook),
                                         _cherryPickRequest.getClass().getSimpleName() + "-" + _cherryPickRequest.getCherryPickRequestNumber() + ".xls",
                                         Workbook.MIME_TYPE);
    }
    catch (Exception e) {
      reportSystemError(e);
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

  @UIControllerMethod
  public String viewLeadScreener()
  {
    // TODO
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String edit()
  {
    _isEditMode = true;
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String cancel()
  {
    // edits are discarded (and edit mode is canceled) by virtue of controller reloading the screen entity from the database
    return viewCherryPickRequest(_cherryPickRequest);
  }

  @UIControllerMethod
  @Transactional
  public String save()
  {
    boolean valid = true;
    if( !saveTransferVolumePerWellRequested() ) valid = false;
    if( !saveTransferVolumePerWellApproved()) valid = false;
    if(! valid )
    {
      return REDISPLAY_PAGE_ACTION_RESULT;
    }

    _isEditMode = false;
    _dao.reattachEntity(_cherryPickRequest);
    _cherryPickRequest.setAssayPlateType(_assayPlateType.getSelection());
    _cherryPickRequest.setRequestedBy(_requestedBy.getSelection());
    _cherryPickRequest.setVolumeApprovedBy(_volumeApprovedBy.getSelection());
    return VIEW_CHERRY_PICK_REQUEST_ACTION_RESULT;
  }

  private boolean saveTransferVolumePerWellRequested()
  {
    try {
      Volume v = null;
      if (!StringUtils.isEmpty(_transferVolumePerWellRequestedValue)) {
        VolumeUnit units = _transferVolumePerWellRequestedType.getSelection();
        v = new Volume(_transferVolumePerWellRequestedValue, units).convertToReasonableUnits();
      }
      _cherryPickRequest.setTransferVolumePerWellRequested(v);
      _transferVolumePerWellRequestedType = null;
      _transferVolumePerWellRequestedValue = null;
    }
    catch (Exception e) {
      // you have to know the field name here.
      // alternative would be to write a validator/UIComponent that could take
      // both values at once.
      String fieldName = "requestedVolumetypedValuetypedValue";
      String msgKey = "invalidUserInput";

      String msg = "Volume value is incorrect";
      if (e.getLocalizedMessage() != null)
        msg += ": " + e.getLocalizedMessage();

      FacesContext facesContext = getFacesContext();
      log.warn("validation on: " + fieldName + ": " + _transferVolumePerWellRequestedValue + e);

      showMessageForLocalComponentId(facesContext, fieldName, msgKey, msg);
      return false;
    }
    return true;
  }

  private boolean saveTransferVolumePerWellApproved()
  {
    try {
      Volume v = null;
      if (!StringUtils.isEmpty(_transferVolumePerWellApprovedValue)) {
        VolumeUnit units = _transferVolumePerWellApprovedType.getSelection();
        v = new Volume(_transferVolumePerWellApprovedValue, units).convertToReasonableUnits();
      }
      _cherryPickRequest.setTransferVolumePerWellApproved(v);
      _transferVolumePerWellApprovedType = null;
      _transferVolumePerWellApprovedValue = null;
    }
    catch (Exception e) {
      // you have to know the field name here.
      // alternative would be to write a validator/UIComponent that could take
      // both values at once.
      String fieldName = "approvedVolumetypedValuetypedValue";
      String msgKey = "invalidUserInput";

      String msg = "Volume value is incorrect";
      if (e.getLocalizedMessage() != null)
        msg += ": " + e.getLocalizedMessage();

      FacesContext facesContext = getFacesContext();
      log.warn("validation on: " + fieldName + ": " + _transferVolumePerWellApprovedValue + e);

      showMessageForLocalComponentId(facesContext, fieldName, msgKey, msg);
      return false;
    }
    return true;
  }

  /**
   * Get the set of empty rows requested by the screener.
   * @return well names that must be left empty on each cherry pick assay plate
   */
  public Set<WellName> getEmptyWellsOnAssayPlate()
  {
    return _cherryPickRequest.getEmptyWellsOnAssayPlate();
  }

  /**
   * Set the set of empty wells.
   * @param emptyWellsOnAssayPlate wells that screener has requested be
   * left empty on each cherry pick assay plate
   */
  public void setEmptyWellsOnAssayPlate(Set<WellName> emptyWellsOnAssayPlate)
  {
    _cherryPickRequest.clearEmptyWellsOnAssayPlate();
    _cherryPickRequest.addEmptyWellsOnAssayPlate(emptyWellsOnAssayPlate);
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
            String[] eagerFetchRelationships = {};
            if (cherryPickRequest.getScreen().getScreenType() == ScreenType.RNAI) {
              eagerFetchRelationships = new String[] {
                // needed by libraryPoolToDuplexWellMapper, below
                "silencingReagents.wells.silencingReagents.gene",
                "silencingReagents.gene" };
            }
            else if (cherryPickRequest.getScreen().getScreenType() == ScreenType.SMALL_MOLECULE) {
              eagerFetchRelationships = new String[] { "compounds" };
            }
            Well well = _dao.findEntityById(Well.class,
                                            wellKey.toString(),
                                            true,
                                            eagerFetchRelationships);
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

          // note: must show warnings after session flush, as some of the warnings are calculated using HQL queries
          _dao.flush();
          showAdminWarnings();
        }
      });
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


  @UIControllerMethod
  @Transactional
  public void showAdminWarnings()
  {
    // eager fetch all data needed to calculate warnings
    CherryPickRequest cherryPickRequest = _dao.reloadEntity(_cherryPickRequest,
                                                            true,
                                                            "screen",
                                                            "labCherryPicks.screenerCherryPick.screenedWell.deprecationActivity",
                                                            "labCherryPicks.sourceWell.deprecationActivity");
    _dao.needReadOnly(cherryPickRequest, "screenerCherryPicks.screenedWell.library");

    boolean warningIssued = false;
    warningIssued |= doWarnOnCherryPickAllowanceExceeded(cherryPickRequest);
    warningIssued |= doWarnOnInvalidPoolWellScreenerCherryPicks(cherryPickRequest);
    warningIssued |= doWarnOnDuplicateScreenerCherryPicks(cherryPickRequest);
    warningIssued |= doWarnOnDeprecatedWells(cherryPickRequest);
    warningIssued |= doWarnOnLibraryScreeningStatus(cherryPickRequest);
    if (!warningIssued) {
      showMessage("cherryPicks.allCherryPicksAreValid");
    }
  }

  private boolean doWarnOnCherryPickAllowanceExceeded(CherryPickRequest cherryPickRequest)
  {
    CherryPickRequestAllowancePolicy policy;
    if (cherryPickRequest instanceof CompoundCherryPickRequest) {
      policy = _compoundCherryPickRequestAllowancePolicy;
    }
    else if (cherryPickRequest instanceof RNAiCherryPickRequest){
      policy = _rnaiCherryPickRequestAllowancePolicy;
    }
    else {
      throw new DevelopmentException("unsupported cherry pick request type: " + cherryPickRequest.getClass().getName());
    }
    if (policy.isCherryPickAllowanceExceeded(cherryPickRequest)) {
      showMessage("cherryPicks.cherryPickAllowanceExceeded",
                  policy.getCherryPickAllowanceUsed(cherryPickRequest),
                  policy.getCherryPickAllowance(cherryPickRequest));
      return true;
    }
    return false;
  }

  private boolean doWarnOnInvalidPoolWellScreenerCherryPicks(CherryPickRequest cherryPickRequest)
  {
    int n = 0;
    for (ScreenerCherryPick screenerCherryPick : cherryPickRequest.getScreenerCherryPicks()) {
      if (screenerCherryPick.getLabCherryPicks().size() == 0) {
        ++n;
      }
    }
    if (n > 0) {
      showMessage("cherryPicks.poolWellsWithoutDuplexWells", Integer.toString(n));
      return true;
    }
    return false;
  }

  private boolean doWarnOnDuplicateScreenerCherryPicks(CherryPickRequest cherryPickRequest)
  {
    Map<WellKey,Number> duplicateScreenerCherryPickWellKeysMap =
      _cherryPickRequestDao.findDuplicateCherryPicksForScreen(cherryPickRequest.getScreen());
    Set<WellKey> duplicateScreenerCherryPickWellKeys = duplicateScreenerCherryPickWellKeysMap.keySet();
    Set<WellKey> ourScreenerCherryPickWellsKeys = new HashSet<WellKey>();
    for (ScreenerCherryPick screenerCherryPick : cherryPickRequest.getScreenerCherryPicks()) {
      ourScreenerCherryPickWellsKeys.add(screenerCherryPick.getScreenedWell().getWellKey());
    }
    duplicateScreenerCherryPickWellKeys.retainAll(ourScreenerCherryPickWellsKeys);
    if (duplicateScreenerCherryPickWellKeysMap.size() > 0) {
      String duplicateWellsList = StringUtils.makeListString(duplicateScreenerCherryPickWellKeys, ", ");
      showMessage("cherryPicks.duplicateCherryPicksInScreen",
                  cherryPickRequest.getScreen().getScreenNumber(),
                  duplicateWellsList);
      return true;
    }
    return false;
  }

  private boolean doWarnOnDeprecatedWells(CherryPickRequest cherryPickRequest)
  {
    Multimap<AdministrativeActivity,WellKey> wellDeprecations =
      new TreeMultimap<AdministrativeActivity,WellKey>();
    for (LabCherryPick labCherryPick : cherryPickRequest.getLabCherryPicks()) {
      Well well = labCherryPick.getSourceWell();
      if (well.isDeprecated()) {
        wellDeprecations.put(well.getDeprecationActivity(), well.getWellKey());
      }
      well = labCherryPick.getScreenerCherryPick().getScreenedWell();
      if (well.isDeprecated()) {
        wellDeprecations.put(well.getDeprecationActivity(), well.getWellKey());
      }
    }
    for (AdministrativeActivity deprecationActivity : wellDeprecations.keySet()) {
      showMessage("cherryPicks.deprecatedWells",
                  deprecationActivity.getComments(),
                  Join.join(", ", wellDeprecations.values()));
      return true;
    }
    return false;
  }

  private boolean doWarnOnLibraryScreeningStatus(CherryPickRequest cherryPickRequest)
  {
    Set<Library> libraries = new LinkedHashSet<Library>();
    for (LabCherryPick labCherryPick : cherryPickRequest.getLabCherryPicks()) {
      libraries.add(labCherryPick.getSourceWell().getLibrary());
      libraries.add(labCherryPick.getScreenerCherryPick().getScreenedWell().getLibrary());
    }
    for (Library library : libraries) {
      if (library.getScreeningStatus() != LibraryScreeningStatus.ALLOWED) {
        if (!cherryPickRequest.getScreen().getLibrariesPermitted().contains(library)) {
          showMessage("libraries.libraryUsageConflict",
                      library.getShortName(),
                      library.getScreeningStatus().getValue());
          return true;
        }
      }
    }
    return false;
  }

  private String doViewCherryPickRequestWellVolumes(boolean forUnfulfilledOnly)
  {
    _wellCopyVolumesBrowser.searchWellsForCherryPickRequest(_cherryPickRequest, forUnfulfilledOnly);
    return VIEW_WELL_VOLUME_SEARCH_RESULTS;
  }
}
