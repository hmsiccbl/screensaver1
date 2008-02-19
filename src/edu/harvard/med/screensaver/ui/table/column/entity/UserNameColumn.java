// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table.column.entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.PropertyPath;
import edu.harvard.med.screensaver.model.RelationshipPath;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.ui.util.ScreensaverUserComparator;
import edu.harvard.med.screensaver.util.NullSafeComparator;

public abstract class UserNameColumn<E extends AbstractEntity> extends TextEntityColumn<E>/*MultiPropertyColumn<T,String>*/
{
  public UserNameColumn(RelationshipPath<E> userEntityPath,
                        String name,
                        String description/*,
                        GenericEntityDAO dao*/, String group)
  {
    super(userEntityPath/*makePropertyPaths(userEntityPath)*/,
          name,
          description/*,
          ColumnType.TEXT*/, 
          group);
    //new AbstractEntityConverter<ScreensaverUser>(ScreensaverUser.class, dao));
  }

  private static <E2 extends AbstractEntity> List<PropertyPath<E2>> makePropertyPaths(RelationshipPath<E2> userEntityPath)
  {
    List<PropertyPath<E2>> propertyPaths = new ArrayList<PropertyPath<E2>>();
    if (userEntityPath != null) {
      propertyPaths.add(new PropertyPath<E2>(userEntityPath, "lastName"));
      propertyPaths.add(new PropertyPath<E2>(userEntityPath, "firstName"));
    }
    return propertyPaths;
  }

  protected abstract ScreensaverUser getUser(E t);

  @Override
  public String/*List<String>*/ getCellValue(E t)
  {
    return getUser(t).getFullNameLastFirst();
//    return Arrays.asList(getUser(t).getLastName(),
//                         getUser(t).getFirstName());
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
