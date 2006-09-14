// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/ui/ScreenViewerController.java $
// $Id: ScreenViewerController.java 443 2006-08-09 20:43:32Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screens;

import java.util.List;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.ui.AbstractController;
import edu.harvard.med.screensaver.ui.SearchResults;
import edu.harvard.med.screensaver.ui.SearchResultsRegistryController;

/**
 * TODO: add comments
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class ScreensBrowserController extends AbstractController
{
  
  // private static fields
  
  private static Logger log = Logger.getLogger(ScreensBrowserController.class);
  
  
  // private instance fields
  
  private DAO _dao;
  private SearchResultsRegistryController _searchResultsRegistry;
  
  
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
  
  public String goBrowseScreens()
  {
    if (_searchResultsRegistry.getSearchResultsRegistrant(Screen.class) != this) {
      List<Screen> screens = _dao.findAllEntitiesWithType(Screen.class);
      SearchResults<Screen> searchResults = new ScreenSearchResults(screens);
      _searchResultsRegistry.registerSearchResults(Screen.class, this, searchResults);
    }
    _searchResultsRegistry.setCurrentSearchType(Screen.class);
    return "goBrowseScreens";
  }
}
