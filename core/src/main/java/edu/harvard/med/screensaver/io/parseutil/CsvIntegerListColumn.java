// $HeadURL: http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/branches/serickson/3411/core/src/main/java/edu/harvard/med/screensaver/io/parseutil/CsvTextSetColumn.java $
// $Id: CsvTextSetColumn.java 6946 2012-01-13 18:24:30Z seanderickson1 $
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.parseutil;

import java.math.BigDecimal;


public class CsvIntegerListColumn extends CsvListColumn<Integer>
{
  public CsvIntegerListColumn(String name, int col, boolean isRequired)
  {
    super(name, col, isRequired);
  }

  @Override
  protected Integer parseElement(String value)
  {
    // note: using BigDecimal instead of Integer.parseInt(), to handle float-formatted values (must have zero decimal portion)
    return new BigDecimal(value).intValueExact();
  }
}