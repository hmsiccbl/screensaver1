// $HeadURL: svn+ssh://js163@orchestra/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.ui.SearchResults;

/**
 * @author s
 */
public class LibrarySearchResults extends SearchResults<Library>
{

  /**
   * Construct a new <code>LibrarySearchResult</code> object.
   * @param unsortedResults the unsorted list of the results, as they are returned from the
   * database
   */
  public LibrarySearchResults(List<Library> unsortedResults)
  {
    super(unsortedResults);
  }

  protected DataModel createDataHeaderColumnModel()
  {
    List<String> tableData = new ArrayList<String>();
    tableData.add("Short Name");
    tableData.add("Library Name");
    tableData.add("Library Type");
    tableData.add("Start Plate");
    tableData.add("End Plate");
    return new ListDataModel(tableData);
  }
  
  protected Object getColumnValue(Library library, String columnName)
  {
    if (columnName.equals("Short Name")) {
      return library.getShortName();
    }
    if (columnName.equals("Library Name")) {
      return library.getLibraryName();
    }
    if (columnName.equals("Library Type")) {
      return library.getLibraryType();
    }
    if (columnName.equals("Start Plate")) {
      return library.getStartPlate();
    }
    if (columnName.equals("End Plate")) {
      return library.getEndPlate();
    }    
    return null;
  }
}
