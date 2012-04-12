// $HeadURL: http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/branches/serickson/3200/web/src/main/java/edu/harvard/med/screensaver/ui/libraries/AnnotationSearchResults.java $
// $Id: AnnotationSearchResults.java 6949 2012-01-13 19:00:59Z seanderickson1 $

// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.

// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.cells;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.datafetcher.EntityDataFetcher;
import edu.harvard.med.screensaver.model.cells.Cell;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TableColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.IntegerEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.TextEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.TextSetEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.model.InMemoryEntityDataModel;
import edu.harvard.med.screensaver.ui.arch.searchresults.EntityBasedEntitySearchResults;

public class CellSearchResults extends EntityBasedEntitySearchResults<Cell, Integer> {
	private static final Logger log = Logger.getLogger(CellSearchResults.class);
	private GenericEntityDAO _dao;
	private CellViewer _cellViewer;

	/**
	 * @motivation for CGLIB2
	 */
	protected CellSearchResults() {
	}

	public CellSearchResults(CellViewer cellViewer, GenericEntityDAO dao) {
		super(cellViewer);

		_dao = dao;
		_cellViewer = cellViewer;
	}

	@Override
	public void initialize() {
		super.initialize();
	}

	public void searchAll() {
    setTitle("Cells");
		EntityDataFetcher<Cell, Integer> dataFetcher = (EntityDataFetcher<Cell, Integer>) new EntityDataFetcher<Cell, Integer>(
				Cell.class, _dao);
		initialize(new InMemoryEntityDataModel<Cell, Integer, Cell>(dataFetcher));

		getColumnManager().setSortAscending(true);
	}

	// implementations of the SearchResults abstract methods

	@Override
	protected List<TableColumn<Cell, ?>> buildColumns() {
		List<TableColumn<Cell, ?>> columns = Lists.newArrayList();

		// NOTE: fields updated to the DWG working group Cell Line Information release candidate document:
		//    - LINCS_DWG_CellLine_MetaData_ReleaseCandidate_Mar2012: 
		//    see: https://docs.google.com/spreadsheet/ccc?key=0AnqCZUjNY3AUdHFZRVRSeXBaZXZOSEVudUNXa29uNVE&hl=en_US#gid=0	
		columns.add(new TextEntityColumn<Cell>(RelationshipPath.from(Cell.class).toProperty("facilityId"), "Facility ID",
				"ID assigned to this cell by the HMS LINCS facility", TableColumn.UNGROUPED) {
			@Override
			public String getCellValue(Cell info) {
				return info.getFacilityId();
			}
			@SuppressWarnings("unchecked")
			@Override
			public Object cellAction(Cell info) {
				return _cellViewer.viewEntity(info);
			}

			@Override
			public boolean isCommandLink() {
				return true;
			}
		});
		// CL:1
		columns.get(columns.size() - 1).setVisible(true);
		columns.add(new TextEntityColumn<Cell>(RelationshipPath.from(Cell.class).toProperty("name"), "Cell Name",
				"The primary name for the cell line as chosen by LINCS", TableColumn.UNGROUPED) {
			@Override
			public String getCellValue(Cell info) {
				return info.getName();
			}
 		});
		columns.get(columns.size() - 1).setVisible(true);

		// CL:2 
		columns.add(new TextEntityColumn<Cell>(RelationshipPath.from(Cell.class).toProperty("cloId"), "CL ID",
				"Unique LINCS identifier", TableColumn.UNGROUPED) {
			@Override
			public String getCellValue(Cell info) {
				return info.getCloId();
			}
		});
		columns.get(columns.size() - 1).setVisible(false);
		
		// CL:3
		columns.add(new TextEntityColumn<Cell>(RelationshipPath.from(Cell.class).toProperty("alternateName"), "Alternate Name",
				"Other relevant names", TableColumn.UNGROUPED) {
			@Override
			public String getCellValue(Cell info) {
				return info.getAlternateName();
			}
		});
		columns.get(columns.size() - 1).setVisible(true);
		
		// CL:4
		columns.add(new TextEntityColumn<Cell>(RelationshipPath.from(Cell.class).toProperty("alternateId"), "Alternate ID",
				"Other relevant IDs for cell lines", TableColumn.UNGROUPED) {
			@Override
			public String getCellValue(Cell info) {
				return info.getAlternateId();
			}
		});
		columns.get(columns.size() - 1).setVisible(true);
		
		// CL:5
		columns.add(new TextEntityColumn<Cell>(RelationshipPath.from(Cell.class).toProperty("centerName"), "Center Name",
				"LINCS center using the cell line", TableColumn.UNGROUPED) {
			@Override
			public String getCellValue(Cell info) {
				return info.getCenterName();
			}
		});
		columns.get(columns.size() - 1).setVisible(false);
		
		// CL:6
		columns.add(new TextEntityColumn<Cell>(RelationshipPath.from(Cell.class).toProperty("centerSpecificId"), "Center Specific ID",
				"LINCS center specific ID", TableColumn.UNGROUPED) {
			@Override
			public String getCellValue(Cell info) {
				return info.getCenterSpecificId();
			}
		});
		columns.get(columns.size() - 1).setVisible(false); // We're hiding this field, and showing the HMS facility ID, which is the same thing, for our facility

		// CL:7
		columns.add(new TextEntityColumn<Cell>(RelationshipPath.from(Cell.class).toProperty("vendorName"), "Provider Name",
				"Name of vendor or lab(provider)  that supplied the cell line", TableColumn.UNGROUPED) {
			@Override
			public String getCellValue(Cell info) {
				return info.getVendor();
			}
		});
		columns.get(columns.size() - 1).setVisible(true);
		
		// CL:8
		columns.add(new TextEntityColumn<Cell>(RelationshipPath.from(Cell.class).toProperty("vendorCatalogId"), "Provider Catalog ID",
				"ID or catalogue number or name assigned to the cell line by the vendor or provider", TableColumn.UNGROUPED) {
			@Override
			public String getCellValue(Cell info) {
				return info.getVendorCatalogId();
			}
		});
		columns.get(columns.size() - 1).setVisible(true);
		
		// CL:9
		columns.add(new TextEntityColumn<Cell>(RelationshipPath.from(Cell.class).toProperty("batchId"), "Provider Batch ID",
				"Vendor/Provider Batch ID number; Batch or lot number assigned to the cell line by the vendor or provider", TableColumn.UNGROUPED) {
			@Override
			public String getCellValue(Cell info) {
				return info.getBatchId();
			}
		});
		columns.get(columns.size() - 1).setVisible(false);
		
		// CL:10 
		columns.add(new TextEntityColumn<Cell>(RelationshipPath.from(Cell.class).toProperty("organism"), "Organism",
				"Organism of origin; a controlled vocabulary describing the organism from which the cell line was derived (e.g. Homo sapiens, Mus musculus, etc.)", TableColumn.UNGROUPED) {
			@Override
			public String getCellValue(Cell info) {
				return info.getOrganism();
			}
		});
		
		//CL:11 
		columns.get(columns.size() - 1).setVisible(true);
		columns.add(new TextEntityColumn<Cell>(RelationshipPath.from(Cell.class).toProperty("organ"), "Organ",
				"Organ of origin; controlled terms describing the organ from which cell line is derived;  (e.g. lung, mammary gland etc.)", TableColumn.UNGROUPED) {
			@Override
			public String getCellValue(Cell info) {
				return info.getOrgan();
			}
		});
		columns.get(columns.size() - 1).setVisible(true);
		
		// CL:12
		columns.add(new TextEntityColumn<Cell>(RelationshipPath.from(Cell.class).toProperty("tissue"), "Tissue",
				"Tissue of origin; A controlled vocabulary describing the tissue from which the cell line was derived", TableColumn.UNGROUPED) {
			@Override
			public String getCellValue(Cell info) {
				return info.getTissue();
			}
		});
		
		// CL:13
		columns.get(columns.size() - 1).setVisible(true);
		columns.add(new TextEntityColumn<Cell>(RelationshipPath.from(Cell.class).toProperty("cellType"), "Cell Type",
				"A controlled vocabulary describing  the cell type from which a cell line was derived; e.g. epithelial like, fibroblast-like, lymphoblast like, hematopoetic, mesenchymal, neural, etc. This provides information about cell morphology.  Also sometimes referred to as cell morphology.", TableColumn.UNGROUPED) {
			@Override
			public String getCellValue(Cell info) {
				return info.getCellType();
			}
		});
		columns.get(columns.size() - 1).setVisible(true);
		
		// CL:14
		columns.get(columns.size() - 1).setVisible(true);
		columns.add(new TextEntityColumn<Cell>(RelationshipPath.from(Cell.class).toProperty("cellTypeDetail"), "Cell Type Detail",
				"Additional description of cell type (histology) that is not available in CL, but may be known from other sources like ATCC", TableColumn.UNGROUPED) {
			@Override
			public String getCellValue(Cell info) {
				return info.getCellTypeDetail();
			}
		});
		columns.get(columns.size() - 1).setVisible(true);
		
		// CL:15
		columns.add(new TextEntityColumn<Cell>(RelationshipPath.from(Cell.class).toProperty("disease"), "Disease",
				"If the cell line came from a particular diseased tissue, the disease should be noted in terms of a controlled vocabulary (e.g. breast cancer, colon cancer, not diseased, etc.)", TableColumn.UNGROUPED) {
			@Override
			public String getCellValue(Cell info) {
				return info.getDisease();
			}
		});
		columns.get(columns.size() - 1).setVisible(true);
		
		// CL:16
		columns.add(new TextEntityColumn<Cell>(RelationshipPath.from(Cell.class).toProperty("diseaseDetail"), "Disease Detail",
				" Additional description of a disease related to the cell line that may not be available in the disease ontology above", TableColumn.UNGROUPED) {
			@Override
			public String getCellValue(Cell info) {
				return info.getDiseaseDetail();
			}
		});
		columns.get(columns.size() - 1).setVisible(true);
		
		// CL:17
		columns.add(new TextSetEntityColumn<Cell>(RelationshipPath.from(Cell.class).toProperty("growthProperties"), "Growth Properties",
				"A controlled vocabulary describing the growth properties of the cell line (e.g. adherent, suspension)", TableColumn.UNGROUPED) {
			@Override
			public Set<String> getCellValue(Cell info) {
				return info.getGrowthProperties();
			}
		});

		// CL:18
		columns.add(new TextEntityColumn<Cell>(RelationshipPath.from(Cell.class).toProperty("geneticModificaton"), "Genetic Modification",
				"Stable transfection or viral transduction.  If yes, the modifications (e.g. expressing GFP-tagged protein) should be described and appropriate references provided. ", TableColumn.UNGROUPED) {
			@Override
			public String getCellValue(Cell info) {
				return info.getGeneticModification();
			}
		});
		columns.get(columns.size() - 1).setVisible(true);

		// CL:19
		columns.add(new TextSetEntityColumn<Cell>(RelationshipPath.from(Cell.class).toProperty("relatedProjects"), "Related Projects",
				"Other projects in which the cell line has been studied / used; A controlled vocabulary describing other large scale projects in which the cell line has been used (e.g. ENCODE, TCGA, ICBP, Epigenomics, etc.)", TableColumn.UNGROUPED) {
			@Override
			public Set<String> getCellValue(Cell info) {
				return info.getRelatedProjects();
			}
		});
		columns.get(columns.size() - 1).setVisible(false);

		// CL:20
		columns.add(new TextEntityColumn<Cell>(RelationshipPath.from(Cell.class).toProperty("recommendedCultureConditions"), "Recommended Culture Conditions",
				"A description of the standard tissue culture conditions (media, supplements, culture dish treatment) used to maintain the cell line.  Description of culture dish treatment conditions would include information about coating of culture dish with fibronectin, collagen, etc, prior to cell plating. If special culture vessels are required to grow the cells, these should also be mentioned and details provided.", TableColumn.UNGROUPED) {
			@Override
			public String getCellValue(Cell info) {
				return info.getRecommendedCultureConditions();
			}
		});
		columns.get(columns.size() - 1).setVisible(false);

		// CL:22
		columns.add(new TextEntityColumn<Cell>(RelationshipPath.from(Cell.class).toProperty("verification"), "Verification Profile",
				"Information pertaining to experimental verification of the cell line identity", TableColumn.UNGROUPED) {
			@Override
			public String getCellValue(Cell info) {
				return info.getVerification();
			}
		});
		
		columns.get(columns.size() - 1).setVisible(false);
		// CL:22
		columns.add(new TextEntityColumn<Cell>(RelationshipPath.from(Cell.class).toProperty("verificationReferenceProfile"), "Verification Reference Profile",
				"Information pertaining to experimental verification of the cell line identity", TableColumn.UNGROUPED) {
			@Override
			public String getCellValue(Cell info) {
				return info.getVerificationReferenceProfile();
			}
		});
		columns.get(columns.size() - 1).setVisible(false);

		// CL:23
		columns.add(new TextEntityColumn<Cell>(RelationshipPath.from(Cell.class).toProperty("mutationsReference"), "Mutations Reference",
				"Mutations inherent in certain cell lines; from a reference", TableColumn.UNGROUPED) {
			@Override
			public String getCellValue(Cell info) {
				return info.getMutationsReference();
			}
		});
		columns.get(columns.size() - 1).setVisible(false);

		// CL:24
		columns.add(new TextEntityColumn<Cell>(RelationshipPath.from(Cell.class).toProperty("mutationsExplicit"), "Mutations Explicit",
				"Mutations inherent in cell line, captured explicitly; e.g. if reference is not available", TableColumn.UNGROUPED) {
			@Override
			public String getCellValue(Cell info) {
				return info.getMutationsExplicit();
			}
		});
		columns.get(columns.size() - 1).setVisible(false);

		// CL:25
		columns.add(new TextEntityColumn<Cell>(RelationshipPath.from(Cell.class).toProperty("organismGender"), "Organism Gender",
				"Whether cell line was obtained from a male or female subject", TableColumn.UNGROUPED) {
			@Override
			public String getCellValue(Cell info) {
				return info.getOrganismGender();
			}
		});
		columns.get(columns.size() - 1).setVisible(true);

		return columns;
	}

}