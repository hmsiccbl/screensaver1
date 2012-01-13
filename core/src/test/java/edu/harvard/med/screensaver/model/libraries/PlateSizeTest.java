// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import junit.framework.TestCase;
import org.apache.log4j.Logger;

public class PlateSizeTest extends TestCase
{
  private static Logger log = Logger.getLogger(PlateSizeTest.class);
  
  public void testEdgeWellNames()
  {
    Set<WellName> edgeWellNames = PlateSize.WELLS_384.getEdgeWellNames(1);
    assertEquals(76, edgeWellNames.size());
    assertTrue(Iterables.all(edgeWellNames, new Predicate<WellName>() { 
      public boolean apply(WellName w) { 
        return w.getRowIndex() < 1 || w.getRowIndex() >= PlateSize.WELLS_384.getRows() - 1 || 
        w.getColumnIndex() < 1 || w.getColumnIndex() >= PlateSize.WELLS_384.getColumns() - 1;
      }
    }));
    
    edgeWellNames = PlateSize.WELLS_96.getEdgeWellNames(3);
    assertEquals(84, edgeWellNames.size());
    assertTrue(Iterables.all(edgeWellNames, new Predicate<WellName>() { 
      public boolean apply(WellName w) { 
        return w.getRowIndex() < 3 || w.getRowIndex() >= PlateSize.WELLS_96.getRows() - 3 || 
        w.getColumnIndex() < 3 || w.getColumnIndex() >= PlateSize.WELLS_96.getColumns() - 3;
      }
    }));
  }

  public void testLabels()
  {
    assertEquals(ImmutableList.of("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"),
                 PlateSize.WELLS_96.getColumnsLabels());
    assertEquals(ImmutableList.of("A", "B", "C", "D", "E", "F", "G", "H"),
                 PlateSize.WELLS_96.getRowsLabels());
  }
}
