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
import java.util.Comparator;
import java.util.List;

import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.ui.SearchResults;

import org.apache.log4j.Logger;


/**
 * A {@link SearchResults} for {@link Library Libraries}.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class LibrarySearchResults extends SearchResults<Library>
{
  
  // private static final fields
  
  private static final Logger log = Logger.getLogger(LibrarySearchResults.class);

  private static final String SHORT_NAME   = "Short Name";
  private static final String LIBRARY_NAME = "Library Name";
  private static final String LIBRARY_TYPE = "Library Type";
  private static final String START_PLATE  = "Start Plate";
  private static final String END_PLATE    = "End Plate";
  
  
  // instance fields
  
  private LibraryViewerController _libraryViewerController;
  
  
  // public contructor
  
  /**
   * Construct a new <code>LibrarySearchResult</code> object.
   * @param unsortedResults the unsorted list of the results, as they are returned from the
   * database
   */
  public LibrarySearchResults(
    List<Library> unsortedResults,
    LibraryViewerController libraryViewerController)
  {
    super(unsortedResults);
    _libraryViewerController = libraryViewerController;
  }

  
  // implementations of the SearchResults abstract methods
  
  @Override
  protected List<String> getColumnHeaders()
  {
    List<String> columnHeaders = new ArrayList<String>();
    columnHeaders.add(SHORT_NAME);
    columnHeaders.add(LIBRARY_NAME);
    columnHeaders.add(LIBRARY_TYPE);
    columnHeaders.add(START_PLATE);
    columnHeaders.add(END_PLATE);
    return columnHeaders;
  }
  
  @Override
  protected boolean isCommandLink(String columnName)
  {
    return columnName.equals(SHORT_NAME);
  }
  
  @Override
  protected boolean isCommandLinkList(String columnName)
  {
    return false;
  }
  
  @Override
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
  
  @Override
  protected Object cellAction(Library library, String columnName)
  {
    _libraryViewerController.setLibrary(library);
    return "showLibrary";
  }
  
  @Override
  protected Comparator<Library> getComparatorForColumnName(String columnName)
  {
    if (columnName.equals(SHORT_NAME)) {
      return new Comparator<Library>() {
        public int compare(Library l1, Library l2) {
          return l1.getShortName().compareTo(l2.getShortName());
        }
      };
    }
    if (columnName.equals(LIBRARY_NAME)) {
      return new Comparator<Library>() {
        public int compare(Library l1, Library l2) {
          return l1.getLibraryName().compareTo(l2.getLibraryName());
        }
      };
    }
    if (columnName.equals(LIBRARY_TYPE)) {
      return new Comparator<Library>() {
        public int compare(Library l1, Library l2) {
          return l1.getLibraryType().getValue().compareTo(l2.getLibraryType().getValue());
        }
      };
    }
    if (columnName.equals(START_PLATE)) {
      return new Comparator<Library>() {
        public int compare(Library l1, Library l2) {
          return l1.getStartPlate().compareTo(l2.getStartPlate());
        }
      };
    }
    if (columnName.equals(END_PLATE)) {
      return new Comparator<Library>() {
        public int compare(Library l1, Library l2) {
          return l1.getEndPlate().compareTo(l2.getEndPlate());
        }
      };
    }
    return null;
  }


  @Override
  protected void setEntityToView(Library entity)
  {
    _libraryViewerController.setLibrary(entity);
  }
}
