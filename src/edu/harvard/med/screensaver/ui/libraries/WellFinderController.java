// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.ui.AbstractController;
import edu.harvard.med.screensaver.util.StringUtils;

public class WellFinderController extends AbstractController
{
  
  private static final Logger log = Logger.getLogger(WellFinderController.class);
  
  
  // private instance fields
  
  private Pattern _plateNumberPattern = Pattern.compile("^\\s*((PL)[-_]?)?(\\d+)\\s*$");
  private Pattern _wellNamePattern = Pattern.compile("^\\s*([A-Za-z]\\d\\d?)\\s*$");
  private String _plateNumber;
  private String _wellName;
  
  private DAO _dao;
  private WellViewerController _wellViewerController;
  private WellSearchResultsController _wellSearchResultsController;
  
  
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

  public DAO getDao()
  {
    return _dao;
  }

  public void setDao(DAO dao)
  {
    _dao = dao;
  }

  public WellSearchResultsController getWellSearchResults()
  {
    return _wellSearchResultsController;
  }

  public void setWellSearchResults(WellSearchResultsController wellSearchResultsController)
  {
    _wellSearchResultsController = wellSearchResultsController;
  }

  public WellViewerController getWellViewer()
  {
    return _wellViewerController;
  }

  public void setWellViewer(WellViewerController wellViewerController)
  {
    _wellViewerController = wellViewerController;
  }

  public String findWell()
  {
    Well well = lookupWell();
    if (well == null) {
      return ERROR_ACTION_RESULT;
    }
    _wellViewerController.setWell(well);
    return "showWell";
  }
  
  
  // private instance methods
  
  private Well lookupWell()
  {
    Integer plateNumber = parsePlateNumber();
    String wellName = parseWellName();
    if (plateNumber == null || wellName == null) {
      return null;
    }
    Well well = _dao.findWell(plateNumber, wellName);
    if (well == null) {
      log.error("didnt find well in the database: " + plateNumber + wellName);      
    }
    return well;
  }
  
  private Integer parsePlateNumber()
  {
    Matcher matcher = _plateNumberPattern.matcher(_plateNumber);
    if (matcher.matches()) {
      String plateNumber = matcher.group(3);
      return Integer.parseInt(plateNumber);
    }
    else {
      log.error("plate number didn't match pattern: " + _plateNumber);
      return null;
    }
  }
  
  private String parseWellName()
  {
    Matcher matcher = _wellNamePattern.matcher(_wellName);
    if (matcher.matches()) {
      String wellName = matcher.group(1);
      if (wellName.length() == 2) {
        wellName = wellName.charAt(0) + "0" + wellName.charAt(1);
      }
      wellName = StringUtils.capitalize(wellName);
      return wellName;
    }
    else {
      log.error("well name didn't match pattern: " + _wellName);
      return null;
    }
  }
}
