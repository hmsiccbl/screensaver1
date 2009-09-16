// $HeadURL$
// $Id$

// Copyright 2006 by the President and Fellows of Harvard College.

// ScreensaverUsersaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.datafetcher.AllEntitiesOfTypeDataFetcher;
import edu.harvard.med.screensaver.db.datafetcher.EntitySetDataFetcher;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.DateEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.IntegerEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.TextEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.UserNameColumn;
import edu.harvard.med.screensaver.ui.users.UserViewer;
import edu.harvard.med.screensaver.util.CollectionUtils;

import org.joda.time.LocalDate;

import com.google.common.collect.Lists;


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

  private String _title;


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
    setTitle(getMessage("screensaver.ui.users.UsersBrowser.title.searchAll"));
    initialize(new AllEntitiesOfTypeDataFetcher<E,Integer>(_type, _dao));
    // default to descending sort order on user ID, to show last created first
    getColumnManager().setSortAscending(false);
  }

  public void searchUsers(Set<ScreeningRoomUser> users)
  {
    initialize(new EntitySetDataFetcher<E,Integer>(_type,
      CollectionUtils.<Integer>entityIds(users),
      _dao));
    // default to ascending sort order on user name
    getColumnManager().setSortColumnName("Name");
    getColumnManager().setSortAscending(true);
  }

  // implementations of the SearchResults abstract methods

   @Override

  protected List<? extends TableColumn<E,?>> buildColumns()
  {
    ArrayList<TableColumn<E,?>> columns = Lists.newArrayList();
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
      "Name", "The full name of the user (last, first)", TableColumn.UNGROUPED, _userViewer) {
      @Override
      protected ScreensaverUser getUser(ScreensaverUser user)
      {
        return user;
      }
    });
    columns.add(new TextEntityColumn<E>(
      new PropertyPath<E>(_type, "email"),
      "Email", "The email of the user", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(ScreensaverUser user) { return user.getEmail(); }
    });
    columns.add(new TextEntityColumn<E>(
      new PropertyPath<E>(_type, "phone"),
      "Phone", "The phone number for this user", TableColumn.ADMIN_COLUMN_GROUP) {
      @Override
      public String getCellValue(ScreensaverUser user) { return user.getPhone(); }
    });
    columns.add(new TextEntityColumn<E>(
      new PropertyPath<E>(_type, "mailingAddress"),
      "Mailing Address", "The mailing address of the user", TableColumn.ADMIN_COLUMN_GROUP) {
      @Override
      public String getCellValue(ScreensaverUser user) { return user.getMailingAddress(); }
    });
    columns.add(new TextEntityColumn<E>(
      new PropertyPath<E>(_type, "ECommonsId"),
      "eCommons ID", "The eCommons ID of the user", TableColumn.ADMIN_COLUMN_GROUP) {
      @Override
      public String getCellValue(ScreensaverUser user) { return user.getECommonsId(); }
    });
    columns.add(new TextEntityColumn<E>(
      new PropertyPath<E>(_type, "loginId"),
      "Login ID", "The login ID of the user", TableColumn.ADMIN_COLUMN_GROUP) {
      @Override
      public String getCellValue(ScreensaverUser user) { return user.getLoginId(); }
    });
    columns.add(new TextEntityColumn<E>(
      new PropertyPath<E>(_type, "harvardId"),
      "Harvard ID", "The Harvard ID of the user", TableColumn.ADMIN_COLUMN_GROUP) {
      @Override
      public String getCellValue(ScreensaverUser user) { return user.getHarvardId(); }
    });
    columns.add(new DateEntityColumn<E>(
      new PropertyPath<E>(_type, "harvardIdExpirationDate"),
      "Harvard ID Initial Expiration Date", "The date this user's Harvard ID is initially set to expire", TableColumn.ADMIN_COLUMN_GROUP) {
      protected LocalDate getDate(E user) { return user.getHarvardIdExpirationDate(); }
    });
    columns.add(new DateEntityColumn<E>(
      new PropertyPath<E>(_type, "dateCreated"),
      "Date Created",
      "The date the user's account was created", TableColumn.ADMIN_COLUMN_GROUP) {
      public LocalDate getDate(ScreensaverUser user) { return user.getDateCreated().toLocalDate(); }
    });
    return columns;
  }

  @Override
  protected void setEntityToView(ScreensaverUser user)
  {
    _userViewer.setUser(user);
  }

  public String getTitle()
  {
    return _title;
  }

  public void setTitle( String value )
  {
    _title = value;
  }
}
