// $HeadURL: http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/branches/serickson/3200/core/src/test/java/edu/harvard/med/screensaver/io/libraries/PlateWellListParserTest.java $
// $Id: PlateWellListParserTest.java 6946 2012-01-13 18:24:30Z seanderickson1 $
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.cells;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.io.ParseException;
import edu.harvard.med.screensaver.model.cells.Cell;
import edu.harvard.med.screensaver.model.cells.ExperimentalCellInformation;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.test.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.test.MakeDummyEntities;

public class CellImporterTest  extends AbstractSpringPersistenceTest
{
	
	String facilityId = "HMSL40001";
	//String type = "Cell Line";
	String name = "Test cell line";
	String cloid = "CLO_ID_1";
	
	String alternateName = "alt name 1";
	String alternateId = "alt ID 1";
	String centerName = "center name 1";
	String centerSpecificId = "CTR-1";
	
	String vendor = "small pharma";
	String vendorCatId = "xxx11111";
	String batchId = "111";
	String organism = "C. Elegans";
	
	String organ = "heart";
	
	String tissue = "tissue x";
	String cellType = "cell type xx";
	String disease = "no disease here";
	String[] growthProperties = { "growthProp1", "growthProp2", "growthProp3" };
	String geneticMod = "modificationx";
	String[] relatedProjects = { "proj1", "proj2", "proj3" };
	String verification = "ISB:1";
	String recommendedCultureCond = "salt lightly, stir";
	String mutations = "x,y, and z";
	String organismGender = "M";
	
	@Autowired
	protected CellParser cellContentsImporter;
	@Autowired
	protected ExperimentalCellInformationParser experimentalCellInformationParser;
	@Autowired
	protected GenericEntityDAO genericEntityDao;
	
	@Transactional
	public void testExperimentalInformationImporter() throws IOException, ParseException 
	{
			File file =(new ClassPathResource("/cells/cell_import_test.xls")).getFile();
			cellContentsImporter.load(file);
      Screen screen = MakeDummyEntities.makeDummyScreen(10001);
      genericEntityDao.persistEntity(screen);
  		flushAndClear();
      file =(new ClassPathResource("/cells/LINCS_ExperimentalCellInformation_test.xls")).getFile();
      experimentalCellInformationParser.load(file);
      flushAndClear();
  		Cell cell = genericEntityDao.findEntityByProperty(Cell.class, "facilityId", facilityId, true,Cell.experimentalCellInformationSetPath);
      assertNotNull(cell);
      Set<ExperimentalCellInformation> ecis = cell.getExperimentalCellInformationSet();
      assertNotNull(ecis);
      assertTrue(!ecis.isEmpty());
      assertEquals(1, ecis.size());
      ExperimentalCellInformation eci = ecis.iterator().next();
      assertEquals(cell, eci.getCell());
      assertEquals("10001", eci.getScreen().getFacilityId());
	}
	
	@Transactional
	public void testCellContentsImporter() throws ParseException, IOException
	{
		File file =(new ClassPathResource("/cells/cell_import_test.xls")).getFile();
		cellContentsImporter.load(file);
		flushAndClear();
		Cell cell = genericEntityDao.findEntityByProperty(Cell.class, "facilityId", facilityId);
		
		doCellTest(cell);
	}

	private void doCellTest(Cell cell) {
		assertNotNull(cell);
		assertEquals(facilityId, cell.getFacilityId());
		assertEquals(name, cell.getName());
		assertEquals(alternateName, cell.getAlternateName());
		assertEquals(alternateId, cell.getAlternateId());
		assertEquals(centerName, cell.getCenterName());
		assertEquals(centerSpecificId, cell.getCenterSpecificId());
		
		assertEquals(cloid, cell.getCloId());
		assertEquals(vendor, cell.getVendor());
		assertEquals(vendorCatId, cell.getVendorCatalogId());
		assertEquals(batchId, cell.getBatchId());
		assertEquals(organism, cell.getOrganism());
		
		assertEquals(organ, cell.getOrgan());
		
		assertEquals(tissue, cell.getTissue());
		assertEquals(cellType, cell.getCellType());
		assertEquals(disease, cell.getDisease());
		assertEquals(Sets.newTreeSet(Lists.newArrayList(growthProperties)), cell.getGrowthProperties());
		assertEquals(geneticMod, cell.getGeneticModification());
		assertEquals(Sets.newTreeSet(Lists.newArrayList(relatedProjects)), cell.getRelatedProjects());
		assertEquals(verification, cell.getVerification());
		assertEquals(recommendedCultureCond, cell.getRecommendedCultureConditions());
		assertEquals(mutations, cell.getMutations());
		assertEquals(organismGender, cell.getOrganismGender());
	}

}
