// $HeadURL: http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/branches/serickson/2920-rev2/core/src/main/java/edu/harvard/med/screensaver/io/parseutil/CsvIntegerColumn.java $
// $Id: CsvIntegerColumn.java 3968 2010-04-08 17:04:35Z atolopko $
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.parseutil;

import java.math.BigDecimal;
import java.util.regex.Matcher;

import edu.harvard.med.screensaver.io.ParseError;
import edu.harvard.med.screensaver.io.ParseException;
import edu.harvard.med.screensaver.io.libraries.LibraryContentsParser;
import edu.harvard.med.screensaver.model.MolarConcentration;
import edu.harvard.med.screensaver.model.MolarUnit;

public class CsvConcentrationColumn extends CsvColumn<Object>
{
  private MolarConcentration molarConcentration = null;
  private BigDecimal mgMlConcentration = null;

  public CsvConcentrationColumn(String name, int col, boolean isRequired)
  {
    super(name, col, isRequired);
  }

  @Override
  protected Object parseField(String string) throws ParseException
  {
    molarConcentration = null;
    mgMlConcentration = null;
    Matcher matcher = LibraryContentsParser.molarConcentrationPattern.matcher(string);
    if (matcher.matches()) {
      MolarUnit unit = MolarUnit.forSymbol(matcher.group(2));
      molarConcentration = MolarConcentration.makeConcentration(matcher.group(1), unit);
      return molarConcentration;
    }
    else {
      matcher = LibraryContentsParser.mgMlConcentrationPattern.matcher(string);
      if (matcher.matches()) {
        mgMlConcentration = new BigDecimal(matcher.group(1));
        return mgMlConcentration;
      }
      else {
        throw new ParseException(new ParseError("field: \"" + getName() +
            "\" could not be interpreted (use either \"##.## M\" or \"##.### mg/ml\"), value entered:" +
              string));
      }
    }
  }

  public BigDecimal getMgMlConcentration()
  {
    return mgMlConcentration;
  }

  public MolarConcentration getMolarConcentration()
  {
    return molarConcentration;
  }
}