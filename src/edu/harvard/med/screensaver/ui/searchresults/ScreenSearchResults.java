// $HeadURL: svn+ssh://js163@orchestra/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.ui.screens.ScreenViewer;
import edu.harvard.med.screensaver.ui.util.ScreensaverUserComparator;


/**
 * A {@link SearchResults} for {@link Screen Screens}.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class ScreenSearchResults extends SearchResults<Screen>
{
  
  // private static final fields
  
  private static final String SCREEN_NUMBER = "Screen Number";
  private static final String LEAD_SCREENER = "Lead Screener";
  private static final String TITLE = "Title";
  
  
  // instance fields
  
  private ScreenViewer _screenViewerController;
  
  
  // public constructor
  
  /**
   * Construct a new <code>ScreenSearchResult</code> object.
   * @param unsortedResults the unsorted list of the results, as they are returned from the
   * database
   */
  public ScreenSearchResults(
    List<Screen> unsortedResults,
    ScreenViewer screenViewerController)
  {
    super(unsortedResults);
    _screenViewerController = screenViewerController;
  }


  // implementations of the SearchResults abstract methods
  
  @Override
  public String showSummaryView()
  {
    // NOTE: if there were more ways to get to a screen search results, then this method would
    // need to be more intelligent
    
    // TODO: may want to initialize the screens browser here as well, eg,
    // "return _screensController.browseScreens();", but i would like to wait until control is
    // factored out
    
    return "goBrowseScreens";
  }
  
  @Override
  protected List<String> getColumnHeaders()
  {
    List<String> columnHeaders = new ArrayList<String>();
    columnHeaders.add(SCREEN_NUMBER);
    columnHeaders.add(LEAD_SCREENER);
    columnHeaders.add(TITLE);
    return columnHeaders;
  }

  @Override
  protected boolean isCommandLink(String columnName)
  {
    return columnName.equals(SCREEN_NUMBER);
  }
  
  @Override
  protected boolean isCommandLinkList(String columnName)
  {
    return false;
  }
  
  @Override
  protected Object getCellValue(Screen screen, String columnName)
  {
    if (columnName.equals(SCREEN_NUMBER)) {
      return screen.getScreenNumber();
    }
    if (columnName.equals(LEAD_SCREENER)) {
      return screen.getLeadScreener().getFullName();
    }
    if (columnName.equals(TITLE)) {
      return screen.getTitle();
    }
    return null;
  }

  @Override
  protected Object cellAction(Screen screen, String columnName)
  {
    _screenViewerController.setScreen(screen);
    _screenViewerController.setSearchResults(this);
    return "showScreen";
  }
  
  @Override
  protected Comparator<Screen> getComparatorForColumnName(String columnName)
  {
    if (columnName.equals(SCREEN_NUMBER)) {
      return new Comparator<Screen>() {
        public int compare(Screen s1, Screen s2) {
          return s1.getScreenNumber().compareTo(s2.getScreenNumber());
        }
      };
    }
    if (columnName.equals(LEAD_SCREENER)) {
      return new Comparator<Screen>() {
        public int compare(Screen s1, Screen s2) {
          return ScreensaverUserComparator.getInstance().compare(s1.getLeadScreener(), 
                                                                 s2.getLeadScreener());
        }
      };
    }
    if (columnName.equals(TITLE)) {
      return new Comparator<Screen>() {
        public int compare(Screen s1, Screen s2) {
          return s1.getTitle().compareTo(s2.getTitle());
        }
      };
    }
    return null;
  }


  @Override
  protected void setEntityToView(Screen entity)
  {
    _screenViewerController.setScreen(entity);
  }
}
