// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.namevaluetable;

import java.util.ArrayList;
import java.util.List;

import edu.harvard.med.screensaver.model.libraries.Reagent;

/**
 * A NameValueTable for Reagents.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
abstract public class ReagentNameValueTable<R extends Reagent> extends NameValueTable
{
  private static final String VERSION = "Library Contents Version";

  protected R _reagent;
  protected List<String> _names = new ArrayList<String>();
  protected List<Object> _values = new ArrayList<Object>();
  protected List<ValueType> _valueTypes = new ArrayList<ValueType>();
  protected List<String> _descriptions = new ArrayList<String>();

  public ReagentNameValueTable(R reagent)
  {
    _reagent = reagent;
  }

  @Override
  public int getNumRows()
  {
    return _names.size();
  }

  @Override
  public String getDescription(int index)
  {
    return _descriptions.get(index);
  }

  @Override
  public String getName(int index)
  {
    return _names.get(index);
  }

  @Override
  public ValueType getValueType(int index)
  {
    return _valueTypes.get(index);
  }

  @Override
  public Object getValue(int index)
  {
    return _values.get(index);
  }

  @Override
  public String getAction(int index, String value)
  {
    return null;
  }

  @Override
  public String getLink(int index, String value)
  {
    return null;
  }

  protected void initializeLists(R reagent)
  {
    addItem(VERSION, reagent.getLibraryContentsVersion().getVersionNumber(), ValueType.TEXT, "The reagent's library contents version");
  }

  protected void addItem(String name, Object value, ValueType valueType, String description)
  {
    _names.add(name);
    _values.add(value);
    _valueTypes.add(valueType);
    _descriptions.add(description);
  }
}

