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
import edu.harvard.med.screensaver.model.users.FacilityUsageRole;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.EntityViewer;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.DateEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.IntegerEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.TextSetEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.TextEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.UserNameColumn;
import edu.harvard.med.screensaver.ui.users.UserViewer;
import edu.harvard.med.screensaver.util.CollectionUtils;

import org.joda.time.LocalDate;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;


/**
 * A {@link SearchResults} for {@link ScreensaverUser Users}.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class UserSearchResults<E extends ScreensaverUser> extends EntitySearchResults<E,Integer>
{
  private GenericEntityDAO _dao;
  private Class<E> _type;

  private String _title;


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
    super((EntityViewer<E>) userViewer);
    _type = type;
    _dao = dao;
  }

  @Override
  public void searchAll()
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
    columns.add(new UserNameColumn<E,E>(
      new RelationshipPath<E>(_type, ""),
      "Name", "The full name of the user (last, first)", TableColumn.UNGROUPED, (UserViewer) getEntityViewer()) {
      @Override
      protected E getUser(E user)
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
      "Phone", "The phone number for this user", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(ScreensaverUser user) { return user.getPhone(); }
    });
    columns.get(columns.size() - 1).setAdministrative(true);
    columns.add(new TextEntityColumn<E>(
      new PropertyPath<E>(_type, "mailingAddress"),
      "Mailing Address", "The mailing address of the user", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(ScreensaverUser user) { return user.getMailingAddress(); }
    });
    columns.get(columns.size() - 1).setAdministrative(true);
    columns.add(new TextEntityColumn<E>(
      new PropertyPath<E>(_type, "ECommonsId"),
      "eCommons ID", "The eCommons ID of the user", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(ScreensaverUser user) { return user.getECommonsId(); }
    });
    columns.get(columns.size() - 1).setAdministrative(true);
    columns.add(new TextEntityColumn<E>(
      new PropertyPath<E>(_type, "loginId"),
      "Login ID", "The login ID of the user", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(ScreensaverUser user) { return user.getLoginId(); }
    });
    columns.get(columns.size() - 1).setAdministrative(true);
    columns.get(columns.size() - 1).setVisible(false);
    columns.add(new TextEntityColumn<E>(
      new PropertyPath<E>(_type, "harvardId"),
      "Harvard ID", "The Harvard ID of the user", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(ScreensaverUser user) { return user.getHarvardId(); }
    });
    columns.get(columns.size() - 1).setAdministrative(true);
    columns.add(new DateEntityColumn<E>(
      new PropertyPath<E>(_type, "harvardIdExpirationDate"),
      "Harvard ID Initial Expiration Date", "The date this user's Harvard ID is initially set to expire", TableColumn.UNGROUPED) {
      protected LocalDate getDate(E user) { return user.getHarvardIdExpirationDate(); }
    });
    columns.get(columns.size() - 1).setAdministrative(true);
    columns.get(columns.size() - 1).setVisible(false);

    columns.add(new TextSetEntityColumn<E>(
      (RelationshipPath<E>) ScreeningRoomUser.facilityUsageRoles,
      "Facility Usage Roles",
      "Record of what the user is doing at the facility", 
      TableColumn.UNGROUPED) {
      public Set<String> getCellValue(ScreensaverUser user) 
      { 
        if (user instanceof ScreeningRoomUser) {
          Set<FacilityUsageRole> facilityUsages = Sets.newTreeSet(((ScreeningRoomUser) user).getFacilityUsageRoles());
          return Sets.newHashSet(Iterables.transform(facilityUsages, FacilityUsageRole.ToDisplayableName));
        }
        return null;
      }
    });
    columns.get(columns.size() - 1).setAdministrative(true);
    columns.get(columns.size() - 1).setVisible(false);

    columns.add(new TextSetEntityColumn<E>(
      (RelationshipPath<E>) ScreensaverUser.roles,
      "Data Access Roles",
      "The primary data access roles assigned to this user's account", 
      TableColumn.UNGROUPED) {
      public Set<String> getCellValue(ScreensaverUser user) 
      { 
        Set<ScreensaverUserRole> roles = Sets.newHashSet(user.getPrimaryScreensaverUserRoles());
        if (roles.isEmpty()) {
          return Sets.newHashSet("<not yet specified>");
        }
        return Sets.newHashSet(Iterables.transform(roles, ScreensaverUserRole.ToDisplayableRoleName)); 
      }
    });
    columns.get(columns.size() - 1).setAdministrative(true);
    columns.get(columns.size() - 1).setVisible(false);
    columns.add(new DateEntityColumn<E>(
      new PropertyPath<E>(_type, "dateCreated"),
      "Date Created",
      "The date the user's account was created", TableColumn.UNGROUPED) {
      public LocalDate getDate(ScreensaverUser user) { return user.getDateCreated().toLocalDate(); }
    });
    columns.get(columns.size() - 1).setAdministrative(true);
    return columns;
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
