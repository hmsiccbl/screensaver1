// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.util;

import junit.framework.TestCase;

public class AlphabeticCounterTest extends TestCase
{
  public void testToLabel()
  {
    assertEquals("A", AlphabeticCounter.toLabel(0));
    assertEquals("B", AlphabeticCounter.toLabel(1));
    assertEquals("Z", AlphabeticCounter.toLabel(25));
    assertEquals("AA", AlphabeticCounter.toLabel(26));
    assertEquals("AZ", AlphabeticCounter.toLabel(51));
    assertEquals("BA", AlphabeticCounter.toLabel(52));
    assertEquals("YA", AlphabeticCounter.toLabel(650));
    assertEquals("YB", AlphabeticCounter.toLabel(651));
    assertEquals("YZ", AlphabeticCounter.toLabel(675));
    assertEquals("ZA", AlphabeticCounter.toLabel(676));
    assertEquals("ZB", AlphabeticCounter.toLabel(677));
    assertEquals("ZZ", AlphabeticCounter.toLabel(701));
    assertEquals("AAA", AlphabeticCounter.toLabel(702));
  }

  public void testToIndex()
  {
    assertEquals(0, AlphabeticCounter.toIndex("A"));
    assertEquals(1, AlphabeticCounter.toIndex("B"));
    assertEquals(25, AlphabeticCounter.toIndex("Z"));
    assertEquals(26, AlphabeticCounter.toIndex("AA"));
    assertEquals(27, AlphabeticCounter.toIndex("AB"));
    assertEquals(51, AlphabeticCounter.toIndex("AZ"));
    assertEquals(52, AlphabeticCounter.toIndex("BA"));
    assertEquals(676, AlphabeticCounter.toIndex("ZA"));
    assertEquals(677, AlphabeticCounter.toIndex("ZB"));
    assertEquals(701, AlphabeticCounter.toIndex("ZZ"));
    assertEquals(702, AlphabeticCounter.toIndex("AAA"));
    assertEquals(703, AlphabeticCounter.toIndex("AAB"));
    assertEquals(702 + 701 - 26, AlphabeticCounter.toIndex("AZZ"));
    assertEquals(1 + (702 + 701 - 26), AlphabeticCounter.toIndex("BAA"));
    assertEquals(702 + 676 * 2, AlphabeticCounter.toIndex("CAA"));
    assertEquals(702 + 676 * 25, AlphabeticCounter.toIndex("ZAA"));
    assertEquals(702 + 676 * 25 + (701 - 26), AlphabeticCounter.toIndex("ZZZ"));
  }
}
