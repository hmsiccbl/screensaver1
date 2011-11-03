// $HeadURL$
// $Id$

// Copyright © 2006, 2010 by the President and Fellows of Harvard College.

// ScreensaverUsersaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.users;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.joda.time.LocalDate;

import edu.harvard.med.iccbl.screensaver.IccblScreensaverConstants;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.datafetcher.DataFetcherUtil;
import edu.harvard.med.screensaver.db.datafetcher.EntityDataFetcher;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.model.Entity;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.users.FacilityUsageRole;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TableColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.DateEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.IntegerEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.TextEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.TextSetEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.UserNameColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.model.InMemoryEntityDataModel;
import edu.harvard.med.screensaver.ui.arch.searchresults.EntityBasedEntitySearchResults;
import edu.harvard.med.screensaver.ui.arch.searchresults.SearchResults;
import edu.harvard.med.screensaver.ui.arch.view.EntityViewer;


/**
 * A {@link SearchResults} for {@link ScreensaverUser Users}.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class UserSearchResults<E extends ScreensaverUser> extends EntityBasedEntitySearchResults<E,Integer>
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
    searchAll("Screeners");
  }

  public void searchAll(String title)
  {
    setTitle(title);
    initialize(new InMemoryEntityDataModel<E,Integer,E>(new EntityDataFetcher<E,Integer>(_type, _dao)));
    // default to descending sort order on user ID, to show last created first
    getColumnManager().setSortAscending(false);
  }

  public void searchUsers(final Set<ScreeningRoomUser> users, String title)
  {
    setTitle(title);
    initialize(new InMemoryEntityDataModel<E,Integer,E>(new EntityDataFetcher<E,Integer>(_type, _dao) {
      @Override
      public void addDomainRestrictions(HqlBuilder hql)
      {
        DataFetcherUtil.addDomainRestrictions(hql, getRootAlias(), Sets.newHashSet(Iterables.transform(users, Entity.ToEntityId)));
      }
    }));
    
    // default to ascending sort order on user name
    getColumnManager().setSortColumnName("Name");
    getColumnManager().setSortAscending(true);
  }

  // implementations of the SearchResults abstract methods

   @Override

  protected List<? extends TableColumn<E,?>> buildColumns()
  {
    ArrayList<TableColumn<E,?>> columns = Lists.newArrayList();
    RelationshipPath<E> pathRoot = RelationshipPath.from(_type);
    columns.add(new IntegerEntityColumn<E>(pathRoot.toProperty("userId"),
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
    columns.add(new UserNameColumn<E,E>(pathRoot,
                                        "Name",
                                        "The full name of the user (last, first)",
                                        TableColumn.UNGROUPED,
                                        (UserViewer) getEntityViewer()) {
      @Override
      protected E getUser(E user)
      {
        return user;
      }
    });
    columns.add(new TextEntityColumn<E>(pathRoot.toProperty("email"),
                                        "Email",
                                        "The email of the user",
                                        TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(ScreensaverUser user) { return user.getEmail(); }
    });
    columns.add(new TextEntityColumn<E>(pathRoot.toProperty("phone"),
                                        "Phone",
                                        "The phone number for this user",
                                        TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(ScreensaverUser user) { return user.getPhone(); }
    });
    columns.get(columns.size() - 1).setAdministrative(true);
    columns.add(new TextEntityColumn<E>(pathRoot.toProperty("mailingAddress"),
                                        "Mailing Address",
                                        "The mailing address of the user",
                                        TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(ScreensaverUser user) { return user.getMailingAddress(); }
    });
    columns.get(columns.size() - 1).setAdministrative(true);

    if (getApplicationProperties().isFeatureEnabled("manage_authentication_credentials")) {
      columns.add(new TextEntityColumn<E>(pathRoot.toProperty("loginId"),
                                          "Login ID",
                                          "The login ID of the user",
                                          TableColumn.UNGROUPED) {
        @Override
        public String getCellValue(ScreensaverUser user)
        {
          return user.getLoginId();
        }
      });
      columns.get(columns.size() - 1).setAdministrative(true);
      columns.get(columns.size() - 1).setVisible(false);
    }

    if (IccblScreensaverConstants.FACILITY_KEY.equals(getApplicationProperties().getProperty(FACILITY_NAME))) {
      columns.add(new TextEntityColumn<E>(pathRoot.toProperty("ECommonsId"),
                                          "eCommons ID",
                                          "The eCommons ID of the user",
                                          TableColumn.UNGROUPED) {
        @Override
        public String getCellValue(ScreensaverUser user)
        {
          return user.getECommonsId();
        }
      });
      columns.get(columns.size() - 1).setAdministrative(true);

      columns.add(new TextEntityColumn<E>(pathRoot.toProperty("harvardId"),
                                          "Harvard ID",
                                          "The Harvard ID of the user",
                                          TableColumn.UNGROUPED) {
        @Override
        public String getCellValue(ScreensaverUser user)
        {
          return user.getHarvardId();
        }
      });
      columns.get(columns.size() - 1).setAdministrative(true);

      columns.add(new DateEntityColumn<E>(pathRoot.toProperty("harvardIdExpirationDate"),
                                          "Harvard ID Initial Expiration Date",
                                          "The date this user's Harvard ID is initially set to expire",
                                          TableColumn.UNGROUPED) {
        protected LocalDate getDate(E user)
        {
          return user.getHarvardIdExpirationDate();
        }
      });
      columns.get(columns.size() - 1).setAdministrative(true);
      columns.get(columns.size() - 1).setVisible(false);
    }

    columns.add(new TextSetEntityColumn<E>(pathRoot.to(ScreeningRoomUser.facilityUsageRoles), // convert to a path with same root type as all other column paths 
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

    columns.add(new TextSetEntityColumn<E>(pathRoot.to(ScreensaverUser.roles), // convert to a path with same root type as all other column paths
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
    columns.add(new DateEntityColumn<E>(pathRoot.toProperty("dateCreated"),
                                        "Date Recorded",
                                        "The date the user's account was first recorded in Screensaver",
                                        TableColumn.UNGROUPED) {
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
