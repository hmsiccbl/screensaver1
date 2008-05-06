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
 * WARNING: this test requires an internet connection.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class PubchemSmilesOrInchiStandardizerTest extends AbstractSpringTest
{
  private PubchemSmilesOrInchiStandardizer _pubchemSmilesOrInchiStandardizer = new PubchemSmilesOrInchiStandardizer();

  public void testStandardizeSmiles1()
  {
    try {
      String standardizedSmiles = _pubchemSmilesOrInchiStandardizer.getPubchemStandardizedSmilesOrInchi(
      "CCC", 
      CompoundIdType.SMILES);      
      assertEquals("CCC", standardizedSmiles);
    }
    catch (EutilsException e) {
      fail("PubchemSmilesOrInchiStandardizer threw an exception: " + e.getMessage());
    }
  }

  public void testStandardizeSmiles2()
  {
    try {
      String standardizedSmiles = _pubchemSmilesOrInchiStandardizer.getPubchemStandardizedSmilesOrInchi(
      "Clc1ccc(\\C=C/c2c(C)n(C)n(c3ccccc3)c2=O)c(Cl)c1", 
      CompoundIdType.SMILES);      
      assertEquals("CC1=C(C(=O)N(N1C)C2=CC=CC=C2)\\C=C/C3=C(C=C(C=C3)Cl)Cl", standardizedSmiles);
    }
    catch (EutilsException e) {
      fail("PubchemSmilesOrInchiStandardizer threw an exception: " + e.getMessage());
    }
  }

//  public void testGetPubchemCidsForInchi1()
//  {
//    try {
//      List<String> pubchemCids = _pubchemSmilesOrInchiSearch.getPubchemCidsForSmilesOrInchi(
//      "N#Cc1c(CN2CCN(C)CC2)n(C)c2ccccc12");
//      assertEquals(1, pubchemCids.size());
//      assertEquals("607443", pubchemCids.get(0));
//    }
//    catch (EutilsException e) {
//      fail("PubchemSmilesOrInchiSearch threw an exception: " + e.getMessage());
//    }
//  }
}
