// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.util.HashSet;
import java.util.Set;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.io.libraries.PlateWellListParser;
import edu.harvard.med.screensaver.io.libraries.PlateWellListParserResult;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.UICommand;
import edu.harvard.med.screensaver.ui.searchresults.WellSearchResults;
import edu.harvard.med.screensaver.util.Pair;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

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
  @Transactional
  public String findWells()
  {
    String result = null;
    PlateWellListParserResult parseResult = _plateWellListParser.parseWellsFromPlateWellList(_plateWellList);
    // display parse errors before proceeding with successfully parsed wells
    for (Pair<Integer,String> error : parseResult.getErrors()) {
      showMessage("libraries.plateWellListParseError", error.getSecond());
    }

    Set<WellKey> foundWells = new HashSet<WellKey>();
    for (WellKey wellKey : parseResult.getParsedWellKeys()) {
      // TODO: eliminate this dao call here; it's wasteful; make this check when loading the data later on
      Well well = _dao.findEntityById(Well.class,
                                      wellKey.toString(),
                                      true);
      if (well == null) {
        showMessage("libraries.noSuchWell", wellKey.getPlateNumber(), wellKey.getWellName());
      }
      else {
        foundWells.add(well.getWellKey());
      }
    }

    if (foundWells.size() > 0) {
      _wellsBrowser.searchWells(foundWells);
      if (foundWells.size() == 1) {
        _wellsBrowser.getRowsPerPageSelector().setSelection(1);
      }
      return BROWSE_WELLS;
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  /**
   * Find the volumes for all copies of the wells specified in the plate-well
   * list, and go to the {@link WellSearchResults} page.
   *
   * @return the controller code for the next appropriate page
   */
  @UICommand
  public String findWellVolumes()
  {
    final String[] result = new String[1];
    _dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        PlateWellListParserResult parseResult = _plateWellListParser.parseWellsFromPlateWellList(_plateWellList);

        // display parse errors before proceeding with successfully parsed wells
        for (Pair<Integer,String> error : parseResult.getErrors()) {
          showMessage("libraries.plateWellListParseError", error.getSecond());
        }

        _wellCopyVolumesBrowser .searchWells(parseResult.getParsedWellKeys());
        result[0] = BROWSE_WELL_VOLUMES;
      }
    });
    return result[0];
  }
  
  private void resetSearchFields()
  {
    _plateNumber = null;
    _wellName = null;
    _plateWellList = null;
  }

}
