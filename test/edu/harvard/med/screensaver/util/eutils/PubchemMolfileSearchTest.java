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

import junit.framework.TestCase;

public class PubchemMolfileSearchTest extends TestCase
{
  public void testPubchemMolfileSearch()
  {
    PubchemMolfileSearch pubchemMolfileSearch = new PubchemMolfileSearch();
    String molfile =
  "Structure942\n" +
  "csChFnd70/09300411422D\n" +
  "\n" +
  " 23 24  0  0  0  0  0  0  0  0999 V2000\n" +
  "    9.7074    2.0948    0.0000 N   0  0  3  0  0  0  0  0  0  0  0  0\n" +
  "   10.9224    1.4024    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
  "   12.1112    2.0948    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\n" +
  "    9.7074    3.4564    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
  "   10.9224    4.1520    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
  "   12.1112    3.4564    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
  "    1.2536    4.1243    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
  "    2.4963    3.4564    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
  "    3.6846    1.4024    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\n" +
  "    4.9004    2.0948    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
  "    3.6846    4.1520    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
  "    4.9004    3.4564    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
  "    2.4963    2.1213    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
  "   10.9224    0.0000    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n" +
  "    8.4916    4.2188    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n" +
  "    1.9086    5.3928    0.0000 F   0  0  0  0  0  0  0  0  0  0  0  0\n" +
  "    0.5734    2.8831    0.0000 F   0  0  0  0  0  0  0  0  0  0  0  0\n" +
  "    0.0000    4.8073    0.0000 F   0  0  0  0  0  0  0  0  0  0  0  0\n" +
  "    6.0758    1.4024    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\n" +
  "   13.3537    4.1243    0.0000 Cl  0  0  0  0  0  0  0  0  0  0  0  0\n" +
  "    8.4916    1.3746    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
  "    6.1273    4.1785    0.0000 Cl  0  0  0  0  0  0  0  0  0  0  0  0\n" +
  "    7.2776    2.0948    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
  "  2  1  1  0  0  0  0\n" +
  "  3  2  1  0  0  0  0\n" +
  "  4  1  1  0  0  0  0\n" +
  "  5  4  1  0  0  0  0\n" +
  "  6  5  2  0  0  0  0\n" +
  "  7  8  1  0  0  0  0\n" +
  "  8 11  2  0  0  0  0\n" +
  "  9 10  1  0  0  0  0\n" +
  " 10 19  1  0  0  0  0\n" +
  " 11 12  1  0  0  0  0\n" +
  " 12 10  2  0  0  0  0\n" +
  " 13  9  2  0  0  0  0\n" +
  " 14  2  2  0  0  0  0\n" +
  " 15  4  2  0  0  0  0\n" +
  " 16  7  1  0  0  0  0\n" +
  " 17  7  1  0  0  0  0\n" +
  " 18  7  1  0  0  0  0\n" +
  " 19 23  1  0  0  0  0\n" +
  " 20  6  1  0  0  0  0\n" +
  " 21  1  1  0  0  0  0\n" +
  " 22 12  1  0  0  0  0\n" +
  " 23 21  1  0  0  0  0\n" +
  "  3  6  1  0  0  0  0\n" +
  "  8 13  1  0  0  0  0\n" +
  "M  END\n" +
  "\n";
    
    List<String> pubchemCids;
    try {
      pubchemCids = pubchemMolfileSearch.getPubchemCidsForMolfile(molfile);
      assertEquals("one resultant pubchem cid", pubchemCids.size(), 1);
      assertEquals("resultant pubchem cid is 2768895", pubchemCids.get(0), "2768895");
    }
    catch (EutilsException e) {
      fail("PubchemMolfileSearch threw an exception: " + e.getMessage());
    }
  }
}
