// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/ui/screens/ScreenFinder.java $
// $Id: ScreenFinder.java 2463 2008-06-04 01:25:21Z ant4 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.users;

import java.util.ArrayList;
import java.util.List;

import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.ui.searchresults.ScreenerSearchResults;
import edu.harvard.med.screensaver.ui.table.Criterion.Operator;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;
import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.log4j.Logger;

import com.google.common.base.Join;

/**
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class ScreenerFinder extends AbstractBackingBean
{

  // private static final fields

  private static final Logger log = Logger.getLogger(ScreenerFinder.class);


  // private instance fields

  private String _lastNamePattern;
  private String _firstNamePattern;
  private ScreenerSearchResults _screenerSearchResults;


  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected ScreenerFinder()
  {
  }

  public ScreenerFinder(ScreenerSearchResults screenerSearchResults)
  {
    _screenerSearchResults = screenerSearchResults;
    _screenerSearchResults.searchUsers();
  }


  // public instance methods

  public String getLastNamePattern()
  {
    return _lastNamePattern;
  }

  public void setLastNamePattern(String lastNamePattern)
  {
    _lastNamePattern = lastNamePattern;
  }

  public String getFirstNamePattern()
  {
    return _firstNamePattern;
  }

  public void setFirstNamePattern(String firstNamePattern)
  {
    _firstNamePattern = firstNamePattern;
  }

  public String getFullNamePattern()
  {
    List<String> nameParts = new ArrayList<String>();
    if (!StringUtils.isEmpty(_lastNamePattern)) {
      nameParts.add(_lastNamePattern + "*");
    }
    else if (!StringUtils.isEmpty(_firstNamePattern)) {
      nameParts.add("*");
    }

    if (!StringUtils.isEmpty(_firstNamePattern)) {
      nameParts.add(_firstNamePattern + "*");
    }
    return Join.join(", ", nameParts);
  }

  @SuppressWarnings("unchecked")
  @UIControllerMethod
  public String findScreenerByNamePattern()
  {
    String pattern = getFullNamePattern();
    if (!StringUtils.isEmpty(pattern)) {
      _screenerSearchResults.searchUsers(); // potentially poor performance, but "correct", as it always searches lastest additions and changes to users
      TableColumn<ScreeningRoomUser,String> column = (TableColumn<ScreeningRoomUser,String>) _screenerSearchResults.getColumnManager().getColumn("Name");
      //Note: the assumption is that alphabetic sort when searching on name is desired
      _screenerSearchResults.getColumnManager().setSortColumn(column);
      _screenerSearchResults.getColumnManager().setSortDirection(SortDirection.ASCENDING);
      column.resetCriteria().setOperatorAndValue(Operator.TEXT_LIKE, pattern);
      if (_screenerSearchResults.getRowCount() == 0) {
        showMessage("users.noUserWithMatchingName", pattern);
      }
      else if (_screenerSearchResults.getRowCount() == 1) {
        _screenerSearchResults.getRowsPerPageSelector().setSelection(1);
        resetSearchFields();
      }
      else {
        _screenerSearchResults.getRowsPerPageSelector().setSelection(_screenerSearchResults.getRowsPerPageSelector().getDefaultSelection());
        resetSearchFields();
      }
      return BROWSE_SCREENERS;
    }
    else {
      showMessage("users.screenerNameRequired");
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  private void resetSearchFields()
  {
    _firstNamePattern = null;
    _lastNamePattern = null;
  }
}

