// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
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
  private static final String LIBRARY_NAME = "Library&nbsp;Name";
  private static final String SHORT_NAME = "Short&nbsp;Name";
  private static final String SCREEN_TYPE = "Screen&nbsp;Type";
  private static final String LIBRARY_TYPE = "Library&nbsp;Type";
  private static final String START_PLATE = "Start&nbsp;Plate";
  private static final String END_PLATE = "End&nbsp;Plate";
  private static final String VENDOR = "Vendor";
  private static final String DESCRIPTION = "Description";
  private static final String NUMBER_OF_WELLS = "Number&nbsp;of&nbsp;Experimental&nbsp;Wells";

  
  // private instance fields
  
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
    addItem(LIBRARY_NAME, library.getLibraryName());
    addItem(SHORT_NAME, library.getShortName());
    addItem(SCREEN_TYPE, library.getScreenType().getValue());
    addItem(LIBRARY_TYPE, library.getLibraryType().getValue());
    addItem(START_PLATE, Integer.toString(library.getStartPlate()));
    addItem(END_PLATE, Integer.toString(library.getEndPlate()));
    if (library.getVendor() != null) {
      addItem(VENDOR, library.getVendor());
    }
    if (library.getDescription() != null) {
      addItem(DESCRIPTION, library.getDescription());
    }
    addItem(NUMBER_OF_WELLS, Integer.toString(librarySize));
  }

  private void addItem(String name, String value)
  {
    _names.add(name);
    _values.add(value);
  }
}

