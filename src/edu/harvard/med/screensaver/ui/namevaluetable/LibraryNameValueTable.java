// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.namevaluetable;

import java.util.Arrays;

import javax.faces.model.ListDataModel;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.libraries.Library;

public class LibraryNameValueTable extends NameValueTable
{
  
  // private static final fields
  
  private static final Logger log = Logger.getLogger(LibraryNameValueTable.class);
  private static final String [] NAMES = {
    "Library&nbsp;Name",
    "Short&nbsp;Name",
    "Screen&nbsp;Type",
    "Library&nbsp;Type",
    "Vendor",
    "Description",
    "Number&nbsp;of&nbsp;Wells",
  };

  
  // private instance fields
  
  private String [] _values;
  
  public LibraryNameValueTable(Library library, int librarySize)
  {
    _values = new String[NAMES.length];
    _values[0] = library.getLibraryName();
    _values[1] = library.getShortName();
    _values[2] = library.getScreenType().getValue();
    _values[3] = library.getLibraryType().getValue();
    _values[4] = library.getVendor();
    _values[5] = library.getDescription();
    _values[6] = Integer.toString(librarySize);
    setDataModel(new ListDataModel(Arrays.asList(_values)));
  }
  
  protected String getAction(int index, String value)
  {
    // library name value table has no actions
    return null;
  }

  protected String getLink(int index, String value)
  {
    // library name value table has no links
    return null;
  }
  
  public String getName(int index)
  {
    return NAMES[index];
  }

  public int getNumRows()
  {
    return NAMES.length;
  }

  protected Object getValue(int index)
  {
    return _values[index];
  }

  protected ValueType getValueType(int index)
  {
    // all values in the library name value table are of type text
    return ValueType.TEXT;
  }
}

