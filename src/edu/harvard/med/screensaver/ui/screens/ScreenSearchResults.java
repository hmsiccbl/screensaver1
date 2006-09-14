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

  /**
   * Construct a new <code>ScreenSearchResult</code> object.
   * @param unsortedResults the unsorted list of the results, as they are returned from the
   * database
   */
  public ScreenSearchResults(List<Screen> unsortedResults)
  {
    super(unsortedResults);
  }
  

  public Object getRawDataCellValue()
  {
    DataModel dataModel = getDataModel();
    Screen screen = (Screen) dataModel.getRowData();
    DataModel columnModel = getDataHeaderColumnModel();
    String columnName = (String) columnModel.getRowData();
    if (columnName.equals("Screen Number")) {
      return screen.getScreenNumber();
    }
    if (columnName.equals("Lead Screener")) {
      ScreeningRoomUser leadScreener = screen.getLeadScreener();
      return leadScreener.getFirstName() + " " + leadScreener.getLastName();      
    }
    if (columnName.equals("Title")) {
      return screen.getTitle();
    }
    return null;
  }
  

  protected DataModel createDataHeaderColumnModel()
  {
    List<String> tableData = new ArrayList<String>();
    tableData.add("Screen Number");
    tableData.add("Lead Screener");
    tableData.add("Title");
    return new ListDataModel(tableData);
  }
}
