// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.util.eutils;

import edu.harvard.med.screensaver.AbstractSpringTest;

/**
 * Test the {@link NCBIGeneInfoProviderImpl}.
 * <p>
 * WARNING: this test requires an internet connection.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class NCBIGeneInfoProviderTest extends AbstractSpringTest
{
  private NCBIGeneInfoProvider _geneInfoProvider = new NCBIGeneInfoProviderImpl();

  // Gnb4
  public void testGetGeneInfoForEntrezgeneId()
  {
    try {
      NCBIGeneInfo geneInfo = _geneInfoProvider.getGeneInfoForEntrezgeneId(14696);
      assertNotNull(geneInfo);
      assertEquals(geneInfo.getGeneName(), "guanine nucleotide binding protein (G protein), beta 4");
      assertEquals(geneInfo.getSpeciesName(), "Mus musculus");
      assertEquals(geneInfo.getEntrezgeneSymbol(), "Gnb4");
    }
    catch (EutilsException e) {
      fail("NCBIGeneInfoProviderImpl threw exception: " + e.getMessage());
    }
  }
}
