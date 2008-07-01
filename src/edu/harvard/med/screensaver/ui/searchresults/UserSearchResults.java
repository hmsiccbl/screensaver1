// $HeadURL$
// $Id$

// Copyright 2006 by the President and Fellows of Harvard College.

// ScreensaverUsersaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.util.ArrayList;
import java.util.List;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.datafetcher.AllEntitiesOfTypeDataFetcher;
import edu.harvard.med.screensaver.model.PropertyPath;
import edu.harvard.med.screensaver.model.RelationshipPath;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.DateEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.EntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.IntegerEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.UserNameColumn;
import edu.harvard.med.screensaver.ui.users.UserViewer;

import org.joda.time.LocalDate;


/**
 * A {@link SearchResults} for {@link ScreensaverUser Users}.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class UserSearchResults<E extends ScreensaverUser> extends EntitySearchResults<E,Integer>
{

  // private static final fields


  // instance fields

  private GenericEntityDAO _dao;
  private Class<E> _type;
  private UserViewer _userViewer;


  // public constructor

  /**
   * @motivation for CGLIB2
   */
  protected UserSearchResults()
  {
  }

  public UserSearchResults(Class<E> type,
                           GenericEntityDAO dao,
                           UserViewer userViewer)
  {
    _type = type;
    _dao = dao;
    _userViewer = userViewer;
  }

  public void searchUsers()
  {
    initialize(new AllEntitiesOfTypeDataFetcher<E,Integer>(_type, _dao));
  }


  // implementations of the SearchResults abstract methods

   @Override
  protected List<? extends TableColumn<E,?>> buildColumns()
  {
    ArrayList<EntityColumn<E,?>> columns = new ArrayList<EntityColumn<E,?>>();
    columns.add(new IntegerEntityColumn<E>(
      new PropertyPath<E>(_type, "userId"),
      "User ID",
      "The user ID",
      TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(E user) { return user.getScreensaverUserId(); }

      @Override
      public Object cellAction(E activity) { return viewSelectedEntity(); }

      @Override
      public boolean isCommandLink() { return true; }
    });
    columns.add(new UserNameColumn<E>(
      new RelationshipPath<E>(_type, ""),
      "User", "The name of the user (last, first)", TableColumn.UNGROUPED) {
      @Override
      protected ScreensaverUser getUser(ScreensaverUser user)
      {
        return user;
      }
    });
    columns.add(new DateEntityColumn<E>(
      new PropertyPath<E>(_type, "dateCreated"),
      "Date Created",
      "The date the user's account was created", TableColumn.UNGROUPED) {
      public LocalDate getDate(ScreensaverUser user) { return user.getDateCreated().toLocalDate(); }
    });

//    TableColumnManager<ScreensaverUser> columnManager = getColumnManager();
//    columnManager.addCompoundSortColumns(columnManager.getColumn("User"),
//                                         columnManager.getColumn("Date Created"));
//    columnManager.addCompoundSortColumns(columnManager.getColumn("Date Created"),
//                                         columnManager.getColumn("User"));

    return columns;
  }

  @Override
  protected void setEntityToView(ScreensaverUser user)
  {
    _userViewer.setUser(user);
  }
}
