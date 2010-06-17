// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.util.Collection;
import java.util.SortedSet;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.io.libraries.ReagentVendorIdentifierListParser;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.UICommand;
import edu.harvard.med.screensaver.ui.searchresults.WellSearchResults;
import edu.harvard.med.screensaver.ui.util.UISelectOneBean;
import edu.harvard.med.screensaver.util.Pair;

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

  @UICommand
  public String findReagents()
  {
    ReagentVendorIdentifierParserResult parseResult =
      _reagentVendorIdentifierListParser.parseReagentVendorIdentifiers(_vendorSelector.getSelection(),
                                                                       _reagentVendorIdentifierList);
    // display parse errors before showing the search result for the successfully parsed ReagentVendorIdentifiers
    for (Pair<Integer,String> error : parseResult.getErrors()) {
      showMessage("libraries.reagentVendorIdentifierListParseError", error.getSecond());
    }
    SortedSet<ReagentVendorIdentifier> rvis = parseResult.getParsedReagentVendorIdentifiers();
    _wellsBrowser.searchReagents(rvis);
    return BROWSE_WELLS;
  }
}
