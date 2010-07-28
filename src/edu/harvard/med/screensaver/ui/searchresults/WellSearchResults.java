// $HeadURL$
// $Id$

// Copyright © 2006, 2010 by the President and Fellows of Harvard College.

// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.db.Query;
import edu.harvard.med.screensaver.db.datafetcher.DataFetcher;
import edu.harvard.med.screensaver.db.datafetcher.DataFetcherUtil;
import edu.harvard.med.screensaver.db.datafetcher.NoOpDataFetcher;
import edu.harvard.med.screensaver.db.datafetcher.Tuple;
import edu.harvard.med.screensaver.db.datafetcher.TupleDataFetcher;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.db.hqlbuilder.JoinType;
import edu.harvard.med.screensaver.io.DataExporter;
import edu.harvard.med.screensaver.io.libraries.smallmolecule.LibraryContentsVersionReference;
import edu.harvard.med.screensaver.io.libraries.smallmolecule.StructureImageProvider;
import edu.harvard.med.screensaver.model.Entity;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryContentsVersion;
import edu.harvard.med.screensaver.model.libraries.LibraryWellType;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.AnnotationValue;
import edu.harvard.med.screensaver.model.screenresults.AssayWell;
import edu.harvard.med.screensaver.model.screenresults.AssayWellControlType;
import edu.harvard.med.screensaver.model.screenresults.DataColumn;
import edu.harvard.med.screensaver.model.screenresults.DataType;
import edu.harvard.med.screensaver.model.screenresults.PartitionedValue;
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
import edu.harvard.med.screensaver.ui.table.column.entity.BooleanTupleColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.EnumTupleColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.ImageTupleColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.IntegerSetTupleColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.IntegerTupleColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.RealTupleColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.TextSetTupleColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.TextTupleColumn;
import edu.harvard.med.screensaver.ui.table.model.DataTableModel;
import edu.harvard.med.screensaver.ui.table.model.VirtualPagingEntityDataModel;
import edu.harvard.med.screensaver.ui.util.UISelectOneBean;
import edu.harvard.med.screensaver.ui.util.ValueReference;
import edu.harvard.med.screensaver.util.Triple;

/**
 * A {@link SearchResults} for {@link Well Wells}.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class WellSearchResults extends TupleBasedEntitySearchResults<Well,String>
{
  private static final Logger log = Logger.getLogger(WellSearchResults.class);

  private static final String WELL_COLUMNS_GROUP = TableColumn.UNGROUPED;
  private static final String SILENCING_REAGENT_COLUMNS_GROUP = "Silencing Reagent";
  private static final String COMPOUND_COLUMNS_GROUP = "Compound";
  private static final String OUR_DATA_COLUMNS_GROUP = "Data Columns";
  private static final String OTHER_DATA_COLUMNS_GROUP = "Data Columns (Other Screen Results)";
  private static final String OTHER_ANNOTATION_TYPES_COLUMN_GROUP = "Annotations (Other Studies)";
  private static final String OUR_ANNOTATION_TYPES_COLUMN_GROUP = "Annotations";

  protected static final Function<DataColumn,Triple<DataColumn,Integer,String>> DataColumnToMetaDataColumn =
    new Function<DataColumn,Triple<DataColumn,Integer,String>>() {
      @Override
      public Triple<DataColumn,Integer,String> apply(DataColumn dataColumn)
      {
        return new Triple<DataColumn,Integer,String>(dataColumn, dataColumn.getScreenResult().getScreen().getScreenNumber(), dataColumn.getScreenResult().getScreen().getTitle());
      }
    };

  protected static final Function<AnnotationType,Triple<AnnotationType,Integer,String>> AnnotationTypeToMetaAnnotationType =
    new Function<AnnotationType,Triple<AnnotationType,Integer,String>>() {
      @Override
        public Triple<AnnotationType,Integer,String> apply(AnnotationType annotType)
        {
          return new Triple<AnnotationType,Integer,String>(annotType, annotType.getStudy().getScreenNumber(), annotType.getStudy().getTitle());
        }
    };

  private enum WellSearchResultMode {
    ALL_WELLS,
    SET_OF_WELLS,
    SCREEN_RESULT_WELLS,
    LIBRARY_WELLS,
    STUDY_REAGENT_WELLS,
    SET_OF_REAGENT_WELLS,
  };

  private GenericEntityDAO _dao;
  private LibrariesDAO _librariesDao;
  private EntityViewPolicy _entityViewPolicy;
  private LibraryViewer _libraryViewer;
  protected StructureImageProvider _structureImageProvider;
  private LibraryContentsVersionReference _libraryContentsVersionRef;

  private WellSearchResultMode _mode;
  private Set<ScreenType> _screenTypes;
  private ScreenResult _screenResult;
  private Study _study;


  public class RowsToFetchReference implements ValueReference<Integer>
  {
    @Override
    public Integer value()
    {
      return getRowsPerPage();
    }
  }

  /**
   * @motivation for CGLIB2
   */
  protected WellSearchResults()
  {}

  /**
   * Construct a new <code>WellSearchResultsViewer</code> object.
   */
  public WellSearchResults(GenericEntityDAO dao,
                           LibrariesDAO librariesDao,
                           EntityViewPolicy entityViewPolicy,
                           LibraryViewer libraryViewer,
                           WellViewer wellViewer,
                           StructureImageProvider structureImageProvider,
                           LibraryContentsVersionReference libraryContentsVersionRef,
                           List<DataExporter<Tuple<String>>> dataExporters)
  {
    super(Well.class, dao, dataExporters, wellViewer);
    _dao = dao;
    _librariesDao = librariesDao;
    _entityViewPolicy = entityViewPolicy;
    _libraryViewer = libraryViewer;
    _structureImageProvider = structureImageProvider;
    _libraryContentsVersionRef = libraryContentsVersionRef;
  }

  /**
   * Called from the top level menu page.
   * 
   * @motivation Initializes the DataTable with a NoOpDataFetcher; causing the bean
   *             to return an empty search result on the first viewing.
   * @see WellSearchResults#searchCommandListener(javax.faces.event.ActionEvent)
   */
  @Override
  public void searchAll()
  {
    setMode(WellSearchResultMode.ALL_WELLS);
    // initially, show an empty search result, but with all columns available
    initialize(buildColumns());

    // start with search panel open
    setTableFilterMode(true);
  }

  public void searchWellsForLibrary(final Library library)
  {
    setMode(WellSearchResultMode.LIBRARY_WELLS);
    _screenTypes = ImmutableSet.of(library.getScreenType());
    TupleDataFetcher<Well,String> dataFetcher = new TupleDataFetcher<Well,String>(Well.class, _dao) {
      @Override
      public void addDomainRestrictions(HqlBuilder hql)
      {
        DataFetcherUtil.addDomainRestrictions(hql, Well.library, library, getRootAlias());
      }
    };
    initialize(dataFetcher);

    // start with search panel closed
    setTableFilterMode(false);
  }

  public void searchWellsForLibraryContentsVersion(final LibraryContentsVersion lcv)
  {
    setMode(WellSearchResultMode.LIBRARY_WELLS);
    _screenTypes = ImmutableSet.of(lcv.getLibrary().getScreenType());
    _libraryContentsVersionRef.setValue(lcv);
    TupleDataFetcher<Well,String> dataFetcher = new TupleDataFetcher<Well,String>(Well.class, _dao) {
      @Override
      public void addDomainRestrictions(HqlBuilder hql)
      {
        DataFetcherUtil.addDomainRestrictions(hql, Well.library, lcv.getLibrary(), getRootAlias());
      }
    };
    initialize(dataFetcher);

    // start with search panel closed
    setTableFilterMode(false);
  }

  public void searchWellsForScreenResult(ScreenResult screenResult)
  {
    searchWellsForScreenResult(screenResult, false);
  }

  public void searchWellsForScreenResult(final ScreenResult screenResult, final boolean filterPositives)
  {
    setMode(WellSearchResultMode.SCREEN_RESULT_WELLS);
    _screenResult = screenResult;
    _screenTypes = ImmutableSet.of(_screenResult.getScreen().getScreenType());
    if (screenResult == null) {
      initialize();
    }
    else {
      TupleDataFetcher<Well,String> dataFetcher =
        new TupleDataFetcher<Well,String>(Well.class, _dao) {
          @Override
          public void addDomainRestrictions(HqlBuilder hql)
          {
            hql.from(AssayWell.class, "aw");
            hql.where(getRootAlias(), "wellId", Operator.EQUAL, "aw", "libraryWell");
            hql.where("aw", "screenResult", Operator.EQUAL, _screenResult);
            if (filterPositives) {
              hql.where("aw", "positive", Operator.EQUAL, Boolean.TRUE);
            }
        }
        };
      initialize(dataFetcher);

      // show columns for this screenResult's data columns
      for (DataColumn dataColumn : screenResult.getDataColumns()) {
        TableColumn<Tuple<String>,?> column = getColumnManager().getColumn(makeColumnName(dataColumn, screenResult.getScreen().getScreenNumber()));
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
    setMode(WellSearchResultMode.SET_OF_WELLS);
    final Set<String> wellKeyStrings = new HashSet<String>(wellKeys.size());
    for (WellKey wellKey : wellKeys) {
      wellKeyStrings.add(wellKey.toString());
      _screenTypes = _librariesDao.findScreenTypesForWells(wellKeys);
    }
    TupleDataFetcher<Well,String> dataFetcher =
      new TupleDataFetcher<Well,String>(Well.class, _dao) {
        @Override
        public void addDomainRestrictions(HqlBuilder hql)
        {
          DataFetcherUtil.addDomainRestrictions(hql, getRootAlias(), wellKeyStrings);
        }
      };
    initialize(dataFetcher);

    // start with search panel closed
    setTableFilterMode(false);

    // TODO: should report # of well keys not found
  }

  public void searchReagentsForStudy(final Study study)
  {
    setMode(WellSearchResultMode.STUDY_REAGENT_WELLS);
    _study = study;
    _screenTypes = Sets.newHashSet(_study.getScreenType());
    TupleDataFetcher<Well,String> dataFetcher =
      new TupleDataFetcher<Well,String>(Well.class, _dao) {
        @Override
        public void addDomainRestrictions(HqlBuilder hql)
        {
          DataFetcherUtil.addDomainRestrictions(hql,
                                                Well.reagents.to(Reagent.studies),
                                                study, getRootAlias());
        }
      };
    initialize(dataFetcher);

    getColumnManager().getColumn("Reagent Vendor").setVisible(true);
    getColumnManager().getColumn("Reagent ID").setVisible(true);
    // show columns for this screenResult's data columns
    for (AnnotationType at : study.getAnnotationTypes()) {
      getColumnManager().getColumn(WellSearchResults.makeColumnName(at, _study.getStudyNumber())).setVisible(true);
    }
  }

  public void searchReagents(final Set<String> reagentIds)
  {
    setMode(WellSearchResultMode.SET_OF_REAGENT_WELLS);
    _screenTypes = _librariesDao.findScreenTypesForReagents(reagentIds);
    TupleDataFetcher<Well,String> dataFetcher =
      new TupleDataFetcher<Well,String>(Well.class, _dao) {
        @Override
        public void addDomainRestrictions(HqlBuilder hql)
        {
          hql.from(getRootAlias(), "reagents", "r").whereIn("r", Reagent.vendorIdentifier.getPropertyName(), reagentIds);
        }
      };
    initialize(dataFetcher);
    getColumnManager().getColumn("Reagent Vendor").setVisible(true);
    getColumnManager().getColumn("Reagent ID").setVisible(true);

    // TODO: should report # of reagent identifiers not found
  }

  private void setMode(WellSearchResultMode mode)
  {
    _mode = mode;
    _study = null;
    _screenResult = null;
    _libraryContentsVersionRef.setValue(null);
    _screenTypes = Sets.newHashSet(ScreenType.values());
  }

  private void initialize(DataFetcher<Tuple<String>,String,PropertyPath<Well>> dataFetcher)
  {
    DataTableModel<Tuple<String>> dataModel =
      new VirtualPagingEntityDataModel<String,Well,Tuple<String>>(dataFetcher, new RowsToFetchReference());
    initialize(dataModel);
  }

  private void initialize(DataFetcher<Tuple<String>,String,PropertyPath<Well>> dataFetcher,
                          List<? extends TableColumn<Tuple<String>,?>> columns)
  {
    DataTableModel<Tuple<String>> dataModel =
      new VirtualPagingEntityDataModel<String,Well,Tuple<String>>(dataFetcher, new RowsToFetchReference());
    initialize(dataModel, columns);
  }

  @Override
  protected UISelectOneBean<Integer> buildRowsPerPageSelector()
  {
    UISelectOneBean<Integer> rowsPerPageSelector = super.buildRowsPerPageSelector();
    rowsPerPageSelector.deleteObserver(_rowsPerPageSelectorObserver);
    rowsPerPageSelector.setDomain(Lists.newArrayList(1, 12, 24, 48, 96, 384));
    rowsPerPageSelector.setSelectionIndex(1);
    rowsPerPageSelector.addObserver(_rowsPerPageSelectorObserver);
    return rowsPerPageSelector;
  }

  @Override
  protected List<? extends TableColumn<Tuple<String>,?>> buildColumns()
  {
    List<TableColumn<Tuple<String>,?>> columns = Lists.newArrayList();
    buildWellPropertyColumns(columns);
    buildReagentPropertyColumns(columns);
    buildCompoundPropertyColumns(columns);
    buildSilencingReagentPropertyColumns(columns);

    // Split the column list so that we can put the other data columns after the annotations 
    // (see: [#2266] command to auto-select data headers for positives from mutual positives screens,
    // wherein the annotation goes before the other data columns) - sde4
    List<DataColumn> dataColumns = Lists.newLinkedList();
    List<DataColumn> otherDataColumns = Lists.newLinkedList();
    Integer thisScreenNumber = _screenResult == null ? null : _screenResult.getScreen().getScreenNumber();
    for (DataColumn entry : findValidDataColumns()) {
      if (entry.getScreenResult().getScreen().getScreenNumber().equals(thisScreenNumber)) {
        dataColumns.add(entry);
      }
      else {
        otherDataColumns.add(entry);
      }
    }
    buildDataColumns(columns, dataColumns);
    buildAnnotationTypeColumns(columns);
    buildDataColumns(columns, otherDataColumns);
    return columns;
  }

  private void buildSilencingReagentPropertyColumns(List<TableColumn<Tuple<String>,?>> columns)
  {
    RelationshipPath<Well> relPath = getWellToReagentRelationshipPath();
    
    columns.add(new TextTupleColumn<Well,String>(
      relPath.to(SilencingReagent.facilityGene).toProperty("geneName"),
      "Gene Name",
      "The gene name for the silencing reagent in the well",
      SILENCING_REAGENT_COLUMNS_GROUP));
    columns.get(columns.size() - 1).setVisible(false);

    columns.add(new IntegerTupleColumn<Well,String>(
      relPath.to(SilencingReagent.facilityGene).toProperty("entrezgeneId"),
      "Entrez Gene ID",
      "The Entrez gene ID for the gene targeted by silencing reagent in the well",
      SILENCING_REAGENT_COLUMNS_GROUP));
    columns.get(columns.size() - 1).setVisible(false);

    columns.add(new EnumTupleColumn<Well,String,SilencingReagentType>(
      relPath.toProperty("silencingReagentType"),
      "Silencing Reagent Type",
      "The Silencing Reagent Type",
      SILENCING_REAGENT_COLUMNS_GROUP,
      SilencingReagentType.values()));
    columns.get(columns.size() - 1).setVisible(false);

    columns.add(new TextSetTupleColumn<Well,String>(relPath.to(SilencingReagent.facilityGene).to(Gene.entrezgeneSymbols),
                                                    "Entrez Gene Symbol",
                                                    "The Entrez gene symbol for the gene targeted by silencing reagent in the well",
                                                    SILENCING_REAGENT_COLUMNS_GROUP));
    columns.get(columns.size() - 1).setVisible(false);

    columns.add(new TextSetTupleColumn<Well,String>(relPath.to(SilencingReagent.facilityGene).to(Gene.genbankAccessionNumbers),
                                                    "Genbank Accession Numbers",
                                                    "The Genbank Accession Numbers for the gene targeted by silencing reagent in the well",
                                                    SILENCING_REAGENT_COLUMNS_GROUP));
    columns.get(columns.size() - 1).setVisible(false);

    columns.add(new TextTupleColumn<Well,String>(
      relPath.toProperty("sequence"),
      "Sequence",
      "The nucleotide sequence of this silencing reagent",
      SILENCING_REAGENT_COLUMNS_GROUP) {
      @Override
      public String getCellValue(Tuple<String> tuple)
      {
        // TODO: 'null' works, but this is not safe if policy view implementation changes! 
        if (!_entityViewPolicy.isAllowedAccessToSilencingReagentSequence(null)) {
          return null;
        }
        return super.getCellValue(tuple);
      }

    });
    columns.get(columns.size() - 1).setVisible(false);

    columns.add(new TextTupleColumn<Well,String>(
      relPath.to(SilencingReagent.facilityGene).toProperty("speciesName"),
      "Species",
      "The species of this silencing reagent",
      SILENCING_REAGENT_COLUMNS_GROUP));
    columns.get(columns.size() - 1).setVisible(false);

    columns.add(new TextSetTupleColumn<Well,String>(relPath.to(SilencingReagent.duplexWells).toProperty("wellId"),
                                                    "Duplex Wells",
                                                    "The duplex wells to which this pool well deconvolutes",
                                                    SILENCING_REAGENT_COLUMNS_GROUP));
    columns.get(columns.size() - 1).setVisible(false);
  }

  private RelationshipPath<Well> getWellToReagentRelationshipPath()
  {
    RelationshipPath<Well> relPath;
    if (accessSpecificLibraryContentsVersion()) {
      relPath = Well.reagents.restrict("libraryContentsVersion", _libraryContentsVersionRef.value());
    }
    else {
      relPath = Well.latestReleasedReagent;
    }
    return relPath;
  }

  private void buildCompoundPropertyColumns(List<TableColumn<Tuple<String>,?>> columns)
  {
    RelationshipPath<Well> relPath = getWellToReagentRelationshipPath();

    final PropertyPath<Well> reagentIdPropertyPath = relPath.toProperty("id");
    columns.add(new ImageTupleColumn<Well,String>(reagentIdPropertyPath,
                                                  "Compound Structure Image",
                                                  "The structure image for the compound in the well",
                                                  COMPOUND_COLUMNS_GROUP) {
      @Override
      public String getCellValue(Tuple<String> tuple)
      {
        if (_structureImageProvider != null) {
          Integer reagentId = (Integer) tuple.getProperty(TupleDataFetcher.makePropertyKey(reagentIdPropertyPath));
          if (reagentId != null) {
            SmallMoleculeReagent smallMoleculeReagent = _dao.findEntityById(SmallMoleculeReagent.class, reagentId, true, Reagent.well.getPath());
            URL url = _structureImageProvider.getImageUrl(smallMoleculeReagent);
            if (url != null) {
              return url.toString();
            }
          }
        }
        return null;
      }
    });
    columns.get(columns.size() - 1).setVisible(false);

    columns.add(new TextTupleColumn<Well,String>(relPath.toProperty("smiles"),
                                                 "Compound SMILES",
                                                 "The SMILES for the compound in the well",
                                                 COMPOUND_COLUMNS_GROUP));
    columns.get(columns.size() - 1).setVisible(false);

    columns.add(new TextTupleColumn<Well,String>(relPath.toProperty("inchi"),
      "Compound InChi",
      "The InChi for the compound in the well",
      COMPOUND_COLUMNS_GROUP));
    columns.get(columns.size() - 1).setVisible(false);

    columns.add(new TextSetTupleColumn<Well,String>(relPath.to(SmallMoleculeReagent.compoundNames),
                                                    "Compound Names",
                                                    "The names of the compound in the well",
                                                    COMPOUND_COLUMNS_GROUP));
    columns.get(columns.size() - 1).setVisible(false);

    columns.add(new IntegerSetTupleColumn<Well,String>(relPath.to(SmallMoleculeReagent.pubchemCids),
      "PubChem CIDs",
      "The PubChem CIDs of the compound in the well",
      COMPOUND_COLUMNS_GROUP));
    columns.get(columns.size() - 1).setVisible(false);

    columns.add(new IntegerSetTupleColumn<Well,String>(relPath.to(SmallMoleculeReagent.chembankIds),
      "ChemBank IDs",
      "The ChemBank IDs of the primary compound in the well",
      COMPOUND_COLUMNS_GROUP));
    columns.get(columns.size() - 1).setVisible(false);
  }

  private void buildReagentPropertyColumns(List<TableColumn<Tuple<String>,?>> columns)
  {
    RelationshipPath<Well> relPath = getWellToReagentRelationshipPath();

    columns.add(new TextTupleColumn<Well,String>(relPath.to(Reagent.vendorName),
                                                 "Reagent Vendor",
      "The vendor of the reagent in this well.",
      WELL_COLUMNS_GROUP));
    columns.add(new TextTupleColumn<Well,String>(relPath.to(Reagent.vendorIdentifier),
      "Reagent ID",
      "The vendor-assigned identifier for the reagent in this well.",
      WELL_COLUMNS_GROUP));
    columns.add(new IntegerTupleColumn<Well,String>(relPath.to(Reagent.libraryContentsVersion).toProperty("versionNumber"),
                                                    "Library Contents Version",
                                                    "The reagent's library contents version",
                                                    WELL_COLUMNS_GROUP));
    columns.get(columns.size() - 1).setVisible(false);
  }

  private void buildWellPropertyColumns(List<TableColumn<Tuple<String>,?>> columns)
  {
    columns.add(new IntegerTupleColumn<Well,String>(RelationshipPath.from(Well.class).toProperty("plateNumber"),
      "Plate",
      "The number of the plate the well is located on",
      WELL_COLUMNS_GROUP));

    columns.add(new TextTupleColumn<Well,String>(RelationshipPath.from(Well.class).toProperty("wellName"),
      "Well",
      "The plate coordinates of the well",
      WELL_COLUMNS_GROUP) {
      @Override
      public boolean isCommandLink()
      {
        return true;
      }

      @Override
      public Object cellAction(Tuple<String> tuple)
      {
        return viewSelectedEntity();
      }
    });

    columns.add(new TextTupleColumn<Well,String>(Well.library.toProperty("libraryName"),
      "Library",
      "The library containing the well",
      WELL_COLUMNS_GROUP) {
      @Override
      public boolean isCommandLink()
      {
        return true;
      }

      @Override
      public Object cellAction(Tuple<String> tuple)
      {
        Well well = _dao.findEntityById(Well.class, tuple.getKey(), true, Well.library.getPath());
        return _libraryViewer.viewEntity(well.getLibrary());
      }
    });

    columns.add(new TextTupleColumn<Well,String>(Well.library.toProperty("provider"),
                                                 "Provider",
                                                 "The vendor or source that provided the library",
                                                 WELL_COLUMNS_GROUP));
    columns.get(columns.size() - 1).setVisible(false);

    columns.add(new EnumTupleColumn<Well,String,ScreenType>(Well.library.toProperty("screenType"),
                                                            "Screen Type",
                                                            "The library screen type",
                                                            WELL_COLUMNS_GROUP,
                                                            ScreenType.values()));
    columns.add(new EnumTupleColumn<Well,String,LibraryWellType>(RelationshipPath.from(Well.class).toProperty("libraryWellType"),
                                                                 "Library Well Type",
                                                                 "The type of well, e.g., 'Experimental', 'Control', 'Empty', etc.",
                                                                 WELL_COLUMNS_GROUP,
                                                                 LibraryWellType.values()));

    columns.add(new TextTupleColumn<Well,String>(RelationshipPath.from(Well.class).toProperty("facilityId"),
      "Facility ID",
      "An alternate identifier assigned by the facility to identify this well",
      WELL_COLUMNS_GROUP));
    columns.get(columns.size() - 1).setVisible(false);

    columns.add(new BooleanTupleColumn<Well,String>(RelationshipPath.from(Well.class).toProperty("deprecated"),
                                                    "Deprecated",
                                                    "Whether the well has been deprecated",
                                                    TableColumn.UNGROUPED));
    columns.get(columns.size() - 1).setVisible(false);

    columns.add(new TextTupleColumn<Well,String>(Well.deprecationActivity.toProperty("comments"),
                                                 "Deprecation Reason",
                                                 "Why the well has been deprecated",
                                                 TableColumn.UNGROUPED));
    columns.get(columns.size() - 1).setVisible(false);
  }

  private void buildDataColumns(List<TableColumn<Tuple<String>,?>> columns, List<DataColumn> dataColumns)
  {
    List<TableColumn<Tuple<String>,?>> otherColumns = Lists.newArrayList();
    boolean isAssayWellTypeColumnCreated = false;

    for (final DataColumn dataColumn : dataColumns) {
      if (dataColumn.isRestricted()) {
        continue;
      }
      Integer screenNumber = dataColumn.getScreenResult().getScreen().getScreenNumber();
      String screenTitle = dataColumn.getScreenResult().getScreen().getTitle();

      TableColumn<Tuple<String>,?> column;

      if (dataColumn.isPartitionPositiveIndicator()) {
        column =
          new EnumTupleColumn<Well,String,PartitionedValue>(Well.resultValues.restrict(ResultValue.DataColumn.getLeaf(), dataColumn).toProperty("partitionedPositiveValue"),
                                                            makeColumnName(dataColumn, screenNumber),
                                                            makeColumnDescription(dataColumn, screenNumber, screenTitle, dataColumn.getDataType()),
                                                            makeScreenColumnGroup(screenNumber, screenTitle),
                                                            PartitionedValue.values());
      }
      else if (dataColumn.isBooleanPositiveIndicator()) {
        column = new BooleanTupleColumn<Well,String>(Well.resultValues.restrict(ResultValue.DataColumn.getLeaf(), dataColumn).toProperty("booleanPositiveValue"),
                                                     makeColumnName(dataColumn, screenNumber),
                                                     makeColumnDescription(dataColumn, screenNumber, screenTitle, dataColumn.getDataType()),
                                                     makeScreenColumnGroup(screenNumber, screenTitle));
      }
      else if (dataColumn.isNumeric()) {
        column = new RealTupleColumn<Well,String>(Well.resultValues.restrict(ResultValue.DataColumn.getLeaf(), dataColumn).toProperty("numericValue"),
                                                  makeColumnName(dataColumn, screenNumber),
                                                  makeColumnDescription(dataColumn, screenNumber, screenTitle, dataColumn.getDataType()),
                                                  makeScreenColumnGroup(screenNumber, screenTitle),
                                                  dataColumn.getDecimalPlaces());
      }
      else {
        column = new TextTupleColumn<Well,String>(Well.resultValues.restrict(ResultValue.DataColumn.getLeaf(), dataColumn).toProperty("value"),
                                                  makeColumnName(dataColumn, screenNumber),
                                                  makeColumnDescription(dataColumn, screenNumber, screenTitle, dataColumn.getDataType()),
                                                  makeScreenColumnGroup(screenNumber, screenTitle));
      }

      column = new ViewPolicyAwareResultValueColumn<Tuple<String>,Object>((TableColumn<Tuple<String>,Object>) column, dataColumn) {
        private String _positivePropertyKey;

        @Override
        protected boolean isResultValueRestricted(Tuple<String> tuple)
        {
          if (_entityViewPolicy.isAllowedAccessToDataColumnDueToMutualPositives(dataColumn)) {
            Boolean isPositive = (Boolean) tuple.getProperty(_positivePropertyKey);
            return !!!_entityViewPolicy.isAllowedAccessToResultValueDueToMutualPositive(isPositive == null ? false : isPositive.booleanValue(),
                                                                                        dataColumn.getScreenResult().getScreen(),
                                                                                        tuple.getKey());
          }
          else {
            assert _entityViewPolicy.visit(dataColumn);;
            return false;
          }
        }

        @Override
        protected void initialize()
        {
          if (_entityViewPolicy.isAllowedAccessToDataColumnDueToMutualPositives(dataColumn)) {
            PropertyPath<ResultValue> positiveProperty = getPropertyPath().getAncestryPath().toProperty("positive");
            _positivePropertyKey = TupleDataFetcher.makePropertyKey(positiveProperty);
            addRelationshipPath(positiveProperty);
            if (log.isDebugEnabled()) {
              log.debug("adding fetch path for ResultValue.isPositive, to calculate visibility of mutual positives for table column " +
                getName());
            }
          }
        }
      };

      // request eager fetching of dataColumn, since Hibernate will otherwise fetch these with individual SELECTs
      // we also need to eager fetch all the way "up" to Screen, for data access policy checks
      //((HasFetchPaths<Well>) column).addRelationshipPath(Well.resultValues.to(ResultValue.DataColumn).to(DataColumn.ScreenResult).to(ScreenResult.screen));

      if (!isAssayWellTypeColumnCreated && column.getGroup().equals(OUR_DATA_COLUMNS_GROUP)) {
        columns.add(new EnumTupleColumn<Well,String,AssayWellControlType>(Well.resultValues.restrict(ResultValue.DataColumn.getLeaf(), dataColumn).toProperty("assayWellControlType"),
                                                                          "Assay Well Control Type",
                                                                          "The type of assay well control",
                                                                          column.getGroup(),
                                                                          AssayWellControlType.values()));
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
      columnGroup = OTHER_DATA_COLUMNS_GROUP + TableColumnManager.GROUP_NODE_DELIMITER + screenNumber + " (" + screenTitle +
        ")";
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
      columnGroup = OTHER_ANNOTATION_TYPES_COLUMN_GROUP + TableColumnManager.GROUP_NODE_DELIMITER + studyNumber + " (" +
        studyTitle + ")";
    }
    return columnGroup;
  }

  private List<DataColumn> findValidDataColumns()
  {
    return _dao.runQuery(new Query() {
      public List<DataColumn> execute(Session session)
      {
        HqlBuilder hql = new HqlBuilder();
        hql.distinctProjectionValues().
        select("dc").
        from(DataColumn.class, "dc").
        from("dc", "screenResult", "sr", JoinType.LEFT_FETCH).
        from("sr", "screen", "s", JoinType.LEFT_FETCH).
        whereIn("s", "screenType", _screenTypes).
        orderBy("s", "screenNumber").orderBy("dc", "ordinal");
        if (log.isDebugEnabled()) {
          log.debug("findValidDataColumns query: " + hql.toHql());
        }
        Iterable<DataColumn> dataColumns = hql.toQuery(session, true).list();
        return Lists.newArrayList(Iterables.filter(dataColumns, Entity.NotRestricted));
      }
    });
  }

  private List<AnnotationType> findValidAnnotationTypes()
  {
    return _dao.runQuery(new Query() {
      public List<AnnotationType> execute(Session session)
      {
        HqlBuilder hql = new HqlBuilder();
        hql.select("at").distinctProjectionValues().
        from(AnnotationType.class, "at").from("at", "study", "s", JoinType.LEFT_FETCH).
        whereIn("s", "screenType", _screenTypes).
          orderBy("s", "screenNumber").orderBy("at", "ordinal");
        if (log.isDebugEnabled()) {
          log.debug("findValidAnnotationTypes query: " + hql.toHql());
        }
        Iterable<AnnotationType> annotTypes = hql.toQuery(session, true).list();
        return Lists.newArrayList(Iterables.filter(annotTypes, Entity.NotRestricted));
      }
    });
  }

  private void buildAnnotationTypeColumns(List<TableColumn<Tuple<String>,?>> columns)
  {
    List<TableColumn<Tuple<String>,?>> otherColumns = Lists.newArrayList();
    for (final AnnotationType annotationType : findValidAnnotationTypes()) {
      Integer studyNumber = annotationType.getStudy().getScreenNumber();
      String studyTitle = annotationType.getStudy().getTitle();
      TableColumn<Tuple<String>,?> column;

      String columnGroup = makeStudyColumnGroup(studyNumber, studyTitle);
      if (annotationType.isNumeric()) {
        // TODO: find appropriate version of reagent
        column = new RealTupleColumn<Well,String>(Well.latestReleasedReagent.to(Reagent.annotationValues).restrict(AnnotationValue.annotationType.getLeaf(), annotationType).toProperty("numericValue"),
                                                  makeColumnName(annotationType, studyNumber),
                                                  WellSearchResults.makeColumnDescription(annotationType, studyNumber, studyTitle, DataType.NUMERIC),
                                                  columnGroup,
                                                  -1);
      }
      else {
        column = new TextTupleColumn<Well,String>(Well.latestReleasedReagent.to(Reagent.annotationValues).restrict(AnnotationValue.annotationType.getLeaf(), annotationType).toProperty("value"),
                                                  makeColumnName(annotationType, studyNumber),
                                                  WellSearchResults.makeColumnDescription(annotationType, studyNumber, studyTitle, DataType.TEXT),
                                                  columnGroup);
      }

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

  /**
   * Effects the replacement of the NoOpDataFetcher (set as the default search for
   * users entering the page) so that the current search will return results.
   * 
   * @motivation to be tied to the actionListener for search field buttons.
   *             Sequence of a user search is:
   *             <ol>
   *             <li>User enters search patterns and submits (by pressing enter or the search button).</li>
   *             <li>The Criteria field notifies the {@link Criterion} of the value change. This triggers the chain of
   *             events that causes the table to refresh. If the table has been initialized with a NoOpDataFetcher (the
   *             default), then no data is retrieved.</li>
   *             <li>This action listener is invoked; it will call <code>initialize</code> on the parent
   *             {@link DataTable} class:</li>
   *             <li>replaces the {@link NoOpDataFetcher} with a valid {@link DataFetcher},</li>
   *             <li>uses the {@link TableColumn}s from the users search set with the proper {@link Criterion},</li>
   *             <li>triggers execution of the search.</li>
   *             </ol>
   * @see WellSearchResults#searchAll
   */
  public void searchCommandListener(javax.faces.event.ActionEvent e)
  {
    // By looking at this flag, we can tell if the search is being done in some context or not.
    if (_mode == WellSearchResultMode.ALL_WELLS) {
      TupleDataFetcher<Well,String> dataFetcher =
        new TupleDataFetcher<Well,String>(Well.class, _dao) {
        @Override
        public void addDomainRestrictions(HqlBuilder hql)
        {
          hql.from(getRootAlias(), "library", "l");
          hql.whereIn("l", "libraryType", LibrarySearchResults.LIBRARY_TYPES_TO_DISPLAY);
        }
        };
      initialize(dataFetcher, getColumnManager().getAllColumns());
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
      initialize();
    }
    return super.resetFilter();
  }

  @Override
  public String resetColumnFilter()
  {
    if (_mode == WellSearchResultMode.ALL_WELLS) {
      // if no other columns have criteria defined, reset to empty search result
      TableColumn<Tuple<String>,?> resetColumn = (TableColumn<Tuple<String>,?>) getRequestMap().get("column");
      boolean willHaveEmptyCriteria = true;
      for (TableColumn<Tuple<String>,?> column : getColumnManager().getAllColumns()) {
        if (!column.getCriterion().isUndefined() && column != resetColumn) {
          willHaveEmptyCriteria = false;
          break;
        }
      }
      if (willHaveEmptyCriteria) {
        // reset to an empty search result
        log.info("all columns have empty criteria; reverting to empty search result");
        initialize();
      }
    }
    return super.resetColumnFilter();
  }

  public static String makeColumnName(MetaDataType mdt, Integer parentIdentifier)
  {
    // note: replacing "_" with white space allows column name labels to wrap, creating narrower columns
    return String.format("%s [%d]", mdt.getName().replaceAll("_", " "), parentIdentifier);
  }

  static String makeColumnDescription(MetaDataType mdt, Integer parentIdentifier, String parentTitle, DataType dataType)
  {
    return makeColumnDescription(mdt.getName(), mdt.getDescription(), parentIdentifier, parentTitle, dataType);
  }

  static String makeColumnDescription(String name,
                                      String description,
                                      Integer parentIdentifier,
                                      String parentTitle,
                                      DataType dataType)
  {
    StringBuilder columnDescription = new StringBuilder();
    columnDescription.append("<i>").append(parentIdentifier).append(": ").append(parentTitle).
      append("</i><br/><b>").append(name).append("</b>");
    if (description != null) {
      columnDescription.append(": ").append(description);
    }
    switch (dataType) {
      case POSITIVE_INDICATOR_PARTITION:
        columnDescription.append("<div class=\"positivesDataColumnLegend\">Legend:<br/>S=Strong<br/>M=Medium<br/>W=Weak<br/>NP=Not a positive<br/>blank=No data</div>");
        break;
      case POSITIVE_INDICATOR_BOOLEAN:
        columnDescription.append("<div class=\"positivesDataColumnLegend\">Legend:<br/>true=Positive<br/>false=Not a positive<br/>blank=No data</div>");
        break;
    }
    return columnDescription.toString();
  }

  private boolean accessSpecificLibraryContentsVersion()
  {
    return _mode == WellSearchResultMode.LIBRARY_WELLS && _libraryContentsVersionRef.value() != null;
  }
}
