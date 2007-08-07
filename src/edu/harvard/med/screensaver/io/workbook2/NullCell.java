// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.workbook2;

import java.util.Date;

public class NullCell extends Cell
{

  public NullCell(Workbook workbook, int sheetIndex, ParseErrorManager errors)
  {
    super(workbook, sheetIndex, errors, (short) 0, 0, false);
  }

  @Override
  public Object clone()
  {
    return new NullCell(_workbook, _sheetIndex, _errors);
  }

  @Override
  public String getAsString()
  {
    return "";
  }

  @Override
  public String getAsString(boolean withValidation)
  {
    return "";
  }

  @Override
  public Boolean getBoolean()
  {
    return Boolean.FALSE;
  }

  @Override
  public short getColumn()
  {
    return -1;
  }

  @Override
  public Date getDate()
  {
    return null;
  }

  @Override
  public Double getDouble()
  {
    return 0.0;
  }

  @Override
  public String getFormattedRowAndColumn()
  {
    return "(?,?)";
  }

  @Override
  public Integer getInteger()
  {
    return 0;
  }

  @Override
  public int getRow()
  {
    return -1;
  }

  @Override
  public jxl.Sheet getSheet()
  {
    return null;
  }

  @Override
  public String getSheetName()
  {
    return "?";
  }

  @Override
  public String getString()
  {
    return "";
  }
}
