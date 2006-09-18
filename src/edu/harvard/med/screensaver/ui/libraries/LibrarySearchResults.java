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

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.ui.SearchResults;

/**
 * @author s
 */
public class LibrarySearchResults extends SearchResults<Library>
{
  
  private static final Logger log = Logger.getLogger(LibrarySearchResults.class);

  private static final String SHORT_NAME   = "Short Name";
  private static final String LIBRARY_NAME = "Library Name";
  private static final String LIBRARY_TYPE = "Library Type";
  private static final String START_PLATE  = "Start Plate";
  private static final String END_PLATE    = "End Plate";
  
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
    tableData.add(SHORT_NAME);
    tableData.add(LIBRARY_NAME);
    tableData.add(LIBRARY_TYPE);
    tableData.add(START_PLATE);
    tableData.add(END_PLATE);
    return new ListDataModel(tableData);
  }
  
  protected boolean isCommandLink(String columnName)
  {
    return columnName.equals(SHORT_NAME);
  }
  
  protected Object getCellValue(Library library, String columnName)
  {
    if (columnName.equals(SHORT_NAME)) {
      return library.getShortName();
    }
    if (columnName.equals(LIBRARY_NAME)) {
      return library.getLibraryName();
    }
    if (columnName.equals(LIBRARY_TYPE)) {
      return library.getLibraryType();
    }
    if (columnName.equals(START_PLATE)) {
      return library.getStartPlate();
    }
    if (columnName.equals(END_PLATE)) {
      return library.getEndPlate();
    }
    return null;
  }
  
  protected Object getCellAction(Library library, String columnName)
  {
    log.info("get cell action YAY for " + library);
    //if (true) throw new NullPointerException();
    return "showLibrary";
  }
}
