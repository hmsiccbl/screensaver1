// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.soaputils;

import java.util.List;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;

/**
 * WARNING: this test requires an internet connection.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class PugSoapUtilTest extends TestCase
{
  private static Logger log = Logger.getLogger(PugSoapUtilTest.class);

  public void testGetPubchemCidsForSmiles1()
  {
    String smiles = "Clc1ccc(\\C=C/c2c(C)n(C)n(c3ccccc3)c2=O)c(Cl)c1";
    try {
      long before = System.currentTimeMillis();
      int[] cids = PugSoapUtil.identitySearch(smiles);
      List<String> pubchemCids = Lists.newLinkedList();
      for(int cid:cids) pubchemCids.add(""+cid);
      log.info("query time: " + (System.currentTimeMillis()-before) + 
          ", smiles:  " + smiles  + ": " + pubchemCids);
      assertEquals(2, pubchemCids.size());
      assertTrue(pubchemCids.contains("1268921"));
      assertTrue(pubchemCids.contains("1268922"));
      
      smiles = "O=C1CC(C)(C)CC(=O)C1C(c1ccccc1)C1=C(O)CC(C)(C)CC1=O";
      before = System.currentTimeMillis();
      cids = PugSoapUtil.identitySearch(smiles);
      pubchemCids = Lists.newLinkedList();
      for(int cid:cids) pubchemCids.add(""+cid);
      log.info("query time: " + (System.currentTimeMillis()-before) + 
          ", smiles:  " + smiles  + ": " + pubchemCids);
      assertEquals(2, pubchemCids.size());
      assertTrue(pubchemCids.contains("558309"));
      assertTrue(pubchemCids.contains("7335957"));
      
    }
    catch (Exception e) {
      fail("exception: " + e.getMessage());
    }
  }

  public void testGetPubchemCidsForSmiles2()
  {
    try {
      int[] cids = PugSoapUtil.identitySearch("N#Cc1c(CN2CCN(C)CC2)n(C)c2ccccc12");
      List<String> pubchemCids = Lists.newLinkedList();
      for(int cid:cids) pubchemCids.add(""+cid);
      
      assertEquals(1, pubchemCids.size());
      assertEquals("607443", pubchemCids.get(0));
    }
    catch (Exception e) {
      fail("exception: " + e.getMessage());
    }
  }

  public void testGetPubchemCidsForSmiles3()
  {
    try {
      int[] cids = PugSoapUtil.identitySearch("NC(=S)c1cnc2ccccn2c1=N");
      List<String> pubchemCids = Lists.newLinkedList();
      for(int cid:cids) pubchemCids.add(""+cid);
      
      assertEquals(1, pubchemCids.size());
      assertEquals("40486874", pubchemCids.get(0));
    }
    catch (Exception e) {
      fail("exception: " + e.getMessage());
    }
  }
  
  public void testGetPubchemCidsForSmiles4()
  {
    try {
      int[] cids = PugSoapUtil.identitySearch("CCOC(=O)C(C#N)C(=O)c1ccc(N)cc1");
      List<String> pubchemCids = Lists.newLinkedList();
      for(int cid:cids) pubchemCids.add(""+cid);
      
      assertEquals(3, pubchemCids.size());
      assertTrue(pubchemCids.contains("577795"));
      assertTrue(pubchemCids.contains("684423"));
      assertTrue(pubchemCids.contains("684424"));
    }
    catch (Exception e) {
      fail("exception: " + e.getMessage());
    }
  }
  
  public void testGetPubchemCidsForInvalidSmiles()
  {
    try {
      String smiles = "CCOC(=O)C(C#N)C(=O)c1ccc(N)cc1)"; // the dangling ')' is a actual encountered error
      int[] cids = PugSoapUtil.identitySearch(smiles);
      List<String> pubchemCids = Lists.newLinkedList();
      for(int cid:cids) pubchemCids.add(""+cid);
      
      fail("faulty SMILES string: " + smiles + ", should generate an exception");
    }
    catch (Exception e) {
      log.info("expected exception: " + e.getMessage());
    }
  }
  
  public void testStandardizeSmiles1()
  {
    try {
      String standardizedSmiles = PugSoapUtil.standardizeSmiles("CCC");      
      assertEquals("CCC", standardizedSmiles);
    }
    catch (Exception e) {
      fail("PubchemSmilesOrInchiStandardizer threw an exception: " + e.getMessage());
    }
  }

  public void testStandardizeSmiles2()
  {
    try {
      String standardizedSmiles = PugSoapUtil.standardizeSmiles("Clc1ccc(\\C=C/c2c(C)n(C)n(c3ccccc3)c2=O)c(Cl)c1");
      assertEquals("CC1=C(C(=O)N(N1C)C2=CC=CC=C2)/C=C\\C3=C(C=C(C=C3)Cl)Cl", standardizedSmiles);
    }
    catch (Exception e) {
      fail("PubchemSmilesOrInchiStandardizer threw an exception: " + e.getMessage());
    }
  }
  
}