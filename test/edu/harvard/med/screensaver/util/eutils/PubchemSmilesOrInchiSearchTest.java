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

/**
 * Test the {@link PubchemCidListProvider}.
 * <p>
 * WARNING: this test requires an internet connection.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class PubchemSmilesOrInchiSearchTest extends AbstractSpringTest
{
  private PubchemSmilesOrInchiSearch _pubchemSmilesOrInchiSearch = new PubchemSmilesOrInchiSearch();

  public void testGetPubchemCidsForSmiles1()
  {
    try {
      List<String> pubchemCids = _pubchemSmilesOrInchiSearch.getPubchemCidsForSmilesOrInchi(
      "Clc1ccc(\\C=C/c2c(C)n(C)n(c3ccccc3)c2=O)c(Cl)c1");
      assertEquals(2, pubchemCids.size());
      assertTrue(pubchemCids.contains("1268921"));
      assertTrue(pubchemCids.contains("1268922"));
    }
    catch (EutilsException e) {
      fail("PubchemSmilesOrInchiSearch threw an exception: " + e.getMessage());
    }
  }

  public void testGetPubchemCidsForSmiles2()
  {
    try {
      List<String> pubchemCids = _pubchemSmilesOrInchiSearch.getPubchemCidsForSmilesOrInchi(
      "N#Cc1c(CN2CCN(C)CC2)n(C)c2ccccc12");
      assertEquals(1, pubchemCids.size());
      assertEquals("607443", pubchemCids.get(0));
    }
    catch (EutilsException e) {
      fail("PubchemSmilesOrInchiSearch threw an exception: " + e.getMessage());
    }
  }

  public void testGetPubchemCidsForSmiles3()
  {
    try {
      List<String> pubchemCids = _pubchemSmilesOrInchiSearch.getPubchemCidsForSmilesOrInchi(
      "NC(=S)c1cnc2ccccn2c1=N");
      assertEquals(1, pubchemCids.size());
      assertEquals("687414", pubchemCids.get(0));
    }
    catch (EutilsException e) {
      fail("PubchemSmilesOrInchiSearch threw an exception: " + e.getMessage());
    }
  }
  
  public void testGetPubchemCidsForSmiles4()
  {
    try {
      List<String> pubchemCids = _pubchemSmilesOrInchiSearch.getPubchemCidsForSmilesOrInchi(
      "CCOC(=O)C(C#N)C(=O)c1ccc(N)cc1");
      assertEquals(3, pubchemCids.size());
      assertTrue(pubchemCids.contains("577795"));
      assertTrue(pubchemCids.contains("684423"));
      assertTrue(pubchemCids.contains("684424"));
    }
    catch (EutilsException e) {
      fail("PubchemSmilesOrInchiSearch threw an exception: " + e.getMessage());
    }
  }
  
  public void testGetPubchemCidsForInvalidSmiles()
  {
    class MockedPubchemSmilesSearch extends PubchemSmilesOrInchiSearch
    {
      private boolean _gotDataOrServerError = false;
      public boolean gotDataOrServerError()
      {
        return _gotDataOrServerError;
      }
      public void reportError(String errorMessage)
      {
        if (errorMessage.startsWith("PUG server reported non-success status")) {
          _gotDataOrServerError = true;
        }
      }
    }
    try {
      MockedPubchemSmilesSearch mockedSearch = new MockedPubchemSmilesSearch();
      List<String> pubchemCids = mockedSearch.getPubchemCidsForSmilesOrInchi(
      "CCOC(=O)C(C#N)C(=O)c1ccc(N)cc1)"); // the dangling ')' is a actual encountered error
      assertNull(pubchemCids);
      assertTrue(mockedSearch.gotDataOrServerError());
    }
    catch (EutilsException e) {
      fail("PubchemSmilesOrInchiSearch threw an exception: " + e.getMessage());
    }
  }
}
