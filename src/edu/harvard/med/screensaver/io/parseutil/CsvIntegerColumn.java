// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.parseutil;

import java.math.BigDecimal;

import edu.harvard.med.screensaver.io.ParseException;

public class CsvIntegerColumn extends CsvColumn<Integer>
{
  public CsvIntegerColumn(String name, int col, boolean isRequired)
  {
    super(name, col, isRequired);
  }

  @Override
  protected Integer parseField(String value) throws ParseException
  {
    // note: using BigDecimal instead of Integer.parseInt(), to handle float-formatted values (must have zero decimal portion)
    return new BigDecimal(value).intValueExact();
  }
}