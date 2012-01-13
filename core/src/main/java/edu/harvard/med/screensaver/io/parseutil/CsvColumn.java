// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.parseutil;

import edu.harvard.med.screensaver.io.ParseError;
import edu.harvard.med.screensaver.io.ParseException;
import edu.harvard.med.screensaver.util.StringUtils;

public abstract class CsvColumn<T> extends Column<String[],T>
{
  public CsvColumn(String name, int col, boolean isRequired)
  {
    super(name, col, isRequired);
  }

  public T getField(String[] row) throws ParseException
  {
    if (getColumn() >= row.length) {
      return null;
    }
    if (StringUtils.isEmpty(row[getColumn()])) {
      return null;
    }
    try {
      return parseField(row[getColumn()]);
    }
    catch (Exception e) {
      throw new ParseException(new ParseError(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage(),
                                              getLocation(row).toString()));
    }
  }

  abstract protected T parseField(String string) throws ParseException;
}