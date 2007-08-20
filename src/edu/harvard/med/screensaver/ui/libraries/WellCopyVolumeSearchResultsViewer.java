// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.ui.control.LibrariesController;
import edu.harvard.med.screensaver.ui.control.ScreensController;
import edu.harvard.med.screensaver.ui.searchresults.SearchResults;
import edu.harvard.med.screensaver.ui.searchresults.SearchResultsViewer;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;

/**
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class WellCopyVolumeSearchResultsViewer extends SearchResultsViewer<WellCopyVolume>
{
  private LibrariesController _librariesController;
  private ScreensController _screensController;
  private WellVolumeSearchResults _wellVolumeSearchResults;
  private GenericEntityDAO _dao;


  public void setLibrariesController(LibrariesController librariesController)
  {
    _librariesController = librariesController;
  }

  public void setScreensController(ScreensController screensController)
  {
    _screensController = screensController;
  }

  public void setGenericEntityDao(GenericEntityDAO dao) 
  {
    _dao = dao;
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public void setSearchResults(SearchResults<WellCopyVolume> wellCopyVolumeSearchResults)
  {
    super.setSearchResults(wellCopyVolumeSearchResults);
    
    MultiMap wellKey2WellCopyVolumes = new MultiValueMap();
    for (WellCopyVolume wellCopyVolume : wellCopyVolumeSearchResults.getContents()) {
      wellKey2WellCopyVolumes.put(wellCopyVolume.getWell().getWellKey(),
                                  wellCopyVolume);
    }

    List<WellVolume> wellVolumes = new ArrayList<WellVolume>();
    for (Iterator iter = wellKey2WellCopyVolumes.keySet().iterator(); iter.hasNext(); ) {
      List<WellCopyVolume> wellCopyVolumes = (List<WellCopyVolume>) wellKey2WellCopyVolumes.get(iter.next());
      wellVolumes.add(new WellVolume(wellCopyVolumes.get(0).getWell(),
                                     wellCopyVolumes));
    }
    _wellVolumeSearchResults = new WellVolumeSearchResults(wellVolumes, _librariesController, _screensController, _dao);
  }

  public WellVolumeSearchResults getWellVolumeSearchResults()
  {
    return _wellVolumeSearchResults;
  }
}
