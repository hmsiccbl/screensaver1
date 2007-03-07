// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.util.eutils;

import java.util.List;

import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.io.libraries.compound.OpenBabelClient;

/**
 * Test the {@link PubchemCidListProvider}.
 * <p>
 * WARNING: this test requires an internet connection.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class PubchemCidListProviderTest extends AbstractSpringTest
{
  private OpenBabelClient _openBabelClient = new OpenBabelClient();
  private PubchemCidListProvider _pubchemCidListProvider = new PubchemCidListProvider();

  public void testGetPubchemCidListForInchi1()
  {
    String inchi = _openBabelClient.convertSmilesToInchi("CC");
    List<String> pubchemCids = _pubchemCidListProvider.getPubchemCidListForInchi(inchi);
    assertEquals(1, pubchemCids.size());
    assertEquals("6324", pubchemCids.get(0));
  }

  public void testGetPubchemCidListForInchi2()
  {
    String inchi = _openBabelClient.convertSmilesToInchi("CCC");
    List<String> pubchemCids = _pubchemCidListProvider.getPubchemCidListForInchi(inchi);
    assertEquals(1, pubchemCids.size());
    assertEquals("6334", pubchemCids.get(0));
  }

  public void testGetPubchemCidListForInchi3()
  {
    String inchi = _openBabelClient.convertSmilesToInchi("CC(=O)C");
    List<String> pubchemCids = _pubchemCidListProvider.getPubchemCidListForInchi(inchi);
    assertEquals(1, pubchemCids.size());
    assertEquals("180", pubchemCids.get(0));
  }

  public void testGetPubchemCidListForInchi4()
  {
    String inchi = _openBabelClient.convertSmilesToInchi("O=C1CCCC=2OC(=O)C(=CC1=2)NC(=O)c3ccccc3");
    List<String> pubchemCids = _pubchemCidListProvider.getPubchemCidListForInchi(inchi);
    assertEquals(0, pubchemCids.size());
    //assertEquals("3822112", pubchemCids.get(0)); // used to return 3822112
  }

  public void testGetPubchemCidListForInchi5()
  {
    String inchi = _openBabelClient.convertSmilesToInchi("COc1ccc(cc1)N3N=C(C(=O)Oc2ccccc2)c4ccccc4(C3(=O))");
    List<String> pubchemCids = _pubchemCidListProvider.getPubchemCidListForInchi(inchi);
    assertEquals(1, pubchemCids.size());
    assertEquals("3595539", pubchemCids.get(0));
  }
  
  public void testGetPubchemCidListForInchi6()
  {
    String inchi = _openBabelClient.convertSmilesToInchi("CON=CNC(=O)c1cc(ccc1(OCC(F)(F)F))OCC(F)(F)F");
    List<String> pubchemCids = _pubchemCidListProvider.getPubchemCidListForInchi(inchi);
    assertEquals(0, pubchemCids.size());
  }
}
