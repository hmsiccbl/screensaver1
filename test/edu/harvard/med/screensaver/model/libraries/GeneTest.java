// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import java.beans.IntrospectionException;

import junit.framework.TestSuite;

import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;
import edu.harvard.med.screensaver.model.EntityNetworkPersister;
import edu.harvard.med.screensaver.model.EntityNetworkPersister.EntityNetworkPersisterException;

import com.google.common.collect.Sets;

public class GeneTest extends AbstractEntityInstanceTest<Gene>
{
  public static TestSuite suite()
  {
    return buildTestSuite(GeneTest.class, Gene.class);
  }

  public GeneTest() throws IntrospectionException
  {
    super(Gene.class);
  }
  
  public void testBuilderMethods() throws EntityNetworkPersisterException
  {
    SilencingReagent reagent = dataFactory.getTestValueForType(SilencingReagent.class);
    Gene gene = reagent.getFacilityGene()
    .withEntrezgeneId(1)
    .withGeneName("genename")
    .withSpeciesName("species")
    .withEntrezgeneSymbol("symbol1")
    .withEntrezgeneSymbol("symbol2")
    .withGenbankAccessionNumber("gbn1")
    .withGenbankAccessionNumber("gbn2");

    assertEquals(new Integer(1), gene.getEntrezgeneId());
    assertEquals("genename", gene.getGeneName());
    assertEquals("species", gene.getSpeciesName());
    assertEquals(Sets.newHashSet("symbol1", "symbol2"), gene.getEntrezgeneSymbols());
    assertEquals(Sets.newHashSet("gbn1", "gbn2"), gene.getGenbankAccessionNumbers());

    
    new EntityNetworkPersister(genericEntityDao, gene).persistEntityNetwork();
    gene = genericEntityDao.findEntityById(Gene.class, gene.getEntityId(), true, "entrezgeneSymbols", "genbankAccessionNumbers");
    
    assertEquals(new Integer(1), gene.getEntrezgeneId());
    assertEquals("genename", gene.getGeneName());
    assertEquals("species", gene.getSpeciesName());
    assertEquals(Sets.newHashSet("symbol1", "symbol2"), gene.getEntrezgeneSymbols());
    assertEquals(Sets.newHashSet("gbn1", "gbn2"), gene.getGenbankAccessionNumbers());
  }
}

