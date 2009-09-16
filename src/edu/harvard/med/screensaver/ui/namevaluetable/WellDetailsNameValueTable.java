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
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.ui.libraries.LibraryViewer;

import org.apache.log4j.Logger;

/**
 * A NameValueTable for the well details portion of the Well Viewer.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class WellDetailsNameValueTable extends NameValueTable
{
  private static final Logger log = Logger.getLogger(WellDetailsNameValueTable.class);

  private static final String PLATE = "Plate";
  private static final String WELL = "Well";
  private static final String LIBRARY = "Library";
  private static final String SCREEN_TYPE = "Screen Type";
  private static final String WELL_TYPE = "Library Well Type";
  private static final String VENDOR_IDENTIFIER = "Vendor/Source Reagent ID";
  private static final String FACILITY_IDENTIFIER = "Facility ID";
  private static final String DEPRECATED = "Deprecated";

  private LibraryViewer _libraryViewer;

  private Well _well;
  private Reagent _versionedReagent;
  private List<String> _names = new ArrayList<String>();
  private List<Object> _values = new ArrayList<Object>();
  private List<String> _descriptions = new ArrayList<String>();
  private List<ValueType> _valueTypes = new ArrayList<ValueType>();


  public WellDetailsNameValueTable(Well well,
                                   Reagent reagent, 
                                   LibraryViewer libraryViewer)
  {
    _well = well;
    _versionedReagent = reagent;
    setDataModel(new ListDataModel(_values));
    initializeLists(_well);
    _libraryViewer = libraryViewer;
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
    String name = getName(index);
    if (name.equals(LIBRARY)) {
      return _libraryViewer.viewLibrary(_well.getLibrary());
    }
    return null;
  }
  
  @Override
  public String getLink(int index, String value)
  {
    return null;
  }

  private void initializeLists(Well well)
  {
    addItem(PLATE, Integer.toString(well.getPlateNumber()), ValueType.TEXT, "The number of the plate the well is located on");
    addItem(WELL, well.getWellName(), ValueType.TEXT, "The plate coordinates of the well");
    addItem(LIBRARY, well.getLibrary().getLibraryName(), ValueType.COMMAND, "The library containing the well");
    addItem(SCREEN_TYPE, well.getLibrary().getScreenType(), ValueType.TEXT, "The library screen type");
    addItem(WELL_TYPE, well.getLibraryWellType(), ValueType.TEXT, "The type of well, e.g., 'Experimental', 'Control', 'Empty', etc.");
    addItem(FACILITY_IDENTIFIER, well.getFacilityId(), ValueType.TEXT, "An alternate identifier assigned by the facility to identify this well");
    if (_versionedReagent != null) { // can be null when used in WellViewer (and well has no reagent)
      addItem(VENDOR_IDENTIFIER, _versionedReagent == null ? "": _versionedReagent.getVendorId().toString(), ValueType.TEXT, "The reagent identifier provided by the vendor/source (e.g., catalog number; reorder number)");
    }
    if (well.isDeprecated()) {
      addItem(DEPRECATED, well.getDeprecationActivity().getComments(), ValueType.TEXT, "Why the well is deprecated");
    }
  }

  private void addItem(String name, Object value, ValueType valueType, String description)
  {
    _names.add(name);
    _values.add(value);
    _valueTypes.add(valueType);
    _descriptions.add(description);
  }
}

