// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.util.List;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.ui.control.LibrariesController;

/**
 * The criteria that characterize the {@link Well Wells} in a {@link WellSearchResults}.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public abstract class WellSearchResultsCriteria
{
  // static members

  private static Logger log = Logger.getLogger(WellSearchResultsCriteria.class);


  // instance data members
  
  protected DAO _dao;

  
  // public constructors and methods

  public WellSearchResultsCriteria(DAO dao)
  {
    _dao = dao;
  }

  /**
   * Get the results. Used to initialize the {@link WellSearchResults}.
   * @return the list of results
   */
  abstract public List<Well> getResults();
  
  /**
   * Reload and return the results. Used to reload results for
   * {@link LibrariesController#downloadWellSearchResults(WellSearchResults) downloading search
   * results}.
   * @return the reloaded list of results
   */
  abstract public List<Well> reloadResults();
}
