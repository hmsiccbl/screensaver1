// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.util.ArrayList;
import java.util.List;

import edu.harvard.med.screensaver.io.DataExporter;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.ui.control.LibrariesController;
import edu.harvard.med.screensaver.ui.table.TableColumn;

import org.apache.log4j.Logger;


/**
 * A {@link SearchResults} for {@link Library Libraries}.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class LibrarySearchResults extends SearchResults<Library,Object>
{

  // private static final fields

  private static final Logger log = Logger.getLogger(LibrarySearchResults.class);


  // instance fields

  private LibrariesController _librariesController;

  private ArrayList<TableColumn<Library>> _columns;


  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected LibrarySearchResults()
  {
  }

  public LibrarySearchResults(LibrariesController librariesController)
  {
    _librariesController = librariesController;
  }


  // implementations of the SearchResults abstract methods

  @Override
  protected List<DataExporter<Library>> getDataExporters()
  {
    return new ArrayList<DataExporter<Library>>();
  }

  @Override
  public String showSummaryView()
  {
    // NOTE: if there were more ways to get to a library search results, then this method would
    // need to be more intelligent
    return BROWSE_LIBRARIES;
  }

  protected List<TableColumn<Library>> getColumns()
  {
    if (_columns == null) {
      _columns = new ArrayList<TableColumn<Library>>();
      _columns.add(new TableColumn<Library>("Short Name", "The abbreviated name for the library") {
        @Override
        public Object getCellValue(Library library) { return library.getShortName(); }

        @Override
        public boolean isCommandLink() { return true; }

        @Override
        public Object cellAction(Library library) { return _librariesController.viewLibrary(library); }
      });
      _columns.add(new TableColumn<Library>("Library Name", "The full name of the library") {
        @Override
        public Object getCellValue(Library library) { return library.getLibraryName(); }
      });
      _columns.add(new TableColumn<Library>("Screen Type", "'RNAi' or 'Small Molecule'") {
        @Override
        public Object getCellValue(Library library) { return library.getScreenType(); }
      });
      _columns.add(new TableColumn<Library>("Library Type", "The type of library, e.g., 'Commercial', 'Known Bioactives', 'siRNA', etc.") {
        @Override
        public Object getCellValue(Library library) { return library.getLibraryType(); }
      });
      _columns.add(new TableColumn<Library>("Start Plate", "The plate number for the first plate in the library", true) {
        @Override
        public Object getCellValue(Library library) { return library.getStartPlate(); }
      });
      _columns.add(new TableColumn<Library>("End Plate", "The plate number for the last plate in the library", true) {
        @Override
        public Object getCellValue(Library library) { return library.getEndPlate(); }
      });
    }
    return _columns;
  }

  @Override
  protected List<Integer[]> getCompoundSorts()
  {
    List<Integer[]> compoundSorts = super.getCompoundSorts();
    compoundSorts.add(new Integer[] {2, 3, 0});
    compoundSorts.add(new Integer[] {3, 0});
    return compoundSorts;
  }

  @Override
  protected void setEntityToView(Library library)
  {
    _librariesController.viewLibrary(library);
  }
}
