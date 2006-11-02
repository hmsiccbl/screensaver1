// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.view.libraries;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.control.LibrariesController;
import edu.harvard.med.screensaver.ui.searchresults.WellSearchResults;
import edu.harvard.med.screensaver.util.StringUtils;

/**
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class WellFinder extends AbstractBackingBean
{
  
  // private static final fields
  
  private static final Logger log = Logger.getLogger(WellFinder.class);
  private static final Pattern _plateNumberPattern =
    Pattern.compile("^\\s*((PL)[-_]?)?(\\d+)\\s*$");
  private static final Pattern _wellNamePattern =
    Pattern.compile("^\\s*([A-Ha-h]([0-9]|[01][0-9]|2[0-4]))\\s*$");
  
  
  // private instance fields
  
  private DAO _dao;
  private LibrariesController _librariesController;
  private String _plateNumber;
  private String _wellName;
  private String _plateWellList;
  
  
  // public instance methods

  public DAO getDao()
  {
    return _dao;
  }

  public void setDao(DAO dao)
  {
    _dao = dao;
  }
  
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

  public String findWell()
  {
    Well well = lookupWell();
    if (well == null) {
      return ERROR_ACTION_RESULT;
    }
    return _librariesController.viewWell(well, null);
  }
  
  public String findWells()
  {
    List<Well> wells = lookupWellsFromPlateWellList();
    WellSearchResults searchResults =
      new edu.harvard.med.screensaver.ui.searchresults.WellSearchResults(
        wells,
        _librariesController);
    return _librariesController.viewWellSearchResults(searchResults);
  }
  
  
  // private instance methods
  
  private List<Well> lookupWellsFromPlateWellList()
  {
    List<Well> wells = new ArrayList<Well>();
    BufferedReader plateWellListReader = new BufferedReader(new StringReader(_plateWellList));
    try {
      for (
        String line = plateWellListReader.readLine();
        line != null;
        line = plateWellListReader.readLine()) {
        
        String [] tokens = line.split("[\\s;,]+");
        if (tokens.length == 0) {
          continue;
        }
        
        Integer plateNumber = parsePlateNumber(tokens[0]);
        for (int i = 1; i < tokens.length; i ++) {
          String wellName = parseWellName(tokens[i]);
          if (plateNumber == null || wellName == null) {
            continue;
          }
          Well well = findWell(plateNumber, wellName);
          if (well != null) {
            wells.add(well);
          }
        }
      }
    }
    catch (IOException e) {
      showMessage("libraries.unexpectedErrorReadingPlateWellList", "searchResults");
    }
    return wells;
  }
  
  private Well lookupWell()
  {
    Integer plateNumber = parsePlateNumber(_plateNumber);
    String wellName = parseWellName(_wellName);
    if (plateNumber == null || wellName == null) {
      return null;
    }
    return findWell(plateNumber, wellName);
  }

  private Well findWell(Integer plateNumber, String wellName) {
    Well well = _dao.findWell(plateNumber, wellName);
    if (well == null) {
      showMessage("libraries.noSuchWell", "searchResults", plateNumber.toString(), wellName);
    }
    return well;
  }
  
  private Integer parsePlateNumber(String plateNumber)
  {
    Matcher matcher = _plateNumberPattern.matcher(plateNumber);
    if (matcher.matches()) {
      plateNumber = matcher.group(3);
      return Integer.parseInt(plateNumber);
    }
    else {
      showMessage("libraries.invalidPlateNumber", plateNumber.toString());
      return null;
    }
  }
  
  private String parseWellName(String wellName)
  {
    Matcher matcher = _wellNamePattern.matcher(wellName);
    if (matcher.matches()) {
      wellName = matcher.group(1);
      if (wellName.length() == 2) {
        wellName = wellName.charAt(0) + "0" + wellName.charAt(1);
      }
      wellName = StringUtils.capitalize(wellName);
      return wellName;
    }
    else {
      showMessage("libraries.invalidWellName", wellName);
      return null;
    }
  }
}
