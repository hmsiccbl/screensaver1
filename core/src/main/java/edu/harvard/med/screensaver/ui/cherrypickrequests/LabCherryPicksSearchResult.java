package edu.harvard.med.screensaver.ui.cherrypickrequests;

import static edu.harvard.med.screensaver.ui.cherrypickrequests.CherryPickRequestViewer.RNAI_COLUMNS_GROUP;
import static edu.harvard.med.screensaver.ui.cherrypickrequests.CherryPickRequestViewer.SMALL_MOLECULE_COLUMNS_GROUP;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.NoSuchEntityException;
import edu.harvard.med.screensaver.db.datafetcher.DataFetcherUtil;
import edu.harvard.med.screensaver.db.datafetcher.EntityDataFetcher;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickAssayPlate;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.LabCherryPick;
import edu.harvard.med.screensaver.model.cherrypicks.LabCherryPick.LabCherryPickStatus;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellCopy;
import edu.harvard.med.screensaver.model.libraries.WellVolumeAdjustment;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.service.cherrypicks.CherryPickRequestAllocator;
import edu.harvard.med.screensaver.service.libraries.WellCopyVolumeAdjuster;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TableColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.BooleanEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.EnumEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.HasFetchPaths;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.IntegerEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.TextEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.model.InMemoryEntityDataModel;
import edu.harvard.med.screensaver.ui.arch.searchresults.EntityBasedEntitySearchResults;
import edu.harvard.med.screensaver.util.NullSafeUtils;

public class LabCherryPicksSearchResult extends EntityBasedEntitySearchResults<LabCherryPick,Integer>
{
  private CherryPickRequest _cherryPickRequest;
  private CherryPickRequestAllocator _cherryPickRequestAllocator;
  private WellCopyVolumeAdjuster _wellCopyVolumeAdjuster;
  private GenericEntityDAO _dao;

  private String _labCherryPickSourceCopyUpdateComments;
  private boolean _isRecordOriginalSourceCopyWellsAsEmpty;

  protected LabCherryPicksSearchResult() {}
  
  public LabCherryPicksSearchResult(CherryPickRequestAllocator cherryPickRequestAllocator,
                                    WellCopyVolumeAdjuster wellCopyVolumeAdjuster,
                                    GenericEntityDAO dao)
  {
    _cherryPickRequestAllocator = cherryPickRequestAllocator;
    _wellCopyVolumeAdjuster = wellCopyVolumeAdjuster;
    _dao = dao;
  }
  
  @Override
  public void searchAll()
  {
    _cherryPickRequest = null;
    initialize();
  }

  public void searchForCherryPickRequest(CherryPickRequest cpr)
  {
    _cherryPickRequest = cpr;
    if (cpr == null) {
      initialize();
    }
    else {
      initialize(new InMemoryEntityDataModel<LabCherryPick,Integer,LabCherryPick>(new EntityDataFetcher<LabCherryPick,Integer>(LabCherryPick.class, _dao) {
        @Override
        public void addDomainRestrictions(HqlBuilder hql)
        {
          DataFetcherUtil.addDomainRestrictions(hql, RelationshipPath.from(LabCherryPick.class).to("cherryPickRequest"), _cherryPickRequest, getRootAlias());
        }
      }));
      getColumnManager().setVisibilityOfColumnsInGroup(RNAI_COLUMNS_GROUP, cpr.getScreen().getScreenType() == ScreenType.RNAI);
      getColumnManager().setVisibilityOfColumnsInGroup(SMALL_MOLECULE_COLUMNS_GROUP, cpr.getScreen().getScreenType() == ScreenType.SMALL_MOLECULE);
    }
    setEditingRole(ScreensaverUserRole.CHERRY_PICK_REQUESTS_ADMIN);
  }
  
  @Override
  public List<? extends TableColumn<LabCherryPick,?>> buildColumns()
  {
    List<TableColumn<LabCherryPick,?>> labCherryPicksTableColumns = buildLabCherryPicksTableColumns();
    getColumnManager().addAllCompoundSorts(buildLabCherryPicksTableCompoundSorts(labCherryPicksTableColumns));
    return labCherryPicksTableColumns;
  }

  private List<TableColumn<LabCherryPick,?>> buildLabCherryPicksTableColumns()
  {
    List<TableColumn<LabCherryPick,?>> labCherryPicksTableColumns = Lists.newArrayList();
    labCherryPicksTableColumns.add(new EnumEntityColumn<LabCherryPick,LabCherryPickStatus>(
      LabCherryPick.wellVolumeAdjustments,
      "Status", "Status",
      TableColumn.UNGROUPED, 
      LabCherryPickStatus.values()) {
      @Override
      public LabCherryPickStatus getCellValue(LabCherryPick lcp)
      {
        return lcp.getStatus();
      }
    });
    ((HasFetchPaths<LabCherryPick>) labCherryPicksTableColumns.get(0)).addRelationshipPath(LabCherryPick.assayPlate.to(CherryPickAssayPlate.cherryPickLiquidTransfer));
    labCherryPicksTableColumns.add(new TextEntityColumn<LabCherryPick>(
      LabCherryPick.sourceWell.to(Well.library).toProperty("shortName"),
      "Library Name", "The library name of the cherry picked well", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(LabCherryPick lcp) { return lcp.getSourceWell().getLibrary().getShortName(); }
    });
    labCherryPicksTableColumns.add(new IntegerEntityColumn<LabCherryPick>(
      LabCherryPick.sourceWell.toProperty("plateNumber"),
      "Library Plate", "The library plate number of the cherry picked well", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(LabCherryPick lcp) { return lcp.getSourceWell().getPlateNumber(); }
    });
    labCherryPicksTableColumns.add(new TextEntityColumn<LabCherryPick>(
      LabCherryPick.sourceWell.toProperty("wellName"),
      "Source Well", "The name of the cherry picked well", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(LabCherryPick lcp) { return lcp.getSourceWell().getWellName(); }
    });
    labCherryPicksTableColumns.add(new TextEntityColumn<LabCherryPick>(
      LabCherryPick.wellVolumeAdjustments.to(WellVolumeAdjustment.copy),
      "Source Copy", "The library plate copy of the cherry picked well", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(LabCherryPick lcp) { return lcp.getSourceCopy() != null ? lcp.getSourceCopy().getName() : ""; }

      @Override
      public void setCellValue(LabCherryPick lcp, String newCopyName)
      {
        lcpNewSourceCopies.put(lcp, newCopyName);
      }

      @Override
      public boolean isEditable()
      {
        return getScreensaverUser().isUserInRole(ScreensaverUserRole.CHERRY_PICK_REQUESTS_ADMIN);
      }
    });

    labCherryPicksTableColumns.add(new LabCherryPickReagentEntityColumn<Reagent,String>(
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

    labCherryPicksTableColumns.add(new LabCherryPickReagentEntityColumn<Reagent,String>(
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

    labCherryPicksTableColumns.add(new LabCherryPickReagentEntityColumn<SilencingReagent,String>(
      SilencingReagent.class,
      new TextEntityColumn<SilencingReagent>(
        SilencingReagent.facilityGene.toProperty("geneName"),
        "Gene", 
        "The name of the gene targeted by the screened well", 
        RNAI_COLUMNS_GROUP) {
        @Override
        public String getCellValue(SilencingReagent r)
        {
          Gene gene = r.getFacilityGene();
          return gene == null ? null : gene.getGeneName();
        }
      }));
    labCherryPicksTableColumns.add(new LabCherryPickReagentEntityColumn<SilencingReagent,Integer>(
      SilencingReagent.class,
      new IntegerEntityColumn<SilencingReagent>(
        SilencingReagent.facilityGene.toProperty("entrezgeneId"),
        "Entrez ID", 
        "The Entrez ID of the gene targeted by the screened well", 
        RNAI_COLUMNS_GROUP) {
        @Override
        public Integer getCellValue(SilencingReagent r)
        {
          Gene gene = r.getFacilityGene();
          return gene == null ? null : gene.getEntrezgeneId();
        }
      }));
    labCherryPicksTableColumns.add(new LabCherryPickReagentEntityColumn<SilencingReagent,String>(SilencingReagent.class,
                                                                                                 new TextEntityColumn<SilencingReagent>(SilencingReagent.facilityGene.to(Gene.entrezgeneSymbols),
        "Entrez Symbol", 
        "The Entrez symbol of the gene targeted by the screened well", 
        RNAI_COLUMNS_GROUP) {
        @Override
        public String getCellValue(SilencingReagent r)
        {
          Gene gene = r.getFacilityGene();
          // TODO: not appropriate to show single, arbitrary entrezgene symbol
          return gene == null ? null : gene.getEntrezgeneSymbols().isEmpty() ? null : gene.getEntrezgeneSymbols().iterator().next();
        }
      }));
    labCherryPicksTableColumns.add(new LabCherryPickReagentEntityColumn<SilencingReagent,String>(SilencingReagent.class,
                                                                                                 new TextEntityColumn<SilencingReagent>(SilencingReagent.facilityGene.to(Gene.genbankAccessionNumbers),
        "Genbank AccNo", 
        "The Genbank accession number of the gene targeted by the screened well",
        RNAI_COLUMNS_GROUP) {
        @Override
        public String getCellValue(SilencingReagent r)
        {
          Gene gene = r.getFacilityGene();
          // TODO: not appropriate to show single, arbitrary genbank acc no
          return gene == null ? null : gene.getGenbankAccessionNumbers().isEmpty() ? null : gene.getGenbankAccessionNumbers().iterator().next();
        }
      }));

    labCherryPicksTableColumns.add(new LabCherryPickReagentEntityColumn<SilencingReagent,String>(SilencingReagent.class,
                                                                                                 new TextEntityColumn<SilencingReagent>(RelationshipPath.from(SilencingReagent.class).toProperty("sequence"),
                                                                                                                                        "Sequence",
                                                                                                                                        "The nucleotide sequence of this silencing reagent",
                                                                                                                                        RNAI_COLUMNS_GROUP) {
      @Override
      public String getCellValue(SilencingReagent r)
      {
        // note: we do not need to check EntityViewPolicy.isAllowedAccessToSilencingReagentSequence, since screeners are allowed access to the sequences they have CP'd, and screeners can only see their own CPR LCPs
        return r.getSequence();
      }
    }));
    
    labCherryPicksTableColumns.add(new BooleanEntityColumn<LabCherryPick>(LabCherryPick.sourceWell.toProperty("deprecated"),
                                                                          "Deprecated",
                                                                          "Whether the cherry picked well has been deprecated (and should not have been cherry picked)",
                                                                          TableColumn.UNGROUPED) {
      @Override
      public Boolean getCellValue(LabCherryPick lcp)
      {
        return lcp.getSourceWell().isDeprecated();
      }
    });
    labCherryPicksTableColumns.get(labCherryPicksTableColumns.size() - 1).setVisible(false);
    labCherryPicksTableColumns.add(new TextEntityColumn<LabCherryPick>(LabCherryPick.sourceWell.to(Well.deprecationActivity).toProperty("comments"),
                                                                       "Deprecation Reason",
                                                                       "Why the cherry picked well has been deprecated (and should not have been cherry picked)",
                                                                       TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(LabCherryPick lcp)
      {
        return lcp.getSourceWell().isDeprecated() ? lcp.getSourceWell().getDeprecationActivity().getComments() : null;
      }
    });
    labCherryPicksTableColumns.get(labCherryPicksTableColumns.size() - 1).setVisible(false);
    labCherryPicksTableColumns.add(new IntegerEntityColumn<LabCherryPick>(
      LabCherryPick.assayPlate.toProperty("plateOrdinal"),
      "Cherry Pick Plate #", "The cherry pick plate number that this cherry pick has been mapped to", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(LabCherryPick lcp) { return lcp.isMapped() ? new Integer(lcp.getAssayPlate().getPlateOrdinal() + 1) : null; }
    });
    labCherryPicksTableColumns.add(new IntegerEntityColumn<LabCherryPick>(
      LabCherryPick.assayPlate.toProperty("attemptOrdinal"), 
      "Attempt #", "The attempt number of the cherry pick plate that this cherry pick has been mapped to", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(LabCherryPick lcp) { return lcp.isMapped() ? new Integer(lcp.getAssayPlate().getAttemptOrdinal() + 1) : null; }
    });
    labCherryPicksTableColumns.add(new TextEntityColumn<LabCherryPick>(
      LabCherryPick.assayPlate,
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

  Map<LabCherryPick,String> lcpNewSourceCopies = Maps.newHashMap();
  
  @Override
  protected void doEdit()
  {
    lcpNewSourceCopies = Maps.newHashMap();
  }

  @Override
  protected void doSave()
  {
    try {
      // TODO: make the following two operations atomic
      _wellCopyVolumeAdjuster.setWellCopyVolumesToEmpty((AdministratorUser) getScreensaverUser(),
                                                        getUserEditedLabCherryPickSourceWellCopies(),
                                                        getLabCherryPickSourceCopyUpdateComments());
                                                    
      _cherryPickRequestAllocator.allocate(lcpNewSourceCopies,
                                           _cherryPickRequest,
                                           (AdministratorUser) getScreensaverUser(),
                                           getLabCherryPickSourceCopyUpdateComments());
      
    }
    catch (NoSuchEntityException e) {
      showMessage("libraries.noSuchCopyForPlate", e.getPropertyValues().get("copy"), e.getPropertyValues().get("plate").toString());
    }
    if (getNestedIn() != null) {
      getNestedIn().reload();
    }
  }

  private Set<WellCopy> getUserEditedLabCherryPickSourceWellCopies()
  {
    Set<WellCopy> wellCopies = Sets.newHashSet();
    for (Map.Entry<LabCherryPick,String> lcpNewSourceCopy : lcpNewSourceCopies.entrySet()) {
      LabCherryPick lcp = _dao.reloadEntity(lcpNewSourceCopy.getKey(), true, LabCherryPick.wellVolumeAdjustments.to(WellVolumeAdjustment.copy).to(Copy.plates));
      if (lcp.getSourceCopy() != null &&
        !NullSafeUtils.nullSafeEquals(lcp.getSourceCopy().getName(), lcpNewSourceCopy.getValue())) {
        wellCopies.add(new WellCopy(lcp.getSourceWell(), lcp.getSourceCopy()));
      }
    }
    return wellCopies;
  }

  public String getLabCherryPickSourceCopyUpdateComments()
  {
    return _labCherryPickSourceCopyUpdateComments;
  }

  public void setLabCherryPickSourceCopyUpdateComments(String labCherryPickSourceCopyUpdateComments)
  {
    _labCherryPickSourceCopyUpdateComments = labCherryPickSourceCopyUpdateComments;
  }

  public boolean isRecordOriginalSourceCopyWellsAsEmpty()
  {
    return _isRecordOriginalSourceCopyWellsAsEmpty;
  }

  public void setRecordOriginalSourceCopyWellsAsEmpty(boolean isRecordOriginalSourceCopyWellsAsEmpty)
  {
    _isRecordOriginalSourceCopyWellsAsEmpty = isRecordOriginalSourceCopyWellsAsEmpty;
  }
}
