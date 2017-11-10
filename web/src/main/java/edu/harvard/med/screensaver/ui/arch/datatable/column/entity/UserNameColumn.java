// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.arch.datatable.column.entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.Entity;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserComparator;
import edu.harvard.med.screensaver.ui.users.UserViewer;
import edu.harvard.med.screensaver.util.NullSafeComparator;

public abstract class UserNameColumn<E extends Entity,U extends ScreensaverUser> extends TextEntityColumn<E>/*MultiPropertyColumn<T,String>*/
{
  private UserViewer _userViewer;
  private static final Logger log = Logger.getLogger(UserNameColumn.class);

  public UserNameColumn(RelationshipPath<E> userEntityPath,
                        String name,
                        String description/*,
                        GenericEntityDAO dao*/, 
                        String group,
                        UserViewer userViewer)
  {
    super(userEntityPath/*makePropertyPaths(userEntityPath)*/,
          name,
          description/*,
          ColumnType.TEXT*/, 
          group);
    _userViewer = userViewer;
    //new AbstractEntityConverter<ScreensaverUser>(ScreensaverUser.class, dao));
  }

  private static <E2 extends Entity> List<PropertyPath<E2>> makePropertyPaths(RelationshipPath<E2> userEntityPath)
  {
    List<PropertyPath<E2>> propertyPaths = new ArrayList<PropertyPath<E2>>();
    if (userEntityPath != null) {
      propertyPaths.add(userEntityPath.toProperty("lastName"));
      propertyPaths.add(userEntityPath.toProperty("firstName"));
    }
    return propertyPaths;
  }

  protected abstract U getUser(E t);

  @Override
  public String/*List<String>*/ getCellValue(E t)
  {
    log.debug("Get fullname last first for user: " + t);
    ScreensaverUser user = getUser(t);
    log.debug("Get fullname last first for user: " + user);
    return user == null ? null : user.getFullNameLastFirst();
//    return Arrays.asList(getUser(t).getLastName(),
//                         getUser(t).getFirstName());
  }

  @Override
  public boolean isCommandLink() 
  { 
    return _userViewer != null;
  }
  
  @Override
  public Object cellAction(E t) 
  { 
    if (_userViewer != null) {
      return _userViewer.viewEntity((ScreeningRoomUser) getUser(t));
    }
    return null;
  }

  @Override
  protected Comparator<E> getAscendingComparator()
  {
    return new NullSafeComparator<E>() {
      public int doCompare(E t1, E t2) {
        return ScreensaverUserComparator.getInstance().compare(getUser(t1), getUser(t2));
      }
    };
  }
}
