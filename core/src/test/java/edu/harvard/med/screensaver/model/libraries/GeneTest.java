// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import junit.framework.TestSuite;
import com.google.common.collect.Sets;

import edu.harvard.med.screensaver.db.EntityInflator;
import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;

public class GeneTest extends AbstractEntityInstanceTest<Gene>
{
  public static TestSuite suite()
  {
    return buildTestSuite(GeneTest.class, Gene.class);
  }

  public GeneTest()
  {
    super(Gene.class);
  }
  
  public void testBuilderMethods()
  {
    // this creation code has been moved to the TestDataFactory
    //    Well well = dataFactory.newInstance(Well.class);
    //    SilencingReagent reagent = well.createSilencingReagent(dataFactory.newInstance(ReagentVendorIdentifier.class),
    //                                                           SilencingReagentType.SIRNA, "ACTG");
    //    Gene gene = dataFactory.newInstance(Gene.class);
    //    Gene gene = reagent.getFacilityGene()
    //    .withEntrezgeneId(1)
    //    .withGeneName("genename")
    //    .withSpeciesName("species")
    //    .withEntrezgeneSymbol("symbol1")
    //    .withEntrezgeneSymbol("symbol2")
    //    .withGenbankAccessionNumber("gbn1")
    //    .withGenbankAccessionNumber("gbn2");

    SilencingReagent reagent = dataFactory.newInstance(SilencingReagent.class);
    Gene gene = reagent.getFacilityGene();
    // Note: see TestDataFactory: SmallMoleculeReagent custom builder for these values
    assertEquals(new Integer(1), gene.getEntrezgeneId());
    assertEquals("genename", gene.getGeneName());
    assertEquals("species", gene.getSpeciesName());
    assertEquals(Sets.newHashSet("symbol1", "symbol2"), gene.getEntrezgeneSymbols());
    assertEquals(Sets.newHashSet("gbn1", "gbn2"), gene.getGenbankAccessionNumbers());
    
    genericEntityDao.mergeEntity(gene);
    gene = new EntityInflator<Gene>(genericEntityDao, gene, true).need(Gene.entrezgeneSymbols).need(Gene.genbankAccessionNumbers).inflate();
    
    assertEquals(new Integer(1), gene.getEntrezgeneId());
    assertEquals("genename", gene.getGeneName());
    assertEquals("species", gene.getSpeciesName());
    assertEquals(Sets.newHashSet("symbol1", "symbol2"), gene.getEntrezgeneSymbols());
    assertEquals(Sets.newHashSet("gbn1", "gbn2"), gene.getGenbankAccessionNumbers());
  }
}

