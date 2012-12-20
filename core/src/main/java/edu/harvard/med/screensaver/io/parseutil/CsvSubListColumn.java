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
import edu.harvard.med.screensaver.io.ParseError;
import edu.harvard.med.screensaver.io.ParseException;

/**
 * CsvSubListColumn extends the concept of CsvListColumn to support a nested level in a list
 * field, used in hairpin (shRNA) reagent libraries to code for multiple genes.
 * 
 * The code does not extend CsvListColumn from an implementation perspective as this would
 * mean splitting on the usual list delimiter (;) first. For backward compatibility, the
 * outer list of lists actually uses colon (:) as a delimiter, so that the interpretation
 * of an existing list column is that it describes the first nested list.
 * 
 * For example, "123;456;789" is parsed as [ [ 123, 456, 789 ] ], not [ [123], [456], [789] ].
 * 
 * @author William Rose
 * @param <E> the type of the sublist elements
 */
public abstract class CsvSubListColumn<E> extends CsvColumn<List<List<E>>>
{
  public CsvSubListColumn(String name, int col, boolean isRequired)
  {
    super(name, col, isRequired);
  }

  @Override
  protected List<List<E>> parseField(String value) throws ParseException
  {
    List<List<E>> list = Lists.newArrayList();
    if (value != null) {
      String[] values = value.split(DataExporter.SUBLIST_DELIMITER, -1);
      for (String v : values) {
    	  // Don't check duplicates here: they are not unexpected
        list.add(parseElement(v.trim()));
      }
    }
    return list;
  }


  protected List<E> parseElement(String value) throws ParseException
  {
    List<E> list = Lists.newArrayList();
    if (value != null) {
      String[] values = value.split(DataExporter.LIST_DELIMITER, -1);
      for (String v : values) {
    	v = v.trim();
      	E val = parseSubElement(v);
      	if(v.length() > 0 && list.contains(val)) {
          throw new ParseException(new ParseError(getName() +
              " cannot contain duplicates: " +
              val));
      	}
        list.add(val);
      }
    }
    return list;
  }

  abstract protected E parseSubElement(String value);
}