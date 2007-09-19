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

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.ui.util.HtmlUtils;

/**
 * A NameValueTable for the Library Viewer.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class LibraryNameValueTable extends NameValueTable
{

  // private static final fields

  private static final Logger log = Logger.getLogger(LibraryNameValueTable.class);

  // the row names
  private static final String LIBRARY_NAME = "Library Name";
  private static final String SHORT_NAME = "Short Name";
  private static final String SCREEN_TYPE = "Screen Type";
  private static final String LIBRARY_TYPE = "Library Type";
  private static final String START_PLATE = "Start Plate";
  private static final String END_PLATE = "End Plate";
  private static final String VENDOR = "Vendor";
  private static final String DESCRIPTION = "Description";
  private static final String NUMBER_OF_WELLS = "Number of Experimental Wells";


  // private instance fields

  private List<String> _descriptions = new ArrayList<String>();
  private List<String> _names = new ArrayList<String>();
  private List<String> _values = new ArrayList<String>();

  @SuppressWarnings("unchecked")
  public LibraryNameValueTable(Library library, int librarySize)
  {
    initializeLists(library, librarySize);
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
    // all values in the library name value table are of type text
    return ValueType.TEXT;
  }

  @Override
  public Object getValue(int index)
  {
    return _values.get(index);
  }

  @Override
  public String getAction(int index, String value)
  {
    // library name value table has no actions
    return null;
  }

  @Override
  public String getLink(int index, String value)
  {
    // library name value table has no links
    return null;
  }


  // private instance methods

  /**
   * Initialize the lists {@link #_names} and {@link #_values}.
   */
  private void initializeLists(Library library, int librarySize) {
    addItem(LIBRARY_NAME, library.getLibraryName(), "The full name of the library");
    addItem(SHORT_NAME, library.getShortName(), "The abbreviated name for the library");
    addItem(SCREEN_TYPE, library.getScreenType().getValue(), "'RNAi' or 'Small Molecule'");
    addItem(LIBRARY_TYPE, library.getLibraryType().getValue(), "The type of library, e.g., 'Commercial', 'Known Bioactives', 'siRNA', etc.");
    addItem(START_PLATE, Integer.toString(library.getStartPlate()), "The plate number for the first plate in the library");
    addItem(END_PLATE, Integer.toString(library.getEndPlate()), "The plate number for the last plate in the library");
    if (library.getVendor() != null) {
      addItem(VENDOR, library.getVendor(), "The name of the library vendor");
    }
    if (library.getDescription() != null) {
      addItem(DESCRIPTION, library.getDescription(), "A description of the library");
    }
    addItem(NUMBER_OF_WELLS, Integer.toString(librarySize), "The number of wells in the library with well type 'Experimental'");
  }

  private void addItem(String name, String value, String description)
  {
    _names.add(HtmlUtils.toNonBreakingSpaces(name));
    _values.add(value);
    _descriptions.add(description);
  }
}

