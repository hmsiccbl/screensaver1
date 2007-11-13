// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.util.Comparator;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.ui.util.ScreensaverUserComparator;
import edu.harvard.med.screensaver.util.NullSafeComparator;

public abstract class UserNameColumn<T> extends TextColumn<T>
{
  public UserNameColumn(String name,
                    String description,
                    GenericEntityDAO dao)
  {
    super(name,
          description);
          //new AbstractEntityConverter<ScreensaverUser>(ScreensaverUser.class, dao));
  }

  protected abstract ScreensaverUser getUser(T t);

  @Override
  public String getCellValue(T t)
  {
    return getUser(t).getFullNameLastFirst();
  }

  @Override
  protected Comparator<T> getAscendingComparator()
  {
    return new NullSafeComparator<T>() {
      public int doCompare(T t1, T t2) {
        return ScreensaverUserComparator.getInstance().compare(getUser(t1), getUser(t2));
      }
    };
  }

}
