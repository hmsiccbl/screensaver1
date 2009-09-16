// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.parseutil;

import edu.harvard.med.screensaver.io.ParseError;
import edu.harvard.med.screensaver.io.libraries.ParseException;
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