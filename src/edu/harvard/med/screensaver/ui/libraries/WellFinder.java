// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
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
import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.ui.searchresults.WellSearchResults;
import edu.harvard.med.screensaver.util.Pair;

import org.apache.log4j.Logger;

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
  @UIControllerMethod
  public String findWell()
  {
    return _wellViewer.viewWell(_plateWellListParser.lookupWell(_plateNumber, _wellName));
  }

  /**
   * Find the wells specified in the plate-well list, and go to the {@link WellSearchResultsViewer}
   * page.
   * @return the controller code for the next appropriate page
   */
  @UIControllerMethod
  public String findWells()
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

        if (foundWells.size() == 0) {
          result[0] = REDISPLAY_PAGE_ACTION_RESULT;
        }
        // show in well viewer, iff the user entered exactly 1 well (counting erroneous wells)
        else if (parseResult.getParsedWellKeys().size() == 1 && parseResult.getErrors().size() == 0) {
          result[0] = _wellViewer.viewWell(parseResult.getParsedWellKeys().first());
        }
        else {
          _wellsBrowser.searchWells(foundWells);
          result[0] = VIEW_WELL_SEARCH_RESULTS;
        }
      }
    });
    return result[0];
  }

  /**
   * Find the volumes for all copies of the wells specified in the plate-well
   * list, and go to the {@link WellSearchResultsViewer} page.
   *
   * @return the controller code for the next appropriate page
   */
  @UIControllerMethod
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
        result[0] = VIEW_WELL_VOLUME_SEARCH_RESULTS;
      }
    });
    return result[0];
  }
}
