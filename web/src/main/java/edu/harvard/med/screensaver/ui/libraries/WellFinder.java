// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.util.SortedSet;

import com.google.common.base.Joiner;
import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.io.libraries.PlateWellListParser;
import edu.harvard.med.screensaver.io.libraries.PlateWellListParserResult;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.ui.arch.view.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.arch.view.aspects.UICommand;
import edu.harvard.med.screensaver.util.Pair;

/**
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class WellFinder extends AbstractBackingBean
{
  private static final Logger log = Logger.getLogger(WellFinder.class);

  private GenericEntityDAO _dao;
  private LibrariesDAO _librariesDao;
  private WellSearchResults _wellsBrowser;
  private WellCopyVolumeSearchResults _wellCopyVolumesBrowser;
  private int _maxQueryInputItems;

  private String _plateWellList;

  /**
   * @motivation for CGLIB2
   */
  protected WellFinder()
  {
  }

  public WellFinder(GenericEntityDAO dao,
                    LibrariesDAO librariesDao,
                    WellSearchResults wellsBrowser,
                    WellCopyVolumeSearchResults wellCopyVolumesBrowser)
  {
    _dao = dao;
    _librariesDao = librariesDao;
    _wellsBrowser = wellsBrowser;
    _wellCopyVolumesBrowser = wellCopyVolumesBrowser;
  }

  public String getPlateWellList()
  {
    return _plateWellList;
  }

  public void setPlateWellList(String plateWellList)
  {
    _plateWellList = plateWellList;
  }

  @UICommand
  public String findWells()
  {
    PlateWellListParserResult result = PlateWellListParser.parseWellsFromPlateWellList(_plateWellList);
    // display parse errors before proceeding with successfully parsed wells
    for (Pair<Integer,String> error : result.getErrors()) {
      showMessage("libraries.plateWellListParseError", error.getSecond());
    }
    
    SortedSet<WellKey> keysToShow = result.getParsedWellKeys();
    if(result.getParsedWellKeys().size() > getMaxQueryInputItems()) {
      showMessage("maxQueryInputSizeReached", result.getParsedWellKeys().size(), getMaxQueryInputItems());
      keysToShow = result.getFirst(getMaxQueryInputItems());
    }

    getCurrentScreensaverUser().logActivity("searching for wells: " +
                                            Joiner.on(", ").join(keysToShow));
    _wellsBrowser.searchWells(keysToShow, "Well Search Results");
    if (_wellsBrowser.getRowCount() == 1) {
      _wellsBrowser.getRowsPerPageSelector().setSelection(1);
    }
    resetSearchFields();
    return BROWSE_WELLS;
  }

  @UICommand
  public String findWellVolumes()
  {
    PlateWellListParserResult parseResult = PlateWellListParser.parseWellsFromPlateWellList(_plateWellList);
    // display parse errors before proceeding with successfully parsed wells
    for (Pair<Integer,String> error : parseResult.getErrors()) {
      showMessage("libraries.plateWellListParseError", error.getSecond());
    }
    
    SortedSet<WellKey> keysToShow = parseResult.getParsedWellKeys();
    if(parseResult.getParsedWellKeys().size() > getMaxQueryInputItems()) {
      showMessage("maxQueryInputSizeReached", parseResult.getParsedWellKeys().size(), getMaxQueryInputItems());
      keysToShow = parseResult.getFirst(getMaxQueryInputItems());
    }
    
    _wellCopyVolumesBrowser.searchWells(keysToShow);
    resetSearchFields();
    return BROWSE_WELL_VOLUMES;
  }
  
  private void resetSearchFields()
  {
    _plateWellList = null;
  }

  public void setMaxQueryInputItems(int _maxQueryInputItems)
  {
    this._maxQueryInputItems = _maxQueryInputItems;
  }

  public int getMaxQueryInputItems()
  {
    return _maxQueryInputItems;
  }
}
