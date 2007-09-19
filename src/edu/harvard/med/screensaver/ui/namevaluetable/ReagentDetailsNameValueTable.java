// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/ui/namevaluetable/WellDetailsNameValueTable.java $
// $Id: WellDetailsNameValueTable.java 1760 2007-08-31 15:00:43Z ant4 $
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

import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.ui.libraries.LibraryViewer;
import edu.harvard.med.screensaver.ui.util.HtmlUtils;

import org.apache.log4j.Logger;

/**
 * A NameValueTable for the well details portion of the Reagent Viewer.
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

  private Well _well;
  private List<String> _names = new ArrayList<String>();
  private List<Object> _values = new ArrayList<Object>();
  private List<String> _descriptions = new ArrayList<String>();
  private List<ValueType> _valueTypes = new ArrayList<ValueType>();


  // public constructor and implementations of NameValueTable abstract methods

  public ReagentDetailsNameValueTable(Well well)
  {
    _well = well;
    initializeLists(well);
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
    // no well detail fields have links
    return null;
  }


  // protected instance methods

  protected Well getWell()
  {
    return _well;
  }

  /**
   * Initialize the lists {@link #_names}, {@link #_values}, and {@link #_valueTypes}. Don't
   * add rows for missing values.
   */
  protected void initializeLists(Well well) {
    if (well.getVendorIdentifier() != null) {
      String vendor = well.getLibrary().getVendor();
      String vendorIdentifier = vendor == null ?
        well.getVendorIdentifier() : vendor + " " + well.getVendorIdentifier();
      addItem(VENDOR_IDENTIFIER, vendorIdentifier, ValueType.TEXT, "The reagent source identifier (e.g., catalog number; reorder number) for the well contents");
    }
  }

  protected void addItem(String name, Object value, ValueType valueType, String description)
  {
    _names.add(HtmlUtils.toNonBreakingSpaces(name));
    _values.add(value);
    _valueTypes.add(valueType);
    _descriptions.add(description);
  }
}

