// $HeadURL:
// svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml
// $
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.parseutil;


import edu.harvard.med.screensaver.io.ParseError;
import edu.harvard.med.screensaver.io.ParseException;


public abstract class Column<R, T>
{
  private String name;
  private final int col;
  private final boolean isRequired;

  Column(String name, int col, boolean isRequired)
  {
    this.name = name;
    this.col = col;
    this.isRequired = isRequired;
  }

  public String getName()
  {
    return name;
  }

  public int getColumn()
  {
    return col;
  }

  public boolean isRequired()
  {
    return isRequired;
  }

  public boolean isConditionallyRequired(R row) throws ParseException
  {
    return isRequired();
  }

  abstract public T getField(R row) throws ParseException;

  public Object getLocation(R row)
  {
    return col + 1;
  }

  public T getValue(R row) throws ParseException
  {
    T value = getField(row);
    if (isConditionallyRequired(row) && value == null) {
      throw new ParseException(new ParseError(getName() + " is required",
                                              getLocation(row).toString()));
    }
    return value;
  }  
}

