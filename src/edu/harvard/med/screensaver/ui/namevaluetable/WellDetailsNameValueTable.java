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

import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.ui.libraries.LibraryViewer;

import org.apache.log4j.Logger;

/**
 * A NameValueTable for the well details portion of the Well Viewer.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class WellDetailsNameValueTable extends ReagentDetailsNameValueTable
{

  // private static final fields

  private static final Logger log = Logger.getLogger(WellDetailsNameValueTable.class);

  // the row names:
  private static final String LIBRARY = "Library";
  private static final String PLATE = "Plate";
  private static final String WELL = "Well";
  private static final String WELL_TYPE = "Well&nbsp;Type";
  private static final String ICCB_NUMBER = "ICCB&nbsp;Number";


  // private instance fields

  private LibraryViewer _libraryViewer;

  private List<String> _names = new ArrayList<String>();
  private List<Object> _values = new ArrayList<Object>();
  private List<String> _descriptions = new ArrayList<String>();
  private List<ValueType> _valueTypes = new ArrayList<ValueType>();


  // public constructor and implementations of NameValueTable abstract methods

  public WellDetailsNameValueTable(Well well,
                                   LibraryViewer libraryViewer)
  {
    super(well);
    _libraryViewer = libraryViewer;
  }


  @Override
  public String getAction(int index, String value)
  {
    String name = getName(index);
    if (name.equals(LIBRARY)) {
      return _libraryViewer.viewLibrary(getWell().getLibrary());
    }
    // other fields do not have actions
    return super.getAction(index, value);
  }

  // private instance methods

  /**
   * Initialize the lists {@link #_names}, {@link #_values}, and {@link #_valueTypes}. Don't
   * add rows for missing values.
   */
  protected void initializeLists(Well well)
  {
    addItem(LIBRARY, well.getLibrary().getLibraryName(), ValueType.COMMAND, "The library containing the well");
    addItem(PLATE, Integer.toString(well.getPlateNumber()), ValueType.TEXT, "The number of the plate the well is located on");
    addItem(WELL, well.getWellName(), ValueType.TEXT, "The plate coordinates of the well");
    addItem(WELL_TYPE, well.getWellType(), ValueType.TEXT, "The type of well, e.g., 'Experimental', 'Control', 'Empty', etc.");
    if (well.getIccbNumber() != null) {
      addItem(ICCB_NUMBER, well.getIccbNumber(), ValueType.TEXT, "The ICCB number for the well contents");
    }
    super.initializeLists(well);
  }
}

