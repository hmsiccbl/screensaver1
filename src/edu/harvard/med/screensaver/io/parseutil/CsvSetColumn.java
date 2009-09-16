// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.parseutil;

import java.util.Set;

import edu.harvard.med.screensaver.io.DataExporter;
import edu.harvard.med.screensaver.io.ParseError;
import edu.harvard.med.screensaver.io.libraries.ParseException;

import com.google.common.collect.Sets;

public abstract class CsvSetColumn<E> extends CsvColumn<Set<E>>
{
  public CsvSetColumn(String name, int col, boolean isRequired)
  {
    super(name, col, isRequired);
  }

  @Override
  protected Set<E> parseField(String value) throws ParseException
  {
    Set<E> set = Sets.newHashSet();
    if (value != null) {
      String[] values = value.split(DataExporter.LIST_DELIMITER);
      for (String v : values) {
        if (!set.add(parseElement(v.trim()))) {
          throw new ParseException(new ParseError(getName() +
                                                  " cannot contain duplicates: " +
                                                  v));
        }
      }
    }
    return set;
  }

  abstract protected E parseElement(String value);
}