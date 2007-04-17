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

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Well;

/**
 * 
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class LibraryContentsWellSearchResultsCriteria extends WellSearchResultsCriteria
{
  // static members

  private static Logger log = Logger.getLogger(LibraryContentsWellSearchResultsCriteria.class);


  // instance data members

  private Library _library;
  private List<Well> _results;
  
  
  // public constructors and methods
  
  public LibraryContentsWellSearchResultsCriteria(DAO dao, Library library)
  {
    super(dao);
    _library = library;
  }
  
  public List<Well> getResults()
  {
    _dao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        _library = (Library) _dao.reloadEntity(_library);
        _dao.need(_library,
          "hbnWells",
          "hbnWells.hbnSilencingReagents",
          "hbnWells.hbnCompounds");
        _results = new ArrayList<Well>(_library.getWells());
      }
    });
    return _results;
  }
  
  public List<Well> reloadResults()
  {
    return getResults();
  }
}

