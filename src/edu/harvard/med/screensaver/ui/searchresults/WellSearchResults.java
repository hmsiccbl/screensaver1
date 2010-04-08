// $HeadURL:
// svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/ui/searchresults/WellSearchResults.java
// $
// $Id$

// Copyright © 2006, 2010 by the President and Fellows of Harvard College.

// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.Query;
import edu.harvard.med.screensaver.db.datafetcher.AllEntitiesOfTypeDataFetcher;
import edu.harvard.med.screensaver.db.datafetcher.DataFetcher;
import edu.harvard.med.screensaver.db.datafetcher.EntityDataFetcher;
import edu.harvard.med.screensaver.db.datafetcher.EntitySetDataFetcher;
import edu.harvard.med.screensaver.db.datafetcher.NoOpDataFetcher;
import edu.harvard.med.screensaver.db.datafetcher.ParentedEntityDataFetcher;
import edu.harvard.med.screensaver.db.hqlbuilder.Conjunction;
import edu.harvard.med.screensaver.db.hqlbuilder.Disjunction;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.db.hqlbuilder.JoinType;
import edu.harvard.med.screensaver.io.DataExporter;
import edu.harvard.med.screensaver.io.libraries.WellsSdfDataExporter;
import edu.harvard.med.screensaver.io.libraries.smallmolecule.StructureImageProvider;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryContentsVersion;
import edu.harvard.med.screensaver.model.libraries.LibraryWellType;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.AnnotationValue;
import edu.harvard.med.screensaver.model.screenresults.AssayWellControlType;
import edu.harvard.med.screensaver.model.screenresults.DataColumn;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.Study;
import edu.harvard.med.screensaver.policy.EntityViewPolicy;
import edu.harvard.med.screensaver.ui.UICommand;
import edu.harvard.med.screensaver.ui.libraries.LibraryViewer;
import edu.harvard.med.screensaver.ui.libraries.WellViewer;
import edu.harvard.med.screensaver.ui.screenresults.MetaDataType;
import edu.harvard.med.screensaver.ui.table.Criterion;
import edu.harvard.med.screensaver.ui.table.DataTable;
import edu.harvard.med.screensaver.ui.table.Criterion.Operator;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;
import edu.harvard.med.screensaver.ui.table.column.TableColumnManager;
import edu.harvard.med.screensaver.ui.table.column.entity.BooleanEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.EnumEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.HasFetchPaths;
import edu.harvard.med.screensaver.ui.table.column.entity.ImageEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.IntegerEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.IntegerSetEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.RealEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.TextEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.TextSetEntityColumn;
import edu.harvard.med.screensaver.ui.table.model.DataTableModel;
import edu.harvard.med.screensaver.util.Triple;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;


/**
 * A {@link SearchResults} for {@link Well Wells}.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class WellSearchResults extends EntitySearchResults<Well,String>
{
  private static final Logger log = Logger.getLogger(WellSearchResults.class);

  private static final String WELL_COLUMNS_GROUP = TableColumn.UNGROUPED;
  private static final String SILENCING_REAGENT_COLUMNS_GROUP = "Silencing Reagent";
  private static final String COMPOUND_COLUMNS_GROUP = "Compound";
  private static final String OUR_DATA_COLUMNS_GROUP = "Data Columns";
  private static final String OTHER_DATA_COLUMNS_GROUP = "Data Columns (Other Screen Results)";
  private static final String OTHER_ANNOTATION_TYPES_COLUMN_GROUP = "Annotations";
  private static final String OUR_ANNOTATION_TYPES_COLUMN_GROUP = "Annotations (Other Studies)";

  private enum WellSearchResultMode {
    SCREEN_RESULT_WELLS,
    STUDY_REAGENT_WELLS,
    LIBRARY_WELLS,
    ALL_WELLS,
    SET_OF_WELLS 
  };

  private static Function<Well,String> WellToDuplexWells = new Function<Well,String>() { 
    public String apply(Well well) {
      return well.getWellKey().toString();
    }
  };

  private GenericEntityDAO _dao;
  private EntityViewPolicy _entityViewPolicy;
  private LibraryViewer _libraryViewer;
  private WellsSdfDataExporter _wellsSdfDataExporter;
  protected StructureImageProvider _structureImageProvider;

  private WellSearchResultMode _mode;
  private Library _library;
  private LibraryContentsVersion _libraryContentsVersion;
  private Study _study;
  private Set<ReagentVendorIdentifier> _reagentIds;
  private ScreenResult _screenResult;
  private Set<Integer> _plateNumbers;


  
  /**
   * @motivation for CGLIB2
   */
  protected WellSearchResults()
  {}

  /**
   * Construct a new <code>WellSearchResultsViewer</code> object.
   */
  public WellSearchResults(GenericEntityDAO dao,
                           EntityViewPolicy entityViewPolicy,
                           LibraryViewer libraryViewer,
                           WellViewer wellViewer,
                           WellsSdfDataExporter wellsSdfDataExporter,
                           StructureImageProvider structureImageProvider,
                           List<DataExporter<?>> dataExporters)
  {
    super(Lists.newArrayList(Iterables.concat(Lists.newArrayList(wellsSdfDataExporter), dataExporters)), wellViewer);
    _wellsSdfDataExporter = wellsSdfDataExporter;
    _dao = dao;
    _entityViewPolicy = entityViewPolicy;
    _libraryViewer = libraryViewer;
    _structureImageProvider = structureImageProvider;
  }

  /**
   * Called from the top level menu page.
   * 
   * @motivation Initializes the DataTable with a NoOpDataFetcher; causing the bean 
   * to return an empty search result on the first viewing.
   * 
   * @see WellSearchResults#searchCommandListener(javax.faces.event.ActionEvent)
   */
  @Override
  public void searchAll()
  {
    _mode = WellSearchResultMode.ALL_WELLS;
    _library = null;
    _screenResult = null;
    _plateNumbers = null;
    _libraryContentsVersion = null;
    // initially, show an empty search result
    initialize(new NoOpDataFetcher<Well,String,PropertyPath<Well>>());

    // start with search panel open
    setTableFilterMode(true);
  }
  
  public void searchWellsForLibrary(Library library)
  {
    _library = library;
    _libraryContentsVersion = null;
    _mode = WellSearchResultMode.LIBRARY_WELLS;
    _screenResult = null;
    _plateNumbers = null;
    initialize(new ParentedEntityDataFetcher<Well,String>(Well.class,
      Well.library,
      _library,
      _dao));

    // start with search panel closed
    setTableFilterMode(false);
  }

  public void searchWellsForLibraryContentsVersion(final LibraryContentsVersion lcv)
  {
    _library = lcv.getLibrary();
    _libraryContentsVersion = lcv;
    _mode = WellSearchResultMode.LIBRARY_WELLS;
    _screenResult = null;
    _plateNumbers = null;
    initialize(new ParentedEntityDataFetcher<Well,String>(Well.class,
      Well.library,
      _library,
      _dao) {
      @Override
      protected void addDomainRestrictions(HqlBuilder hql,
                                           Map<RelationshipPath<Well>,String> path2Alias)
      {
        super.addDomainRestrictions(hql, path2Alias);
        // filter through only the specified released version of the reagent 
        getOrCreateJoin(hql, Well.reagents.restrict("libraryContentsVersion", _libraryContentsVersion), path2Alias, JoinType.LEFT);
      }
    });

    // start with search panel closed
    setTableFilterMode(false);
  }
  
  public void searchWellsForScreenResult(ScreenResult screenResult)
  {
    _mode = WellSearchResultMode.SCREEN_RESULT_WELLS;
    _library = null;
    _screenResult = screenResult;
    _plateNumbers = null;
    if (screenResult == null) {
      initialize(new NoOpDataFetcher<Well,String,PropertyPath<Well>>());
    }
    else {
      initialize(new ParentedEntityDataFetcher<Well,String>(
        Well.class,
        Well.screenResults,
        screenResult,
        _dao));
      
      // show columns for this screenResult's data columns
      for (DataColumn dataColumn : screenResult.getDataColumns()) {
        TableColumn<Well,?> column = getColumnManager().getColumn(makeColumnName(dataColumn, screenResult.getScreen().getScreenNumber()));
        if (column != null) {
          column.setVisible(true);
        }
      }

      // start with search panel closed
      setTableFilterMode(false);
    }
  }

  public void searchWells(Set<WellKey> wellKeys)
  {
    _mode = WellSearchResultMode.SET_OF_WELLS;
    _library = null;
    _screenResult = null;
    _plateNumbers = new HashSet<Integer>();
    _libraryContentsVersion = null;
    Set<String> wellKeyStrings = new HashSet<String>(wellKeys.size());
    for (WellKey wellKey : wellKeys) {
      wellKeyStrings.add(wellKey.toString());
      _plateNumbers.add(wellKey.getPlateNumber());
    }
    initialize(new EntitySetDataFetcher<Well,String>(Well.class,wellKeyStrings, _dao));

    // start with search panel closed
    setTableFilterMode(false);
  }

  public void searchReagentsForStudy(Study study)
  {
    _study = study;
    _reagentIds = null;
    _libraryContentsVersion = null;
    initialize(new ParentedEntityDataFetcher<Well,String>(
      Well.class,
      Well.reagents.to(Reagent.studies),
      study,
      _dao));
    getColumnManager().getColumn("Vendor").setVisible(true);
    getColumnManager().getColumn("Vendor ID").setVisible(true);
    // show columns for this screenResult's data columns
    for (AnnotationType at : study.getAnnotationTypes()) {
      getColumnManager().getColumn(WellSearchResults.makeColumnName(at, _study.getStudyNumber())).setVisible(true);
    }
  }

  public void searchReagents(final Set<ReagentVendorIdentifier> reagentIds)
  {
    _study = null;
    _reagentIds = reagentIds;
    _libraryContentsVersion = null;
    
    initialize(new EntityDataFetcher<Well,String>(Well.class, _dao) {
      @Override
      protected void addDomainRestrictions(HqlBuilder hql,
                                           Map<RelationshipPath<Well>,String> path2Alias)
      {
        hql.from(getRootAlias(), "reagents", "r").whereIn("r", "vendorId", reagentIds);
      }
    });
    getColumnManager().getColumn("Vendor").setVisible(true);
    getColumnManager().getColumn("Vendor ID").setVisible(true);
  }

  public LibraryContentsVersion getLibraryContentsVersion()
  {
    return _libraryContentsVersion;
  }

  @Override
  protected DataTableModel<Well> buildDataTableModel(DataFetcher<Well,String,PropertyPath<Well>> dataFetcher,
                                                     List<? extends TableColumn<Well,?>> columns)
  {
    if (dataFetcher instanceof EntityDataFetcher) {
      return new VirtualPagingEntitySearchResultsDataModel((EntityDataFetcher<Well,String>) dataFetcher);
    }
    return super.buildDataTableModel(dataFetcher, columns);
  }

  @Override
  protected List<? extends TableColumn<Well,?>> buildColumns()
  {
    List<TableColumn<Well,?>> columns = Lists.newArrayList();
    buildWellPropertyColumns(columns);
    buildReagentPropertyColumns(columns);
    buildCompoundPropertyColumns(columns);
    buildSilencingReagentPropertyColumns(columns);
    buildDataColumns(columns);
    buildAnnotationTypeColumns(columns);
    return columns;
  }

  private void buildSilencingReagentPropertyColumns(List<TableColumn<Well,?>> columns)
  {
    columns.add(new WellReagentEntityColumn<SilencingReagent,String>(
      SilencingReagent.class,
      new TextEntityColumn<SilencingReagent>(
        SilencingReagent.facilityGene.toProperty("geneName"),
        "Gene Name",
        "The gene name for the silencing reagent in the well",
        SILENCING_REAGENT_COLUMNS_GROUP) {
        @Override
        public String getCellValue(SilencingReagent reagent)
        {
          Gene gene = reagent.getFacilityGene();
          return gene == null ? null : gene.getGeneName();
        }
      }));
    columns.get(columns.size() - 1).setVisible(false);

    columns.add(new WellReagentEntityColumn<SilencingReagent,Integer>(
      SilencingReagent.class,
      new IntegerEntityColumn<SilencingReagent>(
        SilencingReagent.facilityGene.toProperty("entrezgeneId"),
        "Entrez Gene ID",
        "The Entrez gene ID for the gene targeted by silencing reagent in the well",
        SILENCING_REAGENT_COLUMNS_GROUP) {
        @Override
        public Integer getCellValue(SilencingReagent reagent)
        {
          Gene gene = reagent.getFacilityGene();
          return gene == null ? null : gene.getEntrezgeneId();
        }
      }));
    columns.get(columns.size() - 1).setVisible(false);

    columns.add(new WellReagentEntityColumn<SilencingReagent,String>(
      SilencingReagent.class,
      new TextEntityColumn<SilencingReagent>(
        new PropertyPath<SilencingReagent>(SilencingReagent.class, "silencingReagentType"),
        "Silencing Reagent Type",
        "The enumerated Silencing Reagent Type",
        SILENCING_REAGENT_COLUMNS_GROUP) {
        @Override
        public String getCellValue(SilencingReagent reagent)
        {
          return reagent.getSilencingReagentType().getValue();
        }
      }));
    columns.get(columns.size() - 1).setVisible(false);

    columns.add(new WellReagentEntityColumn<SilencingReagent,Set<String>>(
      SilencingReagent.class,
      new TextSetEntityColumn<SilencingReagent>(
      SilencingReagent.facilityGene.to(Gene.entrezgeneSymbols).toCollectionOfValues(),
      "Entrez Gene Symbol",
      "The Entrez gene symbol for the gene targeted by silencing reagent in the well",
      SILENCING_REAGENT_COLUMNS_GROUP) {
      @Override
      public Set<String> getCellValue(SilencingReagent reagent)
      {
        Gene gene = reagent.getFacilityGene();
        if (gene == null) { return null; }
        return reagent.getFacilityGene().getEntrezgeneSymbols();
      }
    }));
    columns.get(columns.size() - 1).setVisible(false);

    columns.add(new WellReagentEntityColumn<SilencingReagent,Set<String>>(
      SilencingReagent.class,
      new TextSetEntityColumn<SilencingReagent>(
      SilencingReagent.facilityGene.to(Gene.genbankAccessionNumbers).toCollectionOfValues(),
      "Genbank Accession Numbers",
      "The Genbank Accession Numbers for the gene targeted by silencing reagent in the well",
      SILENCING_REAGENT_COLUMNS_GROUP) {
      @Override
      public Set<String> getCellValue(SilencingReagent reagent)
      {
        Gene gene = reagent.getFacilityGene();
        if (gene == null) { return null; }
        return reagent.getFacilityGene().getGenbankAccessionNumbers();
      }
    }));
    columns.get(columns.size() - 1).setVisible(false);

    columns.add(new WellReagentEntityColumn<SilencingReagent,String>(
      SilencingReagent.class,
      new TextEntityColumn<SilencingReagent>(
      new PropertyPath<SilencingReagent>(SilencingReagent.class, "sequence"),
      "Sequence",
      "The nucleotide sequence of this silencing reagent",
      SILENCING_REAGENT_COLUMNS_GROUP) {
      @Override
      public String getCellValue(SilencingReagent reagent)
      {
        if (_entityViewPolicy.isAllowedAccessToSilencingReagentSequence(reagent)) {
          return reagent.getSequence();
        }
        return null;
      }
    }));
    columns.get(columns.size() - 1).setVisible(false);

    columns.add(new WellReagentEntityColumn<SilencingReagent,String>(
      SilencingReagent.class,
      new TextEntityColumn<SilencingReagent>(
      SilencingReagent.facilityGene.toProperty("species"),
      "Species",
      "The species of this silencing reagent",
      SILENCING_REAGENT_COLUMNS_GROUP) {
      @Override
      public String getCellValue(SilencingReagent reagent)
      {
        Gene gene = reagent.getFacilityGene();
        if (gene == null) { return null; }
        return reagent.getFacilityGene().getSpeciesName();
      }
    }));
    columns.get(columns.size() - 1).setVisible(false);

    columns.add(new WellReagentEntityColumn<SilencingReagent,Set<String>>(
      SilencingReagent.class,
      new TextSetEntityColumn<SilencingReagent>(
        SilencingReagent.duplexWells.toProperty("wellId"),
        "Duplex Wells",
        "The duplex wells to which this pool well deconvolutes",
        SILENCING_REAGENT_COLUMNS_GROUP) {
        @Override
        public Set<String> getCellValue(SilencingReagent reagent)
        {
          return Sets.newHashSet(Iterables.transform(reagent.getDuplexWells(), WellToDuplexWells));
        }
      }));
    columns.get(columns.size() - 1).setVisible(false);
  }

  private void buildCompoundPropertyColumns(List<TableColumn<Well,?>> columns)
  {
    columns.add(new WellReagentEntityColumn<SmallMoleculeReagent,String>(
      SmallMoleculeReagent.class,
      new ImageEntityColumn<SmallMoleculeReagent>(
        new PropertyPath<SmallMoleculeReagent>(SmallMoleculeReagent.class, "id"),
        "Compound Structure Image",
        "The structure image for the compound in the well",
        COMPOUND_COLUMNS_GROUP) {
        @Override
        public String getCellValue(SmallMoleculeReagent reagent)
        {
          if (_structureImageProvider != null) {
              return _structureImageProvider.getImageUrl(reagent).toString();
          }
          return null;
        }
      }));
    columns.get(columns.size() - 1).setVisible(false);

    columns.add(new WellReagentEntityColumn<SmallMoleculeReagent,String>(
      SmallMoleculeReagent.class,
      new TextEntityColumn<SmallMoleculeReagent>(
        new PropertyPath<SmallMoleculeReagent>(SmallMoleculeReagent.class, "smiles"),
        "Compound SMILES",
        "The SMILES for the compound in the well",
        COMPOUND_COLUMNS_GROUP) {
        @Override
        public String getCellValue(SmallMoleculeReagent reagent)
        {
          return reagent.getSmiles();
        }
      }));
    columns.get(columns.size() - 1).setVisible(false);

    columns.add(new WellReagentEntityColumn<SmallMoleculeReagent,String>(
      SmallMoleculeReagent.class,
      new TextEntityColumn<SmallMoleculeReagent>(
        new PropertyPath<SmallMoleculeReagent>(SmallMoleculeReagent.class, "inchi"),
      "Compound InChi",
      "The InChi for the compound in the well", 
      COMPOUND_COLUMNS_GROUP) {
      @Override
      public String getCellValue(SmallMoleculeReagent reagent)
      {
        return reagent.getInchi();
      }
    }));
    columns.get(columns.size() - 1).setVisible(false);

    columns.add(new WellReagentEntityColumn<SmallMoleculeReagent,Set<String>>(
      SmallMoleculeReagent.class,
      new TextSetEntityColumn<SmallMoleculeReagent>(
        SmallMoleculeReagent.compoundNames.toCollectionOfValues(),
        "Compound Names",
        "The names of the compound in the well",
        COMPOUND_COLUMNS_GROUP) {
        @Override
        public Set<String> getCellValue(SmallMoleculeReagent reagent)
        {
          return reagent.getCompoundNames();
        }
      }));
    columns.get(columns.size() - 1).setVisible(false);

    columns.add(
      new WellReagentEntityColumn<SmallMoleculeReagent,Set<Integer>>(
        SmallMoleculeReagent.class,
        new IntegerSetEntityColumn<SmallMoleculeReagent>(
          SmallMoleculeReagent.pubchemCids.toCollectionOfValues(),
          "PubChem CIDs",
          "The PubChem CIDs of the compound in the well",
          COMPOUND_COLUMNS_GROUP) {
          @Override
          public Set<Integer> getCellValue(SmallMoleculeReagent reagent)
          {
            return reagent.getPubchemCids();
          }
        }));
    columns.get(columns.size() - 1).setVisible(false);

    columns.add(new WellReagentEntityColumn<SmallMoleculeReagent,Set<Integer>>(
      SmallMoleculeReagent.class,
      new IntegerSetEntityColumn<SmallMoleculeReagent>(
        SmallMoleculeReagent.chembankIds.toCollectionOfValues(),
        "ChemBank IDs",
        "The ChemBank IDs of the primary compound in the well",
        COMPOUND_COLUMNS_GROUP) {
        @Override
        public Set<Integer> getCellValue(SmallMoleculeReagent reagent)
        {
          return reagent.getChembankIds();
        }
      }));
    columns.get(columns.size() - 1).setVisible(false);
  }
  
  private void buildReagentPropertyColumns(List<TableColumn<Well,?>> columns)
  {
    columns.add(new WellReagentEntityColumn<Reagent,String>(Reagent.class,
      new TextEntityColumn<Reagent>(
        new PropertyPath<Reagent>(Reagent.class, "vendorId.vendorName"),
        "Vendor",
        "The vendor of the reagent in this well.", 
        WELL_COLUMNS_GROUP) {
      @Override
      public String getCellValue(Reagent reagent)
      {
        return reagent.getVendorId().getVendorName();
      }
    }));
    
    columns.add(new WellReagentEntityColumn<Reagent,String>(Reagent.class,
      new TextEntityColumn<Reagent>(
        new PropertyPath<Reagent>(Reagent.class, "vendorId.vendorIdentifier"),
        "Vendor ID",
        "The vendor-assigned identifier for the reagent in this well.", 
        WELL_COLUMNS_GROUP) {
      @Override
      public String getCellValue(Reagent reagent)
      {
        return reagent.getVendorId().getVendorIdentifier();
      }
    }));

    columns.add(new WellReagentEntityColumn<Reagent,Integer>(Reagent.class,
      new IntegerEntityColumn<Reagent>(
        Reagent.libraryContentsVersion.toProperty("versionNumber"),
        "Library Contents Version",
        "The reagent's library contents version", 
        WELL_COLUMNS_GROUP) {
      @Override
      public Integer getCellValue(Reagent reagent)
      {
        return reagent.getLibraryContentsVersion().getVersionNumber();
      }
    }));
    columns.get(columns.size() - 1).setVisible(false);
  }

  private void buildWellPropertyColumns(List<TableColumn<Well,?>> columns)
  {
    columns.add(new IntegerEntityColumn<Well>(
      new PropertyPath<Well>(Well.class, "plateNumber"),
      "Plate",
      "The number of the plate the well is located on",
      WELL_COLUMNS_GROUP) {
      @Override
      public Integer getCellValue(Well well)
      {
        return well.getPlateNumber();
      }
    });

    columns.add(new TextEntityColumn<Well>(
      new PropertyPath<Well>(Well.class, "wellName"),
      "Well",
      "The plate coordinates of the well",
      WELL_COLUMNS_GROUP) {
      @Override
      public String getCellValue(Well well)
      {
        return well.getWellName();
      }

      @Override
      public boolean isCommandLink()
      {
        return true;
      }

      @Override
      public Object cellAction(Well well)
      {
        return viewSelectedEntity();
      }
    });

    columns.add(new TextEntityColumn<Well>(
      Well.library.toProperty("libraryName"),
      "Library",
      "The library containing the well",
      WELL_COLUMNS_GROUP) {
      @Override
      public String getCellValue(Well well)
      {
        return well.getLibrary().getLibraryName();
      }

      @Override
      public boolean isCommandLink()
      {
        return true;
      }

      @Override
      public Object cellAction(Well well)
      {
        return _libraryViewer.viewEntity(well.getLibrary());
      }
    });

    columns.add(new EnumEntityColumn<Well,ScreenType>(
      Well.library.toProperty("screenType"),
      "Screen Type",
      "The library screen type",
      WELL_COLUMNS_GROUP,
      ScreenType.values()) {
      @Override
      public ScreenType getCellValue(Well well)
      {
        return well.getLibrary().getScreenType();
      }
    });

    columns.add(new EnumEntityColumn<Well,LibraryWellType>(
      new PropertyPath<Well>(Well.class, "libraryWellType"),
      "Library Well Type",
      "The type of well, e.g., 'Experimental', 'Control', 'Empty', etc.",
      WELL_COLUMNS_GROUP,
      LibraryWellType.values()) {
      @Override
      public LibraryWellType getCellValue(Well well)
      {
        return well.getLibraryWellType();
      }
    });

    columns.add(new TextEntityColumn<Well>(
      new PropertyPath<Well>(Well.class, "facilityId"),
      "Facility ID",
      "An alternate identifier assigned by the facility to identify this well",
      WELL_COLUMNS_GROUP) {
      @Override
      public String getCellValue(Well well)
      {
        return well.getFacilityId();
      }
    });
    columns.get(columns.size() - 1).setVisible(false);

    columns.add(new BooleanEntityColumn<Well>(
      new PropertyPath<Well>(Well.class, "deprecated"),
      "Deprecated", "Whether the well has been deprecated", TableColumn.UNGROUPED) {
      @Override
      public Boolean getCellValue(Well well)
      {
        return well.isDeprecated();
      }
    });
    columns.get(columns.size() - 1).setVisible(false);

    columns.add(new TextEntityColumn<Well>(
      Well.deprecationActivity.toProperty("comments"),
      "Deprecation Reason", "Why the well has been deprecated", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(Well well)
      {
        return well.isDeprecated() ? well.getDeprecationActivity().getComments() : null;
      }
    });
    columns.get(columns.size() - 1).setVisible(false);
  }

  private void buildDataColumns(List<TableColumn<Well,?>> columns)
  {
    List<TableColumn<Well,?>> otherColumns = Lists.newArrayList();
    boolean isAssayWellTypeColumnCreated = false; 
    
    for (Triple<DataColumn,Integer,String> columnAndScreenNumberAndTitle : findDataColumnsForScreenResultsWithMutualPlates()) {
      final DataColumn dataColumn = columnAndScreenNumberAndTitle.getFirst();
      if (dataColumn.isRestricted()) {
        continue;
      }
      Integer screenNumber = columnAndScreenNumberAndTitle.getSecond();
      String screenTitle = columnAndScreenNumberAndTitle.getThird();

      TableColumn<Well,?> column;
//      if (col.isPositiveIndicator() &&
//        col.getPositiveIndicatorType() == DataType.BOOLEAN) {
//          column = new BooleanEntityColumn<Well>(
//            new PropertyPath<Well>(Well.class, "resultValues[dataColumn]", "positive", dataColumn),
//            makeColumnName(dataColumn, screenNumber),
//            dataColumn.getDescription(),
//            columnGroup) {
//            @Override
//            public Boolean getCellValue(Well well)
//            {
//              ResultValue rv = well.getResultValues().get(dataColumn);
//              return rv == null ? null : rv.isPositive();
//            }
//          };
//        }
//        else if (dataColumn.getPositiveIndicatorType() == DataType.NUMERICAL) {
//          column = new FixedDecimalEntityColumn<Well>(
//            new PropertyPath<Well>(Well.class, "resultValues[dataColumn]", "numericValue", dataColumn),
//            makeColumnName(dataColumn, screenNumber),
//            dataColumn.getDescription(),
//            columnGroup) {
//            @Override
//            public BigDecimal getCellValue(Well well)
//            {
//              // TODO: move this code to ResultValue
//              BigDecimal value = null;
//              ResultValue rv = well.getResultValues().get(dataColumn);
//              if (rv != null && rv.getNumericValue() != null) {
//                value = new BigDecimal(rv.getNumericValue());
//                Integer scale = rv.getNumericDecimalPrecision();
//                if (scale == null) {
//                  scale = ResultValue.DEFAULT_DECIMAL_PRECISION;
//                }
//                value = value.setScale(scale, RoundingMode.HALF_UP);
//              }
//              return value;
//            }
//          };
//        }
//        else if (dataColumn.getPositiveIndicatorType() == DataType.PARTITION) {
//          column = new VocabularyEntityColumn<Well,PartitionedValue>(
//            new PropertyPath<Well>(Well.class, "resultValues[dataColumn]", "value", dataColumn),
//            makeColumnName(dataColumn, screenNumber) + " Positive",
//            "Positive flag for " + makeColumnName(dataColumn, screenNumber),
//            columnGroup,
//            null,
//            PartitionedValue.values()) {
//            @Override
//            public PartitionedValue getCellValue(Well well)
//            {
//              ResultValue rv = well.getResultValues().get(dataColumn);
//              if (rv != null) {
//                return PartitionedValue.lookupByValue(rv.getValue());
//              }
//              return null;
//            }
//          };
//        }
//        else {
//          throw new RuntimeException("system error; unhandled DataType");
//        }
//      }
//      else
      if (dataColumn.isNumeric()) {
        column = new RealEntityColumn<Well>(
          Well.resultValues.restrict(ResultValue.DataColumn.getLeaf(), dataColumn).toProperty("numericValue"),
          makeColumnName(dataColumn, screenNumber),
          makeColumnDescription(dataColumn, screenNumber, screenTitle),
          makeScreenColumnGroup(screenNumber, screenTitle),
          dataColumn.getDecimalPlaces()) {
          @Override
          public Double getCellValue(Well well)
          {
            ResultValue rv = well.getResultValues().get(dataColumn);
            if (rv == null || rv.isRestricted()) { 
              return null;
            }
            return rv.getNumericValue();
          }
          
          @Override
          public boolean isSortableSearchable()
          {
            // if related screen result is restricted, this means that we're
            // only allowed to share mutual positives; in this case we do not
            // allow sorting or filtering on this column, since it would allow
            // hidden values to be inferred by the user
            return !!!dataColumn.getScreenResult().isRestricted(); 
          }
        };
      }
      else {
        column = new TextEntityColumn<Well>(
          Well.resultValues.restrict(ResultValue.DataColumn.getLeaf(), dataColumn).toProperty("value"),
          makeColumnName(dataColumn, screenNumber),
          makeColumnDescription(dataColumn, screenNumber, screenTitle),
          makeScreenColumnGroup(screenNumber, screenTitle)) {
          @Override
          public String getCellValue(Well well)
          {
            ResultValue rv = well.getResultValues().get(dataColumn);
            if (rv == null || rv.isRestricted()) { 
              return null;
            }
            return rv.getValue();
          }
          
          @Override
          public boolean isSortableSearchable()
          {
            // if related screen result is restricted, this means that we're
            // only allowed to share mutual positives; in this case we do not
            // allow sorting or filtering on this column, since it would allow
            // hidden values to be inferred by the user
            return !!!dataColumn.getScreenResult().isRestricted(); 
          }
        };
      }

      // request eager fetching of dataColumn, since Hibernate will otherwise fetch these with individual SELECTs
      // we also need to eager fetch all the way "up" to Screen, for data access policy checks
      ((HasFetchPaths<Well>) column).addRelationshipPath(Well.resultValues.to(ResultValue.DataColumn).to(DataColumn.ScreenResult).to(ScreenResult.screen));

      if (!isAssayWellTypeColumnCreated && column.getGroup().equals(OUR_DATA_COLUMNS_GROUP)) {
        columns.add(new EnumEntityColumn<Well,AssayWellControlType>(
          Well.resultValues.restrict(ResultValue.DataColumn.getLeaf(), dataColumn).toProperty("assayWellControlType"),
          "Assay Well Control Type",
          "The type of assay well control",
          column.getGroup(),
          AssayWellControlType.values()) {
          @Override
          public AssayWellControlType getCellValue(Well well)
          {
            return well.getResultValues().get(dataColumn) == null ? null : well.getResultValues().get(dataColumn).getAssayWellControlType();
          }
        });
        isAssayWellTypeColumnCreated = true;
      }
      if (column.getGroup().equals(OUR_DATA_COLUMNS_GROUP)) {
        columns.add(column);
        column.setVisible(true);
      }
      else {
        otherColumns.add(column);
        column.setVisible(false);
      }
    }
    columns.addAll(otherColumns);
  }

  private String makeScreenColumnGroup(Integer screenNumber, String screenTitle)
  {
    String columnGroup;
    if (_screenResult != null && _screenResult.getScreen().getScreenNumber().equals(screenNumber)) {
      columnGroup = OUR_DATA_COLUMNS_GROUP;
    }
    else {
      columnGroup = OTHER_DATA_COLUMNS_GROUP + TableColumnManager.GROUP_NODE_DELIMITER + screenNumber + " (" + screenTitle + ")";
    }
    return columnGroup;
  }

  private String makeStudyColumnGroup(Integer studyNumber, String studyTitle)
  {
    String columnGroup;
    if (_study != null && studyNumber.equals(_study.getStudyNumber())) {
      columnGroup = OUR_ANNOTATION_TYPES_COLUMN_GROUP;
    }
    else {
      columnGroup = OTHER_ANNOTATION_TYPES_COLUMN_GROUP + TableColumnManager.GROUP_NODE_DELIMITER + studyNumber + " (" + studyTitle + ")";
    }
    return columnGroup;
  }
  
  /**
   * @return a list of DataColumns that have RVs for plates in common with the wells of this search result, ordered by screen number and DataColumn ordinal
   */
  private List<Triple<DataColumn,Integer,String>> findDataColumnsForScreenResultsWithMutualPlates()
  {
    return _dao.runQuery(new Query() {
      public List<Triple<DataColumn,Integer,String>> execute(Session session)
      {
        HqlBuilder hql = new HqlBuilder();
        hql.distinctProjectionValues();
        // note: dataColumn->screenResult left fetch join eager fetches as a courtesy to caller, which needs it in impl of TableColumn.isSortableSearchable
        hql.from(DataColumn.class, "col").from("col", "screenResult", "sr", JoinType.LEFT_FETCH).from("sr", "screen", "s", JoinType.INNER);
        hql.select("col").select("s", "screenNumber").select("s", "title");
        hql.orderBy("s", "screenNumber").orderBy("col", "ordinal");

        if (_mode == WellSearchResultMode.SCREEN_RESULT_WELLS) {
          if (_screenResult == null) {
            return Collections.emptyList();
          }
          hql.from("sr", "plateNumbers", "p1", JoinType.INNER);
          hql.from(ScreenResult.class, "sr0").from("sr0", "plateNumbers", "p0", JoinType.INNER);
          hql.where("p0", Operator.EQUAL, "p1").where("sr0", _screenResult);
        }
        else if (_mode == WellSearchResultMode.LIBRARY_WELLS) {
          if (_library == null) {
            return Collections.emptyList();
          }
          hql.from("sr", "plateNumbers", "p", JoinType.INNER);
          hql.where("p", Operator.GREATER_THAN_EQUAL, _library.getStartPlate());
          hql.where("p", Operator.LESS_THAN_EQUAL, _library.getEndPlate());
        }
        else if (_mode == WellSearchResultMode.SET_OF_WELLS) {
          if (_plateNumbers.size() == 0) {
            return Collections.emptyList();
          }
          hql.from("sr", "plateNumbers", "p", JoinType.INNER);
          hql.whereIn("p", _plateNumbers);
        }
        else {
          // find all data columns, no additional HQL needed
        }
        if (log.isDebugEnabled()) {
          log.debug("findValidDataColumns query: " + hql.toHql());
        }
        org.hibernate.Query query = hql.toQuery(session, true);
        query.setResultTransformer(new MetaDataColumnResultTransformer<DataColumn>());
        List<Triple<DataColumn,Integer,String>> result = query.list();
        return result;
      }
    });
  }

  private void buildAnnotationTypeColumns(List<TableColumn<Well,?>> columns)
  {
    List<TableColumn<Well,?>> otherColumns = Lists.newArrayList();
    for (Triple<AnnotationType,Integer,String> atAndStudyNumberAndTitle : findValidAnnotationTypes()) {
      final AnnotationType annotationType = atAndStudyNumberAndTitle.getFirst();
      Integer studyNumber = atAndStudyNumberAndTitle.getSecond();
      String studyTitle = atAndStudyNumberAndTitle.getThird();
      TableColumn<Well,?> column;

      String columnGroup = makeStudyColumnGroup(studyNumber, studyTitle);
      if (annotationType.isNumeric()) {
        column = new RealEntityColumn<Well>(
          Well.reagents.to(Reagent.annotationValues).restrict(AnnotationValue.annotationType.getLeaf(), annotationType).toProperty("numericValue"),
          makeColumnName(annotationType, studyNumber),
          WellSearchResults.makeColumnDescription(annotationType, studyNumber, studyTitle),
          columnGroup,
          -1) {
          @Override
          public Double getCellValue(Well well)
          {
            // TODO: find appropriate version of reagent
            Reagent reagent = well.<Reagent>getLatestReleasedReagent();
            if (reagent != null) {
              AnnotationValue av = reagent.getAnnotationValues().get(annotationType);
              if (av != null) {
                return av.getNumericValue();
              }
            }
            return null;
          }
        };
      }
      else {
        column = new TextEntityColumn<Well>(
          Well.reagents.to(Reagent.annotationValues).restrict(AnnotationValue.annotationType.getLeaf(), annotationType).toProperty("value"),
          makeColumnName(annotationType, studyNumber),
          WellSearchResults.makeColumnDescription(annotationType, studyNumber, studyTitle),
          columnGroup) {
          @Override
          public String getCellValue(Well well)
          {
            // TODO: find appropriate version of reagent
            Reagent reagent = well.<Reagent>getLatestReleasedReagent();
            if (reagent != null) {
              AnnotationValue av = reagent.getAnnotationValues().get(annotationType);
              if (av != null) {
                return av.getValue();
              }
            }
            return null;
          }
        };
      }

      // request eager fetching of annotationType, since Hibernate will otherwise fetch these with individual SELECTs
      ((HasFetchPaths<Well>) column).addRelationshipPath(Well.reagents.to(Reagent.annotationValues).to(AnnotationValue.annotationType).to(AnnotationType.study));

      if (column.getName().equals(OUR_ANNOTATION_TYPES_COLUMN_GROUP)) {
        columns.add(column);
        column.setVisible(true);
      }
      else {
        otherColumns.add(column);
        column.setVisible(false);
      }
    }
    columns.addAll(otherColumns);
  }

  private List<Triple<AnnotationType,Integer,String>> findValidAnnotationTypes()
  {
    return _dao.runQuery(new Query() {
      public List<Triple<AnnotationType,Integer,String>> execute(Session session)
      {
        HqlBuilder hql = new HqlBuilder();
        hql.distinctProjectionValues();
        hql.from(AnnotationType.class, "at").from("at", "study", "s", JoinType.INNER);
        hql.select("at").select("s", "screenNumber").select("s", "title");
        hql.orderBy("s", "screenNumber").orderBy("at", "ordinal");

        if (_mode == WellSearchResultMode.SCREEN_RESULT_WELLS) {
          if (_screenResult == null) {
            return Collections.emptyList();
          }
          hql.where("s", "screenType", Operator.EQUAL, _screenResult.getScreen().getScreenType());
        }
        else if (_mode == WellSearchResultMode.STUDY_REAGENT_WELLS) {
          if (_reagentIds.size() == 0) {
            return Collections.emptyList();
          }
          // select annotation types for studies that have same screenType of the libraries containing the set of reagents in this search result
          hql.from(Library.class, "l").from("l", "wells", "w", JoinType.INNER).from("w", "reagent", "r", JoinType.INNER);
          hql.whereIn("r", "id", _reagentIds);
          hql.where("l", "screenType", Operator.EQUAL, "s", "screenType");
        }

        else if (_mode == WellSearchResultMode.LIBRARY_WELLS) {
          if (_library == null) {
            return Collections.emptyList();
          }
          hql.where("s", "screenType", Operator.EQUAL, _library.getScreenType());
        }
        else if (_mode == WellSearchResultMode.SET_OF_WELLS) {
          if (_plateNumbers.size() == 0) {
            return Collections.emptyList();
          }
          // select annotation types for studies that have same screenType of the libraries containing the set of wells in this search result
          hql.from(Library.class, "l");
          Disjunction librariesWhere = hql.disjunction();
          for (Integer plateNumber : _plateNumbers) {
            Conjunction libraryWhere = hql.conjunction();
            libraryWhere.add(hql.simplePredicate("l.startPlate", Operator.LESS_THAN_EQUAL, plateNumber));
            libraryWhere.add(hql.simplePredicate("l.endPlate", Operator.GREATER_THAN_EQUAL, plateNumber));
            libraryWhere.add(hql.simplePredicate("l.screenType", "s.screenType", Operator.EQUAL));
            librariesWhere.add(libraryWhere);
          }
          hql.where(librariesWhere);
        }
        else {
          // find all annotation types, no additional HQL needed
        }
        if (log.isDebugEnabled()) {
          log.debug("findValidAnnotationTypes query: " + hql.toHql());
        }
        org.hibernate.Query query = hql.toQuery(session, true);
        query.setResultTransformer(new MetaDataColumnResultTransformer<AnnotationType>());
        List<Triple<AnnotationType,Integer,String>> result = query.list();
        return result;
      }
    });
  }

  /**
   * Effects the replacement of the NoOpDataFetcher (set as the default search for 
   * users entering the page) so that the current search will return results.
   * 
   * @motivation to be tied to the actionListener for search field buttons.  
   *   
   * Sequence of a user search is:
   * <ol>
   * <li>User enters search patterns and submits (by pressing enter or the search button).</li>
   * <li>The Criteria field notifies the {@link Criterion} of the value change.  This triggers the chain of 
   * events that causes the table to refresh.  If the table has been initialized with a NoOpDataFetcher (the 
   * default), then no data is retrieved.</li>
   * <li>This action listener is invoked; it will call <code>initialize</code> on the parent {@link DataTable} class:</li>
   * <li>replaces the {@link NoOpDataFetcher} with a valid {@link DataFetcher},</li>
   * <li>uses the {@link TableColumn}s from the users search set with the proper {@link Criterion},</li>
   * <li>triggers execution of the search.</li>
   * </ol>
   * 
   * @see WellSearchResults#searchAllWells()
   */
  public void searchCommandListener(javax.faces.event.ActionEvent e)
  {
    // By looking at this flag, we can tell if the search is being done in some context or not.
    if (_mode == WellSearchResultMode.ALL_WELLS) {
      List<TableColumn<Well,?>> columns = new ArrayList<TableColumn<Well,?>>();
      columns.addAll(getColumnManager().getAllColumns());
      initialize(
        new AllEntitiesOfTypeDataFetcher<Well,String>(Well.class, _dao){
          @Override
          protected void addDomainRestrictions(HqlBuilder hql,
                                               Map<RelationshipPath<Well>,String> path2Alias)
          {
            super.addDomainRestrictions(hql, path2Alias);
            hql.from(getRootAlias(), "library", "l");
            hql.whereIn("l", "libraryType", LibrarySearchResults.LIBRARY_TYPES_TO_DISPLAY);
          }
        }, 
        columns);
    }
  }

  /**
   * Override resetFilter() in order to handle the "reset all" command in
   * "all wells" mode by resetting to an empty search result.
   */
  @UICommand
  public String resetFilter()
  {
    if (_mode == WellSearchResultMode.ALL_WELLS) {
      log.info("reverting to empty search result");
      initialize(new NoOpDataFetcher<Well,String,PropertyPath<Well>>());
    }
    return super.resetFilter();
  }
  
  @Override
  public String resetColumnFilter()
  {
    if (_mode == WellSearchResultMode.ALL_WELLS) {
      // if no other columns have criteria defined, reset to empty search result
      TableColumn<Well,?> resetColumn = (TableColumn<Well,?>) getRequestMap().get("column");
      boolean willHaveEmptyCriteria = true;
      for (TableColumn<Well,?> column : getColumnManager().getAllColumns()) {
        if (!column.getCriterion().isUndefined() && column != resetColumn) {
          willHaveEmptyCriteria = false;
          break;
        }
      }
      if (willHaveEmptyCriteria) {
        // reset to an empty search result
        log.info("all columns have empty criteria; reverting to empty search result");
        initialize(new NoOpDataFetcher<Well,String,PropertyPath<Well>>());
      }
    }
    return super.resetColumnFilter();
  }

  @Override
  @UICommand
  public String downloadSearchResults()
  {
    DataExporter<?> dataExporter = getDataExporterSelector().getSelection();
    if (dataExporter == _wellsSdfDataExporter) {
      _wellsSdfDataExporter.setLibraryContentsVersion(_libraryContentsVersion);
    }
    return super.downloadSearchResults();
  }
  
  static String makeColumnName(MetaDataType mdt, Integer parentIdentifier)
  {
    // note: replacing "_" with white space allows column name labels to wrap, creating narrower columns
    return String.format("%s [%d]", mdt.getName().replaceAll("_", " "), parentIdentifier);
  }

  static String makeColumnDescription(MetaDataType mdt, Integer parentIdentifier, String parentTitle)
  {

    return makeColumnDescription(mdt.getName(), mdt.getDescription(), parentIdentifier, parentTitle);
  }  
  
  static String makeColumnDescription(String name, String description, Integer parentIdentifier, String parentTitle)
  {
    StringBuilder columnDescription = new StringBuilder();
    columnDescription.append("<i>").append(parentIdentifier).append(": ").append(parentTitle).
    append("</i><br/><b>").append(name).append("</b>");
    if (description != null) {
      columnDescription.append(": ").append(description);
    }
    return columnDescription.toString();
  }
  
  class WellReagentEntityColumn<R extends Reagent,T> extends RelatedEntityColumn<Well,R,T> 
  {

    private WellSearchResults _wellSearchResults;

    public WellReagentEntityColumn(Class<R> reagentClass,
                                   TableColumn<R,T> delegateEntityColumn)
    {
      super(reagentClass, 
            (accessSpecificLibraryContentsVersion() ? Well.reagents.restrict(Reagent.libraryContentsVersion.getLeaf(), _libraryContentsVersion) : Well.latestReleasedReagent),
            delegateEntityColumn);
    }

    @Override
    protected R getRelatedEntity(Well well)
    {
      if (accessSpecificLibraryContentsVersion()) {
        return (R) well.getReagents().get(_libraryContentsVersion);
      }
      return (R) well.getLatestReleasedReagent();
    }
  }

  private boolean accessSpecificLibraryContentsVersion()
  {
    return _mode == WellSearchResultMode.LIBRARY_WELLS && _libraryContentsVersion != null;
  }

}
