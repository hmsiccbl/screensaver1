// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

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
import edu.harvard.med.screensaver.ui.AbstractController;
import edu.harvard.med.screensaver.ui.SearchResults;
import edu.harvard.med.screensaver.ui.SearchResultsRegistryController;
import edu.harvard.med.screensaver.util.StringUtils;

/**
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class WellFinderController extends AbstractController
{
  
  private static final Logger log = Logger.getLogger(WellFinderController.class);
  
  
  // private instance fields
  
  private Pattern _plateNumberPattern = Pattern.compile("^\\s*((PL)[-_]?)?(\\d+)\\s*$");
  private Pattern _wellNamePattern = Pattern.compile("^\\s*([A-Ha-h]([0-9]|[01][0-9]|2[0-4]))\\s*$");
  private String _plateNumber;
  private String _wellName;
  private String _plateWellList;
  
  private DAO _dao;
  private LibraryViewerController _libraryViewerController;
  private WellViewerController _wellViewerController;
  private SearchResultsRegistryController _searchResultsRegistry;
  private GeneViewerController _geneViewerController;
  private CompoundViewerController _compoundViewerController;
  
  
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

  public DAO getDao()
  {
    return _dao;
  }

  public void setDao(DAO dao)
  {
    _dao = dao;
  }

  public LibraryViewerController getLibraryViewer()
  {
    return _libraryViewerController;
  }

  public void setLibraryViewer(LibraryViewerController libraryViewer)
  {
    _libraryViewerController = libraryViewer;
  }

  public WellViewerController getWellViewer()
  {
    return _wellViewerController;
  }

  public void setWellViewer(WellViewerController wellViewerController)
  {
    _wellViewerController = wellViewerController;
  }

  public CompoundViewerController getCompoundViewer()
  {
    return _compoundViewerController;
  }

  public void setCompoundViewer(CompoundViewerController compoundViewer)
  {
    _compoundViewerController = compoundViewer;
  }

  public GeneViewerController getGeneViewer()
  {
    return _geneViewerController;
  }

  public void setGeneViewer(GeneViewerController geneViewer)
  {
    _geneViewerController = geneViewer;
  }

  public SearchResultsRegistryController getSearchResultsRegistry()
  {
    return _searchResultsRegistry;
  }

  public void setSearchResultsRegistry(SearchResultsRegistryController searchResultsRegistry)
  {
    _searchResultsRegistry = searchResultsRegistry;
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
  
  public String findWells()
  {
    List<Well> wells = lookupWellsFromPlateWellList();
//    if (wells.size() == 1) {
//      _wellViewerController.setWell(wells.get(0));
//      return "showWell";
//    }
    SearchResults<Well> searchResults = new WellSearchResults(
      wells,
      _libraryViewerController,
      _wellViewerController,
      _compoundViewerController,
      _geneViewerController);
    _searchResultsRegistry.registerSearchResults(Well.class, this, searchResults);
    _searchResultsRegistry.setCurrentSearchType(Well.class);
    return "goWellSearchResults";
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
      showMessage("unexpectedErrorReadingPlateWellList", "searchResults");
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
      showMessage("noSuchWell", new Object [] { plateNumber, wellName }, "searchResults");
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
      showMessage("invalidPlateNumber", new Object [] { plateNumber });
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
      showMessage("invalidWellName", new Object [] { wellName });
      return null;
    }
  }
}
