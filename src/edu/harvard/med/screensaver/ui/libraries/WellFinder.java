// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.io.libraries.PlateWellListParser;
import edu.harvard.med.screensaver.io.libraries.PlateWellListParserResult;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.UICommand;
import edu.harvard.med.screensaver.ui.searchresults.WellSearchResults;
import edu.harvard.med.screensaver.util.Pair;

/**
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class WellFinder extends AbstractBackingBean
{

  // private static final fields

  private static final Logger log = Logger.getLogger(WellFinder.class);


  // private instance fields

  private GenericEntityDAO _dao;
  private LibrariesDAO _librariesDao;
  private PlateWellListParser _plateWellListParser;
  private WellSearchResults _wellsBrowser;
  private WellViewer _wellViewer;
  private WellCopyVolumeSearchResults _wellCopyVolumesBrowser;

  private String _plateNumber;
  private String _wellName;
  private String _plateWellList;


  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected WellFinder()
  {
  }

  public WellFinder(GenericEntityDAO dao,
                    LibrariesDAO librariesDao,
                    WellSearchResults wellsBrowser,
                    WellViewer wellViewer,
                    WellCopyVolumeSearchResults wellCopyVolumesBrowser,
                    PlateWellListParser plateWellListParser)
  {
    _dao = dao;
    _librariesDao = librariesDao;
    _wellsBrowser = wellsBrowser;
    _wellViewer = wellViewer;
    _wellCopyVolumesBrowser = wellCopyVolumesBrowser;
    _plateWellListParser = plateWellListParser;
  }


  // public instance methods

  public String getPlateNumber()
  {
    return _plateNumber;
  }

  public void setPlateNumber(String plateNumber)
  {
    _plateNumber = plateNumber;
  }

  public String getWellName()
  {
    return _wellName;
  }

  public void setWellName(String wellName)
  {
    _wellName = wellName;
  }

  public String getPlateWellList()
  {
    return _plateWellList;
  }

  public void setPlateWellList(String plateWellList)
  {
    _plateWellList = plateWellList;
  }

  /**
   * Find the well with the specified plate number and well name, and go to the appropriate next
   * page depending on the result.
   * @return the control code for the appropriate next page
   */
  @UICommand
  public String findWell()
  {
    Well well = _plateWellListParser.lookupWell(_plateNumber, _wellName);
    if (well != null) {
      resetSearchFields();
      return _wellViewer.viewEntity(well);
    } else {
      showMessage("wells.plateWellNotFound", _plateNumber, _wellName);
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  /**
   * Find the wells specified in the plate-well list, and go to the {@link WellSearchResults}
   * page.
   * @return the controller code for the next appropriate page
   */
  @UICommand
  public String findWells()
  {
    PlateWellListParserResult parseResult = _plateWellListParser.parseWellsFromPlateWellList(_plateWellList);
    // display parse errors before proceeding with successfully parsed wells
    for (Pair<Integer,String> error : parseResult.getErrors()) {
      showMessage("libraries.plateWellListParseError", error.getSecond());
    }
    _wellsBrowser.searchWells(parseResult.getParsedWellKeys());
    return BROWSE_WELLS;
  }

  @UICommand
  public String findWellVolumes()
  {
    PlateWellListParserResult parseResult = _plateWellListParser.parseWellsFromPlateWellList(_plateWellList);
    // display parse errors before proceeding with successfully parsed wells
    for (Pair<Integer,String> error : parseResult.getErrors()) {
      showMessage("libraries.plateWellListParseError", error.getSecond());
    }
    _wellCopyVolumesBrowser.searchWells(parseResult.getParsedWellKeys());
    return BROWSE_WELL_VOLUMES;
  }
  
  private void resetSearchFields()
  {
    _plateNumber = null;
    _wellName = null;
    _plateWellList = null;
  }

}
