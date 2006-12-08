// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screens.ScreenType;

public class WellTest extends TestCase
{
  private static Logger log = Logger.getLogger(WellTest.class);
  private Library _library;

  @Override
  protected void setUp() throws Exception
  {
    _library = new Library("testLibrary", "testLibrary", ScreenType.SMALL_MOLECULE, LibraryType.COMMERCIAL, 1, 1);
    
  }
  
  public void testWellNameParser()
  {
    // TODO: should test based upon some fixed plate size (size shouldn't be
    // determined here)
    for (char row = Character.toLowerCase(Well.MIN_WELL_ROW); row <= Character.toLowerCase(Well.MAX_WELL_ROW); ++row) {
      for (int col = 0; col < Well.PLATE_COLUMNS; ++col) {
        doTestWellName(row, col);
      }
    }
    for (char row = Well.MIN_WELL_ROW; row <= Well.MAX_WELL_ROW; ++row) {
      for (int col = 0; col < Well.PLATE_COLUMNS; ++col) {
        doTestWellName(row, col);
      }
    }
  }
  
  public void testWellEdge()
  {
    for (char row = Character.toLowerCase(Well.MIN_WELL_ROW); row <= Character.toLowerCase(Well.MAX_WELL_ROW); ++row) {
      for (int col = 0; col < Well.PLATE_COLUMNS; ++col) {
        doTestWellEdge(row, col, row == 'a' || row == 'p' || col == 0 || col == 23);
      }
    }
    for (char row = Well.MIN_WELL_ROW; row <= Well.MAX_WELL_ROW; ++row) {
      for (int col = 0; col < Well.PLATE_COLUMNS; ++col) {
        doTestWellEdge(row, col, row == 'A' || row == 'P' || col == 0 || col == 23);
      }
    }
  }


  private void doTestWellEdge(char row, int col, boolean isEdge)
  {
    String wellName = "" + row + "" + (col + 1);
    assertEquals("is edge @ " + wellName, 
                 isEdge,
                 new Well(_library, 1, wellName).isEdgeWell());
  }

  private void doTestWellName(char rowLetter, int col)
  {
    String wellName = "" + rowLetter + "" + (col + 1);
    Well well = new Well(_library, 1, wellName);
    assertEquals(wellName + " row",
                 Character.isUpperCase(rowLetter) ? rowLetter - 'A'
                                                 : rowLetter - 'a',
                 well.getRow());
    assertEquals(wellName + " row letter",
                 Character.toUpperCase(rowLetter),
                 well.getRowLetter());
    assertEquals(wellName + " column",
                 col,
                 well.getColumn());

  }

}

