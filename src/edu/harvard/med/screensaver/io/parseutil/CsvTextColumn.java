// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.parseutil;

import edu.harvard.med.screensaver.io.ParseException;

public class CsvTextColumn extends CsvColumn<String>
{
  public CsvTextColumn(String name, int col, boolean isRequired)
  {
    super(name, col, isRequired);
  }

  @Override
  protected String parseField(String value) throws ParseException
  {
    return value;
  }
}