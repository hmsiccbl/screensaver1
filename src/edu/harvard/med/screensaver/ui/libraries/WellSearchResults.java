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

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.ui.SearchResults;


/**
 * A {@link SearchResults} for {@link Well Wells}.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class WellSearchResults extends SearchResults<Well>
{
  
  // private static final fields
  
  private static final Logger log = Logger.getLogger(WellSearchResults.class);

  private static final String LIBRARY   = "Library";
  private static final String PLATE     = "Plate";
  private static final String WELL      = "Well";
  private static final String WELL_TYPE = "Well Type";
  
  
  // instance fields
  
  private LibraryViewerController _libraryViewerController;
  private WellViewerController _wellViewerController;
  
  
  // public constructor
  
  /**
   * Construct a new <code>WellSearchResults</code> object.
   * @param wells the list of wells
   */
  public WellSearchResults(
    List<Well> unsortedResults,
    LibraryViewerController libraryViewerController,
    WellViewerController wellViewerController)
  {
    super(unsortedResults);
    _libraryViewerController = libraryViewerController;
    _wellViewerController = wellViewerController;
  }

  
  // implementations of the SearchResults abstract methods

  @Override
  protected List<String> getColumnHeaders()
  {
    List<String> columnHeaders = new ArrayList<String>();
    columnHeaders.add(LIBRARY);
    columnHeaders.add(PLATE);
    columnHeaders.add(WELL);
    columnHeaders.add(WELL_TYPE);
    return columnHeaders;
  }
  
  @Override
  protected boolean isCommandLink(String columnName)
  {
    return columnName.equals(LIBRARY) || columnName.equals(WELL);
  }
  
  @Override
  protected Object getCellValue(Well well, String columnName)
  {
    if (columnName.equals(LIBRARY)) {
      return well.getLibrary().getShortName();
    }
    if (columnName.equals(PLATE)) {
      return well.getPlateNumber();
    }
    if (columnName.equals(WELL)) {
      return well.getWellName();
    }
    if (columnName.equals(WELL_TYPE)) {
      return well.getWellType();
    }
    return null;
  }
  
  @Override
  protected Object getCellAction(Well well, String columnName)
  {
    if (columnName.equals(LIBRARY)) {
      _libraryViewerController.setLibrary(well.getLibrary());
      return "showLibrary";
    }
    if (columnName.equals(WELL)) {
      _wellViewerController.setWell(well);
      return "showWell";
    }
    return null;
  }
  
  @Override
  protected Comparator<Well> getComparatorForColumnName(String columnName)
  {
    if (columnName.equals(LIBRARY)) {
      return new Comparator<Well>() {
        public int compare(Well w1, Well w2) {
          return w1.getLibrary().getShortName().compareTo(w2.getLibrary().getShortName());
        }
      };
    }
    if (columnName.equals(PLATE)) {
      return new Comparator<Well>() {
        public int compare(Well w1, Well w2) {
          return w1.getPlateNumber().compareTo(w2.getPlateNumber());
        }
      };
    }
    if (columnName.equals(WELL)) {
      return new Comparator<Well>() {
        public int compare(Well w1, Well w2) {
          return w1.getWellName().compareTo(w2.getWellName());
        }
      };
    }
    if (columnName.equals(WELL_TYPE)) {
      return new Comparator<Well>() {
        public int compare(Well w1, Well w2) {
          return w1.getWellType().compareTo(w2.getWellType());
        }
      };
    }
    return null;
  }
}
