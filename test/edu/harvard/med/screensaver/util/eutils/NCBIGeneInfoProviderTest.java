// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.util.eutils;

import junit.framework.TestCase;

import org.apache.log4j.Logger;


/**
 * Test the {@link NCBIGeneInfoProviderImpl}.
 * <p>
 * WARNING: this test requires an internet connection.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class NCBIGeneInfoProviderTest extends TestCase
{
  private static Logger log = Logger.getLogger(PubchemSmilesOrInchiSearchTest.class);
  
  private NCBIGeneInfoProvider _geneInfoProvider = new NCBIGeneInfoProviderImpl();

  // Gnb4
  public void testGetGeneInfoForEntrezgeneId()
  {
    try {
      long before = System.currentTimeMillis();
      int id = 14696;
      NCBIGeneInfo geneInfo = _geneInfoProvider.getGeneInfoForEntrezgeneId(id);
      log.info("query time: " + (System.currentTimeMillis()-before) + 
          ", entrezGeneId:  " + id + ", geneInfo: " + geneInfo);
      assertNotNull(geneInfo);
      assertEquals(geneInfo.getGeneName(), "guanine nucleotide binding protein (G protein), beta 4");
      assertEquals(geneInfo.getSpeciesName(), "Mus musculus");
      assertEquals(geneInfo.getEntrezgeneSymbol(), "Gnb4");

    
      before = System.currentTimeMillis();
      id = 22848;
      geneInfo = _geneInfoProvider.getGeneInfoForEntrezgeneId(id);
      log.info("query time: " + (System.currentTimeMillis()-before) + 
          ", entrezGeneId:  " + id + ", geneInfo: " + geneInfo);
      assertNotNull(geneInfo);
      assertEquals(geneInfo.getGeneName(), "AP2 associated kinase 1");
      assertEquals(geneInfo.getSpeciesName(), "Homo sapiens");
      assertEquals(geneInfo.getEntrezgeneSymbol(), "AAK1");
      
      before = System.currentTimeMillis();
      id = 9625;
      geneInfo = _geneInfoProvider.getGeneInfoForEntrezgeneId(id);
      log.info("query time: " + (System.currentTimeMillis()-before) + 
          ", entrezGeneId:  " + id + ", geneInfo: " + geneInfo);
      assertNotNull(geneInfo);
      assertEquals(geneInfo.getGeneName(), "apoptosis-associated tyrosine kinase");
      assertEquals(geneInfo.getSpeciesName(), "Homo sapiens");
      assertEquals(geneInfo.getEntrezgeneSymbol(), "AATK");
    }
    catch (EutilsException e) {
      fail("NCBIGeneInfoProviderImpl threw exception: " + e.getMessage());
    }
  }
}