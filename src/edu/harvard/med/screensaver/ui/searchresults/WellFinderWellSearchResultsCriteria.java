// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.io.libraries.PlateWellListParserResult;
import edu.harvard.med.screensaver.model.libraries.Well;

/**
 * 
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class WellFinderWellSearchResultsCriteria extends WellSearchResultsCriteria
{
  // static members

  private static Logger log = Logger.getLogger(WellFinderWellSearchResultsCriteria.class);

  
  // instance data members

  private PlateWellListParserResult _plateWellListParserResult;
  
  
  // public constructors and methods

  public WellFinderWellSearchResultsCriteria(DAO dao, PlateWellListParserResult plateWellListParserResult)
  {
    super(dao);
    _plateWellListParserResult = plateWellListParserResult;
  }

  public List<Well> getResults()
  {
    return new ArrayList<Well>(_plateWellListParserResult.getWells());
  }

  public List<Well> reloadResults()
  {
    SortedSet<Well> oldWells = _plateWellListParserResult.getWells();
    List<Well> reloadedWells = new ArrayList<Well>(oldWells.size());
    for (Well well : oldWells) {
      reloadedWells.add((Well) _dao.reattachEntity(well));
    }
    return reloadedWells;
  }
}
