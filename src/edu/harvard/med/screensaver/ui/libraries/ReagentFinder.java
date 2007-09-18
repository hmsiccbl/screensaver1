// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/ui/libraries/WellFinder.java $
// $Id: WellFinder.java 1760 2007-08-31 15:00:43Z ant4 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.io.libraries.PlateWellListParser;
import edu.harvard.med.screensaver.io.libraries.PlateWellListParserResult;
import edu.harvard.med.screensaver.io.libraries.ReagentVendorIdentifierListParser;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.ui.searchresults.ReagentSearchResults;
import edu.harvard.med.screensaver.ui.searchresults.WellSearchResults;
import edu.harvard.med.screensaver.ui.util.UISelectOneBean;
import edu.harvard.med.screensaver.util.Pair;

import org.apache.log4j.Logger;

/**
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class ReagentFinder extends AbstractBackingBean
{

  // private static final fields

  private static final Logger log = Logger.getLogger(ReagentFinder.class);

  private static final ScreensaverUserRole ADMIN_ROLE = ScreensaverUserRole.LIBRARIES_ADMIN;


  // private instance fields

  private GenericEntityDAO _dao;
  private LibrariesDAO _librariesDao;
  private ReagentVendorIdentifierListParser _reagentVendorIdentifierListParser;
  private ReagentSearchResults _reagentsBrowser;
  private ReagentViewer _reagentViewer;


  private ReagentVendorIdentifier _reagentVendorIdentifier;

  private UISelectOneBean<String> _vendorSelector;
  private String _reagentVendorIdentifierList;


  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected ReagentFinder()
  {
  }

  public ReagentFinder(GenericEntityDAO dao,
                       LibrariesDAO librariesDao,
                       ReagentSearchResults reagentsBrowser,
                       ReagentViewer reagentViewer,
                       ReagentVendorIdentifierListParser reagentVendorIdentifierListParser)
  {
    _dao = dao;
    _librariesDao = librariesDao;
    _reagentsBrowser = reagentsBrowser;
    _reagentViewer = reagentViewer;
    _reagentVendorIdentifierListParser = reagentVendorIdentifierListParser;
    _vendorSelector = new UISelectOneBean<String>(_librariesDao.findAllVendorNames());
  }


  // public instance methods

  public ReagentVendorIdentifier getReagentVendorIdentifier()
  {
    return _reagentVendorIdentifier;
  }

  public void setReagentVendorIdentifier(ReagentVendorIdentifier reagentVendorIdentifierList)
  {
    _reagentVendorIdentifier = reagentVendorIdentifierList;
  }

  public String getReagentVendorIdentifierList()
  {
    return _reagentVendorIdentifierList;
  }

  public void setReagentVendorIdentifierList(String reagentVendorIdentifierList)
  {
    _reagentVendorIdentifierList = reagentVendorIdentifierList;
  }

  @Override
  protected ScreensaverUserRole getEditableAdminRole()
  {
    return ADMIN_ROLE;
  }

  public UISelectOneBean<String> getVendorSelector()
  {
    return _vendorSelector;
  }

  /**
   * Find the reagent with the specified reagent vendor ID, and display in the
   * Reagent Viewer.
   */
  @UIControllerMethod
  public String findReagent()
  {
    List<Well> result = _librariesDao.findReagentWellsByVendorId(_reagentVendorIdentifier);
    if (result.size() > 0) {
      return _reagentViewer.viewReagent(result.get(0));
    }
    else {
      showMessage("libraries.noSuchReagent", _reagentVendorIdentifier);
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
  }

  /**
   * Find the reagents specified in the reagent vendor identifier list, and view
   * results in the Reagents Browser.
   */
  @UIControllerMethod
  public String findReagents()
  {
    final String[] result = new String[1];
    _dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        ReagentVendorIdentifierParserResult parseResult =
          _reagentVendorIdentifierListParser.parseReagentVendorIdentifiers(_vendorSelector.getSelection(),
                                                                           _reagentVendorIdentifierList);
        if (parseResult.getParsedReagentVendorIdentifiers().size() == 1) {
          _reagentVendorIdentifier = parseResult.getParsedReagentVendorIdentifiers().first();
          result[0] = findReagent();
          return;
        }

        // display parse errors before proceeding with successfully parsed ReagentVendorIdentifiers
        for (Pair<Integer,String> error : parseResult.getErrors()) {
          showMessage("libraries.reagentVendorIdentifierListParseError", error.getSecond());
        }

        List<Well> foundWells = new ArrayList<Well>();
        for (ReagentVendorIdentifier reagentVendorIdentifier : parseResult.getParsedReagentVendorIdentifiers()) {
          List<Well> reagentWells = _librariesDao.findReagentWellsByVendorId(reagentVendorIdentifier);
          Well well = null;
          if (reagentWells.size() > 0) {
            well = _dao.findEntityById(Well.class,
                                       reagentWells.get(0).getWellKey().toString(),
                                       true,
                                       "hbnSilencingReagents.gene",
                                       "hbnCompounds");
            if (well != null) {
              foundWells.add(well);
            }
          }
          if (well == null) {
            showMessage("libraries.noSuchReagent", reagentVendorIdentifier);
          }
        }
        _reagentsBrowser.setContents(foundWells);
        result[0] = VIEW_REAGENT_SEARCH_RESULTS;
      }
    });
    return result[0];
  }
}
