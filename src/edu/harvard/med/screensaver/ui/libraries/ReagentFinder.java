// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.io.libraries.ReagentVendorIdentifierListParser;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.UICommand;
import edu.harvard.med.screensaver.ui.searchresults.WellSearchResults;
import edu.harvard.med.screensaver.ui.util.UISelectOneBean;
import edu.harvard.med.screensaver.util.Pair;

import org.apache.log4j.Logger;

public class ReagentFinder extends AbstractBackingBean
{

  // private static final fields

  private static final Logger log = Logger.getLogger(ReagentFinder.class);

  private static final String DEFAULT_VENDOR = "Dharmacon"; // TODO: hack, convenience for RNAi Global consortium users


  // private instance fields

  private GenericEntityDAO _dao;
  private LibrariesDAO _librariesDao;
  private ReagentVendorIdentifierListParser _reagentVendorIdentifierListParser;
  private WellSearchResults _wellsBrowser;

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
                       WellSearchResults wellsBrowser,
                       ReagentVendorIdentifierListParser reagentVendorIdentifierListParser)
  {
    _dao = dao;
    _librariesDao = librariesDao;
    _wellsBrowser = wellsBrowser;
    _reagentVendorIdentifierListParser = reagentVendorIdentifierListParser;
    Collection<String> vendorNames = _librariesDao.findAllVendorNames();
    _vendorSelector =
      vendorNames.contains(DEFAULT_VENDOR) ? new UISelectOneBean<String>(vendorNames, DEFAULT_VENDOR)
                                           : new UISelectOneBean<String>(vendorNames);
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

  public UISelectOneBean<String> getVendorSelector()
  {
    return _vendorSelector;
  }

  /**
   * Find the reagents specified in the reagent vendor identifier list, and view
   * results in the Reagents Browser.
   */
  @UICommand
  public String findReagents()
  {
    final String[] result = new String[1];
    _dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        try {
          ReagentVendorIdentifierParserResult parseResult =
            _reagentVendorIdentifierListParser.parseReagentVendorIdentifiers(_vendorSelector.getSelection(),
                                                                             _reagentVendorIdentifierList);
          // display parse errors before proceeding with successfully parsed ReagentVendorIdentifiers
          for (Pair<Integer,String> error : parseResult.getErrors()) {
            showMessage("libraries.reagentVendorIdentifierListParseError", error.getSecond());
          }

          Set<ReagentVendorIdentifier> foundReagentIds = new HashSet<ReagentVendorIdentifier>();
          for (ReagentVendorIdentifier reagentVendorIdentifier : parseResult.getParsedReagentVendorIdentifiers()) {
            // TODO: eliminate this dao call here; it's wasteful; make this check when loading the data later on
            Set<Reagent> reagents = _librariesDao.findReagents(reagentVendorIdentifier, true);
            if (reagents.size() > 0) {
              foundReagentIds.add(reagentVendorIdentifier);
            }
            else {
              showMessage("libraries.noSuchReagent", reagentVendorIdentifier);
            }
          }

          if (foundReagentIds.size() == 0) {
            result[0] = REDISPLAY_PAGE_ACTION_RESULT;
          }
          else {
            _wellsBrowser.searchReagents(foundReagentIds);
            result[0] = BROWSE_WELLS;
          }
        }
        catch (RuntimeException e) {
          log.error("on find reagents: ", e);
          throw e;
        }
      }
    });
    return result[0];
  }
}
