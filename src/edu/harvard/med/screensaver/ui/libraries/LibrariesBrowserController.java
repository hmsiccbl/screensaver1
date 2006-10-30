// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/ui/LibraryViewerController.java $
// $Id: LibraryViewerController.java 443 2006-08-09 20:43:32Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.util.List;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.ui.AbstractController;
import edu.harvard.med.screensaver.ui.SearchResults;
import edu.harvard.med.screensaver.ui.SearchResultsRegistryController;

/**
 * TODO: add comments
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class LibrariesBrowserController extends AbstractController
{
  
  // private static fields
  
  private static Logger log = Logger.getLogger(LibrariesBrowserController.class);
  
  
  // private instance fields
  
  private DAO _dao;
  private SearchResultsRegistryController _searchResultsRegistry;
  private LibraryViewerController _libraryViewerController;
  private SearchResults<Library> _searchResults;
  
  
  // public instance methods
  
  public DAO getDao()
  {
    return _dao;
  }
  
  public void setDao(DAO dao)
  {
    _dao = dao;
  }

  public SearchResultsRegistryController getSearchResultsRegistry()
  {
    return _searchResultsRegistry;
  }

  public void setSearchResultsRegistry(SearchResultsRegistryController searchResultsRegistry)
  {
    _searchResultsRegistry = searchResultsRegistry;
  }
  
  public LibraryViewerController getLibraryViewer()
  {
    return _libraryViewerController;
  }

  public void setLibraryViewer(LibraryViewerController viewerController)
  {
    _libraryViewerController = viewerController;
  }

  public SearchResults<Library> getSearchResults()
  {
    return _searchResults;
  }

  public void setSearchResults(SearchResults<Library> searchResults)
  {
    _searchResults = searchResults;
  }

  public String goBrowseLibraries()
  {
    if (_searchResultsRegistry.getSearchResultsRegistrant(Library.class) != this) {
      List<Library> libraries = _dao.findAllEntitiesWithType(Library.class);
      _searchResults = new LibrarySearchResults(libraries, _libraryViewerController);
      _searchResultsRegistry.registerSearchResults(Library.class, this, _searchResults);
    }
    _searchResultsRegistry.setCurrentSearchType(Library.class);
    return "goBrowseLibraries";
  }
}
