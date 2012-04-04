// $HeadURL: http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/branches/serickson/3200/core/src/test/java/edu/harvard/med/screensaver/model/screens/CellLineTest.java $
// $Id: CellLineTest.java 6949 2012-01-13 19:00:59Z seanderickson1 $
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.cells;

import junit.framework.TestSuite;
import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;
import edu.harvard.med.screensaver.model.cells.PrimaryCell;

public class PrimaryCellTest extends AbstractEntityInstanceTest<PrimaryCell>
{
  public static TestSuite suite()
  {
    return buildTestSuite(PrimaryCellTest.class, PrimaryCell.class);
  }

  public PrimaryCellTest()
  {
    super(PrimaryCell.class);
  }
}
