// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.ui.table.Criterion;
import edu.harvard.med.screensaver.ui.table.Criterion.Operator;
import edu.harvard.med.screensaver.ui.table.column.TableColumnManager;
import edu.harvard.med.screensaver.ui.table.column.entity.UserNameColumn;
import edu.harvard.med.screensaver.ui.table.model.DataTableModel;

public class UserSearchResultsTest extends AbstractSpringPersistenceTest
{
  private static Logger log = Logger.getLogger(UserSearchResultsTest.class);

  protected ScreenerSearchResults screenersBrowser;

  private ScreeningRoomUser _admin1;
  private ScreeningRoomUser _admin2;
  private ScreeningRoomUser _admin3;

  @Override
  protected void onSetUp() throws Exception
  {
    super.onSetUp();

    screenersBrowser.searchAll();
    TableColumnManager<ScreeningRoomUser> columnManager = screenersBrowser.getColumnManager();
    screenersBrowser.getColumnManager().addCompoundSortColumns(columnManager.getColumn("User"),
                                                               columnManager.getColumn("Date Created"));
    screenersBrowser.getColumnManager().setSortColumnName("Name");
    screenersBrowser.getColumnManager().setSortDirection(SortDirection.DESCENDING);

    _admin1 = MakeDummyEntities.makeDummyUser(1, "Al", "Capone");
    _admin2 = MakeDummyEntities.makeDummyUser(1, "Bugsy", "Malone");
    _admin3 = MakeDummyEntities.makeDummyUser(1, "Jesse", "James");
    genericEntityDao.persistEntity(_admin1);
    genericEntityDao.persistEntity(_admin2);
    genericEntityDao.persistEntity(_admin3);
  }

  public void testContents()
  {
    List<ScreeningRoomUser> _expectedUsers = new ArrayList<ScreeningRoomUser>();
    _expectedUsers.add(_admin2);
    _expectedUsers.add(_admin3);
    _expectedUsers.add(_admin1);

    //screenersBrowser.searchUsers();

    DataTableModel<ScreeningRoomUser> dataTableModel = screenersBrowser.getDataTableModel();
    for (int rowIndex = 0; rowIndex < _expectedUsers.size(); rowIndex++) {
      ScreeningRoomUser user = _expectedUsers.get(rowIndex);
      dataTableModel.setRowIndex(rowIndex);
      screenersBrowser.getColumnManager().getVisibleColumnModel().setRowIndex(1);
      assertEquals("row " + rowIndex + ", col 0", user.getFullNameLastFirst(), screenersBrowser.getCellValue());
    }
  }

  @SuppressWarnings("unchecked")
  public void testFilter()
  {
    //screenersBrowser.searchUsers();
    Criterion<String> criterion = new Criterion<String>(Operator.TEXT_STARTS_WITH, "Mal");
    ((UserNameColumn<ScreeningRoomUser,ScreeningRoomUser>) screenersBrowser.getColumnManager().getColumn("Name"))
    .clearCriteria().addCriterion(criterion);
    DataTableModel<ScreeningRoomUser> dataTableModel = screenersBrowser.getDataTableModel();

    assertEquals("filtered result size", 1, dataTableModel.getRowCount());
    screenersBrowser.getColumnManager().getVisibleColumnModel().setRowIndex(1);
    assertEquals("filtered data", _admin2.getFullNameLastFirst(), screenersBrowser.getCellValue());

    screenersBrowser.resetFilter();
    assertEquals("full result size, after filter reset", 3, dataTableModel.getRowCount());
  }
}
