// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.namevaluetable;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.ListDataModel;

import edu.harvard.med.screensaver.model.libraries.Reagent;

import org.apache.log4j.Logger;

/**
 * A NameValueTable for the reagent details portion of the Reagent Viewer.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class ReagentDetailsNameValueTable extends NameValueTable
{

  // private static final fields

  private static final Logger log = Logger.getLogger(ReagentDetailsNameValueTable.class);

  // the row names:
  private static final String VENDOR_IDENTIFIER = "Reagent Source ID";


  // private instance fields

  private Reagent _reagent;
  private List<String> _names = new ArrayList<String>();
  private List<Object> _values = new ArrayList<Object>();
  private List<String> _descriptions = new ArrayList<String>();
  private List<ValueType> _valueTypes = new ArrayList<ValueType>();


  // public constructor and implementations of NameValueTable abstract methods

  public ReagentDetailsNameValueTable(Reagent reagent)
  {
    _reagent = reagent;
    initializeLists(reagent);
    setDataModel(new ListDataModel(_values));
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
    // other fields do not have actions
    return null;
  }

  @Override
  public String getLink(int index, String value)
  {
    // no reagent detail fields have links
    return null;
  }


  // protected instance methods

  protected Reagent getReagent()
  {
    return _reagent;
  }

  /**
   * Initialize the lists {@link #_names}, {@link #_values}, and {@link #_valueTypes}. Don't
   * add rows for missing values.
   */
  protected void initializeLists(Reagent reagent) {
    if (reagent != null) { // can be null when used in WellViewer (and well has no reagent)
      addItem(VENDOR_IDENTIFIER, reagent.getEntityId().toString(), ValueType.TEXT, "The reagent source identifier (e.g., catalog number; reorder number) for the reagent contents");
    }
  }

  protected void addItem(String name, Object value, ValueType valueType, String description)
  {
    _names.add(name);
    _values.add(value);
    _valueTypes.add(valueType);
    _descriptions.add(description);
  }
}

