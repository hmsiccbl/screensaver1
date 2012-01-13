// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import java.util.HashSet;
import java.util.Set;

import edu.harvard.med.screensaver.ScreensaverConstants;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

public class WellKeyTest extends TestCase
{
  private static Logger log = Logger.getLogger(WellKeyTest.class);

  public void testWellKey()
  {
    Set<WellKey> set1 = new HashSet<WellKey>();
    Set<WellKey> set2 = new HashSet<WellKey>();
    Set<WellKey> set3 = new HashSet<WellKey>();
    Set<WellKey> set4 = new HashSet<WellKey>();
    Set<WellKey> set5 = new HashSet<WellKey>();
    for (int iPlate = 1; iPlate <= 10; ++iPlate) {
      for (int iRow = 0; iRow < ScreensaverConstants.DEFAULT_PLATE_SIZE.getRows(); ++iRow) {
        for (int iCol = 0; iCol < ScreensaverConstants.DEFAULT_PLATE_SIZE.getColumns(); ++iCol) {
          String wellNameStr = String.format("%c%02d", iRow + 'A', iCol + 1);
          assertTrue("added " + iPlate + ":" + (iRow + 'A') + (iCol + 1) + " to set1",
                     set1.add(new WellKey(iPlate, iRow, iCol)));
          assertTrue("added " + iPlate + ":" + (iRow + 'A') + (iCol + 1) + " to set2",
                     set2.add(new WellKey(iPlate + ":" + wellNameStr)));
          assertTrue("added " + iPlate + ":" + (iRow + 'A') + (iCol + 1) + " to set3",
                     set3.add(new WellKey(iPlate, wellNameStr)));
          assertTrue("added " + iPlate + ":" + (iRow + 'A') + (iCol + 1) + " to set4",
                     set4.add(new WellKey(iPlate, new WellName(iRow, iCol))));
          assertTrue("added " + iPlate + ":" + (iRow + 'A') + (iCol + 1) + " to set5",
                     set5.add(new WellKey(iPlate, new WellName(wellNameStr))));
        }
      }
    }
    assertEquals(ScreensaverConstants.DEFAULT_PLATE_SIZE.getWellCount() * 10, set1.size());
    assertEquals(set1, set2);
    assertEquals(set1, set3);
    assertEquals(set1, set4);
    assertEquals(set1, set5);
  }
}

