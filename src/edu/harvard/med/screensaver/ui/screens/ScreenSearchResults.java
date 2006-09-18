// $HeadURL: svn+ssh://js163@orchestra/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screens;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.ui.SearchResults;

/**
 * @author s
 */
public class ScreenSearchResults extends SearchResults<Screen>
{
  private static final String SCREEN_NUMBER = "Screen Number";
  private static final String LEAD_SCREENER = "Lead Screener";
  private static final String TITLE = "Title";
  
  /**
   * Construct a new <code>ScreenSearchResult</code> object.
   * @param unsortedResults the unsorted list of the results, as they are returned from the
   * database
   */
  public ScreenSearchResults(List<Screen> unsortedResults)
  {
    super(unsortedResults);
  }

  public String goScreenViewer()
  {
    return "goScreenViewer";
  }
  
  protected DataModel createDataHeaderColumnModel()
  {
    List<String> tableData = new ArrayList<String>();
    tableData.add(SCREEN_NUMBER);
    tableData.add(LEAD_SCREENER);
    tableData.add(TITLE);
    return new ListDataModel(tableData);
  }

  protected boolean isCommandLink(String columnName)
  {
    return columnName.equals(SCREEN_NUMBER);
  }
  
  protected Object getCellValue(Screen screen, String columnName)
  {
    if (columnName.equals(SCREEN_NUMBER)) {
      return screen.getScreenNumber();
    }
    if (columnName.equals(LEAD_SCREENER)) {
      ScreeningRoomUser leadScreener = screen.getLeadScreener();
      return leadScreener.getFirstName() + " " + leadScreener.getLastName();      
    }
    if (columnName.equals(TITLE)) {
      return screen.getTitle();
    }
    return null;
  }

  protected Object getCellAction(Screen screen, String columnName)
  {
    return "showScreen";
  }
}
