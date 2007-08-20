// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.control.LibrariesController;

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

  private static final ScreensaverUserRole ADMIN_ROLE = ScreensaverUserRole.LIBRARIES_ADMIN;
  
  
  // private instance fields
  
  private LibrariesController _librariesController;
  private String _plateNumber;
  private String _wellName;
  private String _plateWellList;
  
  
  // public instance methods
  
  public LibrariesController getLibrariesController()
  {
    return _librariesController;
  }
  
  public void setLibrariesController(LibrariesController librariesController)
  {
    _librariesController = librariesController;
  }

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
  
  @Override
  protected ScreensaverUserRole getEditableAdminRole()
  {
    return ADMIN_ROLE;
  }

  /**
   * Find the well with the specified plate number and well name, and go to the appropriate next
   * page depending on the result.
   * @return the control code for the appropriate next page
   */
  public String findWell()
  {
    return _librariesController.findWell(_plateNumber, _wellName);
  }
  
  /**
   * Find the wells specified in the plate-well list, and go to the {@link WellSearchResultsViewer}
   * page.
   * @return the controler code for the next appropriate page
   */
  public String findWells()
  {
    return _librariesController.findWells(_plateWellList);
  }

  /**
   * Find the volumes for all copies of the wells specified in the plate-well
   * list, and go to the {@link WellSearchResultsViewer} page.
   * 
   * @return the controler code for the next appropriate page
   */
  public String findWellVolumes()
  {
    return _librariesController.findWellVolumes(_plateWellList);
  }
}
