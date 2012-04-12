package edu.harvard.med.screensaver.rest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.io.libraries.smallmolecule.StructureImageLocator;
import edu.harvard.med.screensaver.model.cells.Cell;
import edu.harvard.med.screensaver.model.cells.ExperimentalCellInformation;
import edu.harvard.med.screensaver.model.screens.Screen;

public class CellConverter extends RestConverter {
	private static final Logger log = Logger.getLogger(CellConverter.class);

	@Autowired
	private LibrariesDAO librariesDao;

	@Autowired
	private StructureImageLocator structureImageLocator;

	public boolean canConvert(Class clazz) {
		return Cell.class.isAssignableFrom(clazz);
	}

	public void marshal(final Object value, final HierarchicalStreamWriter writer, MarshallingContext context) 
	{
		final XStreamUtil util = new XStreamUtil(writer, context, getEntityUriGenerator());
		getDao().doInTransaction(new DAOTransaction() {
			@Override
			public void runTransaction() {
				Cell cell = (Cell) value;
				cell = getDao().reloadEntity(cell);
				write(util, cell);
			}
		});
	}

	protected static void write(final XStreamUtil util, Cell cell) 
	{
		util.writeNode(cell.getFacilityId(), "facilityId");
		util.writeNode(cell.getName(), "name");
		util.writeNode(cell.getCloId(), "cloId" );
		util.writeNode(cell.getVendor(), "vendor" );
		util.writeNode(cell.getVendorCatalogId()	, "vendorCatalogId" );
		util.writeNode(cell.getBatchId(), "batchId" );
		util.writeNode(cell.getOrganism(), "organism" );
		util.writeNode(cell.getTissue(), "tissue" );
		util.writeNode(cell.getCellType(), "cellType" );
		util.writeNode(cell.getCellTypeDetail(), "cellTypeDetail" );
		util.writeNode(cell.getDisease(), "disease" );
		util.writeNode(cell.getDiseaseDetail(), "diseaseDetail" );
		util.writeNodes(cell.getGrowthProperties(), "growthProperties", "growthProperty" );
		util.writeNode(cell.getGeneticModification(), "geneticModification" );
		util.writeNodes(cell.getRelatedProjects(), "relatedProjects", "relatedProject" );
		util.writeNode(cell.getVerification(), "verification" );
		util.writeNode(cell.getVerificationReferenceProfile(), "verificationReferenceProfile" );
		util.writeNode(cell.getRecommendedCultureConditions(), "recommendedCultureConditions" );
		util.writeNode(cell.getMutationsReference(), "mutationsReference" );
		util.writeNode(cell.getMutationsExplicit(), "mutationsExplicit" );
		util.writeNode(cell.getOrganismGender(), "organismGender" );
		
    // TODO: make this write out the ExperimentalCellInformation when that data begins to be imported - sde4
		//util.writeNode(new EntityCollection<ExperimentalCellInformation>(ExperimentalCellInformation.class, cell.getExperimentalCellInformationSet()), "Experiments");
		util.writeNode(new EntityCollection<Screen>(Screen.class, 
				Lists.newArrayList(Iterators.transform(cell.getExperimentalCellInformationSet().iterator(), 
				new Function<ExperimentalCellInformation, Screen>(){

			@Override
			public Screen apply(ExperimentalCellInformation from) {
				return from.getScreen();
			}}))),  "Screens");
	}
 

private String facilityId;

	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		return null;
	}

}