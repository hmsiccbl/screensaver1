// $HeadURL: http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/branches/serickson/3411/core/src/main/java/edu/harvard/med/screensaver/io/parseutil/CsvSetColumn.java $
// $Id: CsvSetColumn.java 6946 2012-01-13 18:24:30Z seanderickson1 $
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.parseutil;

import java.util.List;

import com.google.common.collect.Lists;

import edu.harvard.med.screensaver.io.DataExporter;
import edu.harvard.med.screensaver.io.ParseException;

public abstract class CsvListColumn<E> extends CsvColumn<List<E>>
{
  public CsvListColumn(String name, int col, boolean isRequired)
  {
    super(name, col, isRequired);
  }

  @Override
  protected List<E> parseField(String value) throws ParseException
  {
    List<E> list = Lists.newArrayList();
    if (value != null) {
      String[] values = value.split(DataExporter.LIST_DELIMITER, -1);
      for (String v : values) {
      	E val = parseElement(v.trim());
      	// No duplicate checks in lists: use CsvSetColumn<E> if you want a unique list
        list.add(val);
      }
    }
    return list;
  }

  abstract protected E parseElement(String value);
}