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

import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.ui.libraries.LibraryViewer;
import edu.harvard.med.screensaver.ui.table.TableColumn;

import org.apache.log4j.Logger;


/**
 * A {@link SearchResults} for {@link Library Libraries}.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class LibrarySearchResults extends EntitySearchResults<Library>
{

  // private static final fields

  private static final Logger log = Logger.getLogger(LibrarySearchResults.class);


  // instance fields

  private LibraryViewer _libraryViewer;

  private ArrayList<TableColumn<Library,?>> _columns;


  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected LibrarySearchResults()
  {
  }

  public LibrarySearchResults(LibraryViewer libraryViewer)
  {
    _libraryViewer = libraryViewer;
  }


  // implementations of the SearchResults abstract methods

  protected List<TableColumn<Library,?>> getColumns()
  {
    if (_columns == null) {
      _columns = new ArrayList<TableColumn<Library,?>>();
      _columns.add(new TextColumn<Library>("Short Name", "The abbreviated name for the library") {
        @Override
        public String getCellValue(Library library) { return library.getShortName(); }

        @Override
        public boolean isCommandLink() { return true; }

        @Override
        public Object cellAction(Library library) { return viewCurrentEntity(); }
      });
      _columns.add(new TextColumn<Library>("Library Name", "The full name of the library") {
        @Override
        public String getCellValue(Library library) { return library.getLibraryName(); }
      });
      _columns.add(new EnumColumn<Library,ScreenType>("Screen Type", "'RNAi' or 'Small Molecule'",
        ScreenType.values()) {
        @Override
        public ScreenType getCellValue(Library library) { return library.getScreenType(); }
      });
      _columns.add(new EnumColumn<Library,LibraryType>("Library Type", "The type of library, e.g., 'Commercial', 'Known Bioactives', 'siRNA', etc.",
        LibraryType.values()) {
        @Override
        public LibraryType getCellValue(Library library) { return library.getLibraryType(); }
      });
      _columns.add(new IntegerColumn<Library>("Start Plate", "The plate number for the first plate in the library") {
        @Override
        public Integer getCellValue(Library library) { return library.getStartPlate(); }
      });
      _columns.add(new IntegerColumn<Library>("End Plate", "The plate number for the last plate in the library") {
        @Override
        public Integer getCellValue(Library library) { return library.getEndPlate(); }
      });
    }
    return _columns;
  }

  @Override
  protected void setEntityToView(Library library)
  {
    _libraryViewer.viewLibrary(library, true);
  }

  @Override
  protected List<Integer[]> getCompoundSorts()
  {
    List<Integer[]> compoundSorts = super.getCompoundSorts();
    compoundSorts.add(new Integer[] {2, 3, 0});
    compoundSorts.add(new Integer[] {3, 0});
    return compoundSorts;
  }
}
