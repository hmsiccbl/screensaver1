// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.activities;

import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryScreeningStatus;
import edu.harvard.med.screensaver.model.screens.PlatesUsed;

public class LibraryAndPlatesUsed
{
  private LibrariesDAO _librariesDAO;
  private Library _library;
  private PlatesUsed _platesUsed;
  private Integer _startPlate;
  private Integer _endPlate;

  public LibraryAndPlatesUsed(LibrariesDAO librariesDao, PlatesUsed platesUsed)
  {
    _librariesDAO = librariesDao;
    setPlatesUsed(platesUsed);
  }

  public Library getLibrary()
  {
    if (!!!_platesUsed.getStartPlate().equals(_startPlate) || !!!_platesUsed.getEndPlate().equals(_endPlate)) {
      _startPlate = _platesUsed.getStartPlate();
      _endPlate = _platesUsed.getEndPlate();
      _library = _librariesDAO.findLibraryWithPlate(_platesUsed.getStartPlate());
    }
    return _library;
  }

  public PlatesUsed getPlatesUsed()
  {
    return _platesUsed;
  }

  public void setPlatesUsed(PlatesUsed platesUsed)
  {
    _platesUsed = platesUsed;
    _library = null;
  }

  public String getAdminLibraryWarning()
  {
    Library library = getLibrary();
    if (library != null && library.getScreeningStatus() != LibraryScreeningStatus.ALLOWED) {
      if (!_platesUsed.getLibraryScreening().getScreen().getLibrariesPermitted().contains(library)) {
        return library.getScreeningStatus().getValue();
      }
    }
    return null;
  }
}
