// $HeadURL:
// http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/trunk/core/src/test/java/edu/harvard/med/screensaver/io/libraries/LibraryCopyPlateListParserTest.java
// $
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries;

import java.util.List;

import junit.framework.TestCase;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.harvard.med.screensaver.util.Pair;

public class LibraryCopyPlateListParserTest extends TestCase
{
  public void testSingleLineInput()
  {
    LibraryCopyPlateListParserResult result = LibraryCopyPlateListParser.parsePlateCopies("1 \"Copy X\" 2 D, 111-112, E, 113,114");
    assertEquals("syntax errors size", 0, result.getErrors().size());

    assertEquals("Library plates parsed", Sets.newHashSet(1, 2, 113, 114), result.getPlates());
    assertEquals("Library copies parsed", Sets.newHashSet("Copy X", "D", "E"), result.getCopies());
    assertEquals("Library plate ranges parsed,",
                 Lists.newArrayList(Pair.newPair(111, 112)),
                 Lists.newArrayList(result.getPlateRanges()));
  }

  public void testWithNoSpacesBetweenPlateAndCopy()
  {
    LibraryCopyPlateListParserResult result = LibraryCopyPlateListParser.parsePlateCopies("111A");
    assertEquals("syntax errors size", 0, result.getErrors().size());
    assertEquals("Library plates parsed", Sets.newHashSet(111), result.getPlates());
    assertEquals("Library copies parsed", Sets.newHashSet("A"), result.getCopies());
  }

  public void testMultiLine()
  {
    //LibraryCopyPlateListParserResult result = LibraryCopyPlateListParser.parsePlateCopies("1 C 2 D, 111-112\n E, 113-114");
    List<LibraryCopyPlateListParserResult> results = LibraryCopyPlateListParser.parsePlateCopiesSublists("1 C 2 D, 111-112\n E, 113-114 J");
    int i = 0;
    assertEquals(2, results.size());
    for (LibraryCopyPlateListParserResult result : results) {
      assertEquals("syntax errors size", 0, result.getErrors().size());
      if (i == 0) {
        assertEquals("Library plates parsed", Sets.newHashSet(1, 2), result.getPlates());
        assertEquals("Library plate ranges parsed,",
                     Lists.newArrayList(Pair.newPair(111, 112)),
                     Lists.newArrayList(result.getPlateRanges()));
        assertEquals("Library copies parsed", Sets.newHashSet("C", "D"), result.getCopies());
      }
      else if (i == 1) {
        assertTrue(result.getPlates().isEmpty());
        assertEquals("Library copies parsed", Sets.newHashSet( "E", "J"), result.getCopies());
        assertEquals("Library plate ranges parsed,",
                     Lists.newArrayList( Pair.newPair(113, 114)),
                     Lists.newArrayList(result.getPlateRanges()));
      }
      i++;
    }

  }

  public void testWithErrors()
  {
    LibraryCopyPlateListParserResult result = LibraryCopyPlateListParser.parsePlateCopies("1 C 2 D, 111-\n E, 113-114");
    assertEquals("syntax errors size", 1, result.getErrors().size());
    assertEquals(Lists.newArrayList("unparseable range: 111-"), result.getErrors());
  }
}
