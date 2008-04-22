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
import edu.harvard.med.screensaver.db.datafetcher.DataFetcher;
import edu.harvard.med.screensaver.db.datafetcher.NoOpDataFetcher;
import edu.harvard.med.screensaver.db.datafetcher.ParentedEntityDataFetcher;
import edu.harvard.med.screensaver.io.cherrypicks.CherryPickRequestExporter;
import edu.harvard.med.screensaver.io.libraries.PlateWellListParser;
import edu.harvard.med.screensaver.io.libraries.PlateWellListParserResult;
import edu.harvard.med.screensaver.io.workbook.Workbook;
import edu.harvard.med.screensaver.io.workbook2.Workbook2Utils;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.PropertyPath;
import edu.harvard.med.screensaver.model.RelationshipPath;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickAssayPlate;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickLiquidTransfer;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickLiquidTransferStatus;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.InvalidCherryPickWellException;
import edu.harvard.med.screensaver.model.cherrypicks.LabCherryPick;
import edu.harvard.med.screensaver.model.cherrypicks.LegacyCherryPickAssayPlate;
import edu.harvard.med.screensaver.model.cherrypicks.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.ScreenerCherryPick;
import edu.harvard.med.screensaver.model.cherrypicks.LabCherryPick.LabCherryPickStatus;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.PlateType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellName;
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
import edu.harvard.med.screensaver.ui.libraries.WellCopyVolumeSearchResults;
import edu.harvard.med.screensaver.ui.screens.ScreenViewer;
import edu.harvard.med.screensaver.ui.searchresults.EntitySearchResults;
import edu.harvard.med.screensaver.ui.table.Criterion;
import edu.harvard.med.screensaver.ui.table.Criterion.Operator;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.EntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.EnumEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.IntegerEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.TextEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.VocabularyEntityColumn;
import edu.harvard.med.screensaver.ui.util.JSFUtils;
import edu.harvard.med.screensaver.ui.util.ScreensaverUserComparator;
import edu.harvard.med.screensaver.ui.util.UISelectOneBean;
import edu.harvard.med.screensaver.ui.util.UISelectOneEntityBean;
import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;

public class CherryPickRequestViewer extends AbstractBackingBean
{
  // static members

  private static Logger log = Logger.getLogger(CherryPickRequestViewer.class);

  private static final ScreensaverUserRole EDITING_ROLE = ScreensaverUserRole.CHERRY_PICK_ADMIN;

  private static final String VALIDATE_SELECTED_PLATES_FOR_LIQUID_TRANSFER = "for_liquid_transfer";
  private static final String VALIDATE_SELECTED_PLATES_FOR_DOWNLOAD = "for_download";
  private static final String VALIDATE_SELECTED_PLATES_FOR_DEALLOCATION = "for_deallocaton";

  private static final List<EntityColumn<ScreenerCherryPick,?>> SCREENER_CHERRY_PICKS_TABLE_COLUMNS = new ArrayList<EntityColumn<ScreenerCherryPick,?>>();
  private static final List<List<TableColumn<ScreenerCherryPick,?>>> SCREENER_CHERRY_PICKS_TABLE_COMPOUND_SORTS = new ArrayList<List<TableColumn<ScreenerCherryPick,?>>>();

  private static final String RNAI_COLUMNS_GROUP = "RNAi";
  private static final String SMALL_MOLECULE_COLUMNS_GROUP = "Small Molecule";
  
  static {
    SCREENER_CHERRY_PICKS_TABLE_COLUMNS.add(new IntegerEntityColumn<ScreenerCherryPick>(
      new PropertyPath<ScreenerCherryPick>(ScreenerCherryPick.class, "screenedWell", "plateNumber"),
      "Library Plate", "The library plate number of the well that was originally screened", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(ScreenerCherryPick scp) { return scp.getScreenedWell().getPlateNumber(); }
    });
    SCREENER_CHERRY_PICKS_TABLE_COLUMNS.add(new TextEntityColumn<ScreenerCherryPick>(
      new PropertyPath<ScreenerCherryPick>(ScreenerCherryPick.class, "screenedWell", "wellName"),
      "Screened Well", "The name of the well that was originally screened", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(ScreenerCherryPick scp) { return scp.getScreenedWell().getWellName(); }
    });
    SCREENER_CHERRY_PICKS_TABLE_COLUMNS.add(new IntegerEntityColumn<ScreenerCherryPick>(
      new RelationshipPath<ScreenerCherryPick>(ScreenerCherryPick.class, "labCherryPicks"),
      "Source Wells", "The number of wells to be cherry picked for the screened well", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(ScreenerCherryPick scp) { return scp.getLabCherryPicks().size(); }
    });
    SCREENER_CHERRY_PICKS_TABLE_COLUMNS.add(new TextEntityColumn<ScreenerCherryPick>(
      new PropertyPath<ScreenerCherryPick>(ScreenerCherryPick.class, "screenedWell", "simpleVendorIdentifier"),
      "Vendor ID", "The vendor ID of the screened well", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(ScreenerCherryPick scp) { return scp.getScreenedWell().getSimpleVendorIdentifier(); }
    });
    SCREENER_CHERRY_PICKS_TABLE_COLUMNS.add(new TextEntityColumn<ScreenerCherryPick>(
      new PropertyPath<ScreenerCherryPick>(ScreenerCherryPick.class, "screenedWell", "iccbNumber"),
      "ICCB Number", "The identifier assigned by ICCB-L to the contents of this well", SMALL_MOLECULE_COLUMNS_GROUP) {
      @Override
      public String getCellValue(ScreenerCherryPick scp)
      {
        return scp.getScreenedWell().getIccbNumber();
      }
    });
    SCREENER_CHERRY_PICKS_TABLE_COLUMNS.add(new TextEntityColumn<ScreenerCherryPick>(
      new PropertyPath<ScreenerCherryPick>(ScreenerCherryPick.class, "screenedWell.gene", "geneName"),
      "Gene", "The name of the gene targeted by the screened well", RNAI_COLUMNS_GROUP) {
      @Override
      public String getCellValue(ScreenerCherryPick scp)
      {
        Gene gene = scp.getScreenedWell().getGene();
        return gene == null ? null : gene.getGeneName();
      }
    });
    SCREENER_CHERRY_PICKS_TABLE_COLUMNS.add(new IntegerEntityColumn<ScreenerCherryPick>(
      new PropertyPath<ScreenerCherryPick>(ScreenerCherryPick.class, "screenedWell.gene", "entrezgeneId"),
      "Entrez ID", "The Entrez ID of the gene targeted by the screened well", RNAI_COLUMNS_GROUP) {
      @Override
      public Integer getCellValue(ScreenerCherryPick scp)
      {
        Gene gene = scp.getScreenedWell().getGene();
        return gene == null ? null : gene.getEntrezgeneId();
      }
    });
    SCREENER_CHERRY_PICKS_TABLE_COLUMNS.add(new TextEntityColumn<ScreenerCherryPick>(
      new PropertyPath<ScreenerCherryPick>(ScreenerCherryPick.class, "screenedWell.gene", "entrezgeneSymbol"),
      "Entrez Symbol", "The Entrez symbol of the gene targeted by the screened well", RNAI_COLUMNS_GROUP) {
      @Override
      public String getCellValue(ScreenerCherryPick scp)
      {
        Gene gene = scp.getScreenedWell().getGene();
        return gene == null ? null : gene.getEntrezgeneSymbol();
      }
    });
    SCREENER_CHERRY_PICKS_TABLE_COLUMNS.add(new TextEntityColumn<ScreenerCherryPick>(
      new PropertyPath<ScreenerCherryPick>(ScreenerCherryPick.class, "screenedWell", "genbankAccessionNumber"),
      "Genbank AccNo", "The Genbank accession number of the gene targeted by the screened well", RNAI_COLUMNS_GROUP) {
      @Override
      public String getCellValue(ScreenerCherryPick scp)
      {
        return scp.getScreenedWell().getGenbankAccessionNumber();
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

  private static final List<EntityColumn<LabCherryPick,?>> LAB_CHERRY_PICKS_TABLE_COLUMNS = new ArrayList<EntityColumn<LabCherryPick,?>>();
  private static final List<List<TableColumn<LabCherryPick,?>>> LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS = new ArrayList<List<TableColumn<LabCherryPick,?>>>();

  static {
    LAB_CHERRY_PICKS_TABLE_COLUMNS.add(new EnumEntityColumn<LabCherryPick,LabCherryPickStatus>(
      new RelationshipPath<LabCherryPick>(LabCherryPick.class, "wellVolumeAdjustments"),
      "Status", "Status",
      TableColumn.UNGROUPED, LabCherryPickStatus.values()) {
      @Override
      public LabCherryPickStatus getCellValue(LabCherryPick lcp)
      {
        return lcp.getStatus();
      }
    });
    LAB_CHERRY_PICKS_TABLE_COLUMNS.get(0).addRelationshipPath(new RelationshipPath<LabCherryPick>(LabCherryPick.class,
      "assayPlate.cherryPickLiquidTransfer"));
    LAB_CHERRY_PICKS_TABLE_COLUMNS.add(new IntegerEntityColumn<LabCherryPick>(
      new PropertyPath<LabCherryPick>(LabCherryPick.class, "sourceWell", "plateNumber"),
      "Library Plate", "The library plate number of the cherry picked well", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(LabCherryPick lcp) { return lcp.getSourceWell().getPlateNumber(); }
    });
    LAB_CHERRY_PICKS_TABLE_COLUMNS.add(new TextEntityColumn<LabCherryPick>(
      new PropertyPath<LabCherryPick>(LabCherryPick.class, "sourceWell", "wellName"),
      "Source Well", "The name of the cherry picked well", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(LabCherryPick lcp) { return lcp.getSourceWell().getWellName(); }
    });
    LAB_CHERRY_PICKS_TABLE_COLUMNS.add(new TextEntityColumn<LabCherryPick>(
      new RelationshipPath<LabCherryPick>(LabCherryPick.class, "wellVolumeAdjustments.copy"),
      "Source Copy", "The library plate copy of the cherry picked well", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(LabCherryPick lcp) { return lcp.getSourceCopy() != null ? lcp.getSourceCopy().getName() : ""; }
    });
    LAB_CHERRY_PICKS_TABLE_COLUMNS.add(new TextEntityColumn<LabCherryPick>(
      new PropertyPath<LabCherryPick>(LabCherryPick.class, "sourceWell", "simpleVendorIdentifier"),
      "Vendor ID", "The Vendor ID of the of the cherry picked well", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(LabCherryPick lcp) { return lcp.getSourceWell().getSimpleVendorIdentifier(); }
    });
    LAB_CHERRY_PICKS_TABLE_COLUMNS.add(new TextEntityColumn<LabCherryPick>(
      new PropertyPath<LabCherryPick>(LabCherryPick.class, "sourceWell", "iccbNumber"),
      "ICCB Number", "The identifier assigned by ICCB-L to the contents of this well", SMALL_MOLECULE_COLUMNS_GROUP) {
      @Override
      public String getCellValue(LabCherryPick lcp)
      {
        return lcp.getSourceWell().getIccbNumber();
      }
    });
    LAB_CHERRY_PICKS_TABLE_COLUMNS.add(new TextEntityColumn<LabCherryPick>(
      new PropertyPath<LabCherryPick>(LabCherryPick.class, "sourceWell.gene", "geneName"),
      "Gene", "The name of the gene targeted by the cherry picked well", RNAI_COLUMNS_GROUP) {
      @Override
      public String getCellValue(LabCherryPick lcp)
      {
        Gene gene = lcp.getSourceWell().getGene();
        return gene == null ? null : gene.getGeneName();
      }
    });
    LAB_CHERRY_PICKS_TABLE_COLUMNS.add(new IntegerEntityColumn<LabCherryPick>(
      new PropertyPath<LabCherryPick>(LabCherryPick.class, "sourceWell.gene", "entrezgeneId"),

      "Entrez ID", "The Entrez ID of the gene targeted by the cherry picked well", RNAI_COLUMNS_GROUP) {
      @Override
      public Integer getCellValue(LabCherryPick lcp)
      {
        Gene gene = lcp.getSourceWell().getGene();
        return gene == null ? null : gene.getEntrezgeneId();
      }
    });
    LAB_CHERRY_PICKS_TABLE_COLUMNS.add(new TextEntityColumn<LabCherryPick>(
      new PropertyPath<LabCherryPick>(LabCherryPick.class, "sourceWell.gene", "entrezgeneSymbol"),
      "Entrez Symbol", "The Entrez symbol of the gene targeted by the cherry picked well", RNAI_COLUMNS_GROUP) {
      @Override
      public String getCellValue(LabCherryPick lcp)
      {
        Gene gene = lcp.getSourceWell().getGene();
        return gene == null ? null : gene.getEntrezgeneSymbol();
      }
    });
    LAB_CHERRY_PICKS_TABLE_COLUMNS.add(new TextEntityColumn<LabCherryPick>(
      new PropertyPath<LabCherryPick>(LabCherryPick.class, "sourceWell", "genbankAccessionNumber"),
      "Genbank AccNo", "The Genbank accession number of the gene targeted by the cherry picked well", RNAI_COLUMNS_GROUP) {
      @Override
      public String getCellValue(LabCherryPick lcp)
      {
        return lcp.getSourceWell().getGenbankAccessionNumber();
      }
    });
    LAB_CHERRY_PICKS_TABLE_COLUMNS.add(new IntegerEntityColumn<LabCherryPick>(
      new PropertyPath<LabCherryPick>(LabCherryPick.class, "assayPlate", "plateOrdinal"),
      "Cherry Pick Plate #", "The cherry pick plate number that this cherry pick has been mapped to", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(LabCherryPick lcp) { return lcp.isMapped() ? new Integer(lcp.getAssayPlate().getPlateOrdinal() + 1) : null; }
    });
    LAB_CHERRY_PICKS_TABLE_COLUMNS.add(new IntegerEntityColumn<LabCherryPick>(
      new PropertyPath<LabCherryPick>(LabCherryPick.class, "assayPlate", "attemptOrdinal"),
      "Attempt #", "The attempt number of the cherry pick plate that this cherry pick has been mapped to", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(LabCherryPick lcp) { return lcp.isMapped() ? new Integer(lcp.getAssayPlate().getAttemptOrdinal() + 1) : null; }
    });
    LAB_CHERRY_PICKS_TABLE_COLUMNS.add(new TextEntityColumn<LabCherryPick>(
      new RelationshipPath<LabCherryPick>(LabCherryPick.class, "assayPlate"),
      "Destination Well", "The name of the well that this cherry pick has been mapped to", TableColumn.UNGROUPED) {
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
  private Date _dateOfLiquidTransfer = new Date();
  private String _liquidTransferComments;

  private EmptyWellsConverter _emptyWellsConverter;

  
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
    _isPanelCollapsedMap.put("screenerCherryPicks", true);
    _isPanelCollapsedMap.put("labCherryPicks", true);
    _isPanelCollapsedMap.put("cherryPickPlates", true);

    _labCherryPicksSearchResult = new EntitySearchResults<LabCherryPick,Integer>() {
      @Override
      protected List<? extends TableColumn<LabCherryPick,?>> buildColumns() { return LAB_CHERRY_PICKS_TABLE_COLUMNS; }
      
      @Override
      protected void setEntityToView(LabCherryPick entity) {}
    };
    // initialize LCP search result to initially respect 'show failed LCPs' checkbox 
    if (!_showFailedLabCherryPicks) {
      ((VocabularyEntityColumn) LAB_CHERRY_PICKS_TABLE_COLUMNS.get(0)).clearCriteria().addCriterion(new Criterion<LabCherryPickStatus>(Operator.NOT_EQUAL, LabCherryPickStatus.Failed));
    }

    _screenerCherryPicksSearchResult = new EntitySearchResults<ScreenerCherryPick,Integer>() {
      @Override
      protected List<? extends TableColumn<ScreenerCherryPick,?>> buildColumns() { return SCREENER_CHERRY_PICKS_TABLE_COLUMNS; }
      
      @Override
      protected void setEntityToView(ScreenerCherryPick entity) {}
    };
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
    
    _assayPlateType = new UISelectOneBean<PlateType>(Arrays.asList(PlateType.values()), _cherryPickRequest.getAssayPlateType()) {
      @Override
      protected String getLabel(PlateType plateType) { return plateType.getFullName(); }
    };

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

    _emptyWellsConverter = new EmptyWellsConverter();
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
                            "screenerCherryPicks.screenedWell.silencingReagents");
          _dao.needReadOnly(cherryPickRequest,
                            "screenerCherryPicks.screenedWell.compounds");
          _dao.needReadOnly(cherryPickRequest,
                            "labCherryPicks.assayPlate",
                            "labCherryPicks.sourceWell.silencingReagents",
                            "labCherryPicks.sourceWell.compounds",
                            "labCherryPicks.wellVolumeAdjustments.copy");
          _dao.needReadOnly(cherryPickRequest,
                            "cherryPickAssayPlates.labCherryPicks");

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
      
      _labCherryPicksSearchResult.initialize(buildCherryPicksDataFetcher(LabCherryPick.class));
      _labCherryPicksSearchResult.getColumnManager().addAllCompoundSorts(LAB_CHERRY_PICKS_TABLE_COMPOUND_SORTS);
      _labCherryPicksSearchResult.getColumnManager().setVisibilityOfColumnsInGroup(RNAI_COLUMNS_GROUP, _cherryPickRequest.getScreen().getScreenType() == ScreenType.RNAI);
      _labCherryPicksSearchResult.getColumnManager().setVisibilityOfColumnsInGroup(SMALL_MOLECULE_COLUMNS_GROUP, _cherryPickRequest.getScreen().getScreenType() == ScreenType.SMALL_MOLECULE);
      _labCherryPicksSearchResult.setCurrentScreensaverUser(getCurrentScreensaverUser());
      _labCherryPicksSearchResult.setMessages(getMessages());

      _screenerCherryPicksSearchResult.initialize(buildCherryPicksDataFetcher(ScreenerCherryPick.class));
      _screenerCherryPicksSearchResult.getColumnManager().addAllCompoundSorts(SCREENER_CHERRY_PICKS_TABLE_COMPOUND_SORTS);
      _screenerCherryPicksSearchResult.getColumnManager().setVisibilityOfColumnsInGroup(RNAI_COLUMNS_GROUP, _cherryPickRequest.getScreen().getScreenType() == ScreenType.RNAI);
      _screenerCherryPicksSearchResult.getColumnManager().setVisibilityOfColumnsInGroup(SMALL_MOLECULE_COLUMNS_GROUP, _cherryPickRequest.getScreen().getScreenType() == ScreenType.SMALL_MOLECULE);
      _screenerCherryPicksSearchResult.setCurrentScreensaverUser(getCurrentScreensaverUser());
      _screenerCherryPicksSearchResult.setMessages(getMessages());
      
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
    return _screenerCherryPicksSearchResult;
  }

  public EntitySearchResults<LabCherryPick,Integer> getLabCherryPicksSearchResult()
  {
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
      LAB_CHERRY_PICKS_TABLE_COLUMNS.get(0).clearCriteria();
      if (!showFailedLabCherryPicks) {
        ((VocabularyEntityColumn) LAB_CHERRY_PICKS_TABLE_COLUMNS.get(0)).addCriterion(new Criterion<LabCherryPickStatus>(Operator.NOT_EQUAL, LabCherryPickStatus.Failed));
      }
      _labCherryPicksSearchResult.refilter();
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


  // JSF listeners

  public void toggleShowFailedLabCherryPicks(ValueChangeEvent event)
  {
    setShowFailedLabCherryPicks((Boolean) event.getNewValue());
    // avoid having JSF set backing bean property with the submitted value
    ((UIInput) event.getComponent()).setLocalValueSet(false);
    // force regen of data model
    _labCherryPicksSearchResult.refilter();
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
    if (_cherryPickRequest.getMicroliterTransferVolumePerWellApproved() == null) {
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
  public String deallocateCherryPicksByPlate()
  {
    if (!validateSelectedAssayPlates(VALIDATE_SELECTED_PLATES_FOR_DEALLOCATION)) {
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    _cherryPickRequestAllocator.cancelAndDeallocateAssayPlates(_cherryPickRequest,
                                                               getSelectedAssayPlates(),
                                                               getLiquidTransferPerformedBy().getSelection(),
                                                               getDateOfLiquidTransfer(),
                                                               getLiquidTransferComments());
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
          newCherryPickRequest.setMicroliterTransferVolumePerWellApproved(cherryPickRequest.getMicroliterTransferVolumePerWellApproved());
          newCherryPickRequest.setMicroliterTransferVolumePerWellRequested(cherryPickRequest.getMicroliterTransferVolumePerWellRequested());
          newCherryPickRequest.setVolumeApprovedBy(cherryPickRequest.getVolumeApprovedBy());
          newCherryPickRequest.setDateVolumeApproved(cherryPickRequest.getDateVolumeApproved());
          newCherryPickRequest.setDateRequested(new Date());
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
  public String setEditMode()
  {
    _isEditMode = true;
    _dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        _dao.reattachEntity(_cherryPickRequest); // checks if up-to-date
      }
    });
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String cancelEdit() {
    // edits are discarded (and edit mode is canceled) by virtue of controller reloading the screen entity from the database
    return viewCherryPickRequest(_cherryPickRequest);
  }

  @UIControllerMethod
  public String save() {
    _isEditMode = false;

    _dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        _dao.reattachEntity(_cherryPickRequest);
        _cherryPickRequest.setAssayPlateType(_assayPlateType.getSelection());
        _cherryPickRequest.setRequestedBy(_requestedBy.getSelection());
        _cherryPickRequest.setVolumeApprovedBy(_volumeApprovedBy.getSelection());
      }
    });
    return VIEW_CHERRY_PICK_REQUEST_ACTION_RESULT;
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
        CherryPickLiquidTransfer liquidTransfer = 
          cherryPickRequest.getScreen().createCherryPickLiquidTransfer(performedBy,
                                                                       new Date(),
                                                                       dateOfLiquidTransfer,
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
          _dao.saveOrUpdateEntity(assayPlate);
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
    _wellCopyVolumesBrowser.searchWellsForCherryPickRequest(_cherryPickRequest, forUnfulfilledOnly);
    return VIEW_WELL_VOLUME_SEARCH_RESULTS;
  }
}
